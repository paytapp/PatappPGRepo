package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;;

public class GetParentDetailAction extends AbstractSecureAction implements ServletRequestAware {

	private HttpServletRequest request;
	private static final long serialVersionUID = 4554129659758013515L;
	@Autowired
	private UserDao userDao;

	private static Logger logger = LoggerFactory.getLogger(GetParentDetailAction.class.getName());

	private String superMerchantBusinessName;
	private String subMerchantBusinessName;
	private String resellerBusinessName;
	private String subUserBusinessName;
	private String merchantBusinessName;
	private String resellerType;

	private User userSession = new User();
	List<GetParentDetailAction> getparentlist = new ArrayList<GetParentDetailAction>();

	@Override
	public String execute() {
		try {
			System.out.println("HAHAHAH");
			GetParentDetailAction detail = new GetParentDetailAction();
			userSession = (User) sessionMap.get(Constants.USER.getValue());
			logger.info("Find Parential Details of userType = " + userSession.getUserType().name());
			if (userSession.getUserType().equals(UserType.SUBUSER)) {
				// subUserBusinessName = userSession.getBusinessName();
				User parentUser = userDao.findPayId(userSession.getParentPayId());
				if (parentUser != null && parentUser.getUserType().equals(UserType.MERCHANT)) {
					if (parentUser.isSuperMerchant()) {
						superMerchantBusinessName = parentUser.getBusinessName();
					}

					if (parentUser.getSuperMerchantId() != null && !parentUser.isSuperMerchant()) {
						User subMerchant = userDao.findPayId(userSession.getParentPayId());
						if (subMerchant != null && subMerchant.getUserType().equals(UserType.MERCHANT)) {
							subMerchantBusinessName = subMerchant.getBusinessName();
							User parent = userDao.findBySuperMerchantId(subMerchant.getSuperMerchantId());
							if (parent.isSuperMerchant()) {
								superMerchantBusinessName = parent.getBusinessName();
							} else {
								merchantBusinessName = parent.getBusinessName();
							}
							User reseller = userDao.findResellerId(parent.getResellerId());
							if (reseller != null && reseller.getUserType().equals(UserType.RESELLER)) {
								resellerBusinessName = reseller.getBusinessName();
							}
							
						}
					}

					if (parentUser.getSuperMerchantId() == null && !parentUser.isSuperMerchant()) {
						merchantBusinessName = parentUser.getBusinessName();
					}
					User resellerUser = userDao.findResellerId(parentUser.getResellerId());

					if (resellerUser != null && resellerUser.getUserType().equals(UserType.RESELLER)) {
						resellerBusinessName = resellerUser.getBusinessName();
					}
				}
			}
			if (userSession.getUserType().equals(UserType.MERCHANT)) {
				logger.info("INSIDE MERCHANT");
				User merchant = userDao.findPayId(userSession.getPayId());
				if (merchant != null && merchant.getUserType().equals(UserType.MERCHANT)) {
					if (merchant.getSuperMerchantId() != null && !merchant.isSuperMerchant()) {
						logger.info("seconf if " + merchant.getBusinessName());
						// subMerchantBusinessName = merchant.getBusinessName();
						User ParentsuperMerchant = userDao.findPayId(merchant.getSuperMerchantId());
						logger.info("ParentsuperMerchant = " + ParentsuperMerchant);
						if (ParentsuperMerchant.isSuperMerchant()) {
							logger.info("ParentsuperMerchant = " + ParentsuperMerchant.getBusinessName());
							superMerchantBusinessName = ParentsuperMerchant.getBusinessName();
						}
						User resellerUser = userDao.findResellerId(ParentsuperMerchant.getResellerId());

						if (resellerUser != null && resellerUser.getUserType().equals(UserType.RESELLER)) {
							logger.info("inside resellerUser = " + resellerUser.getBusinessName());
							resellerBusinessName = resellerUser.getBusinessName();
						}

						/*
						 * else { merchantBusinessName = userSession.getBusinessName(); }
						 */
					}

					else if (merchant.getSuperMerchantId() == null && !merchant.isSuperMerchant()) {
						// merchantBusinessName = merchant.getBusinessName();
						User resellerUser = userDao.findResellerId(merchant.getResellerId());

						if (resellerUser != null && resellerUser.getUserType().equals(UserType.RESELLER)) {
							logger.info("inside resellerUser = " + resellerUser.getBusinessName());
							resellerBusinessName = resellerUser.getBusinessName();
						}
					} else if (merchant.isSuperMerchant()) {
						// superMerchantBusinessName = merchant.getBusinessName();
						User resellerUser = userDao.findResellerId(merchant.getResellerId());

						if (resellerUser != null && resellerUser.getUserType().equals(UserType.RESELLER)) {
							logger.info("inside resellerUser = " + resellerUser.getBusinessName());
							resellerBusinessName = resellerUser.getBusinessName();
						}
					}
				}
			}
			if (userSession.getUserType().equals(UserType.RESELLER)) {
				// resellerBusinessName=userSession.getBusinessName();
				if (userSession.isPartnerFlag()) {
					logger.info("inside Partner ");
					setResellerType("PARTNER RESELLER");
				} else {
					logger.info("inside Nonpartner ");
					setResellerType("NORMAL RESELLER");
				}

			}

			detail.setSubUserBusinessName(subUserBusinessName);
			detail.setSubMerchantBusinessName(subMerchantBusinessName);
			detail.setMerchantBusinessName(merchantBusinessName);
			detail.setSuperMerchantBusinessName(superMerchantBusinessName);
			detail.setResellerBusinessName(resellerBusinessName);
			detail.setResellerType(resellerType);

			getparentlist.add(detail);
			return SUCCESS;

		} catch (Exception e) {
			logger.error("Exception in getting Parent list ", e);
			return ERROR;
		}
	}

	public String getResellerType() {
		return resellerType;
	}

	public void setResellerType(String resellerType) {
		this.resellerType = resellerType;
	}

	public String getSuperMerchantBusinessName() {
		return superMerchantBusinessName;
	}

	public void setSuperMerchantBusinessName(String superMerchantBusinessName) {
		this.superMerchantBusinessName = superMerchantBusinessName;
	}

	public String getSubMerchantBusinessName() {
		return subMerchantBusinessName;
	}

	public void setSubMerchantBusinessName(String subMerchantBusinessName) {
		this.subMerchantBusinessName = subMerchantBusinessName;
	}

	public String getResellerBusinessName() {
		return resellerBusinessName;
	}

	public void setResellerBusinessName(String resellerBusinessName) {
		this.resellerBusinessName = resellerBusinessName;
	}

	public String getSubUserBusinessName() {
		return subUserBusinessName;
	}

	public void setSubUserBusinessName(String subUserBusinessName) {
		this.subUserBusinessName = subUserBusinessName;
	}

	public String getMerchantBusinessName() {
		return merchantBusinessName;
	}

	public void setMerchantBusinessName(String merchantBusinessName) {
		this.merchantBusinessName = merchantBusinessName;
	}

	@Override
	public void setServletRequest(HttpServletRequest arg0) {
		this.request = request;
	}
}