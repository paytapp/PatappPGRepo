package com.paymentgateway.pgui.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.pgui.action.AirPayUpiResponseAction;
import com.paymentgateway.pgui.action.ApexPayResponseAction;
import com.paymentgateway.pgui.action.AxisBankUpiResponseAction;
import com.paymentgateway.pgui.action.BillDeskCardResponseAction;
import com.paymentgateway.pgui.action.BillDeskResponseAction;
import com.paymentgateway.pgui.action.BobResponseAction;
import com.paymentgateway.pgui.action.CashfreeResponseAction;
import com.paymentgateway.pgui.action.CashfreeUpiResponseAction;
import com.paymentgateway.pgui.action.CyberSourceResponseAction;
import com.paymentgateway.pgui.action.DigitalSolutionResponseAction;
import com.paymentgateway.pgui.action.FedUpiRefundResponseAction;
import com.paymentgateway.pgui.action.FedUpiResponseAction;
import com.paymentgateway.pgui.action.FederalResponseAction;
import com.paymentgateway.pgui.action.FloxypayResponseAction;
import com.paymentgateway.pgui.action.FssPayResponseAction;
import com.paymentgateway.pgui.action.GlobalpayResponseAction;
import com.paymentgateway.pgui.action.GrezpayResponseAction;
import com.paymentgateway.pgui.action.HandleEPOSResponseAction;
import com.paymentgateway.pgui.action.HandleInvoiceResponse;
import com.paymentgateway.pgui.action.HdfcResponseAction;
import com.paymentgateway.pgui.action.HdfcUpiResponseAction;
import com.paymentgateway.pgui.action.ICICIEnachResponseAction;
import com.paymentgateway.pgui.action.IPintResponseAction;
import com.paymentgateway.pgui.action.ISGPayResponseAction;
import com.paymentgateway.pgui.action.IciciResponseAction;
import com.paymentgateway.pgui.action.IciciUpiAutoPayResponseAction;
import com.paymentgateway.pgui.action.IciciUpiResponseAction;
import com.paymentgateway.pgui.action.IdbiResponseAction;
import com.paymentgateway.pgui.action.IdfcNetBankingResponseAction;
import com.paymentgateway.pgui.action.IdfcUpiResponseAction;
import com.paymentgateway.pgui.action.IsgPayUpiResponseAction;
import com.paymentgateway.pgui.action.KotakResponseAction;
import com.paymentgateway.pgui.action.KotakUpiResponseAction;
import com.paymentgateway.pgui.action.MigsResponseAction;
import com.paymentgateway.pgui.action.P2PTSPResponseAction;
import com.paymentgateway.pgui.action.PaymentGatewayResponseAction;
import com.paymentgateway.pgui.action.PayphiResponseAction;
import com.paymentgateway.pgui.action.PayuResponseAction;
import com.paymentgateway.pgui.action.QaicashResponseAction;
import com.paymentgateway.pgui.action.RazorpayResponseAction;
import com.paymentgateway.pgui.action.RazorpayUpiResponseAction;
import com.paymentgateway.pgui.action.ResponseAction;
import com.paymentgateway.pgui.action.RupayResponseAction;
import com.paymentgateway.pgui.action.SafexpayResponseAction;
import com.paymentgateway.pgui.action.ToshanidigitalResponseAction;
import com.paymentgateway.pgui.action.UpiResponseAction;
import com.paymentgateway.pgui.action.UpigatewayResponseAction;
import com.paymentgateway.pgui.action.VepayResponseAction;
import com.paymentgateway.pgui.action.YesBankUpiResponseAction;
import com.paymentgateway.pgui.action.service.ActionService;
import com.paymentgateway.pgui.action.service.PgActionServiceFactory;

@CrossOrigin
@Controller
@RequestMapping(value = "/jsp")
public class ResponseController {

	@Autowired
	private CyberSourceResponseAction cyberSourceResponseAction;

	@Autowired
	private PayphiResponseAction payphiResponseAction;

	@Autowired
	private PayuResponseAction payuResponseAction;

	@Autowired
	private BobResponseAction bobResponseAction;

	@Autowired
	private HdfcResponseAction hdfcResponseAction;

	@Autowired
	private HdfcUpiResponseAction hdfcUpiResponseAction;

	@Autowired
	private IdfcNetBankingResponseAction idfcNetBankingResponseAction;

	@Autowired
	private IdfcUpiResponseAction idfcUpiResponseAction;

	@Autowired
	private FssPayResponseAction fssPayResponseAction;

	@Autowired
	private BillDeskResponseAction billDeskResponseAction;

	@Autowired
	private BillDeskCardResponseAction billDeskCardResponseAction;

	@Autowired
	private ISGPayResponseAction isgPayResponseAction;

	@Autowired
	private UpiResponseAction upiResponseAction;

	@Autowired
	private IciciUpiResponseAction iciciUpiResponseAction;

	@Autowired
	private ResponseAction responseAction;

	@Autowired
	private RupayResponseAction rupayResponseAction;

	@Autowired
	private SafexpayResponseAction safexpayResponseAction;

	@Autowired
	private IciciResponseAction iciciResponseAction;

	@Autowired
	private FederalResponseAction federalResponseAction;

	@Autowired
	private KotakResponseAction kotakResponseAction;

	@Autowired
	private KotakUpiResponseAction kotakUpiResponseAction;

	@Autowired
	private FedUpiResponseAction fedUpiResponseAction;

	@Autowired
	private FedUpiRefundResponseAction fedUpiRefundResponseAction;

	@Autowired
	private YesBankUpiResponseAction yesBankUpiResponseAction;

	@Autowired
	private MigsResponseAction migsResponseAction;

	@Autowired
	private PaymentGatewayResponseAction paymentGatewayResponseAction;

	@Autowired
	private IdbiResponseAction idbiResponseAction;

	@Autowired
	private IciciUpiAutoPayResponseAction iciciUpiAutoPayResponseAction;

	@Autowired
	private ICICIEnachResponseAction iciciEnachResponseAction;

	@Autowired
	private CashfreeResponseAction cashfreeResponseAction;

	@Autowired
	private CashfreeUpiResponseAction cashfreeUpiResponseAction;

	@Autowired
	private AxisBankUpiResponseAction axisBankUpiResponseAction;

	@Autowired
	private HandleEPOSResponseAction handleEPOSResponseAction;

	@Autowired
	private IsgPayUpiResponseAction isgPayUpiResponseAction;

	@Autowired
	private HandleInvoiceResponse handleInvoiceResponse;

	@Autowired
	private ApexPayResponseAction apexPayResponseAction;

	@Autowired
	private VepayResponseAction vepayResponseAction;

	@Autowired
	private AirPayUpiResponseAction airPayUpiResponseAction;

	@Autowired
	private RazorpayResponseAction razorpayResponseAction;

	@Autowired
	private RazorpayUpiResponseAction razorpayUpiResponseAction;

	@Autowired
	private FloxypayResponseAction floxypayResponseAction;

	@Autowired
	private QaicashResponseAction qaicashResponseAction;

	@Autowired
	private DigitalSolutionResponseAction digitalSolutionResponseAction;

	@Autowired
	private GrezpayResponseAction grezpayResponseAction;

	@Autowired
	private IPintResponseAction iPintResponseAction;

	@Autowired
	private UpigatewayResponseAction upigatewayResponseAction;

	@Autowired
	private ToshanidigitalResponseAction toshanidigitalResponseAction;

	@Autowired
	private GlobalpayResponseAction globalpayResponseAction;

	@Autowired
	private P2PTSPResponseAction p2pTspResponseAction;

	private static Logger logger = LoggerFactory.getLogger(ResponseController.class.getName());

	@RequestMapping(value = "/response", method = RequestMethod.POST)
	public ModelAndView response(HttpServletRequest request, HttpServletResponse httpResponse) throws IOException {
		Fields newFields = new Fields();
		ModelAndView modelAndView = new ModelAndView();
		JSONObject pgPageData = new JSONObject();
		try {
			ActionService service = PgActionServiceFactory.getActionService();
			newFields = service.prepareFields(request.getParameterMap());
			String fieldHash = newFields.get(FieldType.HASH.getName());
			newFields.remove(FieldType.HASH.getName());
			if (null != fieldHash) {
				String calculatedHash = Hasher.getHash(newFields);
				if (fieldHash.equalsIgnoreCase(calculatedHash)) {
					pgPageData.put(FieldType.HASH.getName(), fieldHash);
					pgPageData.put("HASH_FLAG", "Y");
				} else {
					pgPageData.put(FieldType.HASH.getName(), fieldHash);
					pgPageData.put("HASH_FLAG", "N");
				}
			} else {
				pgPageData.put("HASH_FLAG", "N");
			}
			List<String> fieldTypeList = new ArrayList<String>(newFields.getFields().keySet());
			for (String fieldType : fieldTypeList) {
				pgPageData.put(fieldType, newFields.get(fieldType));
			}
			if (pgPageData.has(FieldType.STATUS.getName()) && pgPageData.get(FieldType.STATUS.getName()).toString()
					.equalsIgnoreCase(StatusType.INVALID.getName())) {
				if (pgPageData.has(FieldType.PG_TXN_MESSAGE.getName())) {
					pgPageData.put(FieldType.RESPONSE_MESSAGE.getName(),
							pgPageData.get(FieldType.PG_TXN_MESSAGE.getName()));
				}
			}
		} catch (SystemException e) {
			logger.info("Exception >>>>>", e);
			String path = request.getContextPath();
			logger.info(path);
			if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
				String resultPath = request.getScheme() + "://" + request.getHeader("Host") + "/pgui/jsp/error";
				httpResponse.sendRedirect(resultPath);
			}
			httpResponse.sendRedirect("error");
		}
		modelAndView.addObject("pageData", pgPageData);
		logger.info("Data from pg page to response >>> " + pgPageData);
		if (pgPageData.has(FieldType.CHECKOUT_JS_FLAG.getName())
				&& pgPageData.get(FieldType.CHECKOUT_JS_FLAG.getName()).toString().equalsIgnoreCase("Y")) {
			modelAndView.setViewName("checkoutResponse");
		} else {
			modelAndView.setViewName("response");
		}
		return modelAndView;
	}

	@RequestMapping(value = "/eposResponse", method = RequestMethod.POST)
	public ModelAndView eposResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {

		Fields newFields = new Fields();
		ModelAndView modelAndView = new ModelAndView();
		JSONObject pgPageData = new JSONObject();
		try {
			handleEPOSResponseAction.httpResponseHandler(request);
			ActionService service = PgActionServiceFactory.getActionService();
			newFields = service.prepareFields(request.getParameterMap());
			String fieldHash = newFields.get(FieldType.HASH.getName());
			newFields.remove(FieldType.HASH.getName());
			if (null != fieldHash) {
				String calculatedHash = Hasher.getHash(newFields);
				if (fieldHash.equalsIgnoreCase(calculatedHash)) {
					pgPageData.put(FieldType.HASH.getName(), fieldHash);
					pgPageData.put("HASH_FLAG", "Y");
				} else {
					pgPageData.put(FieldType.HASH.getName(), fieldHash);
					pgPageData.put("HASH_FLAG", "N");
				}
			} else {
				pgPageData.put("HASH_FLAG", "N");
			}
			List<String> fieldTypeList = new ArrayList<String>(newFields.getFields().keySet());
			for (String fieldType : fieldTypeList) {
				pgPageData.put(fieldType, newFields.get(fieldType));
			}
			if (pgPageData.has(FieldType.STATUS.getName()) && pgPageData.get(FieldType.STATUS.getName()).toString()
					.equalsIgnoreCase(StatusType.INVALID.getName())) {
				if (pgPageData.has(FieldType.PG_TXN_MESSAGE.getName())) {
					pgPageData.put(FieldType.RESPONSE_MESSAGE.getName(),
							pgPageData.get(FieldType.PG_TXN_MESSAGE.getName()));
				}
			}
		} catch (SystemException e) {
			logger.info("Exception >>>>>", e);
			String path = request.getContextPath();
			logger.info(path);
			if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
				String resultPath = request.getScheme() + "://" + request.getHeader("Host") + "/pgui/jsp/error";
				response.sendRedirect(resultPath);
			}
			response.sendRedirect("error");
		}
		modelAndView.addObject("pageData", pgPageData);
		logger.info("Data from pg page to response >>> " + pgPageData);
		if (pgPageData.has(FieldType.CHECKOUT_JS_FLAG.getName())
				&& pgPageData.get(FieldType.CHECKOUT_JS_FLAG.getName()).toString().equalsIgnoreCase("Y")) {
			modelAndView.setViewName("checkoutResponse");
		} else {
			modelAndView.setViewName("response");
		}
		return modelAndView;
	}

	@RequestMapping(value = "/invoiceResponse", method = RequestMethod.POST)
	public ModelAndView invoiceResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {

		Fields newFields = new Fields();
		ModelAndView modelAndView = new ModelAndView();
		JSONObject pgPageData = new JSONObject();
		try {

			handleInvoiceResponse.invoiceResponseHAndler(request, response);
			ActionService service = PgActionServiceFactory.getActionService();
			newFields = service.prepareFields(request.getParameterMap());
			String fieldHash = newFields.get(FieldType.HASH.getName());
			newFields.remove(FieldType.HASH.getName());
			if (null != fieldHash) {
				String calculatedHash = Hasher.getHash(newFields);
				if (fieldHash.equalsIgnoreCase(calculatedHash)) {
					pgPageData.put(FieldType.HASH.getName(), fieldHash);
					pgPageData.put("HASH_FLAG", "Y");
				} else {
					pgPageData.put(FieldType.HASH.getName(), fieldHash);
					pgPageData.put("HASH_FLAG", "N");
				}
			} else {
				pgPageData.put("HASH_FLAG", "N");
			}
			List<String> fieldTypeList = new ArrayList<String>(newFields.getFields().keySet());
			for (String fieldType : fieldTypeList) {
				pgPageData.put(fieldType, newFields.get(fieldType));
			}
			if (pgPageData.has(FieldType.STATUS.getName()) && pgPageData.get(FieldType.STATUS.getName()).toString()
					.equalsIgnoreCase(StatusType.INVALID.getName())) {
				if (pgPageData.has(FieldType.PG_TXN_MESSAGE.getName())) {
					pgPageData.put(FieldType.RESPONSE_MESSAGE.getName(),
							pgPageData.get(FieldType.PG_TXN_MESSAGE.getName()));
				}
			}
		} catch (SystemException e) {
			logger.info("Exception >>>>>", e);
			String path = request.getContextPath();
			logger.info(path);
			if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
				String resultPath = request.getScheme() + "://" + request.getHeader("Host") + "/pgui/jsp/error";
				response.sendRedirect(resultPath);
			}
			response.sendRedirect("error");
		}
		modelAndView.addObject("pageData", pgPageData);
		logger.info("Data from pg page to response >>> " + pgPageData);
		if (pgPageData.has(FieldType.CHECKOUT_JS_FLAG.getName())
				&& pgPageData.get(FieldType.CHECKOUT_JS_FLAG.getName()).toString().equalsIgnoreCase("Y")) {
			modelAndView.setViewName("checkoutResponse");
		} else {
			modelAndView.setViewName("response");
		}
		return modelAndView;
	}

	@RequestMapping(value = "/mobikwikResponse", method = RequestMethod.POST)
	public ModelAndView mobikwikResponse(HttpServletRequest request, HttpServletResponse httpResponse)
			throws IOException {
		Fields newFields = new Fields();
		ModelAndView modelAndView = new ModelAndView();
		JSONObject pgPageData = new JSONObject();
		try {
			ActionService service = PgActionServiceFactory.getActionService();
			newFields = service.prepareFields(request.getParameterMap());
			List<String> fieldTypeList = new ArrayList<String>(newFields.getFields().keySet());
			for (String fieldType : fieldTypeList) {
				pgPageData.put(fieldType, newFields.get(fieldType));
			}
		} catch (SystemException e) {
			logger.info("Exception >>>>>", e);
			String path = request.getContextPath();
			logger.info(path);
			if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
				String resultPath = request.getScheme() + "://" + request.getHeader("Host") + "/pgui/jsp/error";
				httpResponse.sendRedirect(resultPath);
			}
			httpResponse.sendRedirect("error");
		}
		modelAndView.addObject("pageData", pgPageData);
		modelAndView.setViewName("mobikwikResponse");
		return modelAndView;
	}

	@RequestMapping(value = "/iciciEnachResponse", method = RequestMethod.POST)
	public ModelAndView iciciEnachResponse(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException {
		Map<String, String> newFields = new HashMap<String, String>();
		ModelAndView modelAndView = new ModelAndView();
		JSONObject pgPageData = new JSONObject();
		newFields = iciciEnachResponseAction.iciciEnachResponseHandler(httpRequest, httpResponse);
		for (String fieldType : newFields.keySet()) {
			pgPageData.put(fieldType, newFields.get(fieldType));
		}
		modelAndView.addObject("pageData", pgPageData);
		modelAndView.setViewName("iciciEnachResponse");
		return modelAndView;
	}

	@RequestMapping(value = "/upiAutoPayResponse", method = RequestMethod.POST)
	public ModelAndView upiAutoPayResponse(HttpServletRequest request, HttpServletResponse httpResponse)
			throws IOException {
		Fields newFields = new Fields();
		ModelAndView modelAndView = new ModelAndView();
		JSONObject pgPageData = new JSONObject();
		try {
			ActionService service = PgActionServiceFactory.getActionService();
			newFields = service.prepareFields(request.getParameterMap());
			if (newFields.contains("orderId")) {
				newFields = new Fields(iciciUpiAutoPayResponseAction.responsePopUpPage(newFields.get("orderId")));
			}
			List<String> fieldTypeList = new ArrayList<String>(newFields.getFields().keySet());
			for (String fieldType : fieldTypeList) {
				pgPageData.put(fieldType, newFields.get(fieldType));
			}
		} catch (SystemException e) {
			logger.info("Exception >>>>>", e);
			String path = request.getContextPath();
			logger.info(path);
			if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
				String resultPath = request.getScheme() + "://" + request.getHeader("Host") + "/pgui/jsp/error";
				httpResponse.sendRedirect(resultPath);
			}
			httpResponse.sendRedirect("error");
		}
		modelAndView.addObject("pageData", pgPageData);
		modelAndView.setViewName("upiAutoPayResponse");
		return modelAndView;
	}

	@RequestMapping(value = "/error")
	public ModelAndView errorPage() {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("error");
		return modelAndView;
	}

	@RequestMapping(value = "/invalidResponse", method = RequestMethod.POST)
	public ModelAndView invalidResponse(HttpServletRequest request) {
		Fields newFields = new Fields();
		ModelAndView modelAndView = new ModelAndView();
		JSONObject pgPageData = new JSONObject();
		try {
			ActionService service = PgActionServiceFactory.getActionService();
			newFields = service.prepareFields(request.getParameterMap());
			String fieldHash = newFields.get(FieldType.HASH.getName());
			newFields.remove(FieldType.HASH.getName());
			String calculatedHash = Hasher.getHash(newFields);
			if (fieldHash.equalsIgnoreCase(calculatedHash)) {
				pgPageData.put(FieldType.HASH.getName(), fieldHash);
				pgPageData.put("HASH_FLAG", "Y");
			} else {
				pgPageData.put(FieldType.HASH.getName(), fieldHash);
				pgPageData.put("HASH_FLAG", "N");
			}
			List<String> fieldTypeList = new ArrayList<String>(newFields.getFields().keySet());
			for (String fieldType : fieldTypeList) {
				pgPageData.put(fieldType, newFields.get(fieldType));
			}

		} catch (SystemException e) {
			logger.info("Exception >>>>>", e);
		}
		modelAndView.addObject("pageData", pgPageData);
		modelAndView.setViewName("response");
		return modelAndView;
	}

	@RequestMapping(value = "/bobResponse", method = RequestMethod.POST)
	public void bobResponseHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
		bobResponseAction.bobResponseHandler(request, response);
	}

	@RequestMapping(value = "/hdfcResponse", method = RequestMethod.POST)
	public void hdfcResponseHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
		hdfcResponseAction.hdfcResponseHandler(request, response);
	}

	@RequestMapping(value = "/payment3ds", method = RequestMethod.POST)
	public void payment3dsHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
		responseAction.responseActionHandler(request, response);
	}

	@RequestMapping(value = "/cyberSource3ds", method = RequestMethod.POST)
	public void cyberSource3dsHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
		cyberSourceResponseAction.cyberSourceResponsehandler(request, response);
	}

	@RequestMapping(value = "/rupayResponse", method = RequestMethod.POST)
	public void rupayResponseHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
		rupayResponseAction.rupayResponseHandler(request, response);
	}

	@RequestMapping(value = "/safexpayResponse", method = RequestMethod.POST)
	public void safexpayResponseHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
		safexpayResponseAction.safexpayResponseHandler(request, response);
	}

	@RequestMapping(value = "/icici3ds", method = RequestMethod.POST)
	public void icici3dsHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
		iciciResponseAction.iciciResponseHandler(request, response);
	}

	@RequestMapping(value = "/federalMPIResponse", method = RequestMethod.POST)
	public void federalMPIResponseHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
		federalResponseAction.federalResponseHandler(request, response);
	}

	@RequestMapping(value = "/payphiResponse", method = RequestMethod.POST)
	public void payPhiResponseHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
		payphiResponseAction.payPhiResponseHandler(request, response);
	}

	@RequestMapping(value = "/payuResponse", method = RequestMethod.POST)
	public void payuResponseHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
		payuResponseAction.payuResponseHandler(request, response);
	}

	@RequestMapping(value = "/fssPayResponse", method = RequestMethod.POST)
	public void fssPayResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
		fssPayResponseAction.fssPayResponseHandler(request, response);
	}

	@RequestMapping(value = "/billdeskResponse", method = RequestMethod.POST)
	public void billdeskResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
		billDeskResponseAction.billDeskResponseHandler(request, response);
	}

	@RequestMapping(value = "/billdeskCardResponse", method = RequestMethod.POST)
	public void billdeskCardResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
		billDeskCardResponseAction.billDeskCardResponseHandler(request, response);
	}

	@RequestMapping(value = "/isgPayResponse", method = RequestMethod.POST)
	public void isgPayResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
		isgPayResponseAction.isgPayResponseHandler(request, response);
	}

	@RequestMapping(value = "/kotakResponse", method = RequestMethod.POST)
	public void lkotakResponseHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
		kotakResponseAction.kotakResponseHandler(request, response);
	}

	@RequestMapping(value = "/idbiResponse", method = RequestMethod.POST)
	public void idbiResponseHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
		idbiResponseAction.idbiResponseHandler(request, response);
	}

	@RequestMapping(value = "/paymentGatewayResponse", method = RequestMethod.POST)
	public void paymentGatewayResponseHandler(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		paymentGatewayResponseAction.paymentGatewayResponseHandler(request, response);
	}

	@RequestMapping(value = "/migsresponse", method = RequestMethod.POST)
	public void migsresponseHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
		migsResponseAction.migsResponseHandler(request, response);
	}

	@RequestMapping(value = "/hdfcUPIResponse", method = RequestMethod.POST)
	public void hdfcUpiResponseHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
		hdfcUpiResponseAction.hdfcUpiResponseHandler(request, response);
	}

	@RequestMapping(value = "/yesBankUpiResponse", method = RequestMethod.POST)
	public void yesBankUpiResponseHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
		yesBankUpiResponseAction.yesBankUpiResponseHandler(request, response);
	}

	@RequestMapping(value = "/axisbankUpiResponse", method = RequestMethod.POST)
	public void axisBankUpiResponseHandler(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		axisBankUpiResponseAction.axisBankUpiResponseHandler(request, response);
	}

	@RequestMapping(value = "/idfcBankUpiResponse", method = RequestMethod.POST)
	public void idfcBankUpiResponseHandler(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		idfcUpiResponseAction.idfcUpiResponseHandler(request, response);
	}

	@RequestMapping(value = "/fedUPIResponse", method = RequestMethod.POST)
	public void fedUPIResponseHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
		fedUpiResponseAction.fedUpiResponseHandler(request, response);
	}

	@RequestMapping(value = "/fedUPIRefundResponse", method = RequestMethod.POST)
	public void fedUPIRefundResponseHandler(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		fedUpiRefundResponseAction.fedUpiRefundResponseHandler(request, response);
	}

	@RequestMapping(value = "/idfcNBResponse", method = RequestMethod.POST)
	public void idfcNBResponseHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
		idfcNetBankingResponseAction.idfcNetBankingResponseHandler(request, response);
	}

	@RequestMapping(value = "/upiResponse", method = RequestMethod.POST)
	public void upiResponseHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
		upiResponseAction.upiResponseHandling(request, response);
	}

	@RequestMapping(value = "/iciciUpiResponseAction", method = RequestMethod.POST)
	public void iciciUpiResponseHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
		iciciUpiResponseAction.iciciUpiResponseHandler(request, response);
	}

	@RequestMapping(value = "/kotakUpiResponse", method = RequestMethod.POST)
	public void kotakUpiResponseHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
		kotakUpiResponseAction.kotakUpiResponseHandler(request, response);
	}

	@RequestMapping(value = "/cashfreeResponse", method = RequestMethod.POST)
	public void cashfreeResponseHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
		cashfreeResponseAction.cashfreeResponseHandler(request, response);
	}

	@RequestMapping(value = "/cashfreeUpiResponse", method = RequestMethod.POST)
	public void cashfreeUpiResponseHandler(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		cashfreeUpiResponseAction.cashfreeUpiResponseHandler(request, response);
	}

	@RequestMapping(value = "/isgPayCallbackResponse", method = RequestMethod.POST)
	public void isgPayCallbackResponseHandler(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		isgPayUpiResponseAction.isgPayResponseHandler(request, response);
	}

	@RequestMapping(value = "/apexPayResponse{pgRefNum}", method = RequestMethod.POST)
	public void apexPayResponseResponseHandler(@PathVariable("pgRefNum") String pgRefNum, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		apexPayResponseAction.apexPayResposneHandler(pgRefNum, request, response);
	}

	@RequestMapping(value = "/vepayResponse", method = RequestMethod.GET)
	public void vepayResponseResponseHandler(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		vepayResponseAction.vepayResponseHandler(request, response);
	}

	@RequestMapping(value = "/airpayUpiResponse", method = RequestMethod.POST)
	public void airpayUpiResponseHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
		airPayUpiResponseAction.airPayResponseHandler(request, response);
	}

	@RequestMapping(value = "/razorpayResponse", method = RequestMethod.POST)
	public void razorpayResponseResponseHandler(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		razorpayResponseAction.razorpayResponseHandler(request, response);
	}

	@RequestMapping(value = "/upiRazorpayResponse", method = RequestMethod.POST)
	public void upiRazorpayResponseHandlernotifications(HttpServletRequest request, HttpServletResponse response,
			@RequestBody String data) throws IOException {
		razorpayUpiResponseAction.razorpayUpiResponseHandler(request, response, data);
	}

	@RequestMapping(value = "/floxypayResponse", method = RequestMethod.GET)
	public void floxypayResponseHandlernotifications(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		floxypayResponseAction.floxypayResposneHandler(request, response);
	}

	@RequestMapping(value = "/qaicashResponse", method = RequestMethod.GET)
	public void qaicashResponseHandlernotifications(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		qaicashResponseAction.qaicashResposneHandler(request, response);
	}

	@RequestMapping(value = "/9digitalResponse", method = RequestMethod.POST)
	public void digitalResponseResponseHandlernotifications(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		digitalSolutionResponseAction.acquirerResposneHandler(request, response);
	}

	@RequestMapping(value = "/grezpayResponse", method = RequestMethod.GET)
	public void grezpayResponseHandlernotifications(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		grezpayResponseAction.grezpayResposneHandler(request, response);
	}

	@RequestMapping(value = "/iPintResponse", method = RequestMethod.GET)
	public void ipintResponseHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
		iPintResponseAction.ipintResponseHandler(request, response);
	}

	@RequestMapping(value = "/upigatewayResponse", method = RequestMethod.GET)
	public void upigatewayResponseHandlernotifications(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		upigatewayResponseAction.upigatewayResposneHandler(request, response);
	}

	@RequestMapping(value = "/toshaniResponse", method = RequestMethod.POST)
	public void toshaniResponseHandlernotifications(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		toshanidigitalResponseAction.toshaniResposneHandler(request, response);
	}

	@RequestMapping(value = "/globalpayResponse", method = RequestMethod.GET)
	public void globalpayResponseHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
		globalpayResponseAction.globalpayResposneHandler(request, response);
	}

	@RequestMapping(value = "/p2pTspCallbackResponse", method = RequestMethod.POST)
	public void mqrResponseHandlernotifications(HttpServletRequest request, HttpServletResponse response,
			@RequestBody String data) throws IOException {
		p2pTspResponseAction.p2pTSPResponseHandler(request, response, data);
	}
}
