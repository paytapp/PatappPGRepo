
package com.paymentgateway.commons.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.dispatcher.SessionMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.api.client.util.Base64;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.TabSettings;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.dao.MPADao;
import com.paymentgateway.commons.dao.TransactionDetailsService;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.ChargingDetails;
import com.paymentgateway.commons.user.CustomerAddress;
import com.paymentgateway.commons.user.CustomerQR;
import com.paymentgateway.commons.user.EPOSTransaction;
import com.paymentgateway.commons.user.MerchantProcessingApplication;
import com.paymentgateway.commons.user.PermissionType;
import com.paymentgateway.commons.user.ResellerCharges;
import com.paymentgateway.commons.user.RouterConfiguration;
import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.user.TransactionSearchDownloadObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;

/**
 * @author Amitosh Aanand
 *
 */
@Service
public class PDFCreator {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private UserDao userDao;

	@Autowired
	private MPADao mpaDao;

	@Autowired
	TransactionDetailsService transactionServiceDao;

	@Autowired
	PropertiesManager propertiesManager;

	@Autowired
	private Fields fields;

	private static final String prefix = "MONGO_DB_";
	private static Logger logger = LoggerFactory.getLogger(PDFCreator.class.getName());
	private static Font mercantNameFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
	private static Font catFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
	private static Font subCatFont = new Font(Font.FontFamily.HELVETICA, 10);
	private static Font smallFont = new Font(Font.FontFamily.HELVETICA, 8);
	private static Font footerFont = new Font(Font.FontFamily.COURIER, 8);

	public InputStream createInvoicePdf(SessionMap<String, Object> sessionMap, File file)
			throws FileNotFoundException, DocumentException {
		logger.info("Creating Invoice PDF");
		Document document = new Document(PageSize.A4, 20, 20, 20, 20);
		FileOutputStream out = new FileOutputStream(file);
		PdfWriter.getInstance(document, out);
		document.open();
		addInvoiceMetaData(document);
		try {
			addInvoiceContent(document, sessionMap);
		} catch (Exception e) {
			logger.error("exception ", e);
		}
		document.close();
		return new FileInputStream(file);
	}

	@SuppressWarnings({ "static-access", "unused" })
	private String fetchPaymentStatus(String bookingId) {
		try {
			List<BasicDBObject> conditionLst = new ArrayList<BasicDBObject>();
			if (StringUtils.isNotBlank(bookingId)) {
				conditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), bookingId));
				BasicDBObject finalquery = new BasicDBObject("$and", conditionLst);
				MongoDatabase dbIns = mongoInstance.getDB();
				MongoCollection<org.bson.Document> coll = dbIns.getCollection(
						propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
				BasicDBObject match = new BasicDBObject("$match", finalquery);
				BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
				BasicDBObject limit = new BasicDBObject("$limit", 1);
				List<BasicDBObject> pipeline = Arrays.asList(match, sort, limit);

				AggregateIterable<org.bson.Document> output = coll.aggregate(pipeline);
				output.allowDiskUse(true);
				MongoCursor<org.bson.Document> cursor = output.iterator();
				while (cursor.hasNext()) {
					org.bson.Document dbobj = cursor.next();
					TransactionSearchDownloadObject transReport = new TransactionSearchDownloadObject();
					return dbobj.getString(FieldType.STATUS.toString());
				}
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	private void addInvoiceContent(Document document, SessionMap<String, Object> sessionMap)
			throws DocumentException, MalformedURLException, IOException, FileNotFoundException, SystemException {
		Font companyNameFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
		Font tableHeaderFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
		Font subCatFont = new Font(Font.FontFamily.HELVETICA, 10);
		// Font smallFont = new Font(Font.FontFamily.HELVETICA, 8);
		Font footerFont = new Font(Font.FontFamily.HELVETICA, 8);

		Paragraph logoParagraph = null;
		Paragraph headingParagraph = null;
		Paragraph categoryParagraph = null;
		Paragraph custNameParagraph = null;

		Paragraph custDetailsParagraph = null;
		Paragraph signatoryParagraph = null;
		// Paragraph amountParagraph = null;
		Paragraph footerParagraph = null;

		TransactionSearch transaction = transactionServiceDao
				.getTransactionForInvoicePdf((String) sessionMap.get(FieldType.ORDER_ID.getName()));

		User user = null;

		String payId = sessionMap.get(FieldType.PAY_ID.getName()).toString();
		MerchantProcessingApplication mpa = mpaDao.fetchMPADataByPayId(payId);

		if (mpa == null) {
			user = userDao.findPayId(payId);
		}

		String merchantImageAddr = getMerchantLogoPath(payId);

		// Header
		document.add(Chunk.NEWLINE);

		float[] columnWidths1 = new float[] { 90f, 100f };
		PdfPTable p = new PdfPTable(2);
		p.setWidths(columnWidths1);
		p.getDefaultCell().setBorderWidth(0f);
		p.setWidthPercentage(100f);
		Image img = null;
		if (StringUtils.isNotBlank(merchantImageAddr)) {

			File file = new File(merchantImageAddr);

			if (file.exists()) {
				img = Image.getInstance(merchantImageAddr);
				img.scaleToFit(100f, 100f);
				img.setAbsolutePosition(25f, 750f);
				logoParagraph = new Paragraph();
				logoParagraph.add(img);
			}

		}
		if (img != null) {
			PdfPCell mercLogo = new PdfPCell(img);
			mercLogo.setBorder(Rectangle.NO_BORDER);
			mercLogo.setFixedHeight(30f);
			p.addCell(new PdfPCell(mercLogo)).setHorizontalAlignment(Element.ALIGN_LEFT);
		} else {
			PdfPCell mercLogo = new PdfPCell(new Paragraph(""));
			mercLogo.setBorder(Rectangle.NO_BORDER);
			mercLogo.setFixedHeight(30f);
			p.addCell(new PdfPCell(mercLogo)).setHorizontalAlignment(Element.ALIGN_LEFT);
		}

		headingParagraph = new Paragraph("INVOICE", companyNameFont);
		headingParagraph.setAlignment(Element.ALIGN_RIGHT);
		headingParagraph.add(Chunk.NEWLINE);

		PdfPCell cell = new PdfPCell(headingParagraph);
		cell.setFixedHeight(30f);
		cell.setBorder(Rectangle.NO_BORDER);
		p.addCell(new PdfPCell(cell)).setHorizontalAlignment(Element.ALIGN_RIGHT);

		document.add(p);

		document.add(Chunk.NEWLINE);
		document.add(Chunk.NEWLINE);
		document.add(Chunk.NEWLINE);

		// Company details
		if (mpa != null) {
			if (StringUtils.isNotBlank(mpa.getCompanyName())) {
				headingParagraph = new Paragraph(mpa.getCompanyName(), companyNameFont);
				document.add(headingParagraph);
			}

			if (StringUtils.isNotBlank(mpa.getTradingAddress1())) {
				headingParagraph = new Paragraph(mpa.getTradingAddress1(), subCatFont);
				headingParagraph.add(Chunk.NEWLINE + mpa.getTradingState() + ", " + mpa.getTradingPin());
				document.add(headingParagraph);
			}
			if (StringUtils.isNotBlank(mpa.getCompanyWebsite())) {
				headingParagraph = new Paragraph(mpa.getCompanyWebsite());
				document.add(headingParagraph);
			}

		}

		// Order ID and Date
		if (StringUtils.isNotBlank((String) sessionMap.get(FieldType.ORDER_ID.getName()))) {
			String paymentType = PaymentType.getpaymentName((String) sessionMap.get(FieldType.PAYMENT_MODE.getName()));

			headingParagraph = new Paragraph(Chunk.NEWLINE + "ORDER ID", catFont);
			headingParagraph.setTabSettings(new TabSettings(132f));
			headingParagraph.add(Chunk.TABBING);
			headingParagraph.add("INVOICE DATE");
			headingParagraph.setTabSettings(new TabSettings(132f));
			headingParagraph.add(Chunk.TABBING);
			headingParagraph.add("PAYMENT METHOD");
			headingParagraph.setTabSettings(new TabSettings(150f));
			headingParagraph.add(Chunk.TABBING);
			headingParagraph.add("MOP TYPE");
			document.add(headingParagraph);

			headingParagraph = new Paragraph((String) sessionMap.get(FieldType.ORDER_ID.getName()), subCatFont);
			headingParagraph.setTabSettings(new TabSettings(150f));
			headingParagraph.add(Chunk.TABBING);
			headingParagraph.add(changeDateFormat((String) sessionMap.get(FieldType.CREATE_DATE.getName())));
			headingParagraph.setTabSettings(new TabSettings(150f));
			headingParagraph.add(Chunk.TABBING);
			headingParagraph.add(paymentType);
			headingParagraph.setTabSettings(new TabSettings(150f));
			headingParagraph.add(Chunk.TABBING);
			headingParagraph.add(MopType.getmopName((String) sessionMap.get(FieldType.MOP_TYPE.getName())));
			document.add(headingParagraph);

			headingParagraph = new Paragraph(Chunk.NEWLINE + "TRANSACTION STATUS", catFont);
			headingParagraph.setTabSettings(new TabSettings(132f));

			if (paymentType.equalsIgnoreCase("Credit Card") || paymentType.equalsIgnoreCase("Debit Card")) {
				headingParagraph.add(Chunk.TABBING);
				headingParagraph.add("CARD NUMBER");
				headingParagraph.setTabSettings(new TabSettings(132f));
				headingParagraph.add(Chunk.TABBING);
				headingParagraph.add("PG REF NO");
				headingParagraph.setTabSettings(new TabSettings(150f));
				headingParagraph.add(Chunk.TABBING);
				headingParagraph.add("");
				document.add(headingParagraph);

				headingParagraph = new Paragraph((String) sessionMap.get(FieldType.STATUS.getName()), subCatFont);
				headingParagraph.setTabSettings(new TabSettings(150f));
				headingParagraph.add(Chunk.TABBING);
				headingParagraph.add(transaction.getCardNumber());
				headingParagraph.setTabSettings(new TabSettings(150f));
				headingParagraph.add(Chunk.TABBING);
				headingParagraph.add(transaction.getPgRefNum());
				headingParagraph.setTabSettings(new TabSettings(150f));
				headingParagraph.add(Chunk.TABBING);
				headingParagraph.add("");
				document.add(headingParagraph);
			} else {
				headingParagraph.add(Chunk.TABBING);
				headingParagraph.add("");
				headingParagraph.setTabSettings(new TabSettings(132f));
				headingParagraph.add(Chunk.TABBING);
				headingParagraph.add("");
				headingParagraph.setTabSettings(new TabSettings(150f));
				headingParagraph.add(Chunk.TABBING);
				headingParagraph.add("");
				document.add(headingParagraph);

				headingParagraph = new Paragraph(transaction.getStatus(), subCatFont);
				headingParagraph.setTabSettings(new TabSettings(150f));
				headingParagraph.add(Chunk.TABBING);
				headingParagraph.add("");
				headingParagraph.setTabSettings(new TabSettings(150f));
				headingParagraph.add(Chunk.TABBING);
				headingParagraph.add("");
				headingParagraph.setTabSettings(new TabSettings(150f));
				headingParagraph.add(Chunk.TABBING);
				headingParagraph.add("");
				document.add(headingParagraph);
			}
		}

		LineSeparator ls = new LineSeparator();

		document.add(new Chunk(ls));
		document.add(Chunk.NEWLINE);
		document.add(Chunk.NEWLINE);

		// Customer Details
		categoryParagraph = new Paragraph("BILL TO,", catFont);
		document.add(categoryParagraph);
		document.add(Chunk.NEWLINE);
		if (StringUtils.isNotBlank((String) sessionMap.get(FieldType.CUST_NAME.getName()))) {
			custNameParagraph = new Paragraph((String) sessionMap.get(FieldType.CUST_NAME.getName()) + ",", catFont);
			document.add(custNameParagraph);
		} else {
			custNameParagraph = new Paragraph("Name: NA");
			document.add(custNameParagraph);
		}
		if (StringUtils.isNotBlank((String) sessionMap.get(FieldType.ADDRESS.getName()))) {
			custDetailsParagraph = new Paragraph((String) sessionMap.get(FieldType.ADDRESS.getName()), subCatFont);
			if (StringUtils.isNotBlank((String) sessionMap.get(FieldType.CITY.getName()))) {
				custDetailsParagraph.add(Chunk.NEWLINE + (String) sessionMap.get(FieldType.CITY.getName()));
			}

			if (StringUtils.isNotBlank((String) sessionMap.get(FieldType.STATE.getName()))) {
				custDetailsParagraph.add(Chunk.NEWLINE + (String) sessionMap.get(FieldType.STATE.getName()) + ", "
						+ (String) sessionMap.get(FieldType.PIN.getName()));
			}
			document.add(custDetailsParagraph);
		}

		document.add(Chunk.NEWLINE);

		// invoice Duration and Contact to customer details

		PdfPTable dataTable = new PdfPTable(3);
		dataTable.getDefaultCell().setBorder(0);
		float[] columnWidths = new float[] { 500f, 20f, 500f };
		dataTable.setWidths(columnWidths);
		dataTable.setWidthPercentage(100f);

		if (StringUtils.isNotBlank((String) sessionMap.get(FieldType.DURATION_FROM.getName()))) {
			custDetailsParagraph = new Paragraph(
					"Duration From: " + (String) sessionMap.get(FieldType.DURATION_FROM.getName()), subCatFont);
			dataTable.addCell(custDetailsParagraph);
			dataTable.addCell("");
		}

		if (StringUtils.isNotBlank((String) sessionMap.get(FieldType.DURATION_TO.getName()))) {
			custDetailsParagraph = new Paragraph(
					"Duration To: " + (String) sessionMap.get(FieldType.DURATION_TO.getName()), subCatFont);
			dataTable.addCell(custDetailsParagraph);
		}
		if (StringUtils.isNotBlank((String) sessionMap.get("MOBILE"))) {
			custDetailsParagraph = new Paragraph("Mobile No: " + (String) sessionMap.get("MOBILE"), subCatFont);
			dataTable.addCell(custDetailsParagraph);
			dataTable.addCell("");
		} else {
			custDetailsParagraph = new Paragraph("Mobile No: ", subCatFont);
			dataTable.addCell(custDetailsParagraph);
			dataTable.addCell("");
		}

		if (StringUtils.isNotBlank((String) sessionMap.get(FieldType.EMAIL.getName()))) {
			custDetailsParagraph = new Paragraph("Email ID: " + (String) sessionMap.get(FieldType.EMAIL.getName()),
					subCatFont);
			dataTable.addCell(custDetailsParagraph);
		} else {
			custDetailsParagraph = new Paragraph("Email ID: ", subCatFont);
			dataTable.addCell(custDetailsParagraph);
		}

		document.add(dataTable);
		document.add(Chunk.NEWLINE);
		document.add(Chunk.NEWLINE);
		LineSeparator ls1 = new LineSeparator();
		document.add(new Chunk(ls1));

		// Invoice Details

		custDetailsParagraph = new Paragraph("INVOICE DETAILS", catFont);
		custDetailsParagraph.setAlignment(Element.ALIGN_CENTER);
		document.add(custDetailsParagraph);
		document.add(Chunk.NEWLINE);

		float[] pointColumnWidths = { 90F, 230F, 90F, 80F };

		PdfPTable invoiceTable = new PdfPTable(4);
		invoiceTable.setWidths(pointColumnWidths);
		invoiceTable.setWidthPercentage(100f);

		PdfPCell headerProName = new PdfPCell(new Phrase("Product Name", tableHeaderFont));
		headerProName.setBackgroundColor(BaseColor.LIGHT_GRAY);
		headerProName.setHorizontalAlignment(Element.ALIGN_CENTER);
		headerProName.setVerticalAlignment(Element.ALIGN_CENTER);
		headerProName.setFixedHeight(25f);
		headerProName.setBorderWidthRight(0f);
		invoiceTable.addCell(headerProName);

		PdfPCell headerDesc = new PdfPCell(new Phrase("Description", tableHeaderFont));
		headerDesc.setBackgroundColor(BaseColor.LIGHT_GRAY);
		headerDesc.setHorizontalAlignment(Element.ALIGN_CENTER);
		headerDesc.setVerticalAlignment(Element.ALIGN_CENTER);
		headerDesc.setFixedHeight(25f);
		headerDesc.setBorderWidthRight(0f);
		invoiceTable.addCell(headerDesc);

		PdfPCell headerQuantity = new PdfPCell(new Phrase("Quantity", tableHeaderFont));
		headerQuantity.setBackgroundColor(BaseColor.LIGHT_GRAY);
		headerQuantity.setHorizontalAlignment(Element.ALIGN_CENTER);
		headerQuantity.setVerticalAlignment(Element.ALIGN_CENTER);
		headerQuantity.setFixedHeight(25f);
		headerQuantity.setBorderWidthRight(0f);
		invoiceTable.addCell(headerQuantity);

		PdfPCell headerAmount = new PdfPCell(new Phrase("Amount", tableHeaderFont));
		headerAmount.setBackgroundColor(BaseColor.LIGHT_GRAY);
		headerAmount.setHorizontalAlignment(Element.ALIGN_CENTER);
		headerAmount.setVerticalAlignment(Element.ALIGN_CENTER);
		headerAmount.setFixedHeight(25f);
		invoiceTable.addCell(headerAmount);

		if (StringUtils.isNotBlank((String) sessionMap.get("PROD_NAME"))) {
			PdfPCell pc1 = new PdfPCell(new Phrase((String) sessionMap.get("PROD_NAME"), subCatFont));
			pc1.setFixedHeight(30f);
			pc1.setBorderWidthRight(0f);
			pc1.setBorderWidthTop(0f);
			pc1.setPaddingTop(Element.ALIGN_MIDDLE);
			pc1.setVerticalAlignment(Element.ALIGN_CENTER);
			pc1.setHorizontalAlignment(Element.ALIGN_CENTER);
			invoiceTable.addCell(new PdfPCell(pc1));
		} else {
			PdfPCell pc1 = new PdfPCell(new Phrase("NA", subCatFont));
			pc1.setFixedHeight(30f);
			pc1.setBorderWidthRight(0f);
			pc1.setBorderWidthTop(0f);
			pc1.setPaddingTop(Element.ALIGN_MIDDLE);
			pc1.setVerticalAlignment(Element.ALIGN_CENTER);
			pc1.setHorizontalAlignment(Element.ALIGN_CENTER);
			invoiceTable.addCell(new PdfPCell(pc1)).setHorizontalAlignment(Element.ALIGN_CENTER);
		}

		if (StringUtils.isNotBlank((String) sessionMap.get("PROD_DESC"))) {
			PdfPCell pc1 = new PdfPCell(new Phrase((String) sessionMap.get("PROD_DESC"), subCatFont));
			pc1.setFixedHeight(30f);
			pc1.setVerticalAlignment(Element.ALIGN_CENTER);
			pc1.setHorizontalAlignment(Element.ALIGN_CENTER);
			pc1.setPaddingTop(Element.ALIGN_MIDDLE);
			pc1.setBorderWidthRight(0f);
			pc1.setBorderWidthTop(0f);
			invoiceTable.addCell(new PdfPCell(pc1)).setHorizontalAlignment(Element.ALIGN_CENTER);
		} else {
			PdfPCell pc1 = new PdfPCell(new Phrase("NA", subCatFont));
			pc1.setFixedHeight(30f);
			pc1.setBorderWidthRight(0f);
			pc1.setBorderWidthTop(0f);
			pc1.setVerticalAlignment(Element.ALIGN_CENTER);
			pc1.setPaddingTop(Element.ALIGN_MIDDLE);
			pc1.setHorizontalAlignment(Element.ALIGN_CENTER);
			invoiceTable.addCell(new PdfPCell(pc1)).setHorizontalAlignment(Element.ALIGN_CENTER);
		}

		if (StringUtils.isNotBlank((String) sessionMap.get(FieldType.QUANTITY.getName()))) {
			PdfPCell pc1 = new PdfPCell(new Phrase((String) sessionMap.get(FieldType.QUANTITY.getName()), subCatFont));
			pc1.setFixedHeight(30f);
			pc1.setBorderWidthRight(0f);
			pc1.setBorderWidthTop(0f);
			pc1.setVerticalAlignment(Element.ALIGN_CENTER);
			pc1.setPaddingTop(Element.ALIGN_MIDDLE);
			pc1.setHorizontalAlignment(Element.ALIGN_CENTER);
			invoiceTable.addCell(new PdfPCell(pc1)).setHorizontalAlignment(Element.ALIGN_CENTER);
		} else {
			PdfPCell pc1 = new PdfPCell(new Phrase("NA", subCatFont));
			pc1.setFixedHeight(30f);
			pc1.setBorderWidthRight(0f);
			pc1.setBorderWidthTop(0f);
			pc1.setVerticalAlignment(Element.ALIGN_CENTER);
			pc1.setPaddingTop(Element.ALIGN_MIDDLE);
			pc1.setHorizontalAlignment(Element.ALIGN_CENTER);
			invoiceTable.addCell(new PdfPCell(pc1)).setHorizontalAlignment(Element.ALIGN_CENTER);
		}

		if (StringUtils.isNotBlank((String) sessionMap.get(FieldType.AMOUNT.getName()))) {
			PdfPCell pc1 = new PdfPCell(new Phrase((String) sessionMap.get(FieldType.AMOUNT.getName()), subCatFont));
			pc1.setFixedHeight(30f);
			pc1.setBorderWidthTop(0f);
			pc1.setVerticalAlignment(Element.ALIGN_CENTER);
			pc1.setPaddingTop(Element.ALIGN_MIDDLE);
			pc1.setHorizontalAlignment(Element.ALIGN_CENTER);
			invoiceTable.addCell(new PdfPCell(pc1)).setHorizontalAlignment(Element.ALIGN_CENTER);
		} else {
			PdfPCell pc1 = new PdfPCell(new Phrase("NA", subCatFont));
			pc1.setFixedHeight(30f);
			pc1.setBorderWidthTop(0f);
			pc1.setVerticalAlignment(Element.ALIGN_CENTER);
			pc1.setPaddingTop(Element.ALIGN_MIDDLE);
			pc1.setHorizontalAlignment(Element.ALIGN_CENTER);
			invoiceTable.addCell(new PdfPCell(pc1)).setHorizontalAlignment(Element.ALIGN_CENTER);
		}
		document.add(invoiceTable);

		float[] pointColumnWidths1 = { 90F, 80F };
		PdfPTable invoiceTable1 = new PdfPTable(2);
		invoiceTable1.setWidths(pointColumnWidths1);
		invoiceTable1.setWidthPercentage(34.7f);
		invoiceTable1.setHorizontalAlignment(Element.ALIGN_RIGHT);

		invoiceTable1.getDefaultCell().setBackgroundColor(BaseColor.LIGHT_GRAY);
		invoiceTable1.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
		invoiceTable1.getDefaultCell().setFixedHeight(20f);

		if (StringUtils.isNotBlank((String) sessionMap.get(FieldType.AMOUNT.getName()))) {

			PdfPCell pc1 = new PdfPCell(new Phrase("SUBTOTAL", subCatFont));
			pc1.setFixedHeight(25f);
			pc1.setBorderWidthRight(0f);
			pc1.setBorderWidthTop(0f);
			pc1.setPaddingTop(Element.ALIGN_MIDDLE);
			pc1.setVerticalAlignment(Element.ALIGN_CENTER);
			pc1.setHorizontalAlignment(Element.ALIGN_CENTER);
			invoiceTable1.addCell(new PdfPCell(pc1)).setHorizontalAlignment(Element.ALIGN_CENTER);
			PdfPCell pc2 = new PdfPCell(new Phrase((String) sessionMap.get(FieldType.AMOUNT.getName()), subCatFont));
			pc2.setFixedHeight(25f);
			pc2.setPaddingTop(Element.ALIGN_MIDDLE);
			pc2.setBorderWidthTop(0f);
			pc2.setVerticalAlignment(Element.ALIGN_CENTER);
			invoiceTable1.addCell(new PdfPCell(pc2)).setHorizontalAlignment(Element.ALIGN_CENTER);
		}
		if (StringUtils.isNotBlank((String) sessionMap.get(FieldType.SERVICE_CHARGE.getName()))) {

			PdfPCell pc1 = new PdfPCell(new Phrase("SERVICE CHARGE", subCatFont));
			pc1.setFixedHeight(25f);
			pc1.setBorderWidthRight(0f);
			pc1.setBorderWidthTop(0f);
			pc1.setPaddingTop(Element.ALIGN_MIDDLE);
			pc1.setVerticalAlignment(Element.ALIGN_CENTER);
			invoiceTable1.addCell(new PdfPCell(pc1)).setHorizontalAlignment(Element.ALIGN_CENTER);
			PdfPCell pc2 = new PdfPCell(
					new Phrase((String) sessionMap.get(FieldType.SERVICE_CHARGE.getName()), subCatFont));
			pc2.setFixedHeight(25f);
			pc2.setBorderWidthTop(0f);
			pc2.setPaddingTop(Element.ALIGN_MIDDLE);
			pc2.setVerticalAlignment(Element.ALIGN_CENTER);
			invoiceTable1.addCell(new PdfPCell(pc2)).setHorizontalAlignment(Element.ALIGN_CENTER);
		} else {
			PdfPCell pc1 = new PdfPCell(new Phrase("SERVICE CHARGE", subCatFont));
			pc1.setFixedHeight(25f);
			pc1.setBorderWidthRight(0f);
			pc1.setBorderWidthTop(0f);
			pc1.setPaddingTop(Element.ALIGN_MIDDLE);
			pc1.setVerticalAlignment(Element.ALIGN_CENTER);
			invoiceTable1.addCell(new PdfPCell(pc1)).setHorizontalAlignment(Element.ALIGN_CENTER);
			PdfPCell pc2 = new PdfPCell(new Phrase("0.00", subCatFont));
			pc2.setFixedHeight(25f);
			pc2.setBorderWidthTop(0f);
			pc2.setPaddingTop(Element.ALIGN_MIDDLE);
			pc2.setVerticalAlignment(Element.ALIGN_CENTER);
			invoiceTable1.addCell(new PdfPCell(pc2)).setHorizontalAlignment(Element.ALIGN_CENTER);
		}

		if (StringUtils.isNotBlank((String) sessionMap.get(FieldType.DISCOUNT.getName()))
				&& sessionMap.get(FieldType.DISCOUNT_FLAG.getName()).toString().equalsIgnoreCase("I")) {
			PdfPCell pc1 = new PdfPCell(new Phrase("DISCOUNT", subCatFont));
			pc1.setFixedHeight(25f);
			pc1.setBorderWidthRight(0f);
			pc1.setBorderWidthTop(0f);
			pc1.setPaddingTop(Element.ALIGN_MIDDLE);
			pc1.setVerticalAlignment(Element.ALIGN_CENTER);
			invoiceTable1.addCell(new PdfPCell(pc1)).setHorizontalAlignment(Element.ALIGN_CENTER);
			PdfPCell pc2 = new PdfPCell(new Phrase((String) sessionMap.get(FieldType.DISCOUNT.getName()), subCatFont));
			pc2.setFixedHeight(25f);
			pc2.setBorderWidthTop(0f);
			pc2.setPaddingTop(Element.ALIGN_MIDDLE);
			pc2.setVerticalAlignment(Element.ALIGN_CENTER);
			invoiceTable1.addCell(new PdfPCell(pc2)).setHorizontalAlignment(Element.ALIGN_CENTER);
		}
		if (StringUtils.isNotBlank((String) sessionMap.get(FieldType.TOTAL_AMOUNT.getName()))) {
			PdfPCell pc1 = new PdfPCell(new Phrase("TOTAL AMOUNT", subCatFont));
			pc1.setFixedHeight(25f);
			pc1.setBorderWidthRight(0f);
			pc1.setBorderWidthTop(0f);
			pc1.setPaddingTop(Element.ALIGN_MIDDLE);
			pc1.setVerticalAlignment(Element.ALIGN_CENTER);
			invoiceTable1.addCell(new PdfPCell(pc1)).setHorizontalAlignment(Element.ALIGN_CENTER);
			PdfPCell pc2 = new PdfPCell(
					new Phrase((String) sessionMap.get(FieldType.TOTAL_AMOUNT.getName()), subCatFont));
			pc2.setFixedHeight(25f);
			pc2.setBorderWidthTop(0f);
			pc2.setPaddingTop(Element.ALIGN_MIDDLE);
			pc2.setVerticalAlignment(Element.ALIGN_CENTER);

			invoiceTable1.addCell(new PdfPCell(pc2)).setHorizontalAlignment(Element.ALIGN_CENTER);
		}

		document.add(invoiceTable1);

		document.add(Chunk.NEWLINE);
		document.add(Chunk.NEWLINE);

		signatoryParagraph = new Paragraph("Thank you for doing business with us.", footerFont);
		signatoryParagraph.setAlignment(Element.ALIGN_CENTER);
		document.add(signatoryParagraph);

		if (mpa != null) {
			footerParagraph = new Paragraph(
					Chunk.NEWLINE + "For any queries feel free to connect with us at " + mpa.getCompanyPhone()
							+ ". You may also drop your query to us at " + mpa.getCompanyEmailId() + ".",
					footerFont);
		} else if (user != null) {
			footerParagraph = new Paragraph(Chunk.NEWLINE + "For any queries feel free to connect with us at "
					+ user.getMobile() + ". You may also drop your query to us at " + user.getEmailId() + ".",
					footerFont);
		}
		footerParagraph.setAlignment(Element.ALIGN_CENTER);
		document.add(footerParagraph);
		document.add(Chunk.NEWLINE);
		document.add(Chunk.NEWLINE);
		document.add(Chunk.NEWLINE);

		// Footer
		float[] columnWidthsForFooter = new float[] { 150f, 70f };
		PdfPTable pdfFooterTable = new PdfPTable(2);
		pdfFooterTable.setWidths(columnWidthsForFooter);
		pdfFooterTable.getDefaultCell().setBorderWidth(0f);
		pdfFooterTable.setHorizontalAlignment(Element.ALIGN_BOTTOM);
		pdfFooterTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
		pdfFooterTable.setWidthPercentage(30f);

		footerParagraph = new Paragraph("Powered by ", subCatFont);
		PdfPCell footerPara = new PdfPCell(footerParagraph);
		footerPara.setPaddingTop(10f);
		footerPara.setPaddingRight(0f);
		footerPara.setBorder(Rectangle.NO_BORDER);
		footerPara.setFixedHeight(30f);
		pdfFooterTable.addCell(new PdfPCell(footerPara)).setHorizontalAlignment(Element.ALIGN_RIGHT);

		Image img2 = Image.getInstance(PropertiesManager.propertiesMap.get("footerLogo"));
		img2.scaleToFit(50f, 100f);

		PdfPCell cellLogo = new PdfPCell(img2);
		cellLogo.setPaddingTop(10f);
		cellLogo.setPaddingLeft(0f);
		cellLogo.setBorder(Rectangle.NO_BORDER);
		cellLogo.setFixedHeight(30f);
		pdfFooterTable.addCell(new PdfPCell(cellLogo)).setHorizontalAlignment(Element.ALIGN_RIGHT);

		document.add(pdfFooterTable);

		document.close();
	}

	@SuppressWarnings("unused")
	private Paragraph addBlankLine(int j) {
		Paragraph blankParagraph = new Paragraph();
		for (int i = 0; i < j; i++) {
			blankParagraph.add(new Paragraph(Chunk.NEWLINE));
		}
		return blankParagraph;
	}

	private void addInvoiceMetaData(Document document) {
		document.addTitle("Invoice");
		document.addSubject("Invoice payment on Payment Gateway Solution Private Limited");
		document.addKeywords("Invoice, Payment Gateway");
		document.addAuthor("Payment Gateway Solution Private Limited, created on " + new Date());
	}

	public InputStream creatorSmartRouterPdf(Map<String, List<RouterConfiguration>> routerConfigMap, File file,
			String merchantName) throws FileNotFoundException, DocumentException {
		logger.info("Creating Smart Router PDF");
		Document document = new Document(PageSize.A4, 20, 20, 20, 20);

		FileOutputStream out = new FileOutputStream(file);
		PdfWriter.getInstance(document, out);
		document.open();
		try {
			addSmartRouterContent(document, routerConfigMap, merchantName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		document.close();
		return new FileInputStream(file);
	}

	@SuppressWarnings("static-access")
	private void addSmartRouterContent(Document document, Map<String, List<RouterConfiguration>> routerConfigMap,
			String merchantName) throws DocumentException, MalformedURLException, IOException, FileNotFoundException {

		String headingPart[] = { "Acquirer Name", "Status", "Description", "Mode", "Payment Type", "Mop Type",
				"Allowed Fail Count", "Always On", "Load%", "Priority", "Retry Time", "Min. Txn", "Max. Txn",
				"Acquirer Mode" };

		Paragraph titleParagraph = null;
		Paragraph signatoryParagraph = null;
		Paragraph nameParagraph = null;

		titleParagraph = new Paragraph(PermissionType.SMART_ROUTER.getPermission(), mercantNameFont);
		titleParagraph.setAlignment(Element.ALIGN_CENTER);
		document.add(titleParagraph);

		nameParagraph = new Paragraph(merchantName, catFont);
		nameParagraph.setAlignment(Element.ALIGN_LEFT);
		document.add(nameParagraph);

		LineSeparator ls1 = new LineSeparator();
		document.add(new Chunk(ls1));

		for (Map.Entry<String, List<RouterConfiguration>> routerConfig : routerConfigMap.entrySet()) {

			float tableWidth = 100f;
			float[] colWidths = { 1.2f, 0.90f, 1f, 0.80f, 1.2f, 0.80f, 1.30f, 1f, 0.90f, 0.90f, 0.80f, 0.90f, 1.1f,
					1.05f };
			PdfPTable configTableData = new PdfPTable(colWidths);
			configTableData.setWidthPercentage(tableWidth);

			PdfPCell cell = new PdfPCell(new Phrase(routerConfig.getKey(),
					new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE)));
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setBackgroundColor(new BaseColor(0, 38, 99));
			cell.setColspan(14);
			configTableData.addCell(cell);
			int count = 0;
			while (count < 14) {
				PdfPCell headingCell;
				headingCell = new PdfPCell(new Phrase(headingPart[count], subCatFont));
				headingCell.setBackgroundColor(BaseColor.GRAY.LIGHT_GRAY);
				headingCell.setHorizontalAlignment(Element.ALIGN_CENTER);
				configTableData.addCell(headingCell);
				configTableData.setKeepTogether(true);
				count++;
			}
			for (RouterConfiguration router : routerConfig.getValue()) {

				PdfPCell acquirerCellData = new PdfPCell(new Phrase(router.getAcquirer(), smallFont));
				acquirerCellData.setFixedHeight(30f);
				configTableData.addCell(new PdfPCell(acquirerCellData)).setHorizontalAlignment(Element.ALIGN_CENTER);

				PdfPCell statusCellData = new PdfPCell(
						new Phrase(String.valueOf(router.isCurrentlyActive()), smallFont));
				statusCellData.setFixedHeight(30f);
				configTableData.addCell(new PdfPCell(statusCellData)).setHorizontalAlignment(Element.ALIGN_CENTER);

				PdfPCell descriptionCellData = new PdfPCell(new Phrase(router.getStatusName().toString(), smallFont));
				descriptionCellData.setFixedHeight(30f);
				configTableData.addCell(new PdfPCell(descriptionCellData)).setHorizontalAlignment(Element.ALIGN_CENTER);

				PdfPCell modeCellData = new PdfPCell(new Phrase(router.getMode(), smallFont));
				modeCellData.setFixedHeight(30f);
				configTableData.addCell(new PdfPCell(modeCellData)).setHorizontalAlignment(Element.ALIGN_CENTER);

				PdfPCell paymentTypeCellData = new PdfPCell(new Phrase(router.getPaymentType(), smallFont));
				paymentTypeCellData.setFixedHeight(30f);
				configTableData.addCell(new PdfPCell(paymentTypeCellData)).setHorizontalAlignment(Element.ALIGN_CENTER);

				PdfPCell mopTypeCellData = new PdfPCell(new Phrase(router.getMopType(), smallFont));
				mopTypeCellData.setFixedHeight(30f);
				configTableData.addCell(new PdfPCell(mopTypeCellData)).setHorizontalAlignment(Element.ALIGN_CENTER);

				PdfPCell allowedFailureCellData = new PdfPCell(
						new Phrase(String.valueOf(router.getAllowedFailureCount()), smallFont));
				allowedFailureCellData.setFixedHeight(30f);
				configTableData.addCell(new PdfPCell(allowedFailureCellData))
						.setHorizontalAlignment(Element.ALIGN_CENTER);

				PdfPCell alwaysOnCellData = new PdfPCell(new Phrase(String.valueOf(router.isAlwaysOn()), smallFont));
				alwaysOnCellData.setFixedHeight(30f);
				configTableData.addCell(new PdfPCell(alwaysOnCellData)).setHorizontalAlignment(Element.ALIGN_CENTER);

				PdfPCell loadCellData = new PdfPCell(new Phrase(String.valueOf(router.getLoadPercentage()), smallFont));
				loadCellData.setFixedHeight(30f);
				configTableData.addCell(new PdfPCell(loadCellData)).setHorizontalAlignment(Element.ALIGN_CENTER);

				PdfPCell priorityCellData = new PdfPCell(new Phrase(router.getPriority(), smallFont));
				priorityCellData.setFixedHeight(30f);
				configTableData.addCell(new PdfPCell(priorityCellData)).setHorizontalAlignment(Element.ALIGN_CENTER);

				PdfPCell retryCellData = new PdfPCell(new Phrase(router.getRetryMinutes(), smallFont));
				retryCellData.setFixedHeight(30f);
				configTableData.addCell(new PdfPCell(retryCellData)).setHorizontalAlignment(Element.ALIGN_CENTER);

				PdfPCell minAmountCellData = new PdfPCell(new Phrase(String.valueOf(router.getMinAmount()), smallFont));
				minAmountCellData.setFixedHeight(30f);
				configTableData.addCell(new PdfPCell(minAmountCellData)).setHorizontalAlignment(Element.ALIGN_CENTER);

				PdfPCell maxAmountCellData = new PdfPCell(new Phrase(String.valueOf(router.getMaxAmount()), smallFont));
				maxAmountCellData.setFixedHeight(30f);
				configTableData.addCell(new PdfPCell(maxAmountCellData)).setHorizontalAlignment(Element.ALIGN_CENTER);

				PdfPCell acquirerModeCellData = new PdfPCell(new Phrase(router.getOnUsoffUsName(), smallFont));
				acquirerModeCellData.setFixedHeight(30f);
				configTableData.addCell(new PdfPCell(acquirerModeCellData))
						.setHorizontalAlignment(Element.ALIGN_CENTER);
			}
			document.add(configTableData);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
		}
		signatoryParagraph = new Paragraph("Thank you for doing business with us.", footerFont);
		signatoryParagraph.setAlignment(Element.ALIGN_CENTER);
		document.add(signatoryParagraph);
		document.close();
		logger.info("smartRouter PDF Succesfully created");
	}

	public InputStream creatChargingDetailsPdf(
			Map<String, List<Map<String, List<ChargingDetails>>>> acquirerTypeDataMap, File file, String merchantName)
			throws FileNotFoundException, DocumentException {
		logger.info("Creating Smart Router PDF");
		Document document = new Document(PageSize.A4, 20, 20, 20, 20);

		FileOutputStream out = new FileOutputStream(file);
		PdfWriter.getInstance(document, out);
		document.open();
		try {
			addChargingDetailsContent(document, acquirerTypeDataMap, merchantName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		document.close();
		return new FileInputStream(file);
	}

	@SuppressWarnings("static-access")
	public void addChargingDetailsContent(Document document,
			Map<String, List<Map<String, List<ChargingDetails>>>> acquirerTypeDataMap, String merchantName)
			throws DocumentException, MalformedURLException, IOException, FileNotFoundException {

		String dataHeading[] = { "Currency", "Mop", "Transaction"/* , "Slab" */, "PG TDR", "PG FC", "Bank TDR",
				"Bank FC", "Reseller TDR", "Reseller FC", "Merchant TDR", "Merchant FC", "Merchant GST", "Min Txn",
				"Max Txn", "Max Charge Merchant", "Max Charge Acquirer" };

		Paragraph logoParagraph = null;
		Paragraph titleParagraph = null;
		Paragraph nameParagraph = null;
		Paragraph AquirerNameParagraph = null;

		Image img = Image.getInstance(PropertiesManager.propertiesMap.get("emailerLogoURL"));
		img.scaleToFit(100f, 100f);
		img.setAbsolutePosition(470f, 800f);
		logoParagraph = new Paragraph();
		logoParagraph.add(img);
		logoParagraph.add(Chunk.NEWLINE);
		document.add(logoParagraph);

		titleParagraph = new Paragraph("Merchants's Charging Details", mercantNameFont);
		titleParagraph.setAlignment(Element.ALIGN_CENTER);
		document.add(titleParagraph);

		nameParagraph = new Paragraph(merchantName, catFont);
		nameParagraph.setAlignment(Element.ALIGN_LEFT);
		document.add(nameParagraph);

		LineSeparator ls1 = new LineSeparator();
		document.add(new Chunk(ls1));

		for (String acquirerTypeDataMapKey : acquirerTypeDataMap.keySet()) {
			List<Map<String, List<ChargingDetails>>> chargingDetailsMapList = acquirerTypeDataMap
					.get(acquirerTypeDataMapKey);

			AquirerNameParagraph = new Paragraph(acquirerTypeDataMapKey, catFont);
			AquirerNameParagraph.setAlignment(Element.ALIGN_LEFT);
			AquirerNameParagraph.add(Chunk.NEWLINE);
			AquirerNameParagraph.add(Chunk.NEWLINE);
			document.add(AquirerNameParagraph);

			for (Map<String, List<ChargingDetails>> chargingDetailsMap : chargingDetailsMapList) {

				for (Map.Entry<String, List<ChargingDetails>> chargingDetailsMapData : chargingDetailsMap.entrySet()) {

					float tableWidth = 100f;
					float[] colWidths = { 1f, 0.90f, 1f, 1f, 1f, 1f, 1.2f, 1.2f, 1.4f, 1.f, 0.80f, 1f, 1.5f, 1.5f,
							1.20f, 1.20f };
					PdfPTable configTableData = new PdfPTable(colWidths);
					configTableData.setWidthPercentage(tableWidth);
					Font font = new Font();
					font.setColor(BaseColor.WHITE);
					PdfPCell cell = new PdfPCell(new Phrase(chargingDetailsMapData.getKey(), font));
					cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					cell.setBackgroundColor(new BaseColor(0, 33, 99));
					cell.setColspan(16);
					configTableData.addCell(cell);

					int count = 0;
					while (count < 16) {
						PdfPCell headCell;
						headCell = new PdfPCell(new Phrase(dataHeading[count], subCatFont));
						headCell.setBackgroundColor(BaseColor.GRAY.LIGHT_GRAY);
						headCell.setHorizontalAlignment(Element.ALIGN_CENTER);
						configTableData.addCell(headCell);
						configTableData.setKeepTogether(true);
						count++;
					}
					for (ChargingDetails chargingDetails : chargingDetailsMapData.getValue()) {

						PdfPCell currencyCellData = new PdfPCell(
								new Phrase(String.valueOf(chargingDetails.getCurrency()), smallFont));
						currencyCellData.setFixedHeight(30f);
						configTableData.addCell(new PdfPCell(currencyCellData))
								.setHorizontalAlignment(Element.ALIGN_CENTER);

						PdfPCell mopTypeCellData = new PdfPCell(
								new Phrase(chargingDetails.getMopType().getName(), smallFont));
						mopTypeCellData.setFixedHeight(30f);
						configTableData.addCell(new PdfPCell(mopTypeCellData))
								.setHorizontalAlignment(Element.ALIGN_CENTER);

						PdfPCell transactionTypeCellData = new PdfPCell(
								new Phrase(chargingDetails.getTransactionType().getName(), smallFont));
						transactionTypeCellData.setFixedHeight(30f);
						configTableData.addCell(new PdfPCell(transactionTypeCellData))
								.setHorizontalAlignment(Element.ALIGN_CENTER);

						/*
						 * PdfPCell slabIdCellData = new PdfPCell(new
						 * Phrase(chargingDetails.getSlabId(), smallFont));
						 * slabIdCellData.setFixedHeight(30f); configTableData.addCell(new
						 * PdfPCell(slabIdCellData)) .setHorizontalAlignment(Element.ALIGN_CENTER);
						 */

						PdfPCell pgTDRCellData = new PdfPCell(
								new Phrase(String.valueOf(chargingDetails.getPgTDR()), smallFont));
						pgTDRCellData.setFixedHeight(30f);
						configTableData.addCell(new PdfPCell(pgTDRCellData))
								.setHorizontalAlignment(Element.ALIGN_CENTER);

						PdfPCell pgFCCellData = new PdfPCell(
								new Phrase(String.valueOf(chargingDetails.getPgFixCharge()), smallFont));
						pgFCCellData.setFixedHeight(30f);
						configTableData.addCell(new PdfPCell(pgFCCellData))
								.setHorizontalAlignment(Element.ALIGN_CENTER);

						PdfPCell bankTDRCellData = new PdfPCell(
								new Phrase(String.valueOf(chargingDetails.getBankTDR()), smallFont));
						bankTDRCellData.setFixedHeight(30f);
						configTableData.addCell(new PdfPCell(bankTDRCellData))
								.setHorizontalAlignment(Element.ALIGN_CENTER);

						PdfPCell bankFCCellData = new PdfPCell(
								new Phrase(String.valueOf(chargingDetails.getBankFixCharge()), smallFont));
						bankFCCellData.setFixedHeight(30f);
						configTableData.addCell(new PdfPCell(bankFCCellData))
								.setHorizontalAlignment(Element.ALIGN_CENTER);

						PdfPCell resellerTDRCellData = new PdfPCell(
								new Phrase(String.valueOf(chargingDetails.getResellerTDR()), smallFont));
						resellerTDRCellData.setFixedHeight(30f);
						configTableData.addCell(new PdfPCell(resellerTDRCellData))
								.setHorizontalAlignment(Element.ALIGN_CENTER);

						PdfPCell resellerFCCellData = new PdfPCell(
								new Phrase(String.valueOf(chargingDetails.getResellerFixCharge()), smallFont));
						resellerFCCellData.setFixedHeight(30f);
						configTableData.addCell(new PdfPCell(resellerFCCellData))
								.setHorizontalAlignment(Element.ALIGN_CENTER);

						PdfPCell merchantTDRCellData = new PdfPCell(
								new Phrase(String.valueOf(chargingDetails.getMerchantTDR()), smallFont));
						merchantTDRCellData.setFixedHeight(30f);
						configTableData.addCell(new PdfPCell(merchantTDRCellData))
								.setHorizontalAlignment(Element.ALIGN_CENTER);

						PdfPCell merchantFCCellData = new PdfPCell(
								new Phrase(String.valueOf(chargingDetails.getMerchantFixCharge()), smallFont));
						merchantFCCellData.setFixedHeight(30f);
						configTableData.addCell(new PdfPCell(merchantFCCellData))
								.setHorizontalAlignment(Element.ALIGN_CENTER);

						PdfPCell merchantServiceTaxCellData = new PdfPCell(
								new Phrase(String.valueOf(chargingDetails.getMerchantServiceTax()), smallFont));
						merchantServiceTaxCellData.setFixedHeight(30f);
						configTableData.addCell(new PdfPCell(merchantServiceTaxCellData))
								.setHorizontalAlignment(Element.ALIGN_CENTER);

						PdfPCell minTxnAmountCellData = new PdfPCell(
								new Phrase(String.valueOf(chargingDetails.getMinTxnAmount()), smallFont));
						minTxnAmountCellData.setFixedHeight(30f);
						configTableData.addCell(new PdfPCell(minTxnAmountCellData))
								.setHorizontalAlignment(Element.ALIGN_CENTER);

						PdfPCell maxTxnAmountCellData = new PdfPCell(
								new Phrase(String.valueOf(chargingDetails.getMaxTxnAmount()), smallFont));
						maxTxnAmountCellData.setFixedHeight(30f);
						configTableData.addCell(new PdfPCell(maxTxnAmountCellData))
								.setHorizontalAlignment(Element.ALIGN_CENTER);

						PdfPCell maxChargeMerchantCellData = new PdfPCell(
								new Phrase(String.valueOf(chargingDetails.getMaxChargeMerchant()), smallFont));
						maxChargeMerchantCellData.setFixedHeight(30f);
						configTableData.addCell(new PdfPCell(maxChargeMerchantCellData))
								.setHorizontalAlignment(Element.ALIGN_CENTER);

						PdfPCell maxChargeAquirerCellData = new PdfPCell(
								new Phrase(String.valueOf(chargingDetails.getMaxChargeAcquirer()), smallFont));
						maxChargeAquirerCellData.setFixedHeight(30f);
						configTableData.addCell(new PdfPCell(maxChargeAquirerCellData))
								.setHorizontalAlignment(Element.ALIGN_CENTER);

					}
					document.add(configTableData);
					document.add(Chunk.NEWLINE);
				}
			}
		}
		document.close();
		logger.info("Charging Details PDF Succesfully created");
	}

	

	public InputStream createEposPdf(EPOSTransaction epos, File file) throws Exception {
		logger.info("Creating EPOS PDF");
		Document document = new Document(PageSize.A4, 20, 20, 20, 20);
		FileOutputStream out = new FileOutputStream(file);
		PdfWriter.getInstance(document, out);
		document.open();
		addEposMetaData(document);
		addEposContent(document, epos);
		document.close();
		return new FileInputStream(file);
	}

	private void addEposMetaData(Document document) {
		document.addTitle("EPOS");
		document.addSubject("EPOS payment on Payment Gateway Solution Private Limited");
		document.addKeywords("EPOS, Payment Gateway");
		document.addAuthor("Payment Gateway Solution Private Limited, created on " + new Date());
	}

	@SuppressWarnings("unused")
	private void addEposContent(Document document, EPOSTransaction epos) throws Exception {
		Paragraph logoParagraph = null;
		Paragraph headingParagraph = null;
		Paragraph categoryParagraph = null;
		Paragraph custNameParagraph = null;

		Paragraph custDetailsParagraph = null;
		Paragraph signatoryParagraph = null;
		Paragraph amountParagraph = null;
		String merchantImageAddr = System.getenv("DTECH_PROPS") + "invoiceImage/" + (String) epos.getPAY_ID() + ".png";
		File file = new File(merchantImageAddr);

		if (StringUtils.isBlank(merchantImageAddr) || merchantImageAddr.contains("null") || !file.exists()) {
			if (StringUtils.isNotBlank((String) epos.getBUSINESS_NAME())) {
				headingParagraph = new Paragraph(epos.getBUSINESS_NAME(), mercantNameFont);
			} else {
				headingParagraph = new Paragraph("Payment Gateway Solution Private Limited", mercantNameFont);
			}
		} else {
			Image img = Image.getInstance(merchantImageAddr);
			img.scaleToFit(100f, 100f);
			img.setAbsolutePosition(25f, 750f);
			logoParagraph = new Paragraph();
			logoParagraph.add(img);
		}

		Image img2 = Image.getInstance(PropertiesManager.propertiesMap.get("emailerLogoURL"));
		img2.scaleToFit(100f, 100f);
		img2.setAbsolutePosition(470f, 800f);
		if (logoParagraph == null) {
			logoParagraph = new Paragraph();
			logoParagraph.add(img2);
		} else {
			logoParagraph.add(img2);
		}
		logoParagraph.add(Chunk.NEWLINE);
		logoParagraph.add(Chunk.NEWLINE);

		document.add(logoParagraph);
		if (headingParagraph != null) {
			document.add(headingParagraph);
		}
		document.add(Chunk.NEWLINE);
		headingParagraph = new Paragraph(
				Chunk.NEWLINE + "1F, CS-06, Ansal Plaza, Vaishali, Ghaziabad, Uttar Pradesh-201010", smallFont);
		headingParagraph.add(Chunk.NEWLINE + "www.paymentgateway.com");
		headingParagraph.add(Chunk.NEWLINE + "support@paymentgateway.com");
		headingParagraph.setTabSettings(new TabSettings(56f));
		headingParagraph.add(Chunk.TABBING);
		String mobile = userDao.findPayId(epos.getPAY_ID()).getMobile();
		if (StringUtils.isNotBlank(mobile)) {
			headingParagraph.add(new Chunk(mobile));
		} else {
			headingParagraph.add(new Chunk("0120 434 4884"));
		}
		document.add(headingParagraph);
		if (StringUtils.isNotBlank(epos.getINVOICE_ID())) {
			headingParagraph = new Paragraph(Chunk.NEWLINE + "ORDER ID: " + epos.getINVOICE_ID(), catFont);
			document.add(headingParagraph);
		} else {
			headingParagraph = new Paragraph(Chunk.NEWLINE + "ORDER ID: ", catFont);
			document.add(headingParagraph);
		}
		LineSeparator ls = new LineSeparator();
		document.add(new Chunk(ls));
		document.add(Chunk.NEWLINE);
		document.add(Chunk.NEWLINE);
		categoryParagraph = new Paragraph("BILL TO,", catFont);
		document.add(categoryParagraph);

		if (StringUtils.isNotBlank(epos.getCUST_MOBILE())) {
			custNameParagraph = new Paragraph(epos.getCUST_MOBILE());
			document.add(custNameParagraph);
		} else if (StringUtils.isNotBlank(epos.getCUST_EMAIL())) {
			custNameParagraph = new Paragraph(epos.getCUST_EMAIL());
			document.add(custNameParagraph);
		}
		document.add(Chunk.NEWLINE);

		PdfPTable dataTable = new PdfPTable(3);
		dataTable.getDefaultCell().setBorder(0);
		float[] columnWidths = new float[] { 500f, 20f, 500f };
		dataTable.setWidths(columnWidths);
		dataTable.setWidthPercentage(100f);

		if (StringUtils.isNotBlank(epos.getCREATE_DATE())) {
			custDetailsParagraph = new Paragraph("Transaction Date and Time: " + epos.getCREATE_DATE(), subCatFont);
			dataTable.addCell(custDetailsParagraph);
		} else {
			custDetailsParagraph = new Paragraph("Transaction Date and Time: ", subCatFont);
			dataTable.addCell(custDetailsParagraph);
		}

		document.add(dataTable);
		document.add(Chunk.NEWLINE);
		document.add(Chunk.NEWLINE);
		LineSeparator ls1 = new LineSeparator();
		document.add(new Chunk(ls1));
		custDetailsParagraph = new Paragraph("BILL DETAILS");
		custDetailsParagraph.setAlignment(Element.ALIGN_CENTER);
		document.add(custDetailsParagraph);
		document.add(Chunk.NEWLINE);
		PdfPTable invoiceTable = new PdfPTable(5);
		invoiceTable.addCell(new PdfPCell(new Phrase("#"))).setHorizontalAlignment(Element.ALIGN_CENTER);
		invoiceTable.addCell(new PdfPCell(new Phrase("Product Name"))).setHorizontalAlignment(Element.ALIGN_CENTER);
		invoiceTable.addCell(new PdfPCell(new Phrase("Description"))).setHorizontalAlignment(Element.ALIGN_CENTER);
		invoiceTable.addCell(new PdfPCell(new Phrase("Quantity"))).setHorizontalAlignment(Element.ALIGN_CENTER);
		invoiceTable.addCell(new PdfPCell(new Phrase("Amount"))).setHorizontalAlignment(Element.ALIGN_CENTER);

		PdfPCell pc = new PdfPCell(new Phrase("1", smallFont));
		pc.setFixedHeight(30f);
		invoiceTable.addCell(new PdfPCell(pc)).setHorizontalAlignment(Element.ALIGN_CENTER);

		PdfPCell pc1 = new PdfPCell(new Phrase("NA", subCatFont));
		pc1.setFixedHeight(30f);
		invoiceTable.addCell(new PdfPCell(pc1)).setHorizontalAlignment(Element.ALIGN_CENTER);

		PdfPCell pc2 = new PdfPCell(new Phrase("NA", subCatFont));
		pc2.setFixedHeight(30f);
		invoiceTable.addCell(new PdfPCell(pc2)).setHorizontalAlignment(Element.ALIGN_CENTER);

		PdfPCell pc3 = new PdfPCell(new Phrase("NA", subCatFont));
		pc3.setFixedHeight(30f);
		invoiceTable.addCell(new PdfPCell(pc3)).setHorizontalAlignment(Element.ALIGN_CENTER);

		if (StringUtils.isNotBlank(epos.getAMOUNT())) {
			PdfPCell pc4 = new PdfPCell(new Phrase(epos.getAMOUNT(), subCatFont));
			pc4.setFixedHeight(30f);
			invoiceTable.addCell(new PdfPCell(pc4)).setHorizontalAlignment(Element.ALIGN_CENTER);
		} else {
			PdfPCell pc4 = new PdfPCell(new Phrase("NA", subCatFont));
			pc1.setFixedHeight(30f);
			invoiceTable.addCell(new PdfPCell(pc1)).setHorizontalAlignment(Element.ALIGN_CENTER);
		}
		document.add(invoiceTable);
		document.add(Chunk.NEWLINE);
		document.add(Chunk.NEWLINE);

		if (StringUtils.isNotBlank(epos.getAMOUNT())) {
			amountParagraph = new Paragraph();
			amountParagraph.setTabSettings(new TabSettings(330f));
			amountParagraph.add(Chunk.TABBING);
			amountParagraph.add(new Chunk("AMOUNT: " + epos.getAMOUNT(), catFont));
		} else {
			amountParagraph = new Paragraph();
			amountParagraph.setTabSettings(new TabSettings(330f));
			amountParagraph.add(Chunk.TABBING);
			amountParagraph.add(new Chunk("AMOUNT: NA", catFont));
		}
		document.add(amountParagraph);

		document.add(Chunk.NEWLINE);
		document.add(Chunk.NEWLINE);
		document.add(Chunk.NEWLINE);
		document.add(Chunk.NEWLINE);
		document.add(Chunk.NEWLINE);
		document.add(Chunk.NEWLINE);

		signatoryParagraph = new Paragraph("Thank you for doing business with us.", footerFont);
		signatoryParagraph.setAlignment(Element.ALIGN_CENTER);
		document.add(signatoryParagraph);
		document.close();

	}

	public InputStream creatResellerChargesPdf(Map<String, List<ResellerCharges>> resellerChargesMap, File file,
			String resellerId) throws Exception {
		logger.info("Creating Reselelr charges PDF");
		Document document = new Document(PageSize.A4, 20, 20, 20, 20);
		FileOutputStream out = new FileOutputStream(file);
		PdfWriter.getInstance(document, out);
		document.open();
		try {
			addResellerChargesContent(document, resellerChargesMap);
		} catch (IOException e) {
			e.printStackTrace();
		}
		document.close();
		return new FileInputStream(file);
	}

	@SuppressWarnings("static-access")
	private void addResellerChargesContent(Document document, Map<String, List<ResellerCharges>> resellerChargesMap)
			throws DocumentException, MalformedURLException, IOException, FileNotFoundException {

		String dataHeading[] = { "Currency", "Mop", "Transaction Type", "Slab", "Reseller Percentage",
				"Reseller Fixed Charge", "PG Percentage From Reseller", "PG Fixed Charge From Reseller", "GST" };

		Paragraph logoParagraph = null;
		Paragraph titleParagraph = null;
		Paragraph AquirerNameParagraph = null;

		Image img = Image.getInstance(PropertiesManager.propertiesMap.get("emailerLogoURL"));
		img.scaleToFit(100f, 100f);
		img.setAbsolutePosition(470f, 800f);
		logoParagraph = new Paragraph();
		logoParagraph.add(img);
		logoParagraph.add(Chunk.NEWLINE);
		document.add(logoParagraph);

		titleParagraph = new Paragraph("Reseller Charges Details", mercantNameFont);
		titleParagraph.setAlignment(Element.ALIGN_CENTER);
		document.add(titleParagraph);

		LineSeparator ls1 = new LineSeparator();
		document.add(new Chunk(ls1));

		for (String resellerChargesMapKey : resellerChargesMap.keySet()) {
			List<ResellerCharges> resellerChargesList = resellerChargesMap.get(resellerChargesMapKey);

			AquirerNameParagraph = new Paragraph(resellerChargesMapKey, catFont);
			AquirerNameParagraph.setAlignment(Element.ALIGN_LEFT);
			AquirerNameParagraph.add(Chunk.NEWLINE);
			AquirerNameParagraph.add(Chunk.NEWLINE);
			document.add(AquirerNameParagraph);
			float tableWidth = 100f;
			float[] colWidths = { 1.3f, 1.5f, 1.5f, 1f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f };
			PdfPTable configTableData = new PdfPTable(colWidths);
			configTableData.setWidthPercentage(tableWidth);
			int count = 0;
			while (count <= 8) {
				PdfPCell headCell;
				headCell = new PdfPCell(new Phrase(dataHeading[count], subCatFont));
				headCell.setBackgroundColor(BaseColor.GRAY.LIGHT_GRAY);
				headCell.setHorizontalAlignment(Element.ALIGN_CENTER);
				configTableData.addCell(headCell);
				configTableData.setKeepTogether(true);
				count++;
			}

			for (ResellerCharges resellerCharges : resellerChargesList) {

				PdfPCell currencyCellData = new PdfPCell(
						new Phrase(String.valueOf(resellerCharges.getCurrency()), smallFont));
				currencyCellData.setFixedHeight(30f);
				configTableData.addCell(new PdfPCell(currencyCellData)).setHorizontalAlignment(Element.ALIGN_CENTER);

				PdfPCell mopTypeCellData = new PdfPCell(new Phrase(resellerCharges.getMopType().getName(), smallFont));
				mopTypeCellData.setFixedHeight(30f);
				configTableData.addCell(new PdfPCell(mopTypeCellData)).setHorizontalAlignment(Element.ALIGN_CENTER);

				PdfPCell transactionTypeCellData = new PdfPCell(
						new Phrase(resellerCharges.getTransactionType().getName(), smallFont));
				transactionTypeCellData.setFixedHeight(30f);
				configTableData.addCell(new PdfPCell(transactionTypeCellData))
						.setHorizontalAlignment(Element.ALIGN_CENTER);

				PdfPCell slabIdCellData = new PdfPCell(new Phrase(resellerCharges.getSlabId(), smallFont));
				slabIdCellData.setFixedHeight(30f);
				configTableData.addCell(new PdfPCell(slabIdCellData)).setHorizontalAlignment(Element.ALIGN_CENTER);

				PdfPCell resellerPercentageCellData = new PdfPCell(
						new Phrase(String.valueOf(resellerCharges.getResellerPercentage()), smallFont));
				resellerPercentageCellData.setFixedHeight(30f);
				configTableData.addCell(new PdfPCell(resellerPercentageCellData))
						.setHorizontalAlignment(Element.ALIGN_CENTER);

				PdfPCell resellerFixedChargeCellData = new PdfPCell(
						new Phrase(String.valueOf(resellerCharges.getResellerFixedCharge()), smallFont));
				resellerFixedChargeCellData.setFixedHeight(30f);
				configTableData.addCell(new PdfPCell(resellerFixedChargeCellData))
						.setHorizontalAlignment(Element.ALIGN_CENTER);

				PdfPCell pgPercentageCellData = new PdfPCell(
						new Phrase(String.valueOf(resellerCharges.getPgPercentage()), smallFont));
				pgPercentageCellData.setFixedHeight(30f);
				configTableData.addCell(new PdfPCell(pgPercentageCellData))
						.setHorizontalAlignment(Element.ALIGN_CENTER);

				PdfPCell pgFixedChargeCellData = new PdfPCell(
						new Phrase(String.valueOf(resellerCharges.getPgFixedCharge()), smallFont));
				pgFixedChargeCellData.setFixedHeight(30f);
				configTableData.addCell(new PdfPCell(pgFixedChargeCellData))
						.setHorizontalAlignment(Element.ALIGN_CENTER);

				PdfPCell gstCellData = new PdfPCell(new Phrase(String.valueOf(resellerCharges.getGst()), smallFont));
				gstCellData.setFixedHeight(30f);
				configTableData.addCell(new PdfPCell(gstCellData)).setHorizontalAlignment(Element.ALIGN_CENTER);

			}
			document.add(configTableData);
			document.add(Chunk.NEWLINE);
		}
		document.close();
		logger.info("Reseller Charges Detail PDF Succesfully created");
	}

	public InputStream createCustomInvoicePdf(TransactionSearch transaction, CustomerAddress customerAddress, File file)
			throws FileNotFoundException, DocumentException {
		logger.info("Creating Custom Invoice PDF");
		Document document = new Document(PageSize.A4, 20, 20, 20, 20);
		FileOutputStream out = new FileOutputStream(file);
		PdfWriter.getInstance(document, out);
		document.open();
		addInvoiceMetaData(document);
		try {
			addCustomInvoiceContent(document, transaction, customerAddress);
		} catch (IOException e) {
			e.printStackTrace();
		}
		document.close();
		return new FileInputStream(file);
	}

	private void addCustomInvoiceContent(Document document, TransactionSearch transaction,
			CustomerAddress customerDetails)
			throws DocumentException, MalformedURLException, IOException, FileNotFoundException {
		logger.info("inside the pdf body for custom invoice");
		Font companyNameFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
		Font tableHeaderFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
		Font subCatFont = new Font(Font.FontFamily.HELVETICA, 10);
		// Font smallFont = new Font(Font.FontFamily.HELVETICA, 8);
		Font footerFont = new Font(Font.FontFamily.HELVETICA, 8);

		Paragraph logoParagraph = null;
		Paragraph headingParagraph = null;
		Paragraph categoryParagraph = null;
		Paragraph custNameParagraph = null;

		Paragraph custDetailsParagraph = null;
		Paragraph signatoryParagraph = null;
		// Paragraph amountParagraph = null;
		Paragraph footerParagraph = null;

		User user = null;
		MerchantProcessingApplication mpa = null;

		String merchantImageAddr = null;

		String merchantPayId = transaction.getPayId();
		String subMerchantPayId = transaction.getSubMerchantId();

		if (StringUtils.isNotBlank(subMerchantPayId)) {
			user = userDao.findPayId(subMerchantPayId);
		} else {
			user = userDao.findPayId(merchantPayId);
		}

		if (StringUtils.isNotBlank(subMerchantPayId)) {
			
				mpa = mpaDao.fetchMPADataByPayId(user.getSuperMerchantId());
				merchantImageAddr = getMerchantLogoPath(user.getPayId());

				File file = new File(merchantImageAddr);

				if (!file.exists()) {
					merchantImageAddr = getMerchantLogoPath(user.getSuperMerchantId());
				}
			
		} else if (StringUtils.isBlank(user.getSuperMerchantId())) {
			mpa = mpaDao.fetchMPADataByPayId(merchantPayId);

			merchantImageAddr = getMerchantLogoPath(merchantPayId);
			File file = new File(merchantImageAddr);

			if (!file.exists()) {
				merchantImageAddr = getMerchantLogoPath(user.getSuperMerchantId());
			}
		} else {
			mpa = mpaDao.fetchMPADataByPayId(merchantPayId);
			merchantImageAddr = getMerchantLogoPath(merchantPayId);
		}

//        if(mpa==null){
//        	user=userDao.findPayId(merchantPayId);
//     
//        }

		// Header
		document.add(Chunk.NEWLINE);

		float[] columnWidths1 = new float[] { 90f, 100f };
		PdfPTable p = new PdfPTable(2);
		p.setWidths(columnWidths1);
		p.getDefaultCell().setBorderWidth(0f);
		p.setWidthPercentage(100f);
		Image img = null;
		if (StringUtils.isNotBlank(merchantImageAddr)) {

			File file = new File(merchantImageAddr);

			if (file.exists()) {
				img = Image.getInstance(merchantImageAddr);
				img.scaleToFit(100f, 100f);
				img.setAbsolutePosition(25f, 750f);
				logoParagraph = new Paragraph();
				logoParagraph.add(img);
			}

		}
		if (img != null) {
			PdfPCell mercLogo = new PdfPCell(img);
			mercLogo.setBorder(Rectangle.NO_BORDER);
			mercLogo.setFixedHeight(30f);
			p.addCell(new PdfPCell(mercLogo)).setHorizontalAlignment(Element.ALIGN_LEFT);
		} else {
			PdfPCell mercLogo = new PdfPCell(new Paragraph(""));
			mercLogo.setBorder(Rectangle.NO_BORDER);
			mercLogo.setFixedHeight(30f);
			p.addCell(new PdfPCell(mercLogo)).setHorizontalAlignment(Element.ALIGN_LEFT);
		}
		if (transaction.getStatus().equalsIgnoreCase(StatusType.CAPTURED.getName())) {
			headingParagraph = new Paragraph("INVOICE", companyNameFont);
			headingParagraph.setAlignment(Element.ALIGN_RIGHT);
			headingParagraph.add(Chunk.NEWLINE);
		} else {
			headingParagraph = new Paragraph("FAILED", companyNameFont);
			headingParagraph.setAlignment(Element.ALIGN_RIGHT);
			headingParagraph.add(Chunk.NEWLINE);
		}

		PdfPCell cell = new PdfPCell(headingParagraph);
		cell.setFixedHeight(30f);
		cell.setBorder(Rectangle.NO_BORDER);
		p.addCell(new PdfPCell(cell)).setHorizontalAlignment(Element.ALIGN_RIGHT);

		document.add(p);

		document.add(Chunk.NEWLINE);
		document.add(Chunk.NEWLINE);
		document.add(Chunk.NEWLINE);

		// Company details
		if (mpa != null) {
			if (StringUtils.isNotBlank(mpa.getCompanyName())) {
				headingParagraph = new Paragraph(mpa.getCompanyName(), companyNameFont);
				document.add(headingParagraph);
			}

			if (StringUtils.isNotBlank(mpa.getTradingAddress1())) {
				headingParagraph = new Paragraph(mpa.getTradingAddress1(), subCatFont);
				headingParagraph.add(Chunk.NEWLINE + mpa.getTradingState() + ", " + mpa.getTradingPin());
				document.add(headingParagraph);
			}
			if (StringUtils.isNotBlank(mpa.getCompanyWebsite())) {
				headingParagraph = new Paragraph(mpa.getCompanyWebsite());
				document.add(headingParagraph);
			}
		}

		// Order ID and Date
		if (StringUtils.isNotBlank(transaction.getOrderId())) {
			String paymentType = PaymentType.getpaymentName(transaction.getPaymentMethods());

			headingParagraph = new Paragraph(Chunk.NEWLINE + "ORDER ID", catFont);
			headingParagraph.setTabSettings(new TabSettings(132f));
			headingParagraph.add(Chunk.TABBING);
			headingParagraph.add("INVOICE DATE");
			headingParagraph.setTabSettings(new TabSettings(132f));
			headingParagraph.add(Chunk.TABBING);
			headingParagraph.add("PAYMENT METHOD");
			headingParagraph.setTabSettings(new TabSettings(150f));
			headingParagraph.add(Chunk.TABBING);
			headingParagraph.add("MOP TYPE");

			document.add(headingParagraph);

			headingParagraph = new Paragraph(transaction.getOrderId(), subCatFont);
			headingParagraph.setTabSettings(new TabSettings(150f));
			headingParagraph.add(Chunk.TABBING);
			headingParagraph.add(changeDateFormat(transaction.gettDate()));
			headingParagraph.setTabSettings(new TabSettings(150f));
			headingParagraph.add(Chunk.TABBING);
			if (StringUtils.isNotBlank(paymentType)) {
				headingParagraph.add(paymentType);
			} else {
				headingParagraph.add("NA");
			}
			headingParagraph.setTabSettings(new TabSettings(150f));
			headingParagraph.add(Chunk.TABBING);
			if (StringUtils.isNotBlank(transaction.getMopType())) {
				headingParagraph.add(MopType.getmopName(transaction.getMopType()));
			} else {
				headingParagraph.add("NA");
			}
			document.add(headingParagraph);

			headingParagraph = new Paragraph(Chunk.NEWLINE + "TRANSACTION STATUS", catFont);
			headingParagraph.setTabSettings(new TabSettings(132f));
			if (StringUtils.isNotBlank(paymentType)) {

				if (paymentType.equalsIgnoreCase("Credit Card") || paymentType.equalsIgnoreCase("Debit Card")) {

					headingParagraph.add(Chunk.TABBING);
					headingParagraph.add("CARD NUMBER");
					headingParagraph.setTabSettings(new TabSettings(132f));
					headingParagraph.add(Chunk.TABBING);
					headingParagraph.add("PG REF NO");
					headingParagraph.setTabSettings(new TabSettings(150f));
					headingParagraph.add(Chunk.TABBING);
					headingParagraph.add("");
					document.add(headingParagraph);

					headingParagraph = new Paragraph(transaction.getStatus(), subCatFont);
					headingParagraph.setTabSettings(new TabSettings(150f));
					headingParagraph.add(Chunk.TABBING);
					headingParagraph.add(transaction.getCardNumber());
					headingParagraph.setTabSettings(new TabSettings(150f));
					headingParagraph.add(Chunk.TABBING);
					headingParagraph.add(transaction.getPgRefNum());

				} else {
					headingParagraph.add(Chunk.TABBING);
					headingParagraph.add("CARD NUMBER");
					headingParagraph.setTabSettings(new TabSettings(132f));
					headingParagraph.add(Chunk.TABBING);
					headingParagraph.add("PG REF NO");
					headingParagraph.setTabSettings(new TabSettings(150f));
					headingParagraph.add(Chunk.TABBING);
					headingParagraph.add("");
					document.add(headingParagraph);

					headingParagraph = new Paragraph(transaction.getStatus(), subCatFont);
					headingParagraph.setTabSettings(new TabSettings(150f));
					headingParagraph.add(Chunk.TABBING);
					headingParagraph.add("NA");
					headingParagraph.setTabSettings(new TabSettings(150f));
					headingParagraph.add(Chunk.TABBING);
					headingParagraph.add(transaction.getPgRefNum());
					headingParagraph.setTabSettings(new TabSettings(150f));
					headingParagraph.add(Chunk.TABBING);
					headingParagraph.add("");
				}
			} else {
				document.add(headingParagraph);
				headingParagraph = new Paragraph(transaction.getStatus(), subCatFont);
			}
			document.add(headingParagraph);

		}

		LineSeparator ls = new LineSeparator();

		document.add(new Chunk(ls));
		document.add(Chunk.NEWLINE);
		document.add(Chunk.NEWLINE);

		// Customer Details
		categoryParagraph = new Paragraph("BILL TO,", catFont);
		document.add(categoryParagraph);
		document.add(Chunk.NEWLINE);
		if (StringUtils.isNotBlank(customerDetails.getCustName())) {
			custNameParagraph = new Paragraph(customerDetails.getCustName() + ",", catFont);
			document.add(custNameParagraph);
		} else {
			custNameParagraph = new Paragraph("Name: NA");
			document.add(custNameParagraph);
		}
		if (StringUtils.isNotBlank(customerDetails.getCustStreetAddress1())) {
			custDetailsParagraph = new Paragraph(customerDetails.getCustStreetAddress1(), subCatFont);
			if (StringUtils.isNotBlank(customerDetails.getCustCity())) {
				custDetailsParagraph.add(Chunk.NEWLINE + customerDetails.getCustCity());
			}

			if (StringUtils.isNotBlank(customerDetails.getCustState())) {
				custDetailsParagraph
						.add(Chunk.NEWLINE + customerDetails.getCustState() + ", " + customerDetails.getCustZip());
			}
			document.add(custDetailsParagraph);
		}

		document.add(Chunk.NEWLINE);

		// invoice Duration and Contact to customer details

		PdfPTable dataTable = new PdfPTable(3);
		dataTable.getDefaultCell().setBorder(0);
		float[] columnWidths = new float[] { 500f, 20f, 500f };
		dataTable.setWidths(columnWidths);
		dataTable.setWidthPercentage(100f);

		if (StringUtils.isNotBlank(customerDetails.getDurationFrom())) {
			custDetailsParagraph = new Paragraph("Duration From: " + customerDetails.getDurationFrom(), subCatFont);
			dataTable.addCell(custDetailsParagraph);
			dataTable.addCell("");
		}

		if (StringUtils.isNotBlank(customerDetails.getDurationTo())) {
			custDetailsParagraph = new Paragraph("Duration To: " + customerDetails.getDurationTo(), subCatFont);
			dataTable.addCell(custDetailsParagraph);
		}
		if (StringUtils.isNotBlank(customerDetails.getCustPhone())) {
			custDetailsParagraph = new Paragraph("Mobile: " + customerDetails.getCustPhone(), subCatFont);
			dataTable.addCell(custDetailsParagraph);
			dataTable.addCell("");
		} else {
			custDetailsParagraph = new Paragraph("Mobile No: ", subCatFont);
			dataTable.addCell(custDetailsParagraph);
			dataTable.addCell("");
		}

		if (StringUtils.isNotBlank(transaction.getCustomerEmail())) {
			custDetailsParagraph = new Paragraph("Email ID: " + transaction.getCustomerEmail(), subCatFont);
			dataTable.addCell(custDetailsParagraph);

		} else {
			custDetailsParagraph = new Paragraph("Email ID: ", subCatFont);
			dataTable.addCell(custDetailsParagraph);

		}

		document.add(dataTable);
		document.add(Chunk.NEWLINE);
		document.add(Chunk.NEWLINE);
		LineSeparator ls1 = new LineSeparator();
		document.add(new Chunk(ls1));

		// Invoice Details

		custDetailsParagraph = new Paragraph("INVOICE DETAILS", catFont);
		custDetailsParagraph.setAlignment(Element.ALIGN_CENTER);
		document.add(custDetailsParagraph);
		document.add(Chunk.NEWLINE);

		float[] pointColumnWidths = { 90F, 230F, 90F, 80F };

		PdfPTable invoiceTable = new PdfPTable(4);
		invoiceTable.setWidths(pointColumnWidths);
		invoiceTable.setWidthPercentage(100f);

		PdfPCell headerProName = new PdfPCell(new Phrase("Product Name", tableHeaderFont));
		headerProName.setBackgroundColor(BaseColor.LIGHT_GRAY);
		headerProName.setHorizontalAlignment(Element.ALIGN_CENTER);
		headerProName.setVerticalAlignment(Element.ALIGN_CENTER);
		headerProName.setFixedHeight(25f);
		headerProName.setBorderWidthRight(0f);
		invoiceTable.addCell(headerProName);

		PdfPCell headerDesc = new PdfPCell(new Phrase("Description", tableHeaderFont));
		headerDesc.setBackgroundColor(BaseColor.LIGHT_GRAY);
		headerDesc.setHorizontalAlignment(Element.ALIGN_CENTER);
		headerDesc.setVerticalAlignment(Element.ALIGN_CENTER);
		headerDesc.setFixedHeight(25f);
		headerDesc.setBorderWidthRight(0f);
		invoiceTable.addCell(headerDesc);

		PdfPCell headerQuantity = new PdfPCell(new Phrase("Quantity", tableHeaderFont));
		headerQuantity.setBackgroundColor(BaseColor.LIGHT_GRAY);
		headerQuantity.setHorizontalAlignment(Element.ALIGN_CENTER);
		headerQuantity.setVerticalAlignment(Element.ALIGN_CENTER);
		headerQuantity.setFixedHeight(25f);
		headerQuantity.setBorderWidthRight(0f);
		invoiceTable.addCell(headerQuantity);

		PdfPCell headerAmount = new PdfPCell(new Phrase("Amount", tableHeaderFont));
		headerAmount.setBackgroundColor(BaseColor.LIGHT_GRAY);
		headerAmount.setHorizontalAlignment(Element.ALIGN_CENTER);
		headerAmount.setVerticalAlignment(Element.ALIGN_CENTER);
		headerAmount.setFixedHeight(25f);
		invoiceTable.addCell(headerAmount);

		if (StringUtils.isNotBlank(transaction.getProductName())) {
			PdfPCell pc1 = new PdfPCell(new Phrase(transaction.getProductName(), subCatFont));
			pc1.setFixedHeight(30f);
			pc1.setBorderWidthRight(0f);
			pc1.setBorderWidthTop(0f);
			pc1.setPaddingTop(Element.ALIGN_MIDDLE);
			pc1.setVerticalAlignment(Element.ALIGN_CENTER);
			pc1.setHorizontalAlignment(Element.ALIGN_CENTER);
			invoiceTable.addCell(new PdfPCell(pc1));
		} else {
			PdfPCell pc1 = new PdfPCell(new Phrase("NA", subCatFont));
			pc1.setFixedHeight(30f);
			pc1.setBorderWidthRight(0f);
			pc1.setBorderWidthTop(0f);
			pc1.setPaddingTop(Element.ALIGN_MIDDLE);
			pc1.setVerticalAlignment(Element.ALIGN_CENTER);
			pc1.setHorizontalAlignment(Element.ALIGN_CENTER);
			invoiceTable.addCell(new PdfPCell(pc1)).setHorizontalAlignment(Element.ALIGN_CENTER);
		}

		if (StringUtils.isNotBlank(transaction.getProductDesc())) {
			PdfPCell pc1 = new PdfPCell(new Phrase(transaction.getProductDesc(), subCatFont));
			pc1.setFixedHeight(30f);
			pc1.setVerticalAlignment(Element.ALIGN_CENTER);
			pc1.setHorizontalAlignment(Element.ALIGN_CENTER);
			pc1.setPaddingTop(Element.ALIGN_MIDDLE);
			pc1.setBorderWidthRight(0f);
			pc1.setBorderWidthTop(0f);
			invoiceTable.addCell(new PdfPCell(pc1)).setHorizontalAlignment(Element.ALIGN_CENTER);
		} else {
			PdfPCell pc1 = new PdfPCell(new Phrase("NA", subCatFont));
			pc1.setFixedHeight(30f);
			pc1.setBorderWidthRight(0f);
			pc1.setBorderWidthTop(0f);
			pc1.setVerticalAlignment(Element.ALIGN_CENTER);
			pc1.setPaddingTop(Element.ALIGN_MIDDLE);
			pc1.setHorizontalAlignment(Element.ALIGN_CENTER);
			invoiceTable.addCell(new PdfPCell(pc1)).setHorizontalAlignment(Element.ALIGN_CENTER);
		}

		if (StringUtils.isNotBlank(transaction.getQuantity())) {
			PdfPCell pc1 = new PdfPCell(new Phrase(transaction.getQuantity(), subCatFont));
			pc1.setFixedHeight(30f);
			pc1.setBorderWidthRight(0f);
			pc1.setBorderWidthTop(0f);
			pc1.setVerticalAlignment(Element.ALIGN_CENTER);
			pc1.setPaddingTop(Element.ALIGN_MIDDLE);
			pc1.setHorizontalAlignment(Element.ALIGN_CENTER);
			invoiceTable.addCell(new PdfPCell(pc1)).setHorizontalAlignment(Element.ALIGN_CENTER);
		} else {
			PdfPCell pc1 = new PdfPCell(new Phrase("NA", subCatFont));
			pc1.setFixedHeight(30f);
			pc1.setBorderWidthRight(0f);
			pc1.setBorderWidthTop(0f);
			pc1.setVerticalAlignment(Element.ALIGN_CENTER);
			pc1.setPaddingTop(Element.ALIGN_MIDDLE);
			pc1.setHorizontalAlignment(Element.ALIGN_CENTER);
			invoiceTable.addCell(new PdfPCell(pc1)).setHorizontalAlignment(Element.ALIGN_CENTER);
		}

		if (StringUtils.isNotBlank(transaction.getAmount())) {
			PdfPCell pc1 = new PdfPCell(new Phrase(transaction.getAmount(), subCatFont));
			pc1.setFixedHeight(30f);
			pc1.setBorderWidthTop(0f);
			pc1.setVerticalAlignment(Element.ALIGN_CENTER);
			pc1.setPaddingTop(Element.ALIGN_MIDDLE);
			pc1.setHorizontalAlignment(Element.ALIGN_CENTER);
			invoiceTable.addCell(new PdfPCell(pc1)).setHorizontalAlignment(Element.ALIGN_CENTER);
		} else {
			PdfPCell pc1 = new PdfPCell(new Phrase("NA", subCatFont));
			pc1.setFixedHeight(30f);
			pc1.setBorderWidthTop(0f);
			pc1.setVerticalAlignment(Element.ALIGN_CENTER);
			pc1.setPaddingTop(Element.ALIGN_MIDDLE);
			pc1.setHorizontalAlignment(Element.ALIGN_CENTER);
			invoiceTable.addCell(new PdfPCell(pc1)).setHorizontalAlignment(Element.ALIGN_CENTER);
		}
		document.add(invoiceTable);

		float[] pointColumnWidths1 = { 90F, 80F };
		PdfPTable invoiceTable1 = new PdfPTable(2);
		invoiceTable1.setWidths(pointColumnWidths1);
		invoiceTable1.setWidthPercentage(34.7f);
		invoiceTable1.setHorizontalAlignment(Element.ALIGN_RIGHT);

		invoiceTable1.getDefaultCell().setBackgroundColor(BaseColor.LIGHT_GRAY);
		invoiceTable1.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
		invoiceTable1.getDefaultCell().setFixedHeight(20f);

		if (StringUtils.isNotBlank(transaction.getAmount())) {

			PdfPCell pc1 = new PdfPCell(new Phrase("SUBTOTAL", subCatFont));
			pc1.setFixedHeight(25f);
			pc1.setBorderWidthRight(0f);
			pc1.setBorderWidthTop(0f);
			pc1.setPaddingTop(Element.ALIGN_MIDDLE);
			pc1.setVerticalAlignment(Element.ALIGN_CENTER);
			pc1.setHorizontalAlignment(Element.ALIGN_CENTER);
			invoiceTable1.addCell(new PdfPCell(pc1)).setHorizontalAlignment(Element.ALIGN_CENTER);
			PdfPCell pc2 = new PdfPCell(new Phrase(transaction.getAmount(), subCatFont));
			pc2.setFixedHeight(25f);
			pc2.setPaddingTop(Element.ALIGN_MIDDLE);
			pc2.setBorderWidthTop(0f);
			pc2.setVerticalAlignment(Element.ALIGN_CENTER);
			invoiceTable1.addCell(new PdfPCell(pc2)).setHorizontalAlignment(Element.ALIGN_CENTER);
		}
		if (StringUtils.isNotBlank(transaction.getAmount()) && StringUtils.isNotBlank(transaction.getTotalAmount())) {

			PdfPCell pc1 = new PdfPCell(new Phrase("CONVENIENCE FEE", subCatFont));
			pc1.setFixedHeight(25f);
			pc1.setBorderWidthRight(0f);
			pc1.setBorderWidthTop(0f);
			pc1.setPaddingTop(Element.ALIGN_MIDDLE);
			pc1.setVerticalAlignment(Element.ALIGN_CENTER);
			invoiceTable1.addCell(new PdfPCell(pc1)).setHorizontalAlignment(Element.ALIGN_CENTER);

			BigDecimal amount = new BigDecimal(transaction.getAmount());
			BigDecimal totalAmount = new BigDecimal(transaction.getTotalAmount());

			BigDecimal totalServiceCharge = totalAmount.subtract(amount).setScale(2);

			PdfPCell pc2 = new PdfPCell(new Phrase(String.valueOf(totalServiceCharge), subCatFont));

			pc2.setFixedHeight(25f);
			pc2.setBorderWidthTop(0f);
			pc2.setPaddingTop(Element.ALIGN_MIDDLE);
			pc2.setVerticalAlignment(Element.ALIGN_CENTER);
			invoiceTable1.addCell(new PdfPCell(pc2)).setHorizontalAlignment(Element.ALIGN_CENTER);
		} /*
			 * else{ PdfPCell pc1 = new PdfPCell(new Phrase("SERVICE CHARGE", subCatFont));
			 * pc1.setFixedHeight(25f); pc1.setBorderWidthRight(0f);
			 * pc1.setBorderWidthTop(0f); pc1.setPaddingTop(Element.ALIGN_MIDDLE);
			 * pc1.setVerticalAlignment(Element.ALIGN_CENTER); invoiceTable1.addCell(new
			 * PdfPCell(pc1)).setHorizontalAlignment(Element.ALIGN_CENTER); PdfPCell pc2 =
			 * new PdfPCell(new Phrase("0.00",subCatFont)); pc2.setFixedHeight(25f);
			 * pc2.setBorderWidthTop(0f); pc2.setPaddingTop(Element.ALIGN_MIDDLE);
			 * pc2.setVerticalAlignment(Element.ALIGN_CENTER); invoiceTable1.addCell(new
			 * PdfPCell(pc2)).setHorizontalAlignment(Element.ALIGN_CENTER); }
			 */
		// Discount is not implemented yet on PG
		/*
		 * if (StringUtils.isNotBlank((String)
		 * orderDetails.get(FieldType.DISCOUNT.getName())) &&
		 * orderDetails.get(FieldType.DISCOUNT_FLAG.getName()).equalsIgnoreCase("I")) {
		 * PdfPCell pc1 = new PdfPCell(new Phrase("DISCOUNT", subCatFont));
		 * pc1.setFixedHeight(25f); pc1.setBorderWidthRight(0f);
		 * pc1.setBorderWidthTop(0f); pc1.setPaddingTop(Element.ALIGN_MIDDLE);
		 * pc1.setVerticalAlignment(Element.ALIGN_CENTER); invoiceTable1.addCell(new
		 * PdfPCell(pc1)).setHorizontalAlignment(Element.ALIGN_CENTER); PdfPCell pc2 =
		 * new PdfPCell(new Phrase( (String)
		 * orderDetails.get(FieldType.DISCOUNT.getName()), subCatFont));
		 * pc2.setFixedHeight(25f); pc2.setBorderWidthTop(0f);
		 * pc2.setPaddingTop(Element.ALIGN_MIDDLE);
		 * pc2.setVerticalAlignment(Element.ALIGN_CENTER); invoiceTable1.addCell(new
		 * PdfPCell(pc2)).setHorizontalAlignment(Element.ALIGN_CENTER); }
		 */
		if (StringUtils.isNotBlank(transaction.getTotalAmount())) {
			PdfPCell pc1 = new PdfPCell(new Phrase("TOTAL AMOUNT", subCatFont));
			pc1.setFixedHeight(25f);
			pc1.setBorderWidthRight(0f);
			pc1.setBorderWidthTop(0f);
			pc1.setPaddingTop(Element.ALIGN_MIDDLE);
			pc1.setVerticalAlignment(Element.ALIGN_CENTER);
			invoiceTable1.addCell(new PdfPCell(pc1)).setHorizontalAlignment(Element.ALIGN_CENTER);
			PdfPCell pc2 = new PdfPCell(new Phrase(transaction.getTotalAmount(), subCatFont));
			pc2.setFixedHeight(25f);
			pc2.setBorderWidthTop(0f);
			pc2.setPaddingTop(Element.ALIGN_MIDDLE);
			pc2.setVerticalAlignment(Element.ALIGN_CENTER);

			invoiceTable1.addCell(new PdfPCell(pc2)).setHorizontalAlignment(Element.ALIGN_CENTER);
		}

		document.add(invoiceTable1);

		document.add(Chunk.NEWLINE);
		document.add(Chunk.NEWLINE);

		signatoryParagraph = new Paragraph("Thank you for doing business with us.", footerFont);
		signatoryParagraph.setAlignment(Element.ALIGN_CENTER);
		document.add(signatoryParagraph);
		if (mpa != null) {
			if (StringUtils.isNotBlank(mpa.getMerchantSupportMobileNumber())
					&& StringUtils.isNotBlank(mpa.getMerchantSupportEmailId())) {
				footerParagraph = new Paragraph(Chunk.NEWLINE + "For any queries feel free to connect with us at "
						+ mpa.getMerchantSupportMobileNumber() + ". You may also drop your query to us at "
						+ mpa.getMerchantSupportEmailId() + ".", footerFont);
				footerParagraph.setAlignment(Element.ALIGN_CENTER);
				document.add(footerParagraph);
			} else {
				footerParagraph = new Paragraph(Chunk.NEWLINE + "For any queries feel free to connect with us at "
						+ user.getMobile() + ". You may also drop your query to us at " + user.getEmailId() + ".",
						footerFont);
				footerParagraph.setAlignment(Element.ALIGN_CENTER);
				document.add(footerParagraph);
			}
		} else if (user != null) {
			footerParagraph = new Paragraph(Chunk.NEWLINE + "For any queries feel free to connect with us at "
					+ user.getMobile() + ". You may also drop your query to us at " + user.getEmailId() + ".",
					footerFont);
			footerParagraph.setAlignment(Element.ALIGN_CENTER);
			document.add(footerParagraph);
		}

		document.add(Chunk.NEWLINE);
		document.add(Chunk.NEWLINE);
		document.add(Chunk.NEWLINE);

		// Footer
		float[] columnWidthsForFooter = new float[] { 150f, 70f };
		PdfPTable pdfFooterTable = new PdfPTable(2);
		pdfFooterTable.setWidths(columnWidthsForFooter);
		pdfFooterTable.getDefaultCell().setBorderWidth(0f);
		pdfFooterTable.setHorizontalAlignment(Element.ALIGN_BOTTOM);
		pdfFooterTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
		pdfFooterTable.setWidthPercentage(30f);

		footerParagraph = new Paragraph("Powered by ", subCatFont);
		PdfPCell footerPara = new PdfPCell(footerParagraph);
		footerPara.setPaddingTop(10f);
		footerPara.setPaddingRight(0f);
		footerPara.setBorder(Rectangle.NO_BORDER);
		footerPara.setFixedHeight(30f);
		pdfFooterTable.addCell(new PdfPCell(footerPara)).setHorizontalAlignment(Element.ALIGN_RIGHT);

		Image img2 = Image.getInstance(PropertiesManager.propertiesMap.get("footerLogo"));
		img2.scaleToFit(50f, 100f);
		/*
		 * logoParagraph = new Paragraph(); //img2.setAbsolutePosition(470f,
		 * Element.ALIGN_BOTTOM); if (logoParagraph == null) { logoParagraph.add(img2);
		 * } else { logoParagraph.add(img2); }
		 */
		PdfPCell cellLogo = new PdfPCell(img2);
		cellLogo.setPaddingTop(10f);
		cellLogo.setPaddingLeft(0f);
		cellLogo.setBorder(Rectangle.NO_BORDER);
		cellLogo.setFixedHeight(30f);
		pdfFooterTable.addCell(new PdfPCell(cellLogo)).setHorizontalAlignment(Element.ALIGN_RIGHT);

		document.add(pdfFooterTable);

		document.close();
	}

	private String getMerchantLogoPath(String payId) {

		try {

			return PropertiesManager.propertiesMap.get("LOGO_FILE_UPLOAD_LOCATION") + "//" + payId + "//" + payId
					+ ".png";

		} catch (Exception e) {
			logger.error("Exception cought Wile saving logoImage File : ", e);
			return "";
		}
	}

	private String changeDateFormat(String dateString) {
		Date date = null;
		String formatteddate = null;
		DateFormat convertFormate = new SimpleDateFormat("dd-MMM-yyyy");
		DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
		try {
			date = inputFormat.parse(dateString);
			formatteddate = convertFormate.format(date);
		} catch (Exception ex) {
			logger.error("Exception ", ex);
		}
		return formatteddate;
	}

	// For Download eNach Registration PDF
	public InputStream createEMandateRegistrationPdf(Map<String, String> eMandateRegistrationMap, File file)
			throws Exception {
		logger.info("Create E-Mandate Registration PDF ");
		Document document = new Document(PageSize.A4, 20, 20, 20, 20);
		FileOutputStream out = new FileOutputStream(file);
		PdfWriter.getInstance(document, out);
		document.open();
		try {
			addEMandateRegistrationContent1(document, eMandateRegistrationMap);
		} catch (IOException e) {
			e.printStackTrace();
		}
		document.close();
		return new FileInputStream(file);
	}

	@SuppressWarnings("static-access")
	private void addEMandateRegistrationContent1(Document document, Map<String, String> eMandateRegistrationMap)
			throws DocumentException, MalformedURLException, IOException, FileNotFoundException {

		Paragraph logoParagraph = null;
		Paragraph titleParagraph = null;

		Paragraph merchantParagraph = null;
		Paragraph custDetailsParagraph = null;
		Paragraph statusParagraph = null;
		Paragraph paymentParagraph = null;
		Paragraph bankDetailsParagraph = null;

		LineSeparator ls = new LineSeparator();
		ls.setPercentage(90f);

		Font statusFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
		Font merchantFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
		statusFont.setColor(BaseColor.DARK_GRAY);

		Font contentFont = new Font(Font.FontFamily.HELVETICA, 10);

		Image img = null;
		String merchantImageAddr = getMerchantLogoPath(eMandateRegistrationMap.get("payId"));

		if (StringUtils.isNotBlank(merchantImageAddr)) {
			File file = new File(merchantImageAddr);
			if (file.exists()) {
				img = Image.getInstance(merchantImageAddr);
			} else {
				img = Image.getInstance(
						PropertiesManager.propertiesMap.get(Constants.CRM_IMAGE_URL.getValue()) + "paymentgateway1.png");
			}
		}

		img.scaleToFit(80f, 80f);
		img.setAbsolutePosition(45f, 750f);
		logoParagraph = new Paragraph();
		logoParagraph.add(img);
		document.add(logoParagraph);

		String merchantName = eMandateRegistrationMap.get("merchantName");
		merchantParagraph = new Paragraph(Chunk.NEWLINE + merchantName, merchantFont);
		merchantParagraph.setAlignment(Element.ALIGN_RIGHT);
		merchantParagraph.setSpacingAfter(40);
		document.add(merchantParagraph);

		titleParagraph = new Paragraph("eNACH Registration Receipt", mercantNameFont);
		titleParagraph.setAlignment(Element.ALIGN_CENTER);
		document.add(titleParagraph);

		document.add(Chunk.NEWLINE);

		document.add(ls);

		if (eMandateRegistrationMap.get("paymentMode").equalsIgnoreCase("Net Banking")
				|| eMandateRegistrationMap.get("paymentMode").equalsIgnoreCase("Debit Card")
				|| eMandateRegistrationMap.get("paymentMode").equalsIgnoreCase(Constants.NA.getValue())) {

			PdfPTable dataTable = new PdfPTable(3);
			dataTable.getDefaultCell().setBorder(0);
			float[] columnWidth = new float[] { 10f, 50f, 40f };
			dataTable.setWidths(columnWidth);
			dataTable.setWidthPercentage(90f);

			custDetailsParagraph = new Paragraph(Chunk.NEWLINE + "Order No: ", contentFont);
			custDetailsParagraph.setAlignment(Paragraph.ALIGN_LEFT);
			dataTable.addCell(custDetailsParagraph);

			custDetailsParagraph = new Paragraph(
					Chunk.NEWLINE + eMandateRegistrationMap.get(FieldType.ORDER_ID.getName()), contentFont);
			custDetailsParagraph.setAlignment(Paragraph.ALIGN_LEFT);
			dataTable.addCell(custDetailsParagraph);

			custDetailsParagraph = new Paragraph(Chunk.NEWLINE + eMandateRegistrationMap.get("accountHolderName"),
					contentFont);
			custDetailsParagraph.setAlignment(Paragraph.ALIGN_RIGHT);
			dataTable.addCell(custDetailsParagraph);

			custDetailsParagraph = new Paragraph(Chunk.NEWLINE);
			dataTable.addCell(custDetailsParagraph);

			custDetailsParagraph = new Paragraph(Chunk.NEWLINE);
			dataTable.addCell(custDetailsParagraph);

			custDetailsParagraph = new Paragraph(
					Chunk.NEWLINE + String.valueOf(fields.fieldMask(eMandateRegistrationMap.get("mobileNumber"))),
					contentFont);
			custDetailsParagraph.setAlignment(Paragraph.ALIGN_RIGHT);
			dataTable.addCell(custDetailsParagraph);

			custDetailsParagraph = new Paragraph(Chunk.NEWLINE);
			dataTable.addCell(custDetailsParagraph);

			custDetailsParagraph = new Paragraph(Chunk.NEWLINE);
			dataTable.addCell(custDetailsParagraph);

			custDetailsParagraph = new Paragraph(
					Chunk.NEWLINE + String.valueOf(fields.maskEmail(eMandateRegistrationMap.get("emailId"))),
					contentFont);
			custDetailsParagraph.setAlignment(Paragraph.ALIGN_RIGHT);
			dataTable.addCell(custDetailsParagraph);

			dataTable.setSpacingAfter(8f);

			document.add(dataTable);

			document.add(ls);

			float[] columnWidths2 = new float[] { 12f, 62f, 12f, 14f };
			PdfPTable dataTable2 = new PdfPTable(4);
			dataTable2.setWidths(columnWidths2);
			dataTable2.getDefaultCell().setBorderWidth(0f);
			dataTable2.setWidthPercentage(90f);

			statusParagraph = new Paragraph(Chunk.NEWLINE + "Txn No: ", statusFont);
			statusParagraph.setAlignment(Element.ALIGN_LEFT);
			dataTable2.addCell(statusParagraph);

			statusParagraph = new Paragraph(Chunk.NEWLINE + eMandateRegistrationMap.get(FieldType.PG_REF_NUM.getName()),
					statusFont);
			statusParagraph.setAlignment(Element.ALIGN_LEFT);
			dataTable2.addCell(statusParagraph);

			statusParagraph = new Paragraph(Chunk.NEWLINE + "Status: ", statusFont);
			statusParagraph.setAlignment(Element.ALIGN_RIGHT);
			dataTable2.addCell(statusParagraph);

			statusParagraph = new Paragraph(Chunk.NEWLINE + eMandateRegistrationMap.get("status"), statusFont);
			statusParagraph.setAlignment(Element.ALIGN_LEFT);
			dataTable2.addCell(statusParagraph);

			document.add(dataTable2);
			
			document.add(Chunk.NEWLINE);

			float[] columnWidths3 = new float[] { 25f, 25f, 25f, 25f };
			PdfPTable dataTable3 = new PdfPTable(4);
			dataTable3.setWidths(columnWidths3);
			dataTable3.getDefaultCell().setBorderWidth(0f);
			dataTable3.setWidthPercentage(90f);

			paymentParagraph = new Paragraph(Chunk.NEWLINE);
			dataTable3.addCell(paymentParagraph);

			paymentParagraph = new Paragraph(Chunk.NEWLINE + "Registration Amount: ", contentFont);
			paymentParagraph.setAlignment(Element.ALIGN_LEFT);
			paymentParagraph.setSpacingBefore(10);
			dataTable3.addCell(paymentParagraph);

			paymentParagraph = new Paragraph(Chunk.NEWLINE + String.valueOf(eMandateRegistrationMap.get("amount")),
					contentFont);
			paymentParagraph.setAlignment(Element.ALIGN_RIGHT);
			dataTable3.addCell(paymentParagraph);

			paymentParagraph = new Paragraph(Chunk.NEWLINE);
			dataTable3.addCell(paymentParagraph);

			paymentParagraph = new Paragraph(Chunk.NEWLINE);
			dataTable3.addCell(paymentParagraph);

			paymentParagraph = new Paragraph(Chunk.NEWLINE + "Frequency: ", contentFont);
			paymentParagraph.setAlignment(Element.ALIGN_LEFT);
			dataTable3.addCell(paymentParagraph);

			paymentParagraph = new Paragraph(
					Chunk.NEWLINE + Frequency.getFrequencyName(eMandateRegistrationMap.get("frequency")), contentFont);
			paymentParagraph.setAlignment(Element.ALIGN_RIGHT);
			dataTable3.addCell(paymentParagraph);

			paymentParagraph = new Paragraph(Chunk.NEWLINE);
			dataTable3.addCell(paymentParagraph);

			paymentParagraph = new Paragraph(Chunk.NEWLINE);
			dataTable3.addCell(paymentParagraph);

			paymentParagraph = new Paragraph(Chunk.NEWLINE + "Tenure: ", contentFont);
			paymentParagraph.setAlignment(Element.ALIGN_LEFT);
			dataTable3.addCell(paymentParagraph);

			paymentParagraph = new Paragraph(Chunk.NEWLINE + eMandateRegistrationMap.get("tenure"), contentFont);
			paymentParagraph.setAlignment(Element.ALIGN_RIGHT);
			dataTable3.addCell(paymentParagraph);

			paymentParagraph = new Paragraph(Chunk.NEWLINE);
			dataTable3.addCell(paymentParagraph);

			paymentParagraph = new Paragraph(Chunk.NEWLINE);
			dataTable3.addCell(paymentParagraph);

			paymentParagraph = new Paragraph(Chunk.NEWLINE + "Start Date: ", contentFont);
			paymentParagraph.setAlignment(Element.ALIGN_LEFT);
			dataTable3.addCell(paymentParagraph);

			paymentParagraph = new Paragraph(Chunk.NEWLINE + eMandateRegistrationMap.get("startDate"), contentFont);
			paymentParagraph.setAlignment(Element.ALIGN_RIGHT);
			dataTable3.addCell(paymentParagraph);

			paymentParagraph = new Paragraph(Chunk.NEWLINE);
			dataTable3.addCell(paymentParagraph);

			paymentParagraph = new Paragraph(Chunk.NEWLINE);
			dataTable3.addCell(paymentParagraph);

			paymentParagraph = new Paragraph(Chunk.NEWLINE + "End Date: ", contentFont);
			paymentParagraph.setAlignment(Element.ALIGN_LEFT);
			dataTable3.addCell(paymentParagraph);

			paymentParagraph = new Paragraph(Chunk.NEWLINE + eMandateRegistrationMap.get("endDate"), contentFont);
			paymentParagraph.setAlignment(Element.ALIGN_RIGHT);
			dataTable3.addCell(paymentParagraph);

			paymentParagraph = new Paragraph(Chunk.NEWLINE);
			dataTable3.addCell(paymentParagraph);

			document.add(dataTable3);
			document.add(Chunk.NEWLINE);

			BaseColor bgColor = new BaseColor(00, 26, 64);
			float[] columnWidths4 = new float[] { 100f };
			PdfPTable dataTable4 = new PdfPTable(1);
			dataTable4.setWidths(columnWidths4);
			dataTable4.getDefaultCell().setBorderWidth(0f);
			dataTable4.setWidthPercentage(90f);

			Font headFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
			headFont.setColor(BaseColor.WHITE);

			PdfPCell headCell = new PdfPCell(new Phrase("Bank Details", headFont));
			headCell.setBackgroundColor(bgColor);
			headCell.setFixedHeight(20f);
			headCell.setBorderWidth(0f);
			headCell.setBorderWidthTop(0.5f);
			headCell.setBorderWidthBottom(1f);
			headCell.setBorderColorTop(BaseColor.WHITE);
			headCell.setPaddingTop(Element.ALIGN_MIDDLE);
			headCell.setVerticalAlignment(Element.ALIGN_CENTER);
			headCell.setHorizontalAlignment(Element.ALIGN_LEFT);

			dataTable4.addCell(new PdfPCell(headCell));
			document.add(dataTable4);

			float[] columnWidths5 = new float[] { 30f, 70f };
			PdfPTable dataTable5 = new PdfPTable(2);
			dataTable5.setWidths(columnWidths5);
			dataTable5.getDefaultCell().setBorderWidth(0f);
			dataTable5.setWidthPercentage(90f);

			bankDetailsParagraph = new Paragraph(Chunk.NEWLINE + "Registration Mode: ", contentFont);
			bankDetailsParagraph.setAlignment(Element.ALIGN_LEFT);
			dataTable5.addCell(bankDetailsParagraph);

			bankDetailsParagraph = new Paragraph(Chunk.NEWLINE + eMandateRegistrationMap.get("paymentMode"),
					contentFont);
			bankDetailsParagraph.setAlignment(Element.ALIGN_LEFT);
			dataTable5.addCell(bankDetailsParagraph);

			bankDetailsParagraph = new Paragraph(Chunk.NEWLINE + "Bank Name: ", contentFont);
			bankDetailsParagraph.setAlignment(Element.ALIGN_LEFT);
			dataTable5.addCell(bankDetailsParagraph);

			bankDetailsParagraph = new Paragraph(Chunk.NEWLINE + eMandateRegistrationMap.get("bankName"), contentFont);
			bankDetailsParagraph.setAlignment(Element.ALIGN_LEFT);
			dataTable5.addCell(bankDetailsParagraph);

			bankDetailsParagraph = new Paragraph(Chunk.NEWLINE + "Account Type: ", contentFont);
			bankDetailsParagraph.setAlignment(Element.ALIGN_LEFT);
			dataTable5.addCell(bankDetailsParagraph);

			bankDetailsParagraph = new Paragraph(Chunk.NEWLINE + eMandateRegistrationMap.get("accountType"),
					contentFont);
			bankDetailsParagraph.setAlignment(Element.ALIGN_LEFT);
			dataTable5.addCell(bankDetailsParagraph);

			bankDetailsParagraph = new Paragraph(Chunk.NEWLINE + "Account Number: ", contentFont);
			bankDetailsParagraph.setAlignment(Element.ALIGN_LEFT);
			dataTable5.addCell(bankDetailsParagraph);

			if (StringUtils.isNotBlank(eMandateRegistrationMap.get("accountNumber"))
					&& !eMandateRegistrationMap.get("accountNumber").equalsIgnoreCase("NA")) {
				bankDetailsParagraph = new Paragraph(
						Chunk.NEWLINE + fields.fieldMask(eMandateRegistrationMap.get("accountNumber")), contentFont);
				bankDetailsParagraph.setAlignment(Element.ALIGN_LEFT);
				dataTable5.addCell(bankDetailsParagraph);
			} else {
				bankDetailsParagraph = new Paragraph(Chunk.NEWLINE + eMandateRegistrationMap.get("accountNumber"),
						contentFont);
				bankDetailsParagraph.setAlignment(Element.ALIGN_LEFT);
				dataTable5.addCell(bankDetailsParagraph);
			}

			bankDetailsParagraph = new Paragraph(Chunk.NEWLINE + "IFSC Code: ", contentFont);
			bankDetailsParagraph.setAlignment(Element.ALIGN_LEFT);
			dataTable5.addCell(bankDetailsParagraph);

			if (StringUtils.isNotBlank(eMandateRegistrationMap.get("ifscCode"))
					&& !eMandateRegistrationMap.get("ifscCode").equalsIgnoreCase(Constants.NA.getValue())) {
				bankDetailsParagraph = new Paragraph(
						Chunk.NEWLINE + String.valueOf(fields.fieldMask(eMandateRegistrationMap.get("ifscCode"))),
						contentFont);
				bankDetailsParagraph.setAlignment(Element.ALIGN_LEFT);
				dataTable5.addCell(bankDetailsParagraph);
			} else {
				bankDetailsParagraph = new Paragraph(Chunk.NEWLINE + eMandateRegistrationMap.get("ifscCode"),
						contentFont);
				bankDetailsParagraph.setAlignment(Element.ALIGN_LEFT);
				dataTable5.addCell(bankDetailsParagraph);
			}

			document.add(dataTable5);
			document.add(Chunk.NEWLINE);

			document.add(ls);

			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			Paragraph footerParagraph = new Paragraph("Please reach us at support@paymentgateway.com in case of queries. ",
					footerFont);
			footerParagraph.setAlignment(Element.ALIGN_RIGHT);
			document.add(footerParagraph);
		}
		document.close();
		logger.info("eNACH Registration Details PDF Succesfully created");
	}

	public InputStream createAutoPayRegistrationPdf(Map<String, String> autoPayMandateRegistrationMap, File file)
			throws Exception {
		logger.info("Create autoPay Mandate Registration PDF ");
		Document document = new Document(PageSize.A4, 20, 20, 20, 20);
		FileOutputStream out = new FileOutputStream(file);
		PdfWriter.getInstance(document, out);
		document.open();
		try {
			addAutoPayMandateRegistrationContent(document, autoPayMandateRegistrationMap);
		} catch (IOException e) {
			logger.error("exception caught in autoPay PDF creater ", e);
		}
		document.close();
		return new FileInputStream(file);
	}

	@SuppressWarnings("static-access")
	private void addAutoPayMandateRegistrationContent(Document document,
			Map<String, String> autoPayMandateRegistrationMap)
			throws DocumentException, MalformedURLException, IOException, FileNotFoundException {

		Font tableFont = new Font(Font.FontFamily.HELVETICA, 12);

		String dataHeadingForNetBanking[] = { "Payment Mode", "Order Id", "Status", "Mobile Number", "Email Id",
				"Registration Amount", "Debit Transaction Amount", "Total Amount", "Frequency", "Tenure",
				" Debit Start Date", "Debit End Date" };
		/*
		 * String dataHeadingForCards[] = { "Registration Mode", "Bank Name",
		 * "CARD NUMBER", "Name On Card", "Mobile Number", "Email Id", "Amount",
		 * "Frequency", "Tenure", "Start Date", "End Date" };
		 */

		Paragraph logoParagraph = null;
		Paragraph titleParagraph = null;

		Image img = null;
		String merchantImageAddr = getMerchantLogoPath(autoPayMandateRegistrationMap.get(FieldType.PAY_ID.getName()));

		if (StringUtils.isNotBlank(merchantImageAddr)) {
			File file = new File(merchantImageAddr);
			if (file.exists()) {
				img = Image.getInstance(merchantImageAddr);
			} else {

				if (StringUtils.isNotBlank(autoPayMandateRegistrationMap.get("logo"))
						&& !autoPayMandateRegistrationMap.get("logo").equalsIgnoreCase(Constants.NA.getValue())) {
					img = Image.getInstance(autoPayMandateRegistrationMap.get("logo"));
				} else {
					img = Image.getInstance(
							PropertiesManager.propertiesMap.get(Constants.CRM_IMAGE_URL.getValue()) + "paymentgateway1.png");
				}

			}
		}

		img.scaleToFit(100f, 100f);
		img.setAbsolutePosition(20f, 740f);
		logoParagraph = new Paragraph();
		logoParagraph.add(img);
		logoParagraph.add(Chunk.NEWLINE);
		logoParagraph.add(Chunk.NEWLINE);
		document.add(logoParagraph);

		titleParagraph = new Paragraph("UPI AutoPay Registration Details", mercantNameFont);
		titleParagraph.setAlignment(Element.ALIGN_CENTER);
		document.add(titleParagraph);

		LineSeparator ls1 = new LineSeparator();
		document.add(new Chunk(ls1));

		if (autoPayMandateRegistrationMap.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase("UP")
				|| autoPayMandateRegistrationMap.get(FieldType.PAYMENT_TYPE.getName())
						.equalsIgnoreCase(Constants.NA.getValue())) {
			float netBankingTableWidth = 100f;
			// float[] netBankingColWidths = { 1f, 1.1f, 1.4f, 1.6f, 1.8f, 1f, 1.4f, 1.8f,
			// 1.3f, 1.8f, 1.2f, 1.5f, 1.5f};
			float[] netBankingColWidths = { 3f, 3f };
			PdfPTable netBankingConfigTableData = new PdfPTable(netBankingColWidths);
			netBankingConfigTableData.setWidthPercentage(netBankingTableWidth);
			int count = 0;

			PdfPCell paymentModeHeadCell = new PdfPCell(new Phrase(dataHeadingForNetBanking[count++], tableFont));
			paymentModeHeadCell.setBackgroundColor(BaseColor.GRAY.LIGHT_GRAY);
			paymentModeHeadCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			netBankingConfigTableData.addCell(paymentModeHeadCell);

			PdfPCell paymentModeCellData = new PdfPCell(new Phrase(
					PaymentType.getpaymentName(autoPayMandateRegistrationMap.get(FieldType.PAYMENT_TYPE.getName())),
					tableFont));
			paymentModeCellData.setFixedHeight(30f);
			netBankingConfigTableData.addCell(new PdfPCell(paymentModeCellData))
					.setHorizontalAlignment(Element.ALIGN_CENTER);

			PdfPCell txnIdHeadCell = new PdfPCell(new Phrase(dataHeadingForNetBanking[count++], tableFont));
			txnIdHeadCell.setBackgroundColor(BaseColor.GRAY.LIGHT_GRAY);
			txnIdHeadCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			netBankingConfigTableData.addCell(txnIdHeadCell);

			PdfPCell txnIdCellData = new PdfPCell(
					new Phrase(autoPayMandateRegistrationMap.get(FieldType.ORDER_ID.getName()), tableFont));
			txnIdCellData.setFixedHeight(30f);
			netBankingConfigTableData.addCell(new PdfPCell(txnIdCellData)).setHorizontalAlignment(Element.ALIGN_CENTER);

			PdfPCell statusHeadCell = new PdfPCell(new Phrase(dataHeadingForNetBanking[count++], tableFont));
			statusHeadCell.setBackgroundColor(BaseColor.GRAY.LIGHT_GRAY);
			statusHeadCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			netBankingConfigTableData.addCell(statusHeadCell);

			PdfPCell statusCellData = new PdfPCell(
					new Phrase(autoPayMandateRegistrationMap.get(FieldType.STATUS.getName()), tableFont));
			statusCellData.setFixedHeight(30f);
			netBankingConfigTableData.addCell(new PdfPCell(statusCellData))
					.setHorizontalAlignment(Element.ALIGN_CENTER);

			PdfPCell bankNameHeadCell = new PdfPCell(new Phrase(dataHeadingForNetBanking[count++], tableFont));
			bankNameHeadCell.setBackgroundColor(BaseColor.GRAY.LIGHT_GRAY);
			bankNameHeadCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			netBankingConfigTableData.addCell(bankNameHeadCell);

			PdfPCell mobileNumberCellData = new PdfPCell(new Phrase(
					String.valueOf(fields.fieldMask(autoPayMandateRegistrationMap.get(FieldType.CUST_PHONE.getName()))),
					tableFont));
			mobileNumberCellData.setFixedHeight(30f);
			netBankingConfigTableData.addCell(new PdfPCell(mobileNumberCellData))
					.setHorizontalAlignment(Element.ALIGN_CENTER);

			PdfPCell emailIdHeadCell = new PdfPCell(new Phrase(dataHeadingForNetBanking[count++], tableFont));
			emailIdHeadCell.setBackgroundColor(BaseColor.GRAY.LIGHT_GRAY);
			emailIdHeadCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			netBankingConfigTableData.addCell(emailIdHeadCell);

			PdfPCell emailIdCellData = new PdfPCell(new Phrase(
					String.valueOf(fields.maskEmail(autoPayMandateRegistrationMap.get(FieldType.CUST_EMAIL.getName()))),
					tableFont));
			emailIdCellData.setFixedHeight(30f);
			netBankingConfigTableData.addCell(new PdfPCell(emailIdCellData))
					.setHorizontalAlignment(Element.ALIGN_CENTER);

			PdfPCell amountHeadCell = new PdfPCell(new Phrase(dataHeadingForNetBanking[count++], tableFont));
			amountHeadCell.setBackgroundColor(BaseColor.GRAY.LIGHT_GRAY);
			amountHeadCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			netBankingConfigTableData.addCell(amountHeadCell);

			PdfPCell amountCellData = new PdfPCell(new Phrase(
					String.valueOf(autoPayMandateRegistrationMap.get(FieldType.AMOUNT.getName())), tableFont));
			amountCellData.setFixedHeight(30f);
			netBankingConfigTableData.addCell(new PdfPCell(amountCellData))
					.setHorizontalAlignment(Element.ALIGN_CENTER);

			PdfPCell debitAmountHeadCell = new PdfPCell(new Phrase(
					dataHeadingForNetBanking[count++] + " ("
							+ AutoPayFrequency.getAutoPayFrequencyName(
									autoPayMandateRegistrationMap.get(FieldType.FREQUENCY.getName()))
							+ ")",
					tableFont));
			debitAmountHeadCell.setBackgroundColor(BaseColor.GRAY.LIGHT_GRAY);
			debitAmountHeadCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			netBankingConfigTableData.addCell(debitAmountHeadCell);

			PdfPCell debitAmountCellData = new PdfPCell(new Phrase(
					String.valueOf(autoPayMandateRegistrationMap.get(FieldType.MONTHLY_AMOUNT.getName())), tableFont));
			debitAmountCellData.setFixedHeight(30f);
			netBankingConfigTableData.addCell(new PdfPCell(debitAmountCellData))
					.setHorizontalAlignment(Element.ALIGN_CENTER);

			PdfPCell totalAmountHeadCell = new PdfPCell(new Phrase(dataHeadingForNetBanking[count++], tableFont));
			totalAmountHeadCell.setBackgroundColor(BaseColor.GRAY.LIGHT_GRAY);
			totalAmountHeadCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			netBankingConfigTableData.addCell(totalAmountHeadCell);

			PdfPCell totalAmountCellData = new PdfPCell(new Phrase(
					String.valueOf(autoPayMandateRegistrationMap.get(FieldType.TOTAL_AMOUNT.getName())), tableFont));
			totalAmountCellData.setFixedHeight(30f);
			netBankingConfigTableData.addCell(new PdfPCell(totalAmountCellData))
					.setHorizontalAlignment(Element.ALIGN_CENTER);

			PdfPCell frequencyHeadCell = new PdfPCell(new Phrase(dataHeadingForNetBanking[count++], tableFont));
			frequencyHeadCell.setBackgroundColor(BaseColor.GRAY.LIGHT_GRAY);
			frequencyHeadCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			netBankingConfigTableData.addCell(frequencyHeadCell);

			PdfPCell frequencyCellData = new PdfPCell(new Phrase(
					String.valueOf(AutoPayFrequency
							.getAutoPayFrequencyName(autoPayMandateRegistrationMap.get(FieldType.FREQUENCY.getName()))),
					tableFont));
			frequencyCellData.setFixedHeight(30f);
			netBankingConfigTableData.addCell(new PdfPCell(frequencyCellData))
					.setHorizontalAlignment(Element.ALIGN_CENTER);

			PdfPCell tenureHeadCell = new PdfPCell(new Phrase(
					dataHeadingForNetBanking[count++] + " ("
							+ AutoPayFrequency.getAutoPayFrequencyName(
									autoPayMandateRegistrationMap.get(FieldType.FREQUENCY.getName()))
							+ ")",
					tableFont));
			tenureHeadCell.setBackgroundColor(BaseColor.GRAY.LIGHT_GRAY);
			tenureHeadCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			netBankingConfigTableData.addCell(tenureHeadCell);

			PdfPCell tenureCellData = new PdfPCell(new Phrase(
					String.valueOf(autoPayMandateRegistrationMap.get(FieldType.TENURE.getName())), tableFont));
			tenureCellData.setFixedHeight(30f);
			netBankingConfigTableData.addCell(new PdfPCell(tenureCellData))
					.setHorizontalAlignment(Element.ALIGN_CENTER);

			PdfPCell startDateHeadCell = new PdfPCell(new Phrase(dataHeadingForNetBanking[count++], tableFont));
			startDateHeadCell.setBackgroundColor(BaseColor.GRAY.LIGHT_GRAY);
			startDateHeadCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			netBankingConfigTableData.addCell(startDateHeadCell);

			PdfPCell startDateCellData = new PdfPCell(new Phrase(
					String.valueOf(autoPayMandateRegistrationMap.get(FieldType.DATE_FROM.getName())), tableFont));
			startDateCellData.setFixedHeight(30f);
			netBankingConfigTableData.addCell(new PdfPCell(startDateCellData))
					.setHorizontalAlignment(Element.ALIGN_CENTER);

			PdfPCell endDateHeadCell = new PdfPCell(new Phrase(dataHeadingForNetBanking[count++], tableFont));
			endDateHeadCell.setBackgroundColor(BaseColor.GRAY.LIGHT_GRAY);
			endDateHeadCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			netBankingConfigTableData.addCell(endDateHeadCell);

			PdfPCell endDateCellData = new PdfPCell(new Phrase(
					String.valueOf(autoPayMandateRegistrationMap.get(FieldType.DATE_TO.getName())), tableFont));
			endDateCellData.setFixedHeight(30f);
			netBankingConfigTableData.addCell(new PdfPCell(endDateCellData))
					.setHorizontalAlignment(Element.ALIGN_CENTER);

			document.add(netBankingConfigTableData);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			Paragraph footerParagraph = new Paragraph("Please reach us at support@paymentgateway.com in case of queries. ",
					footerFont);
			footerParagraph.setAlignment(Element.ALIGN_RIGHT);
			document.add(footerParagraph);
		}
		document.close();
		logger.info("upi autoPay Registration Details PDF Succesfully created");
	}

	public InputStream createStaticUpiQrPdf(CustomerQR customerQR, File file, String payId) throws Exception {
		logger.info("Create Static UPI QR PDF ");
		Document document = new Document(PageSize.A5, 10, 10, 10, 10);
		FileOutputStream out = new FileOutputStream(file);
		PdfWriter.getInstance(document, out);
		document.open();
		try {
			Rectangle rectangle = new Rectangle(22, 20, 400, 580);
			rectangle.setBorderColor(BaseColor.BLACK);
			rectangle.setBorder(Rectangle.BOX);
			rectangle.setBorderWidth(1);
			document.add(rectangle);
			addStaticUpiQRContent(document, customerQR, payId);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception ex) {
			logger.error("exception caught while create static upi qr pdf ", ex);
		}
		document.close();
		return new FileInputStream(file);
	}

	private void addStaticUpiQRContent(Document document, CustomerQR customerQR, String payId)
			throws DocumentException, MalformedURLException, IOException, FileNotFoundException {

		User user = userDao.findPayId(payId);
		Paragraph logoParagraph = null;
		Paragraph titleParagraph = null;

		Image bhimImg = null;
		Image upiImg = null;
		Image logoImg = null;
		Image googlePayPaymentModeImg = null;
		Image paytmPaymentModeImg = null;
		Image phonePePaymentModeImg = null;
		Image qrImage = null;

		String batuwaLogo = PropertiesManager.propertiesMap.get(Constants.CRM_IMAGE_URL.getValue()) + "icon-batuwa.png";
		String paymentgatewayLogo = PropertiesManager.propertiesMap.get(Constants.CRM_IMAGE_URL.getValue())
				+ "icon-paymentgateway.png";

		String batuwaResellerId = PropertiesManager.propertiesMap.get("BATUWA_RESELLER_ID");
		String bhimLogo = PropertiesManager.propertiesMap.get(Constants.CRM_IMAGE_URL.getValue()) + "icon-bhim.png";
		String upiLogo = PropertiesManager.propertiesMap.get(Constants.CRM_IMAGE_URL.getValue()) + "icon-upi.png";
		// String scanAndPayLogo =
		// PropertiesManager.propertiesMap.get(Constants.CRM_IMAGE_URL.getValue())+"scan-pay.png";

		String googlePayPaymentModeLogo = PropertiesManager.propertiesMap.get(Constants.CRM_IMAGE_URL.getValue())
				+ "icon-google-pay.png";
		String paytmPaymentModeLogo = PropertiesManager.propertiesMap.get(Constants.CRM_IMAGE_URL.getValue())
				+ "icon-paytm.png";
		String phonePePaymentModeLogo = PropertiesManager.propertiesMap.get(Constants.CRM_IMAGE_URL.getValue())
				+ "icon-phonepe.png";

		String base64QR = customerQR.getUpiQrCode();

		if (StringUtils.isNotBlank(user.getResellerId()) && StringUtils.isNotBlank(batuwaResellerId)
				&& user.getResellerId().equals(batuwaResellerId)) {

			bhimImg = Image.getInstance(bhimLogo);
			bhimImg.scaleToFit(90f, 90f);
			bhimImg.setAbsolutePosition(122f, 520f);
			logoParagraph = new Paragraph();
			logoParagraph.add(bhimImg);
			document.add(logoParagraph);

			upiImg = Image.getInstance(upiLogo);
			upiImg.scaleToFit(80f, 100f);
			upiImg.setAbsolutePosition(214f, 518f);
			logoParagraph = new Paragraph();
			logoParagraph.add(upiImg);
			logoParagraph.add(Chunk.NEWLINE);
			document.add(logoParagraph);

			logoImg = Image.getInstance(batuwaLogo);
			logoImg.scaleToFit(110f, 150f);
			logoImg.setAbsolutePosition(160f, 420f);
			logoParagraph = new Paragraph();
			logoParagraph.add(logoImg);
			document.add(logoParagraph);

			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);

			titleParagraph = new Paragraph(customerQR.getCompanyName(), mercantNameFont);
			titleParagraph.setAlignment(Element.ALIGN_CENTER);
			document.add(titleParagraph);

			qrImage = Image.getInstance(Base64.decodeBase64(base64QR));
			qrImage.scaleToFit(270f, 200f);
			qrImage.setAbsolutePosition(110f, 195f);
			logoParagraph = new Paragraph();
			logoParagraph.add(qrImage);
			document.add(logoParagraph);

			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);

			titleParagraph = new Paragraph(customerQR.getVpa(), mercantNameFont);
			titleParagraph.setAlignment(Element.ALIGN_CENTER);
			document.add(titleParagraph);
			document.add(Chunk.NEWLINE);

			titleParagraph = new Paragraph("Scan & Pay", mercantNameFont);
			titleParagraph.setAlignment(Element.ALIGN_CENTER);
			document.add(titleParagraph);

			titleParagraph = new Paragraph("using any UPI App", mercantNameFont);
			titleParagraph.setAlignment(Element.ALIGN_CENTER);
			document.add(titleParagraph);

			paytmPaymentModeImg = Image.getInstance(paytmPaymentModeLogo);
			paytmPaymentModeImg.scaleToFit(80f, 80f);
			paytmPaymentModeImg.setAbsolutePosition(120f, 60f);
			logoParagraph = new Paragraph();
			logoParagraph.add(paytmPaymentModeImg);
			document.add(Chunk.NEWLINE);
			document.add(logoParagraph);

			googlePayPaymentModeImg = Image.getInstance(googlePayPaymentModeLogo);
			googlePayPaymentModeImg.scaleToFit(36f, 36f);
			googlePayPaymentModeImg.setAbsolutePosition(215f, 55f);
			logoParagraph = new Paragraph();
			logoParagraph.add(googlePayPaymentModeImg);
			document.add(Chunk.NEWLINE);
			document.add(logoParagraph);

			phonePePaymentModeImg = Image.getInstance(phonePePaymentModeLogo);
			phonePePaymentModeImg.scaleToFit(35f, 35f);
			phonePePaymentModeImg.setAbsolutePosition(270f, 55f);
			logoParagraph = new Paragraph();
			logoParagraph.add(phonePePaymentModeImg);
			document.add(logoParagraph);

		} else {

			bhimImg = Image.getInstance(bhimLogo);
			bhimImg.scaleToFit(90f, 90f);
			bhimImg.setAbsolutePosition(122f, 500f);
			logoParagraph = new Paragraph();
			logoParagraph.add(bhimImg);
			document.add(logoParagraph);

			upiImg = Image.getInstance(upiLogo);
			upiImg.scaleToFit(80f, 100f);
			upiImg.setAbsolutePosition(214f, 500f);
			logoParagraph = new Paragraph();
			logoParagraph.add(upiImg);
			logoParagraph.add(Chunk.NEWLINE);
			document.add(logoParagraph);

			logoImg = Image.getInstance(paymentgatewayLogo);
			logoImg.scaleToFit(110f, 150f);
			logoImg.setAbsolutePosition(160f, 440f);
			logoParagraph = new Paragraph();
			logoParagraph.add(logoImg);
			document.add(logoParagraph);

			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);

			titleParagraph = new Paragraph(customerQR.getCompanyName(), mercantNameFont);
			titleParagraph.setAlignment(Element.ALIGN_CENTER);
			document.add(titleParagraph);

			qrImage = Image.getInstance(Base64.decodeBase64(base64QR));
			qrImage.scaleToFit(270f, 200f);
			qrImage.setAbsolutePosition(110f, 230f);
			logoParagraph = new Paragraph();
			logoParagraph.add(qrImage);
			document.add(logoParagraph);

			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);

			titleParagraph = new Paragraph(customerQR.getVpa(), mercantNameFont);
			titleParagraph.setAlignment(Element.ALIGN_CENTER);
			document.add(titleParagraph);
			document.add(Chunk.NEWLINE);

			titleParagraph = new Paragraph("Scan & Pay", mercantNameFont);
			titleParagraph.setAlignment(Element.ALIGN_CENTER);
			document.add(titleParagraph);

			titleParagraph = new Paragraph("using any UPI App", mercantNameFont);
			titleParagraph.setAlignment(Element.ALIGN_CENTER);
			document.add(titleParagraph);

			paytmPaymentModeImg = Image.getInstance(paytmPaymentModeLogo);
			paytmPaymentModeImg.scaleToFit(80f, 80f);
			paytmPaymentModeImg.setAbsolutePosition(120f, 90f);
			logoParagraph = new Paragraph();
			logoParagraph.add(paytmPaymentModeImg);
			document.add(Chunk.NEWLINE);
			document.add(logoParagraph);

			googlePayPaymentModeImg = Image.getInstance(googlePayPaymentModeLogo);
			googlePayPaymentModeImg.scaleToFit(36f, 36f);
			googlePayPaymentModeImg.setAbsolutePosition(215f, 90f);
			logoParagraph = new Paragraph();
			logoParagraph.add(googlePayPaymentModeImg);
			document.add(Chunk.NEWLINE);
			document.add(logoParagraph);

			phonePePaymentModeImg = Image.getInstance(phonePePaymentModeLogo);
			phonePePaymentModeImg.scaleToFit(35f, 35f);
			phonePePaymentModeImg.setAbsolutePosition(270f, 90f);
			logoParagraph = new Paragraph();
			logoParagraph.add(phonePePaymentModeImg);
			document.add(logoParagraph);
		}
		document.close();
		logger.info("static UPI QR pdf Succesfully created");
	}

}
