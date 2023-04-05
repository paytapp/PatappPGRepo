import React, { Component } from "react";
import Loader from "../Loader/Loader";
import ErrorPage from '../ErrorPage/ErrorPage';

class UpiMerchantHosted extends Component {
    state = {
        data: null
    }

    componentDidMount() {
        try {
            var responseObj = window.id("sessionObj").value;
            responseObj = JSON.parse(responseObj);
            
            if(responseObj.RESPONSE_CODE !== undefined) {
                this.setState({data: responseObj});

                this.startWorker({
                    pgRefNum: responseObj.PG_REF_NUM,
			        requestType: "UPI",
                    token: responseObj.customToken
                });
            } else {
                this.setState({data: "error"});
            }
        } catch(e) {
            this.setState({data: "error"});
            console.error(e);
        }
    }

    startWorker = obj => {
        if(window.Worker) {
            window.worker = new Worker(`${window.basePath}/js/WebWorker.js`);
            
            if(obj.requestType == "UPI") {
                window.worker.postMessage({
                    value: obj.pgRefNum,
                    token: obj.token,
                    requestType: obj.requestType
                });
        
                window.worker.onmessage = (e) => {
                    if(e.data == "cancel") {
                        window.id("cancel-form").submit();
                    } else {
                        try {
                            document.getElementById("resPgRefNum").value = e.data.PG_REF_NUM;
                            document.getElementById("resReturnUrl").value = e.data.RETURN_URL;
                            document.getElementById("upiResponseSubmitForm").submit();
                        } catch {
                            this.setState({data: "error"});
                            console.error(e);
                        }
                    }
                };
            }
        }
    }

    render() {
        let mainContent = <Loader processing={false} approvalNotification={true} />;
        
        if(this.state.data !== null) {
            if(this.state.data == "error") {
                mainContent = <ErrorPage />
            } else {
                mainContent = (
                    <>
                        <Loader processing={false} approvalNotification={true} />

                        <form name="upiResponseSubmitForm" id="upiResponseSubmitForm" target="_self" method="POST" action={`${window.basePath}/jsp/upiResponse`}>
                            <input type="hidden" name="OID" id="resOid" value="" />
                            <input type="hidden" name="RETURN_URL" id="resReturnUrl" value="" />
                            <input type="hidden" name="PG_REF_NUM" id="resPgRefNum" value={this.state.data.PG_REF_NUM} />
                            <input type="hidden" name="token" value={this.state.data.customToken} />
                        </form>

                        <form method="POST" action={`${window.basePath}/jsp/txncancel`} id="cancel-form">
                            <input type="hidden" name="payId" value={this.state.data.PAY_ID} />
                        </form>
                    </>
                );
            }
        }

        return (
            <>
                { mainContent }
            </>
        );
    }
}

export default UpiMerchantHosted;