import Language from '../Language/Language';

var openNavSlide = function() {
    document.querySelector("body").classList.add("navigation-overlay--active");
}

function Header(props) {    
    return (
        <div className="row mt-md-30 mb-md-35">
            <div className="col-12 d-flex justify-content-between align-items-center px-xl-30">
                <div className="d-flex align-items-center custom-merchantName">
                    <button onClick={openNavSlide} className="font-size-20 text-primary d-none d-md-block d-lg-none mr-md-15 border-none bg-none"><i className="pg-icon icon-menu d-block"></i></button>
                    <h3 id="merchantName" className="font-size-18 font-weight-medium text-black d-none d-md-block">
                        { props.dataObj.merchantType }
                    </h3>
                </div>

                {/* Language Section Started */}
                <Language />
                {/* Language Section Ended */}
            </div>
        </div>
    );
}

export default Header;