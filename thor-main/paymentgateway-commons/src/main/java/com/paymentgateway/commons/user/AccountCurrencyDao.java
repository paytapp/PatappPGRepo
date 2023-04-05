package com.paymentgateway.commons.user;

import java.util.List;

import org.springframework.stereotype.Component;

import com.paymentgateway.commons.dao.HibernateAbstractDao;
import com.paymentgateway.commons.exception.DataAccessLayerException;

@Component
public class AccountCurrencyDao extends HibernateAbstractDao {

	public AccountCurrencyDao(){
		super();
	}

	public void create(AccountCurrency accountCurrency) throws DataAccessLayerException {
        super.save(accountCurrency);
    }
	
	public void delete(AccountCurrency accountCurrency) throws DataAccessLayerException {
        super.delete(accountCurrency);
    }
	
	public void update(AccountCurrency accountCurrency) throws DataAccessLayerException {
        super.saveOrUpdate(accountCurrency);
    }
	
	@SuppressWarnings("rawtypes")
	public  List findAll() throws DataAccessLayerException{
	    return super.findAll(AccountCurrency.class);
	}
	 
	public AccountCurrency find(Long id) throws DataAccessLayerException {
	    return (AccountCurrency) super.find(AccountCurrency.class, id);
	}
	 
	public AccountCurrency find(String name) throws DataAccessLayerException {
	    return (AccountCurrency) super.find(AccountCurrency.class, name);
	}
	
}
