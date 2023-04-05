package com.paymentgateway.pgui.action;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.pg.core.util.IciciUtil;

public class IciciECollectionResponseActrion extends AbstractSecureAction implements ServletRequestAware{
	
	static final long serialVersionUID = -4911067009657314343L;
	private static Logger logger = LoggerFactory.getLogger(IciciECollectionResponseActrion.class.getName());

	@Autowired
	private IciciUtil iciciUtils;
	
	@Autowired
	TransactionControllerServiceProvider transactionControllerServiceProvider;
	
	private Fields responseMap = null;
	private HttpServletRequest httpRequest;
	
	@Override
	public void setServletRequest(HttpServletRequest hReq) {
		this.httpRequest = hReq;
	}
	public IciciECollectionResponseActrion() {
	}
	
//	@SuppressWarnings("unchecked")
//	public Map<String, String> ecollectionResponse() {
//		
//		Map<String, String> responseMap=null;
//		try {
//			
//			Map<String, String> obj = (Map<String, String>) JSONUtil.deserialize(httpRequest.getReader());
////			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
////			String json = ow.writeValueAsString(obj);
////			JSONObject res = new JSONObject(json);
////			
////			
////			
////			Map<String, String[]> fieldMapObj = httpRequest.getParameterMap();
////			//Map<String, String> requestMap = new HashMap<String, String>();
////			logger.info("Response of ecollection recieved : " + fieldMapObj);
//			String stringValueArray = null;
//			String encryptedstring = null;
//			
//
//			for (Map.Entry<String, String> entry : obj.entrySet()) {
//				encryptedstring = entry.getValue();
//		
////					for(String val : stringValueArray) {
////						//encryptedstring =val;
////					}
//			}
//			logger.info("Encrypted Response of ecollection received from icici : " + encryptedstring);
//			
//			String decryptedString = iciciUtils.decrypt(encryptedstring); 
//			logger.info("Decrypted Response of ecollection : " + decryptedString);
//			
//			Fields fields = new Fields();
//		//	fields.put(FieldType.ECOLLECT_RESPONSE_FIELD.getName(), decryptedString);
//			fields.logAllFields("ICICI E-Collection Response Recieved :");
//			
//			responseMap = transactionControllerServiceProvider.eCollectionTransact(decryptedString,
//					Constants.TXN_WS_ICICIECOLLECTION_PROCESSOR.getValue());
//			//String response = iciciUtils.eCollectionEncrypt(request);
//		}catch(Exception e) {
//			logger.info("Exception : " + e);
//		}
//		return responseMap;
//	}
}
