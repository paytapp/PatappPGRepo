package com.paymentgateway.crm.actionBeans;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bson.codecs.Decoder;
import org.bson.conversions.Bson;
import org.codehaus.jackson.JsonParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import com.paymentgateway.commons.email.EmailServiceProvider;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.Complaint;
import com.paymentgateway.commons.user.MerchantProcessingApplication;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Base64EncodeDecode;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldFormatType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.crm.mpa.MPAServicesFactory;

@Service
public class ComplaintRaisingService {

	private static Logger logger = LoggerFactory.getLogger(ComplaintRaisingService.class.getName());

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private DataEncDecTool dataEncDecTool;

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private Base64EncodeDecode base64EncodeDecode;

	@Autowired
	private UserDao userDao;

	@Autowired
	private EmailServiceProvider emailServiceProvider;
	
	@Autowired
	private MPAServicesFactory mpaServicesFactory;

	private String status_In_process ="In-Process";
	private String status_Resolved ="Resolved";

	private static final String prefix = "MONGO_DB_";

	public String createComplaint(String merchantId, String subMerchantId, User sessionUser, String status,
			String fileName, File[] uploadedFile, String comments, String complaintType,String  complaintForType) throws IOException {
		logger.info("Inside createComplaint()");
		String complaintId = TransactionManager.getNewTransactionId();
		FileInputStream in = null;
	    FileOutputStream out = null;
	    
		try {
			BasicDBObject complaintObj = new BasicDBObject();
			
			BasicDBObject fileObj = new BasicDBObject();

			complaintObj.put(FieldType.COMPLAINT_ID.getName(), complaintId);
			complaintObj.put(FieldType.PAY_ID.getName(), merchantId);

			if (StringUtils.isNotBlank(subMerchantId) && subMerchantId.equalsIgnoreCase("ALL")) {
				complaintObj.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId);
			} else {
				complaintObj.put(FieldType.SUB_MERCHANT_ID.getName(), null);
			}

			String currentDate = DateCreater.formatDateForDb(new Date());
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date currDateFormat = format.parse(currentDate);
			LocalDate localDate = currDateFormat.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			String DatesIndex = localDate.toString().replaceAll("-", "");

			complaintObj.put(FieldType.DATE_INDEX.getName(), DatesIndex);
			complaintObj.put(FieldType.CREATE_DATE.getName(), currentDate);
			complaintObj.put(FieldType.UPDATE_DATE.getName(), currentDate);
			complaintObj.put(FieldType.CREATED_BY.getName(), sessionUser.getEmailId());
			complaintObj.put(FieldType.UPDATED_BY.getName(), sessionUser.getEmailId());
			complaintObj.put(FieldType.STATUS.getName(), status);
			complaintObj.put(FieldType.COMPLAINT_TYPE.getName(), complaintType);
			
			if(StringUtils.isNotBlank(complaintForType)) {
				 complaintObj.put(FieldType.COMPLAINT_RAISE_FOR_USER_TYPE.getName(), complaintForType);
			 }
			
			if(StringUtils.isNotBlank(complaintForType)) {
				complaintObj.put(FieldType.COMPLAINT_RAISE_BY.getName(), sessionUser.getUserType().name());
			 }else {
					if (sessionUser.isSuperMerchant()) {
						complaintObj.put(FieldType.COMPLAINT_RAISE_BY.getName(), "SUPER-MERCHANT");
						if(StringUtils.isNotBlank(subMerchantId)  && !subMerchantId.equalsIgnoreCase("ALL")) {
							 complaintObj.put(FieldType.COMPLAINT_RAISE_FOR_USER_TYPE.getName(), "SUB-MERCHANT");
						 }else {
							 complaintObj.put(FieldType.COMPLAINT_RAISE_FOR_USER_TYPE.getName(), "SUPER-MERCHANT");
						 }
				  } else if (!sessionUser.isSuperMerchant() && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
						complaintObj.put(FieldType.COMPLAINT_RAISE_BY.getName(), "SUB-MERCHANT");
						complaintObj.put(FieldType.COMPLAINT_RAISE_FOR_USER_TYPE.getName(), "SUB-MERCHANT");
				  } else {
						complaintObj.put(FieldType.COMPLAINT_RAISE_BY.getName(), "MERCHANT");
				  }
			 }

			
			
			
			
			
			 if(StringUtils.isNotBlank(subMerchantId)  && !subMerchantId.equalsIgnoreCase("ALL")) {
				 complaintObj.put(FieldType.COMPLAINT_RAISE_FOR_EMAIL_ID.getName(), userDao.getEmailIdByPayId(subMerchantId));
			 }else {
		    	 complaintObj.put(FieldType.COMPLAINT_RAISE_FOR_EMAIL_ID.getName(), userDao.getEmailIdByPayId(merchantId));
		     }
			 if(StringUtils.isNotBlank(complaintForType) && complaintForType.equalsIgnoreCase("USER")) {
				 complaintObj.put(FieldType.COMPLAINT_RAISE_BY.getName(), complaintForType);
				 complaintObj.put(FieldType.COMPLAINT_RAISE_FOR_USER_TYPE.getName(), complaintForType);
				 complaintObj.put(FieldType.COMPLAINT_RAISE_FOR_EMAIL_ID.getName(), sessionUser.getEmailId());
			 }
			
			
			MongoDatabase dbIns = null;
			dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.COMPLAINT_COLLECTION.getValue()));

			Document complaintDoc = new Document(complaintObj);

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				collection.insertOne(dataEncDecTool.encryptDocument(complaintDoc));
			} else {
				collection.insertOne(complaintDoc);
			}

			
			fileObj.put(FieldType.COMPLAINT_ID.getName(), complaintId);
			fileObj.put(FieldType.COMMENTS.getName(), comments);
			fileObj.put(FieldType.COMMENTED_BY.getName(), sessionUser.getEmailId());
			fileObj.put(FieldType.STATUS.getName(), status);
			fileObj.put(FieldType.CREATE_DATE.getName(), currentDate);
			
		
			MongoCollection<Document> collection1 = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.COMPLAINT_FILE_COLLECTION.getValue()));
	
			if (StringUtils.isNotBlank(fileName)) {
				String[] fileNameArray = fileName.split(",");
				List<BasicDBObject> testplans = new ArrayList<>();
			    BufferedImage image = null;
			   // BufferedReader image = null;
			    File f = null;
			    String path =PropertiesManager.propertiesMap.get("COMPLAINT_RAISE_UPLOAD_IMAGE");
			      f = new File(path); //image file path
			      if(!f.exists()) {
			    	  f.mkdirs();
			      }
			      f = new File(path+complaintId);
		    	  if(!f.exists()) {
			    	  f.mkdirs();}
		    	 for (int i = 0; i < fileNameArray.length; i++) {
			    	 	BasicDBObject complaintObjImage = new BasicDBObject();
			    	 	f.getAbsoluteFile();
			    	 	f = new File(path+complaintId+"\\"+fileNameArray[i]);
			    	 	complaintObjImage.put(FieldType.FILENAME.getName(), fileNameArray[i]);
			    	 	complaintObjImage.put("path", f.getAbsolutePath());
			    	 	testplans.add(complaintObjImage);
				  
			    	 	in = new FileInputStream(uploadedFile[i].getAbsolutePath());
			    	 	out = new FileOutputStream(path+complaintId+"\\"+fileNameArray[i]);
			    	 	int c;
			    	 	while ((c = in.read()) != -1) {
			    	 		out.write(c);
			    	 	}
				    
				
				}
			     if(testplans.size()!=0) {
			    	 fileObj.put("ImageData", testplans);	 
			     }
			}
			Document fileDoc = new Document(fileObj);
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
				collection1.insertOne(dataEncDecTool.encryptDocument(fileDoc));
			} else {
				collection1.insertOne(fileDoc);
			}
			Date date = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
			complaintObj.put(FieldType.COMMENTS.getName(), comments);
			complaintObj.put("DATE", formatter.format(date));
			complaintObj.put(FieldType.MERCHANT_NAME.getName(), userDao.getMerchantNameByPayId(merchantId));
			complaintObj.put(FieldType.PHONE_NO.getName(), userDao.getMerchantPhoneNoByPayId(merchantId));
			sendMailToUsers(complaintId, complaintType,complaintObj);
		} catch (Exception ex) {
			logger.error("Exception : ", ex);
			return "";
		}finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
            	out.close();
            }
        }
		return complaintId;
	}

	public int countComplaint(String dateFrom, String dateTo, String merchantId, String subMerchantId, String createdBy, String complaintType,String status) {
		logger.info("Inside countComplaint()");
		int count = 0;
		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject dateIndexConditionQuery = new BasicDBObject();

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(dateFrom);
			Date dateEnd = format.parse(dateTo);

			LocalDate startDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			String fromDatesIndex = startDate.toString().replaceAll("-", "");
			String toDatesIndex = endDate.toString().replaceAll("-", "");

			dateIndexConditionQuery.put("DATE_INDEX",
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDatesIndex).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(toDatesIndex).toLocalizedPattern()).get());

			if (StringUtils.isNotBlank(merchantId) && !merchantId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantId));
			}

			if (StringUtils.isNotBlank(subMerchantId) && !subMerchantId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId));
			}
			if (StringUtils.isNotEmpty(createdBy) && !createdBy.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.CREATED_BY.getName(), createdBy));
			}
			if (StringUtils.isNotEmpty(complaintType) && !complaintType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.COMPLAINT_TYPE.getName(), complaintType));
			}
			if (StringUtils.isNotEmpty(status) && !complaintType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), status));
			}
			if (!dateIndexConditionQuery.isEmpty()) {
				paramConditionLst.add(dateIndexConditionQuery);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			logger.info("Inside ComplaintRaisingService , viewComplaint , finalQuery = " + finalquery);

			MongoDatabase dbIns = mongoInstance.getDB();

			BasicDBObject skip = null;
			BasicDBObject limit = null;

			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.COMPLAINT_COLLECTION.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", 1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort);
			count = (int) coll.count(finalquery);

		} catch (Exception ex) {
			logger.error("Exception : ", ex);
		}
		return count;
	}

	public List<Complaint> viewComplaint(String dateFrom, String dateTo, String merchantId, String subMerchantId,
			String createdBy, String complaintType, int start, int length ,String status) {
		logger.info("Inside viewComplaint()");
		List<Complaint> complaintList = new ArrayList<Complaint>();
		Map<String, User> userMap = new HashMap<String, User>();

		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject dateIndexConditionQuery = new BasicDBObject();

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(dateFrom);
			Date dateEnd = format.parse(dateTo);

			LocalDate startDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			String fromDatesIndex = startDate.toString().replaceAll("-", "");
			String toDatesIndex = endDate.toString().replaceAll("-", "");

			dateIndexConditionQuery.put("DATE_INDEX",
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDatesIndex).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(toDatesIndex).toLocalizedPattern()).get());

			if (StringUtils.isNotBlank(merchantId) && !merchantId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantId));
			}

			if (StringUtils.isNotBlank(subMerchantId) && !subMerchantId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId));
			}
			if (StringUtils.isNotEmpty(createdBy) && !createdBy.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.CREATED_BY.getName(), createdBy));
			}
			if (StringUtils.isNotEmpty(complaintType) && !complaintType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.COMPLAINT_TYPE.getName(), complaintType));
			}if (StringUtils.isNotEmpty(status) && !status.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), status));
			}

			if (!dateIndexConditionQuery.isEmpty()) {
				paramConditionLst.add(dateIndexConditionQuery);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			logger.info("Inside ComplaintRaisingService , viewComplaint , finalQuery = " + finalquery);

			MongoDatabase dbIns = mongoInstance.getDB();

			List<BasicDBObject> pipeline = null;
			BasicDBObject skip = null;
			BasicDBObject limit = null;

			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.COMPLAINT_COLLECTION.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

			BasicDBObject sortingfillter = new BasicDBObject(FieldType.CREATED_BY.getName(), createdBy);
			
			skip = new BasicDBObject("$skip", start);
			//limit = new BasicDBObject("$limit", length);
			//pipeline = Arrays.asList(match, sort, skip,limit);
			pipeline = Arrays.asList(match, sort, skip);
		//	coll.find().sort(sortingfillter);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
		
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				if (StringUtils.isNotBlank(dbobj.getString("IS_ENCRYPTED"))
						&& dbobj.getString("IS_ENCRYPTED").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}
				Complaint complaint = new Complaint();
				User user = new User();
				if (StringUtils.isNotBlank((String) dbobj.get(FieldType.PAY_ID.getName()))) {
					String payid = (String) dbobj.get(FieldType.PAY_ID.getName());
					if (userMap.get(payid) != null && !userMap.get(payid).getPayId().isEmpty()) {
						user = userMap.get(payid);
					} else {
						user = userDao.findPayId(payid);
						userMap.put(payid, user);
					}
				}

				complaint.setMerchant(user.getBusinessName());

				complaint.setComplaintId(dbobj.getString(FieldType.COMPLAINT_ID.getName()));
				complaint.setCreatedBy(dbobj.getString(FieldType.CREATED_BY.getName()));
				complaint.setUpdatedBy(dbobj.getString(FieldType.UPDATED_BY.getName()));
				complaint.setComments(dbobj.getString(FieldType.COMMENTS.getName()));

				if (dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {

					String subMerchant = dbobj.getString(FieldType.SUB_MERCHANT_ID.getName());
					User subMerchantUser = new User();

					if (userMap.get(subMerchant) != null && !userMap.get(subMerchant).getPayId().isEmpty()) {
						subMerchantUser = userMap.get(subMerchant);
					} else {
						subMerchantUser = userDao.findPayId(subMerchant);
						userMap.put(subMerchant, subMerchantUser);
					}
					if (subMerchantUser != null) {
						complaint.setSubMerchant(subMerchantUser.getBusinessName());
					}
				}

				complaint.setCreateDate(dbobj.getString(FieldType.CREATE_DATE.getName()));
				complaint.setUpdatedDate(dbobj.getString(FieldType.UPDATE_DATE.getName()));
				complaint.setStatus(dbobj.getString(FieldType.STATUS.getName()));

				complaint.setComplaintType(dbobj.getString(FieldType.COMPLAINT_TYPE.getName()));

				complaintList.add(complaint);
			}
		} catch (Exception ex) {
			logger.error("Exception : ", ex);
		}
		return complaintList;
	}

	public Complaint viewComplaintByComplaintId(String complaintId) {
		logger.info("Inside viewComplaintByComplaintId()");

		Complaint complaint = new Complaint();
		try {
			BasicDBObject finalquery = new BasicDBObject(FieldType.COMPLAINT_ID.getName(), complaintId);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.COMPLAINT_COLLECTION.getValue()));
			MongoCursor<Document> cursor1 = coll.find(finalquery).iterator();

			if (cursor1 != null) {
				Document document = cursor1.next();
				MongoCollection<Document> collelction = dbIns.getCollection(
						propertiesManager.propertiesMap.get(prefix + Constants.COMPLAINT_FILE_COLLECTION.getValue()));
				BasicDBObject match = new BasicDBObject("$match", finalquery);
				List<BasicDBObject> pipeline = Arrays.asList(match);
				AggregateIterable<Document> output = collelction.aggregate(pipeline);
				output.allowDiskUse(true);
				MongoCursor<Document> cursor = output.iterator();

				JSONArray msgData = new JSONArray();
				while (cursor.hasNext()) {
					Document dbobj = cursor.next();
					if (StringUtils.isNotBlank(dbobj.getString("IS_ENCRYPTED"))
							&& dbobj.getString("IS_ENCRYPTED").equalsIgnoreCase("Y")) {
						dbobj = dataEncDecTool.decryptDocument(dbobj);
					}
					JSONObject msg = new JSONObject();
					msg.put("name", userDao.getBusinessNameByEmailId(dbobj.getString(FieldType.COMMENTED_BY.getName())));
					msg.put("comment", dbobj.getString(FieldType.COMMENTS.getName()));
					msg.put("status", dbobj.getString(FieldType.STATUS.getName()));
					msg.put("date", dbobj.getString(FieldType.CREATE_DATE.getName()));
					if(dbobj.get("ImageData")!=null) {
						msg.put("file",true );
					}else {
						msg.put("file",false );
					}
					
					msgData.put(msg);
					complaint.setComments(dbobj.getString(FieldType.COMMENTS.getName()));
					complaint.setFileName(dbobj.getString(FieldType.FILENAME.getName()));
					complaint.setCommentedBy(dbobj.getString(FieldType.COMMENTED_BY.getName()));
					complaint.setComplaintFile(dbobj.getString(FieldType.COMPLAINT_FILE.getName()));
					complaint.setStatus(document.getString(FieldType.STATUS.getName()));
					
				}
				complaint.setMessage(msgData.toString());
			}
		} catch (Exception ex) {
			logger.error("Exception : ", ex);
		}
		return complaint;
	}

	private void sendMailToUsers(String complaintId, String complaintType,BasicDBObject complaintObj) {
		logger.info("Inside sendMailToUsers()");
		String emailList = propertiesManager.propertiesMap.get(Constants.COMPLAINT_MAIL_LIST.getValue());
		String mail = (String) complaintObj.get(FieldType.CREATED_BY.getName());
		String mailfor =(String) complaintObj.get(FieldType.COMPLAINT_RAISE_FOR_EMAIL_ID.getName());
		if((mail!=null && mailfor!=null) && mail.contentEquals(mailfor)) {
			emailList=emailList.concat(","+mail);
		}else {
			if(mail!=null) {
				emailList=emailList.concat(","+mail);
			}if(mailfor!=null) {
				emailList=emailList.concat(","+mailfor);
			}
		}
		
		emailServiceProvider.sendComplaintMail(emailList.split(","), complaintId, complaintType,complaintObj);
	}

	public Complaint updateComplaint(String complaintId, String status, String comments, String fileName,
			File[] uploadedFile, User sessionUser) throws IOException {
		logger.info("Inside updateComplaint()");
		JSONObject responseObj = new JSONObject();
		FileInputStream in = null;
	    FileOutputStream out = null;
	    Complaint complaint = new Complaint();
		try {
			
			BasicDBObject fileObj = new BasicDBObject();

			BasicDBObject finalquery = new BasicDBObject(FieldType.COMPLAINT_ID.getName(), complaintId);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.COMPLAINT_COLLECTION.getValue()));

			MongoCollection<Document> collection1 = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.COMPLAINT_FILE_COLLECTION.getValue()));

			Document document = (Document) coll.find(finalquery).first();
			
			
			
			BasicDBList andValues = new BasicDBList();
			List<BasicDBObject> cond = new ArrayList<BasicDBObject>();
			BasicDBObject complanintId = new BasicDBObject(FieldType.COMPLAINT_ID.getName(), complaintId);
			BasicDBObject statusId = new BasicDBObject(FieldType.STATUS.getName(), status);
			cond.add(complanintId);
			cond.add(statusId);
			BasicDBObject finalquerydata = new BasicDBObject("$and", cond);
			Document documentresult = collection1.find(finalquerydata).first();
			if(documentresult!=null) {
				complaint.setMessage("Complaint ID : "+complaintId + " already in "+status);
				return complaint;
			}
			
			
			if (document != null) {

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					document = dataEncDecTool.decryptDocument(document);
				}
				String currentDate = DateCreater.formatDateForDb(new Date());

				Bson filter = new Document(FieldType.COMPLAINT_ID.getName(),
						document.getString(FieldType.COMPLAINT_ID.getName()));
				Bson newValue = new Document(FieldType.UPDATE_DATE.getName(), currentDate)
						.append(FieldType.STATUS.getName(), status)
						.append(FieldType.UPDATED_BY.getName(), sessionUser.getEmailId());

				Bson updateDocument = new Document("$set", newValue);
				coll.updateOne(filter, updateDocument);
				String[] fileNameArray = fileName.split(",");

				fileObj.put(FieldType.COMPLAINT_ID.getName(), complaintId);
				fileObj.put(FieldType.COMMENTS.getName(), comments);
				fileObj.put(FieldType.COMMENTED_BY.getName(), sessionUser.getEmailId());
				fileObj.put(FieldType.STATUS.getName(), status);
				fileObj.put(FieldType.CREATE_DATE.getName(), currentDate);
				List<BasicDBObject> testplans = new ArrayList<>();
				
				File f = null;
				//!fileName.equalsIgnoreCase("")
				
				
			   if(StringUtils.isNotBlank(fileName)) {
			    String path =PropertiesManager.propertiesMap.get("COMPLAINT_RAISE_UPLOAD_IMAGE");
			      f = new File(path); //image file path
			      if(!f.exists()) {
			    	  f.mkdirs();
			      }
			      f = new File(path+complaintId);
		    	  if(!f.exists()) {
			    	  f.mkdirs();}
				
				for (int i = 0; i < fileNameArray.length; i++) {
					//String Base64QrString = base64EncodeDecode.base64Encoder(uploadedFile[i]);
						BasicDBObject complaintObjImage = new BasicDBObject();
						f.getAbsoluteFile();
						f = new File(path+complaintId+"\\"+fileNameArray[i]);
			    	 
						in = new FileInputStream(uploadedFile[i].getAbsolutePath());
						out = new FileOutputStream(path+complaintId+"\\"+fileNameArray[i]);
						int c;
						while ((c = in.read()) != -1) {
							out.write(c);
						}
						complaintObjImage.put(FieldType.FILENAME.getName(), fileNameArray[i]);
						complaintObjImage.put("path", f.getAbsolutePath());
						testplans.add(complaintObjImage);
				}
				 if(testplans.size()!=0) {
			    	 fileObj.put("ImageData", testplans);	 
			     }
			   }
				 
				Document fileDoc = new Document(fileObj);

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					collection1.insertOne(dataEncDecTool.encryptDocument(fileDoc));
				} else {
					collection1.insertOne(fileDoc);
				}

			}
			
			BasicDBObject complaintObj = new BasicDBObject();
			String date = document.getString(FieldType.CREATE_DATE.getName());
						
			String sDate1=date;  
		    Date date1=new SimpleDateFormat("yyyy-MM-dd").parse(sDate1);  
		    SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
			String strDate= formatter.format(date1);
			

			
			complaintObj.put(FieldType.COMPLAINT_ID.getName(), document.getString(FieldType.COMPLAINT_ID.getName()));
			complaintObj.put(FieldType.CREATE_DATE.getName(), document.getString(FieldType.CREATE_DATE.getName()));
			complaintObj.put(FieldType.UPDATE_DATE.getName(), document.getString(FieldType.UPDATE_DATE.getName()));
			complaintObj.put(FieldType.CREATED_BY.getName(), document.getString(FieldType.CREATED_BY.getName()));
			complaintObj.put(FieldType.STATUS.getName(),status );
			complaintObj.put("DATE", strDate);
			complaintObj.put(FieldType.COMPLAINT_TYPE.getName(), document.getString(FieldType.COMPLAINT_TYPE.getName()));
			complaintObj.put(FieldType.COMPLAINT_RAISE_FOR_EMAIL_ID.getName(),document.getString(FieldType.COMPLAINT_RAISE_FOR_EMAIL_ID.getName()));
			complaintObj.put(FieldType.PAY_ID.getName(), document.getString(FieldType.PAY_ID.getName()));
			complaintObj.put(FieldType.COMMENTS.getName(), comments);
			complaintObj.put(FieldType.MERCHANT_NAME.getName(), userDao.getMerchantNameByPayId(document.getString(FieldType.PAY_ID.getName())));
			complaintObj.put(FieldType.PHONE_NO.getName(), userDao.getMerchantPhoneNoByPayId(document.getString(FieldType.PAY_ID.getName())));
				
							
			sendMailToUsers(complaintId, document.getString(FieldType.COMPLAINT_TYPE.getName()),complaintObj);	
			
		
		    
		  			
			
			complaint.setMessage("Your status has been update successfully.");
		} catch (Exception ex) {
			logger.error("Exception : ", ex);
			
		}finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
		return complaint;
	}
	
	
	
	@SuppressWarnings("unchecked")
	public Complaint viewComplaintDetailDownloadAction(String complaintId, String status) {
		logger.info("Inside viewComplaintDetailDownloadAction()");
		Complaint complaint = new Complaint();
		try {
			String zipFileName=null;
			InputStream fileInputStream = null;
			StringBuilder allfileNames = new StringBuilder();
			int sizeSrcFiles=0;
			String[] srcFiles = null;
			String fileNameType=null;
			
			BasicDBObject finalquery = new BasicDBObject(FieldType.COMPLAINT_ID.getName(), complaintId);
			
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.COMPLAINT_COLLECTION.getValue()));
			MongoCursor<Document> cursor1 = coll.find(finalquery).iterator();
			if (cursor1 != null) {
				Document document = cursor1.next();
				MongoCollection<Document> collelction = dbIns.getCollection(
						propertiesManager.propertiesMap.get(prefix + Constants.COMPLAINT_FILE_COLLECTION.getValue()));
				BasicDBList andValues = new BasicDBList();
				List<BasicDBObject> cond = new ArrayList<BasicDBObject>();
				BasicDBObject complanintId = new BasicDBObject(FieldType.COMPLAINT_ID.getName(), complaintId);
				BasicDBObject statusId = new BasicDBObject(FieldType.STATUS.getName(), status);
				cond.add(complanintId);
				cond.add(statusId);
				BasicDBObject finalquerydata = new BasicDBObject("$and", cond);
				MongoCursor<Document> cursor = collelction.find(finalquerydata).iterator();
				JSONArray msgData = new JSONArray();
				List<Document> comments = null;
				JSONObject restult = new JSONObject();
				 String path =PropertiesManager.propertiesMap.get("COMPLAINT_RAISE_UPLOAD_IMAGE");
				 
				while (cursor.hasNext()) {
					Document dbobj = cursor.next();
					 JSONObject json = new JSONObject(JSON.serialize(dbobj));
					 if(json.has("COMPLAINT_ID")) {
						 zipFileName= "LTZ-"+json.getString("COMPLAINT_ID")+".zip";
					 }
					 restult.put("collectionData",json);
					 if(json.has("ImageData")) {
						 JSONArray dbArrayImageData = json.getJSONArray("ImageData");
						 sizeSrcFiles =dbArrayImageData.length();
						 srcFiles = new String[sizeSrcFiles];
						 for(int i=0;i<sizeSrcFiles;++i) {
							 JSONObject imageData = dbArrayImageData.getJSONObject(i);
							 if(imageData.has("path")) {
								  srcFiles[i] =imageData.getString("path");
								 }
						 }
					 }
					 
				}
				
				fileInputStream = zipFileDownloader(srcFiles, zipFileName, path);
				complaint.setFileInputStream(fileInputStream);
				complaint.setZipFileName(zipFileName);
				//complaint.setZipFileName("mp.zip");
			}
		}catch (Exception e) {
			logger.error("Exception Caught : ", e);
		}
		return  complaint;
	}
	

	public InputStream zipFileDownloader(String[] srcFiles, String ZipName, String fileLocation) throws IOException {

		String downLoadPath =PropertiesManager.propertiesMap.get("COMPLAINT_RAISE_DOWNLOAD_IMAGE");
		downLoadPath=downLoadPath+ZipName;
		byte[] buffer = new byte[1024];
		FileOutputStream fos = new FileOutputStream(downLoadPath);
		ZipOutputStream zos = new ZipOutputStream(fos);
		InputStream inputfileStream = null;
		
		 
		if (srcFiles != null) {

			for (int i = 0; i < srcFiles.length; i++) {
				//File srcFile = new File(fileLocation, srcFiles[i]);
				File srcFile = new File(srcFiles[i]);
				FileInputStream fis = new FileInputStream(srcFile);
				zos.putNextEntry(new ZipEntry(srcFile.getName()));

				int length;

				while ((length = fis.read(buffer)) > 0) {
					zos.write(buffer, 0, length);
				}
				zos.closeEntry();
			}
			File file = new File(downLoadPath);
			inputfileStream = new FileInputStream(file);
			zos.close();
		}
	
		return inputfileStream;
	}
	

	
	
}
