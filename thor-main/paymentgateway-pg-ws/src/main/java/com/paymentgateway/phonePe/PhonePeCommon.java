package com.paymentgateway.phonePe;

import java.io.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import javax.mail.Session;
import javax.mail.Transport;

import org.springframework.stereotype.Service;

@Service
public class PhonePeCommon {
	   
	  
	   public static HashMap<String, String> readProperty(String propFileName) {
		   HashMap propMap=new HashMap<String, String>();
		   String ymlFileLocation="";
		   String dataArr[]=new String[2];
		   try {
			   ymlFileLocation = System.getenv("PG_PROPS");
			   System.out.println("File location is ----"+ymlFileLocation);
			   File myObj = new File(ymlFileLocation+propFileName);
			   Scanner myReader = new Scanner(myObj);
			  // System.out.println("File Name with location is #####----"+myReader);
			      while (myReader.hasNextLine()) {
			        String data = myReader.nextLine();
			        //System.out.println(data);
			        if(!data.isEmpty()) {
			        	//System.out.println("Data===="+data);
			        	dataArr[0]=data.split("=")[0];
			        	dataArr[1]=data.split("=")[1];
			        	propMap.put(dataArr[0].trim(), dataArr[1].trim());
			        	//System.out.println("Data[0]===="+data.split("=")[0]);
			        	//System.out.println("Data[1]===="+data.split("=")[1]);
			        }
			      }
			      myReader.close();
		   }catch(Exception e) {
			   e.printStackTrace();
		   }
		   return propMap;
	   }

	   
	   public boolean sendMail(String recipient,String sender,String subject,String text, String host ) {
		   boolean sendMail=false;
		   //String recipient = "ayushaa29@gmail.com";
		   
		      // email ID of  Sender.
		     // String sender = "arvind.thelotus02@gmail.com";
		 
		      // using host as localhost
		     // String host = "127.0.0.1";
		      // Getting system properties
		      Properties properties = System.getProperties();
		 
		      // Setting up mail server
		      properties.setProperty("mail.smtp.host", host);
		 
		      // creating session object to get properties
		      Session session = Session.getDefaultInstance(properties);
		   try {
			// MimeMessage object.
		         MimeMessage message = new MimeMessage(session);
		 
		         // Set From Field: adding senders email to from field.
		         message.setFrom(new InternetAddress(sender));
		 
		         // Set To Field: adding recipient's email to from field.
		         message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
		 
		         // Set Subject: subject of the email
		         message.setSubject(subject);
		 
		         // set body of the email.
		         message.setText(text);
		 
		         // Send email.
		         Transport.send(message);
		         System.out.println("Mail successfully sent");
		         sendMail=true;
		   }catch (MessagingException mex)
		      {
		         mex.printStackTrace();
		      }
		   catch(Exception e) {
			   e.printStackTrace();
		   }
		   
		   return sendMail;
	   }
	   public boolean sendSMS() {
		   boolean sendSMS=false;
		   PhonePeCommon common = new PhonePeCommon();
		HashMap prop = common.readProperty("sms.properties");
			
			//phonepeResponse phonepeResponseData=new phonepeResponse();
			//txnAmount = txnAmount*100;
			
			String kaleyraTestKey = prop.get("kaleyra_test_key").toString();
		   try {
			/*
			 * sendsms.init();
			 * 
			 * sendsms.server = "https://sample.smshosts.com/";
			 * 
			 * sendsms.user = "username";
			 * 
			 * sendsms.password = "password";
			 * 
			 * sendsms.phonenumber = "+9999999999";
			 * 
			 * sendsms.text = "This is a test message";
			 * 
			 * sendsms.send();
			 */		   }catch(Exception e) {
			   e.printStackTrace();
		   }
		   return sendSMS;
	   }
	   
	   public static void main(String args[]) {
		   PhonePeCommon common=new PhonePeCommon();
		   String mailBody="This is mail body ";
		   String host = "127.0.0.1";
		   
		   String recipient = "ayushaa29@gmail.com";
		   
		      // email ID of  Sender.
		      String sender = "arvind.thelotus02@gmail.com";
		      String subject="Subject";
		 
		     
		   try {
			   common.sendMail(recipient,sender,subject,mailBody,host);
		   }catch(Exception e) {
			   e.printStackTrace();
		   }
	   }
}
