const CvvInfo = props => {
    return (
        <div className="whatIsCvv">
            <div className="cvv-info-inner">
                <h2 className="mb-10 lang" data-key="cvvTitle">What is CVV?</h2>
                <div className="d-flex">
                    <div className="cvv-info-img mr-10">
                        <img src={`${Window.basePath}/img/cardCvv.png`} alt="" />
                    </div>
                    <p className="cvv-info-text lang" data-key="cvvText">The CVV number is the last three digits on the back of your card</p>
                </div>
                
                <img className="leftAerrow" src={`${Window.basePath}/img/left-aerrow.png`} alt="" />

                <div className="cvv-info-close full-width">
                    <button className="cvv-info-close-btn lang" data-key="btnOk">Ok</button>
                </div>
            </div>
        </div>
    );
}

export default CvvInfo;