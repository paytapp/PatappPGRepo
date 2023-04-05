package com.paymentgateway.pg.service;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

	/**
	 * @author Surender
	 *
	 */

	
	@RestController
	public class FssSimulator {

		private static final String response = "<result>APPROVED</result><auth>999999</auth><ref>503029434256</ref><avr>N</avr><postdate>0131</postdate><tranid>679921481950301</tranid><trackid>1501300748511000</trackid><payid>-1</payid><udf1>1501300748511000</udf1><udf2>surender@paymentGateway.com</udf2><udf4>1501300748511000</udf4><udf5>1501300748511000</udf5><amt>1.0</amt>";
		
	
		
		@RequestMapping("transaction")
		public String transact(@RequestParam (value="name", defaultValue="World") String name){
			return response;
		}
	}


