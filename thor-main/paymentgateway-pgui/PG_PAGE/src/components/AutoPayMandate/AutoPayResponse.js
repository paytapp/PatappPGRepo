import { Component } from "react";
import Loader from "../Loader/Loader";
import ErrorPage from "../ErrorPage/ErrorPage";
import "./AutoPayResponse.css";

class AutoPayResponse extends Component {
    state = {
        data: null,
        showLoader: false,
    }

    componentDidMount() {
        try {
            var responseObj = window.id("sessionObj").value;
            responseObj = JSON.parse(responseObj);
            
            if(responseObj.STATUS !== undefined) {
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
                                        this.state.data.STATUS === 'Processing' ?
                                            <img src={`${window.basePath}/img/success.png`} alt="" />
                                        : <img src={`${window.basePath}/img/failed.png`} alt="" />
                                    }
                                </div>
                                
                                <div className="eNACH-reponse-msg">
                                    {
                                        this.state.data.STATUS === 'Processing' ?
                                            <div className="success-msg">UPI AutoPay Registration has been completed</div>
                                        : <div className="success-msg text-red">UPI AutoPay Registration Failed</div>
                                    }
                                </div>
                                
                                <div className="eNACH-respone-data">
                                    {
                                        this.state.data.PAYMENT_TYPE === 'UPI' ?
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
                                                    <span>Payer Address</span>
                                                    <span>{this.state.data.PAYER_ADDRESS}</span>
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
                                                    <span>Debit Transaction Amount ({this.state.data.FREQUENCY})</span>
                                                    <span>{this.state.data.MONTHLY_AMOUNT}</span>
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
                                                    <span>Response Message</span>
                                                    <span>{this.state.data.RESPONSE_MESSAGE}</span>
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
                                                    <span>{this.state.data.DATE_FROM}</span>
                                                </div>
                                                
                                                <div className="eNACH-respone-data-div">
                                                    <span>Debit End Date</span>
                                                    <span>{this.state.data.DATE_TO}</span>
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

                                {
                                    this.state.data.RESPONSE_MESSAGE !== 'Duplicate Request' ?
                                        <div className="download-button text-center w-100 mt-20">
                                            <button className="lpay_button lpay_button-md lpay_button-primary d-inline-block" id="download_pdf" onClick={this.pdfDownloadHandler}>Download PDF</button>
                                        </div>
                                    : null

                                }
                                
                                <div className="eNACH-response-progress">
                                    <div className="progess-bar"></div>
                                </div>
                            </div>
                            
                            <input type="hidden" id="logoUrl" value={this.state.data.LOGO} />

                            <form action="downloadUpiAutoPayRegistrationPdf" method="POST" id="downloaPdfForm">
                                <input type="hidden" id="orderId" value={this.state.data.ORDER_ID} name="orderId" />
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

export default AutoPayResponse;