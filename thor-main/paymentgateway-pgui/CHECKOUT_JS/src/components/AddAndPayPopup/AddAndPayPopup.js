import React, { Component } from "react";
import Loader from "../Loader/Loader";
import ErrorPage from '../ErrorPage/ErrorPage';

class AddAndPayPopup extends Component {
    constructor(props) {
        super(props);
  
        this.state = {
            data : null
        }
    };

    componentDidMount() {
        try {
            try {
                let responseJson = JSON.parse(Window.id("sessionObj").value),
                    pageData = JSON.parse(responseJson.suportedPaymentTypeMap),
                    userData = responseJson.userData;

                delete responseJson.suportedPaymentTypeMap;
                delete responseJson.userData;

                const dataObj = {...responseJson, ...pageData, ...userData};
                
                this.setState({ data : dataObj });
                Window.pageInfoObj = dataObj;

                window.parent.postMessage({"isPageVisible" : true}, "*");
            } catch(err) {
                this.setState({data: "error"});
                console.error(err);
            }
        } catch(err) {
            this.setState({data: "error"});
            console.error(err);
        }
    }

    render() {
        let mainContent = <Loader processing={false} approvalNotification={false} />;
        
        if(this.state.data !== null) {
            if(this.state.data == "error") {
                mainContent = <ErrorPage />
            } else {
                mainContent = (
                    <>
                        <h1>Hello Checkout Js</h1>
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

export default AddAndPayPopup;