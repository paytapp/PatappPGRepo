import React, { PureComponent } from "react";
import Checkbox from "../Checkbox/Checkbox";
import WalletList from "./WalletList";
import TitleSection from "../TitleSection/TitleSection";
import PaymentBtn from "../PaymentBtn/PaymentBtn";
import SavedWalletList from "./SavedWalletList";
// import Fade from 'react-reveal/Fade';

class Wallet extends PureComponent {    
    render() {
        let walletListToRender = null;

        if(this.props.title === "Wallet") {
            walletListToRender = this.props.data.wlMopType.map((list, index) => {
                return <WalletList
                    item={list}
                    key={list}
                    duration={index}
                    isActive={list === this.props.walletObj.wallet_ActiveWl_state ? true : false}
                    walletSelectHandler={this.props.walletSelectHandler}
                />
            });
        } else {            
            if(this.props.data.wlToken !== "NA") {
                walletListToRender = JSON.parse(this.props.data.wlToken).map((list, index) => {                
                    return <SavedWalletList
                        item={list}
                        key={list.key}
                        duration={index}
                        isActive={list.key === this.props.activeQuickPayId}
                        quickPayHandler={this.props.quickPayHandler}
                        deleteHandler={this.props.deleteHandler}
                    />
                });
            } else {
                this.props.tokenHandler();
            }
        }

        return (
            <React.Fragment>
                <TitleSection
                    title={this.props.title}
                    navigationHandler={this.props.navigationHandler}
                    btnDataId={this.props.activeNavigation == "savedWallet" ? "quickPay" : "navigation"}
                    paymentType={this.props.activeNavigation == "savedWallet" ? "SC" : null}
                />

                <div className="container custom-container mt-180 mb-60">
                    <div className="tabBox walletBox" id="wallet">
                        <form autoComplete="off" method="post" target="_self" id="wallet-form" action={`${Window.baseUrl}/pay`} onSubmit={(e) => { return this.props.validateFormHandler(e, 'wallet-moptype', 'wallet-list'); }}>
                            <div className={`card-detail-box${this.props.displayError ? ' redLine' : ''}`} id={this.props.title === "Wallet" ? "wallet-list" : "quick-pay"}>
                                <div className="row">
                                    { walletListToRender }
                                </div>
                            </div>
        
                            <input type="hidden" name="paymentType" value="WL" />
                            <input type="hidden" name="mopType" id="wallet-moptype" value={this.props.selectedMopType} />

                            <input type="hidden" name="token" value={this.props.walletObj.walletToken} />
                            <input type="hidden" name="paymentFlow" value={this.props.walletObj.paymentFlow} />
                            
                            {
                                this.props.displayError ?
                                    <p id="error-wallet-list" className="text-danger font-size-12 mb-0" data-key="errorWalletList">Please choose any wallet type</p>
                                : null
                            }                            

                            {
                                this.props.title === "Wallet" ?
                                <div className="row">
                                    <Checkbox
                                        columnId="divWlSave"
                                        checked={this.props.data.save_wl}
                                        name="wlSaveFlag"
                                        dataKey="saveWlText"
                                        checkboxText="Save this wallet for future payments"
                                    />
                                </div>
                                : <input type="hidden" id="wlSaveFlag" name="wlSaveFlag" value="false" />
                            }
                            
                            <button type="submit" className="btn-payment d-none"></button>

                            <input type="hidden" name="encSessionData" value={this.props.data.encSessionData} />
                            <input type="hidden" name="CHECKOUT_JS_FLAG" value={this.props.data.checkOutJsFlag} />
                            <input type="hidden" name="payId" value={this.props.data.PAY_ID} />

                            <PaymentBtn
                                payBtnText={this.props.payBtnText}
                                totalAmount={this.props.totalAmount}
                                submitHandler={this.props.submitHandler}
                                formId="wallet-form"
                                isActivePayBtn={this.props.isActivePayBtn}
                            />
                        </form>                    
                    </div>
                </div>
            </React.Fragment>
        );
    }
}

export default Wallet;