package com.paymentgateway.commons.rule;

import java.util.List;

import com.paymentgateway.commons.dao.HibernateAbstractDao;
import com.paymentgateway.commons.exception.DataAccessLayerException;

public class RuleMapDao extends HibernateAbstractDao {

	public RuleMapDao() {
		super();
	}

	public void create(RuleMap rule) throws DataAccessLayerException {
		super.saveOrUpdate(rule);
	}

	public void delete(RuleMap rule) throws DataAccessLayerException {
		super.delete(rule);
	}

	public RuleMap find(Long id) throws DataAccessLayerException {
		return (RuleMap) super.find(RuleMap.class, id);
	}

	public RuleMap find(String name) throws DataAccessLayerException {
		return (RuleMap) super.find(RuleMap.class, name);
	}

	@SuppressWarnings("rawtypes")
	public List findAll() throws DataAccessLayerException {
		return super.findAll(RuleMap.class);
	}

	public void update(RuleMap rule) throws DataAccessLayerException {
		super.saveOrUpdate(rule);
	}

}
