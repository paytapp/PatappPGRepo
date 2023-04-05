import React, {PureComponent} from "react";
import Checkbox from "../Checkbox/Checkbox";
import Declaration from "../Declaration/Declaration";
import SelectedBanks from "../SelectedBanks/SelectedBanks";
import BankOptionList from "../SelectedBanks/BankOptionList";
import { addConvenienceFee, multilingual } from "../../js/script";
import BankList from "./BankList";
class NetBanking extends PureComponent {
    state = {
        activeBank: '',
        displayError: this.props.displayError
    }

    componentDidMount() {
        addConvenienceFee(this.props.paymentType);
        multilingual();
    }

    optionList = (optionText, index) => {
        return <BankOptionList
            data={optionText}
            key={index}
        />
    }

    netBanking = (e) => {
        window.id("bankList").value = e.target.value;
        this.setState({activeBank: e.target.value});

        window.id("bankList").classList.remove("redLine");
		window.id("error-bankList").style.display = "none";

        window.id("pay-now").classList.remove("btn-disabled");
    }

    selectDefaultBank = e => {
        let selectedValue = e.target.value,
            payBtn = window.id("pay-now");

        this.setState({activeBank: selectedValue});

        if(selectedValue !== "") {
            payBtn.classList.remove("btn-disabled");

            window.id("bankList").classList.remove("redLine");
		    window.id("error-bankList").style.display = "none";

            // this.setState({displayError: false});
        } else {
            // this.setState({displayError: true});

            window.id("bankList").classList.add("redLine");
		    window.id("error-bankList").style.display = "block";

            payBtn.classList.add("btn-disabled");
            e.target.blur();
        }
    }

    render() {
        let counter = 0;
        const defaultBanks = BankList.map((list, index) => {
            if(this.props.dataObj.nbMopType.includes(list.inputValue)) {
                counter++;
                
                if(counter <= 6) {
                    return <SelectedBanks
                        inputId={list.inputId}
                        inputValue={list.inputValue}
                        inputText={list.inputText}
                        key={index}
                        selectedBankHandler={this.netBanking}
                        isActive={list.inputValue === this.state.activeBank ? true : false}
                    />
                }

                return null;
            }

            return null;
        });

        return (
            <div className="col-12 tabBox netBankingBox" id="netBanking">
                <div className="tabbox-inner px-xl-15">
                    <form autoComplete="off" name="netBanking-form" method="post" target="_self" action={`${window.basePath}/jsp/pay`} id="netBankingform" onSubmit={(e) => { return this.props.validateForm(e, 'nbMopType', 'bankList'); }}>
                        <div className="card-detail-box" id="netbanking-list">
                            <div className="row">
                                { defaultBanks }
                            </div>
                        </div>
    
                        <div className="row selectdiv mt-10">
                            <div className="col-lg-6">
                                <label className="w-100 d-block font-size-12 text-grey-light mb-0 lang field-title" data-key="otherBank">Or select bank from dropdown</label> 
                                <select className="form-control border selectBank font-size-12" data-key="selectBank" id="bankList" name="bankList" onChange={this.selectDefaultBank}>
                                    <option value="">Select Bank</option>
                                    {
                                        this.props.dataObj.nbMopType.map((option, index) => {
                                            return this.optionList(option, index)
                                        })
                                    }
                                </select>

                                <div className="resultDiv"><p id="error-bankList" className="text-danger1 font-size-12 lang" data-key="errorBankList" >Please Select Bank</p></div>
                            </div>
    
                            <input type="hidden" id="nbPaymentType" name="paymentType" value="NB" />
                            <input type="hidden" id="nbMopType" name="mopType" value={this.state.activeBank} />
                        </div>
    
                        <div className="row">
                            {/* CHECKBOX STARTED */}
                            <Checkbox
                                columnId="divNbSave"
                                checked={this.props.dataObj.save_nb}
                                name="nbSaveFlag"
                                dataKey="saveNbText"
                                checkboxText="Save this Bank For future payments"
                            />
                            {/* CHECKBOX ENDED */}
                        </div>
    
                        <Declaration
                            id="NB-tax-declaration"
                            btnId="submit-btns-netBanking"
                            submitHandler={this.props.submitHandler}
                            cancelHandler={this.props.cancelHandler}
                            dataObj={this.props.dataObj}
                        />
    
                        <button type="submit" className="btn-payment d-none"></button>
                    </form>
                </div>
            </div>
        );
    }    
}

export default NetBanking;