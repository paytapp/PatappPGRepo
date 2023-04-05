import { Component } from "react";
import Loader from "../Loader/Loader";
import ErrorPage from "../ErrorPage/ErrorPage";
import './invoicePay.css';

class InvoicePayPage extends Component {
  state = {
      data: null,
      showLoader: false,
  };

  componentDidMount() {
    try {
      var responseObj = window.id("sessionObj").value;
      responseObj = JSON.parse(responseObj);
      
      this.setState({data: responseObj});

      if(responseObj.mop == "PG_QR") {
        document.getElementById("invoicePayForm").submit();
      }
    } catch(e) {
        this.setState({data: "error"});
        console.error(e);
    }
  }

  loaderHandler = (e) => {
    e.preventDefault();

    this.setState({showLoader: true});

    document.getElementById("invoicePayForm").submit();
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
        // LOGO
        let logo = <img src={`${window.basePath}/img/logo.png`} alt="" />;
        if(this.state.data.logo !== null && this.state.data.logo !== "" && this.state.data.logo !== undefined) {
          logo = <img src={this.state.data.logo} alt="" style={{maxHeight: '60px'}} />;
        }

        // UDF11
        let udf11 = null;
        if(this.state.data.UDF11 !== undefined) {
          udf11 = (
            <div className="invoice_cell_wrapper w-25 product-name udf_row_one">
              <div className="invoice_cell sky-shade">UDF 11</div>
              <div className="invoice_cell invoice_cell_data">{ this.state.data.UDF11 }</div>
            </div>
          )
        }

        // UDF12
        let udf12 = null;
        if(this.state.data.UDF12 !== undefined) {
          udf12 = (
            <div className="invoice_cell_wrapper w-25 description-div udf_row_one">
              <div className="invoice_cell sky-shade">UDF 12</div>
              <div className="invoice_cell invoice_cell_data">{ this.state.data.UDF12 }</div>
            </div>
          )
        }

        // UDF13
        let udf13 = null;
        if(this.state.data.UDF13 !== undefined) {
          udf13 = (
            <div className="invoice_cell_wrapper w-25 description-div udf_row_one">
              <div className="invoice_cell sky-shade">UDF 13</div>
              <div className="invoice_cell invoice_cell_data">{ this.state.data.UDF13 }</div>
            </div>
          )
        }

        // UDF14
        let udf14 = null;
        if(this.state.data.UDF14 !== undefined) {
          udf14 = (
            <div className="invoice_cell_wrapper w-25 description-div udf_row_one">
              <div className="invoice_cell sky-shade">UDF 14</div>
              <div className="invoice_cell invoice_cell_data">{ this.state.data.UDF14 }</div>
            </div>
          )
        }

        // UDF15
        let udf15 = null;
        if(this.state.data.UDF15 !== undefined) {
          udf15 = (
            <div className="invoice_cell_wrapper w-25 product-name udf_row_two">
              <div className="invoice_cell sky-shade">UDF 15</div>
              <div className="invoice_cell invoice_cell_data">{ this.state.data.UDF15 }</div>
            </div>
          )
        }

        // UDF16
        let udf16 = null;
        if(this.state.data.UDF16 !== undefined) {
          udf16 = (
            <div className="invoice_cell_wrapper w-25 product-name udf_row_two">
              <div className="invoice_cell sky-shade">UDF 16</div>
              <div className="invoice_cell invoice_cell_data">{ this.state.data.UDF16 }</div>
            </div>
          )
        }

        // UDF17
        let udf17 = null;
        if(this.state.data.UDF17 !== undefined) {
          udf17 = (
            <div className="invoice_cell_wrapper w-25 product-name udf_row_two">
              <div className="invoice_cell sky-shade">UDF 17</div>
              <div className="invoice_cell invoice_cell_data">{ this.state.data.UDF17 }</div>
            </div>
          )
        }

        // UDF18
        let udf18 = null;
        if(this.state.data.UDF18 !== undefined) {
          udf18 = (
            <div className="invoice_cell_wrapper w-25 description-div udf_row_two">
              <div className="invoice_cell sky-shade">UDF 18</div>
              <div className="invoice_cell invoice_cell_data">{ this.state.data.UDF18 }</div>
            </div>
          )
        }

        // UDF FIELDS
        let udf_fields = null;
        if(this.state.data.UDF11 !== undefined || this.state.data.UDF12 !== undefined || this.state.data.UDF13 !== undefined || this.state.data.UDF14 !== undefined || this.state.data.UDF15 !== undefined || this.state.data.UDF16 !== undefined || this.state.data.UDF17 !== undefined || this.state.data.UDF18 !== undefined) {
          udf_fields = (
            <>
              <div className="invoice-heading mt-20 mb-10 udf_common">
                <h3>User Define Fields</h3>
              </div>

              <div className="invoice_container invoice_product_table udf_common">
                {/* invoice_box STARTED */}
                <div className="invoice_box product-info udf_row">
                  { udf11 }
                  { udf12 }
                  { udf13 }
                  { udf14 }
                  { udf15 }
                  { udf16 }
                  { udf17 }
                  { udf18 }
                </div>
                {/* invoice_box ENDED */}
              </div>
            </>
          )
        }

        mainContent = (
          <>
            <form method="POST" id="invoicePayForm" action={this.state.data.invoicePayUrl}>
              <div className="invoice_wrapper">
                <div className="logo-wrapper mt-20 text-center mb-20">
                  { logo }                    
                </div>

                <div className="invoice_container invoice_cust_table">
                  {/* invoice_box STARTED */}
                  <div className="invoice_box">
                    <div className="invoice_cell_wrapper w-25">
                      <div className="invoice_cell sky-shade">Invoice Id</div>						
                      <div className="invoice_cell">{ this.state.data.invoiceId }</div>
                    </div>

                    <div className="invoice_cell_wrapper w-25">
                      <div className="invoice_cell sky-shade">Name</div>						
                      <div className="invoice_cell">{ this.state.data.name }</div>
                    </div>

                    <div className="invoice_cell_wrapper w-25">
                      <div className="invoice_cell sky-shade">City</div>
                      <div className="invoice_cell">{ this.state.data.city }</div>
                    </div>
                    
                    <div className="invoice_cell_wrapper w-25">
                      <div className="invoice_cell sky-shade">Country</div>
                      <div className="invoice_cell">{ this.state.data.country }</div>
                    </div>
                  </div>
                  {/* invoice_box ENDED */}

                  {/* invoice_box STARTED */}
                  <div className="invoice_box">
                    <div className="invoice_cell_wrapper w-25">
                      <div className="invoice_cell sky-shade">State</div>
                      <div className="invoice_cell">{ this.state.data.state }</div>
                    </div>
                    
                    <div className="invoice_cell_wrapper w-25">
                      <div className="invoice_cell sky-shade">Zip</div>
                      <div className="invoice_cell">{ this.state.data.zip }</div>
                    </div>
                    
                    <div className="invoice_cell_wrapper w-25">
                      <div className="invoice_cell sky-shade">Phone</div>
                      <div className="invoice_cell">{ this.state.data.phone }</div>
                    </div>
                    
                    <div className="invoice_cell_wrapper w-25">
                      <div className="invoice_cell sky-shade">Email</div>
                      <div className="invoice_cell">{ this.state.data.email }</div>
                    </div>					
                  </div>
                  {/* invoice_box ENDED */}

                  {/* invoice_box STARTED */}
                  <div className="invoice_box">
                    <div className="invoice_cell_wrapper w-100">
                      <div className="invoice_cell sky-shade w-100">Address</div>						
                      <div className="invoice_cell w-100">{ this.state.data.address }</div>
                    </div>					
                  </div>
                  {/* invoice_box ENDED */}
                </div>

                {/* PRODUCT HEADING STARTED */}
                <div className="invoice-heading mt-20 mb-10">
                  <h3>Product Information</h3>
                </div>
                {/* PRODUCT HEADING ENDED */}


                <div className="invoice_container invoice_product_table">
                  {/* invoice_box STARTED */}
                  <div className="invoice_box product-info">
                    <div className="invoice_cell_wrapper w-25 product-name">
                      <div className="invoice_cell sky-shade">Name</div>
                      <div className="invoice_cell">{ this.state.data.productName }</div>
                    </div>
                    
                    <div className="invoice_cell_wrapper w-50 description-div">
                      <div className="invoice_cell sky-shade">Description</div>
                      <div className="invoice_cell">{ this.state.data.productDesc }</div>
                    </div>
                    
                    <div className="invoice_cell_wrapper w-25 quantity-div">
                      <div className="invoice_cell sky-shade">Quantity</div>
                      <div className="invoice_cell">{ this.state.data.quantity }</div>
                    </div>					
                  </div>
                  {/* invoice_box ENDED */}

                  {/* invoice_box STARTED */}
                  <div className="invoice_box duration-div">
                    <div className="invoice_cell_wrapper price-div w-33">
                      <div className="invoice_cell sky-shade price-div">All prices are in</div>
                      <div className="invoice_cell">{ this.state.data.currencyName }</div>
                    </div>
                    
                    <div className="invoice_cell_wrapper w-33">
                      <div className="invoice_cell sky-shade">Expire on</div>
                      <div className="invoice_cell">{ this.state.data.expiresDay }</div>
                    </div>
                    
                    <div className="invoice_cell_wrapper w-33">
                      <div className="invoice_cell sky-shade">Duration</div>
                      <div className="invoice_cell">
                        { this.state.data.durationFrom } <span className="text-bold">To</span> { this.state.data.durationTo }
                      </div>
                    </div>					
                  </div>
                  {/* invoice_box ENDED */}

                  {/* invoice_box STARTED */}
                  <div className="invoice_box amount-div">
                    <div className="invoice_cell_wrapper w-33">
                      <div className="invoice_cell sky-shade">Amount</div>
                      <div className="invoice_cell">{ Number(this.state.data.amount).toFixed(2) }</div>
                    </div>
                    
                    <div className="invoice_cell_wrapper w-33">
                      <div className="invoice_cell sky-shade">Service Charge</div>
                      <div className="invoice_cell">{ this.state.data.serviceCharge }</div>
                    </div>
                    
                    <div className="invoice_cell_wrapper w-33 total-amount">
                      <div className="invoice_cell sky-shade">Total Amount</div>
                      <div className="invoice_cell">{ (Number(this.state.data.totalAmount) / 100).toFixed(2) }</div>
                    </div>					
                  </div>
                  {/* invoice_box ENDED */}

                </div>

                { udf_fields }                

                {
                  this.state.data.enablePay == "TRUE" ?
                  <div className="invoice_pay_button text-center mt-20">
                    <button type="button" onClick={this.loaderHandler} className="primary-btn" id="btnPay">Pay Now</button>
                  </div> : <div id="lblMsg" className="mt-20 text-red text-center">Link has been expired!</div>
                }
                
              </div>

              <input type="hidden" name="PAY_ID" value={this.state.data.payId} />
              <input type="hidden" name="ORDER_ID" value={this.state.data.invoiceId} />
              <input type="hidden" name="AMOUNT" value={this.state.data.totalAmount} />
              <input type="hidden" name="TXNTYPE" value="SALE"/>
              <input type="hidden" name="CUST_NAME" value={this.state.data.name} />
              <input type="hidden" name="CUST_STREET_ADDRESS1" value={this.state.data.address} />
              <input type="hidden" name="CUST_ZIP" value={this.state.data.zip} />
              <input type="hidden" name="CUST_PHONE" value={this.state.data.phone} />
              <input type="hidden" name="CUST_EMAIL" value={this.state.data.email} />
              <input type="hidden" name="PRODUCT_DESC" value={this.state.data.productDesc} />
              <input type="hidden" name="CURRENCY_CODE" value={this.state.data.currencyCode} />
              <input type="hidden" name="RETURN_URL" value={this.state.data.returnUrl} />
              <input type="hidden" name="SUB_MERCHANT_ID" value={this.state.data.subMerchantId} />
              <input type="hidden" name="QUANTITY" value={this.state.data.quantity} />
              <input type="hidden" name="PRODUCT_NAME" value={this.state.data.productName} />
              <input type="hidden" name="HASH" value={this.state.data.hash} />

              {this.state.data.UDF11 !== undefined ? <input type="hidden" name="UDF11" value={this.state.data.UDF11} /> : null }              
              {this.state.data.UDF12 !== undefined ? <input type="hidden" name="UDF12" value={this.state.data.UDF12} /> : null }              
              {this.state.data.UDF13 !== undefined ? <input type="hidden" name="UDF13" value={this.state.data.UDF13} /> : null }              
              {this.state.data.UDF14 !== undefined ? <input type="hidden" name="UDF14" value={this.state.data.UDF14} /> : null }              
              {this.state.data.UDF15 !== undefined ? <input type="hidden" name="UDF15" value={this.state.data.UDF15} /> : null }              
              {this.state.data.UDF16 !== undefined ? <input type="hidden" name="UDF16" value={this.state.data.UDF16} /> : null }              
              {this.state.data.UDF17 !== undefined ? <input type="hidden" name="UDF17" value={this.state.data.UDF17} /> : null }              
              {this.state.data.UDF18 !== undefined ? <input type="hidden" name="UDF18" value={this.state.data.UDF18} /> : null }              
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

export default InvoicePayPage;