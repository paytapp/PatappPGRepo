import { Component } from "react";
import Loader from "../Loader/Loader";
import ErrorPage from "../ErrorPage/ErrorPage";

class CheckoutResponse extends Component {
    state = {
        data: null
    };

    componentDidMount() {
        try {
            let responseObj = window.id("sessionObj").value;
            responseObj = JSON.parse(responseObj);
            
            if(responseObj.RESPONSE_CODE !== undefined) {                
                if(window.location !== window.parent.location) {
                    responseObj.closeIframe = true;
                    window.parent.postMessage(responseObj, "*");
                    window.close();
                } else if(window.opener !== undefined) {
                    responseObj.closeIframe = true;
                    window.opener.postMessage(responseObj, "*");
                    window.close();
                }                
            } else if(responseObj.ENCDATA !== undefined) {
                window.parent.postMessage(responseObj, "*");
            } else {
                this.setState({data: "error"});
            }

        } catch(e) {
            this.setState({data: "error"});
            console.error(e);
        }
    }

    render() {
        let mainContent = <Loader processing={false} approvalNotification={false} />;
        if(this.state.data !== null) {
            if(this.state.data == "error") {
                mainContent = <ErrorPage />
            }
        }

        return (
            <>
                { mainContent }
            </>
        );
    }
}

export default CheckoutResponse;