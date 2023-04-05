const WalletList = props => {
    return (
        <div className="col-6 col-md-4 pt-5 pb-20 bankList" id={props.item + "_wallet"}>
            <label className={`custom-control-label bank_list_label d-flex align-items-center flex-column py-15 ${props.isActive ? 'active' : ''}`} htmlFor={props.item}>
                <input type="radio" className="custom-control-input" id={props.item} value={props.item} name="wallet-radio" checked={props.isActive ? true : false} onChange={props.selectWallet} />
                <span className="nb-icon">
                    <img src={window.basePath + '/img/' + props.item + '.png'} alt={props.item} />
                </span>
                <span className="nb-text d-block mt-10 font-size-14">{window.walletText[props.item]}</span>
            </label>
        </div>
    );
}

export default WalletList;