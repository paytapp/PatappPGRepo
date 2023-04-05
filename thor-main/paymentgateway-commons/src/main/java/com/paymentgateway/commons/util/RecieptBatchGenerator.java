package com.paymentgateway.commons.util;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.RecieptBatchGeneratorDao;

@Service
public class RecieptBatchGenerator {

	/**
	 * Sandeep Sharma
	 */

	private static Logger logger = LoggerFactory.getLogger(RecieptBatchGenerator.class.getName());

	@Autowired
	private RecieptBatchGeneratorDao recieptBatchGeneratorDao;

	String initialAlpha = "A";
	String initialNumericReciept = "000001";
	String newRecieptNo = null;
	String newBatchNo = null;

	public synchronized String getNewRecieptNo() {
		try {
			Map<String, String> details = recieptBatchGeneratorDao.fatchLastRecieptNo();
			if (details.isEmpty()) {
				newRecieptNo = initialAlpha + initialNumericReciept;
				recieptBatchGeneratorDao.insertLatestRecieptNo(newRecieptNo, initialAlpha);
			} else {
				String recieptNo = details.get(FieldType.RECIEPT_NO.getName());
				String alphaseries = details.get(FieldType.ALPHA_SERIES.getName());

				if (recieptNo.substring(1, 7).equals("999999")) {
					char alpha = alphaseries.charAt(0);
					alpha++;

					if (alpha > 'Z') {
						logger.info("limit excced of reciept no is Z999999");
					} else {
						alphaseries = String.valueOf(alpha);
						String numericSeriesReciept = initialNumericReciept;
						newRecieptNo = alphaseries + numericSeriesReciept;
					}

				} else {
					alphaseries = recieptNo.substring(0, 1);
					long firstreciept = Long.valueOf(recieptNo.substring(1, 7)) + 1;
					String recieptseries = String.valueOf(String.format("%06d", firstreciept));
					newRecieptNo = alphaseries + recieptseries;
				}

				if (recieptNo.equals(newRecieptNo)) {
					logger.debug("duplicate reciept no found " + recieptNo);
				}

				recieptBatchGeneratorDao.updateLatestRecieptNo(recieptNo, newRecieptNo, alphaseries);
			}

		} catch (Exception e) {
			logger.error("Exception ", e);
		}

		return newRecieptNo;

	}

	public synchronized String getNewBatchNo() {
		try {
			Map<String, String> details = recieptBatchGeneratorDao.fatchLastBatchNo();
			if (details.isEmpty()) {
				newBatchNo = "000000";
				recieptBatchGeneratorDao.insertLatestBatchNo(newBatchNo);
			} else {
				String batchNo = details.get(FieldType.BATCH_NO.getName());

				if (batchNo.equals("999999")) {
					logger.error("limit excced for batch no is 999999");
				} else {
					newBatchNo = batchNo;
				}
				if (batchNo.equals(newBatchNo)) {
					logger.debug("duplicate batch no found " + batchNo);
				}

				recieptBatchGeneratorDao.updateLatestBatchNo(batchNo, newBatchNo);
			}

		} catch (Exception e) {
			logger.error("Exception ", e);
		}

		return newBatchNo;
	}
}
