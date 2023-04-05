import { Component } from "react";
import { inputHandler, isNumberKey, numOnly, numberEnterPhone, toolTipCvvHandler, addConvenienceFee, multilingual } from "../../js/script";
import RadioButton from "../RadioButton/RadioButton";
import "./emiStyle.css";
import Card from "../Card/Card";
import Loader from "../Loader/Loader";

class EMI extends Component {
    constructor(props) {
        super(props);

        this.state = {
            emiPaymentType: '',
            issuerBankName: '',
            emiBankList: null,
            isBankListActive: false,
            isEmiDetails: false,
            emiSlab: null,
            emiSlabActiveIndex: null,
            isScrollActive: false,
            showLoader: false
        };
    }

    addList = listArr => {
        return listArr.map(item => {
            return <option value={item} key={item}>{item}</option>
        });
    }

    updateEMIBankList = that => {
        const val = that.value;
        const isValue = val !== '';        
        const {emiCCIssuer, emiDCIssuer} = this.props.dataObj;

        this.setState({
            emiBankList: isValue ? this.addList(val === 'EMICC' ? emiCCIssuer : emiDCIssuer) : null,
            emiPaymentType: val,
            isBankListActive: isValue,
            issuerBankName: '',
            isEmiDetails: false,
            emiSlab: null,
            emiSlabActiveIndex: null,
            isScrollActive: false
        });

        if(isValue) {
            window.id("error-emiPaymentType").classList.add("d-none");
        }
    }

    fetchEmiDetail = that => {
        const val = that.value;

        if(val !== '') {
            window.id("error-emiBankName").classList.add("d-none");

            const payload = {
                paymentType: this.state.emiPaymentType.slice(3),
                payId: this.props.dataObj.PAY_ID,
                issuerBank: val,
                amount: Number(this.props.dataObj.AMOUNT) / 100
            }

            this.setState({showLoader: true});
            
            fetch(`${window.basePath}/jsp/fetchEmiDetail`, {
                method : "POST",
                body: JSON.stringify(payload),
                headers : { 
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                }      
            })
            .then(response => response.json())
            .then(data => {
                this.setState({
                    emiSlab: JSON.parse(data.emiSlab),
                    isEmiDetails: true,
                    issuerBankName: val,
                    showLoader: false,
                    emiSlabActiveIndex: null,
                    isScrollActive: false
                });
            })
            .catch(error => console.log(error));
        } else {
            this.setState({
                isEmiDetails: false,
                issuerBankName: val,
                isScrollActive: false
            })
        }
    }

    showEmiPaymentBox = (e, index) => {
        if(index !== this.state.emiSlabActiveIndex) {
            this.setState({
                emiSlabActiveIndex: index,
                isScrollActive: true
            });

            window.id("error-emi-detail").classList.add("d-none");
        } else {
            this.setState({
                emiSlabActiveIndex: null,
                isScrollActive: false
            });
        }
    }

    render() {
        // LOADER
        let loader = null;
        if(this.state.showLoader) {
            loader = <Loader processing={false} approvalNotification={false} />
        }

        // EMI DEBIT CARD
        let emiDcList = null;
        if(this.props.dataObj.emiDC) {
            emiDcList = <option value="EMIDC">Debit Card</option>
        }

        // EMI CREDIT CARD
        let emiCcList = null;
        if(this.props.dataObj.emiCC) {
            emiCcList = <option value="EMICC">Credit Card</option>
        }

        // EMI BANK LIST
        let emiBankList = null;
        if(this.state.isBankListActive) {
            emiBankList = (
                <div className="col-sm-6 col-lg-4">
                    <select data-id="mopType" id="emiBankName" onChange={e => this.fetchEmiDetail(e.target)} className="form-control mt-10 mt-sm-0 selectBank font-size-12" data-key="selectBank"  value={this.state.issuerBankName}>
                        <option value="">Select Bank</option>
                        { this.state.emiBankList }
                    </select>
                    <span className="text-danger font-size-12 d-none" id="error-emiBankName">Please select bank</span>
                </div>
            )
        }

        // EMI DETAILS
        let emiDetails = null;
        if(this.state.isEmiDetails) {
            // EMI SLAB LIST
            let emiSlabList = null;
            if(this.state.emiSlab !== null) {
                const currencyCode = <i className="pg-icon icon-inr mr-3"></i>;
                // const totalAmount = window.id("pay-now").querySelector(".value-block").innerHTML;
                emiSlabList = this.state.emiSlab.map((list, index) => {
                    const isActiveList = index === this.state.emiSlabActiveIndex;

                    // EMI FORM BOX
                    let emiFormBox = null;
                    if(isActiveList) {
                        emiFormBox = (
                            <div className={`bg-grey-lightest-2 emi-form-box ${isActiveList ? 'active-alt' : ''}`}>
                                <Card
                                    dataObj={this.props.dataObj}
                                    paymentType={this.state.emiPaymentType}
                                    submitHandler={this.props.submitHandler}
                                    cancelHandler={this.props.cancelHandler}
                                    formParameters={{
                                        tenure: list.tenure,
                                        issuerName: this.state.issuerBankName,
                                        rateOfInterest: list.rateOfInterest,
                                        perMonthEmiAmount: list.perMonthEmiAmount,
                                        totalEmiAmount: list.totalEmiAmount,
                                        emiInterest: list.interest
                                    }}
                                />
                            </div>
                        )
                    }

                    return (
                        <li className={`position-relative row-emi-detail ${isActiveList ? 'active mb-10' : ''}`} id={`row-${list.tenure}`} key={`row-${list.tenure}`}>
                            <label htmlFor={`emi-list-${index}`} className={`w-100 d-flex mb-10 emi-detail-heading max-width-485 cursor-pointer ${isActiveList ? 'px-15 py-10' : ''}`} onClick={e => this.showEmiPaymentBox(e, index)}>
                                <RadioButton isActive={isActiveList} name="emiBreakup" id={`emi-list-${index}`} />
                                <div className="emi-detail-right w-100">
                                    <div className="align-items-center position-relative d-md-flex font-size-12 font-size-sm-14 text-grey-light">
                                        <span className="d-inline-flex align-items-center font-weight-bold">{currencyCode} {list.perMonthEmiAmount.toFixed(2)}</span>
                                        <span className="d-inline-flex mx-3">x</span>
                                        <span className="font-weight-bold">{list.tenure}</span>
                                        <span className="d-inline-flex mx-3 font-weight-bold">months</span>
                                        <span className={`${isActiveList ? 'd-none' : ''} d-sm-inline-flex mr-3`}>|</span>
                                        <span className={`${isActiveList ? 'd-block' : ''} d-sm-inline text-grey-light font-weight-bold`}>{list.rateOfInterest}% pa</span>
                                    </div>
                                </div>
                            </label>

                            <div className="emi-detail-right w-100">
                                <div className="emi-detail-content bg-grey-lightest-2 border-primary max-width-485 font-size-12 text-grey-light">
                                    <div className="row text-center">
                                        <div className="col-12 col-sm-4 d-flex d-sm-block justify-content-between align-items-center mb-5 mb-sm-0">
                                            <h4 className="mb-sm-5">EMI/Month</h4>
                                            <div className="d-flex justify-content-sm-center align-items-center">{currencyCode} {list.perMonthEmiAmount.toFixed(2)}</div>
                                        </div>
                                        <div className="col-12 col-sm-4 d-flex d-sm-block justify-content-between align-items-center mb-5 mb-sm-0">
                                            <h4 className="mb-sm-5">Interest Amount</h4>
                                            <div className="d-flex justify-content-sm-center align-items-center">{currencyCode} {list.interest.toFixed(2)}</div>
                                        </div>
                                        <div className="col-12 col-sm-4 d-flex d-sm-block justify-content-between align-items-center mb-5 mb-sm-0">
                                            <h4 className="mb-sm-5">Total EMI Cost</h4>
                                            <div className="d-flex justify-content-sm-center align-items-center">{currencyCode} {list.totalEmiAmount.toFixed(2)}</div>
                                        </div>
                                    </div>
                                </div>

                                { emiFormBox }
                            </div>
                        </li>
                    );
                })
            }

            // EMI DETAILS
            emiDetails = (
                <>
                    <div id="emi-detail" className="full-width mt-20">
                        <ul id="emi-detail-data" className="mb-0 list-unstyled">
                            { emiSlabList }
                        </ul>
                    </div>
                    <span className="text-danger font-size-12 d-none" id="error-emi-detail">Please choose EMI Plan</span>
                </>
            )
        }

        return (
            <>
                <div className="col-12 tabBox emiBox" id="emi">
                    <div className={`tabbox-inner px-xl-15 ${this.state.isScrollActive ? 'custom-scroll' : ''}`}>
                        <div className="row">
                            <div className="col-sm-6 col-lg-4">
                                <select data-id="paymentType" id="emiPaymentType" onChange={e => { this.updateEMIBankList(e.target); addConvenienceFee(e.target.value); }} className="form-control selectBank font-size-12" data-key="selectDebitCredit">
                                    <option value="">Select Debit/Credit</option>
                                    { emiCcList }
                                    { emiDcList }
                                </select>
                                <span className="text-danger font-size-12 d-none" id="error-emiPaymentType">Please select Debit/Credit</span>
                            </div>

                            { emiBankList }
                        </div>

                        { emiDetails }
                    </div>
                </div>

                { loader }
            </>
        );
    }
}

export default EMI;