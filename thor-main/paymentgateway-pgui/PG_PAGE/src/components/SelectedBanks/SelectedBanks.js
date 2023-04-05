import { Component } from "react";

class SelectedBanks extends Component {
    addIcon = (iconCode) => {
        return <span dangerouslySetInnerHTML={{__html: iconCode}} />
    }

    render() {
        let iconObj = {
            "axis" : '<i class="pg-icon icon-axis font-size-30 font-size-lg-34"></i>',
            "bob" : '<i class="pg-icon icon-bob font-size-30 font-size-lg-34"></i>',
            "icici" : '<span class="pg-icon icon-icici font-size-30 font-size-lg-34"><span class="path1"></span><span class="path2"></span><span class="path3"></span></span>',
            "hdfc" : '<span class="pg-icon icon-hdfc font-size-30 font-size-lg-34"><span class="path1"></span><span class="path2"></span><span class="path3"></span><span class="path4"></span><span class="path5"></span></span>',
            "sbi" : '<i class="pg-icon icon-sbi font-size-30 font-size-lg-34"></i>',
            "kotak" : '<span class="pg-icon icon-kotak font-size-30 font-size-lg-34"><span class="path1"></span><span class="path2"></span><span class="path3"></span></span>',
            "pnb" : '<i class="pg-icon icon-pnb font-size-30 font-size-lg-34"></i>',
            "yesbank" : '<span class="pg-icon icon-yes-bank font-size-30 font-size-lg-34"><span class="path1"></span><span class="path2"></span><span class="path3"></span><span class="path4"></span><span class="path5"></span><span class="path6"></span><span class="path7"></span><span class="path8"></span><span class="path9"></span><span class="path10"></span><span class="path11"></span><span class="path12"></span><span class="path13"></span></span>',
            "canarabank" : '<span class="pg-icon icon-canara-bank font-size-30 font-size-lg-34"><span class="path1"></span><span class="path2"></span><span class="path3"></span></span>',
            "idbi" : '<span class="pg-icon icon-idbi-bank font-size-30 font-size-lg-34"></span>',
            "indusind" : '<span class="pg-icon icon-indus-bank font-size-24 font-size-lg-24"><span class="path1"></span><span class="path2"></span><span class="path3"></span><span class="path4"></span><span class="path5"></span><span class="path6"></span><span class="path7"></span><span class="path8"></span><span class="path9"></span><span class="path10"></span><span class="path11"></span><span class="path12"></span><span class="path13"></span></span>',
            "rblbank" : '<span class="pg-icon icon-rbl-bank font-size-30 font-size-lg-34"><span class="path1"></span><span class="path2"></span><span class="path3"></span></span>',
            "unionbank" : '<span class="pg-icon icon-union font-size-30 font-size-lg-34"><span class="path1"></span><span class="path2"></span></span>',
            "idfcbank" : '<span class="pg-icon icon-idfc-bank font-size-30 font-size-lg-34"><span class="path1"></span><span class="path2"></span><span class="path3"></span><span class="path4"></span><span class="path5"></span></span>'
        }

        return (
            <div className="col-6 col-sm-4 bankList pt-5 pb-20">
                <label className={`custom-control-label bank_list_label d-flex align-items-center flex-column py-15 ${this.props.isActive ? 'active' : ''}`} htmlFor={this.props.inputId}>
                    <input
                        type="radio"
                        className="custom-control-input"
                        id={this.props.inputId}
                        value={this.props.inputValue}
                        name="netbankingRadio"
                        checked={this.props.isActive ? true : false}
                        onChange={this.props.selectedBankHandler} />
                    <span className="nb-icon d-flex align-items-center justify-content-center">
                        {this.addIcon(iconObj[this.props.inputId])}
                    </span>
                    <span className="nb-text d-block mt-5 font-size-14">{this.props.inputText}</span>
                </label>
            </div>
        );
    }
}

export default SelectedBanks;