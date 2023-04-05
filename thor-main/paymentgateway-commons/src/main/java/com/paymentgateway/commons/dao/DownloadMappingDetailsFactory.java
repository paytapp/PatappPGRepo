package com.paymentgateway.commons.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.Mop;
import com.paymentgateway.commons.user.Payment;
import com.paymentgateway.commons.user.ProductionReportObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;

@Component
public class DownloadMappingDetailsFactory {

	private static Logger logger = LoggerFactory.getLogger(DownloadMappingDetailsFactory.class.getName());
	
	@Autowired
	private UserDao userDao;
	
	public List<ProductionReportObject> getActiveMappingList(String merchant, String acquirer , String currency ,User sessionUser){
		List<ProductionReportObject> mappingList = new ArrayList<ProductionReportObject>();
		try {
			User user = new User();
			List<Merchants> merchantList = userDao.getMerchantActiveList();
			for(Merchants merchants : merchantList) {
				String emailId = merchants.getEmailId();
				String payId = merchants.getPayId();
				user = new UserDao().findByEmailId(emailId);
				Set<Account> userAccountSet = user.getAccounts();
				if(userAccountSet.size()>0) {
				   for (Account acc : userAccountSet) {
					   Set<Payment> savedPaymentSet = acc.getPayments();
					   if(savedPaymentSet.size()>0) {
					   for (Payment payment : savedPaymentSet) {
						   Set<Mop> mopSet = payment.getMops();
						   if(mopSet.size()>0) {
							for (Mop mop : mopSet) {
								ProductionReportObject production = new ProductionReportObject();
					          if(payment.getPaymentType().getName().equalsIgnoreCase("Net Banking") || payment.getPaymentType().getName().equalsIgnoreCase("Wallet")) {
					        	  production.setCode(mop.getMopType().getCode());
					          }//end if
					          else {
					        	  production.setMop(mop.getMopType().getName());
					          }//end else
					          production.setMerchant(merchants.getBusinessName());
				              production.setPayId(payId);
				              production.setPaymentType(payment.getPaymentType().getName());
				              mappingList.add(production);
							}//end for
						   }//end if
						   else {
							   continue;
						   }//end else
						  }//end for
					  }//end if
					  else {
						   continue;
					 }
				   }//end for
				}//end if
				else {
					continue;
				}
			}//end for
				
			return mappingList;
		}catch(Exception e) {
			logger.error("Exception", e);
		}
		return mappingList;
	}
	
	public List<ProductionReportObject> getActiveMappingListByMerchant(String merchant, String acquirer , String currency ,User sessionUser){
		List<ProductionReportObject> mappingList = new ArrayList<ProductionReportObject>();
		try {
			User user = new User();
			String emailId = userDao.getEmailIdByPayId(merchant);
			user = new UserDao().findByEmailId(emailId);
			Set<Account> userAccountSet = user.getAccounts();
			if(userAccountSet.size()>0) {
			for (Account acc : userAccountSet) {
				   Set<Payment> savedPaymentSet = acc.getPayments();
				   if(savedPaymentSet.size()>0) {
				   for (Payment payment : savedPaymentSet) {
					   Set<Mop> mopSet = payment.getMops();
					   if(mopSet.size()>0) {
						for (Mop mop : mopSet) {
							ProductionReportObject production = new ProductionReportObject();
				          if(payment.getPaymentType().getName().equalsIgnoreCase("Net Banking") || payment.getPaymentType().getName().equalsIgnoreCase("Wallet")) {
				        	  production.setCode(mop.getMopType().getCode());
				          }//end if
				          else {
				        	  production.setMop(mop.getMopType().getName());
				          }//end else
				          production.setMerchant(userDao.getBusinessNameByPayId(merchant));
						  production.setPayId(merchant);
			              production.setPaymentType(payment.getPaymentType().getName());
			              mappingList.add(production);
						}//end for
					   }//end if
					   else {
						   continue;
					   }//end else
					  }//end for
				  }//end if
				  else {
					   continue;
				 }
				
			}//end for
			}//end if
		
		
		return mappingList;
		
		}catch(Exception e) {
			logger.error("Exception", e);
		}
		return mappingList;
		}
		
	public List<ProductionReportObject> getMappingListByMerchantAcquirer(String merchant, String acquirer , String currency ,User sessionUser){
		List<ProductionReportObject> mappingList = new ArrayList<ProductionReportObject>();
		try {
			User user = new User();
			String emailId = userDao.getEmailIdByPayId(merchant);
			user = new UserDao().findByEmailId(emailId);
			Set<Account> userAccountSet = user.getAccounts();
			if(userAccountSet.size()>0) {
				Account account = user.getAccountUsingAcquirerCode1(acquirer);
				if(!(account==null)) {
			for (Account acc : userAccountSet) {
				if(acc.getAcquirerPayId().equals(account.getAcquirerPayId())) {
					Set<Payment> savedPaymentSet = acc.getPayments();
					   if(savedPaymentSet.size()>0) {
					   for (Payment payment : savedPaymentSet) {
						   Set<Mop> mopSet = payment.getMops();
						   if(mopSet.size()>0) {
							for (Mop mop : mopSet) {
								ProductionReportObject production = new ProductionReportObject();
					          if(payment.getPaymentType().getName().equalsIgnoreCase("Net Banking") || payment.getPaymentType().getName().equalsIgnoreCase("Wallet")) {
					        	  production.setCode(mop.getMopType().getCode());
					          }//end if
					          else {
					        	  production.setMop(mop.getMopType().getName());
					          }//end else
					          production.setMerchant(userDao.getBusinessNameByPayId(merchant));
							  production.setPayId(merchant);
				              production.setPaymentType(payment.getPaymentType().getName());
				              mappingList.add(production);
							}//end for
						   }//end if
						   else {
							   continue;
						   }//end else
						  }//end for
					  }//end if
					  else {
						   continue;
					 }
				}//end if
				else {
					continue;
				}
			}//end for
		}//end if
	  }//end if
		return mappingList;
		}catch(Exception e) {
			logger.error("Exception", e);
		}
		return mappingList;
    }
	
	public List<ProductionReportObject> getMappingListByAcquirer(String merchant, String acquirer , String currency ,User sessionUser){
		List<ProductionReportObject> mappingList = new ArrayList<ProductionReportObject>();
		try {
			User user = new User();
			List<Merchants> merchantList = userDao.getMerchantActiveList();
			for(Merchants merchants : merchantList) {
				String emailId = merchants.getEmailId();
				String payId = merchants.getPayId();
				user = new UserDao().findByEmailId(emailId);
				Set<Account> userAccountSet = user.getAccounts();
				if(userAccountSet.size()>0) {
					Account account = user.getAccountUsingAcquirerCode1(acquirer);
					if(!(account==null)) {
				for (Account acc : userAccountSet) {
					if(acc.getAcquirerPayId().equals(account.getAcquirerPayId())) {
						Set<Payment> savedPaymentSet = acc.getPayments();
						   if(savedPaymentSet.size()>0) {
						   for (Payment payment : savedPaymentSet) {
							   Set<Mop> mopSet = payment.getMops();
							   if(mopSet.size()>0) {
								for (Mop mop : mopSet) {
									ProductionReportObject production = new ProductionReportObject();
						          if(payment.getPaymentType().getName().equalsIgnoreCase("Net Banking") || payment.getPaymentType().getName().equalsIgnoreCase("Wallet")) {
						        	  production.setCode(mop.getMopType().getCode());
						          }//end if
						          else {
						        	  production.setMop(mop.getMopType().getName());
						          }//end else
						          production.setMerchant(merchants.getBusinessName());
								  production.setPayId(payId);
					              production.setPaymentType(payment.getPaymentType().getName());
					              mappingList.add(production);
								}//end for
							   }//end if
							   else {
								   continue;
							   }//end else
							  }//end for
						  }//end if
						  else {
							   continue;
						 }
					}//end if
					else {
						continue;
					}
				}//end for
			}//end if
					else {
						continue;
					}//end else
				}//end if
				else {
					continue;
				}//end else
			}//end for
			return mappingList;
		}catch(Exception e) {
			logger.error("Exception", e);
		}
		return mappingList;
	}
	
	public List<ProductionReportObject> getMappingListByCurrency(String merchant, String acquirer , String currency ,User sessionUser){
		List<ProductionReportObject> mappingList = new ArrayList<ProductionReportObject>();
		try {
			User user = new User();
			List<Merchants> merchantList = userDao.getMerchantActiveList();
			for(Merchants merchants : merchantList) {
				String emailId = merchants.getEmailId();
				String payId = merchants.getPayId();
				user = new UserDao().findByEmailId(emailId);
				Set<Account> userAccountSet = user.getAccounts();
				if(userAccountSet.size()>0) {
					for (Account acc : userAccountSet) {
						   Set<AccountCurrency> accCurrSet = acc.getAccountCurrencySet();
							
							for (AccountCurrency accCurr : accCurrSet) {
								String currency1 = accCurr.getCurrencyCode();
								if(currency1.equals(currency)) {
									Set<Payment> savedPaymentSet = acc.getPayments();
									   if(savedPaymentSet.size()>0) {
									   for (Payment payment : savedPaymentSet) {
										   Set<Mop> mopSet = payment.getMops();
										   if(mopSet.size()>0) {
											for (Mop mop : mopSet) {
												ProductionReportObject production = new ProductionReportObject();
									          if(payment.getPaymentType().getName().equalsIgnoreCase("Net Banking") || payment.getPaymentType().getName().equalsIgnoreCase("Wallet")) {
									        	  production.setCode(mop.getMopType().getCode());
									          }//end if
									          else {
									        	  production.setMop(mop.getMopType().getName());
									          }//end else
									          production.setPayId(merchants.getPayId());
											  production.setMerchant(merchants.getBusinessName());
								              production.setPaymentType(payment.getPaymentType().getName());
								              mappingList.add(production);
											}//end for
										   }//end if
										   else {
											   continue;
										   }//end else
										  }//end for
									  }//end if
									  else {
										   continue;
									 }
									}//end if
								else {
									continue;
								}
								}//end for
					}//end for
				}//end if
				else {
					continue;
				}
			}//end for
			return mappingList;
			}catch(Exception e) {
				logger.error("Exception", e);
			}
		return mappingList;
		}
	
	public List<ProductionReportObject> getMappingListByAcquirerCurrency(String merchant, String acquirer , String currency ,User sessionUser){
		List<ProductionReportObject> mappingList = new ArrayList<ProductionReportObject>();
		try {
			User user = new User();
			List<Merchants> merchantList = userDao.getMerchantActiveList();
			for(Merchants merchants : merchantList) {
				String emailId = merchants.getEmailId();
				String payId = merchants.getPayId();
				user = new UserDao().findByEmailId(emailId);
				Set<Account> userAccountSet = user.getAccounts();
				if(userAccountSet.size()>0) {
					Account account = user.getAccountUsingAcquirerCode1(acquirer);
					if(!(account==null)) {
				for (Account acc : userAccountSet) {
					if(acc.getAcquirerPayId().equals(account.getAcquirerPayId())) {
						Set<AccountCurrency> accCurrSet = acc.getAccountCurrencySet();
						for (AccountCurrency accCurr : accCurrSet) {
							String currency1 = accCurr.getCurrencyCode();
							if(currency1.equals(currency)) {
								Set<Payment> savedPaymentSet = acc.getPayments();
								   if(savedPaymentSet.size()>0) {
								   for (Payment payment : savedPaymentSet) {
									   Set<Mop> mopSet = payment.getMops();
									   if(mopSet.size()>0) {
										for (Mop mop : mopSet) {
											ProductionReportObject production = new ProductionReportObject();
								          if(payment.getPaymentType().getName().equalsIgnoreCase("Net Banking") || payment.getPaymentType().getName().equalsIgnoreCase("Wallet")) {
								        	  production.setCode(mop.getMopType().getCode());
								          }//end if
								          else {
								        	  production.setMop(mop.getMopType().getName());
								          }//end else
								          production.setMerchant(merchants.getBusinessName());
										  production.setPayId(payId);
							              production.setPaymentType(payment.getPaymentType().getName());
							              mappingList.add(production);
										}//end for
									   }//end if
									   else {
										   continue;
									   }//end else
									  }//end for
								  }//end if
								  else {
									   continue;
								 }
								}//end if
							else {
								continue;
							}
							}//end for
					}//end if
					else {
						continue;
					}
				}//end for
			}//end if
					else {
						continue;
					}
				}//end if
				else {
					continue;
				}
			}//end for
							
			return mappingList;
		}catch(Exception e) {
			logger.error("Exception", e);
		}
		return mappingList;
		}
	
	public List<ProductionReportObject> getMappingListByMerchantCurrency(String merchant, String acquirer , String currency ,User sessionUser){
		List<ProductionReportObject> mappingList = new ArrayList<ProductionReportObject>();
		try {
			User user = new User();
			String emailId = userDao.getEmailIdByPayId(merchant);
			user = new UserDao().findByEmailId(emailId);
			Set<Account> userAccountSet = user.getAccounts();
			if(userAccountSet.size()>0) {
			for (Account acc : userAccountSet) {
				   Set<AccountCurrency> accCurrSet = acc.getAccountCurrencySet();
					
					for (AccountCurrency accCurr : accCurrSet) {
						String currency1 = accCurr.getCurrencyCode();
						if(currency1.equals(currency)) {
							Set<Payment> savedPaymentSet = acc.getPayments();
							   if(savedPaymentSet.size()>0) {
							   for (Payment payment : savedPaymentSet) {
								   Set<Mop> mopSet = payment.getMops();
								   if(mopSet.size()>0) {
									for (Mop mop : mopSet) {
										ProductionReportObject production = new ProductionReportObject();
							          if(payment.getPaymentType().getName().equalsIgnoreCase("Net Banking") || payment.getPaymentType().getName().equalsIgnoreCase("Wallet")) {
							        	  production.setCode(mop.getMopType().getCode());
							          }//end if
							          else {
							        	  production.setMop(mop.getMopType().getName());
							          }//end else
							          production.setPayId(merchant);
									  production.setMerchant(userDao.getBusinessNameByPayId(merchant));
						              production.setPaymentType(payment.getPaymentType().getName());
						              mappingList.add(production);
									}//end for
								   }//end if
								   else {
									   continue;
								   }//end else
								  }//end for
							  }//end if
							  else {
								   continue;
							 }
							}//end if
						else {
							continue;
						}
						}//end for
			}//end for
			}//end if
			return mappingList;
		}catch(Exception e) {
			logger.error("Exception", e);
		}
		return mappingList;
		}
	
	public List<ProductionReportObject> getMappingListByMerchantAcquirerCurrency(String merchant, String acquirer , String currency ,User sessionUser){
		List<ProductionReportObject> mappingList = new ArrayList<ProductionReportObject>();
		try {
			User user = new User();
			String emailId = userDao.getEmailIdByPayId(merchant);
			user = new UserDao().findByEmailId(emailId);
			Set<Account> userAccountSet = user.getAccounts();
			if(userAccountSet.size()>0) {
				Account account = user.getAccountUsingAcquirerCode1(acquirer);
				if(!(account==null)) {
			for (Account acc : userAccountSet) {
				if(acc.getAcquirerPayId().equals(account.getAcquirerPayId())) {
                    Set<AccountCurrency> accCurrSet = acc.getAccountCurrencySet();
					
					for (AccountCurrency accCurr : accCurrSet) {
						String currency1 = accCurr.getCurrencyCode();
						if(currency1.equals(currency)) {
							Set<Payment> savedPaymentSet = acc.getPayments();
							   if(savedPaymentSet.size()>0) {
							   for (Payment payment : savedPaymentSet) {
								   Set<Mop> mopSet = payment.getMops();
								   if(mopSet.size()>0) {
									for (Mop mop : mopSet) {
										ProductionReportObject production = new ProductionReportObject();
							          if(payment.getPaymentType().getName().equalsIgnoreCase("Net Banking") || payment.getPaymentType().getName().equalsIgnoreCase("Wallet")) {
							        	  production.setCode(mop.getMopType().getCode());
							          }//end if
							          else {
							        	  production.setMop(mop.getMopType().getName());
							          }//end else
							          production.setMerchant(userDao.getBusinessNameByPayId(merchant));
										production.setPayId(merchant);
						              production.setPaymentType(payment.getPaymentType().getName());
						              mappingList.add(production);
									}//end for
								   }//end if
								   else {
									   continue;
								   }//end else
								  }//end for
							  }//end if
							  else {
								   continue;
							 }
							}//end if
						else {
							continue;
						}
						}//end for
				}//end if
				else {
					continue;
				}
			}//end for
				}//end if
			}//end if
			return mappingList;
		}catch(Exception e) {
			logger.error("Exception", e);
		}
		return mappingList;
		}
}
