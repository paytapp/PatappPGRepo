package com.paymentgateway.pg.core.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.Validator;

@Service
public class ProcessorValidatorFactory {

	@Autowired
	private FssValidator fssValidator;
	@Autowired
	private FirstDataValidator firstDataValidator;
	@Autowired
	private FederalValidator federalValidator;
	@Autowired
	private MigsValidator migsValidator;
	@Autowired
	private BobValidator bobValidator;
	@Autowired
	private KotakValidator kotakValidator;
	@Autowired
	private IdbiValidator idbiValidator;
	@Autowired
	private YesBankCbValidator yesBnakCbValidator;
	@Autowired
	private AxisBankCbValidator axisBankCbValidator;
	@Autowired
	private IdfcUpiValidator idfcUpivalidator;
	@Autowired
	private FssPayValidator fssPayValidator;
	@Autowired
	private BillDeskValidator billDeskValidator;
	@Autowired
	private ISGPayValidator isgPayValidator;
	@Autowired
	private HDFCValidator hdfcValidator;
	@Autowired
	private IciciUpiValidator iciciUpiValidator;
	@Autowired
	private PayphiValidator payphiValidator;
	@Autowired
	private PayuValidator payuValidator;
	@Autowired
	private SafexpayValidator safexpayValidator;
	@Autowired
	private CashfreeValidator cashfreeValidator;
	@Autowired
	private ApexPayValidator apexPayValidator;
	@Autowired
	private VepayValidator vepayValidator;
	@Autowired
	private AirPayValidator airPayValidator;
	@Autowired
	private RazorpayValidator razorpayValidator;
	@Autowired
	private QaicashValidator qaicashValidator;
	@Autowired
	private FloxypayValidator floxypayValidator;
	@Autowired
	private IPintValidator iPintValidator;
	@Autowired
	private P2ptspValidator p2ptspValidator;
	@Autowired
	private DigitalsolutionValidator digitalsolutionValidator;
	
	public Validator getInstance(Fields fields) {

		AcquirerType acquirer = AcquirerType.getInstancefromCode(fields.get(FieldType.ACQUIRER_TYPE.getName()));
		if (null == acquirer) {
			return null;
		}
		switch (acquirer) {
		case FSS:
			return fssValidator;
		case HDFC:
			return hdfcValidator;
		case IDFC_FIRSTDATA:
			return firstDataValidator;
		case ICICI_FIRSTDATA:
			return firstDataValidator;
		case FEDERAL:
			return federalValidator;
		case ICICIUPI:
			return iciciUpiValidator;
		case AXISMIGS:
			return migsValidator;
		case BOB:
			return bobValidator;
		case YESBANKCB:
			return yesBnakCbValidator;
		case AXISBANKCB:
			return axisBankCbValidator;
		case KOTAK:
			return kotakValidator;
		case IDBIBANK:
			return idbiValidator;
		case IDFCUPI:
			return idfcUpivalidator;
		case FSSPAY:
			return fssPayValidator;
		case BILLDESK:
			return billDeskValidator;
		case ISGPAY:
			return isgPayValidator;
		case PAYPHI:
			return payphiValidator;
		case PAYU:
			return payuValidator;
		case AXISBANK:
			return axisBankCbValidator;
		case SAFEXPAY:
			return safexpayValidator;
		case CASHFREE:
			return cashfreeValidator;
		case APEXPAY:
			return apexPayValidator;
		case VEPAY:	
			return vepayValidator;
		case AIRPAY:
			return airPayValidator;
		case RAZORPAY:
			return razorpayValidator;
		case QAICASH:
			return qaicashValidator;
		case FLOXYPAY:
			return floxypayValidator;
		case DIGITALSOLUTIONS:
		case GREZPAY:
		case IPINT:
			return iPintValidator;
		case P2PTSP:
			return p2ptspValidator;
		case UPIGATEWAY:
		case TOSHANIDIGITAL:
		case GLOBALPAY:
			return floxypayValidator;
			
		default:
			return null;
		}
	}
}
