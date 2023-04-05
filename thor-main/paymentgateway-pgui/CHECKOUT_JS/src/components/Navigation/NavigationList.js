import { Component } from "react";
import Fade from 'react-reveal/Fade';
import "./NavigationList.css";

class NavigationList extends Component {
    addIcon = (iconCode) => {
        return <span className="nav-icon d-flex align-items-center" dangerouslySetInnerHTML={{__html: iconCode}} />
    }

    render() {
        let iconObj = {
            "SC" : '<i class="font-size-20 pg-icon icon-quick-pay tab-span"></i>',
            "CC" : '<i class="font-size-20 pg-icon icon-cards tab-span"></i>',
            "DC" : '<i class="font-size-20 pg-icon icon-cards tab-span"></i>',
            "CD" : '<i class="font-size-20 pg-icon icon-cash tab-span"></i>',
            "CR" : '<i class="font-size-20 pg-icon icon-crypto tab-span"></i>',
            "WL" : '<i class="font-size-20 pg-icon icon-wallet-alt tab-span"></i>',
            "NB" : '<i class="font-size-20 pg-icon icon-bank tab-span"></i>',
            "UP" : '<span class="font-size-26 pg-icon icon-upi tab-span"><span class="path1"></span><span class="path2"></span><span class="path3"></span></span>',
            "EM" : '<i class="font-size-20 pg-icon icon-emi tab-span"></i>',
            "UPI_QR" : '<span class="font-size-26 pg-icon icon-upi tab-span"><span class="path1"></span><span class="path2"></span><span class="path3"></span></span>'
        }
    
        return (            
            <li className="NavigationList">
                <button data-id={this.props.btnDataId} data-type={this.props.btnDataType} onClick={e => { this.props.navigationHandler(e); this.props.navStateHandler(e); }} className="NavigationList-btn d-flex align-items-center font-size-14 py-12 px-15">
                    {this.addIcon(iconObj[this.props.btnDataType])}
                    <span className="tab-span ml-15">{this.props.btnText}</span>
                </button>
            </li>            
        );
    }
}

export default NavigationList;