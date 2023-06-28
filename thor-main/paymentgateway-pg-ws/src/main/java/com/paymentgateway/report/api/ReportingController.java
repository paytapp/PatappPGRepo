package com.paymentgateway.report.api;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;

@RestController
@RequestMapping("/report")
public class ReportingController {

	private static Logger logger = LoggerFactory.getLogger(ReportingController.class.getName());

	@Autowired
	private ReportingApiUtils reportingApiUtils;
	
	@Autowired
	PropertiesManager propertiesManager;

	@RequestMapping(method = RequestMethod.POST, value = "/transactionReport", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Object saleRefundSettledReport(@RequestBody Map<String, String> reqMap) {
		Object result = new Object();
		try {
			Fields fields = new Fields(reqMap);

			fields.logAllFieldsPayOut("Reporting Api Raw Request:");
			fields.clean();
			Map<String, String> validationMap = reportingApiUtils.validateFields(fields);
			
			if(!validationMap.isEmpty()) {
				return validationMap;
			}
			
			boolean hashResult = reportingApiUtils.validateHash(fields);
			if (!hashResult) {
				try {
				fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("Hash Invalid For Request " + " fields are " + fields.maskFieldsRequest(fields.getFields()));
				}catch(Exception ex) {
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseMessage());
				}
				return fields.getFields();
			}
			
			result = reportingApiUtils.getData(fields);
			
		} catch (Exception e) {
			logger.error("exception in reporting api ", e);
		}
		return result;
	}

}
