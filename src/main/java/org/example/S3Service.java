package org.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.core.sync.RequestBody;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


@Slf4j
@Service
public class S3Service {

    private final S3Client s3Client;
    private final String bucketName;

    // Threshold size 4 using multipart upload.
    private static final long MULTIPART_UPLOAD_THRESHOLD = 15 * 1024 * 1024; // 15MB

    //constructor
    public S3Service(S3Client s3Client, @Value("${aws.s3.uploadBucketName}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    // upload file, either as a single upload or multipart upload based on file size
    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        long startTime = System.currentTimeMillis(); // Record start time

        try {
            if (file.getSize() > MULTIPART_UPLOAD_THRESHOLD) {
                // Use multipart upload for large file
                log.info("Using multipart upload for large file");

                String url = multipartUpload(file, fileName);
                long endTime = System.currentTimeMillis(); // Record the end time
                log.info("Multipart upload completed in {} ms", (endTime - startTime));
                return url;
            } else {
                // Use single upload for small file
                log.info("Using single upload for small file");

                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileName)
                        .build();
                s3Client.putObject(putObjectRequest,
                        RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
                long endTime = System.currentTimeMillis(); // Record end time
                log.info("Single upload completed in {} ms", (endTime - startTime));
                return s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(fileName)).toExternalForm();
            }
        } catch (S3Exception e) {
            log.error("Failed to upload file to S3", e);
            throw new IOException("Failed to upload file to S3", e);
        }
    }

    private String multipartUpload(MultipartFile file, String fileName) throws IOException {
        // create ExecutorService with fixed thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        try {
            // Create multipart upload request
            CreateMultipartUploadRequest createMultipartUploadRequest = CreateMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();
            CreateMultipartUploadResponse createMultipartUploadResponse = s3Client.createMultipartUpload(createMultipartUploadRequest);
            String uploadId = createMultipartUploadResponse.uploadId();

            // Upload parts
            List<CompletedPart> completedParts = new ArrayList<>();
            long fileSize = file.getSize();
            long partSize = 15 * 1024 * 1024; //15MB.
            long filePosition = 0; // start at zero

            // List to store futures 4 concurrent execution
            List<Future<CompletedPart>> futures = new ArrayList<>();

            for (int i = 1; filePosition < fileSize; i++) { // Loop through file & upload parts until the whole file is uploaded.
                final int partNumber = i;
                final long currPartSize = Math.min(partSize, fileSize - filePosition); // set size of next part to upload. make sure it's less than 5mb and not more than remaining file size.

                // Submit a callable task to upload each part concurrently
                futures.add(executorService.submit(() -> {
                    // Create UploadPartRequest with the bucket, key, upload ID, and part number.
                    UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .uploadId(uploadId)
                            .partNumber(partNumber)
                            .build();


                    // Upload part & get eTag
                    String eTag = s3Client.uploadPart(uploadPartRequest,
                            RequestBody.fromInputStream(file.getInputStream(), currPartSize)).eTag();

                    // Return completed part info
                    return CompletedPart.builder()
                            .partNumber(partNumber)
                            .eTag(eTag)
                            .build();
                }));

                filePosition += partSize; // go 2 next part of file
            }

            // get completed parts from futures
            for (Future<CompletedPart> future : futures) {
                try {
                    // Add each completed part to the list
                    completedParts.add(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error uploading part", e);
                    throw new IOException("Error uploading part", e);
                }
            }

            // do multipart upload
            CompleteMultipartUploadRequest completeMultipartUploadRequest = CompleteMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .uploadId(uploadId)
                    .multipartUpload(CompletedMultipartUpload.builder().parts(completedParts).build())
                    .build();
            s3Client.completeMultipartUpload(completeMultipartUploadRequest);
            return s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(fileName)).toExternalForm();

        } catch (S3Exception e) {
            log.error("Failed to complete multipart upload to S3", e);

            throw new IOException("Failed to complete multipart upload to S3", e);
        }
        finally {
            // Shutdown the executor service
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException ex) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
//package org.example;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import software.amazon.awssdk.services.s3.S3Client;
//import software.amazon.awssdk.services.s3.model.*;
//import software.amazon.awssdk.core.sync.RequestBody;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import lombok.extern.slf4j.Slf4j;
//@Slf4j
//@Service
//public class S3Service {
//
//    private static final Logger logger = Logger.getLogger(S3Service.class.getName());
//
//    private final S3Client s3Client;
//    private final String bucketName;
//
//    // Threshold size in bytes for using multipart upload.
//    private static final long MULTIPART_UPLOAD_THRESHOLD = 5 * 1024 * 1024; // 5MB
//
//    public S3Service(S3Client s3Client, @Value("${aws.s3.uploadBucketName}") String bucketName) {
//        this.s3Client = s3Client;
//        this.bucketName = bucketName;
//    }
//
//    public String uploadFile(MultipartFile file) throws IOException {
//        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
//        long startTime = System.currentTimeMillis(); // Record start time
//
//        try {
//            if (file.getSize() > MULTIPART_UPLOAD_THRESHOLD) {
//                // Use multipart upload for large files
//                String url = multipartUpload(file, fileName);
//                long endTime = System.currentTimeMillis(); // Record the end time
//                log.info("Multipart upload completed in {} ms", (endTime - startTime));
//                return url;
//
//            } else {
//                // Use single upload for small files
//                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
//                        .bucket(bucketName)
//                        .key(fileName)
//                        .build();
//                s3Client.putObject(putObjectRequest,
//                        RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
//                return s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(fileName)).toExternalForm();
//            }
//        } catch (S3Exception e) {
//            logger.log(Level.SEVERE, "Failed to upload file to S3", e);
//            throw new IOException("Failed to upload file to S3", e);
//        }
//    }
//
//    private String multipartUpload(MultipartFile file, String fileName) throws IOException {
//        try {
//            // Create a multipart upload request
//            CreateMultipartUploadRequest createMultipartUploadRequest = CreateMultipartUploadRequest.builder()
//                    .bucket(bucketName)
//                    .key(fileName)
//                    .build();
//            CreateMultipartUploadResponse createMultipartUploadResponse = s3Client.createMultipartUpload(createMultipartUploadRequest);
//            String uploadId = createMultipartUploadResponse.uploadId();
//
//            // Upload parts
//            List<CompletedPart> completedParts = new ArrayList<>();
//            long contentLength = file.getSize();
//            long partSize = 5 * 1024 * 1024; // Set part size to 5MB.
//
//            long filePosition = 0;
//            for (int i = 1; filePosition < contentLength; i++) {
//                partSize = Math.min(partSize, contentLength - filePosition);
//                UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
//                        .bucket(bucketName)
//                        .key(fileName)
//                        .uploadId(uploadId)
//                        .partNumber(i)
//                        .build();
//                completedParts.add(CompletedPart.builder()
//                        .partNumber(i)
//                        .eTag(s3Client.uploadPart(uploadPartRequest,
//                                RequestBody.fromInputStream(file.getInputStream(), partSize)).eTag())
//                        .build());
//                filePosition += partSize;
//            }
//
//            // Complete the multipart upload
//            CompleteMultipartUploadRequest completeMultipartUploadRequest = CompleteMultipartUploadRequest.builder()
//                    .bucket(bucketName)
//                    .key(fileName)
//                    .uploadId(uploadId)
//                    .multipartUpload(CompletedMultipartUpload.builder().parts(completedParts).build())
//                    .build();
//            s3Client.completeMultipartUpload(completeMultipartUploadRequest);
//            return s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(fileName)).toExternalForm();
//
//        } catch (S3Exception e) {
//            logger.log(Level.SEVERE, "Failed to complete multipart upload to S3", e);
//            throw new IOException("Failed to complete multipart upload to S3", e);
//        }
//    }
//}