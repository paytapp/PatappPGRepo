package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.New;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.ParentMerchantMappingDao;
import com.paymentgateway.commons.user.CustomerCategory;
import com.paymentgateway.commons.user.ParentMerchantMapping;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;

public class ParentMerchantMappingAction extends AbstractSecureAction {

	private static final long serialVersionUID = 3633315932760552341L;

	private static Logger logger = LoggerFactory.getLogger(ParentMerchantMappingAction.class.getName());

	@Autowired
	private ParentMerchantMappingDao parentMerchantMappingDao;

	private List<ParentMerchantMapping> parentMerchantMappingList = new ArrayList<>();

	private String parentPayId;
	private String merchantPayId;

	private String mappingData;

	private String response;
	private String responseMsg;

	@Override
	public String execute() {

		logger.info("inside execute() , Saving Parent Merchant Mapping");

		User sessionUser = (User) sessionMap.get(Constants.USER);

		try {
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {

				if (mappingData != null) {

					parentMerchantMappingDao.deleteMappingOldMapping(parentPayId, sessionUser);

					JSONArray mappingArray = new JSONArray(mappingData);

					for (int i = 0; i < mappingArray.length(); i++) {

						ParentMerchantMapping parentMerchantMapping = new ParentMerchantMapping();
						JSONObject jsonObj = mappingArray.getJSONObject(i);

						parentMerchantMapping.setActiveFlag(jsonObj.getBoolean("activeFlag"));
						parentMerchantMapping.setMerchantPayId(jsonObj.getString("merchantPayID"));
						if (jsonObj.getString("customerCategory").equalsIgnoreCase("silver")) {
							parentMerchantMapping.setCustomerCategory(CustomerCategory.SILVER.toString());
						}
						if (jsonObj.getString("customerCategory").equalsIgnoreCase("gold")) {
							parentMerchantMapping.setCustomerCategory(CustomerCategory.GOLD.toString());
						}
						if (jsonObj.getString("customerCategory").equalsIgnoreCase("diamond")) {
							parentMerchantMapping.setCustomerCategory(CustomerCategory.DIAMOND.toString());
						}
						if (jsonObj.getString("customerCategory").equalsIgnoreCase("platinum")) {
							parentMerchantMapping.setCustomerCategory(CustomerCategory.PLATINUM.toString());
						}
						if (jsonObj.getString("customerCategory").equalsIgnoreCase("default")) {
							parentMerchantMapping.setCustomerCategory(CustomerCategory.DEFAULT.toString());
						}
						parentMerchantMapping.setLoad(jsonObj.getString("load"));
						parentMerchantMapping.setParentPayId(parentPayId);

						parentMerchantMappingDao.createOrUpdate(parentMerchantMapping, sessionUser);
					}
				}
			}
		} catch (Exception e) {
			logger.error("exception in execute() ", e);
		}

		return SUCCESS;
	}

	public String fetchMappingByParentPayId() {

		try {

			if (StringUtils.isNotBlank(parentPayId))
				setParentMerchantMappingList(parentMerchantMappingDao.getMappingListUsingParentPayId(parentPayId));

		} catch (Exception e) {
			logger.error("exception in fetchMappingByParentPayId() ", e);
		}

		return SUCCESS;

	}

	public String deleteMapping() {

		try {

			if (StringUtils.isNotBlank(parentPayId) && StringUtils.isNotBlank(merchantPayId)) {
				boolean isDeleted = parentMerchantMappingDao.deleteMapping(parentPayId, merchantPayId);

				if (isDeleted) {
					setResponse("success");
					setResponseMsg("Deleted Successfully");
				} else {
					setResponse("failed");
					setResponseMsg("Mapping Failed to Delete");
				}

			}

		} catch (Exception e) {
			logger.error("exception in fetchMappingByParentPayId() ", e);
			setResponse("failed");
			setResponseMsg("Something Went Wrong");
		}

		return SUCCESS;

	}

	public List<ParentMerchantMapping> getParentMerchantMappingList() {
		return parentMerchantMappingList;
	}

	public void setParentMerchantMappingList(List<ParentMerchantMapping> parentMerchantMappingList) {
		this.parentMerchantMappingList = parentMerchantMappingList;
	}

	public String getMappingData() {
		return mappingData;
	}

	public void setMappingData(String mappingData) {
		this.mappingData = mappingData;
	}

	public String getParentPayId() {
		return parentPayId;
	}

	public void setParentPayId(String parentPayId) {
		this.parentPayId = parentPayId;
	}

	public String getMerchantPayId() {
		return merchantPayId;
	}

	public void setMerchantPayId(String merchantPayId) {
		this.merchantPayId = merchantPayId;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getResponseMsg() {
		return responseMsg;
	}

	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}

}
