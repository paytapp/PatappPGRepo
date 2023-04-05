package com.paymentgateway.pg.ws;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import org.apache.catalina.filters.RemoteAddrFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.PropertiesManager;

@Component
public class FilterRegistration {

	@Autowired
	private PropertiesManager propertiesManager;
	
	@Bean
	public FilterRegistrationBean myFilterRegistration() {
	    FilterRegistrationBean registration = new FilterRegistrationBean();
	    registration.setFilter(addressFilter());
	    registration.setDispatcherTypes(DispatcherType.REQUEST);
	    registration.addUrlPatterns(Constants.INTERNAL_WEBSERVICE_URL.getValue(),Constants.INTERNAL_ICICI_IMPS_URL.getValue(), Constants.INTERNAL_ICICI_CIB_URL.getValue());
	    registration.addInitParameter(Constants.ALLOW.getValue(), propertiesManager.propertiesMap.get(Constants.INTERNAL_WEBSERVICE_ALLOWED_IP.getValue()));
	    registration.setName(Constants.INTERNAL_WEBSERVICE_FILTER_NAME.getValue());
	    registration.setOrder(1);

	    return registration;
	}
	public Filter addressFilter() {
	    return new RemoteAddrFilter();
	}
}
