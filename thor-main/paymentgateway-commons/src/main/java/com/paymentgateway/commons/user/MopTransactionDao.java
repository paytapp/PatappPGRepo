package com.paymentgateway.commons.user;

import java.util.List;

import org.springframework.stereotype.Component;

import com.paymentgateway.commons.dao.HibernateAbstractDao;
import com.paymentgateway.commons.exception.DataAccessLayerException;

@Component
public class MopTransactionDao extends HibernateAbstractDao{
	
	public MopTransactionDao() {
        super();
    }

	public void create(MopTransaction mopTransaction) throws DataAccessLayerException {
        super.save(mopTransaction);
    }
	
	public void delete(MopTransaction mopTransaction) throws DataAccessLayerException {
        super.save(mopTransaction);
    }
	
	public void update(MopTransaction mopTransaction) throws DataAccessLayerException {
        super.saveOrUpdate(mopTransaction);
    }
	
	@SuppressWarnings("rawtypes")
	public  List findAll() throws DataAccessLayerException{
	    return super.findAll(MopTransaction.class);
	}
	 
	public MopTransaction find(Long id) throws DataAccessLayerException {
	    return (MopTransaction) super.find(MopTransaction.class, id);
	}
	 
	public MopTransaction find(String name) throws DataAccessLayerException {
	    return (MopTransaction) super.find(MopTransaction.class, name);
	}

}
