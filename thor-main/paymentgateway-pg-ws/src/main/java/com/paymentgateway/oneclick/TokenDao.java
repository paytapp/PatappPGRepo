package com.paymentgateway.oneclick;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.HibernateAbstractDao;
import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.user.Token;
import com.paymentgateway.commons.util.FieldType;

/**
 * @author Sunil
 *
 */
@Service
public class TokenDao extends HibernateAbstractDao {
	public TokenDao() {
		super();
	}

	public void create(Token token) {
		super.save(token);
	}

	public void delete(Token token) {
		super.delete(token);
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
	
	//Remove all tokens matching email and return the count
	public int removeAll(String email){
		return 0;
	}
	
	public Map<String, Token> getAll(String payId, String email){
		StringBuilder hql = new StringBuilder();
		
		hql.append("from Token ");
		
		if(!StringUtils.isEmpty(email) && !StringUtils.isEmpty(payId)){
			hql.append("where email = '");
			hql.append(email);
			hql.append("'");
			hql.append(" and payId = '");
			hql.append(payId);
			hql.append("'");
		} else {
			return null;
		}
		
		List<Token> tokens = (List<Token>) super.findAllBy(hql.toString());
		
		HashMap<String, Token> tokenMap = new HashMap<String, Token>();
		for(Token token : tokens){
			tokenMap.put(token.getId(), token);
		}
		
		return tokenMap;
	}
	
	public Map<String, String> getToken(String tokenId){
		StringBuilder hql = new StringBuilder();
		
		hql.append("from Token ");
		
		if(!StringUtils.isEmpty(tokenId)){
			hql.append("where id = '");
			hql.append(tokenId);
			hql.append("'");
		} else {
			return null;
		}
		
		List<Token> tokens = (List<Token>) super.findAllBy(hql.toString());
		
		Map<String, String> tokenMap = new HashMap<String, String>();
		for (Token token  : tokens) {
		//	tokenMap.put(FieldType.CARD_EXP_DT.getName(), token.getExpiryDate());
		//	tokenMap.put(FieldType.CARD_NUMBER.getName(), token.getCardNumber());
			tokenMap.put(FieldType.MOP_TYPE.getName(), token.getMopType());
			tokenMap.put(FieldType.CUST_NAME.getName(), token.getCustomerName());
			tokenMap.put(FieldType.PAYMENT_TYPE.getName(), token.getPaymentType());
			tokenMap.put(FieldType.INTERNAL_CARD_ISSUER_BANK.getName(), token.getCardIssuerBank());
			tokenMap.put(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName(), token.getCardIssuerCountry());
			
		}
		return tokenMap;
	}
	
	public Token getCardNumber(String cardNumber, String payId, String email) {
		Token responseToken = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			
			Query query = session.createQuery("from Token where cardNumber = :cardNumber and payId =:payId and email =:email");
			query.setParameter("cardNumber", cardNumber);
			query.setParameter("payId", payId);
			query.setParameter("email", email);
			
			responseToken = (Token) query.uniqueResult();
				
			tx.commit();	
			
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} 
		finally {
			autoClose(session);
		}
		return responseToken;
	}
}
