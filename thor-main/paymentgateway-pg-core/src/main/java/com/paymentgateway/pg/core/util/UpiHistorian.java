package com.paymentgateway.pg.core.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class UpiHistorian {

	private static Logger logger = LoggerFactory.getLogger(UpiHistorian.class.getName());

	@Autowired
	private Fields field;

	public void findPrevious(Fields fields) throws SystemException {

		switch (AcquirerType.getInstancefromCode(fields.get(FieldType.ACQUIRER_TYPE.getName()))) {
		case FEDERAL:
			findFieldsForFederal(fields);
			break;
		case FSS:
			findFieldsForHdfc(fields);
			break;
		case YESBANKCB:
			findFieldsForYesBankCb(fields);
			break;
		case IDFCUPI:
			findFieldsForIdfcUpi(fields);
			break;
		case KOTAK:
			findFieldsForKotak(fields);
			break;
		case ICICIUPI:
			findFieldsForIciciUpi(fields);
			break;
		default:
			break;
		}

		populateFieldsFromPrevious(fields);
	}

	private void findFieldsForIciciUpi(Fields fields) {
		String refId = fields.get(FieldType.PG_REF_NUM.getName());
		if (null != refId) {
			String txnType = fields.get(FieldType.TXNTYPE.getName());
			if (txnType.equals(TransactionType.SALE.getName())) {
				try {
					field.refreshPreviousForIciciUpiSale(fields);
				} catch (SystemException e) {
					logger.info("Exception in UpiHistorian " + e.getMessage());
				}
			}
		}
	}
	
	public void findPreviousForRefund(Fields fields) throws SystemException {

		switch (AcquirerType.getInstancefromCode(fields.get(FieldType.ACQUIRER_TYPE.getName()))) {
		case FEDERAL:
			findFieldsForFederalRefund(fields);
			break;
		case FSS:
			findFieldsForHdfc(fields);
			break;
		case YESBANKCB:
			findFieldsForYesBankCb(fields);
			break;
		case IDFCUPI:
			findFieldsForIdfcUpi(fields);
			break;
		case KOTAK:
			findFieldsForKotak(fields);
			break;
		default:
			break;
		}

		populateFieldsFromPrevious(fields);
	}

	public void findFieldsForFederal(Fields fields) {

		String refId = fields.get(FieldType.UDF5.getName());
		if (null != refId) {
			String txnType = fields.get(FieldType.TXNTYPE.getName());
			if (txnType.equals(TransactionType.SALE.getName())) {
				try {
					field.refreshPreviousForFedUpi(fields);
				} catch (SystemException e) {
					logger.info("Exception in UpiHistorian " + e.getMessage());
				}
			} // if

		}

	}

	public void findFieldsForFederalRefund(Fields fields) {

		String refId = fields.get(FieldType.UDF5.getName());
		if (null != refId) {
			String txnType = fields.get(FieldType.TXNTYPE.getName());
			if (txnType.equals(TransactionType.REFUND.getName())) {
				try {
					field.refreshPreviousForFedUpiRefund(fields);
				} catch (SystemException e) {
					logger.info("Exception in UpiHistorian " + e.getMessage());
				}
			} // if

		}

	}

	public void findFieldsForHdfc(Fields fields) {

		String refId = fields.get(FieldType.PG_REF_NUM.getName());
		if (null != refId) {
			String txnType = fields.get(FieldType.TXNTYPE.getName());
			if (txnType.equals(TransactionType.SALE.getName())) {
				try {
					field.refreshPreviousForHdfcUpi(fields);
				} catch (SystemException e) {
					logger.info("Exception in UpiHistorian " + e.getMessage());
				}
			} // if

		}

	}

	public void findFieldsForYesBankCb(Fields fields) {

		String refId = fields.get(FieldType.PG_REF_NUM.getName());
		if (null != refId) {
			String txnType = fields.get(FieldType.TXNTYPE.getName());
			if (txnType.equals(TransactionType.SALE.getName())) {
				try {
					field.refreshPreviousForHdfcUpi(fields);
				} catch (SystemException e) {
					logger.info("Exception in UpiHistorian " + e.getMessage());
				}
			} // if

		}

	}

	public void findFieldsForIdfcUpi(Fields fields) {

		String refId = fields.get(FieldType.PG_REF_NUM.getName());
		if (null != refId) {
			String txnType = fields.get(FieldType.TXNTYPE.getName());
			if (txnType.equals(TransactionType.SALE.getName())) {
				try {
					field.refreshPreviousForIdfcUpiSale(fields);
				} catch (SystemException e) {
					logger.info("Exception in UpiHistorian " + e.getMessage());
				}
			} // if

		}

	}

	public void findFieldsForKotak(Fields fields) {

		String refId = fields.get(FieldType.PG_REF_NUM.getName());
		if (null != refId) {
			String txnType = fields.get(FieldType.TXNTYPE.getName());
			if (txnType.equals(TransactionType.SALE.getName())) {
				try {
					field.refreshPreviousForHdfcUpi(fields);
				} catch (SystemException e) {
					logger.info("Exception in UpiHistorian " + e.getMessage());
				}
			} // if

		}

	}

	public void populateFieldsFromPrevious(Fields fields) throws SystemException {
		Fields previous = fields.getPrevious();
		if (null != previous && previous.size() > 0) {
			// ORDER_ID in this request is required from previous request, this
			// will allow to link support
			// transactions to link to original transactions
			fields.put(FieldType.ORDER_ID.getName(), previous.get(FieldType.ORDER_ID.getName()));

			// Currency Code is required to process amount formating in support
			// transactions
			String currencyCode = previous.get(FieldType.CURRENCY_CODE.getName());
			if (null != currencyCode) {
				fields.put(FieldType.CURRENCY_CODE.getName(), currencyCode);
			}

			// get PG_REF_NO for capture
			String pgRefNo = previous.get(FieldType.PG_REF_NUM.getName());
			if (null != pgRefNo) {
				fields.put(FieldType.PG_REF_NUM.getName(), pgRefNo);
			}

			String acqId = previous.get(FieldType.ACQ_ID.getName());
			if (null != acqId) {
				fields.put(FieldType.ACQ_ID.getName(), acqId);
			}
			
			// OID of original transaction
			String oid = previous.get(FieldType.OID.getName());
			if (null != oid) {
				fields.put(FieldType.OID.getName(), oid);
			}

			// Mop type of original transaction
			String mopType = previous.get(FieldType.MOP_TYPE.getName());
			if (null != mopType) {
				fields.put(FieldType.MOP_TYPE.getName(), mopType);
			}

			// Payment type of original transaction
			String paymentType = previous.get(FieldType.PAYMENT_TYPE.getName());
			if (null != paymentType) {
				fields.put(FieldType.PAYMENT_TYPE.getName(), paymentType);
			}

			String internalOrigTxnType = previous.get(FieldType.ORIG_TXNTYPE.getName());
			if (null != paymentType) {
				fields.put(FieldType.ORIG_TXNTYPE.getName(), internalOrigTxnType);
			}

			String amount = previous.get(FieldType.AMOUNT.getName());
			if (null != amount) {
				fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(amount, currencyCode));
			}

			String custPhone = previous.get(FieldType.CUST_PHONE.getName());
			if (null != custPhone) {
				fields.put(FieldType.CUST_PHONE.getName(), custPhone);
			}

			String desc = previous.get(FieldType.PRODUCT_DESC.getName());
			if (null != desc) {
				fields.put(FieldType.PRODUCT_DESC.getName(), desc);
			}

			String email = previous.get(FieldType.CUST_EMAIL.getName());
			if (null != email) {
				fields.put(FieldType.CUST_EMAIL.getName(), email);
			}
			
			String payerAddress = previous.get(FieldType.PAYER_ADDRESS.getName());
			if (null != payerAddress) {
				fields.put(FieldType.PAYER_ADDRESS.getName(), payerAddress);
			}
			
			String custId = previous.get(FieldType.CUST_ID.getName());
			if (null != custId) {
				fields.put(FieldType.CUST_ID.getName(), custId);
			}
			
			String refundDays = previous.get(FieldType.REFUND_DAYS.getName());
			if (null != refundDays) {
				fields.put(FieldType.REFUND_DAYS.getName(), refundDays);
			}
			
			String vendorId = previous.get(FieldType.VENDOR_ID.getName());
			if (null != vendorId) {
				fields.put(FieldType.VENDOR_ID.getName(), vendorId);
			}
			
			String prductPrice = previous.get(FieldType.PRODUCT_PRICE.getName());
			if (null != prductPrice) {
				fields.put(FieldType.PRODUCT_PRICE.getName(), prductPrice);
			}

			String name = previous.get(FieldType.CUST_NAME.getName());
			if (null != name) {
				fields.put(FieldType.CUST_NAME.getName(), name);
			}

			String returnUrl = previous.get(FieldType.RETURN_URL.getName());
			if (null != returnUrl) {
				fields.put(FieldType.RETURN_URL.getName(), returnUrl);
			}

			String origTxnId = previous.get(FieldType.ORIG_TXN_ID.getName());
			if (null != origTxnId) {
				fields.put(FieldType.ORIG_TXN_ID.getName(), origTxnId);
			}

			String payId = previous.get(FieldType.PAY_ID.getName());
			if (null != payId) {
				fields.put(FieldType.PAY_ID.getName(), payId);
			}

			String acquirerType = previous.get(FieldType.ACQUIRER_TYPE.getName());
			if (null != acquirerType) {
				fields.put(FieldType.ACQUIRER_TYPE.getName(), acquirerType);
			}

			String udf1 = previous.get(FieldType.UDF1.getName());
			if (null != udf1) {
				fields.put(FieldType.UDF1.getName(), udf1);
			}

			String udf2 = previous.get(FieldType.UDF2.getName());
			if (null != udf2) {
				fields.put(FieldType.UDF2.getName(), udf2);
			}

			String udf3 = previous.get(FieldType.UDF3.getName());
			if (null != udf3) {
				fields.put(FieldType.UDF3.getName(), udf3);
			}

			String udf4 = previous.get(FieldType.UDF4.getName());
			if (null != udf4) {
				fields.put(FieldType.UDF4.getName(), udf4);
			}

			String udf5 = previous.get(FieldType.UDF5.getName());
			if (null != udf5) {
				fields.put(FieldType.UDF5.getName(), udf5);
			}
			String udf6 = previous.get(FieldType.UDF6.getName());
			if (null != udf6) {
				fields.put(FieldType.UDF6.getName(), udf6);
			}
			String udf7 = previous.get(FieldType.UDF7.getName());
			if (null != udf7) {
				fields.put(FieldType.UDF7.getName(), Amount.toDecimal(udf7, currencyCode));
			}
			String udf8 = previous.get(FieldType.UDF8.getName());
			if (null != udf8) {
				fields.put(FieldType.UDF8.getName(), Amount.toDecimal(udf8, currencyCode));
			}
			String udf9 = previous.get(FieldType.UDF9.getName());
			if (null != udf9) {
				fields.put(FieldType.UDF9.getName(), Amount.toDecimal(udf9, currencyCode));
			}
			String udf10 = previous.get(FieldType.UDF10.getName());
			if (null != udf10) {
				fields.put(FieldType.UDF10.getName(), Amount.toDecimal(udf10, currencyCode));
			}
			String udf11 = previous.get(FieldType.UDF11.getName());
			if (null != udf11) {
				fields.put(FieldType.UDF11.getName(), udf11);
			}
			String udf12 = previous.get(FieldType.UDF12.getName());
			if (null != udf12) {
				fields.put(FieldType.UDF12.getName(), udf12);
			}
			String udf13 = previous.get(FieldType.UDF13.getName());
			if (null != udf13) {
				fields.put(FieldType.UDF13.getName(), udf13);
			}
			String udf14 = previous.get(FieldType.UDF14.getName());
			if (null != udf14) {
				fields.put(FieldType.UDF14.getName(), udf14);
			}
			String udf15 = previous.get(FieldType.UDF15.getName());
			if (null != udf15) {
				fields.put(FieldType.UDF15.getName(), udf15);
			}
			String udf16 = previous.get(FieldType.UDF16.getName());
			if (null != udf16) {
				fields.put(FieldType.UDF16.getName(), udf16);
			}
			String udf17 = previous.get(FieldType.UDF17.getName());
			if (null != udf17) {
				fields.put(FieldType.UDF17.getName(), udf17);
			}
			String udf18 = previous.get(FieldType.UDF18.getName());
			if (null != udf18) {
				fields.put(FieldType.UDF18.getName(), udf18);
			}
			String custCountry = previous.get(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName());
			if (null != custCountry) {
				fields.put(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName(), custCountry);
			}
			String custIP = previous.get(FieldType.INTERNAL_CUST_IP.getName());
			if (null != custIP) {
				fields.put(FieldType.INTERNAL_CUST_IP.getName(), custIP);
			}

			String totalAmount = previous.get(FieldType.TOTAL_AMOUNT.getName());
			if (null != totalAmount) {
				fields.put(FieldType.TOTAL_AMOUNT.getName(), Amount.formatAmount(totalAmount, currencyCode));
			}

			String surchargeFlag = previous.get(FieldType.SURCHARGE_FLAG.getName());
			if (null != surchargeFlag) {
				fields.put(FieldType.SURCHARGE_FLAG.getName(), surchargeFlag);
			}

			String acquirerTdrSc = previous.get(FieldType.ACQUIRER_TDR_SC.getName());
			if (null != acquirerTdrSc) {
				fields.put(FieldType.ACQUIRER_TDR_SC.getName(), acquirerTdrSc);
			}

			String acquirerGst = previous.get(FieldType.ACQUIRER_GST.getName());
			if (null != acquirerGst) {
				fields.put(FieldType.ACQUIRER_GST.getName(), acquirerGst);
			}

			String pgGst = previous.get(FieldType.PG_GST.getName());
			if (null != pgGst) {
				fields.put(FieldType.PG_GST.getName(), pgGst);
			}

			String pgTdrSc = previous.get(FieldType.PG_TDR_SC.getName());
			if (null != pgTdrSc) {
				fields.put(FieldType.PG_TDR_SC.getName(), pgTdrSc);
			}

			String resellerCharge = previous.get(FieldType.RESELLER_CHARGES.getName());
			if (null != resellerCharge) {
				fields.put(FieldType.RESELLER_CHARGES.getName(), resellerCharge);
			}

			String resellerGst = previous.get(FieldType.RESELLER_GST.getName());
			if (null != acquirerGst) {
				fields.put(FieldType.RESELLER_GST.getName(), resellerGst);
			}

			String merchantTdrsc = previous.get(FieldType.MERCHANT_TDR_SC.getName());
			if (null != merchantTdrsc) {
				fields.put(FieldType.MERCHANT_TDR_SC.getName(), merchantTdrsc);
			}

			String merchantgst = previous.get(FieldType.MERCHANT_GST.getName());
			if (null != merchantgst) {
				fields.put(FieldType.MERCHANT_GST.getName(), merchantgst);
			}

			String acquirerMode = previous.get(FieldType.ACQUIRER_MODE.getName());
			if (null != acquirerMode) {
				fields.put(FieldType.ACQUIRER_MODE.getName(), acquirerMode);
			}

			String slabId = previous.get(FieldType.SLAB_ID.getName());
			if (null != slabId) {
				fields.put(FieldType.SLAB_ID.getName(), slabId);
			}

			String categoryCode = previous.get(FieldType.CATEGORY_CODE.getName());
			if (null != categoryCode) {
				fields.put(FieldType.CATEGORY_CODE.getName(), categoryCode);
			}

			String skuCode = previous.get(FieldType.SKU_CODE.getName());
			if (null != skuCode) {
				fields.put(FieldType.SKU_CODE.getName(), skuCode);
			}

			String prodName = previous.get(FieldType.PRODUCT_NAME.getName());
			if (null != prodName) {
				fields.put(FieldType.PRODUCT_NAME.getName(), prodName);
			}

			String quantity = previous.get(FieldType.QUANTITY.getName());
			if (null != quantity) {
				fields.put(FieldType.QUANTITY.getName(), quantity);
			}

			String productAmount = previous.get(FieldType.PRODUCT_AMOUNT.getName());
			if (null != productAmount) {
				fields.put(FieldType.PRODUCT_AMOUNT.getName(), productAmount);
			}

			String refundCycleDays = previous.get(FieldType.REFUND_CYCLE_DAYS.getName());
			if (null != refundCycleDays) {
				fields.put(FieldType.REFUND_CYCLE_DAYS.getName(), refundCycleDays);
			}
			
			String zName = previous.get(FieldType.Z_NAME.getName());
			if (null != zName) {
				fields.put(FieldType.Z_NAME.getName(), zName);
			}

			String cName = previous.get(FieldType.C_NAME.getName());
			if (null != cName) {
				fields.put(FieldType.C_NAME.getName(), cName);
			}

			String dName = previous.get(FieldType.D_NAME.getName());
			if (null != dName) {
				fields.put(FieldType.D_NAME.getName(), dName);
			}

			String receiptNo = previous.get(FieldType.RECIEPT_NO.getName());
			if (null != receiptNo) {
				fields.put(FieldType.RECIEPT_NO.getName(), receiptNo);
			}

			if (StringUtils.isNotBlank(previous.get(FieldType.SUB_MERCHANT_ID.getName()))) {
				fields.put(FieldType.SUB_MERCHANT_ID.getName(), previous.get(FieldType.SUB_MERCHANT_ID.getName()));
			}

		}

	}
}
