package com.paymentgateway.pgui.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.paymentgateway.commons.api.BindbControllerServiceProvider;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;

@Controller
@RequestMapping(value = "/jsp")
public class BinResolverController {

	private static Logger logger = LoggerFactory.getLogger(BinResolverController.class.getName());

	@Autowired
	private BindbControllerServiceProvider bindbControllerServiceProvider;

	@RequestMapping(value = "/binResolver", method = RequestMethod.POST)
	public @ResponseBody Map<String, String> binResolver(HttpServletRequest request,
			@RequestBody Map<String, String> reqmap) {
		Map<String, String> binRangeResponseMap = new HashMap<String, String>();
		Map<String, String> binResponse = new HashMap<String, String>();
		try {
			binRangeResponseMap = bindbControllerServiceProvider.binfind(reqmap.get("bin"), reqmap.get("payId"));
			request.getSession().setAttribute(Constants.BIN.getValue(), binRangeResponseMap);
			binResponse.put("cardHolderType", binRangeResponseMap.get(FieldType.CARD_HOLDER_TYPE.getName()));
			binResponse.put("issuerBankName", binRangeResponseMap.get(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()));
			binResponse.put("issuerCountry", binRangeResponseMap.get(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()));
			binResponse.put("mopType", binRangeResponseMap.get(FieldType.MOP_TYPE.getName()));
			binResponse.put("paymentType", binRangeResponseMap.get(FieldType.PAYMENT_TYPE.getName()));
			binResponse.put("paymentsRegion", binRangeResponseMap.get(FieldType.PAYMENTS_REGION.getName()));
		} catch (Exception e) {
			logger.error("Exception = ", e);
		}
		return binResponse;
	}

	@RequestMapping(value = "/emiBinResolver", method = RequestMethod.POST)
	public @ResponseBody Map<String, String> emiBinResolver(HttpServletRequest request,
			@RequestBody Map<String, String> reqmap) {
		Map<String, String> binRangeResponseMap = new HashMap<String, String>();
		Map<String, String> binResponse = new HashMap<String, String>();
		try {
			binRangeResponseMap = bindbControllerServiceProvider.emiBinFind(reqmap.get("bin"));
			request.getSession().setAttribute(Constants.BIN.getValue(), binRangeResponseMap);
			binResponse.put("cardHolderType", binRangeResponseMap.get(FieldType.CARD_HOLDER_TYPE.getName()));
			binResponse.put("issuerBankName", binRangeResponseMap.get(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()));
			binResponse.put("issuerCountry", binRangeResponseMap.get(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()));
			binResponse.put("mopType", binRangeResponseMap.get(FieldType.MOP_TYPE.getName()));
			binResponse.put("paymentType", binRangeResponseMap.get(FieldType.PAYMENT_TYPE.getName()));
			binResponse.put("paymentsRegion", binRangeResponseMap.get(FieldType.PAYMENTS_REGION.getName()));
		} catch (Exception e) {
			logger.error("Exception = ", e);
		}
		return binResponse;
	}
}
