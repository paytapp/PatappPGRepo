import CancelButton from "../CancelButton/CancelButton";
import SubmitButton from "../SubmitButton/SubmitButton";
import useWindowSize from "../../utils/useWindowSize";

const Declaration = props => {
    const { width } = useWindowSize();
    
    return (
        <div className={`row ${props.className}`}>
            <div className="col-md-6 card_charges d-none">
                <span className="mr-10"><img src={window.basePath + '/img/info.png'} alt="" />&nbsp;&nbsp;</span>
                <span id={props.id}></span>
            </div>


            {width < 991 && (
                <div className="col-md-12 submit-btns-tab mb-md-15 mb-lg-0 d-md-flex align-items-center justify-content-end" id={props.btnId}>
                    <SubmitButton submitHandler={props.submitHandler} />
                    <CancelButton cancelHandler={props.cancelHandler} dataObj={props.dataObj} />
                </div>
            )}
        </div>
    );
}

export default Declaration;