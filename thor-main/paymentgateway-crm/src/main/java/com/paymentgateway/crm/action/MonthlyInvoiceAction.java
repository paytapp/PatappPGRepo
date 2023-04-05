package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.html.WebColors;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.dao.MPADao;
import com.paymentgateway.commons.dao.SUFDetailDao;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.MerchantProcessingApplication;
import com.paymentgateway.commons.user.SUFDetail;
import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StateCodes;
import com.paymentgateway.crm.mongoReports.TxnReports;

/**
 * @auther Sandeep Sharma
 */

@Service
public class MonthlyInvoiceAction {

	@Autowired
	private UserDao userDao;

	@Autowired
	private TxnReports txnReport;

	@Autowired
	private MPADao mpaDao;

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private SUFDetailDao sufDetailDao;

	private static Logger logger = LoggerFactory.getLogger(MonthlyInvoiceAction.class.getName());
	private static final String prefix = "MONGO_DB_";

	private User user;

	public void generate(String merchantPayId, String subMerchantPayId, String invoiceMonth, String invoiceNo,
			String hsnNo) {

		String filename = null;
		if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("All")) {
			user = userDao.findPayId(subMerchantPayId);
		} else {
			user = userDao.findPayId(merchantPayId);
		}

		String invoiceLocation = "/home/Properties/tempFileLocation/Invoice/";
		try {
			Files.createDirectories(Paths.get(invoiceLocation));
		} catch (IOException e1) {
			logger.error("Error in creating Directorie ", e1);
		}

		if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("All")) {
			filename = "MonthlyInvoice_" + subMerchantPayId + "_" + invoiceMonth + ".pdf";
		} else {
			filename = "MonthlyInvoice_" + merchantPayId + "_" + invoiceMonth + ".pdf";
		}
		try {
			createMonthlyInvoicePdf(merchantPayId, subMerchantPayId, user, invoiceMonth, invoiceNo, hsnNo, filename,
					invoiceLocation);

			// Update file status
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<org.bson.Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.MONTHLY_INVOICE_FILE_STATUS_COLLECTION.getValue()));

			BasicDBObject query = new BasicDBObject().append(FieldType.MERCHANT_ID.getName(), merchantPayId)
					.append("INVOICE_MONTH", invoiceMonth).append("INVOICE_NO", invoiceNo).append("FILENAME", filename);
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("All")) {
				query.append(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId);
			}

			BasicDBObject setData = new BasicDBObject();
			setData.append("$set", new BasicDBObject().append(FieldType.STATUS.getName(), "Ready"));

			coll.updateOne(query, setData);

		} catch (Exception e) {
			logger.error("Exception ", e);

			// Update file status
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<org.bson.Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.MONTHLY_INVOICE_FILE_STATUS_COLLECTION.getValue()));

			BasicDBObject query = new BasicDBObject().append(FieldType.MERCHANT_ID.getName(), merchantPayId)
					.append("INVOICE_MONTH", invoiceMonth).append("FILENAME", filename);
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("All")) {
				query.append(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId);
			}

			coll.deleteOne(query);
		}

	}

	public void createMonthlyInvoicePdf(String merchantPayId, String subMerchantPayId, User user, String date,
			String invoiceNo, String hsnNo, String filename, String invoiceLocation)
			throws MalformedURLException, IOException, FileNotFoundException, DocumentException {
		logger.info("Creating invoice PDF");

		OutputStream file = new FileOutputStream(new File(invoiceLocation + filename));
		Document document = new Document(PageSize.A4, 60, 60, 80, 40);
		PdfWriter.getInstance(document, file);
		document.open();
		addMonthlyInvoiceMetaData(document);
		try {
			addMonthlyInvoiceContent(document, user, date, merchantPayId, subMerchantPayId, invoiceNo, hsnNo);
		} catch (IOException e) {
			logger.error("Exception in file generation ", e);
		}
		document.close();
		file.close();
	}

	private void addMonthlyInvoiceMetaData(Document document) {
		document.addTitle("Monthly Invoice");
		document.addSubject("Monthly Invoice on Payment Gateway Solution Private Limited");
		document.addKeywords("Monthly Invoice, Payment Gateway");
		document.addAuthor("Payment Gateway Solution Private Limited, created on " + new Date());
	}

	private void addMonthlyInvoiceContent(Document document, User user, String date, String merchantPayId,
			String subMerchantPayId, String invoiceNo, String hsnNo)
			throws DocumentException, MalformedURLException, IOException, FileNotFoundException {

		SimpleDateFormat getdate = new SimpleDateFormat("MMMM-yyyy");
		SimpleDateFormat setdate = new SimpleDateFormat("MMMM-yy");
		SimpleDateFormat setdate1 = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MMM-yy");
		String descDate = "";
		String fromdate = "";
		String todate = "";
		String firstdate = "";
		String lastdate = "";
		try {
			Date convdate = getdate.parse(date);
			descDate = setdate.format(convdate);
			try {
				firstdate = getFirstDay(convdate);
				lastdate = getLastDay(convdate);

				Date from = sdf1.parse(firstdate);
				Date to = sdf1.parse(lastdate);

				fromdate = setdate1.format(from) + " 00:00:00";
				todate = setdate1.format(to)+ " 23:59:59";
			} catch (Exception e) {
				logger.error("Error in getiing first and last date ", e);
			}
		} catch (ParseException e) {
			logger.error("Error in parsing date ", e);
		}

//		logger.info("first date of the month = " + firstdate);
//		logger.info("last date of the month = " + lastdate);

		String businessName = "";
		String address = "";
		String city = "";
		String state = "";
		String postcode = "";
		String gstno = "";
		String statecode = "";
		List<MerchantProcessingApplication> mpadata = new ArrayList<MerchantProcessingApplication>();

		if (StringUtils.isNotBlank(user.getBusinessName())) {
			businessName = user.getBusinessName();
		}

		mpadata = mpaDao.fetchMPADataPerPayId(user.getPayId());
		for (MerchantProcessingApplication mpaFields : mpadata) {
			if (StringUtils.isNotBlank(user.getCity())) {
				city = user.getCity();
			}

			if (StringUtils.isNotBlank(mpaFields.getTradingState())) {
				state = mpaFields.getTradingState();
				if (StringUtils.isNotBlank(mpaFields.getTradingCountry())
						&& mpaFields.getTradingCountry().equalsIgnoreCase("India")) {
					statecode = StateCodes.getCodeUsingInstance(state);
				}
			}

			if (StringUtils.isNotBlank(mpaFields.getTradingPin())) {
				postcode = mpaFields.getTradingPin();
			}
			if (StringUtils.isNotBlank(mpaFields.getGstin())) {
				gstno = mpaFields.getGstin();
			}
			if (StringUtils.isNotBlank(mpaFields.getTradingAddress1())) {
				address = mpaFields.getTradingAddress1();
			}
		}
		List<SUFDetail> sufCharge = new ArrayList<SUFDetail>();
		List<TransactionSearch> data = new ArrayList<TransactionSearch>();
		BigDecimal totaltdr = new BigDecimal("0.00");
		BigDecimal tdr = new BigDecimal("0.00");
		BigDecimal gst = new BigDecimal("0.00");
		BigDecimal igst = new BigDecimal("0.00");
		BigDecimal cgst = new BigDecimal("0.00");
		BigDecimal sgst = new BigDecimal("0.00");
		BigDecimal total = new BigDecimal("0.00");
		BigDecimal sufAmount = new BigDecimal("0.00");
		sufCharge = sufDetailDao.fetchSufChargeByPayId(merchantPayId);
		data = txnReport.txnRecordMonthlyInvoice(merchantPayId, subMerchantPayId, fromdate, todate);

		for (TransactionSearch datalist : data) {
			totaltdr = totaltdr.add(new BigDecimal(datalist.getTdr_Surcharge()));
			gst = gst.add(new BigDecimal(datalist.getGst_charge()));
			sufAmount = sufAmount
					.add(getSufCharge(datalist.getPayId(), datalist.getOrigTxnType(), datalist.getPaymentMethods(),
							datalist.getMopType(), datalist.getAmount(), sufCharge, datalist.getPaymentRegion()));
		}
		total = total.add(totaltdr).add(gst).add(sufAmount);
		tdr = total.divide(new BigDecimal("1.18"), 2, BigDecimal.ROUND_HALF_EVEN);
		if (statecode.equals("09")) {
			cgst = tdr.multiply(new BigDecimal("0.09")).setScale(2, BigDecimal.ROUND_HALF_EVEN);
			sgst = tdr.multiply(new BigDecimal("0.09")).setScale(2, BigDecimal.ROUND_HALF_EVEN);
			igst = new BigDecimal("0.00");
		} else {
			cgst = new BigDecimal("0.00");
			sgst = new BigDecimal("0.00");
			igst = tdr.multiply(new BigDecimal("0.18")).setScale(2, BigDecimal.ROUND_HALF_EVEN);
		}

		Font companyName = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
		companyName.setColor(BaseColor.BLACK);

		Font companyDetail = new Font(Font.FontFamily.TIMES_ROMAN, 8);
		companyDetail.setColor(BaseColor.BLACK);

		Font taxInvoice = new Font(Font.FontFamily.TIMES_ROMAN, 11);
		taxInvoice.setColor(BaseColor.BLACK);
		taxInvoice.setStyle(Font.BOLDITALIC);

		Font billing = new Font(Font.FontFamily.TIMES_ROMAN, 8);
		billing.setColor(BaseColor.BLACK);
		billing.setStyle(Font.BOLD);

		Font invoiceheadings = new Font(Font.FontFamily.TIMES_ROMAN, 8);
		invoiceheadings.setColor(BaseColor.BLACK);
		invoiceheadings.setStyle(Font.BOLD);

		Font invoiceheadingsitalic = new Font(Font.FontFamily.TIMES_ROMAN, 8);
		invoiceheadingsitalic.setColor(BaseColor.BLACK);
		invoiceheadingsitalic.setStyle(Font.BOLDITALIC);

		float[] mainouterDiv = new float[] { 100f };
		PdfPTable mainouterTable = new PdfPTable(1);
		mainouterTable.setWidths(mainouterDiv);
		mainouterTable.getDefaultCell().setBorderWidth(2f);
		mainouterTable.setWidthPercentage(100f);

		float[] outerDiv = new float[] { 100f };
		PdfPTable outerTable = new PdfPTable(1);
		outerTable.setWidths(outerDiv);
		outerTable.getDefaultCell().setBorderWidth(0f);
		outerTable.getDefaultCell().setPaddingLeft(2f);
		outerTable.getDefaultCell().setPaddingRight(2f);
		outerTable.setWidthPercentage(100f);

		float[] mainDiv = new float[] { 100f };
		PdfPTable mainTable = new PdfPTable(1);
		mainTable.setWidths(mainDiv);
		mainTable.getDefaultCell().setBorderWidth(0f);
		mainTable.setWidthPercentage(100f);

		document.add(Chunk.NEWLINE);
		float[] heading = new float[] { 55f, 45f };
		PdfPTable headingtable = new PdfPTable(2);
		headingtable.setWidths(heading);
		headingtable.getDefaultCell().setBorderWidth(0f);
		;
		headingtable.setWidthPercentage(100f);

		Image img2 = Image.getInstance(PropertiesManager.propertiesMap.get("monthlyInvoceLogo"));
		img2.scaleToFit(90f, 110f);
		PdfPCell cellLogo = new PdfPCell(img2);
		cellLogo.setPaddingTop(10f);
		cellLogo.setPaddingLeft(20f);
		cellLogo.setFixedHeight(45f);
		cellLogo.setRowspan(5);
		cellLogo.setBorder(Rectangle.NO_BORDER);
		headingtable.addCell(new PdfPCell(cellLogo)).setHorizontalAlignment(Element.ALIGN_LEFT);

		PdfPCell paymentGatewayDetail = new PdfPCell(new Phrase("Payment Gateway Solution Private Limited", companyName));
		paymentGatewayDetail.setPaddingTop(-2f);
		paymentGatewayDetail.setPaddingBottom(2f);
		paymentGatewayDetail.setBorderWidth(0f);
		paymentGatewayDetail.setHorizontalAlignment(Element.ALIGN_RIGHT);
		paymentGatewayDetail.setVerticalAlignment(Element.ALIGN_RIGHT);
		headingtable.addCell(paymentGatewayDetail);

		PdfPCell paymentGatewayDetail1 = new PdfPCell(
				new Phrase(Chunk.NEWLINE + "1F-CS-06, Ansal Plaza, Vaishali, Ghaziabad, UP-201010", companyDetail));
		paymentGatewayDetail1.setBorderWidth(0f);
		paymentGatewayDetail1.setPaddingTop(-5f);
		paymentGatewayDetail1.setHorizontalAlignment(Element.ALIGN_RIGHT);
		paymentGatewayDetail1.setVerticalAlignment(Element.ALIGN_RIGHT);
		headingtable.addCell(paymentGatewayDetail1);

		PdfPCell paymentGatewayDetail12 = new PdfPCell(
				new Phrase(Chunk.NEWLINE + "www.paymentGateway.com || 0120-4334884", companyDetail));
		paymentGatewayDetail12.setBorderWidth(0f);
		paymentGatewayDetail12.setPaddingTop(-7f);
		paymentGatewayDetail12.setHorizontalAlignment(Element.ALIGN_RIGHT);
		paymentGatewayDetail12.setVerticalAlignment(Element.ALIGN_RIGHT);
		headingtable.addCell(paymentGatewayDetail12);

		PdfPCell paymentGatewayDetail13 = new PdfPCell(new Phrase(Chunk.NEWLINE + "CIN: U74999DL2016PTC300289", companyDetail));
		paymentGatewayDetail13.setBorderWidth(0f);
		paymentGatewayDetail13.setPaddingTop(-7f);
		paymentGatewayDetail13.setHorizontalAlignment(Element.ALIGN_RIGHT);
		paymentGatewayDetail13.setVerticalAlignment(Element.ALIGN_RIGHT);
		headingtable.addCell(paymentGatewayDetail13);

		PdfPCell paymentGatewayDetail14 = new PdfPCell(new Phrase(Chunk.NEWLINE + "GSTN: 09AADCL0683Q1ZW", companyDetail));
		paymentGatewayDetail14.setBorderWidth(0f);
		paymentGatewayDetail14.setPaddingTop(-7f);
		paymentGatewayDetail14.setHorizontalAlignment(Element.ALIGN_RIGHT);
		paymentGatewayDetail14.setVerticalAlignment(Element.ALIGN_RIGHT);
		headingtable.addCell(paymentGatewayDetail14);

		headingtable.setSpacingAfter(10f);
		mainTable.addCell(headingtable);

		float[] tax = new float[] { 47f, 23f, 30f };
		PdfPTable invoiceheading = new PdfPTable(3);
		invoiceheading.setWidths(tax);
		invoiceheading.getDefaultCell().setBorderWidth(0f);
		invoiceheading.setWidthPercentage(90f);

		PdfPCell invoice = new PdfPCell(new Phrase(" ", taxInvoice));
		invoice.setBorderWidth(0f);
		invoice.setBorderWidthTop(1f);
		invoice.setBorderColor(BaseColor.RED);

		invoiceheading.addCell(invoice);

		PdfPCell invoice2 = new PdfPCell(new Phrase("Tax Invoice", taxInvoice));
		invoice2.setBorderWidth(0f);
		invoice2.setHorizontalAlignment(Element.ALIGN_CENTER);
		invoice2.setVerticalAlignment(Element.ALIGN_CENTER);
		invoice2.setPaddingTop(-8f);
		invoiceheading.addCell(invoice2);

		PdfPCell invoice3 = new PdfPCell(new Phrase(" ", taxInvoice));
		invoice3.setBorderWidth(0f);
		invoice3.setBorderWidthTop(1f);
		invoice3.setBorderColor(BaseColor.RED);
		invoiceheading.addCell(invoice3);
		invoiceheading.setSpacingAfter(20f);

		mainTable.addCell(invoiceheading);
		outerTable.addCell(mainTable);

		float[] bill = new float[] { 40f, 8f, 23f, 30f };
		PdfPTable billingTable = new PdfPTable(4);
		billingTable.setWidths(bill);
		billingTable.getDefaultCell().setBorderWidth(0f);
		billingTable.setWidthPercentage(100f);
		BaseColor myColor = WebColors.getRGBColor("#d1d1d1");

		PdfPCell billto = new PdfPCell(new Phrase("Bill To: ", billing));
		billto.setBorderWidth(0f);
		billto.setBackgroundColor(myColor);
		billto.setFixedHeight(10f);
		billto.setBorderWidthTop(1f);
		billto.setBorderWidthBottom(1f);
		billto.setBorderWidthRight(1f);
		billto.setBorderWidthLeft(1f);
		billto.setHorizontalAlignment(Element.ALIGN_LEFT);
		billto.setVerticalAlignment(Element.ALIGN_LEFT);

		billingTable.addCell(billto);

		PdfPCell blank = new PdfPCell(new Phrase(""));
		blank.setBorderWidth(0f);

		billingTable.addCell(blank);

		PdfPCell invoiceno = new PdfPCell(new Phrase("Invoice No: ", invoiceheadings));
		invoiceno.setBorderWidth(0f);
		invoiceno.setBackgroundColor(myColor);
		invoiceno.setFixedHeight(20f);
		invoiceno.setBorderWidthTop(1f);
		invoiceno.setBorderWidthLeft(1f);
		invoiceno.setHorizontalAlignment(Element.ALIGN_LEFT);
		invoiceno.setVerticalAlignment(Element.ALIGN_LEFT);

		billingTable.addCell(invoiceno);

		PdfPCell invoicedetail = new PdfPCell(new Phrase("Payment Gateway" + invoiceNo, companyDetail));
		invoicedetail.setBorderWidth(0f);
		invoicedetail.setBorderWidthTop(1f);
		invoicedetail.setBorderWidthLeft(1f);
		invoicedetail.setBorderWidthRight(1f);
		invoicedetail.setHorizontalAlignment(Element.ALIGN_RIGHT);
		invoicedetail.setVerticalAlignment(Element.ALIGN_RIGHT);

		billingTable.addCell(invoicedetail);

		billto = new PdfPCell(new Phrase(businessName, billing));
		billto.setBorderWidth(0f);
		billto.setBorderWidthRight(1f);
		billto.setBorderWidthLeft(1f);
		billto.setHorizontalAlignment(Element.ALIGN_LEFT);
		billto.setVerticalAlignment(Element.ALIGN_LEFT);

		billingTable.addCell(billto);

		blank = new PdfPCell(new Phrase(""));
		blank.setBorderWidth(0f);

		billingTable.addCell(blank);

		invoiceno = new PdfPCell(new Phrase("Invoice Date: ", invoiceheadings));
		invoiceno.setBorderWidth(0f);
		invoiceno.setBackgroundColor(myColor);
		invoiceno.setBorderWidthTop(1f);
		invoiceno.setBorderWidthLeft(1f);
		invoiceno.setHorizontalAlignment(Element.ALIGN_LEFT);
		invoiceno.setVerticalAlignment(Element.ALIGN_LEFT);

		billingTable.addCell(invoiceno);

		invoicedetail = new PdfPCell(new Phrase(lastdate, companyDetail));
		invoicedetail.setBorderWidth(0f);
		invoicedetail.setBorderWidthTop(1f);
		invoicedetail.setBorderWidthLeft(1f);
		invoicedetail.setBorderWidthRight(1f);
		invoicedetail.setHorizontalAlignment(Element.ALIGN_RIGHT);
		invoicedetail.setVerticalAlignment(Element.ALIGN_RIGHT);

		billingTable.addCell(invoicedetail);

		billto = new PdfPCell(new Phrase(address, companyDetail));
		billto.setBorderWidth(0f);
		billto.setBorderWidthRight(1f);
		billto.setBorderWidthLeft(1f);
		billto.setHorizontalAlignment(Element.ALIGN_LEFT);
		billto.setVerticalAlignment(Element.ALIGN_LEFT);

		billingTable.addCell(billto);

		blank = new PdfPCell(new Phrase(""));
		blank.setBorderWidth(0f);

		billingTable.addCell(blank);

		invoiceno = new PdfPCell(new Phrase("Invoice Period: ", invoiceheadings));
		invoiceno.setBorderWidth(0f);
		invoiceno.setBackgroundColor(myColor);
		invoiceno.setBorderWidthTop(1f);
		invoiceno.setBorderWidthLeft(1f);
		invoiceno.setHorizontalAlignment(Element.ALIGN_LEFT);
		invoiceno.setVerticalAlignment(Element.ALIGN_LEFT);

		billingTable.addCell(invoiceno);

		invoicedetail = new PdfPCell(new Phrase(firstdate + " to " + lastdate, companyDetail));
		invoicedetail.setBorderWidth(0f);
		invoicedetail.setBorderWidthTop(1f);
		invoicedetail.setBorderWidthLeft(1f);
		invoicedetail.setBorderWidthRight(1f);
		invoicedetail.setHorizontalAlignment(Element.ALIGN_RIGHT);
		invoicedetail.setVerticalAlignment(Element.ALIGN_RIGHT);

		billingTable.addCell(invoicedetail);

		billto = new PdfPCell(new Phrase(city + " " + state + " - " + postcode, companyDetail));
		billto.setBorderWidth(0f);
		billto.setBorderWidthRight(1f);
		billto.setBorderWidthLeft(1f);
		billto.setHorizontalAlignment(Element.ALIGN_LEFT);
		billto.setVerticalAlignment(Element.ALIGN_LEFT);

		billingTable.addCell(billto);

		blank = new PdfPCell(new Phrase(""));
		blank.setBorderWidth(0f);

		billingTable.addCell(blank);

		invoiceno = new PdfPCell(new Phrase("Place of Supply: ", invoiceheadings));
		invoiceno.setBorderWidth(0f);
		invoiceno.setBackgroundColor(myColor);
		invoiceno.setBorderWidthTop(1f);
		invoiceno.setBorderWidthLeft(1f);
		invoiceno.setHorizontalAlignment(Element.ALIGN_LEFT);
		invoiceno.setVerticalAlignment(Element.ALIGN_LEFT);

		billingTable.addCell(invoiceno);

		invoicedetail = new PdfPCell(new Phrase(state, companyDetail));
		invoicedetail.setBorderWidth(0f);
		invoicedetail.setBorderWidthTop(1f);
		invoicedetail.setBorderWidthLeft(1f);
		invoicedetail.setBorderWidthRight(1f);
		invoicedetail.setHorizontalAlignment(Element.ALIGN_RIGHT);
		invoicedetail.setVerticalAlignment(Element.ALIGN_RIGHT);

		billingTable.addCell(invoicedetail);

		billto = new PdfPCell(new Phrase("GSTN: " + gstno, companyDetail));
		billto.setBorderWidth(0f);
		billto.setBorderWidthRight(1f);
		billto.setBorderWidthBottom(1f);
		billto.setBorderWidthLeft(1f);
		billto.setFixedHeight(15f);
		billto.setHorizontalAlignment(Element.ALIGN_LEFT);
		billto.setVerticalAlignment(Element.ALIGN_LEFT);

		billingTable.addCell(billto);

		blank = new PdfPCell(new Phrase(""));
		blank.setBorderWidth(0f);
		blank.setFixedHeight(15f);

		billingTable.addCell(blank);

		invoiceno = new PdfPCell(new Phrase("State Code: ", invoiceheadings));
		invoiceno.setBorderWidth(0f);
		invoiceno.setBackgroundColor(myColor);
		invoiceno.setBorderWidthTop(1f);
		invoiceno.setBorderWidthLeft(1f);
		invoiceno.setBorderWidthBottom(1f);
		invoiceno.setFixedHeight(15f);
		invoiceno.setHorizontalAlignment(Element.ALIGN_LEFT);
		invoiceno.setVerticalAlignment(Element.ALIGN_LEFT);

		billingTable.addCell(invoiceno);

		invoicedetail = new PdfPCell(new Phrase(statecode, companyDetail));
		invoicedetail.setBorderWidth(0f);
		invoicedetail.setBorderWidthTop(1f);
		invoicedetail.setBorderWidthLeft(1f);
		invoicedetail.setBorderWidthBottom(1f);
		invoicedetail.setFixedHeight(15f);
		invoicedetail.setBorderWidthRight(1f);
		invoicedetail.setHorizontalAlignment(Element.ALIGN_RIGHT);
		invoicedetail.setVerticalAlignment(Element.ALIGN_RIGHT);

		billingTable.addCell(invoicedetail);

		outerTable.addCell(billingTable);

		float[] blnk = new float[] { 100f };
		PdfPTable blankTable = new PdfPTable(1);
		blankTable.setWidths(blnk);
		blankTable.getDefaultCell().setBorderWidth(0f);
		blankTable.setWidthPercentage(100f);

		PdfPCell space = new PdfPCell(new Phrase(""));
		space.setBorderWidth(0f);
		space.setFixedHeight(30f);
		space.setHorizontalAlignment(Element.ALIGN_LEFT);
		space.setVerticalAlignment(Element.ALIGN_LEFT);

		blankTable.addCell(space);

		outerTable.addCell(blankTable);

		float[] desc = new float[] { 8f, 40f, 26f, 26f };
		PdfPTable descTable = new PdfPTable(4);
		descTable.setWidths(desc);
		descTable.getDefaultCell().setBorderWidth(0f);
		descTable.setWidthPercentage(100f);

		PdfPCell slNo = new PdfPCell(new Phrase("Sl No. ", billing));
		slNo.setBorderWidth(0f);
		slNo.setBorderWidthTop(1f);
		slNo.setBorderWidthLeft(1f);
		slNo.setBorderWidthBottom(1f);
		slNo.setFixedHeight(15f);
		slNo.setHorizontalAlignment(Element.ALIGN_CENTER);
		slNo.setVerticalAlignment(Element.ALIGN_CENTER);

		descTable.addCell(slNo);

		PdfPCell description = new PdfPCell(new Phrase("Description ", billing));
		description.setBorderWidth(0f);
		description.setBorderWidthTop(1f);
		description.setBorderWidthLeft(1f);
		description.setBorderWidthBottom(1f);
		description.setFixedHeight(15f);
		description.setHorizontalAlignment(Element.ALIGN_LEFT);
		description.setVerticalAlignment(Element.ALIGN_LEFT);

		descTable.addCell(description);

		PdfPCell hsn = new PdfPCell(new Phrase("HSN/SAC ", billing));
		hsn.setBorderWidth(0f);
		hsn.setBorderWidthTop(1f);
		hsn.setBorderWidthLeft(1f);
		hsn.setBorderWidthBottom(1f);
		hsn.setFixedHeight(15f);
		hsn.setHorizontalAlignment(Element.ALIGN_RIGHT);
		hsn.setVerticalAlignment(Element.ALIGN_RIGHT);

		descTable.addCell(hsn);

		PdfPCell amount = new PdfPCell(new Phrase("Amount", billing));
		amount.setBorderWidth(0f);
		amount.setBorderWidthTop(1f);
		amount.setBorderWidthLeft(1f);
		amount.setBorderWidthRight(1f);
		amount.setBorderWidthBottom(1f);
		amount.setFixedHeight(15f);
		amount.setHorizontalAlignment(Element.ALIGN_RIGHT);
		amount.setVerticalAlignment(Element.ALIGN_RIGHT);

		descTable.addCell(amount);

		slNo = new PdfPCell(new Phrase("", billing));
		slNo.setBorderWidth(0f);
		slNo.setFixedHeight(15f);
		slNo.setBorderWidthLeft(1f);

		descTable.addCell(slNo);

		description = new PdfPCell(new Phrase("", billing));
		description.setBorderWidth(0f);
		description.setFixedHeight(15f);
		description.setBorderWidthLeft(1f);

		descTable.addCell(description);

		hsn = new PdfPCell(new Phrase("", billing));
		hsn.setBorderWidth(0f);
		hsn.setFixedHeight(15f);
		hsn.setBorderWidthLeft(1f);

		descTable.addCell(hsn);

		amount = new PdfPCell(new Phrase("", billing));
		amount.setBorderWidth(0f);
		amount.setFixedHeight(15f);
		amount.setBorderWidthLeft(1f);
		amount.setBorderWidthRight(1f);
		descTable.addCell(amount);

		slNo = new PdfPCell(new Phrase("1", billing));
		slNo.setBorderWidth(0f);
		slNo.setFixedHeight(15f);
		slNo.setBorderWidthLeft(1f);
		slNo.setHorizontalAlignment(Element.ALIGN_CENTER);
		slNo.setVerticalAlignment(Element.ALIGN_CENTER);
		descTable.addCell(slNo);

		description = new PdfPCell(new Phrase("Transactional charges for Payment Gateway Services", companyDetail));
		description.setBorderWidth(0f);
		description.setFixedHeight(15f);
		description.setBorderWidthLeft(1f);
		description.setHorizontalAlignment(Element.ALIGN_LEFT);
		description.setVerticalAlignment(Element.ALIGN_LEFT);

		descTable.addCell(description);

		hsn = new PdfPCell(new Phrase(hsnNo, companyDetail));
		hsn.setBorderWidth(0f);
		hsn.setFixedHeight(15f);
		hsn.setBorderWidthLeft(1f);
		hsn.setHorizontalAlignment(Element.ALIGN_RIGHT);
		hsn.setVerticalAlignment(Element.ALIGN_RIGHT);

		descTable.addCell(hsn);

		amount = new PdfPCell(new Phrase(tdr.toString(), companyDetail));
		amount.setBorderWidth(0f);
		amount.setFixedHeight(15f);
		amount.setBorderWidthLeft(1f);
		amount.setBorderWidthRight(1f);
		amount.setHorizontalAlignment(Element.ALIGN_RIGHT);
		amount.setVerticalAlignment(Element.ALIGN_RIGHT);

		descTable.addCell(amount);

		slNo = new PdfPCell(new Phrase("", billing));
		slNo.setBorderWidth(0f);
		slNo.setFixedHeight(15f);
		slNo.setBorderWidthLeft(1f);
		slNo.setHorizontalAlignment(Element.ALIGN_CENTER);
		slNo.setVerticalAlignment(Element.ALIGN_CENTER);
		descTable.addCell(slNo);

		description = new PdfPCell(new Phrase("For the month of " + descDate, invoiceheadingsitalic));
		description.setBorderWidth(0f);
		description.setFixedHeight(15f);
		description.setBorderWidthLeft(1f);
		description.setHorizontalAlignment(Element.ALIGN_LEFT);
		description.setVerticalAlignment(Element.ALIGN_LEFT);

		descTable.addCell(description);

		hsn = new PdfPCell(new Phrase("", companyDetail));
		hsn.setBorderWidth(0f);
		hsn.setFixedHeight(15f);
		hsn.setBorderWidthLeft(1f);
		hsn.setHorizontalAlignment(Element.ALIGN_RIGHT);
		hsn.setVerticalAlignment(Element.ALIGN_RIGHT);

		descTable.addCell(hsn);

		amount = new PdfPCell(new Phrase("", companyDetail));
		amount.setBorderWidth(0f);
		amount.setFixedHeight(15f);
		amount.setBorderWidthLeft(1f);
		amount.setBorderWidthRight(1f);
		amount.setHorizontalAlignment(Element.ALIGN_RIGHT);
		amount.setVerticalAlignment(Element.ALIGN_RIGHT);

		descTable.addCell(amount);

		slNo = new PdfPCell(new Phrase("", billing));
		slNo.setBorderWidth(0f);
		slNo.setFixedHeight(45f);
		slNo.setBorderWidthLeft(1f);
		slNo.setHorizontalAlignment(Element.ALIGN_CENTER);
		slNo.setVerticalAlignment(Element.ALIGN_CENTER);
		descTable.addCell(slNo);

		description = new PdfPCell(new Phrase("", invoiceheadings));
		description.setBorderWidth(0f);
		description.setFixedHeight(45f);
		description.setBorderWidthLeft(1f);
		description.setHorizontalAlignment(Element.ALIGN_LEFT);
		description.setVerticalAlignment(Element.ALIGN_LEFT);
		descTable.addCell(description);

		hsn = new PdfPCell(new Phrase("", companyDetail));
		hsn.setBorderWidth(0f);
		hsn.setFixedHeight(45f);
		hsn.setBorderWidthLeft(1f);
		hsn.setHorizontalAlignment(Element.ALIGN_RIGHT);
		hsn.setVerticalAlignment(Element.ALIGN_RIGHT);
		descTable.addCell(hsn);

		amount = new PdfPCell(new Phrase("", companyDetail));
		amount.setBorderWidth(0f);
		amount.setFixedHeight(45f);
		amount.setBorderWidthLeft(1f);
		amount.setBorderWidthRight(1f);
		amount.setHorizontalAlignment(Element.ALIGN_RIGHT);
		amount.setVerticalAlignment(Element.ALIGN_RIGHT);
		descTable.addCell(amount);

		slNo = new PdfPCell(new Phrase("", billing));
		slNo.setBorderWidth(0f);
		slNo.setFixedHeight(15f);
		slNo.setBorderWidthLeft(1f);
		slNo.setHorizontalAlignment(Element.ALIGN_CENTER);
		slNo.setVerticalAlignment(Element.ALIGN_CENTER);
		descTable.addCell(slNo);

		hsn = new PdfPCell(new Phrase("SubTotal", companyDetail));
		hsn.setBorderWidth(0f);
		hsn.setFixedHeight(15f);
		hsn.setBorderWidthLeft(1f);
		hsn.setBorderWidthTop(1f);
		hsn.setBorderWidthBottom(1f);
		hsn.setHorizontalAlignment(Element.ALIGN_RIGHT);
		hsn.setVerticalAlignment(Element.ALIGN_RIGHT);
		hsn.setColspan(2);
		descTable.addCell(hsn);

		amount = new PdfPCell(new Phrase(tdr.toString(), companyDetail));
		amount.setBorderWidth(0f);
		amount.setFixedHeight(15f);
		amount.setBorderWidthLeft(1f);
		amount.setBorderWidthRight(1f);
		amount.setBorderWidthTop(1f);
		amount.setBorderWidthBottom(1f);
		amount.setHorizontalAlignment(Element.ALIGN_RIGHT);
		amount.setVerticalAlignment(Element.ALIGN_RIGHT);
		descTable.addCell(amount);

		slNo = new PdfPCell(new Phrase("", billing));
		slNo.setBorderWidth(0f);
		slNo.setFixedHeight(15f);
		slNo.setBorderWidthLeft(1f);
		slNo.setHorizontalAlignment(Element.ALIGN_CENTER);
		slNo.setVerticalAlignment(Element.ALIGN_CENTER);
		descTable.addCell(slNo);

		description = new PdfPCell(new Phrase("", invoiceheadings));
		description.setBorderWidth(0f);
		description.setFixedHeight(15f);
		description.setBorderWidthLeft(1f);
		description.setHorizontalAlignment(Element.ALIGN_LEFT);
		description.setVerticalAlignment(Element.ALIGN_LEFT);

		descTable.addCell(description);

		hsn = new PdfPCell(new Phrase("Add. CGST@9% (Rs)", companyDetail));
		hsn.setBorderWidth(0f);
		hsn.setFixedHeight(15f);
		hsn.setBorderWidthLeft(1f);
		hsn.setBorderWidthBottom(1f);
		hsn.setHorizontalAlignment(Element.ALIGN_RIGHT);
		hsn.setVerticalAlignment(Element.ALIGN_RIGHT);

		descTable.addCell(hsn);

		amount = new PdfPCell(new Phrase(cgst.toString(), companyDetail));
		amount.setBorderWidth(0f);
		amount.setFixedHeight(15f);
		amount.setBorderWidthLeft(1f);
		amount.setBorderWidthRight(1f);
		amount.setBorderWidthBottom(1f);
		amount.setHorizontalAlignment(Element.ALIGN_RIGHT);
		amount.setVerticalAlignment(Element.ALIGN_RIGHT);

		descTable.addCell(amount);

		slNo = new PdfPCell(new Phrase("", billing));
		slNo.setBorderWidth(0f);
		slNo.setFixedHeight(15f);
		slNo.setBorderWidthLeft(1f);
		slNo.setHorizontalAlignment(Element.ALIGN_CENTER);
		slNo.setVerticalAlignment(Element.ALIGN_CENTER);
		descTable.addCell(slNo);

		description = new PdfPCell(new Phrase("", invoiceheadings));
		description.setBorderWidth(0f);
		description.setFixedHeight(15f);
		description.setBorderWidthLeft(1f);
		description.setHorizontalAlignment(Element.ALIGN_LEFT);
		description.setVerticalAlignment(Element.ALIGN_LEFT);

		descTable.addCell(description);

		hsn = new PdfPCell(new Phrase("Add. SGST@9% (Rs)", companyDetail));
		hsn.setBorderWidth(0f);
		hsn.setFixedHeight(15f);
		hsn.setBorderWidthLeft(1f);
		hsn.setBorderWidthBottom(1f);
		hsn.setHorizontalAlignment(Element.ALIGN_RIGHT);
		hsn.setVerticalAlignment(Element.ALIGN_RIGHT);

		descTable.addCell(hsn);

		amount = new PdfPCell(new Phrase(sgst.toString(), companyDetail));
		amount.setBorderWidth(0f);
		amount.setFixedHeight(15f);
		amount.setBorderWidthLeft(1f);
		amount.setBorderWidthRight(1f);
		amount.setBorderWidthBottom(1f);
		amount.setHorizontalAlignment(Element.ALIGN_RIGHT);
		amount.setVerticalAlignment(Element.ALIGN_RIGHT);

		descTable.addCell(amount);

		slNo = new PdfPCell(new Phrase("", billing));
		slNo.setBorderWidth(0f);
		slNo.setFixedHeight(15f);
		slNo.setBorderWidthLeft(1f);
		slNo.setHorizontalAlignment(Element.ALIGN_CENTER);
		slNo.setVerticalAlignment(Element.ALIGN_CENTER);
		descTable.addCell(slNo);

		description = new PdfPCell(new Phrase("", invoiceheadings));
		description.setBorderWidth(0f);
		description.setFixedHeight(15f);
		description.setBorderWidthLeft(1f);
		description.setHorizontalAlignment(Element.ALIGN_LEFT);
		description.setVerticalAlignment(Element.ALIGN_LEFT);

		descTable.addCell(description);

		hsn = new PdfPCell(new Phrase("Add. IGST@18% (Rs)", companyDetail));
		hsn.setBorderWidth(0f);
		hsn.setFixedHeight(15f);
		hsn.setBorderWidthLeft(1f);
		hsn.setHorizontalAlignment(Element.ALIGN_RIGHT);
		hsn.setVerticalAlignment(Element.ALIGN_RIGHT);

		descTable.addCell(hsn);

		amount = new PdfPCell(new Phrase(igst.toString(), companyDetail));
		amount.setBorderWidth(0f);
		amount.setFixedHeight(15f);
		amount.setBorderWidthLeft(1f);
		amount.setBorderWidthRight(1f);
		amount.setHorizontalAlignment(Element.ALIGN_RIGHT);
		amount.setVerticalAlignment(Element.ALIGN_RIGHT);

		descTable.addCell(amount);

		slNo = new PdfPCell(new Phrase("", billing));
		slNo.setBorderWidth(0f);
		slNo.setFixedHeight(15f);
		slNo.setBorderWidthTop(1f);
		slNo.setBorderWidthLeft(1f);
		slNo.setBorderWidthBottom(1f);
		slNo.setBackgroundColor(myColor);
		slNo.setHorizontalAlignment(Element.ALIGN_CENTER);
		slNo.setVerticalAlignment(Element.ALIGN_CENTER);
		descTable.addCell(slNo);

		description = new PdfPCell(new Phrase("Total", invoiceheadings));
		description.setBorderWidth(0f);
		description.setFixedHeight(15f);
		description.setColspan(2);
		description.setBorderWidthTop(1f);
		description.setBorderWidthLeft(1f);
		description.setBorderWidthBottom(1f);
		description.setBackgroundColor(myColor);
		description.setHorizontalAlignment(Element.ALIGN_CENTER);
		description.setVerticalAlignment(Element.ALIGN_CENTER);

		descTable.addCell(description);

		amount = new PdfPCell(new Phrase(total.toString(), companyDetail));
		amount.setBorderWidth(0f);
		amount.setFixedHeight(15f);
		amount.setBorderWidthTop(1f);
		amount.setBorderWidthLeft(1f);
		amount.setBorderWidthRight(1f);
		amount.setBorderWidthBottom(1f);
		amount.setBackgroundColor(myColor);
		amount.setHorizontalAlignment(Element.ALIGN_RIGHT);
		amount.setVerticalAlignment(Element.ALIGN_RIGHT);

		descTable.addCell(amount);
		outerTable.addCell(descTable);

		float[] amt = new float[] { 15f, 85f };
		PdfPTable amtTable = new PdfPTable(2);
		amtTable.setWidths(amt);
		amtTable.getDefaultCell().setBorderWidth(0f);
		amtTable.setWidthPercentage(100f);

		PdfPCell amtheading = new PdfPCell(new Phrase("", billing));
		amtheading.setBorderWidth(0f);
		amtheading.setFixedHeight(5f);
		amtheading.setHorizontalAlignment(Element.ALIGN_LEFT);
		amtheading.setVerticalAlignment(Element.ALIGN_LEFT);

		amtTable.addCell(amtheading);

		PdfPCell amtINR = new PdfPCell(new Phrase(" ", billing));
		amtINR.setBorderWidth(0f);
		amtINR.setFixedHeight(5f);
		amtINR.setHorizontalAlignment(Element.ALIGN_LEFT);
		amtINR.setVerticalAlignment(Element.ALIGN_LEFT);

		amtTable.addCell(amtINR);

		amtheading = new PdfPCell(new Phrase("Total In Words:", billing));
		amtheading.setBorderWidth(0f);
		amtheading.setFixedHeight(15f);
		amtheading.setHorizontalAlignment(Element.ALIGN_LEFT);
		amtheading.setVerticalAlignment(Element.ALIGN_LEFT);

		amtTable.addCell(amtheading);
		int sign = total.signum();
		if (sign == -1) {
			amtINR = new PdfPCell(
					new Phrase("INR Minus " + getDecimalValue(total.negate().toString()) + " Only", billing));
		} else {
			amtINR = new PdfPCell(new Phrase("INR " + getDecimalValue(total.toString()) + " Only", billing));
		}
		amtINR.setBorderWidth(0f);
		amtINR.setFixedHeight(15f);
		amtINR.setHorizontalAlignment(Element.ALIGN_LEFT);
		amtINR.setVerticalAlignment(Element.ALIGN_LEFT);

		amtTable.addCell(amtINR);

		outerTable.addCell(amtTable);

		float[] info = new float[] { 100f };
		PdfPTable infoTable = new PdfPTable(1);
		infoTable.setWidths(info);
		infoTable.getDefaultCell().setBorderWidth(0f);
		infoTable.setWidthPercentage(100f);

		PdfPCell information = new PdfPCell(
				new Phrase("* This is a consolidated invoice for the captioned period", invoiceheadingsitalic));
		information.setBorderWidth(0f);
		information.setFixedHeight(15f);
		information.setPaddingTop(5f);
		information.setHorizontalAlignment(Element.ALIGN_LEFT);
		information.setVerticalAlignment(Element.ALIGN_LEFT);

		infoTable.addCell(information);

		information = new PdfPCell(new Phrase("", billing));
		information.setBorderWidth(0f);
		information.setFixedHeight(25f);
		information.setHorizontalAlignment(Element.ALIGN_LEFT);
		information.setVerticalAlignment(Element.ALIGN_LEFT);

		infoTable.addCell(information);

		information = new PdfPCell(
				new Phrase("This is a computer generated invoice and does not require signature", billing));
		information.setBorderWidth(0f);
		information.setFixedHeight(15f);
		information.setHorizontalAlignment(Element.ALIGN_CENTER);
		information.setVerticalAlignment(Element.ALIGN_CENTER);

		infoTable.addCell(information);

		outerTable.addCell(infoTable);

		mainouterTable.addCell(outerTable);
		document.add(mainouterTable);

		document.close();

	}

	public static String getString(int number) {
		switch (number) {
		case 0:
			return "Zero";
		case 1:
			return "One";
		case 2:
			return "Two";
		case 3:
			return "Three";
		case 4:
			return "Four";
		case 5:
			return "Five";
		case 6:
			return "Six";
		case 7:
			return "Seven";
		case 8:
			return "Eight";
		case 9:
			return "Nine";
		case 10:
			return "Ten";
		case 11:
			return "Eleven";
		case 12:
			return "Twelve";
		case 13:
			return "Thriteen";
		case 14:
			return "Fourteen";
		case 15:
			return "Fifteen";
		case 16:
			return "Sixteen";
		case 17:
			return "Seventeen";
		case 18:
			return "Eighteen";
		case 19:
			return "Nineteen";
		}
		return "";
	}

	public static String getTeen(int num) {

		switch (num) {
		case 2:
			return "Twenty";
		case 3:
			return "Thirty";
		case 4:
			return "Fourty";
		case 5:
			return "Fifty";
		case 6:
			return "Sixty";
		case 7:
			return "Seventy";
		case 8:
			return "Eighty";
		case 9:
			return "Ninety";
		}
		return "";

	}

	// This method will provide whole number string representation
	public static String getWholeWord(int number) {

		String output = "";
		int input = 0;

		String inputNumberString = String.valueOf(number);

		int lastNum = Integer
				.valueOf(inputNumberString.substring(inputNumberString.length() - 1, inputNumberString.length()));
		int numberBeforeLast = (number > 9
				? (Integer.valueOf(
						inputNumberString.substring(inputNumberString.length() - 2, inputNumberString.length() - 1)))
				: 0);

		if (number >= 1000) {
			input = number / 1000;
			output = getWholeWord(input) + " Thousand ";
		}
		input = number % 1000;
		if (input >= 100) {
			int tempNum = input / 100;
			output += getString(tempNum) + " Hundread ";
		}

		if (numberBeforeLast > 0) {
			int tempNum = input;
			if (numberBeforeLast == 1) {
				tempNum = Integer.valueOf(String.valueOf(numberBeforeLast) + lastNum);
				output += getString(tempNum);
				lastNum = 0;
				numberBeforeLast = 1;
			} else {
				output += getTeen(numberBeforeLast) + " ";
			}
		}

		if (lastNum > 0) {
			output += getString(lastNum) + " ";
		} else if (numberBeforeLast == 0 && number < 100) {
			output += getString(lastNum) + " ";
		}
		return output;
	}

	// This method will return decimal value String representation
	public static String getDecimalValue(String decimal) {
		String output = "";

		// check whether the decimal string contains fractions
		if (decimal.contains(".")) {

			// Identify the fraction and non fraction parts in decimal
			String partBeforeDecimalPoint = decimal.split("\\.")[0];
			String partAfterDecimalPoint = decimal.split("\\.")[1];

			if (partBeforeDecimalPoint.length() > 0)
				output = getWholeWord(Integer.parseInt(partBeforeDecimalPoint));

			if (partAfterDecimalPoint.length() > 0)
				output += ((output.length() > 0 ? " and " : "") + getWholeWord(Integer.parseInt(partAfterDecimalPoint))
						+ " Paisa");

		} else {
			output = getWholeWord(Integer.parseInt(decimal));
		}
		return output;
	}

	public String getFirstDay(Date d) throws Exception {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(d);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		Date dddd = calendar.getTime();
		SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MMM-yy");
		return sdf1.format(dddd);
	}

	public String getLastDay(Date d) throws Exception {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(d);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		Date dddd = calendar.getTime();
		SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MMM-yy");
		return sdf1.format(dddd);
	}

	public BigDecimal getSufCharge(String payId, String txnType, String paymentType, String mopType, String baseAmount,
			List<SUFDetail> sufCharge, String paymentRegion) {
		try {

			BigDecimal fixedCharge = null;
			BigDecimal percentageCharge = null;

			for (SUFDetail suf : sufCharge) {

				String slabArray[] = suf.getSlab().split("-");

				BigDecimal baseZero = new BigDecimal(slabArray[0]);
				BigDecimal baseOne = new BigDecimal(slabArray[1]);
				BigDecimal baseAmountBigDecimal = new BigDecimal(baseAmount);

				if (suf.getPayId().equalsIgnoreCase(payId) && suf.getTxnType().equalsIgnoreCase(txnType)
						&& suf.getPaymentType().equalsIgnoreCase(paymentType)
						&& suf.getMopType().equalsIgnoreCase(mopType)
						&& suf.getPaymentRegion().equalsIgnoreCase(paymentRegion)) {

					if ((baseAmountBigDecimal.compareTo(baseZero) == 1 || baseAmountBigDecimal.compareTo(baseZero) == 0)
							&& (baseAmountBigDecimal.compareTo(baseOne) == -1)
							|| baseAmountBigDecimal.compareTo(baseOne) == 0) {

						fixedCharge = new BigDecimal(suf.getFixedCharge());
						percentageCharge = (new BigDecimal(suf.getPercentageAmount())
								.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
								.multiply(new BigDecimal(baseAmount))).setScale(2, RoundingMode.FLOOR);

						break;
					}
				}

			}
			if (fixedCharge == null && percentageCharge == null) {
				fixedCharge = new BigDecimal("0.00");
				percentageCharge = new BigDecimal("0.00");
			} else if (fixedCharge == null && !(percentageCharge == null)) {
				fixedCharge = new BigDecimal("0.00");
			} else if (!(fixedCharge == null) && percentageCharge == null) {
				percentageCharge = new BigDecimal("0.00");
			}
			return fixedCharge.add(percentageCharge);
		} catch (Exception ex) {
			logger.error("Exception caught while calculate suf charges for monthly invoice pdf : ", ex);
			return null;
		}
	}

}
