package com.paymentgateway.pgui.action.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.IssuerDetailsDao;
import com.paymentgateway.commons.dao.PaymentOptionsDao;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.IssuerDetails;
import com.paymentgateway.commons.user.PaymentOptions;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PaymentTypeProvider;
import com.paymentgateway.commons.util.PaymentTypeTransactionProvider;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.pg.core.util.LocaleLanguageType;

@Service("prepareRequestParemeterService")
public class PrepareRequestParemeterService {

	private static Logger logger = LoggerFactory.getLogger(PrepareRequestParemeterService.class.getName());

	@Autowired
	private PaymentTypeProvider paymentTypeProvider;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private PaymentOptionsDao paymentOptionsDao;

	@Autowired
	private IssuerDetailsDao issuerDetailsDao;

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private UserSettingDao userSettingDao;


	private Map<String, Object> supportedPaymentTypeMap = new HashMap<String, Object>();

	@SuppressWarnings({ "static-access" })
	public JSONObject prepareRequestParameter(Fields fields, User user, HttpServletRequest request)
			throws SystemException {
		
		UserSettingData userSettings=userSettingDao.fetchDataUsingPayId(user.getPayId());

		PaymentTypeTransactionProvider paymentTypeTransactionProvider = paymentTypeProvider
				.setSupportedPaymentOptions(user.getPayId());
//        PaymentOptions paymentOption = paymentOptionsDao.getPaymentOption(user.getPayId());	
		PaymentOptions paymentOption = new PaymentOptions();
		if (fields.contains(FieldType.SUB_MERCHANT_ID.getName())
				&& StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
			paymentOption = paymentOptionsDao.getPaymentOption(fields.get(FieldType.SUB_MERCHANT_ID.getName()));
		} else {
			paymentOption = paymentOptionsDao.getPaymentOption(fields.get(FieldType.PAY_ID.getName()));
		}
		JSONObject requestParameterJson = new JSONObject();
		if (paymentOption != null) {

			setSupportedPaymentTypeMap(paymentTypeTransactionProvider.getSupportedPaymentTypeMap());

			request.getSession().setAttribute(Constants.PAYMENT_TYPE_MOP.getValue(),
					paymentTypeTransactionProvider.getSupportedPaymentTypeMap());

			// StringBuilder request = new StringBuilder();
			// (String) request.getSession().getAttribute(FieldType.PAY_ID.getName())

			String paymentTypeMops = paymentTypeTransactionProvider.getSupportedPaymentTypeMap().toString();
			Object regionType = (Object) request.getSession().getAttribute(Constants.REGION_TYPE.getValue());
			Object merchantPaymentTypeObject = (Object) request.getSession()
					.getAttribute(FieldType.MERCHANT_PAYMENT_TYPE.getName());

			String merchantPaymentType = "";
			if (null != merchantPaymentTypeObject) {
				merchantPaymentType = (String) merchantPaymentTypeObject;
			}

			if (merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AD.getValue())
					&& (!paymentTypeMops.contains(Constants.PAYMENT_TYPE_AD.getValue()))) {
				throw new SystemException(ErrorType.PAYMENT_OPTION_NOT_SUPPORTED, "Unsupported payment type");
			}
			if (merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_UPI.getValue())
					&& (!paymentTypeMops.contains(Constants.PAYMENT_TYPE_UP.getValue()))) {
				throw new SystemException(ErrorType.PAYMENT_OPTION_NOT_SUPPORTED, "Unsupported payment type");
			}
			if (merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_MQR.getValue())
					&& (!paymentTypeMops.contains(Constants.PAYMENT_TYPE_MQR.getValue()))) {
				throw new SystemException(ErrorType.PAYMENT_OPTION_NOT_SUPPORTED, "Unsupported payment type");
			}
			if (merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CARD.getValue())
					&& ((!paymentTypeMops.contains(Constants.PAYMENT_TYPE_CC.getValue()))
							|| (!paymentTypeMops.contains(Constants.PAYMENT_TYPE_DC.getValue())))) {
				throw new SystemException(ErrorType.PAYMENT_OPTION_NOT_SUPPORTED, "Unsupported payment type");
			}
			if (merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CC.getValue())
					&& (!paymentTypeMops.contains(Constants.PAYMENT_TYPE_CC.getValue()))) {
				throw new SystemException(ErrorType.PAYMENT_OPTION_NOT_SUPPORTED, "Unsupported payment type");
			}
			if (merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_DC.getValue())
					&& (!paymentTypeMops.contains(Constants.PAYMENT_TYPE_DC.getValue()))) {
				throw new SystemException(ErrorType.PAYMENT_OPTION_NOT_SUPPORTED, "Unsupported payment type");
			}
			if (merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_WL.getValue())
					&& (!paymentTypeMops.contains(Constants.PAYMENT_TYPE_WL.getValue()))) {
				throw new SystemException(ErrorType.PAYMENT_OPTION_NOT_SUPPORTED, "Unsupported payment type");
			}
			if (merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_COD.getValue())
					&& (!paymentTypeMops.contains(Constants.PAYMENT_TYPE_CD.getValue()))) {
				throw new SystemException(ErrorType.PAYMENT_OPTION_NOT_SUPPORTED, "Unsupported payment type");
			}
			if (merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_CC.getValue())
					&& (!paymentTypeMops.contains(Constants.PAYMENT_TYPE_EM_CC.getValue()))) {
				throw new SystemException(ErrorType.PAYMENT_OPTION_NOT_SUPPORTED, "Unsupported payment type");
			}
			if (merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_DC.getValue())
					&& (!paymentTypeMops.contains(Constants.PAYMENT_TYPE_EM_DC.getValue()))) {
				throw new SystemException(ErrorType.PAYMENT_OPTION_NOT_SUPPORTED, "Unsupported payment type");
			}
			if (merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_NB.getValue())
					&& (!paymentTypeMops.contains(Constants.PAYMENT_TYPE_NB.getValue()))) {
				throw new SystemException(ErrorType.PAYMENT_OPTION_NOT_SUPPORTED, "Unsupported payment type");
			}
			if (merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CRYPTO.getValue())
					&& (!paymentTypeMops.contains(Constants.PAYMENT_TYPE_CRYPTO.getValue()))) {
				throw new SystemException(ErrorType.PAYMENT_OPTION_NOT_SUPPORTED, "Unsupported payment type");
			}
			// end

			boolean all = false;
			if (fields.contains(FieldType.EPOS_PAYMENT_OPTION.getName())
					&& StringUtils.isNotBlank(fields.get(FieldType.EPOS_PAYMENT_OPTION.getName()))) {

				if (fields.get(FieldType.EPOS_PAYMENT_OPTION.getName()).equalsIgnoreCase("PG_QR")
						|| fields.get(FieldType.EPOS_PAYMENT_OPTION.getName()).equalsIgnoreCase("ALL")) {
					all = true;
				}

				logger.info("Creating request parameter for EPOS specifc to customer request");
				requestParameterJson.put(Constants.ADS.getValue(), true);
				requestParameterJson.put(Constants.PAYMENT_ADSIMG_URL.getValue(),
						propertiesManager.propertiesMap.get(Constants.PAYMENT_ADSIMG_URL.getValue()));
				requestParameterJson.put(Constants.PAYMENT_ADSIMG_LINK_URL.getValue(),
						propertiesManager.propertiesMap.get(Constants.PAYMENT_ADSIMG_LINK_URL.getValue()));
				// Check TDR or Surcharge Mode
				if (StringUtils.isNotBlank(fields.get(FieldType.SURCHARGE_FLAG.getName()))
						&& fields.get(FieldType.SURCHARGE_FLAG.getName()).equalsIgnoreCase("Y")) {

					requestParameterJson.put("isSurcharge", true);
				} else {
					requestParameterJson.put("isSurcharge", false);
				}
				requestParameterJson.put("currencyCode",
						(String) request.getSession().getAttribute(FieldType.CURRENCY_CODE.getName()));

				if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_AD.getValue())
						&& ((all) || (fields.get(FieldType.EPOS_PAYMENT_OPTION.getName())
								.equalsIgnoreCase(PaymentType.AD.getCode())))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_UPI.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CARD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_COD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CRYPTO.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AAMARPAY.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_WL.getValue()))) {
					requestParameterJson.put("autoDebit", true);
				} else {
					requestParameterJson.put("autoDebit", false);
				}

				if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_WL.getValue()) && (paymentOption.isWallet())
						&& ((all) || (fields.get(FieldType.EPOS_PAYMENT_OPTION.getName())
								.equalsIgnoreCase(PaymentType.WALLET.getCode())))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_UPI.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CARD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_COD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CRYPTO.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AAMARPAY.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AD.getValue()))) {
					requestParameterJson.put("wallet", true);
				} else {
					requestParameterJson.put("wallet", false);
				}

				if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_CC.getValue())
						&& ((all) || (fields.get(FieldType.EPOS_PAYMENT_OPTION.getName()).equalsIgnoreCase("CARD")))
						&& (paymentOption.isCreditCard())
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_UPI.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_COD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AAMARPAY.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CRYPTO.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_WL.getValue()))) {
					requestParameterJson.put("creditCard", true);
				} else {
					requestParameterJson.put("creditCard", false);
				}

				if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_DC.getValue())
						&& ((all) || (fields.get(FieldType.EPOS_PAYMENT_OPTION.getName()).equalsIgnoreCase("CARD")))
						&& (paymentOption.isDebitCard())
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_UPI.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_COD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CRYPTO.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AAMARPAY.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_WL.getValue()))) {
					requestParameterJson.put("debitCard", true);
				} else {
					requestParameterJson.put("debitCard", false);
				}

				if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_CD.getValue()) && (paymentOption.isCashOnDelivery())
						&& ((all) || (fields.get(FieldType.EPOS_PAYMENT_OPTION.getName())
								.equalsIgnoreCase(PaymentType.COD.getCode())))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_UPI.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AAMARPAY.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CRYPTO.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_WL.getValue()))) {
					requestParameterJson.put("cod", true);
				} else {
					requestParameterJson.put("cod", false);
				}

				if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_CRYPTO.getValue()) && (paymentOption.isCrypto())
						&& ((all) || (fields.get(FieldType.EPOS_PAYMENT_OPTION.getName())
								.equalsIgnoreCase(PaymentType.CRYPTO.getCode())))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_UPI.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AAMARPAY.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_COD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_WL.getValue()))) {
					requestParameterJson.put("crypto", true);
				} else {
					requestParameterJson.put("crypto", false);
				}
				
				if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_AAMARPAY.getValue()) && (paymentOption.isAamarPay())
						&& ((all) || (fields.get(FieldType.EPOS_PAYMENT_OPTION.getName())
								.equalsIgnoreCase(PaymentType.AAMARPAY.getCode())))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_UPI.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_COD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_WL.getValue()))) {
					requestParameterJson.put("aamarPay", true);
				} else {
					requestParameterJson.put("aamarPay", false);
				}

				if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_EM_CC.getValue()) && (paymentOption.isEmi())
						&& ((all) || (fields.get(FieldType.EPOS_PAYMENT_OPTION.getName())
								.equalsIgnoreCase(PaymentType.EMI.getCode())))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_UPI.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_COD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CRYPTO.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AAMARPAY.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_WL.getValue()))) {
					requestParameterJson.put("emiCC", true);
					List<IssuerDetails> issuerList = issuerDetailsDao.getIssuerDetailsForTxn(user.getPayId(), "CC");
					List<String> emCCIssuerList = new ArrayList<String>();
					for (IssuerDetails issuer : issuerList) {
						if (!emCCIssuerList.contains(issuer.getIssuerName())) {
							emCCIssuerList.add(issuer.getIssuerName());
						}
					}
					requestParameterJson.put("emiCCIssuer", emCCIssuerList);

				} else {
					requestParameterJson.put("emiCC", false);
				}

				if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_EM_DC.getValue()) && (paymentOption.isEmi())
						&& ((all) || (fields.get(FieldType.EPOS_PAYMENT_OPTION.getName())
								.equalsIgnoreCase(PaymentType.EMI.getCode())))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_UPI.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_COD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AAMARPAY.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CRYPTO.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_WL.getValue()))) {
					requestParameterJson.put("emiDC", true);
					List<IssuerDetails> issuerList = issuerDetailsDao.getIssuerDetailsForTxn(user.getPayId(), "DC");
					List<String> emiDCIssuerList = new ArrayList<String>();
					for (IssuerDetails issuer : issuerList) {
						if (!emiDCIssuerList.contains(issuer.getIssuerName())) {
							emiDCIssuerList.add(issuer.getIssuerName());
						}
					}
					requestParameterJson.put("emiDCIssuer", emiDCIssuerList);
				} else {
					requestParameterJson.put("emiDC", false);
				}

				if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_NB.getValue())
						&& ((all) || (fields.get(FieldType.EPOS_PAYMENT_OPTION.getName())
								.equalsIgnoreCase(PaymentType.NET_BANKING.getCode())))
						&& (paymentOption.isNetBanking())
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_UPI.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_COD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CRYPTO.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AAMARPAY.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_WL.getValue()))) {
					requestParameterJson.put("netBanking", true);
				} else {
					requestParameterJson.put("netBanking", false);
				}

				if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_PC.getValue())
						&& ((all) || (fields.get(FieldType.EPOS_PAYMENT_OPTION.getName())
								.equalsIgnoreCase(PaymentType.PREPAID_CARD.getCode())))
						&& (paymentOption.isPrepaidCard())
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_UPI.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_COD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AAMARPAY.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CRYPTO.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_WL.getValue()))) {
					requestParameterJson.put("prepaidCard", true);
				} else {
					requestParameterJson.put("prepaidCard", false);
				}

				if (null != regionType && regionType.toString().contains(Constants.PAYMENT_TYPE_IN.getValue())
						&& ((all) || (fields.get(FieldType.EPOS_PAYMENT_OPTION.getName()).equalsIgnoreCase("CARD")))
						&& (paymentOption.isInternational())
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_UPI.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_COD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AAMARPAY.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CRYPTO.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_WL.getValue()))) {
					requestParameterJson.put("international", true);
				} else {
					requestParameterJson.put("international", false);
				}

				if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_DP.getValue())
						&& ((all) || (fields.get(FieldType.EPOS_PAYMENT_OPTION.getName())
								.equalsIgnoreCase(PaymentType.DEBIT_CARD_WITH_PIN.getCode())))
						&& (paymentOption.isDebitCardWithPin())
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_UPI.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AAMARPAY.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_COD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CRYPTO.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_WL.getValue()))) {
					requestParameterJson.put("debitWithPin", true);
				} else {
					requestParameterJson.put("debitWithPin", false);
				}

				if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_UP.getValue())
						&& ((all) || (fields.get(FieldType.EPOS_PAYMENT_OPTION.getName())
								.equalsIgnoreCase(PaymentType.UPI.getCode())))
						&& (paymentOption.isUpi())
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CARD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AD.getValue()))) {
					requestParameterJson.put("upi", true);
				} else {
					requestParameterJson.put("upi", false);
				}
				
				if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_MQR.getValue())
						&& ((all) || (fields.get(FieldType.EPOS_PAYMENT_OPTION.getName())
								.equalsIgnoreCase(PaymentType.MQR.getCode())))
						&& (paymentOption.isMqr())
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_MQR.getValue()))) {
					requestParameterJson.put("mqr", true);
				} else {
					requestParameterJson.put("mqr", false);
				}

			} else {
				requestParameterJson.put(Constants.ADS.getValue(), true);
				requestParameterJson.put(Constants.PAYMENT_ADSIMG_URL.getValue(),
						propertiesManager.propertiesMap.get(Constants.PAYMENT_ADSIMG_URL.getValue()));
				requestParameterJson.put(Constants.PAYMENT_ADSIMG_LINK_URL.getValue(),
						propertiesManager.propertiesMap.get(Constants.PAYMENT_ADSIMG_LINK_URL.getValue()));
				// Check TDR or Surcharge Mode
				if (StringUtils.isNotBlank(fields.get(FieldType.SURCHARGE_FLAG.getName()))
						&& fields.get(FieldType.SURCHARGE_FLAG.getName()).equalsIgnoreCase("Y")) {
					requestParameterJson.put("isSurcharge", true);
				} else {
					requestParameterJson.put("isSurcharge", false);
				}
				requestParameterJson.put("currencyCode",
						(String) request.getSession().getAttribute(FieldType.CURRENCY_CODE.getName()));
				if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_AD.getValue())
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_UPI.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CARD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_COD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_NB.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_COD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CRYPTO.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AAMARPAY.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_WL.getValue()))) {
					requestParameterJson.put("autoDebit", true);
				} else {
					requestParameterJson.put("autoDebit", false);
				}
				if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_WL.getValue()) && (paymentOption.isWallet())
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_UPI.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CARD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_COD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CRYPTO.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AAMARPAY.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_NB.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AD.getValue()))) {
					requestParameterJson.put("wallet", true);
				} else {
					requestParameterJson.put("wallet", false);
				}

				if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_CC.getValue()) && (paymentOption.isCreditCard())
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_UPI.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_COD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CRYPTO.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AAMARPAY.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_NB.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_WL.getValue()))) {
					requestParameterJson.put("creditCard", true);
				} else {
					requestParameterJson.put("creditCard", false);
				}

				if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_CD.getValue()) && (paymentOption.isCashOnDelivery())
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_UPI.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CRYPTO.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AAMARPAY.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_NB.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CARD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_WL.getValue()))) {
					requestParameterJson.put("cod", true);
				} else {
					requestParameterJson.put("cod", false);
				}

				if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_CRYPTO.getValue()) && (paymentOption.isCrypto())
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_UPI.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_NB.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AAMARPAY.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CARD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_COD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_WL.getValue()))) {
					requestParameterJson.put("crypto", true);
				} else {
					requestParameterJson.put("crypto", false);
				}

				if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_EM_CC.getValue()) && (paymentOption.isEmi())
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_UPI.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_COD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CRYPTO.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_NB.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AAMARPAY.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CARD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_WL.getValue()))) {
					requestParameterJson.put("emiCC", true);
					List<IssuerDetails> issuerList = issuerDetailsDao.getIssuerDetailsForTxn(user.getPayId(), "CC");
					List<String> emCCIssuerList = new ArrayList<String>();
					for (IssuerDetails issuer : issuerList) {
						if (!emCCIssuerList.contains(issuer.getIssuerName())) {
							emCCIssuerList.add(issuer.getIssuerName());
						}
					}
					requestParameterJson.put("emiCCIssuer", emCCIssuerList);

				} else {
					requestParameterJson.put("emiCC", false);
				}

				if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_EM_DC.getValue()) && (paymentOption.isEmi())
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_UPI.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_COD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CRYPTO.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_NB.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AAMARPAY.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CARD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_WL.getValue()))) {
					requestParameterJson.put("emiDC", true);
					List<IssuerDetails> issuerList = issuerDetailsDao.getIssuerDetailsForTxn(user.getPayId(), "DC");
					List<String> emiDCIssuerList = new ArrayList<String>();
					for (IssuerDetails issuer : issuerList) {
						if (!emiDCIssuerList.contains(issuer.getIssuerName())) {
							emiDCIssuerList.add(issuer.getIssuerName());
						}
					}
					requestParameterJson.put("emiDCIssuer", emiDCIssuerList);
				} else {
					requestParameterJson.put("emiDC", false);
				}

				if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_NB.getValue()) && (paymentOption.isNetBanking())
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_UPI.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_COD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CRYPTO.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AAMARPAY.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CARD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_WL.getValue()))) {
					requestParameterJson.put("netBanking", true);
				} else {
					requestParameterJson.put("netBanking", false);
				}

				if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_PC.getValue()) && (paymentOption.isPrepaidCard())
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_UPI.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_COD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_NB.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CRYPTO.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AAMARPAY.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CARD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_WL.getValue()))) {
					requestParameterJson.put("prepaidCard", true);
				} else {
					requestParameterJson.put("prepaidCard", false);
				}

				if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_DC.getValue()) && (paymentOption.isDebitCard())
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_UPI.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_COD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CRYPTO.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AAMARPAY.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_NB.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_WL.getValue()))) {
					requestParameterJson.put("debitCard", true);
				} else {
					requestParameterJson.put("debitCard", false);
				}

				// for International card
				if (null != regionType && regionType.toString().contains(Constants.PAYMENT_TYPE_IN.getValue())
						&& (paymentOption.isInternational())
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_UPI.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_COD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CRYPTO.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AAMARPAY.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_NB.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_WL.getValue()))) {
					requestParameterJson.put("international", true);
				} else {
					requestParameterJson.put("international", false);
				}

				if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_DP.getValue())
						&& (paymentOption.isDebitCardWithPin())
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_UPI.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CRYPTO.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AAMARPAY.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_COD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_NB.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_WL.getValue()))) {
					requestParameterJson.put("debitWithPin", true);
				} else {
					requestParameterJson.put("debitWithPin", false);
				}

				if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_UP.getValue()) && (paymentOption.isUpi())
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CARD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_NB.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AAMARPAY.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_WL.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_COD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CRYPTO.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AD.getValue()))) {
					requestParameterJson.put("upi", true);
				} else {
					requestParameterJson.put("upi", false);
				}
				
				if (requestParameterJson.has("mqr")
						&& StringUtils.isNotBlank(requestParameterJson.get("mqr").toString())
						&& requestParameterJson.get("mqr").toString().equalsIgnoreCase("true")) {
					requestParameterJson.put("mqr", true);
				} else if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_MQR.getValue())
						&& (paymentOption.isMqr())
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CARD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_NB.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_UPI.getValue()))

						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_WL.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_COD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CRYPTO.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AD.getValue()))) {
					requestParameterJson.put("mqr", true);
				} else {
					requestParameterJson.put("mqr", false);
				}

				if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_AAMARPAY.getValue()) && (paymentOption.isAamarPay())
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CARD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_NB.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_UPI.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_WL.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_COD.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_CRYPTO.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_CC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_EMI_DC.getValue()))
						&& (!merchantPaymentType.equals(Constants.MERCHANT_PAYMENT_AD.getValue()))) {
					requestParameterJson.put("aamarPay", true);
				} else {
					requestParameterJson.put("aamarPay", false);
				}
				
				if ((!request.getSession().getAttribute(Constants.TOKEN.getValue()).toString()
						.equals(Constants.NA.getValue())) && (paymentOption.isExpressPay())) {
					requestParameterJson.put("tokenAvailable", true);
				} else {
					requestParameterJson.put("tokenAvailable", false);
				}

				if ((!request.getSession().getAttribute(Constants.VPA_TOKEN.getValue()).toString()
						.equals(Constants.NA.getValue())) && (paymentOption.isUpi())) {
					requestParameterJson.put("vpaTokenAvailable", true);
				} else {
					requestParameterJson.put("vpaTokenAvailable", false);
				}

				if ((!request.getSession().getAttribute(Constants.NB_TOKEN.getValue()).toString()
						.equals(Constants.NA.getValue())) && (paymentOption.isNetBanking())) {
					requestParameterJson.put("nbTokenAvailable", true);
				} else {
					requestParameterJson.put("nbTokenAvailable", false);
				}

				if ((!request.getSession().getAttribute(Constants.WL_TOKEN.getValue()).toString()
						.equals(Constants.NA.getValue())) && (paymentOption.isWallet())) {
					requestParameterJson.put("wlTokenAvailable", true);
				} else {
					requestParameterJson.put("wlTokenAvailable", false);
				}

				/*
				 * String custEmailId = fields.get(FieldType.CUST_EMAIL.getName()); if
				 * (custEmailId != null && !String.valueOf(custEmailId).trim().equals("")) {
				 * requestParameterJson.put("emailId", true); } else {
				 * requestParameterJson.put("emailId", false); }
				 */

				if (StringUtils.isBlank(userSettings.getCardSaveParam())) {
					requestParameterJson.put("express_pay", false);
				} else if (StringUtils.isBlank(fields.get(userSettings.getCardSaveParam()))) {
					requestParameterJson.put("express_pay", false);
				} else {
					Object expressFlag = (Object) request.getSession()
							.getAttribute(Constants.EXPRESS_PAY_FLAG.getValue());
					if (null != expressFlag) {
						expressFlag = expressFlag.toString();
						if (expressFlag.equals(Constants.TRUE_STRING.getValue()) && (paymentOption.isExpressPay())) {
							requestParameterJson.put("express_pay", true);
						} else {
							requestParameterJson.put("express_pay", false);
						}
					} else {
						requestParameterJson.put("express_pay", false);
					}
				}

				if (StringUtils.isBlank(userSettings.getNbSaveParam())) {
					requestParameterJson.put("save_nb", false);
				} else if (StringUtils.isBlank(fields.get(userSettings.getNbSaveParam()))) {
					requestParameterJson.put("save_nb", false);
				} else {
					Object savenbFlag = (Object) request.getSession().getAttribute(Constants.SAVE_NB_FLAG.getValue());
					if (null != savenbFlag) {
						savenbFlag = savenbFlag.toString();
						if (savenbFlag.equals(Constants.TRUE_STRING.getValue())) {
							requestParameterJson.put("save_nb", true);
						} else {
							requestParameterJson.put("save_nb", false);
						}
					} else {
						requestParameterJson.put("save_nb", false);
					}
				}

				if (StringUtils.isBlank(userSettings.getWlSaveParam())) {
					requestParameterJson.put("save_wl", false);
				} else if (StringUtils.isBlank(fields.get(userSettings.getWlSaveParam()))) {
					requestParameterJson.put("save_wl", false);
				} else {
					Object savewlFlag = (Object) request.getSession().getAttribute(Constants.SAVE_WL_FLAG.getValue());
					if (null != savewlFlag) {
						savewlFlag = savewlFlag.toString();
						if (savewlFlag.equals(Constants.TRUE_STRING.getValue())) {
							requestParameterJson.put("save_wl", true);
						} else {
							requestParameterJson.put("save_wl", false);
						}
					} else {
						requestParameterJson.put("save_wl", false);
					}
				}
				if (StringUtils.isBlank(userSettings.getVpaSaveParam())) {
					requestParameterJson.put("save_vpa", false);
				} else if (StringUtils.isBlank(fields.get(userSettings.getVpaSaveParam()))) {
					requestParameterJson.put("save_vpa", false);
				} else {
					Object saveVpaFlag = (Object) request.getSession().getAttribute(Constants.SAVE_VPA_FLAG.getValue());
					if (null != saveVpaFlag) {
						saveVpaFlag = saveVpaFlag.toString();
						if (saveVpaFlag.equals(Constants.TRUE_STRING.getValue())) {
							requestParameterJson.put("save_vpa", true);
						} else {
							requestParameterJson.put("save_vpa", false);
						}
					} else {
						requestParameterJson.put("save_vpa", false);
					}
				}
			}
			Object autoDebitSurcharge = (Object) request.getSession().getAttribute(FieldType.AD_SURCHARGE.getName());
			if (autoDebitSurcharge != null) {
				requestParameterJson.put("surcharge_ad",
						request.getSession().getAttribute(FieldType.AD_SURCHARGE.getName()).toString());
			} else {
				requestParameterJson.put("surcharge_ad", false);
			}

			Object ccSurchargeSurcharge = (Object) request.getSession()
					.getAttribute(FieldType.CC_CONSUMER_SURCHARGE.getName());
			if (ccSurchargeSurcharge != null) {
				requestParameterJson.put("surcharge_cc_consumer",
						request.getSession().getAttribute(FieldType.CC_CONSUMER_SURCHARGE.getName()).toString());
			} else {
				requestParameterJson.put("surcharge_cc_consumer", false);
			}

			Object ccCommercialSurcharge = (Object) request.getSession()
					.getAttribute(FieldType.CC_COMMERCIAL_SURCHARGE.getName());
			if (ccCommercialSurcharge != null) {
				requestParameterJson.put("surcharge_cc_commercial",
						request.getSession().getAttribute(FieldType.CC_COMMERCIAL_SURCHARGE.getName()).toString());
			} else {
				requestParameterJson.put("surcharge_cc_commercial", false);
			}

			Object ccPremiumSurcharge = (Object) request.getSession()
					.getAttribute(FieldType.CC_PREMIUM_SURCHARGE.getName());
			if (ccPremiumSurcharge != null) {
				requestParameterJson.put("surcharge_cc_premium",
						request.getSession().getAttribute(FieldType.CC_PREMIUM_SURCHARGE.getName()).toString());
			} else {
				requestParameterJson.put("surcharge_cc_premium", false);
			}

			Object ccAmexSurcharge = (Object) request.getSession().getAttribute(FieldType.CC_AMEX_SURCHARGE.getName());
			if (ccAmexSurcharge != null) {
				requestParameterJson.put("surcharge_cc_amex",
						request.getSession().getAttribute(FieldType.CC_AMEX_SURCHARGE.getName()).toString());
			} else {
				requestParameterJson.put("surcharge_cc_amex", false);
			}

			Object cdSurcharge = (Object) request.getSession().getAttribute(FieldType.CD_SURCHARGE.getName());
			if (cdSurcharge != null) {
				requestParameterJson.put("surcharge_cd",
						request.getSession().getAttribute(FieldType.CD_SURCHARGE.getName()).toString());
			} else {
				requestParameterJson.put("surcharge_cd", false);
			}

			Object crSurcharge = (Object) request.getSession().getAttribute(FieldType.CR_SURCHARGE.getName());
			if (crSurcharge != null) {
				requestParameterJson.put("surcharge_cr",
						request.getSession().getAttribute(FieldType.CR_SURCHARGE.getName()).toString());
			} else {
				requestParameterJson.put("surcharge_cr", false);
			}

			Object dcVisaSurcharge = (Object) request.getSession().getAttribute(FieldType.DC_VISA_SURCHARGE.getName());
			if (dcVisaSurcharge != null) {
				requestParameterJson.put("surcharge_dc_visa",
						request.getSession().getAttribute(FieldType.DC_VISA_SURCHARGE.getName()).toString());
			} else {
				requestParameterJson.put("surcharge_dc_visa", false);
			}

			Object dcMasterSurcharge = (Object) request.getSession()
					.getAttribute(FieldType.DC_MASTERCARD_SURCHARGE.getName());
			if (dcMasterSurcharge != null) {
				requestParameterJson.put("surcharge_dc_mastercard",
						request.getSession().getAttribute(FieldType.DC_MASTERCARD_SURCHARGE.getName()).toString());
			} else {
				requestParameterJson.put("surcharge_dc_mastercard", false);
			}

			Object dcRupaySurcharge = (Object) request.getSession()
					.getAttribute(FieldType.DC_RUPAY_SURCHARGE.getName());
			if (dcRupaySurcharge != null) {
				requestParameterJson.put("surcharge_dc_rupay",
						request.getSession().getAttribute(FieldType.DC_RUPAY_SURCHARGE.getName()).toString());
			} else {
				requestParameterJson.put("surcharge_dc_rupay", false);
			}

			// changes by shivanand

			Object nbSurcharge = (Object) request.getSession().getAttribute(FieldType.NB_SURCHARGE.getName());
			if (nbSurcharge != null) {
				requestParameterJson.put("surcharge_nb",
						request.getSession().getAttribute(FieldType.NB_SURCHARGE.getName()).toString());
			} else {
				requestParameterJson.put("surcharge_nb", false);
			}
			// end

			Object pcSurcharge = (Object) request.getSession().getAttribute(FieldType.PC_SURCHARGE.getName());
			if (pcSurcharge != null) {
				requestParameterJson.put("surcharge_pc",
						request.getSession().getAttribute(FieldType.PC_SURCHARGE.getName()).toString());
			} else {
				requestParameterJson.put("surcharge_pc", false);
			}

			Object upSurcharge = (Object) request.getSession().getAttribute(FieldType.UP_SURCHARGE.getName());
			if (upSurcharge != null) {
				requestParameterJson.put("surcharge_up",
						request.getSession().getAttribute(FieldType.UP_SURCHARGE.getName()).toString());
			} else {
				requestParameterJson.put("surcharge_up", false);
			}
			
			
			Object mqrSurcharge = (Object) request.getSession().getAttribute(FieldType.MQR_SURCHARGE.getName());
			if (mqrSurcharge != null) {
				requestParameterJson.put("surcharge_mqr",
						request.getSession().getAttribute(FieldType.MQR_SURCHARGE.getName()).toString());
			} else {
				requestParameterJson.put("surcharge_mqr", false);
			}
			
			Object apSurcharge = (Object) request.getSession().getAttribute(FieldType.AP_SURCHARGE.getName());
			if (apSurcharge != null) {
				requestParameterJson.put("surcharge_ap",
						request.getSession().getAttribute(FieldType.AP_SURCHARGE.getName()).toString());
			} else {
				requestParameterJson.put("surcharge_ap", false);
			}

			Object wlSurcharge = (Object) request.getSession().getAttribute(FieldType.WL_SURCHARGE.getName());
			if (wlSurcharge != null) {
				requestParameterJson.put("surcharge_wl",
						request.getSession().getAttribute(FieldType.WL_SURCHARGE.getName()).toString());
			} else {
				requestParameterJson.put("surcharge_wl", false);
			}

			Object inSurcharge = (Object) request.getSession().getAttribute(FieldType.IN_SURCHARGE.getName());
			if (inSurcharge != null) {
				requestParameterJson.put("surcharge_in",
						request.getSession().getAttribute(FieldType.IN_SURCHARGE.getName()).toString());
			} else {
				requestParameterJson.put("surcharge_in", false);
			}

			Object emCCSurcharge = (Object) request.getSession().getAttribute(FieldType.EMI_CC_SURCHARGE.getName());
			if (emCCSurcharge != null) {
				requestParameterJson.put("surcharge_em_cc",
						request.getSession().getAttribute(FieldType.EMI_CC_SURCHARGE.getName()).toString());
			} else {
				requestParameterJson.put("surcharge_em_cc", false);
			}

			Object emDCSurcharge = (Object) request.getSession().getAttribute(FieldType.EMI_DC_SURCHARGE.getName());
			if (emDCSurcharge != null) {
				requestParameterJson.put("surcharge_em_dc",
						request.getSession().getAttribute(FieldType.EMI_DC_SURCHARGE.getName()).toString());
			} else {
				requestParameterJson.put("surcharge_em_dc", false);
			}

			List<String> ccMopType = new ArrayList<String>();
			Object ccMop = paymentTypeTransactionProvider.getSupportedPaymentTypeMap().get("CC");

			if (ccMop != null) {
				if (ccMop.toString().contains(Constants.RUPAY.getValue()) && paymentOption.getMopTypeString()
						.contains(PaymentType.CREDIT_CARD.getName() + "-" + Constants.MOP_RUPAY.getValue())) {
					ccMopType.add(Constants.MOP_RUPAY.getValue());
				}
				if (ccMop.toString().contains(Constants.VISA.getValue()) && paymentOption.getMopTypeString()
						.contains(PaymentType.CREDIT_CARD.getName() + "-" + Constants.MOP_VISA.getValue())) {
//					if (!ccMopType.toString().isEmpty()) {
//						ccMopType.append(Constants.MOP_COMMA.getValue());
//					}
					ccMopType.add(Constants.MOP_VISA.getValue());
				}
				if (ccMop.toString().contains(Constants.MASTERCARD.getValue()) && paymentOption.getMopTypeString()
						.contains(PaymentType.CREDIT_CARD.getName() + "-" + Constants.MOP_MASTERCARD.getValue())) {
//					if (!ccMopType.toString().isEmpty()) {
//						ccMopType.append(Constants.MOP_COMMA.getValue());
//					}
					ccMopType.add(Constants.MOP_MASTERCARD.getValue());
				}
				if (ccMop.toString().contains(Constants.AMEX.getValue()) && paymentOption.getMopTypeString()
						.contains(PaymentType.CREDIT_CARD.getName() + "-" + Constants.MOP_AMEX.getValue())) {
//					if (!ccMopType.toString().isEmpty()) {
//						ccMopType.append(Constants.MOP_COMMA.getValue());
//					}
					ccMopType.add(Constants.MOP_AMEX.getValue());
				}
				if (ccMop.toString().contains(Constants.DINERS.getValue()) && paymentOption.getMopTypeString()
						.contains(PaymentType.CREDIT_CARD.getName() + "-" + Constants.MOP_DINNER.getValue())) {
//					if (!ccMopType.toString().isEmpty()) {
//						ccMopType.append(Constants.MOP_COMMA.getValue());
//					}
					ccMopType.add(Constants.MOP_DINNER.getValue());
				}
			} else {
				requestParameterJson.put("ccMopTypes", false);

			}

			requestParameterJson.put("ccMopTypes", ccMopType);

//			StringBuilder pcMopType = new StringBuilder();
			List<String> pcMopType = new ArrayList<String>();
			Object pcMop = paymentTypeTransactionProvider.getSupportedPaymentTypeMap().get("PC");
			if (pcMop != null) {
				if (pcMop.toString().contains(Constants.RUPAY.getValue()) && paymentOption.getMopTypeString()
						.contains(PaymentType.PREPAID_CARD.getName() + "-" + Constants.MOP_RUPAY.getValue())) {
					pcMopType.add(Constants.MOP_RUPAY.getValue());
				}
				if (pcMop.toString().contains(Constants.VISA.getValue()) && paymentOption.getMopTypeString()
						.contains(PaymentType.PREPAID_CARD.getName() + "-" + Constants.MOP_VISA.getValue())) {
//					if (!pcMopType.toString().isEmpty()) {
//						pcMopType.append(Constants.MOP_COMMA.getValue());
//					}
					pcMopType.add(Constants.MOP_VISA.getValue());
				}
				if (pcMop.toString().contains(Constants.MASTERCARD.getValue()) && paymentOption.getMopTypeString()
						.contains(PaymentType.PREPAID_CARD.getName() + "-" + Constants.MOP_MASTERCARD.getValue())) {
//					if (!pcMopType.toString().isEmpty()) {
//						pcMopType.append(Constants.MOP_COMMA.getValue());
//					}
					pcMopType.add(Constants.MOP_MASTERCARD.getValue());
				}
				if (pcMop.toString().contains(Constants.AMEX.getValue()) && paymentOption.getMopTypeString()
						.contains(PaymentType.PREPAID_CARD.getName() + "-" + Constants.MOP_AMEX.getValue())) {
//					if (!pcMopType.toString().isEmpty()) {
//						pcMopType.append(Constants.MOP_COMMA.getValue());
//					}
					pcMopType.add(Constants.MOP_AMEX.getValue());
				}
				if (pcMop.toString().contains(Constants.DINERS.getValue()) && paymentOption.getMopTypeString()
						.contains(PaymentType.PREPAID_CARD.getName() + "-" + Constants.MOP_DINNER.getValue())) {
//					if (!pcMopType.toString().isEmpty()) {
//						pcMopType.append(Constants.MOP_COMMA.getValue());
//					}
					pcMopType.add(Constants.MOP_DINNER.getValue());
				}
			} else {
				requestParameterJson.put("pcMopTypes", false);

			}
			requestParameterJson.put("pcMopTypes", pcMopType);

			// StringBuilder dcMopType = new StringBuilder();
			List<String> dcMopType = new ArrayList<String>();
			Object dcMop = paymentTypeTransactionProvider.getSupportedPaymentTypeMap().get("DC");

			if (dcMop != null) {
				if (dcMop.toString().contains(Constants.RUPAY.getValue()) && paymentOption.getMopTypeString()
						.contains(PaymentType.DEBIT_CARD.getName() + "-" + Constants.MOP_RUPAY.getValue())) {
					dcMopType.add(Constants.MOP_RUPAY.getValue());
				}
				if (dcMop.toString().contains(Constants.VISA.getValue()) && paymentOption.getMopTypeString()
						.contains(PaymentType.DEBIT_CARD.getName() + "-" + Constants.MOP_VISA.getValue())) {
//					if (!dcMopType.toString().isEmpty()) {
//						dcMopType.add(Constants.MOP_COMMA.getValue());
//					}
					dcMopType.add(Constants.MOP_VISA.getValue());
				}
				if (dcMop.toString().contains(Constants.MASTERCARD.getValue()) && paymentOption.getMopTypeString()
						.contains(PaymentType.DEBIT_CARD.getName() + "-" + Constants.MOP_MASTERCARD.getValue())) {
//					if (!dcMopType.toString().isEmpty()) {
//						dcMopType.add(Constants.MOP_COMMA.getValue());
//					}
					dcMopType.add(Constants.MOP_MASTERCARD.getValue());
				}
				if (dcMop.toString().contains(Constants.MAESTRO.getValue()) && paymentOption.getMopTypeString()
						.contains(PaymentType.DEBIT_CARD.getName() + "-" + Constants.MOP_MAESTRO.getValue())) {
//					if (!dcMopType.toString().isEmpty()) {
//						dcMopType.add(Constants.MOP_COMMA.getValue());
//					}
					dcMopType.add(Constants.MOP_MAESTRO.getValue());
				}
			} else {
				requestParameterJson.put("dcMopTypes", false);
			}
			requestParameterJson.put("dcMopTypes", dcMopType);

			List<String> nbMopTypeList = new ArrayList<String>();
			Object nbMop = paymentTypeTransactionProvider.getSupportedPaymentTypeMap().get("NB");
			if (nbMop != null) {

				try {
					String mopListArray[] = nbMop.toString().split(",");
					Object mopTypeListAll[] = Class.forName("com.paymentgateway.commons.util.MopType")
							.getEnumConstants();

					for (String mopName : mopListArray) {

						mopName = mopName.replace("[", "");
						mopName = mopName.replace("]", "");
						mopName = mopName.replace(" ", "");
						for (Object mopTypeObject : mopTypeListAll) {

							if (mopName.equalsIgnoreCase(String.valueOf(mopTypeObject))) {
								MopType mopType = (MopType) mopTypeObject;
								nbMopTypeList.add(mopType.getName());
							}

						}
					}
				}

				catch (Exception e) {
					logger.error("Error while fetching mop List for Net Banking", e);
				}

			}

			else {
				requestParameterJson.put("nbMopType", false);
			}
			String moptype = paymentOption.getMopTypeString();
			List<String> moptypenb = new ArrayList<String>();
			List<String> moptypewl = new ArrayList<String>();
			String[] temp = moptype.split(",", moptype.length());
			for (String mopString : temp) {
				String[] temp1 = mopString.split("-");
				if (temp1[0].equalsIgnoreCase("Net Banking")) {
					moptypenb.add(MopType.getmopName(temp1[1]));
				}
				if (temp1[0].equalsIgnoreCase("Wallet")) {
					moptypewl.add(MopType.getmop(temp1[1]).toString());
				}
			}

			List<String> bankNameList = nbMopTypeList.stream().sorted(Comparator.comparing(n -> n.toString()))
					.collect(Collectors.toList());
			requestParameterJson.put("nbMopType", bankNameList);
			// end

			requestParameterJson.put("merchantType", user.getBusinessName());

			if (userSettings.isIframePaymentFlag()) {
				requestParameterJson.put("iframeOpt", true);
			} else {
				requestParameterJson.put("iframeOpt", false);
			}

			if (userSettings.isCheckOutJsFlag()) {
				requestParameterJson.put("checkOutJsFlag", true);
			} else {
				requestParameterJson.put("checkOutJsFlag", false);
			}

			Object upMop = paymentTypeTransactionProvider.getSupportedPaymentTypeMap().get("UP");
			if (upMop != null) {
				if (upMop.toString().contains(Constants.MOP_GOOGLEPAY_PARAMETER.getValue())
						&& paymentOption.getMopTypeString()
								.contains(PaymentType.UPI.getName() + "-" + Constants.MOP_GOOGLEPAY.getValue())) {
					requestParameterJson.put("googlePay", true);
				} else {
					requestParameterJson.put("googlePay", false);
				}

				if (fields.contains(FieldType.EPOS_PAYMENT_OPTION.getName())
						&& StringUtils.isNotBlank(fields.get(FieldType.EPOS_PAYMENT_OPTION.getName()))) {
					if ((paymentOption.isUpiQr())
							&& ((all)
									|| (fields.get(FieldType.EPOS_PAYMENT_OPTION.getName()).equalsIgnoreCase("UPI_QR")))
							&& (upMop.toString().contains(Constants.MOP_UPI_QR_PARAMETER.getValue()))) {
						requestParameterJson.put("upiQr", true);
					} else {
						requestParameterJson.put("upiQr", false);
					}
				} else {
					if ((paymentOption.isUpiQr())
							&& (upMop.toString().contains(Constants.MOP_UPI_QR_PARAMETER.getValue()))) {
						requestParameterJson.put("upiQr", true);
					} else {
						requestParameterJson.put("upiQr", false);
					}
				}
			} else {
				requestParameterJson.put("googlePay", false);
				requestParameterJson.put("upiQr", false);
			}

			List<String> wlMopTypeList = new ArrayList<String>();
			Object wlMop = paymentTypeTransactionProvider.getSupportedPaymentTypeMap().get("WL");
			if (wlMop != null) {

				try {
					String mopListArray[] = wlMop.toString().split(",");
					Object mopTypeListAll[] = Class.forName("com.paymentgateway.commons.util.MopType")
							.getEnumConstants();

					for (String mopName : mopListArray) {

						mopName = mopName.replace("[", "");
						mopName = mopName.replace("]", "");
						mopName = mopName.replace(" ", "");
						for (Object mopTypeObject : mopTypeListAll) {

							if (mopName.equalsIgnoreCase(String.valueOf(mopTypeObject))) {
								MopType mopType = (MopType) mopTypeObject;
								if (moptypewl.contains(mopType.toString())) {
									wlMopTypeList.add(mopType.getName());
								}
							}

						}
					}
				}

				catch (Exception e) {
					logger.error("Error while fetching mop List for Net Banking", e);
				}
				requestParameterJson.put("wlMopType", wlMopTypeList);
			}

			else {
				requestParameterJson.put("wlMopType", false);
			}

			List<String> emCCMopTypeList = new ArrayList<String>();
			Object emCCMop = paymentTypeTransactionProvider.getSupportedPaymentTypeMap().get("EMCC");
			if (emCCMop != null) {

				try {
					String mopListArray[] = emCCMop.toString().split(",");
					Object mopTypeListAll[] = Class.forName("com.paymentgateway.commons.util.MopType")
							.getEnumConstants();

					for (String mopName : mopListArray) {

						mopName = mopName.replace("[", "");
						mopName = mopName.replace("]", "");
						mopName = mopName.replace(" ", "");
						for (Object mopTypeObject : mopTypeListAll) {

							if (mopName.equalsIgnoreCase(String.valueOf(mopTypeObject))) {
								MopType mopType = (MopType) mopTypeObject;
								emCCMopTypeList.add(mopType.getCode());
							}

						}
					}
				}

				catch (Exception e) {
					logger.error("Error while fetching mop List for EMI CC", e);
				}
			}

			else {
				requestParameterJson.put("emCCMopType", false);
			}
			requestParameterJson.put("emCCMopType", emCCMopTypeList);

			List<String> emDCMopTypeList = new ArrayList<String>();
			Object emDCMop = paymentTypeTransactionProvider.getSupportedPaymentTypeMap().get("EMDC");
			if (emDCMop != null) {

				try {
					String mopListArray[] = emDCMop.toString().split(",");
					Object mopTypeListAll[] = Class.forName("com.paymentgateway.commons.util.MopType")
							.getEnumConstants();

					for (String mopName : mopListArray) {

						mopName = mopName.replace("[", "");
						mopName = mopName.replace("]", "");
						mopName = mopName.replace(" ", "");
						for (Object mopTypeObject : mopTypeListAll) {

							if (mopName.equalsIgnoreCase(String.valueOf(mopTypeObject))) {
								MopType mopType = (MopType) mopTypeObject;
								emDCMopTypeList.add(mopType.getCode());
							}

						}
					}
				}

				catch (Exception e) {
					logger.error("Error while fetching mop List for EMI DC", e);
				}
			}

			else {
				requestParameterJson.put("emDCMopType", false);
			}
			requestParameterJson.put("emDCMopType", emDCMopTypeList);
			// String logoUrl =
			// PropertiesManager.propertiesMap.get(Constants.LOGO_FILE_UPLOAD_LOCATION.getValue())
			// + "//" + user.getPayId() + "//" + user.getPayId() + ".png";

			String encodedLogoImage = getBase64LogoPerMerchant(user,userSettings);

			requestParameterJson.put("defaultLangauge",
					LocaleLanguageType.getLocaleLangauge(user.getDefaultLanguage()));
			requestParameterJson.put("merchantLogoFlag", userSettings.isLogoFlag());
			requestParameterJson.put("merchantLogoName", user.getBusinessName());
			requestParameterJson.put("encodedLogoImage", encodedLogoImage);
			requestParameterJson.put("codName", userSettings.getCodName());

			requestParameterJson.put("paymentSlab", userSettings.getPaymentMessageSlab());

			log("Final supported parameter request >>>" + requestParameterJson.toString());
		}
		return requestParameterJson;
	}

	public void log(String message) {
		message = Pattern.compile("(encodedLogoImage\":\")([\\s\\S]*?)(\",\")").matcher(message).replaceAll("$1$3");
		logger.info(message);
	}

	public Map<String, Object> getSupportedPaymentTypeMap() {
		return supportedPaymentTypeMap;
	}

	public void setSupportedPaymentTypeMap(Map<String, Object> supportedPaymentTypeMap) {
		this.supportedPaymentTypeMap = supportedPaymentTypeMap;
	}

	public String getBase64LogoPerMerchant(User user, UserSettingData userSettings) {
		String base64File = "";
		File file = null;
		if (userSettings.isAllowLogoInPgPage()) {

			if (userSettings.isLogoFlag()) {
				file = new File(PropertiesManager.propertiesMap.get(Constants.LOGO_FILE_UPLOAD_LOCATION.getValue())
						+ "//" + user.getPayId(), user.getPayId() + ".png");
			} else {
				if (StringUtils.isNotEmpty(user.getSuperMerchantId())) {
					User superMerchant = userDao.findPayId(user.getSuperMerchantId());
					UserSettingData superMerchantSettings=userSettingDao.fetchDataUsingPayId(superMerchant.getPayId());

					if (superMerchant != null && superMerchantSettings.isLogoFlag()) {
						file = new File(
								PropertiesManager.propertiesMap.get(Constants.LOGO_FILE_UPLOAD_LOCATION.getValue())
										+ "//" + superMerchant.getPayId(),
								superMerchant.getPayId() + ".png");
					} else {
						return "";
					}
				} else {
					return "";
				}
			}
		} else {
			return "";
		}
		try (FileInputStream imageInFile = new FileInputStream(file)) {
			byte fileData[] = new byte[(int) file.length()];
			imageInFile.read(fileData);
			base64File = Base64.getEncoder().encodeToString(fileData);
		} catch (FileNotFoundException e) {
			logger.error("Exception caught while encoding into Base64, " + e);
			return "";
		} catch (IOException e) {
			logger.error("Exception caught while encoding into Base64, " + e);
			return "";
		} catch (Exception e) {
			logger.error("Exception caught while encoding into Base64, " + e);
			return "";
		}
		return base64File;
	}
}
