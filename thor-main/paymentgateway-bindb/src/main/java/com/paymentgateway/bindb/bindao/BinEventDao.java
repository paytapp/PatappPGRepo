package com.paymentgateway.bindb.bindao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.HibernateAbstractDao;
import com.paymentgateway.commons.exception.DataAccessLayerException;
import com.paymentgateway.commons.util.BinRangeEvent;

@Service
public class BinEventDao extends HibernateAbstractDao{
	
	private static final String getCompleteBinRangeDetailQueryLow = "from BinRange b where b.binCodeLow = :binCodeLow";
	private static final String getCompleteBinRangeDetailQueryHigh = "from BinRange b where b.binCodeHigh = :binCodeHigh";

	private static Logger logger = LoggerFactory.getLogger(BinEventDao.class.getName());

	public void create(BinRangeEvent binRange) throws DataAccessLayerException {
		super.save(binRange);
	}

}
