package com.paymentgateway.firstdata;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class MyAuthenticator extends Authenticator {
    private String username, password;
 
    public MyAuthenticator(String user, String pass) {
      username = user;
      password = pass;
    }
 
    protected PasswordAuthentication getPasswordAuthentication() {
      System.out.println("Requesting Prompt : " + getRequestingPrompt());
      System.out.println("Requesting Protocol: " + getRequestingProtocol());
      System.out.println("Requesting Scheme : " + getRequestingScheme());
      System.out.println("Requesting Site  : " + getRequestingSite());
      return new PasswordAuthentication(username, password.toCharArray());
    }
  }