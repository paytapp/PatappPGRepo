import { Component } from "react";
import Loader from "../Loader/Loader";
import ErrorPage from "../ErrorPage/ErrorPage";
import "./EnachResponse.css";

class EnachResponse extends Component {
    state = {
        data: null,
        showLoader: false,
    }

    componentDidMount() {
        try {
            var responseObj = window.id("sessionObj").value;
            responseObj = JSON.parse(responseObj);
            
            if(responseObj.response !== undefined) {
                this.setState({data: responseObj});
            } else {
                this.setState({data: "error"});
            }
        } catch(e) {
            this.setState({data: "error"});
            console.error(e);
        }
    }

    pdfDownloadHandler = (e) => {
        e.preventDefault();

        document.getElementById("downloaPdfForm").submit();
    }

    render() {
        let loader = null;
        if(this.state.showLoader) {
            loader = <Loader processing={false} approvalNotification={false} />
        }

        // MAIN CONTENT
        let mainContent = <Loader processing={false} approvalNotification={false} />;
        if(this.state.data !== null) {
            if(this.state.data === "error") {
                mainContent = <ErrorPage />
            } else {
                mainContent = (
                    <>
                        <div className="eNACH-response">
                            <div className="eNACH-response-box">
                                <div className="eNACH-response-icon text-center">
                                    {
                                        this.state.data.STATUS === 'Captured' ?
                                            <img src={`${window.basePath}/img/success.png`} alt="" />
                                        : <img src={`${window.basePath}/img/failed.png`} alt="" />
                                    }
                                </div>
                                
                                <div className="eNACH-reponse-msg">
                                    {
                                        this.state.data.STATUS === 'Captured' ?
                                            <div className="success-msg">eNACH Registration has been completed</div>
                                        : <div className="success-msg text-red">eNACH Registration Failed</div>
                                    }
                                </div>
                                
                                <div className="eNACH-respone-data">
                                    {
                                        this.state.data.PAYMENT_TYPE === 'Net Banking' || this.state.data.PAYMENT_TYPE === 'Debit Card' ?
                                            <>
                                                <div className="eNACH-respone-data-div">
                                                    <span>Registration Mode</span>
                                                    <span>{this.state.data.PAYMENT_TYPE}</span>
                                                </div>
                                                
                                                <div className="eNACH-respone-data-div">
                                                    <span>Order ID</span>
                                                    <span>{this.state.data.ORDER_ID}</span>
                                                </div>
                                                
                                                <div className="eNACH-respone-data-div">
                                                    <span>A/c Type</span>
                                                    <span>{this.state.data.ACCOUNT_TYPE}</span>
                                                </div>
                                                
                                                <div className="eNACH-respone-data-div">
                                                    <span>A/c Number</span>
                                                    <span>{this.state.data.ACCOUNT_NO}</span>
                                                </div>
                                                
                                                <div className="eNACH-respone-data-div">
                                                    <span>A/c Holder Name</span>
                                                    <span>{this.state.data.ACCOUNT_HOLDER_NAME}</span>
                                                </div>
                                                
                                                <div className="eNACH-respone-data-div">
                                                    <span>Bank Name</span>
                                                    <span>{this.state.data.BANK_NAME}</span>
                                                </div>
                                                
                                                <div className="eNACH-respone-data-div">
                                                    <span>UMRN Number</span>
                                                    <span>{this.state.data.UMRN_NUMBER}</span>
                                                </div>
                                                
                                                <div className="eNACH-respone-data-div">
                                                    <span>Mobile Number</span>
                                                    <span>{this.state.data.CUST_PHONE}</span>
                                                </div>
                                                
                                                <div className="eNACH-respone-data-div">
                                                    <span>Email ID</span>
                                                    <span>{this.state.data.CUST_EMAIL}</span>
                                                </div>
                                                
                                                <div className="eNACH-respone-data-div">
                                                    <span>Registration Amount</span>
                                                    <span>{this.state.data.AMOUNT}</span>
                                                </div>

                                                <div className="eNACH-respone-data-div">
                                                    <span>Debit Amount ({ this.state.data.FREQUENCY })</span>
                                                    <span>{this.state.data.MAX_AMOUNT}</span>
                                                </div>

                                                <div className="eNACH-respone-data-div">
                                                    <span>Total Amount</span>
                                                    <span>{this.state.data.TOTAL_AMOUNT}</span>
                                                </div>

                                                <div className="eNACH-respone-data-div">
                                                    <span>Status</span>
                                                    <span>{this.state.data.STATUS}</span>
                                                </div>
                                                
                                                <div className="eNACH-respone-data-div">
                                                    <span>Frequency</span>
                                                    <span>{this.state.data.FREQUENCY}</span>
                                                </div>
                                                
                                                <div className="eNACH-respone-data-div">
                                                    <span>Tenure ({this.state.data.FREQUENCY})</span>
                                                    <span>{this.state.data.TENURE}</span>
                                                </div>
                                                
                                                <div className="eNACH-respone-data-div">
                                                    <span>Debit Start Date</span>
                                                    <span>{this.state.data.FROMDATE}</span>
                                                </div>
                                                
                                                <div className="eNACH-respone-data-div">
                                                    <span>Debit End Date</span>
                                                    <span>{this.state.data.TODATE}</span>
                                                </div>
                                            </>
                                        : <>
                                            <div className="eNACH-respone-data-div">
                                                <span>Status</span>
                                                <span>{this.state.data.STATUS}</span>
                                            </div>
                                            
                                            <div className="eNACH-respone-data-div">
                                                <span>Response Message</span>
                                                <span>{this.state.data.RESPONSE_MESSAGE}</span>
                                            </div>
                                        </>
                                    }
                                </div>
                                
                                <div className="download-button text-center w-100 mt-20">
                                    <button className="lpay_button lpay_button-md lpay_button-primary d-inline-block border-none" id="download_pdf" onClick={this.pdfDownloadHandler}>Download PDF</button>
                                </div>                                
                                
                                <div className="eNACH-response-progress">
                                    <div className="progess-bar"></div>
                                </div>
                            </div>
                            
                            <input type="hidden" id="logoUrl" value={this.state.data.LOGO} />

                            <form action="downloadENachRegistrationPdf" method="POST" id="downloaPdfForm">
                                <input type="hidden" id="txnId" value={this.state.data.TXN_ID} name="txnId" />
                            </form>
                        </div>
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

export default EnachResponse;