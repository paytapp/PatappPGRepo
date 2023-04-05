import Fade from 'react-reveal/Fade';

const WalletList = props => {
    return (
        <div className={`col-12 wallet-list${props.isActive ? ' active' : ''}`} id={`${props.item}_wallet`}>
            <Fade bottom duration={200 * (props.duration + 1)}>
                <label className={`custom-control-label d-flex align-items-center px-15 py-12`} htmlFor={props.item}>
                    <input type="radio" className="custom-control-input" id={props.item} value={props.item} name="wallet-radio" checked={props.isActive ? true : false} onChange={props.walletSelectHandler} />
                    <span className="nb-icon rounded bg-white">
                        <img src={`${Window.basePath}/img/${props.item}.png`} alt={props.item} height="22" />
                    </span>
                    <span className="nb-text d-block font-size-14 ml-10">{Window.walletText[props.item]}</span>
                </label>
            </Fade>            
        </div>
    );
}

export default WalletList;