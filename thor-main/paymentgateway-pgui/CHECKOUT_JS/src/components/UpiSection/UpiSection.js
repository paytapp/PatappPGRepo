import React, { PureComponent } from "react";
import PaymentBtn from "../PaymentBtn/PaymentBtn";
import TitleSection from "../TitleSection/TitleSection";
import UpiBox from "./UpiBox";
import UpiQrBox from "./UpiQrBox";

class UpiSection extends PureComponent {
    state = {
        activeComponent: this.props.data.upi ? "UP" : this.props.activeComponentHandler("UPI_QR")
    }

    render() {
        const components = [
            {
                "label": "UP",
                "isActive": this.props.data.upi,
                "component": UpiBox
            },
            {
                "label": "UPI_QR",
                "isActive": this.props.data.upiQr,
                "component": UpiQrBox
            }
        ];
        
        const componentsToRender = components.map((Component, index) => {
            if(Component.isActive) {
                let SpecificComponent = Component.component;
                return <SpecificComponent
                    key={index}
                    isActive={Component.label === this.state.activeComponent ? true : false}
                    upiIconHandler={this.props.upiIconHandler}
                    upiToggleListHandler={this.props.upiToggleListHandler}
                    loaderHandler={this.props.loaderHandler}
                    navigationHandler={this.props.navigationHandler}
                    btnDataId="upi"
                    title={this.props.title}
                    checkOutJsFlag={this.props.data.checkOutJsFlag}
                    vpaFlag={this.props.data.save_vpa}
                />
            }

            return null;
        });

        return (
            <React.Fragment>
                <TitleSection title={this.props.title} navigationHandler={this.props.navigationHandler} btnDataId="navigation" />

                <div className="container custom-container mt-170 mb-60">
                    <div className="tabBox upiBox" id="upi">
                        {componentsToRender}
                    </div>
                </div>

                <div className="px-15">
                    { this.props.isVisiblePayBtn ? <PaymentBtn payBtnText={this.props.payBtnText} submitHandler={this.props.submitHandler} customClass="ml-0 mr-0" totalAmount={this.props.totalAmount} /> : null }
                </div>

                <form name="upiResponseForm" id="upiResponseForm" action="" method="post" target="_self"></form>
                <form name="upiRedirectForm" id="upiRedirectForm" action={`${Window.baseUrl}/upiRedirect`} method="post" target="_self">
                    <input type="hidden" name="encSessionData" id="upi-encSessionData" />
                    <input type="hidden" name="CHECKOUT_JS_FLAG" value={this.props.data.checkOutJsFlag} />
                    <input type="hidden" name="payId" value={this.props.data.PAY_ID} />
                </form>

                <form method="POST" name="upiResponseSubmitForm" id="upiResponseSubmitForm" target="_self" action={`${Window.baseUrl}/upiResponse`}>
                    <input type="hidden" name="PG_REF_NUM" id="resPgRefNum" value="" />
                    <input type="hidden" name="RETURN_URL" id="resReturnUrl" value="" />
                    <input type="hidden" name="CHECKOUT_JS_FLAG" value={this.props.data.checkOutJsFlag} />
                </form>
            </React.Fragment>
        );
    }
}

export default UpiSection;