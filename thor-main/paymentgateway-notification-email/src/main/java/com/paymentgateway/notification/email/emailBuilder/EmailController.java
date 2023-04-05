package com.paymentgateway.notification.email.emailBuilder;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.paymentgateway.commons.user.EmailData;
import com.paymentgateway.commons.user.Invoice;
import com.paymentgateway.commons.user.ResponseObject;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.MPAStatusEmail;
import com.paymentgateway.commons.util.MailersObject;
import com.paymentgateway.notification.email.emailApi.EmailApi;

@RestController
public class EmailController {
	@Autowired
	private EmailBuilder emailBuilder;

	@Autowired
	private EmailApi emailApi;
	
	//private static Logger logger = LoggerFactory.getLogger(EmailController.class.getName());
	
	@RequestMapping(method = RequestMethod.POST,value = "/emailValidator")
	public ResponseObject  emailValidator(@RequestBody final ResponseObject responseObject) throws Exception{
		   emailBuilder.emailValidator(responseObject);
		 return responseObject;
	}
	
	
	@RequestMapping(method = RequestMethod.POST,value = "/emailAddUser/{firstName}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseObject  addUser(@RequestBody ResponseObject responseObject, @PathVariable String firstName) throws Exception{
	
		   emailBuilder.emailAddUser(responseObject, firstName);
		 return responseObject;
	}
	
	
	@RequestMapping(method = RequestMethod.POST,value = "/demoMandateSign", consumes = MediaType.APPLICATION_JSON_VALUE)
	public String demoeNACHSign(@RequestBody Map<String, String> requestField) throws Exception {
	
		String response;
		response = emailBuilder.eMandateSign(new Fields(requestField));
		 return response;
	}
	
	@RequestMapping(method = RequestMethod.POST,value = "/demoUpiAutoPayMandateSign", consumes = MediaType.APPLICATION_JSON_VALUE)
	public String demoUpiAutoPayMandateSign(@RequestBody Map<String, String> requestField) throws Exception {
	
		String response;
		response = emailBuilder.upiAutoPayMandateSign(new Fields(requestField));
		 return response;
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/emailPasswordChange/{emailId}",consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseObject  passwordChange(@RequestBody  ResponseObject responseObject, @PathVariable String emailId) throws Exception{
		   emailBuilder.emailPasswordChange(responseObject, emailId);
		 return responseObject;
		
	}
	@RequestMapping(method = RequestMethod.POST,value = "/passwordResetEmail",consumes = MediaType.APPLICATION_JSON_VALUE)
	public void passwordReset(@RequestParam String accountValidationID,@RequestParam String email) throws Exception{
		emailBuilder.passwordResetEmail(accountValidationID,email);
		
	}
	
	@RequestMapping(method = RequestMethod.POST,value = "/invoiceLinkEmail", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void invoiceLink(@RequestBody Invoice invoice){
		emailBuilder.invoiceLinkEmail(invoice);
		
	}
	
	@RequestMapping(method = RequestMethod.POST,value = "/remittanceProcessEmail", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void remittanceProcess(@RequestParam String utr, @RequestParam String payId, @RequestParam String merchant,
			@RequestParam String datefrom, @RequestParam String netAmount,@RequestParam String remittedDate, @RequestParam String remittedAmount,
			@RequestParam String status){
		 emailBuilder.remittanceProcessEmail(utr,payId,merchant,datefrom,netAmount,remittedDate,remittedAmount,status);
		
	}
	
	@RequestMapping(method = RequestMethod.POST,value = "/sendBulkEmailServiceTaxUpdate" , consumes = MediaType.APPLICATION_JSON_VALUE)
	public void sendBulkEmailServiceTax(@RequestParam String emailID, @RequestParam String subject, @RequestParam String messageBody){
		emailBuilder.sendBulkEmailServiceTaxUpdate(emailID,subject,messageBody);
		
	}
	
	@SuppressWarnings("rawtypes")
	@RequestMapping(method = RequestMethod.POST,value = "/sendPaymentAdviseEmail" , consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String , String> sendPaymentAdviseEmail(@RequestBody Map reqParam){
		
		return emailBuilder.sendPaymentAdvieReport(reqParam.get("subMerchantPayId"), reqParam.get("merchantPayId"), reqParam.get("sessionUserPayId"),
				reqParam.get("payoutDate"), reqParam.get("currency"));
		
//		emailBuilder.sendPaymentAdvieReport(reqParam.get(Constants.EMAIL.getValue()), reqParam.get(Constants.SUBJECT.getValue()), 
//				reqParam.get(Constants.MESSAGE_BODY.getValue()), 
//				reqParam.get(Constants.FILE_NAME.getValue()), reqParam.get("SXSSFWorkbook"), reqParam.get("merchantPayId"), reqParam.get("sessionUserPayId"),
//				reqParam.get("dateFromObj"), reqParam.get("dateToObj"));
		
		
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/emailPendingRequest", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody void emailPendinRequest(@RequestBody Map<String, String> requestMap) {
		emailBuilder.sendPendingRequestUpdateEmail(requestMap);
		
	}
	
	@RequestMapping(method = RequestMethod.POST,value = "/mpaEmailSender" , consumes = MediaType.APPLICATION_JSON_VALUE)
	public void sendMPAEmail(@RequestBody MPAStatusEmail mpaStatusEmail){
		emailBuilder.sendMPAEmail(mpaStatusEmail);
	}
	
	@RequestMapping(method = RequestMethod.POST,value = "/emailApi" , consumes = MediaType.APPLICATION_JSON_VALUE)
	public void emailApi(@RequestBody EmailData email){
		emailApi.sendEmail(email);
		
	}
	
	@RequestMapping(method = RequestMethod.POST,value = "/multipleUserEmailSender" , consumes = MediaType.APPLICATION_JSON_VALUE)
	public void sendMultipleUserSystemDownEmail(@RequestBody MailersObject mailersObject){
		emailBuilder.sendMultipleUserSystemDownEmail(mailersObject);
	}
	
}
