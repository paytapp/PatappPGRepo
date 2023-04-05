import "./css/MobikwikSection.css";
import WalletMobileInput from "./WalletMobileInput";
import InsufficientBalance from "./InsufficientBalance.js"
import WalletPasswordInput from "./WalletPasswordInput";
import WalletBalance from "./WalletBalance";

const MobikwikSection = (props) => {
    let mobileInputBox = null;
    if(props.walletObj.wallet_mobile_isActive) {
        mobileInputBox = <WalletMobileInput
            verifyUserHandler={props.verifyUserHandler}
            resetErrorHandler={props.resetErrorHandler}
            editWalletNumberHandler={props.editWalletNumberHandler}
            walletObj={props.walletObj}
        />
    }

    let otpBox = null;
    if(props.walletObj.mobikwik_isOtpActive_state) {
        otpBox = <WalletPasswordInput
            loadWalletBalanceHandler={props.loadWalletBalanceHandler}
            mobikwikOtpHandler={props.mobikwikOtpHandler}
            walletObj={props.walletObj}
            updateMobikwikState={props.updateMobikwikState}
        />
    }

    let insufficientBox = null;
    if(props.walletObj.walletSufficientBalance && window.id("wallet-moptype").value !== window.walletToCompare) {
        insufficientBox = <InsufficientBalance
            walletObj={props.walletObj}
            loaderHandler={props.loaderHandler}
            getTotalAmount={props.getTotalAmount}
            loadWalletBalanceHandler={props.loadWalletBalanceHandler}
        />
    }

    let loadBalanceBox = null;
    if(props.walletObj.loadBalance) {
        loadBalanceBox = (
            <>
                <WalletBalance
                    loadWalletBalanceHandler={props.loadWalletBalanceHandler}
                    walletObj={props.walletObj}                
                />

                { insufficientBox }
            </>
        );
    }

    return (
        <div className="row mt-20" id="mobikwik-section">
            { mobileInputBox }            
            { otpBox }
            { loadBalanceBox }            
        </div>
    );
}

export default MobikwikSection;