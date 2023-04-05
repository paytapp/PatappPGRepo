import { useEffect } from "react";
import SavedCard from "./SavedCard";
import ScrollButton from "./ScrollButton";

const SaveCardWrap = props => {
    useEffect(() => {
        props.updateFeeHandler("SC");        
    }, []);

    const submitEXform = _ => {
        window.id('exSubmit').classList.add("btn-disabled");

        return true;
    }

    return (
        <div className="row" id="savedCardsWrap">
            <div className="col-12">
                <form autoComplete="off" name="exCard" method="post" target="_self" action={`${window.basePath}/jsp/pay`} id="exCard" onSubmit={submitEXform}>
                    <h3 className="mb-5 text-grey-lighter-alt font-size-14 font-size-md-16 font-weight-medium lang" data-key="savedCards">Saved Cards</h3>
                    <input type="hidden" name="paymentType" value="EX" />
                    <small id="cardSupportedEX" className="text-danger text-left"></small>

                    <div className="horizontal-nav-wrapper">
                        <div id="save-horizontal-nav" className="horizontal-nav">
                            <ul className="savedCards mb-0 horizontal-nav-content px-2 px-md-5 pt-5 pb-15 d-sm-flex flex-wrap d-md-block" id="saveCardBox">
                                {
                                    JSON.parse(window.pageInfoObj.cardToken).map(item => {
                                        return <SavedCard
                                            cardHolderType={item.cardHolderType}
                                            cardIssuerBank={item.cardIssuerBank}
                                            cardMask={item.cardMask}
                                            expiryDate={item.expiryDate}
                                            key={item.key}
                                            isActive={item.key === props.activeId}
                                            handleClick={props.handleClick}
                                            handleUserInput={props.handleUserInput}
                                            inputValue={props.inputValue}
                                            elementKey={item.key}
                                            mopType={item.mopType}
                                            paymentType={item.paymentType}
                                            paymentsRegion={item.paymentsRegion}
                                        />
                                    })
                                }
                            </ul>
                        </div>
                        
                        <ScrollButton backBtnId="save-scroll-left" nextBtnId="save-scroll-right" />
                    </div>

                    <input type="hidden" name="cvvNumber1" id="currentCvvInput" />
                    <input type="hidden" name="amount" id="orderTotalAmountInput" />
                    {/* <input type="hidden" name="tokenId1" id="currentTokenIdInput" /> */}
                    <input type="hidden" name="mopType" id="savedCardMopType" />
                    
                    {/* <p className="radioError font-size-12" id="radioError">Select atleast one Saved Card</p> */}

                    <div className="row">
                        <div className="col-md-6 card_charges d-none">
                            <span className="mr-10"><img src={window.basePath + "/img/info.png"} alt="" />&nbsp;&nbsp;</span>
                            <span id="charge-info"></span>
                        </div>
                    </div>

                    {/* <span className="CTANote" style="display: none;"> </span> */}                    
                </form>
            </div>
        </div>
    );
}

export default SaveCardWrap;