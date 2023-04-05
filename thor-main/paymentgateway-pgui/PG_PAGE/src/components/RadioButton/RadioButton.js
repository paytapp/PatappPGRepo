import React, { useState } from "react";

const RadioButton = props => {
    return (
        <>  
            <div className={`container_check ${props.isActive ? 'radio--active' : ''}`}>
                <span className="checkmark_checkbox chk-save"></span>
            </div>
        </>
    );
}

export default RadioButton;