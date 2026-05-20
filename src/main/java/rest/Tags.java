package rest;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;

import java.awt.Color;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.CheckedTemplate;
import io.quarkiverse.renarde.Controller;
import model.Tag;

public class Tags extends Controller {

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance index(List<Tag> tags);
        public static native TemplateInstance newTag();
        public static native TemplateInstance edit(Tag tag);
    }

    @Path("/admin/tags")
    public TemplateInstance index() {
        List<Tag> tags = Tag.list("deletedAt is null order by name");
        return Templates.index(tags);
    }

    @Path("/admin/tags/new")
    public TemplateInstance newTag() {
        return Templates.newTag();
    }

    @Path("/admin/tags")
    @POST
    @Transactional
    public void add(@RestForm @NotBlank String name, @RestForm String slug) {
        if (validationFailed()) {
            newTag();
        }
        Tag tag = new Tag();
        tag.name = name;
        String slugBase = (slug != null && !slug.isBlank()) ? slug : name;
        tag.slug = Tag.generateUniqueSlug(slugBase, null);
        tag.persist();
        index();
    }

    @Path("/admin/tags/{id}/edit")
    public TemplateInstance edit(@RestPath Long id) {
        Tag tag = Tag.find("id = ?1 and deletedAt is null", id).firstResult();
        if (tag == null) {
            index();
        }
        return Templates.edit(tag);
    }

    @Path("/admin/tags/{id}/update")
    @POST
    @Transactional
    public void update(@RestPath Long id, @RestForm @NotBlank String name, @RestForm String slug) {
        if (validationFailed()) {
            edit(id);
        }
        Tag tag = Tag.find("id = ?1 and deletedAt is null", id).firstResult();
        if (tag != null) {
            tag.name = name;
            String slugBase = (slug != null && !slug.isBlank()) ? slug : name;
            tag.slug = Tag.generateUniqueSlug(slugBase, tag.id);
            tag.persist();
        }
        index();
    }

    @Path("/admin/tags/{id}/delete")
    @POST
    @Transactional
    public void delete(@RestPath Long id) {
        Tag tag = Tag.find("id = ?1 and deletedAt is null", id).firstResult();
        if (tag != null) {
            tag.deletedAt = new Date();
            tag.persist();
        }
        index();
    }

    @Path("/admin/tags/export/pdf")
    @GET
    @Produces("application/pdf")
    public Response exportPdf() {
        List<Tag> tags = Tag.list("deletedAt is null order by name");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph("Tags", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            title.setSpacingAfter(12);
            document.add(title);

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setSpacingBefore(6);
            table.setWidths(new float[]{1, 4, 3});

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            addPdfCell(table, "#", headerFont, true);
            addPdfCell(table, "Name", headerFont, true);
            addPdfCell(table, "Slug", headerFont, true);

            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            for (Tag tag : tags) {
                addPdfCell(table, String.valueOf(tag.id), cellFont, false);
                addPdfCell(table, tag.name, cellFont, false);
                addPdfCell(table, tag.slug, cellFont, false);
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            document.close();
            throw new RuntimeException("Failed to generate PDF", e);
        }

        return Response.ok(baos.toByteArray())
                .header("Content-Disposition", "attachment; filename=\"tags.pdf\"")
                .type("application/pdf")
                .build();
    }

    @Path("/admin/tags/export/xlsx")
    @GET
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public Response exportExcel() {
        List<Tag> tags = Tag.list("deletedAt is null order by name");
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Tags");

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"#", "Name", "Slug"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Tag tag : tags) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(tag.id);
                row.createCell(1).setCellValue(tag.name);
                row.createCell(2).setCellValue(tag.slug);
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);

            return Response.ok(baos.toByteArray())
                    .header("Content-Disposition", "attachment; filename=\"tags.xlsx\"")
                    .type("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel", e);
        }
    }

    private void addPdfCell(PdfPTable table, String text, Font font, boolean header) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        if (header) {
            cell.setBackgroundColor(new Color(220, 220, 220));
        }
        cell.setPadding(6);
        table.addCell(cell);
    }
}
