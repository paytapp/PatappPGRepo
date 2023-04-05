package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.dao.BinRangeDao;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.BinRange;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.DataEncoder;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.crm.actionBeans.BinRangeDisplayServices;
import com.paymentgateway.crm.actionBeans.BinRangeFilter;

/**
 * @ Neeraj
 */
public class BinRangeDisplayAction extends AbstractSecureAction {

	@Autowired
	private DataEncoder encoder;

	@Autowired
	private CrmValidator validator;

	@Autowired
	MongoInstance mongoInstance;

	@Autowired
	PropertiesManager propertiesManager;

	@Autowired
	private BinRangeDisplayServices binRangeDisplayServices;

	@Autowired
	private BinRangeDao binRangeDao;

	private static final long serialVersionUID = 730021546498302127L;
	private static Logger logger = LoggerFactory.getLogger(BinRangeDisplayAction.class.getName());
	private List<BinRange> aaData = new ArrayList<BinRange>();
	private int draw;
	private int length;
	private int start;
	private int recordsTotal;
	public long recordsFiltered;
	public String binCode;

	public String execute() {
		try {
			if (StringUtils.isBlank(binCode)) {
				return SUCCESS;
			}
			setRecordsTotal(binRangeDisplayServices.getBinRangTotalByBinCode(binCode));
			if (getLength() == -1) {
				setLength((int) getRecordsTotal());
			}
			setAaData(encoder.encodeBinRange(binRangeDao.findByBinCodeLow(binCode)));
			recordsFiltered = recordsTotal;
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception Caught while search bin by binCode " , exception);
			return ERROR;
		}
	}

	/* Bin search For EMI */
	public String emiBinDisplay() {
		try {
			setRecordsTotal(binRangeDisplayServices.getEMIBinRangTotalByBinCode(binCode));
			if (getLength() == -1) {
				setLength((int) getRecordsTotal());
			}
			setAaData(encoder.encodeBinRange(binRangeDao.findEMIBinByBinCodeLow(binCode)));
			recordsFiltered = recordsTotal;
		} catch (Exception exception) {
			logger.error("Exception Caught while search bin by binCode " , exception);
			return ERROR;
		}
		return SUCCESS;
	}

	public void validate() {
		if ((validator.validateBlankField(getBinCode()))) {
			addFieldError(CrmFieldType.BIN_CODE.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.BIN_CODE, getBinCode()))) {
			addFieldError(CrmFieldType.BIN_CODE.getName(), validator.getResonseObject().getResponseMessage());
		}
	}

	public int getDraw() {
		return draw;
	}

	public void setDraw(int draw) {
		this.draw = draw;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public List<BinRange> getAaData() {
		return aaData;
	}

	public void setAaData(List<BinRange> aaData) {
		this.aaData = aaData;
	}

	public long getRecordsFiltered() {
		return recordsFiltered;
	}

	public void setRecordsFiltered(long recordsFiltered) {
		this.recordsFiltered = recordsFiltered;
	}

	public int getRecordsTotal() {
		return recordsTotal;
	}

	public void setRecordsTotal(int recordsTotal) {
		this.recordsTotal = recordsTotal;
	}

	public String getBinCode() {
		return binCode;
	}

	public void setBinCode(String binCode) {
		this.binCode = binCode;
	}
}
