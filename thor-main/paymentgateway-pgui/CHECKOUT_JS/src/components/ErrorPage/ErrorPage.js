import React from "react";
import "./ErrorPage.css";

const ErrorPage = _ => {
    return (
        <React.Fragment>
            <header className="bg-primary py-15 position-fixed top-0 left-0 w-100">
                <div className="container">
                    <div className="row">
                        <div className="col-12">
                            <h1 className="m-0"><img src={window.basePath + '/img/white-logo.png'} alt="" /></h1>
                        </div>                        
                    </div>                    
                </div>
            </header>

            <div className="container container-error-page d-flex align-items-center justify-content-center">
                <div className="row">
                    <div className="col-12">
                        <div className="error-content text-center">
                            <h1 className="text-black font-weight-medium font-size-55 mb-10">Oops!</h1>
                            <p className="text-primary-lightest font-size-30 font-weight-normal">We can't find the page you are looking for.</p>
                        </div>
                    </div>                    
                </div>                
            </div>            
        </React.Fragment>
    );
}

export default ErrorPage;