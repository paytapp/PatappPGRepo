import "./Loader.css";
const Loader = (props) => {
    return (
        <div className="loader loader2" id="loading2">
            <div className="w-100 vh-100 d-flex justify-content-center align-items-center flex-column">
                <div className="loaderImage">
                    <img src={window.basePath + "/img/loader.gif"} alt="Loader" />
                </div>

                {props.approvalNotification ?
                    <div id="approvedNotification" className="approvedNotification text-center">
                        <h3 className="lang" data-key="upiApprovalText">Please approve the payment in your UPI App</h3>
                        <p className="lang" data-key="upiStopRefresh">Do not refresh this page or press back button</p>
                    </div> : null
                }

                {props.processing ?
                    <div id="loading2Loader" className="defaultText mt-10 text-center">
                        <h3 className="lang" data-key="defaultLoaderText">Please wait while we process your payment...</h3>
                    </div> : null
                }
            </div>
        </div>
    );
}

export default Loader;