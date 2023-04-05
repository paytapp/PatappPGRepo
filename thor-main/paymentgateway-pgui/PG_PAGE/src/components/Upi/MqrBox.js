import React, { useEffect, useState } from "react";
import Base from "../../utils/Base";
import Loader from "../Loader/Loader";
import { isNumberKey, startWorker } from "../../js/script";
import CancelButton from "../CancelButton/CancelButton";
import { useRef } from "react";
import auxillary from "../../hoc/auxiliary";
import { useCallback } from "react";


const ScanImage = props => {
    const [loader, setloader] = useState(true),
        [qrCode, setqrCode] = useState(null),
        [isQrCodeImage, setisQrCodeImage] = useState(false),
        [useVPA, setVPA] = useState(null),
        [isBtnDisabled, setIsBtnDisabled] = useState(true),
        [isUtrError, setIsUtrError] = useState(false);

    const utrNumberRef = useRef();
    
    const utrSubmitHandler = evt => {
        evt.preventDefault();

        const utrNumber = utrNumberRef.current.value;

        if(utrNumber.length === 12) {
            const payload = {
                "utrNumber": utrNumber
            }

            setloader(true);

            fetch(`${window.basePath}/jsp/submitUtr`, {
                method: 'POST',
                body: JSON.stringify(payload),
                headers : {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json',
                }
            })
            .then((response) => response.json())
            .then((responseJson) => {
                window.oid = responseJson.PG_REF_NUM;

				startWorker({
					oid: window.oid,
					requestType: "MQR"
				});
            })
            .catch((error) => {
                setloader(false);
                console.error(error);
            });
        } else {
            setIsUtrError(true);
        }
    }

    const removeErrorMessage = evt => {
        const that = evt.target;

        if(that.value !== "") {
            setIsUtrError(false);
        }
    }

    const utrValidateHandler = (evt) => {
        const that = evt.target;

        if(that.value.length === 12) {
            setIsBtnDisabled(false);
        } else {
            setIsBtnDisabled(true);
        }
    }

    const fetchQrImage = useCallback(() => {
        const updateQrImage = (obj) => {
            setloader(false);
    
            if(obj.responseCode === "000") {
                if(obj.mqrQrCode !== undefined && obj.mqrQrCode !== null) {
                    window.mqrCodeResponse = obj;

                    const TOTAL_AMT = (Number(obj.totalAmount) / 100).toFixed(2);

                    if(window.id("new_head") !== null) {
                        window.id("new_head").querySelector(".value-block").innerHTML = TOTAL_AMT;
                    }
                    
                    if(window.id("pay-now") !== null) {
                        window.id("pay-now").querySelector(".value-block").innerHTML = TOTAL_AMT;	
                    }
    
                    // props.klass.setState({TOTAL_AMT: (Number(obj.totalAmount) / 100).toFixed(2)});
    
                    setqrCode(obj.mqrQrCode);
                    setVPA(obj.mqrVpa);
    
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

            fetch(`${window.basePath}/jsp/qrPay`, {
                method : "POST",
                body: JSON.stringify(payload),
                headers : {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json',
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
    }, [props.klass]);

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

    let vpaToDisplay = null;
    if(useVPA !== null) {
        vpaToDisplay = (
            <div className="font-size-14">
                <strong>VPA</strong>: { useVPA }
            </div>
        );
    }

    let utrErrorToDisplay = null;
    if(isUtrError) {
        utrErrorToDisplay = <p className="font-size-11 mb-0 position-absolute text-red top-36">Please enter valid UTR / UPI Reference No.</p>
    }

    const guideImage = (
        <div className="col-md-6 mt-15 mt-md-0 align-items-center justify-content-center d-flex">
            <img src={`${window.basePath}/img/utr-guide.png`} alt="" className="img-fluid" />
        </div>
    );

    const guideText = props.width < 768 ? 'below' : 'above';
    const guideImgDesktop = props.width > 768 ? guideImage : null;
    const guideImgMobile = props.width < 768 ? guideImage : null;    

    let utrFieldsToDisplay = null;
    const mqrResponse = window.mqrCodeResponse;
    if(window.mqrCodeResponse !== "") {
        if(mqrResponse.mqrQrCode !== undefined && mqrResponse.mqrQrCode !== null) {
    // if(window.mqrCodeResponse === "") { // remove this line after completion
    //     if(mqrResponse.mqrQrCode === undefined || mqrResponse.mqrQrCode === null) { // remove this line after completion
            utrFieldsToDisplay = (
                <div className="row">
                    <div className="col-12">
                        <label htmlFor="utrNumber" className="font-size-12 font-weight-normal mb-5 text-grey-light">UTR / UPI Reference No.</label>
                    </div>
    
                    <div className="col-md-6">
                        <div className="position-relative">
                            <input
                                type="text"
                                name="utrNumber"
                                id="utrNumber"
                                ref={utrNumberRef}
                                maxLength={12}
                                className="inputField form-control py-6"
                                onChange={e => {
                                    isNumberKey(e);
                                    utrValidateHandler(e);
                                    removeErrorMessage(e);
                                }}
                                onBlur={e => {
                                    utrValidateHandler(e);
                                }}
                                autoComplete="off"
                            />
                            { utrErrorToDisplay }
                        </div>                     
                    </div>
    
                    <div className="col-md-6 pl-md-0 d-flex align-items-center mt-15 justify-content-center mt-md-0 justify-content-md-start">
                        <button
                            type="button"
                            onClick={utrSubmitHandler}
                            className={`btn line-height-16 btn-primary border-radius-15 font-weight-bold font-size-16 py-10 px-30 ${isBtnDisabled ? 'btn-disabled' : ''}`}>
                            Submit</button>

                        {/* <CancelButton cancelHandler={props.cancelHandler} dataObj={props.dataObj} className='ml-15' /> */}
                    </div>
    
                    <div className="col-12">
                        <p className="mb-0 mt-15 font-size-12 p-15 bg-grey-primary">Please fill the reference number with a 12 digits number. To guide you there is an example {guideText}.</p>
                    </div>
    
                    { guideImgMobile }
                </div>
            )
        }
    }

    return (
        <>
            <div className="toggle-content bg-white border-none">
                <div className="row mb-10">
                    { guideImgDesktop }

                    <div className="col-md-6">
                        <div className="box-shadow-secondary border-primary h-100 py-15">
                            <div id="upi-qr-img" className="text-center">
                                { vpaToDisplay }
                                { qrCodeImage }
                            </div>
                            
                            <div className="text-center font-size-14 lang" data-key="upiQrText">(Please scan UPI QR using any UPI App)</div>
                        </div>
                    </div>
                </div>

                { utrFieldsToDisplay }
            </div>

            {loader ? <Loader processing={false} approvalNotification={false} /> : null}
        </>
    );
}

class MqrBox extends Base {
    componentDidMount() {
        this.addConvenienceFee(this.props.paymentType, this.props.klass);

        const paymentBtn = window.id("pay-now");
        if(paymentBtn !== null) {
            paymentBtn.classList.add("d-none");
        }
    }

    render() {
        return (
            <>
                <div className={`toggle-list ${this.props.isActive ? 'active' : ''}`} data-type="MQR">
                    {this.props.isActive ? <ScanImage
                        klass={this.props.klass}
                        submitHandler={this.props.submitHandler}
                        cancelHandler={this.props.cancelHandler}
                        dataObj={this.props.dataObj}
                        width={this.props.width}
                    /> : null }
                </div>
            </>
        );
    }
}

export default auxillary(MqrBox);