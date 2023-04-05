import { useState, useEffect } from "react";
import { addConvenienceFee, multilingual } from "../../js/script";
import Declaration from "../Declaration/Declaration";

export default function SingleMopTabBox(props) {
    const [checkState, setCheckState] = useState(false);

    useEffect(() => {
        addConvenienceFee(props.paymentType);
        multilingual();
    }, [])

    const mopTypeHandler = e => {
        e.preventDefault();

        setCheckState(e.target.checked);

        window.id(`${props.dataKey}-moptype`).value = e.target.value;

        window.id(`error-${props.dataKey}_${props.paymentType}`).style.display = "none";
		window.id(`${props.dataKey}_${props.paymentType}`).classList.remove("redLine");
		window.id("pay-now").classList.remove("btn-disabled");
    }

    return (
        <div className={`col-12 tabBox ${props.dataKey}Box`} id={props.dataKey}>
            <div className="tabbox-inner px-xl-15">
                <form autoComplete="off" method="post" target="_self" action={`${window.basePath}/jsp/pay`} id={`form-${props.paymentType}`} onSubmit={(e) => { return props.validateForm(e, `${props.dataKey}-moptype`, `${props.dataKey}_${props.paymentType}`); }}>
                    <div className="card-detail-box" id={`${props.dataKey}-list`}>
                        <div className="row">
                            <div className="col-8 col-sm-6 col-md-4 bankList mb-0">
                                <label className={`custom-control-label bank_list_label d-flex align-items-center flex-column py-15 ${checkState ? 'active' : ''}`} htmlFor={`${props.dataKey}-radio`} id={`${props.dataKey}_${props.paymentType}`}>
                                    <input
                                        type="radio"
                                        className="custom-control-input"
                                        id={`${props.dataKey}-radio`}
                                        value={props.paymentType}
                                        name={props.dataKey}
                                        onChange={mopTypeHandler} />
                                    <span className="nb-icon d-flex align-items-center justify-content-center rounded bg-white">
                                        <i className={`pg-icon font-size-30 font-size-lg-40 icon-crypto`}></i>
                                    </span>
                                    <span className="lang nb-text d-flex mt-10 px-10 text-center" data-key={props.dataKey} id={`${props.dataKey}-text`}>{props.btnText}</span>
                                </label>
                            </div>											
                        </div>
                    </div>

                    <div className="resultDiv">
                        <p id={`error-${props.dataKey}_${props.paymentType}`} className="text-danger1 font-size-12 lang" data-key={`error${(props.dataKey).charAt(0).toUpperCase() + (props.dataKey).slice(1)}`}>{`Please choose ${props.btnText}`}</p>
                    </div>

                    <input type="hidden" name="paymentType" value={props.paymentType} />
                    <input type="hidden" name="mopType" id={`${props.dataKey}-moptype`} />
                    <input type="hidden" name="amount" className={`${props.dataKey}-amount`} />

                    <button type="submit" className="btn-payment d-none"></button>

                    <Declaration
                        id={`${props.paymentType}-tax-declaration`}
                        btnId={`submit-btns-${props.dataKey}`}
                        submitHandler={props.submitHandler}
                        cancelHandler={props.cancelHandler}
                        dataObj={props.dataObj}
                    />
                </form>
            </div>
        </div>
    );
}