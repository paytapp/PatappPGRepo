/**
 * Used to close an open chargeback case which is already refunded
 */
package com.paymentgateway.scheduler.tasks;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.ChargebackDao;
import com.paymentgateway.commons.user.Chargeback;
import com.paymentgateway.commons.user.SchedulerJobs;
import com.paymentgateway.scheduler.commons.ChargebackDataProvider;
import com.paymentgateway.scheduler.commons.MaintainSchedulerLogs;
import com.paymentgateway.scheduler.core.TaskManager;

/**
 * @author Amitosh Aanand
 *
 */
@Service
public class ChargebackFinalStatus implements TaskManager {

	@Autowired
	private ChargebackDataProvider chargebackDataProvider;

	@Autowired
	private ChargebackDao chargebackDao;

	@Autowired
	private MaintainSchedulerLogs maintainSchedulerLogs;

	private static final Logger logger = LoggerFactory.getLogger(ChargebackFinalStatus.class);

	public void startChargebackStatusUpdater(SchedulerJobs job) {
		List<Chargeback> chargeBackList = chargebackDao.fetchRefundedChargebackCases();
		try {
			if (chargeBackList.isEmpty()) {
				logger.info("On " + new Date() + " no chargebacks cases are refunded");
			} else {
				List<Chargeback> closableChargebacks = new ArrayList<Chargeback>();
				for (Chargeback chargeback : chargeBackList) {
					if (chargebackDataProvider.checkRefundSettledForChargeback(chargeback.getPgRefNum())) {
						closableChargebacks.add(chargeback);
					}
				}
				if (!closableChargebacks.isEmpty()) {
					for (Chargeback ch : closableChargebacks) {
						chargebackDao.closeChargebackCase(ch.getId());
					}
				} else {
					logger.info("No chargeback cases are eligible for closing");
				}
			}
			taskLogger(job, null, null, true);
		} catch (Exception e) {
			logger.error("Exception caught while updating final status of chargeback cases, " , e);
			taskLogger(job, null, null, false);
		}
	}

	@Override
	public void taskLogger(SchedulerJobs job, String request, String response, Boolean jobStatus) {
		maintainSchedulerLogs.taskLogger(job, request, response, jobStatus);
	}
}