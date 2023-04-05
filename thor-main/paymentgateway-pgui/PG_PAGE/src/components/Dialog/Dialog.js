import { Component } from "react";
import './Dialog.css';

class Dialog extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        const dialogType = this.props.dialogType === 'success' ? 'successMsg' : 'failedMsg';
        return (
            <>
                <div className="position-fixed top-0 left-0 w-100 h-100 bg-dark-transparent dialog-container">
                    <div className={`dialog-box border-radius-primary ${dialogType}`}>
                        <div className="position-relative p-30">
                            <button className="bg-none border-none dialog-close-btn font-size-14" onClick={this.props.onClose}><i className="pg-icon icon-cancel-cross"></i></button>
                            <div className="card_box_icon text-center line-height-30 d-flex justify-content-center align-items-center text-white font-size-20 mb-15 mx-auto rounded-circle" style={{backgroundImage: `url(${window.basePath}/img/${dialogType}.png)`}}></div>
                            <h4 className="text-center statusResult text-capitalize mb-10 font-weight-medium font-size-18">{ this.props.children }</h4>
                        </div>
                    </div>
                </div>
            </>
        )
    }
}

export default Dialog;