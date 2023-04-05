import { Component } from 'react';
import PaymentBox from '../PaymentSection/PaymentBox';
import auxillary from '../../hoc/auxiliary';

class ListNav extends Component {
    
    addIcon = (iconCode) => {
        return <span className="nav-icon" dangerouslySetInnerHTML={{__html: iconCode}} />
    }
    
    render() {
        let iconObj = {
            "SC" : '<i class="font-size-20 pg-icon icon-quick-pay tab-span"></i>',
            "CC" : '<i class="font-size-20 pg-icon icon-cards tab-span"></i>',
            "DC" : '<i class="font-size-20 pg-icon icon-cards tab-span"></i>',
            "CD" : '<i class="font-size-20 pg-icon icon-cash tab-span"></i>',
            "CR" : '<i class="font-size-20 pg-icon icon-crypto tab-span"></i>',
            "WL" : '<i class="font-size-20 pg-icon icon-wallet-alt tab-span"></i>',
            "PPL" : '<i class="font-size-20 pg-icon icon-wallet-alt tab-span"></i>',
            "PPWL" : '<i class="font-size-20 pg-icon icon-wallet-alt tab-span"></i>',
            "NB" : '<i class="font-size-20 pg-icon icon-bank tab-span"></i>',
            "UP" : '<span class="font-size-26 pg-icon icon-upi tab-span"><span class="path1"></span><span class="path2"></span><span class="path3"></span></span>',
            "EM" : '<i class="font-size-20 pg-icon icon-emi tab-span"></i>',
            "UPI_QR" : '<span class="font-size-26 pg-icon icon-upi tab-span"><span class="path1"></span><span class="path2"></span><span class="path3"></span></span>',
            "MQR" : '<i class="font-size-16 pg-icon icon-qr-code tab-span"></i>',
            "AP" : '<i class="font-size-20 pg-icon icon-crypto tab-span"></i>'
        }        

        let mobileContent = null;

        if(this.props.width < 768) {
            if(this.props.paymentType === this.props.btnDataType && window.querySelector(".tabLi.active") !== null) {
                mobileContent = <PaymentBox
                    klass={this.props.klass}
                    paymentType={this.props.paymentType}
                    dataObj={this.props.dataObj}
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
                    submitHandler={this.props.submitHandler}
                    cancelHandler={this.props.cancelHandler}
                />
            }
        }

        return (
            <li className="nav-list">
                <button id={this.props.btnId} data-id={this.props.btnDataId} data-type={this.props.btnDataType} onClick={this.props.clickHandler} className={`tabLi d-flex align-items-center`}>
                    {this.addIcon(iconObj[this.props.btnDataType])}
                    <span className="tab-span lang" data-key={this.props.dataKey}>{this.props.btnText}</span>
                </button>
                
                { mobileContent }
            </li>
        );
    }
}

export default auxillary(ListNav);