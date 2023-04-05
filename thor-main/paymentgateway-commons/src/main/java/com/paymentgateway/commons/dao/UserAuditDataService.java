package com.paymentgateway.commons.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.UserAudit;
import com.paymentgateway.commons.util.UserStatusType;

@Service
public class UserAuditDataService {

	private static Logger logger = LoggerFactory.getLogger(UserAuditDataService.class.getName());

	public UserAuditDataService() {
	}

	public List<UserAudit> getAllUserAuditList(String industryCategory, String status) throws SystemException {
		List<UserAudit> merchants = new ArrayList<UserAudit>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		Query<UserAudit> query = null;

		List<UserAudit> mpaMerchants = new ArrayList<UserAudit>();
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("from UserAudit");
		try {

			if (industryCategory.equalsIgnoreCase("ALL") && status.equalsIgnoreCase("ALL")) {
				query = session.createQuery(sqlBuilder.toString(), UserAudit.class);

			} else if (industryCategory.equalsIgnoreCase("ALL") && !status.equalsIgnoreCase("ALL")) {
				sqlBuilder.append(" u where u.userStatus = :userStatus");
				query = session.createQuery(sqlBuilder.toString(), UserAudit.class);
				if (status.equalsIgnoreCase("ACTIVE"))
					query.setParameter("userStatus", UserStatusType.ACTIVE);
				if (status.equalsIgnoreCase("PENDING"))
					query.setParameter("userStatus", UserStatusType.PENDING);
				if (status.equalsIgnoreCase("TRANSACTION_BLOCKED"))
					query.setParameter("userStatus", UserStatusType.TRANSACTION_BLOCKED);
				if (status.equalsIgnoreCase("SUSPENDED"))
					query.setParameter("userStatus", UserStatusType.SUSPENDED);
				if (status.equalsIgnoreCase("TERMINATED"))
					query.setParameter("userStatus", UserStatusType.TERMINATED);
				if (status.equalsIgnoreCase("APPROVED"))
					query.setParameter("userStatus", UserStatusType.APPROVED);
				if (status.equalsIgnoreCase("REJECTED"))
					query.setParameter("userStatus", UserStatusType.REJECTED);

			} else if (!industryCategory.equalsIgnoreCase("ALL") && status.equalsIgnoreCase("ALL")) {
				sqlBuilder.append(" u where u.industryCategory = :industryCategory");
				query = session.createQuery(sqlBuilder.toString(), UserAudit.class);
				query.setParameter("industryCategory", industryCategory);

			} else if (!industryCategory.equalsIgnoreCase("ALL") && !status.equalsIgnoreCase("ALL")) {
				sqlBuilder.append(" u where u.industryCategory = :industryCategory and u.userStatus = :userStatus");

				query = session.createQuery(sqlBuilder.toString(), UserAudit.class);
				query.setParameter("industryCategory", industryCategory);
				if (status.equalsIgnoreCase("ACTIVE"))
					query.setParameter("userStatus", UserStatusType.ACTIVE);
				if (status.equalsIgnoreCase("PENDING"))
					query.setParameter("userStatus", UserStatusType.PENDING);
				if (status.equalsIgnoreCase("TRANSACTION_BLOCKED"))
					query.setParameter("userStatus", UserStatusType.TRANSACTION_BLOCKED);
				if (status.equalsIgnoreCase("SUSPENDED"))
					query.setParameter("userStatus", UserStatusType.SUSPENDED);
				if (status.equalsIgnoreCase("TERMINATED"))
					query.setParameter("userStatus", UserStatusType.TERMINATED);
				if (status.equalsIgnoreCase("APPROVED"))
					query.setParameter("userStatus", UserStatusType.APPROVED);
				if (status.equalsIgnoreCase("REJECTED"))
					query.setParameter("userStatus", UserStatusType.REJECTED);
			}
			mpaMerchants = query.getResultList();
			tx.commit();

			for (UserAudit user : mpaMerchants) {
				UserAudit userAduitData = new UserAudit();

				userAduitData.setId(user.getId());
				userAduitData.setPayId(user.getPayId());
				userAduitData.setBusinessName(user.getBusinessName());
				userAduitData.setEmailId(user.getEmailId());
				userAduitData.setUserStatus(user.getUserStatus());
				userAduitData.setMobile(user.getMobile());
				userAduitData.setMpaDataUpdateDate(user.getMpaDataUpdateDate());
				userAduitData.setMpaDataUpdatedBy(user.getMpaDataUpdatedBy());
				userAduitData.setMpaDataUpdatedByEmail(user.getMpaDataUpdatedByEmail());
				userAduitData.setMpaDataUpdatedByUserType(user.getMpaDataUpdatedByUserType());

				merchants.add(userAduitData);
			}
		} catch (Exception exception) {
			logger.error("Database error", exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, ErrorType.DATABASE_ERROR.getResponseMessage());
		}
		return merchants;
	}
}