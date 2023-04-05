
import React, { Component } from "react";
import Wrapper from "../wrapper/Wrapper";
import Heading from "../heading/Heading";
import {isNumberKey, validateMobileNumber} from '../../js/scripts';
import OtpPinLogin from "./otpPinLogin";
import Captcha from './Captcha';

class Login extends Component{
    
    constructor(){
        super();
        this.state = {
            mobileNumberDiv : true,
            otpButtonDiv : false,
            otpPinDivEnable : false,
            resetButton: false,
            otpPinDivElement: "",
            fields: {},
            error: {},
            response : {}
        }
    }



    openField = (event, _active) => {
        this.setState({ 'otpButtonDiv': false,'otpPinDivEnable': true,'otpPinDivElement' : _active })
    }

    changeHandler = (event) => {
        let fields = this.state.fields;
        fields[event.target.name] = event.target.value;
        this.setState({fields});
        this.setState({error: {}});
    }



    submitWithCaptcha = (event) => {
        var _value = event.target.value;
        if(_value.length == 4){
            
            let fields = this.state.fields;
            fields['pin'] = document.querySelector("#otpId").value;
            fields['loginType'] = 'pin';
            fields['data'] = 'pwd';
            this.setState({fields});
            fetch(window.basePath + '/json/loginHandler.json', {
                // method: 'POST',
                // body: JSON.stringify(this.state.fields),
                headers : {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                }
            })
            .then((response) => response.json())
            .then((responseJson) => {
                let response = this.state.response;
                let error = this.state.error;
                response['responseCode'] = responseJson;
                this.setState({response});
                
                if(this.state.response.responseCode.responseCode == 'fail'){
                    this.props.tabHandler(this, "login");
                   error['validCredential'] = this.state.response.responseCode.responseMsg
                }else{
                    this.props.tabHandler(this, "dashboard");
                }
                this.setState({error});
            })
            .catch((error) => {
                console.error(error);
            });
        }
    }

    openActionButton = (e) => {
        var _value = e.target.value;
        if(_value.length == 10){
            var _readonly = document.createAttribute("readonly");
            e.target.setAttributeNode(_readonly);
            this.setState({'otpButtonDiv': true, 'resetButton': true});
        }
    }


    numberValid = (e) => {
        let error = this.state.error;
        if(validateMobileNumber(e)){
            error['mobileNumberError'] = '';
        }else{
            error['mobileNumberError'] = 'Please type valid mobile number';
        }
        this.setState({error});
    }
    
    render(){
        
        return (
            <>
                <Wrapper>

                   <Heading title="Login Form" customClass="col-md-12 lp-heading" />

                   <div className="login_wrapper">
                        { <span className='error-msg'>{this.state.error['validCredential']}</span> }
                       { this.state.mobileNumberDiv && 
                       <div className="col-md-12 mb-20">
                            <div className="lpay_input_group">
                                <label htmlFor="">Mobile Number</label>
                                { this.state.resetButton && <span onClick={this.resetLogin} className="reset-button">Reset Number</span> }
                                <input type="text" id="mobileNumber" name="phoneNumber" maxLength="10" data-var='mobileNumber' onInput={e => { isNumberKey(e);this.changeHandler(e);this.openActionButton(e); }} onBlur={(e) => {this.numberValid(e)}}  className="lpay_input" />
                                { <span className='error-msg'>{this.state.error['mobileNumberError']}</span> }
                            </div>
                       </div>
                       }

                       { this.state.otpButtonDiv &&
                            <div className="login-action-btn mb-20 text-center">
                                <button className="lpay_button lpay_button-md lpay_button-primary taget-div" onClick={(e) => {this.openField(e, 'pin')}} data-target="login-pin">Login with PIN</button>
                                <button className="lpay_button lpay_button-md lpay_button-secondary taget-div" onClick={(e) => {this.openField(e, 'otp')}} id="loginOtp" data-target="login-otp">Login with OTP</button>
                            </div>
                       }
                       
                        { this.state.otpPinDivEnable &&

                            <OtpPinLogin label={this.state.otpPinDivElement} otp="otpId" relName='otp' changeHandler={this.changeHandler}  openField={this.openField} visible={this.state.otpPinDivElement} />

                        }

                        { this.state.otpPinDivElement &&

                            <Captcha
                                captchaHandler={this.submitWithCaptcha}
                                captchaValue={this.state.fields['captcha'] !== undefined ? this.state.fields['captcha'] : ""}
                                errorHandler={this.state.error.errorCaptcha}
                                relName="captcha"
                                changeHandler={this.changeHandler}
                            />
                        }

                   </div>
                   
                    <p className="tabchange_text">Want to create account ? <button onClick={e => { this.props.tabHandler(e, "register"); }}>Register now</button></p>

                    
                    
                </Wrapper>
           </>
        )
    }

}

export default Login;