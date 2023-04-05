import { Component } from "react";

class Base extends Component {
    fetchData = (methodName, actionName, payload) => {
        return fetch(`${window.basePath}/jsp/${actionName}`, {
            method : methodName,
            body: JSON.stringify(payload),
            headers : {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        })
        .then((response) => response.json())
        .then((responseJson) => {
            return responseJson;
        });
    }

    addConvenienceFee(paymentType, klass) {
        if(paymentType !== undefined && paymentType !== null && paymentType !== "") {
            Object.keys(window.surchargeMopType).forEach(element => {
                if(element === paymentType && element === "CC") {
                    if(window.id("mopTypeCCDiv2") !== null && window.id("mopTypeCCDiv2").value !== "") {
                        if(window.id("mopTypeCCDiv2").value === "AX") {
                            this.setFee({
                                showGST: true,
                                surchargeAmt: window.pageInfoObj.surcharge_cc_amex
                            }, klass);
                        } else {
                            this.setFee({
                                showGST: true,
                                surchargeAmt: window.pageInfoObj[window.surchargeMopType[[paymentType]][[window.id("cardHolderTypeId").value]]]
                            }, klass);
                        }
                    } else {
                        this.setFee({
                            showGST: true,
                            surchargeAmt: window.pageInfoObj.surcharge_cc_consumer
                        }, klass);
                    }
                } else if(element === paymentType && element === "DC") {
                    if(window.id("mopTypeCCDiv2") !== null && window.id("mopTypeCCDiv2").value !== "") {
                        this.setFee({
                            showGST: false,
                            surchargeAmt: window.pageInfoObj[window.surchargeMopType[[paymentType]][[window.id("mopTypeCCDiv2").value]]]
                        }, klass);
                    } else {
                        this.setFee({
                            showGST: false,
                            surchargeAmt: window.pageInfoObj.surcharge_dc_visa
                        }, klass);
                    }
                } else if(element === paymentType) {
                    this.setFee({
                        showGST: false,
                        surchargeAmt: window.pageInfoObj[window.surchargeMopType[paymentType]]
                    }, klass);
                }
            });
        } else {
            this.setFee({showGST: false, surchargeAmt: 0}, klass);
        }
    }
    
    setFee(obj, klass) {
        let isSurcharge = window.pageInfoObj["ENABLE_SURCHARGE"],
            amount = Number(window.pageInfoObj["AMOUNT"]) / 100,
            surchargeAmt = Number(obj.surchargeAmt),
            TOTAL_AMT = (amount + surchargeAmt).toFixed(2),
            GST = 0,
            SURCHARGE = 0,
            isGSTVisible = false;
    
        if(isSurcharge === "Y" && obj.showGST && amount > 500) {
            GST = surchargeAmt - (surchargeAmt * (100/(118)));
            SURCHARGE = surchargeAmt - window.GST;
            
            SURCHARGE = SURCHARGE.toFixed(2);
            GST = GST.toFixed(2);
            isGSTVisible = true;
    
            // this.adjustOrderSummaryWidth();
        } else {
            SURCHARGE = surchargeAmt.toFixed(2);
    
            isGSTVisible = false;
        }
    
        klass.setState({
            TOTAL_AMT: TOTAL_AMT,
            GST: GST,
            SURCHARGE: SURCHARGE,
            isGSTVisible: isGSTVisible
        });
    }

    adjustOrderSummaryWidth() {
        let orderSummaryList = window.id("order-summary").querySelectorAll("li");
        orderSummaryList.forEach(function(that) {
            let elementId = that.getAttribute("id");
    
            if(elementId !== "customerName") {
                let spanWidth = that.querySelector(".summary-label-text").getBoundingClientRect().width;
                that.querySelector(".summary-label").style.width = "calc(100% - "+ spanWidth +"px)";
            }
        });
    }    
}

export default Base;