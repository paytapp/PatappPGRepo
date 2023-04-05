package com.paymentgateway.notification.sms.service;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;

@RestController
public class SmsController {
	private static Logger logger = LoggerFactory.getLogger(SmsController.class.getName());

	/*
	 * @Autowired private SendPerformanceSmsService1 sendPerformanceSmsService1;
	 */

	@Autowired
	private UserDao userDao;

	@Autowired
	private TransactionStatusUpdateService transactionStatusUpdateService;

	@RequestMapping(method = RequestMethod.POST, value = "/sendPerformanceSms", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public void sendPerformanceSms(@RequestBody Map<String, String> reqMap) {
		Fields fields = new Fields(reqMap);
		// sendPerformanceSmsService1.sendPerformanceSms(fields.get("dateFrom").toString(),
		// fields.get("dateTo").toString(), fields.get("merchantEmailId").toString(),
		// fields.get("paymentMethods").toString(), fields.get("acquirer").toString(),
		// fields.get("smsParam").toString(), fields.get("txnType").toString());
		logger.info("Inside SendPerformanceSmsAction sendPerformanceSms Method");
		System.out.println("Inside SendPerformanceSmsAction sendPerformanceSms Method");
	}

	@RequestMapping(method = RequestMethod.POST, value = "/updateTxnStatus", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void updateTxnStatus(@RequestBody Map<String, String> reqmap) {

		Fields fields = new Fields(reqmap);
		transactionStatusUpdateService.updateTxnStatus(fields);

	}

	@RequestMapping(method = RequestMethod.POST, value = "/updateOldTxnStatus", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void updateOldTxnStatus(@RequestBody Map<String, String> reqmap) {

		transactionStatusUpdateService.updateOldTxnStatus(reqmap.get("FromDate").toString(),
				reqmap.get("ToDate").toString());

	}

	@RequestMapping(method = RequestMethod.POST, value = "/hashGen", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String hashGen(@RequestBody Map<String, String> reqMap) {
		Fields fields = new Fields(reqMap);

		if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))) {
			return "PAY_ID Is Blank";
		} else if (StringUtils.isBlank(fields.get(FieldType.ORDER_ID.getName()))) {
			return "ORDER_ID Is Blank";
		} else if (StringUtils.isBlank(fields.get(FieldType.AMOUNT.getName()))) {
			return "AMOUNT Is Blank";
		} else if (StringUtils.isBlank(fields.get(FieldType.TXNTYPE.getName()))) {
			return "TXNTYPE Is Blank";
		} else if (StringUtils.isBlank(fields.get(FieldType.CURRENCY_CODE.getName()))) {
			return "CURRENCY_CODE Is Blank";
		}

		else {

			try {
				return "HASH == " + Hasher.getHash(fields);

			} catch (SystemException e) {
				e.printStackTrace();
				return "Unable to generate HASH";
			}
		}

	}
}
