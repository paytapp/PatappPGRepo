package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.action.AbstractSecureAction;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.crm.actionBeans.ResellerRevenueDataService;

/**
 * @author Amitosh Aanand
 *
 */
public class ResellerRevenueReportAction extends AbstractSecureAction {
	
	private static Logger logger = LoggerFactory.getLogger(ResellerRevenueReportAction.class.getName());

	@Autowired
	private UserDao userDao;

	@Autowired
	private ResellerRevenueDataService resellerRevenueDataService;

	private String merchantPayId;
	private String resellerId;
	private String dateFrom;
	private String dateTo;
	public List<Merchants> listMerchant = new ArrayList<Merchants>();
	public List<Merchants> listReseller = new ArrayList<Merchants>();
	private Map<String, HashMap<String, String>> resellerRevenue = new HashMap<String, HashMap<String, String>>();
	private int resellerRevenueLength;
	
	private static final long serialVersionUID = -1580414966022924784L;

	@SuppressWarnings("unchecked")
	public String execute() {
		try {
			logger.info("inside ResellerRevenueReportAction, execute function ");
			setListMerchant(userDao.getMerchantList());
			setListReseller(userDao.getResellerList());
			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (sessionUser.getUserType().equals(UserType.SUPERADMIN)
					|| sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				setResellerRevenue(resellerRevenueDataService.fetchResellerRevenueReport(sessionUser, merchantPayId, resellerId,
						dateFrom, dateTo));
			} else if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				setResellerRevenue(resellerRevenueDataService.fetchResellerRevenueReport(sessionUser, merchantPayId,
						sessionUser.getResellerId(), dateFrom, dateTo));
			}
			setResellerRevenueLength(getResellerRevenue().size());

		} catch (Exception e) {
			return ERROR;
		}
		return SUCCESS;
	}

	public String getMerchantPayId() {
		return merchantPayId;
	}

	public void setMerchantPayId(String merchantPayId) {
		this.merchantPayId = merchantPayId;
	}

	public String getResellerId() {
		return resellerId;
	}

	public void setResellerId(String resellerId) {
		this.resellerId = resellerId;
	}

	public String getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(String dateFrom) {
		this.dateFrom = dateFrom;
	}

	public String getDateTo() {
		return dateTo;
	}

	public void setDateTo(String dateTo) {
		this.dateTo = dateTo;
	}

	public List<Merchants> getListMerchant() {
		return listMerchant;
	}

	public void setListMerchant(List<Merchants> listMerchant) {
		this.listMerchant = listMerchant;
	}

	public List<Merchants> getListReseller() {
		return listReseller;
	}

	public void setListReseller(List<Merchants> listReseller) {
		this.listReseller = listReseller;
	}

	public Map<String, HashMap<String, String>> getResellerRevenue() {
		return resellerRevenue;
	}

	public void setResellerRevenue(Map<String, HashMap<String, String>> resellerRevenue) {
		this.resellerRevenue = resellerRevenue;
	}
	public int getResellerRevenueLength() {
		return resellerRevenueLength;
	}

	public void setResellerRevenueLength(int resellerRevenueLength) {
		this.resellerRevenueLength = resellerRevenueLength;
	}
}