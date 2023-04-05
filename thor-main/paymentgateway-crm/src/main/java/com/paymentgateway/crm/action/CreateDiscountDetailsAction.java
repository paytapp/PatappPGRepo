package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.DiscountDetailsDao;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.BusinessType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.Discount;
import com.paymentgateway.commons.util.IssuerType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TDRStatus;
/*
 * @author Rajit 
*/
public class CreateDiscountDetailsAction extends AbstractSecureAction {

	@Autowired
	private DiscountDetailsDao discountDetailsDao;
	
	@Autowired
	private UserDao userDao;
	
	private static final long serialVersionUID = -497906401989984364L;
	private static Logger logger = LoggerFactory.getLogger(CreateDiscountDetailsAction.class.getName());
	
	private String discountApplicableOn;
	private String discount;
	private String discountType;
	private String paymentType;
	private String issuerBank;
	private String mopType;
	private String paymentRegion;
	private String cardHolderType;
	private String amountSlab;
	private String emiDuration;
	private String fixedCharges;
	private String percentageCharges;
	private int length;
	private int start;
	private String response;
	private User sessionUser = new User();
	private List<Discount> aaData = new ArrayList<Discount>();
	private List<String> slab = new ArrayList<String>();
	private Collection<String> industryCategory = new ArrayList<String>();
	
	String discountArr[] = null;
	//String categoryArr[] = null;
	//String pgArr[] = null;
	String paymentTypeArr[] = null;
	String issuerBankArr[] = null; 
	String mopTypeArr[] = null;
	String paymentRegionArr[] = null;
	String cardHolderTypeArr[] = null;
	String amountSlabArr[] = null;
	String emiDurationArr[] = null;
	
	public String execute() {
		logger.info("inside create discount details action ");
		
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		
		if(StringUtils.isNotBlank(discount)) {
			discountArr = discount.split(",");
		}
		
		if(StringUtils.isNotBlank(paymentType)) {
			paymentTypeArr = paymentType.split(",");
		}
		if(StringUtils.isNotBlank(issuerBank)) {
			if(issuerBank.equalsIgnoreCase("All")) {
				issuerBankArr =  IssuerType.getAllIssuerName();
			} else {
				issuerBankArr = issuerBank.split(",");
			}
		} else {
			issuerBankArr = "NA".split(",");
		}
		
		if(StringUtils.isNotBlank(mopType)) {
			mopTypeArr = mopType.split(",");
		}
		if(StringUtils.isNotBlank(paymentRegion)) {
			paymentRegionArr = paymentRegion.split(",");
		}
		if(StringUtils.isNotBlank(cardHolderType)) {
			if(cardHolderType.equalsIgnoreCase("All")) {
				cardHolderTypeArr = CardHolderType.getCardHolderType();
			} else {
				cardHolderTypeArr = cardHolderType.split(",");
			}
		} else {
			cardHolderTypeArr = "NA".split(",");
		}
		if(StringUtils.isNotBlank(amountSlab)) {
			
			if(amountSlab.equalsIgnoreCase("All")) {
				List<String> slab = new ArrayList<String>();
				slab.add(PropertiesManager.propertiesMap.get("LimitSlab1MinAmount")+"-"+PropertiesManager.propertiesMap.get("LimitSlab1MaxAmount"));
				slab.add(PropertiesManager.propertiesMap.get("LimitSlab2MinAmount")+"-"+PropertiesManager.propertiesMap.get("LimitSlab2MaxAmount"));
				slab.add(PropertiesManager.propertiesMap.get("LimitSlab3MinAmount")+"-"+PropertiesManager.propertiesMap.get("LimitSlab3MaxAmount"));
				amountSlabArr = slab.toString().replace("[", "").replace("]", "").split(",");
			} else {
				amountSlabArr = amountSlab.split(",");
			}
		}
		if(StringUtils.isNotBlank(emiDuration)) {
			emiDurationArr = emiDuration.split(",");
		}
		
		if(discountApplicableOn.equalsIgnoreCase("Merchant")) {
			
			if(StringUtils.isNotBlank(discount)) {
				
				for(String discount : discountArr) {
					
					for(String paymentType : paymentTypeArr) {
						
							for(String issuerBank : issuerBankArr) {
								
								for(String mopType : mopTypeArr) {
									
									for(String paymentRegion : paymentRegionArr) {
									
										for(String cardHolderType : cardHolderTypeArr) {
										
											for(String amountSlab : amountSlabArr) {
										
												if(discountDetailsDao.checkAlreadyExist(discountApplicableOn, discount, /*discountType,*/ PaymentType.getpaymentName(paymentType),
														issuerBank.trim(), mopType, paymentRegion, cardHolderType, amountSlab, emiDuration, TDRStatus.ACTIVE.getName())) {
													
													setResponse("Discount Detail Already Exists");
													
												} else {
													Discount discountObj = new Discount();
													discountObj = createNewDiscountObject(discountObj, discountApplicableOn, discount, discountType, PaymentType.getpaymentName(paymentType),
															issuerBank.trim(), mopType, paymentRegion, cardHolderType, amountSlab, emiDuration, fixedCharges, percentageCharges, 
															sessionUser.getEmailId(), TDRStatus.ACTIVE.getName());
													
													discountDetailsDao.insert(discountObj);
													setResponse("Discount Detail Save Successfully");
												}
											}
										}
									}
								}
							}
					}
				}
			}
		} else if (discountApplicableOn.equalsIgnoreCase("Category")) {
			
			if(StringUtils.isNotBlank(discount)) {
				
				for(String discount : discountArr) {
					
					for(String paymentType : paymentTypeArr) {
						
							for(String issuerBank : issuerBankArr) {
								
								for(String mopType : mopTypeArr) {
									
									for(String paymentRegion : paymentRegionArr) {
									
										for(String cardHolderType : cardHolderTypeArr) {
										
											for(String amountSlab : amountSlabArr) {
										
												if(discountDetailsDao.checkAlreadyExist(discountApplicableOn, discount, /*discountType,*/ PaymentType.getpaymentName(paymentType),
														issuerBank.trim(), mopType, paymentRegion, cardHolderType, amountSlab, emiDuration, TDRStatus.ACTIVE.getName())) {
													
													setResponse("Discount Detail Already Exists");
													
												} else {
													Discount discountObj = new Discount();
													discountObj = createNewDiscountObject(discountObj, discountApplicableOn, discount, discountType, PaymentType.getpaymentName(paymentType),
															issuerBank.trim(), mopType, paymentRegion, cardHolderType, amountSlab, emiDuration, fixedCharges, percentageCharges, 
															sessionUser.getEmailId(), TDRStatus.ACTIVE.getName());
													
													discountDetailsDao.insert(discountObj);
													setResponse("Discount Detail Save Successfully");
												}
											}
										}
									}
								}
							}
					}
				}
			}
			
		} else {
			if(discountApplicableOn.equalsIgnoreCase("PaymentGateway")) {
				discount = "PaymentGateway";
					
					for(String paymentType : paymentTypeArr) {
						
							for(String issuerBank : issuerBankArr) {
								
								for(String mopType : mopTypeArr) {
									
									for(String paymentRegion : paymentRegionArr) {
									
										for(String cardHolderType : cardHolderTypeArr) {
										
											for(String amountSlab : amountSlabArr) {
										
												if(discountDetailsDao.checkAlreadyExist(discountApplicableOn, discount,/* discountType,*/ PaymentType.getpaymentName(paymentType),
														issuerBank.trim(), mopType, paymentRegion, cardHolderType, amountSlab, emiDuration, TDRStatus.ACTIVE.getName())) {
													
													setResponse("Discount Detail Already Exists");
													
												} else {
													Discount discountObj = new Discount();
													discountObj = createNewDiscountObject(discountObj, discountApplicableOn, discount, discountType,  PaymentType.getpaymentName(paymentType),
															issuerBank.trim(), mopType, paymentRegion, cardHolderType, amountSlab, emiDuration, fixedCharges, percentageCharges, 
															sessionUser.getEmailId(), TDRStatus.ACTIVE.getName());
													
													discountDetailsDao.insert(discountObj);
													setResponse("Discount Detail Save Successfully");
												}
											}
										}
									}
								}
							}
					}
			}
			
		}
		setAaData(discountDetailsDao.getAllActiveDetails());
		return SUCCESS;
	}
	
	private Discount createNewDiscountObject(Discount discountObj, String discounApplicationOn, String discount, String discountType, String paymentType,
			String issuerBank, String mopType, String paymentRegion, String cardHolderType, String amountSlab, String emiDuration, String fixedCharges, String percentageCharges, 
			String loginEmailId, String status) {
		
		
		discountObj.setDiscountApplicableOn(discounApplicationOn);
		discountObj.setDiscount(discount); 
		discountObj.setDiscountType(discountType);
		discountObj.setPaymentType(paymentType);
		discountObj.setMopType(mopType);
		discountObj.setPaymentRegion(paymentRegion);
		discountObj.setSlab(amountSlab);
		discountObj.setFixedCharges(fixedCharges);
		discountObj.setPercentageCharges(percentageCharges);
		discountObj.setRequestedBy(loginEmailId);
		discountObj.setStatus(status);
		
		if(StringUtils.isNotBlank(emiDuration)) {
			discountObj.setEmiDuration(emiDuration);
		}
		if(StringUtils.isNotBlank(issuerBank) && !issuerBank.equalsIgnoreCase("NA")) {
			discountObj.setIssuerBank(issuerBank);
		}
		if(StringUtils.isNotBlank(cardHolderType) && !cardHolderType.equalsIgnoreCase("NA")) {
			discountObj.setCardHolderType(cardHolderType);
		}
		return discountObj;
	}
	
	public String getActiveDiscountDetails() {
		
		setAaData(discountDetailsDao.getAllActiveDetails());
		List<String> slabList = new ArrayList<String>();
		Map<String, String> category = new HashMap<String, String>();
		
		slabList.add(PropertiesManager.propertiesMap.get("LimitSlab1MinAmount")+"-"+PropertiesManager.propertiesMap.get("LimitSlab1MaxAmount"));
		slabList.add(PropertiesManager.propertiesMap.get("LimitSlab2MinAmount")+"-"+PropertiesManager.propertiesMap.get("LimitSlab2MaxAmount"));
		slabList.add(PropertiesManager.propertiesMap.get("LimitSlab3MinAmount")+"-"+PropertiesManager.propertiesMap.get("LimitSlab3MaxAmount"));
		setSlab(slabList);
		
		category = BusinessType.getIndustryCategoryList();
		setIndustryCategory(category.values());
		return SUCCESS;
	}
	
	public String editDiscountDetail() {
		
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		
		if(discountApplicableOn.equalsIgnoreCase("MERCHANT")) {
			discount = userDao.getpayIdByBusinessName(discount);
		} else if(discountApplicableOn.equalsIgnoreCase("PAYMENTGATEWAY")) {
			discount = "paymentGateway";
		}
		
		discountDetailsDao.inActiveDiscountDetail(discountApplicableOn, discount, discountType, paymentType, issuerBank, mopType, paymentRegion, cardHolderType, amountSlab,
				emiDuration, sessionUser.getEmailId());
			
			Discount discountObj = new Discount();
			
			
			discountObj = createNewDiscountObject(discountObj, discountApplicableOn, discount, discountType, paymentType,
					issuerBank, mopType, paymentRegion, cardHolderType, amountSlab, emiDuration, fixedCharges, percentageCharges, 
					sessionUser.getEmailId(), TDRStatus.ACTIVE.getName());
			
			discountDetailsDao.insert(discountObj);
		
			setAaData(discountDetailsDao.getAllActiveDetails());
			setResponse("Discount Detail Edit Successfully");
			return SUCCESS;
	}
	
	public String deleteDiscountDetail() {
		
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		if(discountApplicableOn.equalsIgnoreCase("MERCHANT")) {
			discount = userDao.getpayIdByBusinessName(discount);
		} else if(discountApplicableOn.equalsIgnoreCase("PAYMENTGATEWAY")) {
			discount = "PaymentGateway";
		}

		
		discountDetailsDao.inActiveDiscountDetail(discountApplicableOn, discount, discountType, paymentType, issuerBank, mopType, paymentRegion, cardHolderType, amountSlab, 
				emiDuration, sessionUser.getEmailId());
		
		setAaData(discountDetailsDao.getAllActiveDetails());
		setResponse("Discount Detail Delete Successfully");
		return SUCCESS;
	}
	
	public String filterDiscountDetail() {
		
			if(discountApplicableOn.equalsIgnoreCase("PAYMENTGATEWAY")) {
				discount = "PaymentGateway";
				setAaData(discountDetailsDao.getFilteredDetails(discountApplicableOn, discount));
				
			} else {
				setAaData(discountDetailsDao.getFilteredDetails(discountApplicableOn, discount));
			}
			return SUCCESS;
	}
	
	public void validate() {
		
	}
	
	public String getDiscountApplicableOn() {
		return discountApplicableOn;
	}
	public void setDiscountApplicableOn(String discountApplicableOn) {
		this.discountApplicableOn = discountApplicableOn;
	}
	public String getDiscountType() {
		return discountType;
	}
	public void setDiscountType(String discountType) {
		this.discountType = discountType;
	}
	public String getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}
	public String getIssuerBank() {
		return issuerBank;
	}
	public void setIssuerBank(String issuerBank) {
		this.issuerBank = issuerBank;
	}
	public String getMopType() {
		return mopType;
	}
	public void setMopType(String mopType) {
		this.mopType = mopType;
	}
	public String getPaymentRegion() {
		return paymentRegion;
	}
	public void setPaymentRegion(String paymentRegion) {
		this.paymentRegion = paymentRegion;
	}
	public String getCardHolderType() {
		return cardHolderType;
	}
	public void setCardHolderType(String cardHolderType) {
		this.cardHolderType = cardHolderType;
	}
	public String getAmountSlab() {
		return amountSlab;
	}
	public void setAmountSlab(String amountSlab) {
		this.amountSlab = amountSlab;
	}
	public String getEmiDuration() {
		return emiDuration;
	}
	public void setEmiDuration(String emiDuration) {
		this.emiDuration = emiDuration;
	}
	public List<Discount> getAaData() {
		return aaData;
	}
	public void setAaData(List<Discount> aaData) {
		this.aaData = aaData;
	}
	public String getFixedCharges() {
		return fixedCharges;
	}
	public void setFixedCharges(String fixedCharges) {
		this.fixedCharges = fixedCharges;
	}
	public String getPercentageCharges() {
		return percentageCharges;
	}
	public void setPercentageCharges(String percentageCharges) {
		this.percentageCharges = percentageCharges;
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
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	public List<String> getSlab() {
		return slab;
	}
	public void setSlab(List<String> slab) {
		this.slab = slab;
	}
	public Collection<String> getIndustryCategory() {
		return industryCategory;
	}

	public void setIndustryCategory(Collection<String> collection) {
		this.industryCategory = collection;
	}
	public String getDiscount() {
		return discount;
	}

	public void setDiscount(String discount) {
		this.discount = discount;
	}


}
