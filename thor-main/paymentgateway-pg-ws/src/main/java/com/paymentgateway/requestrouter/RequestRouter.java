package com.paymentgateway.requestrouter;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.pg.core.util.ProcessManager;
import com.paymentgateway.pg.core.util.Processor;
import com.paymentgateway.pg.core.util.ResponseCreator;

//@Service
public class RequestRouter {

	@Autowired
	@Qualifier("securityProcessor")
	private Processor securityProcessor;

	@Autowired
	@Qualifier("historyProcessor")
	private Processor historyProcessor;

	@Autowired
	@Qualifier("fssProcessor")
	private Processor fssProcessor;

	@Autowired
	@Qualifier("fssPayProcessor")
	private Processor fssPayProcessor;


	@Autowired
	@Qualifier("firstDataProcessor")
	private Processor firstDataProcessor;

	@Autowired
	@Qualifier("federalProcessor")
	private Processor federalProcessor;

	@Autowired
	@Qualifier("updateProcessor")
	private Processor updateProcessor;

	@Autowired
	@Qualifier("merchantHostedProcessor")
	private Processor merchantHostedProcessor;

	@Autowired
	@Qualifier("fraudProcessor")
	private Processor fraudProcessor;

	@Autowired
	@Qualifier("bobProcessor")
	private Processor bobProcessor;

	@Autowired
	@Qualifier("cyberSourceProcessor")
	private Processor cyberSourceProcessor;

	@Autowired
	@Qualifier("kotakProcessor")
	private Processor kotakProcessor;
	
	@Autowired
	@Qualifier("idbiProcessor")
	private Processor idbiProcessor;

	@Autowired
	@Qualifier("billDeskProcessor")
	private Processor billDeskProcessor;

	@Autowired
	@Qualifier("idfcUpiProcessor")
	private Processor idfcUpiProcessor;

	@Autowired
	@Qualifier("googlePayProcessor")
	private Processor googlePayProcessor;

	@Autowired
	@Qualifier("isgPayProcessor")
	private Processor isgPayProcessor;

	@Autowired
	@Qualifier("payuProcessor")
	private Processor payuProcessor;

	@Autowired
	@Qualifier("hdfcProcessor")
	private Processor hdfcProcessor;

	@Autowired
	@Qualifier("iciciUpiProcessor")
	private Processor iciciUpiProcessor;

	@Autowired
	@Qualifier("payphiProcessor")
	private Processor payphiProcessor;

	@Autowired
	@Qualifier("safexpayProcessor")
	private Processor safexpayProcessor;

	@Autowired
	@Qualifier("cashfreeProcessor")
	private Processor cashfreeProcessor;
	
	@Autowired
	@Qualifier("apexPayProcessor")
	private Processor apexPayProcessor;
	
	@Autowired
	@Qualifier("vepayProcessor")
	private Processor vepayProcessor;
	
	@Autowired
	@Qualifier("airPayProcessor")
	private Processor airPayProcessor;
	
	@Autowired
	@Qualifier("razorpayProcessor")
	private Processor razorpayProcessor;
	
	@Autowired
	@Qualifier("qaicashProcessor")
	private Processor qaicashProcessor;
	
	@Autowired
	@Qualifier("floxypayProcessor")
	private Processor floxypayProcessor;
	
	@Autowired
	@Qualifier("digitalsolutionProcessor")
	private Processor digitalsolutionProcessor;
	
	@Autowired
	@Qualifier("grezpayProcessor")
	private Processor grezpayProcessor;
	
	@Autowired
	@Qualifier("upigatewayProcessor")
	private Processor upigatewayProcessor;
	
	@Autowired
	@Qualifier("p2PTSPProcessor")
	private Processor p2PTSPProcessor;

	@Autowired
	@Qualifier("ipintProcessor")
	private Processor ipintProcessor;
	
	@Autowired
	@Qualifier("toshanidigitalProcessor")
	private Processor toshanidigitalProcessor;
	
	@Autowired
	@Qualifier("globalpayProcessor")
	private Processor globalpayProcessor;
	
	
	@Autowired
	private ResponseCreator responseCreator;

	Fields fields = null;

	public RequestRouter(Fields fields) {
		this.fields = fields;
	}

	public Map<String, String> route(Fields fields) {

		String shopifyFlag = fields.get(FieldType.INTERNAL_SHOPIFY_YN.getName());
		String flagPay = fields.get(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());

		// Process security
		ProcessManager.flow(securityProcessor, fields, false);

		// fraud Prevention processor
		ProcessManager.flow(fraudProcessor, fields, false);

		// History Processor
		ProcessManager.flow(historyProcessor, fields, false);

		// Insert new order
		ProcessManager.flow(merchantHostedProcessor, fields, false);

		// Process transaction with FSS
		ProcessManager.flow(fssProcessor, fields, false);

		// Process transaction with FSS
		ProcessManager.flow(fssPayProcessor, fields, false);

		
		// Process transaction with FirstData
		ProcessManager.flow(firstDataProcessor, fields, false);

		// Process transaction with FEDERAL
		ProcessManager.flow(federalProcessor, fields, false);

		// Process transaction with BOB
		ProcessManager.flow(bobProcessor, fields, false);


		// Process transaction with IDBI
		ProcessManager.flow(idbiProcessor, fields, false);

		// Process transaction with IDBIUPI
		ProcessManager.flow(idfcUpiProcessor, fields, false);

		// Process transaction with GOOGLEPAY
		ProcessManager.flow(googlePayProcessor, fields, false);

		// Process transaction with YESBANKCB
		ProcessManager.flow(cyberSourceProcessor, fields, false);

		// Process transaction with KOTAK
		ProcessManager.flow(kotakProcessor, fields, false);

		// Process transaction with BillDesk
		ProcessManager.flow(billDeskProcessor, fields, false);
	
		// Process transaction with ISGPAY
		ProcessManager.flow(isgPayProcessor, fields, false);

		// Process transaction with HDFC
		ProcessManager.flow(hdfcProcessor, fields, false);

		// Process transaction with ICICI UPI
		ProcessManager.flow(iciciUpiProcessor, fields, false);

		// Process transaction with Payphi
		ProcessManager.flow(payphiProcessor, fields, false);

		// Process transaction with PAYU
		ProcessManager.flow(payuProcessor, fields, false);

		// Process transaction with Safexpay
		ProcessManager.flow(safexpayProcessor, fields, false);

		// Process transaction with APEXPAY
		ProcessManager.flow(apexPayProcessor, fields, false);

		// Process transaction with Cashfree
		ProcessManager.flow(cashfreeProcessor, fields, false);

		// Process transaction with vepay
		ProcessManager.flow(vepayProcessor, fields, false);
		
		// Process transaction with Airpay
		ProcessManager.flow(airPayProcessor, fields, false);
	
		// Process transaction with Razorpay
		ProcessManager.flow(razorpayProcessor, fields, false);
			
		// Process transaction with Qaicash
		ProcessManager.flow(qaicashProcessor, fields, false);
				
		// Process transaction with Floxypay
		ProcessManager.flow(floxypayProcessor, fields, false);

		// Process transaction with Digital solution
		ProcessManager.flow(digitalsolutionProcessor, fields, false);
		
		// Process transaction with Grezpay
		ProcessManager.flow(grezpayProcessor, fields, false);
		
		// Process transaction with upigateway
		ProcessManager.flow(upigatewayProcessor, fields, false);
		
		// Process transaction with ToshaniDigital
		ProcessManager.flow(toshanidigitalProcessor, fields, false);
				
		// Process transaction with IPINT
		ProcessManager.flow(ipintProcessor, fields, false);
				
		// Process transaction with Globalpay
		ProcessManager.flow(globalpayProcessor, fields, false);
		
		// Process transaction with P2P
		ProcessManager.flow(p2PTSPProcessor, fields, false);
		
		// Update processor
		ProcessManager.flow(updateProcessor, fields, true);
				
		// Generate response for user
		createResponse(fields);

		if (!StringUtils.isEmpty(shopifyFlag)) {
			fields.put(FieldType.INTERNAL_SHOPIFY_YN.getName(), shopifyFlag);
		} else if (!StringUtils.isEmpty(flagPay)) {
			fields.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), flagPay);
		}
		return fields.getFields();
	}

	public void createResponse(Fields fields) {
		responseCreator.create(fields);
	}
}
