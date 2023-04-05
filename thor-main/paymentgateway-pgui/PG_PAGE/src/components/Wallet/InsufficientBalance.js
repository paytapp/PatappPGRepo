const insufficientBalance = props => {

    const addMoneyHandler = e => {
        e.preventDefault();

        props.loaderHandler({showLoader: true});

        const payload = {
            payId : window.pageInfoObj.PAY_ID,
            phoneNo : props.walletObj.walletLoggedNumber,
            otp : props.walletObj.walletOtp,
            totalAmount : (Number(props.getTotalAmount()) - Number(props.walletObj.walletBalance)).toFixed(2)
        }

        fetch(`${window.basePath}/jsp/addMoney`, {
            method : "POST",
            body: JSON.stringify(payload),
            headers : {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }      
        })
        .then((response) => response.json())
        .then((responseJson) => {
            if(responseJson.response === "Success") {
                let _newWin = window.open(responseJson.addMoneyUrl, "Add Money", "toolbar=no, location=no, statusbar=no, menubar=no, scrollbars=1, resizable=0, width=1000, height=700");

                props.loaderHandler({showLoader: false});

                window.addMoneyTimer = setInterval(() => {
                    if(_newWin.closed) {
                        clearInterval(window.addMoneyTimer);

                        props.loadWalletBalanceHandler({
                            hideLoader: true,
                            walletLoggedNumber: props.walletObj.walletLoggedNumber,
                            walletOtp: props.walletObj.walletOtp,
                            isOtpVerified: true
                        }, e);
                    }
        
                    window.addEventListener("message", event => {
                        var obj = event.data;
                        if(obj.status) {
                            clearInterval(window.addMoneyTimer);
    
                            props.loadWalletBalanceHandler({
                                hideLoader: true,
                                walletLoggedNumber: props.walletObj.walletLoggedNumber,
                                walletOtp: props.walletObj.walletOtp,
                                isOtpVerified: true    
                            }, e);
                        }
                    });
                }, 500);
            }
        })
        .catch((error) => {
            console.error(error);
        });
    }

    return (
        <div className="col-sm-6 text-center" id="insufficient-fund">
            <div className="bg-grey-primary border p-15 h-100">
                <p className="text-danger font-size-14">
                    <span className="lang" data-key="insufficientFund">Insufficient funds!</span><br />
                    <span className="lang" data-key="rechargeWallet">Please recharge your wallet...</span>
                </p>
                <button onClick={addMoneyHandler} className="btn btn-secondary font-size-14 d-inline-flex align-items-center"><i className="pg-icon icon-plus-circle mr-5 font-size-16"></i><span className="lang" data-key="addMoney">Add money</span></button>
            </div>
        </div>
    );
}

export default insufficientBalance;