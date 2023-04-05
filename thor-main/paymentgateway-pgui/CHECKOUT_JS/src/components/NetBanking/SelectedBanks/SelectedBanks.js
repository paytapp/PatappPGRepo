import { Component } from "react";
import Fade from 'react-reveal/Fade';

class SelectedBanks extends Component {
    addIcon = (iconCode) => {
        return <span className="d-flex" dangerouslySetInnerHTML={{__html: iconCode}} />
    }

    render() {
        let iconObj = {
            "axis" : '<i class="pg-icon font-size-24 icon-axis"></i>',
            "bob" : '<i class="pg-icon font-size-24 icon-bob"></i>',
            "icici" : '<span class="pg-icon font-size-24 icon-icici"><span class="path1"></span><span class="path2"></span><span class="path3"></span></span>',
            "hdfc" : '<span class="pg-icon font-size-24 icon-hdfc"><span class="path1"></span><span class="path2"></span><span class="path3"></span><span class="path4"></span><span class="path5"></span></span>',
            "sbi" : '<i class="pg-icon font-size-24 icon-sbi"></i>',
            "kotak" : '<span class="pg-icon font-size-24 icon-kotak"><span class="path1"></span><span class="path2"></span><span class="path3"></span></span>',
            "pnb" : '<i class="pg-icon icon-pnb font-size-24"></i>',
            "yesbank" : '<span class="pg-icon icon-yes-bank font-size-24"><span class="path1"></span><span class="path2"></span><span class="path3"></span><span class="path4"></span><span class="path5"></span><span class="path6"></span><span class="path7"></span><span class="path8"></span><span class="path9"></span><span class="path10"></span><span class="path11"></span><span class="path12"></span><span class="path13"></span></span>',
            "canarabank" : '<span class="pg-icon icon-canara-bank font-size-24"><span class="path1"></span><span class="path2"></span><span class="path3"></span></span>',
            "idbi" : '<span class="pg-icon icon-idbi-bank font-size-24"></span>',
            "indusind" : '<span class="pg-icon icon-indus-bank font-size-20"><span class="path1"></span><span class="path2"></span><span class="path3"></span><span class="path4"></span><span class="path5"></span><span class="path6"></span><span class="path7"></span><span class="path8"></span><span class="path9"></span><span class="path10"></span><span class="path11"></span><span class="path12"></span><span class="path13"></span></span>',
            "rblbank" : '<span class="pg-icon icon-rbl-bank font-size-24"><span class="path1"></span><span class="path2"></span><span class="path3"></span></span>',
            "unionbank" : '<span class="pg-icon icon-union font-size-24"><span class="path1"></span><span class="path2"></span></span>',
            "idfcbank" : '<span class="pg-icon icon-idfc-bank font-size-24"><span class="path1"></span><span class="path2"></span><span class="path3"></span><span class="path4"></span><span class="path5"></span></span>'
        }

        return (
            <div className="col-4 bankList p-0">
                <Fade bottom duration={200 * (this.props.duration + 1)}>
                    <label className={`custom-control-label bank_list_label d-flex align-items-center flex-column pb-15 pt-20 ${this.props.isActive ? 'active' : ''}`} htmlFor={this.props.inputId}>
                        <input
                            type="radio"
                            className="custom-control-input"
                            id={this.props.inputId}
                            value={this.props.inputValue}
                            name="netbankingRadio"
                            checked={this.props.isActive ? true : false}
                            onChange={this.props.labelSelectHandler} />
                        <span className="nb-icon d-flex align-items-center justify-content-center rounded bg-white">
                            {this.addIcon(iconObj[this.props.inputId])}
                        </span>
                        <span className="nb-text d-block mt-5 mt-lg-10 font-size-12">{this.props.inputText}</span>
                    </label>
                </Fade>
            </div>
        );
    }
}

export default SelectedBanks;