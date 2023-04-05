package com.paymentgateway.crm.dashboard;

import java.text.ParseException;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.User;

@Component
public class BarChartService {
	private static Logger logger = LoggerFactory.getLogger(BarChartService.class.getName());
	@Autowired
	private BarChartQuery barChartQuery;

	public PieChart getDashboardValues(String payId, String currency,
			String dateFrom, String dateTo,User sessionUser) throws SystemException,
			ParseException {
		PieChart pieChart = new PieChart();
	
		pieChart=barChartQuery.barChartTotalSummary(payId, currency, dateFrom, dateTo,sessionUser);
	
		return pieChart;
	}
	
	
	/*public PieChart prepareResellerlist(String payId, String resellerId,
	String currency, String dateFrom, String dateTo)
	throws SystemException, ParseException {
PieChart pieChart = new PieChart();
try (Connection connection = getConnection()) {
	try (PreparedStatement preparedStatement = connection
			.prepareStatement("{call resellerBarChartTransactionRecord(?,?,?,?,?)}")) {
		DateFormat df = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss");
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
		String startDate = sdf1.format(df.parse(dateFrom));
		String endDate = sdf1.format(df.parse(dateTo));
		preparedStatement.setString(1, payId);
		preparedStatement.setString(2, resellerId);
		preparedStatement.setString(3, currency);
		preparedStatement.setString(4, startDate);
		preparedStatement.setString(5, endDate);
		try (ResultSet rs = preparedStatement.executeQuery()) {
			rs.next();
			pieChart.setTotalCredit(rs
					.getString(CrmFieldConstants.TOTAL_CREDIT
							.getValue()));
			pieChart.setTotalDebit(rs
					.getString(CrmFieldConstants.TOTAL_DEBIT.getValue()));
			pieChart.setNet(rs
					.getString(CrmFieldConstants.TOTAL_NETBANKING
							.getValue()));
			pieChart.setOther(rs
					.getString(CrmFieldConstants.TOTAL_OTHER.getValue()));

		}
	}
} catch (SQLException exception) {
	logger.error("Database error", exception);
	throw new SystemException(ErrorType.DATABASE_ERROR,
			ErrorType.DATABASE_ERROR.getResponseMessage());
}
return pieChart;
}

public PieChart getPaymentMethodDashboardValues(String payId,
	String currency, String dateFrom, String dateTo)
	throws SystemException, ParseException {
PieChart pieChart = new PieChart();
try (Connection connection = getConnection()) {
	try (PreparedStatement preparedStatement = connection
			.prepareStatement("{call paymentMethodsSummary(?,?,?,?)}")) {
		DateFormat df = new SimpleDateFormat("dd-MM-yy");
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
		String startDate = sdf1.format(df.parse(dateFrom));
		String endDate = sdf1.format(df.parse(dateTo));
		preparedStatement.setString(1, payId);
		preparedStatement.setString(2, currency);
		preparedStatement.setString(3, startDate);
		preparedStatement.setString(4, endDate);
		try (ResultSet rs = preparedStatement.executeQuery()) {
			rs.next();
			pieChart.setTotalCreditCardsTransaction(rs
					.getString(CrmFieldConstants.TOTAL_CREDITCARDS_TRANSACTION
							.getValue()));
			pieChart.setTotalNetBankingTransaction(rs
					.getString(CrmFieldConstants.TOTAL_NETBANKING_TRANSACTION
							.getValue()));
			pieChart.setTotalDebitCardsTransaction(rs
					.getString(CrmFieldConstants.TOTAL_DEBIT_TRANSACTION
							.getValue()));
			pieChart.setTotalWalletTransaction(rs
					.getString(CrmFieldConstants.TOTAL_WALLET_TRANSACTION
							.getValue()));

		}
	}
} catch (SQLException exception) {
	logger.error("Database error", exception);
	throw new SystemException(ErrorType.DATABASE_ERROR,
			ErrorType.DATABASE_ERROR.getResponseMessage());
}
return pieChart;
}

public PieChart getAcquirerBarChartDashboardValues(String acquirer,
	String currency, String dateFrom, String dateTo) throws SystemException,
	ParseException {
PieChart pieChart = new PieChart();
try (Connection connection = getConnection()) {
	try (PreparedStatement preparedStatement = connection
			.prepareStatement("{call acquirerBarChart(?,?,?,?)}")) {
		DateFormat df = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss");
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
		String startDate = sdf1.format(df.parse(dateFrom));
		String endDate = sdf1.format(df.parse(dateTo));
		preparedStatement.setString(1, acquirer);
		preparedStatement.setString(2, currency);
		preparedStatement.setString(3, startDate);
		preparedStatement.setString(4, endDate);
		try (ResultSet rs = preparedStatement.executeQuery()) {
			rs.next();
			pieChart.setTotalCredit(rs
					.getString(CrmFieldConstants.TOTAL_CREDIT
							.getValue()));
			pieChart.setTotalDebit(rs
					.getString(CrmFieldConstants.TOTAL_DEBIT.getValue()));
			pieChart.setNet(rs
					.getString(CrmFieldConstants.TOTAL_NETBANKING
							.getValue()));
			pieChart.setOther(rs
					.getString(CrmFieldConstants.TOTAL_OTHER.getValue()));

		}
	}
} catch (SQLException exception) {
	logger.error("Database error", exception);
	throw new SystemException(ErrorType.DATABASE_ERROR,
			ErrorType.DATABASE_ERROR.getResponseMessage());
}
return pieChart;
}*/
}