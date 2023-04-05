import { Component } from "react";
import Loader from "../Loader/Loader";
import ErrorPage from "../ErrorPage/ErrorPage";
import "./StaticPgQrResponse.css";
class StaticPgQrResponse extends Component {
    state = {
        data: null,
        showLoader: false,
        orderId: null,
        isCustomerInfo: false
    }

    componentDidMount() {
        try {
            var responseObj = window.id("sessionObj").value;
            responseObj = JSON.parse(responseObj);
            
            if(responseObj.payId !== undefined) {
                this.generateOrderId(responseObj.businessName);
                this.setState({data: responseObj});
            } else {
                this.setState({data: "error"});
            }
        } catch(e) {
            this.setState({data: "error"});
            console.error(e);
        }
    }

    generateOrderId = merchantName => {
        this.setState({ orderId: merchantName.slice(0,2).toUpperCase() + String(new Date().getTime())});
    }

    roundOf = e => {
        let _value = e.target.value;

        if(_value.indexOf(".") != -1) {
            let _num = Number(_value);
            e.target.value = _num.toFixed(2);
        }
    }

    isAlphabetOnly = e => {
        if(e.target.value === " ") {
            e.target.value = "";
        } else {
            e.target.value = e.target.value.replace(/[^A-Za-z ]/g, '');
        }
    }

    isNumberWithDots = e => {
        e.target.value = e.target.value.replace(/[^0-9.]/g, '');
    }

    isNumberOnly = e => {
        e.target.value = e.target.value.replace(/[^0-9]/g, '');
    }

    isSpecialCharacterOnly = e => {
        e.target.value = e.target.value.replace(/[^A-Za-z0-9!#$%&'*/=?^_+-`{|}~]/g, '');
    }

    validateMobile = (e, elementId) => {
        const $input = document.getElementById(elementId);

        if($input !== null) {
            if($input.value !== "") {
                if($input.value.length !== 10) {
                    this.displayError(e, $input);

                    return false;
                } else {
                    this.removeError(e, $input);

                    return true;
                }
            } else {
                this.removeError(e, $input);

                return true;
            }
        }

        return true;
    }

    isValidEmail = that => {
        const regex = /^[_A-Za-z0-9-]+(\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*(\.[_A-Za-z0-9-]+)$/,
            value = that.value;
    
        if(value.match(regex)) {
            return true;
        }
    
        return false;
    }

    validateEmail = (e, elementId) => {
        const $input = document.getElementById(elementId);

        if($input !== null) {
            if($input.value !== "") {
                if(this.isValidEmail($input)) {
                    this.removeError(e, $input);

                    return true;
                } else {
                    this.displayError(e, $input);

                    return false;
                }
            } else {
                this.removeError(e, $input);

                return true;
            }
        }

        return true;
    }

    displayError = (e, $input) => {
        $input.closest("div").classList.add("hasError");
    }

    removeError = (e, $input) => {
        $input.closest("div").classList.remove("hasError");
    }

    isValidAmount = e => {
        if(e.target.value !== "") {
            e.target.value = Number(e.target.value).toFixed(2);
        }
    }

    validateAmount = (e, elementId) => {
        let that = document.getElementById(elementId),
            _val = that.value;

        if(_val !== "" && _val !== " ") {
            if(_val.length == 1) {
                if(_val.indexOf("0") != -1) {
                    that.value = _val.slice(0, _val.length - 1);
                }
            }
         
            let regex = /[.]/g,
                _getPeriod = _val.match(regex);
    
            if(_getPeriod !== null) {
                if(_getPeriod.length > 1) {
                    that.value = _val.slice(0, _val.length - 1);
                }
    
                let _getString = _val.slice(_val.indexOf("."));
                if(_getString.length > 3) {
                    that.value = _val.slice(0, _val.length - 1);
                }
            }

            this.removeError(e, that);

            return true;
        } else {
            this.displayError(e, that);

            return false;
        }
    }

    validateField = e => {
        let result = this.validateAmount(e, "amount");

        if(this.state.isCustomerInfo) {
            result = result && this.validateEmail(e, "customerEmail");
            result = result && this.validateMobile(e, "customerMobile");
        }

        return result;
    }

    payButtonHandler = e => {
        e.preventDefault();

        if(this.validateField(e)) {
            this.setState({ showLoader: true });

            document.getElementById("amountInput").value = document.getElementById("amount").value;

            if(this.state.isCustomerInfo) {
                document.getElementById("customer-name").value = document.getElementById("customerName").value;
                document.getElementById("customer-email").value = document.getElementById("customerEmail").value;
                document.getElementById("customer-mobile").value = document.getElementById("customerMobile").value;
            }
        
            document.getElementById("redirectToPaymentAction").submit();
        }
    }

    customerInfoHandler = e => {
        this.setState({isCustomerInfo: !this.state.isCustomerInfo});
    }

    render() {
        let loader = null;
        if(this.state.showLoader) {
            loader = <Loader processing={false} approvalNotification={false} />
        }

        // MAIN CONTENT
        let mainContent = <Loader processing={false} approvalNotification={false} />;
        if(this.state.data !== null) {
            if(this.state.data === "error") {
                mainContent = <ErrorPage />
            } else {
                // MERCHANT LOGO
                let merchantLogo = <img src={`${window.basePath}/img/logo.png`} alt="Payment Gateway" />;
                if(this.state.data.merchantLogo !== "" && this.state.data.merchantLogo !== null && this.state.data.merchantLogo !== undefined) {
                    merchantLogo = <img src={this.state.data.merchantLogo} alt="Payment Gateway" height="70" />;
                }

                // CUSTOMER INFO
                let customerInfo = null;
                if(this.state.isCustomerInfo) {
                    customerInfo = (
                        <>
                            <div className="p-0 position-relative">
                                <label className="placeHolderText w-100 text-grey-light font-size-12 line-height-15 d-inline-flex mb-5 lang field-title" data-key="orderId">Name</label>
                                <input
                                    autoComplete="new-password"
                                    className="pField inputField amount_field form-control font-size-14 font-size-sm-16 text-grey-dark font-weight-medium p-8 border outline-none"
                                    type="text"
                                    name="customerName"
                                    id="customerName"
                                    onInput={ this.isAlphabetOnly }
                                    onCopy={e => { e.preventDefault(); }}
                                    onPaste={e => { e.preventDefault(); }}
                                    onDrop={e => { e.preventDefault(); }}
                                />
                            </div>

                            <div className="p-0 position-relative">
                                <label className="placeHolderText w-100 text-grey-light font-size-12 line-height-15 d-inline-flex mb-5 lang field-title" data-key="orderId">Email</label>
                                <input
                                    autoComplete="new-password"
                                    className="pField inputField amount_field form-control font-size-14 font-size-sm-16 text-grey-dark font-weight-medium p-8 border outline-none"
                                    type="text"
                                    name="customerEmail"
                                    id="customerEmail"
                                    onInput={this.isSpecialCharacterOnly}
                                    onBlur={e => this.validateEmail(e, "customerEmail")}
                                    onCopy={e => { e.preventDefault(); }}
                                    onPaste={e => { e.preventDefault(); }}
                                    onDrop={e => { e.preventDefault(); }}
                                />
                                <p className="error">Please enter valid Email ID.</p>
                            </div>

                            <div className="p-0 position-relative">
                                <label className="placeHolderText w-100 text-grey-light font-size-12 line-height-15 d-inline-flex mb-5 lang field-title" data-key="orderId">Mobile</label>
                                <input
                                    autoComplete="new-password"
                                    className="pField inputField amount_field form-control font-size-14 font-size-sm-16 text-grey-dark font-weight-medium p-8 border outline-none"
                                    type="text"
                                    name="customerMobile"
                                    id="customerMobile"
                                    maxLength={10}
                                    inputMode="numeric"
                                    onInput={ this.isNumberOnly }
                                    onBlur={e => this.validateMobile(e, "customerMobile")}
                                    onCopy={e => { e.preventDefault(); }}
                                    onPaste={e => { e.preventDefault(); }}
                                    onDrop={e => { e.preventDefault(); }}
                                />
                                <p className="error">Please enter valid mobile number.</p>
                            </div>
                        </>
                    );
                }

                mainContent = (
                    <>
                        <section className="response_container">
                            <div className="response_wrapper d-flex justify-content-between flex-column p-20 pb-0">
                                <div className="response_header px-15">
                                    {
                                        this.state.data.payId !== this.state.data.PAYTENSE_PAY_ID ?
                                        <div className="response_logo justify-content-center pt-15">
                                            { merchantLogo }
                                        </div> : null
                                    }

                                    <div className="response_text text-center mt-20">
                                        <div className="response_merchant font-size-24">{ this.state.data.businessName }</div>
                                        <div className="response_orderId font-size-14 mt-10">
                                            Order ID : <span className="font-weight-bold" id="ORDER_ID">{ this.state.orderId }</span>
                                        </div>
                                    </div>
                                </div>

                                <div className="response_body px-15">
                                    <div className="p-0 position-relative">
                                        <label className="placeHolderText w-100 text-grey-light font-size-12 line-height-15 d-inline-flex mb-5 lang field-title" data-key="orderId">Amount <span className="text-red ml-5">*</span></label>
                                        <input
                                            autoComplete="new-password"
                                            className="pField inputField amount_field form-control font-size-14 font-size-sm-16 text-grey-dark font-weight-medium p-8 border outline-none"
                                            type="text"
                                            name="amount"
                                            id="amount"
                                            onBlur={e => {this.roundOf(e); this.isValidAmount(e); } }
                                            onInput={e => { this.isNumberWithDots(e); this.validateAmount(e, "amount"); this.removeError(e, document.getElementById("amount")); }}
                                            onCopy={e => { e.preventDefault(); }}
                                            maxLength="12"
                                            inputMode="numeric"
                                            onPaste={e => { e.preventDefault(); }}
                                            onDrop={e => { e.preventDefault(); }}
                                        />
                                        <p className="error">Please enter amount</p>
                                    </div>

                                    { customerInfo }

                                    <div className="w-100 text-right">
                                        <button className="btn-additional-info" onClick={ this.customerInfoHandler }>{this.state.isCustomerInfo ? "-" : "+"} Additional Info</button>
                                    </div>
                                </div>

                                <div className="response_footer mt-20">
                                    <div className={`response_button text-center ${this.state.data.payId === this.state.data.PAYTENSE_PAY_ID ? "mb-20" : null}`}>
                                        <button id="payButton" className="response_pay" onClick={ this.payButtonHandler }>Pay</button>
                                    </div>

                                    {
                                        this.state.data.payId !== this.state.data.PAYTENSE_PAY_ID ?
                                        <div id="footer">
                                            <div className="mt-20 col-lg-12 bg-grey-dark-primary d-flex align-items-center pt-15 pb-5 border-radius-bl-md-0 justify-content-center" id="footer-poweredby">
                                                <span className="text-grey-light-primary mtn-30 font-size-12">Powered By</span>
                                                <span className="font-family-logo ml-5 mr-5 font-size-18 text-white">Payment Gateway</span>
                                                <span className="text-white">&copy;</span>
                                            </div>
                                        </div> : null
                                    }
                                </div>
                            </div>
                        </section>

                        <form action="redirectToPaymentAction" id="redirectToPaymentAction" method="POST">
                            <input type="hidden" value={this.state.data.payId} name="payId" id="payId" />
                            <input type="hidden" value={this.state.orderId} name="orderId" id="orderId" />
                            <input type="hidden" value="" name="amount" id="amountInput" />
                            <input type="hidden" value="" name="name" id="customer-name" />
                            <input type="hidden" value="" name="email" id="customer-email" />
                            <input type="hidden" value="" name="mobile" id="customer-mobile" />
                        </form>
                    </>
                );
            }
        }

        return (
            <>
                { mainContent }
            </>
        );
    }
}

export default StaticPgQrResponse;