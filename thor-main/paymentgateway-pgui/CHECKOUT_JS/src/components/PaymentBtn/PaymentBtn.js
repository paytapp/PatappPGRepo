import React from "react";

const PaymentBtn = props => {
    let totalAmount = null;
    if(window.name === "add-and-pay") {
        totalAmount = window.parent.document.getElementById("amountToAdd").value;
    } else {
        totalAmount = props.totalAmount;        
    }

    return (        
        <div className="row position-fixed w-100 bottom-0">
            {/* <div className="col-6 px-0">
                <button type="reset" className="btn-primary w-100 border-radius-none border-none font-weight-bold font-size-16 py-10 btn-disabled d-flex align-items-center justify-content-center">Cancel</button>
            </div> */}

            <div className="col-12 px-0">
                <button type="button" id="pay-now" onClick={props.submitHandler} data-id={props.formId} className={`btn btn-primary w-100 border-radius-none border-none font-weight-bold font-size-16 py-10 d-flex align-items-center justify-content-center ${props.customClass} ${props.isActivePayBtn ? '' : 'btn-disabled'}`}>
                    <span className="mr-10">Pay</span> <i className="pg-icon icon-inr"></i> <span className="payBtnAmount ml-5">{ totalAmount }</span>
                </button>
            </div>
        </div>       
    );
}

export default PaymentBtn;