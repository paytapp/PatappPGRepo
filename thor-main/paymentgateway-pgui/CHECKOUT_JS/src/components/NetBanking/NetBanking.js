import React, {PureComponent} from "react";
import Checkbox from "../Checkbox/Checkbox";
// import Declaration from "../Declaration/Declaration";
import SelectedBanks from "./SelectedBanks/SelectedBanks";
import BankOptionList from "./SelectedBanks/BankOptionList";
import PaymentBtn from "../PaymentBtn/PaymentBtn";
import TitleSection from "../TitleSection/TitleSection";
import Fade from 'react-reveal/Fade';
import BankList from './BankList';

class NetBanking extends PureComponent {
    state = {
        activeBank: '',
        displayError: false,
    }

    render() {
        let counter = 0;
        const defaultBanksToRender = BankList.map((list, index) => {
            if(this.props.data.nbMopType.includes(list.inputValue)) {
                counter++;
                
                if(counter <= 6) {
                    return <SelectedBanks
                        inputId={list.inputId}
                        inputValue={list.inputValue}
                        inputText={list.inputText}
                        key={index}
                        labelSelectHandler={this.props.labelSelectHandler}
                        isActive={list.inputValue === this.props.selectedMopType ? true : false}
                        duration={index}
                    />
                }

                return null;
            }

            return null;
        });

        const bankListToRender = this.props.data.nbMopType.map((option, index) => {
            return <BankOptionList
                data={option}
                key={index}
            />
        });

        return (
            <React.Fragment>
                <TitleSection title={this.props.title} navigationHandler={this.props.navigationHandler} btnDataId="navigation" />

                <div className="container custom-container mt-166">
                    <div className="tabBox netBankingBox" id="netBanking">
                        <form autoComplete="off" name="netBanking-form" method="post" target="_self" action={`${Window.baseUrl}/pay`} id="netBankingform" onSubmit={(e) => { return this.props.validateFormHandler(e, 'nbMopType', 'bankList'); }}>
                            <div className="card-detail-box" id="netbanking-list">
                                <div className="row">
                                    { defaultBanksToRender }
                                </div>
                            </div>

                            <Fade bottom duration={1400}>
                                <div className="row selectdiv mt-10">
                                    <div className="col-lg-6">
                                        <label className="w-100 d-block font-size-12 text-grey-light mb-0 lang field-title" data-key="otherBank">Or select bank from dropdown</label> 
                                        <select value={this.props.selectedMopType} className={`form-control border selectBank ${this.props.displayError ? 'redLine' : ''}`} data-key="selectBank" id="bankList" name="bankList" onChange={this.props.selectDefaultBankHandler}>
                                            <option value="">Select Bank</option>
                                            { bankListToRender }
                                        </select>

                                        {this.props.displayError ? <p id="error-bankList" className="text-danger font-size-12 mb-0" data-key="errorBankList">Please Select Bank</p> : null }
                                    </div>

                                    <input type="hidden" id="nbPaymentType" name="paymentType" value="NB" />
                                    <input type="hidden" id="nbMopType" name="mopType" value={this.props.selectedMopType} />
                                </div>
                            </Fade>

                            <Fade bottom duration={1600}>
                                <div className="row">
                                    {/* CHECKBOX STARTED */}
                                    <Checkbox
                                        columnId="divNbSave"
                                        checked={this.props.data.save_nb}
                                        name="nbSaveFlag"
                                        dataKey="saveNbText"
                                        checkboxText="Save this Bank For future payments"
                                    />
                                    {/* CHECKBOX ENDED */}
                                </div>
                            </Fade>

                            <input type="hidden" name="encSessionData" value={this.props.data.encSessionData} />
                            <input type="hidden" name="CHECKOUT_JS_FLAG" value={this.props.data.checkOutJsFlag} />
                            <input type="hidden" name="payId" value={this.props.data.PAY_ID} />

                            <button type="submit" className="btn-payment d-none"></button>
                                
                            <PaymentBtn payBtnText={this.props.payBtnText} totalAmount={this.props.totalAmount} submitHandler={this.props.submitHandler} formId="netBankingform" isActivePayBtn={this.props.isActivePayBtn} />
                        </form>                
                    </div>
                </div>
            </React.Fragment>
        );
    }    
}

export default NetBanking;