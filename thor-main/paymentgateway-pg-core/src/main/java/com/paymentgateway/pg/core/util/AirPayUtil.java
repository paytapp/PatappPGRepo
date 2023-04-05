package com.paymentgateway.pg.core.util;

import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;

@Service
public class AirPayUtil {

	private static Logger logger = LoggerFactory.getLogger(AirPayUtil.class.getName());

	// Method for Creating private key
	public String generatingPvtKey(Fields fields) {
		String privateKey = "";
		try {
			String sTemp = fields.get(FieldType.TXN_KEY.getName()) + "@" + fields.get(FieldType.ADF1.getName()) + ":|:"
					+ fields.get(FieldType.ADF2.getName());
//			logger.info("private key making String >> " + sTemp);
			MessageDigest md1 = MessageDigest.getInstance("SHA-256");
			md1.update(sTemp.getBytes());
			byte byteData[] = md1.digest();
			StringBuffer sb1 = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
				sb1.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			}
			privateKey = sb1.toString();
		} catch (Exception e) {
			logger.error("Exception in AirPayUtil.generatingPvtKey >>> ", e);
		}
		return privateKey;
	}

	// generating checksum
	public String generatingCheckSum(JSONObject json, Fields fields) {
		String sChecksum = "";
		try {
			String privateKey = null;
			DateFormat df = new SimpleDateFormat("YYYY-MM-dd");
			String sTemp = fields.get(FieldType.ADF1.getName()) + "~:~" + fields.get(FieldType.ADF2.getName());
//			logger.info("private key making String >> " + sTemp);
			MessageDigest md1 = MessageDigest.getInstance("SHA-256");
			md1.update(sTemp.getBytes());
			byte byteData[] = md1.digest();
			StringBuffer sb1 = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
				sb1.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			}
			privateKey = sb1.toString();
			String sCurDate = df.format(new Date());

			String sAllData = json.get("buyerEmail").toString() + json.get("buyerFirstName").toString()
					+ json.get("buyerLastName").toString() + json.get("buyerAddress").toString()
					+ json.get("buyerCity").toString() + json.get("buyerState").toString()
					+ json.get("buyerCountry").toString() + json.get("amount").toString()
					+ json.get("orderid").toString() + json.get("channel_mode") + json.get("vpa") + sCurDate;
//			logger.info("sAllData String values >> " + sAllData);
			String sTemp1 = privateKey + "@" + sAllData;
//			logger.info("Checksum making String >> " + sTemp1);
			md1.update(sTemp1.getBytes());
			byte byteData1[] = md1.digest();
			sb1 = new StringBuffer();
			for (int i = 0; i < byteData1.length; i++) {
				sb1.append(Integer.toString((byteData1[i] & 0xff) + 0x100, 16).substring(1));
			}
			sChecksum = sb1.toString();
//			logger.info("Checksum >> " + sChecksum);
		} catch (Exception e) {
			logger.error("Exception in AirPayUtil.generatingCheckSum >>> ", e);
		}
		return sChecksum;
	}

	public String generatingRefundCheckSum(JSONObject json, Fields fields) {
		String sChecksum = "";
		try {
			String privateKey = null;
			DateFormat df = new SimpleDateFormat("YYYY-MM-dd");
			String sTemp = fields.get(FieldType.ADF1.getName()) + "~:~" + fields.get(FieldType.ADF2.getName());
//			logger.info("private key making String >> " + sTemp);
			MessageDigest md1 = MessageDigest.getInstance("SHA-256");
			md1.update(sTemp.getBytes());
			byte byteData[] = md1.digest();
			StringBuffer sb1 = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
				sb1.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			}
			privateKey = sb1.toString();
			String sCurDate = df.format(new Date());

			String sAllData = json.get("mercid").toString() + json.get("mode").toString()
					+ json.get("transactions").toString() + sCurDate;
//			logger.info("sAllData String values >> " + sAllData);
			String sTemp1 = privateKey + "@" + sAllData;
//			logger.info("Checksum making String >> " + sTemp1);
			md1.update(sTemp1.getBytes());
			byte byteData1[] = md1.digest();
			sb1 = new StringBuffer();
			for (int i = 0; i < byteData1.length; i++) {
				sb1.append(Integer.toString((byteData1[i] & 0xff) + 0x100, 16).substring(1));
			}
			sChecksum = sb1.toString();
//			logger.info("Checksum >> " + sChecksum);
		} catch (Exception e) {
			logger.error("Exception in AirPayUtil.generatingCheckSum >>> ", e);
		}
		return sChecksum;
	}

	public String generatingStatusCheckSum(Fields fields) {
		String sChecksum = "";
		try {
			String privateKey = null;
			DateFormat df = new SimpleDateFormat("YYYY-MM-dd");
			String sTemp = fields.get(FieldType.ADF1.getName()) + "~:~" + fields.get(FieldType.ADF2.getName());
//			logger.info("private key making String >> " + sTemp);
			MessageDigest md1 = MessageDigest.getInstance("SHA-256");
			md1.update(sTemp.getBytes());
			byte byteData[] = md1.digest();
			StringBuffer sb1 = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
				sb1.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			}
			privateKey = sb1.toString();
			String sCurDate = df.format(new Date());

			String sAllData = fields.get(FieldType.MERCHANT_ID.getName()) + fields.get(FieldType.PG_REF_NUM.getName())
					+ sCurDate;
//			logger.info("sAllData String values >> " + sAllData);
			String sTemp1 = privateKey + "@" + sAllData;
//			logger.info("Checksum making String >> " + sTemp1);
			md1.update(sTemp1.getBytes());
			byte byteData1[] = md1.digest();
			sb1 = new StringBuffer();
			for (int i = 0; i < byteData1.length; i++) {
				sb1.append(Integer.toString((byteData1[i] & 0xff) + 0x100, 16).substring(1));
			}
			sChecksum = sb1.toString();
//			logger.info("Checksum >> " + sChecksum);
		} catch (Exception e) {
			logger.error("Exception in AirPayUtil.generatingCheckSum >>> ", e);
		}
		return sChecksum;
	}
}
