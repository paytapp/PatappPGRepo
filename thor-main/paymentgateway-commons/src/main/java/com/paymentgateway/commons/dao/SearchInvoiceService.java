package com.paymentgateway.commons.dao;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.Invoice;

@Service
public class SearchInvoiceService {

	@Autowired
	private InvoiceDao invoiceDao;

	public SearchInvoiceService() {
	}

	public List<Invoice> getInvoiceList(String fromDate, String toDate, String merchantPayId, String userType,
			/*String invoiceNo,*/ String customerEmail, String currency, String invoiceType,String status, String phone, String productName,String subMerchantId, String subUserId)
			throws SQLException, ParseException, SystemException {
		List<Invoice> invoiceList = new ArrayList<Invoice>();
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");

		invoiceList = invoiceDao.getInvoiceList(sdf1.format(sdf.parse(fromDate)), sdf1.format(sdf.parse(toDate)),
				merchantPayId, userType, /*invoiceNo,*/ customerEmail, currency, invoiceType,status,phone,productName,subMerchantId, subUserId);

		return invoiceList;
	}
	
	public List<Invoice> getInvoiceListBySubUserId(String fromDate, String toDate, String merchantPayId, String status,  String subUserId)
			throws SQLException, ParseException, SystemException {
		List<Invoice> invoiceList = new ArrayList<Invoice>();
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");

		invoiceList = invoiceDao.getInvoiceListBySubUserId(sdf1.format(sdf.parse(fromDate)), sdf1.format(sdf.parse(toDate)),
				merchantPayId, status, subUserId);

		return invoiceList;
	}
}
