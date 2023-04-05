import "./TitleSection.css";
import Fade from 'react-reveal/Fade';

const TitleSection = props => {
    let editWalletNumber = null;
    if(props.walletMobileDisabled) {
        editWalletNumber = (
            <div className="d-flex align-items-center py-11 pr-15">
                <i className="pg-icon icon-user-o font-size-14"></i>
                <span className="font-size-14 ml-5 mr-10">+91 {props.walletLoggedNumber}</span>
                <button className="border-none bg-none d-flex" type="button" onClick={e => {props.editWalletNumberHandler(e);}}>
                    <i className="pg-icon icon-edit font-size-18"></i>
                </button>
            </div>
        );
    }

    let backIcon = null;
    if(props.componentType !== "addMoney") {
        backIcon = <Fade left><i className="pg-icon icon-left-arrow"></i></Fade>
    }

    return (
        <div className="TitleSection d-flex align-items-center justify-content-between w-100 position-fixed top-120 z-index-999">
            <button className="d-flex align-items-center font-size-14 py-12 px-15 border-none" onClick={props.navigationHandler} data-id={props.btnDataId} data-type={props.paymentType}>
                { backIcon }

                <Fade left cascade>
                    <span className={props.componentType !== "addMoney" ? "ml-10" : ""}>{props.title}</span>
                </Fade>
            </button>

            { editWalletNumber }
        </div>
    );
}

export default TitleSection;