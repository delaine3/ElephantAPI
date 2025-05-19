package org.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.Body;
import software.amazon.awssdk.services.sesv2.model.Content;
import software.amazon.awssdk.services.sesv2.model.Destination;
import software.amazon.awssdk.services.sesv2.model.EmailContent;
import software.amazon.awssdk.services.sesv2.model.Message;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SesV2Exception;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class EmailService {
    private static final Logger logger = Logger.getLogger(EmailService.class.getName());
    @Value("${aws.accessKeyId}")
    private String accessKeyId;

    @Value("${aws.secretAccessKey}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    private String wrapInHtmlTags(String title, String subtitle, String content) {
        String htmlTemplate = """
        <html>
        <head>
        <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        h1 { color: #333333; }
        h2 { color: #555555; }
        p { color: #777777; }
 
        </style>
        </head>
        <body>
            <h1>%s</h1>
            <img src="https://trunk-storage22.s3.amazonaws.com/image-5.png"/>
            <h2>%s</h2>
            <p>%s</p>
        </body>
        </html>
    """;

        return String.format(htmlTemplate, title, subtitle, content);
    }

    public void send(String sender, String recipient, String subject, String emailBody) {

        String bodyHTML = wrapInHtmlTags("ELephant", "Elephant 2", emailBody);


        AwsBasicCredentials creds = AwsBasicCredentials.builder()
                .accessKeyId(accessKeyId)
                .secretAccessKey(secretKey)
                .build();
        SesV2Client sesv2 = SesV2Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(creds))
                .build();
        Destination destination = Destination.builder()
                .toAddresses(recipient)
                .build();

        Content content = Content.builder()
                .data(bodyHTML)
                .build();

        Content sub = Content.builder()
                .data(subject)
                .build();

        Body body = Body.builder()
                .html(content)
                .build();

        Message msg = Message.builder()
                .subject(sub)
                .body(body)
                .build();

        EmailContent emailContent = EmailContent.builder()
                .simple(msg)
                .build();

        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .destination(destination)
                .content(emailContent)
                .fromEmailAddress(sender)
                .build();

        try {
            logger.info("Attempting to send an email through Amazon SES using the AWS SDK for Java...");
            sesv2.sendEmail(emailRequest);
            logger.info("Email was sent successfully.");
        } catch (SesV2Exception e) {
            logger.log(Level.SEVERE, "The email was not sent. Error message: {0}", e.awsErrorDetails().errorMessage());
        }
    }

    private String readHtmlTemplate(String templatePath) {
        try {
            ClassPathResource resource = new ClassPathResource(templatePath);
            return new String(Files.readAllBytes(Paths.get(resource.getURI())));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
