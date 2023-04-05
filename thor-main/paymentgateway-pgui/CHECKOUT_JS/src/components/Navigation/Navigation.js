import React, { PureComponent } from "react";
import Fade from 'react-reveal/Fade';
import TitleSection from "../TitleSection/TitleSection";
import NavigationList from "./NavigationList";

class Navigation extends PureComponent {
    render() {
        const isSavedVpa = this.props.data.vpaTokenAvailable && this.props.data.upi && JSON.parse(this.props.data.vpaToken).length > 0 && this.props.data.vpaToken !== "NA",
            isSavedNb = this.props.data.nbTokenAvailable && this.props.data.netBanking && JSON.parse(this.props.data.nbToken).length > 0 && this.props.data.nbToken !== "NA",
            isSavedWl = this.props.data.wlTokenAvailable && this.props.data.wallet && JSON.parse(this.props.data.wlToken).length > 0 && this.props.data.wlToken !== "NA";
            
        const navList = [
            {
                "paymentType" : "SC",
                "btnDataId" : "quickPay",
                "btnText" : "Quick Pay",
                "isVisible" : isSavedVpa || isSavedNb || isSavedWl,
                "navtype": "default"
            },
            {
                "paymentType" : this.props.data.creditCard ? "CC" : "DC",
                "btnDataId" : "cards",
                "btnText" : "Cards",
                "isVisible" : (this.props.data.creditCard && !this.props.data.debitCard) || (this.props.data.debitCard && !this.props.data.creditCard) || (this.props.data.creditCard && this.props.data.debitCard),
                "navtype": "default"
            },
            {
                "paymentType" : this.props.data.upi ? "UP" : "UPI_QR",
                "btnDataId" : "upi",
                "btnText" : this.props.data.upi ? "UPI" : "UPI QR",
                "isVisible" : (this.props.data.upi && !this.props.data.upiQr) || (this.props.data.upiQr && !this.props.data.upi) || (this.props.data.upi && this.props.data.upiQr),
                "navtype": "default"
            },
            {
                "paymentType" : "NB",
                "btnDataId" : "netBanking",
                "btnText" : "Net Banking",
                "isVisible" : this.props.data.netBanking && this.props.data.nbMopType.length > 0,
                "navtype": "default"
            },
            {
                "paymentType" : "WL",
                "btnDataId" : "wallet",
                "btnText" : "Wallet",
                "isVisible" : this.props.data.wallet && this.props.data.wlMopType.length > 0,
                "navtype": "default"
            },
            {
                "paymentType" : "EM",
                "btnDataId" : "emi",
                "btnText" : "EMI",
                "isVisible" : this.props.data.emiDC || this.props.data.emiCC,
                "navtype": "default"
            },
            {
                "paymentType" : "CD",
                "btnDataId" : "cashOnDelivery",
                "btnText" : this.props.data.codName !== "" && this.props.data.codName !== undefined && this.props.data.codName !== null ? this.props.data.codName : "Cash on Delivery",
                "isVisible" : this.props.data.cod,
                "navtype": "default"
            },
            {
                "paymentType" : "CR",
                "btnDataId" : "crypto",
                "btnText" : "Crypto",
                "isVisible" : this.props.data.crypto,
                "navtype": "default"
            },
            {
                "paymentType" : "UP",
                "btnDataId" : "savedVpa",
                "btnText" : "Saved UPI ID",
                "isVisible" : isSavedVpa,
                "navtype": "saved"
            },
            {
                "paymentType" : "NB",
                "btnDataId" : "savedNetBanking",
                "btnText" : "Saved Banks",
                "isVisible" : isSavedNb,
                "navtype": "saved"
            },
            {
                "paymentType" : "WL",
                "btnDataId" : "savedWallet",
                "btnText" : "Saved Wallet",
                "isVisible" : isSavedWl,
                "navtype": "saved"
            },
            {
                "paymentType" : this.props.data.creditCard ? "CC" : "DC",
                "btnDataId" : "addPaycards",
                "btnText" : "Cards",
                "isVisible" : (this.props.data.creditCard && !this.props.data.debitCard) || (this.props.data.debitCard && !this.props.data.creditCard) || (this.props.data.creditCard && this.props.data.debitCard),
                "navtype": "addMoney"
            },
            {
                "paymentType" : "NB",
                "btnDataId" : "addPayNetBanking",
                "btnText" : "Net Banking",
                "isVisible" : this.props.data.netBanking && this.props.data.nbMopType.length > 0,
                "navtype": "addMoney"
            },
        ];

        const componentsToRender = navList.map((list, index) => {
            if(list.isVisible && this.props.componentType === list.navtype) {
                return <NavigationList
                    key={list.paymentType}
                    btnDataType={list.paymentType}
                    btnDataId={list.btnDataId}
                    btnText={list.btnText}
                    navigationHandler={this.props.navigationHandler}
                    navStateHandler={this.props.navStateHandler}
                    duration={index * 100 + 400}
                />                
            }

            return null;
        });

        // TITLE SECTION
        let titleSection = null;
        if(this.props.componentType === "saved" || this.props.componentType === "addMoney") {
            let $btnDataId = "navigation";
            if(this.props.componentType === "addMoney") {
                $btnDataId = "addAndPay";
            }
            titleSection = <TitleSection
                title={this.props.title}
                navigationHandler={this.props.navigationHandler}
                btnDataId={$btnDataId}
                componentType={this.props.componentType}
            />;
        }
    
        return (
            <React.Fragment>
                { titleSection }

                <Fade bottom casCade>
                    <div className={`container custom-container ${this.props.paymentType === 'SC' || this.props.componentType == 'addMoney' ? 'mt-180' : 'mt-135'}`}>
                        <ul className="Navigation list-unstyled mb-0">
                            {componentsToRender}
                        </ul>
                    </div>
                </Fade>
            </React.Fragment>                
        );
    }
}

export default Navigation;