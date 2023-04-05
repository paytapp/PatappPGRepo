
function Heading(props){
    return(
        <div className={props.customClass}>
            <div className="heading_with_icon mb-30">
                { props.icon && <span className="heading_icon_box"><i className="fa fa-bar-chart-o" aria-hidden="true"></i></span> }
                <h2 className="heading_text lpay_button-primary">{props.title}</h2>
            </div>
        </div>
    )
}

export default Heading;

