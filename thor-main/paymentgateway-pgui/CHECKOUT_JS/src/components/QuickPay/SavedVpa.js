import React from "react";
import TitleSection from "../TitleSection/TitleSection";
import SavedVpaList from "./SavedVpaList";
import PaymentBtn from "../PaymentBtn/PaymentBtn";

const SavedVpa = props => {
    let vpaListToRender = null;

    if(props.data.vpaToken !== "NA") {
        vpaListToRender = JSON.parse(props.data.vpaToken).map((list, index) => {
            return <SavedVpaList
                item={list}
                key={list.key}
                duration={index}
                isActive={list.key === props.activeQuickPayId}
                quickPayHandler={props.quickPayHandler}
                deleteHandler={props.deleteHandler}
            />
        });
    } else {
        props.tokenHandler();
    }

    return (
        <React.Fragment>
            <TitleSection title={props.title} navigationHandler={props.navigationHandler} btnDataId="quickPay" paymentType="SC" />

            <div className="container custom-container mt-180 mb-60">
                <div className="tabBox">
                    <div className={`card-detail-box`} id="quick-pay">
                        <div className="row">
                            { vpaListToRender }
                        </div>
                    </div>

                    <input type="hidden" name="paymentType" value="UP" />
                    <input type="hidden" name="mopType" id="up-moptype" value={props.selectedMopType} />
                    
                    {/* {
                        this.props.displayError ?
                            <p id="error-wallet-list" className="text-danger font-size-12 mb-0" data-key="errorWalletList">Please choose any wallet type</p>
                        : null
                    } */}

                    <form name="upiResponseForm" id="upiResponseForm" action="" method="post" target="_self"></form>
                    <form name="upiRedirectForm" id="upiRedirectForm" action={`${Window.baseUrl}/upiRedirect`} method="post" target="_self">
                        <input type="hidden" name="encSessionData" id="upi-encSessionData" />
                        <input type="hidden" name="CHECKOUT_JS_FLAG" value={props.data.checkOutJsFlag} />
                        <input type="hidden" name="payId" value={props.data.PAY_ID} />
                    </form>

                    <form method="POST" name="upiResponseSubmitForm" id="upiResponseSubmitForm" target="_self" action={`${Window.baseUrl}/upiResponse`}>
                        <input type="hidden" name="PG_REF_NUM" id="resPgRefNum" value="" />
                        <input type="hidden" name="RETURN_URL" id="resReturnUrl" value="" />
                        <input type="hidden" name="CHECKOUT_JS_FLAG" value={props.data.checkOutJsFlag} />
                    </form>

                    <PaymentBtn payBtnText={props.payBtnText} totalAmount={props.totalAmount} submitHandler={props.submitHandler} isActivePayBtn={props.isActivePayBtn} />
                </div>
            </div>
        </React.Fragment>
    );
}

export default SavedVpa;