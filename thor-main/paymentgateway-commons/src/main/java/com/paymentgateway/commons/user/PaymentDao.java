package com.paymentgateway.commons.user;

import java.util.List;

import org.springframework.stereotype.Component;

import com.paymentgateway.commons.dao.HibernateAbstractDao;
import com.paymentgateway.commons.exception.DataAccessLayerException;

@Component
public class PaymentDao extends HibernateAbstractDao {
	
	public PaymentDao() {
        super();
    }

	public void create(Payment payment) throws DataAccessLayerException {
        super.save(payment);
    }
	
	public void delete(Payment payment) throws DataAccessLayerException {
        super.delete(payment);
    }
	
	public void update(Payment payment) throws DataAccessLayerException {
        super.saveOrUpdate(payment);
    }
	
	@SuppressWarnings("rawtypes")
	public  List findAll() throws DataAccessLayerException{
	    return super.findAll(Payment.class);
	}
	 
	public Payment find(Long id) throws DataAccessLayerException {
	    return (Payment) super.find(Payment.class, id);
	}
	 
	public Payment find(String name) throws DataAccessLayerException {
	    return (Payment) super.find(Payment.class, name);
	}
	
}
