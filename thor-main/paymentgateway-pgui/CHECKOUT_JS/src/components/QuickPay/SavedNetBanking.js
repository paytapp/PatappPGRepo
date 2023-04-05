import React, { useState } from "react";
import TitleSection from "../TitleSection/TitleSection";
import SavedNbList from "./SavedNbList";
import PaymentBtn from "../PaymentBtn/PaymentBtn";

const SavedNetBanking = props => {
    let nbListToRender = null;
    if(props.data.nbToken !== "NA") {
        nbListToRender = JSON.parse(props.data.nbToken).map((list, index) => {
            return <SavedNbList
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
                    <form autoComplete="off" name="netBanking-form" method="post" target="_self" action={`${Window.baseUrl}/pay`} id="netBankingform" onSubmit={(e) => { return props.validateFormHandler(e, 'nbMopType', 'bankList'); }}>
                        <div className={`card-detail-box`} id="quick-pay">
                            <div className="row">
                                { nbListToRender }
                            </div>
                        </div>
                        
                        {
                            props.displayError ?
                                <p id="error-wallet-list" className="text-danger font-size-12 mb-0" data-key="errorWalletList">Please choose any wallet type</p>
                            : null
                        }

                        <input type="hidden" id="nbPaymentType" name="paymentType" value="NB" />
                        <input type="hidden" id="nbMopType" name="mopType" value={props.selectedMopType} />

                        <input type="hidden" name="encSessionData" value={props.data.encSessionData} />
                        <input type="hidden" name="CHECKOUT_JS_FLAG" value={props.data.checkOutJsFlag} />
                        <input type="hidden" name="payId" value={props.data.PAY_ID} />

                        <button type="submit" className="btn-payment d-none"></button>

                        <PaymentBtn payBtnText={props.payBtnText} totalAmount={props.totalAmount} submitHandler={props.submitHandler} isActivePayBtn={props.isActivePayBtn} formId="netBankingform" />
                    </form>
                </div>
            </div>
        </React.Fragment>
    );
}

export default SavedNetBanking;