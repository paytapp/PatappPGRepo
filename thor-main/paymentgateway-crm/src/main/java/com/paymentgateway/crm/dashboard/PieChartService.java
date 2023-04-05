package com.paymentgateway.crm.dashboard;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;

@Component
public class PieChartService {
	@Autowired
	private BarChartQuery barChartQuery;

	private PieChart pieChart = new PieChart();

	private static Logger logger = LoggerFactory.getLogger(PieChartService.class.getName());

	public PieChartService() {

	}

	public PieChart getDashboardValues(String payId, String currency, String dateFrom, String dateTo)
			throws SystemException, ParseException {
		DateFormat df = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss");
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
		String startDate = sdf1.format(df.parse(dateFrom));
		String endDate = sdf1.format(df.parse(dateTo));

		HashMap<String, String> chartvalue = barChartQuery.chartTotalSummary(payId, currency, startDate, endDate);
		pieChart.setVisa(chartvalue.get(MopType.VISA.getName()));
		pieChart.setMastercard(chartvalue.get(MopType.MASTERCARD.getName()));
		pieChart.setNet(chartvalue.get(PaymentType.NET_BANKING.getName()));
		pieChart.setAmex(chartvalue.get(MopType.AMEX.getName()));
		pieChart.setMaestro(chartvalue.get(MopType.MAESTRO.getName()));
		pieChart.setEzeeClick(chartvalue.get(MopType.EZEECLICK.getName()));
		pieChart.setOther(chartvalue.get("Other"));

		return pieChart;
	}

	public PieChart getSaleDataForPieChart(String dateFrom, String dateTo, User sessionUser, String payId, String subMerchantId, String paymentRegion, String currency) throws SystemException, ParseException {
		
		String startDate = dateFrom + " 00:00:00";
		String endDate = dateTo + " 23:59:59";

		pieChart = barChartQuery.salePieChartTotalRecords(startDate, endDate, sessionUser, payId, subMerchantId, paymentRegion, currency);

		return pieChart;
	}

	public PieChart getRefundDataForPieChart(String dateFrom, String dateTo, User sessionUser, String payId, String subMerchantId,String paymentRegion, String currency) throws SystemException, ParseException {
		
		String startDate = dateFrom + " 00:00:00";
		String endDate = dateTo + " 23:59:59";

		pieChart = barChartQuery.refundPieChartTotalRecords(startDate, endDate, sessionUser, payId, subMerchantId, paymentRegion, currency);

		return pieChart;
	}
	
	public MerchantTransaction getHigestMerchantData(String dateFrom, String dateTo, String payId, boolean saleReportFlag, User sessionUser, String currency) throws SystemException, ParseException {
		
		String startDate = dateFrom + " 00:00:00";
		String endDate = dateTo + " 23:59:59";

		MerchantTransaction higestMerchant= barChartQuery.higestMerchantInAmountAndVolumne(startDate, endDate, payId, saleReportFlag, sessionUser, currency);

		return higestMerchant;
	}
	
	public MerchantTransaction getLowestMerchantData(String dateFrom, String dateTo, String payId, boolean saleReportFlag, User sessionUser, String currency) throws SystemException, ParseException {
		
		String startDate = dateFrom + " 00:00:00";
		String endDate = dateTo + " 23:59:59";

		MerchantTransaction higestMerchant= barChartQuery.lowestMerchantInAmountAndVolumne(startDate, endDate, payId, saleReportFlag, sessionUser, currency);

		return higestMerchant;
	}


}
