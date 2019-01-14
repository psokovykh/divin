package io.github.psokovykh.divin.debug;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Temporary class for IO functionality from {@link DebugVcGui}
 */
public class DebugInfoIO {

	public static File outputDebugInfoXml(DebugInfoXML dbgInfo, Window window) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(DebugInfoXML.class);
		Marshaller mar= context.createMarshaller();
		mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		var fc = new FileChooser();
		fc.setTitle("Choose, where to save XML");
		final File file = fc.showSaveDialog(window);
		mar.marshal(dbgInfo, file);
		return file;
	}

	public static DebugInfoXML inputDebugInfoXml(File xmlFile) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(DebugInfoXML.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		return (DebugInfoXML) jaxbUnmarshaller.unmarshal( xmlFile );
	}

	public static void outputDebugInfoPdf(DebugInfoXML debugInfo, Window window) {
		var fc = new FileChooser();
		fc.setTitle("Choose, where to save PDF");
		File pdfFile = fc.showSaveDialog(window);

		try {
			Font font = new Font(Font.HELVETICA, 12, Font.BOLD);
			Font fontTitle = new Font(Font.HELVETICA, 16, Font.UNDERLINE);
			Document doc = new Document();
			PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(pdfFile));

			Paragraph title = new Paragraph("Dumped information", fontTitle);
			title.setAlignment(Paragraph.ALIGN_CENTER);

			title.setSpacingBefore(20);
			title.setSpacingAfter(20);


			Paragraph tablePara = new Paragraph();
			PdfPTable table = new PdfPTable(3);
			table.setWidthPercentage(100);
			// setting column widths
			table.setWidths(new float[]{4.0f, 8.0f, 16.0f});
			PdfPCell cell = new PdfPCell();
			// table headers
			cell.setPhrase(new Phrase("Priority", font));
			table.addCell(cell);
			cell.setPhrase(new Phrase("Target PM Name", font));
			table.addCell(cell);
			cell.setPhrase(new Phrase("Text", font));
			table.addCell(cell);

			var requests = debugInfo.getReqList().getRequests();
			if (requests != null && requests.size() != 0) {
				for (var req : requests) {
					table.addCell(String.valueOf(req.getPriority()));
					table.addCell(req.getTargetPmName());
					table.addCell(req.getText());
				}
				tablePara.add(table);
			} else {
				tablePara = new Paragraph("There are no requests in the dump", font);
			}
			tablePara.setSpacingBefore(20);
			tablePara.setSpacingAfter(20);

			Paragraph tableResPara = new Paragraph();
			PdfPTable tableRes = new PdfPTable(2);
			tableRes.setWidthPercentage(100);
			// setting column widths
			tableRes.setWidths(new float[]{4.0f, 8.0f});
			PdfPCell cellRes = new PdfPCell();
			// table headers
			cellRes.setPhrase(new Phrase("Priority", font));
			tableRes.addCell(cellRes);
			cellRes.setPhrase(new Phrase("Text", font));
			tableRes.addCell(cellRes);

			// adding table rows
			var responses = debugInfo.getResList().getResponses();
			if (responses != null && responses.size() != 0) {
				for (var req : responses) {
					tableRes.addCell(String.valueOf(req.getPriority()));
					tableRes.addCell(req.getText());
				}
				tableResPara.add(tableRes);
			} else {
				tableResPara = new Paragraph("There are no responses in the dump", font);
			}
			tableResPara.setSpacingBefore(20);
			tableResPara.setSpacingAfter(20);

			doc.open();
			// adding table to document
			doc.add(title);
			doc.add(tablePara);
			doc.add(tableResPara);
			doc.close();
			writer.close();
		} catch (DocumentException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
