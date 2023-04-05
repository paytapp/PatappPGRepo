import React from "react";
import "./css/MobikwikSection.css";
import TitleSection from "../TitleSection/TitleSection";
import WalletMobileInput from "./WalletMobileInput";
import WalletPasswordInput from "./WalletPasswordInput";

const WalletLogin = props => {
    let walletPassword = null;
    if(props.walletObj.wallet_isOtpActive_state) {
        walletPassword = <WalletPasswordInput
            walletObj={props.walletObj}
            loadWalletBalanceHandler={props.loadWalletBalanceHandler}
            updateMobikwikState={props.updateMobikwikState}
            mobikwikOtpHandler={props.mobikwikOtpHandler}
        />
    }

    return (
        <React.Fragment>
            <TitleSection
                title={props.title}
                navigationHandler={props.navigationHandler}
                btnDataId={props.activeNavigation}
                paymentType={props.paymentType}
            />

            <div className="container custom-container mt-170">
                <div className="row" id="mobikwik-section">
                    <WalletMobileInput
                        walletObj={props.walletObj}
                        numberInputHandler={props.numberInputHandler}
                        verifyUserHandler={props.verifyUserHandler}
                        resetErrorHandler={props.resetErrorHandler}
                        editWalletNumberHandler={props.editWalletNumberHandler}
                    />

                    { walletPassword }
                </div>
            </div>            
        </React.Fragment>
    );
}

export default WalletLogin;