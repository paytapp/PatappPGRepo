package com.paymentgateway.notification.sms;
/*package com.paymentgateway.notification.sms;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import com.paymentgateway.notification.sms.sendSms.SmsSenderData;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PaymentGatewayNotificationSmsApplication.class)
@EnableAutoConfiguration
@ComponentScan("com.paymentgateway")
public class SmsSenderTest {
	
   @Autowired
	private SmsSenderData smsSender;
	@Test
	public void test() {
		Map<String,String> responseMap = new HashMap<String, String>();
		responseMap.put("PAY_ID", "1708011210131006");
		responseMap.put("transactionEmailerFlag", "true");
		responseMap.put("mobile", "8860705801");
		smsSender.sendSMS(responseMap);
		
	}

}
*/