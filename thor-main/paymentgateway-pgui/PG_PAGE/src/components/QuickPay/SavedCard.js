import { useEffect } from "react";
import { addConvenienceFee, isNumberKey, numberEnterPhone, toolTipCvvHandler } from "../../js/script";
import CvvInfo from "../CvvInfo/CvvInfo";
import DeleteButton from "./DeleteButton";

const SavedCard = props => {
    const disableEnterPress = (e) => {
        var key;
        if (window.event)
            key = window.event.keyCode; // IE
        else
            key = e.which; // firefox
        return (key !== 13);
    }

    const enableExButton = (event) => {
        let that = event.target,
            _exSubmit = window.id('pay-now');

        if (window.id("cardSupportedEX").innerHTML === "This card is no longer supported") {
            _exSubmit.classList.add("btn-disabled");
        } else {
            if (that.value.length === that.maxLength) {
                _exSubmit.classList.remove("btn-disabled");
            } else {
                _exSubmit.classList.add("btn-disabled");
            }
        }
    }

    useEffect(() => {
        if(props.activeClass !== "") {
            addConvenienceFee("SC");
            window.id("pay-now").classList.add("btn-disabled");
        }
    }, []);

    return (
        <li className={`saveCardDetails flex-wrap ${props.isActive}`} id={`tokenid-${props.elementKey}`} data-type="SC" onClick={props.handleClick}>
            <input type="hidden" className="payment-type" value={props.paymentType} />
            <input type="hidden" className="expiryDate" value={props.expiryDate} />
            <input type="hidden" className="mop-type" value={props.mopType} />
            <input type="hidden" className="cardholder-type" value={props.cardHolderType} />
            <input type="hidden" className="payments-region" value={props.paymentsRegion} />

            <div className="card-dtls d-flex flex-column w-100 p-10 p-md-18">
                <div className="d-flex align-items-center">
                    <div className="mop-img h-md-31">
                        <img src={window.basePath + "/img/" + props.mopType.toLowerCase() + ".png"} alt="" className="img-fluid" />
                    </div>
                    <div className="d-md-none ml-10 font-size-12 m-card-mask">
                        {props.cardMask}
                    </div>
                </div>

                <div className="d-flex d-md-block justify-content-between">
                    <div className="w-save-card">
                        <label className="custom-control-label d-none" htmlFor={"tokenId" + props.elementKey}>
                            <input
                                className="custom-control-input visaRadio"
                                type="radio"
                                name="tokenId"
                                id={"tokenId" + props.elementKey}
                                checked={props.isActive}
                                value={props.elementKey} />
                        </label>

                        <div className="card-expired-error font-size-14 mt-10">Card Expired</div>

                        <small className="save-payment-type font-size-12 font-size-md-16">
                            {props.paymentType === 'CC' ? "Credit Card" : "Debit Card"}
                        </small>

                        <small className="save-issuer-bank font-size-10 font-size-md-12">
                            {props.cardIssuerBank !== null && props.cardIssuerBank !== 'NA' ? props.cardIssuerBank : null}
                        </small>
                        
                        <div className="position-relative d-none d-md-block">
                            <div className="font-size-12 font-weight-medium save-card-number">
                                {props.cardMask}
                            </div>
                            <input type="hidden"
                                name="cardNumber"
                                autoComplete="off"
                                id={"exCardNumber" + props.elementKey}
                                className="form-control1 transparent-input font-size-13 font-size-sm-18 font-weight-medium bg-none border-none"
                                value={props.cardMask}
                                theme="simple"
                                readOnly={true}
                            />
                        </div>
                    </div>
                    
                    <div className="savedCvv cvv-info mt-5 d-flex align-items-end">
                        <div className="position-relative">
                            <input
                                autoComplete="new-password"
                                type="text"
                                inputMode="numeric"
                                name="cvvNumber"
                                id={"ccvvNumber" + props.elementKey}
                                maxLength="3"
                                onInput={isNumberKey}
                                onKeyPress={e => {isNumberKey(e); disableEnterPress(e);}}
                                onKeyUp={e => {enableExButton(e); numberEnterPhone(e);}}
                                onChange={props.handleUserInput}
                                className="savDetailsCvv"
                                placeholder="CVV"
                                onPaste={e => false}		
                                onDrop={e => false}
                                value={props.inputValue}
                            />
                            <img className="info d-none d-sm-block" onMouseOver={e => toolTipCvvHandler(e, 'block')} onMouseOut={e => toolTipCvvHandler(e, 'none')} src={window.basePath + '/img/info.png'} alt="" />
                        </div>

                        <CvvInfo />
                    </div>
                </div>

                <DeleteButton
                    elementKey={props.elementKey}
                    deleteHandler={props.deleteHandler}
                    action="deleteCard"
                />
            </div>
            <p className="d-none text-danger text-center w-100 mb-0 mt-5 font-size-12 card-expiry-error">Card expiry due in <span className="expiration-days"></span> days.</p>

            <div className="d-none text-danger w-100 font-size-12 saved-cvv-error"></div>
        </li>
    );
}

export default SavedCard;