package com.paymentgateway.pg.core.fraudPrevention.core;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.FraudPrevention;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.FraudRuleType;
import com.paymentgateway.commons.util.Helper;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.SystemConstants;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.TxnType;
import com.paymentgateway.commons.util.Weekdays;
import com.paymentgateway.pg.core.fraudPreention.dao.FraudPreventionMongoDao;
import com.paymentgateway.pg.core.fraudPrevention.model.FraudPreventionDao;
import com.paymentgateway.pg.core.fraudPrevention.model.FraudTxnDao;
import com.paymentgateway.pg.core.fraudPrevention.util.FraudPreventionUtil;
import com.paymentgateway.pg.core.util.DefaultCryptoManager;

/**
 * @author Harpreet, Rahul, Shiva
 *
 */
@Service
public class FraudRuleImplementor {

	@Autowired
	private FraudPreventionUtil fraudPreventionUtil;

	@Autowired
	private FraudTxnDao fraudTxnDao;

	@Autowired
	private DefaultCryptoManager defaultCryptoManager;

	@Autowired
	private FraudPreventionMongoDao fraudPreventionDao;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private DataEncDecTool dataEncDecTool;
	
	private static final String prefix = "MONGO_DB_";

	private static Logger logger = LoggerFactory.getLogger(FraudRuleImplementor.class.getName());

	public void applyRule(Fields fields) throws SystemException {
		List<FraudPrevention> fraudPreventionRuleList = new ArrayList<FraudPrevention>();
		try {
			String payId2 = fields.get(FieldType.PAY_ID.getName());
			String subMerchantId = fields.get(FieldType.SUB_MERCHANT_ID.getName());
			 FraudPreventionDao fraudPreventionDao = new FraudPreventionDao();
			// FraudTxnDao fraudTxnDao = new FraudTxnDao();
			fraudPreventionRuleList = fraudPreventionDao.getFraudRuleListbyPayId(payId2);

			if (fraudPreventionRuleList != null) {

				// sorting the list according to ruleGroupId
				// performace optimization
				Collections.sort(fraudPreventionRuleList);

				for (FraudPrevention fraudPrevention : fraudPreventionRuleList) {
					final String payId = fields.get(FieldType.PAY_ID.getName());
					final String subMerchantPayId = fields.get(FieldType.SUB_MERCHANT_ID.getName());
					final FraudRuleType fraudType = fraudPrevention.getFraudType();
					String txnType = fields.get(FieldType.TXNTYPE.getName());
					boolean result = true;
					// boolean alwaysOnflag = fraudPrevention.isAlwaysOnFlag();
					/*
					 * if (alwaysOnflag == false) { result = validateTime(fraudPrevention); }
					 */

					/* if (result == true) { */

					switch (fraudType) {

					case WHITE_LIST_IP_ADDRESS:
						if (!(txnType.equals(TransactionType.NEWORDER.getName()))) {
							break;
						}
						boolean flag = false;
						logger.info(" FraudRuleImplementor");

						Collection<String> wlIpAddressList = Helper
								.parseFields(fraudPrevention.getWhiteListIpAddress());
						flag = validateWhiteListField(fields, fraudPrevention, wlIpAddressList, flag);
						logger.info(" after FraudRuleImplementor  ");

						if (!flag) {
							logger.info(
									" to validate fields of same category  validateWhiteListField equalsIgnoreCase(fraudFieldValue)");
							fields.put(Constants.PG_FRAUD_TYPE.getValue(), fraudPrevention.getFraudType().getValue());
							fields.put(FieldType.STATUS.getName(), StatusType.DENIED_BY_FRAUD.getName());
							throw new SystemException(ErrorType.DENIED_BY_FRAUD,
									"Fraud transaction with PAY_ID=" + fields.get(CrmFieldType.PAY_ID.getName())+" SUB_MERCHANT_ID "+fields.get(CrmFieldType.PAY_ID.getName())
											+ " detected and blocked" + "with FRAUD_TYPE: " + fraudType.getValue());

						}
						break;
					case BLOCK_TXN_AMOUNT:
						if ((txnType.equals(TransactionType.NEWORDER.getName()))) {
							break;
						}
						String currencyCode = fields.get(FieldType.CURRENCY_CODE.getName());
						if (currencyCode.equalsIgnoreCase(fraudPrevention.getCurrency()) 
								&& !(fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TxnType.REFUND.getName()))) {
							if (fraudPrevention.getPaymentType()
									.equalsIgnoreCase(fields.get(FieldType.PAYMENT_TYPE.getName()))
									&& fraudPrevention.getPaymentRegion()
											.equalsIgnoreCase(fields.get(FieldType.PAYMENTS_REGION.getName()))) {
							String tempRequestAmount = fields.get(FieldType.TOTAL_AMOUNT.getName());
							Double requestAmount = Double
									.parseDouble(Amount.toDecimal(tempRequestAmount, currencyCode));
							if (!((requestAmount >= Double.parseDouble(fraudPrevention.getMinTransactionAmount())
									&& (requestAmount <= Double
											.parseDouble(fraudPrevention.getMaxTransactionAmount()))))) {
								fields.put(Constants.PG_FRAUD_TYPE.getValue(),
										fraudPrevention.getFraudType().getValue());
								fields.put(FieldType.STATUS.getName(), StatusType.DENIED_BY_FRAUD.getName());
								throw new SystemException(ErrorType.DENIED_BY_FRAUD,
										"Fraud transaction with PAY_ID=" + fields.get(CrmFieldType.PAY_ID.getName())
												+ " detected and blocked" + "with FRAUD_TYPE: " + fraudType.getValue());
							}
						}
						}
						break;
					case BLOCK_NO_OF_TXNS:
						if ((txnType.equals(TransactionType.NEWORDER.getName()))) {
							break;
						}
						String ipAddress = fields.get(FieldType.INTERNAL_CUST_IP.getName());
						LocalDateTime currentDateTime = DateCreater.now();
						String currentStamp = currentDateTime.format(DateCreater.dateTimeFormat);

						String hrlyStartTimeStamp = DateCreater.subtractHours(currentDateTime, 1);
						String dailyStartTimeStamp = DateCreater.subtractDays(currentDateTime, 1);
						String weeklyStartTimeStamp = DateCreater.subtractWeeks(currentDateTime, 1);
						String monthlyStartTimeStamp = DateCreater.subtractMonths(currentDateTime, 1);
						Map<String, String> timeStampMap = new HashMap<String, String>();
						timeStampMap.put("currentStamp", currentStamp);
						timeStampMap.put("hrlyStartStamp", hrlyStartTimeStamp);
						timeStampMap.put("dailyStartStamp", dailyStartTimeStamp);
						timeStampMap.put("weekhlyStartStamp", weeklyStartTimeStamp);
						timeStampMap.put("monthlyStartStamp", monthlyStartTimeStamp);

						LinkedList<Long> noOfTxnList = fraudPreventionDao
								.getSpecificIPandIntervalTransactions(ipAddress, payId, timeStampMap);
						LinkedList<Long> noOfTxnAllowedList = new LinkedList<Long>();
						/*
						 * noOfTxnAllowedList.add(Long.parseLong(fraudPrevention .getHourlyTxnLimit()));
						 * noOfTxnAllowedList.add(Long.parseLong(fraudPrevention .getDailyTxnLimit()));
						 * noOfTxnAllowedList.add(Long.parseLong(fraudPrevention .getWeeklyTxnLimit()));
						 * noOfTxnAllowedList.add(Long.parseLong(fraudPrevention
						 * .getMonthlyTxnLimit()));
						 */

						for (long noOfTxn : noOfTxnList) {
							for (long noOfTxnAllowed : noOfTxnAllowedList) {
								if (!(noOfTxn < noOfTxnAllowed)) {
									fields.put(Constants.PG_FRAUD_TYPE.getValue(),
											fraudPrevention.getFraudType().getValue());
									fields.put(FieldType.STATUS.getName(), StatusType.DENIED_BY_FRAUD.getName());
									throw new SystemException(ErrorType.DENIED_BY_FRAUD,
											"Fraud transaction with PAY_ID=" + fields.get(CrmFieldType.PAY_ID.getName())
													+ " detected and blocked" + "with FRAUD_TYPE: "
													+ fraudType.getValue());
								}
							}
						}
						break;
					case BLOCK_DOMAIN_NAME:
						if (!(txnType.equals(TransactionType.NEWORDER.getName()))) {
							break;
						}
						Collection<String> domainNameList = Helper.parseFields(fraudPrevention.getDomainName());
						for (String blockDomain : domainNameList) {
							validateDomainFields(fields, fraudPrevention, blockDomain);
						}
						break;
					case BLOCK_IP_ADDRESS:
						if (!(txnType.equals(TransactionType.NEWORDER.getName()))) {
							break;
						}
						Collection<String> ipAddressList = Helper.parseFields(fraudPrevention.getIpAddress());
						for (String blockIpAddress : ipAddressList) {
							validateRequestField(fields, fraudPrevention, blockIpAddress);
						}
						break;
					case BLOCK_EMAIL_ID:
						if (!(txnType.equals(TransactionType.NEWORDER.getName()))) {
							break;
						}
						Collection<String> blockEmailIdList = Helper.parseFields(fraudPrevention.getEmail());
						for (String blockEmail : blockEmailIdList) {
							validateRequestField(fields, fraudPrevention, blockEmail);
						}
						break;
					case BLOCK_CARD_ISSUER_COUNTRY:
						if ((txnType.equals(TransactionType.NEWORDER.getName()))) {
							break;
						}
						Collection<String> blockCardIssuerList = Helper.parseFields(fraudPrevention.getIssuerCountry());
						for (String blockCardIssuer : blockCardIssuerList) {
							validateRequestField(fields, fraudPrevention, blockCardIssuer);
						}
						break;
					case BLOCK_USER_COUNTRY:
						if (!(txnType.equals(TransactionType.NEWORDER.getName()))) {
							break;
						}
						Collection<String> blockUserCountryList = Helper.parseFields(fraudPrevention.getUserCountry());
						for (String blockUserCountry : blockUserCountryList) {
							validateRequestField(fields, fraudPrevention, blockUserCountry);
						}
						break;
					case BLOCK_CARD_NO:
						if ((txnType.equals(TransactionType.NEWORDER.getName()))) {
							break;
						}
						Collection<String> cardNoList = Helper.parseFields(fraudPrevention.getNegativeCard());

						if (fields.contains(fraudType.getFieldName())) {
							String txnCardNumber = defaultCryptoManager
									.hashCardNumber(fields.get(FieldType.CARD_NUMBER.getName()));
							for (String cardNo : cardNoList) {
								if (txnCardNumber.equals(cardNo)) {
									boolean alwaysOnflag = fraudPrevention.isAlwaysOnFlag();
									if (alwaysOnflag == false) {
										result = validateTime(fraudPrevention);
									} else {
										fields.put(Constants.PG_FRAUD_TYPE.getValue(),
												fraudPrevention.getFraudType().getValue());
										fields.put(FieldType.STATUS.getName(), StatusType.DENIED_BY_FRAUD.getName());
										throw new SystemException(ErrorType.DENIED_BY_FRAUD,
												"Fraud transaction with PAY_ID="
														+ fields.get(CrmFieldType.PAY_ID.getName())
														+ " detected and blocked" + "with FRAUD_TYPE: "
														+ fraudType.getValue());
									}
									if (result == true) {
										fields.put(Constants.PG_FRAUD_TYPE.getValue(),
												fraudPrevention.getFraudType().getValue());
										fields.put(FieldType.STATUS.getName(), StatusType.DENIED_BY_FRAUD.getName());
										throw new SystemException(ErrorType.DENIED_BY_FRAUD,
												"Fraud transaction with PAY_ID="
														+ fields.get(CrmFieldType.PAY_ID.getName())
														+ " detected and blocked" + "with FRAUD_TYPE: "
														+ fraudType.getValue());
									}
								}
							}
						}

						break;
					case BLOCK_CARD_BIN:
						if ((txnType.equals(TransactionType.NEWORDER.getName()))) {
							break;
						}
						Collection<String> cardBinList = Helper.parseFields(fraudPrevention.getNegativeBin());
						if (fields.contains(fraudType.getFieldName())) {
							String reqCardNo = fields.get(FieldType.CARD_NUMBER.getName());
							String reqBin = reqCardNo.substring(0, SystemConstants.CARD_BIN_LENGTH);
							for (String cardBin : cardBinList) {
								if (reqBin.equals(cardBin)) {
									boolean alwaysOnflag = fraudPrevention.isAlwaysOnFlag();
									if (alwaysOnflag == false) {
										result = validateTime(fraudPrevention);
									} else {
										fields.put(Constants.PG_FRAUD_TYPE.getValue(),
												fraudPrevention.getFraudType().getValue());
										fields.put(FieldType.STATUS.getName(), StatusType.DENIED_BY_FRAUD.getName());
										throw new SystemException(ErrorType.DENIED_BY_FRAUD,
												"Fraud transaction with PAY_ID="
														+ fields.get(CrmFieldType.PAY_ID.getName())
														+ " detected and blocked" + "with FRAUD_TYPE: "
														+ fraudType.getValue());
									}
									if (result == true) {
										fields.put(Constants.PG_FRAUD_TYPE.getValue(),
												fraudPrevention.getFraudType().getValue());
										fields.put(FieldType.STATUS.getName(), StatusType.DENIED_BY_FRAUD.getName());
										throw new SystemException(ErrorType.DENIED_BY_FRAUD,
												"Fraud transaction with PAY_ID="
														+ fields.get(CrmFieldType.PAY_ID.getName())
														+ " detected and blocked" + "with FRAUD_TYPE: "
														+ fraudType.getValue());
									}
								}
							}
						}
						break;
					case BLOCK_CARD_TXN_THRESHOLD:
						if ((txnType.equals(TransactionType.NEWORDER.getName()))) {
							break;
						}
						if (fields.contains(fraudType.getFieldName())) {
							String cardHash = defaultCryptoManager
									.hashCardNumber(fields.get(FieldType.CARD_NUMBER.getName()));
							long noOfTransctions = fraudTxnDao
									.getPerCardTransactions(fields.get(FieldType.PAY_ID.getName()), cardHash);
							long perCardTxnAllowed = Long.parseLong(fraudPrevention.getPerCardTransactionAllowed());
							if (!(noOfTransctions < perCardTxnAllowed)) {
								fields.put(Constants.PG_FRAUD_TYPE.getValue(),
										fraudPrevention.getFraudType().getValue());
								fields.put(FieldType.STATUS.getName(), StatusType.DENIED_BY_FRAUD.getName());
								throw new SystemException(ErrorType.DENIED_BY_FRAUD,
										"Fraud transaction with PAY_ID=" + fields.get(CrmFieldType.PAY_ID.getName())
												+ " detected and blocked" + "with FRAUD_TYPE: " + fraudType.getValue());
							}
						}
						break;

					case BLOCK_TXN_VELOCITY:
						if ((txnType.equals(TransactionType.NEWORDER.getName()))) {
							break;
						}
						if (fields.contains(fraudType.getFieldName())) {
							if (fraudPrevention.getPaymentType()
									.equalsIgnoreCase(fields.get(FieldType.PAYMENT_TYPE.getName()))
									&& fraudPrevention.getPaymentRegion()
											.equalsIgnoreCase(fields.get(FieldType.PAYMENTS_REGION.getName()))) {
								String cardHash = Hasher.getHash(fields.get(FieldType.CARD_NUMBER.getName()));
								long noOftransactions = txnVelocityCheck(fields, fraudPrevention, cardHash);
								long perCardTxnAllowed = Long.parseLong(fraudPrevention.getNoOfTransactionAllowed());
								if (!(noOftransactions < perCardTxnAllowed)) {
									fields.put(Constants.PG_FRAUD_TYPE.getValue(),
											fraudPrevention.getFraudType().getValue());
									fields.put(FieldType.STATUS.getName(), StatusType.DENIED_BY_FRAUD.getName());
									throw new SystemException(ErrorType.DENIED_BY_FRAUD,
											"Fraud transaction with PAY_ID=" + fields.get(CrmFieldType.PAY_ID.getName())
													+ " detected and blocked" + "with FRAUD_TYPE: "
													+ fraudType.getValue());
								}
							}

						}
						break;

					case BLOCK_AMOUNT_VELOCITY:
						if ((txnType.equals(TransactionType.NEWORDER.getName()))) {
							break;
						}
						if (fields.contains(fraudType.getFieldName())) {
							if (fraudPrevention.getPaymentType()
									.equalsIgnoreCase(fields.get(FieldType.PAYMENT_TYPE.getName()))
									&& fraudPrevention.getPaymentRegion()
											.equalsIgnoreCase(fields.get(FieldType.PAYMENTS_REGION.getName()))) {
								String cardHash = defaultCryptoManager
										.hashCardNumber(fields.get(FieldType.CARD_NUMBER.getName()));
								BigDecimal totalTxnAmount = amountVelocityCheck(fields, fraudPrevention, cardHash);
								totalTxnAmount = totalTxnAmount.add(
										new BigDecimal(Amount.toDecimal(fields.get(FieldType.TOTAL_AMOUNT.getName()),
												fields.get(FieldType.CURRENCY_CODE.getName()))));
								BigDecimal allowedAmount = new BigDecimal(fraudPrevention.getAmountAllowed());
								int comparsionResult = totalTxnAmount.compareTo(allowedAmount);
								if ((comparsionResult > 0)) {
									fields.put(Constants.PG_FRAUD_TYPE.getValue(),
											fraudPrevention.getFraudType().getValue());
									fields.put(FieldType.STATUS.getName(), StatusType.DENIED_BY_FRAUD.getName());
									throw new SystemException(ErrorType.DENIED_BY_FRAUD,
											"Fraud transaction with PAY_ID=" + fields.get(CrmFieldType.PAY_ID.getName())
													+ " detected and blocked" + "with FRAUD_TYPE: "
													+ fraudType.getValue());
								}
							}
						}
						break;
						
						case BLOCK_SALE_AMOUNT_VELOCITY:
						if ((txnType.equals(TransactionType.NEWORDER.getName()))) {
							break;
						}
						if (fields.contains(fraudType.getFieldName())) {
							if (fraudPrevention.getPaymentType()
									.equalsIgnoreCase(fields.get(FieldType.PAYMENT_TYPE.getName()))
									&& fraudPrevention.getPaymentRegion()
											.equalsIgnoreCase(fields.get(FieldType.PAYMENTS_REGION.getName())) 
											&& !(fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TxnType.REFUND.getName()))) {
								
								BigDecimal totalTxnAmount = saleAmountVelocityCheck(fields, fraudPrevention);
								totalTxnAmount = totalTxnAmount.add(
										new BigDecimal(Amount.toDecimal(fields.get(FieldType.TOTAL_AMOUNT.getName()),
												fields.get(FieldType.CURRENCY_CODE.getName()))));
								BigDecimal allowedAmount = new BigDecimal(fraudPrevention.getAmountAllowed());
								int comparsionResult = totalTxnAmount.compareTo(allowedAmount);
								if ((comparsionResult > 0)) {
									fields.put(Constants.PG_FRAUD_TYPE.getValue(),
											fraudPrevention.getFraudType().getValue());
									fields.put(FieldType.STATUS.getName(), StatusType.DENIED_BY_FRAUD.getName());
									throw new SystemException(ErrorType.DENIED_BY_FRAUD,
											"Fraud transaction with PAY_ID=" + fields.get(FieldType.PAY_ID.getName())
													+ " detected and blocked" + "with FRAUD_TYPE: "
													+ fraudType.getValue());
								}
							}
						}
						break;
						
					case BLOCK_VPA:
						if ((txnType.equals(TransactionType.NEWORDER.getName()))) {
							break;
						}
						
						if(StringUtils.isNotBlank(fields.get(FieldType.PAYER_ADDRESS.getName()))){
							
							String reqVpa= fields.get(FieldType.PAYER_ADDRESS.getName()).toLowerCase();
							String hashVpa =  Hasher.getHash(reqVpa);
							
							String savedHashVpa = fraudPrevention.getVpaHash();
							
							if(hashVpa.equals(savedHashVpa)){
								boolean alwaysOnflag = fraudPrevention.isAlwaysOnFlag();
								if (alwaysOnflag == false) {
									result = validateTime(fraudPrevention);
								} else {
									fields.put(Constants.PG_FRAUD_TYPE.getValue(),
											fraudPrevention.getFraudType().getValue());
									fields.put(FieldType.STATUS.getName(), StatusType.DENIED_BY_FRAUD.getName());
									throw new SystemException(ErrorType.DENIED_BY_FRAUD,
											"Fraud transaction with PAY_ID="
													+ fields.get(CrmFieldType.PAY_ID.getName())
													+ " detected and blocked" + "with FRAUD_TYPE: "
													+ fraudType.getValue());
								}
								if (result == true) {
									fields.put(Constants.PG_FRAUD_TYPE.getValue(),
											fraudPrevention.getFraudType().getValue());
									fields.put(FieldType.STATUS.getName(), StatusType.DENIED_BY_FRAUD.getName());
									throw new SystemException(ErrorType.DENIED_BY_FRAUD,
											"Fraud transaction with PAY_ID="
													+ fields.get(CrmFieldType.PAY_ID.getName())
													+ " detected and blocked" + "with FRAUD_TYPE: "
													+ fraudType.getValue());
								}
							}
						}
						break;
						
					case BLOCK_VPA_TXN:
						if ((txnType.equals(TransactionType.NEWORDER.getName()))) {
							break;
						}
						
						if(StringUtils.isNotBlank(fields.get(FieldType.PAYER_ADDRESS.getName()))){
							String requestVpa= fields.get(FieldType.PAYER_ADDRESS.getName());
							
							String decryptedVpa;
							
							if(StringUtils.isNotBlank(subMerchantPayId)){
								decryptedVpa =  defaultCryptoManager.decryptVpa(subMerchantPayId, fraudPrevention.getVpaEncrypted());
							}else{
								decryptedVpa =  defaultCryptoManager.decryptVpa(payId, fraudPrevention.getVpaEncrypted());
							}

							if (requestVpa.equals(decryptedVpa)) {
								long noOfTransctions = fraudTxnDao
										.getPerVpaTransactions(fields.get(FieldType.PAY_ID.getName()), decryptedVpa);
								long perCardTxnAllowed = Long.parseLong(fraudPrevention.getVpaTotalTransactionAllowed());
								
								boolean alwaysOnflag = fraudPrevention.isAlwaysOnFlag();
								
								if (alwaysOnflag == false) {
									result = validateTime(fraudPrevention);
								} else {
									if (!(noOfTransctions < perCardTxnAllowed)) {
										fields.put(Constants.PG_FRAUD_TYPE.getValue(),
												fraudPrevention.getFraudType().getValue());
										fields.put(FieldType.STATUS.getName(), StatusType.DENIED_BY_FRAUD.getName());
										throw new SystemException(ErrorType.DENIED_BY_FRAUD,
												"Fraud transaction with PAY_ID="
														+ fields.get(CrmFieldType.PAY_ID.getName())
														+ " detected and blocked" + "with FRAUD_TYPE: "
														+ fraudType.getValue());
									}
								}
								
								if (result == true && !(noOfTransctions < perCardTxnAllowed)) {
									fields.put(Constants.PG_FRAUD_TYPE.getValue(),
											fraudPrevention.getFraudType().getValue());
									fields.put(FieldType.STATUS.getName(), StatusType.DENIED_BY_FRAUD.getName());
									throw new SystemException(ErrorType.DENIED_BY_FRAUD,
											"Fraud transaction with PAY_ID="
													+ fields.get(CrmFieldType.PAY_ID.getName())
													+ " detected and blocked" + "with FRAUD_TYPE: "
													+ fraudType.getValue());
								}
								
							}
						}
						break;
						
					default: // logically not reachable
						logger.error("Something went wrong while checking fraud field values");
						break;
					}
					/* } */

				}
			} else {
				return;
			}

		} catch (ParseException exception) {
			logger.error("error : " , exception);
		} catch (UnknownHostException exception) {
			logger.error("error : " , exception);
		}
	}

	// to validate fields of same category
	private void validateRequestField(Fields fields, final FraudPrevention fraudPrevention,
			final String fraudFieldValue) throws SystemException {
		// checking other fields
		boolean result = true;
		FraudRuleType fraudType = fraudPrevention.getFraudType();
		if (fields.contains(fraudType.getFieldName())) {
			if (fields.get(fraudType.getFieldName()).equalsIgnoreCase(fraudFieldValue)) {
				boolean alwaysOnflag = fraudPrevention.isAlwaysOnFlag();
				if (alwaysOnflag == false) {
					result = validateTime(fraudPrevention);
				} else {
					fields.put(Constants.PG_FRAUD_TYPE.getValue(), fraudPrevention.getFraudType().getValue());
					fields.put(FieldType.STATUS.getName(), StatusType.DENIED_BY_FRAUD.getName());
					throw new SystemException(ErrorType.DENIED_BY_FRAUD,
							"Fraud transaction with PAY_ID=" + fields.get(CrmFieldType.PAY_ID.getName())
									+ " detected and blocked" + "with FRAUD_TYPE: " + fraudType.getValue());
				}
				if (result == true) {
					fields.put(Constants.PG_FRAUD_TYPE.getValue(), fraudPrevention.getFraudType().getValue());
					fields.put(FieldType.STATUS.getName(), StatusType.DENIED_BY_FRAUD.getName());
					throw new SystemException(ErrorType.DENIED_BY_FRAUD,
							"Fraud transaction with PAY_ID=" + fields.get(CrmFieldType.PAY_ID.getName())
									+ " detected and blocked" + "with FRAUD_TYPE: " + fraudType.getValue());
				}
			}
		}
	}

	// to validate fields of same category
	private boolean validateWhiteListField(Fields fields, final FraudPrevention fraudPrevention,
			final Collection<String> ipAddressList, boolean check) throws SystemException {
		// checking other fields
		logger.info(" to validate fields of same category  validateWhiteListField");
		FraudRuleType fraudType = fraudPrevention.getFraudType();
		if (fields.contains(fraudType.getFieldName())) {
			logger.info(" to validate fields of same category  validateWhiteListField fraudType.getFieldName()");

			for (String ipAddress : ipAddressList) {
				if (fields.get(fraudType.getFieldName()).equalsIgnoreCase(ipAddress)) {
					check = true;
				}
			}
			logger.info(" to validate fields of same done");
		}
		return check;
	}

	private void validateDomainFields(Fields fields, final FraudPrevention fraudPrevention, String blockDomain)
			throws SystemException, UnknownHostException {
		FraudRuleType fraudType = fraudPrevention.getFraudType();
		if (fields.contains(fraudType.getFieldName())) {
			InetAddress addr = InetAddress.getByName(fields.get(FieldType.INTERNAL_CUST_IP.getName()));
			// InetAddress host = addr.getLocalHost();
			String host = addr.getHostName();
			if (host.equalsIgnoreCase(blockDomain)) {
				fields.put(Constants.PG_FRAUD_TYPE.getValue(), fraudPrevention.getFraudType().getValue());
				fields.put(FieldType.STATUS.getName(), StatusType.DENIED_BY_FRAUD.getName());
				throw new SystemException(ErrorType.DENIED_BY_FRAUD,
						"Fraud transaction with PAY_ID=" + fields.get(CrmFieldType.PAY_ID.getName())
								+ " detected and blocked" + "with FRAUD_TYPE: " + fraudType.getValue());
			}
		}
	}

	private boolean validateTime(FraudPrevention fraudPrevention) {
		String dateActiveFrom = fraudPrevention.getDateActiveFrom();
		String dateActiveTo = fraudPrevention.getDateActiveTo();

		Date date = new Date();
		Format formatter = new SimpleDateFormat("yyyyMMdd");
		String todayDate = formatter.format(date);

		int compareDateFrom = dateActiveFrom.compareTo(todayDate);
		int compareDateTo = dateActiveTo.compareTo(todayDate);

		if ((compareDateFrom == 0 || compareDateFrom < 0) && ((compareDateTo == 0 || compareDateTo > 0))) {

			String startTime = fraudPrevention.getStartTime();
			String endTime = fraudPrevention.getEndTime();

			SimpleDateFormat sdfTime = new SimpleDateFormat("HHmmss");
			Date now = new Date();
			String currentTime = sdfTime.format(now);

			int compareStartTime = startTime.compareTo(currentTime);
			int compareEndTime = endTime.compareTo(currentTime);

			if ((compareStartTime == 0 || compareStartTime < 0) && ((compareEndTime == 0 || compareEndTime > 0))) {

				String repeatDays = fraudPrevention.getRepeatDays();
				Collection<String> daysList = Helper.parseFields(repeatDays);
				DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
				Weekdays dayInstance = Weekdays.getDayInstance(dayOfWeek.toString());
				String dayCode = dayInstance.getCode();

				for (String day : daysList) {
					if (dayCode.equals(day)) {

						return true;

					}
				}
			}
		}
		return false;
	}

	private Long txnVelocityCheck(Fields fields, FraudPrevention fraudPrevention, String cardHash) {
		long count = 0;
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
		if (fraudPrevention.getTimePeriod().equalsIgnoreCase("DAILY")) {

			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 0);
			Date date = cal.getTime();
			String modifiedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
			String startDate = modifiedDate + " 00:00:00";
			String endDate = modifiedDate + " 23:59:59";

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject payIdQuery = new BasicDBObject(FieldType.PAY_ID.getName(),
					fields.get(FieldType.PAY_ID.getName()));
			BasicDBObject statusQuery = new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
			
			BasicDBObject txnTypeQuery = new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());

			paramConditionLst.add(payIdQuery);
			paramConditionLst.add(statusQuery);
			
			paramConditionLst.add(txnTypeQuery);

			BasicDBObject dateQuery = new BasicDBObject();

			dateQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(startDate).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(endDate).toLocalizedPattern()).get());

			paramConditionLst.add(dateQuery);
			
			// sending Start date and end date is same
            paramConditionLst.add(new BasicDBObject("$or", DateCreater.getDateIndex(modifiedDate, modifiedDate)));
			
			
			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			
			logger.info("final Query for "+fraudPrevention.getTimePeriod()+" txnVelocityCheck() "+finalquery);

			count = coll.count(finalquery);

		} else if (fraudPrevention.getTimePeriod().equalsIgnoreCase("WEEKLY")) {

			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 0);
			Date date = cal.getTime();
			String modifiedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
			String endDate = modifiedDate + " 23:59:59";

			// Date before a week
			Calendar cal2 = Calendar.getInstance();
			cal2.add(Calendar.DATE, -6);
			Date date2 = cal2.getTime();
			String modifiedDate2 = new SimpleDateFormat("yyyy-MM-dd").format(date2);
			String startDate = modifiedDate2 + " 23:59:59";

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject payIdQuery = new BasicDBObject(FieldType.PAY_ID.getName(),
					fields.get(FieldType.PAY_ID.getName()));
			BasicDBObject statusQuery = new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
			
			BasicDBObject txnTypeQuery = new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());

			paramConditionLst.add(payIdQuery);
			paramConditionLst.add(statusQuery);
			
			paramConditionLst.add(txnTypeQuery);

			BasicDBObject dateQuery = new BasicDBObject();

			dateQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(startDate).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(endDate).toLocalizedPattern()).get());

			paramConditionLst.add(dateQuery);
			
			// sending Start date and end date is same
            paramConditionLst.add(new BasicDBObject("$or", DateCreater.getDateIndex(modifiedDate2, modifiedDate)));
			
			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			logger.info("final Query for "+fraudPrevention.getTimePeriod()+" txnVelocityCheck() "+finalquery);

			count = coll.count(finalquery);

		}

		else if (fraudPrevention.getTimePeriod().equalsIgnoreCase("MONTHLY")) {

			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 0);
			Date date = cal.getTime();
			String modifiedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
			String endDate = modifiedDate + " 23:59:59";

			// Date before a month
			Calendar cal2 = Calendar.getInstance();
			cal2.add(Calendar.DATE, -29);
			Date date2 = cal2.getTime();
			String modifiedDate2 = new SimpleDateFormat("yyyy-MM-dd").format(date2);
			String startDate = modifiedDate2 + " 23:59:59";

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject payIdQuery = new BasicDBObject(FieldType.PAY_ID.getName(),
					fields.get(FieldType.PAY_ID.getName()));
			BasicDBObject statusQuery = new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
			BasicDBObject txnTypeQuery = new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());

			paramConditionLst.add(payIdQuery);
			paramConditionLst.add(statusQuery);
			paramConditionLst.add(txnTypeQuery);

			BasicDBObject dateQuery = new BasicDBObject();

			dateQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(startDate).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(endDate).toLocalizedPattern()).get());

			paramConditionLst.add(dateQuery);
			// sending Start date and end date is same
            paramConditionLst.add(new BasicDBObject("$or", DateCreater.getDateIndex(modifiedDate2, modifiedDate)));
			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			logger.info("final Query for "+fraudPrevention.getTimePeriod()+" txnVelocityCheck() "+finalquery);

			count = coll.count(finalquery);

		}

		else if (fraudPrevention.getTimePeriod().equalsIgnoreCase("YEARLY")) {

			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 0);
			Date date = cal.getTime();
			String modifiedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
			String endDate = modifiedDate + " 23:59:59";

			// Date before a year
			Calendar cal2 = Calendar.getInstance();
			cal2.add(Calendar.DATE, -364);
			Date date2 = cal2.getTime();
			String modifiedDate2 = new SimpleDateFormat("yyyy-MM-dd").format(date2);
			String startDate = modifiedDate2 + " 23:59:59";

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject payIdQuery = new BasicDBObject(FieldType.PAY_ID.getName(),
					fields.get(FieldType.PAY_ID.getName()));
			BasicDBObject statusQuery = new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
			
			BasicDBObject txnTypeQuery = new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());

			paramConditionLst.add(payIdQuery);
			paramConditionLst.add(statusQuery);
			
			paramConditionLst.add(txnTypeQuery);

			BasicDBObject dateQuery = new BasicDBObject();

			dateQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(startDate).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(endDate).toLocalizedPattern()).get());

			paramConditionLst.add(dateQuery);
			paramConditionLst.add(new BasicDBObject("$or", DateCreater.getDateIndex(modifiedDate2, modifiedDate)));
			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			logger.info("final Query for "+fraudPrevention.getTimePeriod()+" txnVelocityCheck() "+finalquery);

			count = coll.count(finalquery);

		}

		return count;

	}

	private BigDecimal amountVelocityCheck(Fields fields, FraudPrevention fraudPrevention, String cardHash) {
		BigDecimal totalTxnAmount = BigDecimal.ZERO;
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
		if (fraudPrevention.getTimePeriod().equalsIgnoreCase("DAILY")) {
			// Date Today
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 0);
			Date date = cal.getTime();
			String modifiedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
			String startDate = modifiedDate + " 00:00:00";
			String endDate = modifiedDate + " 23:59:59";

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject payIdQuery = new BasicDBObject(FieldType.PAY_ID.getName(),
					fields.get(FieldType.PAY_ID.getName()));
			BasicDBObject statusQuery = new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
			
			BasicDBObject txnTypeQuery = new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());

			paramConditionLst.add(payIdQuery);
			paramConditionLst.add(statusQuery);
			paramConditionLst.add(txnTypeQuery);

			BasicDBObject dateQuery = new BasicDBObject();

			dateQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(startDate).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(endDate).toLocalizedPattern()).get());

			paramConditionLst.add(dateQuery);
			paramConditionLst.add(new BasicDBObject("$or", DateCreater.getDateIndex(modifiedDate, modifiedDate)));
			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			logger.info("final Query for "+fraudPrevention.getTimePeriod()+" amountVelocityCheck() "+finalquery);

			FindIterable<Document> cursor = coll.find(finalquery).sort(new BasicDBObject("CREATE_DATE", -1));

			MongoCursor<Document> cursor2 = cursor.iterator();

			while (cursor2.hasNext()) {
				Document documentObj = cursor2.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					documentObj = dataEncDecTool.decryptDocument(documentObj);
				} 
				
				if (null != documentObj) {
					String txnAmount = documentObj.getString(FieldType.TOTAL_AMOUNT.getName());
					totalTxnAmount = totalTxnAmount.add(new BigDecimal(txnAmount));
				}

			}
			cursor2.close();

		} else if (fraudPrevention.getTimePeriod().equalsIgnoreCase("WEEKLY")) {

			// Date Today
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 0);
			Date date = cal.getTime();
			String modifiedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
			String endDate = modifiedDate + " 23:59:59";

			// Date before a week
			Calendar cal2 = Calendar.getInstance();
			cal2.add(Calendar.DATE, -6);
			Date date2 = cal2.getTime();
			String modifiedDate2 = new SimpleDateFormat("yyyy-MM-dd").format(date2);
			String startDate = modifiedDate2 + " 23:59:59";

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject payIdQuery = new BasicDBObject(FieldType.PAY_ID.getName(),
					fields.get(FieldType.PAY_ID.getName()));
			BasicDBObject statusQuery = new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
			
			BasicDBObject txnTypeQuery = new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());

			paramConditionLst.add(payIdQuery);
			paramConditionLst.add(statusQuery);
			
			paramConditionLst.add(txnTypeQuery);

			BasicDBObject dateQuery = new BasicDBObject();

			dateQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(startDate).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(endDate).toLocalizedPattern()).get());

			paramConditionLst.add(dateQuery);
			paramConditionLst.add(new BasicDBObject("$or", DateCreater.getDateIndex(modifiedDate2, modifiedDate)));
			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			logger.info("final Query for "+fraudPrevention.getTimePeriod()+" amountVelocityCheck() "+finalquery);

			FindIterable<Document> cursor = coll.find(finalquery).sort(new BasicDBObject("CREATE_DATE", -1));

			MongoCursor<Document> cursor2 = cursor.iterator();

			while (cursor2.hasNext()) {
				Document documentObj = cursor2.next();

				
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					documentObj = dataEncDecTool.decryptDocument(documentObj);
				} 
				
				if (null != documentObj) {
					String txnAmount = documentObj.getString(FieldType.TOTAL_AMOUNT.getName());
					totalTxnAmount = totalTxnAmount.add(new BigDecimal(txnAmount));
				}

			}
			cursor2.close();
		}

		else if (fraudPrevention.getTimePeriod().equalsIgnoreCase("MONTHLY")) {

			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 0);
			Date date = cal.getTime();
			String modifiedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
			String endDate = modifiedDate + " 23:59:59";

			// Date before a month
			Calendar cal2 = Calendar.getInstance();
			cal2.add(Calendar.DATE, -29);
			Date date2 = cal2.getTime();
			String modifiedDate2 = new SimpleDateFormat("yyyy-MM-dd").format(date2);
			String startDate = modifiedDate2 + " 23:59:59";

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject payIdQuery = new BasicDBObject(FieldType.PAY_ID.getName(),
					fields.get(FieldType.PAY_ID.getName()));
			BasicDBObject statusQuery = new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
			
			BasicDBObject txnTypeQuery = new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());

			paramConditionLst.add(payIdQuery);
			paramConditionLst.add(statusQuery);
			
			paramConditionLst.add(txnTypeQuery);

			BasicDBObject dateQuery = new BasicDBObject();

			dateQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(startDate).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(endDate).toLocalizedPattern()).get());

			paramConditionLst.add(dateQuery);
			paramConditionLst.add(new BasicDBObject("$or", DateCreater.getDateIndex(modifiedDate2, modifiedDate)));
			
			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			logger.info("final Query for "+fraudPrevention.getTimePeriod()+" amountVelocityCheck() "+finalquery);
			

			FindIterable<Document> cursor = coll.find(finalquery).sort(new BasicDBObject("CREATE_DATE", -1));

			MongoCursor<Document> cursor2 = cursor.iterator();

			while (cursor2.hasNext()) {
				Document documentObj = cursor2.next();
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
                        && propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					documentObj = dataEncDecTool.decryptDocument(documentObj);
                }
				
				if (null != documentObj) {
					String txnAmount = documentObj.getString(FieldType.TOTAL_AMOUNT.getName());
					totalTxnAmount = totalTxnAmount.add(new BigDecimal(txnAmount));
				}

			}
			cursor2.close();
		}

		else if (fraudPrevention.getTimePeriod().equalsIgnoreCase("YEARLY")) {

			// Date Today
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 0);
			Date date = cal.getTime();
			String modifiedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
			String endDate = modifiedDate + " 23:59:59";

			// Date before a year
			Calendar cal2 = Calendar.getInstance();
			cal2.add(Calendar.DATE, -364);
			Date date2 = cal2.getTime();
			String modifiedDate2 = new SimpleDateFormat("yyyy-MM-dd").format(date2);
			String startDate = modifiedDate2 + " 23:59:59";

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject payIdQuery = new BasicDBObject(FieldType.PAY_ID.getName(),
					fields.get(FieldType.PAY_ID.getName()));
			BasicDBObject statusQuery = new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
			
			BasicDBObject txnTypeQuery = new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());

			paramConditionLst.add(payIdQuery);
			paramConditionLst.add(statusQuery);
			
			paramConditionLst.add(txnTypeQuery);

			BasicDBObject dateQuery = new BasicDBObject();

			dateQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(startDate).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(endDate).toLocalizedPattern()).get());

			paramConditionLst.add(dateQuery);
			paramConditionLst.add(new BasicDBObject("$or", DateCreater.getDateIndex(modifiedDate2, modifiedDate)));
			
			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			logger.info("final Query for "+fraudPrevention.getTimePeriod()+" amountVelocityCheck() "+finalquery);

			FindIterable<Document> cursor = coll.find(finalquery).sort(new BasicDBObject("CREATE_DATE", -1));

			MongoCursor<Document> cursor2 = cursor.iterator();

			while (cursor2.hasNext()) {
				Document documentObj = cursor2.next();
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
                        && propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					documentObj = dataEncDecTool.decryptDocument(documentObj);
                }
				
				if (null != documentObj) {
					String txnAmount = documentObj.getString(FieldType.TOTAL_AMOUNT.getName());
					totalTxnAmount = totalTxnAmount.add(new BigDecimal(txnAmount));
				}

			}
			cursor2.close();
		}

		return totalTxnAmount;

	}
	
	private BigDecimal saleAmountVelocityCheck(Fields fields, FraudPrevention fraudPrevention) {
		BigDecimal totalTxnAmount = BigDecimal.ZERO;
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
		if (fraudPrevention.getTimePeriod().equalsIgnoreCase("DAILY")) {
			// Date Today
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 0);
			Date date = cal.getTime();
			String modifiedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
			String startDate = modifiedDate + " 00:00:00";
			String endDate = modifiedDate + " 23:59:59";

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject payIdQuery = new BasicDBObject(FieldType.PAY_ID.getName(),
					fields.get(FieldType.PAY_ID.getName()));
			String dateIndex = modifiedDate.replace("-", "");
			
			//BasicDBObject dateIndexQuery = new BasicDBObject(FieldType.DATE_INDEX.getName(), dateIndex);
			BasicDBObject statusQuery = new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
			BasicDBObject txnTypeQuery = new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
			BasicDBObject paymentTypeQuery = new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), fields.get(FieldType.PAYMENT_TYPE.getName()));
			BasicDBObject paymentRegionQuery = new BasicDBObject(FieldType.PAYMENTS_REGION.getName(), fields.get(FieldType.PAYMENTS_REGION.getName()));
			
			
			//paramConditionLst.add(dateIndexQuery);
			paramConditionLst.add(payIdQuery);
			paramConditionLst.add(statusQuery);
			paramConditionLst.add(txnTypeQuery);
			paramConditionLst.add(paymentTypeQuery);
			paramConditionLst.add(paymentRegionQuery);

			BasicDBObject dateQuery = new BasicDBObject();

			dateQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(startDate).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(endDate).toLocalizedPattern()).get());

			paramConditionLst.add(dateQuery);
			paramConditionLst.add(new BasicDBObject("$or", DateCreater.getDateIndex(modifiedDate, modifiedDate)));
			
			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			logger.info("final Query for "+fraudPrevention.getTimePeriod()+" saleAmountVelocityCheck() "+finalquery);

			FindIterable<Document> cursor = coll.find(finalquery).sort(new BasicDBObject("CREATE_DATE", -1));

			MongoCursor<Document> cursor2 = cursor.iterator();

			while (cursor2.hasNext()) {
				Document documentObj = cursor2.next();
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
                        && propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					documentObj = dataEncDecTool.decryptDocument(documentObj);
                }
				
				if (null != documentObj) {
					String txnAmount = documentObj.getString(FieldType.TOTAL_AMOUNT.getName());
					totalTxnAmount = totalTxnAmount.add(new BigDecimal(txnAmount));
				}

			}
			
			
			cursor2.close();

		} else if (fraudPrevention.getTimePeriod().equalsIgnoreCase("WEEKLY")) {

			// Date Today
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 0);
			Date date = cal.getTime();
			String modifiedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
			String endDate = modifiedDate + " 23:59:59";

			// Date before a week
			Calendar cal2 = Calendar.getInstance();
			cal2.add(Calendar.DATE, -6);
			Date date2 = cal2.getTime();
			String modifiedDate2 = new SimpleDateFormat("yyyy-MM-dd").format(date2);
			String startDate = modifiedDate2 + " 23:59:59";
		

			
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject payIdQuery = new BasicDBObject(FieldType.PAY_ID.getName(),
					fields.get(FieldType.PAY_ID.getName()));
			
			
			BasicDBObject statusQuery = new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
			BasicDBObject txnTypeQuery = new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
			BasicDBObject paymentTypeQuery = new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), fields.get(FieldType.PAYMENT_TYPE.getName()));
			BasicDBObject paymentRegionQuery = new BasicDBObject(FieldType.PAYMENTS_REGION.getName(), fields.get(FieldType.PAYMENTS_REGION.getName()));
			
			paramConditionLst.add(payIdQuery);
			paramConditionLst.add(statusQuery);
			paramConditionLst.add(txnTypeQuery);
			paramConditionLst.add(paymentTypeQuery);
			paramConditionLst.add(paymentRegionQuery);


			BasicDBObject dateQuery = new BasicDBObject();

			dateQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(startDate).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(endDate).toLocalizedPattern()).get());

			paramConditionLst.add(dateQuery);
			paramConditionLst.add(new BasicDBObject("$or", DateCreater.getDateIndex(modifiedDate2, modifiedDate)));
			
			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			logger.info("final Query for "+fraudPrevention.getTimePeriod()+" saleAmountVelocityCheck() "+finalquery);

			FindIterable<Document> cursor = coll.find(finalquery).sort(new BasicDBObject("CREATE_DATE", -1));

			MongoCursor<Document> cursor2 = cursor.iterator();

			while (cursor2.hasNext()) {
				Document documentObj = cursor2.next();
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
                        && propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					documentObj = dataEncDecTool.decryptDocument(documentObj);
                }
				
				if (null != documentObj) {
					String txnAmount = documentObj.getString(FieldType.TOTAL_AMOUNT.getName());
					totalTxnAmount = totalTxnAmount.add(new BigDecimal(txnAmount));
				}

			}
			cursor2.close();
		}

		else if (fraudPrevention.getTimePeriod().equalsIgnoreCase("MONTHLY")) {

			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 0);
			Date date = cal.getTime();
			String modifiedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
			String endDate = modifiedDate + " 23:59:59";

			// Date before a month
			Calendar cal2 = Calendar.getInstance();
			cal2.add(Calendar.DATE, -29);
			Date date2 = cal2.getTime();
			String modifiedDate2 = new SimpleDateFormat("yyyy-MM-dd").format(date2);
			String startDate = modifiedDate2 + " 23:59:59";

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject payIdQuery = new BasicDBObject(FieldType.PAY_ID.getName(),
					fields.get(FieldType.PAY_ID.getName()));
			BasicDBObject statusQuery = new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
			BasicDBObject txnTypeQuery = new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
			BasicDBObject paymentTypeQuery = new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), fields.get(FieldType.PAYMENT_TYPE.getName()));
			BasicDBObject paymentRegionQuery = new BasicDBObject(FieldType.PAYMENTS_REGION.getName(), fields.get(FieldType.PAYMENTS_REGION.getName()));
			
			paramConditionLst.add(payIdQuery);
			paramConditionLst.add(statusQuery);
			paramConditionLst.add(txnTypeQuery);
			paramConditionLst.add(paymentTypeQuery);
			paramConditionLst.add(paymentRegionQuery);

			BasicDBObject dateQuery = new BasicDBObject();

			dateQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(startDate).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(endDate).toLocalizedPattern()).get());

			paramConditionLst.add(dateQuery);
			paramConditionLst.add(new BasicDBObject("$or", DateCreater.getDateIndex(modifiedDate2, modifiedDate)));
			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			logger.info("final Query for "+fraudPrevention.getTimePeriod()+" saleAmountVelocityCheck() "+finalquery);

			FindIterable<Document> cursor = coll.find(finalquery).sort(new BasicDBObject("CREATE_DATE", -1));

			MongoCursor<Document> cursor2 = cursor.iterator();

			while (cursor2.hasNext()) {
				Document documentObj = cursor2.next();
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
                        && propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					documentObj = dataEncDecTool.decryptDocument(documentObj);
                }
				
				if (null != documentObj) {
					String txnAmount = documentObj.getString(FieldType.TOTAL_AMOUNT.getName());
					totalTxnAmount = totalTxnAmount.add(new BigDecimal(txnAmount));
				}

			}
			cursor2.close();
		}

		else if (fraudPrevention.getTimePeriod().equalsIgnoreCase("YEARLY")) {

			// Date Today
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 0);
			Date date = cal.getTime();
			String modifiedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
			String endDate = modifiedDate + " 23:59:59";

			// Date before a year
			Calendar cal2 = Calendar.getInstance();
			cal2.add(Calendar.DATE, -364);
			Date date2 = cal2.getTime();
			String modifiedDate2 = new SimpleDateFormat("yyyy-MM-dd").format(date2);
			String startDate = modifiedDate2 + " 23:59:59";

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject payIdQuery = new BasicDBObject(FieldType.PAY_ID.getName(),
					fields.get(FieldType.PAY_ID.getName()));
			BasicDBObject statusQuery = new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
			BasicDBObject txnTypeQuery = new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
			BasicDBObject paymentTypeQuery = new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), fields.get(FieldType.PAYMENT_TYPE.getName()));
			BasicDBObject paymentRegionQuery = new BasicDBObject(FieldType.PAYMENTS_REGION.getName(), fields.get(FieldType.PAYMENTS_REGION.getName()));
			
			paramConditionLst.add(payIdQuery);
			paramConditionLst.add(statusQuery);
			paramConditionLst.add(txnTypeQuery);
			paramConditionLst.add(paymentTypeQuery);
			paramConditionLst.add(paymentRegionQuery);

			BasicDBObject dateQuery = new BasicDBObject();

			dateQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(startDate).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(endDate).toLocalizedPattern()).get());

			paramConditionLst.add(dateQuery);
			paramConditionLst.add(new BasicDBObject("$or", DateCreater.getDateIndex(modifiedDate2, modifiedDate)));
			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			logger.info("final Query for "+fraudPrevention.getTimePeriod()+" saleAmountVelocityCheck() "+finalquery);

			FindIterable<Document> cursor = coll.find(finalquery).sort(new BasicDBObject("CREATE_DATE", -1));

			MongoCursor<Document> cursor2 = cursor.iterator();

			while (cursor2.hasNext()) {
				Document documentObj = cursor2.next();
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
                        && propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					documentObj = dataEncDecTool.decryptDocument(documentObj);
                }
				
				if (null != documentObj) {
					String txnAmount = documentObj.getString(FieldType.TOTAL_AMOUNT.getName());
					totalTxnAmount = totalTxnAmount.add(new BigDecimal(txnAmount));
				}

			}
			cursor2.close();
		}

		return totalTxnAmount;

	}
}