import ListNav from '../ListNav/ListNav';
import { addConvenienceFee } from '../../js/script';
import { useEffect, useState } from 'react';
import Logo from '../Logo/Logo';

const FindComponent = function({ link: paymentType, index: id, type, props }) {
    switch (paymentType) {
        case "PPL":
            if(props.dataObj.wallet && props.dataObj.wlMopType.includes("PaytmWallet")) {
                return (<ListNav
                    key={id}
                    btnId="payTmLi"
                    btnDataId="wallet"
                    btnDataType="PPL"
                    dataKey="paytm"
                    btnText="Paytm"
                    paymentType={type}
                    dataObj={props.dataObj}
                    validateForm={props.validateForm}
                    submitHandler={props.submitHandler}
                    cancelHandler={props.cancelHandler}
                    clickHandler={props.tabHandler.bind(this, id)} />);
            } else {
                return null;
            }
        break;

        case "PPWL":
            if(props.dataObj.wallet && props.dataObj.wlMopType.includes("PhonePayWallet")) {
                return (<ListNav
                    key={id}
                    btnId="phonePeLi"
                    btnDataId="wallet"
                    btnDataType="PPWL"
                    dataKey="phonepe"
                    btnText="PhonePe"
                    paymentType={type}
                    dataObj={props.dataObj}
                    validateForm={props.validateForm}
                    submitHandler={props.submitHandler}
                    cancelHandler={props.cancelHandler}
                    clickHandler={props.tabHandler.bind(this, id)} />);
            } else {
                return null;
            }
        break;

        case "SC":
			if((props.dataObj.vpaTokenAvailable && props.dataObj.vpaToken !== "NA") || (props.dataObj.nbTokenAvailable && props.dataObj.nbToken !== "NA") || (props.dataObj.wlTokenAvailable && props.dataObj.wlToken !== "NA" && JSON.parse(props.dataObj.wlToken).length > 0)) {
                return (<ListNav
                    key={id}
                    btnId="showCardLi"
                    btnDataId="saveDetails"
                    btnDataType="SC"
                    dataKey="quickPay"
                    btnText="Quick Pay"
                    paymentType={type}
                    dataObj={props.dataObj}
                    deleteHandler={props.deleteHandler}
                    submitHandler={props.submitHandler}
                    cancelHandler={props.cancelHandler}
                    clickHandler={props.tabHandler.bind(this, id)} />);
			} else {
                return null;
            }
		break;

        case "CARD":
            if (props.dataObj.creditCard && !props.dataObj.debitCard) {
                return (<ListNav
                    key={id}
                    btnId="creditLi"
                    btnDataId="debitWithPin"
                    btnDataType="CC"
                    dataKey="creditCard"
                    btnText="Credit Card"
                    paymentType={type}
                    dataObj={props.dataObj}
                    submitHandler={props.submitHandler}
                    cancelHandler={props.cancelHandler}
                    clickHandler={props.tabHandler.bind(this, id)} />);
            } else if(props.dataObj.debitCard && !props.dataObj.creditCard) {
                return (<ListNav
                    key={id}
                    btnId="debitLi"
                    btnDataId="debitWithPin"
                    btnDataType="DC"
                    dataKey="debitCard"
                    btnText="Debit Card"
                    paymentType={type}
                    dataObj={props.dataObj}
                    submitHandler={props.submitHandler}
                    cancelHandler={props.cancelHandler}
                    clickHandler={props.tabHandler.bind(this, id)} />);
            } else if(props.dataObj.creditCard && props.dataObj.debitCard) {
                return (<ListNav
                    key={id}
                    btnId="creditLi"
                    btnDataId="debitWithPin"
                    btnDataType="CC"
                    dataKey="card"
                    btnText="Cards"
                    paymentType={type}
                    dataObj={props.dataObj}
                    submitHandler={props.submitHandler}
                    cancelHandler={props.cancelHandler}
                    clickHandler={props.tabHandler.bind(this, id)} />);
            } else {
                return null;
            }
        break;

        case "UPI_MERGED":
            if(props.dataObj.upi && !props.dataObj.upiQr) {
                return (<ListNav
                    key={id}
                    btnId="upiLi"
                    btnDataId="upi"
                    btnDataType="UP"
                    dataKey="upi"
                    btnText="UPI"
                    paymentType={type}
                    dataObj={props.dataObj}
                    submitHandler={props.submitHandler}
                    cancelHandler={props.cancelHandler}
                    clickHandler={props.tabHandler.bind(this, id)} />);
            } else if(props.dataObj.upiQr && !props.dataObj.upi) {
                return (<ListNav
                    key={id}
                    btnId="upiLi"
                    btnDataId="upi"
                    btnDataType="UPI_QR"
                    dataKey="upi"
                    btnText="UPI"
                    paymentType={type}
                    dataObj={props.dataObj}
                    submitHandler={props.submitHandler}
                    cancelHandler={props.cancelHandler}
                    clickHandler={props.tabHandler.bind(this, id)} />);
            } else if(props.dataObj.upi && props.dataObj.upiQr) {
                return (<ListNav
                    key={id}
                    btnId="upiLi"
                    btnDataId="upi"
                    btnDataType="UP"
                    dataKey="upi"
                    btnText="UPI"
                    paymentType={type}
                    dataObj={props.dataObj}
                    submitHandler={props.submitHandler}
                    cancelHandler={props.cancelHandler}
                    clickHandler={props.tabHandler.bind(this, id)} />);
            } else {
                return null;
            }
        break;

        case "MQR":
            if(props.dataObj.mqr) {
                return (<ListNav
                    klass={props.klass}
                    key={id}
                    btnId="mqrLi"
                    btnDataId="mqr"
                    btnDataType="MQR"
                    dataKey="mqr"
                    btnText="Scan & Pay"
                    paymentType={type}
                    dataObj={props.dataObj}
                    submitHandler={props.submitHandler}
                    cancelHandler={props.cancelHandler}
                    clickHandler={props.tabHandler.bind(this, id)} />);
            } else {
                return null;
            }
        break;

        case "NB":
            if (props.dataObj.netBanking && props.dataObj.nbMopType.length > 0) {
                // createBankList(props.dataObj.nbMopType);
                return (<ListNav
                    key={id}
                    btnId="nbLi"
                    btnDataId="netBanking"
                    btnDataType="NB"
                    dataKey="netBanking"
                    btnText="Net Banking"
                    paymentType={type}
                    dataObj={props.dataObj}
                    validateForm={props.validateForm}
                    submitHandler={props.submitHandler}
                    cancelHandler={props.cancelHandler}
                    clickHandler={props.tabHandler.bind(this, id)} />);
            } else {
                return null;
            }
        break;

        case "WL":
            if (props.dataObj.wallet && props.dataObj.wlMopType.length > 0) {
                // _createWalletList(props.dataObj.wlMopType);
                return (<ListNav
                    key={id}
                    btnId="wlLi"
                    btnDataId="wallet"
                    btnDataType="WL"
                    dataKey="wallet"
                    btnText="Wallet"
                    paymentType={type}
                    dataObj={props.dataObj}
                    walletSelectHandler={props.walletSelectHandler}
                    verifyUserHandler={props.verifyUserHandler}
                    mobikwikOtpHandler={props.mobikwikOtpHandler}
                    resetErrorHandler={props.resetErrorHandler}
                    startTimerHandler={props.startTimerHandler}
                    loadWalletBalanceHandler={props.loadWalletBalanceHandler}
                    editWalletNumberHandler={props.editWalletNumberHandler}
                    walletObj={props.walletObj}
                    validateForm={props.validateForm}
                    loaderHandler={props.loaderHandler}
                    getTotalAmount={props.getTotalAmount}
                    updateMobikwikState={props.updateMobikwikState}
                    submitHandler={props.submitHandler}
                    cancelHandler={props.cancelHandler}
                    clickHandler={props.tabHandler.bind(this, id)} />);
            } else {
                return null;
            }
        break;

        case "EM":
            if(props.dataObj.emiDC || props.dataObj.emiCC) {
                return (<ListNav
                    key={id}
                    btnId="emiLi"
                    btnDataId="emi"
                    btnDataType="EM"
                    dataKey="emi"
                    btnText="EMI"
                    paymentType={type}
                    dataObj={props.dataObj}
                    submitHandler={props.submitHandler}
                    cancelHandler={props.cancelHandler}
                    clickHandler={props.tabHandler.bind(this, id)} />);
            } else {
                return null;
            }
        break;

        case "CD":
            if(props.dataObj.cod) {
                return (<ListNav
                    key={id}
                    btnId="codLi"
                    btnDataId="cashOnDelivery"
                    btnDataType="CD"
                    dataKey={props.dataObj !== null ? props.dataObj.codName !== "" && props.dataObj.codName !== undefined && props.dataObj.codName !== null ? props.dataObj.codName.replace(/\s/g,'').toLowerCase() : "cashondelivery" : "cashondelivery"} 
                    btnText={props.dataObj !== null ? props.dataObj.codName !== "" && props.dataObj.codName !== undefined && props.dataObj.codName !== null ? props.dataObj.codName : "Cash on Delivery" : "Cash on Delivery"}
                    paymentType={type}
                    dataObj={props.dataObj}
                    validateForm={props.validateForm}
                    submitHandler={props.submitHandler}
                    cancelHandler={props.cancelHandler}
                    clickHandler={props.tabHandler.bind(this, id)} />);
            } else {
                return null;
            }
        break;

        case "CR":
            if(props.dataObj.crypto) {
                return (<ListNav
                    key={id}
                    btnId="cryptoLi"
                    btnDataId="crypto"
                    btnDataType="CR"
                    dataKey="crypto"
                    btnText="Crypto"
                    paymentType={type}
                    dataObj={props.dataObj}
                    validateForm={props.validateForm}
                    submitHandler={props.submitHandler}
                    cancelHandler={props.cancelHandler}
                    clickHandler={props.tabHandler.bind(this, id)}
                />);
            } else {
                return null;
            }
        break;

        case "AP":
            if(props.dataObj.aamarPay) {
                return (<ListNav
                    key={id}
                    btnId="apLi"
                    btnDataId="aamarPay"
                    btnDataType="AP"
                    dataKey="aamarPay"
                    btnText="aamarPay"
                    paymentType={type}
                    dataObj={props.dataObj}
                    validateForm={props.validateForm}
                    submitHandler={props.submitHandler}
                    cancelHandler={props.cancelHandler}
                    clickHandler={props.tabHandler.bind(this, id)}
                />);
            } else {
                return null;
            }
        break;
    
        default:
            return null;
    }
};

function Navigation(props) {
    const [paymentType, setpaymentType] = useState(null);

    useEffect(() => {
        if(props.paymentType == null) {
            let _list = window.querySelector(".tabLi");

            if(_list !== null) {
                if(window.innerWidth >= 768) {
                    _list.classList.add("active");
                    addConvenienceFee(_list.getAttribute("data-type"));
                    setpaymentType(_list.getAttribute("data-type"));
                } else {
                    addConvenienceFee();
                    setpaymentType();
                }    
            }
        } else {
            addConvenienceFee(props.paymentType);
            setpaymentType(props.paymentType);
        }
    }, [props.paymentType]);

    return (
        <div className="col-12 col-md-4 col-lg-3 bg-white border-radius-tl-md-20 border-right-md-grey-lighter mh-xl-525" id="navigation-column">
            <div className="logo h-105 align-items-center justify-content-center" id="logo">
                <Logo dataObj={props.dataObj} />
            </div>
            <ul className="list-unstyled horizontal-nav-content mb-0 mb-md-15" id="navigation">
                { props.links !== null ? props.links.map((link, index) => <FindComponent key={index} index={index} link={link} type={paymentType} props={props} />) : null }
            </ul>
        </div>
    );
}

export default Navigation;