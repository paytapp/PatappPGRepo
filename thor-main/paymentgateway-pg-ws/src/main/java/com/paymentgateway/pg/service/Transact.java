
package com.paymentgateway.pg.service;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.paymentgateway.airPay.AirPaySaleResponseHandler;
import com.paymentgateway.apexPay.ApexPaySaleResponseHandler;
import com.paymentgateway.billDesk.BillDeskSaleResponseHandler;
import com.paymentgateway.bob.BobSaleResponseHandler;
import com.paymentgateway.cashfree.CashfreeSaleResponseHandler;
import com.paymentgateway.cashfree.CashfreeUpiQrResponseHandler;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.P2MPayoutUtil;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.federal.FederalInvalidSaleResponseHandler;
import com.paymentgateway.floxypay.FloxypaySaleResponseHandler;
import com.paymentgateway.fss.RupaySaleResponseHandler;
import com.paymentgateway.fssPay.FssPaySaleResponseHandler;
import com.paymentgateway.globalpay.GlobalpayCallbackHandler;
import com.paymentgateway.globalpay.GlobalpaySaleResponseHandler;
import com.paymentgateway.grezpay.GrezpaySaleResponseHandler;
import com.paymentgateway.hdfc.HdfcSaleResponseHandler;
import com.paymentgateway.idbi.IdbiSaleResponseHandler;
import com.paymentgateway.ipint.IpintSaleResponseHandler;
import com.paymentgateway.isgpay.ISGPaySaleResponseHandler;
import com.paymentgateway.kotak.KotakSaleResponseHandler;
import com.paymentgateway.kotak.upi.KotakUpiSaleResponseHandler;
import com.paymentgateway.payphi.PayphiSaleResponseHandler;
import com.paymentgateway.payu.PayuSaleResponseHandler;
import com.paymentgateway.qaicash.QaicashSaleResponseHandler;
import com.paymentgateway.razorpay.RazorpaySaleResponseHandler;
import com.paymentgateway.requestrouter.RequestRouter;
import com.paymentgateway.safexpay.SafexpaySaleResponseHandler;
import com.paymentgateway.toshanidigital.ToshanidigitalSaleResponseHandler;
import com.paymentgateway.upigateway.UpigatewaySaleResponseHandler;
//import com.paymentgateway.vepay.VepaySaleResponseHandler;

@RestController
@CrossOrigin
public class Transact {
	private static Logger logger = LoggerFactory.getLogger(Transact.class.getName());

	@Autowired
	private RequestRouter router;

	@Autowired
	private FederalInvalidSaleResponseHandler federalInvalidSaleResponseHandler;

	@Autowired
	private UpiSaleResponseHandler upiSaleResponseHandler;

	@Autowired
	private KotakUpiSaleResponseHandler googlePaySaleResponseHandler;

	@Autowired
	private BobSaleResponseHandler bobSaleResponseHandler;

	@Autowired
	private HdfcSaleResponseHandler hdfcSaleResponseHandler;

	@Autowired
	private KotakSaleResponseHandler kotakSaleResponseHandler;

	@Autowired
	private RupaySaleResponseHandler rupaySaleResponseHandler;

	@Autowired
	private StatusEnquiryProcessor statusEnquiryProcessor;

	@Autowired
	private IdbiSaleResponseHandler idbiSaleResponseHandler;

	@Autowired
	private FssPaySaleResponseHandler fssPaySaleResponseHandler;

	@Autowired
	private BillDeskSaleResponseHandler billDeskSaleResponseHandler;

	@Autowired
	private PaymentRequestProcessor paymentRequestProcessor;

	@Autowired
	private InvoiceProcessor invoiceProcessor;

	@Autowired
	private ISGPaySaleResponseHandler iSGPaySaleResponseHandler;

	@Autowired
	private OrderConfirmationProcessor orderConfirmationProcessor;

	@Autowired
	private CustomTransactionEnquiry customTransactionEnquiry;

	@Autowired
	private NodalSettlement nodalSettlement;

	@Autowired
	private IciciEnachMandateEnquiryProcessor iciciEnachMandateEnquiryProcessor;

	@Autowired
	private ENachRegistrationService eNachRegistrationService;

	@Autowired
	private IciciEnachTransactionService iciciEnachTransactionService;

	@Autowired
	private SettledTransactionEnquiry settledTransactionEnquiry;

	@Autowired
	private VpaValidationIDFC vpaValidationIDFC;

	@Autowired
	private PayphiSaleResponseHandler payphiSaleResponseHandler;

	@Autowired
	private CustomerUPIQrService customerUPIQrService;

	@Autowired
	private IciciUpiAutoPayTransactionService upiAutoPayTransactionService;

	@Autowired
	private PayuSaleResponseHandler payuSaleResponseHandler;

	@Autowired
	private RetryCallbackService retryCallbackService;

	@Autowired
	private P2MPayoutUtil p2MPayoutUtil;

	@Autowired
	private RefundUtilityDataService refundUtilityDataService;

	@Autowired
	private SafexpaySaleResponseHandler safexpaySaleResponseHandler;

	@Autowired
	private MerchantStatusEnquiry merchantStatusEnquiry;

	@Autowired
	private CashfreeSaleResponseHandler cashfreeSaleResponseHandler;

	@Autowired
	private CashfreeUpiQrResponseHandler cashfreeUpiQrResponseHandler;

	@Autowired
	private ApexPaySaleResponseHandler apexPaySaleResponseHandler;

	@Autowired
	private AirPaySaleResponseHandler airPaySaleResponseHandler;

	@Autowired
	private RazorpaySaleResponseHandler razorpaySaleResponseHandler;

	@Autowired
	private QaicashSaleResponseHandler qaicashSaleResponseHandler;

	@Autowired
	private FloxypaySaleResponseHandler floxypaySaleResponseHandler;
	
	@Autowired
	private GrezpaySaleResponseHandler grezpaySaleResponseHandler;
	
	@Autowired
	private UpigatewaySaleResponseHandler upigatewaySaleResponseHandler;
	
	@Autowired
	private IpintSaleResponseHandler ipintSaleResponseHandler;
	
	@Autowired
	private ToshanidigitalSaleResponseHandler toshanidigitalSaleResponseHandler;
	
	@Autowired
	private IpintAggregatorMerchantService ipintAggregatorMerchantService;
	
	@Autowired
	private GlobalpaySaleResponseHandler globalpaySaleResponseHandler;
	
	@Autowired
	private GlobalpayCallbackHandler globalpayCallbackHandler; 
	
	
	/*
	 * @Autowired private VepaySaleResponseHandler vepaySaleResponseHandler;
	 */

	@Autowired
	private UpiPaymentsService upiPaymentsService;

	@RequestMapping(method = RequestMethod.POST, value = "/transact", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> transact(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			fields.removeInternalFields();
			fields.clean();
			fields.removeExtraFields();
			// To put request blob
			String fieldsAsString = fields.getFieldsAsBlobString();
			fields.put(FieldType.INTERNAL_REQUEST_FIELDS.getName(), fieldsAsString);
			fields.logAllFields("Refine Request:");
			Map<String, String> responseMap = new HashMap<String, String>();
			if (fields.get(FieldType.TXNTYPE.getName()).equals(TransactionType.STATUS.getName())) {
				responseMap = merchantStatusEnquiry.process(fields);
			} else {
				responseMap = router.route(fields);
			}
			responseMap.remove(FieldType.INTERNAL_CUSTOM_MDC.getName());
			return responseMap;
		} catch (Exception exception) {
			// Ideally this should be a non-reachable code
			logger.error("Exception", exception);
			return null;
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/process/payment", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> processPayment(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			fields.clean();
			Fields internalFields = fields.removeInternalFields();
			// Refine the fields sent by internal application
			fields.removeExtraInternalFields();
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), Constants.Y_FLAG.getValue());
			// put Internal fields back
			fields.put(internalFields);
			fields.logAllFields("Refine Request:");
			Map<String, String> responseMap = router.route(fields);

			return responseMap;
		} catch (Exception exception) {
			// Ideally this should be a non-reachable code
			logger.error("Exception", exception);
			return null;
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/federalProcess/response", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> federalMPIResponse(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			fields.clean();
			Fields internalFields = fields.removeInternalFields();
			// Refine the fields sent by internal application
			fields.removeExtraInternalFields();
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), Constants.Y_FLAG.getValue());
			// put Internal fields back
			fields.put(internalFields);
			Map<String, String> responseMap = federalInvalidSaleResponseHandler.process(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/upiProcess/response", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> federalUPIResponse(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			fields.clean();
			Fields internalFields = fields.removeInternalFields();
			// Refine the fields sent by internal application
			fields.removeExtraInternalFields();
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), Constants.Y_FLAG.getValue());
			fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), Constants.N_FLAG.getValue());

			// put Internal fields back
			fields.put(internalFields);

			// Add dummy hash
			fields.put(FieldType.HASH.getName(), "2E94A6B4D8C3AFFAA1C2FDC417DCBAA78C63E6D056BD1B4E58426BD20AE5C044");
			Map<String, String> responseMap = upiSaleResponseHandler.process(fields);
			return responseMap;
		} catch (Exception exception) {
			// Ideally this should be a non-reachable code
			logger.error("Exception", exception);
			return null;
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/bobProcess/response", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> bobResponse(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			fields.clean();
			Fields internalFields = fields.removeInternalFields();
			// Refine the fields sent by internal application
			fields.removeExtraInternalFields();
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), Constants.Y_FLAG.getValue());
			// put Internal fields back
			fields.put(internalFields);
			Map<String, String> responseMap = bobSaleResponseHandler.process(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/payphi/response", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> payphiResponse(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			fields.clean();
			Fields internalFields = fields.removeInternalFields();
			// Refine the fields sent by internal application
			fields.removeExtraInternalFields();
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), Constants.Y_FLAG.getValue());
			// put Internal fields back
			fields.put(internalFields);
			Map<String, String> responseMap = new HashMap<String, String>();
			responseMap = payphiSaleResponseHandler.process(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/hdfcProcess/response", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> hdfcResponse(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			fields.clean();
			Fields internalFields = fields.removeInternalFields();
			// Refine the fields sent by internal application
			fields.removeExtraInternalFields();
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), Constants.Y_FLAG.getValue());
			// put Internal fields back
			fields.put(internalFields);
			Map<String, String> responseMap = hdfcSaleResponseHandler.process(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/idbiProcess/response", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> idbiProcess(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			fields.clean();
			Fields internalFields = fields.removeInternalFields();
			// Refine the fields sent by internal application
			fields.removeExtraInternalFields();
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), Constants.Y_FLAG.getValue());
			// put Internal fields back
			fields.put(internalFields);
			Map<String, String> responseMap = idbiSaleResponseHandler.process(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/fssPayProcess/response", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> fssPayResponse(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request FSSPAY:");
			fields.clean();
			Fields internalFields = fields.removeInternalFields();
			// Refine the fields sent by internal application
			fields.removeExtraInternalFields();
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), Constants.Y_FLAG.getValue());
			// put Internal fields back
			fields.put(internalFields);
			Map<String, String> responseMap = fssPaySaleResponseHandler.process(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/billDeskProcess/response", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> billDeskResponse(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request BillDesk:");
			fields.clean();
			Fields internalFields = fields.removeInternalFields();
			// Refine the fields sent by internal application
			fields.removeExtraInternalFields();
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), Constants.Y_FLAG.getValue());
			// put Internal fields back
			fields.put(internalFields);
			Map<String, String> responseMap = billDeskSaleResponseHandler.process(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/googlePayProcess/response", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> googlePayResponse(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			fields.clean();
			Fields internalFields = fields.removeInternalFields();
			// Refine the fields sent by internal application
			fields.removeExtraInternalFields();
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), Constants.Y_FLAG.getValue());
			// put Internal fields back
			fields.put(internalFields);
			Map<String, String> responseMap = googlePaySaleResponseHandler.process(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/internalRefund", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> internalRefund(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);

			String userEmail = fields.get(FieldType.INTERNAL_USER_EMAIL.getName());
			fields.logAllFields("Raw Request:");
			fields.removeInternalFields();
			fields.clean();
			fields.removeExtraFields();
			// To put request blob
			String fieldsAsString = fields.getFieldsAsBlobString();
			fields.put(FieldType.INTERNAL_REQUEST_FIELDS.getName(), fieldsAsString);
			fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
			if (StringUtils.isNotEmpty(userEmail)) {
				fields.put(FieldType.INTERNAL_USER_EMAIL.getName(), userEmail);
			}

			if (fields.contains(FieldType.UDF6.getName())) {
				if (!fields.get(FieldType.UDF6.getName()).isEmpty()) {
					fields.put(FieldType.UDF6.getName(), Constants.Y_FLAG.getValue());
				}
			}
			fields.logAllFields("Refine Request:");
			Map<String, String> responseMap = router.route(fields);
			responseMap.remove(FieldType.INTERNAL_CUSTOM_MDC.getName());
			return responseMap;
		} catch (Exception exception) {
			// Ideally this should be a non-reachable code
			logger.error("Exception", exception);
			return null;
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/kotakProcess/response", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> kotakResponse(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			fields.clean();
			Fields internalFields = fields.removeInternalFields();
			// Refine the fields sent by internal application
			fields.removeExtraInternalFields();
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), Constants.Y_FLAG.getValue());
			// put Internal fields back
			fields.put(internalFields);
			Map<String, String> responseMap = kotakSaleResponseHandler.process(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/rupayProcess/response", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> rupayResponse(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			fields.clean();
			Fields internalFields = fields.removeInternalFields();
			// Refine the fields sent by internal application
			fields.removeExtraInternalFields();
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), Constants.Y_FLAG.getValue());
			// put Internal fields back
			fields.put(internalFields);
			Map<String, String> responseMap = rupaySaleResponseHandler.process(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/enquiry/process", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> enquiryResponse(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			fields.clean();
			Fields internalFields = fields.removeInternalFields();
			// Refine the fields sent by internal application
			fields.removeExtraInternalFields();
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), Constants.Y_FLAG.getValue());
			// put Internal fields back
			fields.put(internalFields);
			Map<String, String> responseMap = statusEnquiryProcessor.process(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/enquiry/orderConfirm", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public void orderConfirmResponse(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			fields.clean();
			Fields internalFields = fields.removeInternalFields();
			// Refine the fields sent by internal application
			fields.removeExtraInternalFields();
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), Constants.Y_FLAG.getValue());
			// put Internal fields back
			fields.put(internalFields);
			orderConfirmationProcessor.process(fields);
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/psk/paymentrequest", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> paymentrequest(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			// fields.put(FieldType.TXNTYPE.getName(),TransactionType.POSREQUEST.getName());
			fields.logAllFields("Raw Request For New Payment Via POS: ");
			Map<String, String> responseMap = paymentRequestProcessor.process(fields);
			responseMap.remove(FieldType.HASH.getName());
			return responseMap;
		} catch (Exception exception) {
			// Ideally this should be a non-reachable code
			logger.error("Exception", exception);
			return null;
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/invoice/process", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> invoiceController(@RequestBody Map<String, String> reqmap) {
		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw request received for Invoice API:");
			fields.clean();
			Map<String, String> responseMap = invoiceProcessor.process(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception caught while creating invoice using Invoice API", exception);
			return null;
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/isgPayProcess/response", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> ispPayResponse(@RequestBody Map<String, String> reqmap) {
		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request ISG: ");
			fields.clean();
			Fields internalFields = fields.removeInternalFields();
			// Refine the fields sent by internal application
			fields.removeExtraInternalFields();
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), Constants.Y_FLAG.getValue());
			// put Internal fields back
			fields.put(internalFields);
			Map<String, String> responseMap = iSGPaySaleResponseHandler.process(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/customTransactionStatus", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, Map<String, String>> customTransactionStatus(
			@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			fields.removeInternalFields();
			fields.clean();
			fields.logAllFields("Refine Request:");
			Map<String, Map<String, String>> responseMap = customTransactionEnquiry.getSaleCaptureTransaction(fields);

			return responseMap;
		} catch (Exception exception) {

			logger.error("Exception caught ", exception);
			return null;

		}
	}

	// For ICICI ENach Registration status enquiry
	@RequestMapping(method = RequestMethod.POST, value = "/enachMandateEnquiry", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> iciciEnachMandateEnquiryResponse(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			Map<String, String> responseMap = iciciEnachMandateEnquiryProcessor.ENachRegistrationStausEnquiry(fields);
			// Map<String, String> responseMap =
			// enachMandateEnquiryProcessor.ENachRegistrationStausEnquiry(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}

	}

	// For ICICI ENach Registration link
	@RequestMapping(method = RequestMethod.POST, value = "/eMandateRegistration", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> iciciEnachRegistrationLink(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			Map<String, String> responseMap = eNachRegistrationService.eMandateSignLink(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}

	}

	// For ICICI ENach Registration status enquiry By Order id
	@RequestMapping(method = RequestMethod.POST, value = "/enachRegistrationEnquiry", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> iciciEnachRegistrationEnquiry(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			Map<String, String> responseMap = eNachRegistrationService.eNachRegistrationStatusEnquiry(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}

	}

	// For ICICI ENach Sale Transaction
	@RequestMapping(method = RequestMethod.POST, value = "/enachTransactionSchedule", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> iciciEnachTransactionSchedule(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			Map<String, String> responseMap = iciciEnachTransactionService.eNachTransactionSchedule(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}
	}

	// For ICICI ENach Sale Transaction
	@RequestMapping(method = RequestMethod.POST, value = "/enachTransactionStatusEnquiry", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> iciciEnachTransactionStatusEnquiry(
			@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			Map<String, String> responseMap = iciciEnachTransactionService.eNachTransactionStausEnquiry(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/merchantEnachTransactionStatusEnquiry", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> merchantEnachTransactionStatusEnquiry(
			@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			Map<String, String> responseMap = iciciEnachTransactionService.merchantENachTransactionStausEnquiry(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/nodalSettlement", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, Map<String, String>> nodalSettlement(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			fields.removeInternalFields();
			fields.clean();
			fields.logAllFields("Refine Request:");
			Map<String, Map<String, String>> responseMap = new HashMap<String, Map<String, String>>();
			;
			if (nodalSettlement.isMendatoryFieldEmpty(fields)) {

				logger.info("Invalid Fields or Empty Mendatory Fields found");

				Map<String, String> authMap = new HashMap<String, String>();
				authMap.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
				authMap.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
				authMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
				responseMap.put(ErrorType.AUTHENTICATION_FAILED.getResponseMessage(), authMap);
				return responseMap;

			}

			if (!nodalSettlement.validateHashForApi(fields)) {

				logger.info("Invalid Hash found");

				Map<String, String> authMap = new HashMap<String, String>();
				authMap.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
				authMap.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
				authMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
				authMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Hash");
				responseMap.put(ErrorType.AUTHENTICATION_FAILED.getResponseMessage(), authMap);

				return responseMap;

			}

			responseMap = nodalSettlement.getnodalSettlementTransaction(fields);

			return responseMap;
		} catch (Exception exception) {

			logger.error("Exception caught ", exception);
			return null;

		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/uptimeTest", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> uptimeTest() {
		Map<String, String> responseMap = new HashMap<String, String>();
		try {
			logger.info("PGWS Uptime test called");
			responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
			responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
		} catch (Exception exception) {
			logger.error("Exception in API", exception);
		}
		return responseMap;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/recon/settlement", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> reconSettlement(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request for Recon Settlement API: ");
			fields.removeInternalFields();
			fields.clean();
			fields.logAllFields("Refine Request for Recon Settlement API: ");
			Map<String, String> responseMap = settledTransactionEnquiry.verifySettledTransaction(fields);

			return responseMap;
		} catch (Exception exception) {

			logger.error("Exception caught ", exception);
			return null;

		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/validateVpa/idfcupi", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> validateVpaIdfc(@RequestBody Map<String, String> reqmap) {

		try {
			Map<String, String> resMap = new HashMap<String, String>();

			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request for VPA Validation API: ");
			JSONObject responseJson = vpaValidationIDFC.validationResponse(fields);
			logger.info(" Response for VPA validation  >>> " + responseJson);

			for (String jString : responseJson.keySet()) {

				resMap.put(jString, responseJson.get(jString).toString());
			}

			return resMap;
		} catch (Exception exception) {
			logger.error("Exception caught ", exception);
			return null;

		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/transact/getCustQR", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> getCustomerQr(@RequestBody Map<String, String> reqmap) {

		try {
			Map<String, String> resMap = new HashMap<String, String>();

			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request for Customer QR Generation: ");

			// check acquirer for generation static upi qr
			String acquirer = PropertiesManager.propertiesMap.get("STATIC_UPI_QR_ACQUIRER");
			if (acquirer.equalsIgnoreCase("CASHFREE")) {
				logger.info("Entered in cashfree acquirer");
				resMap = customerUPIQrService.getCashfreeStaticUpiQr(fields);
			} else if (acquirer.equalsIgnoreCase("YESBANKCB")) {
				// TODO
			} else if (acquirer.equalsIgnoreCase("AXISBANK")) {
				// TODO
			} else {
				resMap = customerUPIQrService.generateCustQR(fields);
			}

			return resMap;
		} catch (Exception exception) {
			logger.error("Exception caught ", exception);
			return null;

		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/transact/getCustDynamicQR", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> getCustomerDynamicQr(@RequestBody Map<String, String> reqmap) {

		try {
			Map<String, String> resMap = new HashMap<String, String>();

			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request for Customer QR Generation: ");

			// check acquirer for generation static upi qr
			String acquirer = "CASHFREE";
			if (acquirer.equalsIgnoreCase("CASHFREE")) {
				logger.info("Entered in cashfree acquirer");
				resMap = customerUPIQrService.getCashfreeDynamicUpiQr(fields);
			} else if (acquirer.equalsIgnoreCase("YESBANKCB")) {
				// TODO
			} else if (acquirer.equalsIgnoreCase("AXISBANK")) {
				// TODO
			} else {
				resMap = customerUPIQrService.generateCustQR(fields);
			}

			return resMap;
		} catch (Exception exception) {
			logger.error("Exception caught ", exception);
			return null;

		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/upiAutoPayRegistrationLink", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> iciciUpiAutoPayRegistrationLink(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			Map<String, String> responseMap = upiAutoPayTransactionService.upiAutoPayRegistrationLink(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/upiAutoPayMandateEnquiry", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> iciciUpiAutoPayMandateEnquiry(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			Map<String, String> responseMap = upiAutoPayTransactionService.upiAutoPayMandateEnquiry(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/upiAutoPayDebitTransaction", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> iciciUpiAutoPayDebitTransaction(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			Map<String, String> responseMap = upiAutoPayTransactionService.debitTransaction(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/upiAutoPayTransactionNotification", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> iciciUpiAutoPayTransactionNotification(
			@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			Map<String, String> responseMap = upiAutoPayTransactionService.notificationDebitTransaction(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/upiAutoPayTransactionStatusEnquiry", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> iciciUpiAutoPayDebitTransactionStatusEnquiry(
			@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			Map<String, String> responseMap = upiAutoPayTransactionService.statusEnquiry(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/upiAutoPayTransactionStatusEnquiryByCriteria", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> iciciUpiAutoPayDebitTransactionStatusEnquiryByCriteria(
			@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			Map<String, String> responseMap = upiAutoPayTransactionService.statusEnquiryByCriteria(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/payu/response", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> payuResponse(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			fields.clean();
			Fields internalFields = fields.removeInternalFields();
			// Refine the fields sent by internal application
			fields.removeExtraInternalFields();
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), Constants.Y_FLAG.getValue());
			// put Internal fields back
			fields.put(internalFields);
			Map<String, String> responseMap = payuSaleResponseHandler.process(fields);
			return responseMap;

		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/process/retryCallback", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void retryCallback(@RequestBody Map<String, String> reqmap) {

		try {

			Fields fields = new Fields(reqmap);
			fields.logAllFields("Retry callback Request: ");
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), Constants.Y_FLAG.getValue());

			String response = retryCallbackService.sendCallback(fields);

			logger.info("Callback response sent to client " + response);

		} catch (Exception exception) {
			logger.error("Exception in sending retry callback ", exception);

		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/merchant/P2MPayout", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> p2mPayout(@RequestBody Map<String, String> reqmap) {

		Map<String, String> responseMap = new HashMap<String, String>();

		try {

			Fields fields = new Fields(reqmap);
			fields.logAllFields("Request Received to update VPA for Payout in P2M Mode : ");

			String p2mPayIdList = PropertiesManager.propertiesMap.get("P2M_MERCHANT_PAYID");

			if (!p2mPayIdList.contains(fields.get(FieldType.PAY_ID.getName()))) {

				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.MAPPING_REQUEST_REJECTED.getCode());
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(),
						ErrorType.MAPPING_REQUEST_REJECTED.getResponseMessage());
				return responseMap;
			}

			responseMap = p2MPayoutUtil.updateMercVPAForP2mPayout(fields);

			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception in updating VPA for Payout in P2M Mode ", exception);
			responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.MAPPING_REQUEST_REJECTED.getCode());
			responseMap.put(FieldType.RESPONSE_MESSAGE.getName(),
					ErrorType.MAPPING_REQUEST_REJECTED.getResponseMessage());
			return responseMap;
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/safexpayProcess/response", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> safexpayResponse(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			fields.clean();
			Fields internalFields = fields.removeInternalFields();
			// Refine the fields sent by internal application
			fields.removeExtraInternalFields();
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), Constants.Y_FLAG.getValue());
			// put Internal fields back
			fields.put(internalFields);
			Map<String, String> responseMap = safexpaySaleResponseHandler.process(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}

	}

	@SuppressWarnings("unchecked")
	@RequestMapping(method = RequestMethod.POST, value = "/internalRefundFromFile", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, Object> internalRefundFromFile(@RequestBody Map<String, Object> reqmap) {
		Map<String, Object> finalResponseMap = null;
		List<JSONObject> jsonObjList = new ArrayList<JSONObject>();
		try {
			int totalValidEntryCount = 0;
			int totalCapture = 0;
			int totalRejected = 0;
			int totalDeclined = 0;
			int totalError = 0;
			int totalDenied = 0;
			int totalFailed = 0;
			int totalInvalid = 0;
			int totalAuthenticationFailed = 0;
			int totalAcquirerDown = 0;
			int totalFailedAtAcquirer = 0;
			int totalAcquirerTimeOut = 0;
			String userEmail = null;
			List<Map<String, String>> validDataList = (List<Map<String, String>>) reqmap.get("validData");
			List<Map<String, String>> invalidDataList = (List<Map<String, String>>) reqmap.get("inValidData");
			Integer totalFileEntry = (Integer) reqmap.get("totalFileEntry");
			Integer totalInvalidEntry = (Integer) reqmap.get("totalInvalid");
			String refundType = (String) reqmap.get("refundType");
			String sessionUserString = (String) reqmap.get("sessionUser");

			for (Map<String, String> jsonObject : validDataList) {
				totalValidEntryCount++;
				Fields fields = new Fields(jsonObject);

				userEmail = fields.get(FieldType.INTERNAL_USER_EMAIL.getName());
				fields.logAllFields("Raw Request:");
				fields.removeInternalFields();
				fields.clean();
				fields.removeExtraFields();
				// To put request blob
				String fieldsAsString = fields.getFieldsAsBlobString();
				fields.put(FieldType.INTERNAL_REQUEST_FIELDS.getName(), fieldsAsString);
				fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
				if (StringUtils.isNotEmpty(userEmail)) {
					fields.put(FieldType.INTERNAL_USER_EMAIL.getName(), userEmail);
				}

				if (fields.contains(FieldType.UDF6.getName())) {
					if (!fields.get(FieldType.UDF6.getName()).isEmpty()) {
						fields.put(FieldType.UDF6.getName(), Constants.Y_FLAG.getValue());
					}
				}
				fields.logAllFields("Refine Request:");
				Map<String, String> responseMap = router.route(fields);
				responseMap.remove(FieldType.INTERNAL_CUSTOM_MDC.getName());

				if (responseMap.get(FieldType.STATUS.getName()).equals(StatusType.CAPTURED.getName())) {
					totalCapture++;
				} else if (responseMap.get(FieldType.STATUS.getName()).equals(StatusType.REJECTED.getName())) {
					totalRejected++;
				} else if (responseMap.get(FieldType.STATUS.getName()).equals(StatusType.DECLINED.getName())) {
					totalDeclined++;
				} else if (responseMap.get(FieldType.STATUS.getName()).equals(StatusType.ERROR.getName())) {
					totalError++;
				} else if (responseMap.get(FieldType.STATUS.getName()).equals(StatusType.DENIED.getName())) {
					totalDenied++;
				} else if (responseMap.get(FieldType.STATUS.getName()).equals(StatusType.FAILED.getName())) {
					totalFailed++;
				} else if (responseMap.get(FieldType.STATUS.getName()).equals(StatusType.INVALID.getName())) {
					totalInvalid++;
				} else if (responseMap.get(FieldType.STATUS.getName())
						.equals(StatusType.AUTHENTICATION_FAILED.getName())) {
					totalAuthenticationFailed++;
				} else if (responseMap.get(FieldType.STATUS.getName()).equals(StatusType.ACQUIRER_DOWN.getName())) {
					totalAcquirerDown++;
				} else if (responseMap.get(FieldType.STATUS.getName())
						.equals(StatusType.FAILED_AT_ACQUIRER.getName())) {
					totalFailedAtAcquirer++;
				} else if (responseMap.get(FieldType.STATUS.getName()).equals(StatusType.ACQUIRER_TIMEOUT.getName())) {
					totalAcquirerTimeOut++;
				}

				JSONObject json = refundUtilityDataService.createFileEntryWithStatus(
						jsonObject.get(FieldType.PAY_ID.getName()), jsonObject.get(FieldType.ORDER_ID.getName()),
						jsonObject.get(FieldType.AMOUNT.getName()), responseMap.get(FieldType.STATUS.getName()));
				jsonObjList.add(json);
			}

			refundUtilityDataService.saveRefundFileDataStatusInDb(jsonObjList, invalidDataList, totalFileEntry,
					totalValidEntryCount, totalInvalidEntry, totalCapture, totalRejected, totalDeclined, totalError,
					totalDenied, totalFailed, totalInvalid, totalAuthenticationFailed, totalAcquirerDown,
					totalFailedAtAcquirer, totalAcquirerTimeOut, userEmail, refundType, sessionUserString);
			return finalResponseMap;
		} catch (Exception exception) {
			// Ideally this should be a non-reachable code
			logger.error("Exception", exception);

			return null;
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/cashfreeProcess/response", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> CashfreeResponse(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Cashfree Raw Request:");
			fields.clean();
			Fields internalFields = fields.removeInternalFields();
			// Refine the fields sent by internal application
			fields.removeExtraInternalFields();
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), Constants.Y_FLAG.getValue());
			// put Internal fields back
			fields.put(internalFields);
			Map<String, String> responseMap = cashfreeSaleResponseHandler.process(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);

			return null;
		}
	}

	// This controller is used for Vpa Validation
	@RequestMapping(method = RequestMethod.POST, value = "/api/vpaValidation", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> vpaValidation(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("VPA Validation Request Recieved:");
			Map<String, String> responseMap = upiPaymentsService.validateVpa(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}
	}

	// After Vpa Validation amount collection controller
	@RequestMapping(method = RequestMethod.POST, value = "/api/collect", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> collect(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Collect Request Recieved for VPA:");
			Map<String, String> responseMap = upiPaymentsService.collectAmount(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/cashfreeUpiQrProcess/upiQrResponse")
	public void cashfreeVpaResponse(HttpServletRequest request, HttpServletResponse response) {
		try {
			cashfreeUpiQrResponseHandler.cashfreeUpiQrResponseHandler(request, response);
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/apexPayProcess/response", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> apexPayResponse(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			fields.clean();
			Fields internalFields = fields.removeInternalFields();
			// Refine the fields sent by internal application
			fields.removeExtraInternalFields();
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), Constants.Y_FLAG.getValue());
			// put Internal fields back
			fields.put(internalFields);
			Map<String, String> responseMap = apexPaySaleResponseHandler.process(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}
	}

	/*
	 * @RequestMapping(method = RequestMethod.POST, value =
	 * "/vepayProcess/response", consumes = MediaType.APPLICATION_JSON_VALUE,
	 * produces = MediaType.APPLICATION_JSON_VALUE) public @ResponseBody Map<String,
	 * String> vepayResponse(@RequestBody Map<String, String> reqmap) {
	 * 
	 * Fields fields = new Fields(reqmap); fields.logAllFields("Raw Request:");
	 * fields.clean(); Fields internalFields = fields.removeInternalFields(); //
	 * Refine the fields sent by internal application
	 * fields.removeExtraInternalFields();
	 * fields.put(FieldType.IS_INTERNAL_REQUEST.getName(),
	 * Constants.Y_FLAG.getValue()); // put Internal fields back
	 * fields.put(internalFields); Map<String, String> responseMap =
	 * vepaySaleResponseHandler.process(fields); return responseMap; }
	 */

	@RequestMapping(method = RequestMethod.POST, value = "/sendCallback", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String sendCallback(@RequestBody Map<String, String> reqmap) {

		try {

			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request for sending callback: ");
			String response = retryCallbackService.sendCallbackToMerchant(fields);

			logger.info("Callback response from client " + response);

			return response;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/airPayProcess/response", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> airPayResponse(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("AirPay Raw Request:");
			fields.clean();
			Fields internalFields = fields.removeInternalFields();
			// Refine the fields sent by internal application
			fields.removeExtraInternalFields();
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), Constants.Y_FLAG.getValue());
			// put Internal fields back
			fields.put(internalFields);
			Map<String, String> responseMap = airPaySaleResponseHandler.process(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);

			return null;
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/razorpay/response", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> razorpayProcess(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			fields.clean();
			Fields internalFields = fields.removeInternalFields();
			// Refine the fields sent by internal application
			fields.removeExtraInternalFields();
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), Constants.Y_FLAG.getValue());
			// put Internal fields back
			fields.put(internalFields);
			Map<String, String> responseMap = new HashMap<String, String>();
			responseMap = razorpaySaleResponseHandler.process(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/qaicashProcess/response", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> qaicashProcess(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			fields.clean();
			Fields internalFields = fields.removeInternalFields();
			// Refine the fields sent by internal application
			fields.removeExtraInternalFields();
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), Constants.Y_FLAG.getValue());
			// put Internal fields back
			fields.put(internalFields);
			Map<String, String> responseMap = new HashMap<String, String>();
			responseMap = qaicashSaleResponseHandler.process(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/floxypayProcess/response", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> floxypayProcess(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			fields.clean();
			Fields internalFields = fields.removeInternalFields();
			// Refine the fields sent by internal application
			fields.removeExtraInternalFields();
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), Constants.Y_FLAG.getValue());
			// put Internal fields back
			fields.put(internalFields);
			Map<String, String> responseMap = new HashMap<String, String>();
			responseMap = floxypaySaleResponseHandler.process(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}

	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/grezpayProcess/response", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> grezpayProcess(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			fields.clean();
			Fields internalFields = fields.removeInternalFields();
			// Refine the fields sent by internal application
			fields.removeExtraInternalFields();
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), Constants.Y_FLAG.getValue());
			// put Internal fields back
			fields.put(internalFields);
			Map<String, String> responseMap = new HashMap<String, String>();
			responseMap = grezpaySaleResponseHandler.process(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/upigatewayProcess/response", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> upigatewayProcess(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			fields.clean();
			Fields internalFields = fields.removeInternalFields();
			// Refine the fields sent by internal application
			fields.removeExtraInternalFields();
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), Constants.Y_FLAG.getValue());
			// put Internal fields back
			fields.put(internalFields);
			Map<String, String> responseMap = new HashMap<String, String>();
			responseMap = upigatewaySaleResponseHandler.process(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}

	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/floxypayCallback/response", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void floxypayCallback(@RequestBody Map<String, String> reqmap) {

		try {

			JSONObject callbackJson = new JSONObject();

			for (Entry<String, String> entry : reqmap.entrySet()) {
				callbackJson.put(entry.getKey().toString(), entry.getValue().toString());
			}

			logger.info("Floxypay Callback Received :" + callbackJson.toString());
		} catch (Exception exception) {
			logger.error("Exception in getting floxypay callback response ", exception);
		}

	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/qaicashCallback/response", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void qaicashCallback(@RequestBody Map<String, String> reqmap) {

		try {

			JSONObject callbackJson = new JSONObject();

			for (Entry<String, String> entry : reqmap.entrySet()) {
				callbackJson.put(entry.getKey().toString(), entry.getValue().toString());
			}

			logger.info("Qaicash Callback Received for pay in {}" , callbackJson.toString());
		} catch (Exception exception) {
			logger.error("Exception in getting Qaicash callback response ", exception);
		}

	}

	
	@RequestMapping(method = RequestMethod.POST, value = "/grezpayCallback/response", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void grezpayCallback(@RequestBody Map<String, String> reqmap) {

		try {

			JSONObject callbackJson = new JSONObject();

			for (Entry<String, String> entry : reqmap.entrySet()) {
				callbackJson.put(entry.getKey().toString(), entry.getValue().toString());
			}

			logger.info("Grezpay Callback Received for pay in {}" , callbackJson.toString());
		} catch (Exception exception) {
			logger.error("Exception in getting Grezpay callback response ", exception);
		}

	}
	
	
	@RequestMapping(method = RequestMethod.POST, value = "/upigatewayCallback/response")
	public void upigatewayCallback(HttpServletRequest httpRequest, HttpServletResponse response) {

		try {

			JSONObject callbackJson = new JSONObject();
			Enumeration<String> keys = httpRequest.getSession().getAttributeNames();
			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				callbackJson.put(key, (String) httpRequest.getSession().getAttribute(key).toString());
			}
			
			logger.info("UPIGateway Callback Received for pay in {}" , callbackJson.toString());
		} catch (Exception exception) {
			logger.error("Exception in getting UPIGateway callback response ", exception);
		}

	}
	
	// Ipint server to server call for MID using merchnat details
		@RequestMapping(method = RequestMethod.POST, value = "/api/ipintMid", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
		public @ResponseBody Map<String, String> ipintMid(@RequestBody Map<String, String> reqmap) {

			try {
				Fields fields = new Fields(reqmap);
				fields.logAllFields("Collect Request Recieved for VPA:");
				Map<String, String> responseMap = ipintAggregatorMerchantService.aggregatorMerchnatProcessor(fields);
				return responseMap;
			} catch (Exception exception) {
				logger.error("Exception", exception);
				return null;
			}
		}
	
	@RequestMapping(method = RequestMethod.POST, value = "/ipintProcess/response", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> iPintResponse(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			fields.clean();
			Fields internalFields = fields.removeInternalFields();
			// Refine the fields sent by internal application
			fields.removeExtraInternalFields();
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), Constants.Y_FLAG.getValue());
			// put Internal fields back
			fields.put(internalFields);
			Map<String, String> responseMap = ipintSaleResponseHandler.process(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}

	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/toshanidigitalProcess/response", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> toshanidigitalResponse(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			fields.clean();
			Fields internalFields = fields.removeInternalFields();
			// Refine the fields sent by internal application
			fields.removeExtraInternalFields();
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), Constants.Y_FLAG.getValue());
			// put Internal fields back
			fields.put(internalFields);
			Map<String, String> responseMap = toshanidigitalSaleResponseHandler.process(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}

	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/globalpayProcess/response", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> globalpayResponse(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			fields.clean();
			Fields internalFields = fields.removeInternalFields();
			// Refine the fields sent by internal application
			fields.removeExtraInternalFields();
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), Constants.Y_FLAG.getValue());
			// put Internal fields back
			fields.put(internalFields);
			Map<String, String> responseMap = globalpaySaleResponseHandler.process(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}

	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/globalpayCallback", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String globalpayCallback(@RequestBody String reqJson,HttpServletRequest httpRequest) {

		try {
			
			logger.info("Callback Reveived for Globalpay {}" , reqJson);
			globalpayCallbackHandler.process(reqJson,httpRequest);
			return "success";
		} catch (Exception exception) {
			logger.error("Exception in handling Globalpay Callback Response", exception);
			return null;
		}

	}
	
}