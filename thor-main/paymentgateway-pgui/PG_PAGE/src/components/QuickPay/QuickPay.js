import { PureComponent } from "react";
import SaveNbWrap from "./SaveNbWrap";
import SaveVpaWrap from "./SaveVpaWrap";
import SaveWlWrap from "./SaveWlWrap";
import Declaration from "../Declaration/Declaration";
import { addCustomScroll, addConvenienceFee, multilingual } from "../../js/script";

class QuickPay extends PureComponent {
    constructor(props) {
        super(props);

        this.state = {
            scrollFlag: false,
            activeId: null,
            showCard: false,
            showVpa: false,
            showNb: false,
            showWl: false,
            cvvInputVal: "",
            mopType: "",
            convenienceFee: null
        }
    }

    componentDidMount() {
        let flag = false;
        flag = this.setVisibility({
            token: this.props.dataObj.vpaToken,
            element: "showVpa",
            _flag: flag,
            isAvailable: this.props.dataObj.vpaTokenAvailable
        });

        flag = this.setVisibility({
            token: this.props.dataObj.nbToken,
            element: "showNb",
            _flag: flag,
            isAvailable: this.props.dataObj.nbTokenAvailable
        });

        flag = this.setVisibility({
            token: this.props.dataObj.wlToken,
            element: "showWl",
            _flag: flag,
            isAvailable: this.props.dataObj.wlTokenAvailable
        });

        multilingual();
    }

    componentDidUpdate() {
        if(!this.state.scrollFlag) {
            addCustomScroll();

            this.setState({scrollFlag: true});
        }
    }

    setVisibility = obj => {
        if(obj.token !== "NA" && obj.isAvailable) {
            if(!obj._flag) {
                this.setState({[obj.element] : true});
                
                return true;
            } else {
                this.setState({[obj.element] : true});
                return true
            }
        }

        return false;
    }

    handleClick = (event, key, paymentType, $mopType) => {
        let that = event.target.tagName !== "LABEL" ? event.target.closest("label") : event.target;

        if(!that.classList.contains("active")) {
            // ADD ACTIVE TO SELECTED ITEM
            this.setState({activeId: key});

            // ADD SURCHARGE IN ORDER SUMMARY
            addConvenienceFee(paymentType);

            this.setState({mopType: $mopType});
            
            window.id("pay-now").classList.remove("btn-disabled");
        }
    }

    updateFeeHandler = paymentType => {
        if(this.state.convenienceFee === null) {
            this.setState({convenienceFee: paymentType});
            addConvenienceFee(paymentType);
        }
    }

    render() {
        const controlsConfig = {
            left: {
                defaultIconContainerStyle: {
                    minWidth: "10px"                                    
                },
                defaultIconStyle: {
                    borderWidth: "0 1px 1px 0"
                }
            },
            right: {
                defaultIconContainerStyle: {
                    minWidth: "10px"                                    
                },
                defaultIconStyle: {
                    borderWidth: "0 1px 1px 0"
                }
            }
        };

        return (
            <div className="col-12 tabBox saveDetailsBox" id="saveDetails">
                <div className="tabbox-inner px-xl-15">
                    {this.state.showVpa && this.props.dataObj.vpaTokenAvailable ?
                    <SaveVpaWrap
                        activeId={this.state.activeId}
                        deleteHandler={this.props.deleteHandler}
                        handleClick={this.handleClick}
                        updateFeeHandler={this.updateFeeHandler}
                        token={this.props.dataObj.vpaToken}
                        mopType={this.state.mopType}
                        controlsConfig={controlsConfig}
                    /> : null}

                    {this.state.showNb && this.props.dataObj.nbTokenAvailable ?
                    <SaveNbWrap
                        activeId={this.state.activeId}
                        deleteHandler={this.props.deleteHandler}
                        handleClick={this.handleClick}
                        updateFeeHandler={this.updateFeeHandler}
                        token={this.props.dataObj.nbToken}
                        mopType={this.state.mopType}
                        controlsConfig={controlsConfig}
                    /> : null}

                    {this.state.showWl && this.props.dataObj.wlTokenAvailable ?
                    <SaveWlWrap
                        activeId={this.state.activeId}
                        deleteHandler={this.deleteHandler}
                        handleClick={this.handleClick}
                        updateFeeHandler={this.updateFeeHandler}              
                        token={this.props.dataObj.wlToken}
                        mopType={this.state.mopType}
                        controlsConfig={controlsConfig}
                    /> : null}

                    <Declaration
                        id="common-tax-declaration"
                        btnId="submit-btns-debitWithPin"
                        dataObj={this.props.dataObj}
                        submitHandler={this.props.submitHandler}
                        cancelHandler={this.props.cancelHandler}
                    />
                </div>
            </div>
        );
    }
}

export default QuickPay;