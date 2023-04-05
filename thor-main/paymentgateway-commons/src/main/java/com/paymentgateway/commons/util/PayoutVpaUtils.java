package com.paymentgateway.commons.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.PayoutAcquirerMappingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.PayoutAcquireMapping;
import com.paymentgateway.commons.user.PayoutAcquirer;
import com.paymentgateway.commons.user.PayoutVpa;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;

@Service
public class PayoutVpaUtils implements Serializable {

	private static final long serialVersionUID = 7063450254641111082L;

	private static Logger logger = LoggerFactory.getLogger(PayoutVpaUtils.class.getName());

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private UserDao userDao;

	@Autowired
	private PayoutAcquirerMappingDao payoutAcquirerMappingDao;

	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	private static final String prefix = "MONGO_DB_";

	public PayoutVpa createVpa(PayoutAcquireMapping acqMapping) {

		PayoutVpa payoutVpa = new PayoutVpa();
		try {

			payoutVpa.setVanBeneficiaryName("PAYMENT GATEWAY SOLUTION PVT LTD");
			payoutVpa.setBankName(acqMapping.getBankName());
			payoutVpa.setPayId(acqMapping.getPayId());
			payoutVpa.setSubMerchantPayId(acqMapping.getSubMerchantPayId());
			payoutVpa.setMerchantName(acqMapping.getMerchantName());

			switch (PayoutAcquirer.getInstanceFromCode(acqMapping.getBankName())) {

			case ICICI:

				long randomNumer = getRandomNumber();

				String randomNumberString = "9" + randomNumer;
				randomNumberString = randomNumberString.substring(0, 8);
				payoutVpa.setVan("LTZ" + randomNumberString);
				payoutVpa.setVpa("PaymentGateway." + randomNumberString + "@icici");
				payoutVpa.setVanIfsc("ICIC0000104");
				payoutVpa.setStatus(ErrorType.SUCCESS.getResponseMessage());
				payoutVpa.setResponseMsg(ErrorType.SUCCESS.getResponseMessage());
				payoutAcquirerMappingDao.addVirtualAccountDetails(payoutVpa);
				break;
			case PAYTM:

				try {
					
					String url = PropertiesManager.propertiesMap.get("TransactionWSPayoutPaytmCreateWalletURL");

					Map<String, String> requestMap = new HashMap<String, String>();
					Map<String, String> responseMap = new HashMap<String, String>();
					
					if(StringUtils.isNotBlank(acqMapping.getSubMerchantName())){
						requestMap.put(FieldType.WALLET_NAME.getName(), acqMapping.getMerchantName()+"_"+acqMapping.getSubMerchantName());
					}else{
						requestMap.put(FieldType.WALLET_NAME.getName(), acqMapping.getMerchantName());
					}
					
					requestMap.put(FieldType.PURPOSE.getName(), "Payout");
					requestMap.put(FieldType.PAY_ID.getName(), acqMapping.getPayId());

					requestMap.put(FieldType.HASH.getName(), Hasher.getHash(new Fields(requestMap)));

					responseMap = transactionControllerServiceProvider.communicatePaytmPayoutApi(requestMap, url);

					payoutVpa.setStatus(responseMap.get(FieldType.STATUS.getName()));
					payoutVpa.setResponseMsg(responseMap.get(FieldType.PG_RESPONSE_MSG.getName()));

					if (StringUtils.isNotBlank(payoutVpa.getStatus()) && (payoutVpa.getStatus()
							.equalsIgnoreCase(ErrorType.SUCCESS.getResponseMessage())
							|| payoutVpa.getStatus().equalsIgnoreCase(ErrorType.DUPLICATE.getResponseMessage()))) {
						payoutVpa.setVan(responseMap.get(FieldType.VIRTUAL_ACC_NUM.getName()));
						payoutVpa.setVanIfsc(responseMap.get(FieldType.IFSC_CODE.getName()));
						payoutVpa.setStatus(responseMap.get(FieldType.STATUS.getName()));
						payoutVpa.setResponseMsg(responseMap.get(FieldType.PG_RESPONSE_MSG.getName()));
						payoutVpa.setSubWalletId(responseMap.get(FieldType.SUB_WALLET_ID.getName()));
						payoutVpa.setWalletName(responseMap.get(FieldType.WALLET_NAME.getName()));
						payoutAcquirerMappingDao.addVirtualAccountDetails(payoutVpa);
					} else {
						logger.info("Payout Response Failed " + payoutVpa.getStatus() + " Msg "
								+ payoutVpa.getResponseMsg());
					}

				} catch (Exception e) {
					logger.info("exception in paytm create wallet ", e);
				}

				break;
				
			case CASHFREE:
				//Cashfree dont have virtual account creation for payout. so we taking random VA for reportings
				String randomNumberStringCf = "3" + getRandomNumberCashfree();
				randomNumberString = randomNumberStringCf.substring(0, 8);
				payoutVpa.setVan("CF" + randomNumberString);
				payoutVpa.setVanIfsc("CF000000021");
				payoutVpa.setStatus(ErrorType.SUCCESS.getResponseMessage());
				payoutVpa.setResponseMsg(ErrorType.SUCCESS.getResponseMessage());
				payoutAcquirerMappingDao.addVirtualAccountDetails(payoutVpa);
				break;
				
			case APEXPAY:
				//APEXPAY dont have virtual account creation for payout. so we taking random VA for reportings
				String randomNumberStringAP = "1" + getRandomNumberApexPay();
				randomNumberString = randomNumberStringAP.substring(0, 8);
				payoutVpa.setVan("AP" + randomNumberString);
				payoutVpa.setVanIfsc("AP000000021");
				payoutVpa.setStatus(ErrorType.SUCCESS.getResponseMessage());
				payoutVpa.setResponseMsg(ErrorType.SUCCESS.getResponseMessage());
				payoutAcquirerMappingDao.addVirtualAccountDetails(payoutVpa);
				
				break;
			case FONEPAISA:
				//PHONEPAISA dont have virtual account creation for payout. so we taking random VA for reportings
				String randomNumberStringPP = "1" + getRandomNumberPhonePaisa();
				randomNumberString = randomNumberStringPP.substring(0, 8);
				payoutVpa.setVan("FP" + randomNumberString);
				payoutVpa.setVanIfsc("FP000000021");
				payoutVpa.setStatus(ErrorType.SUCCESS.getResponseMessage());
				payoutVpa.setResponseMsg(ErrorType.SUCCESS.getResponseMessage());
				payoutAcquirerMappingDao.addVirtualAccountDetails(payoutVpa);
				break;
				
			case FLOXYPAY:
				//FLOXYPAY dont have virtual account creation for payout. so we taking random VA for reportings
				String randomNumberStringFL = "1" + getRandomNumberPhonePaisa();
				randomNumberString = randomNumberStringFL.substring(0, 8);
				payoutVpa.setVan("FL" + randomNumberString);
				payoutVpa.setVanIfsc("FL000000021");
				payoutVpa.setStatus(ErrorType.SUCCESS.getResponseMessage());
				payoutVpa.setResponseMsg(ErrorType.SUCCESS.getResponseMessage());
				payoutAcquirerMappingDao.addVirtualAccountDetails(payoutVpa);
				break;
				
			default:
				String randomNumberStringDef = "1" + getRandomNumberPhonePaisa();
				randomNumberString = randomNumberStringDef.substring(0, 8);
				payoutVpa.setVan("DEF" + randomNumberString);
				payoutVpa.setVanIfsc("DEF00000021");
				payoutVpa.setStatus(ErrorType.SUCCESS.getResponseMessage());
				payoutVpa.setResponseMsg(ErrorType.SUCCESS.getResponseMessage());
				payoutAcquirerMappingDao.addVirtualAccountDetails(payoutVpa);
				
				break;
			}

		} catch (Exception e) {
			logger.info("exception in createWallet ", e);
		}

		return payoutVpa;

	}

	public boolean isVpaSaved(PayoutAcquireMapping acqMapping) {
		try {

			BasicDBObject fetchQuery = new BasicDBObject();

			if (StringUtils.isNotBlank(acqMapping.getPayId())) {
				fetchQuery.put(FieldType.PAY_ID.getName(), acqMapping.getPayId());
			}

			if (StringUtils.isNotBlank(acqMapping.getSubMerchantPayId())) {
				fetchQuery.put(FieldType.SUB_MERCHANT_ID.getName(), acqMapping.getSubMerchantPayId());
			}

			if (StringUtils.isNotBlank(acqMapping.getBankName())) {
				fetchQuery.put(FieldType.ACQUIRER_NAME.getName(), acqMapping.getBankName());
			}

			// fetchQuery.put(FieldType.STATUS.getName(), "ACTIVE");

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.PAYOUT_VIRTUAL_ACCOUNT_DETAILS.getValue()));
			MongoCursor<Document> cursor = collection.find(fetchQuery).iterator();

			if (cursor.hasNext()) {
				return true;
			}
		} catch (Exception e) {
			logger.info("exception in isVpaSaved() ", e);
		}

		return false;

	}

	public PayoutVpa fetchVpaDetail(PayoutAcquireMapping acqMapping) {

		PayoutVpa payoutVpa = new PayoutVpa();
		try {

			BasicDBObject fetchQuery = new BasicDBObject();

			if (StringUtils.isNotBlank(acqMapping.getPayId())) {
				fetchQuery.put(FieldType.PAY_ID.getName(), acqMapping.getPayId());
			}
			if (StringUtils.isNotBlank(acqMapping.getSubMerchantPayId())) {
				fetchQuery.put(FieldType.SUB_MERCHANT_ID.getName(), acqMapping.getSubMerchantPayId());
			}

			if (StringUtils.isNotBlank(acqMapping.getSubMerchantPayId())) {
				fetchQuery.put(FieldType.SUB_MERCHANT_ID.getName(), acqMapping.getSubMerchantPayId());
			}

			if (StringUtils.isNotBlank(acqMapping.getBankName())) {
				fetchQuery.put(FieldType.ACQUIRER_NAME.getName(), acqMapping.getBankName());
			}

			// fetchQuery.put(FieldType.STATUS.getName(), "ACTIVE");

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.PAYOUT_VIRTUAL_ACCOUNT_DETAILS.getValue()));
			MongoCursor<Document> cursor = collection.find(fetchQuery).iterator();

			if (cursor.hasNext()) {
				Document doc = cursor.next();
				payoutVpa.setStatus(doc.getString(FieldType.STATUS.getName()));
				payoutVpa.setVanBeneficiaryName(doc.getString(FieldType.VIRTUAL_BENEFICIARY_NAME.getName()));
				payoutVpa.setVan(doc.getString(FieldType.VIRTUAL_ACC_NUM.getName()));
				payoutVpa.setVanIfsc(doc.getString(FieldType.IFSC_CODE.getName()));
				payoutVpa.setVpa(doc.getString(FieldType.VPA.getName()));
				payoutVpa.setSubWalletId(doc.getString(FieldType.SUB_WALLET_ID.getName()));
				payoutVpa.setStatus(doc.getString(FieldType.STATUS.getName()));
				payoutVpa.setResponseMsg(doc.getString(FieldType.PG_RESPONSE_MSG.getName()));
			}
		} catch (Exception e) {
			logger.info("exception in isVpaSaved() ", e);
		}

		return payoutVpa;
	}

//	public List<PayoutVpa> fetchAllVirtualAcountDetails(PayoutAcquireMapping acqMapping) {
//
//		return null;
//
//	}

	public Long getRandomNumber() {

		boolean isDuplicateVirtualAccountNo = false;

		Long randomNum;

		do {
			logger.info("generating random number");
			randomNum = (long) Math.round(Math.round(Math.random() * (99999999 - 10000000) + 10000000));
			isDuplicateVirtualAccountNo = userDao.checkDuplicateVirtualAccountNo("LTZ" + randomNum);
			logger.info(
					"virtual Account is " + "LETZ" + randomNum + " found duplicate? " + isDuplicateVirtualAccountNo);
		} while (isDuplicateVirtualAccountNo);

		return randomNum;
	}
	
	public Long getRandomNumberCashfree() {

		boolean isDuplicateVirtualAccountNo = false;

		Long randomNum;

		do {
			logger.info("generating random number");
			randomNum = (long) Math.round(Math.round(Math.random() * (99999999 - 10000000) + 10000000));
			isDuplicateVirtualAccountNo = userDao.checkDuplicateVirtualAccountNo("CF" + randomNum);
			logger.info(
					"virtual Account is " + "CF" + randomNum + " found duplicate? " + isDuplicateVirtualAccountNo);
		} while (isDuplicateVirtualAccountNo);

		return randomNum;
	}
	
	public Long getRandomNumberApexPay() {

		boolean isDuplicateVirtualAccountNo = false;

		Long randomNum;

		do {
			logger.info("generating random number");
			randomNum = (long) Math.round(Math.round(Math.random() * (99999999 - 10000000) + 10000000));
			isDuplicateVirtualAccountNo = userDao.checkDuplicateVirtualAccountNo("AP" + randomNum);
			logger.info(
					"virtual Account is " + "AP" + randomNum + " found duplicate? " + isDuplicateVirtualAccountNo);
		} while (isDuplicateVirtualAccountNo);

		return randomNum;
	}
	
	public Long getRandomNumberPhonePaisa() {

		boolean isDuplicateVirtualAccountNo = false;

		Long randomNum;

		do {
			logger.info("generating random number");
			randomNum = (long) Math.round(Math.round(Math.random() * (99999999 - 10000000) + 10000000));
			isDuplicateVirtualAccountNo = userDao.checkDuplicateVirtualAccountNo("FP" + randomNum);
			logger.info(
					"virtual Account is " + "FP" + randomNum + " found duplicate? " + isDuplicateVirtualAccountNo);
		} while (isDuplicateVirtualAccountNo);

		return randomNum;
	}
	
	public Long getRandomNumberFloxyPay() {

		boolean isDuplicateVirtualAccountNo = false;

		Long randomNum;

		do {
			logger.info("generating random number");
			randomNum = (long) Math.round(Math.round(Math.random() * (99999999 - 10000000) + 10000000));
			isDuplicateVirtualAccountNo = userDao.checkDuplicateVirtualAccountNo("FL" + randomNum);
			logger.info(
					"virtual Account is " + "FL" + randomNum + " found duplicate? " + isDuplicateVirtualAccountNo);
		} while (isDuplicateVirtualAccountNo);

		return randomNum;
	}

}
