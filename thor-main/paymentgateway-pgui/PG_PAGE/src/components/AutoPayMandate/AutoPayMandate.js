import React, { Component } from 'react';
import Loader from '../Loader/Loader';
import ErrorPage from '../ErrorPage/ErrorPage';
import "./AutoPayMandate.css";

class AutoPayMandate extends Component {
    state = {
        data: null,
        showLoader: false,
        message: ""
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

    emptyCheckHandler = (e) => {
        e.preventDefault();

        if(e.target.value !== "") {
            e.target.closest(".input-wrapper").classList.remove("has-error");
        }
    }

    autoPaySubmitHandler = (e) => {
        e.preventDefault();

        let payload = {},
            inputBox = document.querySelectorAll("[data-var]"),
            count = 0;

        for(let i = 0; i < inputBox.length; i++) {
            let that = inputBox[i];
            
            if(that.value === "" && that.hasAttribute("required")) {
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
        

        if(inputBox.length === count) {
            this.setState({showLoader : true});

            // document.getElementById("enach-form").submit();

            fetch(`${window.basePath}/jsp/getUpiAutoPayToken`, {
                method : "POST",
                body : JSON.stringify(payload),
                headers : {
                    'Content-Type' : 'application/json',
                    'Accept' : 'application/json'
                }      
            })
            .then((response) => response.json())
            .then((data) => {
                document.getElementById("upiAutoPay").submit();

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

    autoPayCancelHandler = e => {
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

                    if(_newVal === -1 && element.value !== "") {
                        const result = this.createFormInput({name: element.getAttribute('data-var'), value: element.value});
                        
                        document.querySelector("#cancelForm").appendChild(result);
                    }
                });
            }
        }

        document.getElementById("cancelForm").submit();
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
                mainContent = (
                    <>
                        {
                            this.state.data.RESPONSE !== "success" ?
                                <div className="lpay_popup">
                                    <div className="lpay_popup-inner">
                                        <div className="lpay_popup-innerbox" data-status="error">	
                                            <div className="lpay_popup-innerbox-error lpay-center">
                                                <div className="lpay_popup_icon">
                                                    <img src={`${window.basePath}/img/wrong-tick.png`} alt="" />
                                                </div>
                                                
                                                {
                                                    this.state.data.RESPONSE === 'INVALID REQUEST' ?
                                                        <>
                                                            <div className="lpay_popup-content">
                                                                <h3 className="responseMsg">Invalid Request</h3>
                                                            </div>
                                                    
                                                            <div className="lpay_popup-button">
                                                                <button className="lpay_button lpay_button-md lpay_button-primary cnclButton" data-text="hashFail" id="confirmButton" onClick={this.autoPayCancelHandler}>OK</button>
                                                            </div>
                                                        </>
                                                    : null
                                                }
                                                
                                                {
                                                    this.state.data.RESPONSE === 'Duplicate order Id' ?
                                                        <>
                                                            <div className="lpay_popup-content">
                                                                <h3 className="responseMsg">Duplicate Request</h3>
                                                            </div>

                                                            <div className="lpay_popup-button">
                                                                <button className="lpay_button lpay_button-md lpay_button-primary cnclButton" data-text="duplicateOrderId" id="confirmButton" onClick={this.autoPayCancelHandler}>OK</button>
                                                            </div>
                                                        </>
                                                    : null
                                                }   
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            : null
                        }

                        <input type="hidden" value={this.state.data.RESPONSE} id="repon" readOnly={true} />

                        {/* <div className="loader-container w-100 vh-100 lpay-center">
                            <div className="loaderImage">
                                <img src={`${window.basePath}/img/loader.gif`} alt="Loader" />
                            </div>
                        </div> */}


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
                                        <div className="heading_with_icon text-center ">							
                                            <h1 className="heading_text">UPI AutoPay Mandate Registration </h1>
                                        </div>						
                                    </div>
                            
                                    <div className="col-md-3 mb-20 input-wrapper">
                                        <div className="lpay_input_group">
                                            <label htmlFor="">Payer VPA <span className="text-red">*</span></label>
                                            <input
                                                type="text"
                                                data-var="payerVPA"
                                                id="payerVpa"
                                                name="payerVPA"
                                                className="lpay_input"
                                                autoComplete="off"
                                                required="required"
                                                onInput={(e) => {this.emptyCheckHandler(e); }}
                                            />
                                            <div className="error_field">Payer VPA should not blank</div>
                                        </div>                                        
                                    </div>

                                    <div className="col-md-3 mb-20 input-wrapper">
                                        <div className="lpay_input_group">
                                            <label htmlFor="">Remarks</label>
                                            <input
                                                type="text"
                                                data-var="note"
                                                id="note"
                                                name="note"
                                                className="lpay_input"
                                                autoComplete="off"
                                                onInput={this.emptyCheckHandler}
                                            />
                                            <div className="error_field">Note should not blank</div>
                                        </div>
                                    </div>
                                    

                                    <div className="col-md-3 mb-20 input-wrapper">
                                        <div className="lpay_input_group">
                                            <label htmlFor="">Mobile Number</label>
                                            <input
                                                type="text"
                                                data-var="custMobile"
                                                id="consumerMobileNo"
                                                name="custMobile"
                                                className="lpay_input"
                                                maxLength="10"
                                                autoComplete="off"
                                                value={this.state.data.CUST_MOBILE}
                                                readOnly={true}
                                            />
                                            <div className="error_field">Mobile number should not blank</div>
                                        </div>
                                    </div>
                                    

                                    <div className="col-md-3 mb-20 input-wrapper">
                                        <div className="lpay_input_group">
                                            <label htmlFor="">Email Id</label>
                                            <input
                                                type="text"
                                                data-var="custEmail"
                                                id="consumerEmailId"
                                                name="custEmail"
                                                className="lpay_input"
                                                autoComplete="off"
                                                value={ this.state.data.CUST_EMAIL}
                                                readOnly={true}
                                            />
                                            <div className="error_field">Email ID should not blank</div>
                                        </div>
                                    </div>
                                    
                                    <div className="col-md-3 mb-20 input-wrapper">
                                        <div className="lpay_input_group">
                                            <label htmlFor="">Amount</label>
                                            <input
                                                type="text"
                                                data-var="amount"
                                                id="amount"
                                                name="amount"
                                                className="lpay_input"
                                                autoComplete="off"
                                                value={this.state.data.AMOUNT}
                                                readOnly={true}
                                            />
                                            <div className="error_field">Amount should not blank</div>
                                        </div>
                                    </div>
                                    
                        
                                    <div className="col-md-3 mb-20 input-wrapper">
                                        <div className="lpay_input_group">
                                        <label htmlFor="monthlyAmount">{this.state.data.FREQUENCY === 'As And When Presented' ? 'ADHO -' : this.state.data.FREQUENCY} Debit Amount</label>
                                            <input
                                                type="text"
                                                data-var="monthlyAmount"
                                                id="monthlyAmount"
                                                name="monthlyAmount"
                                                className="lpay_input"
                                                autoComplete="off"
                                                value={this.state.data.MONTHLY_AMOUNT}
                                                readOnly={true}
                                            />
                                            <div className="error_field">Max Amount should not blank</div>
                                        </div>
                                    </div>
                                    
                        
                                    <div className="col-md-3 mb-20 input-wrapper">
                                        <div className="lpay_input_group">
                                            <label htmlFor="">Total Amount</label>
                                            <input
                                                type="text"
                                                data-var="totalAmount"
                                                id="totalAmount"
                                                name="totalAmount"
                                                className="lpay_input"
                                                autoComplete="off"
                                                value={this.state.data.TOTAL_AMOUNT}
                                                readOnly={true}
                                            />
                                            <div className="error_field">Total Amount should not blank</div>
                                        </div>
                                    </div>
                            
                                    <div className="col-md-3 mb-20 input-wrapper">
                                        <div className="lpay_input_group">
                                            <label htmlFor="">Tenure</label>
                                            <input
                                                type="text"
                                                data-var="tenure"
                                                id="tenure"
                                                name="tenure"
                                                className="lpay_input"
                                                autoComplete="off"
                                                value={this.state.data.TENURE}
                                                readOnly={true}
                                            />
                                        </div>
                                    </div>
                            
                                    <div className="col-md-3 mb-20 input-wrapper">
                                        <div className="lpay_input_group">
                                            <label htmlFor="">Start Debit Date</label>
                                            <input
                                                type="text"
                                                data-var="startDate"
                                                id="startDate"
                                                name="startDate"
                                                className="lpay_input"
                                                autoComplete="off"
                                                value={this.state.data.DEBIT_START_DATE}
                                                readOnly={true}
                                            />
                                        </div>
                                    </div>
                            
                                    <div className="col-md-3 mb-20 input-wrapper">
                                        <div className="lpay_input_group">
                                            <label htmlFor="">Debit End Date</label>
                                            <input
                                                type="text"
                                                data-var="endDate"
                                                id="endDate"
                                                name="endDate"
                                                className="lpay_input"
                                                autoComplete="off"
                                                value={this.state.data.DEBIT_END_DATE}
                                                readOnly={true}
                                            />
                                        </div>
                                    </div>

                                    <div className="col-md-3 mb-20 hide-input input-wrapper">
                                        <div className="lpay_input_group">                                            
                                            <input
                                                type="text"
                                                data-var="frequency"
                                                id="frequency"
                                                name="frequency"
                                                className="lpay_input"
                                                autoComplete="off"
                                                value={this.state.data.FREQUENCY}
                                                readOnly={true}
                                            />
                                        </div>
                                    </div>
                        
                            
                                    <div className="col-md-3 mb-20 hide-input input-wrapper">
                                        <div className="lpay_input_group">
                                            <input type="text" data-var="payId" name="payId" value={this.state.data.PAY_ID} readOnly={true} />
                                        </div>
                                    </div>

                                    {
                                        this.state.data.SUB_MERCHANT_ID !== null && this.state.data.SUB_MERCHANT_ID !== '' ?
                                            <div className="col-md-3 mb-20 hide-input input-wrapper">
                                                <div className="lpay_input_group">
                                                    <input type="text" data-var="subMerchantPayId" name="subMerchantPayId" value={this.state.data.SUB_MERCHANT_ID} readOnly={true} />
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
                                                    <input type="text" data-var="merchantLogo" name="merchantLogo" value={this.state.data.LOGO} readOnly={true} />
                                                </div>						
                                            </div>
                                        : null
                                    }
                            
                                    <div className="col-md-3 mb-20 hide-input input-wrapper">
                                        <div className="lpay_input_group">
                                            <input type="text" id="returnUrl" data-var="returnUrl" name="returnUrl" value={this.state.data.MERCHANT_RETURN_URL} readOnly={true} />
                                        </div>					
                                    </div>
                            
                                    <div className="col-md-3 mb-20 hide-input input-wrapper">
                                        <div className="lpay_input_group">
                                            <input type="text" id="consumerId" data-var="orderId" name="orderId" value={this.state.data.ORDER_ID} readOnly={true} />
                                        </div>
                                    </div>
                            
                                    <div className="col-md-3 mb-20 hide-input input-wrapper">
                                        <div className="lpay_input_group">
                                            <input type="text" id="purpose" data-var="purpose" name="purpose" value={this.state.data.PURPOSE} readOnly={true} />
                                        </div>
                                    </div>
                                    
                                    <div className="col-md-12 text-center">
                                        <button className="lpay_button lpay_button-md lpay_button-primary" id="btnSubmit" onClick={this.autoPaySubmitHandler}>Submit</button>
                                        <button className="lpay_button lpay_button-md lpay_button-secondary cnclButton" data-text="cancel" onClick={this.autoPayCancelHandler}>Cancel</button>
                                    </div>

                                    <div className="col-md-12 text-right mt-15">
                                        <div className="footer_logo text-right">
                                            <span>Powered By</span>
                                            <img src={`${window.basePath}/img/upi-autopay-logo.png`} alt="/" />
                                            <img src={`${window.basePath}/img/npci-logo.png`} alt="/" />
                                        </div>                                        
                                    </div>
                                </div>
                            </section>
                        </div>

                        {/* <input type="hidden" id="logoUrl" value={this.state.data.LOGO} readOnly={true} /> */}

                        <form action="upiAutoPayResponse" method="POST" id="cancelForm">
                            <input type="hidden" name="message" id="msg" value={this.state.message} />
                            {
                                this.state.data.RESPONSE === 'INVALID REQUEST' ?
                                    <input type="hidden" name="merchantReturnUrl" value={this.state.data.MERCHANT_RETURN_URL} readOnly={true} />
                                : null
                            }

                            {
                                this.state.data.RESPONSE === 'Duplicate order Id' ?
                                    <input type="hidden" name="merchantReturnUrl" value={this.state.data.MERCHANT_RETURN_URL} readOnly={true} />
                                : null
                            }
                        </form>

                        <form id="upiAutoPay" action="upiAutoPayResponse" method="POST">
                            <input type="hidden" name="orderId" id="orderId" value={this.state.data.ORDER_ID} readOnly={true} />
                        </form>

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

export default AutoPayMandate;