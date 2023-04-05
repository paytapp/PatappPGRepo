import React, { PureComponent } from "react";
import UpiBox from "./UpiBox";
import UpiQrBox from "./UpiQrBox";
import { multilingual } from "../../js/script";
class Upi extends PureComponent {
    state = {
        activeComponent: this.props.dataObj.upi ? "UP" : "UPI_QR"
    }

    componentDidMount() {
        multilingual();
    }

    toggleListHandler = e => {
        e.preventDefault();

        let toggleList = e.target.closest(".toggle-list"),
            isActive = toggleList.classList.contains("active"),
            mopType = toggleList.getAttribute("data-type");

        if(!isActive) {
            this.setState({activeComponent: mopType});
        }

        if(mopType === "UPI_QR") {
            window.id("pay-now").classList.add("d-none");
        } else {
            window.id("pay-now").classList.remove("d-none");
        }
    }

    iconHandler = (isActive) => {
        return (
            <span className="toggle-icon">
                {isActive ?
                    <i className="pg-icon icon-check-circle d-inline-block font-weight-bold font-size-20 text-primary"></i>
                :
                    <i className="pg-icon icon-right-arrow d-inline-block font-weight-bold font-size-14 text-primary"></i>                    
                }
            </span>
        );
    }

    render() {
        const components = [
            {
                "label": "UP",
                "isActive": this.props.dataObj.upi,
                "COMPONENT": UpiBox
            },
            {
                "label": "UPI_QR",
                "isActive": this.props.dataObj.upiQr,
                "COMPONENT": UpiQrBox
            }
        ];
        
        const componentsToRender = components.map((Component, index) => {
            if(Component.isActive) {
                return <Component.COMPONENT
                    key={index}
                    isActive={Component.label === this.state.activeComponent ? true : false}
                    iconToggleHandler={this.iconHandler}
                    clickHandler={this.toggleListHandler}
                    paymentType={this.props.paymentType}
                    submitHandler={this.props.submitHandler}
                    cancelHandler={this.props.cancelHandler}
                    dataObj={this.props.dataObj}
                />
            } else {
                return null;
            }
        });

        return (
            <div className="col-12 tabBox upiBox" id="upi">
                <div className="tabbox-inner px-xl-15">
                    <div className="toggle-list-box">
                        {componentsToRender}
                    </div>
                </div>
            </div>
        );
    }
}

export default Upi;