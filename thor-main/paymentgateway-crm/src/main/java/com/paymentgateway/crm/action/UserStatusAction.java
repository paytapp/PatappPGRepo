/**
 * 
 */
package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.MerchantGridViewService;
import com.paymentgateway.commons.dao.PendingMappingRequestDao;
import com.paymentgateway.commons.user.ChargingDetailsDao;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.SurchargeDetailsDao;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.ModeType;
import com.paymentgateway.commons.util.UserStatusType;
import com.paymentgateway.crm.mpa.Constants;

/**
 * @author Amitosh Aanand
 *
 */
public class UserStatusAction extends AbstractSecureAction {

	@Autowired
	private UserDao userDao;

	@Autowired
	private ChargingDetailsDao chargingDetailsDao;

	@Autowired
	private SurchargeDetailsDao surchargeDetailsDao;

	@Autowired
	private PendingMappingRequestDao pendingMappingRequestDao;

	@Autowired
	private MerchantGridViewService merchantGridViewService;

	private static final long serialVersionUID = -6507156892037317718L;
	private static Logger logger = LoggerFactory.getLogger(UserStatusAction.class.getName());

	private String payId;
	private String userType;
	private String response;
	private String userStatus;
	private String modeType;

	private User user = new User();
	public List<Merchants> listReseller = new ArrayList<Merchants>();
	public List<Merchants> listSuperMerchant = new ArrayList<Merchants>();
	public List<Merchants> listMerchant = new ArrayList<Merchants>();
	public List<Merchants> listSubMerchant = new ArrayList<Merchants>();
	public List<Merchants> listAcquirer = new ArrayList<Merchants>();
	public List<Merchants> listAgent = new ArrayList<Merchants>();
	public List<Merchants> listSubUser = new ArrayList<Merchants>();
	public List<Merchants> listParentMerchant = new ArrayList<Merchants>();

	public String execute() {
		try {

			User sessionUser = (User) sessionMap.get(Constants.USER);

			if (StringUtils.isNotBlank(userType)) {
				logger.info("Fetching list of user for user type: " + userType);
				if (userType.equals(UserType.RESELLER.name())) {
					setListReseller(userDao.getUserListbyUserType(userType, sessionUser.getResellerId()));
				} else if (userType.equals(UserType.MERCHANT.name())) {
					setListMerchant(userDao.getUserListbyUserType(userType, sessionUser.getResellerId()));
				} else if (userType.equals("SUPER_MERCHANT")) {
					setListSuperMerchant(userDao.getUserListbyUserType(userType, sessionUser.getResellerId()));
				} else if (userType.equals("SUB_MERCHANT")) {
					setListSubMerchant(userDao.getUserListbyUserType(userType, sessionUser.getResellerId()));
				} else if (userType.equals(UserType.SUBUSER.name())) {
					setListSubUser(userDao.getUserListbyUserType(userType, sessionUser.getResellerId()));
				} else if (userType.equals(UserType.ACQUIRER.name())) {
					setListAcquirer(userDao.getUserListbyUserType(userType, sessionUser.getResellerId()));
				} else if (userType.equals(UserType.PARENTMERCHANT.name())) {
					setListParentMerchant(userDao.getUserListbyUserType(userType, sessionUser.getResellerId()));
				}
			} else {
				logger.info("Fetching list of all Reseller,Merchant,SubMerchant,SuperMerchant,SubUser,Acquirer");
				setListReseller(userDao.getUserListbyUserType("RESELLER", sessionUser.getResellerId()));
				setListMerchant(userDao.getUserListbyUserType("MERCHANT", sessionUser.getResellerId()));
				setListSuperMerchant(userDao.getUserListbyUserType("SUPER_MERCHANT", sessionUser.getResellerId()));
				setListSubMerchant(userDao.getUserListbyUserType("SUB_MERCHANT", sessionUser.getResellerId()));
				setListSubUser(userDao.getUserListbyUserType("SUBUSER", sessionUser.getResellerId()));
				setListAcquirer(userDao.getUserListbyUserType("ACQUIRER", sessionUser.getResellerId()));
				setListParentMerchant(userDao.getUserListbyUserType("PARENTMERCHANT", sessionUser.getResellerId()));
			}
		} catch (Exception e) {
			logger.info("Exception, " + e);
		}
		return SUCCESS;
	}

	public String updateUserStatus() {
		try {
			logger.info("Updating user status for user with Pay ID: " + payId + " to user status: " + userStatus);
			user = (User) sessionMap.get(Constants.USER);
			User userFromDb = userDao.findPayId(payId);

			if (userFromDb == null) {
				setResponse("User not found");
				return SUCCESS;
			}

			if (userFromDb.getUserType().equals(UserType.RESELLER) || userFromDb.getUserType().equals(UserType.SUBUSER)
					|| (userFromDb.isSuperMerchant() == false
							&& StringUtils.isNotBlank(userFromDb.getSuperMerchantId())) || userFromDb.getUserType().equals(UserType.PARENTMERCHANT)) {
			} else {
				if (!pendingMappingRequestDao.findActiveMappingByEmailIdForActiveStatus(userFromDb.getEmailId())) {
					setResponse(CrmFieldConstants.USER_MERCHANT_MAPPING_NOT_SET.getValue());
					return SUCCESS;
				}
				if (!(chargingDetailsDao.isChargingDetailsSet(user.getPayId())
						|| surchargeDetailsDao.isSurchargeDetailsSet(user.getPayId()))) {
					setResponse(CrmFieldConstants.USER_CHARGINGDETAILS_NOT_SET_MSG.getValue());
					return SUCCESS;
				}
			}
			UserStatusType UserStatus = UserStatusType.valueOf(userStatus);
			userFromDb.setUserStatus(UserStatus);
			Date date = new Date();
			if (UserStatus.equals(UserStatusType.ACTIVE)) {
				userFromDb.setActivationDate(date);
			} else {
				userFromDb.setActivationDate(null);
			}

			if ((userFromDb.getUserType().equals(UserType.MERCHANT)
					&& StringUtils.isBlank(userFromDb.getSuperMerchantId())) || userFromDb.isSuperMerchant()) {

				ModeType mode = ModeType.valueOf(modeType);
				userFromDb.setModeType(mode);
			}
			userFromDb.setUpdateDate(date);
			userFromDb.setUpdatedBy(user.getEmailId());
			userDao.update(userFromDb);
			merchantGridViewService.addUserInMap(userFromDb);
			execute();
			setResponse("SUCCESS");
		} catch (Exception e) {
			logger.info("Exception, " + e);
			setResponse("Something went wrong !");
		}
		return SUCCESS;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(String userStatus) {
		this.userStatus = userStatus;
	}

	public List<Merchants> getListReseller() {
		return listReseller;
	}

	public void setListReseller(List<Merchants> listReseller) {
		this.listReseller = listReseller;
	}

	public List<Merchants> getListSuperMerchant() {
		return listSuperMerchant;
	}

	public void setListSuperMerchant(List<Merchants> listSuperMerchant) {
		this.listSuperMerchant = listSuperMerchant;
	}

	public List<Merchants> getListMerchant() {
		return listMerchant;
	}

	public void setListMerchant(List<Merchants> listMerchant) {
		this.listMerchant = listMerchant;
	}

	public List<Merchants> getListSubMerchant() {
		return listSubMerchant;
	}

	public void setListSubMerchant(List<Merchants> listSubMerchant) {
		this.listSubMerchant = listSubMerchant;
	}

	public List<Merchants> getListAcquirer() {
		return listAcquirer;
	}

	public void setListAcquirer(List<Merchants> listAcquirer) {
		this.listAcquirer = listAcquirer;
	}

	public List<Merchants> getListAgent() {
		return listAgent;
	}

	public void setListAgent(List<Merchants> listAgent) {
		this.listAgent = listAgent;
	}

	public List<Merchants> getListSubUser() {
		return listSubUser;
	}

	public void setListSubUser(List<Merchants> listSubUser) {
		this.listSubUser = listSubUser;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getModeType() {
		return modeType;
	}

	public void setModeType(String modeType) {
		this.modeType = modeType;
	}

	public List<Merchants> getListParentMerchant() {
		return listParentMerchant;
	}

	public void setListParentMerchant(List<Merchants> listParentMerchant) {
		this.listParentMerchant = listParentMerchant;
	}
	

}