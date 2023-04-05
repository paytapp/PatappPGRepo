package com.paymentgateway.commons.util;
/*package com.paymentgateway.commons.util;

import org.apache.struts2.interceptor.BackgroundProcess;
import org.apache.struts2.interceptor.ExecuteAndWaitInterceptor;
import org.springframework.stereotype.Service;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;

@Service
public class ExecuteAndWaitPaymentPage extends ExecuteAndWaitInterceptor {

	 private static final long serialVersionUID = 1L;


	    *//**
	     * {@inheritDoc}
	     *//*
	    @Override
	    protected BackgroundProcess getNewBackgroundProcess(String arg0, ActionInvocation arg1, int arg2) {
	        return new YourBackgroundProcess(arg0, arg1, arg2, ActionContext.getContext());
	    }

	}



	 class YourBackgroundProcess extends BackgroundProcess {

	    *//**
		 * 
		 *//*
		private static final long serialVersionUID = 7288248769934514866L;
		private final ActionContext context;

	    public YourBackgroundProcess(String threadName, ActionInvocation invocation, int threadPriority, ActionContext context) {
	        super(threadName, invocation, threadPriority);
	        this.context = context;
	     }

	    *//**
	     * {@inheritDoc}
	     *//*
	    @Override
	    protected void beforeInvocation() {
	        ActionContext.setContext(context);
	    }

	    *//**
	     * {@inheritDoc}
	     *//*
	   @Override
	    protected void afterInvocation() {
	        ActionContext.setContext(null);
	    }

	}


*/