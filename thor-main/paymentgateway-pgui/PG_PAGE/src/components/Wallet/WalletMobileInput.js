import { useState, useEffect } from "react";
import {inputHandler} from "../../js/script";

const WalletMobileInput = props => {
    const [useMobile, setMobile] = useState("");

    useEffect(() => {
        setMobile(props.walletObj.walletLoggedNumber);
    }, [props.walletObj.walletLoggedNumber])

    let resetMobileNumber = null;
    if(props.walletObj.walletMobileDisabled) {
        resetMobileNumber = (
            <div className="text-right line-height-15" id="btn-edit-wallet">
                <button className="font-size-12 text-primary text-underline lang p-0 border-none bg-none" data-key="useAnotherMobile" onClick={(e) => {props.editWalletNumberHandler('edit', e);}}>Use another Mobile Number</button>
            </div>
        );
    }

    const isNumberKey = evt => {
        let elementValue = evt.target.value;

        if (!(/^[0-9]+$/.test(elementValue))) {
            setMobile(elementValue.replace(/[^0-9]/g, ""));
        } else {
            setMobile(elementValue);
        }
    }

    return (
        <div className="col-sm-6" id="wallet-mobile-box">
            <label className="w-100 text-grey-light font-size-12 line-height-15 d-inline-flex mb-5 lang" data-key="mobileNumber">Mobile Number</label>
            <input
                type="text"
                autoComplete="off"
                className="form-control font-size-14 font-size-sm-16 text-grey-dark font-weight-medium p-8 border outline-none"
                id="wallet-mobile-number"
                onInput={(e) => {isNumberKey(e); props.verifyUserHandler(e); props.resetErrorHandler();}}
                onKeyPress={isNumberKey}
                onPaste={inputHandler}
                onDrop={inputHandler}
                maxLength="10"
                value={useMobile}
                disabled={props.walletObj.walletMobileDisabled}
                inputMode="numeric" />

            { resetMobileNumber }            
            

            {props.walletObj.mobikwik_errorMobile_state !== null ? <div className="font-size-12 line-height-15 text-danger" id="error-wallet-number">{props.walletObj.mobikwik_errorMobile_state}</div> : null }
        </div>
    );
}

export default WalletMobileInput;