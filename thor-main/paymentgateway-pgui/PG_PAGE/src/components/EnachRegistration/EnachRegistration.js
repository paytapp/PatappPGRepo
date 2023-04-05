import React, { Component } from 'react';
import Loader from '../Loader/Loader';
import ErrorPage from '../ErrorPage/ErrorPage';
import MandateError from '../MandateError/MandateError';
import { inputHandler, isNumberKey } from '../../js/script';
import HiddenInput from './HiddenInput';
import BootstrapSelect from 'react-bootstrap-select-dropdown';
import 'bootstrap/dist/css/bootstrap.min.css';
import "./EnachRegistration.css";

class EnachRegistration extends Component {
    state = {
        data: null,
        showLoader: false,
        message: "",
        activeRegMode: "",
        selectedBank: "",
        selectedAccountType: "",
        registrationMode: [
            {
                "labelKey": "netBanking",
                "value": "Net Banking"
            },
            {
                "labelKey": "cards",
                "value": "Debit Card"
            }
        ],
        accountType: [
            {
                "labelKey": "Saving",
                "value": "Saving"
            }
        ]
    }

    componentDidMount() {
        try {
            var responseObj = window.id("sessionObj").value;
            responseObj = JSON.parse(responseObj);
            
            if(responseObj.RESPONSE !== undefined) {
                this.setState({data: responseObj});
            } else {
                this.setState({data: "error"});
            }
        } catch(e) {
            this.setState({data: "error"});
            console.error(e);
        }
    }

    registrationModeHandler = e => {
        this.setState({
            activeRegMode: e.selectedKey[0],
            selectedBank: ""
        });
    }

    accountTypeHandler = e => {
        this.setState({selectedAccountType: e.selectedKey[0]});
    }

    bankListHandler = e => {
        this.setState({selectedBank: e.selectedKey[0]});
    }

    onlyAlphaSpace = e => {
        let that = e.target;
        if(that.value == " ") {
            that.value = "";
        } else {
            that.value = that.value.replace(/[^a-zA-Z ]/g, '');
        }
    }

    onlyAlphaNumeric = e => {
        let that = e.target;
        that.value = that.value.replace(/[^a-zA-Z0-9]/g, '');
    }

    upperCaseHandler = e => {
        let _val = e.target.value;
        e.target.value = _val.toUpperCase();
    }

    emptyCheckHandler = e => {
        if(e.target.value !== "") {
            e.target.closest(".input-wrapper").classList.remove("has-error");
        }
    }

    emptySelectBoxHandler = e => {
        if(e.selectedKey.length > 0) {
            const parentWrapper = e.el.offsetParent.offsetParent;            
            parentWrapper.classList.remove("has-error");
        }
    }

    eNachSubmitHandler = (e) => {
        e.preventDefault();

        let payload = {},
            inputBox = document.querySelectorAll("[data-var]"),
            count = 0;

        for(let i = 0; i < inputBox.length; i++) {
            let that = inputBox[i];
            
            if(that.value == "" && that.hasAttribute("required")) {
                let inputWrapper = that.closest(".input-wrapper"),
                    inputLabel = inputWrapper.querySelector("label"),
                    errorField = inputWrapper.querySelector(".error_field");

                if(inputLabel !== null && errorField !== null) {
                    let labelText = inputLabel.innerText;
                    errorField.innerText = `${labelText} should not blank`;
                    inputWrapper.classList.add("has-error");
                }
            } else {
                payload[that.getAttribute("data-var")] = that.value;

                count++;
            }            
        }
        

        if(inputBox.length == count) {
            this.setState({showLoader : true});

            fetch(`${window.basePath}/jsp/getEnachFormToken`, {
                method : "POST",
                body : JSON.stringify(payload),
                headers : {
                    'Content-Type' : 'application/json',
                    'Accept' : 'application/json'
                }      
            })
            .then((response) => response.json())
            .then((data) => {
                document.getElementById("paynimoData").value = JSON.stringify(data);

                this.setState({showLoader : false});
            })
            .catch((error) => {
                console.error(error);
            });
        }
    }

    createFormInput = obj => {
        const inputField = document.createElement("input");
        inputField.type = "hidden";
        inputField.name = obj.name;
        inputField.value = obj.value;

        return inputField;
    }

    eNachCancelHandler = e => {
        e.preventDefault();

        let $message = e.target.getAttribute("data-text");

        this.setState({
            message: $message,
            showLoader: true
        });

        if($message === "cancel") {
            let inputBox = document.querySelectorAll("[data-var]");
            
            if(inputBox.length !== undefined) {
                inputBox.forEach(element => {
                    let _new =  element.closest(".input-wrapper").classList,
                        _newVal =  _new.toString().indexOf("d-none");

                    if(_newVal == -1 && element.value != "") {
                        const result = this.createFormInput({name: element.getAttribute('data-var'), value: element.value});
                        
                        document.querySelector("#cancelForm").appendChild(result);
                    }
                });
            }
        }

        document.getElementById("cancelForm").submit();
    }

    errorSwitchHandler = response => {
        switch(response) {
            case "INVALID REQUEST":
                return <MandateError
                    responseMsg="Invalid Request"
                    dataText="hashFail"
                    eNachCancelHandler={this.eNachCancelHandler}
                />;

            case "Duplicate order Id":
                return <MandateError
                    responseMsg="Duplicate Request"
                    dataText="duplicateOrderId"
                    eNachCancelHandler={this.eNachCancelHandler}
                />;

            case "Invalid End Date":
                return <MandateError
                    responseMsg="Invalid End Date"
                    dataText="invalidEndDate"
                    eNachCancelHandler={this.eNachCancelHandler}
                />;

            case "Invalid Start Date":
                return <MandateError
                    responseMsg="Invalid Start Date"
                    dataText="invalidStartDate"
                    eNachCancelHandler={this.eNachCancelHandler}
                />;

            case "Invalid Amount":
                return <MandateError
                    responseMsg="Invalid Amount"
                    dataText="invalidAmount"
                    eNachCancelHandler={this.eNachCancelHandler}
                />;

            case "Invalid Monthly Amount":
                return <MandateError
                    responseMsg="Invalid Monthly Amount"
                    dataText="invalidMonthlyAmount"
                    eNachCancelHandler={this.eNachCancelHandler}
                />;

            case "Invalid Frequency":
                return <MandateError
                    responseMsg="Invalid Frequency"
                    dataText="invalidFrequency"
                    eNachCancelHandler={this.eNachCancelHandler}
                />;

            case "Invalid Tenure":
                return <MandateError
                    responseMsg="Invalid Tenure"
                    dataText="invalidTenure"
                    eNachCancelHandler={this.eNachCancelHandler}
                />;

            case "Invalid Merchant ID":
                return <MandateError
                    responseMsg="Invalid Merchant ID"
                    dataText="invalidMerchantID"
                    eNachCancelHandler={this.eNachCancelHandler}
                />;

            case "Invalid Customer Mobile":
                return <MandateError
                    responseMsg="Invalid Customer Mobile"
                    dataText="invalidCustomerMobile"
                    eNachCancelHandler={this.eNachCancelHandler}
                />;

            case "Invalid Customer Email":
                return <MandateError
                    responseMsg="Invalid Customer Email"
                    dataText="invalidCustomerEmail"
                    eNachCancelHandler={this.eNachCancelHandler}
                />;

            case "Invalid Request ID":
                return <MandateError
                    responseMsg="Invalid Request ID"
                    dataText="invalidRequestID"
                    eNachCancelHandler={this.eNachCancelHandler}
                />;
            
            default:
                return null;
        }
    }

    cancelFieldSwitchHandler = response => {
        const responseArr = [
                "INVALID REQUEST",
                "Duplicate order Id",
                "Invalid End Date",
                "Invalid Start Date",
                "Invalid Amount",
                "Invalid Monthly Amount",
                "Invalid Frequency",
                "Invalid Tenure",
                "Invalid Merchant ID",
                "Invalid Customer Mobile",
                "Invalid Customer Email",
                "Invalid Request ID"
            ],
            url = this.state.data.MERCHANT_RETURN_URL;

        let result = null;
        responseArr.forEach(element => {
            if(response == element) {
                result = <HiddenInput inputValue={url} />
            } else {
                result = null;
            }
        });

        return result;
    }

    render() {
        let loader = null;
        if(this.state.showLoader) {
            loader = <Loader processing={false} approvalNotification={false} />
        }

        let mainContent = <Loader processing={false} approvalNotification={false} />;
        if(this.state.data !== null) {
            if(this.state.data === "error") {
                mainContent = <ErrorPage />
            } else {
                let popupBox = null;
                if(this.state.data.RESPONSE !== "success") {
                    popupBox = (
                        <div className="lpay_popup">
                            <div className="lpay_popup-inner">
                                <div className="lpay_popup-innerbox" data-status="error">	
                                    <div className="lpay_popup-innerbox-error lpay-center">
                                        <div className="lpay_popup_icon">
                                            <img src={`${window.basePath}/img/wrong-tick.png`} alt="" />
                                        </div>

                                        { this.errorSwitchHandler(this.state.data.RESPONSE) }
                                    </div>
                                </div>
                            </div>
                        </div>
                    );
                }

                let bankNameFields = null;
                if(this.state.activeRegMode == "netBanking") {
                    bankNameFields = (
                        <div className="col-md-3 mb-20 input-wrapper" data-id="netBanking">
                            <div className="lpay_select_group">
                                <label htmlFor="bankCode">Bank Name</label>
                                <BootstrapSelect
                                    options={JSON.parse(this.state.data.nbList)}
                                    id="bankCode"
                                    className="w-100"
                                    placeholder="Select Bank"
                                    onChange={e => { this.bankListHandler(e); this.emptySelectBoxHandler(e); }}
                                />
                                <input type="hidden" name="bankCode" data-var="bankCode" value={this.state.selectedBank} readOnly={true} required="required" />
                                <div className="error_field">Please select bank name</div>
                            </div>			
                        </div>
                    );
                } else if(this.state.activeRegMode == "cards") {
                    bankNameFields = (
                        <div className="col-md-3 mb-20 input-wrapper" data-id="cards">
                            <div className="lpay_select_group">
                                <label htmlFor="bankCode">Bank Name</label>
                                <BootstrapSelect
                                    options={JSON.parse(this.state.data.dcBankList)}
                                    id="bankCode"
                                    className="w-100"
                                    placeholder="Select Bank"
                                    onChange={e => { this.bankListHandler(e); this.emptySelectBoxHandler(e); }}
                                />
                                <input type="hidden" name="bankCode" data-var="bankCode" value={this.state.selectedBank} readOnly={true} required="required" />
                                <div className="error_field">Please select bank name</div>
                            </div>
                        </div>
                    );
                }

                mainContent = (
                    <>
                        { popupBox }

                        <input type="hidden" value={this.state.data.RESPONSE} id="repon" readOnly={true} />

                        <div className="eNach-div min-vh-100">
                            <section className="eNach lapy_section white-bg box-shadow-box mt-60 p-15">
                                <div className="row d-flex mb-20 align-items-center">
                                    <div className="col-md-3">
                                        {
                                            this.state.data.LOGO !== null && this.state.data.LOGO !== '' ?
                                                <img src={this.state.data.LOGO} id="logo" alt="" />
                                            : <img src={`${window.basePath}/img/paymentGateway.png`} id="logo" alt="Payment Gateway" />
                                        }
                                    </div>
                                    
                                    <div className="col-md-6 text-center">
                                        <div className="disclaimer-text">
                                            Please do not refresh the page or press back button
                                        </div>                                        
                                    </div>
                                    
                                    <div className="col-md-3 text-right">
                                        <span className="font-size-18">{ this.state.data.MERCHANT_NAME }</span>
                                    </div>                                    
                                </div>
                                
                                <div className="row">
                                    <div className="col-md-12 ribbon-heading mb-20">
                                        <div className="heading_with_icon text-center">
                                            <h1 className="heading_text">eNACH Registration</h1>
                                        </div>
                                    </div>

                                    <div className="col-12">
                                        <div className="row">
                                            <div className="col-md-3 mb-20 input-wrapper">
                                                <div className="lpay_select_group">
                                                    <label htmlFor="paymentMode">Registration Mode</label>
                                                    <BootstrapSelect
                                                        options={this.state.registrationMode}
                                                        id="paymentMode"
                                                        placeholder="Select Mode"
                                                        className="w-100"
                                                        onChange={(e) => { this.registrationModeHandler(e); this.emptySelectBoxHandler(e); }}
                                                    />
                                                    <input type="hidden" name="paymentMode" data-var="paymentMode" value={this.state.activeRegMode} readOnly={true} required="required" />
                                                    <div className="error_field">Please select registration mode</div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                    <div className="col-12">
                                        <div className="row">
                                            { bankNameFields }
                                        </div>
                                    </div>

                                    <div className="col-md-3 mb-20 input-wrapper">
                                        <div className="lpay_select_group">
                                            <label htmlFor="accountType">Account Type</label>
                                            <BootstrapSelect
                                                options={this.state.accountType}
                                                id="accountType"
                                                placeholder="Select Account Type"
                                                className="w-100"
                                                onChange={e => { this.accountTypeHandler(e); this.emptySelectBoxHandler(e); }}
                                            />
                                            <input type="hidden" name="accountType" data-var="accountType" value={this.state.selectedAccountType} readOnly={true} required="required" />
                                            <div className="error_field">Please select account type</div>
                                        </div>
                                    </div>

                                    <div className="col-md-3 mb-20 input-wrapper">
                                        <div className="lpay_input_group">
                                            <label htmlFor="accountNumber">Account Number</label>
                                            <input
                                                type="text"
                                                onPaste={inputHandler}
                                                maxLength="20"
                                                onInput={e => { isNumberKey(e); this.emptyCheckHandler(e); }}
                                                data-var="accountNumber"
                                                id="accountNumber"
                                                className="lpay_input"
                                                required="required"
                                            />
                                            <div className="error_field">Account number should not blank</div>
                                        </div>                                        
                                    </div>

                                    <div className="col-md-3 mb-20 input-wrapper">
                                        <div className="lpay_input_group">
                                            <label htmlFor="accountHolderName">Account Holder Name</label>
                                            <input
                                                type="text"
                                                onInput={e => {this.onlyAlphaSpace(e); this.emptyCheckHandler(e); this.upperCaseHandler(e); }}
                                                data-var="accountHolderName"
                                                id="accountHolderName"
                                                className="lpay_input"
                                                required="required"
                                            />
                                            <div className="error_field">Account holder should not blank</div>
                                        </div>
                                    </div>

                                    <div className="col-md-3 mb-20 input-wrapper">
                                        <div className="lpay_input_group">
                                            <label htmlFor="ifsc">IFSC Code</label>
                                            <input
                                                type="text"
                                                onPaste={inputHandler}
                                                maxLength="11"
                                                onInput={e => { this.onlyAlphaNumeric(e); this.emptyCheckHandler(e); this.upperCaseHandler(e); }}
                                                data-var="ifscCode"
                                                id="ifsc"
                                                className="lpay_input"
                                                required="required"
                                            />
                                            <div className="error_field">IFSC should not blank</div>
                                        </div>                                        
                                    </div>

                                    <div className="col-md-3 mb-20 input-wrapper">
                                        <div className="lpay_input_group">
                                            <label htmlFor="consumerMobileNo">Mobile Number</label>
                                            <input
                                                type="text"
                                                data-var="consumerMobileNo"
                                                id="consumerMobileNo"
                                                name="consumerMobileNo"
                                                className="lpay_input"
                                                maxLength="10"
                                                onKeyPress={isNumberKey}
                                                autoComplete="off"
                                                value={this.state.data.CUSTOMER_MOBILE}
                                                readOnly={true}
                                                required="required"
                                            />
                                            <div className="error_field">Mobile number should not blank</div>
                                        </div>
                                    </div>

                                    <div className="col-md-3 mb-20 input-wrapper">
                                        <div className="lpay_input_group">
                                            <label htmlFor="consumerEmailId">Email Id</label>
                                            <input
                                                type="text"
                                                data-var="consumerEmailId"
                                                id="consumerEmailId"
                                                name="consumerEmailId"
                                                className="lpay_input"
                                                autoComplete="off"
                                                value={this.state.data.CUSTOMER_EMAIL}
                                                readOnly={true}
                                                required="required"
                                            />
                                            <div className="error_field">Email ID should not blank</div>
                                        </div>
                                    </div>

                                    <div className="col-md-3 mb-20 input-wrapper">
                                        <div className="lpay_input_group">
                                            <label htmlFor="amount">Amount</label>
                                            <input
                                                type="text"
                                                data-var="amount"
                                                id="amount"
                                                name="amount"
                                                className="lpay_input"
                                                onKeyPress={isNumberKey}
                                                autoComplete="off"
                                                value={this.state.data.AMOUNT}
                                                readOnly={true}
                                                required="required"
                                            />
                                            <div className="error_field">Amount should not blank</div>
                                        </div>
                                    </div>

                                    <div className="col-md-3 mb-20 input-wrapper">
                                        <div className="lpay_input_group">
                                            <label htmlFor="maxAmount">{this.state.data.FREQUENCY === 'As And When Presented' ? 'ADHO -' : this.state.data.FREQUENCY} Debit Amount</label>
                                            <input
                                                type="text"
                                                data-var="maxAmount"
                                                id="maxAmount"
                                                name="maxAmount"
                                                className="lpay_input"
                                                onKeyPress={isNumberKey}
                                                autoComplete="off"
                                                value={this.state.data.MONTHLY_AMOUNT}
                                                readOnly={true}
                                                required="required"
                                            />
                                            <div className="error_field">Max Amount should not blank</div>
                                        </div>
                                    </div>

                                    <div className="col-md-3 mb-20 input-wrapper">
                                        <div className="lpay_input_group">
                                            <label htmlFor="totalAmount">Total Amount</label>
                                            <input
                                                type="text"
                                                data-var="totalAmount"
                                                id="totalAmount"
                                                name="totalAmount"
                                                className="lpay_input"
                                                onKeyPress={isNumberKey}
                                                autoComplete="off"
                                                value={this.state.data.TOTAL_AMOUNT}
                                                readOnly={true}
                                                required="required"
                                            />
                                            <div className="error_field">Total Amount should not blank</div>
                                        </div>
                                    </div>

                                    <div className="col-md-3 mb-20 hide-input input-wrapper">
                                        <div className="lpay_input_group">
                                            <label htmlFor="frequency">Frequency</label>
                                            <input
                                                type="text"
                                                data-var="frequency"
                                                id="frequency"
                                                name="frequency"
                                                className="lpay_input"
                                                autoComplete="off"
                                                value={this.state.data.FREQUENCY}
                                                readOnly={true}
                                                required="required"
                                            />
                                        </div>
                                    </div>

                                    <div className="col-md-3 mb-20 input-wrapper">
                                        <div className="lpay_input_group">
                                            <label htmlFor="tenure">Tenure</label>
                                            <input
                                                type="text"
                                                data-var="tenure"
                                                id="tenure"
                                                name="tenure"
                                                className="lpay_input"
                                                autoComplete="off"
                                                value={this.state.data.TENURE}
                                                readOnly={true}
                                                required="required"
                                            />
                                        </div>
                                    </div>

                                    <div className="col-md-3 mb-20 input-wrapper">
                                        <div className="lpay_input_group">
                                            <label htmlFor="debitStartDate">Debit Start Date</label>
                                            <input
                                                type="text"
                                                data-var="debitStartDate"
                                                id="debitStartDate"
                                                name="debitStartDate"
                                                className="lpay_input"
                                                autoComplete="off"
                                                value={this.state.data.DEBIT_START_DATE}
                                                readOnly={true}
                                                required="required"
                                            />
                                        </div>
                                    </div>

                                    <div className="col-md-3 mb-20 input-wrapper">
                                        <div className="lpay_input_group">
                                            <label htmlFor="debitEndDate">Debit End Date</label>
                                            <input
                                                type="text"
                                                data-var="debitEndDate"
                                                id="debitEndDate"
                                                name="debitEndDate"
                                                className="lpay_input"
                                                autoComplete="off"
                                                value={this.state.data.DEBIT_END_DATE}
                                                readOnly={true}
                                                required="required"
                                            />
                                        </div>
                                    </div>                        
                            
                                    <div className="col-md-3 mb-20 hide-input input-wrapper">
                                        <div className="lpay_input_group">
                                            <input
                                                type="text"
                                                data-var="payId"
                                                name="payId"
                                                value={this.state.data.PAY_ID}
                                                readOnly={true}
                                            />
                                        </div>
                                    </div>

                                    <div className="col-md-3 mb-20 hide-input input-wrapper">
                                        <div className="lpay_input_group">
                                            <input
                                                type="text"
                                                data-var="registrationDate"
                                                value={this.state.data.REGISTRATION_DATE}
                                                readOnly={true}
                                            />
                                        </div>
                                    </div>

                                    {
                                        this.state.data.SUB_MERCHANT_ID !== null && this.state.data.SUB_MERCHANT_ID !== '' ?
                                            <div className="col-md-3 mb-20 hide-input input-wrapper">
                                                <div className="lpay_input_group">
                                                    <input
                                                        type="text"
                                                        data-var="subMerchantPayId"
                                                        name="subMerchantPayId"
                                                        value={this.state.data.SUB_MERCHANT_ID}
                                                        readOnly={true}
                                                    />
                                                </div>
                                            </div>
                                        : null
                                    }
                            
                                    <div className="col-md-3 mb-20 hide-input input-wrapper">
                                        <div className="lpay_input_group">					
                                            <input type="hidden" data-var="mopType" name="mopType" id="mopType" />
                                        </div>					
                                    </div>

                                    {
                                        this.state.data.LOGO !== null && this.state.data.LOGO !== '' ?
                                            <div className="col-md-3 mb-20 hide-input input-wrapper">
                                                <div className="lpay_input_group">
                                                    <input
                                                        type="text"
                                                        data-var="merchantLogo"
                                                        name="merchantLogo"
                                                        value={this.state.data.LOGO}
                                                        readOnly={true}
                                                    />
                                                </div>						
                                            </div>
                                        : null
                                    }

                                    <div className="col-md-3 mb-20 hide-input input-wrapper">
                                        <div className="lpay_input_group">
                                            <input                                                
                                                type="text"
                                                id="returnUrl"
                                                data-var="returnUrl"
                                                name="returnUrl"
                                                value={this.state.data.RETURN_URL}
                                                readOnly={true}                                                    
                                            />
                                        </div>					
                                    </div>
                            
                                    <div className="col-md-3 mb-20 hide-input input-wrapper">
                                        <div className="lpay_input_group">
                                            <input                                                
                                                type="text"
                                                id="merchantReturnUrl"
                                                data-var="merchantReturnUrl"
                                                name="merchantReturnUrl"
                                                value={this.state.data.MERCHANT_RETURN_URL}
                                                readOnly={true}                                                    
                                            />
                                        </div>					
                                    </div>

                                    <div className="col-md-3 mb-20 hide-input input-wrapper">
                                        <div className="lpay_input_group">
                                            <input
                                                type="text"
                                                id="consumerId"
                                                data-var="consumerId"
                                                value={this.state.data.CONSUMER_ID}
                                                readOnly={true}                                                    
                                            />
                                        </div>
                                    </div>                                    
                                    
                                    <div className="col-md-12 text-center">
                                        <button className="lpay_button lpay_button-md lpay_button-primary" id="btnSubmit" onClick={this.eNachSubmitHandler}>Submit</button>
                                        <button className="lpay_button lpay_button-md lpay_button-secondary cnclButton" data-text="cancel" onClick={this.eNachCancelHandler}>Cancel</button>
                                    </div>

                                    <div className="col-md-12 text-right mt-15">
                                        <div className="footer_logo text-right">
                                            <span>Powered By</span>
                                            <img src={`${window.basePath}/img/nach-logo.png`} alt="/" />
                                            <img src={`${window.basePath}/img/npci-logo.png`} alt="/" />
                                        </div>                                        
                                    </div>
                                </div>
                            </section>
                        </div>

                        <form action="iciciEnachResponse" method="POST" id="cancelForm">
                            <input type="hidden" name="message" id="msg" value={this.state.message} />

                            { this.cancelFieldSwitchHandler(this.state.data.RESPONSE) }
                        </form>

                        {/* <form id="upiAutoPay" action="upiAutoPayResponse" method="POST">
                            <input type="hidden" name="orderId" id="orderId" value={this.state.data.PAY_ID} readOnly={true} />
                        </form> */}

                        { loader }
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

export default EnachRegistration;