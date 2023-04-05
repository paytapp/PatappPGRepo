import React, { Component } from "react";
import auxillary from "../../hoc/auxiliary";

import Navigation from '../Navigation/Navigation';
import ContentSection from '../ContentSection/ContentSection';
import OrderSummary from '../OrderSummary/OrderSummary';
import Footer from '../Footer/Footer';
import NavigationMobile from '../NavigationMobile/NavigationMobile';
import HeaderMobile from '../HeaderMobile/HeaderMobile';
import Loader from '../Loader/Loader';

class PageData extends Component {
    componentDidMount() {
        import('../../js/script').then(script => script.wrapperPosition());
    }

    render() {
        let loader = null;
    
        if(this.props.loader.showLoader) {
            loader = <Loader
                processing={this.props.loader.defaultText}
                approvalNotification={this.props.loader.approvalNotification}
            />
        }
        
        return (
            <React.Fragment>
                <div className={`px-15 ${this.props.walletObj.mobikwik_isActive_state ? 'mobikwik--active' : ''}`} id="container-outer-wrap">
                    <div className="container custom-container">
                        <div 
                            className="row bg-light-sky-dark border-radius-md-20 border-radius-tl-20 border-radius-tr-20 box-shadow-primary border-primary" 
                            // style={{backgroundImage: `url(${window.basePath}/img/bg-5.png)`}} 
                            id="custom-container-row">
                            <HeaderMobile dataObj={this.props.data} />
    
                            <div className="col-12">
                                <div className="row">
                                    <Navigation
                                        klass={this.props.klass}
                                        dataObj={this.props.data}
                                        paymentType={this.props.paymentType}
                                        walletSelectHandler={this.props.walletSelectHandler}
                                        verifyUserHandler={this.props.verifyUserHandler}
                                        mobikwikOtpHandler={this.props.mobikwikOtpHandler}
                                        resetErrorHandler={this.props.resetErrorHandler}
                                        startTimerHandler={this.props.startTimerHandler}
                                        loadWalletBalanceHandler={this.props.loadWalletBalanceHandler}
                                        editWalletNumberHandler={this.props.editWalletNumberHandler}                                        
                                        walletObj={this.props.walletObj}
                                        tabHandler={(e, id) => {this.props.tabHandler(e, id); this.props.getPaymentType(e, id)}}
                                        submitHandler={this.props.submitHandler}
                                        cancelHandler={this.props.cancelHandler}
                                        validateForm={this.props.validateForm}
                                        loaderHandler={this.props.loaderHandler}
                                        getTotalAmount={this.props.getTotalAmount}
                                        updateMobikwikState={this.props.updateMobikwikState}
                                        links={this.props.links}
                                        deleteHandler={this.props.deleteHandler}
                                    />
    
                                    {this.props.width >= 768 && (
                                        <ContentSection
                                            klass={this.props.klass}
                                            submitHandler={this.props.submitHandler}
                                            cancelHandler={this.props.cancelHandler}
                                            dataObj={this.props.data}
                                            paymentType={this.props.paymentType}
                                            walletObj={this.props.walletObj}
                                            walletSelectHandler={this.props.walletSelectHandler}
                                            verifyUserHandler={this.props.verifyUserHandler}
                                            mobikwikOtpHandler={this.props.mobikwikOtpHandler}
                                            resetErrorHandler={this.props.resetErrorHandler}
                                            startTimerHandler={this.props.startTimerHandler}
                                            loadWalletBalanceHandler={this.props.loadWalletBalanceHandler}
                                            editWalletNumberHandler={this.props.editWalletNumberHandler}
                                            validateForm={this.props.validateForm}
                                            loaderHandler={this.props.loaderHandler}
                                            deleteHandler={this.props.deleteHandler}
                                            getTotalAmount={this.props.getTotalAmount}
                                            updateMobikwikState={this.props.updateMobikwikState}
                                        />
                                    )}
    
                                    {this.props.width >= 992 && (
                                        <OrderSummary
                                            dataObj={this.props.data}
                                            submitHandler={this.props.submitHandler}
                                            cancelHandler={this.props.cancelHandler}
                                        />
                                    )}                                
                                </div>
                                <Footer />
                            </div>
                        </div>
                    </div>
                </div>
    
                { this.props.width < 992 && (
                    <NavigationMobile
                        data={this.props.data}
                        submitHandler={this.props.submitHandler}
                        cancelHandler={this.props.cancelHandler}
                    />
                ) }

                <form name="upiResponseForm" id="upiResponseForm" action="" method="post" target="_self"></form>
                <form name="upiRedirectForm" id="upiRedirectForm" action={`${window.basePath}/jsp/upiRedirect`} method="post" target="_self"></form>

                <form name="upiResponseSubmitForm" id="upiResponseSubmitForm" target="_self" method="POST" action={`${window.basePath}/jsp/upiResponse`}>
                    <input type="hidden" name="PG_REF_NUM" id="resPgRefNum" value="" />
                    <input type="hidden" name="RETURN_URL" id="resReturnUrl" value="" />
                </form>

                <form method="POST" action={`${window.basePath}/jsp/txncancel`} id="cancel-form">
                    <input type="hidden" name="payId" value={this.props.data.PAY_ID} />
                </form>

                <form method="POST" action="" id="response-form"></form>
    
                { loader }
            </React.Fragment>
        );
    }
}

export default auxillary(PageData);