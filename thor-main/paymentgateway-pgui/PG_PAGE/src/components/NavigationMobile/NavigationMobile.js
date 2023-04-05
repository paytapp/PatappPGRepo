import React from "react";
import OrderSummary from "../OrderSummary/OrderSummary";

const NavigationMobile = props => {
    const closeNavSlideHandler = e => {
        e.preventDefault();

        window.querySelector("body").classList.remove("navigation-overlay--active");
    }
    return (
        <React.Fragment>
            <div className="navigation-overlay" id="navigation-overlay">
                <div className="d-flex justify-content-between justify-content-md-end mb-15 mr-15">
                    <div id="merchantName-mobile" className="font-size-18 font-weight-medium text-white d-md-none ml-15 mr-15">
                        {props.data.merchantType}
                    </div>
                    <button id="btn-close-navigation" onClick={closeNavSlideHandler} className="text-primary font-size-20 bg-none border-none">
                        <i className="pg-icon icon-cancel-circle"></i>
                    </button>
                </div>
                <div id="summary-wrap-mobile">
                    <OrderSummary
                        dataObj={props.data}
                        submitHandler={props.submitHandler}
                        cancelHandler={props.cancelHandler}
                    />
                </div>
            </div>
            <div className="popup-overlay"></div>
        </React.Fragment>
    );
}

export default NavigationMobile;