import React, { Component } from 'react';
import Heading from '../heading/Heading';
import Wrapper from "../wrapper/Wrapper";
import {isNumberKey} from '../../js/scripts';

class SignUp extends Component{

    


    render(){
        return (
            <>
                <Wrapper>
                    <Heading title="Create Account" customClass="col-md-12" />
                    <div className="login_wrapper">
                        <div className="col-md-12 mb-20">
                            <div className="lpay_input_group">
                                <label htmlFor="">Mobile Number</label>
                                <input type="text" onInput={isNumberKey} className="lpay_input" />
                            </div>
                        </div>
                        <div className="login-action-btn">
                            <button className="lpay_button lpay_button-md lpay_button-primary taget-div" data-target="login-pin">Login with PIN</button>
                            <button className="lpay_button lpay_button-md lpay_button-secondary taget-div" id="loginOtp" data-target="login-otp">Login with OTP</button>
					    </div>
                    </div>
                <p className="tabchange_text">Already have account ? <button onClick={e => { this.props.tabHandler(e, "login"); }}>Login now</button></p>
                </Wrapper>
            </>
        )
    }

}

export default SignUp