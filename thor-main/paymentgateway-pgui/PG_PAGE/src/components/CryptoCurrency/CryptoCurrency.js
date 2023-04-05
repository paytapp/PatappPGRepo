import { useState, useEffect } from "react";
import { addConvenienceFee, multilingual } from "../../js/script";
import Declaration from "../Declaration/Declaration";

export default function CryptoCurrency(props) {
    const [checkState, setCheckState] = useState(false);

    useEffect(() => {
        addConvenienceFee(props.paymentType);
        multilingual();
    }, [])

    const setCryptoCurrency = e => {
        e.preventDefault();

        setCheckState(e.target.checked);

        window.id("crypto-moptype").value = e.target.value;

        window.id("error-crypto_CR").style.display = "none";
		window.id("crypto_CR").classList.remove("redLine");
		window.id("pay-now").classList.remove("btn-disabled");
    };

    return (
        <div className="col-12 tabBox cryptoBox" id="crypto">
            <div className="tabbox-inner px-xl-15">
                <form autoComplete="off" method="post" target="_self" action={`${window.basePath}/jsp/pay`} id="form-CR" onSubmit={(e) => { return props.validateForm(e, 'crypto-moptype', 'crypto_CR'); }}>
                    <div className="card-detail-box" id="crypto-list">
                        <div className="row">
                            <div className="col-8 col-sm-6 col-md-4 bankList mb-0">
                                <label className={`custom-control-label bank_list_label d-flex align-items-center flex-column py-15 ${checkState ? 'active' : ''}`} htmlFor="crypto-radio" id="crypto_CR">
                                    <input
                                        type="radio"
                                        className="custom-control-input"
                                        id="crypto-radio"
                                        value="CR"
                                        name="crypto"
                                        onChange={setCryptoCurrency} />
                                    <span className="nb-icon d-flex align-items-center justify-content-center rounded bg-white">
                                        <i className="pg-icon font-size-30 font-size-lg-40 icon-crypto"></i>
                                    </span>
                                    <span className="lang nb-text d-flex mt-10 px-10 text-center" data-key={props.dataKey} id="crypto-text">{props.btnText}</span>
                                </label>
                            </div>											
                        </div>
                    </div>

                    <div className="resultDiv">
                        <p id="error-crypto_CR" className="text-danger1 font-size-12 lang" data-key="errorCrypto">Please choose Crypto</p>
                    </div>

                    <input type="hidden" name="paymentType" value="CR" />
                    <input type="hidden" name="mopType" id="crypto-moptype" />
                    <input type="hidden" name="amount" className="crypto-amount" />

                    <button type="submit" className="btn-payment d-none"></button>

                    <Declaration
                        id="CR-tax-declaration"
                        btnId="submit-btns-crypto"
                        submitHandler={props.submitHandler}
                        cancelHandler={props.cancelHandler}
                        dataObj={props.dataObj}
                    />
                </form>
            </div>
        </div>
    );
}