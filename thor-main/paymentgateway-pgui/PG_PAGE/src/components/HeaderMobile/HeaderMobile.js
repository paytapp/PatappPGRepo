import React from "react";
import Logo from "../Logo/Logo";

const HeaderMobile = props => {
    const navSlideHandler = e => {
        e.preventDefault();
    
        window.querySelector("body").classList.add("navigation-overlay--active");
    }

    let custName = null;
    if(props.dataObj.CUST_NAME !== null && props.dataObj.CUST_NAME !== undefined && props.dataObj.CUST_NAME !== "") {
        custName = (
            <li className="d-flex justify-content-between">
                <span className="text-grey-light">{props.dataObj.CUST_NAME}</span>
            </li>
        );
    }

    return (
        <React.Fragment>
            {/* LOGO MOBILE STARTED */}
            <div className="col-12 bg-white d-flex justify-content-between align-items-center py-15 border-radius-tl-20 border-radius-tr-20 d-md-none">
                <button className="font-size-20 text-primary d-md-none border-none bg-none" onClick={navSlideHandler}>
                    <i className="pg-icon icon-menu d-block"></i>
                </button>
                <div id="logo-mobile" className="ml-15">
                    <Logo />
                </div>
            </div>
            {/* LOGO MOBILE ENDED */}

            {/* MOBILE SUMMARY STARTED */}
            <div className="col-12 mobile-summary bg-white d-md-none">
                <ul className="list-unstyled bg-grey-primary p-15 mb-15 border-radius-8 box-shadow-primary border-primary font-size-14">

                    { custName }

                    <li className="d-flex justify-content-between flex-wrap">
                        <span className="text-grey-lighter lang" data-key="orderId">Order Id</span>
                        <span className="text-grey-light">{props.dataObj.ORDER_ID}</span>
                    </li>
                </ul>
            </div>
            {/* MOBILE SUMMARY ENDED */}
        </React.Fragment>
    )
}

export default HeaderMobile;