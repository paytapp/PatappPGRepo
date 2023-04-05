function ResponseContent(props) {
    let dataObj = {
        "ORDER_ID" : "Order ID",
        "PAY_ID": "Pay ID",
        "REG_NUMBER": "Registration No.",
        "PG_REF_NUM": "PG Ref No.",
        "MERCHANT_NAME": "Merchant Name",
        "RESPONSE_DATE_TIME": "Create Date",
        "RESPONSE_MESSAGE": "Response Message",
        "STATUS": "Status",
        "TOTAL_AMOUNT": "Total Amount",
        "ENCDATA": "Encrypted Data",
        "orderid": "Order ID",
        "amount": "Amount"
    };

    let summaryList = null;
    if(props.data !== null) {
        summaryList = Object.keys(dataObj).map(key => {
            if(props.data[key] !== null && props.data[key] !== "" && props.data[key] !== undefined) {
                if(key == "TOTAL_AMOUNT" || key == "amount") {
                    return (
                        <li className="d-flex justify-content-between pt-10 font-weight-medium font-size-18" key={key}>
                            <span className="d-inline-block mwp-40">{ dataObj[key] }</span>
                            <span>
                                <span className="font-weight-medium mr-5">&#8377;</span>
                                <span>
                                    { key == "TOTAL_AMOUNT" ? (Number(props.data[key]) / 100).toFixed(2) : props.data[key] }
                                </span>
                            </span>
                        </li>
                    )
                } else if(key === "PAY_ID") {
                    if(props.data.ENCDATA !== undefined) {
                        return (
                            <li className="d-flex justify-content-between pt-15 font-size-14 line-height-16" key={key}>
                                <span className="d-inline-block mwp-40">{ dataObj[key] }</span>
                                <span className="mwp-60 word-wrap text-right">{ props.data[key] }</span>
                            </li>
                        );
                    }
                } else {
                    return (
                        <li className="d-flex justify-content-between pt-15 font-size-14 line-height-16" key={key}>
                            <span className="d-inline-block mwp-40">{ dataObj[key] }</span>
                            <span className="mwp-60 word-wrap text-right">{ props.data[key] }</span>
                        </li>
                    );
                }
            }

            return null;
        });
    }

    return (
        <div className="row">
            <div className="col-12 d-flex justify-content-center">
                <div className={`card_box p-15 w-100 ${props.cardBox}`}>
                    <div className="card_box_icon text-center line-height-30 d-flex justify-content-center align-items-center text-white font-size-20 mb-15 mx-auto rounded-circle" style={{backgroundImage: `url(${window.basePath}/img/${props.cardBox}.png)`}}></div>
        
                    <h3 className="text-center statusResult text-capitalize mb-10 font-weight-medium font-size-18">{ props.statusResult }</h3>
        
                    <ul className="list-unstyled mb-0">
                        { summaryList }
                    </ul>
                </div>
            </div>
            
            {
                props.timer !== undefined ? props.timer.isTextVisible ?
                (
                    <div className="col-12 text-center font-size-14 font-weight-bold mb-30">You will automatically redirect to Payment Page in { props.timer.timerText }</div>
                ) : null : null
            }
        </div>
    );
}

export default ResponseContent;