import { Component } from "react";
import { EmailBtn } from "../EmailBtn/EmailBtn";
import { PdfButton } from "../PdfButton/PdfButton";

class ResponseHeader extends Component {
    downloadButtonsHandler = () => {
        const { CUST_EMAIL, RETURN_URL } = this.props.klass.state.data;

        // TO SHOW EMAIL BUTTON WHEN CUST_EMAIL IS NOT EMPTY
        let emailButton = null;
        if(CUST_EMAIL !== null && CUST_EMAIL !== undefined && CUST_EMAIL !== '') {
            emailButton = <EmailBtn klass={this.props.klass} />
        }

        // TO SHOW EMAIL AND DOWNLOAD FOR EPOS AND INVOICE
        const pageType = RETURN_URL.substring(RETURN_URL.lastIndexOf('/') + 1);
        if(pageType !== "response") {
            return (
                <div className="buttonDownLoad justify-content-end">
                    <PdfButton klass={this.props.klass} />
                    { emailButton }
                </div>
            );
        }

        return null;
    }

    render() {
        return (
            <div className="row">
                <div className="col-12 d-flex align-items-center justify-content-between">
                    <div>
                        <img src={`${window.basePath}/img/logo.png`} alt="Payment Gateway Solutions Pvt. Ltd." />
                    </div>
                    { this.downloadButtonsHandler() }
                </div>                        
            </div>
        );
    }
}

export default ResponseHeader;