package com.paymentgateway.commons.rule;

import java.util.List;

import com.paymentgateway.commons.dao.HibernateAbstractDao;
import com.paymentgateway.commons.exception.DataAccessLayerException;

public class RuleDao extends HibernateAbstractDao {

	public RuleDao() {
		super();
	}

	public void create(Rule rule) throws DataAccessLayerException {
		super.saveOrUpdate(rule);
	}

	public void delete(Rule rule) throws DataAccessLayerException {
		super.delete(rule);
	}

	public Rule find(Long id) throws DataAccessLayerException {
		return (Rule) super.find(Rule.class, id);
	}

	public Rule find(String name) throws DataAccessLayerException {
		return (Rule) super.find(Rule.class, name);
	}

	@SuppressWarnings("rawtypes")
	public List findAll() throws DataAccessLayerException {
		return super.findAll(Rule.class);
	}

	public void update(Rule rule) throws DataAccessLayerException {
		super.saveOrUpdate(rule);
	}

}
