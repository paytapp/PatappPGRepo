package com.paymentgateway.pg.core.util;

import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class AcquirerTxnAmountProvider {

	public String amountProvider(Fields fields) throws SystemException {
		String amount = null;
		if (!StringUtils.isBlank(fields.get(FieldType.SURCHARGE_FLAG.getName()))
				&& fields.get(FieldType.SURCHARGE_FLAG.getName()).equalsIgnoreCase("Y")) {
			AcquirerType acquirer = AcquirerType.getInstancefromCode(fields.get(FieldType.ACQUIRER_TYPE.getName()));
			String addSurchargeFlag = null;

			String refundFlag = null;
			String txnType = null;
			switch (acquirer) {
			case FSS:
				txnType = fields.get(FieldType.TXNTYPE.getName());
				if (txnType.equals(TransactionType.REFUND.getName())) {
					refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
					if (refundFlag.equalsIgnoreCase("R")) {
						refundAmountComparator(fields);
						addSurchargeFlag = PropertiesManager.propertiesMap.get(Constants.FSS_ADD_SURCHARGE.getValue());
						amount = amountCalculator(fields, addSurchargeFlag);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					} else {
						amount = fields.get(FieldType.AMOUNT.getName());
						fields.put(FieldType.TOTAL_AMOUNT.getName(), amount);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					}
				} else {
					addSurchargeFlag = PropertiesManager.propertiesMap.get(Constants.FSS_ADD_SURCHARGE.getValue());
					amount = amountCalculator(fields, addSurchargeFlag);
					amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
				}
				break;
			case FSSPAY:
				txnType = fields.get(FieldType.TXNTYPE.getName());
				if (txnType.equals(TransactionType.REFUND.getName())) {
					refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
					if (refundFlag.equalsIgnoreCase("R")) {
						refundAmountComparator(fields);
						addSurchargeFlag = PropertiesManager.propertiesMap
								.get(Constants.FSS_PAY_ADD_SURCHARGE.getValue());
						amount = amountCalculator(fields, addSurchargeFlag);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					} else {
						amount = fields.get(FieldType.AMOUNT.getName());
						fields.put(FieldType.TOTAL_AMOUNT.getName(), amount);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					}
				} else {
					addSurchargeFlag = PropertiesManager.propertiesMap.get(Constants.FSS_PAY_ADD_SURCHARGE.getValue());
					amount = amountCalculator(fields, addSurchargeFlag);
					amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
				}
				break;
			case PAYU:
				txnType = fields.get(FieldType.TXNTYPE.getName());
				if (txnType.equals(TransactionType.REFUND.getName())) {
					refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
					if (refundFlag.equalsIgnoreCase("R")) {
						refundAmountComparator(fields);
						addSurchargeFlag = PropertiesManager.propertiesMap.get(Constants.PAYU_ADD_SURCHARGE.getValue());
						amount = amountCalculator(fields, addSurchargeFlag);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					} else {
						amount = fields.get(FieldType.AMOUNT.getName());
						fields.put(FieldType.TOTAL_AMOUNT.getName(), amount);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					}
				} else {
					addSurchargeFlag = PropertiesManager.propertiesMap.get(Constants.PAYU_ADD_SURCHARGE.getValue());
					amount = amountCalculator(fields, addSurchargeFlag);
					amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
				}
				break;
			case BILLDESK:
				txnType = fields.get(FieldType.TXNTYPE.getName());
				if (txnType.equals(TransactionType.REFUND.getName())) {
					refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
					if (refundFlag.equalsIgnoreCase("R")) {
						refundAmountComparator(fields);
						addSurchargeFlag = PropertiesManager.propertiesMap
								.get(Constants.BILLDESK_ADD_SURCHARGE.getValue());
						amount = amountCalculator(fields, addSurchargeFlag);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					} else {
						amount = fields.get(FieldType.AMOUNT.getName());
						fields.put(FieldType.TOTAL_AMOUNT.getName(), amount);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					}
				} else {
					addSurchargeFlag = PropertiesManager.propertiesMap.get(Constants.BILLDESK_ADD_SURCHARGE.getValue());
					amount = amountCalculator(fields, addSurchargeFlag);
					amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
				}
				break;
			case FEDERAL:
				addSurchargeFlag = PropertiesManager.propertiesMap.get(Constants.FEDERAL_ADD_SURCHARGE.getValue());
				amount = amountCalculator(fields, addSurchargeFlag);
				String paymentType = fields.get(FieldType.PAYMENT_TYPE.getName());
				if (paymentType.equals(PaymentType.UPI.getCode())) {
					amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
				}
				break;
			case ICICI_FIRSTDATA:
				txnType = fields.get(FieldType.TXNTYPE.getName());
				if (txnType.equals(TransactionType.REFUND.getName())) {
					refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
					if (refundFlag.equalsIgnoreCase("R")) {
						refundAmountComparator(fields);
						addSurchargeFlag = PropertiesManager.propertiesMap
								.get(Constants.FIRSTDATA_ADD_SURCHARGE.getValue());
						amount = amountCalculator(fields, addSurchargeFlag);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					} else {
						amount = fields.get(FieldType.AMOUNT.getName());
						fields.put(FieldType.TOTAL_AMOUNT.getName(), amount);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					}
				} else {
					addSurchargeFlag = PropertiesManager.propertiesMap
							.get(Constants.FIRSTDATA_ADD_SURCHARGE.getValue());
					amount = amountCalculator(fields, addSurchargeFlag);
					amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
				}
				break;
			case IDFC_FIRSTDATA:
				txnType = fields.get(FieldType.TXNTYPE.getName());
				if (txnType.equals(TransactionType.REFUND.getName())) {
					refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
					if (refundFlag.equalsIgnoreCase("R")) {
						refundAmountComparator(fields);
						addSurchargeFlag = PropertiesManager.propertiesMap
								.get(Constants.FIRSTDATA_ADD_SURCHARGE.getValue());
						amount = amountCalculator(fields, addSurchargeFlag);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					} else {
						amount = fields.get(FieldType.AMOUNT.getName());
						fields.put(FieldType.TOTAL_AMOUNT.getName(), amount);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					}
				} else {
					addSurchargeFlag = PropertiesManager.propertiesMap
							.get(Constants.FIRSTDATA_ADD_SURCHARGE.getValue());
					amount = amountCalculator(fields, addSurchargeFlag);
					amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
				}
				break;
			case IDFCUPI:
				txnType = fields.get(FieldType.TXNTYPE.getName());
				if (txnType.equals(TransactionType.REFUND.getName())) {
					refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
					if (refundFlag.equalsIgnoreCase("R")) {
						refundAmountComparator(fields);
						addSurchargeFlag = PropertiesManager.propertiesMap
								.get(Constants.IDFCUPI_ADD_SURCHARGE.getValue());
						amount = amountCalculator(fields, addSurchargeFlag);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					} else {
						amount = fields.get(FieldType.AMOUNT.getName());
						fields.put(FieldType.TOTAL_AMOUNT.getName(), amount);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					}
				} else {
					addSurchargeFlag = PropertiesManager.propertiesMap.get(Constants.IDFCUPI_ADD_SURCHARGE.getValue());
					amount = amountCalculator(fields, addSurchargeFlag);
					amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
				}
				break;
			case SAFEXPAY:
				txnType = fields.get(FieldType.TXNTYPE.getName());
				if (txnType.equals(TransactionType.REFUND.getName())) {
					refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
					if (refundFlag.equalsIgnoreCase("R")) {
						refundAmountComparator(fields);
						addSurchargeFlag = PropertiesManager.propertiesMap
								.get(Constants.SAFEXPAY_ADD_SURCHARGE.getValue());
						amount = amountCalculator(fields, addSurchargeFlag);
					} else {
						amount = fields.get(FieldType.AMOUNT.getName());
						fields.put(FieldType.TOTAL_AMOUNT.getName(), amount);
					}
				} else {
					addSurchargeFlag = PropertiesManager.propertiesMap.get(Constants.SAFEXPAY_ADD_SURCHARGE.getValue());
					amount = amountCalculator(fields, addSurchargeFlag);
				}
				break;
			case ICICIUPI:
				txnType = fields.get(FieldType.TXNTYPE.getName());
				if (txnType.equals(TransactionType.REFUND.getName())) {
					refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
					if (refundFlag.equalsIgnoreCase("R")) {
						refundAmountComparator(fields);
						addSurchargeFlag = PropertiesManager.propertiesMap
								.get(Constants.ICICIUPI_ADD_SURCHARGE.getValue());
						amount = amountCalculator(fields, addSurchargeFlag);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					} else {
						amount = fields.get(FieldType.AMOUNT.getName());
						fields.put(FieldType.TOTAL_AMOUNT.getName(), amount);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					}
				} else {
					addSurchargeFlag = PropertiesManager.propertiesMap.get(Constants.ICICIUPI_ADD_SURCHARGE.getValue());
					amount = amountCalculator(fields, addSurchargeFlag);
					amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
				}
				break;
			case AXISMIGS:
				addSurchargeFlag = PropertiesManager.propertiesMap.get(Constants.AXIS_MIGS_ADD_SURCHARGE.getValue());
				amount = amountCalculator(fields, addSurchargeFlag);
				break;
			case BOB:
			case HDFC:
				txnType = fields.get(FieldType.TXNTYPE.getName());
				if (txnType.equals(TransactionType.REFUND.getName())) {
					refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
					if (refundFlag.equalsIgnoreCase("R")) {
						refundAmountComparator(fields);
						addSurchargeFlag = PropertiesManager.propertiesMap.get(Constants.BOB_ADD_SURCHARGE.getValue());
						amount = amountCalculator(fields, addSurchargeFlag);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					} else {
						amount = fields.get(FieldType.AMOUNT.getName());
						fields.put(FieldType.TOTAL_AMOUNT.getName(), amount);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					}
				} else {
					addSurchargeFlag = PropertiesManager.propertiesMap.get(Constants.BOB_ADD_SURCHARGE.getValue());
					amount = amountCalculator(fields, addSurchargeFlag);
					amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
				}
				break;
			case KOTAK:
				txnType = fields.get(FieldType.TXNTYPE.getName());
				if (txnType.equals(TransactionType.REFUND.getName())) {
					refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
					if (refundFlag.equalsIgnoreCase("R")) {
						refundAmountComparator(fields);
						addSurchargeFlag = PropertiesManager.propertiesMap
								.get(Constants.KOTAK_ADD_SURCHARGE.getValue());
						amount = amountCalculator(fields, addSurchargeFlag);
					} else {
						amount = fields.get(FieldType.AMOUNT.getName());
						fields.put(FieldType.TOTAL_AMOUNT.getName(), amount);
					}
				} else {
					addSurchargeFlag = PropertiesManager.propertiesMap.get(Constants.KOTAK_ADD_SURCHARGE.getValue());
					amount = amountCalculator(fields, addSurchargeFlag);
				}
				break;
			case ISGPAY:
				txnType = fields.get(FieldType.TXNTYPE.getName());
				if (txnType.equals(TransactionType.REFUND.getName())) {
					refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
					if (refundFlag.equalsIgnoreCase("R")) {
						refundAmountComparator(fields);
						addSurchargeFlag = PropertiesManager.propertiesMap
								.get(Constants.ISGPAY_ADD_SURCHARGE.getValue());
						amount = amountCalculator(fields, addSurchargeFlag);
					} else {
						amount = fields.get(FieldType.AMOUNT.getName());
						fields.put(FieldType.TOTAL_AMOUNT.getName(), amount);
					}
				} else {
					addSurchargeFlag = PropertiesManager.propertiesMap.get(Constants.ISGPAY_ADD_SURCHARGE.getValue());
					amount = amountCalculator(fields, addSurchargeFlag);
				}
				break;
			case IDBIBANK:
				txnType = fields.get(FieldType.TXNTYPE.getName());
				if (txnType.equals(TransactionType.REFUND.getName())) {
					refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
					if (refundFlag.equalsIgnoreCase("R")) {
						refundAmountComparator(fields);
						addSurchargeFlag = PropertiesManager.propertiesMap
								.get(Constants.IDBIBANK_ADD_SURCHARGE.getValue());
						amount = amountCalculator(fields, addSurchargeFlag);
					} else {
						amount = fields.get(FieldType.AMOUNT.getName());
						fields.put(FieldType.TOTAL_AMOUNT.getName(), amount);
					}
				} else {
					addSurchargeFlag = PropertiesManager.propertiesMap.get(Constants.IDBIBANK_ADD_SURCHARGE.getValue());
					amount = amountCalculator(fields, addSurchargeFlag);
				}
				break;

			case YESBANKCB:
				String paymentTyp = fields.get(FieldType.PAYMENT_TYPE.getName());
				if (paymentTyp.equals(PaymentType.UPI.getCode())) {
					txnType = fields.get(FieldType.TXNTYPE.getName());
					if (txnType.equals(TransactionType.REFUND.getName())) {
						refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
						if (refundFlag.equalsIgnoreCase("R")) {
							refundAmountComparator(fields);
							addSurchargeFlag = PropertiesManager.propertiesMap
									.get(Constants.YESBANKCB_UPI_ADD_SURCHARGE.getValue());
							amount = amountCalculator(fields, addSurchargeFlag);
							amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
						} else {
							amount = fields.get(FieldType.AMOUNT.getName());
							fields.put(FieldType.TOTAL_AMOUNT.getName(), amount);
							amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
						}
					} else {
						addSurchargeFlag = PropertiesManager.propertiesMap
								.get(Constants.YESBANKCB_UPI_ADD_SURCHARGE.getValue());
						amount = amountCalculator(fields, addSurchargeFlag);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					}
				} else {
					txnType = fields.get(FieldType.TXNTYPE.getName());
					if (txnType.equals(TransactionType.REFUND.getName())) {
						refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
						if (refundFlag.equalsIgnoreCase("R")) {
							refundAmountComparator(fields);
							addSurchargeFlag = PropertiesManager.propertiesMap
									.get(Constants.YESBANKCB_ADD_SURCHARGE.getValue());
							amount = amountCalculator(fields, addSurchargeFlag);
							amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
						} else {
							amount = fields.get(FieldType.AMOUNT.getName());
							fields.put(FieldType.TOTAL_AMOUNT.getName(), amount);
							amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
						}
					} else {
						addSurchargeFlag = PropertiesManager.propertiesMap
								.get(Constants.YESBANKCB_ADD_SURCHARGE.getValue());
						amount = amountCalculator(fields, addSurchargeFlag);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					}
				}
				break;

			case AXISBANKCB:
				txnType = fields.get(FieldType.TXNTYPE.getName());
				if (txnType.equals(TransactionType.REFUND.getName())) {
					refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
					if (refundFlag.equalsIgnoreCase("R")) {
						refundAmountComparator(fields);
						addSurchargeFlag = PropertiesManager.propertiesMap
								.get(Constants.AXISBANKCB_ADD_SURCHARGE.getValue());
						amount = amountCalculator(fields, addSurchargeFlag);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					} else {
						amount = fields.get(FieldType.AMOUNT.getName());
						fields.put(FieldType.TOTAL_AMOUNT.getName(), amount);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					}
				} else {
					addSurchargeFlag = PropertiesManager.propertiesMap
							.get(Constants.AXISBANKCB_ADD_SURCHARGE.getValue());
					amount = amountCalculator(fields, addSurchargeFlag);
					amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
				}

				break;

			case PAYPHI:
				txnType = fields.get(FieldType.TXNTYPE.getName());
				if (txnType.equals(TransactionType.REFUND.getName())) {
					refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
					if (refundFlag.equalsIgnoreCase("R")) {
						refundAmountComparator(fields);
						addSurchargeFlag = PropertiesManager.propertiesMap
								.get(Constants.PAYPHI_ADD_SURCHARGE.getValue());
						amount = amountCalculator(fields, addSurchargeFlag);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					} else {
						amount = fields.get(FieldType.AMOUNT.getName());
						fields.put(FieldType.TOTAL_AMOUNT.getName(), amount);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					}
				} else {
					addSurchargeFlag = PropertiesManager.propertiesMap.get(Constants.PAYPHI_ADD_SURCHARGE.getValue());
					amount = amountCalculator(fields, addSurchargeFlag);
					amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
				}

				break;
			case CASHFREE:
				txnType = fields.get(FieldType.TXNTYPE.getName());
				if (txnType.equals(TransactionType.REFUND.getName())) {
					refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
					if (refundFlag.equalsIgnoreCase("R")) {
						refundAmountComparator(fields);
						addSurchargeFlag = PropertiesManager.propertiesMap
								.get(Constants.CASHFREE_ADD_SURCHARGE.getValue());
						amount = amountCalculator(fields, addSurchargeFlag);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					} else {
						amount = fields.get(FieldType.AMOUNT.getName());
						fields.put(FieldType.TOTAL_AMOUNT.getName(), amount);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					}
				} else {
					addSurchargeFlag = PropertiesManager.propertiesMap.get(Constants.CASHFREE_ADD_SURCHARGE.getValue());
					amount = amountCalculator(fields, addSurchargeFlag);
					amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
				}
				break;
				
			case AIRPAY:
				txnType = fields.get(FieldType.TXNTYPE.getName());
				if (txnType.equals(TransactionType.REFUND.getName())) {
					refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
					if (refundFlag.equalsIgnoreCase("R")) {
						refundAmountComparator(fields);
						addSurchargeFlag = PropertiesManager.propertiesMap
								.get(Constants.AIRPAY_ADD_SURCHARGE.getValue());
						amount = amountCalculator(fields, addSurchargeFlag);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					} else {
						amount = fields.get(FieldType.AMOUNT.getName());
						fields.put(FieldType.TOTAL_AMOUNT.getName(), amount);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					}
				} else {
					addSurchargeFlag = PropertiesManager.propertiesMap.get(Constants.AIRPAY_ADD_SURCHARGE.getValue());
					amount = amountCalculator(fields, addSurchargeFlag);
					amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
				}
				break;
			case QAICASH:
				txnType = fields.get(FieldType.TXNTYPE.getName());
				if (txnType.equals(TransactionType.REFUND.getName())) {
					refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
					if (refundFlag.equalsIgnoreCase("R")) {
						refundAmountComparator(fields);
						addSurchargeFlag = PropertiesManager.propertiesMap
								.get(Constants.QAICASH_ADD_SURCHARGE.getValue());
						amount = amountCalculator(fields, addSurchargeFlag);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					} else {
						amount = fields.get(FieldType.AMOUNT.getName());
						fields.put(FieldType.TOTAL_AMOUNT.getName(), amount);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					}
				} else {
					addSurchargeFlag = PropertiesManager.propertiesMap.get(Constants.QAICASH_ADD_SURCHARGE.getValue());
					amount = amountCalculator(fields, addSurchargeFlag);
					amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
				}
				break;
			case GLOBALPAY:
				txnType = fields.get(FieldType.TXNTYPE.getName());
				if (txnType.equals(TransactionType.REFUND.getName())) {
					refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
					if (refundFlag.equalsIgnoreCase("R")) {
						refundAmountComparator(fields);
						addSurchargeFlag = PropertiesManager.propertiesMap
								.get(Constants.GLOBALPAY_ADD_SURCHARGE.getValue());
						amount = amountCalculator(fields, addSurchargeFlag);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					} else {
						amount = fields.get(FieldType.AMOUNT.getName());
						fields.put(FieldType.TOTAL_AMOUNT.getName(), amount);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					}
				} else {
					addSurchargeFlag = PropertiesManager.propertiesMap.get(Constants.GLOBALPAY_ADD_SURCHARGE.getValue());
					amount = amountCalculator(fields, addSurchargeFlag);
					amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
				}
				break;
			case DIGITALSOLUTIONS:
				txnType = fields.get(FieldType.TXNTYPE.getName());
				if (txnType.equals(TransactionType.REFUND.getName())) {
					refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
					if (refundFlag.equalsIgnoreCase("R")) {
						refundAmountComparator(fields);
						addSurchargeFlag = PropertiesManager.propertiesMap
								.get(Constants.DIGITALSOLUTION_ADD_SURCHARGE.getValue());
						amount = amountCalculator(fields, addSurchargeFlag);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					} else {
						amount = fields.get(FieldType.AMOUNT.getName());
						fields.put(FieldType.TOTAL_AMOUNT.getName(), amount);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					}
				} else {
					addSurchargeFlag = PropertiesManager.propertiesMap.get(Constants.DIGITALSOLUTION_ADD_SURCHARGE.getValue());
					amount = amountCalculator(fields, addSurchargeFlag);
					amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
				}
				break;
			case FLOXYPAY:
				txnType = fields.get(FieldType.TXNTYPE.getName());
				if (txnType.equals(TransactionType.REFUND.getName())) {
					refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
					if (refundFlag.equalsIgnoreCase("R")) {
						refundAmountComparator(fields);
						addSurchargeFlag = PropertiesManager.propertiesMap
								.get(Constants.FLOXYPAY_ADD_SURCHARGE.getValue());
						amount = amountCalculator(fields, addSurchargeFlag);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					} else {
						amount = fields.get(FieldType.AMOUNT.getName());
						fields.put(FieldType.TOTAL_AMOUNT.getName(), amount);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					}
				} else {
					addSurchargeFlag = PropertiesManager.propertiesMap.get(Constants.FLOXYPAY_ADD_SURCHARGE.getValue());
					amount = amountCalculator(fields, addSurchargeFlag);
					amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
				}
				break;
			case RAZORPAY:
				txnType = fields.get(FieldType.TXNTYPE.getName());
				if (txnType.equals(TransactionType.REFUND.getName())) {
					refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
					if (refundFlag.equalsIgnoreCase("R")) {
						refundAmountComparator(fields);
						addSurchargeFlag = PropertiesManager.propertiesMap
								.get(Constants.RAZORPAY_ADD_SURCHARGE.getValue());
						amount = amountCalculator(fields, addSurchargeFlag);
					} else {
						amount = fields.get(FieldType.AMOUNT.getName());
						fields.put(FieldType.TOTAL_AMOUNT.getName(), amount);
					}
				} else {
					addSurchargeFlag = PropertiesManager.propertiesMap.get(Constants.RAZORPAY_ADD_SURCHARGE.getValue());
					amount = amountCalculator(fields, addSurchargeFlag);
				}
				break;

			case GREZPAY:
				txnType = fields.get(FieldType.TXNTYPE.getName());
				if (txnType.equals(TransactionType.REFUND.getName())) {
					refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
					if (refundFlag.equalsIgnoreCase("R")) {
						refundAmountComparator(fields);
						addSurchargeFlag = PropertiesManager.propertiesMap
								.get(Constants.GREZPAY_ADD_SURCHARGE.getValue());
						amount = amountCalculator(fields, addSurchargeFlag);
					} else {
						amount = fields.get(FieldType.AMOUNT.getName());
						fields.put(FieldType.TOTAL_AMOUNT.getName(), amount);
					}
				} else {
					addSurchargeFlag = PropertiesManager.propertiesMap.get(Constants.GREZPAY_ADD_SURCHARGE.getValue());
					amount = amountCalculator(fields, addSurchargeFlag);
				}
				break;
			case VEPAY:
				txnType = fields.get(FieldType.TXNTYPE.getName());
				if (txnType.equals(TransactionType.REFUND.getName())) {
					refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
					if (refundFlag.equalsIgnoreCase("R")) {
						refundAmountComparator(fields);
						addSurchargeFlag = PropertiesManager.propertiesMap
								.get(Constants.VEPAY_ADD_SURCHARGE.getValue());
						amount = amountCalculator(fields, addSurchargeFlag);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					} else {
						amount = fields.get(FieldType.AMOUNT.getName());
						fields.put(FieldType.TOTAL_AMOUNT.getName(), amount);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					}
				} else {
					addSurchargeFlag = PropertiesManager.propertiesMap.get(Constants.VEPAY_ADD_SURCHARGE.getValue());
					amount = amountCalculator(fields, addSurchargeFlag);
					amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
				}

				break;
				
			case IPINT:
				txnType = fields.get(FieldType.TXNTYPE.getName());
				if (txnType.equals(TransactionType.REFUND.getName())) {
					refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
					if (refundFlag.equalsIgnoreCase("R")) {
						refundAmountComparator(fields);
						addSurchargeFlag = PropertiesManager.propertiesMap
								.get(Constants.IPINT_ADD_SURCHARGE.getValue());
						amount = amountCalculator(fields, addSurchargeFlag);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					} else {
						amount = fields.get(FieldType.AMOUNT.getName());
						fields.put(FieldType.TOTAL_AMOUNT.getName(), amount);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					}
				} else {
					addSurchargeFlag = PropertiesManager.propertiesMap.get(Constants.IPINT_ADD_SURCHARGE.getValue());
					amount = amountCalculator(fields, addSurchargeFlag);
					amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
				}
				break;	
				
			case UPIGATEWAY:
				txnType = fields.get(FieldType.TXNTYPE.getName());
				if (txnType.equals(TransactionType.REFUND.getName())) {
					refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
					if (refundFlag.equalsIgnoreCase("R")) {
						refundAmountComparator(fields);
						addSurchargeFlag = PropertiesManager.propertiesMap
								.get(Constants.UPIGATEWAY_ADD_SURCHARGE.getValue());
						amount = amountCalculator(fields, addSurchargeFlag);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					} else {
						amount = fields.get(FieldType.AMOUNT.getName());
						fields.put(FieldType.TOTAL_AMOUNT.getName(), amount);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					}
				} else {
					addSurchargeFlag = PropertiesManager.propertiesMap.get(Constants.UPIGATEWAY_ADD_SURCHARGE.getValue());
					amount = amountCalculator(fields, addSurchargeFlag);
					amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
				}

				break;
				
			case P2PTSP:
				addSurchargeFlag = PropertiesManager.propertiesMap.get(Constants.P2PTSPMQR_ADD_SURCHARGE.getValue());
				amount = amountCalculator(fields, addSurchargeFlag);
				break;
				
			case TOSHANIDIGITAL:
				txnType = fields.get(FieldType.TXNTYPE.getName());
				if (txnType.equals(TransactionType.REFUND.getName())) {
					refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
					if (refundFlag.equalsIgnoreCase("R")) {
						refundAmountComparator(fields);
						addSurchargeFlag = PropertiesManager.propertiesMap
								.get(Constants.TOSHANIDIGITAL_ADD_SURCHARGE.getValue());
						amount = amountCalculator(fields, addSurchargeFlag);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					} else {
						amount = fields.get(FieldType.AMOUNT.getName());
						fields.put(FieldType.TOTAL_AMOUNT.getName(), amount);
						amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					}
				} else {
					addSurchargeFlag = PropertiesManager.propertiesMap.get(Constants.TOSHANIDIGITAL_ADD_SURCHARGE.getValue());
					amount = amountCalculator(fields, addSurchargeFlag);
					amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
				}

				break;
			}
			

		} else {
			AcquirerType acquirer = AcquirerType.getInstancefromCode(fields.get(FieldType.ACQUIRER_TYPE.getName()));
			switch (acquirer) {
			case FSS:
				amount = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName()));
				break;
			case FSSPAY:
				amount = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName()));
				break;
			case BILLDESK:
				amount = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName()));
				break;
			case FEDERAL:
				amount = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName()));
				break;
			case ICICI_FIRSTDATA:
				amount = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName()));
				break;
			case IDFC_FIRSTDATA:
				amount = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName()));
				break;
			case IDFCUPI:
				amount = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName()));
				break;
			case ICICIUPI:
				amount = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName()));
				break;
			case SAFEXPAY:
				amount = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName()));
				break;
			case AXISMIGS:
				amount = fields.get(FieldType.AMOUNT.getName());
				break;
			case BOB:
			case HDFC:
				amount = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName()));
				break;
			case KOTAK:
				amount = fields.get(FieldType.AMOUNT.getName());
				break;
			case IDBIBANK:
				amount = fields.get(FieldType.AMOUNT.getName());
			case YESBANKCB:
				amount = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName()));
				break;
			case AXISBANKCB:
				amount = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName()));
				break;
			case ISGPAY:
				amount = fields.get(FieldType.AMOUNT.getName());
				break;
			case PAYPHI:
				amount = fields.get(FieldType.AMOUNT.getName());
				break;
			case PAYU:
				amount = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName()));
				break;
			case CASHFREE:
				amount = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName()));
				break;
			case VEPAY:
				amount = fields.get(FieldType.AMOUNT.getName());
				break;
			case AIRPAY:
				amount = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName()));
				break;
			case RAZORPAY:
				amount = fields.get(FieldType.AMOUNT.getName());
				break;
			case QAICASH:
				amount = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName()));
				break;
			case GREZPAY:
				amount = fields.get(FieldType.AMOUNT.getName());
				break;
			case FLOXYPAY:
				amount = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName()));
				break;
			case DIGITALSOLUTIONS:
				amount = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName()));
				break;
			case IPINT:
				amount = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName()));
				break;
			case UPIGATEWAY:
				amount = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName()));
				break;
			case P2PTSP:
				amount = fields.get(FieldType.AMOUNT.getName());
				break;
			case TOSHANIDIGITAL:
			case GLOBALPAY:
				amount = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName()));
				break;
			}
			

		}
		return amount;
	}

	public String amountCalculator(Fields fields, String addSurchargeFlag) {
		String amount = null;
		if (addSurchargeFlag.equals("Y")) {
			amount = fields.get(FieldType.TOTAL_AMOUNT.getName());
		} else {
			amount = fields.get(FieldType.AMOUNT.getName());
		}
		return amount;
	}

	public void refundAmountComparator(Fields fields) throws SystemException {
		String amount = fields.get(FieldType.AMOUNT.getName());
		String saleAmount = fields.get(FieldType.SALE_AMOUNT.getName());
		if (!saleAmount.equals(amount)) {
			throw new SystemException(ErrorType.REFUND_FLAG_AMOUNT_NOT_MATCH, "Error processing refund");
		}

	}

}
