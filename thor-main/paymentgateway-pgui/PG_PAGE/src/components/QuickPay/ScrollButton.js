import React from "react";

const ScrollButton = props => {
    return (
        <React.Fragment>
            <button type="button" id={props.backBtnId} className="horizontal-nav-btn horizontal-nav-btn-left bg-white-transparent px-2">
                <i className="pg-icon icon-left-arrow d-inline-block mt-5 text-grey-light font-weight-bold"></i>
            </button>
            <button type="button" id={props.nextBtnId} className="horizontal-nav-btn horizontal-nav-btn-right bg-white-transparent px-2">
                <i className="pg-icon icon-right-arrow d-inline-block mt-5 text-grey-light font-weight-bold"></i>
            </button>
        </React.Fragment>
    );
}

export default ScrollButton;