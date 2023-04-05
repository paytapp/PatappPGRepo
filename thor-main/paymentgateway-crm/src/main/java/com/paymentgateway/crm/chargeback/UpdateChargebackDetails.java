package com.paymentgateway.crm.chargeback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.ChargebackDao;
import com.paymentgateway.commons.user.Chargeback;
import com.paymentgateway.crm.action.AbstractSecureAction;
import com.paymentgateway.crm.chargeback.util.CaseStatus;
import com.paymentgateway.crm.chargeback.util.ChargebackStatus;

public class UpdateChargebackDetails extends AbstractSecureAction {

	@Autowired
	private ChargebackDao chargebackDao;
	private static final long serialVersionUID = 3981325447017559786L;
	private static Logger logger = LoggerFactory.getLogger(UpdateChargebackDetails.class.getName());
	private String Id;
	private String caseId;

	private Chargeback chargeback = new Chargeback();

	public String execute() {

		try {
			setChargeback(chargebackDao.findByCaseId(getCaseId()));
			Chargeback chargeback = new Chargeback();
			chargeback = getChargeback();
			chargeback.setChargebackStatus(ChargebackStatus.ACCEPTED.getName());
			chargeback.setStatus(CaseStatus.CLOSE.getName());
			chargebackDao.update(chargeback);
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	public String getCaseId() {
		return caseId;
	}

	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}

	public Chargeback getChargeback() {
		return chargeback;
	}

	public void setChargeback(Chargeback chargeback) {
		this.chargeback = chargeback;
	}

	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}

}
