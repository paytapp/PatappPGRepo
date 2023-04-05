import DeleteButton from "./DeleteButton";
import { useEffect } from "react";
import { addConvenienceFee } from "../../js/script";

const SavedVpa = props => {
    useEffect(() => {
        if(props.isActive !== "") {
            addConvenienceFee("UP");
        }            
    }, []);

    return (        
        <label className={`custom-control-label w-100`} htmlFor={`inputId-${props.elementKey}`}>
            <input type="hidden" className="payerAddress" value={props.vpa} />

            <div className="card-dtls w-100 p-14">
                <div className="vpa-upi-icon">
                    <span className="font-size-26 pg-icon icon-upi">
                        <span className="path1"></span>
                        <span className="path2"></span>
                        <span className="path3"></span>
                    </span>
                </div>

                <div className="d-flex align-items-center mt-15">
                    <input
                        className="custom-control-input"
                        type="radio"
                        name="tokenId"
                        id={`inputId-${props.elementKey}`}
                        checked={props.isActive}
                        onChange={e => props.handleClick(e, props.elementKey, "UP", props.vpa)}
                        value={props.elementKey} />                            
                    

                    <div className="position-relative overflow-hidden">
                        <div className="font-size-14 font-weight-medium vpa-mask text-ellipses overflow-hidden">
                            {props.vpaMask}
                        </div>
                    </div>
                </div>

                <DeleteButton
                    elementKey={props.elementKey}
                    deleteHandler={props.deleteHandler}
                    action="deleteVpa"
                    checkBoxId={`inputId-${props.elementKey}`}
                    confirmText="VPA"
                    tokenName="vpaToken"
                    tokenAvailableName="vpaTokenAvailable"
                />
            </div>
        </label>
    );
}

export default SavedVpa;