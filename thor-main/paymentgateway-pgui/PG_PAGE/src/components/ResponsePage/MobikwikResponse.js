import React, { Component } from 'react';
import Loader from '../Loader/Loader';
import ErrorPage from '../ErrorPage/ErrorPage';
import "../../css/response.css";
import ResponseFooter from './ResponseFooter';
import ResponseHeader from './ResponseHeader';
import ResponseContent from './ResponseContent';

class MobikwikResponse extends Component {
    state = {
        data: null,
        statusResult: null,
        cardBox: null,
        timer: {
            isTextVisible: false,
            timerText: "00:05"
        },
        loader: {
            showLoader: false,
            defaultText: false,
            approvalNotification: false
        }
    }

    componentDidMount() {
        try {
            var responseObj = window.id("sessionObj").value;
            responseObj = JSON.parse(responseObj);
            
            if(responseObj.statuscode !== undefined) {
                this.setState({data: responseObj});

                this.updateStatus(responseObj.statuscode, responseObj.statusmessage);

                this.startTimerHandler();
            } else {
                this.setState({data: "error"});
            }
        } catch(e) {
            this.setState({data: "error"});
            console.error(e);
        }
    }

    updateStatus = (status, msg) => {
        this.setState({statusResult: msg});
        
        if(status == "0") {
            this.setState({cardBox: "successMsg"});
        } else {
            this.setState({cardBox: "failedMsg"});
        }
    }

    startTimerHandler = _ => {
        let sec = 5;

        this.setState(prevState => ({
            timer: {
                ...prevState.timer,
                isTextVisible: true
            }
        }));        
    
        let timeInterval = setInterval(() => {
            secpass();
        }, 1000);
    
        const secpass = _ => {
            let min     = Math.floor(sec / 60),
                remSec  = sec % 60;
            
            if (remSec < 10) {
                remSec = '0' + remSec;
            }
    
            if (min < 10) {
                min = '0' + min;        
            }      

            this.setState(prevState => ({
                timer: {
                    ...prevState.timer,
                    timerText: min + ":" + remSec
                }
            }));
            
            if (sec > 0) {            
                sec = sec - 1;
            } else {
                clearInterval(timeInterval);

                this.setState(prevState => ({
                    loader: {
                        ...prevState.loader,
                        showLoader: true
                    },
                    timer: {
                        ...prevState.timer,
                        timerText: "00:00",
                        isTextVisible: false
                    }
                }));

                const responseObj = { "status" :  true };
        
                if(window.location !== window.parent.location) {
                    window.parent.postMessage(responseObj, "*");
                } else if(window.opener !== undefined && window.opener !== null) {
                    window.opener.postMessage(responseObj, "*");
                }
                
                window.close();
            }
        }
    }

    render() {
        // LOADER
        let loader = null;    
        if(this.state.loader.showLoader) {
            loader = <Loader
                processing={this.state.loader.defaultText}
                approvalNotification={this.state.loader.approvalNotification}
            />
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
                                <ResponseHeader />
                        
                                <ResponseContent
                                    cardBox={this.state.cardBox}
                                    statusResult={this.state.statusResult}
                                    data={this.state.data}
                                    timer={this.state.timer}
                                />
                        
                                <ResponseFooter />
                            </div>		
                        </div>

                        { loader }
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

export default MobikwikResponse;