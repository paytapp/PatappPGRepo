package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.PendingMappingRequestDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.kms.EncryptDecryptService;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.ProductionReportObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;

public class ProductionDetailsReportAction extends AbstractSecureAction {

	@Autowired
	private CrmValidator validator;

	@Autowired
	private UserDao userDao;
	
    @Autowired
	EncryptDecryptService encryptDecryptService;

	@Autowired
	private PendingMappingRequestDao pendingMappingRequestDao;

	private static Logger logger = LoggerFactory.getLogger(ProductionDetailsReportAction.class.getName());
	private static final long serialVersionUID = 8733557567586189516L;

	private String merchant;
	private String acquirer;
	private String subMerchant;
	private String currency;

	private List<ProductionReportObject> aaData;

	public String execute() {
		List<ProductionReportObject> productionList = new ArrayList<ProductionReportObject>();
		try {

			User user = new User();
			Session session = null;
			if(merchant.equalsIgnoreCase("ALL") && acquirer.equalsIgnoreCase("ALL") && currency.equalsIgnoreCase("ALL")) {
				List<Merchants> merchantList = userDao.getMerchantActiveList(); 
				for(Merchants merchants : merchantList) {
					String emailId = merchants.getEmailId();
					String payId = merchants.getPayId();
					user = new UserDao().findByEmailId(emailId);
					Set<Account> userAccountSet = user.getAccounts();
					if(userAccountSet.size()>0) {
					   for (Account acc : userAccountSet) {
						
							ProductionReportObject production = new ProductionReportObject();
							Set<AccountCurrency> accCurrSet = acc.getAccountCurrencySet();
							
							for (AccountCurrency accCurr : accCurrSet) {
							production.setMerchant(merchants.getBusinessName());
							production.setPayId(payId);
							production.setMerchantId(accCurr.getMerchantId());
							production.setTxnKey(accCurr.getTxnKey());
							
							if (!StringUtils.isAnyEmpty(accCurr.getPassword())) {
								String decryptedPassword = encryptDecryptService.decrypt(payId,
										accCurr.getPassword());
								production.setPassword(decryptedPassword);
							}
							if (!StringUtils.isAnyEmpty(accCurr.getAdf6())) {
								String decryptedPassword = encryptDecryptService.decrypt(payId,
										accCurr.getAdf6());
								production.setAdf6(decryptedPassword);
							}
							if (!StringUtils.isAnyEmpty(accCurr.getAdf7())) {
								String decryptedPassword = encryptDecryptService.decrypt(payId,
										accCurr.getAdf7());
								production.setAdf7(decryptedPassword);
							}
							
							production.setAdf1(accCurr.getAdf1());
							production.setAdf2(accCurr.getAdf2());
							production.setAdf3(accCurr.getAdf3());
							production.setAdf4(accCurr.getAdf4());
							production.setAdf5(accCurr.getAdf5());
							production.setAdf8(accCurr.getAdf8());
							production.setAdf9(accCurr.getAdf9());
							production.setAdf10(accCurr.getAdf10());
							production.setAdf11(accCurr.getAdf11());
							productionList.add(production);
							
							}
						}
					}else {
						continue;
					}
					
				}
				setAaData(productionList);
				return SUCCESS;			
					
					
			}else if(!(merchant.equalsIgnoreCase("ALL")) && acquirer.equalsIgnoreCase("ALL") && currency.equalsIgnoreCase("ALL")) {
				String emailId = userDao.getEmailIdByPayId(merchant);
				user = new UserDao().findByEmailId(emailId);
				Set<Account> userAccountSet = user.getAccounts();
				if(userAccountSet.size()>0) {
				for (Account acc : userAccountSet) {
						ProductionReportObject production = new ProductionReportObject();
						Set<AccountCurrency> accCurrSet = acc.getAccountCurrencySet();
						
						for (AccountCurrency accCurr : accCurrSet) {
						production.setMerchant(userDao.getBusinessNameByPayId(merchant));
						production.setPayId(merchant);
						production.setMerchantId(accCurr.getMerchantId());
						production.setTxnKey(accCurr.getTxnKey());
						
						if (!StringUtils.isAnyEmpty(accCurr.getPassword())) {
							String decryptedPassword = encryptDecryptService.decrypt(merchant,
									accCurr.getPassword());
							production.setPassword(decryptedPassword);
						}
						if (!StringUtils.isAnyEmpty(accCurr.getAdf6())) {
							String decryptedPassword = encryptDecryptService.decrypt(merchant,
									accCurr.getAdf6());
							production.setAdf6(decryptedPassword);
						}
						if (!StringUtils.isAnyEmpty(accCurr.getAdf7())) {
							String decryptedPassword = encryptDecryptService.decrypt(merchant,
									accCurr.getAdf7());
							production.setAdf7(decryptedPassword);
						}
						
						production.setAdf1(accCurr.getAdf1());
						production.setAdf2(accCurr.getAdf2());
						production.setAdf3(accCurr.getAdf3());
						production.setAdf4(accCurr.getAdf4());
						production.setAdf5(accCurr.getAdf5());
						production.setAdf8(accCurr.getAdf8());
						production.setAdf9(accCurr.getAdf9());
						production.setAdf10(accCurr.getAdf10());
						production.setAdf11(accCurr.getAdf11());
						productionList.add(production);
						
						}
					  }
					}else {
						setAaData(productionList);
						return SUCCESS;
					}
			setAaData(productionList);
			return SUCCESS;
			}else if((merchant.equalsIgnoreCase("ALL")) && !(acquirer.equalsIgnoreCase("ALL")) && currency.equalsIgnoreCase("ALL")) {
				
				    List<Merchants> merchantList = userDao.getMerchantActiveList(); 
				    for(Merchants merchants : merchantList) {
				    	String emailId = merchants.getEmailId();
						String payId = merchants.getPayId();
						user = new UserDao().findByEmailId(emailId);
						Set<Account> userAccountSet = user.getAccounts();
						if(userAccountSet.size()>0) {
							Account account = user.getAccountUsingAcquirerCode1(acquirer);
							if(!(account==null)) {
							for(Account acc : userAccountSet) {
								if(acc.getAcquirerPayId().equals(account.getAcquirerPayId())) {

									ProductionReportObject production = new ProductionReportObject();
									Set<AccountCurrency> accCurrSet = acc.getAccountCurrencySet();
									
									for (AccountCurrency accCurr : accCurrSet) {
									production.setMerchant(merchants.getBusinessName());
									production.setPayId(payId);
									production.setMerchantId(accCurr.getMerchantId());
									production.setTxnKey(accCurr.getTxnKey());
									
									if (!StringUtils.isAnyEmpty(accCurr.getPassword())) {
										String decryptedPassword = encryptDecryptService.decrypt(payId,
												accCurr.getPassword());
										production.setPassword(decryptedPassword);
									}
									if (!StringUtils.isAnyEmpty(accCurr.getAdf6())) {
										String decryptedPassword = encryptDecryptService.decrypt(payId,
												accCurr.getAdf6());
										production.setAdf6(decryptedPassword);
									}
									if (!StringUtils.isAnyEmpty(accCurr.getAdf7())) {
										String decryptedPassword = encryptDecryptService.decrypt(payId,
												accCurr.getAdf7());
										production.setAdf7(decryptedPassword);
									}
									
									production.setAdf1(accCurr.getAdf1());
									production.setAdf2(accCurr.getAdf2());
									production.setAdf3(accCurr.getAdf3());
									production.setAdf4(accCurr.getAdf4());
									production.setAdf5(accCurr.getAdf5());
									production.setAdf8(accCurr.getAdf8());
									production.setAdf9(accCurr.getAdf9());
									production.setAdf10(accCurr.getAdf10());
									production.setAdf11(accCurr.getAdf11());
									productionList.add(production);
									
									}//for end
								  }else {
									  continue;
								  }
								}//end for
							}else {
									continue;
									}
						}else {
							continue;
						}
				    	
				    }
					
					
				setAaData(productionList);
				return SUCCESS;
		}else if(!(merchant.equalsIgnoreCase("ALL")) && !(acquirer.equalsIgnoreCase("ALL")) && currency.equalsIgnoreCase("ALL")) {
			String emailId = userDao.getEmailIdByPayId(merchant);
			user = new UserDao().findByEmailId(emailId);
			Set<Account> userAccountSet = user.getAccounts();
			if(userAccountSet.size()>0) {
				Account account = user.getAccountUsingAcquirerCode1(acquirer);
				if(!(account==null)) {
			for (Account acc : userAccountSet) {
				if(acc.getAcquirerPayId().equals(account.getAcquirerPayId())) {
					ProductionReportObject production = new ProductionReportObject();
					Set<AccountCurrency> accCurrSet = acc.getAccountCurrencySet();
					
					for (AccountCurrency accCurr : accCurrSet) {
					production.setMerchant(userDao.getBusinessNameByPayId(merchant));
					production.setPayId(merchant);
					production.setMerchantId(accCurr.getMerchantId());
					production.setTxnKey(accCurr.getTxnKey());
					
					if (!StringUtils.isAnyEmpty(accCurr.getPassword())) {
						String decryptedPassword = encryptDecryptService.decrypt(merchant,
								accCurr.getPassword());
						production.setPassword(decryptedPassword);
					}
					if (!StringUtils.isAnyEmpty(accCurr.getAdf6())) {
						String decryptedPassword = encryptDecryptService.decrypt(merchant,
								accCurr.getAdf6());
						production.setAdf6(decryptedPassword);
					}
					if (!StringUtils.isAnyEmpty(accCurr.getAdf7())) {
						String decryptedPassword = encryptDecryptService.decrypt(merchant,
								accCurr.getAdf7());
						production.setAdf7(decryptedPassword);
					}
					
					production.setAdf1(accCurr.getAdf1());
					production.setAdf2(accCurr.getAdf2());
					production.setAdf3(accCurr.getAdf3());
					production.setAdf4(accCurr.getAdf4());
					production.setAdf5(accCurr.getAdf5());
					production.setAdf8(accCurr.getAdf8());
					production.setAdf9(accCurr.getAdf9());
					production.setAdf10(accCurr.getAdf10());
					production.setAdf11(accCurr.getAdf11());
					productionList.add(production);
					
					}//for end
					
				}else {
					continue;
				}
				
			}//for end
			}//if end
				else {
					setAaData(productionList);
					return SUCCESS;
				}
			}//if end
			setAaData(productionList);
			return SUCCESS;
		}else if((merchant.equalsIgnoreCase("ALL")) && (acquirer.equalsIgnoreCase("ALL")) && !(currency.equalsIgnoreCase("ALL"))) {
				List<Merchants> merchantList = userDao.getMerchantActiveList(); 
				for(Merchants merchants : merchantList) {
					String emailId = merchants.getEmailId();
					String payId = merchants.getPayId();
					user = new UserDao().findByEmailId(emailId);
					Set<Account> userAccountSet = user.getAccounts();
					if(userAccountSet.size()>0) {
						   for (Account acc : userAccountSet) {
							
								ProductionReportObject production = new ProductionReportObject();
								Set<AccountCurrency> accCurrSet = acc.getAccountCurrencySet();
								
								for (AccountCurrency accCurr : accCurrSet) {
									String currency1 = accCurr.getCurrencyCode();
									if(currency1.equals(currency)) {
										production.setPayId(merchants.getPayId());
										production.setMerchant(merchants.getBusinessName());
										production.setMerchantId(accCurr.getMerchantId());
										production.setTxnKey(accCurr.getTxnKey());
										
										if (!StringUtils.isAnyEmpty(accCurr.getPassword())) {
											String decryptedPassword = encryptDecryptService.decrypt(payId,
													accCurr.getPassword());
											production.setPassword(decryptedPassword);
										}
										if (!StringUtils.isAnyEmpty(accCurr.getAdf6())) {
											String decryptedPassword = encryptDecryptService.decrypt(payId,
													accCurr.getAdf6());
											production.setAdf6(decryptedPassword);
										}
										if (!StringUtils.isAnyEmpty(accCurr.getAdf7())) {
											String decryptedPassword = encryptDecryptService.decrypt(payId,
													accCurr.getAdf7());
											production.setAdf7(decryptedPassword);
										}
										
										production.setAdf1(accCurr.getAdf1());
										production.setAdf2(accCurr.getAdf2());
										production.setAdf3(accCurr.getAdf3());
										production.setAdf4(accCurr.getAdf4());
										production.setAdf5(accCurr.getAdf5());
										production.setAdf8(accCurr.getAdf8());
										production.setAdf9(accCurr.getAdf9());
										production.setAdf10(accCurr.getAdf10());
										production.setAdf11(accCurr.getAdf11());
										productionList.add(production);
										
									}//if end
									else {
										continue;
									}
											
									}//for end
						   }//for end
					}//if end
					else {
						continue;
					}
				}//for end
			 setAaData(productionList);
			return SUCCESS;
		}else if(!(merchant.equalsIgnoreCase("ALL")) && (acquirer.equalsIgnoreCase("ALL")) && !(currency.equalsIgnoreCase("ALL"))) {
			String emailId = userDao.getEmailIdByPayId(merchant);
			user = new UserDao().findByEmailId(emailId);
			Set<Account> userAccountSet = user.getAccounts();
			if(userAccountSet.size()>0) {
			for (Account acc : userAccountSet) {
					ProductionReportObject production = new ProductionReportObject();
					Set<AccountCurrency> accCurrSet = acc.getAccountCurrencySet();
					
					for (AccountCurrency accCurr : accCurrSet) {
						String currency1 = accCurr.getCurrencyCode();
						if(currency1.equals(currency)) {
							production.setPayId(merchant);
							production.setMerchant(userDao.getBusinessNameByPayId(merchant));
							production.setMerchantId(accCurr.getMerchantId());
							production.setTxnKey(accCurr.getTxnKey());
							
							if (!StringUtils.isAnyEmpty(accCurr.getPassword())) {
								String decryptedPassword = encryptDecryptService.decrypt(merchant,
										accCurr.getPassword());
								production.setPassword(decryptedPassword);
							}
							if (!StringUtils.isAnyEmpty(accCurr.getAdf6())) {
								String decryptedPassword = encryptDecryptService.decrypt(merchant,
										accCurr.getAdf6());
								production.setAdf6(decryptedPassword);
							}
							if (!StringUtils.isAnyEmpty(accCurr.getAdf7())) {
								String decryptedPassword = encryptDecryptService.decrypt(merchant,
										accCurr.getAdf7());
								production.setAdf7(decryptedPassword);
							}
							
							production.setAdf1(accCurr.getAdf1());
							production.setAdf2(accCurr.getAdf2());
							production.setAdf3(accCurr.getAdf3());
							production.setAdf4(accCurr.getAdf4());
							production.setAdf5(accCurr.getAdf5());
							production.setAdf8(accCurr.getAdf8());
							production.setAdf9(accCurr.getAdf9());
							production.setAdf10(accCurr.getAdf10());
							production.setAdf11(accCurr.getAdf11());
							productionList.add(production);
							
						}//if end
						else {
							continue;
						}
								
						}//for end
					}//for end
			}//if end	
		setAaData(productionList);
		return SUCCESS;
		
		}else if((merchant.equalsIgnoreCase("ALL")) && !(acquirer.equalsIgnoreCase("ALL")) && !(currency.equalsIgnoreCase("ALL"))){

		    List<Merchants> merchantList = userDao.getMerchantActiveList(); 
		    for(Merchants merchants : merchantList) {
		    	String emailId = merchants.getEmailId();
				String payId = merchants.getPayId();
				user = new UserDao().findByEmailId(emailId);
				Set<Account> userAccountSet = user.getAccounts();
				if(userAccountSet.size()>0) {
					Account account = user.getAccountUsingAcquirerCode1(acquirer);
					if(!(account==null)) {
					for(Account acc : userAccountSet) {
						if(acc.getAcquirerPayId().equals(account.getAcquirerPayId())) {

							ProductionReportObject production = new ProductionReportObject();
							Set<AccountCurrency> accCurrSet = acc.getAccountCurrencySet();
							
							for (AccountCurrency accCurr : accCurrSet) {
								String currency1 = accCurr.getCurrencyCode();
								if(currency1.equals(currency)) {
									production.setMerchant(merchants.getBusinessName());
									production.setPayId(payId);
									production.setMerchantId(accCurr.getMerchantId());
									production.setTxnKey(accCurr.getTxnKey());
									
									if (!StringUtils.isAnyEmpty(accCurr.getPassword())) {
										String decryptedPassword = encryptDecryptService.decrypt(payId,
												accCurr.getPassword());
										production.setPassword(decryptedPassword);
									}
									if (!StringUtils.isAnyEmpty(accCurr.getAdf6())) {
										String decryptedPassword = encryptDecryptService.decrypt(payId,
												accCurr.getAdf6());
										production.setAdf6(decryptedPassword);
									}
									if (!StringUtils.isAnyEmpty(accCurr.getAdf7())) {
										String decryptedPassword = encryptDecryptService.decrypt(payId,
												accCurr.getAdf7());
										production.setAdf7(decryptedPassword);
									}
									
									production.setAdf1(accCurr.getAdf1());
									production.setAdf2(accCurr.getAdf2());
									production.setAdf3(accCurr.getAdf3());
									production.setAdf4(accCurr.getAdf4());
									production.setAdf5(accCurr.getAdf5());
									production.setAdf8(accCurr.getAdf8());
									production.setAdf9(accCurr.getAdf9());
									production.setAdf10(accCurr.getAdf10());
									production.setAdf11(accCurr.getAdf11());
									productionList.add(production);
									
								}//if end
								else {
									continue;
								}
							}//for end
						}else {
							  continue;
						  }
						}//end for
					}else {
							continue;
							}
				}else {
					continue;
				}
		    	
		    }
				
				
			setAaData(productionList);
			return SUCCESS;
		}else {
			String emailId = userDao.getEmailIdByPayId(merchant);
			user = new UserDao().findByEmailId(emailId);
			Set<Account> userAccountSet = user.getAccounts();
			if(userAccountSet.size()>0) {
				Account account = user.getAccountUsingAcquirerCode1(acquirer);
				if(!(account==null)) {
			for (Account acc : userAccountSet) {
				if(acc.getAcquirerPayId().equals(account.getAcquirerPayId())) {
					ProductionReportObject production = new ProductionReportObject();
					Set<AccountCurrency> accCurrSet = acc.getAccountCurrencySet();
					
					for (AccountCurrency accCurr : accCurrSet) {
						String currency1 = accCurr.getCurrencyCode();
						if(currency1.equals(currency)) {
							production.setMerchant(userDao.getBusinessNameByPayId(merchant));
							production.setPayId(merchant);
							production.setMerchantId(accCurr.getMerchantId());
							production.setTxnKey(accCurr.getTxnKey());
							
							if (!StringUtils.isAnyEmpty(accCurr.getPassword())) {
								String decryptedPassword = encryptDecryptService.decrypt(merchant,
										accCurr.getPassword());
								production.setPassword(decryptedPassword);
							}
							if (!StringUtils.isAnyEmpty(accCurr.getAdf6())) {
								String decryptedPassword = encryptDecryptService.decrypt(merchant,
										accCurr.getAdf6());
								production.setAdf6(decryptedPassword);
							}
							if (!StringUtils.isAnyEmpty(accCurr.getAdf7())) {
								String decryptedPassword = encryptDecryptService.decrypt(merchant,
										accCurr.getAdf7());
								production.setAdf7(decryptedPassword);
							}
							
							production.setAdf1(accCurr.getAdf1());
							production.setAdf2(accCurr.getAdf2());
							production.setAdf3(accCurr.getAdf3());
							production.setAdf4(accCurr.getAdf4());
							production.setAdf5(accCurr.getAdf5());
							production.setAdf8(accCurr.getAdf8());
							production.setAdf9(accCurr.getAdf9());
							production.setAdf10(accCurr.getAdf10());
							production.setAdf11(accCurr.getAdf11());
							productionList.add(production);
							
						}//if end
						else {
							continue;
						}
					}//for end
				}else {
					continue;
				}
				
			}//for end
			}//if end
				else {
					setAaData(productionList);
					return SUCCESS;
				}
			}//if end
			setAaData(productionList);
			return SUCCESS;
		}
			
		}catch(Exception e){
		logger.info("Exception Caught "+e);
     }
		return SUCCESS;
   }
	
	public void validate() {		
	if ((validator.validateBlankField(getAcquirer()) || (getAcquirer().equals(CrmFieldConstants.ALL.getValue())))) {
		} else if (!(validator.validateField(CrmFieldType.ACQUIRER, getAcquirer()))) {
		addFieldError(CrmFieldType.ACQUIRER.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
	}
	if ((validator.validateBlankField(getMerchant())) || (getMerchant().equals(CrmFieldConstants.ALL.getValue()))) {
		} else if (!(validator.validateField(CrmFieldType.PAY_ID, getMerchant()))) {
		addFieldError(CrmFieldType.PAY_ID.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
	}
	if ((validator.validateBlankField(getCurrency()) || (getCurrency().equals(CrmFieldConstants.ALL.getValue())))) {
		} else if (!(validator.validateField(CrmFieldType.CURRENCY, getCurrency()))) {
		addFieldError(CrmFieldType.CURRENCY.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
	}
	}

	public List<ProductionReportObject> getAaData() {
		return aaData;
	}

	public void setAaData(List<ProductionReportObject> aadata) {
		this.aaData = aadata;
	}

	public String getMerchant() {
		return merchant;
	}

	public void setMerchant(String merchant) {
		this.merchant = merchant;
	}

	public String getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

}

