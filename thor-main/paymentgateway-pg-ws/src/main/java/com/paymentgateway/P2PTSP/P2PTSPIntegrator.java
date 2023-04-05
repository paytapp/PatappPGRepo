package com.paymentgateway.P2PTSP;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;

@Service
public class P2PTSPIntegrator {

	@Autowired
	private P2PTSPService p2PTSPService;

	private static final Logger logger = LoggerFactory.getLogger(P2PTSPIntegrator.class.getName());

	public void process(Fields fields) throws SystemException, IOException {

		send(fields);

	}// process

	public void send(Fields fields) throws SystemException, IOException {
		fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));
		fields.put(FieldType.ORIG_TXN_ID.getName(), fields.get(FieldType.TXN_ID.getName()));

		logger.info("Generating MQR code for PAY_ID: " + fields.get(FieldType.PAY_ID.getName()) + " with ORDER_ID: "
				+ fields.get(FieldType.ORDER_ID.getName()));
		fields = p2PTSPService.getQrCode(fields);
	}
}
