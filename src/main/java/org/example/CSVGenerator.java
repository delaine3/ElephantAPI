package org.example;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

@Component
public class CSVGenerator {

    public static ByteArrayInputStream toCSV(List<Elephant> elephants) {
       final CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), format);) {

            List<String> headers = Arrays.asList("ElephantID", "Name", "Age", "Species", "Location", "Weight", "Height", "HealthStatus", "LastHealthCheckDate", "Birthday");
            csvPrinter.printRecord(headers);

            for (Elephant elephant : elephants) {
                List<String> data = Arrays.asList(
                        String.valueOf(elephant.getId()),
                        elephant.getName(),
                        String.valueOf(elephant.getAge()),
                        elephant.getSpecies(),
                        elephant.getLocation(),
                        String.valueOf(elephant.getWeight()),
                        String.valueOf(elephant.getHeight()),
                        elephant.getHealthStatus(),
                        String.valueOf(elephant.getLastHealthCheckDate()),
                        String.valueOf(elephant.getBirthday())
                );

                csvPrinter.printRecord(data);
            }

            csvPrinter.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("fail to import data to CSV file: " + e.getMessage());
        }
    }
    public void generateCSV(List<Elephant> elephants, PrintWriter writer) {
        writer.write("ElephantID,Name,Age,Species,Location,Weight,Height,HealthStatus,LastHealthCheckDate,Birthday\n");
        for (Elephant elephant : elephants) {
            writer.write(String.format("%d,%s,%d,%s,%s,%.2f,%.2f,%s,%s,%s\n",
                    elephant.getId(),
                    elephant.getName(),
                    elephant.getAge(),
                    elephant.getSpecies(),
                    elephant.getLocation(),
                    elephant.getWeight(),
                    elephant.getHeight(),
                    elephant.getHealthStatus(),
                    elephant.getLastHealthCheckDate(),
                    elephant.getBirthday()));
        }
    }
}