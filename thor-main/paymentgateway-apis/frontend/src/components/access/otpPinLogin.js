import React, { Component } from "react";

class OtpPinLogin extends Component{

    
    state = {
        'otp' : <div className="d-flex justify-content-between mb-20" onClick={e => { this.props.openField(e, 'pin') }}><span>00:50</span><span>Login with PIN</span></div>,
        'pin' : <div className="d-flex justify-content-between mb-20" onClick={e => { this.props.openField(e, 'otp') }}><span>Forget PIN</span><span>Login wit OTP</span></div>,
        'pin1' : '',
        'pin2' : '',
        'pin3' : '',
        'pin4' : '',
        'pin5' : '',
        'pin6' : '',
    }

    keyHandler = (event, _that) => {
        var _keyCode = event.which || event.keyCode;
        if(_keyCode > 47 && _keyCode < 58){
            if(event.target.nextElementSibling){
                if(event.target.value != ''){
                    event.target.nextElementSibling.focus();
                }
            }
        }else{
            event.target.value = '';
        }
        if(_keyCode == 8){
            if(event.target.previousElementSibling){
                event.target.previousElementSibling.focus();
            }
        }
    
    }

    handleChange = (evt) => {
        this.setState({
            [evt.target.name] : evt.target.value
        })
        if(evt.target.name == "pin6"){
            document.querySelector("[name='captcha']").focus();
        }
    }


    // componentDidMount(){
    //     this.timerFunction();
    // }

    timerFunction = () => {
        var sec = 50, countDiv = document.getElementById("timer"),
        secpass,
        countDown   = setInterval(function () {
            'use strict';
            
            secpass();
        }, 1000);
    
        function secpass() {
            'use strict';
            
            var min     = Math.floor(sec / 60),
                remSec  = sec % 60;
            
            if (remSec < 10) {
                remSec = '0' + remSec;
            }
            if (min < 10) {
                min = '0' + min;
            
            }
            countDiv.innerHTML = min + ":" + remSec;
            
            if (sec > 0) {
                
                sec = sec - 1;
                
            } else {
                clearInterval(countDown);

                // $("[data-id='generateOtp']").removeClass("d-none");
                // $("#resendOtp").removeClass("d-none");
                // $("#resendOtpSignUp").removeClass("d-none");
                // countDiv.innerHTML = '';
                
            }
        }
    }
    

    render(){
        
        return(
            <div className="col-md-12 mb-20 optPinLogin">
                <div className="lpay_input_group">
                    <label htmlFor="">{this.props.label}</label>
                    <div className="lpay_input_group_pin">
                        <span id="timer"></span>
                        
                        <input 
                        type="text" 
                        autoFocus 
                        maxLength="1" 
                        onKeyUp={this.keyHandler} 
                        onChange={this.handleChange} 
                        className="lpay_input"
                        name='pin1'
                        value={this.state.pin1} />
                        
                        <input type="text" name='pin2' value={this.state.pin2} onKeyUp={this.keyHandler} onChange={this.handleChange}  maxLength="1" className="lpay_input mb-2" />
                        <input type="text" name='pin3' value={this.state.pin3} onKeyUp={this.keyHandler} onChange={this.handleChange}  maxLength="1" className="lpay_input mb-2" />
                        <input type="text" name='pin4' value={this.state.pin4} onKeyUp={this.keyHandler} onChange={this.handleChange} maxLength="1" className="lpay_input mb-2" />
                        <input type="text" name='pin5' value={this.state.pin5} onKeyUp={this.keyHandler} onChange={this.handleChange} maxLength="1" className="lpay_input mb-2" />
                        <input type="text" name='pin6' value={this.state.pin6} onKeyUp={this.keyHandler} onChange={this.handleChange} maxLength="1" className="lpay_input mb-2" />
                    </div>
                    { this.state[this.props.visible] }
                    <input type="hidden" name="otp" id={this.props.otp} onChange={e => this.props.changeHandler(e)} value={this.state.pin1 + this.state.pin2 + this.state.pin3 + this.state.pin4 + this.state.pin5 + this.state.pin6} />
                </div>
                
            </div>
        )
    }
}


export default OtpPinLogin;