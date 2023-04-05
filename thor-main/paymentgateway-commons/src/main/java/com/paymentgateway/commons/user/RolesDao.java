package com.paymentgateway.commons.user;

import java.util.List;

import org.springframework.stereotype.Component;

import com.paymentgateway.commons.dao.HibernateAbstractDao;
import com.paymentgateway.commons.exception.DataAccessLayerException;


@Component
public class RolesDao extends HibernateAbstractDao {

	public RolesDao() {
        super();
    }

	public void create(Roles roles) throws DataAccessLayerException {
        super.saveOrUpdate(roles);
    }
	
	 public void delete(Roles roles) throws DataAccessLayerException {
	        super.delete(roles);
	    }
	
	 public Roles find(Long id) throws DataAccessLayerException {
	        return (Roles) super.find(Roles.class, id);
	    }

	 public Roles find(String name) throws DataAccessLayerException {
	        return (Roles) super.find(Roles.class, name);
	    }
	 @SuppressWarnings("rawtypes")
	 public  List findAll() throws DataAccessLayerException{
	        return super.findAll(Roles.class);
	    }
	 
	 public void update(Roles roles) throws DataAccessLayerException {
	        super.saveOrUpdate(roles);
	    }
	
}
