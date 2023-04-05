import { Component } from "react";
import Loader from "../Loader/Loader";
import ErrorPage from "../ErrorPage/ErrorPage";
import "./EposPayPage.css";

class EposPayPage extends Component {
    state = {
        data: null,
        showLoader: false,
    };

    componentDidMount() {
        try {
            var responseObj = window.id("sessionObj").value;
            responseObj = JSON.parse(responseObj);
            
            this.setState({data: responseObj});
        } catch(e) {
            this.setState({data: "error"});
            console.error(e);
        }
    }

    loaderHandler = (e) => {
        e.preventDefault();

        this.setState({showLoader: true});

        document.getElementById("form-payment").submit();
    }

    render() {
        let loader = null;
        if(this.state.showLoader) {
            loader = <Loader processing={true} approvalNotification={false} />
        }

        let mainContent = <Loader processing={false} approvalNotification={false} />;
        if(this.state.data !== null) {
            if(this.state.data == "error") {
                mainContent = <ErrorPage />
            } else {
                // UDF11
                let udf11 = null;
                if(this.state.data.UDF11 !== "") {
                    udf11 = (
                        <li className="d-flex justify-content-between">
                            <span className="text-black font-weight-normal">UDF 11</span>
                            <span className="text-black line-height-18 text-right property">{ this.state.data.UDF11 }</span>
                        </li>
                    )
                }

                // UDF12
                let udf12 = null;
                if(this.state.data.UDF12 !== "") {
                    udf12 = (
                        <li className="d-flex justify-content-between">
                            <span className="text-black font-weight-normal">UDF 12</span>
                            <span className="text-black line-height-18 text-right property">{ this.state.data.UDF12 }</span>
                        </li>
                    )
                }

                // UDF13
                let udf13 = null;
                if(this.state.data.UDF13 !== "") {
                    udf13 = (
                        <li className="d-flex justify-content-between">
                            <span className="text-black font-weight-normal">UDF 13</span>
                            <span className="text-black line-height-18 text-right property">{ this.state.data.UDF13 }</span>
                        </li>
                    )
                }

                // UDF14
                let udf14 = null;
                if(this.state.data.UDF14 !== "") {
                    udf14 = (
                        <li className="d-flex justify-content-between">
                            <span className="text-black font-weight-normal">UDF 14</span>
                            <span className="text-black line-height-18 text-right property">{ this.state.data.UDF14 }</span>
                        </li>
                    )
                }

                // UDF15
                let udf15 = null;
                if(this.state.data.UDF15 !== "") {
                    udf15 = (
                        <li className="d-flex justify-content-between">
                            <span className="text-black font-weight-normal">UDF 15</span>
                            <span className="text-black line-height-18 text-right property">{ this.state.data.UDF15 }</span>
                        </li>
                    )
                }

                // UDF16
                let udf16 = null;
                if(this.state.data.UDF16 !== "") {
                    udf16 = (
                        <li className="d-flex justify-content-between">
                            <span className="text-black font-weight-normal">UDF 16</span>
                            <span className="text-black line-height-18 text-right property">{ this.state.data.UDF16 }</span>
                        </li>
                    )
                }

                // UDF17
                let udf17 = null;
                if(this.state.data.UDF17 !== "") {
                    udf17 = (
                        <li className="d-flex justify-content-between">
                            <span className="text-black font-weight-normal">UDF 17</span>
                            <span className="text-black line-height-18 text-right property">{ this.state.data.UDF17 }</span>
                        </li>
                    )
                }

                // UDF18
                let udf18 = null;
                if(this.state.data.UDF18 !== "") {
                    udf18 = (
                        <li className="d-flex justify-content-between">
                            <span className="text-black font-weight-normal">UDF 18</span>
                            <span className="text-black line-height-18 text-right property">{ this.state.data.UDF18 }</span>
                        </li>
                    )
                }

                let udf_fields = null;
                if(this.state.data.UDF11 !== "" || this.state.data.UDF12 !== "" || this.state.data.UDF13 !== "" || this.state.data.UDF14 !== "" || this.state.data.UDF15 !== "" || this.state.data.UDF16 !== "" || this.state.data.UDF17 !== "" || this.state.data.UDF18 !== "") {
                    udf_fields = (
                        <>
                            <div className="row bg-white py-10 mt-15 box-shadow-primary border border-primary rounded-primary" id="udf-fields">
                                <div className="col-12">
                                    <h3 className="font-size-16 font-weight-normal text-grey-light mb-10">User Defined Fields</h3>
                                    <ul className="list-unstyled m-0 p-0 font-size-14 list-details" id="udf-list">
                                    { udf11 }
                                    { udf12 }
                                    { udf13 }
                                    { udf14 }
                                    { udf15 }
                                    { udf16 }
                                    { udf17 }
                                    { udf18 }
                                    </ul>
                                </div>
                            </div>
                        </>
                    );
                }

                mainContent = (
                    <>
                        <form method="POST" id="form-payment" className="p-15" action={this.state.data.EPOS_SALE_PAYMENT_URL}>
                            <div className="container">
                                <div className="row bg-white py-10 box-shadow-primary border border-primary rounded-primary">
                                    <div className="col-12 text-center">
                                        <img src={`${window.basePath}/img/logo.png`} alt="Payment Gateway" />
                                    </div>

                                    <div className="col-12 text-center my-20">
                                        <span className="font-weight-normal text-grey-light font-size-14">Amount</span>
                                        <div className="font-size-26 text-grey-dark line-height-26">
                                            <i className="fa fa-inr"></i>
                                            <span className="font-weight-medium">{this.state.data.AMOUNT}</span>
                                        </div>
                                    </div>
                                </div>

                                <div className="row bg-white py-10 mt-15 box-shadow-primary border border-primary rounded-primary">
                                    <div className="col-12">
                                        <h3 className="font-size-16 font-weight-normal text-grey-light mb-10">Payment Details</h3>
                                        <ul className="list-unstyled m-0 p-0 font-size-14 list-details">					
                                            <li className="d-flex justify-content-between">
                                                <span className="text-black font-weight-normal">Invoice Id</span>
                                                <span className="text-black line-height-18 text-right">{this.state.data.INVOICE_ID}</span>
                                            </li>
                                            <li className="d-flex justify-content-between">
                                                <span className="text-black font-weight-normal">Payment Mode</span>
                                                <span className="text-black line-height-18 text-right">{this.state.data.EPOS_PAYMENT_OPTION}</span>
                                            </li>
                                            <li className="d-flex justify-content-between">
                                                <span className="text-black font-weight-normal">Requested On</span>
                                                <span className="text-black line-height-18 text-right">{this.state.data.CREATE_DATE}</span>
                                            </li>
                                            <li className="d-flex justify-content-between">
                                                <span className="text-black font-weight-normal">Request Valid till</span>
                                                <span className="text-black line-height-18 text-right">{this.state.data.EXPIRY_DATE}</span>
                                            </li>
                                        </ul>
                                    </div>
                                </div>

                                <div className="row bg-white py-10 mt-15 box-shadow-primary border border-primary rounded-primary" id="customerDetails">
                                    <div className="col-12">
                                        <h3 className="font-size-16 font-weight-normal text-grey-light mb-10">Customer Details</h3>
                                        <ul className="list-unstyled m-0 p-0 font-size-14 list-details">
                                            <li className="d-flex justify-content-between">
                                                <span className="text-black font-weight-normal">Name</span>
                                                <span className="text-black line-height-18 text-right property">{this.state.data.CUST_NAME}</span>
                                            </li>
                                            <li className="d-flex justify-content-between">
                                                <span className="text-black font-weight-normal">Email</span>
                                                <span className="text-black line-height-18 text-right property">{this.state.data.CUST_EMAIL}</span>
                                            </li>
                                            <li className="d-flex justify-content-between">
                                                <span className="text-black font-weight-normal">Mobile</span>
                                                <span className="text-black line-height-18 text-right property">{this.state.data.CUST_MOBILE}</span>
                                            </li>
                                        </ul>
                                    </div>
                                </div>

                                { udf_fields }


                                <div className="row my-15">
                                    <div className="col-12 text-center">
                                        {
                                            this.state.data.enablePay == "TRUE" ?
                                                <button type="button" onClick={this.loaderHandler} className="btn btn-primary" id="btn-pay-now">Pay Now</button>
                                            : <div id="expired-link" className="text-red font-weight-medium">This payment link has been expired!</div>
                                        }
                                    </div>
                                </div>
                            </div>

                            <input type="hidden" name="PAY_ID" value={this.state.data.PAY_ID} />
                            <input type="hidden" name="EPOS_PAYMENT_OPTION" value={this.state.data.EPOS_PAYMENT_OPTION} />
                            <input type="hidden" name="ORDER_ID" value={this.state.data.INVOICE_ID} />
                            <input type="hidden" name="AMOUNT" value={this.state.data.totalamount} />
                            <input type="hidden" name="TXNTYPE" value="SALE" />
                            <input type="hidden" name="CUST_NAME" value={this.state.data.CUST_NAME} />
                            <input type="hidden" name="CUST_STREET_ADDRESS1" value="" />
                            <input type="hidden" name="CUST_ZIP" value="" />
                            <input type="hidden" name="CUST_PHONE" value={this.state.data.CUST_MOBILE} />
                            <input type="hidden" name="CUST_EMAIL" value={this.state.data.CUST_EMAIL} />
                            <input type="hidden" name="PRODUCT_DESC" value="" />
                            <input type="hidden" name="CURRENCY_CODE" value={this.state.data.CURRENCY_CODE} />
                            <input type="hidden" name="RETURN_URL" value={this.state.data.RETURN_URL} />
                            <input type="hidden" name="HASH" value={this.state.data.hash} />

                            <input type="hidden" name="UDF11" value={ this.state.data.UDF11 } />
                            <input type="hidden" name="UDF12" value={ this.state.data.UDF12 } />
                            <input type="hidden" name="UDF13" value={ this.state.data.UDF13 } />
                            <input type="hidden" name="UDF14" value={ this.state.data.UDF14 } />
                            <input type="hidden" name="UDF15" value={ this.state.data.UDF15 } />
                            <input type="hidden" name="UDF16" value={ this.state.data.UDF16 } />
                            <input type="hidden" name="UDF17" value={ this.state.data.UDF17 } />
                            <input type="hidden" name="UDF18" value={ this.state.data.UDF18 } />
                        </form>
                    </>
                );
            }
        }

        return (
            <>
              { mainContent }
      
              { loader }
            </>
          );
    }

}

export default EposPayPage;