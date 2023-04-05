import React, { Component } from "react";
import Fade from 'react-reveal/Fade';
import TitleSection from "../TitleSection/TitleSection";
import NewWindow from "../NewWindow/NewWindow";
import PaymentBtn from "../PaymentBtn/PaymentBtn";

class WalletBalance extends Component {
    addMoneyHandler = e => {
        e.preventDefault();

        this.props.loaderHandler({showLoader: true});

        const payload = {
            payId : Window.pageInfoObj.PAY_ID,
            phoneNo : this.props.walletObj.walletLoggedNumber,
            otp : this.props.walletObj.walletOtp,
            totalAmount : (this.props.getTotalAmount("surcharge_wl") - Number(this.props.walletObj.walletBalance)).toFixed(2),
            walletName: this.props.selectedMopType
        }

        fetch(`${Window.baseUrl}/addMoney`, {
            method : "POST",
            body: JSON.stringify(payload),
            headers : {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }      
        })
        .then((response) => response.json())
        .then(responseJson => {
            if(responseJson.response == "Success") {
                const windowParam = NewWindow.iframe();

                let _newWin = window.open(responseJson.addMoneyUrl, "", "scrollbars=yes,width="+ windowParam.width +",height="+ windowParam.height +",top="+ windowParam.top +",left="+ windowParam.left +",toolbar=0");

                this.props.loaderHandler({showLoader: false});

                Window.addMoneyTimer = setInterval(() => {
                    if(_newWin !== null) {
                        if(_newWin.closed) {
                            clearInterval(Window.addMoneyTimer);
            
                            // this.props.loaderHandler({ showLoader: true });
    
                            this.props.loadWalletBalanceHandler({
                                hideLoader: true,
                                walletLoggedNumber: this.props.walletObj.walletLoggedNumber,
                                walletOtp: this.props.walletObj.walletOtp,
                                isOtpVerified: true
                            }, e);
                        }
                    }

                    
                    window.addEventListener("message", event => {
                        var obj = event.data;
                        if(obj.status) {
                            clearInterval(Window.addMoneyTimer);

                            // this.props.loaderHandler({ showLoader: true });
    
                            this.props.loadWalletBalanceHandler({
                                hideLoader: true,
                                walletLoggedNumber: this.props.walletObj.walletLoggedNumber,
                                walletOtp: this.props.walletObj.walletOtp,
                                isOtpVerified: true
                            }, e);
                        }
                    });
                }, 500);
            }            
        })
        .catch((error) => {
            console.error(error);
        });
    }

    render() {
        let inSufficientBalance = null;
        if(!this.props.walletObj.walletSufficientBalance) {
            inSufficientBalance = (
                <Fade right>
                    <div className="col-sm-6 text-center mt-15" id="insufficient-fund">
                        <div className="bg-grey-primary border border-radius-primary p-15 h-100">
                            <p className="text-danger font-size-14">
                                <span>Insufficient funds!</span><br />
                                <span>Please recharge your wallet...</span>
                            </p>
                            <button onClick={this.addMoneyHandler} className="btn btn-secondary font-size-14 d-inline-flex align-items-center">
                                <i className="pg-icon icon-plus-circle mr-5 font-size-16"></i>
                                <span>Add money</span>
                            </button>
                        </div>
                    </div>
                </Fade>
            );
        }
    
        return (
            <React.Fragment>
                <TitleSection
                    title={this.props.title}
                    navigationHandler={this.props.navigationHandler}
                    btnDataId="walletLogin"
                    paymentType={this.props.paymentType}
                    walletLoggedNumber={this.props.walletObj.walletLoggedNumber}
                    walletMobileDisabled={this.props.walletObj.walletMobileDisabled}
                    editWalletNumberHandler={this.props.editWalletNumberHandler}
                />
    
                <div className="container custom-container mt-180">
                    <form autoComplete="off" method="post" target="_self" id="wallet-form" action={`${Window.baseUrl}/pay`} onSubmit={(e) => { return this.props.validateFormHandler(e, 'wallet-moptype', 'wallet-list'); }}>
                        <div className="row" id="mobikwik-section">
                            <Fade left>
                                <div className="col-sm-6 text-center" id="available-balance">
                                    <div className="bg-grey-primary border border-radius-primary p-15 h-100">
                                        <div className="d-inline-block">
                                            <div className="d-flex align-items-center justify-content-center font-weight-bold font-size-30">
                                                <span className="pg-icon icon-inr mr-10"></span>
                                                <span id="mobikwik-amount">{this.props.walletObj.walletBalance}</span>
                                            </div>
        
                                            <button type="button" onClick={(e) => {this.props.loadWalletBalanceHandler({
                                                    hideLoader: true,
                                                    walletLoggedNumber: this.props.walletObj.walletLoggedNumber,
                                                    walletOtp: this.props.walletObj.walletOtp,
                                                    isOtpVerified: true
                                                }, e);}} className="font-size-12 d-flex align-items-center justify-content-center mtn-5 border-none">
                                                    <i className="pg-icon icon-refresh mr-5"></i>
                                                    <span className="text-underline">Refresh Balance</span>
                                            </button>
                                        </div>
                                        <div className="font-size-18 mt-10">Available Balance</div>
                                    </div>
                                </div>
                            </Fade>
                            
                            { inSufficientBalance }
                        </div>

                        <input type="hidden" name="paymentType" value="WL" />
                        <input type="hidden" name="mopType" id="wallet-moptype" value={this.props.selectedMopType} />
                        <input type="hidden" id="wlSaveFlag" name="wlSaveFlag" value="false" />
                        <input type="hidden" name="phoneNo" value={this.props.walletObj.walletLoggedNumber} />
                        <input type="hidden" name="otp" value={this.props.walletObj.walletOtp} />

                        <input type="hidden" name="encSessionData" value={this.props.data.encSessionData} />
                        <input type="hidden" name="CHECKOUT_JS_FLAG" value={this.props.data.checkOutJsFlag} />
                        <input type="hidden" name="payId" value={this.props.data.PAY_ID} />

                        <input type="hidden" name="token" value={this.props.walletObj.walletToken} />
                        <input type="hidden" name="paymentFlow" value={this.props.walletObj.paymentFlow} />

                        <button type="submit" className="btn-payment d-none"></button>

                        <PaymentBtn payBtnText={this.props.payBtnText} totalAmount={this.props.totalAmount} submitHandler={this.props.submitHandler} formId="wallet-form" isActivePayBtn={this.props.isActivePayBtn} />
                    </form>
                </div>
            </React.Fragment>
        );
    }
}

export default WalletBalance;