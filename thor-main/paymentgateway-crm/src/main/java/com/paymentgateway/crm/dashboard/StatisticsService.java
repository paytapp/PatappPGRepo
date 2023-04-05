package com.paymentgateway.crm.dashboard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.DataAccessObject;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.CrmFieldConstants;

/**
 * @author Chandan
 */
@Service
public class StatisticsService {
	
	private static Logger logger = LoggerFactory.getLogger(StatisticsService.class.getName());

	public StatisticsService() {
	}

	private Connection getConnection() throws SQLException {
		return DataAccessObject.getBasicConnection();
	}

	/*public Statistics getDashboardValues(String payId, String currency)
			throws SystemException {
		
		
		
	
		

		Statistics statistics = new Statistics();
		try (Connection connection = getConnection()) {
			try (PreparedStatement prepStmt = connection
					.prepareStatement("{call statisticsSummary(?,?)}")) {
				prepStmt.setString(1, payId);
				prepStmt.setString(2, currency);
				try (ResultSet rs = prepStmt.executeQuery()) {
					rs.next();
					statistics.setTotalSuccess(rs
							.getString(CrmFieldConstants.TOTAL_SUCCESS
									.getValue()));
					statistics.setTotalFailed(rs
							.getString(CrmFieldConstants.TOTAL_FAILED
									.getValue()));
					statistics.setTotalRefunded(rs
							.getString(CrmFieldConstants.TOTAL_REFUNDED
									.getValue()));
					statistics.setRefundedAmount(rs
							.getString(CrmFieldConstants.REFUNDED_AMOUNT
									.getValue()));
					statistics.setApprovedAmount(rs
							.getString(CrmFieldConstants.APPROVED_AMOUNT
									.getValue()));
				}
			}
		} catch (SQLException exception) {
			logger.error("Database error", exception);
			throw new SystemException(ErrorType.DATABASE_ERROR,
					ErrorType.DATABASE_ERROR.getResponseMessage());
		}
		return statistics;
	}*/

	public Statistics getDashboardResellerValues(String payId,
			String resellerId, String currency) throws SystemException {

		Statistics statistics = new Statistics();
		try (Connection connection = getConnection()) {
			try (PreparedStatement prepStmt = connection
					.prepareStatement("{call resellerStatisticsSummary(?,?,?)}")) {
				prepStmt.setString(1, payId);
				prepStmt.setString(2, resellerId);
				prepStmt.setString(3, currency);
				try (ResultSet rs = prepStmt.executeQuery()) {
					rs.next();
					statistics.setTotalSuccess(rs
							.getString(CrmFieldConstants.TOTAL_SUCCESS
									.getValue()));
					statistics.setTotalFailed(rs
							.getString(CrmFieldConstants.TOTAL_FAILED
									.getValue()));
					statistics.setTotalRefunded(rs
							.getString(CrmFieldConstants.TOTAL_REFUNDED
									.getValue()));
					statistics.setRefundedAmount(rs
							.getString(CrmFieldConstants.REFUNDED_AMOUNT
									.getValue()));
					statistics.setApprovedAmount(rs
							.getString(CrmFieldConstants.APPROVED_AMOUNT
									.getValue()));
				}
			}
		} catch (SQLException exception) {
			logger.error("Database error", exception);
			throw new SystemException(ErrorType.DATABASE_ERROR,
					ErrorType.DATABASE_ERROR.getResponseMessage());
		}
		return statistics;
	}

	public Statistics getCreditCardsDashboardValues(String payId,
			String currency, String dateFrom, String dateTo)
			throws SystemException, ParseException {

		Statistics statistics = new Statistics();
		try (Connection connection = getConnection()) {
			try (PreparedStatement prepStmt = connection
					.prepareStatement("{call creditCardsAnalticsReport(?,?,?,?)}")) {
				DateFormat df = new SimpleDateFormat("dd-MM-yy");
				SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
				String startDate = sdf1.format(df.parse(dateFrom));
				String endDate = sdf1.format(df.parse(dateTo));
				prepStmt.setString(1, payId);
				prepStmt.setString(2, currency);
				prepStmt.setString(3, startDate);
				prepStmt.setString(4, endDate);
				try (ResultSet rs = prepStmt.executeQuery()) {
					rs.next();
					statistics
							.setTotalCreditTransaction(rs
									.getString(CrmFieldConstants.TOTAL_CREDIT_TRANSCTION
											.getValue()));
					statistics.setTotalCreditSuccess(rs
							.getString(CrmFieldConstants.TOTAL_CREDIT_SUCCESS
									.getValue()));
					statistics.setTotalCreditFailed(rs
							.getString(CrmFieldConstants.TOTAL_CREDIT_FAILED
									.getValue()));
					statistics.setTotalCreditBaunced(rs
							.getString(CrmFieldConstants.TOTAL_CREDIT_BAUNCED
									.getValue()));
					statistics.setTotalCreditCancelled(rs
							.getString(CrmFieldConstants.TOTAL_CREDIT_CANCELLED
									.getValue()));
					statistics.setTotalCreditDropped(rs
							.getString(CrmFieldConstants.TOTAL_CREDIT_DROPPED
									.getValue()));
					statistics.setTotalDebitTransaction(rs
							.getString(CrmFieldConstants.TOTAL_DEBIT_TRANSCTION
									.getValue()));
					statistics.setTotalDebitSuccess(rs
							.getString(CrmFieldConstants.TOTAL_DEBIT_SUCCESS
									.getValue()));
					statistics.setTotalDebitFailed(rs
							.getString(CrmFieldConstants.TOTAL_DEBIT_FAILED
									.getValue()));
					statistics.setTotalDebitBaunced(rs
							.getString(CrmFieldConstants.TOTAL_DEBIT_BAUNCED
									.getValue()));
					statistics.setTotalDebitCancelled(rs
							.getString(CrmFieldConstants.TOTAL_DEBIT_CANCELLED
									.getValue()));
					statistics.setTotalDebitDropped(rs
							.getString(CrmFieldConstants.TOTAL_DEBIT_DROPPED
									.getValue()));

					statistics
							.setTotalNetBankTransaction(rs
									.getString(CrmFieldConstants.TOTAL_NET_BANK_TRANSACTION
											.getValue()));
					statistics.setTotalNetBankSuccess(rs
							.getString(CrmFieldConstants.TOTAL_NET_BANK_SUCCESS
									.getValue()));
					statistics.setTotalNetBankFailed(rs
							.getString(CrmFieldConstants.TOTAL_NET_BANK_FAILED
									.getValue()));
					statistics
							.setTotalNetBankCancelled(rs
									.getString(CrmFieldConstants.TOTAL_NET_BANK_CANCELLED
											.getValue()));
					statistics.setTotalNetBankDropped(rs
							.getString(CrmFieldConstants.TOTAL_NET_BANK_DROPPED
									.getValue()));

					statistics
							.setTotalWalletTransaction(rs
									.getString(CrmFieldConstants.TOTAL_WALLET_TRANSACTIONS
											.getValue()));
					statistics.setTotalWalletSuccess(rs
							.getString(CrmFieldConstants.TOTAL_WALLET_SUCCESS
									.getValue()));
					statistics.setTotalWalletFailed(rs
							.getString(CrmFieldConstants.TOTAL_WALLET_FAILED
									.getValue()));
					statistics.setTotalWalletCancelled(rs
							.getString(CrmFieldConstants.TOTAL_WALLET_CANCELLED
									.getValue()));
					statistics.setTotalWalletDropped(rs
							.getString(CrmFieldConstants.TOTAL_WALLET_DROPPED
									.getValue()));

				}
			}
		} catch (SQLException exception) {
			logger.error("Database error", exception);
			throw new SystemException(ErrorType.DATABASE_ERROR,
					ErrorType.DATABASE_ERROR.getResponseMessage());
		}
		return statistics;
	}

	public Statistics getCreditCardMopTypeValue(String payId, String currency,
			String dateFrom, String dateTo) throws SystemException,
			ParseException {

		Statistics statistics = new Statistics();
		try (Connection connection = getConnection()) {
			try (PreparedStatement prepStmt = connection
					.prepareStatement("{call creditCardsMopTypeSummary(?,?,?,?)}")) {
				DateFormat df = new SimpleDateFormat("dd-MM-yy");
				SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
				String startDate = sdf1.format(df.parse(dateFrom));
				String endDate = sdf1.format(df.parse(dateTo));
				prepStmt.setString(1, payId);
				prepStmt.setString(2, currency);
				prepStmt.setString(3, startDate);
				prepStmt.setString(4, endDate);
				try (ResultSet rs = prepStmt.executeQuery()) {
					rs.next();
					statistics
							.setTotalVisa(rs
									.getString(CrmFieldConstants.TOTAL_VISAS
											.getValue()));
					statistics.setTotalSuccessVisa(rs
							.getString(CrmFieldConstants.TOTAL_SUCCESS_VISA
									.getValue()));
					statistics.setTotalFailedVisa(rs
							.getString(CrmFieldConstants.TOTAL_FAILED_VISA
									.getValue()));
					statistics.setTotalDroppedVisa(rs
							.getString(CrmFieldConstants.TOTAL_DROPPED_VISA
									.getValue()));
					statistics.setTotalCancelledVisa(rs
							.getString(CrmFieldConstants.TOTAL_CANCELLED_VISA
									.getValue()));
					statistics
							.setTotalAmex(rs
									.getString(CrmFieldConstants.TOTAL_AMEXS
											.getValue()));
					statistics.setTotalSuccessAmex(rs
							.getString(CrmFieldConstants.TOTAL_SUCCESS_AMEX
									.getValue()));
					statistics.setTotalFailedAmex(rs
							.getString(CrmFieldConstants.TOTAL_FAILED_AMEX
									.getValue()));
					statistics.setTotalDroppedAmex(rs
							.getString(CrmFieldConstants.TOTAL_DROPPED_AMEX
									.getValue()));
					statistics.setTotalCancelledAmex(rs
							.getString(CrmFieldConstants.TOTAL_CANCELLED_AMEX
									.getValue()));
					statistics.setTotalMaster(rs
							.getString(CrmFieldConstants.TOTAL_MASTERS
									.getValue()));
					statistics.setTotalSuccessMaster(rs
							.getString(CrmFieldConstants.TOTAL_SUCCESS_MASTER
									.getValue()));
					statistics.setTotalFailedMaster(rs
							.getString(CrmFieldConstants.TOTAL_FAILED_MASTER
									.getValue()));
					statistics.setTotalDroppedMaster(rs
							.getString(CrmFieldConstants.TOTAL_DROPPED_MASTER
									.getValue()));
					statistics.setTotalCancelledMaster(rs
							.getString(CrmFieldConstants.TOTAL_CANCELLED_MASTER
									.getValue()));
					statistics.setTotalMestro(rs
							.getString(CrmFieldConstants.TOTAL_MESTRO
									.getValue()));
					statistics.setTotalSuccessMestro(rs
							.getString(CrmFieldConstants.TOTAL_SUCCESS_MESTRO
									.getValue()));
					statistics.setTotalFailedMestro(rs
							.getString(CrmFieldConstants.TOTAL_FAILED_MESTRO
									.getValue()));
					statistics.setTotalDroppedMestro(rs
							.getString(CrmFieldConstants.TOTAL_DROPPED_MESTRO
									.getValue()));
					statistics.setTotalCancelledMestro(rs
							.getString(CrmFieldConstants.TOTAL_CANCELLED_MESTRO
									.getValue()));

					statistics
							.setTotalDiner(rs
									.getString(CrmFieldConstants.TOTAL_DINER
											.getValue()));
					statistics.setTotalSuccessDiner(rs
							.getString(CrmFieldConstants.TOTAL_SUCCESS_DINER
									.getValue()));
					statistics.setTotalFailedDiner(rs
							.getString(CrmFieldConstants.TOTAL_FAILED_DINER
									.getValue()));
					statistics.setTotalDroppedDiner(rs
							.getString(CrmFieldConstants.TOTAL_DROPPED_DINER
									.getValue()));
					statistics.setTotalCancelledDiner(rs
							.getString(CrmFieldConstants.TOTAL_CANCELLED_DINER
									.getValue()));

				}
			}
		} catch (SQLException exception) {
			logger.error("Database error", exception);
			throw new SystemException(ErrorType.DATABASE_ERROR,
					ErrorType.DATABASE_ERROR.getResponseMessage());
		}
		return statistics;
	}

	public Statistics getDibetCardMopTypeValue(String payId, String currency,
			String dateFrom, String dateTo) throws SystemException,
			ParseException {

		Statistics statistics = new Statistics();
		try (Connection connection = getConnection()) {
			try (PreparedStatement prepStmt = connection
					.prepareStatement("{call debitCardsMopTypeSummary(?,?,?,?)}")) {
				DateFormat df = new SimpleDateFormat("dd-MM-yy");
				SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
				String startDate = sdf1.format(df.parse(dateFrom));
				String endDate = sdf1.format(df.parse(dateTo));
				prepStmt.setString(1, payId);
				prepStmt.setString(2, currency);
				prepStmt.setString(3, startDate);
				prepStmt.setString(4, endDate);
				try (ResultSet rs = prepStmt.executeQuery()) {
					rs.next();
					statistics
							.setTotalVisa(rs
									.getString(CrmFieldConstants.TOTAL_VISAS
											.getValue()));
					statistics.setTotalSuccessVisa(rs
							.getString(CrmFieldConstants.TOTAL_SUCCESS_VISA
									.getValue()));
					statistics.setTotalFailedVisa(rs
							.getString(CrmFieldConstants.TOTAL_FAILED_VISA
									.getValue()));
					statistics.setTotalDroppedVisa(rs
							.getString(CrmFieldConstants.TOTAL_DROPPED_VISA
									.getValue()));
					statistics.setTotalCancelledVisa(rs
							.getString(CrmFieldConstants.TOTAL_CANCELLED_VISA
									.getValue()));
					statistics
							.setTotalAmex(rs
									.getString(CrmFieldConstants.TOTAL_AMEXS
											.getValue()));
					statistics.setTotalSuccessAmex(rs
							.getString(CrmFieldConstants.TOTAL_SUCCESS_AMEX
									.getValue()));
					statistics.setTotalFailedAmex(rs
							.getString(CrmFieldConstants.TOTAL_FAILED_AMEX
									.getValue()));
					statistics.setTotalDroppedAmex(rs
							.getString(CrmFieldConstants.TOTAL_DROPPED_AMEX
									.getValue()));
					statistics.setTotalCancelledAmex(rs
							.getString(CrmFieldConstants.TOTAL_CANCELLED_AMEX
									.getValue()));
					statistics.setTotalMaster(rs
							.getString(CrmFieldConstants.TOTAL_MASTERS
									.getValue()));
					statistics.setTotalSuccessMaster(rs
							.getString(CrmFieldConstants.TOTAL_SUCCESS_MASTER
									.getValue()));
					statistics.setTotalFailedMaster(rs
							.getString(CrmFieldConstants.TOTAL_FAILED_MASTER
									.getValue()));
					statistics.setTotalDroppedMaster(rs
							.getString(CrmFieldConstants.TOTAL_DROPPED_MASTER
									.getValue()));
					statistics.setTotalCancelledMaster(rs
							.getString(CrmFieldConstants.TOTAL_CANCELLED_MASTER
									.getValue()));
					statistics.setTotalMestro(rs
							.getString(CrmFieldConstants.TOTAL_MESTRO
									.getValue()));
					statistics.setTotalSuccessMestro(rs
							.getString(CrmFieldConstants.TOTAL_SUCCESS_MESTRO
									.getValue()));
					statistics.setTotalFailedMestro(rs
							.getString(CrmFieldConstants.TOTAL_FAILED_MESTRO
									.getValue()));
					statistics.setTotalDroppedMestro(rs
							.getString(CrmFieldConstants.TOTAL_DROPPED_MESTRO
									.getValue()));
					statistics.setTotalCancelledMestro(rs
							.getString(CrmFieldConstants.TOTAL_CANCELLED_MESTRO
									.getValue()));

					statistics
							.setTotalDiner(rs
									.getString(CrmFieldConstants.TOTAL_DINER
											.getValue()));
					statistics.setTotalSuccessDiner(rs
							.getString(CrmFieldConstants.TOTAL_SUCCESS_DINER
									.getValue()));
					statistics.setTotalFailedDiner(rs
							.getString(CrmFieldConstants.TOTAL_FAILED_DINER
									.getValue()));
					statistics.setTotalDroppedDiner(rs
							.getString(CrmFieldConstants.TOTAL_DROPPED_DINER
									.getValue()));
					statistics.setTotalCancelledDiner(rs
							.getString(CrmFieldConstants.TOTAL_CANCELLED_DINER
									.getValue()));

				}
			}
		} catch (SQLException exception) {
			logger.error("Database error", exception);
			throw new SystemException(ErrorType.DATABASE_ERROR,
					ErrorType.DATABASE_ERROR.getResponseMessage());
		}
		return statistics;
	}
}
