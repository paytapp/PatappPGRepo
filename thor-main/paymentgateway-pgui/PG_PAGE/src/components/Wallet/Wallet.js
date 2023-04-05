import React, { Component } from "react";
import Checkbox from "../Checkbox/Checkbox";
import Declaration from "../Declaration/Declaration";
import { addCustomScroll, addConvenienceFee, multilingual } from "../../js/script";
import WalletList from "./WalletList";
import MobikwikSection from "./MobikwikSection";

class Wallet extends Component {
    componentDidMount() {
        addCustomScroll();
        addConvenienceFee(this.props.paymentType);
        multilingual();
    }

    render() {
        // LOGGED DETAILS
        let loggedInDetail = null;
        if(this.props.walletObj.loadBalance) {            
            loggedInDetail = (
                <div className="col-sm-8 text-right" id="wallet-logged-in">
                    <div className="d-inline-block text-right">
                        <div className="font-size-12 font-size-sm-14">
                            <span className="lang" data-key="loggedIn">You are logged in using</span> <span className="font-size-15 font-size-sm-16 font-size-sm-18 text-grey-dark font-weight-bold" id="wallet-logged-mobile">{ this.props.walletObj.walletLoggedNumber }</span> <span className="lang" data-key="loggedInAlt"></span>
                        </div>
                        <div className="line-height-15">
                            <button type="button" className="bg-none border-none text-underline font-size-10 font-size-sm-12 text-primary lang" data-key="useAnotherMobileAlt" onClick={e => this.props.editWalletNumberHandler("edit", e)}>Login using another Mobile Number</button>
                        </div>
                    </div>
                </div>    
            )
        }

        // SAVE DETAIL CHECK BOX
        let saveDetailCheckbox = null;
        if(!this.props.walletObj.mobikwik_isActive_state) {
            saveDetailCheckbox = (
                <div className="row">
                    <Checkbox
                        columnId="divWlSave"
                        checked={this.props.dataObj.save_wl}
                        name="wlSaveFlag"
                        dataKey="saveWlText"
                        checkboxText="Save this Wallet for future payments"
                    />
                </div>
            )             
        }

        // MOBIKWIK SECTION
        let mobikwikSection = null;
        if(this.props.walletObj.mobikwik_isActive_state) {
            mobikwikSection = <MobikwikSection
                verifyUserHandler={this.props.verifyUserHandler}
                mobikwikOtpHandler={this.props.mobikwikOtpHandler}
                resetErrorHandler={this.props.resetErrorHandler}
                startTimerHandler={this.props.startTimerHandler}
                loadWalletBalanceHandler={this.props.loadWalletBalanceHandler}
                editWalletNumberHandler={this.props.editWalletNumberHandler}
                walletObj={this.props.walletObj}
                loaderHandler={this.props.loaderHandler}
                getTotalAmount={this.props.getTotalAmount}
                updateMobikwikState={this.props.updateMobikwikState}
            />
        }

        // AMOUNT TO BE ADDED INPUT FIELD
        let amountToAddInput = null;
        if(window.id("wallet-moptype") !== null) {
            if(this.props.walletObj.walletSufficientBalance && window.id("wallet-moptype").value === window.walletToCompare) {
                amountToAddInput = <input type="hidden" name="amountToAdd" id="amountToAdd" value={(Number(this.props.getTotalAmount()) - Number(this.props.walletObj.walletBalance)).toFixed(2)} />
            }
        }

        return (
            <div className="col-12 tabBox walletBox" id="wallet">
                <div className="tabbox-inner px-xl-15">
                    <form autoComplete="off" method="post" target="_self" id="wallet-form" action={`${window.basePath}/jsp/pay`} onSubmit={(e) => { return this.props.validateForm(e, 'wallet-moptype', 'walletList'); }}>
                        <div className="card-detail-box" id="wallet-list">
                            <div className="row">
                                {this.props.dataObj.wlMopType.map((list, index) => {                                    
                                    if((this.props.paymentType === "PPWL" && list === "PhonePayWallet") || (this.props.paymentType === "PPL" && list === "PaytmWallet")) {
                                        return <WalletList
                                            item={list}
                                            key={list}
                                            isActive={list === this.props.walletObj.mobikwik_ActiveWl_state ? true : false}
                                            selectWallet={this.props.walletSelectHandler}
                                        />
                                    } else if(this.props.paymentType !== "PPWL" && this.props.paymentType !== "PPL") {
                                        return <WalletList
                                            item={list}
                                            key={list}
                                            isActive={list === this.props.walletObj.mobikwik_ActiveWl_state ? true : false}
                                            selectWallet={this.props.walletSelectHandler}
                                        />
                                    }
                                })}

                                { loggedInDetail }
                            </div>
                        </div>
    
                        <input type="hidden" name="paymentType" value="WL" />
                        <input type="hidden" name="mopType" value={this.props.walletObj.mobikwik_ActiveWl_state !== null ? this.props.walletObj.mobikwik_ActiveWl_state : ''} id="wallet-moptype" />
                        <input type="hidden" name="phoneNo" value={this.props.walletObj.walletLoggedNumber} />
                        <input type="hidden" name="otp" value={this.props.walletObj.walletOtp} />
                        <input type="hidden" name="token" value={this.props.walletObj.walletToken} id="wallet-token" />
                        <input type="hidden" name="paymentFlow" value={this.props.walletObj.paymentFlow} />
                        <input type="hidden" name="amount" className="wallet-amount" value={this.props.getTotalAmount()} />

                        {/* AMOUNT TO ADD INPUT FIELD */}
                        { amountToAddInput }
                        
                        <p id="error-walletList" className="text-danger1 font-size-12 lang" data-key="errorWalletList">Please choose any Wallet type</p>

                        { saveDetailCheckbox }
    
                        { mobikwikSection }

                        <Declaration
                            id="WL-tax-declaration"
                            btnId="submit-btns-wallet"
                            submitHandler={this.props.submitHandler}
                            cancelHandler={this.props.cancelHandler}
                            dataObj={this.props.dataObj}
                            className="mt-15"
                        />
    
                        <button type="submit" className="btn-payment d-none"></button>
                    </form>
                </div>
            </div>
        );
    }
}

export default Wallet;