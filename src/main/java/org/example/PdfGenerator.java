package org.example;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.util.Matrix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfGenerator {

    @Autowired
    private ElephantRepository elephantRepository;

    // Create PDF from list of elephants
    public byte[] createPdf(List<Elephant> elephants) throws IOException {
        PDDocument document = new PDDocument(); // New PDF doc
        PDPage page = new PDPage(PDRectangle.A4); // New page
        page.setRotation(90); // Rotate page to landscape

        document.addPage(page); // Add page

        PDPageContentStream contentStream = new PDPageContentStream(document, page); // Content stream
        contentStream.transform(new Matrix(0, 1, -1, 0, page.getMediaBox().getWidth(), 0)); // Rotate content
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12); // Set font
        contentStream.beginText(); // Begin text
        contentStream.newLineAtOffset(25, 550);
        contentStream.showText("Elephants Data"); // Title
        contentStream.endText();

        float margin = 25;
        float yStart = 520;
        float tableWidth = page.getMediaBox().getHeight() - 2 * margin;
        float yPosition = yStart;
        float headerRowHeight = 25f;
        float rowHeight = 20f;

        // Column widths and headers
        float[] columnWidths = {30, 50, 30, 60, 80, 60, 60, 80, 90, 70};
        String[] headers = {"ID", "Name", "Age", "Species", "Location", "Weight", "Height", "Health Status", "Most Recent Health Check", "Birthday"};

        // Draw table
        drawTable(contentStream, yPosition, margin, tableWidth, headers, elephants, columnWidths, headerRowHeight, rowHeight);

        contentStream.close();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        document.save(byteArrayOutputStream); // Save doc
        document.close();

        return byteArrayOutputStream.toByteArray(); // Return PDF bytes
    }

    private void drawTable(PDPageContentStream contentStream, float yPosition, float margin, float tableWidth, String[] headers, List<Elephant> elephants, float[] columnWidths, float headerRowHeight, float rowHeight) throws IOException {
        float yStart = yPosition;

        // Draw header row
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
        drawRow(contentStream, yPosition, margin, headers, columnWidths, headerRowHeight, true);
        yPosition -= headerRowHeight;

        // Draw data rows
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        for (Elephant elephant : elephants) {
            String[] data = {
                    String.valueOf(elephant.getId()),
                    elephant.getName(),
                    String.valueOf(elephant.getAge()),
                    elephant.getSpecies(),
                    elephant.getLocation(),
                    String.format("%.2f", elephant.getWeight()),
                    String.format("%.2f", elephant.getHeight()),
                    elephant.getHealthStatus(),
                    elephant.getLastHealthCheckDate().toString(),
                    elephant.getBirthday().toString()
            };
            drawRow(contentStream, yPosition, margin, data, columnWidths, rowHeight, false);
            yPosition -= rowHeight;
        }

        // Draw borders
        drawBorders(contentStream, yStart, yPosition, margin, tableWidth, columnWidths, headerRowHeight, rowHeight);
    }

    private void drawRow(PDPageContentStream contentStream, float y, float margin, String[] cells, float[] columnWidths, float rowHeight, boolean isHeader) throws IOException {
        float nextX = margin;
        float textYOffset = isHeader ? rowHeight - 10 : rowHeight - 15; // Adjust text position for header row
        for (int i = 0; i < cells.length; i++) {
            contentStream.addRect(nextX, y - rowHeight, columnWidths[i], rowHeight);
            contentStream.beginText();
            contentStream.newLineAtOffset(nextX + 2, y - rowHeight + textYOffset);

            // Wrap text
            List<String> lines = getWrappedText(cells[i], columnWidths[i] - 4, PDType1Font.HELVETICA, 10);
            for (String line : lines) {
                contentStream.showText(line);
                contentStream.newLineAtOffset(0, -10);
            }

            contentStream.endText();
            nextX += columnWidths[i];
        }
    }

    private List<String> getWrappedText(String text, float width, PDType1Font font, float fontSize) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String lineWithWord = currentLine + (currentLine.length() > 0 ? " " : "") + word;
            if (font.getStringWidth(lineWithWord) / 1000 * fontSize > width) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                currentLine.append((currentLine.length() > 0 ? " " : "")).append(word);
            }
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    private void drawBorders(PDPageContentStream contentStream, float yStart, float yPosition, float margin, float tableWidth, float[] columnWidths, float headerRowHeight, float rowHeight) throws IOException {
        float tableHeight = yStart - yPosition;
        int numRows = (int) (tableHeight / rowHeight);
        for (int i = 0; i <= numRows -12; i++) {
            float rowY = yStart - (i == 0 ? headerRowHeight : headerRowHeight + rowHeight * (i - 1));
            contentStream.moveTo(margin, rowY);
            contentStream.lineTo(margin + tableWidth, rowY);
        }
        for (int i = 0; i <= columnWidths.length; i++) {
            float colX = margin + sumArray(columnWidths, i);
            contentStream.moveTo(colX, yStart);
            contentStream.lineTo(colX, yStart - tableHeight);
        }
        contentStream.stroke();
    }

    private float sumArray(float[] array, int upto) {
        float sum = 0;
        for (int i = 0; i < upto; i++) {
            sum += array[i];
        }
        return sum;
    }
}
