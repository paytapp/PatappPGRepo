import { myCancelAction } from "../../js/script";
import React, { useState } from "react";
import CancelButton from "../CancelButton/CancelButton";
import SubmitButton from "../SubmitButton/SubmitButton";
import useWindowSize from "../../utils/useWindowSize";
import Language from "../Language/Language";

function OrderSummary(props) {    
    const { width } = useWindowSize();

    let custName = null;
    if(props.dataObj.CUST_NAME !== null && props.dataObj.CUST_NAME !== undefined && props.dataObj.CUST_NAME !== "") {
        custName = (
            <li className="justify-content-between border-bottom-darker pb-10 mb-10" id="customerName">
                <span className="text-white">{props.dataObj.CUST_NAME}</span>
            </li>
        );
    }

    const rupeeIcon = <i className=" pg-icon icon-inr mr-5"></i>;

    return (
        <div className="col-12 col-lg-3 d-lg-block mh-xl-525" id="summary-column">
            <div className="mt-lg-95 mb-lg-50 px-xl-15">
                <div id="summary-wrap-desktop">

                    { width < 768 && (
                        <div className="row d-md-flex mb-15 mb-md-0">
                            <div className="col-12 d-flex justify-content-end" id="lang-switch-mobile">
                                <Language />
                            </div>
                        </div>
                    ) }

                    <div className="row">
                        <div className="col-12">
                            <h2 className="font-weight-light font-size-18 font-size-lg-24 font-size-xl-28 text-blue-light border-bottom-darker pb-10 mb-10 lang" data-key="summary" id="summary-title">Summary</h2>
                            <ul className="list-unstyled mb-0 font-size-14" id="order-summary">
                                { custName }

                                <li className="d-flex justify-content-between flex-wrap" id="order-id">
                                    <span className="text-white lang summary-label" data-key="orderId">Order ID</span>
                                    <span className="text-white summary-label-text" title="LP1623036062390">{props.dataObj !== null ? props.dataObj.ORDER_ID : null}</span>
                                </li>

                                <li className={`justify-content-between flex-wrap ${props.dataObj.isSurcharge ? 'd-flex' : 'd-none'}`} id="amout_tab">
                                    <span className="text-white lang summary-label" data-key="amount" id="amount">Amount</span>
                                    <span className="text-white summary-label-text d-inline-flex align-items-center" id="innerAmount">
                                        { rupeeIcon }
                                        <span className="value-block">{(Number(props.dataObj.AMOUNT) / 100).toFixed(2)}</span>
                                    </span>
                                </li>

                                <li className={`justify-content-between flex-wrap ${props.dataObj.isSurcharge ? 'd-flex' : 'd-none'}`} id="tdrBLock_head">
                                    <span id="surchargeName" className="text-white lang summary-label" data-key="convenienceFee">Convenience Fee</span>
                                    <span className="text-white summary-label-text d-inline-flex align-items-center" id="surcharge">
                                        { rupeeIcon }
                                        <span className="value-block"></span>
                                    </span>
                                </li>
                                
                                <li className="justify-content-between flex-wrap d-none" id="gst-block">
                                    <span id="gstName" className="text-white lang summary-label" data-key="gst">GST</span>
                                    <span className="text-white summary-label-text d-inline-flex align-items-center" id="gstAmount">
                                        { rupeeIcon }
                                        <span className="value-block"></span>
                                    </span>
                                </li>

                                <li className="d-flex justify-content-between font-size-18 font-weight-bold flex-wrap" id="new_head">
                                    <span className="text-white lang summary-label" data-key="amountPayable">Amount Payable</span>
                                    <span className="text-white summary-label-text d-inline-flex align-items-center" id="totalAmount">
                                        <i className=" pg-icon icon-inr mr-5 font-size-15"></i>
                                        <span className="value-block"></span>
                                    </span>
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>

                { width >= 991 && (
                    <React.Fragment>
                        <div id="submit-btns-desktop">
                            <SubmitButton submitHandler={props.submitHandler} />
                            <CancelButton cancelHandler={props.cancelHandler} dataObj={props.dataObj} />
                        </div>

                        <div className="d-flex text-grey-lighter align-items-center mt-15 justify-content-center mb-15" id="safe-secure-logo">
                            <i className="pg-icon icon-secure-payment font-size-26 text-white"></i> <span className="font-size-12 ml-10 text-white">Safe and Secure Payments</span>
                        </div>
                    </React.Fragment>
                ) }
            </div>
        </div>
    );
}

export default OrderSummary;