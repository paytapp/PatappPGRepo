import React, { Component } from 'react';
import Loader from '../Loader/Loader';
import ErrorPage from '../ErrorPage/ErrorPage';
import "../../css/response.css";
import ResponseFooter from './ResponseFooter';
import ResponseHeader from './ResponseHeader';
import ResponseContent from './ResponseContent';
import Dialog from '../Dialog/Dialog';

class Response extends Component {
    constructor(props) {
        super(props);
        
        this.state = {
            data: null,
            statusResult: null,
            cardBox: null,
            isDialogOpen: false,
            dialogMsg: null,
            dialogType: null,
            showLoader: false
        };

        this.downloadForm = React.createRef();
    }

    componentDidMount() {
        try {
            let responseObj = window.id("sessionObj").value;
            responseObj = JSON.parse(responseObj);
            
            if(responseObj.RESPONSE_CODE !== undefined) {
                this.fetchData(responseObj);
            } else if(responseObj.ENCDATA !== undefined) {
                this.fetchData(responseObj);
            } else {
                this.setState({data: "error"});
            }
        } catch(e) {
            this.setState({data: "error"});
            console.error(e);
        }
    }

    fetchData = responseObj => {
        if(window.location !== window.parent.location) {
            const resObj = this.addParam(responseObj);

            window.parent.postMessage(resObj, "*");
            window.close();
        } else if(window.opener !== undefined && window.opener !== null) {
            const resObj = this.addParam(responseObj);

            window.opener.postMessage(resObj, "*");
            window.close();
        } else {
            this.setState({data: responseObj});

            if(responseObj.ENCDATA === undefined) {
                this.updateStatus(responseObj.STATUS);
            }
        }
    }

    addParam = responseObj => {
        let resObj = responseObj;
        if(resObj.PAYMENT_FlOW == undefined && resObj.PAYMENT_FlOW !== "ADDANDPAY") {
            resObj.closeIframe = true;
        }

        return resObj;
    }

    updateStatus = (status) => {
        if(status == "AUTHENTICATION_FAILED" || status == "Cancelled" || status == "Invalid") {
            this.setState({statusResult: "Payment Failed"});
            this.setState({cardBox: "failedMsg"});
        } else if(status == "Captured" || status == "Success") {
            this.setState({statusResult: "Payment Successful"});
            this.setState({cardBox: "successMsg"});
        } else if(status == "Pending") {
            this.setState({statusResult: "Payment Pending"});
            this.setState({cardBox: "pendingMsg"});
        } else {
            this.setState({statusResult: "Payment Failed"});
            this.setState({cardBox: "failedMsg"});
        }        
    }

    dialogCloseHandler = () => {
        this.setState({isDialogOpen: false});
    }

    downloadPdfHandler = () => {
        this.downloadForm.current.submit();
    }

    render() {
        // LOADER
        let loaderToRender = null;
        if(this.state.showLoader) {
            loaderToRender = <Loader processing={false} approvalNotification={false} />
        }

        // MAIN CONTENT
        let mainContent = <Loader processing={false} approvalNotification={false} />;
        if(this.state.data !== null) {
            if(this.state.data == "error") {
                mainContent = <ErrorPage />
            } else {
                mainContent = (
                    <>
                        <div className="container custom-container" id="response-container">
                            <div className="bg-grey-secondary p-15 pb-0 p-lg-60 pb-lg-0 border-radius-20 box-shadow-primary border-primary">
                                <ResponseHeader klass={this} />
                        
                                <ResponseContent
                                    cardBox={this.state.cardBox}
                                    statusResult={this.state.statusResult}
                                    data={this.state.data}
                                />
                        
                                <ResponseFooter />
                            </div>		
                        </div>

                        <form action="downloadPDF" method="POST" ref={this.downloadForm}>
                            <input type='hidden' name='ORDER_ID' value={this.state.data.ORDER_ID} />
                        </form>
                    </>
                );
            }
        }

        let dialogBox = null;
        if(this.state.isDialogOpen) {
            dialogBox = <Dialog dialogType={this.state.dialogType} onClose={this.dialogCloseHandler}>{ this.state.dialogMsg }</Dialog>
        }
        
        return (
            <>
                { mainContent }
                { dialogBox }
                { loaderToRender }
            </>
        );
    }
}

export default Response;