import DeleteButton from "./DeleteButton";
import { useEffect } from "react";
import { addConvenienceFee } from "../../js/script";

const SavedNb = props => {
    useEffect(() => {
        if(props.isActive !== "") {
            addConvenienceFee("NB");
        }
    }, []);

    return (        
        <label className={`custom-control-label w-100`} htmlFor={`inputId-${props.elementKey}`}>
            <input type="hidden" className="nb-moptype" value={props.code} />
            <input type="hidden" className="temp-nbMopType" value={props.value} />
            <div className="card-dtls w-100 p-14">
                <span className="nb-icon d-flex align-items-center justify-content-center">
                    <img src={`${window.basePath}/img/${props.code}.png`} className="img-fluid" alt="" />
                </span>
                <div className="d-flex align-items-center mt-5">
                    <input
                        className="custom-control-input"
                        type="radio"
                        name="tokenId"
                        id={`inputId-${props.elementKey}`}
                        checked={props.isActive}
                        onChange={e => props.handleClick(e, props.elementKey, "NB", props.value)}
                        value={props.elementKey}
                    />                        

                    <div className="position-relative">
                        <div className="nb-text d-block font-size-14">
                            {window.nbDisplayName[props.code]}
                        </div>
                    </div>
                </div>

                <DeleteButton
                    elementKey={props.elementKey}
                    deleteHandler={props.deleteHandler}
                    action="deleteNbToken"
                    checkBoxId={`inputId-${props.elementKey}`}
                    confirmText="Bank"
                    tokenName="nbToken"
                    tokenAvailableName="nbTokenAvailable"
                />
            </div>
        </label>
    );
}

export default SavedNb;