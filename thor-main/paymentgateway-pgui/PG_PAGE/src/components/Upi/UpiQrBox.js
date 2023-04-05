import React, { useEffect, useState, useCallback } from "react";
import Loader from "../Loader/Loader";
import { startWorker } from "../../js/script";
import Declaration from "../Declaration/Declaration";

const ScanImage = props => {
    const [loader, setloader] = useState(true),
        [qrCode, setqrCode] = useState(null),
        [isQrCodeImage, setisQrCodeImage] = useState(false);

    const fetchQrImage = useCallback(() => {
        const updateQrImage = (obj) => {
            setloader(false);
            
            if(null !== obj) {
                window.oid = obj.oid;
            }
    
            if(obj.responseCode === "000" && obj.responseMessage === "SUCCESS") {
                if(obj.upiQrCode !== undefined && obj.upiQrCode !== null) {
                    window.upiQrCodeResponse = obj;
    
                    setqrCode(obj.upiQrCode);
    
                    startWorker({
                        oid: window.oid,
                        requestType: "UPI_QR"
                    });
    
                    setisQrCodeImage(true);
                } else {
                    setisQrCodeImage(false);
                }
            } else {
                setisQrCodeImage(false);
            }
        }

        if(window.mqrCodeResponse === "") {
            const payload = {
                "payId": window.pageInfoObj.PAY_ID
            }
    
            setloader(true);
    
            fetch(`${window.basePath}/jsp/upiQrPay`, {
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
                setloader(false);
            })
            .catch((error) => {
                setloader(false);
                console.error(error);
            });
        } else {
            updateQrImage(window.mqrCodeResponse);
        }        
    }, []);

    useEffect(() => {
        fetchQrImage();
    }, [fetchQrImage]);

    let qrCodeImage = <img src={"data:image/png;base64," + qrCode} className="img-fluid" alt="" />;
    if(!isQrCodeImage) {
        qrCodeImage = (
            <div className="dummy-qr-code position-relative bg-white p-10">
                <img src={`${window.basePath}/img/dummy-qr-code.jpg`} alt="" className="img-fluid" />
                <button className="qr-refresh-wrapper position-absolute d-flex flex-column" onClick={fetchQrImage}>
                    <i className="pg-icon icon-refresh mr-5 font-size-16"></i>
                    <span className="font-size-10 mt-10">CLICK TO RELOAD QR CODE</span>
                </button>
            </div>
        );
    }

    return (
        <>
            <div className="toggle-content pl-30 pl-sm-40 pr-15">
                <div className="row mb-10">
                    <div className="col-md-5 mb-10 d-flex align-items-center justify-content-center">
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
                    </div>
                    <div className="col-md-7">
                        <div id="upi-qr-img" className="text-center">
                            { qrCodeImage }
                        </div>

                        <div className="text-center font-size-14 lang" data-key="upiQrText">(Please scan UPI QR using any UPI App)</div>
                    </div>
                </div>

                {/* <div className="row">
                    <div className="col-md-6 card_charges d-none pl-md-0">
                        <span className="mr-10"><img src={window.basePath + '/img/info.png'} alt="" />&nbsp;&nbsp;</span>
                        <span id="UPI_QR-tax-declaration"></span>
                    </div>
                </div>

                <div className="col-md-12 submit-btns-tab mb-md-15 mb-lg-0 d-md-none align-items-center justify-content-end" id="submit-btns-upiQr"></div> */}

                <Declaration
                    klass={props.klass}
                    id="common-tax-declaration"
                    btnId="submit-btns-upiQr"
                    submitHandler={props.submitHandler}
                    cancelHandler={props.cancelHandler}
                    dataObj={props.dataObj}
                    className="mt-10 justify-content-center"
                />
            </div>

            {loader ? <Loader processing={false} approvalNotification={false} /> : null}
        </>
    );
}

const UpiQrBox = props => {
    return (        
        <div className={`toggle-list ${props.isActive ? 'active' : ''}`} data-type="UPI_QR">
            <span className="font-size-12 d-inline-block mb-5 lang" data-key="payUsingQr">PAY USING QR CODE</span>
            <button className="d-flex toggle-box pr-15 pt-10 w-100" onClick={props.clickHandler}>
                <span className="font-size-20 pg-icon icon-qr-code mr-sm-10 ml-6"></span>
                <span className="d-flex flex-column w-100">
                    <span className="d-flex w-100 justify-content-between">
                        <span className="font-size-12 font-weight-bold toggle-title">QR Code</span>

                        {props.iconToggleHandler(props.isActive)}
                    </span>
                    <span className="d-block font-size-12">Scan the QR Code using your UPI App</span>
                </span>
            </button>

            {props.isActive ? <ScanImage /> : null }
        </div>
    );
}

export default UpiQrBox;