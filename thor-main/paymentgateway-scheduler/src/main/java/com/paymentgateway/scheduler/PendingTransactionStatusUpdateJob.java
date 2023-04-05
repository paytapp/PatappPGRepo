package com.paymentgateway.scheduler;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.pg.core.util.ProcessManager;
import com.paymentgateway.pg.core.util.Processor;
import com.paymentgateway.scheduler.commons.TransactionDataProvider;

public class PendingTransactionStatusUpdateJob extends QuartzJobBean {

	@Autowired
	@Qualifier("updateProcessor")
	private Processor updateProcessor;

	@Autowired
	private TransactionDataProvider transactionDataProvider;

	@Autowired
	private UserDao userDao;

	private static final Logger logger = LoggerFactory.getLogger(PendingTransactionStatusUpdateJob.class);

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		fetchPendingTransactionStatusData();
	}

	private void fetchPendingTransactionStatusData() {
		try {

			logger.info("Started fetching pending transaction");
			Set<String> orderIdSet = transactionDataProvider.fetchPendingTransactionData();

			for (String orderId : orderIdSet) {

				logger.info("Timeout status added request in DB for OrderId == " + orderId);

				JSONObject data = new JSONObject();
				data.put(FieldType.ORDER_ID.getName(), orderId);

				// DB Entry in letzpayTransaction and update status in transactionStatus
				Fields fields = new Fields();
				// fieldsDao.getPreviousForPgRefNum(pgrefNum);
				fields = transactionDataProvider.fetchPendingTransactionDataByOrderId(orderId);
				if (StringUtils.isNotBlank(FieldType.STATUS.getName()) && (fields.get(FieldType.STATUS.getName())
						.equalsIgnoreCase(StatusType.CAPTURED.getName())
						|| fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.FAILED.getName())
						|| fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.REJECTED.getName())
						|| fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CANCELLED.getName())
						|| fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.DECLINED.getName())
						|| fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.TIMEOUT.getName()))) {
					
				} else {
					User user = new User();
					user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
					fields.put(FieldType.TXNTYPE.getName(), user.getModeType().getName());
					fields.put(FieldType.ORIG_TXNTYPE.getName(), user.getModeType().getName());
					fields.put(FieldType.STATUS.getName(), StatusType.TIMEOUT.getName());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.TIMEOUT.getResponseCode());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.TIMEOUT.getResponseMessage());
					fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
					fields.put(FieldType.SURCHARGE_FLAG.getName(), fields.get(FieldType.SURCHARGE_FLAG.getName()));
					fields.put(FieldType.PAYMENTS_REGION.getName(), fields.get(FieldType.PAYMENTS_REGION.getName()));
					fields.put(FieldType.CARD_HOLDER_TYPE.getName(), fields.get(FieldType.CARD_HOLDER_TYPE.getName()));
					String pgrefNum = TransactionManager.getNewTransactionId();
					fields.put(FieldType.PG_REF_NUM.getName(), pgrefNum);
					fields.put(FieldType.TXN_ID.getName(), pgrefNum);
					fields.put(FieldType.ORIG_TXN_ID.getName(), pgrefNum);
					if (StringUtils.isNotBlank(fields.get(FieldType.ORDER_ID.getName()))) {
						// Update processor
						ProcessManager.flow(updateProcessor, fields, true);
					}
				}
			}
		}

		catch (Exception e) {
			logger.error("Exception in fetch Pending Transaction Status Data from scheduler", e);
		}
	}
}
