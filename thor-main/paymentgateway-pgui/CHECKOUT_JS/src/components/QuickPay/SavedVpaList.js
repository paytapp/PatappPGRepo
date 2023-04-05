import Fade from 'react-reveal/Fade';
import DeleteButton from '../DeleteButton/DeleteButton';

const SavedVpaList = props => {
    return (
        <div className={`col-12 quick-pay-list${props.isActive ? ' active' : ''}`} id={`quick-pay-${props.item.key}`}>
            <Fade bottom duration={200 * (props.duration + 1)}>
                <div className="position-relative">
                    <label className={`custom-control-label position-relative d-flex align-items-center px-15 py-12 cursor-pointer`} htmlFor={props.item.key}>
                        <input
                            type="radio"
                            className="custom-control-input"
                            id={props.item.key}
                            value={props.item.vpa}
                            name="wallet-radio"
                            checked={props.isActive ? true : false}
                            onChange={props.quickPayHandler}
                        />

                        {/* <span className="nb-icon rounded bg-white">
                            <img src={`${Window.basePath}/img/${props.item}.png`} alt={props.item} height="22" />
                        </span> */}

                        <div className="vpa-upi-icon d-flex">
                            <span className="font-size-26 pg-icon icon-upi">
                                <span className="path1"></span>
                                <span className="path2"></span>
                                <span className="path3"></span>
                            </span>
                        </div>

                        <span className="nb-text d-block font-size-14 ml-10">{props.item.vpaMask}</span>
                    </label>

                    {props.isActive ?
                        <DeleteButton
                            elementKey={props.item.key}
                            deleteHandler={props.deleteHandler}
                            action="deleteVpa"
                            checkBoxId={props.item.key}
                            confirmText="VPA"
                            tokenName="vpaToken"
                            tokenAvailableName="vpaTokenAvailable"
                        /> : null
                    }

                </div>
            </Fade>            
        </div>
    );
}

export default SavedVpaList;