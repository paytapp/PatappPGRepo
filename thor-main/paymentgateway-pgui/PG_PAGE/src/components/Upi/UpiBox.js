import { useState, useEffect } from "react";
import { inputHandler, addConvenienceFee } from "../../js/script";
import Checkbox from "../Checkbox/Checkbox";
import Declaration from "../Declaration/Declaration";

const InputField = props => {    
    const isValidVpaBoolean = _ => {
        var vpaRegex = /[A-Za-z0-9][A-Za-z0-9.-]*@[A-Za-z]{2,}$/,
            vpaElement = window.id("vpaCheck"),
            vpaValue = vpaElement.value.trim();

        if (!vpaValue.match(vpaRegex)) {
            return false;
        }
        
        return true;
    }

    const isValidVpa = e => {
        let that = e.target,
            vpaRegex = /^[a-zA-Z0-9\.@-]+$/,
            vpaValue = that.value.trim();
    
        that.classList.remove("redLine");
        window.id('red1').style.display = 'none';
        window.id('enterVpa').style.display = 'none';
    
        if (!vpaValue.match(vpaRegex)) {
            that.value = vpaValue.replace(/[^a-zA-Z0-9\.@-]/g, "");
        }
    }

    const enableButton = _ => {
        let upiSbmtBtn = window.id('pay-now'),
            vpaDisplayStyle = window.id("red1").style.display;
        
        if(vpaDisplayStyle !== "block" && isValidVpaBoolean()) {
            upiSbmtBtn.classList.remove("btn-disabled");
        } else {
            upiSbmtBtn.classList.add("btn-disabled");
        }
    }

    const restrictKeyVpa = e => {
        let key = e.keyCode,
            leftKey = 37,
            rightKey = 39,
            deleteKey = 46,
            backspaceKey = 8,
            tabKey = 9,
            point = 190,
            subtract = 189,
            subtractMoz = 173;

        if (e.key === "!" || e.key === "#" || e.key === "$" || e.key === "%" || e.key === "^" || e.key === "&" || e.key === "*" || e.key === "(" || e.key === ")" || e.key === ">" || e.key === "_") {
            return false;
        }

        return ((key >= 48 && key <= 57) || (key >= 33 && key <= 39) || (key >= 65 && key <= 90) || (key >= 96 && key <= 105) || key === backspaceKey || key === tabKey || key === leftKey || key === rightKey || key === deleteKey || key === point || key === subtract || key === subtractMoz || key === 12 || key === 40 || key === 45 || key === 109 || key === 110);
    }

    const isValidVpaOnFocusOut = _ => {
        let vpaRegex = /[A-Za-z0-9][A-Za-z0-9.-]*@[A-Za-z]{2,}$/,
            vpaElement = window.id("vpaCheck"),
            vpaValue = (vpaElement.value).trim();
    
        if (!vpaValue) {
            window.id('enterVpa').style.display = "block";
            window.id('red1').style.display = "none";
            vpaElement.classList.add("redLine");
            return false;
        } else if (!vpaValue.match(vpaRegex)) {
            vpaElement.classList.add("redLine");
            window.id('red1').style.display = "block";
            window.id('enterVpa').style.display = "none";
            return false;
        }
        return true;
    }

    const removeSpace = evt => {
        const that = evt.target,
        value = that.value;

        that.value = value.trim();
    }

    if(props.isActive) {
        return (
            <div className="toggle-content pb-10 pl-30 pl-sm-40 pr-15">
                <div className="row">
                    <div className="col-sm-6 col-md-12 col-lg-6">								
                        <div className="vpaSection">
                            <label className="placeHolderText placeHolderTextVPA font-size-12 text-grey-light mb-0 lang field-title" data-key="vpaAddress">UPI ID</label>
                            <input
                                type="text"
                                name="VPA"
                                id="vpaCheck"
                                placeholder=" "
                                className="inputField form-control"
                                onKeyUp={e => { enableButton(); isValidVpa(e); }}
                                onChange={e => { enableButton(); isValidVpa(e); restrictKeyVpa(e); }}
                                onKeyDown={e => { restrictKeyVpa(e); }}
                                onBlur={isValidVpaOnFocusOut}
                                onCopy={inputHandler}
                                onPaste={inputHandler}
                                onDrop={inputHandler}
                                onInput={removeSpace}
                            />
                        </div>
                        
                        <p className="red1 lang" data-key="invalidVpa" id="red1">Invalid UPI ID</p>
                        <p className="red1 lang" data-key="enterVpa" id="enterVpa">Please Enter UPI ID</p>
                    </div>

                    <div className="col-12 mt-5">
                        <label className="vpaPara text-grey-light lang mb-0" data-key="vpaPara">UPI ID is a unique Payment address that is linked to a person's bank account to make payments.</label>
                    </div>
                    
                    <div><small id="errorBox" className="text-danger"></small></div>

                    {/* CHECKBOX STARTED */}
                    <Checkbox
                        columnId="divVpaSave"
                        checked={props.dataObj.save_vpa}
                        name="vpaSaveFlag"
                        dataKey="saveVpaText"
                        checkboxText="Save this UPI ID for future payments"
                    />
                    {/* CHECKBOX ENDED */}
                </div>

                <Declaration
                    id="UP-tax-declaration"
                    btnId="submit-btns-upi"
                    submitHandler={props.submitHandler}
                    cancelHandler={props.cancelHandler}
                    dataObj={props.dataObj}
                />
            </div>
        );
    } else {
        return null;
    }
}

const UpiBox = props => {
    useEffect(() => {
        addConvenienceFee(props.paymentType);
    }, []);

    return (
        <div className={`toggle-list ${props.isActive ? 'active' : ''}`} data-type="UP">
            <span className="font-size-12 d-inline-block mb-5 lang" data-key="payUsingUpi">PAY USING UPI ID</span>
            <button className="d-flex toggle-box pr-15 pt-10 w-100" onClick={props.clickHandler}>
                <span className="font-size-26 pg-icon icon-upi mr-sm-10">
                    <span className="path1"></span>
                    <span className="path2"></span>
                    <span className="path3"></span>
                </span>
                <span className="d-flex flex-column w-100">
                    <span className="d-flex w-100 justify-content-between">
                        <span className="font-size-12 font-weight-bold toggle-title lang" data-key="vpaAddress">UPI ID</span>

                        {props.iconToggleHandler(props.isActive)}
                    </span>
                    <span className="d-block font-size-12">Google Pay, BHIM, PhonePe &amp; more</span>
                </span>
            </button>

            <InputField
                isActive={props.isActive}
                submitHandler={props.submitHandler}
                cancelHandler={props.cancelHandler}
                dataObj={props.dataObj}                    
            />
        </div>
    );
}

export default UpiBox;