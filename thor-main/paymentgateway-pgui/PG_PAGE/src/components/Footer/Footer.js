import { useEffect } from 'react';


function Footer(props) {
    // useEffect(() => {
    //     const script = document.createElement("script");
    //     script.src = "https://seal.godaddy.com/getSeal?sealID=EVoAzH2kvG77fHymXssJIMbQzvrDM03cGIg5VIWPDl7DAqDeqE5L7gOUtefG";

    //     script.async = true;

    //     document.getElementById("siteseal").appendChild(script);
    // }, []);

    // const openWebSeal = (e, link) => {
    //     e.preventDefault();

    //     window.open(link,
    //         'Panacea Certificate',
    //         'height=500,width=650,scrollbar=yes,status=no,menubar=no,toolbar=no,resizable');
    // }

    return (
        <div className="row custom-footer bg-light-sky border-radius-bl-20 border-radius-br-20 position-relative" id="footer">
            <div className="col-md-8 col-lg-9 d-sm-flex justify-content-sm-between justify-content-lg-start payment-accept py-15 py-lg-20">
                <div className="d-flex justify-content-center align-items-center">
                    {/* <span className="pg-icon icon-verified-visa font-size-20 mr-5"><span className="path1"></span><span className="path2"></span><span className="path3"></span><span className="path4"></span><span className="path5"></span><span className="path6"></span><span className="path7"></span><span className="path8"></span><span className="path9"></span><span className="path10"></span><span className="path11"></span><span className="path12"></span><span className="path13"></span><span className="path14"></span><span className="path15"></span></span> */}

                    {/* <span className="pg-icon icon-mastercard font-size-20 mr-5"><span className="path1"></span><span className="path2"></span><span className="path3"></span><span className="path4"></span><span className="path5"></span><span className="path6"></span><span className="path7"></span><span className="path8"></span><span className="path9"></span><span className="path10"></span><span className="path11"></span><span className="path12"></span><span className="path13"></span><span className="path14"></span><span className="path15"></span><span className="path16"></span><span className="path17"></span><span className="path18"></span><span className="path19"></span><span className="path20"></span><span className="path21"></span><span className="path22"></span><span className="path23"></span></span> */}

                    <img src={`${window.basePath}/img/visa-logo.png`} alt="" />
                    <img src={`${window.basePath}/img/mcard.png`} alt="" />

                    <span className="pg-icon icon-rupay-logo font-size-20 mr-5"><span className="path1"></span><span className="path2"></span><span className="path3"></span><span className="path4"></span></span>

                    <span className="pg-icon icon-american-express font-size-24 mr-md-5"><span className="path1"></span><span className="path2"></span><span className="path3"></span></span>
                </div>

                {/* <div className="d-flex justify-content-center align-items-center mt-10 mt-sm-0 site-verification-logo">
                    <button onClick={e => openWebSeal(e, "http://seal.panaceainfosec.com/index.php?certid=CERT1FD1F59DBD")} className="mr-10">
                        <img src={`${window.basePath}/img/pci-dss-webseal.png`} alt="" className="d-block" />
                    </button>

                    <span id="siteseal"></span>
                </div> */}
            </div>
            <div className="col-md-4 col-lg-3 bg-grey-dark-primary d-flex align-items-center pt-15 pb-5 border-radius-br-20 border-radius-bl-20 border-radius-bl-md-0 justify-content-center" id="footer-poweredby">
                <span className="text-grey-light-primary mtn-30 font-size-12">Powered By</span>
                <span className="font-family-logo ml-5 mr-5 font-size-18 text-white" id="companyName">
                Paytapp
                </span>
                <span className="text-white">&copy;</span>
            </div>
        </div>
    );
}

export default Footer;