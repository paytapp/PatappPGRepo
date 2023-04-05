const Declaration = props => {
    return(
        <div className="row">
            <div className="col-md-6 card_charges d-none">
                <span className="mr-10"><img src={`${Window.basePath}/img/info.png`} alt="" />&nbsp;&nbsp;</span>
                <span id={props.id}></span>
            </div>
            {/* <div className="col-md-12 submit-btns-tab mb-md-15 mb-lg-0 d-md-flex align-items-center justify-content-end" id={props.btnId}></div> */}
        </div>
    );
}

export default Declaration;