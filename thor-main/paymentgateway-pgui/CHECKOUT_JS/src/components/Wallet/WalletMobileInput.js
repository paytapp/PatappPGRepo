import Fade from 'react-reveal/Fade';
import { useState } from 'react';

const WalletMobileInput = props => {
    let mobileEditBtn = null;
    if(props.walletObj.walletMobileDisabled) {
        mobileEditBtn = (
            <div className="position-absolute top-0 right-0 h-100 d-flex align-items-center" id="btn-edit-wallet">
                <button className="text-primary p-0 border-none bg-none d-flex mr-15" onClick={(e) => {props.editWalletNumberHandler('edit', e);}}><i className="pg-icon icon-edit font-size-20"></i></button>
            </div>
        );
    }

    let errorMobile = null;
    if(props.walletObj.wallet_errorMobile_state !== "") {
        errorMobile = (
            <div className="font-size-12 line-height-15 text-danger" id="error-wallet-number">{props.walletObj.wallet_errorMobile_state}</div>
        );
    }

    return (
        <Fade bottom>
            <div className="col-sm-6" id="wallet-mobile-box">
                <label className="w-100 text-grey-light font-size-12 line-height-15 d-inline-flex mb-5">Mobile Number</label>

                <div className="position-relative">
                    <input
                        type="text"
                        autoComplete="off"
                        className="form-control font-size-14 font-size-sm-16 text-grey-dark font-weight-medium p-8 border outline-none"
                        id="wallet-mobile-number"
                        onInput={(e) => {props.numberInputHandler(e); props.verifyUserHandler(e); props.resetErrorHandler();}}
                        onPaste={props.inputHandler}
                        onDrop={props.inputHandler}
                        maxLength="10"
                        inputMode="numeric"
                        value={props.walletObj.walletMobile}
                        disabled={props.walletObj.walletMobileDisabled}
                    />

                    { mobileEditBtn }
                </div>

                <input type="hidden" id="phoneNo" name="phoneNo" />

                { errorMobile }
            </div>
        </Fade>
    );
}

export default WalletMobileInput;