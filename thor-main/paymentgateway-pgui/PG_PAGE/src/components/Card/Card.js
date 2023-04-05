import React, { Component } from "react";
import { inputHandler, isNumberKey, numOnly, numberEnterPhone, toolTipCvvHandler, addConvenienceFee, multilingual } from "../../js/script";
import helpers from "./js/helpers";
import Checkbox from "../Checkbox/Checkbox";
import Declaration from "../Declaration/Declaration";
import Loader from "../Loader/Loader";
import CvvInfo from "../CvvInfo/CvvInfo";
import auxillary from '../../hoc/auxiliary';
class Card extends Component {
    state = {
        checked: false,
        loader: false
    }

    componentDidMount() {
        this.createMopTypeImages(this.props.dataObj.ccMopTypes, "cc", "ccMopIcon", "credit_cards");
        this.createMopTypeImages(this.props.dataObj.dcMopTypes, "dc", "dcMopIcon", "debit_cards");
        this.createMopTypeImages(this.props.dataObj.emCCMopType, "emicc", "emiccMopIcon", "emi_credit_cards");
        this.createMopTypeImages(this.props.dataObj.emDCMopType, "emidc", "emidcMopIcon", "emi_debit_cards");

        addConvenienceFee(this.props.paymentType);
        multilingual();
    }

    // CREATE MOP IMAGE
    
    createMopTypeImages = (mopType, paymentType, className, id) => {
        if(mopType !== undefined) {
            let collection = [];
            mopType.forEach((mop, index) => {
                let currentMopType = mop.toLowerCase();
                collection.push('<img src="'+ window.basePath + '/img/' + currentMopType + '.png" alt="' + currentMopType + '" id="' + currentMopType + paymentType +'" class="'+ className +'">');
            });
            
            window.id(id).innerHTML = collection.join('');
        }
    }

    checkFields= e => {
        let _cardNumber = window.id("card-number"),
            cvvNumber = window.id('cvvNumber'),
            cardName = window.id('cardName');
    
        if(!helpers.checkFirstLetter()) {
            e.preventDefault();
        }
    
        if(!helpers.checkLuhnInner(_cardNumber)) {
            e.preventDefault();
        } else if(!helpers.checkCardSupported()) {
            e.preventDefault();
        }
    
        if (cvvNumber.value.length !== Number(cvvNumber.getAttribute("maxlength"))) {
            window.id('emptyCvv').style.display = 'none';
            window.id('cvvValidate').style.display = "block";
            cvvNumber.classList.add("redLine");
            e.preventDefault();
        }
    
        if (cvvNumber.value.length === 0) {
            window.id('cvvValidate').style.display = "none";
            window.id('emptyCvv').style.display = 'block';
            cvvNumber.classList.add("redLine");
            e.preventDefault();
        }
        if (!cardName.value) {
            window.id('nameError').style.display = 'block';
            cardName.classList.add("redLine");
            e.preventDefault();
        }
        if (!helpers.CheckExpiry()) {
            let paymentDate = window.id('paymentDate'),
                emptyExpiry = window.id('emptyExpiry'),
                validExpDate = window.id('validExpDate');
    
            if (paymentDate.value) {
                emptyExpiry.style.display = 'none';
                validExpDate.style.display = 'block';
            } else {
                emptyExpiry.style.display = 'block';
                validExpDate.style.display = 'none';
            }

            paymentDate.classList.add("redLine");
            e.preventDefault();
        }
    
        window.id('setExpiryMonth').value = window.id('paymentDate').value.split('/')[0];
        window.id('setExpiryYear').value = '20' + window.id('paymentDate').value.split('/')[1];
        window.id('cardNumber').value = window.querySelector('.cardNumber').value;
    
        if (helpers.checkFirstLetter() && helpers.CheckExpiryBoolean() && helpers.checkCvv() && helpers.nameCheckKeyUp() && helpers.checkMopTypeValidForUser() && helpers.checkLuhnInner(_cardNumber) && helpers.checkLuhnBooleanVal() && helpers.checkCardSupported()) {
            this.setState({loader: true, defaultText: true});
        } else {
            e.preventDefault();
        }
    }

    render() {
        let loader = null;
        if(this.state.loader) {
            loader = <Loader processing={true} approvalNotification={false} />
        }

        // EMI PARAMETERS
        let emiParameters = Object.keys(this.props.formParameters).map(keys => {
            return <input type="hidden" name={keys} value={this.props.formParameters[keys]} key={keys} />
        });

        return (
            <>
                <>
                    <form autoComplete="off" name="creditcard-form" method="post" target="_self" action={`${window.basePath}/jsp/pay`} id="creditCard" onSubmit={this.checkFields}>
                        <div className="row cardSection">
                            <div className="allCreditCard" id="credit_cards"></div>
                            <div className="allCreditCard" id="debit_cards"></div>
                            <div className="allCreditCard" id="emi_credit_cards"></div>
                            <div className="allCreditCard" id="emi_debit_cards"></div>

                            <div className="col-lg-6 cardNumber1 card-number" id="divCardNumber">
                                {/* <s:set name="valid cardnumber" value="getText('CardNumber')" />
                                <input type="hidden" id="cardPlaceHolderCC" value="%{cardNumberTextCC}" />
                                <input type="hidden" id="validCardDetail" value="%{valid cardnumber}" /> */}

                                <input type="hidden" id="paymentType2" name="paymentType" />
                                <input type="hidden" id="mopTypeCCDiv2" name="mopType" />
                                <input type="hidden" id="issuerBankName2" name="issuerBankName" />
                                <input type="hidden" id="issuerCountry2" name="issuerCountry" />
                                <input type="hidden" id="cardHolderTypeId" name="cardHolderType" />
                                <input type="hidden" id="paymentsRegionId" name="paymentsRegion" />
    
                                {/* PARAMETERS FOR EMI */}
                                { emiParameters }

                                <input type="text" id="cardNumber" name="cardNumber" className="d-none" />

                                <label htmlFor="card-number" className="placeHolderText placeHolderTextCardNum w-100 text-grey-light font-size-12 line-height-15 d-inline-flex justify-content-between mb-5 field-title"><span className="lang" data-key="cardNumber">Card Number</span><span id="supported-payment-type" className="d-none" data-type="NA" data-region="NA"></span></label>

                                <div className="position-relative">
                                    <div className="userMoptype d-flex justify-content-end">
                                        <img id="userMoptypeIcon" className="align-self-center" src={window.basePath + '/img/bc.png'} alt="" />
                                    </div>
                                    
                                    <input
                                        autoComplete="false"
                                        className="cardNumber pField masked inputField form-control font-size-14 font-size-sm-16 text-grey-dark font-weight-medium p-8 border outline-none"
                                        type="text"
                                        id="card-number"
                                        placeholder=" "
                                        onInput={(e) => {helpers.enterCardNum(); helpers.fourDigitSpace(e); helpers.enterCardNumRmvErrMsg(); helpers.tabChangeKeyPress(e,'paymentDate'); helpers.removeEnterCardMsg();}}
                                        onKeyUp={(e) => {helpers.restrictSpaceRemoval(e); helpers.enterCardNum(); helpers.enterCardNumRmvErrMsg(); helpers.tabChangeKeyPress(e,'paymentDate');}}
                                        onBlur={(e) => {helpers.checkCardSupported(); helpers.checkLuhn(e);}}
                                        onKeyDown={(e) => {helpers.removeEnterCardMsg()}}
                                        onPaste={inputHandler}
                                        maxLength="23"
                                        inputMode="numeric" />
                                </div>

                                <div className="resultDiv">
                                    <p id="checkStartNo" className="text-danger1 font-size-12 lang" data-key="checkStartNo">Invalid Card Number</p>
                                    <p id="validCardCheck" className="text-danger1 font-size-12 lang" data-key="validCardCheck">Invalid Card Number</p>
                                    <p id="emptyCardNumber" className="text-danger1 font-size-12 lang" data-key="emptyCardNumber">Please Enter Card Number</p>
                                    <p id="notSupportedCard" className="text-danger1 font-size-12 lang" data-key="notSupportedCard">Card Not Supported</p>
                                    <p id="emiBinCheckError" className="text-danger1 font-size-12"></p>
                                </div>
                            </div>

                            <div className="col-6 col-sm-4 col-lg-3 validityMain">
                                <div className="validity" id="validity">
                                    <label htmlFor="paymentDate" className="placeHolderText placeHolderTextExpDate w-100 text-grey-light font-size-12 line-height-15 d-inline-flex mb-5 lang" data-key="expiry">Expiry</label>
                                    
                                    <input
                                        type="text"
                                        id="paymentDate"
                                        inputMode="numeric"
                                        autoComplete="new-password"
                                        placeholder=" "
                                        className="inputField validityDateCard form-control font-size-14 font-size-sm-16 text-grey-dark font-weight-medium p-8 border outline-none"
                                        maxLength="5"
                                        onKeyUp={(e) => {helpers.monthYearEnter(e); helpers.restrictSlashRemoval(e); helpers.enterCardNum(); helpers.removeMmDdError(); helpers.numberEnterCvv(e); helpers.tabChangeKeyPress(e,'cvvNumber');}}
                                        onKeyDown={(e) => {helpers.monthYearEnter(e); helpers.restrictSlashRemoval(e); return numOnly(e);}}
                                        onBlur={helpers.CheckExpiryOnBlur}
                                        onPaste={inputHandler}
                                        onDrop={inputHandler} />
    
                                    <input type="hidden" name="expiryYear" id="setExpiryYear" />
                                    <input type="hidden" name="expiryMonth" id="setExpiryMonth" />
                                    
                                    <div className="resultDiv mb-md-5 clearfix">
                                        <div className="validExpDate">
                                            <p id="validExpDate" className="text-danger1 font-size-12 lang" data-key="validExpDate">Invalid Date</p>
                                            <p id="emptyExpiry" className="text-danger1 font-size-12 lang" data-key="emptyExpiry">Please Enter Expiry</p>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div className="col-6 col-sm-4 col-lg-3 cvv-info cvv" id="divCvv">
                                <label className="placeHolderText placeHolderTextCvv w-100 text-grey-light font-size-12 line-height-15 d-inline-flex mb-5">CVV</label>
                                
                                <div className="position-relative">
                                    <input
                                        autoComplete="new-password"
                                        className="pField inputField form-control font-size-14 font-size-sm-16 text-grey-dark font-weight-medium p-8 border outline-none"
                                        type="text"
                                        inputMode="numeric"									
                                        name="cvvNumber"
                                        placeholder=" "
                                        id="cvvNumber"
                                        onInput={isNumberKey}
                                        onKeyPress={isNumberKey}
                                        onKeyUp={(e) => {helpers.enterCardNum(); helpers.removeCvvError(); numberEnterPhone(e); helpers.tabChangeKeyPress(e,'cardName');}}
                                        onBlur={helpers.checkCvvFocusOut}
                                        onPaste={inputHandler}
                                        maxLength="3"
                                        onDrop={inputHandler} />
                                    
                                    {this.props.width > 768 && <img className="info" onMouseOver={(e) => toolTipCvvHandler(e, 'block')} onMouseOut={(e) => toolTipCvvHandler(e, 'none')} src={window.basePath + '/img/info.png'} alt="" /> }
                                </div>
                                
                                {this.props.width > 768 && <CvvInfo /> }
                                
                                <div className="cvvValidate">
                                    <p id="cvvValidate" className="text-danger1 font-size-12 lang" data-key="cvvValidate">Invalid CVV</p>
                                    <p id="emptyCvv" className="text-danger1 font-size-12 lang" data-key="emptyCvv">Please Enter CVV</p>
                                </div>
                            </div>

                            <div className="col-sm-4 col-lg-12 card-holder-name" id="divName">
                                <label className="placeHolderText placeHolderTextNameOnCard w-100 text-grey-light font-size-12 line-height-15 d-inline-flex mb-5 lang field-title" data-key="nameOnCard">Name on Card</label>
                                <input
                                    autoComplete="new-password"
                                    className="pField inputField card_holder_name form-control font-size-14 font-size-sm-16 text-grey-dark font-weight-medium p-8 border outline-none"
                                    type="text"
                                    name="cardName"
                                    id="cardName"
                                    placeholder=" "
                                    onKeyPress={(e) => {return helpers.isCharacterKeyWithSpace(e)}}
                                    onKeyUp={(e) => {helpers.nameCheckKeyUp(); helpers.alphabetEnterPhone(e); helpers.enterCardNum();}}
                                    onKeyDown={(e) => {helpers.alphabetEnterPhone(e)}}
                                    onBlur={(e) => {helpers.nameCheck(); e.target.scrollLeft = 0;}}
                                    onCopy={inputHandler}
                                    onPaste={inputHandler}
                                    onDrop={inputHandler}
                                    maxLength="256"
                                />
                                
                                <div className="resultDiv">
                                    <p id="nameError" className="text-danger1 font-size-12 lang" data-key="nameError">Please Enter Name</p>
                                    <p id="invalidError" className="text-danger1 font-size-12 lang" data-key="invalidError">Invalid Name</p>
                                </div>
                            </div>

                            {/* CHECKBOX STARTED */}
                            <Checkbox
                                columnId="divSaveCard"
                                checked={this.state.checked}
                                name="cardsaveflag"
                                dataKey="saveCardCheckboxText"
                                checkboxText="Save this card For future payments"
                            />
                            {/* CHECKBOX ENDED */}

                        </div>

                        {/* <div className="row mt-10">
                            <div className="col-12">
                                <div className="bg-grey-primary border font-size-14 p-10" id="rbi-guideline">As per RBI's directive, w.e.f October 01, 2020, any indian debit/credit card not yet used for online e-commerce will be blocked for all online transactions. In case of such failure, please contact your card issuing bank to enable online usage.</div>
                            </div>
                        </div> */}

                        <Declaration
                            id="common-tax-declaration"
                            btnId="submit-btns-debitWithPin"
                            submitHandler={this.props.submitHandler}
                            cancelHandler={this.props.cancelHandler}
                            dataObj={this.props.dataObj}
                        />

                        <input type="hidden" id="ccdc-amount" name="amount" />
                        
                        <button type="submit" className="btn-payment d-none"></button>
                    </form>                    
                </>

                { loader }
            </>
        );
    }
}

export default auxillary(Card);