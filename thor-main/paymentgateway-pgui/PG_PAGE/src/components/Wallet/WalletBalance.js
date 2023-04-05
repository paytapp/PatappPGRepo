const WalletBalance = props => {
    return (
        <div className="col-sm-6 text-center" id="available-balance">
            <div className="bg-grey-primary border p-15 h-100">
                <div className="d-flex flex-column">
                    <div className="d-flex align-items-center justify-content-center font-weight-bold font-size-30"><span className="pg-icon icon-inr mr-10"></span><span id="mobikwik-amount">{ props.walletObj.walletBalance }</span></div>
                    <button onClick={(e) => {props.loadWalletBalanceHandler({
                        hideLoader: true,
                        walletLoggedNumber: props.walletObj.walletLoggedNumber,
                        walletOtp: props.walletObj.walletOtp,
                        isOtpVerified: true

            }, e);}} className="font-size-12 d-flex align-items-center justify-content-center mtn-5 border-none bg-none"><i className="pg-icon icon-refresh mr-5"></i><span className="text-underline lang" data-key="refreshBalance">Refresh Balance</span></button>
                </div>
                <div className="font-size-18 mt-10 lang" data-key="availableBalance">Available Balance</div>
            </div>
        </div>
    );
}

export default WalletBalance;