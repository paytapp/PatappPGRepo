import "./wrapper.css";


function Wrapper({children}){
    return (
        <div className="lp-wrapper">
            {children}
        </div>
    )
}

export default Wrapper;