package com.paymentgateway.pgui.action.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.IssuerDetailsDao;
import com.paymentgateway.commons.user.IssuerDetails;

/**
 * @author Rahul
 *
 */

@Service
public class FetchEmiDetailsService {

	public JSONObject prepareEmiDetails(String issuerBank, String payId, String paymentType, String amount) {

		IssuerDetailsDao issuerDetailsDao = new IssuerDetailsDao();
		List<IssuerDetails> slabList = new ArrayList<IssuerDetails>();
		slabList = issuerDetailsDao.fetchEmiSlab(issuerBank, payId, paymentType);
		JSONObject requestParameterJson = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		for (IssuerDetails slab : slabList) {

			JSONObject req = new JSONObject();
			double txnAmount = Double.parseDouble(amount);
			double rateOfInterest = Double.parseDouble(slab.getRateOfInterest());
			rateOfInterest = rateOfInterest / (12 * 100);
			double tenure = Double.parseDouble(slab.getTenure());

			double emi =0;
			if(rateOfInterest!=0) {
			emi =(txnAmount * rateOfInterest * Math.pow(1 + rateOfInterest, tenure))
					/ (Math.pow(1 + rateOfInterest, tenure) - 1);
			}else {
				emi =(txnAmount /tenure);
						
			}
			
			BigDecimal monthlyEmi = new BigDecimal(emi).setScale(0, BigDecimal.ROUND_HALF_UP);
			double totalAmount = emi * tenure;
			BigDecimal totalEMIAmount = new BigDecimal(totalAmount).setScale(0, BigDecimal.ROUND_HALF_UP);
			BigDecimal interest = totalEMIAmount.subtract(new BigDecimal(txnAmount));
			interest = interest.setScale(0, BigDecimal.ROUND_HALF_UP);
			/*
			 * rateOfInterest = ((double) rateOfInterest) / 100; int totalMonths = 12; int
			 * tenure = Integer.parseInt(slab.getTenure());
			 * 
			 * double tenureYearly = ((double) tenure) / totalMonths;
			 * 
			 * double emiAmount = txnAmount * Math.pow(1 + (rateOfInterest / totalMonths),
			 * tenureYearly * totalMonths); BigDecimal txnAmountAfterRoundOff = new
			 * BigDecimal(emiAmount).setScale(2, BigDecimal.ROUND_HALF_UP); BigDecimal
			 * tenureInDecimal = new BigDecimal(tenure);
			 * 
			 * BigDecimal emiPerMonth = txnAmountAfterRoundOff.divide(tenureInDecimal, 2,
			 * RoundingMode.HALF_UP);
			 */
			req.put("tenure", slab.getTenure());
			req.put("perMonthEmiAmount", monthlyEmi);
			req.put("totalEmiAmount", totalEMIAmount);
			req.put("rateOfInterest", slab.getRateOfInterest());
			req.put("interest", interest);

			jsonArray.put(req);

		}
		requestParameterJson.put("emiSlab", jsonArray);
		return requestParameterJson;

	}

}
