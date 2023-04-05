const SubmitButton = props => (
    <button type="button" id="pay-now" onClick={props.submitHandler} className="btn btn-primary w-100 border-radius-5 font-weight-bold font-size-16 py-10 mt-15 btn-disabled">
        <span className="d-flex align-items-center justify-content-center">
            <span className="lang mr-10 line-height-16" id="payBtnKey" data-key="payBtnText">Pay</span>
            <span className="payBtnAmount d-inline-flex align-items-center">
                <i className=" pg-icon icon-inr mr-5 font-size-15"></i>
                <span className="value-block line-height-16"></span>
            </span>
        </span>
    </button>
);

export default SubmitButton;