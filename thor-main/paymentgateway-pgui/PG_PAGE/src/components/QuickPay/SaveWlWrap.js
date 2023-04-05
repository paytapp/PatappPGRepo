import { useEffect } from "react";
import SavedWl from "./SavedWl";
import { HorizontalScrollContainer, HorizontalScrollItem } from "react-simple-horizontal-scroller";
import { multilingual } from "../../js/script";

const SaveWlWrap = props => {
    useEffect(() => {
        props.updateFeeHandler("WL");
        multilingual();
    }, []);

    return (
        <div className="row" id="savedWlWrap">
            <div className="col-12">
                <h3 className="text-grey-light font-size-12 font-weight-medium lang" data-key="savedWallet">Saved Wallets</h3>
            </div>

            <div className="col-12">
                <form autoComplete="off" method="post" target="_self" action={`${window.basePath}/jsp/pay`} id="save-wallet-form">
                    <input type="hidden" name="paymentType" value="WL" />
                    <input type="hidden" name="mopType" id="save-wallet-moptype" value={props.mopType} />
                    <input type="hidden" name="amount" className="wallet-amount" />
                    <input type="hidden" id="save-wlSaveFlag" name="wlSaveFlag" value={false} />

                    <div className="savedWl mb-0 d-flex overflow-hidden w-100" id="savedWl">
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
                                    return <HorizontalScrollItem key={item.key} className={`saveWlDetails${item.key === props.activeId ? ' active' : ''}`} id={`listId-${item.key}`} style={{width: 160, margin: itemMargin, boxSizing: 'border-box', display: 'flex', flexShrink: 0}}>
                                        <SavedWl
                                            isActive={item.key === props.activeId}
                                            handleClick={props.handleClick}
                                            elementKey={item.key}
                                            code={item.code}
                                            value={item.value}
                                            deleteHandler={props.deleteHandler}
                                        />
                                    </HorizontalScrollItem>
                                })
                            }
                        </HorizontalScrollContainer>
                    </div>

                    <button type="submit" className="btn-payment d-none"></button>
                </form>
            </div>
        </div>
    );
}

export default SaveWlWrap;