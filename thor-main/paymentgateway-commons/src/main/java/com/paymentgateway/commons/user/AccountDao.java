package com.paymentgateway.commons.user;

import java.util.List;

import org.springframework.stereotype.Component;

import com.paymentgateway.commons.dao.HibernateAbstractDao;
import com.paymentgateway.commons.exception.DataAccessLayerException;

@Component
public class AccountDao extends HibernateAbstractDao {

	public AccountDao(){
		super();
	}

	public void create(Account account) throws DataAccessLayerException {
        super.save(account);
    }
	
	public void delete(Account account) throws DataAccessLayerException {
        super.delete(account);
    }
	
	public void update(Account account) throws DataAccessLayerException {
        super.saveOrUpdate(account);
    }
	
	@SuppressWarnings("rawtypes")
	public  List findAll() throws DataAccessLayerException{
	    return super.findAll(Account.class);
	}
	 
	public Account find(Long id) throws DataAccessLayerException {
	    return (Account) super.find(Account.class, id);
	}
	 
	public Account find(String name) throws DataAccessLayerException {
	    return (Account) super.find(Account.class, name);
	}
}
