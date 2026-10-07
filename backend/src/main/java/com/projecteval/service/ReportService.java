package com.projecteval.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.projecteval.dto.evaluation.EvaluationDetailsResponse;
import com.projecteval.dto.evaluation.AutomatedTestResultDto;
import com.projecteval.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final EvaluationService evaluationService;

    public byte[] generatePdfReport(Long projectId) {
        EvaluationDetailsResponse eval = evaluationService.getEvaluationDetails(projectId);
        if (eval == null || eval.getProject() == null) {
            throw new ResourceNotFoundException("Evaluation data not found for project: " + projectId);
        }

        Document document = new Document(PageSize.A4, 36, 36, 40, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Document Header
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new Color(30, 41, 59));
            Paragraph title = new Paragraph("PROJECT EVALUATION REPORT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(100, 116, 139));
            Paragraph sub = new Paragraph("ProjectEval - Automated Project Evaluation & Testing Platform", subFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(20);
            document.add(sub);

            // Project & Student Metadata Table
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(15);

            addInfoCell(infoTable, "Project Title:", eval.getProject().getTitle());
            addInfoCell(infoTable, "Student Name:", eval.getProject().getStudentName());
            addInfoCell(infoTable, "Technology Stack:", eval.getProject().getTechnologyStack() != null ? eval.getProject().getTechnologyStack() : "N/A");
            addInfoCell(infoTable, "Department:", eval.getProject().getStudentDepartment());
            addInfoCell(infoTable, "Submission Date:", eval.getProject().getSubmittedAt() != null ?
                    eval.getProject().getSubmittedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A");
            addInfoCell(infoTable, "Evaluator:", eval.getEvaluatorName() != null ? eval.getEvaluatorName() : "Automated System");

            document.add(infoTable);

            // Scorecard Summary Box
            PdfPTable scoreTable = new PdfPTable(4);
            scoreTable.setWidthPercentage(100);
            scoreTable.setSpacingAfter(20);

            double auto = eval.getAutomatedScore() != null ? eval.getAutomatedScore() : 0.0;
            double manual = eval.getManualTotal() != null ? eval.getManualTotal() : 0.0;
            double finalScore = eval.getFinalScore() != null ? eval.getFinalScore() : auto + manual;
            String grade = eval.getGrade() != null ? eval.getGrade() : "Pending";

            addMetricBox(scoreTable, "AUTOMATED (85)", String.format("%.1f", auto), new Color(238, 242, 255));
            addMetricBox(scoreTable, "MANUAL (15)", String.format("%.1f", manual), new Color(240, 253, 244));
            addMetricBox(scoreTable, "FINAL SCORE (100)", String.format("%.1f", finalScore), new Color(254, 243, 199));
            addMetricBox(scoreTable, "FINAL GRADE", grade, new Color(236, 253, 245));

            document.add(scoreTable);

            // Section: Automated Evaluation Breakdown
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(15, 23, 42));
            Paragraph autoSection = new Paragraph("Level 1: Automated Test Results Breakdown", sectionFont);
            autoSection.setSpacingBefore(10);
            autoSection.setSpacingAfter(10);
            document.add(autoSection);

            PdfPTable testTable = new PdfPTable(new float[]{3.5f, 1.8f, 1.0f, 1.0f});
            testTable.setWidthPercentage(100);
            testTable.setSpacingAfter(15);

            addTableHeader(testTable, "Test Name");
            addTableHeader(testTable, "Category");
            addTableHeader(testTable, "Status");
            addTableHeader(testTable, "Marks");

            if (eval.getTestResults() != null) {
                for (AutomatedTestResultDto t : eval.getTestResults()) {
                    PdfPCell nameCell = new PdfPCell(new Phrase(t.getTestName(), FontFactory.getFont(FontFactory.HELVETICA, 8)));
                    nameCell.setPadding(5);
                    testTable.addCell(nameCell);

                    PdfPCell catCell = new PdfPCell(new Phrase(t.getCategory(), FontFactory.getFont(FontFactory.HELVETICA, 8)));
                    catCell.setPadding(5);
                    testTable.addCell(catCell);

                    PdfPCell statusCell = new PdfPCell(new Phrase(t.getStatus().name(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8,
                            "PASS".equals(t.getStatus().name()) ? new Color(22, 101, 52) : new Color(185, 28, 28))));
                    statusCell.setPadding(5);
                    testTable.addCell(statusCell);

                    PdfPCell markCell = new PdfPCell(new Phrase(t.getMarksAwarded() + "/" + t.getMaxMarks(), FontFactory.getFont(FontFactory.HELVETICA, 8)));
                    markCell.setPadding(5);
                    testTable.addCell(markCell);
                }
            }
            document.add(testTable);

            // Section: Manual Evaluation
            Paragraph manualSection = new Paragraph("Level 2: Manual Faculty Evaluation", sectionFont);
            manualSection.setSpacingBefore(10);
            manualSection.setSpacingAfter(10);
            document.add(manualSection);

            PdfPTable manualTable = new PdfPTable(new float[]{3.0f, 1.5f});
            manualTable.setWidthPercentage(60);
            manualTable.setHorizontalAlignment(Element.ALIGN_LEFT);
            manualTable.setSpacingAfter(15);

            addManualRow(manualTable, "Innovation & Originality", (eval.getInnovationMarks() != null ? eval.getInnovationMarks() : 0.0) + " / 3.0");
            addManualRow(manualTable, "Technical Implementation & Rigor", (eval.getTechnicalMarks() != null ? eval.getTechnicalMarks() : 0.0) + " / 4.0");
            addManualRow(manualTable, "Documentation Quality", (eval.getDocumentationMarks() != null ? eval.getDocumentationMarks() : 0.0) + " / 3.0");
            addManualRow(manualTable, "Presentation & Code Readability", (eval.getPresentationMarks() != null ? eval.getPresentationMarks() : 0.0) + " / 2.0");
            addManualRow(manualTable, "Final Outcome & Working Demo", (eval.getOutcomeMarks() != null ? eval.getOutcomeMarks() : 0.0) + " / 3.0");
            addManualRow(manualTable, "Total Manual Marks", manual + " / 15.0");

            document.add(manualTable);

            // Evaluator Remarks
            if (eval.getComments() != null && !eval.getComments().isBlank()) {
                Paragraph remarksHeader = new Paragraph("Evaluator Comments & Observations:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11));
                remarksHeader.setSpacingBefore(5);
                document.add(remarksHeader);

                Paragraph remarksBody = new Paragraph(eval.getComments(), FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(51, 65, 85)));
                remarksBody.setSpacingAfter(15);
                document.add(remarksBody);
            }

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error rendering PDF report: " + e.getMessage(), e);
        }

        return out.toByteArray();
    }

    private void addInfoCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(4);
        cell.addElement(new Phrase(label + " " + (value != null ? value : ""), FontFactory.getFont(FontFactory.HELVETICA, 9)));
        table.addCell(cell);
    }

    private void addMetricBox(PdfPTable table, String title, String value, Color bg) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(bg);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph p1 = new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new Color(71, 85, 105)));
        p1.setAlignment(Element.ALIGN_CENTER);
        Paragraph p2 = new Paragraph(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(15, 23, 42)));
        p2.setAlignment(Element.ALIGN_CENTER);

        cell.addElement(p1);
        cell.addElement(p2);
        table.addCell(cell);
    }

    private void addTableHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE)));
        cell.setBackgroundColor(new Color(30, 41, 59));
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addManualRow(PdfPTable table, String label, String value) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, FontFactory.getFont(FontFactory.HELVETICA, 9)));
        c1.setPadding(5);
        PdfPCell c2 = new PdfPCell(new Phrase(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9)));
        c2.setPadding(5);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(c1);
        table.addCell(c2);
    }
}
