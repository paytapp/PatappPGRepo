package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.paymentgateway.commons.util.ReconFile;

/**
 * @ Shaiwal
 */
public class ReconDataDisplayAction extends AbstractSecureAction {

	@Autowired
	private ReconFileDao reconFileDao;

	private static final long serialVersionUID = 730021546498302127L;
	private static Logger logger = LoggerFactory.getLogger(ReconDataDisplayAction.class.getName());
	private List<ReconFile> aaData = new ArrayList<ReconFile>();
	private int draw;
	private int length;
	private int start;
	private int recordsTotal;
	public long recordsFiltered;

	public String execute() {
		try {
			setRecordsTotal(reconFileDao.getReconFilesCount("BOOKING"));
			if (getLength() == -1) {
				setLength((int) getRecordsTotal());
			}
			setAaData(reconFileDao.getReconFiles("BOOKING",start,length));
			recordsFiltered = recordsTotal;
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception Caught while sgetting booking file list" , exception);
			return ERROR;
		}
	}
	
	public String getMprTxn() {
		try {
			setRecordsTotal(reconFileDao.getReconFilesCount("MPR"));
			if (getLength() == -1) {
				setLength((int) getRecordsTotal());
			}
			setAaData(reconFileDao.getReconFiles("MPR",start,length));
			recordsFiltered = recordsTotal;
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception Caught while sgetting MPR file list" , exception);
			return ERROR;
		}
	}
	
	public String getRefundTxn() {
		try {
			setRecordsTotal(reconFileDao.getReconFilesCount("REFUND"));
			if (getLength() == -1) {
				setLength((int) getRecordsTotal());
			}
			setAaData(reconFileDao.getReconFiles("REFUND",start,length));
			recordsFiltered = recordsTotal;
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception Caught while sgetting Refund file list" , exception);
			return ERROR;
		}
	}

	
	public String getStatementTxn() {
		try {
			setRecordsTotal(reconFileDao.getReconFilesCount("STATEMENT"));
			if (getLength() == -1) {
				setLength((int) getRecordsTotal());
			}
			setAaData(reconFileDao.getReconFiles("STATEMENT",start,length));
			recordsFiltered = recordsTotal;
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception Caught while sgetting STATEMENT file list" , exception);
			return ERROR;
		}
	}
	
	
	public String getAgentTxn() {
		try {
			setRecordsTotal(reconFileDao.getReconFilesCount("STATEMENT"));
			if (getLength() == -1) {
				setLength((int) getRecordsTotal());
			}
			setAaData(reconFileDao.getReconFiles("STATEMENT",start,length));
			recordsFiltered = recordsTotal;
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception Caught while sgetting STATEMENT file list" , exception);
			return ERROR;
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

	public List<ReconFile> getAaData() {
		return aaData;
	}

	public void setAaData(List<ReconFile> aaData) {
		this.aaData = aaData;
	}

}
