export const EmailBtn = props => {
    const dialogHandler = (status, msg) => {
        props.klass.setState({
            showLoader: false,
            isDialogOpen: true,
            dialogMsg: msg,
            dialogType: status
        });
    }
    const sendEmailHandler = () => {
        props.klass.setState({showLoader: true});

        const payload = {ORDER_ID: props.klass.state.data.ORDER_ID};
        fetch(`${window.basePath}/jsp/sendMail`, {
            method: 'POST',
            body: JSON.stringify(payload),
            headers : {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        })
        .then((response) => response.json())
        .then(data => {            
            dialogHandler(data.status, data.responseMsg);            
        });
    }

    return (
        <button className="pos_btn btn font-size-14 p-0 ml-5" id="mailPdf" onClick={sendEmailHandler}><i className="fas fa-envelope font-size-18"></i> Email</button>
    )
}