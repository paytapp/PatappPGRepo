import React, { useState, useRef, useEffect } from "react";
import ReactCodesInput from 'react-codes-input';
import 'react-codes-input/lib/react-codes-input.min.css';
import Fade from 'react-reveal/Fade';

const WalletPasswordInput = props => {
    const $pinWrapperRef = useRef(null),
          [useWalletOtp, setPin] = useState("");

    useEffect(() => {
        if(props.walletObj.wallet_errorOtp_state !== null) {    
            setPin("");
        }
        
    }, [props.walletObj.wallet_errorOtp_state]);

    const inputChangeHandler = value => {
        props.updateMobikwikState({wallet_errorOtp_state: ""});
        if(value.length === 6) {
            setPin(value);

            props.loadWalletBalanceHandler({
                hideLoader: true,
                walletLoggedNumber: props.walletObj.walletLoggedNumber,
                walletOtp: value,
                isOtpVerified: false
            });
        }
    }

    let otpTimer = null;
    if(props.walletObj.wallet_showOtpTimer_state) {
        otpTimer = (
            <span className="text-grey-dark font-size-12" id="otp-timer">Resend in <span>{props.walletObj.wallet_otpTimer_state}</span></span>
        );
    }

    let resendOtp = null;
    if(props.walletObj.wallet_showResendOtp_state) {
        resendOtp = (
            <button className="font-weight-medium text-primary text-right font-size-14 text-underline border-none bg-none" id="btn-resend-otp" onClick={props.mobikwikOtpHandler}>Resend OTP</button>
        );
    }

    let otpMsgSent = null;
    if(props.walletObj.wallet_otpMsgSent_state !== null) {
        otpMsgSent = (
            <div className="font-size-12 text-green" id="msg-otp-sent">{props.walletObj.wallet_otpMsgSent_state}</div>
        );
    }

    let otpMsgError = null;
    if(props.walletObj.wallet_errorOtp_state !== null) {
        otpMsgError = (
            <div className="font-size-12 text-danger" id="error-otp">{props.walletObj.wallet_errorOtp_state}</div>
        );
    }

    return (
        <Fade bottom>
            <div className="col-sm-6 mt-15 mt-sm-0" id="wallet-otp-box">                
                <label className="w-100 text-grey-light font-size-12 line-height-15 d-inline-flex justify-content-between mb-5">
                    <span>Enter OTP</span>

                    { otpTimer }
                    { resendOtp }
                </label>

                <ReactCodesInput
                    initialFocus={true}
                    wrapperRef={$pinWrapperRef}
                    id="pin"
                    codeLength={6}
                    type="number"
                    hide={true}
                    value={useWalletOtp}
                    onChange={res => { inputChangeHandler(res); }}
                />

                <input type="hidden" name="otp" id="wallet-otp" />
                
                { otpMsgSent }
                { otpMsgError }                
            </div>
        </Fade>
    );
}

export default WalletPasswordInput;