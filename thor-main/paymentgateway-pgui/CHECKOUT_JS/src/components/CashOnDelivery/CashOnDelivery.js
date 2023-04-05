import React, { useState } from "react";
import PaymentBtn from "../PaymentBtn/PaymentBtn";
import TitleSection from "../TitleSection/TitleSection";

const CashOnDelivery = props => {
    const [checkState, setCheckState] = useState(false);

    const selectHandler = e => {
        e.preventDefault();
        setCheckState(e.target.checked);
    }

    return (
        <React.Fragment>
            <TitleSection title={props.title} navigationHandler={props.navigationHandler} btnDataId="navigation" />
            
            <div className="container custom-container mt-180">
                <div className="tabBox codBox" id="cashOnDelivery">
                    <form autoComplete="off" method="post" target="_self" action={`${Window.baseUrl}/pay`} id="form-CD" onSubmit={(e) => { return props.validateFormHandler(e, 'cod-moptype', 'cash_on_delivery'); }}>
                        <div className="card-detail-box" id="cod-list">
                            <div className="row">
                                <div className="col-6 col-sm-6 col-lg-4 mb-0 bankList">
                                    <label className={`custom-control-label d-flex align-items-center flex-column py-15 ${props.displayError ? 'redLine' : null} ${checkState ? 'active' : ''}`} htmlFor="cod-radio" id="cash_on_delivery">
                                        <input
                                            type="radio"
                                            className="custom-control-input"
                                            id="cod-radio"
                                            value="COD"
                                            name="COD"
                                            onChange={e => { props.labelSelectHandler(e); selectHandler(e); }}
                                        />
                                        <span className="nb-icon d-flex align-items-center justify-content-center"><img src={`${Window.basePath}/img/cod-logo.png`} alt="" className="w-100" /></span>
                                        <span className="nb-text d-block mt-5 mt-lg-10 font-size-12" data-key={Window.codDataKey} id="cod-text">
                                            { props.title }
                                        </span>
                                    </label>
                                </div>											
                            </div>
                        </div>

                        {
                            props.displayError ?
                            <p id="error-cash_on_delivery" className="text-danger font-size-12 mb-0" data-key="errorCOD">Please choose { props.title }</p>
                            : null
                        }
                        

                        <input type="hidden" name="paymentType" value="CD" />
                        <input type="hidden" name="mopType" id="cod-moptype" value={props.selectedMopType} />

                        <input type="hidden" name="encSessionData" value={props.data.encSessionData} />
                        <input type="hidden" name="CHECKOUT_JS_FLAG" value={props.data.checkOutJsFlag} />
                        <input type="hidden" name="payId" value={props.data.PAY_ID} />

                        {/* <input type="hidden" name="amount" className="cod-amount" /> */}

                        {/* <div className="row mt-10">
                            <div className="col-md-6 card_charges d-none">
                                <span className="mr-10"><img src={Window.basePath + '/img/info.png'} alt="" />&nbsp;&nbsp;</span>
                                <span id="CD-tax-declaration"></span>
                            </div>
                        </div> */}

                        <button type="submit" className="btn-payment d-none"></button>

                        <PaymentBtn payBtnText={props.payBtnText} totalAmount={props.totalAmount} submitHandler={props.submitHandler} formId="form-CD" isActivePayBtn={props.isActivePayBtn} />
                    </form>                
                </div>
            </div>
        </React.Fragment>
    );
}

export default CashOnDelivery;