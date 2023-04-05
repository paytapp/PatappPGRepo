package com.paymentgateway.commons.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;

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
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.MPAMerchant;
import com.paymentgateway.commons.user.MerchantDetails;
import com.paymentgateway.commons.util.MPAStatusType;
import com.paymentgateway.commons.util.UserStatusType;

@Component
public class MPAMerchantDao extends HibernateAbstractDao{
	
	private static Logger logger = LoggerFactory.getLogger(MPAMerchantDao.class.getName());
	
	public MPAMerchantDao() {
		super();
	}
	
	public void create(MPAMerchant mpaMerchant) throws DataAccessLayerException {
		super.save(mpaMerchant);
	}

	public MPAMerchant find(String name) throws DataAccessLayerException {
		return (MPAMerchant) super.find(MPAMerchant.class, name);
	}

	@SuppressWarnings("rawtypes")
	public List findAll() throws DataAccessLayerException {
		return super.findAll(MPAMerchant.class);
	}

	public void update(MPAMerchant mpaMerchant) throws DataAccessLayerException {
		super.saveOrUpdate(mpaMerchant);
	}
	
	public MPAMerchant findByPayId(String payId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		MPAMerchant mpaMerchant = null;
		try {
			mpaMerchant =  (MPAMerchant) session.createQuery("from MPAMerchant m where m.payId = :payId")
											  .setParameter("payId", payId).setCacheable(true)
			                                  .getSingleResult();
			
			tx.commit();
			
		}catch (NoResultException noResultException){
					return null;
		}catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			logger.error("error : " , hibernateException);
			handleException(hibernateException,tx);
		} 
		catch (Exception e) {
			logger.error("error : " , e);
		}
		finally {
			autoClose(session);
		}
		return mpaMerchant;
	}
	
	@SuppressWarnings("unchecked")
	public List<MPAMerchant> getMerchantsByIndustryCatagory(String industryCategory){
		List<MPAMerchant> mpaMerchantList = new ArrayList<MPAMerchant>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			mpaMerchantList = session.createQuery("from MPAMerchant m where m.industryCategory = :industryCategory")
											  .setParameter("industryCategory", industryCategory)
			                                  .getResultList();
			
			tx.commit();
		
		}catch (NoResultException noResultException){
					return null;
		}catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			logger.error("error : " , hibernateException);
			handleException(hibernateException,tx);
		} 
		catch (Exception e) {
			logger.error("error " , e);
		}
		finally {
			autoClose(session);
		}
		return mpaMerchantList;
	}
	
	public List<MerchantDetails> getAllMPAMerchants(String industryCategory, String status, String statusBy) throws SystemException {
		List<MerchantDetails> merchants = new ArrayList<MerchantDetails>();
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");  
		
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		Query<MPAMerchant> query = null;
		
		List<MPAMerchant> mpaMerchants = new ArrayList<MPAMerchant>();
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("from MPAMerchant");
		try {

			if(statusBy.equalsIgnoreCase("ALL")) {
				
				if (industryCategory.equalsIgnoreCase("ALL") && status.equalsIgnoreCase("ALL")) {
					query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
					
				} else if (industryCategory.equalsIgnoreCase("ALL") && !status.equalsIgnoreCase("ALL")) {
					sqlBuilder.append(" u where u.userStatus = :userStatus");
					query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
					if(status.equalsIgnoreCase("APPROVED"))
						query.setParameter("userStatus", MPAStatusType.APPROVED.getStatusCode());
					if(status.equalsIgnoreCase("REJECTED"))
						query.setParameter("userStatus", MPAStatusType.REJECTED.getStatusCode());
					
				} else if (!industryCategory.equalsIgnoreCase("ALL") && status.equalsIgnoreCase("ALL")) {
					sqlBuilder.append(" u where u.industryCategory = :industryCategory");
					query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
					query.setParameter("industryCategory", industryCategory);
					
				}else if (!industryCategory.equalsIgnoreCase("ALL") && !status.equalsIgnoreCase("ALL")) {
					sqlBuilder.append(" u where u.industryCategory = :industryCategory and u.userStatus = :userStatus");
					
					query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
					query.setParameter("industryCategory", industryCategory);
					if(status.equalsIgnoreCase("APPROVED"))
						query.setParameter("userStatus", MPAStatusType.APPROVED.getStatusCode());
					if(status.equalsIgnoreCase("REJECTED"))
						query.setParameter("userStatus", MPAStatusType.REJECTED.getStatusCode());
				}
				
			} else {
					
					if(statusBy.equalsIgnoreCase("Checker")) {
						if(industryCategory.equalsIgnoreCase("ALL") && status.equalsIgnoreCase("ALL") ){
							sqlBuilder.append(" u where u.checkerStatus = :checkerStatus");
							
							query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
							query.setParameter("checkerStatus", status);
							
						}else if (industryCategory.equalsIgnoreCase("ALL") && !status.equalsIgnoreCase("ALL")) {
							sqlBuilder.append(" u where u.checkerStatus = :checkerStatus and u.userStatus = :userStatus");
							
							query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
							query.setParameter("checkerStatus", status);
							if(status.equalsIgnoreCase("APPROVED"))
								query.setParameter("userStatus", MPAStatusType.APPROVED.getStatusCode());
							if(status.equalsIgnoreCase("REJECTED"))
								query.setParameter("userStatus", MPAStatusType.REJECTED.getStatusCode());				
		
						} else if (!industryCategory.equalsIgnoreCase("ALL") && status.equalsIgnoreCase("ALL")) {
							sqlBuilder.append(" u where u.checkerStatus = :checkerStatus and u.industryCategory = :industryCategory");
							
							query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
							query.setParameter("checkerStatus", status);
							query.setParameter("industryCategory", industryCategory);
		
						}else if (!industryCategory.equalsIgnoreCase("ALL") && !status.equalsIgnoreCase("ALL")) {
							
							sqlBuilder.append(" u where u.checkerStatus = :checkerStatus and u.industryCategory = :industryCategory and u.userStatus = :userStatus");
							
							query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
							query.setParameter("checkerStatus", status);
							query.setParameter("industryCategory", industryCategory);
							if(status.equalsIgnoreCase("APPROVED"))
								query.setParameter("userStatus", MPAStatusType.APPROVED.getStatusCode());
							if(status.equalsIgnoreCase("REJECTED"))
								query.setParameter("userStatus", MPAStatusType.REJECTED.getStatusCode());					
						}
					}else if(statusBy.equalsIgnoreCase("Maker")) {
						if(industryCategory.equalsIgnoreCase("ALL") && status.equalsIgnoreCase("ALL") ){
							sqlBuilder.append(" u where u.makerStatus = :makerStatus");
							
							query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
							query.setParameter("makerStatus", status);
							
						}else if (industryCategory.equalsIgnoreCase("ALL") && !status.equalsIgnoreCase("ALL")) {
							sqlBuilder.append(" u where u.makerStatus = :makerStatus and u.userStatus = :userStatus");
							
							query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
							query.setParameter("makerStatus", status);
							if(status.equalsIgnoreCase("APPROVED"))
								query.setParameter("userStatus", MPAStatusType.APPROVED.getStatusCode());
							if(status.equalsIgnoreCase("REJECTED"))
								query.setParameter("userStatus", MPAStatusType.REJECTED.getStatusCode());						
		
						} else if (!industryCategory.equalsIgnoreCase("ALL") && status.equalsIgnoreCase("ALL")) {
							sqlBuilder.append(" u where u.makerStatus = :makerStatus and u.industryCategory = :industryCategory");
							
							query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
							query.setParameter("makerStatus", status);
							query.setParameter("industryCategory", industryCategory);
		
						}else if (!industryCategory.equalsIgnoreCase("ALL") && !status.equalsIgnoreCase("ALL")) {
							
							sqlBuilder.append(" u where u.makerStatus = :makerStatus and u.industryCategory = :industryCategory and u.userStatus = :userStatus");
							
							query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
							query.setParameter("makerStatus", status);
							query.setParameter("industryCategory", industryCategory);
							if(status.equalsIgnoreCase("APPROVED"))
								query.setParameter("userStatus", MPAStatusType.APPROVED.getStatusCode());
							if(status.equalsIgnoreCase("REJECTED"))
								query.setParameter("userStatus", MPAStatusType.REJECTED.getStatusCode());						
						}
					}else if(statusBy.equalsIgnoreCase("Admin")) {
						if(industryCategory.equalsIgnoreCase("ALL") && status.equalsIgnoreCase("ALL") ){
							sqlBuilder.append(" u where u.adminStatus = :adminStatus");
							
							query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
							query.setParameter("adminStatus", status);
							
						}else if (industryCategory.equalsIgnoreCase("ALL") && !status.equalsIgnoreCase("ALL")) {
							sqlBuilder.append(" u where u.adminStatus = :adminStatus and u.userStatus = :userStatus");
							
							query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
							query.setParameter("adminStatus", status);
							if(status.equalsIgnoreCase("APPROVED"))
								query.setParameter("userStatus", MPAStatusType.APPROVED.getStatusCode());
							if(status.equalsIgnoreCase("REJECTED"))
								query.setParameter("userStatus", MPAStatusType.REJECTED.getStatusCode());						
		
						} else if (!industryCategory.equalsIgnoreCase("ALL") && status.equalsIgnoreCase("ALL")) {
							sqlBuilder.append(" u where u.adminStatus = :adminStatus and u.industryCategory = :industryCategory");
							
							query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
							query.setParameter("adminStatus", status);
							query.setParameter("industryCategory", industryCategory);
		
						}else if (!industryCategory.equalsIgnoreCase("ALL") && !status.equalsIgnoreCase("ALL")) {
							
							sqlBuilder.append(" u where u.adminStatus = :adminStatus and u.industryCategory = :industryCategory and u.userStatus = :userStatus");
							
							query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
							query.setParameter("adminStatus", status);
							query.setParameter("industryCategory", industryCategory);
							if(status.equalsIgnoreCase("APPROVED"))
								query.setParameter("userStatus", MPAStatusType.APPROVED.getStatusCode());
							if(status.equalsIgnoreCase("REJECTED"))
								query.setParameter("userStatus", MPAStatusType.REJECTED.getStatusCode());						
						}
					}
			}
			mpaMerchants = query.getResultList();
			tx.commit();
			
			for(MPAMerchant mpa : mpaMerchants) {
				String registrationDate = formatter.format(mpa.getRegistrationDate());
				MerchantDetails merchant = new MerchantDetails();
				merchant.setPayId(mpa.getPayId());
				merchant.setBusinessName(mpa.getBusinessName());
				merchant.setEmailId(mpa.getEmailId());
				merchant.setMobile(mpa.getMobile());
				merchant.setRegistrationDate(registrationDate);
				merchant.setUserType(mpa.getUserType().name());
				merchant.setCheckerName(mpa.getCheckerName());
				merchant.setMakerName(mpa.getMakerName());
				merchant.setMakerStatus(mpa.getMakerStatus());
				merchant.setCheckerStatus(mpa.getCheckerStatus());
				merchant.setMakerStatusUpDate(mpa.getMakerStatusUpDate());
				merchant.setCheckerStatusUpDate(mpa.getCheckerStatusUpDate());
				merchant.setCheckerComments(mpa.getCheckerComments());
				merchant.setMakerComments(mpa.getMakerComments());
				String status1 = mpa.getUserStatus();

			if (status != null) {
				UserStatusType userStatus = UserStatusType.valueOf(status1);
				merchant.setStatus(userStatus);
			}
			merchants.add(merchant);
		}
			
		}catch (Exception exception) {
			logger.error("Database error", exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, ErrorType.DATABASE_ERROR.getResponseMessage());
		}
		return merchants;
	}
	
	public List<MerchantDetails> getMPAMerchantsBasedOnCheckerMaker(String status,String statusBy,String industryCategory ,String permissionType,String payId) throws SystemException {
		List<MerchantDetails> merchants = new ArrayList<MerchantDetails>();

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<MPAMerchant> mpaMerchants = new ArrayList<MPAMerchant>();
		Query<MPAMerchant> query = null;
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("from MPAMerchant u where ");
		
		try {
			
			if(status.equalsIgnoreCase("ALL") && industryCategory.equalsIgnoreCase("ALL") && statusBy.equalsIgnoreCase("ALL")) {
				
					sqlBuilder.append("u.checkerPayId = :payId or u.makerPayId = :payId");
					query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
					query.setParameter("payId", payId);
				
			}else if(!status.equalsIgnoreCase("ALL") && industryCategory.equalsIgnoreCase("ALL") && statusBy.equalsIgnoreCase("ALL")){
					sqlBuilder.append("(u.checkerPayId = :payId or u.makerPayId = :payId) and u.userStatus = :userStatus");
					query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
					query.setParameter("payId", payId);
					if(status.equalsIgnoreCase("APPROVED"))
						query.setParameter("userStatus", MPAStatusType.APPROVED.getStatusCode());
					if(status.equalsIgnoreCase("REJECTED"))
						query.setParameter("userStatus", MPAStatusType.REJECTED.getStatusCode());
					
			}else if(!status.equalsIgnoreCase("ALL") && !industryCategory.equalsIgnoreCase("ALL") && statusBy.equalsIgnoreCase("ALL")){
					sqlBuilder.append("(u.checkerPayId = :payId or u.makerPayId = :payId) and u.userStatus = :userStatus and u.industryCategory = :industryCategory");
					query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
					query.setParameter("payId", payId);
					if(status.equalsIgnoreCase("APPROVED"))
						query.setParameter("userStatus", MPAStatusType.APPROVED.getStatusCode());
					if(status.equalsIgnoreCase("REJECTED"))
						query.setParameter("userStatus", MPAStatusType.REJECTED.getStatusCode());
					query.setParameter("industryCategory", industryCategory);
					
			}else if(status.equalsIgnoreCase("ALL") && !industryCategory.equalsIgnoreCase("ALL") && statusBy.equalsIgnoreCase("ALL")){
					sqlBuilder.append("(u.checkerPayId = :payId or u.makerPayId = :payId) and u.industryCategory = :industryCategory");
					
					query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
					query.setParameter("payId", payId);
					query.setParameter("industryCategory", industryCategory);
					
			}else if(!status.equalsIgnoreCase("ALL") && !industryCategory.equalsIgnoreCase("ALL") && !statusBy.equalsIgnoreCase("ALL")){
				if(statusBy.equalsIgnoreCase("Checker")) {
					sqlBuilder.append("(u.checkerPayId = :payId or u.makerPayId = :payId) and u.userStatus = :userStatus and u.industryCategory = :industryCategory and u.checkerStatus = :checkerStatus");
					
					query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
					query.setParameter("payId", payId);
					if(status.equalsIgnoreCase("APPROVED"))
						query.setParameter("userStatus", MPAStatusType.APPROVED.getStatusCode());
					if(status.equalsIgnoreCase("REJECTED"))
						query.setParameter("userStatus", MPAStatusType.REJECTED.getStatusCode());
					query.setParameter("industryCategory", industryCategory);
					query.setParameter("checkerStatus", status);
					
				}else if(statusBy.equalsIgnoreCase("Maker")) {
					sqlBuilder.append("(u.checkerPayId = :payId or u.makerPayId = :payId) and u.userStatus = :userStatus and u.industryCategory = :industryCategory and u.makerStatus = :makerStatus");
					
					query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
					query.setParameter("payId", payId);
					if(status.equalsIgnoreCase("APPROVED"))
						query.setParameter("userStatus", MPAStatusType.APPROVED.getStatusCode());
					if(status.equalsIgnoreCase("REJECTED"))
						query.setParameter("userStatus", MPAStatusType.REJECTED.getStatusCode());
					query.setParameter("industryCategory", industryCategory);
					query.setParameter("makerStatus", status);
					
				}
//				else if(statusBy.equalsIgnoreCase("Checker")) {
//					sqlBuilder.append("(u.checkerPayId = :payId or u.makerPayId = :payId) and u.userStatus = :userStatus and u.industryCategory = :industryCategory and u.checkerStatus = :checkerStatus");
//					
//					query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
//					query.setParameter("makerPayId", payId);
//					if(status.equalsIgnoreCase("APPROVED"))
//						query.setParameter("userStatus", MPAStatusType.APPROVED.getStatusCode());
//					if(status.equalsIgnoreCase("REJECTED"))
//						query.setParameter("userStatus", MPAStatusType.REJECTED.getStatusCode());
//					query.setParameter("industryCategory", industryCategory);
//					query.setParameter("checkerStatus", status);
//					
//				}
				else if(statusBy.equalsIgnoreCase("Admin")) {
					sqlBuilder.append("(u.checkerPayId = :payId or u.makerPayId = :payId) and u.userStatus = :userStatus and u.industryCategory = :industryCategory and u.adminStatus = :adminStatus");
					
					query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
					query.setParameter("payId", payId);
					if(status.equalsIgnoreCase("APPROVED"))
						query.setParameter("userStatus", MPAStatusType.APPROVED.getStatusCode());
					if(status.equalsIgnoreCase("REJECTED"))
						query.setParameter("userStatus", MPAStatusType.REJECTED.getStatusCode());
					query.setParameter("industryCategory", industryCategory);
					query.setParameter("adminStatus", status);
					
				}
//				else if(statusBy.equalsIgnoreCase("Maker")) {
//					sqlBuilder.append("(u.checkerPayId = :payId or u.makerPayId = :payId) and u.userStatus = :userStatus and u.industryCategory = :industryCategory and u.makerStatus = :makerStatus");
//					
//					query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
//					query.setParameter("checkerPayId", payId);
//					if(status.equalsIgnoreCase("APPROVED"))
//						query.setParameter("userStatus", MPAStatusType.APPROVED.getStatusCode());
//					if(status.equalsIgnoreCase("REJECTED"))
//						query.setParameter("userStatus", MPAStatusType.REJECTED.getStatusCode());
//					query.setParameter("industryCategory", industryCategory);
//					query.setParameter("makerStatus", status);
//					
//				}else if(statusBy.equalsIgnoreCase("Admin")) {
//					sqlBuilder.append("(u.checkerPayId = :payId or u.makerPayId = :payId) and u.userStatus = :userStatus and u.industryCategory = :industryCategory and u.adminStatus = :adminStatus");
//					
//					query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
//					query.setParameter("checkerPayId", payId);
//					if(status.equalsIgnoreCase("APPROVED"))
//						query.setParameter("userStatus", MPAStatusType.APPROVED.getStatusCode());
//					if(status.equalsIgnoreCase("REJECTED"))
//						query.setParameter("userStatus", MPAStatusType.REJECTED.getStatusCode());
//					query.setParameter("industryCategory", industryCategory);
//					query.setParameter("adminStatus", status);
//					
//				}
			}else if(status.equalsIgnoreCase("ALL") && !industryCategory.equalsIgnoreCase("ALL")){
					sqlBuilder.append("(u.checkerPayId = :payId or u.makerPayId = :payId) and u.industryCategory = :industryCategory");

					query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
					query.setParameter("payId", payId);
					query.setParameter("industryCategory", industryCategory);
					
//					sqlBuilder.append("u.makerPayId = :makerPayId and u.industryCategory = :industryCategory");
//					query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
//					query.setParameter("makerPayId", payId);
//					query.setParameter("industryCategory", industryCategory);
					
			}else if(status.equalsIgnoreCase("ALL") && industryCategory.equalsIgnoreCase("ALL")){
					sqlBuilder.append("(u.checkerPayId = :payId or u.makerPayId = :payId)");
					
					query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
					query.setParameter("payId", payId);
										
//					sqlBuilder.append("u.makerPayId = :makerPayId");
//					query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
//					query.setParameter("makerPayId", payId);
			}else if(!status.equalsIgnoreCase("ALL") && industryCategory.equalsIgnoreCase("ALL") && !statusBy.equalsIgnoreCase("ALL")){
				if(statusBy.equalsIgnoreCase("Checker")) {
					sqlBuilder.append("(u.checkerPayId = :payId or u.makerPayId = :payId) and u.checkerStatus = :checkerStatus and u.userStatus = :userStatus");
					
					query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
					query.setParameter("payId", payId);
					query.setParameter("checkerStatus", status);
					if(status.equalsIgnoreCase("APPROVED"))
						query.setParameter("userStatus", MPAStatusType.APPROVED.getStatusCode());
					if(status.equalsIgnoreCase("REJECTED"))
						query.setParameter("userStatus", MPAStatusType.REJECTED.getStatusCode());
					
				}else if(statusBy.equalsIgnoreCase("Maker")) {
					sqlBuilder.append("(u.checkerPayId = :payId or u.makerPayId = :payId) and u.makerStatus = :makerStatus and u.userStatus = :userStatus");
					
					query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
					query.setParameter("payId", payId);
					query.setParameter("makerStatus", status);
					if(status.equalsIgnoreCase("APPROVED"))
						query.setParameter("userStatus", MPAStatusType.APPROVED.getStatusCode());
					if(status.equalsIgnoreCase("REJECTED"))
						query.setParameter("userStatus", MPAStatusType.REJECTED.getStatusCode());
					
				}
//				else if(permissionType.equals("Maker") && statusBy.equalsIgnoreCase("Checker")) {
//					sqlBuilder.append("u.makerPayId = :makerPayId and u.checkerStatus = :checkerStatus and u.userStatus = :userStatus");
//					
//					query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
//					query.setParameter("makerPayId", payId);
//					query.setParameter("checkerStatus", status);
//					if(status.equalsIgnoreCase("APPROVED"))
//						query.setParameter("userStatus", MPAStatusType.APPROVED.getStatusCode());
//					if(status.equalsIgnoreCase("REJECTED"))
//						query.setParameter("userStatus", MPAStatusType.REJECTED.getStatusCode());
//					
//				}
				else if(statusBy.equalsIgnoreCase("Admin")) {
					sqlBuilder.append("(u.checkerPayId = :payId or u.makerPayId = :payId) and u.adminStatus = :adminStatus and u.userStatus = :userStatus");
					
					query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
					query.setParameter("payId", payId);
					query.setParameter("adminStatus", status);
					if(status.equalsIgnoreCase("APPROVED"))
						query.setParameter("userStatus", MPAStatusType.APPROVED.getStatusCode());
					if(status.equalsIgnoreCase("REJECTED"))
						query.setParameter("userStatus", MPAStatusType.REJECTED.getStatusCode());
					
				}
//				else if(permissionType.equals("Checker") && statusBy.equalsIgnoreCase("Maker")) {
//					sqlBuilder.append("u.checkerPayId = :checkerPayId and u.makerStatus = :makerStatus and u.userStatus = :userStatus");
//					
//					query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
//					query.setParameter("checkerPayId", payId);
//					query.setParameter("makerStatus", status);
//					if(status.equalsIgnoreCase("APPROVED"))
//						query.setParameter("userStatus", MPAStatusType.APPROVED.getStatusCode());
//					if(status.equalsIgnoreCase("REJECTED"))
//						query.setParameter("userStatus", MPAStatusType.REJECTED.getStatusCode());
//				}
//				else if(permissionType.equals("Checker") && statusBy.equalsIgnoreCase("Admin")) {
//					sqlBuilder.append("u.checkerPayId = :checkerPayId and u.adminStatus = :adminStatus and u.userStatus = :userStatus");
//					
//					query = session.createQuery(sqlBuilder.toString(), MPAMerchant.class);
//					query.setParameter("checkerPayId", payId);
//					query.setParameter("adminStatus", status);
//					if(status.equalsIgnoreCase("APPROVED"))
//						query.setParameter("userStatus", MPAStatusType.APPROVED.getStatusCode());
//					if(status.equalsIgnoreCase("REJECTED"))
//						query.setParameter("userStatus", MPAStatusType.REJECTED.getStatusCode());
//				}
			}
			mpaMerchants = query.getResultList();
			tx.commit();
			
			 for(MPAMerchant mpaMerchant : mpaMerchants) {
				
					String registrationDate = formatter.format(mpaMerchant.getRegistrationDate());
					MerchantDetails merchant = new MerchantDetails();
					merchant.setPayId(mpaMerchant.getPayId());
					merchant.setBusinessName(mpaMerchant.getBusinessName());
					merchant.setEmailId(mpaMerchant.getEmailId());
					merchant.setMobile(mpaMerchant.getMobile());
					merchant.setRegistrationDate(registrationDate);
					merchant.setUserType(mpaMerchant.getUserType().name());
					merchant.setCheckerName(mpaMerchant.getCheckerName());
					merchant.setMakerName(mpaMerchant.getMakerName());
					merchant.setMakerStatus(mpaMerchant.getMakerStatus());
					merchant.setCheckerStatus(mpaMerchant.getCheckerStatus());
					merchant.setMakerStatusUpDate(mpaMerchant.getMakerStatusUpDate());
					merchant.setCheckerStatusUpDate(mpaMerchant.getCheckerStatusUpDate());
					merchant.setCheckerComments(mpaMerchant.getCheckerComments());
					merchant.setMakerComments(mpaMerchant.getMakerComments());
					String status1 = mpaMerchant.getUserStatus();

				if (status != null) {
					UserStatusType userStatus = UserStatusType.valueOf(status1);
					merchant.setStatus(userStatus);
				}
				merchants.add(merchant);
			}
			
		}catch (Exception exception) {
			logger.error("Database error", exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, ErrorType.DATABASE_ERROR.getResponseMessage());
		}
		return merchants;
	}
}
