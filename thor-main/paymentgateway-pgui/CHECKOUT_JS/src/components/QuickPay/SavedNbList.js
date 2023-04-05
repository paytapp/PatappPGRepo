import Fade from 'react-reveal/Fade';
import DeleteButton from '../DeleteButton/DeleteButton';

const SavedNbList = props => {
    return (
        <div className={`col-12 quick-pay-list${props.isActive ? ' active' : ''}`} id={`quick-pay-${props.item.key}`}>
            <Fade bottom duration={200 * (props.duration + 1)}>
                <div className="position-relative">
                    <label className={`custom-control-label position-relative d-flex align-items-center px-15 py-12 cursor-pointer`} htmlFor={props.item.key}>
                        <input
                            type="radio"
                            className="custom-control-input"
                            id={props.item.key}
                            value={props.item.value}
                            name="wallet-radio"
                            checked={props.isActive ? true : false}
                            onChange={props.quickPayHandler}
                        />
                        <span className="nb-icon rounded bg-white">
                            <img src={`${Window.basePath}/img/${props.item.code}.png`} alt={props.item.code} height="22" />
                        </span>

                        <span className="nb-text d-block font-size-14 ml-10">{props.item.value}</span>
                    </label>

                    {props.isActive ?
                        <DeleteButton
                            elementKey={props.item.key}
                            deleteHandler={props.deleteHandler}
                            action="deleteNbToken"
                            checkBoxId={props.item.key}
                            confirmText="Bank"
                            tokenName="nbToken"
                            tokenAvailableName="nbTokenAvailable"
                        /> : null
                    }

                </div>
            </Fade>            
        </div>
    );
}

export default SavedNbList;