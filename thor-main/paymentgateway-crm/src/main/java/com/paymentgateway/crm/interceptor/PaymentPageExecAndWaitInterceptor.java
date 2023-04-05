package com.paymentgateway.crm.interceptor;
import org.apache.struts2.interceptor.BackgroundProcess;
import org.apache.struts2.interceptor.ExecuteAndWaitInterceptor;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;

public class PaymentPageExecAndWaitInterceptor extends ExecuteAndWaitInterceptor {

    private static final long serialVersionUID = 1L;


    /**
     * {@inheritDoc}
     */
    @Override
    protected BackgroundProcess getNewBackgroundProcess(String arg0, ActionInvocation arg1, int arg2) {
        return new YourBackgroundProcess(arg0, arg1, arg2, ActionContext.getContext());
    }

}


 class YourBackgroundProcess extends BackgroundProcess {

    /**
	 * 
	 */
	private static final long serialVersionUID = 6096088013180447932L;
	private final ActionContext context;

    public YourBackgroundProcess(String threadName, ActionInvocation invocation, int threadPriority, ActionContext context) {
        super(threadName, invocation, threadPriority);
        this.context = context;
     }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void beforeInvocation() {
        ActionContext.setContext(context);
    }

    /**
     * {@inheritDoc}
     */
   @Override
    protected void afterInvocation() {
        ActionContext.setContext(context);
    }

}