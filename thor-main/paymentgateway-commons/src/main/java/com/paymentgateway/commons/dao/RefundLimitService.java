package com.paymentgateway.commons.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.user.RefundLimitObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;

import software.amazon.awssdk.utils.StringUtils;

/**
 * @author Mehboob Alam
 *
 */

@Service
public class RefundLimitService {

	@Autowired
	private UserDao userDao;

	@Autowired
	private RefundLimitDao refundLimitDao;

	private static Logger logger = LoggerFactory.getLogger(RefundLimitService.class.getName());

	public void createOrUpdateRefundLimit(String payId, String subMerchantId, Float oneTimeRefundLimit,
			Float refundLimitRemains, Float extraRefundLimit, User sessionUser, Boolean oneTimeRefundAmount, Boolean extraRefundAmount, String remarks) {
		logger.info("Inside the createOrUpdateRefundLimit : ");
		RefundLimitObject refundLimitObj = null;
		try {
			
			User subMerchant = null;
			User merchant = userDao.findPayId(payId);
			
			if (merchant.isSuperMerchant() && StringUtils.isNotBlank(subMerchantId)) {
				subMerchant = userDao.findPayId(subMerchantId);
			}
			
			if (extraRefundLimit > 0) {
				if (subMerchant != null) {
					subMerchant.setExtraRefundLimit(extraRefundLimit);
					subMerchant.setOneTimeRefundLimit(0);
					subMerchant.setRefundLimitRemains(0);
					userDao.saveOrUpdate(subMerchant);
					refundLimitObj = refundLimitDao.getActiveByPayId(payId,subMerchantId);
				} else {
					merchant.setExtraRefundLimit(extraRefundLimit);
					merchant.setOneTimeRefundLimit(0);
					merchant.setRefundLimitRemains(0);
					userDao.saveOrUpdate(merchant);
					refundLimitObj = refundLimitDao.getActiveByPayId(payId,null);
				}
				refundLimitObj = updateLimits(oneTimeRefundLimit, refundLimitRemains, extraRefundLimit, refundLimitObj, sessionUser, oneTimeRefundAmount, extraRefundAmount, remarks, merchant, subMerchant);
				
			}else if (oneTimeRefundLimit != null && refundLimitRemains != null) {
//				if (limitChangedFlag) {
//					if (subMerchant != null) {
//						subMerchant.setExtraRefundLimit(0);
//						subMerchant.setOneTimeRefundLimit(oneTimeRefundLimit);
//						subMerchant.setRefundLimitRemains(oneTimeRefundLimit);
//						userDao.saveOrUpdate(subMerchant);
//						refundLimitObj = refundLimitDao.getActiveByPayId(payId, subMerchantId);
//					} else {
//						merchant.setExtraRefundLimit(0);
//						merchant.setOneTimeRefundLimit(oneTimeRefundLimit);
//						merchant.setRefundLimitRemains(oneTimeRefundLimit);
//						userDao.saveOrUpdate(merchant);
//						refundLimitObj = refundLimitDao.getActiveByPayId(payId, null);
//						//refundLimitObj = session.get(RefundLimitObject.class, merchant.getPayId());
//					}
//					refundLimitObj = updateLimits(payId, subMerchantId, oneTimeRefundLimit, refundLimitRemains, extraRefundLimit, refundLimitObj, sessionUser, oneTimeRefundAmount, extraRefundAmount, merchant, subMerchant);
//				} else {
					if (subMerchant != null) {
						subMerchant.setExtraRefundLimit(0);
						subMerchant.setOneTimeRefundLimit(oneTimeRefundLimit);
						subMerchant.setRefundLimitRemains(refundLimitRemains);
						userDao.saveOrUpdate(subMerchant);
						refundLimitObj = refundLimitDao.getActiveByPayId(payId, subMerchantId);
						//refundLimitObj = session.get(RefundLimitObject.class, subMerchant.getId());
					} else {
						merchant.setExtraRefundLimit(0);
						merchant.setOneTimeRefundLimit(oneTimeRefundLimit);
						merchant.setRefundLimitRemains(refundLimitRemains);
						userDao.saveOrUpdate(merchant);
						refundLimitObj = refundLimitDao.getActiveByPayId(payId, null);
						//refundLimitObj = session.get(RefundLimitObject.class, merchant.getPayId());
					}
					refundLimitObj = updateLimits(oneTimeRefundLimit, refundLimitRemains, extraRefundLimit, refundLimitObj, sessionUser, oneTimeRefundAmount, extraRefundAmount, remarks, merchant, subMerchant);
			}

		} catch (Exception ex) {
			logger.error("Exception Caught while updating Refund limit : ", ex);
		}
	}

	public List<RefundLimitObject> fetchRefundLimitReport(String payId, String subMerchantId, String dateTo,
			String dateFrom, User sessionUser) {
		logger.info("Inside the fetchRefundLimitReport : ");
		List<RefundLimitObject> refundLimiTObjList = new ArrayList<RefundLimitObject>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				if (StringUtils.isNotBlank(subMerchantId) && !subMerchantId.equalsIgnoreCase("ALL")) {
					refundLimiTObjList = refundLimitDao.getLimitReportByPayId(payId, subMerchantId, dateFrom, dateTo);
				} else if(StringUtils.isNotBlank(payId) && !payId.equalsIgnoreCase("ALL")){
					refundLimiTObjList = refundLimitDao.getLimitReportByPayId(payId, subMerchantId, dateFrom, dateTo);
				}else {
					refundLimiTObjList = refundLimitDao.findAllMerchantlimitReport(dateFrom, dateTo);
				}
			} else if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				if (StringUtils.isNotBlank(subMerchantId) && !subMerchantId.equalsIgnoreCase("ALL")) {
					refundLimiTObjList = refundLimitDao.findAllMerchantlimitReportByResellerId(sessionUser.getResellerId(),
							payId, subMerchantId, dateFrom, dateTo);
				} else if (StringUtils.isNotBlank(payId) && !payId.equalsIgnoreCase("ALL")) {
					refundLimiTObjList = refundLimitDao.findAllMerchantlimitReportByResellerId(sessionUser.getResellerId(),
							payId, subMerchantId, dateFrom, dateTo);
				} else {
					refundLimiTObjList = refundLimitDao.findAllMerchantlimitReportByResellerId(sessionUser.getResellerId(),
							null, null, dateFrom, dateTo);
				}
			} else if (sessionUser.getUserType().equals(UserType.MERCHANT)) {

				if (sessionUser.isSuperMerchant() && StringUtils.isNotBlank(subMerchantId)) {
					refundLimiTObjList = refundLimitDao.getLimitReportByPayId(sessionUser.getPayId(), subMerchantId, dateFrom, dateTo);
				} else if(!sessionUser.isSuperMerchant() && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())){
					refundLimiTObjList = refundLimitDao.getLimitReportByPayId(sessionUser.getSuperMerchantId(), sessionUser.getPayId(), dateFrom, dateTo);
				}else {
					refundLimiTObjList = refundLimitDao.getLimitReportByPayId(sessionUser.getPayId(), null, dateFrom, dateTo);
				}
			}

		} catch (Exception ex) {
			logger.error("Exception caught in fetchRefundLimitReport : ", ex);
		}
		return refundLimiTObjList;
	}

	public RefundLimitObject updateLimits(Float oneTimeRefundLimit, Float refundLimitRemains,
			Float extraRefundLimit, RefundLimitObject refundLimiTObjFromDb, User sessionUser, Boolean oneTimeRefundAmount, Boolean extraRefundAmount, String remarks, User merchant, User subMerchant) {
		logger.info("Inside the updateLimits : ");
		RefundLimitObject refundLimitObj = new RefundLimitObject();
		String payId = null;
		String subMerchantId = null;
		String resellerId = null;
		try {

			SimpleDateFormat sdfcurrdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date currDate = new Date();
			String currentdate = sdfcurrdate.format(currDate);
			
			if(subMerchant != null) {
				subMerchantId = subMerchant.getPayId();
				payId = subMerchant.getSuperMerchantId();
				resellerId = subMerchant.getResellerId();
				refundLimitObj.setSubMerchantName(subMerchant.getBusinessName());
			}
			if(merchant != null) {
				payId = merchant.getPayId();
				resellerId = merchant.getResellerId();
				refundLimitObj.setMerchantName(merchant.getBusinessName());
			}
			refundLimitObj.setExtraRefundAmount(extraRefundAmount);
			refundLimitObj.setOneTimeRefundAmount(oneTimeRefundAmount);
			refundLimitObj.setPayId(payId);
			refundLimitObj.setResellerId(resellerId);
			refundLimitObj.setSubMerchantId(subMerchantId);
			refundLimitObj.setRemarks(remarks);
			if (refundLimiTObjFromDb != null) {
				 refundLimitDao.deActivate(currentdate, sessionUser, payId, subMerchantId);
				if (extraRefundLimit > 0) {
					refundLimitObj.setCredit(extraRefundLimit);
					refundLimitObj.setAssignorEmail(sessionUser.getEmailId());
					refundLimitObj.setAssignorMobile(sessionUser.getMobile());
					refundLimitObj.setAssignorName(sessionUser.getBusinessName());
					refundLimitObj.setDate(currentdate);

				} else {
					refundLimitObj.setCredit(oneTimeRefundLimit);
					refundLimitObj.setBalance(refundLimitRemains);
					refundLimitObj.setDebit(oneTimeRefundLimit - refundLimitRemains);
					refundLimitObj.setAssignorEmail(sessionUser.getEmailId());
					refundLimitObj.setAssignorMobile(sessionUser.getMobile());
					refundLimitObj.setAssignorName(sessionUser.getBusinessName());
					refundLimitObj.setDate(currentdate);

				}
				refundLimitDao.activateLimit(currentdate, refundLimitObj);
			} else {
				if (extraRefundLimit > 0) {
					refundLimitObj.setCredit(extraRefundLimit);
					refundLimitObj.setAssignorEmail(sessionUser.getEmailId());
					refundLimitObj.setAssignorMobile(sessionUser.getMobile());
					refundLimitObj.setAssignorName(sessionUser.getBusinessName());
					refundLimitObj.setDate(currentdate);
				} else {
					refundLimitObj.setCredit(oneTimeRefundLimit);
					refundLimitObj.setBalance(refundLimitRemains);
					refundLimitObj.setDebit(oneTimeRefundLimit - refundLimitRemains);
					if(sessionUser == null) {
						refundLimitObj.setAssignorEmail("NA");
						refundLimitObj.setAssignorMobile("NA");
						refundLimitObj.setAssignorName("NA");
						refundLimitObj.setDate(currentdate);
					}else {
						refundLimitObj.setAssignorEmail(sessionUser.getEmailId());
						refundLimitObj.setAssignorMobile(sessionUser.getMobile());
						refundLimitObj.setAssignorName(sessionUser.getBusinessName());
						refundLimitObj.setDate(currentdate);
					}
				}
				refundLimitDao.activateLimit(currentdate, refundLimitObj);
				// refundLimitDao.create(refundLimitObj);
				// session.save(refundLimitObj);
			}
			
		} catch (Exception ex) {
			logger.error("Exception caught : ", ex);
		}
		return refundLimitObj;
	}
	
	public void updateLimitsFromTxn(User user) {
		logger.info("Inside the updateLimitsInReport : ");
		RefundLimitObject limitObject = null;
		boolean subMerchantFlag = false;
		try {
			if(!user.isSuperMerchant() && StringUtils.isNotBlank(user.getSuperMerchantId())) {
				subMerchantFlag = true;
				limitObject = refundLimitDao.getActiveByPayId(user.getSuperMerchantId(), user.getPayId());
			}else {
				limitObject = refundLimitDao.getActiveByPayId(user.getPayId(), null);
			}
			if(limitObject != null) {
				limitObject.setBalance(user.getRefundLimitRemains());
				limitObject.setDebit(user.getOneTimeRefundLimit() - user.getRefundLimitRemains());
				refundLimitDao.update(limitObject);
			}else {
				if(subMerchantFlag) {
					updateLimits(user.getOneTimeRefundLimit(), user.getRefundLimitRemains(), user.getExtraRefundLimit(), limitObject, null, true, false, null, null, user);
				} else {
					updateLimits(user.getOneTimeRefundLimit(), user.getRefundLimitRemains(), user.getExtraRefundLimit(), limitObject, null, true, false, null, user, null);
				}
				
			}
			
		} catch(Exception ex) {
			logger.error("Exception caught in updateLimitsInReport : ", ex);
		}
		
	}
	
	public RefundLimitObject getLimitByPayId(String payId, String subMerchantId) {
		
		RefundLimitObject refundLimitObj = refundLimitDao.getActiveByPayId(payId, subMerchantId);
		if(refundLimitObj != null) {
			if(refundLimitObj.isOneTimeRefundAmount()) {
				refundLimitObj.setOneTimeRefundLimit(refundLimitObj.getCredit());
				refundLimitObj.setRefundLimitRemains(refundLimitObj.getBalance());
			} else {
				refundLimitObj.setExtraRefundLimit(refundLimitObj.getCredit());
			}
		}
		return refundLimitObj;
	}
}