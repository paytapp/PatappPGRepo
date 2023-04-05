const UpiContainer = props => (
    <div className={`col-12 tabBox ${props.className}`} id={props.id}>
        <div className="tabbox-inner">
            <div className="toggle-list-box">
                { props.children }
            </div>
        </div>
    </div>
)

export { UpiContainer };