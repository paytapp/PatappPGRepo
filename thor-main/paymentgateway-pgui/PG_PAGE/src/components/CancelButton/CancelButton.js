const CancelButton = props => (
    <div className="text-center mt-10 mt-md-0 mt-lg-10">
        {/* <form method="POST" action={`${window.basePath}/jsp/txncancel`} id="cancel-form"> */}
            {/* <input type="hidden" name="payId" value={props.dataObj.PAY_ID} /> */}
            <button
                onClick={props.cancelHandler}
                type="button"
                key="ReturnToMerchant"
                id="ccCancelButton"
                className="font-weight-bold font-size-14 lang bg-none"
                name="ccCancelButton"
                theme="simple"
                data-key="cancel">Cancel</button>

        {/* </form> */}
    </div>
);

export default CancelButton;