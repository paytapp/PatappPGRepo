package com.paymentgateway.notification.sms.controller;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.paymentgateway.commons.dao.HibernateAbstractDao;
import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.UserDao;

@RestController
public class UpdateExistingMerchantVANandVPA extends HibernateAbstractDao {

	@Autowired
	private UserDao userDao;

	private static Logger logger = LoggerFactory.getLogger(UpdateExistingMerchantVANandVPA.class.getName());

	@RequestMapping(method = RequestMethod.POST, value = "/updateVirtalAccountNoAndVPA")
	public @ResponseBody String updateVirtalAccountNoAndVPA(@RequestBody String reqmap) {

		try {
			List<Merchants> allActiveMerchantList = userDao.getAllMerchantwithVirtualAccountNo();
			for (Merchants merchant : allActiveMerchantList) {
				String payid = merchant.getPayId();
				String oldvirtualAccountNo = merchant.getVirtualAccountNo();
				if (oldvirtualAccountNo != null) {
					String virtualAccountNo = StringUtils.rightPad(oldvirtualAccountNo, 12, "0");
					String vPA = "Payment GateWay." + virtualAccountNo.substring(4) + "@icici";
					updateMerchantVANandVPA(virtualAccountNo, vPA, payid);
				} else {
					logger.info("No Virtual account No. found for = " + payid);
				}

			}
			return "Update SuccessFully";
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return "Exception Caught";
		}
	}

	public void updateMerchantVANandVPA(String virtualAccountNo, String merchantVPA, String payId) {

		logger.error("Update = " + payId);

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			session.createQuery("update User U set U.virtualAccountNo = :virtualAccountNo, U.merchantVPA = :merchantVPA"
					+ " where U.payId = :payId").setParameter("virtualAccountNo", virtualAccountNo)
					.setParameter("merchantVPA", merchantVPA).setParameter("payId", payId).executeUpdate();
			tx.commit();

		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
	}

}
