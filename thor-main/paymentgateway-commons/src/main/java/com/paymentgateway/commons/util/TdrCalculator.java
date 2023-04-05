package com.paymentgateway.commons.util;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.ChargingDetailsFactory;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.ChargingDetails;
import com.paymentgateway.commons.user.TransactionHistory;
import com.paymentgateway.commons.user.TransactionSummaryReport;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PercentageCalculator;
import com.paymentgateway.commons.util.TransactionType;

/**
 * @author Puneet
 *
 */

@Service
public class TdrCalculator {
	private BigDecimal netTdr;
	private BigDecimal serviceTax;
	private BigDecimal merchantServiceTax;
	private BigDecimal pgServiceTax;
	private BigDecimal fixCharge = BigDecimal.valueOf(0.00);
	private BigDecimal tdrRate;
	private BigDecimal bankTdr;
	private BigDecimal merchantTdr;
	private BigDecimal pgTdr;
	private BigDecimal merchantNetTdr;
	private BigDecimal pgNetTdr;
	private BigDecimal bankNetTdr;

	@Autowired
	private ChargingDetailsFactory chargingDetailsFactory;
	// for summary report page
	public TransactionSummaryReport setTdrValues(TransactionSummaryReport transactionSummaryReport, User user) {

		ChargingDetails detail = null;
		if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
				|| user.getUserType().equals(UserType.MERCHANT) || user.getUserType().equals(UserType.RESELLER)
				|| user.getUserType().equals(UserType.SUBUSER) || user.getUserType().equals(UserType.ACQUIRER)
				|| user.getUserType().equals(UserType.SUBACQUIRER)) {

			detail = chargingDetailsFactory.getChargingDetail(transactionSummaryReport.getTxnDate(),
					transactionSummaryReport.getPayId(), transactionSummaryReport.getAcquirer(),
					transactionSummaryReport.getPaymentMethod(), transactionSummaryReport.getMopType(),
					transactionSummaryReport.getTxnType(), transactionSummaryReport.getCurrencyCode());
		} else {
			Account account = user.getAccountUsingAcquirerCode(transactionSummaryReport.getAcquirer());
			detail = account.getChargingDetails(transactionSummaryReport.getTxnDate(),
					transactionSummaryReport.getPaymentMethod(), transactionSummaryReport.getMopType(),
					transactionSummaryReport.getTxnType(), transactionSummaryReport.getCurrencyCode());
		}
		if (null == detail) {
			return transactionSummaryReport;
		}
		int places = Currency.getNumberOfPlaces(transactionSummaryReport.getCurrencyCode());
		BigDecimal approvedAmount = BigDecimal
				.valueOf(Double.parseDouble(transactionSummaryReport.getApprovedAmount()));
		setNetTdr(approvedAmount, detail, places);
		transactionSummaryReport.setTdr(netTdr.setScale(places, BigDecimal.ROUND_HALF_UP).toString());
		transactionSummaryReport.setServiceTax(serviceTax.setScale(places, BigDecimal.ROUND_UP).toString());
		transactionSummaryReport.setNetAmount(approvedAmount.subtract(netTdr).subtract(serviceTax)
				.setScale(places, BigDecimal.ROUND_DOWN).toString());
		transactionSummaryReport.setMerchantFixCharge(fixCharge.setScale(places, BigDecimal.ROUND_HALF_UP).toString());
		return transactionSummaryReport;
	}

	// For refund details page
	public TransactionHistory setTdrRefundDetails(TransactionHistory transDetails, int decimalPlaces) {
		ChargingDetails detail = chargingDetailsFactory.getChargingDetail(transDetails.getCreateDate(),
				transDetails.getPayId(), transDetails.getAcquirerCode(),
				transDetails.getPaymentType(),
				transDetails.getMopType(), transDetails.getTxnType(),
				transDetails.getCurrencyCode());
		// Set net amount and other charges
		transDetails.setRefundAvailable(new BigDecimal(transDetails.getCapturedAmount())
				.subtract(transDetails.getRefundedAmount()).setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP));
	//	setNetTdr(transDetails.getCapturedAmount(), detail, decimalPlaces);
		transDetails.setFixedTxnFee(fixCharge);
		transDetails.setMerchantTDR(tdrRate);
		transDetails.setPercentecServiceTax(BigDecimal.valueOf(detail.getBankServiceTax()));
		transDetails.setTdr(netTdr.setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP));
		transDetails.setNetAmount(new BigDecimal(transDetails.getAuthorizedAmount()).subtract(netTdr).subtract(serviceTax)
				.setScale(decimalPlaces, BigDecimal.ROUND_DOWN));
		transDetails.setServiceTax(serviceTax.setScale(decimalPlaces, BigDecimal.ROUND_UP));
		transDetails.setChargebackAmount(BigDecimal.valueOf(0.00)); // Remove or
		// not??
		return transDetails;
	}

	public void setNetTdr(BigDecimal capturedAmount, ChargingDetails chargingDetails, int places) {
		BigDecimal fixChargeLimit = BigDecimal.valueOf(chargingDetails.getFixChargeLimit());

		if (chargingDetails.isAllowFixCharge() && (capturedAmount.compareTo(fixChargeLimit)) == 1) {
			tdrRate = BigDecimal.valueOf(chargingDetails.getMerchantTDRAFC());
			fixCharge = BigDecimal.valueOf(chargingDetails.getMerchantFixChargeAFC());
			netTdr = PercentageCalculator.getPercentage(capturedAmount, chargingDetails.getMerchantTDRAFC());
			netTdr = netTdr.add(BigDecimal.valueOf(chargingDetails.getMerchantFixChargeAFC()));
		} else {
			tdrRate = BigDecimal.valueOf(chargingDetails.getMerchantTDR());
			fixCharge = BigDecimal.valueOf(chargingDetails.getMerchantFixCharge());
			netTdr = PercentageCalculator.getPercentage(capturedAmount, chargingDetails.getMerchantTDR());
			netTdr = netTdr.add(BigDecimal.valueOf(chargingDetails.getMerchantFixCharge()));
		}
		serviceTax = PercentageCalculator.getPercentage(netTdr, chargingDetails.getMerchantServiceTax());
	}

	// MIS report calculate TDR
	public TransactionSummaryReport setMisTransactionReportCalculate(
			TransactionSummaryReport transactionSummaryReport) {
		// check condition for Refuand and Sale
		if (transactionSummaryReport.getTxnType().equals(TransactionType.SALE.getName())
				|| transactionSummaryReport.getTxnType().equals(TransactionType.CAPTURE.getName())) {
			ChargingDetails detail = null;
			detail = chargingDetailsFactory.getChargingDetail(transactionSummaryReport.getTxnDate(),
					transactionSummaryReport.getPayId(), transactionSummaryReport.getAcquirer(),
					transactionSummaryReport.getPaymentMethod(), transactionSummaryReport.getMopType(),
					transactionSummaryReport.getTxnType(), transactionSummaryReport.getCurrencyCode());
			if (null == detail) {
				return transactionSummaryReport;
			}
			int places = Currency.getNumberOfPlaces(transactionSummaryReport.getCurrencyCode());
			BigDecimal approvedAmount = BigDecimal
					.valueOf(Double.parseDouble(transactionSummaryReport.getApprovedAmount()));
			setBankTdr(approvedAmount, detail, places);
			transactionSummaryReport.setTdr(bankNetTdr.setScale(places, BigDecimal.ROUND_HALF_UP).toString());
			transactionSummaryReport.setServiceTax(serviceTax.setScale(places, BigDecimal.ROUND_UP).toString());
			transactionSummaryReport.setTdr(merchantNetTdr.setScale(places, BigDecimal.ROUND_HALF_UP).toString());
			transactionSummaryReport.setServiceTax(merchantServiceTax.setScale(places, BigDecimal.ROUND_UP).toString());
			transactionSummaryReport.setTotalAggregatorcommissionAmount(
					(pgTdr.add(pgServiceTax)).setScale(places, BigDecimal.ROUND_DOWN).toString());
			transactionSummaryReport
					.setTotalAmountPayToMerchant(approvedAmount.subtract(merchantNetTdr.add(merchantServiceTax))
							.setScale(places, BigDecimal.ROUND_DOWN).toString());
			transactionSummaryReport.setTotalPayoutToNodal(approvedAmount.subtract(bankNetTdr.add(serviceTax))
					.setScale(places, BigDecimal.ROUND_DOWN).toString());
			return transactionSummaryReport;
		} else {
			BigDecimal approvedAmount = BigDecimal
					.valueOf(Double.parseDouble(transactionSummaryReport.getApprovedAmount()));
			transactionSummaryReport
					.setApprovedAmount(Constants.SUBTRACTION_SIGN.getValue() + (approvedAmount).toString());
			transactionSummaryReport.setTotalAggregatorcommissionAmount(Constants.FALSE.getValue());
			transactionSummaryReport
					.setTotalAmountPayToMerchant(Constants.SUBTRACTION_SIGN.getValue() + (approvedAmount).toString());
			transactionSummaryReport
					.setTotalPayoutToNodal(Constants.SUBTRACTION_SIGN.getValue() + (approvedAmount).toString());
			return transactionSummaryReport;
		}
	}

	public void setBankTdr(BigDecimal capturedAmount, ChargingDetails chargingDetails, int places) {
		BigDecimal fixChargeLimit = BigDecimal.valueOf(chargingDetails.getFixChargeLimit());

		if (chargingDetails.isAllowFixCharge() && (capturedAmount.compareTo(fixChargeLimit)) == 1) {
			merchantTdr = BigDecimal.valueOf(chargingDetails.getMerchantTDRAFC());
			merchantNetTdr = PercentageCalculator.getPercentage(capturedAmount, chargingDetails.getMerchantTDRAFC());
			merchantNetTdr = merchantNetTdr.add(BigDecimal.valueOf(chargingDetails.getMerchantFixChargeAFC()));
			pgTdr = BigDecimal.valueOf(chargingDetails.getPgTDRAFC());
			pgNetTdr = PercentageCalculator.getPercentage(capturedAmount, chargingDetails.getPgTDRAFC());
			pgNetTdr = pgNetTdr.add(BigDecimal.valueOf(chargingDetails.getPgChargeAFC()));
			bankTdr = BigDecimal.valueOf(chargingDetails.getBankTDRAFC());
			bankNetTdr = PercentageCalculator.getPercentage(capturedAmount, chargingDetails.getBankTDRAFC());
			bankNetTdr = bankNetTdr.add(BigDecimal.valueOf(chargingDetails.getBankFixChargeAFC()));
			fixCharge = BigDecimal.valueOf(chargingDetails.getMerchantFixChargeAFC());

		} else {
			merchantTdr = BigDecimal.valueOf(chargingDetails.getMerchantTDR());
			merchantNetTdr = PercentageCalculator.getPercentage(capturedAmount, chargingDetails.getMerchantTDR());
			merchantNetTdr = merchantNetTdr.add(BigDecimal.valueOf(chargingDetails.getMerchantFixCharge()));
			pgTdr = BigDecimal.valueOf(chargingDetails.getPgTDR());
			pgNetTdr = PercentageCalculator.getPercentage(capturedAmount, chargingDetails.getPgTDR());
			pgNetTdr = pgNetTdr.add(BigDecimal.valueOf(chargingDetails.getPgFixCharge()));
			bankTdr = BigDecimal.valueOf(chargingDetails.getBankTDR());
			bankNetTdr = PercentageCalculator.getPercentage(capturedAmount, chargingDetails.getBankTDR());
			bankNetTdr = bankNetTdr.add(BigDecimal.valueOf(chargingDetails.getBankFixCharge()));
			fixCharge = BigDecimal.valueOf(chargingDetails.getMerchantFixCharge());
		}
		merchantServiceTax = PercentageCalculator.getPercentage(merchantNetTdr,
				chargingDetails.getMerchantServiceTax());
		pgServiceTax = PercentageCalculator.getPercentage(pgNetTdr, chargingDetails.getMerchantServiceTax());
		serviceTax = PercentageCalculator.getPercentage(bankNetTdr, chargingDetails.getMerchantServiceTax());
	}
}
