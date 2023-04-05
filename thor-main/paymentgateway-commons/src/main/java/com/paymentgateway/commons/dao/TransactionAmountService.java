package com.paymentgateway.commons.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;

/**
 * @author shashi
 *
 */
@Service
public class TransactionAmountService {
	private static Logger logger = LoggerFactory
			.getLogger(TransactionAmountService.class.getName());

	private static final String todayCapturedAmountQuery = "select sum(amount) as todayCapturedAmount from TRANSACTION where  PAY_ID=? AND TXNTYPE in ('SALE','CAPTURE') AND STATUS ='Captured' AND CREATE_DATE between curdate() and DATE_ADD(curdate(),INTERVAL 1 DAY) ";
	private static final String todayRefundAmountQuery = "select sum(amount) as todayRefundAmount from TRANSACTION where PAY_ID=? AND txntype='REFUND' AND STATUS='Captured' AND CREATE_DATE between curdate() and DATE_ADD(curdate(),INTERVAL 1 DAY)";
	private Connection getConnection() throws SQLException {
		return DataAccessObject.getBasicConnection();
	}

	// Today captured amount
	public float todayCaptureAmountFatch(String payId)
			throws SystemException {
		float todayCapturedAmount = 0;
		try (Connection connection = getConnection()) {
			try (PreparedStatement preparedStatement = connection
					.prepareStatement(todayCapturedAmountQuery)) {
				preparedStatement.setString(1, payId);
				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					while (resultSet.next()) {
						todayCapturedAmount = resultSet
								.getFloat(FieldType.TODAY_CAPTURED_AMOUNT
										.getName());
					}
				}
			}
		} catch (SQLException sqlException) {
			logger.error("Database error while fetching captured amount : " , sqlException);
			throw new SystemException(ErrorType.DATABASE_ERROR,
					ErrorType.DATABASE_ERROR.getResponseMessage());
		}
		return todayCapturedAmount;
	}

	// today refunded AMOUNT
	public float todayRefundAmountFatch(String payId)
			throws SystemException {
		float todayRefundedAmount = 0;
		try (Connection connection = getConnection()) {
			try (PreparedStatement preparedStatement = connection
					.prepareStatement(todayRefundAmountQuery)) {
				preparedStatement.setString(1, payId);
				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					while (resultSet.next()) {
						todayRefundedAmount = resultSet
								.getFloat(FieldType.TODAY_REFUND.getName());
					}
				}
			}
		} catch (SQLException sqlException) {
			logger.error("Database error while fetching refunded amount : " , sqlException);
			throw new SystemException(ErrorType.DATABASE_ERROR,
					ErrorType.DATABASE_ERROR.getResponseMessage());
		}
		return todayRefundedAmount;
	}
}
