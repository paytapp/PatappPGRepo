package com.paymentgateway.commons.email;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itextpdf.text.Anchor;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.html.WebColors;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.paymentgateway.commons.action.AbstractSecureAction;
import com.paymentgateway.commons.dao.ChargebackDao;
import com.paymentgateway.commons.dao.NodalTransferDao;
import com.paymentgateway.commons.dao.SUFDetailDao;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.user.Chargeback;
import com.paymentgateway.commons.user.CibNodalTransaction;
import com.paymentgateway.commons.user.MerchantPaymentAdviseDownloadObject;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.PaymentSearchDownloadObject;
import com.paymentgateway.commons.user.SUFDetail;
import com.paymentgateway.commons.user.TransactionSearchDownloadObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.AWSSESEmailService;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TxnType;

/**
 * @author Sandeep Sharma
 */

@Component
public class MerchantPaymentAdviseReportpdfAutoSend extends AbstractSecureAction {

	@Autowired
	PaymentAdviseReportFileCreator paymentAdviseReportFileCreator;

	@Autowired
	private SUFDetailDao sufDetailDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	AWSSESEmailService awsSESEmailService;
	
	@Autowired
	PepipostEmailSender pepipostEmailSender;

	@Autowired
	private NodalTransferDao nodalTransferDao;

	@Autowired
	private ChargebackDao chargebackDao;
	
	@Autowired
	private PropertiesManager propertiesManager;
	
	@Autowired
	private UserSettingDao userSettingDao;


	private static Logger logger = LoggerFactory.getLogger(MerchantPaymentAdviseReportpdfAutoSend.class.getName());
	private static final long serialVersionUID = 7658901196135972529L;

	private String payoutDate;
	private String merchantPayId;
	private String subMerchantPayId;
	private String filename;
	private String filenameexcel;
	private FileInputStream fileInputStream;
	private User sessionUser = new User();

	private List<PaymentSearchDownloadObject> settlementdata = new ArrayList<PaymentSearchDownloadObject>();
	Map<String, MerchantPaymentAdviseDownloadObject> saleSettledMap = new HashMap<String, MerchantPaymentAdviseDownloadObject>();
	Map<String, MerchantPaymentAdviseDownloadObject> refundSettledMap = new HashMap<String, MerchantPaymentAdviseDownloadObject>();
	Image img = null;

	BigDecimal saleAmount = new BigDecimal("0.00");
	BigDecimal refundAmount = new BigDecimal("0.00");
	Document document = new Document(PageSize.A4, 20, 20, 20, 10);

	@SuppressWarnings("unchecked")
	public void getpdfFileForEmail(String subMerchantPayId, String merchantPayId, String sessionUserPayId,
			String payoutDate, String currency) {

		sessionUser = userDao.findPayId(sessionUserPayId);
		List<Merchants> merchantList = new ArrayList<Merchants>();
		if (merchantPayId.equalsIgnoreCase("ALL")) {
			Map<String,Merchants> merchantMap = userSettingDao.getActiveMerchantByPaymentAdvice();
			merchantList = userDao.getActiveMerchantByPaymentAdviceFromMerchantList(merchantMap);
		} else {
			if(StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equals("null") && !subMerchantPayId.equalsIgnoreCase("ALL")){
				merchantList.add(getMerchant(userDao.findPayId(subMerchantPayId)));
			}else{
				merchantList.add(getMerchant(userDao.findPayId(merchantPayId)));
			}
			
		}
		for (Merchants merchant : merchantList) {
			try {
				int saletxn = 0;
				int refundtxn = 0;

				List<TransactionSearchDownloadObject> transactionList = new ArrayList<TransactionSearchDownloadObject>();
				List<MerchantPaymentAdviseDownloadObject> paymentAdviceList = new ArrayList<MerchantPaymentAdviseDownloadObject>();
				List<CibNodalTransaction> cibData = new ArrayList<CibNodalTransaction>();
				List<SUFDetail> sufCharge = new ArrayList<SUFDetail>();
				List<Chargeback> userChargeback = new ArrayList<Chargeback>();
				List<Merchants> subMerchantList = new ArrayList<Merchants>();
				SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy");
				SimpleDateFormat dateFor = new SimpleDateFormat("yyyy-MM-dd");
				SimpleDateFormat sdftxndate = new SimpleDateFormat("yyyyMMdd");
				SimpleDateFormat dateForshow = new SimpleDateFormat("dd MMMM yyyy");

				setPayoutDate(DateCreater.toDateTimeformatCreater(payoutDate));
				

				String merchPayId = "";
				String subMerchPayId = "";

				// Super Merchant
				if (merchant.getIsSuperMerchant() == true) {
					merchPayId = merchant.getPayId();
					UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(merchant.getPayId());

					if (subMerchantPayId.equalsIgnoreCase("ALL") || merchantSettings.isPaymentAdviceFlag()) {
						subMerchantList = userDao.getSuperMerchantAndSubMerchantListBySuperPayId(merchant.getPayId());

					} else {
						User subMerchant = userDao.findPayId(subMerchantPayId);
						subMerchPayId = subMerchant.getPayId();
						subMerchantList.add(getMerchant(subMerchant));
					}

					// subMerchant
				} else if (merchant.getIsSuperMerchant() == false
						&& StringUtils.isNotBlank(merchant.getSuperMerchantId())) {
					merchPayId = merchant.getSuperMerchantId();
					subMerchPayId = merchant.getPayId();

					// Merchant
				} else {
					merchPayId = merchant.getPayId();

				}

				if (merchPayId.equalsIgnoreCase("All")) {
					sufCharge = sufDetailDao.getAllActiveSufDetails();
				} else {
					sufCharge = sufDetailDao.fetchSufChargeByPayId(merchPayId);
				}
				
				if (subMerchantPayId.equalsIgnoreCase("null") || subMerchantPayId.equalsIgnoreCase("All")) {
					if (subMerchantList.size() > 0) {
						for (Merchants merch : subMerchantList) {
							senMailToSubmerchants(payoutDate, merch, currency);
						}
					}
				}
				Date fRom = sdf2.parse(payoutDate);
				String payoutDatePaymentAdvice = dateFor.format(fRom);
				paymentAdviceList = paymentAdviseReportFileCreator.merchantPaymentAdviseDownloadAutoSendForSale(
						merchPayId, subMerchPayId, payoutDatePaymentAdvice, sessionUser, currency);
				//logger.info("List create successfully for Auto Send Download MerchantPaymentAdviceReport");
				saleSettledMap.clear();
				refundSettledMap.clear();
				for (MerchantPaymentAdviseDownloadObject paymentAdvice : paymentAdviceList) {
					if (paymentAdvice.getOrigTxnType().equals(TxnType.SALE.getName())) {
						saleSettledMap.put(paymentAdvice.getOid(), paymentAdvice);

					} else {
						refundSettledMap.put(paymentAdvice.getOid(), paymentAdvice);
					}
				}
				DateFormat df = new SimpleDateFormat("ddMMyyhhmmss");
				if (StringUtils.isNotBlank(subMerchPayId)) {
					merchant = getMerchant(userDao.findPayId(subMerchPayId));
					filename = "DailyPaymentsAdvise" + merchant.getPayId() + df.format(new Date()) + ".pdf";
				} else {
					filename = "DailyPaymentsAdvise" + merchant.getPayId() + df.format(new Date()) + ".pdf";
				}
				Document document = null;
				ByteArrayOutputStream baos = null;
				byte[] bytes = null;
				document = new Document(PageSize.A4, 20, 20, 20, 10);
				baos = new ByteArrayOutputStream();
				PdfWriter.getInstance(document, baos);
				document.open();
				document.addTitle("Payment Advise");
				document.addSubject("Merchant Payment on Payment Gateway Solution Private Limited");
				document.addKeywords("Payment Advice, Payment Gateway");
				document.addAuthor("Payment Gateway Solution Private Limited, created on " + new Date());
				logger.info("inside the pdf body for Payment Advice");
				Font tableHeaderFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
				tableHeaderFont.setColor(BaseColor.GRAY);
				Font totalFont = new Font(Font.FontFamily.HELVETICA, 10);
				totalFont.setColor(BaseColor.WHITE);
				Font subCatFont = new Font(Font.FontFamily.HELVETICA, 10);
				Font headingtop = new Font(Font.FontFamily.HELVETICA, 10);
				headingtop.setColor(BaseColor.GRAY);
				Font catFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
				Font smallFont = new Font(Font.FontFamily.HELVETICA, 9);
				Font footerFont = new Font(Font.FontFamily.HELVETICA, 10);
				footerFont.setColor(BaseColor.BLUE);

				Set<String> utr = new HashSet<String>();
				String utrNo = "NA";
				String delim = "-";
				String transferMode = "NA";
				String fromdate = "NA";
				String todate = "NA";
				String account = "NA";
				String dateFrom = "NA";
				String dateTo = "NA";
				Date createDate = null;
				Set<String> dateIndex = new HashSet<String>();
				Set<String> transferModeSet = new HashSet<String>();

				BigDecimal SUFAmount = new BigDecimal("0.00");
				BigDecimal ccTotalAmount = new BigDecimal("0.00");
				BigDecimal dcTotalAmount = new BigDecimal("0.00");
				BigDecimal nbTotalAmount = new BigDecimal("0.00");
				BigDecimal inTotalAmount = new BigDecimal("0.00");
				BigDecimal upTotalAmount = new BigDecimal("0.00");
				BigDecimal wlTotalAmount = new BigDecimal("0.00");
				BigDecimal impsTotalAmount = new BigDecimal("0.00");
				BigDecimal neftTotalAmount = new BigDecimal("0.00");
				BigDecimal rtgsTotalAmount = new BigDecimal("0.00");
				BigDecimal codTotalAmount = new BigDecimal("0.00");

				BigDecimal ccTdr = new BigDecimal("0.00");
				BigDecimal dcTdr = new BigDecimal("0.00");
				BigDecimal nbTdr = new BigDecimal("0.00");
				BigDecimal inTdr = new BigDecimal("0.00");
				BigDecimal upTdr = new BigDecimal("0.00");
				BigDecimal wlTdr = new BigDecimal("0.00");
				BigDecimal impsTdr = new BigDecimal("0.00");
				BigDecimal neftTdr = new BigDecimal("0.00");
				BigDecimal rtgsTdr = new BigDecimal("0.00");
				BigDecimal codTdr = new BigDecimal("0.00");

				BigDecimal ccGst = new BigDecimal("0.00");
				BigDecimal dcGst = new BigDecimal("0.00");
				BigDecimal nbGst = new BigDecimal("0.00");
				BigDecimal inGst = new BigDecimal("0.00");
				BigDecimal upGst = new BigDecimal("0.00");
				BigDecimal wlGst = new BigDecimal("0.00");
				BigDecimal impsGst = new BigDecimal("0.00");
				BigDecimal neftGst = new BigDecimal("0.00");
				BigDecimal rtgsGst = new BigDecimal("0.00");
				BigDecimal codGst = new BigDecimal("0.00");

				BigDecimal ccSuf = new BigDecimal("0.00");
				BigDecimal dcSuf = new BigDecimal("0.00");
				BigDecimal nbSuf = new BigDecimal("0.00");
				BigDecimal inSuf = new BigDecimal("0.00");
				BigDecimal upSuf = new BigDecimal("0.00");
				BigDecimal wlSuf = new BigDecimal("0.00");
				BigDecimal impsSuf = new BigDecimal("0.00");
				BigDecimal neftSuf = new BigDecimal("0.00");
				BigDecimal rtgsSuf = new BigDecimal("0.00");
				BigDecimal codSuf = new BigDecimal("0.00");

				BigDecimal totalAmount = new BigDecimal("0.00");
				BigDecimal totalTdr = new BigDecimal("0.00");
				BigDecimal totalGst = new BigDecimal("0.00");
				BigDecimal totalSuf = new BigDecimal("0.00");

				BigDecimal refundccTotalAmount = new BigDecimal("0.00");
				BigDecimal refunddcTotalAmount = new BigDecimal("0.00");
				BigDecimal refundnbTotalAmount = new BigDecimal("0.00");
				BigDecimal refundinTotalAmount = new BigDecimal("0.00");
				BigDecimal refundupTotalAmount = new BigDecimal("0.00");
				BigDecimal refundwlTotalAmount = new BigDecimal("0.00");
				BigDecimal refundimpsTotalAmount = new BigDecimal("0.00");
				BigDecimal refundneftTotalAmount = new BigDecimal("0.00");
				BigDecimal refundrtgsTotalAmount = new BigDecimal("0.00");
				BigDecimal refundcodTotalAmount = new BigDecimal("0.00");

				BigDecimal refundccTdr = new BigDecimal("0.00");
				BigDecimal refunddcTdr = new BigDecimal("0.00");
				BigDecimal refundnbTdr = new BigDecimal("0.00");
				BigDecimal refundinTdr = new BigDecimal("0.00");
				BigDecimal refundupTdr = new BigDecimal("0.00");
				BigDecimal refundwlTdr = new BigDecimal("0.00");
				BigDecimal refundimpsTdr = new BigDecimal("0.00");
				BigDecimal refundneftTdr = new BigDecimal("0.00");
				BigDecimal refundrtgsTdr = new BigDecimal("0.00");
				BigDecimal refundcodTdr = new BigDecimal("0.00");

				BigDecimal refundccGst = new BigDecimal("0.00");
				BigDecimal refunddcGst = new BigDecimal("0.00");
				BigDecimal refundnbGst = new BigDecimal("0.00");
				BigDecimal refundinGst = new BigDecimal("0.00");
				BigDecimal refundupGst = new BigDecimal("0.00");
				BigDecimal refundwlGst = new BigDecimal("0.00");
				BigDecimal refundimpsGst = new BigDecimal("0.00");
				BigDecimal refundneftGst = new BigDecimal("0.00");
				BigDecimal refundrtgsGst = new BigDecimal("0.00");
				BigDecimal refundcodGst = new BigDecimal("0.00");

				BigDecimal refundccSuf = new BigDecimal("0.00");
				BigDecimal refunddcSuf = new BigDecimal("0.00");
				BigDecimal refundnbSuf = new BigDecimal("0.00");
				BigDecimal refundinSuf = new BigDecimal("0.00");
				BigDecimal refundupSuf = new BigDecimal("0.00");
				BigDecimal refundwlSuf = new BigDecimal("0.00");
				BigDecimal refundimpsSuf = new BigDecimal("0.00");
				BigDecimal refundneftSuf = new BigDecimal("0.00");
				BigDecimal refundrtgsSuf = new BigDecimal("0.00");
				BigDecimal refundcodSuf = new BigDecimal("0.00");

				BigDecimal refundtotalAmount = new BigDecimal("0.00");
				BigDecimal refundtotalTdr = new BigDecimal("0.00");
				BigDecimal refundtotalGst = new BigDecimal("0.00");
				BigDecimal refundtotalSuf = new BigDecimal("0.00");

				BigDecimal settlementAmount = new BigDecimal("0.00");

				Boolean cc = false;
				Boolean dc = false;
				Boolean nb = false;
				Boolean in = false;
				Boolean up = false;
				Boolean wl = false;
				Boolean imps = false;
				Boolean rtgs = false;
				Boolean neft = false;
				Boolean cod = false;
				Boolean refundcc = false;
				Boolean refunddc = false;
				Boolean refundnb = false;
				Boolean refundin = false;
				Boolean refundup = false;
				Boolean refundwl = false;
				Boolean refundimps = false;
				Boolean refundrtgs = false;
				Boolean refundneft = false;
				Boolean refundcod = false;

				Paragraph logoParagraph = null;
				Paragraph headingParagraph = null;
				Paragraph nameParagraph = null;
				Paragraph reporttypeParagraph = null;

				Paragraph custDetailsParagraph = null;
				Paragraph footerParagraph = null;

				User user = null;

				String merchantImageAddr = null;
				if (StringUtils.isNotBlank(subMerchPayId)) {
					user = userDao.findPayId(subMerchPayId);
				} else {
					user = userDao.findPayId(merchPayId);
				}
				if (StringUtils.isNotBlank(subMerchantPayId)) {
						merchantImageAddr = getMerchantLogoPath(user.getPayId());

						File file = new File(merchantImageAddr);

						if (!file.exists()) {
							merchantImageAddr = getMerchantLogoPath(user.getSuperMerchantId());
						}
				} else if (StringUtils.isBlank(user.getSuperMerchantId())) {
					merchantImageAddr = getMerchantLogoPath(merchantPayId);
					File file = new File(merchantImageAddr);

					if (!file.exists()) {
						merchantImageAddr = getMerchantLogoPath(user.getSuperMerchantId());
					}
				} else {
					merchantImageAddr = getMerchantLogoPath(merchantPayId);
				}

				//logger.info("inside set sale transaction");

				for (MerchantPaymentAdviseDownloadObject sale : saleSettledMap.values()) {
					saletxn++;
					try {
						createDate = sdf1.parse(sale.getCreateDate());
					} catch (ParseException e) {
						logger.error("Exception in date parse ", e);
					}
					dateIndex.add(sdftxndate.format(createDate));
					utr.add(sale.getUtrNo());
					BigDecimal baseAmount = new BigDecimal(sale.getGrossAmount());
					BigDecimal tdr = new BigDecimal(sale.getTdr());
					BigDecimal gst = new BigDecimal(sale.getGst());
					if (sale.getPaymentType().equalsIgnoreCase("Credit Card")) {
						cc = true;
						ccTotalAmount = ccTotalAmount.add(baseAmount);
						ccTdr = ccTdr.add(tdr);
						ccGst = ccGst.add(gst);
						SUFAmount = getSufCharge(sale.getPayId(), sale.getOrigTxnType(), sale.getPaymentType(),
								sale.getCardNetwork(), sale.getBaseAmount(), sufCharge, sale.getPaymentRegion());
						ccSuf = ccSuf.add(SUFAmount);
						totalAmount = totalAmount.add(baseAmount);
						totalTdr = totalTdr.add(tdr);
						totalGst = totalGst.add(gst);
						totalSuf = totalSuf.add(SUFAmount);

					}
					if (sale.getPaymentType().equalsIgnoreCase("Debit Card")) {
						dc = true;
						dcTotalAmount = dcTotalAmount.add(baseAmount);
						dcTdr = dcTdr.add(tdr);
						dcGst = dcGst.add(gst);
						SUFAmount = getSufCharge(sale.getPayId(), sale.getOrigTxnType(), sale.getPaymentType(),
								sale.getCardNetwork(), sale.getBaseAmount(), sufCharge, sale.getPaymentRegion());
						dcSuf = dcSuf.add(SUFAmount);
						totalAmount = totalAmount.add(baseAmount);
						totalTdr = totalTdr.add(tdr);
						totalGst = totalGst.add(gst);
						totalSuf = totalSuf.add(SUFAmount);

					}
					if (sale.getPaymentType().equalsIgnoreCase("Cod")) {
						cod = true;
						codTotalAmount = codTotalAmount.add(baseAmount);
						codTdr = codTdr.add(tdr);
						codGst = codGst.add(gst);
						SUFAmount = getSufCharge(sale.getPayId(), sale.getOrigTxnType(), sale.getPaymentType(),
								sale.getCardNetwork(), sale.getBaseAmount(), sufCharge, sale.getPaymentRegion());
						codSuf = codSuf.add(SUFAmount);
						totalAmount = totalAmount.add(baseAmount);
						totalTdr = totalTdr.add(tdr);
						totalGst = totalGst.add(gst);
						totalSuf = totalSuf.add(SUFAmount);

					}
					if (sale.getPaymentType().equalsIgnoreCase("Net Banking")) {
						nb = true;
						nbTotalAmount = nbTotalAmount.add(baseAmount);
						nbTdr = nbTdr.add(tdr);
						nbGst = nbGst.add(gst);
						SUFAmount = getSufCharge(sale.getPayId(), sale.getOrigTxnType(), sale.getPaymentType(),
								sale.getCardNetwork(), sale.getBaseAmount(), sufCharge, sale.getPaymentRegion());
						nbSuf = nbSuf.add(SUFAmount);
						totalAmount = totalAmount.add(baseAmount);
						totalTdr = totalTdr.add(tdr);
						totalGst = totalGst.add(gst);
						totalSuf = totalSuf.add(SUFAmount);

					}
					if (sale.getPaymentType().equalsIgnoreCase("Wallet")) {
						wl = true;
						wlTotalAmount = wlTotalAmount.add(baseAmount);
						wlTdr = wlTdr.add(tdr);
						wlGst = wlGst.add(gst);
						SUFAmount = getSufCharge(sale.getPayId(), sale.getOrigTxnType(), sale.getPaymentType(),
								sale.getCardNetwork(), sale.getBaseAmount(), sufCharge, sale.getPaymentRegion());
						wlSuf = wlSuf.add(SUFAmount);
						totalAmount = totalAmount.add(baseAmount);
						totalTdr = totalTdr.add(tdr);
						totalGst = totalGst.add(gst);
						totalSuf = totalSuf.add(SUFAmount);

					}

					if (sale.getPaymentType().equalsIgnoreCase("UPI")) {
						up = true;
						upTotalAmount = upTotalAmount.add(baseAmount);
						upTdr = upTdr.add(tdr);
						upGst = upGst.add(gst);
						SUFAmount = getSufCharge(sale.getPayId(), sale.getOrigTxnType(), sale.getPaymentType(),
								sale.getCardNetwork(), sale.getBaseAmount(), sufCharge, sale.getPaymentRegion());
						upSuf = upSuf.add(SUFAmount);
						totalAmount = totalAmount.add(baseAmount);
						totalTdr = totalTdr.add(tdr);
						totalGst = totalGst.add(gst);
						totalSuf = totalSuf.add(SUFAmount);

					}

					if (sale.getPaymentType().equalsIgnoreCase("NEFT")) {
						neft = true;
						neftTotalAmount = neftTotalAmount.add(baseAmount);
						neftTdr = neftTdr.add(tdr);
						neftGst = neftGst.add(gst);
						SUFAmount = getSufCharge(sale.getPayId(), sale.getOrigTxnType(), sale.getPaymentType(),
								sale.getCardNetwork(), sale.getBaseAmount(), sufCharge, sale.getPaymentRegion());
						neftSuf = neftSuf.add(SUFAmount);
						totalAmount = totalAmount.add(baseAmount);
						totalTdr = totalTdr.add(tdr);
						totalGst = totalGst.add(gst);
						totalSuf = totalSuf.add(SUFAmount);

					}

					if (sale.getPaymentType().equalsIgnoreCase("RTGS")) {
						rtgs = true;
						rtgsTotalAmount = rtgsTotalAmount.add(baseAmount);
						rtgsTdr = rtgsTdr.add(tdr);
						rtgsGst = rtgsGst.add(gst);
						SUFAmount = getSufCharge(sale.getPayId(), sale.getOrigTxnType(), sale.getPaymentType(),
								sale.getCardNetwork(), sale.getBaseAmount(), sufCharge, sale.getPaymentRegion());
						rtgsSuf = rtgsSuf.add(SUFAmount);
						totalAmount = totalAmount.add(baseAmount);
						totalTdr = totalTdr.add(tdr);
						totalGst = totalGst.add(gst);
						totalSuf = totalSuf.add(SUFAmount);

					}

					if (sale.getPaymentType().equalsIgnoreCase("IMPS")) {
						imps = true;
						impsTotalAmount = impsTotalAmount.add(baseAmount);
						impsTdr = impsTdr.add(tdr);
						impsGst = impsGst.add(gst);
						SUFAmount = getSufCharge(sale.getPayId(), sale.getOrigTxnType(), sale.getPaymentType(),
								sale.getCardNetwork(), sale.getBaseAmount(), sufCharge, sale.getPaymentRegion());
						impsSuf = impsSuf.add(SUFAmount);
						totalAmount = totalAmount.add(baseAmount);
						totalTdr = totalTdr.add(tdr);
						totalGst = totalGst.add(gst);
						totalSuf = totalSuf.add(SUFAmount);

					}

					if (sale.getPaymentType().equalsIgnoreCase("international")) {
						in = true;
						inTotalAmount = inTotalAmount.add(baseAmount);
						inTdr = inTdr.add(tdr);
						inGst = inGst.add(gst);
						SUFAmount = getSufCharge(sale.getPayId(), sale.getOrigTxnType(), sale.getPaymentType(),
								sale.getCardNetwork(), sale.getBaseAmount(), sufCharge, sale.getPaymentRegion());
						inSuf = inSuf.add(SUFAmount);
						totalAmount = totalAmount.add(baseAmount);
						totalTdr = totalTdr.add(tdr);
						totalGst = totalGst.add(gst);
						totalSuf = totalSuf.add(SUFAmount);

					}

				}
				//logger.info("inside set refund transaction");

				for (MerchantPaymentAdviseDownloadObject refund : refundSettledMap.values()) {
					refundtxn++;
					try {
						createDate = sdf1.parse(refund.getCreateDate());
					} catch (ParseException e) {
						logger.error("Exception in date parse ", e);
					}
					dateIndex.add(sdftxndate.format(createDate));
					BigDecimal baseAmount = new BigDecimal(refund.getBaseAmount());
					BigDecimal tdr = new BigDecimal(refund.getTdr());
					BigDecimal gst = new BigDecimal(refund.getGst());
					if (refund.getPaymentType().equalsIgnoreCase("Credit Card")) {
						refundcc = true;
						refundccTotalAmount = refundccTotalAmount.add(baseAmount);
						refundccTdr = refundccTdr.add(tdr);
						refundccGst = refundccGst.add(gst);
						SUFAmount = getSufCharge(refund.getPayId(), refund.getOrigTxnType(), refund.getPaymentType(),
								refund.getCardNetwork(), refund.getBaseAmount(), sufCharge, refund.getPaymentRegion());
						refundccSuf = refundccSuf.add(SUFAmount);
						refundtotalAmount = refundtotalAmount.add(baseAmount);
						refundtotalTdr = refundtotalTdr.add(tdr);
						refundtotalGst = refundtotalGst.add(gst);
						refundtotalSuf = refundtotalSuf.add(SUFAmount);

					}
					if (refund.getPaymentType().equalsIgnoreCase("Debit Card")) {
						refunddc = true;
						refunddcTotalAmount = refunddcTotalAmount.add(baseAmount);
						refunddcTdr = refunddcTdr.add(tdr);
						refunddcGst = refunddcGst.add(gst);
						SUFAmount = getSufCharge(refund.getPayId(), refund.getOrigTxnType(), refund.getPaymentType(),
								refund.getCardNetwork(), refund.getBaseAmount(), sufCharge, refund.getPaymentRegion());
						refunddcSuf = refunddcSuf.add(SUFAmount);
						refundtotalAmount = refundtotalAmount.add(baseAmount);
						refundtotalTdr = refundtotalTdr.add(tdr);
						refundtotalGst = refundtotalGst.add(gst);
						refundtotalSuf = refundtotalSuf.add(SUFAmount);
					}
					if (refund.getPaymentType().equalsIgnoreCase("Cod")) {
						refundcod = true;
						refundcodTotalAmount = refundcodTotalAmount.add(baseAmount);
						refundcodTdr = refundcodTdr.add(tdr);
						refundcodGst = refundcodGst.add(gst);
						SUFAmount = getSufCharge(refund.getPayId(), refund.getOrigTxnType(), refund.getPaymentType(),
								refund.getCardNetwork(), refund.getBaseAmount(), sufCharge, refund.getPaymentRegion());
						refundcodSuf = refundcodSuf.add(SUFAmount);
						refundtotalAmount = refundtotalAmount.add(baseAmount);
						refundtotalTdr = refundtotalTdr.add(tdr);
						refundtotalGst = refundtotalGst.add(gst);
						refundtotalSuf = refundtotalSuf.add(SUFAmount);

					}
					if (refund.getPaymentType().equalsIgnoreCase("Net Banking")) {
						refundnb = true;
						refundnbTotalAmount = refundnbTotalAmount.add(baseAmount);
						refundnbTdr = refundnbTdr.add(tdr);
						refundnbGst = refundnbGst.add(gst);
						SUFAmount = getSufCharge(refund.getPayId(), refund.getOrigTxnType(), refund.getPaymentType(),
								refund.getCardNetwork(), refund.getBaseAmount(), sufCharge, refund.getPaymentRegion());
						refundnbSuf = refundnbSuf.add(SUFAmount);
						refundtotalAmount = refundtotalAmount.add(baseAmount);
						refundtotalTdr = refundtotalTdr.add(tdr);
						refundtotalGst = refundtotalGst.add(gst);
						refundtotalSuf = refundtotalSuf.add(SUFAmount);

					}
					if (refund.getPaymentType().equalsIgnoreCase("Wallet")) {
						refundwl = true;
						refundwlTotalAmount = refundwlTotalAmount.add(baseAmount);
						refundwlTdr = refundwlTdr.add(tdr);
						refundwlGst = refundwlGst.add(gst);
						SUFAmount = getSufCharge(refund.getPayId(), refund.getOrigTxnType(), refund.getPaymentType(),
								refund.getCardNetwork(), refund.getBaseAmount(), sufCharge, refund.getPaymentRegion());
						refundwlSuf = refundwlSuf.add(SUFAmount);
						refundtotalAmount = refundtotalAmount.add(baseAmount);
						refundtotalTdr = refundtotalTdr.add(tdr);
						refundtotalGst = refundtotalGst.add(gst);
						refundtotalSuf = refundtotalSuf.add(SUFAmount);

					}

					if (refund.getPaymentType().equalsIgnoreCase("UPI")) {
						refundup = true;
						refundupTotalAmount = refundupTotalAmount.add(baseAmount);
						refundupTdr = refundupTdr.add(tdr);
						refundupGst = refundupGst.add(gst);
						SUFAmount = getSufCharge(refund.getPayId(), refund.getOrigTxnType(), refund.getPaymentType(),
								refund.getCardNetwork(), refund.getBaseAmount(), sufCharge, refund.getPaymentRegion());
						refundupSuf = refundupSuf.add(SUFAmount);
						refundtotalAmount = refundtotalAmount.add(baseAmount);
						refundtotalTdr = refundtotalTdr.add(tdr);
						refundtotalGst = refundtotalGst.add(gst);
						refundtotalSuf = refundtotalSuf.add(SUFAmount);

					}

					if (refund.getPaymentType().equalsIgnoreCase("NEFT")) {
						refundneft = true;
						refundneftTotalAmount = refundneftTotalAmount.add(baseAmount);
						refundneftTdr = refundneftTdr.add(tdr);
						refundneftGst = refundneftGst.add(gst);
						SUFAmount = getSufCharge(refund.getPayId(), refund.getOrigTxnType(), refund.getPaymentType(),
								refund.getCardNetwork(), refund.getBaseAmount(), sufCharge, refund.getPaymentRegion());
						refundneftSuf = refundneftSuf.add(SUFAmount);
						refundtotalAmount = refundtotalAmount.add(baseAmount);
						refundtotalTdr = refundtotalTdr.add(tdr);
						refundtotalGst = refundtotalGst.add(gst);
						refundtotalSuf = refundtotalSuf.add(SUFAmount);

					}

					if (refund.getPaymentType().equalsIgnoreCase("RTGS")) {
						refundrtgs = true;
						refundrtgsTotalAmount = refundrtgsTotalAmount.add(baseAmount);
						refundrtgsTdr = refundrtgsTdr.add(tdr);
						refundrtgsGst = refundrtgsGst.add(gst);
						SUFAmount = getSufCharge(refund.getPayId(), refund.getOrigTxnType(), refund.getPaymentType(),
								refund.getCardNetwork(), refund.getBaseAmount(), sufCharge, refund.getPaymentRegion());
						refundrtgsSuf = refundrtgsSuf.add(SUFAmount);
						refundtotalAmount = refundtotalAmount.add(baseAmount);
						refundtotalTdr = refundtotalTdr.add(tdr);
						refundtotalGst = refundtotalGst.add(gst);
						refundtotalSuf = refundtotalSuf.add(SUFAmount);

					}

					if (refund.getPaymentType().equalsIgnoreCase("IMPS")) {
						refundimps = true;
						refundimpsTotalAmount = refundimpsTotalAmount.add(baseAmount);
						refundimpsTdr = refundimpsTdr.add(tdr);
						refundimpsGst = refundimpsGst.add(gst);
						SUFAmount = getSufCharge(refund.getPayId(), refund.getOrigTxnType(), refund.getPaymentType(),
								refund.getCardNetwork(), refund.getBaseAmount(), sufCharge, refund.getPaymentRegion());
						refundimpsSuf = refundimpsSuf.add(SUFAmount);
						refundtotalAmount = refundtotalAmount.add(baseAmount);
						refundtotalTdr = refundtotalTdr.add(tdr);
						refundtotalGst = refundtotalGst.add(gst);
						refundtotalSuf = refundtotalSuf.add(SUFAmount);

					}

					if (refund.getPaymentType().equalsIgnoreCase("international")) {
						refundin = true;
						refundinTotalAmount = refundinTotalAmount.add(baseAmount);
						refundinTdr = refundinTdr.add(tdr);
						refundinGst = refundinGst.add(gst);
						SUFAmount = getSufCharge(refund.getPayId(), refund.getOrigTxnType(), refund.getPaymentType(),
								refund.getCardNetwork(), refund.getBaseAmount(), sufCharge, refund.getPaymentRegion());
						refundinSuf = refundinSuf.add(SUFAmount);
						refundtotalAmount = refundtotalAmount.add(baseAmount);
						refundtotalTdr = refundtotalTdr.add(tdr);
						refundtotalGst = refundtotalGst.add(gst);
						refundtotalSuf = refundtotalSuf.add(SUFAmount);

					}

				}
				settlementAmount = totalAmount.subtract(totalTdr).subtract(totalGst).subtract(totalSuf)
						.subtract(codTotalAmount).subtract(refundtotalAmount.subtract(refundtotalTdr)
								.subtract(refundtotalGst).subtract(refundtotalSuf).subtract(refundcodTotalAmount));

				if (!utr.isEmpty()) {
					for (String utrIterate : utr) {
						cibData = nodalTransferDao.fetchTransferModeandAccountByUtr(utrIterate);
						if (!cibData.isEmpty()) {
							for (CibNodalTransaction cib : cibData) {
								transferModeSet.add(cib.getTxnType());
								account = cib.getBankAccountNumber();
							}
						}
					}
					transferMode = String.join(delim, transferModeSet);
					utrNo = String.join(delim, utr);
				}
				if (!dateIndex.isEmpty()) {
					try {
						Date fromDate = null;
						Date toDate = null;
						TreeSet<String> treeSet = new TreeSet<String>(dateIndex);
						String[] dateArray = treeSet.toArray(new String[treeSet.size()]);
						if (dateArray.length == 1) {
							fromDate = sdftxndate.parse(dateArray[0]);
							fromdate = dateForshow.format(fromDate);
							dateFrom = dateFor.format(fromDate) + " 00:00:00";
							todate = dateForshow.format(fromDate);
							dateTo = dateFor.format(fromDate)+ " 23:59:59";
						} else {
							fromDate = sdftxndate.parse(dateArray[0]);
							toDate = sdftxndate.parse(dateArray[dateArray.length - 1]);
							fromdate = dateForshow.format(fromDate);
							dateFrom = dateFor.format(fromDate)+ " 00:00:00";
							todate = dateForshow.format(toDate);
							dateTo = dateFor.format(toDate)+ " 23:59:59";
						}
					} catch (ParseException e) {
						logger.error("date parse Exception ", e);
					}
				}
				settlementdata.clear();
				if (!dateFrom.equalsIgnoreCase("NA") && !dateTo.equalsIgnoreCase("NA")) {
					userChargeback = chargebackDao.findChargebackByPayid(merchPayId, dateFrom, dateTo);
						settlementdata = paymentAdviseReportFileCreator.searchPaymentForDownload(merchPayId,
								subMerchPayId, dateFrom, dateTo, sessionUser, currency);
					
				}
				int chargeback = 0;
				BigDecimal chargeBackAmount = new BigDecimal("0.00");
				for (Chargeback chargeBack : userChargeback) {
					chargeback++;
					chargeBackAmount = chargeBackAmount.add(chargeBack.getTotalchargebackAmount());
				}
				String payoutDateShow = dateForshow.format(fRom);
				// Header
				//logger.info("header");

				document.add(Chunk.NEWLINE);

				float[] heading = new float[] { 100f };
				PdfPTable headingtable = new PdfPTable(1);
				headingtable.setWidths(heading);
				headingtable.getDefaultCell().setBorderWidth(0f);
				headingtable.setWidthPercentage(100f);
				DateFormat dfh = new SimpleDateFormat("ddMMyy");
				headingParagraph = new Paragraph("Daily Payment Advice : " + user.getPayId() + dfh.format(new Date()),
						headingtop);
				headingParagraph.setAlignment(Element.ALIGN_LEFT);
				headingParagraph.add(Chunk.NEWLINE);
				headingParagraph.add(Chunk.NEWLINE);
				headingtable.addCell(headingParagraph);
				document.add(headingtable);

				//logger.info("name and logo");

				float[] info = new float[] { 50f, 40f };
				PdfPTable merchantDetail = new PdfPTable(2);
				merchantDetail.setWidths(info);
				merchantDetail.getDefaultCell().setBorderWidth(0f);
				merchantDetail.setWidthPercentage(90f);
				if (user.getFirstName() != null || user.getLastName() != null) {
					nameParagraph = new Paragraph(
							user.getFirstName() + " " + user.getLastName() + Chunk.NEWLINE + user.getPayId(), catFont);
				} else {
					nameParagraph = new Paragraph(" " + Chunk.NEWLINE + user.getPayId(), catFont);
				}
				nameParagraph.setAlignment(Element.ALIGN_LEFT);
				nameParagraph.add(Chunk.NEWLINE);
				nameParagraph.add(Chunk.NEWLINE);
				merchantDetail.addCell(nameParagraph);

				Image img = null;
				if (StringUtils.isNotBlank(merchantImageAddr)) {
					File file = new File(merchantImageAddr);
					if (file.exists()) {
						img = Image.getInstance(merchantImageAddr);
						img.scaleToFit(100f, 30f);
						img.setAbsolutePosition(20f, 700f);
						logoParagraph = new Paragraph();
						logoParagraph.add(Chunk.NEWLINE);
						logoParagraph.add(img);
					} else {
						img = Image.getInstance(PropertiesManager.propertiesMap.get("noLogoMerchant"));
						img.scaleToFit(100f, 30f);
						img.setAbsolutePosition(20f, 500f);
						logoParagraph = new Paragraph();
						logoParagraph.add(Chunk.NEWLINE);
						logoParagraph.add(img);
					}
				}
				currency = propertiesManager.getAlphabaticCurrencyCode(currency);
				PdfPCell mercLogo = new PdfPCell(img);
				mercLogo.setBorderWidth(0f);
				mercLogo.setFixedHeight(20f);
				merchantDetail.addCell(new PdfPCell(mercLogo)).setHorizontalAlignment(Element.ALIGN_RIGHT);
				document.add(merchantDetail);

				document.add(Chunk.NEWLINE);
				document.add(Chunk.NEWLINE);
				document.add(Chunk.NEWLINE);

				LineSeparator ls = new LineSeparator();
				ls.setPercentage(90f);
				document.add(new Chunk(ls));

				PdfPTable dataTable = new PdfPTable(4);
				dataTable.getDefaultCell().setBorder(0);
				float[] columnWidths = new float[] { 15f, 47f, 14f, 14f };
				dataTable.setWidths(columnWidths);
				dataTable.setWidthPercentage(90f);

				//logger.info("merchant details");

				custDetailsParagraph = new Paragraph(Chunk.NEWLINE + "Settled Amount: ", smallFont);
				custDetailsParagraph.setAlignment(Paragraph.ALIGN_LEFT);
				dataTable.addCell(custDetailsParagraph);

				if (cc || dc) {
					custDetailsParagraph = new Paragraph(Chunk.NEWLINE + currency + " " + settlementAmount.toString(), smallFont);
				} else {
					custDetailsParagraph = new Paragraph(Chunk.NEWLINE + "INR " + settlementAmount.toString(), smallFont);
				}
				custDetailsParagraph.setAlignment(Paragraph.ALIGN_LEFT);
				dataTable.addCell(custDetailsParagraph);

				custDetailsParagraph = new Paragraph(Chunk.NEWLINE + "Settlement Date: ", smallFont);
				custDetailsParagraph.setAlignment(Paragraph.ALIGN_RIGHT);
				dataTable.addCell(custDetailsParagraph);

				custDetailsParagraph = new Paragraph(Chunk.NEWLINE + payoutDateShow, smallFont);
				custDetailsParagraph.setAlignment(Paragraph.ALIGN_RIGHT);
				dataTable.addCell(custDetailsParagraph);

				custDetailsParagraph = new Paragraph("Account No.: ", smallFont);
				custDetailsParagraph.setAlignment(Paragraph.ALIGN_LEFT);
				dataTable.addCell(custDetailsParagraph);

				custDetailsParagraph = new Paragraph(account, smallFont);
				custDetailsParagraph.setAlignment(Paragraph.ALIGN_LEFT);
				dataTable.addCell(custDetailsParagraph);

				custDetailsParagraph = new Paragraph("Captured From: ", smallFont);
				custDetailsParagraph.setAlignment(Paragraph.ALIGN_RIGHT);
				dataTable.addCell(custDetailsParagraph);

				custDetailsParagraph = new Paragraph(fromdate, smallFont);
				custDetailsParagraph.setAlignment(Paragraph.ALIGN_RIGHT);
				dataTable.addCell(custDetailsParagraph);

				custDetailsParagraph = new Paragraph("UTR: ", smallFont);
				custDetailsParagraph.setAlignment(Paragraph.ALIGN_LEFT);
				dataTable.addCell(custDetailsParagraph);

				custDetailsParagraph = new Paragraph(utrNo, smallFont);
				custDetailsParagraph.setAlignment(Paragraph.ALIGN_LEFT);
				dataTable.addCell(custDetailsParagraph);

				custDetailsParagraph = new Paragraph("Captured To: ", smallFont);
				custDetailsParagraph.setAlignment(Paragraph.ALIGN_RIGHT);
				dataTable.addCell(custDetailsParagraph);

				custDetailsParagraph = new Paragraph(todate, smallFont);
				custDetailsParagraph.setAlignment(Paragraph.ALIGN_RIGHT);
				dataTable.addCell(custDetailsParagraph);

				custDetailsParagraph = new Paragraph("Transfer Mode: ", smallFont);
				custDetailsParagraph.setAlignment(Paragraph.ALIGN_LEFT);
				dataTable.addCell(custDetailsParagraph);

				custDetailsParagraph = new Paragraph(transferMode, smallFont);
				custDetailsParagraph.setAlignment(Paragraph.ALIGN_LEFT);
				dataTable.addCell(custDetailsParagraph);

				custDetailsParagraph = new Paragraph("", smallFont);
				custDetailsParagraph.setAlignment(Paragraph.ALIGN_LEFT);
				dataTable.addCell(custDetailsParagraph);

				custDetailsParagraph = new Paragraph("", smallFont);
				custDetailsParagraph.setAlignment(Paragraph.ALIGN_LEFT);
				dataTable.addCell(custDetailsParagraph);

				document.add(dataTable);
				document.add(new Chunk(ls));
				document.add(Chunk.NEWLINE);
				document.add(Chunk.NEWLINE);

				float[] columnWidths3 = new float[] { 82.3f, 8.7f };
				PdfPTable p3 = new PdfPTable(2);
				p3.setWidths(columnWidths3);
				p3.getDefaultCell().setBorderWidth(0f);
				p3.setWidthPercentage(90f);

				reporttypeParagraph = new Paragraph(Chunk.NEWLINE + "SALE SETTLED ", tableHeaderFont);
				reporttypeParagraph.setAlignment(Element.ALIGN_LEFT);
				p3.addCell(reporttypeParagraph);
				
				if (cc || dc) {
					reporttypeParagraph = new Paragraph(Chunk.NEWLINE + "(In " + currency + ")", tableHeaderFont);
				} else {
					reporttypeParagraph = new Paragraph(Chunk.NEWLINE + "(In INR) ", tableHeaderFont);
				}
				
				reporttypeParagraph.setAlignment(Element.ALIGN_RIGHT);
				p3.addCell(reporttypeParagraph);

				document.add(p3);
//					document.add(new Chunk(ls));
				//logger.info("sale table");

				float[] saleWidths = { 19F, 17F, 10F, 17F, 10F, 17F };

				PdfPTable saleTable = new PdfPTable(6);
				saleTable.setWidths(saleWidths);
				saleTable.setWidthPercentage(90f);

				PdfPCell salepayemntType = new PdfPCell(new Phrase("Payment Type", catFont));
				salepayemntType.setHorizontalAlignment(Element.ALIGN_CENTER);
				salepayemntType.setVerticalAlignment(Element.ALIGN_CENTER);
				salepayemntType.setFixedHeight(15f);
				salepayemntType.setBorderWidth(0f);
				salepayemntType.setBorderWidthBottom(1f);
				saleTable.addCell(salepayemntType);

				PdfPCell saletotalAmount = new PdfPCell(new Phrase("Total Amount", catFont));
				saletotalAmount.setHorizontalAlignment(Element.ALIGN_CENTER);
				saletotalAmount.setVerticalAlignment(Element.ALIGN_CENTER);
				saletotalAmount.setFixedHeight(15f);
				saletotalAmount.setBorderWidth(0f);
				saletotalAmount.setBorderWidthBottom(1f);
				saleTable.addCell(saletotalAmount);

				PdfPCell saletdr = new PdfPCell(new Phrase("TDR", catFont));
				saletdr.setHorizontalAlignment(Element.ALIGN_CENTER);
				saletdr.setVerticalAlignment(Element.ALIGN_CENTER);
				saletdr.setFixedHeight(15f);
				saletdr.setBorderWidth(0f);
				saletdr.setBorderWidthBottom(1f);
				saleTable.addCell(saletdr);

				PdfPCell salegst = new PdfPCell(new Phrase("GST @18 %", catFont));
				salegst.setHorizontalAlignment(Element.ALIGN_CENTER);
				salegst.setVerticalAlignment(Element.ALIGN_CENTER);
				salegst.setFixedHeight(15f);
				salegst.setBorderWidth(0f);
				salegst.setBorderWidthBottom(1f);
				saleTable.addCell(salegst);

				PdfPCell salesuf = new PdfPCell(new Phrase("SUF", catFont));
				salesuf.setHorizontalAlignment(Element.ALIGN_CENTER);
				salesuf.setVerticalAlignment(Element.ALIGN_CENTER);
				salesuf.setFixedHeight(15f);
				salesuf.setBorderWidth(0f);
				salesuf.setBorderWidthBottom(1f);
				saleTable.addCell(salesuf);

				PdfPCell salenetAmount = new PdfPCell(new Phrase("Net Amount", catFont));
				salenetAmount.setHorizontalAlignment(Element.ALIGN_CENTER);
				salenetAmount.setVerticalAlignment(Element.ALIGN_CENTER);
				salenetAmount.setFixedHeight(15f);
				salenetAmount.setBorderWidth(0f);
				salenetAmount.setBorderWidthBottom(1f);
				saleTable.addCell(salenetAmount);

				document.add(new Chunk(ls));
				PdfPCell saleTT;
				PdfPCell saleTA;
				PdfPCell saleTDR;
				PdfPCell saleGST;
				PdfPCell saleSUF;
				PdfPCell saleNA;

				if (cc) {
					saleTT = new PdfPCell(new Phrase("Credit Card", subCatFont));
					saleTT.setFixedHeight(17f);
					saleTT.setBorderWidth(0f);
					saleTT.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTT.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTT.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTT));

					saleTA = new PdfPCell(new Phrase(ccTotalAmount.toString(), subCatFont));
					saleTA.setFixedHeight(17f);
					saleTA.setBorderWidth(0f);
					saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTA));

					saleTDR = new PdfPCell(new Phrase(ccTdr.toString(), subCatFont));
					saleTDR.setFixedHeight(17f);
					saleTDR.setBorderWidth(0f);
					saleTDR.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTDR.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTDR));

					saleGST = new PdfPCell(new Phrase(ccGst.toString(), subCatFont));
					saleGST.setFixedHeight(17f);
					saleGST.setBorderWidth(0f);
					saleGST.setPaddingTop(Element.ALIGN_MIDDLE);
					saleGST.setVerticalAlignment(Element.ALIGN_CENTER);
					saleGST.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleGST));

					saleSUF = new PdfPCell(new Phrase(ccSuf.toString(), subCatFont));
					saleSUF.setFixedHeight(17f);
					saleSUF.setBorderWidth(0f);
					saleSUF.setPaddingTop(Element.ALIGN_MIDDLE);
					saleSUF.setVerticalAlignment(Element.ALIGN_CENTER);
					saleSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleSUF));

					saleNA = new PdfPCell(new Phrase(
							ccTotalAmount.subtract(ccTdr).subtract(ccGst).subtract(ccSuf).toString(), subCatFont));
					saleNA.setFixedHeight(17f);
					saleNA.setBorderWidth(0f);
					saleNA.setPaddingTop(Element.ALIGN_MIDDLE);
					saleNA.setVerticalAlignment(Element.ALIGN_CENTER);
					saleNA.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleNA));
				}

				if (dc) {
					saleTT = new PdfPCell(new Phrase("Debit Card", subCatFont));
					saleTT.setFixedHeight(17f);
					saleTT.setBorderWidth(0f);
					saleTT.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTT.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTT.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTT));

					saleTA = new PdfPCell(new Phrase(dcTotalAmount.toString(), subCatFont));
					saleTA.setFixedHeight(17f);
					saleTA.setBorderWidth(0f);
					saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTA));

					saleTDR = new PdfPCell(new Phrase(dcTdr.toString(), subCatFont));
					saleTDR.setFixedHeight(17f);
					saleTDR.setBorderWidth(0f);
					saleTDR.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTDR.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTDR));

					saleGST = new PdfPCell(new Phrase(dcGst.toString(), subCatFont));
					saleGST.setFixedHeight(17f);
					saleGST.setBorderWidth(0f);
					saleGST.setPaddingTop(Element.ALIGN_MIDDLE);
					saleGST.setVerticalAlignment(Element.ALIGN_CENTER);
					saleGST.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleGST));

					saleSUF = new PdfPCell(new Phrase(dcSuf.toString(), subCatFont));
					saleSUF.setFixedHeight(17f);
					saleSUF.setBorderWidth(0f);
					saleSUF.setPaddingTop(Element.ALIGN_MIDDLE);
					saleSUF.setVerticalAlignment(Element.ALIGN_CENTER);
					saleSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleSUF));

					saleTA = new PdfPCell(new Phrase(
							dcTotalAmount.subtract(dcTdr).subtract(dcGst).subtract(dcSuf).toString(), subCatFont));
					saleTA.setFixedHeight(17f);
					saleTA.setBorderWidth(0f);
					saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTA));
				}
				if (in) {
					saleTT = new PdfPCell(new Phrase("International", subCatFont));
					saleTT.setFixedHeight(17f);
					saleTT.setBorderWidth(0f);
					saleTT.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTT.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTT.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTT));

					saleTA = new PdfPCell(new Phrase(inTotalAmount.toString(), subCatFont));
					saleTA.setFixedHeight(17f);
					saleTA.setBorderWidth(0f);
					saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTA));

					saleTDR = new PdfPCell(new Phrase(inTdr.toString(), subCatFont));
					saleTDR.setFixedHeight(17f);
					saleTDR.setBorderWidth(0f);
					saleTDR.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTDR.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTDR));

					saleGST = new PdfPCell(new Phrase(inGst.toString(), subCatFont));
					saleGST.setFixedHeight(17f);
					saleGST.setBorderWidth(0f);
					saleGST.setPaddingTop(Element.ALIGN_MIDDLE);
					saleGST.setVerticalAlignment(Element.ALIGN_CENTER);
					saleGST.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleGST));

					saleSUF = new PdfPCell(new Phrase(inSuf.toString(), subCatFont));
					saleSUF.setFixedHeight(17f);
					saleSUF.setBorderWidth(0f);
					saleSUF.setPaddingTop(Element.ALIGN_MIDDLE);
					saleSUF.setVerticalAlignment(Element.ALIGN_CENTER);
					saleSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleSUF));

					saleTA = new PdfPCell(new Phrase(
							inTotalAmount.subtract(inTdr).subtract(inGst).subtract(inSuf).toString(), subCatFont));
					saleTA.setFixedHeight(17f);
					saleTA.setBorderWidth(0f);
					saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTA));
				}
				if (nb) {
					saleTT = new PdfPCell(new Phrase("Net Banking", subCatFont));
					saleTT.setFixedHeight(17f);
					saleTT.setBorderWidth(0f);
					saleTT.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTT.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTT.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTT));

					saleTA = new PdfPCell(new Phrase(nbTotalAmount.toString(), subCatFont));
					saleTA.setFixedHeight(17f);
					saleTA.setBorderWidth(0f);
					saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTA));

					saleTDR = new PdfPCell(new Phrase(nbTdr.toString(), subCatFont));
					saleTDR.setFixedHeight(17f);
					saleTDR.setBorderWidth(0f);
					saleTDR.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTDR.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTDR));

					saleGST = new PdfPCell(new Phrase(nbGst.toString(), subCatFont));
					saleGST.setFixedHeight(17f);
					saleGST.setBorderWidth(0f);
					saleGST.setPaddingTop(Element.ALIGN_MIDDLE);
					saleGST.setVerticalAlignment(Element.ALIGN_CENTER);
					saleGST.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleGST));

					saleSUF = new PdfPCell(new Phrase(nbSuf.toString(), subCatFont));
					saleSUF.setFixedHeight(17f);
					saleSUF.setBorderWidth(0f);
					saleSUF.setPaddingTop(Element.ALIGN_MIDDLE);
					saleSUF.setVerticalAlignment(Element.ALIGN_CENTER);
					saleSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleSUF));

					saleTA = new PdfPCell(new Phrase(
							nbTotalAmount.subtract(nbTdr).subtract(nbGst).subtract(nbSuf).toString(), subCatFont));
					saleTA.setFixedHeight(17f);
					saleTA.setBorderWidth(0f);
					saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTA));
				}
				if (up) {
					saleTT = new PdfPCell(new Phrase("UPI", subCatFont));
					saleTT.setFixedHeight(17f);
					saleTT.setBorderWidth(0f);
					saleTT.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTT.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTT.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTT));

					saleTA = new PdfPCell(new Phrase(upTotalAmount.toString(), subCatFont));
					saleTA.setFixedHeight(17f);
					saleTA.setBorderWidth(0f);
					saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTA));

					saleTDR = new PdfPCell(new Phrase(upTdr.toString(), subCatFont));
					saleTDR.setFixedHeight(17f);
					saleTDR.setBorderWidth(0f);
					saleTDR.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTDR.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTDR));

					saleGST = new PdfPCell(new Phrase(upGst.toString(), subCatFont));
					saleGST.setFixedHeight(17f);
					saleGST.setBorderWidth(0f);
					saleGST.setPaddingTop(Element.ALIGN_MIDDLE);
					saleGST.setVerticalAlignment(Element.ALIGN_CENTER);
					saleGST.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleGST));

					saleSUF = new PdfPCell(new Phrase(upSuf.toString(), subCatFont));
					saleSUF.setFixedHeight(17f);
					saleSUF.setBorderWidth(0f);
					saleSUF.setPaddingTop(Element.ALIGN_MIDDLE);
					saleSUF.setVerticalAlignment(Element.ALIGN_CENTER);
					saleSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleSUF));

					saleTA = new PdfPCell(new Phrase(
							upTotalAmount.subtract(upTdr).subtract(upGst).subtract(upSuf).toString(), subCatFont));
					saleTA.setFixedHeight(17f);
					saleTA.setBorderWidth(0f);
					saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTA));
				}
				if (wl) {
					saleTT = new PdfPCell(new Phrase("Wallet", subCatFont));
					saleTT.setFixedHeight(17f);
					saleTT.setBorderWidth(0f);
					saleTT.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTT.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTT.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTT));

					saleTA = new PdfPCell(new Phrase(wlTotalAmount.toString(), subCatFont));
					saleTA.setFixedHeight(17f);
					saleTA.setBorderWidth(0f);
					saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTA));

					saleTDR = new PdfPCell(new Phrase(wlTdr.toString(), subCatFont));
					saleTDR.setFixedHeight(17f);
					saleTDR.setBorderWidth(0f);
					saleTDR.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTDR.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTDR));

					saleGST = new PdfPCell(new Phrase(wlGst.toString(), subCatFont));
					saleGST.setFixedHeight(17f);
					saleGST.setBorderWidth(0f);
					saleGST.setPaddingTop(Element.ALIGN_MIDDLE);
					saleGST.setVerticalAlignment(Element.ALIGN_CENTER);
					saleGST.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleGST));

					saleSUF = new PdfPCell(new Phrase(wlSuf.toString(), subCatFont));
					saleSUF.setFixedHeight(17f);
					saleSUF.setBorderWidth(0f);
					saleSUF.setPaddingTop(Element.ALIGN_MIDDLE);
					saleSUF.setVerticalAlignment(Element.ALIGN_CENTER);
					saleSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleSUF));

					saleTA = new PdfPCell(new Phrase(
							wlTotalAmount.subtract(wlTdr).subtract(wlGst).subtract(wlSuf).toString(), subCatFont));
					saleTA.setFixedHeight(17f);
					saleTA.setBorderWidth(0f);
					saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTA));
				}
				if (imps) {
					saleTT = new PdfPCell(new Phrase("IMPS", subCatFont));
					saleTT.setFixedHeight(17f);
					saleTT.setBorderWidth(0f);
					saleTT.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTT.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTT.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTT));

					saleTA = new PdfPCell(new Phrase(impsTotalAmount.toString(), subCatFont));
					saleTA.setFixedHeight(17f);
					saleTA.setBorderWidth(0f);
					saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTA));

					saleTDR = new PdfPCell(new Phrase(impsTdr.toString(), subCatFont));
					saleTDR.setFixedHeight(17f);
					saleTDR.setBorderWidth(0f);
					saleTDR.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTDR.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTDR));

					saleGST = new PdfPCell(new Phrase(impsGst.toString(), subCatFont));
					saleGST.setFixedHeight(17f);
					saleGST.setBorderWidth(0f);
					saleGST.setPaddingTop(Element.ALIGN_MIDDLE);
					saleGST.setVerticalAlignment(Element.ALIGN_CENTER);
					saleGST.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleGST));

					saleSUF = new PdfPCell(new Phrase(impsSuf.toString(), subCatFont));
					saleSUF.setFixedHeight(17f);
					saleSUF.setBorderWidth(0f);
					saleSUF.setPaddingTop(Element.ALIGN_MIDDLE);
					saleSUF.setVerticalAlignment(Element.ALIGN_CENTER);
					saleSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleSUF));

					saleTA = new PdfPCell(
							new Phrase(impsTotalAmount.subtract(impsTdr).subtract(impsGst).subtract(impsSuf).toString(),
									subCatFont));
					saleTA.setFixedHeight(17f);
					saleTA.setBorderWidth(0f);
					saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTA));
				}
				if (neft) {
					saleTT = new PdfPCell(new Phrase("NEFT", subCatFont));
					saleTT.setFixedHeight(17f);
					saleTT.setBorderWidth(0f);
					saleTT.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTT.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTT.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTT));

					saleTA = new PdfPCell(new Phrase(neftTotalAmount.toString(), subCatFont));
					saleTA.setFixedHeight(17f);
					saleTA.setBorderWidth(0f);
					saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTA));

					saleTDR = new PdfPCell(new Phrase(neftTdr.toString(), subCatFont));
					saleTDR.setFixedHeight(17f);
					saleTDR.setBorderWidth(0f);
					saleTDR.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTDR.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTDR));

					saleGST = new PdfPCell(new Phrase(neftGst.toString(), subCatFont));
					saleGST.setFixedHeight(17f);
					saleGST.setBorderWidth(0f);
					saleGST.setPaddingTop(Element.ALIGN_MIDDLE);
					saleGST.setVerticalAlignment(Element.ALIGN_CENTER);
					saleGST.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleGST));

					saleSUF = new PdfPCell(new Phrase(neftSuf.toString(), subCatFont));
					saleSUF.setFixedHeight(17f);
					saleSUF.setBorderWidth(0f);
					saleSUF.setPaddingTop(Element.ALIGN_MIDDLE);
					saleSUF.setVerticalAlignment(Element.ALIGN_CENTER);
					saleSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleSUF));

					saleTA = new PdfPCell(
							new Phrase(neftTotalAmount.subtract(neftTdr).subtract(neftGst).subtract(neftSuf).toString(),
									subCatFont));
					saleTA.setFixedHeight(17f);
					saleTA.setBorderWidth(0f);
					saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTA));
				}
				if (rtgs) {
					saleTT = new PdfPCell(new Phrase("RTGS", subCatFont));
					saleTT.setFixedHeight(17f);
					saleTT.setBorderWidth(0f);
					saleTT.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTT.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTT.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTT));

					saleTA = new PdfPCell(new Phrase(rtgsTotalAmount.toString(), subCatFont));
					saleTA.setFixedHeight(17f);
					saleTA.setBorderWidth(0f);
					saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTA));

					saleTDR = new PdfPCell(new Phrase(rtgsTdr.toString(), subCatFont));
					saleTDR.setFixedHeight(17f);
					saleTDR.setBorderWidth(0f);
					saleTDR.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTDR.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTDR));

					saleGST = new PdfPCell(new Phrase(rtgsGst.toString(), subCatFont));
					saleGST.setFixedHeight(17f);
					saleGST.setBorderWidth(0f);
					saleGST.setPaddingTop(Element.ALIGN_MIDDLE);
					saleGST.setVerticalAlignment(Element.ALIGN_CENTER);
					saleGST.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleGST));

					saleSUF = new PdfPCell(new Phrase(rtgsSuf.toString(), subCatFont));
					saleSUF.setFixedHeight(17f);
					saleSUF.setBorderWidth(0f);
					saleSUF.setPaddingTop(Element.ALIGN_MIDDLE);
					saleSUF.setVerticalAlignment(Element.ALIGN_CENTER);
					saleSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleSUF));

					saleTA = new PdfPCell(
							new Phrase(rtgsTotalAmount.subtract(rtgsTdr).subtract(rtgsGst).subtract(rtgsSuf).toString(),
									subCatFont));
					saleTA.setFixedHeight(17f);
					saleTA.setBorderWidth(0f);
					saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTA));
				}
				if (cod) {
					saleTT = new PdfPCell(new Phrase("COD", subCatFont));
					saleTT.setFixedHeight(17f);
					saleTT.setBorderWidth(0f);
					saleTT.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTT.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTT.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTT));

					saleTA = new PdfPCell(new Phrase(codTotalAmount.toString(), subCatFont));
					saleTA.setFixedHeight(17f);
					saleTA.setBorderWidth(0f);
					saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTA));

					saleTDR = new PdfPCell(new Phrase(codTdr.toString(), subCatFont));
					saleTDR.setFixedHeight(17f);
					saleTDR.setBorderWidth(0f);
					saleTDR.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTDR.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTDR));

					saleGST = new PdfPCell(new Phrase(codGst.toString(), subCatFont));
					saleGST.setFixedHeight(17f);
					saleGST.setBorderWidth(0f);
					saleGST.setPaddingTop(Element.ALIGN_MIDDLE);
					saleGST.setVerticalAlignment(Element.ALIGN_CENTER);
					saleGST.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleGST));

					saleSUF = new PdfPCell(new Phrase(codSuf.toString(), subCatFont));
					saleSUF.setFixedHeight(17f);
					saleSUF.setBorderWidth(0f);
					saleSUF.setPaddingTop(Element.ALIGN_MIDDLE);
					saleSUF.setVerticalAlignment(Element.ALIGN_CENTER);
					saleSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleSUF));

					saleTA = new PdfPCell(new Phrase(
							new BigDecimal("0.00").subtract(codTdr).subtract(codGst).subtract(codSuf).toString(),
							subCatFont));
					saleTA.setFixedHeight(17f);
					saleTA.setBorderWidth(0f);
					saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTA));
				}

				if (!cc && !dc && !in && !nb && !up && !cod && !neft && !rtgs && !imps && !wl) {

					saleTDR = new PdfPCell(new Phrase("NO DATA FOUND FOR SALE SETTLED", subCatFont));
					saleTDR.setFixedHeight(20f);
					saleTDR.setColspan(6);
					saleTDR.setBorderWidth(0f);
					saleTDR.setPaddingTop(Element.ALIGN_MIDDLE);
					saleTDR.setVerticalAlignment(Element.ALIGN_CENTER);
					saleTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
					saleTable.addCell(new PdfPCell(saleTDR));

				}
				saleTT = new PdfPCell(new Phrase("Total", totalFont));
				BaseColor myColorpan = WebColors.getRGBColor("#002664");
				saleTT.setBackgroundColor(myColorpan);
				saleTT.setFixedHeight(20f);
				saleTT.setBorderWidth(0f);
				saleTT.setBorderWidthTop(1f);
				saleTT.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTT.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTT.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTT));

				saleTA = new PdfPCell(new Phrase(totalAmount.toString(), subCatFont));
				saleTA.setFixedHeight(20f);
				saleTA.setBorderWidth(0f);
				saleTA.setBorderWidthTop(1f);
				saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTA));

				saleTDR = new PdfPCell(new Phrase(totalTdr.toString(), subCatFont));
				saleTDR.setFixedHeight(20f);
				saleTDR.setBorderWidth(0f);
				saleTDR.setBorderWidthTop(1f);
				saleTDR.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTDR.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTDR));

				saleGST = new PdfPCell(new Phrase(totalGst.toString(), subCatFont));
				saleGST.setFixedHeight(20f);
				saleGST.setBorderWidth(0f);
				saleGST.setBorderWidthTop(1f);
				saleGST.setPaddingTop(Element.ALIGN_MIDDLE);
				saleGST.setVerticalAlignment(Element.ALIGN_CENTER);
				saleGST.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleGST));

				saleSUF = new PdfPCell(new Phrase(totalSuf.toString(), subCatFont));
				saleSUF.setFixedHeight(20f);
				saleSUF.setBorderWidth(0f);
				saleSUF.setBorderWidthTop(1f);
				saleSUF.setPaddingTop(Element.ALIGN_MIDDLE);
				saleSUF.setVerticalAlignment(Element.ALIGN_CENTER);
				saleSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleSUF));

				saleAmount = totalAmount.subtract(totalTdr).subtract(totalGst).subtract(totalSuf)
						.subtract(codTotalAmount);

				saleTA = new PdfPCell(new Phrase(saleAmount.toString(), subCatFont));
				saleTA.setFixedHeight(20f);
				saleTA.setBorderWidth(0f);
				saleTA.setBorderWidthTop(1f);
				saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTA));

				document.add(saleTable);
				//logger.info("outside refund table");

				float[] refund = new float[] { 82.3f, 8.7f };
				PdfPTable refundsettle = new PdfPTable(2);
				refundsettle.setWidths(refund);
				refundsettle.getDefaultCell().setBorderWidth(0f);
				refundsettle.setWidthPercentage(90f);

				reporttypeParagraph = new Paragraph(Chunk.NEWLINE + "REFUND SETTLED ", tableHeaderFont);
				reporttypeParagraph.setAlignment(Element.ALIGN_LEFT);
				refundsettle.addCell(reporttypeParagraph);
				if (refundcc || refunddc) {
					reporttypeParagraph = new Paragraph(Chunk.NEWLINE + "(In " + currency + ")", tableHeaderFont);
				} else {
					reporttypeParagraph = new Paragraph(Chunk.NEWLINE + "(In INR) ", tableHeaderFont);
				}
				reporttypeParagraph.setAlignment(Element.ALIGN_RIGHT);
				refundsettle.addCell(reporttypeParagraph);

				document.add(refundsettle);

				float[] refundColumnWidths = { 19F, 17F, 10F, 17F, 10F, 17F };

				PdfPTable refundTable = new PdfPTable(6);
				refundTable.setWidths(refundColumnWidths);
				refundTable.setWidthPercentage(90f);

				PdfPCell refundpayemntType = new PdfPCell(new Phrase("Payment Type", catFont));
				refundpayemntType.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundpayemntType.setVerticalAlignment(Element.ALIGN_CENTER);
				refundpayemntType.setFixedHeight(17f);
				refundpayemntType.setBorderWidth(0f);
				refundpayemntType.setBorderWidthBottom(1f);
				refundTable.addCell(refundpayemntType);

				PdfPCell refundTotalAmount = new PdfPCell(new Phrase("Total Amount", catFont));
				refundTotalAmount.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTotalAmount.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTotalAmount.setFixedHeight(17f);
				refundTotalAmount.setBorderWidth(0f);
				refundTotalAmount.setBorderWidthBottom(1f);
				refundTable.addCell(refundTotalAmount);

				PdfPCell refundtdr = new PdfPCell(new Phrase("TDR", catFont));
				refundtdr.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundtdr.setVerticalAlignment(Element.ALIGN_CENTER);
				refundtdr.setFixedHeight(17f);
				refundtdr.setBorderWidth(0f);
				refundtdr.setBorderWidthBottom(1f);
				refundTable.addCell(refundtdr);

				PdfPCell refundgst = new PdfPCell(new Phrase("GST @18 %", catFont));
				refundgst.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundgst.setVerticalAlignment(Element.ALIGN_CENTER);
				refundgst.setFixedHeight(17f);
				refundgst.setBorderWidth(0f);
				refundgst.setBorderWidthBottom(1f);
				refundTable.addCell(refundgst);

				PdfPCell refundsuf = new PdfPCell(new Phrase("SUF", catFont));
				refundsuf.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundsuf.setVerticalAlignment(Element.ALIGN_CENTER);
				refundsuf.setFixedHeight(17f);
				refundsuf.setBorderWidth(0f);
				refundsuf.setBorderWidthBottom(1f);
				refundTable.addCell(refundsuf);

				PdfPCell refundnetAmount = new PdfPCell(new Phrase("Net Amount", catFont));
				refundnetAmount.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundnetAmount.setVerticalAlignment(Element.ALIGN_CENTER);
				refundnetAmount.setFixedHeight(17f);
				refundnetAmount.setBorderWidth(0f);
				refundnetAmount.setBorderWidthBottom(1f);
				refundTable.addCell(refundnetAmount);

				document.add(new Chunk(ls));

				PdfPCell refundTT;
				PdfPCell refundTA;
				PdfPCell refundTDR;
				PdfPCell refundGST;
				PdfPCell refundSUF;
				PdfPCell refundNA;
				//logger.info("inside refund table");

				if (refundcc) {
					refundTT = new PdfPCell(new Phrase("Credit Card", subCatFont));
					refundTT.setFixedHeight(17f);
					refundTT.setBorderWidth(0f);
					refundTT.setPaddingTop(Element.ALIGN_MIDDLE);
					refundTT.setVerticalAlignment(Element.ALIGN_CENTER);
					refundTT.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundTT));

					refundTA = new PdfPCell(new Phrase(refundccTotalAmount.toString(), subCatFont));
					refundTA.setFixedHeight(17f);
					refundTA.setBorderWidth(0f);
					refundTA.setPaddingTop(Element.ALIGN_MIDDLE);
					refundTA.setVerticalAlignment(Element.ALIGN_CENTER);
					refundTA.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundTA));

					refundTDR = new PdfPCell(new Phrase(refundccTdr.toString(), subCatFont));
					refundTDR.setFixedHeight(17f);
					refundTDR.setBorderWidth(0f);
					refundTDR.setPaddingTop(Element.ALIGN_MIDDLE);
					refundTDR.setVerticalAlignment(Element.ALIGN_CENTER);
					refundTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundTDR));

					refundGST = new PdfPCell(new Phrase(refundccGst.toString(), subCatFont));
					refundGST.setFixedHeight(17f);
					refundGST.setBorderWidth(0f);
					refundGST.setPaddingTop(Element.ALIGN_MIDDLE);
					refundGST.setVerticalAlignment(Element.ALIGN_CENTER);
					refundGST.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundGST));

					refundSUF = new PdfPCell(new Phrase(refundccSuf.toString(), subCatFont));
					refundSUF.setFixedHeight(17f);
					refundSUF.setBorderWidth(0f);
					refundSUF.setPaddingTop(Element.ALIGN_MIDDLE);
					refundSUF.setVerticalAlignment(Element.ALIGN_CENTER);
					refundSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundSUF));

					refundNA = new PdfPCell(new Phrase(refundccTotalAmount.subtract(refundccTdr).subtract(refundccGst)
							.subtract(refundccSuf).toString(), subCatFont));
					refundNA.setFixedHeight(17f);
					refundNA.setBorderWidth(0f);
					refundNA.setPaddingTop(Element.ALIGN_MIDDLE);
					refundNA.setVerticalAlignment(Element.ALIGN_CENTER);
					refundNA.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundNA));
				}
				if (refunddc) {
					refundTT = new PdfPCell(new Phrase("Debit Card", subCatFont));
					refundTT.setFixedHeight(17f);
					refundTT.setBorderWidth(0f);
					refundTT.setPaddingTop(Element.ALIGN_MIDDLE);
					refundTT.setVerticalAlignment(Element.ALIGN_CENTER);
					refundTT.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundTT));

					refundTA = new PdfPCell(new Phrase(refunddcTotalAmount.toString(), subCatFont));
					refundTA.setFixedHeight(17f);
					refundTA.setBorderWidth(0f);
					refundTA.setPaddingTop(Element.ALIGN_MIDDLE);
					refundTA.setVerticalAlignment(Element.ALIGN_CENTER);
					refundTA.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundTA));

					refundTDR = new PdfPCell(new Phrase(refunddcTdr.toString(), subCatFont));
					refundTDR.setFixedHeight(17f);
					refundTDR.setBorderWidth(0f);
					refundTDR.setPaddingTop(Element.ALIGN_MIDDLE);
					refundTDR.setVerticalAlignment(Element.ALIGN_CENTER);
					refundTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundTDR));

					refundGST = new PdfPCell(new Phrase(refunddcGst.toString(), subCatFont));
					refundGST.setFixedHeight(17f);
					refundGST.setBorderWidth(0f);
					refundGST.setPaddingTop(Element.ALIGN_MIDDLE);
					refundGST.setVerticalAlignment(Element.ALIGN_CENTER);
					refundGST.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundGST));

					refundSUF = new PdfPCell(new Phrase(refunddcSuf.toString(), subCatFont));
					refundSUF.setFixedHeight(17f);
					refundSUF.setBorderWidth(0f);
					refundSUF.setPaddingTop(Element.ALIGN_MIDDLE);
					refundSUF.setVerticalAlignment(Element.ALIGN_CENTER);
					refundSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundSUF));

					refundNA = new PdfPCell(new Phrase(refunddcTotalAmount.subtract(refunddcTdr).subtract(refunddcGst)
							.subtract(refunddcSuf).toString(), subCatFont));
					refundNA.setFixedHeight(17f);
					refundNA.setBorderWidth(0f);
					refundNA.setPaddingTop(Element.ALIGN_MIDDLE);
					refundNA.setVerticalAlignment(Element.ALIGN_CENTER);
					refundNA.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundNA));
				}
				if (refundin) {
					refundTT = new PdfPCell(new Phrase("International", subCatFont));
					refundTT.setFixedHeight(17f);
					refundTT.setBorderWidth(0f);
					refundTT.setPaddingTop(Element.ALIGN_MIDDLE);
					refundTT.setVerticalAlignment(Element.ALIGN_CENTER);
					refundTT.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundTT));

					refundTA = new PdfPCell(new Phrase(refundinTotalAmount.toString(), subCatFont));
					refundTA.setFixedHeight(17f);
					refundTA.setBorderWidth(0f);
					refundTA.setPaddingTop(Element.ALIGN_MIDDLE);
					refundTA.setVerticalAlignment(Element.ALIGN_CENTER);
					refundTA.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundTA));

					refundTDR = new PdfPCell(new Phrase(refundinTdr.toString(), subCatFont));
					refundTDR.setFixedHeight(17f);
					refundTDR.setBorderWidth(0f);
					refundTDR.setPaddingTop(Element.ALIGN_MIDDLE);
					refundTDR.setVerticalAlignment(Element.ALIGN_CENTER);
					refundTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundTDR));

					refundGST = new PdfPCell(new Phrase(refundinGst.toString(), subCatFont));
					refundGST.setFixedHeight(17f);
					refundGST.setBorderWidth(0f);
					refundGST.setPaddingTop(Element.ALIGN_MIDDLE);
					refundGST.setVerticalAlignment(Element.ALIGN_CENTER);
					refundGST.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundGST));

					refundSUF = new PdfPCell(new Phrase(refundinSuf.toString(), subCatFont));
					refundSUF.setFixedHeight(17f);
					refundSUF.setBorderWidth(0f);
					refundSUF.setPaddingTop(Element.ALIGN_MIDDLE);
					refundSUF.setVerticalAlignment(Element.ALIGN_CENTER);
					refundSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundSUF));

					refundNA = new PdfPCell(new Phrase(refundinTotalAmount.subtract(refundinTdr).subtract(refundinGst)
							.subtract(refundinSuf).toString(), subCatFont));
					refundNA.setFixedHeight(17f);
					refundNA.setBorderWidth(0f);
					refundNA.setPaddingTop(Element.ALIGN_MIDDLE);
					refundNA.setVerticalAlignment(Element.ALIGN_CENTER);
					refundNA.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundNA));
				}
				if (refundnb) {
					refundTT = new PdfPCell(new Phrase("Net Banking", subCatFont));
					refundTT.setFixedHeight(17f);
					refundTT.setBorderWidth(0f);
					refundTT.setPaddingTop(Element.ALIGN_MIDDLE);
					refundTT.setVerticalAlignment(Element.ALIGN_CENTER);
					refundTT.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundTT));

					refundTA = new PdfPCell(new Phrase(refundnbTotalAmount.toString(), subCatFont));
					refundTA.setFixedHeight(17f);
					refundTA.setBorderWidth(0f);
					refundTA.setPaddingTop(Element.ALIGN_MIDDLE);
					refundTA.setVerticalAlignment(Element.ALIGN_CENTER);
					refundTA.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundTA));

					refundTDR = new PdfPCell(new Phrase(refundnbTdr.toString(), subCatFont));
					refundTDR.setFixedHeight(17f);
					refundTDR.setBorderWidth(0f);
					refundTDR.setPaddingTop(Element.ALIGN_MIDDLE);
					refundTDR.setVerticalAlignment(Element.ALIGN_CENTER);
					refundTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundTDR));

					refundGST = new PdfPCell(new Phrase(refundnbGst.toString(), subCatFont));
					refundGST.setFixedHeight(17f);
					refundGST.setBorderWidth(0f);
					refundGST.setPaddingTop(Element.ALIGN_MIDDLE);
					refundGST.setVerticalAlignment(Element.ALIGN_CENTER);
					refundGST.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundGST));

					refundSUF = new PdfPCell(new Phrase(refundnbSuf.toString(), subCatFont));
					refundSUF.setFixedHeight(17f);
					refundSUF.setBorderWidth(0f);
					refundSUF.setPaddingTop(Element.ALIGN_MIDDLE);
					refundSUF.setVerticalAlignment(Element.ALIGN_CENTER);
					refundSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundSUF));

					refundNA = new PdfPCell(new Phrase(refundnbTotalAmount.subtract(refundnbTdr).subtract(refundnbGst)
							.subtract(refundnbSuf).toString(), subCatFont));
					refundNA.setFixedHeight(17f);
					refundNA.setBorderWidth(0f);
					refundNA.setPaddingTop(Element.ALIGN_MIDDLE);
					refundNA.setVerticalAlignment(Element.ALIGN_CENTER);
					refundNA.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundNA));
				}
				if (refundup) {
					refundTT = new PdfPCell(new Phrase("UPI", subCatFont));
					refundTT.setFixedHeight(17f);
					refundTT.setBorderWidth(0f);
					refundTT.setPaddingTop(Element.ALIGN_MIDDLE);
					refundTT.setVerticalAlignment(Element.ALIGN_CENTER);
					refundTT.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundTT));

					refundTA = new PdfPCell(new Phrase(refundupTotalAmount.toString(), subCatFont));
					refundTA.setFixedHeight(17f);
					refundTA.setBorderWidth(0f);
					refundTA.setPaddingTop(Element.ALIGN_MIDDLE);
					refundTA.setVerticalAlignment(Element.ALIGN_CENTER);
					refundTA.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundTA));

					refundTDR = new PdfPCell(new Phrase(refundupTdr.toString(), subCatFont));
					refundTDR.setFixedHeight(17f);
					refundTDR.setBorderWidth(0f);
					refundTDR.setPaddingTop(Element.ALIGN_MIDDLE);
					refundTDR.setVerticalAlignment(Element.ALIGN_CENTER);
					refundTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundTDR));

					refundGST = new PdfPCell(new Phrase(refundupGst.toString(), subCatFont));
					refundGST.setFixedHeight(17f);
					refundGST.setBorderWidth(0f);
					refundGST.setPaddingTop(Element.ALIGN_MIDDLE);
					refundGST.setVerticalAlignment(Element.ALIGN_CENTER);
					refundGST.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundGST));

					refundSUF = new PdfPCell(new Phrase(refundupSuf.toString(), subCatFont));
					refundSUF.setFixedHeight(17f);
					refundSUF.setBorderWidth(0f);
					refundSUF.setPaddingTop(Element.ALIGN_MIDDLE);
					refundSUF.setVerticalAlignment(Element.ALIGN_CENTER);
					refundSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundSUF));

					refundNA = new PdfPCell(new Phrase(refundupTotalAmount.subtract(refundupTdr).subtract(refundupGst)
							.subtract(refundupSuf).toString(), subCatFont));
					refundNA.setFixedHeight(17f);
					refundNA.setBorderWidth(0f);
					refundNA.setPaddingTop(Element.ALIGN_MIDDLE);
					refundNA.setVerticalAlignment(Element.ALIGN_CENTER);
					refundNA.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundNA));
				}
				if (refundwl) {
					refundTT = new PdfPCell(new Phrase("Wallet", subCatFont));
					refundTT.setFixedHeight(17f);
					refundTT.setBorderWidth(0f);
					refundTT.setPaddingTop(Element.ALIGN_MIDDLE);
					refundTT.setVerticalAlignment(Element.ALIGN_CENTER);
					refundTT.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundTT));

					refundTA = new PdfPCell(new Phrase(refundwlTotalAmount.toString(), subCatFont));
					refundTA.setFixedHeight(17f);
					refundTA.setBorderWidth(0f);
					refundTA.setPaddingTop(Element.ALIGN_MIDDLE);
					refundTA.setVerticalAlignment(Element.ALIGN_CENTER);
					refundTA.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundTA));

					refundTDR = new PdfPCell(new Phrase(refundwlTdr.toString(), subCatFont));
					refundTDR.setFixedHeight(17f);
					refundTDR.setBorderWidth(0f);
					refundTDR.setPaddingTop(Element.ALIGN_MIDDLE);
					refundTDR.setVerticalAlignment(Element.ALIGN_CENTER);
					refundTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundTDR));

					refundGST = new PdfPCell(new Phrase(refundwlGst.toString(), subCatFont));
					refundGST.setFixedHeight(17f);
					refundGST.setBorderWidth(0f);
					refundGST.setPaddingTop(Element.ALIGN_MIDDLE);
					refundGST.setVerticalAlignment(Element.ALIGN_CENTER);
					refundGST.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundGST));

					refundSUF = new PdfPCell(new Phrase(refundwlSuf.toString(), subCatFont));
					refundSUF.setFixedHeight(17f);
					refundSUF.setBorderWidth(0f);
					refundSUF.setPaddingTop(Element.ALIGN_MIDDLE);
					refundSUF.setVerticalAlignment(Element.ALIGN_CENTER);
					refundSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundSUF));

					refundNA = new PdfPCell(new Phrase(refundwlTotalAmount.subtract(refundwlTdr).subtract(refundwlGst)
							.subtract(refundwlSuf).toString(), subCatFont));
					refundNA.setFixedHeight(17f);
					refundNA.setBorderWidth(0f);
					refundNA.setPaddingTop(Element.ALIGN_MIDDLE);
					refundNA.setVerticalAlignment(Element.ALIGN_CENTER);
					refundNA.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundNA));
				}
				if (refundimps) {
					refundTT = new PdfPCell(new Phrase("IMPS", subCatFont));
					refundTT.setFixedHeight(17f);
					refundTT.setBorderWidth(0f);
					refundTT.setPaddingTop(Element.ALIGN_MIDDLE);
					refundTT.setVerticalAlignment(Element.ALIGN_CENTER);
					refundTT.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundTT));

					refundTA = new PdfPCell(new Phrase(refundimpsTotalAmount.toString(), subCatFont));
					refundTA.setFixedHeight(17f);
					refundTA.setBorderWidth(0f);
					refundTA.setPaddingTop(Element.ALIGN_MIDDLE);
					refundTA.setVerticalAlignment(Element.ALIGN_CENTER);
					refundTA.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundTA));

					refundTDR = new PdfPCell(new Phrase(refundimpsTdr.toString(), subCatFont));
					refundTDR.setFixedHeight(17f);
					refundTDR.setBorderWidth(0f);
					refundTDR.setPaddingTop(Element.ALIGN_MIDDLE);
					refundTDR.setVerticalAlignment(Element.ALIGN_CENTER);
					refundTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundTDR));

					refundGST = new PdfPCell(new Phrase(refundimpsGst.toString(), subCatFont));
					refundGST.setFixedHeight(17f);
					refundGST.setBorderWidth(0f);
					refundGST.setPaddingTop(Element.ALIGN_MIDDLE);
					refundGST.setVerticalAlignment(Element.ALIGN_CENTER);
					refundGST.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundGST));

					refundSUF = new PdfPCell(new Phrase(refundimpsSuf.toString(), subCatFont));
					refundSUF.setFixedHeight(17f);
					refundSUF.setBorderWidth(0f);
					refundSUF.setPaddingTop(Element.ALIGN_MIDDLE);
					refundSUF.setVerticalAlignment(Element.ALIGN_CENTER);
					refundSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundSUF));

					refundNA = new PdfPCell(new Phrase(refundimpsTotalAmount.subtract(refundimpsTdr)
							.subtract(refundimpsGst).subtract(refundimpsSuf).toString(), subCatFont));
					refundNA.setFixedHeight(17f);
					refundNA.setBorderWidth(0f);
					refundNA.setPaddingTop(Element.ALIGN_MIDDLE);
					refundNA.setVerticalAlignment(Element.ALIGN_CENTER);
					refundNA.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundNA));
				}
				if (refundneft) {
					refundTT = new PdfPCell(new Phrase("NEFT", subCatFont));
					refundTT.setFixedHeight(17f);
					refundTT.setBorderWidth(0f);
					refundTT.setPaddingTop(Element.ALIGN_MIDDLE);
					refundTT.setVerticalAlignment(Element.ALIGN_CENTER);
					refundTT.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundTT));

					refundTA = new PdfPCell(new Phrase(refundneftTotalAmount.toString(), subCatFont));
					refundTA.setFixedHeight(17f);
					refundTA.setBorderWidth(0f);
					refundTA.setPaddingTop(Element.ALIGN_MIDDLE);
					refundTA.setVerticalAlignment(Element.ALIGN_CENTER);
					refundTA.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundTA));

					refundTDR = new PdfPCell(new Phrase(refundneftTdr.toString(), subCatFont));
					refundTDR.setFixedHeight(17f);
					refundTDR.setBorderWidth(0f);
					refundTDR.setPaddingTop(Element.ALIGN_MIDDLE);
					refundTDR.setVerticalAlignment(Element.ALIGN_CENTER);
					refundTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundTDR));

					refundGST = new PdfPCell(new Phrase(refundneftGst.toString(), subCatFont));
					refundGST.setFixedHeight(17f);
					refundGST.setBorderWidth(0f);
					refundGST.setPaddingTop(Element.ALIGN_MIDDLE);
					refundGST.setVerticalAlignment(Element.ALIGN_CENTER);
					refundGST.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundGST));

					refundSUF = new PdfPCell(new Phrase(refundneftSuf.toString(), subCatFont));
					refundSUF.setFixedHeight(17f);
					refundSUF.setBorderWidth(0f);
					refundSUF.setPaddingTop(Element.ALIGN_MIDDLE);
					refundSUF.setVerticalAlignment(Element.ALIGN_CENTER);
					refundSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundSUF));

					refundNA = new PdfPCell(new Phrase(refundneftTotalAmount.subtract(refundneftTdr)
							.subtract(refundneftGst).subtract(refundneftSuf).toString(), subCatFont));
					refundNA.setFixedHeight(17f);
					refundNA.setBorderWidth(0f);
					refundNA.setPaddingTop(Element.ALIGN_MIDDLE);
					refundNA.setVerticalAlignment(Element.ALIGN_CENTER);
					refundNA.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundNA));

				}
				if (refundrtgs) {
					refundTT = new PdfPCell(new Phrase("RTGS", subCatFont));
					refundTT.setFixedHeight(17f);
					refundTT.setBorderWidth(0f);
					refundTT.setPaddingTop(Element.ALIGN_MIDDLE);
					refundTT.setVerticalAlignment(Element.ALIGN_CENTER);
					refundTT.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundTT));

					refundTA = new PdfPCell(new Phrase(refundrtgsTotalAmount.toString(), subCatFont));
					refundTA.setFixedHeight(17f);
					refundTA.setBorderWidth(0f);
					refundTA.setPaddingTop(Element.ALIGN_MIDDLE);
					refundTA.setVerticalAlignment(Element.ALIGN_CENTER);
					refundTA.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundTA));

					refundTDR = new PdfPCell(new Phrase(refundrtgsTdr.toString(), subCatFont));
					refundTDR.setFixedHeight(17f);
					refundTDR.setBorderWidth(0f);
					refundTDR.setPaddingTop(Element.ALIGN_MIDDLE);
					refundTDR.setVerticalAlignment(Element.ALIGN_CENTER);
					refundTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundTDR));

					refundGST = new PdfPCell(new Phrase(refundrtgsGst.toString(), subCatFont));
					refundGST.setFixedHeight(17f);
					refundGST.setBorderWidth(0f);
					refundGST.setPaddingTop(Element.ALIGN_MIDDLE);
					refundGST.setVerticalAlignment(Element.ALIGN_CENTER);
					refundGST.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundGST));

					refundSUF = new PdfPCell(new Phrase(refundrtgsSuf.toString(), subCatFont));
					refundSUF.setFixedHeight(17f);
					refundSUF.setBorderWidth(0f);
					refundSUF.setPaddingTop(Element.ALIGN_MIDDLE);
					refundSUF.setVerticalAlignment(Element.ALIGN_CENTER);
					refundSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundSUF));

					refundNA = new PdfPCell(new Phrase(refundrtgsTotalAmount.subtract(refundrtgsTdr)
							.subtract(refundrtgsGst).subtract(refundrtgsSuf).toString(), subCatFont));
					refundNA.setFixedHeight(17f);
					refundNA.setBorderWidth(0f);
					refundNA.setPaddingTop(Element.ALIGN_MIDDLE);
					refundNA.setVerticalAlignment(Element.ALIGN_CENTER);
					refundNA.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundNA));
				}
				if (refundcod) {
					refundTT = new PdfPCell(new Phrase("COD", subCatFont));
					refundTT.setFixedHeight(17f);
					refundTT.setBorderWidth(0f);
					refundTT.setPaddingTop(Element.ALIGN_MIDDLE);
					refundTT.setVerticalAlignment(Element.ALIGN_CENTER);
					refundTT.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundTT));

					refundTA = new PdfPCell(new Phrase(refundcodTotalAmount.toString(), subCatFont));
					refundTA.setFixedHeight(17f);
					refundTA.setBorderWidth(0f);
					refundTA.setPaddingTop(Element.ALIGN_MIDDLE);
					refundTA.setVerticalAlignment(Element.ALIGN_CENTER);
					refundTA.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundTA));

					refundTDR = new PdfPCell(new Phrase(refundcodTdr.toString(), subCatFont));
					refundTDR.setFixedHeight(17f);
					refundTDR.setBorderWidth(0f);
					refundTDR.setPaddingTop(Element.ALIGN_MIDDLE);
					refundTDR.setVerticalAlignment(Element.ALIGN_CENTER);
					refundTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundTDR));

					refundGST = new PdfPCell(new Phrase(refundcodGst.toString(), subCatFont));
					refundGST.setFixedHeight(17f);
					refundGST.setBorderWidth(0f);
					refundGST.setPaddingTop(Element.ALIGN_MIDDLE);
					refundGST.setVerticalAlignment(Element.ALIGN_CENTER);
					refundGST.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundGST));

					refundSUF = new PdfPCell(new Phrase(refundcodSuf.toString(), subCatFont));
					refundSUF.setFixedHeight(17f);
					refundSUF.setBorderWidth(0f);
					refundSUF.setPaddingTop(Element.ALIGN_MIDDLE);
					refundSUF.setVerticalAlignment(Element.ALIGN_CENTER);
					refundSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundSUF));

					refundNA = new PdfPCell(new Phrase(new BigDecimal("0.00").subtract(refundcodTdr)
							.subtract(refundcodGst).subtract(refundcodSuf).toString(), subCatFont));
					refundNA.setFixedHeight(17f);
					refundNA.setBorderWidth(0f);
					refundNA.setPaddingTop(Element.ALIGN_MIDDLE);
					refundNA.setVerticalAlignment(Element.ALIGN_CENTER);
					refundNA.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundNA));
				}
				if (!refundcc && !refunddc && !refundin && !refundnb && !refundup && !refundcod && !refundneft
						&& !refundrtgs && !refundimps && !refundwl) {

					refundTDR = new PdfPCell(new Phrase("NO DATA FOUND FOR REFUND SETTLED", subCatFont));
					refundTDR.setFixedHeight(20f);
					refundTDR.setColspan(6);
					refundTDR.setBorderWidth(0f);
					refundTDR.setPaddingTop(Element.ALIGN_MIDDLE);
					refundTDR.setVerticalAlignment(Element.ALIGN_CENTER);
					refundTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
					refundTable.addCell(new PdfPCell(refundTDR));

				}
				refundTT = new PdfPCell(new Phrase("Total", totalFont));
				BaseColor myColor = WebColors.getRGBColor("#002664");
				refundTT.setBackgroundColor(myColor);
				refundTT.setFixedHeight(20f);
				refundTT.setBorderWidth(0f);
				refundTT.setBorderWidthTop(1f);
				refundTT.setBorderWidthBottom(0.5f);
				refundTT.setBorderColorBottom(BaseColor.WHITE);
				refundTT.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTT.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTT.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTT));

				refundTA = new PdfPCell(new Phrase(refundtotalAmount.toString(), subCatFont));
				refundTA.setFixedHeight(20f);
				refundTA.setBorderWidth(0f);
				refundTA.setBorderWidthTop(1f);
				refundTA.setBorderWidthBottom(0.5f);
				refundTA.setBorderColorBottom(BaseColor.WHITE);
				refundTA.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTA.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTA));

				refundTDR = new PdfPCell(new Phrase(refundtotalTdr.toString(), subCatFont));
				refundTDR.setFixedHeight(20f);
				refundTDR.setBorderWidth(0f);
				refundTDR.setBorderWidthTop(1f);
				refundTDR.setBorderWidthBottom(0.5f);
				refundTDR.setBorderColorBottom(BaseColor.WHITE);
				refundTDR.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTDR.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTDR));

				refundGST = new PdfPCell(new Phrase(refundtotalGst.toString(), subCatFont));
				refundGST.setFixedHeight(20f);
				refundGST.setBorderWidth(0f);
				refundGST.setBorderWidthTop(1f);
				refundGST.setBorderWidthBottom(0.5f);
				refundGST.setBorderColorBottom(BaseColor.WHITE);
				refundGST.setPaddingTop(Element.ALIGN_MIDDLE);
				refundGST.setVerticalAlignment(Element.ALIGN_CENTER);
				refundGST.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundGST));

				refundSUF = new PdfPCell(new Phrase(refundtotalSuf.toString(), subCatFont));
				refundSUF.setFixedHeight(20f);
				refundSUF.setBorderWidth(0f);
				refundSUF.setBorderWidthTop(1f);
				refundSUF.setBorderWidthBottom(0.5f);
				refundSUF.setBorderColorBottom(BaseColor.WHITE);
				refundSUF.setPaddingTop(Element.ALIGN_MIDDLE);
				refundSUF.setVerticalAlignment(Element.ALIGN_CENTER);
				refundSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundSUF));

				refundAmount = refundtotalAmount.subtract(refundtotalTdr).subtract(refundtotalGst)
						.subtract(refundtotalSuf).subtract(refundcodTotalAmount);
				refundNA = new PdfPCell(new Phrase(refundAmount.toString(), subCatFont));
				refundNA.setFixedHeight(20f);
				refundNA.setBorderWidth(0f);
				refundNA.setBorderWidthTop(1f);
				refundNA.setBorderWidthBottom(0.5f);
				refundNA.setBorderColorBottom(BaseColor.WHITE);
				refundNA.setPaddingTop(Element.ALIGN_MIDDLE);
				refundNA.setVerticalAlignment(Element.ALIGN_CENTER);
				refundNA.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundNA));

				refundTT = new PdfPCell(new Phrase("Settlement Amount", totalFont));
				refundTT.setBackgroundColor(myColor);
				refundTT.setFixedHeight(20f);
				refundTT.setBorderWidth(0f);
				refundTT.setBorderWidthTop(0.5f);
				refundTT.setBorderWidthBottom(1f);
				refundTT.setBorderColorTop(BaseColor.WHITE);
				refundTT.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTT.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTT.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTT));

				refundTA = new PdfPCell(new Phrase("", subCatFont));
				refundTA.setBackgroundColor(myColor);
				refundTA.setFixedHeight(20f);
				refundTA.setBorderWidth(0f);
				refundTA.setBorderWidthTop(0.5f);
				refundTA.setBorderWidthBottom(1f);
				refundTA.setBorderColorTop(BaseColor.WHITE);
				refundTable.addCell(new PdfPCell(refundTA));

				refundTDR = new PdfPCell(new Phrase("", subCatFont));
				refundTDR.setBackgroundColor(myColor);
				refundTDR.setFixedHeight(20f);
				refundTDR.setBorderWidth(0f);
				refundTDR.setBorderWidthTop(0.5f);
				refundTDR.setBorderWidthBottom(1f);
				refundTDR.setBorderColorTop(BaseColor.WHITE);
				refundTable.addCell(new PdfPCell(refundTDR));

				refundGST = new PdfPCell(new Phrase("", subCatFont));
				refundGST.setBackgroundColor(myColor);
				refundGST.setFixedHeight(20f);
				refundGST.setBorderWidth(0f);
				refundGST.setBorderWidthTop(0.5f);
				refundGST.setBorderWidthBottom(1f);
				refundGST.setBorderColorTop(BaseColor.WHITE);
				refundTable.addCell(new PdfPCell(refundGST));

				refundSUF = new PdfPCell(new Phrase("", subCatFont));
				refundSUF.setBackgroundColor(myColor);
				refundSUF.setFixedHeight(20f);
				refundSUF.setBorderWidth(0f);
				refundSUF.setBorderWidthTop(0.5f);
				refundSUF.setBorderWidthBottom(1f);
				refundSUF.setBorderColorTop(BaseColor.WHITE);
				refundTable.addCell(new PdfPCell(refundSUF));

				refundNA = new PdfPCell(new Phrase(settlementAmount.toString(), totalFont));
				refundNA.setBackgroundColor(myColor);
				refundNA.setFixedHeight(20f);
				refundNA.setBorderWidth(0f);
				refundNA.setBorderWidthTop(0.5f);
				refundNA.setBorderWidthBottom(1f);
				refundNA.setBorderColorTop(BaseColor.WHITE);
				refundNA.setPaddingTop(Element.ALIGN_MIDDLE);
				refundNA.setVerticalAlignment(Element.ALIGN_CENTER);
				refundNA.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundNA));

				document.add(refundTable);
				//logger.info("outside footer settings");

				float[] footerWidths = { 50f, 50f };

				PdfPTable footerTable = new PdfPTable(2);
				footerTable.setWidths(footerWidths);
				footerTable.setWidthPercentage(100f);
				footerTable.getDefaultCell().setBorderWidth(0f);

				Anchor anchor = new Anchor("support@PaymentGateway.com");
				anchor.setReference("mailto:support@PaymentGateway.com");

				Chunk url = new Chunk("support@PaymentGateway.com");
				url.setFont(footerFont);
				Chunk msg = new Chunk("This is a computer generated copy");
				msg.setFont(headingtop);
				footerParagraph = new Paragraph();
				footerParagraph.add(Chunk.NEWLINE);
				footerParagraph.add(url);
				footerParagraph.add(Chunk.NEWLINE);
				footerParagraph.add(msg);
				footerParagraph.setAlignment(Element.ALIGN_LEFT);
				footerTable.addCell(footerParagraph);

				Image img2 = Image.getInstance(PropertiesManager.propertiesMap.get("footerLogo"));
				img2.scaleToFit(90f, 110f);
				PdfPCell cellLogo = new PdfPCell(img2);
				cellLogo.setPaddingTop(15f);
				cellLogo.setPaddingLeft(0f);
				cellLogo.setBorder(Rectangle.NO_BORDER);
				cellLogo.setFixedHeight(50f);
				footerTable.addCell(new PdfPCell(cellLogo)).setHorizontalAlignment(Element.ALIGN_RIGHT);

				document.add(footerTable);
				document.close();
				bytes = baos.toByteArray();

				//logger.info("creating excel file");
				ByteArrayOutputStream bos = null;
				byte[] excelFileAsBytes = null;

				
					SXSSFWorkbook wb = new SXSSFWorkbook(100);
					Row row;
					int rownum = 1;
					// Create a blank sheet
					Sheet sheet = wb.createSheet("Settlement Report");
					row = sheet.createRow(0);

					row.createCell(0).setCellValue("Sr No");
					row.createCell(1).setCellValue("Txn Id");
					row.createCell(2).setCellValue("Pg Ref Num");
					row.createCell(3).setCellValue("Merchant");
					row.createCell(4).setCellValue("Captured Date");
					row.createCell(5).setCellValue("Settled Date");
					row.createCell(6).setCellValue("Payout Date");
					row.createCell(7).setCellValue("UTR NO");
					row.createCell(8).setCellValue("Order Id");
					row.createCell(9).setCellValue("RRN");
					row.createCell(10).setCellValue("Payment Method");
					row.createCell(11).setCellValue("MopType");
					row.createCell(12).setCellValue("Mask");
					row.createCell(13).setCellValue("Cust Name");
					row.createCell(14).setCellValue("CardHolder Type");
					row.createCell(15).setCellValue("Txn Type");
					row.createCell(16).setCellValue("Transaction Mode");
					row.createCell(17).setCellValue("Status");
					row.createCell(18).setCellValue("Transaction Region");
					row.createCell(19).setCellValue("Base Amount");
					row.createCell(20).setCellValue("Total Amount");
					row.createCell(21).setCellValue("TDR / Surcharge");
					row.createCell(22).setCellValue("GST");
					row.createCell(23).setCellValue("Merchant Amount");
					row.createCell(23).setCellValue("Transaction Flag");
					row.createCell(24).setCellValue("Part Settled Flag");
					row.createCell(25).setCellValue("UDF11");
					row.createCell(26).setCellValue("UDF12");
					row.createCell(27).setCellValue("UDF13");
					row.createCell(28).setCellValue("UDF14");
					row.createCell(29).setCellValue("UDF15");
					row.createCell(30).setCellValue("UDF16");
					row.createCell(31).setCellValue("UDF17");
					row.createCell(32).setCellValue("UDF18");

					for (PaymentSearchDownloadObject transactionSearch : settlementdata) {

						row = sheet.createRow(rownum++);
						transactionSearch.setSrNo(String.valueOf(rownum - 1));

						Object[] objArr = transactionSearch
								.myCsvMethodemailPaymentsReportCapturedForSpecificSubMerchant();

						int cellnum = 0;
						for (Object obj : objArr) {
							// this line creates a cell in the next column of that row
							Cell cell = row.createCell(cellnum++);
							if (obj instanceof String)
								cell.setCellValue((String) obj);
							else if (obj instanceof Integer)
								cell.setCellValue((Integer) obj);

						}
					}
					try {
						String FILE_EXTENSION = ".xlsx";
						if (StringUtils.isNotBlank(subMerchPayId)) {
							filenameexcel = "Settled_Transaction_Report" + subMerchPayId + df.format(new Date())
									+ FILE_EXTENSION;
						} else {
							filenameexcel = "Settled_Transaction_Report" + merchPayId + df.format(new Date())
									+ FILE_EXTENSION;
						}
						bos = new ByteArrayOutputStream();
						try {
							wb.write(bos);
						} finally {
							bos.close();
						}
						excelFileAsBytes = bos.toByteArray();

					} catch (Exception exception) {
						logger.error("Exception in excel file ", exception);
					}

				
				try {
					if (cc || dc || refundcc || refunddc) {
					}else {
						currency = null;
					}
					String subject = getMailSubject(fromdate, todate, merchant);
					String messageBody = getEmailBodyWithAttachement(payoutDateShow, merchant,
							merchantImageAddr, saletxn, refundtxn, saleAmount, refundAmount, settlementAmount, utrNo,
							account, chargeback, chargeBackAmount, currency);
					pepipostEmailSender.sendEmailWithAttachment(subject, messageBody, filename, bytes, merchant.getEmailId(), "",
							filenameexcel, excelFileAsBytes, user.getTransactionEmailId());

				} catch (Exception exception) {
					logger.error("Exception", exception);
				}
				logger.info("Auto Send Merchnt Payment Advise File generated successfully for "
						+ merchant.getBusinessName() + " Merchant");
			} catch (Exception exception) {
				logger.error("Exception", exception);
			}
		}
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

	private HashMap<String, HashMap> getMerchant(Merchants merchant, List<Merchants> subMerchantList) {

		HashMap<String, List<Merchants>> hm = new HashMap<String, List<Merchants>>();
		HashMap<String, String> hm2 = new HashMap<String, String>();
		HashMap<String, HashMap> hm3 = new HashMap<String, HashMap>();
		if (merchant.getIsSuperMerchant() || !merchant.getIsSuperMerchant() && subMerchantList.size() != 0) {

			for (Merchants subMerchant : subMerchantList) {
				hm2.put("MerchantPayId", merchant.getPayId());
				hm2.put("SubMerchantPayId", subMerchant.getPayId());
				subMerchantList.remove(subMerchant);
				hm.put("SubMerchantList", subMerchantList);
				hm3.put("SubMerchantMap", hm);
				hm3.put("Merchant", hm2);
				break;
			}

		} else {
			hm2.put("MerchantPayId", merchant.getPayId());
			hm2.put("SubMerchantPayId", "");
			hm3.put("Merchant", hm2);
		}
		return hm3;
	}

	public BigDecimal getSufCharge(String payId, String txnType, String paymentType, String mopType, Object baseAmount,
			List<SUFDetail> sufCharge, String paymentRegion) {
		try {

			BigDecimal fixedCharge = null;
			BigDecimal percentageCharge = null;
			for (SUFDetail suf : sufCharge) {

				String slabArray[] = suf.getSlab().split("-");

				BigDecimal baseZero = new BigDecimal(slabArray[0]);
				BigDecimal baseOne = new BigDecimal(slabArray[1]);
				BigDecimal baseAmountBigDecimal = new BigDecimal(baseAmount.toString());

				if (suf.getPayId().equalsIgnoreCase(payId) && suf.getTxnType().equalsIgnoreCase(txnType)
						&& suf.getPaymentType().equalsIgnoreCase(paymentType)
						&& suf.getMopType().equalsIgnoreCase(mopType)
						&& suf.getPaymentRegion().equalsIgnoreCase(paymentRegion)
						&& (suf.getPaymentType().equalsIgnoreCase("NEFT")
								|| suf.getPaymentType().equalsIgnoreCase("IMPS")
								|| suf.getPaymentType().equalsIgnoreCase("RTGS"))) {

					if ((baseAmountBigDecimal.compareTo(baseZero) == 1 || baseAmountBigDecimal.compareTo(baseZero) == 0)
							&& (baseAmountBigDecimal.compareTo(baseOne) == -1)
							|| baseAmountBigDecimal.compareTo(baseOne) == 0) {

						fixedCharge = new BigDecimal(suf.getFixedCharge());
						percentageCharge = (new BigDecimal(suf.getPercentageAmount())
								.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
								.multiply(new BigDecimal(baseAmount.toString()))).setScale(2, RoundingMode.FLOOR);

						break;
					}
				} else if (suf.getPayId().equalsIgnoreCase(payId) && suf.getTxnType().equalsIgnoreCase(txnType)
						&& suf.getPaymentType().equalsIgnoreCase(paymentType)
						&& suf.getMopType().equalsIgnoreCase(mopType)
						&& suf.getPaymentRegion().equalsIgnoreCase(paymentRegion)) {

					if ((baseAmountBigDecimal.compareTo(baseZero) == 1 || baseAmountBigDecimal.compareTo(baseZero) == 0)
							&& (baseAmountBigDecimal.compareTo(baseOne) == -1)
							|| baseAmountBigDecimal.compareTo(baseOne) == 0) {

						fixedCharge = new BigDecimal(suf.getFixedCharge());
						percentageCharge = (new BigDecimal(suf.getPercentageAmount())
								.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
								.multiply(new BigDecimal(baseAmount.toString()))).setScale(2, RoundingMode.FLOOR);

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
			logger.error("exception caught while calculate suf charges for payment advise report : " , ex);
			return null;
		}
	}

	public Merchants getMerchant(User user) {

		Merchants merchant = new Merchants();

		merchant.setEmailId(user.getEmailId());
		merchant.setPayId(user.getPayId());
		merchant.setBusinessName(user.getBusinessName());
		merchant.setSuperMerchantId(user.getSuperMerchantId());
		merchant.setIsSuperMerchant(user.isSuperMerchant());

		return merchant;
	}

	public void senMailToSubmerchants(String payoutDate, Merchants merchant, String currency) {
		try {
			int saletxn = 0;
			int refundtxn = 0;

			Map<String, MerchantPaymentAdviseDownloadObject> saleSettledMap = new HashMap<String, MerchantPaymentAdviseDownloadObject>();
			Map<String, MerchantPaymentAdviseDownloadObject> refundSettledMap = new HashMap<String, MerchantPaymentAdviseDownloadObject>();
			List<TransactionSearchDownloadObject> transactionList = new ArrayList<TransactionSearchDownloadObject>();
			List<MerchantPaymentAdviseDownloadObject> paymentAdviceList = new ArrayList<MerchantPaymentAdviseDownloadObject>();
			List<SUFDetail> sufCharge = new ArrayList<SUFDetail>();
			List<CibNodalTransaction> cibData = new ArrayList<CibNodalTransaction>();
			List<Chargeback> userChargeback = new ArrayList<Chargeback>();
			String merchPayId = "";
			String subMerchPayId = "";
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy");
			SimpleDateFormat dateFor = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat sdftxndate = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat dateForshow = new SimpleDateFormat("dd MMMM yyyy");

			merchPayId = merchant.getSuperMerchantId();
			subMerchPayId = merchant.getPayId();

			if (StringUtils.isNotBlank(merchPayId)) {
				sufCharge = sufDetailDao.fetchSufChargeByPayId(merchPayId);
			}
			Date fRom = sdf2.parse(payoutDate);
			String payoutDatePaymentAdvice = dateFor.format(fRom);

			paymentAdviceList = paymentAdviseReportFileCreator.merchantPaymentAdviseDownloadAutoSendForSale(merchPayId,
					subMerchPayId, payoutDatePaymentAdvice, sessionUser, currency);
			logger.info("List create successfully for Auto Send Download MerchantPaymentAdviceReport");

			for (MerchantPaymentAdviseDownloadObject paymentAdvice : paymentAdviceList) {
				if (paymentAdvice.getOrigTxnType().equals(TxnType.SALE.getName())) {
					saleSettledMap.put(paymentAdvice.getOid(), paymentAdvice);

				} else {
					refundSettledMap.put(paymentAdvice.getOid(), paymentAdvice);
				}
			}
			DateFormat df = new SimpleDateFormat("ddMMyyhhmmss");
			filename = "DailyPaymentsAdvise" + subMerchPayId + df.format(new Date()) + ".pdf";
			Document document = null;
			ByteArrayOutputStream baos = null;
			byte[] bytes = null;
			document = new Document(PageSize.A4, 20, 20, 20, 10);
			baos = new ByteArrayOutputStream();
			PdfWriter.getInstance(document, baos);
			document.open();
			document.addTitle("Payment Advise");
			document.addSubject("Merchant Payment on Payment Gateway Solution Private Limited");
			document.addKeywords("Payment Advice, Payment Gateway");
			document.addAuthor("Payment Gateway Solution Private Limited, created on " + new Date());
			//logger.info("inside the pdf body for Payment Advice");
			Font tableHeaderFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
			tableHeaderFont.setColor(BaseColor.GRAY);
			Font totalFont = new Font(Font.FontFamily.HELVETICA, 10);
			totalFont.setColor(BaseColor.WHITE);
			Font subCatFont = new Font(Font.FontFamily.HELVETICA, 10);
			Font headingtop = new Font(Font.FontFamily.HELVETICA, 10);
			headingtop.setColor(BaseColor.GRAY);
			Font catFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
			Font smallFont = new Font(Font.FontFamily.HELVETICA, 9);
			Font footerFont = new Font(Font.FontFamily.HELVETICA, 10);
			footerFont.setColor(BaseColor.BLUE);

			Set<String> utr = new HashSet<String>();
			String utrNo = "NA";
			String delim = "-";
			String transferMode = "NA";
			String fromdate = "NA";
			String todate = "NA";
			String account = "NA";
			String dateFrom = "NA";
			String dateTo = "NA";
			Date createDate = null;
			Set<String> dateIndex = new HashSet<String>();
			Set<String> transferModeSet = new HashSet<String>();

			BigDecimal SUFAmount = new BigDecimal("0.00");
			BigDecimal ccTotalAmount = new BigDecimal("0.00");
			BigDecimal dcTotalAmount = new BigDecimal("0.00");
			BigDecimal nbTotalAmount = new BigDecimal("0.00");
			BigDecimal inTotalAmount = new BigDecimal("0.00");
			BigDecimal upTotalAmount = new BigDecimal("0.00");
			BigDecimal wlTotalAmount = new BigDecimal("0.00");
			BigDecimal impsTotalAmount = new BigDecimal("0.00");
			BigDecimal neftTotalAmount = new BigDecimal("0.00");
			BigDecimal rtgsTotalAmount = new BigDecimal("0.00");
			BigDecimal codTotalAmount = new BigDecimal("0.00");

			BigDecimal ccTdr = new BigDecimal("0.00");
			BigDecimal dcTdr = new BigDecimal("0.00");
			BigDecimal nbTdr = new BigDecimal("0.00");
			BigDecimal inTdr = new BigDecimal("0.00");
			BigDecimal upTdr = new BigDecimal("0.00");
			BigDecimal wlTdr = new BigDecimal("0.00");
			BigDecimal impsTdr = new BigDecimal("0.00");
			BigDecimal neftTdr = new BigDecimal("0.00");
			BigDecimal rtgsTdr = new BigDecimal("0.00");
			BigDecimal codTdr = new BigDecimal("0.00");

			BigDecimal ccGst = new BigDecimal("0.00");
			BigDecimal dcGst = new BigDecimal("0.00");
			BigDecimal nbGst = new BigDecimal("0.00");
			BigDecimal inGst = new BigDecimal("0.00");
			BigDecimal upGst = new BigDecimal("0.00");
			BigDecimal wlGst = new BigDecimal("0.00");
			BigDecimal impsGst = new BigDecimal("0.00");
			BigDecimal neftGst = new BigDecimal("0.00");
			BigDecimal rtgsGst = new BigDecimal("0.00");
			BigDecimal codGst = new BigDecimal("0.00");

			BigDecimal ccSuf = new BigDecimal("0.00");
			BigDecimal dcSuf = new BigDecimal("0.00");
			BigDecimal nbSuf = new BigDecimal("0.00");
			BigDecimal inSuf = new BigDecimal("0.00");
			BigDecimal upSuf = new BigDecimal("0.00");
			BigDecimal wlSuf = new BigDecimal("0.00");
			BigDecimal impsSuf = new BigDecimal("0.00");
			BigDecimal neftSuf = new BigDecimal("0.00");
			BigDecimal rtgsSuf = new BigDecimal("0.00");
			BigDecimal codSuf = new BigDecimal("0.00");

			BigDecimal totalAmount = new BigDecimal("0.00");
			BigDecimal totalTdr = new BigDecimal("0.00");
			BigDecimal totalGst = new BigDecimal("0.00");
			BigDecimal totalSuf = new BigDecimal("0.00");

			BigDecimal refundccTotalAmount = new BigDecimal("0.00");
			BigDecimal refunddcTotalAmount = new BigDecimal("0.00");
			BigDecimal refundnbTotalAmount = new BigDecimal("0.00");
			BigDecimal refundinTotalAmount = new BigDecimal("0.00");
			BigDecimal refundupTotalAmount = new BigDecimal("0.00");
			BigDecimal refundwlTotalAmount = new BigDecimal("0.00");
			BigDecimal refundimpsTotalAmount = new BigDecimal("0.00");
			BigDecimal refundneftTotalAmount = new BigDecimal("0.00");
			BigDecimal refundrtgsTotalAmount = new BigDecimal("0.00");
			BigDecimal refundcodTotalAmount = new BigDecimal("0.00");

			BigDecimal refundccTdr = new BigDecimal("0.00");
			BigDecimal refunddcTdr = new BigDecimal("0.00");
			BigDecimal refundnbTdr = new BigDecimal("0.00");
			BigDecimal refundinTdr = new BigDecimal("0.00");
			BigDecimal refundupTdr = new BigDecimal("0.00");
			BigDecimal refundwlTdr = new BigDecimal("0.00");
			BigDecimal refundimpsTdr = new BigDecimal("0.00");
			BigDecimal refundneftTdr = new BigDecimal("0.00");
			BigDecimal refundrtgsTdr = new BigDecimal("0.00");
			BigDecimal refundcodTdr = new BigDecimal("0.00");

			BigDecimal refundccGst = new BigDecimal("0.00");
			BigDecimal refunddcGst = new BigDecimal("0.00");
			BigDecimal refundnbGst = new BigDecimal("0.00");
			BigDecimal refundinGst = new BigDecimal("0.00");
			BigDecimal refundupGst = new BigDecimal("0.00");
			BigDecimal refundwlGst = new BigDecimal("0.00");
			BigDecimal refundimpsGst = new BigDecimal("0.00");
			BigDecimal refundneftGst = new BigDecimal("0.00");
			BigDecimal refundrtgsGst = new BigDecimal("0.00");
			BigDecimal refundcodGst = new BigDecimal("0.00");

			BigDecimal refundccSuf = new BigDecimal("0.00");
			BigDecimal refunddcSuf = new BigDecimal("0.00");
			BigDecimal refundnbSuf = new BigDecimal("0.00");
			BigDecimal refundinSuf = new BigDecimal("0.00");
			BigDecimal refundupSuf = new BigDecimal("0.00");
			BigDecimal refundwlSuf = new BigDecimal("0.00");
			BigDecimal refundimpsSuf = new BigDecimal("0.00");
			BigDecimal refundneftSuf = new BigDecimal("0.00");
			BigDecimal refundrtgsSuf = new BigDecimal("0.00");
			BigDecimal refundcodSuf = new BigDecimal("0.00");

			BigDecimal refundtotalAmount = new BigDecimal("0.00");
			BigDecimal refundtotalTdr = new BigDecimal("0.00");
			BigDecimal refundtotalGst = new BigDecimal("0.00");
			BigDecimal refundtotalSuf = new BigDecimal("0.00");

			BigDecimal settlementAmount = new BigDecimal("0.00");

			Boolean cc = false;
			Boolean dc = false;
			Boolean nb = false;
			Boolean in = false;
			Boolean up = false;
			Boolean wl = false;
			Boolean imps = false;
			Boolean rtgs = false;
			Boolean neft = false;
			Boolean cod = false;
			Boolean refundcc = false;
			Boolean refunddc = false;
			Boolean refundnb = false;
			Boolean refundin = false;
			Boolean refundup = false;
			Boolean refundwl = false;
			Boolean refundimps = false;
			Boolean refundrtgs = false;
			Boolean refundneft = false;
			Boolean refundcod = false;

			Paragraph logoParagraph = null;
			Paragraph headingParagraph = null;
			Paragraph nameParagraph = null;
			Paragraph reporttypeParagraph = null;

			Paragraph custDetailsParagraph = null;
			Paragraph footerParagraph = null;

			User user = null;

			String merchantImageAddr = null;
			if (StringUtils.isNotBlank(subMerchPayId)) {
				user = userDao.findPayId(subMerchPayId);
			} else {
				user = userDao.findPayId(merchPayId);
			}
			if (StringUtils.isNotBlank(subMerchantPayId)) {
			
					merchantImageAddr = getMerchantLogoPath(user.getPayId());

					File file = new File(merchantImageAddr);

					if (!file.exists()) {
						merchantImageAddr = getMerchantLogoPath(user.getSuperMerchantId());
					}
			} else if (StringUtils.isBlank(user.getSuperMerchantId())) {
				merchantImageAddr = getMerchantLogoPath(merchantPayId);
				File file = new File(merchantImageAddr);

				if (!file.exists()) {
					merchantImageAddr = getMerchantLogoPath(user.getSuperMerchantId());
				}
			} else {
				merchantImageAddr = getMerchantLogoPath(merchantPayId);
			}

			//logger.info("inside set sale transaction");

			for (MerchantPaymentAdviseDownloadObject sale : saleSettledMap.values()) {
				try {
					createDate = sdf1.parse(sale.getCreateDate());
				} catch (ParseException e) {
					logger.error("Exception in date parse ", e);
				}
				dateIndex.add(sdftxndate.format(createDate));
				utr.add(sale.getUtrNo());
				saletxn++;
				BigDecimal baseAmount = new BigDecimal(sale.getGrossAmount());
				BigDecimal tdr = new BigDecimal(sale.getTdr());
				BigDecimal gst = new BigDecimal(sale.getGst());
				if (sale.getPaymentType().equalsIgnoreCase("Credit Card")) {
					cc = true;
					ccTotalAmount = ccTotalAmount.add(baseAmount);
					ccTdr = ccTdr.add(tdr);
					ccGst = ccGst.add(gst);
					SUFAmount = getSufCharge(sale.getPayId(), sale.getOrigTxnType(), sale.getPaymentType(),
							sale.getCardNetwork(), sale.getBaseAmount(), sufCharge, sale.getPaymentRegion());
					ccSuf = ccSuf.add(SUFAmount);
					totalAmount = totalAmount.add(baseAmount);
					totalTdr = totalTdr.add(tdr);
					totalGst = totalGst.add(gst);
					totalSuf = totalSuf.add(SUFAmount);

				}
				if (sale.getPaymentType().equalsIgnoreCase("Debit Card")) {
					dc = true;
					dcTotalAmount = dcTotalAmount.add(baseAmount);
					dcTdr = dcTdr.add(tdr);
					dcGst = dcGst.add(gst);
					SUFAmount = getSufCharge(sale.getPayId(), sale.getOrigTxnType(), sale.getPaymentType(),
							sale.getCardNetwork(), sale.getBaseAmount(), sufCharge, sale.getPaymentRegion());
					dcSuf = dcSuf.add(SUFAmount);
					totalAmount = totalAmount.add(baseAmount);
					totalTdr = totalTdr.add(tdr);
					totalGst = totalGst.add(gst);
					totalSuf = totalSuf.add(SUFAmount);

				}
				if (sale.getPaymentType().equalsIgnoreCase("Cod")) {
					cod = true;
					codTotalAmount = codTotalAmount.add(baseAmount);
					codTdr = codTdr.add(tdr);
					codGst = codGst.add(gst);
					SUFAmount = getSufCharge(sale.getPayId(), sale.getOrigTxnType(), sale.getPaymentType(),
							sale.getCardNetwork(), sale.getBaseAmount(), sufCharge, sale.getPaymentRegion());
					codSuf = codSuf.add(SUFAmount);
					totalAmount = totalAmount.add(baseAmount);
					totalTdr = totalTdr.add(tdr);
					totalGst = totalGst.add(gst);
					totalSuf = totalSuf.add(SUFAmount);

				}
				if (sale.getPaymentType().equalsIgnoreCase("Net Banking")) {
					nb = true;
					nbTotalAmount = nbTotalAmount.add(baseAmount);
					nbTdr = nbTdr.add(tdr);
					nbGst = nbGst.add(gst);
					SUFAmount = getSufCharge(sale.getPayId(), sale.getOrigTxnType(), sale.getPaymentType(),
							sale.getCardNetwork(), sale.getBaseAmount(), sufCharge, sale.getPaymentRegion());
					nbSuf = nbSuf.add(SUFAmount);
					totalAmount = totalAmount.add(baseAmount);
					totalTdr = totalTdr.add(tdr);
					totalGst = totalGst.add(gst);
					totalSuf = totalSuf.add(SUFAmount);

				}
				if (sale.getPaymentType().equalsIgnoreCase("Wallet")) {
					wl = true;
					wlTotalAmount = wlTotalAmount.add(baseAmount);
					wlTdr = wlTdr.add(tdr);
					wlGst = wlGst.add(gst);
					SUFAmount = getSufCharge(sale.getPayId(), sale.getOrigTxnType(), sale.getPaymentType(),
							sale.getCardNetwork(), sale.getBaseAmount(), sufCharge, sale.getPaymentRegion());
					wlSuf = wlSuf.add(SUFAmount);
					totalAmount = totalAmount.add(baseAmount);
					totalTdr = totalTdr.add(tdr);
					totalGst = totalGst.add(gst);
					totalSuf = totalSuf.add(SUFAmount);

				}

				if (sale.getPaymentType().equalsIgnoreCase("UPI")) {
					up = true;
					upTotalAmount = upTotalAmount.add(baseAmount);
					upTdr = upTdr.add(tdr);
					upGst = upGst.add(gst);
					SUFAmount = getSufCharge(sale.getPayId(), sale.getOrigTxnType(), sale.getPaymentType(),
							sale.getCardNetwork(), sale.getBaseAmount(), sufCharge, sale.getPaymentRegion());
					upSuf = upSuf.add(SUFAmount);
					totalAmount = totalAmount.add(baseAmount);
					totalTdr = totalTdr.add(tdr);
					totalGst = totalGst.add(gst);
					totalSuf = totalSuf.add(SUFAmount);

				}

				if (sale.getPaymentType().equalsIgnoreCase("NEFT")) {
					neft = true;
					neftTotalAmount = neftTotalAmount.add(baseAmount);
					neftTdr = neftTdr.add(tdr);
					neftGst = neftGst.add(gst);
					SUFAmount = getSufCharge(sale.getPayId(), sale.getOrigTxnType(), sale.getPaymentType(),
							sale.getCardNetwork(), sale.getBaseAmount(), sufCharge, sale.getPaymentRegion());
					neftSuf = neftSuf.add(SUFAmount);
					totalAmount = totalAmount.add(baseAmount);
					totalTdr = totalTdr.add(tdr);
					totalGst = totalGst.add(gst);
					totalSuf = totalSuf.add(SUFAmount);

				}

				if (sale.getPaymentType().equalsIgnoreCase("RTGS")) {
					rtgs = true;
					rtgsTotalAmount = rtgsTotalAmount.add(baseAmount);
					rtgsTdr = rtgsTdr.add(tdr);
					rtgsGst = rtgsGst.add(gst);
					SUFAmount = getSufCharge(sale.getPayId(), sale.getOrigTxnType(), sale.getPaymentType(),
							sale.getCardNetwork(), sale.getBaseAmount(), sufCharge, sale.getPaymentRegion());
					rtgsSuf = rtgsSuf.add(SUFAmount);
					totalAmount = totalAmount.add(baseAmount);
					totalTdr = totalTdr.add(tdr);
					totalGst = totalGst.add(gst);
					totalSuf = totalSuf.add(SUFAmount);

				}

				if (sale.getPaymentType().equalsIgnoreCase("IMPS")) {
					imps = true;
					impsTotalAmount = impsTotalAmount.add(baseAmount);
					impsTdr = impsTdr.add(tdr);
					impsGst = impsGst.add(gst);
					SUFAmount = getSufCharge(sale.getPayId(), sale.getOrigTxnType(), sale.getPaymentType(),
							sale.getCardNetwork(), sale.getBaseAmount(), sufCharge, sale.getPaymentRegion());
					impsSuf = impsSuf.add(SUFAmount);
					totalAmount = totalAmount.add(baseAmount);
					totalTdr = totalTdr.add(tdr);
					totalGst = totalGst.add(gst);
					totalSuf = totalSuf.add(SUFAmount);

				}

				if (sale.getPaymentType().equalsIgnoreCase("international")) {
					in = true;
					inTotalAmount = inTotalAmount.add(baseAmount);
					inTdr = inTdr.add(tdr);
					inGst = inGst.add(gst);
					SUFAmount = getSufCharge(sale.getPayId(), sale.getOrigTxnType(), sale.getPaymentType(),
							sale.getCardNetwork(), sale.getBaseAmount(), sufCharge, sale.getPaymentRegion());
					inSuf = inSuf.add(SUFAmount);
					totalAmount = totalAmount.add(baseAmount);
					totalTdr = totalTdr.add(tdr);
					totalGst = totalGst.add(gst);
					totalSuf = totalSuf.add(SUFAmount);

				}

			}
			//logger.info("inside set refund transaction");

			for (MerchantPaymentAdviseDownloadObject refund : refundSettledMap.values()) {
				refundtxn++;
				BigDecimal baseAmount = new BigDecimal(refund.getBaseAmount());
				BigDecimal tdr = new BigDecimal(refund.getTdr());
				BigDecimal gst = new BigDecimal(refund.getGst());
				if (refund.getPaymentType().equalsIgnoreCase("Credit Card")) {
					refundcc = true;
					refundccTotalAmount = refundccTotalAmount.add(baseAmount);
					refundccTdr = refundccTdr.add(tdr);
					refundccGst = refundccGst.add(gst);
					SUFAmount = getSufCharge(refund.getPayId(), refund.getOrigTxnType(), refund.getPaymentType(),
							refund.getCardNetwork(), refund.getBaseAmount(), sufCharge, refund.getPaymentRegion());
					refundccSuf = refundccSuf.add(SUFAmount);
					refundtotalAmount = refundtotalAmount.add(baseAmount);
					refundtotalTdr = refundtotalTdr.add(tdr);
					refundtotalGst = refundtotalGst.add(gst);
					refundtotalSuf = refundtotalSuf.add(SUFAmount);

				}
				if (refund.getPaymentType().equalsIgnoreCase("Debit Card")) {
					refunddc = true;
					refunddcTotalAmount = refunddcTotalAmount.add(baseAmount);
					refunddcTdr = refunddcTdr.add(tdr);
					refunddcGst = refunddcGst.add(gst);
					SUFAmount = getSufCharge(refund.getPayId(), refund.getOrigTxnType(), refund.getPaymentType(),
							refund.getCardNetwork(), refund.getBaseAmount(), sufCharge, refund.getPaymentRegion());
					refunddcSuf = refunddcSuf.add(SUFAmount);
					refundtotalAmount = refundtotalAmount.add(baseAmount);
					refundtotalTdr = refundtotalTdr.add(tdr);
					refundtotalGst = refundtotalGst.add(gst);
					refundtotalSuf = refundtotalSuf.add(SUFAmount);
				}
				if (refund.getPaymentType().equalsIgnoreCase("Cod")) {
					refundcod = true;
					refundcodTotalAmount = refundcodTotalAmount.add(baseAmount);
					refundcodTdr = refundcodTdr.add(tdr);
					refundcodGst = refundcodGst.add(gst);
					SUFAmount = getSufCharge(refund.getPayId(), refund.getOrigTxnType(), refund.getPaymentType(),
							refund.getCardNetwork(), refund.getBaseAmount(), sufCharge, refund.getPaymentRegion());
					refundcodSuf = refundcodSuf.add(SUFAmount);
					refundtotalAmount = refundtotalAmount.add(baseAmount);
					refundtotalTdr = refundtotalTdr.add(tdr);
					refundtotalGst = refundtotalGst.add(gst);
					refundtotalSuf = refundtotalSuf.add(SUFAmount);

				}
				if (refund.getPaymentType().equalsIgnoreCase("Net Banking")) {
					refundnb = true;
					refundnbTotalAmount = refundnbTotalAmount.add(baseAmount);
					refundnbTdr = refundnbTdr.add(tdr);
					refundnbGst = refundnbGst.add(gst);
					SUFAmount = getSufCharge(refund.getPayId(), refund.getOrigTxnType(), refund.getPaymentType(),
							refund.getCardNetwork(), refund.getBaseAmount(), sufCharge, refund.getPaymentRegion());
					refundnbSuf = refundnbSuf.add(SUFAmount);
					refundtotalAmount = refundtotalAmount.add(baseAmount);
					refundtotalTdr = refundtotalTdr.add(tdr);
					refundtotalGst = refundtotalGst.add(gst);
					refundtotalSuf = refundtotalSuf.add(SUFAmount);

				}
				if (refund.getPaymentType().equalsIgnoreCase("Wallet")) {
					refundwl = true;
					refundwlTotalAmount = refundwlTotalAmount.add(baseAmount);
					refundwlTdr = refundwlTdr.add(tdr);
					refundwlGst = refundwlGst.add(gst);
					SUFAmount = getSufCharge(refund.getPayId(), refund.getOrigTxnType(), refund.getPaymentType(),
							refund.getCardNetwork(), refund.getBaseAmount(), sufCharge, refund.getPaymentRegion());
					refundwlSuf = refundwlSuf.add(SUFAmount);
					refundtotalAmount = refundtotalAmount.add(baseAmount);
					refundtotalTdr = refundtotalTdr.add(tdr);
					refundtotalGst = refundtotalGst.add(gst);
					refundtotalSuf = refundtotalSuf.add(SUFAmount);

				}

				if (refund.getPaymentType().equalsIgnoreCase("UPI")) {
					refundup = true;
					refundupTotalAmount = refundupTotalAmount.add(baseAmount);
					refundupTdr = refundupTdr.add(tdr);
					refundupGst = refundupGst.add(gst);
					SUFAmount = getSufCharge(refund.getPayId(), refund.getOrigTxnType(), refund.getPaymentType(),
							refund.getCardNetwork(), refund.getBaseAmount(), sufCharge, refund.getPaymentRegion());
					refundupSuf = refundupSuf.add(SUFAmount);
					refundtotalAmount = refundtotalAmount.add(baseAmount);
					refundtotalTdr = refundtotalTdr.add(tdr);
					refundtotalGst = refundtotalGst.add(gst);
					refundtotalSuf = refundtotalSuf.add(SUFAmount);

				}

				if (refund.getPaymentType().equalsIgnoreCase("NEFT")) {
					refundneft = true;
					refundneftTotalAmount = refundneftTotalAmount.add(baseAmount);
					refundneftTdr = refundneftTdr.add(tdr);
					refundneftGst = refundneftGst.add(gst);
					SUFAmount = getSufCharge(refund.getPayId(), refund.getOrigTxnType(), refund.getPaymentType(),
							refund.getCardNetwork(), refund.getBaseAmount(), sufCharge, refund.getPaymentRegion());
					refundneftSuf = refundneftSuf.add(SUFAmount);
					refundtotalAmount = refundtotalAmount.add(baseAmount);
					refundtotalTdr = refundtotalTdr.add(tdr);
					refundtotalGst = refundtotalGst.add(gst);
					refundtotalSuf = refundtotalSuf.add(SUFAmount);

				}

				if (refund.getPaymentType().equalsIgnoreCase("RTGS")) {
					refundrtgs = true;
					refundrtgsTotalAmount = refundrtgsTotalAmount.add(baseAmount);
					refundrtgsTdr = refundrtgsTdr.add(tdr);
					refundrtgsGst = refundrtgsGst.add(gst);
					SUFAmount = getSufCharge(refund.getPayId(), refund.getOrigTxnType(), refund.getPaymentType(),
							refund.getCardNetwork(), refund.getBaseAmount(), sufCharge, refund.getPaymentRegion());
					refundrtgsSuf = refundrtgsSuf.add(SUFAmount);
					refundtotalAmount = refundtotalAmount.add(baseAmount);
					refundtotalTdr = refundtotalTdr.add(tdr);
					refundtotalGst = refundtotalGst.add(gst);
					refundtotalSuf = refundtotalSuf.add(SUFAmount);

				}

				if (refund.getPaymentType().equalsIgnoreCase("IMPS")) {
					refundimps = true;
					refundimpsTotalAmount = refundimpsTotalAmount.add(baseAmount);
					refundimpsTdr = refundimpsTdr.add(tdr);
					refundimpsGst = refundimpsGst.add(gst);
					SUFAmount = getSufCharge(refund.getPayId(), refund.getOrigTxnType(), refund.getPaymentType(),
							refund.getCardNetwork(), refund.getBaseAmount(), sufCharge, refund.getPaymentRegion());
					refundimpsSuf = refundimpsSuf.add(SUFAmount);
					refundtotalAmount = refundtotalAmount.add(baseAmount);
					refundtotalTdr = refundtotalTdr.add(tdr);
					refundtotalGst = refundtotalGst.add(gst);
					refundtotalSuf = refundtotalSuf.add(SUFAmount);

				}

				if (refund.getPaymentType().equalsIgnoreCase("international")) {
					refundin = true;
					refundinTotalAmount = refundinTotalAmount.add(baseAmount);
					refundinTdr = refundinTdr.add(tdr);
					refundinGst = refundinGst.add(gst);
					SUFAmount = getSufCharge(refund.getPayId(), refund.getOrigTxnType(), refund.getPaymentType(),
							refund.getCardNetwork(), refund.getBaseAmount(), sufCharge, refund.getPaymentRegion());
					refundinSuf = refundinSuf.add(SUFAmount);
					refundtotalAmount = refundtotalAmount.add(baseAmount);
					refundtotalTdr = refundtotalTdr.add(tdr);
					refundtotalGst = refundtotalGst.add(gst);
					refundtotalSuf = refundtotalSuf.add(SUFAmount);

				}

			}
			settlementAmount = totalAmount.subtract(totalTdr).subtract(totalGst).subtract(totalSuf)
					.subtract(codTotalAmount).subtract(refundtotalAmount.subtract(refundtotalTdr)
							.subtract(refundtotalGst).subtract(refundtotalSuf).subtract(refundcodTotalAmount));

			if (!utr.isEmpty()) {
				for (String utrIterate : utr) {
					cibData = nodalTransferDao.fetchTransferModeandAccountByUtr(utrIterate);
					if (!cibData.isEmpty()) {
						for (CibNodalTransaction cib : cibData) {
							transferModeSet.add(cib.getTxnType());
							account = cib.getBankAccountNumber();
						}
					}
				}
				transferMode = String.join(delim, transferModeSet);
				utrNo = String.join(delim, utr);
			}
			if (!dateIndex.isEmpty()) {
				try {
					Date fromDate = null;
					Date toDate = null;
					TreeSet<String> treeSet = new TreeSet<String>(dateIndex);
					String[] dateArray = treeSet.toArray(new String[treeSet.size()]);
					if (dateArray.length == 1) {
						fromDate = sdftxndate.parse(dateArray[0]);
						fromdate = dateForshow.format(fromDate);
						dateFrom = dateFor.format(fromDate) + " 00:00:00";
						todate = dateForshow.format(fromDate);
						dateTo = dateFor.format(fromDate)+ " 23:59:59";
					} else {
						fromDate = sdftxndate.parse(dateArray[0]);
						toDate = sdftxndate.parse(dateArray[dateArray.length - 1]);
						fromdate = dateForshow.format(fromDate);
						dateFrom = dateFor.format(fromDate)+ " 00:00:00";
						todate = dateForshow.format(toDate);
						dateTo = dateFor.format(toDate)+ " 23:59:59";
					}
				} catch (ParseException e) {
					logger.error("date parse Exception ", e);
				}
			}
			settlementdata.clear();
			if (!dateFrom.equalsIgnoreCase("NA") && !dateTo.equalsIgnoreCase("NA")) {
					userChargeback = chargebackDao.findChargebackByPayid(merchPayId, dateFrom, dateTo);
				
					settlementdata = paymentAdviseReportFileCreator.searchPaymentForDownload(merchPayId, subMerchPayId,
							dateFrom, dateTo, sessionUser, currency);
				
			}
			int chargeback = 0;
			BigDecimal chargeBackAmount = new BigDecimal("0.00");
			for (Chargeback chargeBack : userChargeback) {
				chargeback++;
				chargeBackAmount = chargeBackAmount.add(chargeBack.getTotalchargebackAmount());
			}
			String payoutDateshow = dateForshow.format(fRom);
			// Header
			//logger.info("header");

			document.add(Chunk.NEWLINE);

			float[] heading = new float[] { 100f };
			PdfPTable headingtable = new PdfPTable(1);
			headingtable.setWidths(heading);
			headingtable.getDefaultCell().setBorderWidth(0f);
			headingtable.setWidthPercentage(100f);
			DateFormat dfh = new SimpleDateFormat("ddMMyy");
			headingParagraph = new Paragraph("Daily Payment Advice : " + user.getPayId() + dfh.format(new Date()),
					headingtop);
			headingParagraph.setAlignment(Element.ALIGN_LEFT);
			headingParagraph.add(Chunk.NEWLINE);
			headingParagraph.add(Chunk.NEWLINE);
			headingtable.addCell(headingParagraph);
			document.add(headingtable);

			//logger.info("name and logo");

			float[] info = new float[] { 50f, 40f };
			PdfPTable merchantDetail = new PdfPTable(2);
			merchantDetail.setWidths(info);
			merchantDetail.getDefaultCell().setBorderWidth(0f);
			merchantDetail.setWidthPercentage(90f);
			if (user.getFirstName() != null || user.getLastName() != null) {
				nameParagraph = new Paragraph(
						user.getFirstName() + " " + user.getLastName() + Chunk.NEWLINE + user.getPayId(), catFont);
			} else {
				nameParagraph = new Paragraph(" " + Chunk.NEWLINE + user.getPayId(), catFont);
			}
			nameParagraph.setAlignment(Element.ALIGN_LEFT);
			nameParagraph.add(Chunk.NEWLINE);
			nameParagraph.add(Chunk.NEWLINE);
			merchantDetail.addCell(nameParagraph);

			Image img = null;
			if (StringUtils.isNotBlank(merchantImageAddr)) {

				File file = new File(merchantImageAddr);

				if (file.exists()) {
					img = Image.getInstance(merchantImageAddr);
					img.scaleToFit(100f, 30f);
					img.setAbsolutePosition(20f, 700f);
					logoParagraph = new Paragraph();
					logoParagraph.add(Chunk.NEWLINE);
					logoParagraph.add(img);
				} else {

					img = Image.getInstance(PropertiesManager.propertiesMap.get("noLogoMerchant"));
					img.scaleToFit(100f, 30f);
					img.setAbsolutePosition(20f, 500f);
					logoParagraph = new Paragraph();
					logoParagraph.add(Chunk.NEWLINE);
					logoParagraph.add(img);
				}

			}
			currency = propertiesManager.getAlphabaticCurrencyCode(currency);
			PdfPCell mercLogo = new PdfPCell(img);
			mercLogo.setBorderWidth(0f);
			mercLogo.setFixedHeight(20f);
			merchantDetail.addCell(new PdfPCell(mercLogo)).setHorizontalAlignment(Element.ALIGN_RIGHT);
			document.add(merchantDetail);

			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);

			LineSeparator ls = new LineSeparator();
			ls.setPercentage(90f);
			document.add(new Chunk(ls));

			PdfPTable dataTable = new PdfPTable(4);
			dataTable.getDefaultCell().setBorder(0);
			float[] columnWidths = new float[] { 15f, 47f, 14f, 14f };
			dataTable.setWidths(columnWidths);
			dataTable.setWidthPercentage(90f);

			//logger.info("merchant details");

			custDetailsParagraph = new Paragraph(Chunk.NEWLINE + "Settled Amount: ", smallFont);
			custDetailsParagraph.setAlignment(Paragraph.ALIGN_LEFT);
			dataTable.addCell(custDetailsParagraph);

			if (cc || dc) {
				custDetailsParagraph = new Paragraph(Chunk.NEWLINE + currency + " " + settlementAmount.toString(), smallFont);
			} else {
				custDetailsParagraph = new Paragraph(Chunk.NEWLINE + "INR " + settlementAmount.toString(), smallFont);
			}
			custDetailsParagraph.setAlignment(Paragraph.ALIGN_LEFT);
			dataTable.addCell(custDetailsParagraph);

			custDetailsParagraph = new Paragraph(Chunk.NEWLINE + "Settlement Date: ", smallFont);
			custDetailsParagraph.setAlignment(Paragraph.ALIGN_RIGHT);
			dataTable.addCell(custDetailsParagraph);

			custDetailsParagraph = new Paragraph(Chunk.NEWLINE + payoutDateshow, smallFont);
			custDetailsParagraph.setAlignment(Paragraph.ALIGN_RIGHT);
			dataTable.addCell(custDetailsParagraph);

			custDetailsParagraph = new Paragraph("Account No.: ", smallFont);
			custDetailsParagraph.setAlignment(Paragraph.ALIGN_LEFT);
			dataTable.addCell(custDetailsParagraph);

			custDetailsParagraph = new Paragraph(account, smallFont);
			custDetailsParagraph.setAlignment(Paragraph.ALIGN_LEFT);
			dataTable.addCell(custDetailsParagraph);

			custDetailsParagraph = new Paragraph("Captured From: ", smallFont);
			custDetailsParagraph.setAlignment(Paragraph.ALIGN_RIGHT);
			dataTable.addCell(custDetailsParagraph);

			custDetailsParagraph = new Paragraph(fromdate, smallFont);
			custDetailsParagraph.setAlignment(Paragraph.ALIGN_RIGHT);
			dataTable.addCell(custDetailsParagraph);

			custDetailsParagraph = new Paragraph("UTR: ", smallFont);
			custDetailsParagraph.setAlignment(Paragraph.ALIGN_LEFT);
			dataTable.addCell(custDetailsParagraph);

			custDetailsParagraph = new Paragraph(utrNo, smallFont);
			custDetailsParagraph.setAlignment(Paragraph.ALIGN_LEFT);
			dataTable.addCell(custDetailsParagraph);

			custDetailsParagraph = new Paragraph("Captured To: ", smallFont);
			custDetailsParagraph.setAlignment(Paragraph.ALIGN_RIGHT);
			dataTable.addCell(custDetailsParagraph);

			custDetailsParagraph = new Paragraph(todate, smallFont);
			custDetailsParagraph.setAlignment(Paragraph.ALIGN_RIGHT);
			dataTable.addCell(custDetailsParagraph);

			custDetailsParagraph = new Paragraph("Transfer Mode: ", smallFont);
			custDetailsParagraph.setAlignment(Paragraph.ALIGN_LEFT);
			dataTable.addCell(custDetailsParagraph);

			custDetailsParagraph = new Paragraph(transferMode, smallFont);
			custDetailsParagraph.setAlignment(Paragraph.ALIGN_LEFT);
			dataTable.addCell(custDetailsParagraph);

			custDetailsParagraph = new Paragraph("", smallFont);
			custDetailsParagraph.setAlignment(Paragraph.ALIGN_LEFT);
			dataTable.addCell(custDetailsParagraph);

			custDetailsParagraph = new Paragraph("", smallFont);
			custDetailsParagraph.setAlignment(Paragraph.ALIGN_LEFT);
			dataTable.addCell(custDetailsParagraph);

			document.add(dataTable);
			document.add(new Chunk(ls));
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);

			float[] columnWidths3 = new float[] { 82.3f, 8.7f };
			PdfPTable p3 = new PdfPTable(2);
			p3.setWidths(columnWidths3);
			p3.getDefaultCell().setBorderWidth(0f);
			p3.setWidthPercentage(90f);

			reporttypeParagraph = new Paragraph(Chunk.NEWLINE + "SALE SETTLED ", tableHeaderFont);
			reporttypeParagraph.setAlignment(Element.ALIGN_LEFT);
			p3.addCell(reporttypeParagraph);
			
			if (cc || dc) {
				reporttypeParagraph = new Paragraph(Chunk.NEWLINE + "(In " + currency + ")", tableHeaderFont);
			} else {
				reporttypeParagraph = new Paragraph(Chunk.NEWLINE + "(In INR) ", tableHeaderFont);
			}
			
			reporttypeParagraph.setAlignment(Element.ALIGN_RIGHT);
			p3.addCell(reporttypeParagraph);

			document.add(p3);
//			document.add(new Chunk(ls));
			//logger.info("sale table");

			float[] saleWidths = { 19F, 17F, 10F, 17F, 10F, 17F };

			PdfPTable saleTable = new PdfPTable(6);
			saleTable.setWidths(saleWidths);
			saleTable.setWidthPercentage(90f);

			PdfPCell salepayemntType = new PdfPCell(new Phrase("Payment Type", catFont));
			salepayemntType.setHorizontalAlignment(Element.ALIGN_CENTER);
			salepayemntType.setVerticalAlignment(Element.ALIGN_CENTER);
			salepayemntType.setFixedHeight(15f);
			salepayemntType.setBorderWidth(0f);
			salepayemntType.setBorderWidthBottom(1f);
			saleTable.addCell(salepayemntType);

			PdfPCell saletotalAmount = new PdfPCell(new Phrase("Total Amount", catFont));
			saletotalAmount.setHorizontalAlignment(Element.ALIGN_CENTER);
			saletotalAmount.setVerticalAlignment(Element.ALIGN_CENTER);
			saletotalAmount.setFixedHeight(15f);
			saletotalAmount.setBorderWidth(0f);
			saletotalAmount.setBorderWidthBottom(1f);
			saleTable.addCell(saletotalAmount);

			PdfPCell saletdr = new PdfPCell(new Phrase("TDR", catFont));
			saletdr.setHorizontalAlignment(Element.ALIGN_CENTER);
			saletdr.setVerticalAlignment(Element.ALIGN_CENTER);
			saletdr.setFixedHeight(15f);
			saletdr.setBorderWidth(0f);
			saletdr.setBorderWidthBottom(1f);
			saleTable.addCell(saletdr);

			PdfPCell salegst = new PdfPCell(new Phrase("GST @18 %", catFont));
			salegst.setHorizontalAlignment(Element.ALIGN_CENTER);
			salegst.setVerticalAlignment(Element.ALIGN_CENTER);
			salegst.setFixedHeight(15f);
			salegst.setBorderWidth(0f);
			salegst.setBorderWidthBottom(1f);
			saleTable.addCell(salegst);

			PdfPCell salesuf = new PdfPCell(new Phrase("SUF", catFont));
			salesuf.setHorizontalAlignment(Element.ALIGN_CENTER);
			salesuf.setVerticalAlignment(Element.ALIGN_CENTER);
			salesuf.setFixedHeight(15f);
			salesuf.setBorderWidth(0f);
			salesuf.setBorderWidthBottom(1f);
			saleTable.addCell(salesuf);

			PdfPCell salenetAmount = new PdfPCell(new Phrase("Net Amount", catFont));
			salenetAmount.setHorizontalAlignment(Element.ALIGN_CENTER);
			salenetAmount.setVerticalAlignment(Element.ALIGN_CENTER);
			salenetAmount.setFixedHeight(15f);
			salenetAmount.setBorderWidth(0f);
			salenetAmount.setBorderWidthBottom(1f);
			saleTable.addCell(salenetAmount);

			document.add(new Chunk(ls));
			PdfPCell saleTT;
			PdfPCell saleTA;
			PdfPCell saleTDR;
			PdfPCell saleGST;
			PdfPCell saleSUF;
			PdfPCell saleNA;

			if (cc) {
				saleTT = new PdfPCell(new Phrase("Credit Card", subCatFont));
				saleTT.setFixedHeight(17f);
				saleTT.setBorderWidth(0f);
				saleTT.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTT.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTT.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTT));

				saleTA = new PdfPCell(new Phrase(ccTotalAmount.toString(), subCatFont));
				saleTA.setFixedHeight(17f);
				saleTA.setBorderWidth(0f);
				saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTA));

				saleTDR = new PdfPCell(new Phrase(ccTdr.toString(), subCatFont));
				saleTDR.setFixedHeight(17f);
				saleTDR.setBorderWidth(0f);
				saleTDR.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTDR.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTDR));

				saleGST = new PdfPCell(new Phrase(ccGst.toString(), subCatFont));
				saleGST.setFixedHeight(17f);
				saleGST.setBorderWidth(0f);
				saleGST.setPaddingTop(Element.ALIGN_MIDDLE);
				saleGST.setVerticalAlignment(Element.ALIGN_CENTER);
				saleGST.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleGST));

				saleSUF = new PdfPCell(new Phrase(ccSuf.toString(), subCatFont));
				saleSUF.setFixedHeight(17f);
				saleSUF.setBorderWidth(0f);
				saleSUF.setPaddingTop(Element.ALIGN_MIDDLE);
				saleSUF.setVerticalAlignment(Element.ALIGN_CENTER);
				saleSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleSUF));

				saleNA = new PdfPCell(new Phrase(
						ccTotalAmount.subtract(ccTdr).subtract(ccGst).subtract(ccSuf).toString(), subCatFont));
				saleNA.setFixedHeight(17f);
				saleNA.setBorderWidth(0f);
				saleNA.setPaddingTop(Element.ALIGN_MIDDLE);
				saleNA.setVerticalAlignment(Element.ALIGN_CENTER);
				saleNA.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleNA));
			}

			if (dc) {
				saleTT = new PdfPCell(new Phrase("Debit Card", subCatFont));
				saleTT.setFixedHeight(17f);
				saleTT.setBorderWidth(0f);
				saleTT.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTT.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTT.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTT));

				saleTA = new PdfPCell(new Phrase(dcTotalAmount.toString(), subCatFont));
				saleTA.setFixedHeight(17f);
				saleTA.setBorderWidth(0f);
				saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTA));

				saleTDR = new PdfPCell(new Phrase(dcTdr.toString(), subCatFont));
				saleTDR.setFixedHeight(17f);
				saleTDR.setBorderWidth(0f);
				saleTDR.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTDR.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTDR));

				saleGST = new PdfPCell(new Phrase(dcGst.toString(), subCatFont));
				saleGST.setFixedHeight(17f);
				saleGST.setBorderWidth(0f);
				saleGST.setPaddingTop(Element.ALIGN_MIDDLE);
				saleGST.setVerticalAlignment(Element.ALIGN_CENTER);
				saleGST.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleGST));

				saleSUF = new PdfPCell(new Phrase(dcSuf.toString(), subCatFont));
				saleSUF.setFixedHeight(17f);
				saleSUF.setBorderWidth(0f);
				saleSUF.setPaddingTop(Element.ALIGN_MIDDLE);
				saleSUF.setVerticalAlignment(Element.ALIGN_CENTER);
				saleSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleSUF));

				saleTA = new PdfPCell(new Phrase(
						dcTotalAmount.subtract(dcTdr).subtract(dcGst).subtract(dcSuf).toString(), subCatFont));
				saleTA.setFixedHeight(17f);
				saleTA.setBorderWidth(0f);
				saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTA));
			}
			if (in) {
				saleTT = new PdfPCell(new Phrase("International", subCatFont));
				saleTT.setFixedHeight(17f);
				saleTT.setBorderWidth(0f);
				saleTT.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTT.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTT.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTT));

				saleTA = new PdfPCell(new Phrase(inTotalAmount.toString(), subCatFont));
				saleTA.setFixedHeight(17f);
				saleTA.setBorderWidth(0f);
				saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTA));

				saleTDR = new PdfPCell(new Phrase(inTdr.toString(), subCatFont));
				saleTDR.setFixedHeight(17f);
				saleTDR.setBorderWidth(0f);
				saleTDR.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTDR.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTDR));

				saleGST = new PdfPCell(new Phrase(inGst.toString(), subCatFont));
				saleGST.setFixedHeight(17f);
				saleGST.setBorderWidth(0f);
				saleGST.setPaddingTop(Element.ALIGN_MIDDLE);
				saleGST.setVerticalAlignment(Element.ALIGN_CENTER);
				saleGST.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleGST));

				saleSUF = new PdfPCell(new Phrase(inSuf.toString(), subCatFont));
				saleSUF.setFixedHeight(17f);
				saleSUF.setBorderWidth(0f);
				saleSUF.setPaddingTop(Element.ALIGN_MIDDLE);
				saleSUF.setVerticalAlignment(Element.ALIGN_CENTER);
				saleSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleSUF));

				saleTA = new PdfPCell(new Phrase(
						inTotalAmount.subtract(inTdr).subtract(inGst).subtract(inSuf).toString(), subCatFont));
				saleTA.setFixedHeight(17f);
				saleTA.setBorderWidth(0f);
				saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTA));
			}
			if (nb) {
				saleTT = new PdfPCell(new Phrase("Net Banking", subCatFont));
				saleTT.setFixedHeight(17f);
				saleTT.setBorderWidth(0f);
				saleTT.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTT.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTT.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTT));

				saleTA = new PdfPCell(new Phrase(nbTotalAmount.toString(), subCatFont));
				saleTA.setFixedHeight(17f);
				saleTA.setBorderWidth(0f);
				saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTA));

				saleTDR = new PdfPCell(new Phrase(nbTdr.toString(), subCatFont));
				saleTDR.setFixedHeight(17f);
				saleTDR.setBorderWidth(0f);
				saleTDR.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTDR.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTDR));

				saleGST = new PdfPCell(new Phrase(nbGst.toString(), subCatFont));
				saleGST.setFixedHeight(17f);
				saleGST.setBorderWidth(0f);
				saleGST.setPaddingTop(Element.ALIGN_MIDDLE);
				saleGST.setVerticalAlignment(Element.ALIGN_CENTER);
				saleGST.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleGST));

				saleSUF = new PdfPCell(new Phrase(nbSuf.toString(), subCatFont));
				saleSUF.setFixedHeight(17f);
				saleSUF.setBorderWidth(0f);
				saleSUF.setPaddingTop(Element.ALIGN_MIDDLE);
				saleSUF.setVerticalAlignment(Element.ALIGN_CENTER);
				saleSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleSUF));

				saleTA = new PdfPCell(new Phrase(
						nbTotalAmount.subtract(nbTdr).subtract(nbGst).subtract(nbSuf).toString(), subCatFont));
				saleTA.setFixedHeight(17f);
				saleTA.setBorderWidth(0f);
				saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTA));
			}
			if (up) {
				saleTT = new PdfPCell(new Phrase("UPI", subCatFont));
				saleTT.setFixedHeight(17f);
				saleTT.setBorderWidth(0f);
				saleTT.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTT.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTT.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTT));

				saleTA = new PdfPCell(new Phrase(upTotalAmount.toString(), subCatFont));
				saleTA.setFixedHeight(17f);
				saleTA.setBorderWidth(0f);
				saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTA));

				saleTDR = new PdfPCell(new Phrase(upTdr.toString(), subCatFont));
				saleTDR.setFixedHeight(17f);
				saleTDR.setBorderWidth(0f);
				saleTDR.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTDR.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTDR));

				saleGST = new PdfPCell(new Phrase(upGst.toString(), subCatFont));
				saleGST.setFixedHeight(17f);
				saleGST.setBorderWidth(0f);
				saleGST.setPaddingTop(Element.ALIGN_MIDDLE);
				saleGST.setVerticalAlignment(Element.ALIGN_CENTER);
				saleGST.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleGST));

				saleSUF = new PdfPCell(new Phrase(upSuf.toString(), subCatFont));
				saleSUF.setFixedHeight(17f);
				saleSUF.setBorderWidth(0f);
				saleSUF.setPaddingTop(Element.ALIGN_MIDDLE);
				saleSUF.setVerticalAlignment(Element.ALIGN_CENTER);
				saleSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleSUF));

				saleTA = new PdfPCell(new Phrase(
						upTotalAmount.subtract(upTdr).subtract(upGst).subtract(upSuf).toString(), subCatFont));
				saleTA.setFixedHeight(17f);
				saleTA.setBorderWidth(0f);
				saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTA));
			}
			if (wl) {
				saleTT = new PdfPCell(new Phrase("Wallet", subCatFont));
				saleTT.setFixedHeight(17f);
				saleTT.setBorderWidth(0f);
				saleTT.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTT.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTT.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTT));

				saleTA = new PdfPCell(new Phrase(wlTotalAmount.toString(), subCatFont));
				saleTA.setFixedHeight(17f);
				saleTA.setBorderWidth(0f);
				saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTA));

				saleTDR = new PdfPCell(new Phrase(wlTdr.toString(), subCatFont));
				saleTDR.setFixedHeight(17f);
				saleTDR.setBorderWidth(0f);
				saleTDR.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTDR.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTDR));

				saleGST = new PdfPCell(new Phrase(wlGst.toString(), subCatFont));
				saleGST.setFixedHeight(17f);
				saleGST.setBorderWidth(0f);
				saleGST.setPaddingTop(Element.ALIGN_MIDDLE);
				saleGST.setVerticalAlignment(Element.ALIGN_CENTER);
				saleGST.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleGST));

				saleSUF = new PdfPCell(new Phrase(wlSuf.toString(), subCatFont));
				saleSUF.setFixedHeight(17f);
				saleSUF.setBorderWidth(0f);
				saleSUF.setPaddingTop(Element.ALIGN_MIDDLE);
				saleSUF.setVerticalAlignment(Element.ALIGN_CENTER);
				saleSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleSUF));

				saleTA = new PdfPCell(new Phrase(
						wlTotalAmount.subtract(wlTdr).subtract(wlGst).subtract(wlSuf).toString(), subCatFont));
				saleTA.setFixedHeight(17f);
				saleTA.setBorderWidth(0f);
				saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTA));
			}
			if (imps) {
				saleTT = new PdfPCell(new Phrase("IMPS", subCatFont));
				saleTT.setFixedHeight(17f);
				saleTT.setBorderWidth(0f);
				saleTT.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTT.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTT.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTT));

				saleTA = new PdfPCell(new Phrase(impsTotalAmount.toString(), subCatFont));
				saleTA.setFixedHeight(17f);
				saleTA.setBorderWidth(0f);
				saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTA));

				saleTDR = new PdfPCell(new Phrase(impsTdr.toString(), subCatFont));
				saleTDR.setFixedHeight(17f);
				saleTDR.setBorderWidth(0f);
				saleTDR.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTDR.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTDR));

				saleGST = new PdfPCell(new Phrase(impsGst.toString(), subCatFont));
				saleGST.setFixedHeight(17f);
				saleGST.setBorderWidth(0f);
				saleGST.setPaddingTop(Element.ALIGN_MIDDLE);
				saleGST.setVerticalAlignment(Element.ALIGN_CENTER);
				saleGST.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleGST));

				saleSUF = new PdfPCell(new Phrase(impsSuf.toString(), subCatFont));
				saleSUF.setFixedHeight(17f);
				saleSUF.setBorderWidth(0f);
				saleSUF.setPaddingTop(Element.ALIGN_MIDDLE);
				saleSUF.setVerticalAlignment(Element.ALIGN_CENTER);
				saleSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleSUF));

				saleTA = new PdfPCell(new Phrase(
						impsTotalAmount.subtract(impsTdr).subtract(impsGst).subtract(impsSuf).toString(), subCatFont));
				saleTA.setFixedHeight(17f);
				saleTA.setBorderWidth(0f);
				saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTA));
			}
			if (neft) {
				saleTT = new PdfPCell(new Phrase("NEFT", subCatFont));
				saleTT.setFixedHeight(17f);
				saleTT.setBorderWidth(0f);
				saleTT.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTT.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTT.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTT));

				saleTA = new PdfPCell(new Phrase(neftTotalAmount.toString(), subCatFont));
				saleTA.setFixedHeight(17f);
				saleTA.setBorderWidth(0f);
				saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTA));

				saleTDR = new PdfPCell(new Phrase(neftTdr.toString(), subCatFont));
				saleTDR.setFixedHeight(17f);
				saleTDR.setBorderWidth(0f);
				saleTDR.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTDR.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTDR));

				saleGST = new PdfPCell(new Phrase(neftGst.toString(), subCatFont));
				saleGST.setFixedHeight(17f);
				saleGST.setBorderWidth(0f);
				saleGST.setPaddingTop(Element.ALIGN_MIDDLE);
				saleGST.setVerticalAlignment(Element.ALIGN_CENTER);
				saleGST.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleGST));

				saleSUF = new PdfPCell(new Phrase(neftSuf.toString(), subCatFont));
				saleSUF.setFixedHeight(17f);
				saleSUF.setBorderWidth(0f);
				saleSUF.setPaddingTop(Element.ALIGN_MIDDLE);
				saleSUF.setVerticalAlignment(Element.ALIGN_CENTER);
				saleSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleSUF));

				saleTA = new PdfPCell(new Phrase(
						neftTotalAmount.subtract(neftTdr).subtract(neftGst).subtract(neftSuf).toString(), subCatFont));
				saleTA.setFixedHeight(17f);
				saleTA.setBorderWidth(0f);
				saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTA));
			}
			if (rtgs) {
				saleTT = new PdfPCell(new Phrase("RTGS", subCatFont));
				saleTT.setFixedHeight(17f);
				saleTT.setBorderWidth(0f);
				saleTT.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTT.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTT.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTT));

				saleTA = new PdfPCell(new Phrase(rtgsTotalAmount.toString(), subCatFont));
				saleTA.setFixedHeight(17f);
				saleTA.setBorderWidth(0f);
				saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTA));

				saleTDR = new PdfPCell(new Phrase(rtgsTdr.toString(), subCatFont));
				saleTDR.setFixedHeight(17f);
				saleTDR.setBorderWidth(0f);
				saleTDR.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTDR.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTDR));

				saleGST = new PdfPCell(new Phrase(rtgsGst.toString(), subCatFont));
				saleGST.setFixedHeight(17f);
				saleGST.setBorderWidth(0f);
				saleGST.setPaddingTop(Element.ALIGN_MIDDLE);
				saleGST.setVerticalAlignment(Element.ALIGN_CENTER);
				saleGST.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleGST));

				saleSUF = new PdfPCell(new Phrase(rtgsSuf.toString(), subCatFont));
				saleSUF.setFixedHeight(17f);
				saleSUF.setBorderWidth(0f);
				saleSUF.setPaddingTop(Element.ALIGN_MIDDLE);
				saleSUF.setVerticalAlignment(Element.ALIGN_CENTER);
				saleSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleSUF));

				saleTA = new PdfPCell(new Phrase(
						rtgsTotalAmount.subtract(rtgsTdr).subtract(rtgsGst).subtract(rtgsSuf).toString(), subCatFont));
				saleTA.setFixedHeight(17f);
				saleTA.setBorderWidth(0f);
				saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTA));
			}
			if (cod) {
				saleTT = new PdfPCell(new Phrase("COD", subCatFont));
				saleTT.setFixedHeight(17f);
				saleTT.setBorderWidth(0f);
				saleTT.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTT.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTT.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTT));

				saleTA = new PdfPCell(new Phrase(codTotalAmount.toString(), subCatFont));
				saleTA.setFixedHeight(17f);
				saleTA.setBorderWidth(0f);
				saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTA));

				saleTDR = new PdfPCell(new Phrase(codTdr.toString(), subCatFont));
				saleTDR.setFixedHeight(17f);
				saleTDR.setBorderWidth(0f);
				saleTDR.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTDR.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTDR));

				saleGST = new PdfPCell(new Phrase(codGst.toString(), subCatFont));
				saleGST.setFixedHeight(17f);
				saleGST.setBorderWidth(0f);
				saleGST.setPaddingTop(Element.ALIGN_MIDDLE);
				saleGST.setVerticalAlignment(Element.ALIGN_CENTER);
				saleGST.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleGST));

				saleSUF = new PdfPCell(new Phrase(codSuf.toString(), subCatFont));
				saleSUF.setFixedHeight(17f);
				saleSUF.setBorderWidth(0f);
				saleSUF.setPaddingTop(Element.ALIGN_MIDDLE);
				saleSUF.setVerticalAlignment(Element.ALIGN_CENTER);
				saleSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleSUF));

				saleTA = new PdfPCell(
						new Phrase(new BigDecimal("0.00").subtract(codTdr).subtract(codGst).subtract(codSuf).toString(),
								subCatFont));
				saleTA.setFixedHeight(17f);
				saleTA.setBorderWidth(0f);
				saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTA));
			}

			if (!cc && !dc && !in && !nb && !up && !cod && !neft && !rtgs && !imps && !wl) {

				saleTDR = new PdfPCell(new Phrase("NO DATA FOUND FOR SALE SETTLED", subCatFont));
				saleTDR.setFixedHeight(20f);
				saleTDR.setColspan(6);
				saleTDR.setBorderWidth(0f);
				saleTDR.setPaddingTop(Element.ALIGN_MIDDLE);
				saleTDR.setVerticalAlignment(Element.ALIGN_CENTER);
				saleTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
				saleTable.addCell(new PdfPCell(saleTDR));

			}
			saleTT = new PdfPCell(new Phrase("Total", totalFont));
			BaseColor myColorpan = WebColors.getRGBColor("#002664");
			saleTT.setBackgroundColor(myColorpan);
			saleTT.setFixedHeight(20f);
			saleTT.setBorderWidth(0f);
			saleTT.setBorderWidthTop(1f);
			saleTT.setPaddingTop(Element.ALIGN_MIDDLE);
			saleTT.setVerticalAlignment(Element.ALIGN_CENTER);
			saleTT.setHorizontalAlignment(Element.ALIGN_CENTER);
			saleTable.addCell(new PdfPCell(saleTT));

			saleTA = new PdfPCell(new Phrase(totalAmount.toString(), subCatFont));
			saleTA.setFixedHeight(20f);
			saleTA.setBorderWidth(0f);
			saleTA.setBorderWidthTop(1f);
			saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
			saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
			saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
			saleTable.addCell(new PdfPCell(saleTA));

			saleTDR = new PdfPCell(new Phrase(totalTdr.toString(), subCatFont));
			saleTDR.setFixedHeight(20f);
			saleTDR.setBorderWidth(0f);
			saleTDR.setBorderWidthTop(1f);
			saleTDR.setPaddingTop(Element.ALIGN_MIDDLE);
			saleTDR.setVerticalAlignment(Element.ALIGN_CENTER);
			saleTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
			saleTable.addCell(new PdfPCell(saleTDR));

			saleGST = new PdfPCell(new Phrase(totalGst.toString(), subCatFont));
			saleGST.setFixedHeight(20f);
			saleGST.setBorderWidth(0f);
			saleGST.setBorderWidthTop(1f);
			saleGST.setPaddingTop(Element.ALIGN_MIDDLE);
			saleGST.setVerticalAlignment(Element.ALIGN_CENTER);
			saleGST.setHorizontalAlignment(Element.ALIGN_CENTER);
			saleTable.addCell(new PdfPCell(saleGST));

			saleSUF = new PdfPCell(new Phrase(totalSuf.toString(), subCatFont));
			saleSUF.setFixedHeight(20f);
			saleSUF.setBorderWidth(0f);
			saleSUF.setBorderWidthTop(1f);
			saleSUF.setPaddingTop(Element.ALIGN_MIDDLE);
			saleSUF.setVerticalAlignment(Element.ALIGN_CENTER);
			saleSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
			saleTable.addCell(new PdfPCell(saleSUF));

			saleAmount = totalAmount.subtract(totalTdr).subtract(totalGst).subtract(totalSuf).subtract(codTotalAmount);

			saleTA = new PdfPCell(new Phrase(saleAmount.toString(), subCatFont));
			saleTA.setFixedHeight(20f);
			saleTA.setBorderWidth(0f);
			saleTA.setBorderWidthTop(1f);
			saleTA.setPaddingTop(Element.ALIGN_MIDDLE);
			saleTA.setVerticalAlignment(Element.ALIGN_CENTER);
			saleTA.setHorizontalAlignment(Element.ALIGN_CENTER);
			saleTable.addCell(new PdfPCell(saleTA));

			document.add(saleTable);
			//logger.info("outside refund table");

			float[] refund = new float[] { 82.3f, 8.7f };
			PdfPTable refundsettle = new PdfPTable(2);
			refundsettle.setWidths(refund);
			refundsettle.getDefaultCell().setBorderWidth(0f);
			refundsettle.setWidthPercentage(90f);

			reporttypeParagraph = new Paragraph(Chunk.NEWLINE + "REFUND SETTLED ", tableHeaderFont);
			reporttypeParagraph.setAlignment(Element.ALIGN_LEFT);
			refundsettle.addCell(reporttypeParagraph);
			
			if (refundcc || refunddc) {
				reporttypeParagraph = new Paragraph(Chunk.NEWLINE + "(In " + currency + ")", tableHeaderFont);
			} else {
				reporttypeParagraph = new Paragraph(Chunk.NEWLINE + "(In INR) ", tableHeaderFont);
			}
			reporttypeParagraph.setAlignment(Element.ALIGN_RIGHT);
			refundsettle.addCell(reporttypeParagraph);

			document.add(refundsettle);

			float[] refundColumnWidths = { 19F, 17F, 10F, 17F, 10F, 17F };

			PdfPTable refundTable = new PdfPTable(6);
			refundTable.setWidths(refundColumnWidths);
			refundTable.setWidthPercentage(90f);

			PdfPCell refundpayemntType = new PdfPCell(new Phrase("Payment Type", catFont));
			refundpayemntType.setHorizontalAlignment(Element.ALIGN_CENTER);
			refundpayemntType.setVerticalAlignment(Element.ALIGN_CENTER);
			refundpayemntType.setFixedHeight(17f);
			refundpayemntType.setBorderWidth(0f);
			refundpayemntType.setBorderWidthBottom(1f);
			refundTable.addCell(refundpayemntType);

			PdfPCell refundTotalAmount = new PdfPCell(new Phrase("Total Amount", catFont));
			refundTotalAmount.setHorizontalAlignment(Element.ALIGN_CENTER);
			refundTotalAmount.setVerticalAlignment(Element.ALIGN_CENTER);
			refundTotalAmount.setFixedHeight(17f);
			refundTotalAmount.setBorderWidth(0f);
			refundTotalAmount.setBorderWidthBottom(1f);
			refundTable.addCell(refundTotalAmount);

			PdfPCell refundtdr = new PdfPCell(new Phrase("TDR", catFont));
			refundtdr.setHorizontalAlignment(Element.ALIGN_CENTER);
			refundtdr.setVerticalAlignment(Element.ALIGN_CENTER);
			refundtdr.setFixedHeight(17f);
			refundtdr.setBorderWidth(0f);
			refundtdr.setBorderWidthBottom(1f);
			refundTable.addCell(refundtdr);

			PdfPCell refundgst = new PdfPCell(new Phrase("GST @18 %", catFont));
			refundgst.setHorizontalAlignment(Element.ALIGN_CENTER);
			refundgst.setVerticalAlignment(Element.ALIGN_CENTER);
			refundgst.setFixedHeight(17f);
			refundgst.setBorderWidth(0f);
			refundgst.setBorderWidthBottom(1f);
			refundTable.addCell(refundgst);

			PdfPCell refundsuf = new PdfPCell(new Phrase("SUF", catFont));
			refundsuf.setHorizontalAlignment(Element.ALIGN_CENTER);
			refundsuf.setVerticalAlignment(Element.ALIGN_CENTER);
			refundsuf.setFixedHeight(17f);
			refundsuf.setBorderWidth(0f);
			refundsuf.setBorderWidthBottom(1f);
			refundTable.addCell(refundsuf);

			PdfPCell refundnetAmount = new PdfPCell(new Phrase("Net Amount", catFont));
			refundnetAmount.setHorizontalAlignment(Element.ALIGN_CENTER);
			refundnetAmount.setVerticalAlignment(Element.ALIGN_CENTER);
			refundnetAmount.setFixedHeight(17f);
			refundnetAmount.setBorderWidth(0f);
			refundnetAmount.setBorderWidthBottom(1f);
			refundTable.addCell(refundnetAmount);

			document.add(new Chunk(ls));

			PdfPCell refundTT;
			PdfPCell refundTA;
			PdfPCell refundTDR;
			PdfPCell refundGST;
			PdfPCell refundSUF;
			PdfPCell refundNA;
			//logger.info("inside refund table");

			if (refundcc) {
				refundTT = new PdfPCell(new Phrase("Credit Card", subCatFont));
				refundTT.setFixedHeight(17f);
				refundTT.setBorderWidth(0f);
				refundTT.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTT.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTT.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTT));

				refundTA = new PdfPCell(new Phrase(refundccTotalAmount.toString(), subCatFont));
				refundTA.setFixedHeight(17f);
				refundTA.setBorderWidth(0f);
				refundTA.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTA.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTA));

				refundTDR = new PdfPCell(new Phrase(refundccTdr.toString(), subCatFont));
				refundTDR.setFixedHeight(17f);
				refundTDR.setBorderWidth(0f);
				refundTDR.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTDR.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTDR));

				refundGST = new PdfPCell(new Phrase(refundccGst.toString(), subCatFont));
				refundGST.setFixedHeight(17f);
				refundGST.setBorderWidth(0f);
				refundGST.setPaddingTop(Element.ALIGN_MIDDLE);
				refundGST.setVerticalAlignment(Element.ALIGN_CENTER);
				refundGST.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundGST));

				refundSUF = new PdfPCell(new Phrase(refundccSuf.toString(), subCatFont));
				refundSUF.setFixedHeight(17f);
				refundSUF.setBorderWidth(0f);
				refundSUF.setPaddingTop(Element.ALIGN_MIDDLE);
				refundSUF.setVerticalAlignment(Element.ALIGN_CENTER);
				refundSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundSUF));

				refundNA = new PdfPCell(new Phrase(refundccTotalAmount.subtract(refundccTdr).subtract(refundccGst)
						.subtract(refundccSuf).toString(), subCatFont));
				refundNA.setFixedHeight(17f);
				refundNA.setBorderWidth(0f);
				refundNA.setPaddingTop(Element.ALIGN_MIDDLE);
				refundNA.setVerticalAlignment(Element.ALIGN_CENTER);
				refundNA.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundNA));
			}
			if (refunddc) {
				refundTT = new PdfPCell(new Phrase("Debit Card", subCatFont));
				refundTT.setFixedHeight(17f);
				refundTT.setBorderWidth(0f);
				refundTT.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTT.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTT.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTT));

				refundTA = new PdfPCell(new Phrase(refunddcTotalAmount.toString(), subCatFont));
				refundTA.setFixedHeight(17f);
				refundTA.setBorderWidth(0f);
				refundTA.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTA.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTA));

				refundTDR = new PdfPCell(new Phrase(refunddcTdr.toString(), subCatFont));
				refundTDR.setFixedHeight(17f);
				refundTDR.setBorderWidth(0f);
				refundTDR.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTDR.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTDR));

				refundGST = new PdfPCell(new Phrase(refunddcGst.toString(), subCatFont));
				refundGST.setFixedHeight(17f);
				refundGST.setBorderWidth(0f);
				refundGST.setPaddingTop(Element.ALIGN_MIDDLE);
				refundGST.setVerticalAlignment(Element.ALIGN_CENTER);
				refundGST.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundGST));

				refundSUF = new PdfPCell(new Phrase(refunddcSuf.toString(), subCatFont));
				refundSUF.setFixedHeight(17f);
				refundSUF.setBorderWidth(0f);
				refundSUF.setPaddingTop(Element.ALIGN_MIDDLE);
				refundSUF.setVerticalAlignment(Element.ALIGN_CENTER);
				refundSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundSUF));

				refundNA = new PdfPCell(new Phrase(refunddcTotalAmount.subtract(refunddcTdr).subtract(refunddcGst)
						.subtract(refunddcSuf).toString(), subCatFont));
				refundNA.setFixedHeight(17f);
				refundNA.setBorderWidth(0f);
				refundNA.setPaddingTop(Element.ALIGN_MIDDLE);
				refundNA.setVerticalAlignment(Element.ALIGN_CENTER);
				refundNA.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundNA));
			}
			if (refundin) {
				refundTT = new PdfPCell(new Phrase("International", subCatFont));
				refundTT.setFixedHeight(17f);
				refundTT.setBorderWidth(0f);
				refundTT.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTT.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTT.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTT));

				refundTA = new PdfPCell(new Phrase(refundinTotalAmount.toString(), subCatFont));
				refundTA.setFixedHeight(17f);
				refundTA.setBorderWidth(0f);
				refundTA.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTA.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTA));

				refundTDR = new PdfPCell(new Phrase(refundinTdr.toString(), subCatFont));
				refundTDR.setFixedHeight(17f);
				refundTDR.setBorderWidth(0f);
				refundTDR.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTDR.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTDR));

				refundGST = new PdfPCell(new Phrase(refundinGst.toString(), subCatFont));
				refundGST.setFixedHeight(17f);
				refundGST.setBorderWidth(0f);
				refundGST.setPaddingTop(Element.ALIGN_MIDDLE);
				refundGST.setVerticalAlignment(Element.ALIGN_CENTER);
				refundGST.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundGST));

				refundSUF = new PdfPCell(new Phrase(refundinSuf.toString(), subCatFont));
				refundSUF.setFixedHeight(17f);
				refundSUF.setBorderWidth(0f);
				refundSUF.setPaddingTop(Element.ALIGN_MIDDLE);
				refundSUF.setVerticalAlignment(Element.ALIGN_CENTER);
				refundSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundSUF));

				refundNA = new PdfPCell(new Phrase(refundinTotalAmount.subtract(refundinTdr).subtract(refundinGst)
						.subtract(refundinSuf).toString(), subCatFont));
				refundNA.setFixedHeight(17f);
				refundNA.setBorderWidth(0f);
				refundNA.setPaddingTop(Element.ALIGN_MIDDLE);
				refundNA.setVerticalAlignment(Element.ALIGN_CENTER);
				refundNA.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundNA));
			}
			if (refundnb) {
				refundTT = new PdfPCell(new Phrase("Net Banking", subCatFont));
				refundTT.setFixedHeight(17f);
				refundTT.setBorderWidth(0f);
				refundTT.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTT.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTT.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTT));

				refundTA = new PdfPCell(new Phrase(refundnbTotalAmount.toString(), subCatFont));
				refundTA.setFixedHeight(17f);
				refundTA.setBorderWidth(0f);
				refundTA.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTA.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTA));

				refundTDR = new PdfPCell(new Phrase(refundnbTdr.toString(), subCatFont));
				refundTDR.setFixedHeight(17f);
				refundTDR.setBorderWidth(0f);
				refundTDR.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTDR.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTDR));

				refundGST = new PdfPCell(new Phrase(refundnbGst.toString(), subCatFont));
				refundGST.setFixedHeight(17f);
				refundGST.setBorderWidth(0f);
				refundGST.setPaddingTop(Element.ALIGN_MIDDLE);
				refundGST.setVerticalAlignment(Element.ALIGN_CENTER);
				refundGST.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundGST));

				refundSUF = new PdfPCell(new Phrase(refundnbSuf.toString(), subCatFont));
				refundSUF.setFixedHeight(17f);
				refundSUF.setBorderWidth(0f);
				refundSUF.setPaddingTop(Element.ALIGN_MIDDLE);
				refundSUF.setVerticalAlignment(Element.ALIGN_CENTER);
				refundSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundSUF));

				refundNA = new PdfPCell(new Phrase(refundnbTotalAmount.subtract(refundnbTdr).subtract(refundnbGst)
						.subtract(refundnbSuf).toString(), subCatFont));
				refundNA.setFixedHeight(17f);
				refundNA.setBorderWidth(0f);
				refundNA.setPaddingTop(Element.ALIGN_MIDDLE);
				refundNA.setVerticalAlignment(Element.ALIGN_CENTER);
				refundNA.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundNA));
			}
			if (refundup) {
				refundTT = new PdfPCell(new Phrase("UPI", subCatFont));
				refundTT.setFixedHeight(17f);
				refundTT.setBorderWidth(0f);
				refundTT.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTT.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTT.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTT));

				refundTA = new PdfPCell(new Phrase(refundupTotalAmount.toString(), subCatFont));
				refundTA.setFixedHeight(17f);
				refundTA.setBorderWidth(0f);
				refundTA.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTA.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTA));

				refundTDR = new PdfPCell(new Phrase(refundupTdr.toString(), subCatFont));
				refundTDR.setFixedHeight(17f);
				refundTDR.setBorderWidth(0f);
				refundTDR.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTDR.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTDR));

				refundGST = new PdfPCell(new Phrase(refundupGst.toString(), subCatFont));
				refundGST.setFixedHeight(17f);
				refundGST.setBorderWidth(0f);
				refundGST.setPaddingTop(Element.ALIGN_MIDDLE);
				refundGST.setVerticalAlignment(Element.ALIGN_CENTER);
				refundGST.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundGST));

				refundSUF = new PdfPCell(new Phrase(refundupSuf.toString(), subCatFont));
				refundSUF.setFixedHeight(17f);
				refundSUF.setBorderWidth(0f);
				refundSUF.setPaddingTop(Element.ALIGN_MIDDLE);
				refundSUF.setVerticalAlignment(Element.ALIGN_CENTER);
				refundSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundSUF));

				refundNA = new PdfPCell(new Phrase(refundupTotalAmount.subtract(refundupTdr).subtract(refundupGst)
						.subtract(refundupSuf).toString(), subCatFont));
				refundNA.setFixedHeight(17f);
				refundNA.setBorderWidth(0f);
				refundNA.setPaddingTop(Element.ALIGN_MIDDLE);
				refundNA.setVerticalAlignment(Element.ALIGN_CENTER);
				refundNA.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundNA));
			}
			if (refundwl) {
				refundTT = new PdfPCell(new Phrase("Wallet", subCatFont));
				refundTT.setFixedHeight(17f);
				refundTT.setBorderWidth(0f);
				refundTT.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTT.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTT.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTT));

				refundTA = new PdfPCell(new Phrase(refundwlTotalAmount.toString(), subCatFont));
				refundTA.setFixedHeight(17f);
				refundTA.setBorderWidth(0f);
				refundTA.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTA.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTA));

				refundTDR = new PdfPCell(new Phrase(refundwlTdr.toString(), subCatFont));
				refundTDR.setFixedHeight(17f);
				refundTDR.setBorderWidth(0f);
				refundTDR.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTDR.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTDR));

				refundGST = new PdfPCell(new Phrase(refundwlGst.toString(), subCatFont));
				refundGST.setFixedHeight(17f);
				refundGST.setBorderWidth(0f);
				refundGST.setPaddingTop(Element.ALIGN_MIDDLE);
				refundGST.setVerticalAlignment(Element.ALIGN_CENTER);
				refundGST.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundGST));

				refundSUF = new PdfPCell(new Phrase(refundwlSuf.toString(), subCatFont));
				refundSUF.setFixedHeight(17f);
				refundSUF.setBorderWidth(0f);
				refundSUF.setPaddingTop(Element.ALIGN_MIDDLE);
				refundSUF.setVerticalAlignment(Element.ALIGN_CENTER);
				refundSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundSUF));

				refundNA = new PdfPCell(new Phrase(refundwlTotalAmount.subtract(refundwlTdr).subtract(refundwlGst)
						.subtract(refundwlSuf).toString(), subCatFont));
				refundNA.setFixedHeight(17f);
				refundNA.setBorderWidth(0f);
				refundNA.setPaddingTop(Element.ALIGN_MIDDLE);
				refundNA.setVerticalAlignment(Element.ALIGN_CENTER);
				refundNA.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundNA));
			}
			if (refundimps) {
				refundTT = new PdfPCell(new Phrase("IMPS", subCatFont));
				refundTT.setFixedHeight(17f);
				refundTT.setBorderWidth(0f);
				refundTT.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTT.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTT.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTT));

				refundTA = new PdfPCell(new Phrase(refundimpsTotalAmount.toString(), subCatFont));
				refundTA.setFixedHeight(17f);
				refundTA.setBorderWidth(0f);
				refundTA.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTA.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTA));

				refundTDR = new PdfPCell(new Phrase(refundimpsTdr.toString(), subCatFont));
				refundTDR.setFixedHeight(17f);
				refundTDR.setBorderWidth(0f);
				refundTDR.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTDR.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTDR));

				refundGST = new PdfPCell(new Phrase(refundimpsGst.toString(), subCatFont));
				refundGST.setFixedHeight(17f);
				refundGST.setBorderWidth(0f);
				refundGST.setPaddingTop(Element.ALIGN_MIDDLE);
				refundGST.setVerticalAlignment(Element.ALIGN_CENTER);
				refundGST.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundGST));

				refundSUF = new PdfPCell(new Phrase(refundimpsSuf.toString(), subCatFont));
				refundSUF.setFixedHeight(17f);
				refundSUF.setBorderWidth(0f);
				refundSUF.setPaddingTop(Element.ALIGN_MIDDLE);
				refundSUF.setVerticalAlignment(Element.ALIGN_CENTER);
				refundSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundSUF));

				refundNA = new PdfPCell(new Phrase(refundimpsTotalAmount.subtract(refundimpsTdr).subtract(refundimpsGst)
						.subtract(refundimpsSuf).toString(), subCatFont));
				refundNA.setFixedHeight(17f);
				refundNA.setBorderWidth(0f);
				refundNA.setPaddingTop(Element.ALIGN_MIDDLE);
				refundNA.setVerticalAlignment(Element.ALIGN_CENTER);
				refundNA.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundNA));
			}
			if (refundneft) {
				refundTT = new PdfPCell(new Phrase("NEFT", subCatFont));
				refundTT.setFixedHeight(17f);
				refundTT.setBorderWidth(0f);
				refundTT.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTT.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTT.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTT));

				refundTA = new PdfPCell(new Phrase(refundneftTotalAmount.toString(), subCatFont));
				refundTA.setFixedHeight(17f);
				refundTA.setBorderWidth(0f);
				refundTA.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTA.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTA));

				refundTDR = new PdfPCell(new Phrase(refundneftTdr.toString(), subCatFont));
				refundTDR.setFixedHeight(17f);
				refundTDR.setBorderWidth(0f);
				refundTDR.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTDR.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTDR));

				refundGST = new PdfPCell(new Phrase(refundneftGst.toString(), subCatFont));
				refundGST.setFixedHeight(17f);
				refundGST.setBorderWidth(0f);
				refundGST.setPaddingTop(Element.ALIGN_MIDDLE);
				refundGST.setVerticalAlignment(Element.ALIGN_CENTER);
				refundGST.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundGST));

				refundSUF = new PdfPCell(new Phrase(refundneftSuf.toString(), subCatFont));
				refundSUF.setFixedHeight(17f);
				refundSUF.setBorderWidth(0f);
				refundSUF.setPaddingTop(Element.ALIGN_MIDDLE);
				refundSUF.setVerticalAlignment(Element.ALIGN_CENTER);
				refundSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundSUF));

				refundNA = new PdfPCell(new Phrase(refundneftTotalAmount.subtract(refundneftTdr).subtract(refundneftGst)
						.subtract(refundneftSuf).toString(), subCatFont));
				refundNA.setFixedHeight(17f);
				refundNA.setBorderWidth(0f);
				refundNA.setPaddingTop(Element.ALIGN_MIDDLE);
				refundNA.setVerticalAlignment(Element.ALIGN_CENTER);
				refundNA.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundNA));

			}
			if (refundrtgs) {
				refundTT = new PdfPCell(new Phrase("RTGS", subCatFont));
				refundTT.setFixedHeight(17f);
				refundTT.setBorderWidth(0f);
				refundTT.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTT.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTT.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTT));

				refundTA = new PdfPCell(new Phrase(refundrtgsTotalAmount.toString(), subCatFont));
				refundTA.setFixedHeight(17f);
				refundTA.setBorderWidth(0f);
				refundTA.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTA.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTA));

				refundTDR = new PdfPCell(new Phrase(refundrtgsTdr.toString(), subCatFont));
				refundTDR.setFixedHeight(17f);
				refundTDR.setBorderWidth(0f);
				refundTDR.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTDR.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTDR));

				refundGST = new PdfPCell(new Phrase(refundrtgsGst.toString(), subCatFont));
				refundGST.setFixedHeight(17f);
				refundGST.setBorderWidth(0f);
				refundGST.setPaddingTop(Element.ALIGN_MIDDLE);
				refundGST.setVerticalAlignment(Element.ALIGN_CENTER);
				refundGST.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundGST));

				refundSUF = new PdfPCell(new Phrase(refundrtgsSuf.toString(), subCatFont));
				refundSUF.setFixedHeight(17f);
				refundSUF.setBorderWidth(0f);
				refundSUF.setPaddingTop(Element.ALIGN_MIDDLE);
				refundSUF.setVerticalAlignment(Element.ALIGN_CENTER);
				refundSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundSUF));

				refundNA = new PdfPCell(new Phrase(refundrtgsTotalAmount.subtract(refundrtgsTdr).subtract(refundrtgsGst)
						.subtract(refundrtgsSuf).toString(), subCatFont));
				refundNA.setFixedHeight(17f);
				refundNA.setBorderWidth(0f);
				refundNA.setPaddingTop(Element.ALIGN_MIDDLE);
				refundNA.setVerticalAlignment(Element.ALIGN_CENTER);
				refundNA.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundNA));
			}
			if (refundcod) {
				refundTT = new PdfPCell(new Phrase("COD", subCatFont));
				refundTT.setFixedHeight(17f);
				refundTT.setBorderWidth(0f);
				refundTT.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTT.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTT.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTT));

				refundTA = new PdfPCell(new Phrase(refundcodTotalAmount.toString(), subCatFont));
				refundTA.setFixedHeight(17f);
				refundTA.setBorderWidth(0f);
				refundTA.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTA.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTA.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTA));

				refundTDR = new PdfPCell(new Phrase(refundcodTdr.toString(), subCatFont));
				refundTDR.setFixedHeight(17f);
				refundTDR.setBorderWidth(0f);
				refundTDR.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTDR.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTDR));

				refundGST = new PdfPCell(new Phrase(refundcodGst.toString(), subCatFont));
				refundGST.setFixedHeight(17f);
				refundGST.setBorderWidth(0f);
				refundGST.setPaddingTop(Element.ALIGN_MIDDLE);
				refundGST.setVerticalAlignment(Element.ALIGN_CENTER);
				refundGST.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundGST));

				refundSUF = new PdfPCell(new Phrase(refundcodSuf.toString(), subCatFont));
				refundSUF.setFixedHeight(17f);
				refundSUF.setBorderWidth(0f);
				refundSUF.setPaddingTop(Element.ALIGN_MIDDLE);
				refundSUF.setVerticalAlignment(Element.ALIGN_CENTER);
				refundSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundSUF));

				refundNA = new PdfPCell(new Phrase(new BigDecimal("0.00").subtract(refundcodTdr).subtract(refundcodGst)
						.subtract(refundcodSuf).toString(), subCatFont));
				refundNA.setFixedHeight(17f);
				refundNA.setBorderWidth(0f);
				refundNA.setPaddingTop(Element.ALIGN_MIDDLE);
				refundNA.setVerticalAlignment(Element.ALIGN_CENTER);
				refundNA.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundNA));
			}
			if (!refundcc && !refunddc && !refundin && !refundnb && !refundup && !refundcod && !refundneft
					&& !refundrtgs && !refundimps && !refundwl) {

				refundTDR = new PdfPCell(new Phrase("NO DATA FOUND FOR REFUND SETTLED", subCatFont));
				refundTDR.setFixedHeight(20f);
				refundTDR.setColspan(6);
				refundTDR.setBorderWidth(0f);
				refundTDR.setPaddingTop(Element.ALIGN_MIDDLE);
				refundTDR.setVerticalAlignment(Element.ALIGN_CENTER);
				refundTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
				refundTable.addCell(new PdfPCell(refundTDR));

			}
			refundTT = new PdfPCell(new Phrase("Total", totalFont));
			BaseColor myColor = WebColors.getRGBColor("#002664");
			refundTT.setBackgroundColor(myColor);
			refundTT.setFixedHeight(20f);
			refundTT.setBorderWidth(0f);
			refundTT.setBorderWidthTop(1f);
			refundTT.setBorderWidthBottom(0.5f);
			refundTT.setBorderColorBottom(BaseColor.WHITE);
			refundTT.setPaddingTop(Element.ALIGN_MIDDLE);
			refundTT.setVerticalAlignment(Element.ALIGN_CENTER);
			refundTT.setHorizontalAlignment(Element.ALIGN_CENTER);
			refundTable.addCell(new PdfPCell(refundTT));

			refundTA = new PdfPCell(new Phrase(refundtotalAmount.toString(), subCatFont));
			refundTA.setFixedHeight(20f);
			refundTA.setBorderWidth(0f);
			refundTA.setBorderWidthTop(1f);
			refundTA.setBorderWidthBottom(0.5f);
			refundTA.setBorderColorBottom(BaseColor.WHITE);
			refundTA.setPaddingTop(Element.ALIGN_MIDDLE);
			refundTA.setVerticalAlignment(Element.ALIGN_CENTER);
			refundTA.setHorizontalAlignment(Element.ALIGN_CENTER);
			refundTable.addCell(new PdfPCell(refundTA));

			refundTDR = new PdfPCell(new Phrase(refundtotalTdr.toString(), subCatFont));
			refundTDR.setFixedHeight(20f);
			refundTDR.setBorderWidth(0f);
			refundTDR.setBorderWidthTop(1f);
			refundTDR.setBorderWidthBottom(0.5f);
			refundTDR.setBorderColorBottom(BaseColor.WHITE);
			refundTDR.setPaddingTop(Element.ALIGN_MIDDLE);
			refundTDR.setVerticalAlignment(Element.ALIGN_CENTER);
			refundTDR.setHorizontalAlignment(Element.ALIGN_CENTER);
			refundTable.addCell(new PdfPCell(refundTDR));

			refundGST = new PdfPCell(new Phrase(refundtotalGst.toString(), subCatFont));
			refundGST.setFixedHeight(20f);
			refundGST.setBorderWidth(0f);
			refundGST.setBorderWidthTop(1f);
			refundGST.setBorderWidthBottom(0.5f);
			refundGST.setBorderColorBottom(BaseColor.WHITE);
			refundGST.setPaddingTop(Element.ALIGN_MIDDLE);
			refundGST.setVerticalAlignment(Element.ALIGN_CENTER);
			refundGST.setHorizontalAlignment(Element.ALIGN_CENTER);
			refundTable.addCell(new PdfPCell(refundGST));

			refundSUF = new PdfPCell(new Phrase(refundtotalSuf.toString(), subCatFont));
			refundSUF.setFixedHeight(20f);
			refundSUF.setBorderWidth(0f);
			refundSUF.setBorderWidthTop(1f);
			refundSUF.setBorderWidthBottom(0.5f);
			refundSUF.setBorderColorBottom(BaseColor.WHITE);
			refundSUF.setPaddingTop(Element.ALIGN_MIDDLE);
			refundSUF.setVerticalAlignment(Element.ALIGN_CENTER);
			refundSUF.setHorizontalAlignment(Element.ALIGN_CENTER);
			refundTable.addCell(new PdfPCell(refundSUF));

			refundAmount = refundtotalAmount.subtract(refundtotalTdr).subtract(refundtotalGst).subtract(refundtotalSuf)
					.subtract(refundcodTotalAmount);
			refundNA = new PdfPCell(new Phrase(refundAmount.toString(), subCatFont));
			refundNA.setFixedHeight(20f);
			refundNA.setBorderWidth(0f);
			refundNA.setBorderWidthTop(1f);
			refundNA.setBorderWidthBottom(0.5f);
			refundNA.setBorderColorBottom(BaseColor.WHITE);
			refundNA.setPaddingTop(Element.ALIGN_MIDDLE);
			refundNA.setVerticalAlignment(Element.ALIGN_CENTER);
			refundNA.setHorizontalAlignment(Element.ALIGN_CENTER);
			refundTable.addCell(new PdfPCell(refundNA));

			refundTT = new PdfPCell(new Phrase("Settlement Amount", totalFont));
			refundTT.setBackgroundColor(myColor);
			refundTT.setFixedHeight(20f);
			refundTT.setBorderWidth(0f);
			refundTT.setBorderWidthTop(0.5f);
			refundTT.setBorderWidthBottom(1f);
			refundTT.setBorderColorTop(BaseColor.WHITE);
			refundTT.setPaddingTop(Element.ALIGN_MIDDLE);
			refundTT.setVerticalAlignment(Element.ALIGN_CENTER);
			refundTT.setHorizontalAlignment(Element.ALIGN_CENTER);
			refundTable.addCell(new PdfPCell(refundTT));

			refundTA = new PdfPCell(new Phrase("", subCatFont));
			refundTA.setBackgroundColor(myColor);
			refundTA.setFixedHeight(20f);
			refundTA.setBorderWidth(0f);
			refundTA.setBorderWidthTop(0.5f);
			refundTA.setBorderWidthBottom(1f);
			refundTA.setBorderColorTop(BaseColor.WHITE);
			refundTable.addCell(new PdfPCell(refundTA));

			refundTDR = new PdfPCell(new Phrase("", subCatFont));
			refundTDR.setBackgroundColor(myColor);
			refundTDR.setFixedHeight(20f);
			refundTDR.setBorderWidth(0f);
			refundTDR.setBorderWidthTop(0.5f);
			refundTDR.setBorderWidthBottom(1f);
			refundTDR.setBorderColorTop(BaseColor.WHITE);
			refundTable.addCell(new PdfPCell(refundTDR));

			refundGST = new PdfPCell(new Phrase("", subCatFont));
			refundGST.setBackgroundColor(myColor);
			refundGST.setFixedHeight(20f);
			refundGST.setBorderWidth(0f);
			refundGST.setBorderWidthTop(0.5f);
			refundGST.setBorderWidthBottom(1f);
			refundGST.setBorderColorTop(BaseColor.WHITE);
			refundTable.addCell(new PdfPCell(refundGST));

			refundSUF = new PdfPCell(new Phrase("", subCatFont));
			refundSUF.setBackgroundColor(myColor);
			refundSUF.setFixedHeight(20f);
			refundSUF.setBorderWidth(0f);
			refundSUF.setBorderWidthTop(0.5f);
			refundSUF.setBorderWidthBottom(1f);
			refundSUF.setBorderColorTop(BaseColor.WHITE);
			refundTable.addCell(new PdfPCell(refundSUF));

			refundNA = new PdfPCell(new Phrase(settlementAmount.toString(), totalFont));
			refundNA.setBackgroundColor(myColor);
			refundNA.setFixedHeight(20f);
			refundNA.setBorderWidth(0f);
			refundNA.setBorderWidthTop(0.5f);
			refundNA.setBorderWidthBottom(1f);
			refundNA.setBorderColorTop(BaseColor.WHITE);
			refundNA.setPaddingTop(Element.ALIGN_MIDDLE);
			refundNA.setVerticalAlignment(Element.ALIGN_CENTER);
			refundNA.setHorizontalAlignment(Element.ALIGN_CENTER);
			refundTable.addCell(new PdfPCell(refundNA));

			document.add(refundTable);
			//logger.info("outside footer settings");

			float[] footerWidths = { 50f, 50f };

			PdfPTable footerTable = new PdfPTable(2);
			footerTable.setWidths(footerWidths);
			footerTable.setWidthPercentage(100f);
			footerTable.getDefaultCell().setBorderWidth(0f);

			Anchor anchor = new Anchor("support@PaymentGateway.com");
			anchor.setReference("mailto:support@PaymentGateway.com");

			Chunk url = new Chunk("support@PaymentGateway.com");
			url.setFont(footerFont);
			Chunk msg = new Chunk("This is a computer generated copy");
			msg.setFont(headingtop);
			footerParagraph = new Paragraph();
			footerParagraph.add(Chunk.NEWLINE);
			footerParagraph.add(url);
			footerParagraph.add(Chunk.NEWLINE);
			footerParagraph.add(msg);
			footerParagraph.setAlignment(Element.ALIGN_LEFT);
			footerTable.addCell(footerParagraph);

			Image img2 = Image.getInstance(PropertiesManager.propertiesMap.get("footerLogo"));
			img2.scaleToFit(90f, 110f);
			PdfPCell cellLogo = new PdfPCell(img2);
			cellLogo.setPaddingTop(15f);
			cellLogo.setPaddingLeft(0f);
			cellLogo.setBorder(Rectangle.NO_BORDER);
			cellLogo.setFixedHeight(50f);
			footerTable.addCell(new PdfPCell(cellLogo)).setHorizontalAlignment(Element.ALIGN_RIGHT);

			document.add(footerTable);
			document.close();
			bytes = baos.toByteArray();

			//logger.info("creating excel file");
			ByteArrayOutputStream bos = null;
			byte[] excelFileAsBytes = null;



				SXSSFWorkbook wb = new SXSSFWorkbook(100);
				Row row;
				int rownum = 1;
				// Create a blank sheet
				Sheet sheet = wb.createSheet("Settlement Report");
				row = sheet.createRow(0);

				row.createCell(0).setCellValue("Sr No");
				row.createCell(1).setCellValue("Txn Id");
				row.createCell(2).setCellValue("Pg Ref Num");
				row.createCell(3).setCellValue("Merchant");
				row.createCell(4).setCellValue("Captured Date");
				row.createCell(5).setCellValue("Settled Date");
				row.createCell(6).setCellValue("Payout Date");
				row.createCell(7).setCellValue("UTR NO");
				row.createCell(8).setCellValue("Order Id");
				row.createCell(9).setCellValue("RRN");
				row.createCell(10).setCellValue("Payment Method");
				row.createCell(11).setCellValue("MopType");
				row.createCell(12).setCellValue("Mask");
				row.createCell(13).setCellValue("Cust Name");
				row.createCell(14).setCellValue("CardHolder Type");
				row.createCell(15).setCellValue("Txn Type");
				row.createCell(16).setCellValue("Transaction Mode");
				row.createCell(17).setCellValue("Status");
				row.createCell(18).setCellValue("Transaction Region");
				row.createCell(19).setCellValue("Base Amount");
				row.createCell(20).setCellValue("Total Amount");
				row.createCell(21).setCellValue("TDR / Surcharge");
				row.createCell(22).setCellValue("GST");
				row.createCell(23).setCellValue("Merchant Amount");
				row.createCell(23).setCellValue("Transaction Flag");
				row.createCell(24).setCellValue("Part Settled Flag");
				row.createCell(25).setCellValue("UDF11");
				row.createCell(26).setCellValue("UDF12");
				row.createCell(27).setCellValue("UDF13");
				row.createCell(28).setCellValue("UDF14");
				row.createCell(29).setCellValue("UDF15");
				row.createCell(30).setCellValue("UDF16");
				row.createCell(31).setCellValue("UDF17");
				row.createCell(32).setCellValue("UDF18");

				for (PaymentSearchDownloadObject transactionSearch : settlementdata) {

					row = sheet.createRow(rownum++);
					transactionSearch.setSrNo(String.valueOf(rownum - 1));

					Object[] objArr = transactionSearch.myCsvMethodemailPaymentsReportCapturedForSpecificSubMerchant();

					int cellnum = 0;
					for (Object obj : objArr) {
						// this line creates a cell in the next column of that row
						Cell cell = row.createCell(cellnum++);
						if (obj instanceof String)
							cell.setCellValue((String) obj);
						else if (obj instanceof Integer)
							cell.setCellValue((Integer) obj);

					}
				}
				try {
					String FILE_EXTENSION = ".xlsx";
					if (StringUtils.isNotBlank(subMerchPayId)) {
						filenameexcel = "Settled_Transaction_Report" + subMerchPayId + df.format(new Date())
								+ FILE_EXTENSION;
					} else {
						filenameexcel = "Settled_Transaction_Report" + merchPayId + df.format(new Date())
								+ FILE_EXTENSION;
					}
					// this Writes the workbook

					bos = new ByteArrayOutputStream();
					try {
						wb.write(bos);
					} finally {
						bos.close();
					}
					excelFileAsBytes = bos.toByteArray();

				} catch (Exception exception) {
					logger.error("Exception", exception);
				}
			
			try {
				if (cc || dc || refundcc || refunddc) {
				}else {
					currency = null;
				}
				String subject = getMailSubject(fromdate, todate, merchant);
				String messageBody = getEmailBodyWithAttachement(payoutDateshow, merchant,
						merchantImageAddr, saletxn, refundtxn, saleAmount, refundAmount, settlementAmount, utrNo,
						account, chargeback, chargeBackAmount, currency);
				pepipostEmailSender.sendEmailWithAttachment(subject, messageBody, filename, bytes, merchant.getEmailId(), "",
						filenameexcel, excelFileAsBytes, user.getTransactionEmailId());
			} catch (Exception exception) {
				logger.error("Exception", exception);
			}
			logger.info("Auto Send SubMerchnt Payment Advise File generated successfully for "
					+ merchant.getBusinessName() + " Merchant");
		} catch (Exception e) {
			logger.error("Exception ",e);
		}

	}

	public String getMailSubject(String dateFrom, String dateTo, Merchants merchant) {

		String subject = "Payment Advise | " + dateFrom + " To " + dateTo + " | " + merchant.getBusinessName()
				+ " | Payment Gateway";
		if(dateFrom.equalsIgnoreCase("NA") && dateTo.equalsIgnoreCase("NA")) {
			subject = "Payment Advise | " + merchant.getBusinessName()+ " | Payment Gateway";
		}

		return subject;
	}
	
	public String getEmailBodyWithAttachement(String payoutDate, Merchants merchant, String image, int saletxn,
			int refundtxn, BigDecimal saleAmount, BigDecimal refundAmount, BigDecimal settlementAmount, String utr,
			String account, int chargeback, BigDecimal chargebackamount, String currency)
			throws BadElementException, MalformedURLException, IOException {

		StringBuilder body = new StringBuilder();
		String currencyType = "INR ";
		if(StringUtils.isNotBlank(currency)) {
			currencyType = currency + " ";
		}
		
		if (image.contains("null.png")) {
			image = PropertiesManager.propertiesMap.get("noLogoMerchant");
		}
		String footerlogo = PropertiesManager.propertiesMap.get("emailerLogoURL");
		String img = PropertiesManager.propertiesMap.get("dashboardEmailLogo");
		String imgdocs = PropertiesManager.propertiesMap.get("paymentAdviceReportDocs");

		body.append("<!DOCTYPE html>");
		body.append(
				"<html lang='en'> <head> <meta charset='UTF-8'> <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
		body.append(
				"<link href=\"https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700;900&display=swap\" rel=\"stylesheet\"><title>Payment Advise Report</title>");
		body.append("<style> body{font-family: 'Roboto', sans-serif;} </style>");
		body.append("</head>");
		body.append("<body bgcolor='#fff'>");
		body.append("<table width=\"800\" align=\"center\" cellspacing=\"10px\">");
		body.append("<tr>");
		body.append("<td><img src=\"" + image + "\" width=\"100\" alt=\"/\"></td>");
		body.append("<td align=\"right\"><h2>Daily Settlement Report - " + payoutDate + "</h2></td>");
		body.append("</tr>");
		body.append("<tr><td colspan=\"2\"> <table bgcolor=\"#002664\" cellpadding=\"10px\" width=\"100%\">");
		body.append("<tr><td colspan=\"4\" style=\"color: #fff\" align=\"center\"><img width=\"100\" src=\"" + imgdocs
				+ "\" alt=\"\"><h2>" + merchant.getBusinessName() + "</h2></td></tr>");
		body.append("<tr align='center' style='color: #fff'><td width='33%'><span> "
				+ currencyType + saleAmount.setScale(2, BigDecimal.ROUND_HALF_EVEN)
				+ "</span><br><span style='display: block'>Total Sale</span></td>");
		body.append(" <td width='33%'><span> " + currencyType + refundAmount.setScale(2, BigDecimal.ROUND_HALF_EVEN)
				+ "</span><br><span style='display: block'>Total Refund</span></td>");
		body.append("<td width='33%'><span> " + currencyType + settlementAmount.setScale(2, BigDecimal.ROUND_HALF_EVEN)
				+ "</span><br><span style='display: block'>Total Settled</span></td></tr>");
		body.append(
				"<tr align='center'><td colspan='4'><a href='https://www.PaymentGateway.com/crm/jsp/login' style='border-radius:25px;text-decoration: none;display:inline-block;padding: 0px 20px;color: #fff;background-color: #3477e0'><img src=\""
						+ img + "\" alt=\"/\"></a></td></tr></table></td></tr>");

		body.append("<tr ><td><table cellpadding=\"3px\"><tr><td>Captured Transactions:</td><td><b>" + saletxn
				+ "</b></td></tr><tr><td>Refund Transactions</td><td><b>" + refundtxn
				+ "</b></td></tr><tr><td>UTR:</td><td><b>" + utr + "</b></td></tr></table></td>");
		body.append("<td><table cellpadding='3px' align='right'><tr><td>No. of Chargeback:</td><td><b>" + chargeback
				+ "</b></td></tr><tr><td>Chargeback Amount:</td><td><b>" + currencyType + chargebackamount
				+ "</b></td></tr><tr><td>Account No</td><td><b>" + account + "</b></td></tr></table></td></tr>");
		body.append(
				"<tr><td><a href=\"mailto:support@PaymentGateway.com\">support@PaymentGateway.com</a></td><td align=\"right\"><img src=\""
						+ footerlogo + "\" width=\"150\" alt=\"/\"></td></tr>");
		body.append("</table> </body> </html>");

		return body.toString();

	}

	public String getMerchantPayId() {
		return merchantPayId;
	}

	public void setMerchantPayId(String merchantPayId) {
		this.merchantPayId = merchantPayId;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public User getSessionUser() {
		return sessionUser;
	}

	public void setSessionUser(User sessionUser) {
		this.sessionUser = sessionUser;
	}

	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}

	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}

	public FileInputStream getFileInputStream() {
		return fileInputStream;
	}

	public String getPayoutDate() {
		return payoutDate;
	}

	public void setPayoutDate(String payoutDate) {
		this.payoutDate = payoutDate;
	}

}