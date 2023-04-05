package com.paymentgateway.pg.core.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StaticDataProvider;
import com.paymentgateway.commons.util.StatusType;

@Service("timeoutProcessor")
public class TimeOutProcessor implements Processor {
	private static Logger logger = LoggerFactory.getLogger(TimeOutProcessor.class.getName());

	@Autowired
	private Fields field;

	@Autowired
	private UserDao userDao;

	@Autowired
	private FieldsDao fieldsDao;

	@Autowired
	private StaticDataProvider staticDataProvider;

	@Autowired
	private PropertiesManager propertiesManager;

	private String skipStatusChangeFlag;

	@Override
	public void preProcess(Fields fields) throws SystemException {
		skipStatusChangeFlag = fields.get(Constants.TRANSACTION_COMPLETE_FLAG.getValue());
		if (StringUtils.isBlank(skipStatusChangeFlag) || !skipStatusChangeFlag.equals(Constants.Y_FLAG.getValue())) {

			User user = null;

			if (propertiesManager.propertiesMap.get("useStaticData").equalsIgnoreCase("Y")) {
				user = staticDataProvider.getUserData(fields.get(FieldType.PAY_ID.getName()));
			} else {
				user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
			}

			logger.info("Changing status to time out, transaction Id: " + fields.get(FieldType.TXN_ID.getName()));
			fields.put(FieldType.TXNTYPE.getName(), user.getModeType().getName());
			fields.put(FieldType.ORIG_TXNTYPE.getName(), user.getModeType().getName());
			fields.put(FieldType.STATUS.getName(), StatusType.TIMEOUT.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.TIMEOUT.getResponseCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.TIMEOUT.getResponseMessage());
			fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
			fields.put(FieldType.SURCHARGE_FLAG.getName(), fields.get(FieldType.SURCHARGE_FLAG.getName()));
			fields.put(FieldType.PAYMENTS_REGION.getName(), fields.get(FieldType.PAYMENTS_REGION.getName()));
			fields.put(FieldType.CARD_HOLDER_TYPE.getName(), fields.get(FieldType.CARD_HOLDER_TYPE.getName()));
			String pgrefNum = fields.get(FieldType.PG_REF_NUM.getName());
			if (pgrefNum == null) {
				fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));
				fields.put(FieldType.ORIG_TXN_ID.getName(), fields.get(FieldType.TXN_ID.getName()));
			}
		}
	}

	@Override
	public void process(Fields fields) throws SystemException {

		// Validate if already a captured TXN before timeout
		// Added by Shaiwal due to issues in BOB Response Action -- ISSUE - Session Map
		// is Null

		boolean isCaptured = fieldsDao.getCapturedForPgRef(fields.get(FieldType.PG_REF_NUM.getName()));
		boolean isAlreadyUpdted = fieldsDao.getAlreadyUpdated(fields.get(FieldType.PG_REF_NUM.getName()));

		if (!isCaptured && !isAlreadyUpdted) {
			if (StringUtils.isBlank(skipStatusChangeFlag)
					|| !skipStatusChangeFlag.equals(Constants.Y_FLAG.getValue())) {
				field.insert(fields);
			}
		} else {
			logger.info("Timeout not entered as Txn is already captured or updated for transaction Id: "
					+ fields.get(FieldType.TXN_ID.getName()));
		}

		fields.removeExtraFields();
		fields.removeInternalFields();
	}

	@Override
	public void postProcess(Fields fields) throws SystemException {
		fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
	}
}
