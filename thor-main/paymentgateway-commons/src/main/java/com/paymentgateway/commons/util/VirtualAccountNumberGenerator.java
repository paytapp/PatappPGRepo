package com.paymentgateway.commons.util;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.VirtualAccountNumberGeneratorDao;

/*
 * @auther Sandeep Sharma 
 */

@Service("virtualAccountNuberGenerator")
public class VirtualAccountNumberGenerator {

	@Autowired
	private VirtualAccountNumberGeneratorDao virtualAccountNumberGeneratorDao;

	private static Logger logger = LoggerFactory.getLogger(VirtualAccountNumberGenerator.class.getName());

	String initialNumeric = "50000000";
	String newVirtualAccountNo = null;

	public synchronized String getNewVirtualAccountNo() {
		try {
			Map<String, String> details = virtualAccountNumberGeneratorDao.fatchVirtualAccountNo();
			if (details.isEmpty()) {
				newVirtualAccountNo = initialNumeric;
				virtualAccountNumberGeneratorDao.insertLatestVirtualAccountNo(newVirtualAccountNo);
			} else {
				String virtualAccountNo = details.get(FieldType.VIRTUAL_ACC_NUM.getName());
				newVirtualAccountNo = virtualAccountNo;
			}

		} catch (Exception e) {
			logger.error("Exception " , e);
		}

		return newVirtualAccountNo;

	}

}
