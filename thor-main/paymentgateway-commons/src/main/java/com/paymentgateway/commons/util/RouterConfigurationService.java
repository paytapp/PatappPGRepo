package com.paymentgateway.commons.util;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.RouterConfigurationDao;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;

@Service
public class RouterConfigurationService {

	private static Logger logger = LoggerFactory.getLogger(RouterConfigurationService.class.getName());

	@Autowired
	private RouterConfigurationDao routerConfigurationDao;

	public String autoUdpateRouterConfiguration(Fields fields, String status) {

		logger.info("inside autoUdpateRouterConfiguration ");

		try {

			/*
			 * String paymentType = fields.get(FieldType.PAYMENT_TYPE.getName()); String
			 * mopType = fields.get(FieldType.MOP_TYPE.getName()); String payId =
			 * fields.get(FieldType.PAY_ID.getName()); String currency =
			 * fields.get(FieldType.CURRENCY_CODE.getName()); String acquirer =
			 * fields.get(FieldType.ACQUIRER_TYPE.getName()); String transactionType =
			 * fields.get(FieldType.TXNTYPE.getName()); String paymentsRegion =
			 * AccountCurrencyRegion.DOMESTIC.toString(); String cardType =
			 * CardHolderType.COMMERCIAL.toString();
			 * 
			 * 
			 * String slabIdVal = ""; String slabId = ""; String slabAmountArrayString
			 * =PropertiesManager.propertiesMap.get(Constants.SWITCH_ACQUIRER_AMOUNT.
			 * getValue());
			 * 
			 * // If no slab is set for this merchant , create a default slab with 00 as ID
			 * and limit from 0.01 to 1000000.00 if (!slabAmountArrayString.contains(payId))
			 * { slabAmountArrayString = "00-0.01-1000000.00-"+payId+"-ALL"; }
			 * 
			 * String[] slabArray = slabAmountArrayString.split(","); double
			 * transactionAmount = Double.valueOf(fields.get(FieldType.AMOUNT.getName()));
			 * 
			 * for (String amountSlab : slabArray) {
			 * 
			 * if (!amountSlab.contains(payId)) { continue; }
			 * 
			 * String[] slabSplit = amountSlab.split("-"); slabIdVal = slabSplit[0]; String
			 * minTransactionAmount = slabSplit[1]; String maxTransactionAmount =
			 * slabSplit[2]; String paymentTypeSlab = "ALL";
			 * 
			 * if (!StringUtils.isBlank(slabSplit[4])) { paymentTypeSlab = slabSplit[4]; }
			 * 
			 * if (!paymentTypeSlab.equalsIgnoreCase(paymentType)) {
			 * 
			 * slabId = "00"; minTransactionAmount = "0.01"; maxTransactionAmount =
			 * "1000000.00";
			 * 
			 * }
			 * 
			 * double minTxnAmount = Double.valueOf(minTransactionAmount); double
			 * maxTxnAmount = Double.valueOf(maxTransactionAmount);
			 * 
			 * if (transactionAmount >= minTxnAmount && transactionAmount <= maxTxnAmount) {
			 * slabId = slabIdVal; } }
			 * 
			 * if (slabId.equals("")) { slabId = "01"; }
			 * 
			 * logger.info("inside autoUdpateRouterConfiguration , transactionType =  " +
			 * transactionType); String transactionTypeName = "SALE";
			 * 
			 * if (!transactionType.equalsIgnoreCase(TransactionType.NEWORDER.getName())) {
			 * 
			 * String identifier = payId + currency + paymentType + mopType +
			 * transactionTypeName+paymentsRegion+cardType+slabId;
			 * 
			 * RouterConfiguration routerConfiguration = new RouterConfiguration();
			 * routerConfiguration =
			 * routerConfigurationDao.findRulesByIdentifierAcquirer(identifier, acquirer);
			 * 
			 * int allowedFailCount = routerConfiguration.getAllowedFailureCount(); int
			 * totalFailCount = routerConfiguration.getFailureCount() + 1; int
			 * loadPercentage = routerConfiguration.getLoadPercentage(); String mode =
			 * routerConfiguration.getMode();
			 * 
			 * if (routerConfiguration.isAlwaysOn()) {
			 * 
			 * Session session = null; String id = routerConfiguration.getId(); session =
			 * HibernateSessionProvider.getSession(); Transaction tx =
			 * session.beginTransaction();
			 * 
			 * try { session.load(routerConfiguration, routerConfiguration.getId());
			 * 
			 * RouterConfiguration routerConfigurationNew = (RouterConfiguration) session
			 * .get(RouterConfiguration.class, id);
			 * routerConfigurationNew.setFailureCount(totalFailCount);
			 * session.update(routerConfigurationNew); tx.commit(); session.close(); logger.
			 * info("inside autoUdpateRouterConfiguration , acquirer marked always on  , identifier = "
			 * + identifier); return "success";
			 * 
			 * } catch (HibernateException e) { if (tx != null) tx.rollback(); logger.
			 * error("Exception occured while updating router configuration , exception = "
			 * + e); } catch (Exception e) { if (tx != null) tx.rollback(); logger.
			 * error("Exception occured while updating router configuration , exception = "
			 * + e); } finally { session.close(); }
			 * 
			 * 
			 * }
			 * 
			 * logger.info("inside autoUdpateRouterConfiguration , mode , identifier  =  " +
			 * mode + "  " + identifier);
			 * 
			 * boolean isNextAcquirerAvailable =
			 * routerConfigurationDao.checkAvailableAcquirers(identifier, acquirer);
			 * 
			 * if (totalFailCount > allowedFailCount) {
			 * 
			 * Date currentDate = new Date(); Session session = null; String id =
			 * routerConfiguration.getId(); session = HibernateSessionProvider.getSession();
			 * Transaction tx = session.beginTransaction();
			 * session.load(routerConfiguration, routerConfiguration.getId()); try {
			 * RouterConfiguration routerConfigurationNew = (RouterConfiguration) session
			 * .get(RouterConfiguration.class, id);
			 * 
			 * routerConfigurationNew.setFailureCount(totalFailCount + 1);
			 * 
			 * if (isNextAcquirerAvailable) {
			 * routerConfigurationNew.setCurrentlyActive(false);
			 * routerConfigurationNew.setUpdatedDate(currentDate);
			 * routerConfigurationNew.setUpdatedBy("AUTO_UPDATE");
			 * routerConfigurationNew.setRequestedBy("AUTO_UPDATE");
			 * routerConfigurationNew.setStopTime(currentDate);
			 * routerConfigurationNew.setStatusName("Auto Shutdown");
			 * routerConfigurationNew.setDown(true);
			 * currentDate.setMinutes(currentDate.getMinutes() + 30);
			 * routerConfigurationNew.setEndTime(currentDate); }
			 * 
			 * session.update(routerConfigurationNew); tx.commit(); session.close(); logger.
			 * info("inside autoUdpateRouterConfiguration , acquirer sent to shut down , identifier = "
			 * + identifier); } catch (HibernateException e) { if (tx != null)
			 * tx.rollback(); logger.
			 * error("Exception occured while updating router configuration , exception = "
			 * + e); } catch (Exception e) { if (tx != null) tx.rollback(); logger.
			 * error("Exception occured while updating router configuration , exception = "
			 * + e); } finally { session.close(); }
			 * 
			 * // In auto mode , select next available best acquirer if
			 * (mode.equalsIgnoreCase("AUTO")) {
			 * 
			 * if (isNextAcquirerAvailable) {
			 * 
			 * RouterConfiguration nextConfig = routerConfigurationDao
			 * .findNextAvailableConfigurationByIdentifier(identifier);
			 * 
			 * Date date = new Date(); Session sessionNew = null; Long idNew =
			 * nextConfig.getId(); session = HibernateSessionProvider.getSession();
			 * Transaction txNew = session.beginTransaction(); session.load(nextConfig,
			 * nextConfig.getId()); try { RouterConfiguration routerConfigurationNew =
			 * (RouterConfiguration) session .get(RouterConfiguration.class, idNew);
			 * routerConfigurationNew.setCurrentlyActive(true);
			 * routerConfigurationNew.setUpdatedDate(currentDate);
			 * routerConfigurationNew.setUpdatedBy("AUTO_UPDATE");
			 * routerConfigurationNew.setRequestedBy("AUTO_UPDATE");
			 * routerConfigurationNew.setStatusName("Auto Start");
			 * routerConfigurationNew.setLoadPercentage(100);
			 * routerConfigurationNew.setDown(false);
			 * session.update(routerConfigurationNew); txNew.commit(); } catch
			 * (HibernateException e) { if (txNew != null) txNew.rollback(); logger.error(
			 * "Exception occured while updating router configuration auto mode, exception = "
			 * + e); } catch (Exception e) { if (tx != null) tx.rollback(); logger.error(
			 * "Exception occured while updating router configuration auto mode, exception = "
			 * + e); } finally { session.close(); }
			 * 
			 * }
			 * 
			 * }
			 * 
			 * else {
			 * 
			 * // In manual mode , sleect next best available acquirer that is manually
			 * turned // on RouterConfiguration nextConfig =
			 * routerConfigurationDao.findNextActiveAcquirer(identifier); int currentLoad =
			 * nextConfig.getLoadPercentage(); Transaction txNew =
			 * session.beginTransaction(); session.load(nextConfig, nextConfig.getId()); try
			 * { RouterConfiguration routerConfigurationNew = (RouterConfiguration) session
			 * .get(RouterConfiguration.class, id);
			 * routerConfigurationNew.setLoadPercentage(loadPercentage+currentLoad);
			 * session.update(routerConfigurationNew); txNew.commit(); } catch
			 * (HibernateException e) { if (txNew != null) txNew.rollback(); logger.error(
			 * "Exception occured while updating router configuration manual  mode , exception = "
			 * + e); } catch (Exception e) { if (tx != null) tx.rollback(); logger.error(
			 * "Exception occured while updating router configuration manual  mode , exception = "
			 * + e); } finally { session.close(); }
			 * 
			 * }
			 * 
			 * }
			 * 
			 * else { Session session = null; Long id = routerConfiguration.getId(); session
			 * = HibernateSessionProvider.getSession(); Transaction tx =
			 * session.beginTransaction(); session.load(routerConfiguration,
			 * routerConfiguration.getId()); try { RouterConfiguration
			 * routerConfigurationNew = (RouterConfiguration) session
			 * .get(RouterConfiguration.class, id);
			 * routerConfigurationNew.setFailureCount(totalFailCount + 1);
			 * session.update(routerConfigurationNew); tx.commit(); } catch
			 * (HibernateException e) { if (tx != null) tx.rollback(); logger.error(
			 * "Exception occured while updating router configuration fail count less than allowed, exception = "
			 * + e); } catch (Exception e) { if (tx != null) tx.rollback(); logger.error(
			 * "Exception occured while updating router configuration fail count less than allowed, exception = "
			 * + e); } finally { session.close(); }
			 * 
			 * }
			 * 
			 * }
			 */

		}

		catch (Exception e) {
			logger.error("Cannot update error in Router Configuration , exception = " + e);
		}
		return "success";

	}

	public void clearFailCount(Fields fields) {

		//logger.info("inside clearFailCount ");
		String transactionTypeName = "SALE";
		Session session = null;
		try {
			String paymentType = fields.get(FieldType.PAYMENT_TYPE.getName());
			String mopType = fields.get(FieldType.MOP_TYPE.getName());
			String payId = fields.get(FieldType.PAY_ID.getName());
			String currency = fields.get(FieldType.CURRENCY_CODE.getName());
			String acquirer = fields.get(FieldType.ACQUIRER_TYPE.getName());
			String transactionType = fields.get(FieldType.TXNTYPE.getName());
			String status = fields.get(FieldType.STATUS.getName());
			String paymentsRegion = AccountCurrencyRegion.DOMESTIC.toString();
			String cardHolderType = CardHolderType.CONSUMER.toString();		

				String identifier = payId + currency + paymentType + mopType + transactionTypeName+paymentsRegion+cardHolderType;

				logger.info("inside clearFailCount , identifier = " + identifier + "  acquirer = " + acquirer);
				//RouterConfiguration routerConfiguration = new RouterConfiguration();
				//routerConfiguration = routerConfigurationDao.findRulesByIdentifierAcquirer(identifier, acquirer);

				/*if (routerConfiguration.getFailureCount()>0) {
					
					Long id = routerConfiguration.getId();
					session = HibernateSessionProvider.getSession();
					Transaction tx = session.beginTransaction();
					session.load(routerConfiguration, routerConfiguration.getId());

					RouterConfiguration routerConfigurationNew = (RouterConfiguration) session
							.get(RouterConfiguration.class, id);
					routerConfigurationNew.setFailureCount(0);
					session.update(routerConfigurationNew);
					tx.commit();
					
					logger.info("inside clearFailCount , identifier = " + identifier + "  acquirer = " + acquirer
							+ " Failure count reset to 0");

				}*/			
		}

		catch (Exception e) {
			logger.error("Exception occured while clearing fail count , Exception = " , e);
		}
		 finally {
			 if (session != null ) {
				 session.close();
			 }
			
			}

	}
}
