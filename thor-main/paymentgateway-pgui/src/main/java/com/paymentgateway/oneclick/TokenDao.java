package com.paymentgateway.oneclick;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.dao.HibernateAbstractDao;
import com.paymentgateway.commons.kms.EncryptDecryptService;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.NBToken;
import com.paymentgateway.commons.user.Token;
import com.paymentgateway.commons.user.VpaToken;
import com.paymentgateway.commons.user.WLToken;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TokenStatus;

/**
 * @author Sunil,Rahul
 *
 */
@Service
public class TokenDao extends HibernateAbstractDao {

	@Autowired
	MongoInstance mongoInstance;

	@Autowired
	PropertiesManager propertiesManager;
	
	@Autowired
	EncryptDecryptService encryptDecryptService;

	private static final String prefix = "MONGO_DB_";

	private static Logger logger = LoggerFactory.getLogger(TokenDao.class.getName());

	public void create(BasicDBObject token) {
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.TOKEN_COLLECTION_NAME.getValue()));
			Document doc = new Document(token);
			coll.insertOne(doc);
		} catch (MongoException ex) {
			logger.error("Exception while insert save card in DB" + ex);
		}
	}
	
	public void createNBToken(BasicDBObject token) {
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.NB_TOKEN_COLLECTION_NAME.getValue()));
			Document doc = new Document(token);
			coll.insertOne(doc);
		} catch (MongoException ex) {
			logger.error("Exception while insert Net Banking token in DB" + ex);
		}
	}
	
	public void createWLToken(BasicDBObject token) {
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.WL_TOKEN_COLLECTION_NAME.getValue()));
			Document doc = new Document(token);
			coll.insertOne(doc);
		} catch (MongoException ex) {
			logger.error("Exception while insert Wallet token in DB" + ex);
		}
	}

	public void createVpa(BasicDBObject token) {
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.SAVED_VPA_COLLECTION_NAME.getValue()));
			Document doc = new Document(token);
			coll.insertOne(doc);
		} catch (MongoException ex) {
			logger.error("Exception while insert save VPA in DB" + ex);
		}
	}

	public void delete(Token token) {
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.TOKEN_COLLECTION_NAME.getValue()));
			List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();

			finalList.add(new BasicDBObject(FieldType.PAY_ID.getName(), token.getPayId()));
			finalList.add(new BasicDBObject(FieldType.TOKEN_ID.getName(), token.getId()));
			finalList.add(new BasicDBObject("CARD_SAVE_PARAM", token.getCardSaveParam()));

			BasicDBObject finalQuery = new BasicDBObject("$and", finalList);
			BasicDBObject match = new BasicDBObject("$match", finalQuery);
			List<BasicDBObject> pipeline = Arrays.asList(match);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				coll.deleteOne(dbobj);
			}
			cursor.close();
		} catch (Exception ex) {
			logger.error("Exception while delete save card from DB" + ex);
		}
	}

	public void deleteVpa(VpaToken token) {
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.SAVED_VPA_COLLECTION_NAME.getValue()));
			List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();

			finalList.add(new BasicDBObject(FieldType.PAY_ID.getName(), token.getPayId()));
			finalList.add(new BasicDBObject(FieldType.TOKEN_ID.getName(), token.getId()));
			finalList.add(new BasicDBObject("VPA_SAVE_PARAM", token.getVpaSaveParam()));

			BasicDBObject finalQuery = new BasicDBObject("$and", finalList);
			BasicDBObject match = new BasicDBObject("$match", finalQuery);
			List<BasicDBObject> pipeline = Arrays.asList(match);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				coll.deleteOne(dbobj);
			}
			cursor.close();
		} catch (Exception ex) {
			logger.error("Exception while delete save VPA from DB" + ex);
		}
	}

	public void deleteNbBank(NBToken token) {
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.NB_TOKEN_COLLECTION_NAME.getValue()));
			List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();

			finalList.add(new BasicDBObject(FieldType.PAY_ID.getName(), token.getPayId()));
			finalList.add(new BasicDBObject(FieldType.TOKEN_ID.getName(), token.getId()));
			finalList.add(new BasicDBObject("SAVE_PARAM", token.getSaveParam()));

			BasicDBObject finalQuery = new BasicDBObject("$and", finalList);
			BasicDBObject match = new BasicDBObject("$match", finalQuery);
			List<BasicDBObject> pipeline = Arrays.asList(match);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				coll.deleteOne(dbobj);
			}
			cursor.close();
		} catch (Exception ex) {
			logger.error("Exception while delete save NB bank from DB" + ex);
		}
	}
	
	public void deleteSavedWallet(WLToken token) {
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.WL_TOKEN_COLLECTION_NAME.getValue()));
			List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();

			finalList.add(new BasicDBObject(FieldType.PAY_ID.getName(), token.getPayId()));
			finalList.add(new BasicDBObject(FieldType.TOKEN_ID.getName(), token.getId()));
			finalList.add(new BasicDBObject("SAVE_PARAM", token.getSaveParam()));

			BasicDBObject finalQuery = new BasicDBObject("$and", finalList);
			BasicDBObject match = new BasicDBObject("$match", finalQuery);
			List<BasicDBObject> pipeline = Arrays.asList(match);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				coll.deleteOne(dbobj);
			}
			cursor.close();
		} catch (Exception ex) {
			logger.error("Exception while delete save Wallet from DB" + ex);
		}
	}
	
	public Token find(String token) {
		return (Token) super.find(Token.class, token);
	}

	public void saveOrUpdate(Token token) {
		super.saveOrUpdate(token);
	}

	public List<Token> findAll() {
		return (List<Token>) super.findAll(Token.class);
	}

	// Remove all tokens matching email and return the count
	public int removeAll(String email) {
		return 0;
	}

	public Map<String, Token> getAll(String payId, String cardSaveParam) {

		Map<String, Token> allCard = new HashMap<String, Token>();
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.TOKEN_COLLECTION_NAME.getValue()));

			List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();

			finalList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			finalList.add(new BasicDBObject("CARD_SAVE_PARAM", cardSaveParam));

			BasicDBObject finalQuery = new BasicDBObject("$and", finalList);
			BasicDBObject match = new BasicDBObject("$match", finalQuery);
			List<BasicDBObject> pipeline = Arrays.asList(match);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				Token token = new Token();

				token.setId(dbobj.getString("_id"));
				token.setTokenId(dbobj.getString(FieldType.TOKEN_ID.getName()));
				token.setCardHolderType(dbobj.getString(FieldType.CARD_HOLDER_TYPE.getName()));
				token.setCardIssuerBank(dbobj.getString(FieldType.CARD_ISSUER_BANK.getName()));
				token.setMopType(dbobj.getString(FieldType.MOP_TYPE.getName()));
				token.setPaymentType(dbobj.getString(FieldType.PAYMENT_TYPE.getName()));
				token.setCustomerName(dbobj.getString(FieldType.CUST_NAME.getName()));
				token.setStatus(TokenStatus.valueOf(dbobj.getString(FieldType.STATUS.getName()).toString()));
				token.setCardSaveParam(dbobj.getString("CARD_SAVE_PARAM"));
				token.setCardNumber(dbobj.getString(FieldType.CARD_NUMBER.getName()));
				token.setExpiryDate(encryptDecryptService.decrypt(PropertiesManager.propertiesMap.get(Constants.SAVE_CARD_ADMIN_PAYID.getValue()),dbobj.getString(FieldType.CARD_EXP_DT.getName())));
				//token.setExpiryDate(dbobj.getString(FieldType.CARD_EXP_DT.getName()));
				token.setCardMask(dbobj.getString(FieldType.CARD_MASK.getName()));
				token.setKeyId(dbobj.getString(FieldType.KEY_ID.getName()));
				token.setPayId(dbobj.getString(FieldType.PAY_ID.getName()));
				token.setCardIssuerBank(dbobj.getString(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()));
				token.setCardIssuerCountry(dbobj.getString(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()));
				token.setCardHolderType(dbobj.getString(FieldType.CARD_HOLDER_TYPE.getName()));
				token.setPaymentsRegion(dbobj.getString(FieldType.PAYMENTS_REGION.getName()));

				allCard.put(token.getId(), token);
			}
			cursor.close();
			return allCard;
		} catch (Exception ex) {
			logger.error("Exception while get all save cards from DB" + ex);
		}
		return allCard;
	}

	public Map<String, VpaToken> getAllVpa(String payId, String vpaSaveParam) {

		Map<String, VpaToken> allVpa = new HashMap<String, VpaToken>();
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.SAVED_VPA_COLLECTION_NAME.getValue()));

			List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();

			finalList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			finalList.add(new BasicDBObject("VPA_SAVE_PARAM", vpaSaveParam));

			BasicDBObject finalQuery = new BasicDBObject("$and", finalList);
			BasicDBObject match = new BasicDBObject("$match", finalQuery);
			List<BasicDBObject> pipeline = Arrays.asList(match);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				VpaToken token = new VpaToken();

				token.setId(dbobj.getString("_id"));
				token.setTokenId(dbobj.getString(FieldType.TOKEN_ID.getName()));
				token.setMopType(dbobj.getString(FieldType.MOP_TYPE.getName()));
				token.setPaymentType(dbobj.getString(FieldType.PAYMENT_TYPE.getName()));
				token.setPayerName(dbobj.getString(FieldType.PAYER_NAME.getName()));
				token.setStatus(TokenStatus.valueOf(dbobj.getString(FieldType.STATUS.getName()).toString()));
				token.setVpaSaveParam(dbobj.getString("VPA_SAVE_PARAM"));
				token.setVpaMask(dbobj.getString(FieldType.VPA_MASK.getName()));
				token.setVpa(dbobj.getString(FieldType.PAYER_ADDRESS.getName()));
				token.setKeyId(dbobj.getString(FieldType.KEY_ID.getName()));
				token.setPayId(dbobj.getString(FieldType.PAY_ID.getName()));
				token.setCardHolderType(dbobj.getString(FieldType.CARD_HOLDER_TYPE.getName()));
				token.setPaymentsRegion(dbobj.getString(FieldType.PAYMENTS_REGION.getName()));

				allVpa.put(token.getId(), token);
			}
			cursor.close();
			return allVpa;
		} catch (Exception ex) {
			logger.error("Exception while get all saved Vpa from DB" + ex);
		}
		return allVpa;
	}

	public Map<String, NBToken> getAllNBBank(String payId, String saveParam) {

		Map<String, NBToken> allBank = new HashMap<String, NBToken>();
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.NB_TOKEN_COLLECTION_NAME.getValue()));

			List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();

			finalList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			finalList.add(new BasicDBObject("SAVE_PARAM", saveParam));

			BasicDBObject finalQuery = new BasicDBObject("$and", finalList);
			BasicDBObject match = new BasicDBObject("$match", finalQuery);
			List<BasicDBObject> pipeline = Arrays.asList(match);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				NBToken token = new NBToken();

				token.setId(dbobj.getString("_id"));
				token.setTokenId(dbobj.getString(FieldType.TOKEN_ID.getName()));
				token.setMopType(dbobj.getString(FieldType.MOP_TYPE.getName()));
				token.setPaymentType(dbobj.getString(FieldType.PAYMENT_TYPE.getName()));
				//token.setPayerName(dbobj.getString(FieldType.PAYER_NAME.getName()));
				token.setStatus(TokenStatus.valueOf(dbobj.getString(FieldType.STATUS.getName()).toString()));
				//token.setVpaSaveParam(dbobj.getString("VPA_SAVE_PARAM"));
				//token.setVpaMask(dbobj.getString(FieldType.VPA_MASK.getName()));
				//token.setVpa(dbobj.getString(FieldType.PAYER_ADDRESS.getName()));
				token.setKeyId(dbobj.getString(FieldType.KEY_ID.getName()));
				token.setPayId(dbobj.getString(FieldType.PAY_ID.getName()));
				token.setCardHolderType(dbobj.getString(FieldType.CARD_HOLDER_TYPE.getName()));
				token.setPaymentsRegion(dbobj.getString(FieldType.PAYMENTS_REGION.getName()));

				allBank.put(token.getId(), token);
			}
			cursor.close();
			return allBank;
		} catch (Exception ex) {
			logger.error("Exception while get all saved Bank from DB" + ex);
		}
		return allBank;
	}
	
	
	public Map<String, WLToken> getAllWallet(String payId, String saveParam) {

		Map<String, WLToken> allWallet = new HashMap<String, WLToken>();
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.WL_TOKEN_COLLECTION_NAME.getValue()));

			List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();

			finalList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			finalList.add(new BasicDBObject("SAVE_PARAM", saveParam));

			BasicDBObject finalQuery = new BasicDBObject("$and", finalList);
			BasicDBObject match = new BasicDBObject("$match", finalQuery);
			List<BasicDBObject> pipeline = Arrays.asList(match);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				WLToken token = new WLToken();

				token.setId(dbobj.getString("_id"));
				token.setTokenId(dbobj.getString(FieldType.TOKEN_ID.getName()));
				token.setMopType(dbobj.getString(FieldType.MOP_TYPE.getName()));
				token.setPaymentType(dbobj.getString(FieldType.PAYMENT_TYPE.getName()));
				//token.setPayerName(dbobj.getString(FieldType.PAYER_NAME.getName()));
				token.setStatus(TokenStatus.valueOf(dbobj.getString(FieldType.STATUS.getName()).toString()));
				//token.setVpaSaveParam(dbobj.getString("VPA_SAVE_PARAM"));
				//token.setVpaMask(dbobj.getString(FieldType.VPA_MASK.getName()));
				//token.setVpa(dbobj.getString(FieldType.PAYER_ADDRESS.getName()));
				token.setKeyId(dbobj.getString(FieldType.KEY_ID.getName()));
				token.setPayId(dbobj.getString(FieldType.PAY_ID.getName()));
				token.setCardHolderType(dbobj.getString(FieldType.CARD_HOLDER_TYPE.getName()));
				token.setPaymentsRegion(dbobj.getString(FieldType.PAYMENTS_REGION.getName()));

				allWallet.put(token.getId(), token);
			}
			cursor.close();
			return allWallet;
		} catch (Exception ex) {
			logger.error("Exception while get all saved wallet from DB" + ex);
		}
		return allWallet;
	}

	public Map<String, String> getToken(String tokenId) {
		Map<String, String> tokenMap = new HashMap<String, String>();
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.TOKEN_COLLECTION_NAME.getValue()));

			BasicDBObject finalQuery = new BasicDBObject(FieldType.TOKEN_ID.getName(), tokenId);
			List<BasicDBObject> pipeline = Arrays.asList(finalQuery);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				tokenMap.put(FieldType.MOP_TYPE.getName(), dbobj.getString(FieldType.MOP_TYPE.getName()));
				tokenMap.put(FieldType.CUST_NAME.getName(), dbobj.getString(FieldType.CUST_NAME.getName()));
				tokenMap.put(FieldType.PAYMENT_TYPE.getName(), dbobj.getString(FieldType.PAYMENT_TYPE.getName()));
				tokenMap.put(FieldType.INTERNAL_CARD_ISSUER_BANK.getName(),
						dbobj.getString(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()));
				tokenMap.put(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName(),
						dbobj.getString(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()));

			}
			cursor.close();
			return tokenMap;
		} catch (Exception ex) {
			logger.error("Exception while get token by tokenId from DB" + ex);
		}
		return tokenMap;
	}

	public Token findTokenById(String id) {

		Token token = new Token();
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.TOKEN_COLLECTION_NAME.getValue()));
			List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();

			finalList.add(new BasicDBObject("_id", id));
			finalList.add(new BasicDBObject(FieldType.STATUS.getName(), TokenStatus.ACTIVE.name().toString()));

			BasicDBObject finalQuery = new BasicDBObject("$and", finalList);

			List<BasicDBObject> pipeline = Arrays.asList(finalQuery);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				token.setId(dbobj.getString("_id"));
				token.setTokenId(dbobj.getString(FieldType.TOKEN_ID.getName()));
				token.setCardHolderType(dbobj.getString(FieldType.CARD_HOLDER_TYPE.getName()));
				token.setCardIssuerBank(dbobj.getString(FieldType.CARD_ISSUER_BANK.getName()));
				token.setMopType(dbobj.getString(FieldType.MOP_TYPE.getName()));
				token.setPaymentType(dbobj.getString(FieldType.PAYMENT_TYPE.getName()));
				token.setCustomerName(dbobj.getString(FieldType.CUST_NAME.getName()));
				token.setStatus(TokenStatus.valueOf(dbobj.getString(FieldType.STATUS.getName()).toString()));
				token.setCardSaveParam(dbobj.getString("CARD_SAVE_PARAM"));
				token.setCardNumber(dbobj.getString(FieldType.CARD_NUMBER.getName()));
				token.setExpiryDate(dbobj.getString(FieldType.CARD_EXP_DT.getName()));
				token.setCardMask(dbobj.getString(FieldType.CARD_MASK.getName()));
				token.setKeyId(dbobj.getString(FieldType.KEY_ID.getName()));
				token.setPayId(dbobj.getString(FieldType.PAY_ID.getName()));
				token.setCardIssuerBank(dbobj.getString(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()));
				token.setCardIssuerCountry(dbobj.getString(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()));
				token.setCardHolderType(dbobj.getString(FieldType.CARD_HOLDER_TYPE.getName()));
				token.setPaymentsRegion(dbobj.getString(FieldType.PAYMENTS_REGION.getName()));

			}
			cursor.close();
			return token;
		} catch (Exception ex) {

			logger.error("Exception while get token by id in DB" + ex);
		}
		return token;
	}

	public boolean getCardNumber(String cardMask, String payId, String cardSaveParam) {
		boolean responseToken = false;
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.TOKEN_COLLECTION_NAME.getValue()));

			List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();

			finalList.add(new BasicDBObject(FieldType.CARD_MASK.getName(), cardMask));
			finalList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			finalList.add(new BasicDBObject("CARD_SAVE_PARAM", cardSaveParam));

			BasicDBObject finalquery = new BasicDBObject("$and", finalList);
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			List<BasicDBObject> pipeline = Arrays.asList(match);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				return true;
			}

			cursor.close();
			return responseToken;
		} catch (MongoException mongoException) {
			logger.info("Caught execption while check this card is already saved in DB" + mongoException);
		}
		return responseToken;
	}
	
	public boolean getNBBank(String mopType, String paymentType, String payId, String saveParam) {
		boolean responseToken = false;
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.NB_TOKEN_COLLECTION_NAME.getValue()));

			List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();

			finalList.add(new BasicDBObject(FieldType.MOP_TYPE.getName(), mopType));
			finalList.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
			finalList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			finalList.add(new BasicDBObject("SAVE_PARAM", saveParam));
			

			BasicDBObject finalquery = new BasicDBObject("$and", finalList);
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			List<BasicDBObject> pipeline = Arrays.asList(match);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				return true;
			}

			cursor.close();
			return responseToken;
		} catch (MongoException mongoException) {
			logger.info("Caught execption while check this bank is already saved in DB" + mongoException);
		}
		return responseToken;
	}

	public boolean getWallet(String mopType, String paymentType, String payId, String saveParam) {
		boolean responseToken = false;
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.WL_TOKEN_COLLECTION_NAME.getValue()));

			List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();

			finalList.add(new BasicDBObject(FieldType.MOP_TYPE.getName(), mopType));
			finalList.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
			finalList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			finalList.add(new BasicDBObject("SAVE_PARAM", saveParam));
			

			BasicDBObject finalquery = new BasicDBObject("$and", finalList);
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			List<BasicDBObject> pipeline = Arrays.asList(match);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				return true;
			}

			cursor.close();
			return responseToken;
		} catch (MongoException mongoException) {
			logger.info("Caught execption while check this wallet is already saved in DB" + mongoException);
		}
		return responseToken;
	}
	
	public boolean getVPA(String vpa, String payId, String vpaSaveParam) {
		boolean responseToken = false;
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.SAVED_VPA_COLLECTION_NAME.getValue()));

			List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();

			finalList.add(new BasicDBObject(FieldType.PAYER_ADDRESS.getName(), vpa));
			finalList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			finalList.add(new BasicDBObject("VPA_SAVE_PARAM", vpaSaveParam));

			BasicDBObject finalquery = new BasicDBObject("$and", finalList);
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			List<BasicDBObject> pipeline = Arrays.asList(match);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				return true;
			}

			cursor.close();
			return responseToken;
		} catch (MongoException mongoException) {
			logger.info("Caught execption while check VPA is already saved in DB" + mongoException);
		}
		return responseToken;
	}


}
