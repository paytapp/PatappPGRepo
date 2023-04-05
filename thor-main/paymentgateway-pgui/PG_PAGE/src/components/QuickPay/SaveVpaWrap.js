import { useEffect, useState } from "react";
import SavedVpa from "./SavedVpa";
import { HorizontalScrollContainer, HorizontalScrollItem } from "react-simple-horizontal-scroller";
import { multilingual } from "../../js/script";

const SaveVpaWrap = props => {
    useEffect(() => {
        props.updateFeeHandler("UP");
        multilingual();
    }, []);

    return (
        <div className="row" id="savedVpaWrap-alt">
            <div className="col-12">
                <h3 className="text-grey-light font-size-12 font-weight-medium lang" data-key="savedVpa">Saved VPA</h3>
            </div>
            <div className="col-12">
                <div className="savedVpa mb-0 d-flex overflow-hidden w-100" id="savedVPA-alt">
                    <HorizontalScrollContainer controlsConfig={props.controlsConfig}>
                        {
                            JSON.parse(props.token).map((item, index) => {
                                let itemMargin = "0 0 0 5px";
                                if(JSON.parse(props.token).length > 3) {
                                    itemMargin = "0 15px";
                                } else if(JSON.parse(props.token).length > 1) {
                                    if(index == 0) {
                                        itemMargin = "0 15px 0 5px";
                                    } else if(index == 1) {
                                        itemMargin = "0 15px";
                                    } else if(index == 2) {
                                        itemMargin = "0 0 0 15px";
                                    }
                                }

                                return <HorizontalScrollItem key={item.key} className={`saveVpaDetails${item.key === props.activeId ? ' active' : ''}`} id={`listId-${item.key}`} style={{width: 160, margin: itemMargin, boxSizing: 'border-box', display: 'flex', flexShrink: 0}}>
                                    <SavedVpa
                                        isActive={item.key === props.activeId}
                                        handleClick={props.handleClick}
                                        elementKey={item.key}
                                        vpa={item.vpa}
                                        vpaMask={item.vpaMask}
                                        deleteHandler={props.deleteHandler}
                                    />
                                </HorizontalScrollItem>
                            })
                        }
                    </HorizontalScrollContainer>
                </div>
            </div>
            <div className="col-12">
                <p className="text-danger1 font-size-12" id="error-blank-vpa">Please select any VPA</p>
            </div>
        </div>
    );
}

export default SaveVpaWrap;