import DeleteButton from "./DeleteButton";
import { useEffect } from "react";
import { addConvenienceFee } from "../../js/script";

const SavedWl = props => {
    useEffect(() => {
        if(props.isActive !== "") {
            addConvenienceFee("WL");
        }
    }, []);

    return (        
        <label className={`custom-control-label w-100`} htmlFor={`inputId-${props.elementKey}`}>
            <input type="hidden" className="wl-moptype" value={props.code} />
            <input type="hidden" className="temp-wlMopType" value={props.value} />

            <div className="card-dtls w-100 p-14">
                <span className="nb-icon d-flex align-items-center justify-content-center">
                    <img src={`${window.basePath}/img/${props.value}.png`} className="img-fluid" alt="" />
                </span>

                <div className="d-flex align-items-center mt-5">
                    <input
                        className="custom-control-input"
                        type="radio"
                        name="tokenId"
                        id={`inputId-${props.elementKey}`}
                        checked={props.isActive}
                        onChange={e => props.handleClick(e, props.elementKey, "WL", props.value)}
                        value={props.elementKey}
                    />

                    <div className="position-relative">
                        <div className="nb-text d-block font-size-14">
                            {window.walletText[props.value]}
                        </div>
                    </div>
                </div>

                <DeleteButton
                    elementKey={props.elementKey}
                    deleteHandler={props.deleteHandler}
                    action="deleteWlToken"
                    checkBoxId={`inputId-${props.elementKey}`}
                    confirmText="Wallet"
                    tokenName="wlToken"
                    tokenAvailableName="wlTokenAvailable"
                />
            </div>
        </label>
    );
}

export default SavedWl;