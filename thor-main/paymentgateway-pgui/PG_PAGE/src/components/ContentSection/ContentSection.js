import React, { Component } from "react";
import Header from "../Header/Header";
import PaymentSection from "../PaymentSection/PaymentSection";

class ContentSection extends Component {
    render() {
        return (
            <div className="col-12 col-md-8 col-lg-6 bg-white border-radius-br-md-20 border-radius-lg-none mh-xl-525">
                <Header dataObj={this.props.dataObj} />

                <PaymentSection
                    klass={this.props.klass}
                    submitHandler={this.props.submitHandler}
                    cancelHandler={this.props.cancelHandler}
                    dataObj={this.props.dataObj}
                    paymentType={this.props.paymentType}
                    walletSelectHandler={this.props.walletSelectHandler}
                    verifyUserHandler={this.props.verifyUserHandler}
                    mobikwikOtpHandler={this.props.mobikwikOtpHandler}
                    resetErrorHandler={this.props.resetErrorHandler}
                    startTimerHandler={this.props.startTimerHandler}
                    loadWalletBalanceHandler={this.props.loadWalletBalanceHandler}
                    editWalletNumberHandler={this.props.editWalletNumberHandler}
                    walletObj={this.props.walletObj}
                    validateForm={this.props.validateForm}
                    loaderHandler={this.props.loaderHandler}
                    getTotalAmount={this.props.getTotalAmount}
                    updateMobikwikState={this.props.updateMobikwikState}
                    deleteHandler={this.props.deleteHandler}
                />
            </div>
        );
    }

}

export default ContentSection;