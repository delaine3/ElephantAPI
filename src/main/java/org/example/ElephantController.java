package org.example;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;

@Slf4j
@RestController
@RequestMapping("/api/elephants")
@CrossOrigin(origins = {
    "http://localhost:3000",
    "http://192.168.1.3:3000"
})
public class ElephantController {
    @Autowired
    private CSVGenerator csvGenerator;
    @Autowired
    private PdfGenerator pdfGenerator;
    @Autowired
    private ElephantRepository elephantRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private S3Service s3Service;



@Autowired
private DataSource dataSource;

@PostConstruct
public void logDataSourceDetails() {
    System.out.println("Connected to: " + dataSource);
}

    @PostMapping("/test-error")
    public String triggerException() {
        throw new RuntimeException("Test error");
    }
 @GetMapping("/")
    public String home() {
        return "The backend is running!";
    }
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = s3Service.uploadFile(file);
            log.info("File upload succeeded");
            return ResponseEntity.ok(fileUrl);
        } catch (IOException e) {
            log.error("File upload failed", e);
            return ResponseEntity.status(500).body("File upload failed: " + e.getMessage());
        }
    }

    @PostMapping("/send-email")
    public ResponseEntity<String> sendEmail(@RequestParam String to, @RequestParam String subject, @RequestParam String body) {
        log.info("Sending email to: {}", to);
        try {
            emailService.send("support@arterialjs.org", to, subject, body);
            log.info("Email sent successfully to: {}", to);
            return ResponseEntity.ok("Email sent successfully!");
        } catch (Exception e) {
            log.error("Email sending failed", e);
            return ResponseEntity.status(500).body("Email sending failed: " + e.getMessage());
        }
    }

  @GetMapping("/{id}")
public ResponseEntity<Elephant> getElephant(@PathVariable int id) {
    log.info("Fetching elephant with ID: {}", id);

    try {
        Elephant elephant = elephantRepository.getElephant(id);
        if (elephant != null) {
            log.info("Fetched elephant: {}", elephant);
            return ResponseEntity.ok(elephant);
        } else {
            log.warn("Elephant with ID {} not found.", id);
            return ResponseEntity.status(404).body(null);
        }
    } catch (Exception e) {
        log.error("Failed to fetch elephant with ID: " + id, e);
        return ResponseEntity.status(500).body(null);
    }
}


    // private Map<String, Object> getOktaClaims(Authentication authentication) {
    //     Map<String, Object> claims = new HashMap<>();
    //         Jwt jwt = (Jwt) authentication.getPrincipal();
    //         claims = jwt.getClaims();

    //     return claims;
    // }
    @PostMapping
    public ResponseEntity<String> addElephant(@RequestBody Elephant elephant) {
        log.info("Adding elephant: {}", elephant);
        try {
            elephantRepository.addElephant(elephant);
            log.info("Added elephant: {}", elephant);
            return ResponseEntity.ok("Elephant added successfully!");
        } catch (Exception e) {
            log.error("Failed to add elephant", e);
            return ResponseEntity.status(500).body("Failed to add elephant: " + e.getMessage());
        }
    }

    @GetMapping("/headers")
    public ResponseEntity<List<String>> getHeaders() {
        log.info("Fetching elephant headers.");
        try {
            List<String> headers = Arrays.asList("ElephantID", "Name", "Age", "Species", "Location", "Weight", "Height", "HealthStatus", "LastHealthCheckDate", "Birthday");
            log.info("Fetched headers: {}", headers);
            return ResponseEntity.ok(headers);
        } catch (Exception e) {
            log.error("Failed to fetch headers", e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllElephants(@RequestBody DataTable dt) {
        log.info("Fetching all elephants with DataTable: {}", dt);
        try {
            int start = dt.getStart();
            int length = dt.getLength();
            String searchValue = dt.getSearchValue();
            String orderColumn = dt.getOrderColumn();
            String orderDir = dt.getOrderDir();

            List<Map<String, Object>> elephants = elephantRepository.getAllElephants(start, length, searchValue, orderColumn, orderDir);

            long totalRecords = elephantRepository.getTotalCount();
            long filteredRecords = elephantRepository.getFilteredCount(searchValue);

            Map<String, Object> response = new HashMap<>();
            response.put("draw", dt.getDraw());
            response.put("recordsTotal", totalRecords);
            response.put("recordsFiltered", filteredRecords);
            response.put("data", elephants);

            log.info("Fetched all elephants: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch elephants", e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/download-csv")
    public ResponseEntity<Resource> downloadCSV(@RequestBody DataTable dt) {
        log.info("Downloading CSV for DataTable: {}", dt);
        try {
            String filename = "elephants.csv";

            int start = dt.getStart();
            int length = dt.getLength();
            String searchValue = dt.getSearch().getValue();
            String orderColumn = dt.getColumns().get(dt.getOrder().get(0).getColumn()).getData();
            String orderDir = dt.getOrder().get(0).getDir();

            Map<String, Object> params = new HashMap<>();
            params.put("start", start);
            params.put("length", length);
            params.put("searchValue", searchValue);
            params.put("orderColumn", orderColumn);
            params.put("orderDir", orderDir);

            List<Elephant> elephants = elephantRepository.getElephantsForParams(params);

            InputStreamResource file = new InputStreamResource(CSVGenerator.toCSV(elephants));

            log.info("CSV file created successfully.");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/csv"))
                    .body(file);
        } catch (Exception e) {
            log.error("Failed to create CSV", e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/download-pdf")
    public ResponseEntity<byte[]> downloadElephants(@RequestBody DataTable dt) throws IOException {
        log.info("Downloading PDF for DataTable: {}", dt);
        try {
            int start = dt.getStart();
            int length = dt.getLength();
            String searchValue = dt.getSearch().getValue();
            String orderColumn = dt.getColumns().get(dt.getOrder().get(0).getColumn()).getData();
            String orderDir = dt.getOrder().get(0).getDir();

            Map<String, Object> params = new HashMap<>();
            params.put("start", start);
            params.put("length", length);
            params.put("searchValue", searchValue);
            params.put("orderColumn", orderColumn);
            params.put("orderDir", orderDir);

            List<Elephant> elephants = elephantRepository.getElephantsForParams(params);
            byte[] pdfContents = pdfGenerator.createPdf(elephants);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "elephants.pdf");

            log.info("PDF file created successfully.");
            return ResponseEntity.ok().headers(headers).body(pdfContents);
        } catch (IOException e) {
            log.error("Failed to create PDF", e);
            return ResponseEntity.status(500).body(null);
        }
    }
}
