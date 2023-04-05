import React, { Component } from 'react';
import './captcha.css';

class Captcha extends Component{

    

    render(){
        return(
        <div className="col-md-12 mb-20">
            <div className="lpay_input_group captcha_div">                
                <input
                    type="text"
                    name={this.props.relName}
                    value={this.props.captchaValue}
                    onInput={e => {this.props.changeHandler(e);this.props.captchaHandler(e); }}
                    maxLength="4"
                    className="lpay_input"
                />

                <img src={`${window.basePath}/img/captcha.jpg`} alt="" />
                { <span className='error-msg'>{ this.props.errorHandler }</span> }
            </div>
        </div>  
        )
    }

}

export default Captcha;