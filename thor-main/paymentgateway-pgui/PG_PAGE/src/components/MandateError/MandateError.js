const MandateError = (props) => {
    return (
        <>
            <div className="lpay_popup-content">
                <h3 className="responseMsg">{ props.responseMsg }</h3>
            </div>
    
            <div className="lpay_popup-button">
                <button className="lpay_button lpay_button-md lpay_button-primary cnclButton" data-text={props.dataText} id="confirmButton" onClick={props.autoPayCancelHandler}>OK</button>
            </div>
        </>
    );
}

export default MandateError;