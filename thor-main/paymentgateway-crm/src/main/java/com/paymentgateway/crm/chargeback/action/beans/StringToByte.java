package com.paymentgateway.crm.chargeback.action.beans;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import com.paymentgateway.commons.user.User;
import com.paymentgateway.crm.action.AbstractSecureAction;
import com.paymentgateway.crm.chargeback.util.ChargebackConstants;

public class StringToByte extends AbstractSecureAction {
	private User user = new User();
	private static final long serialVersionUID = 2001408860593264625L;
	
	public byte[] getOldCommentToByte(String comment) {
		if (null != comment) {
			byte[] allFields = Base64.encodeBase64(comment.toString().getBytes());
			
			return allFields;
		} else {
			return null;
		}
	}
	public byte[] makeByteToString(String comment) {
		if (null != comment) {
			byte[] allFields = Base64.decodeBase64(comment.toString().getBytes());
			
			return allFields;
		} else {
			return null;
		}
	}
	public byte[] makeStringToByte(String comment) {
		if (null != comment) {
			byte[] allFields = Base64.encodeBase64(comment.toString().getBytes());
			
			return allFields;
		} else {
			return null;
		}
	}
	public String makeNewComment(String comment,String user,String documentId) {
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		System.out.println(dateFormat.format(date)); 
		
		StringBuilder allFieldsSum = new StringBuilder();
		
		allFieldsSum.append(ChargebackConstants.SPACE.getValue());
		allFieldsSum.append(comment);
		allFieldsSum.append(ChargebackConstants.SPACE.getValue());
		allFieldsSum.append(ChargebackConstants.COMMENTED_BY.getValue());
		allFieldsSum.append(user);
		allFieldsSum.append(ChargebackConstants.AT.getValue());
		allFieldsSum.append(dateFormat.format(date));
		allFieldsSum.append(ChargebackConstants.SPACE.getValue());
		if (!StringUtils.isEmpty(documentId)){
		allFieldsSum.append(ChargebackConstants.TILT.getValue() + documentId + ChargebackConstants.TILT.getValue());
		allFieldsSum.append(ChargebackConstants.SPACE.getValue());
		}
		allFieldsSum.append(ChargebackConstants.SEPERATOR.getValue());
		

		return 	allFieldsSum.toString();
	}
	
	
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}

}
