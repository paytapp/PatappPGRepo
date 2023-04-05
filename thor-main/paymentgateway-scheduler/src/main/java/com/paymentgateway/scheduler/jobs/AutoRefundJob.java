package com.paymentgateway.scheduler.jobs;

import java.util.List;
import java.util.Map;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.scheduler.commons.AutoRefundTransactions;
import com.paymentgateway.scheduler.commons.ConfigurationProvider;
import com.paymentgateway.scheduler.commons.TransactionDataProvider;

/**
 * @author Sandeep Sharma
 *
 */

public class AutoRefundJob extends QuartzJobBean {

	@Autowired
	private ConfigurationProvider configurationProvider;

	@Autowired
	private TransactionDataProvider transactionDataProvider;

	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	Logger logger = LoggerFactory.getLogger(AutoRefundJob.class.getName());

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		fetchAutoRefundTxn();
	}

	private void fetchAutoRefundTxn() {

		try {

			logger.info("Started fetching post settled transaction data for auto refund");
			List<AutoRefundTransactions> txns = transactionDataProvider.fetchPostCapturedTxnData();

			for (AutoRefundTransactions txnData : txns) {
				logger.info("Sending Auto Refund request , orderID == " + txnData.getOrderId());
				Fields fields = new Fields();

				fields.put(FieldType.PAY_ID.getName(), txnData.getPayId());
				fields.put(FieldType.CURRENCY_CODE.getName(), txnData.getCurrencyCode());
				fields.put(FieldType.AMOUNT.getName(),
						Amount.formatAmount(txnData.getAmount(), txnData.getCurrencyCode()));
				fields.put(FieldType.ORDER_ID.getName(), txnData.getOrderId());
				fields.put(FieldType.REFUND_ORDER_ID.getName(), TransactionManager.getNewTransactionId());
				fields.put(FieldType.PG_REF_NUM.getName(), txnData.getPgRefNum());
				fields.put(FieldType.TXNTYPE.getName(), "REFUND");
				String hash = Hasher.getHash(fields);
				fields.put(FieldType.HASH.getName(), hash);

				Map<String, String> res = transactionControllerServiceProvider.transact(fields,
						configurationProvider.getAutoRefundUrl());

				logger.info("Auto refund transaction with order id == " + fields.get(FieldType.ORDER_ID.getName())
						+ " with response received from pg ws == " + res.get(FieldType.RESPONSE_MESSAGE.getName()));

//				JSONObject data = new JSONObject();
//
//				data.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
//				data.put(FieldType.CURRENCY_CODE.getName(), fields.get(FieldType.CURRENCY_CODE.getName()));
//				data.put(FieldType.AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
//				data.put(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName()));
//				data.put(FieldType.REFUND_ORDER_ID.getName(), fields.get(FieldType.REFUND_ORDER_ID.getName()));
//				data.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.PG_REF_NUM.getName()));
//				data.put(FieldType.TXNTYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
//				data.put(FieldType.HASH.getName(), fields.get(FieldType.HASH.getName()));

//				serviceControllerProvider.autoRefundTxn(data, autoRefundUrl);
			}

		}

		catch (Exception e) {
			logger.error("Exception in Auto Refund from scheduler", e);
		}
	}
}
