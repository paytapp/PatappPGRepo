import Card from "../Card/Card";
import CashOnDelivery from "../CashOnDelivery/CashOnDelivery";
import EMI from "../EMI/EMI";
import NetBanking from "../NetBanking/NetBanking";
import QuickPay from "../QuickPay/QuickPay";
import Upi from '../Upi/Upi';
import Wallet from "../Wallet/Wallet";
import SingleMopTabBox from "../SingleMopTabBox/SingleMopTabBox";
import { UpiContainer } from "../Upi/UpiContainer";
import MqrBox from "../Upi/MqrBox";

const PaymentBox = props => {
    if(props.paymentType === "SC") {
        return <QuickPay
            dataObj={props.dataObj}
            paymentType={props.paymentType}
            loaderHandler={props.loaderHandler}
            deleteHandler={props.deleteHandler}
            submitHandler={props.submitHandler}
            cancelHandler={props.cancelHandler}
        />;
    } else if (props.paymentType === "CC" || props.paymentType === "DC") {
        return (
            <div className="col-12 tabBox debitWithPinBox" id="debitWithPin">
                <div className="tabbox-inner px-xl-15">
                    <Card
                        dataObj={props.dataObj}
                        paymentType={props.paymentType}
                        submitHandler={props.submitHandler}
                        cancelHandler={props.cancelHandler}
                        formParameters={{
                            tenure: '',
                            issuerName: '',
                            rateOfInterest: '',
                            perMonthEmiAmount: '',
                            totalEmiAmount: '',
                            emiInterest: ''
                        }}
                    />
                </div>
            </div>
        )
    } else if(props.paymentType === "UP" || props.paymentType === "UPI_QR") {
        return <Upi
            dataObj={props.dataObj}
            paymentType={props.paymentType}
            submitHandler={props.submitHandler}
            cancelHandler={props.cancelHandler}
        />
    } else if(props.paymentType === "MQR") {
        return <UpiContainer className="mqrBox" id="mqr">
            <MqrBox
                klass={props.klass}
                isActive={true}
                paymentType={props.paymentType}
                submitHandler={props.submitHandler}
                cancelHandler={props.cancelHandler}
                dataObj={props.dataObj}
            />
        </UpiContainer>
    } else if(props.paymentType === "NB") {
        return <NetBanking
            dataObj={props.dataObj}
            paymentType={props.paymentType}
            validateForm={props.validateForm}
            submitHandler={props.submitHandler}
            cancelHandler={props.cancelHandler}
        />
    } else if(props.paymentType === "WL" || props.paymentType === "PPL" || props.paymentType === "PPWL") {
        return <Wallet
            paymentType={props.paymentType}
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
        />
    } else if(props.paymentType === "EM") {
        return <EMI
            dataObj={props.dataObj}
            paymentType={props.paymentType}
            submitHandler={props.submitHandler}
            cancelHandler={props.cancelHandler}                
        />
    } else if(props.paymentType === "CD") {
        let codDataKey = "cashondelivery",
            codBtnText = "Cash on Delivery";

        if(props.dataObj !== null) {
            if(props.dataObj.codName !== '' && props.dataObj.codName !== undefined && props.dataObj.codName !== null) {
                codDataKey = props.dataObj.codName.replace(/\s/g,'').toLowerCase();
                codBtnText = props.dataObj.codName;
            }
        }

        return <CashOnDelivery
            dataObj={props.dataObj}
            paymentType={props.paymentType}
            validateForm={props.validateForm}
            dataKey={codDataKey} 
            btnText={codBtnText}
            submitHandler={props.submitHandler}
            cancelHandler={props.cancelHandler}
        />
    } else if(props.paymentType === "CR") {
        return <SingleMopTabBox
            dataObj={props.dataObj}
            paymentType={props.paymentType}
            validateForm={props.validateForm}
            dataKey="crypto"
            btnText="Crypto"
            submitHandler={props.submitHandler}
            cancelHandler={props.cancelHandler}
        />
    } else if(props.paymentType === 'AP') {
        return <SingleMopTabBox
            dataObj={props.dataObj}
            paymentType={props.paymentType}
            validateForm={props.validateForm}
            dataKey="aamarPay"
            btnText="aamarPay"
            submitHandler={props.submitHandler}
            cancelHandler={props.cancelHandler}
        />
    } else {
        return null;
    }
}

export default PaymentBox;