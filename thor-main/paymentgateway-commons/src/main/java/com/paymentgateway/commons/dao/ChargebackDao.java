package com.paymentgateway.commons.dao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.transaction.SystemException;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.exception.DataAccessLayerException;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.Chargeback;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;

@Component
public class ChargebackDao extends HibernateAbstractDao {

	private static Logger logger = LoggerFactory.getLogger(ChargebackDao.class.getName());

	public ChargebackDao() {
		super();
	}

	public void create(Chargeback chargeback) throws DataAccessLayerException {
		super.save(chargeback);
	}

	public void delete(Chargeback chargeback) throws DataAccessLayerException {
		super.delete(chargeback);
	}

	public void update(Chargeback chargeback) throws DataAccessLayerException {
		super.saveOrUpdate(chargeback);
	}

	public Chargeback find(String name) throws DataAccessLayerException {
		return (Chargeback) super.find(Chargeback.class, name);
	}

	public List<Chargeback> findChargebackByPayid(String payId, String fromDate, String toDate) {
		List<Chargeback> userChargeback = new ArrayList<Chargeback>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			String sqlQuery = "select ch.* from Chargeback ch where ch.updateDate >= :fromDate and ch.updateDate <= :toDate and payId like :payId and ch.updateDate=(select max(updateDate) from Chargeback) ";
			if (payId.equals("ALL")) {
				payId = "%";
			}

			userChargeback = session.createNativeQuery(sqlQuery, Chargeback.class).setParameter("payId", payId)
					.setParameter("fromDate", fromDate).setParameter("toDate", toDate).getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return userChargeback;
	}

	public List<Chargeback> findAllChargeback(String dateFrom, String dateTo, String payId, String chargebackType,
			String chargebackStatus) {
		List<Chargeback> userChargeback = new ArrayList<Chargeback>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		StringBuilder sqlBuilder = new StringBuilder();
		Query<Chargeback> query = null;
		try {

			// String sqlQuery = "select ch.* from Chargeback ch where
			// ch.updateDate >= :fromDate and ch.updateDate <= :toDate and payId
			// like :payId and chargebackType like :chargebackType and
			// chargebackStatus like :chargebackStatus and pgRefNum like
			// :pgRefNum";

			sqlBuilder.append("from Chargeback ch where ch.updateDate >= :fromDate and ch.updateDate <= :toDate");

			if (!payId.equalsIgnoreCase("ALL")) {
				sqlBuilder.append(" and payId = :payId");
				query = session.createQuery(sqlBuilder.toString(), Chargeback.class);
				query.setParameter("payId", payId);
			} else if (!chargebackType.equals("ALL") && !chargebackStatus.equals("ALL")) {
				sqlBuilder.append(" and chargebackType = :chargebackType and chargebackStatus = :chargebackStatus");
				query = session.createQuery(sqlBuilder.toString(), Chargeback.class);
				query.setParameter("chargebackStatus", chargebackStatus);
				query.setParameter("chargebackType", chargebackType);

			} else if (!chargebackType.equals("ALL") && chargebackStatus.equals("ALL")) {

				sqlBuilder.append(" and chargebackType = :chargebackType");
				query = session.createQuery(sqlBuilder.toString(), Chargeback.class);
				query.setParameter("chargebackType", chargebackType);

			} else if (!chargebackStatus.equals("ALL") && chargebackType.equals("ALL")) {
				sqlBuilder.append(" and chargebackStatus = :chargebackStatus");
				query = session.createQuery(sqlBuilder.toString(), Chargeback.class);
				query.setParameter("chargebackStatus", chargebackStatus);

			} else {
				query = session.createQuery(sqlBuilder.toString(), Chargeback.class);
			}

			query.setParameter("fromDate", dateFrom);
			query.setParameter("toDate", dateTo);
			userChargeback = query.getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return userChargeback;
	}

	public List<Chargeback> findAllDistinctChargeback(String dateFrom, String dateTo, String payId,
			String subMerchantId, String chargebackType, String chargebackStatus, User sessionUser, String orderId) {
		List<Chargeback> userChargeback = new ArrayList<Chargeback>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		StringBuilder sqlBuilder = new StringBuilder();
		Query<Chargeback> query = null;
		try {

			Date fromDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(dateFrom);
			Date toDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(dateTo);
			// String sqlQuery = "select ch.* from Chargeback ch where
			// ch.updateDate >= :fromDate and ch.updateDate <= :toDate and payId
			// like :payId and chargebackType like :chargebackType and
			// chargebackStatus like :chargebackStatus and pgRefNum like
			// :pgRefNum";

			// sqlBuilder.append("from Chargeback ch where ch.updateDate >= :fromDate and
			// ch.updateDate <= :toDate");

			if (StringUtils.isNotBlank(orderId)) {
				if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)) {
					sqlBuilder.append("from Chargeback ch where ch.orderId = :orderId");
					query = session.createQuery(sqlBuilder.toString(), Chargeback.class);
				}else if(sessionUser.getUserType().equals(UserType.MERCHANT)) {
					sqlBuilder.append("from Chargeback ch where ch.orderId = :orderId and ch.payId = :payId");
					query = session.createQuery(sqlBuilder.toString(), Chargeback.class);
					query.setParameter("payId", payId);
				}
				query.setParameter("orderId", orderId);
				userChargeback = query.getResultList();
				tx.commit();
				if(userChargeback.stream().filter(c -> c.getStatus().equals("Closed")).count() == 1) {
					
					for(Chargeback chbk : userChargeback) {
						chbk.setCloseButtonFlag(true);
					}
				}
			} else {
				sqlBuilder.append("from Chargeback ch where ch.updateDate >= :fromDate and ch.updateDate <= :toDate");
				
				if (payId.equalsIgnoreCase("ALL")) {

					if (!chargebackType.equals("ALL") && !chargebackStatus.equals("ALL")) {
						sqlBuilder.append(
								" and chargebackType = :chargebackType and status = :chargebackStatus and updateDate in(select max(updateDate) from Chargeback group by caseId)");
						query = session.createQuery(sqlBuilder.toString(), Chargeback.class);
						query.setParameter("chargebackStatus", chargebackStatus);
						query.setParameter("chargebackType", chargebackType);

					} else if (!chargebackType.equals("ALL") && chargebackStatus.equals("ALL")) {

						sqlBuilder.append(
								" and chargebackType = :chargebackType and updateDate in(select max(updateDate) from Chargeback group by caseId)");
						query = session.createQuery(sqlBuilder.toString(), Chargeback.class);
						query.setParameter("chargebackType", chargebackType);

					} else if (!chargebackStatus.equals("ALL") && chargebackType.equals("ALL")) {
						sqlBuilder.append(
								" and status = :chargebackStatus and updateDate in(select max(updateDate) from Chargeback group by caseId)");
						query = session.createQuery(sqlBuilder.toString(), Chargeback.class);
						query.setParameter("chargebackStatus", chargebackStatus);

					} else {
						sqlBuilder.append(" and updateDate in(select max(updateDate) from Chargeback group by caseId)");
						query = session.createQuery(sqlBuilder.toString(), Chargeback.class);
					}
				} else {

					if (StringUtils.isNotBlank(subMerchantId)) {

						if (!chargebackType.equals("ALL") && !chargebackStatus.equals("ALL")
								&& !subMerchantId.equalsIgnoreCase("ALL")) {
							sqlBuilder.append(
									" and chargebackType = :chargebackType and status = :chargebackStatus and subMerchantId = :subMerchantId and payId = :payId and"
											+ " updateDate in(select max(updateDate) from Chargeback group by caseId)");
							query = session.createQuery(sqlBuilder.toString(), Chargeback.class);
							query.setParameter("chargebackStatus", chargebackStatus);
							query.setParameter("chargebackType", chargebackType);
							query.setParameter("subMerchantId", subMerchantId);
							query.setParameter("payId", payId);

						} else if (!chargebackType.equals("ALL") && chargebackStatus.equals("ALL")
								&& subMerchantId.equalsIgnoreCase("ALL")) {

							sqlBuilder.append(
									" and chargebackType = :chargebackType and payId = :payId and updateDate in(select max(updateDate) from Chargeback group by caseId)");
							query = session.createQuery(sqlBuilder.toString(), Chargeback.class);
							query.setParameter("chargebackType", chargebackType);
							query.setParameter("payId", payId);

						} else if (!chargebackStatus.equals("ALL") && chargebackType.equals("ALL")
								&& subMerchantId.equalsIgnoreCase("ALL")) {
							sqlBuilder.append(
									" and status = :chargebackStatus and payId = :payId and updateDate in(select max(updateDate) from Chargeback group by caseId)");
							query = session.createQuery(sqlBuilder.toString(), Chargeback.class);
							query.setParameter("chargebackStatus", chargebackStatus);
							query.setParameter("payId", payId);

						} else if (chargebackStatus.equals("ALL") && chargebackType.equals("ALL")
								&& !subMerchantId.equalsIgnoreCase("ALL")) {
							sqlBuilder.append(
									" and subMerchantId = :subMerchantId and payId = :payId and updateDate in(select max(updateDate) from Chargeback group by caseId)");
							query = session.createQuery(sqlBuilder.toString(), Chargeback.class);

							query.setParameter("subMerchantId", subMerchantId);
							query.setParameter("payId", payId);

						} else if (chargebackStatus.equals("ALL") && chargebackType.equals("ALL")
								&& subMerchantId.equalsIgnoreCase("ALL")) {
							sqlBuilder.append(
									" and payId = :payId and updateDate in(select max(updateDate) from Chargeback group by caseId)");
							query = session.createQuery(sqlBuilder.toString(), Chargeback.class);

							query.setParameter("payId", payId);

						} else if (chargebackStatus.equals("ALL") && !chargebackType.equals("ALL")
								&& !subMerchantId.equalsIgnoreCase("ALL")) {
							sqlBuilder.append(
									" and subMerchantId = :subMerchantId and chargebackType = :chargebackType and payId = :payId and updateDate in(select max(updateDate) from Chargeback group by caseId)");
							query = session.createQuery(sqlBuilder.toString(), Chargeback.class);
							query.setParameter("subMerchantId", subMerchantId);
							query.setParameter("chargebackType", chargebackType);
							query.setParameter("payId", payId);

						} else if (!chargebackStatus.equals("ALL") && chargebackType.equals("ALL")
								&& !subMerchantId.equalsIgnoreCase("ALL")) {
							sqlBuilder.append(
									" and subMerchantId = :subMerchantId and status = :chargebackStatus and payId = :payId and updateDate in(select max(updateDate) from Chargeback group by caseId)");
							query = session.createQuery(sqlBuilder.toString(), Chargeback.class);
							query.setParameter("subMerchantId", subMerchantId);
							query.setParameter("chargebackStatus", chargebackStatus);
							query.setParameter("payId", payId);

						} else if (!chargebackStatus.equals("ALL") && !chargebackType.equals("ALL")
								&& subMerchantId.equalsIgnoreCase("ALL")) {
							sqlBuilder.append(
									" and status = :chargebackStatus and chargebackType = :chargebackType and payId = :payId and updateDate in(select max(updateDate) from Chargeback group by caseId)");
							query = session.createQuery(sqlBuilder.toString(), Chargeback.class);
							query.setParameter("chargebackStatus", chargebackStatus);
							query.setParameter("chargebackType", chargebackType);
							query.setParameter("payId", payId);

						}
					} else {

						if (!chargebackType.equals("ALL") && !chargebackStatus.equals("ALL")) {
							sqlBuilder.append(
									" and chargebackType = :chargebackType and status = :chargebackStatus and payId = :payId and updateDate in(select max(updateDate) from Chargeback group by caseId)");
							query = session.createQuery(sqlBuilder.toString(), Chargeback.class);
							query.setParameter("chargebackStatus", chargebackStatus);
							query.setParameter("chargebackType", chargebackType);
							query.setParameter("payId", payId);

						} else if (!chargebackType.equals("ALL") && chargebackStatus.equals("ALL")) {

							sqlBuilder.append(
									" and chargebackType = :chargebackType and payId = :payId and updateDate in(select max(updateDate) from Chargeback group by caseId)");
							query = session.createQuery(sqlBuilder.toString(), Chargeback.class);
							query.setParameter("chargebackType", chargebackType);
							query.setParameter("payId", payId);

						} else if (!chargebackStatus.equals("ALL") && chargebackType.equals("ALL")) {
							sqlBuilder.append(
									" and status = :chargebackStatus and payId = :payId and updateDate in(select max(updateDate) from Chargeback group by caseId)");
							query = session.createQuery(sqlBuilder.toString(), Chargeback.class);
							query.setParameter("chargebackStatus", chargebackStatus);
							query.setParameter("payId", payId);

						} else {
							sqlBuilder.append(
									" and payId = :payId and updateDate in(select max(updateDate) from Chargeback group by caseId)");
							query = session.createQuery(sqlBuilder.toString(), Chargeback.class);
							query.setParameter("payId", payId);
						}
					}
				}
				query.setParameter("fromDate", fromDate);
				query.setParameter("toDate", toDate);
				userChargeback = query.getResultList();
				tx.commit();
			}
			
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} catch (ParseException e) {
			e.printStackTrace();
		} finally {
			autoClose(session);
		}
		return userChargeback;
	}

	public Chargeback findByCaseId(String caseId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		Chargeback responseUser = null;
		try {

			responseUser = (Chargeback) session
					.createQuery("from Chargeback CH where CH.caseId = :caseId order by CH.updateDate desc")
					.setParameter("caseId", caseId).setCacheable(true).setMaxResults(1).uniqueResult();
			tx.commit();
			return responseUser;
		} catch (NoResultException noResultException) {
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			logger.error("error : " , hibernateException);
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return responseUser;
	}

	public Chargeback findByTxnId(String txnID, String status, String chargebackType) {

		Chargeback chargeback = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			if (chargebackType.equals("Pre Arbitration")) {
				chargeback = (Chargeback) session
						.createQuery("from Chargeback CH where CH.transactionId = :txnID and CH.status != 'Closed' ")
						.setParameter("txnID", txnID).setCacheable(true).getSingleResult();
				tx.commit();
				return chargeback;
			} else {
				chargeback = (Chargeback) session.createQuery("from Chargeback CH where CH.transactionId = :txnID ")
						.setParameter("txnID", txnID).setCacheable(true).getSingleResult();
				tx.commit();
				return chargeback;
			}
		} catch (NoResultException noResultException) {
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			logger.error("error : " , hibernateException);
			handleException(hibernateException, tx);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			autoClose(session);
		}
		return chargeback;
	}

	public void updateComment(String caseId, byte[] comments) throws SystemException {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			int updateStatus = session
					.createQuery("update Chargeback C set C.comments = :comments where C.caseId = :caseId")
					.setParameter("caseId", caseId).setParameter("comments", comments).executeUpdate();
			tx.commit();
			if (1 != updateStatus) {
				throw new SystemException(
						"Error updating comments with Chargeback: " + caseId + ErrorType.DATABASE_ERROR);
			}
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
	}

	public void updateStatus(String chargebackStatus, String caseId) throws SystemException {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			int updateStatus = session
					.createQuery(
							"update Chargeback C set C.chargebackStatus = :chargebackStatus where C.caseId = :caseId ")
					.setParameter("caseId", caseId).setParameter("chargebackStatus", chargebackStatus).executeUpdate();
			tx.commit();
			if (1 != updateStatus) {
				throw new SystemException(
						"Error updating Status with Chargeback: " + caseId + ErrorType.DATABASE_ERROR);
			}
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
	}// close updateStatus();

	public void UpdateData(Chargeback chargeback) {
		super.save(chargeback);

	}

	public Chargeback findbyId(String caseId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		Chargeback responseUser = null;

		try {

			responseUser = (Chargeback) session
					.createQuery("from Chargeback CH where CH.caseId = :caseId  order by CH.updateDate desc")
					.setParameter("caseId", caseId).setCacheable(true).setMaxResults(1).uniqueResult();
			tx.commit();
			return responseUser;
		} catch (NoResultException noResultException) {
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			logger.error("error : " , hibernateException);
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return responseUser;
	}
	
	public Chargeback findDuplicateChargeback(String orderId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		Chargeback responseUser = null;

		try {

			responseUser = (Chargeback) session
					.createQuery("from Chargeback CH where CH.orderId = :orderId and  status ='Open' order by CH.updateDate desc")
					.setParameter("orderId", orderId).setCacheable(true).setMaxResults(1).uniqueResult();
			tx.commit();
			return responseUser;
		} catch (NoResultException noResultException) {
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			logger.error("error : " , hibernateException);
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return responseUser;
	}

	@SuppressWarnings("unchecked")
	public List<Chargeback> chargebackList(String caseId) {
		List<Chargeback> chargebackslist = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		chargebackslist = (List<Chargeback>) session
				.createQuery("Select updateDate,chargebackStatus  from Chargeback ch where ch.caseId = :caseId")
				.setParameter("caseId", caseId).getResultList();
		tx.commit();
		return chargebackslist;
	}
	@SuppressWarnings("unchecked")
	public List<Chargeback> chargebackByList(String caseId) {
		List<Chargeback> chargebackslist = new ArrayList<Chargeback>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		
		List<Object[]> chargebackslistRaw = session.createQuery("Select updateDate,chargebackStatus  from Chargeback ch where ch.caseId = :caseId order by updateDate")
				.setParameter("caseId", caseId).getResultList();
		
		
		for(Object[] obj : chargebackslistRaw) {
			
			Chargeback chrbck = new Chargeback();
			chrbck.setUpdateDate((Date)obj[0]);
			chrbck.setChargebackStatus((String)obj[1]);
			
			chargebackslist.add(chrbck);
		}
		tx.commit();
		return chargebackslist;
	}
	@SuppressWarnings("unchecked")
	public List<Chargeback> findBypgRefNum(String payId, String pgRefNum, String chargebackType) {

		List<Chargeback> chargeback = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			chargeback = (List<Chargeback>) session.createQuery(
					" from Chargeback CH where CH.payId = :payId and CH.pgRefNum = :pgRefNum order by updateDate")
					.setParameter("pgRefNum", pgRefNum)
					.setParameter("payId", payId).getResultList();
			tx.commit();
			return chargeback;

		} catch (NoResultException noResultException) {
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			logger.error("error : " , hibernateException);
			handleException(hibernateException, tx);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			autoClose(session);
		}
		return chargeback;
	}

	public List<Chargeback> fetchRefundedChargebackCases() {
		List<Chargeback> chargeback = new ArrayList<Chargeback>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			String sqlQuery = "select max(updateDate), Id, pgRefNum, status from Chargeback WHERE "
					+ "NOT status = 'Closed' and status = 'Refunded' group by pgRefNum, status, Id";
			chargeback = session.createNativeQuery(sqlQuery, Chargeback.class).getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return chargeback;
	}

	public void closeChargebackCase(String Id) {
		Date updatedDate = new Date();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			Chargeback chargeback = session.load(Chargeback.class, Id);
			chargeback.setStatus("Closed");
			chargeback.setUpdateDate(updatedDate);
			save(chargeback);
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
	}
	
	
	public List<Chargeback> findChargebackForPayout(String payId, String subMerchantId, String dateFrom, String dateTo) {
		List<Chargeback> userChargeback = new ArrayList<Chargeback>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		Query<Chargeback> query = null;
		
		try {
			Date fromDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateFrom);
			Date toDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateTo);
			
			if(StringUtils.isNotBlank(subMerchantId) && !subMerchantId.equalsIgnoreCase("ALL")) {
				
				String sqlQuery = "from Chargeback where updateDate >= :fromDate and updateDate <= :toDate and payId = :payId and subMerchantId = :subMerchantId and updateDate in(select max(updateDate) from Chargeback group by caseId) ";
				query = session.createQuery(sqlQuery, Chargeback.class);
				query.setParameter("payId", payId);
				query.setParameter("subMerchantId", subMerchantId);			
			}else if(!payId.equalsIgnoreCase("ALL")){
				String sqlQuery = "from Chargeback where updateDate >= :fromDate and updateDate <= :toDate and payId = :payId and updateDate in(select max(updateDate) from Chargeback group by caseId) ";
				query = session.createQuery(sqlQuery, Chargeback.class);
				query.setParameter("payId", payId);
			}else {
				String sqlQuery = "from Chargeback where updateDate >= :fromDate and updateDate <= :toDate and updateDate in(select max(updateDate) from Chargeback group by caseId) ";
				query = session.createQuery(sqlQuery, Chargeback.class);
			}
			query.setParameter("fromDate", fromDate);
			query.setParameter("toDate", toDate);
			userChargeback = query.getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} catch (ParseException e) {
			e.printStackTrace();
		} finally {
			autoClose(session);
		}
		return userChargeback;
	}
	
	public Chargeback findChargebackLastStatusByOrderId(String payId, String orderId) {

		Chargeback chargebackList = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			if (StringUtils.isBlank(payId)) {
				chargebackList = (Chargeback) session.createQuery(
						"from Chargeback CH where CH.updateDate = (select max(updateDate) from Chargeback c where c.orderId = :orderId order by caseId)")
						.setParameter("orderId", orderId).getSingleResult();
			} else {
				chargebackList = (Chargeback) session.createQuery(
						"from Chargeback CH where CH.updateDate = (select max(updateDate) from Chargeback c where c.payId = :payId and c.orderId = :orderId order by caseId)")
						.setParameter("orderId", orderId).setParameter("payId", payId).getSingleResult();
			}
			tx.commit();
			return chargebackList;

		} catch (NoResultException noResultException) {
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			logger.error("error : " , hibernateException);
			handleException(hibernateException, tx);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			autoClose(session);
		}
		return chargebackList;
	}
}
