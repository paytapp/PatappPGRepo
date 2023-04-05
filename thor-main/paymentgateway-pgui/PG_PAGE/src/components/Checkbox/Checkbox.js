import React, { useState } from "react";

const Checkbox = props => {
    const [checkedState, setCheckedState] = useState(props.checked);

    const handleCheckbox = (event) => {
        setCheckedState(event.target.checked);
    }

    return (
        <React.Fragment>
            { props.checked ?
                <div className="col-12" id={props.columnId}>
                    <label className="container_check">
                        <input
                            type="checkbox"
                            name={props.name + '1'}
                            checked={checkedState}
                            onChange={handleCheckbox}
                            id={props.name + '1'} />
                        <span className="lang field-title" data-key={props.dataKey}>{props.checkboxText}</span>
                        <span className="checkmark_checkbox chk-save"></span>
                    </label>
                </div> : null
            }
        
            <input type="hidden" id={props.name} name={props.name} value={checkedState} />
        </React.Fragment>
    );
}

export default Checkbox;