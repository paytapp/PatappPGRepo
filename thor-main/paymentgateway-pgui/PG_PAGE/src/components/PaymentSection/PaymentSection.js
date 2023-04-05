import React, { useState, useEffect } from "react";
import PaymentBox from "./PaymentBox";

const PaymentSection = props => {
    const [usePaymentType, setPaymentType] = useState(null);

    useEffect(() => {
        if(props.paymentType == null) {
            let _list = document.querySelector(".tabLi"),
                _paymentType = _list.getAttribute("data-type");

            setPaymentType(_paymentType);
        } else {
            setPaymentType(props.paymentType);
        }
    }, [props.paymentType]);

    return (
        <div className="row paymentSections">
            <PaymentBox
                klass={props.klass}
                submitHandler={props.submitHandler}
                cancelHandler={props.cancelHandler}
                paymentType={usePaymentType}
                dataObj={props.dataObj}
                walletSelectHandler={props.walletSelectHandler}
                verifyUserHandler={props.verifyUserHandler}
                mobikwikOtpHandler={props.mobikwikOtpHandler}
                resetErrorHandler={props.resetErrorHandler}
                startTimerHandler={props.startTimerHandler}
                loadWalletBalanceHandler={props.loadWalletBalanceHandler}
                editWalletNumberHandler={props.editWalletNumberHandler}
                walletObj={props.walletObj}
                validateForm={props.validateForm}
                loaderHandler={props.loaderHandler}
                getTotalAmount={props.getTotalAmount}
                updateMobikwikState={props.updateMobikwikState}
                deleteHandler={props.deleteHandler}
            />
        </div>
    );
}

export default PaymentSection;