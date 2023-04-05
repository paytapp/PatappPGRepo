import React, { useEffect, useState } from "react";
import { startWorker } from "../../js/script";
// import Loader from "../Loader/Loader";
import Fade from 'react-reveal/Fade';
import TitleSection from "../TitleSection/TitleSection";

const ScanImage = props => {
    const [loader, setloader] = useState(true),
          [qrCode, setqrCode] = useState(null);

    const payload = {
        "payId": Window.pageInfoObj.PAY_ID,
        "CHECKOUT_JS_FLAG": Window.pageInfoObj.checkOutJsFlag,
        "encSessionData": Window.pageInfoObj.encSessionData
    }

    useEffect(() => {
        fetch(`${Window.baseUrl}/upiQrPay`, {
            method : "POST",
            body: JSON.stringify(payload),
            headers : { 
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }      
        })
        .then((response) => response.json())
        .then((responseJson) => {
            updateQrImage(responseJson);            
        })
        .catch((error) => {
            console.error(error);
        });
    }, []);

    const updateQrImage = (obj) => {
        let responseCode = "",
            data = new FormData(),
            myMap = new Map(),
            _transStatus = "";

        if(null !== obj) {
            _transStatus = obj.transactionStatus;
            Window.oid = obj.oid;
            responseCode = obj.responseCode;
            myMap = obj.responseFields;
        }

        if(obj.responseCode == "000" && obj.responseMessage == "SUCCESS") {
            if(obj.upiQrCode !== undefined) {
				setqrCode(obj.upiQrCode);

				startWorker({
					oid: Window.oid,
					requestType: "UPI_QR"
				});

                props.loaderHandler({showLoader: false});
			}
		} else {
			alert("Something went wrong!");
		}
    }

    return (
        <React.Fragment>
            <div className="toggle-content pl-30 pl-sm-40 pr-15">
                <div className="row mb-10">
                    {/* <div className="col-md-5 mb-10 d-flex align-items-center justify-content-center">
                        <span className="pg-icon icon-upi-qr font-size-50 font-sm-size-70">
                            <span className="path1"></span>
                            <span className="path2"></span>
                            <span className="path3"></span>
                            <span className="path4"></span>
                            <span className="path5"></span>
                            <span className="path6"></span>
                            <span className="path7"></span>
                            <span className="path8"></span>
                            <span className="path9"></span>
                            <span className="path10"></span>
                            <span className="path11"></span>
                            <span className="path12"></span>
                            <span className="path13"></span>
                            <span className="path14"></span>
                            <span className="path15"></span>
                            <span className="path16"></span>
                            <span className="path17"></span>
                            <span className="path18"></span>
                            <span className="path19"></span>
                            <span className="path20"></span>
                            <span className="path21"></span>
                            <span className="path22"></span>
                            <span className="path23"></span>
                            <span className="path24"></span>
                            <span className="path25"></span>
                            <span className="path26"></span>
                            <span className="path27"></span>
                            <span className="path28"></span>
                            <span className="path29"></span>
                        </span>
                    </div> */}
                    <div className="col-md-7">
                        <div id="upi-qr-img" className="text-center"><img src={"data:image/png;base64," + qrCode} className="img-fluid" alt="" /></div>
                        <div className="text-center font-size-14 lang" data-key="upiQrText">(Please scan UPI QR using any UPI App)</div>
                    </div>
                </div>

                <form name="upiResponseSubmitForm" id="upiResponseSubmitForm" target="_self" method="POST" action={`${Window.baseUrl}/upiResponse`}>
                    <input type="hidden" name="PG_REF_NUM" id="resPgRefNum" value="" />
                    <input type="hidden" name="RETURN_URL" id="resReturnUrl" value="" />
                    <input type="hidden" name="CHECKOUT_JS_FLAG" value={props.checkOutJsFlag} />
                </form>

                {/* <div className="row">
                    <div className="col-md-6 card_charges d-none pl-md-0">
                        <span className="mr-10"><img src={Window.basePath + '/img/info.png'} alt="" />&nbsp;&nbsp;</span>
                        <span id="UPI_QR-tax-declaration"></span>
                    </div>
                </div> */}

                {/* <div className="col-md-12 submit-btns-tab mb-md-15 mb-lg-0 d-md-none align-items-center justify-content-end" id="submit-btns-upiQr"></div> */}
            </div>

            {/* {loader ? <Loader processing={false} approvalNotification={false} /> : null} */}
        </React.Fragment>
    );
}

const UpiQrBox = props => {
    let upiQrBox = (
        <div className="toggle-list-box">
            <div className={`toggle-list ${props.isActive || props.isActive == undefined ? 'active' : ''}`} data-type="UPI_QR">
                <span className="font-size-14 d-inline-block mb-5 lang" data-key="payUsingQr">PAY USING QR CODE</span>
                <button className="d-flex toggle-box pr-15 pt-10 w-100" onClick={props.upiToggleListHandler}>
                    <span className="font-size-20 pg-icon icon-qr-code mr-sm-10 ml-6"></span>
                    <span className="d-flex flex-column w-100">
                        <span className="d-flex w-100 justify-content-between">
                            <span className="font-size-14 font-weight-bold toggle-title">QR Code</span>

                            {props.upiIconHandler(props.isActive)}
                        </span>
                        <span className="d-block font-size-12">Scan the QR Code using your UPI app</span>
                    </span>
                </button>

                {props.isActive || props.isActive == undefined ? <ScanImage loaderHandler={props.loaderHandler} checkOutJsFlag={props.data !== undefined ? props.data.checkOutJsFlag : props.checkOutJsFlag} /> : null }
            </div>
        </div>
    );

    return (
        <>  
            {
                props.isActive || props.isActive == undefined ?
                <TitleSection title={props.title} navigationHandler={props.navigationHandler} btnDataId="navigation" />
                : null
            }      

            <Fade bottom>
                <React.Fragment>
                    { props.isActive || props.isActive == undefined ?
                        <div className="container custom-container mt-170 mb-60">
                            <div className="tabBox upiBox" id="upi">
                                { upiQrBox }
                            </div>
                        </div>
                    : upiQrBox }
                </React.Fragment>
            </Fade>
        </>
    );
}

export default UpiQrBox;