package com.paymentgateway.pgui.action;

import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.pgui.action.service.FetchEmiDetailsService;

/**
 * @author Rahul
 *
 */
@Service
public class EmiSlabAction {

	@Autowired
	private FetchEmiDetailsService fetchEmiDetailsService;

	public JSONObject emiSlabRequestHandler(Map<String, String> reqMap) {

		JSONObject emiTenureJson = fetchEmiDetailsService.prepareEmiDetails(reqMap.get("issuerBank"),
				reqMap.get("payId"), reqMap.get("paymentType"), reqMap.get("amount"));
		return emiTenureJson;
	}

}
