import { useState, useEffect } from "react";
import { addConvenienceFee, multilingual } from "../../js/script";
import Declaration from "../Declaration/Declaration";

export default function CashOnDelivery(props) {
    const [checkState, setCheckState] = useState(false);

    useEffect(() => {
        addConvenienceFee(props.paymentType);
        multilingual();
    }, [])

    const setCashOnDelivery = e => {
        e.preventDefault();
        setCheckState(e.target.checked);

        window.id("cod-moptype").value = e.target.value;

        window.id("error-cash_on_delivery").style.display = "none";
		window.id("cash_on_delivery").classList.remove("redLine");
		window.id("pay-now").classList.remove("btn-disabled");
    };

    return (
        <div className="col-12 tabBox codBox" id="cashOnDelivery">
            <div className="tabbox-inner px-xl-15">
                <form autoComplete="off" method="post" target="_self" action={`${window.basePath}/jsp/pay`} id="form-CD" onSubmit={(e) => { return props.validateForm(e, 'cod-moptype', 'cash_on_delivery'); }}>
                    <div className="card-detail-box" id="cod-list">
                        <div className="row">
                            <div className="col-8 col-sm-6 col-md-4 bankList mb-0">
                                <label className={`custom-control-label bank_list_label d-flex align-items-center flex-column py-15 ${checkState ? 'active' : ''}`} htmlFor="cod-radio" id="cash_on_delivery">
                                    <input
                                        type="radio"
                                        className="custom-control-input"
                                        id="cod-radio"
                                        value="COD"
                                        name="COD"
                                        onChange={setCashOnDelivery} />
                                    <span className="nb-icon rounded bg-white"><img src={window.basePath + '/img/cod-logo.png'} alt="" /></span>
                                    <span className="lang nb-text d-flex mt-10 px-10 text-center" data-key={props.dataKey} id="cod-text">{props.btnText}</span>
                                </label>
                            </div>											
                        </div>
                    </div>

                    <div className="resultDiv">
                        <p id="error-cash_on_delivery" className="text-danger1 font-size-12 lang" data-key="errorCOD">Please choose COD</p>
                    </div>

                    <input type="hidden" name="paymentType" value="CD" />
                    <input type="hidden" name="mopType" id="cod-moptype" />
                    <input type="hidden" name="amount" className="cod-amount" />

                    <button type="submit" className="btn-payment d-none"></button>

                    <Declaration
                        id="CD-tax-declaration"
                        btnId="submit-btns-cashOnDelivery"
                        submitHandler={props.submitHandler}
                        cancelHandler={props.cancelHandler}
                        dataObj={props.dataObj}
                    />
                </form>
            </div>
        </div>
    );
}