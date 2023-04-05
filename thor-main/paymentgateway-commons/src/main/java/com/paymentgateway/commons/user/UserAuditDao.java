package com.paymentgateway.commons.user;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.dao.HibernateAbstractDao;
import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.exception.DataAccessLayerException;

@Component
public class UserAuditDao extends HibernateAbstractDao {

	public UserAuditDao() {
		super();
	}

	public void create(UserAudit userAudit) throws DataAccessLayerException {
		super.save(userAudit);
	}

	public void delete(UserAudit userAudit) throws DataAccessLayerException {
		super.delete(userAudit);
	}

	public void update(UserAudit userAudit) throws DataAccessLayerException {
		super.saveOrUpdate(userAudit);
	}

	@SuppressWarnings("rawtypes")
	public List findAll() throws DataAccessLayerException {
		return super.findAll(UserAudit.class);
	}

	public UserAudit find(Long id) throws DataAccessLayerException {
		return (UserAudit) super.find(UserAudit.class, id);
	}

	public UserAudit find(String name) throws DataAccessLayerException {
		return (UserAudit) super.find(UserAudit.class, name);
	}

	public void updateUserAudit(UserAudit userAudit) throws DataAccessLayerException {
		super.save(userAudit);
	}

	public List<UserAudit> getUserAuditDataById(String Id) {

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<UserAudit> users = null;

		try {
			Query<UserAudit> query = session.createQuery("from UserAudit U where U.id = '" + Id + "'", UserAudit.class);
			users = query.getResultList();
			tx.commit();

		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return users;
	}

}
