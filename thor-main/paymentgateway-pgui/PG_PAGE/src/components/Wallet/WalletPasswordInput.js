import {useState, useRef, useEffect} from "react";
import ReactCodesInput from 'react-codes-input';
import 'react-codes-input/lib/react-codes-input.min.css';

const WalletPasswordInput = props => {
    const $pinWrapperRef = useRef(null),
          [useWalletOtp, setPin] = useState("");
    
    useEffect(() => {
        if(props.walletObj.mobikwik_errorOtp_state !== null) {    
            setPin("");
        }
        
    }, [props.walletObj.mobikwik_errorOtp_state]);

    const inputChangeHandler = value => {
        props.updateMobikwikState({mobikwik_errorOtp_state: ""});
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

    return (
        
        <div className="col-sm-6 mt-15 mt-sm-0" id="wallet-otp-box">
            {/* <div className="d-inline-block"> */}
                <label className="w-100 text-grey-light font-size-12 line-height-15 d-inline-flex justify-content-between mb-5">
                    <span className="lang" data-key="enterOtp">Enter OTP</span>

                    {props.walletObj.mobikwik_showOtpTimer_state ? <span className="text-grey-dark font-size-12" id="otp-timer">Resend in <span>{props.walletObj.mobikwik_otpTimer_state}</span></span> : null}

                    {props.walletObj.mobikwik_showResendOtp_state ? <button className="font-weight-medium text-primary text-right font-size-14 text-underline border-none bg-none lang" data-key="resendOtp" id="btn-resend-otp" onClick={props.mobikwikOtpHandler}>Resend OTP</button> : null}                                
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
                
                {props.walletObj.mobikwik_otpMsgSent_state !== null ? <div className="font-size-12 text-green" id="msg-otp-sent">{props.walletObj.mobikwik_otpMsgSent_state}</div> : null}

                {props.walletObj.mobikwik_errorOtp_state !== null ? <div className="font-size-12 text-danger" id="error-otp">{props.walletObj.mobikwik_errorOtp_state}</div> : null}
            {/* </div> */}
        </div>
    );
}

export default WalletPasswordInput;