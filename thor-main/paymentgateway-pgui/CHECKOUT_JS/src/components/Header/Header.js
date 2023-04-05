import "./Header.css";

const Header = props => {
    // LOGO IMAGE
    let logoImg = null;
    if(props.data.merchantLogoFlag && props.data.merchantLogoFlag !== undefined && props.data.merchantLogoFlag !== null && props.data.encodedLogoImage !== "" && props.data.encodedLogoImage !== null && props.data.encodedLogoImage !== undefined) {
        logoImg = (<img src={`data:image/png;base64,${props.data.encodedLogoImage}`} className="custom-logo" alt={props.data.merchantLogoName} />);
    } else {
        logoImg = (<img src={Window.basePath + '/img/logo.png'} alt="Payment Gateway" />);
    }

    // AMOUNT    
    let amount = null;
    if(window.name == "add-and-pay") {
        amount = window.parent.document.getElementById("amountToAdd").value;
    } else {
        if(props.totalAmount !== null) {
            amount = props.totalAmount;
        } else {
            amount = (props.data.AMOUNT / 100).toFixed(2);
        }
    }

    return (
        <>
            <div className="container-fluid bg-primary py-15 position-fixed top-0 z-index-999">
                <div className="container custom-container">
                    <div className="row">
                        <div className="col-12 d-flex flex-wrap">
                            <div className="Header-logo bg-white p-15 d-flex align-items-center justify-content-center border-radius-primary">
                                { logoImg }
                            </div>

                            <div className="Header-content d-flex flex-column justify-content-between ml-15 text-white">
                                <div>
                                    <div className="Header-merchant">{props.data.merchantType}</div>
                                    <div className="Header-orderId font-size-11">Order ID: {props.data.ORDER_ID}</div>
                                </div>
                                <div className="font-weight-medium"><i className="pg-icon icon-inr"></i> { amount }</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <button type="button" className="border-none bg-none position-fixed text-white right-0 top-0 pr-10 pt-10 font-size-12 z-index-999" onClick={props.cancelHandler}><i className="pg-icon icon-cancel-cross"></i></button>
        </>
    );
}

export default Header;