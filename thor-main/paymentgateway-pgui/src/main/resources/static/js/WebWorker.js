let timeCount = 0;
const sleepResponse = function (_val, requestType) {
    setTimeout(function() {
        timeCount += 10000;

        if(timeCount > 900000) {
            self.postMessage("cancel");
        } else {
            verifyUpiResponseReceived(_val, requestType)
        }
    }, 10000);
};

function verifyUpiResponseReceived(_val, requestType) {
    const payload = {
        "pgRefNum": _val
    };

    fetch("/pgui/jsp/verifyUpiResponse", {
        method: 'POST',
        body: JSON.stringify(payload),
        headers : { 
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        }      
    }).then(response => response.json())
    .then(responseJson => {
        if (null != responseJson) {
            let _transactionStatus = responseJson.transactionStatus;
                
            if(_transactionStatus == "Sent to Bank" || _transactionStatus == "Pending") {
                sleepResponse(_val, requestType);
            } else {
                self.postMessage(responseJson);
            }
        } else {
            sleepResponse(_val, requestType);
        }
    })
    .catch(error => console.error(error));
}

self.onmessage = function (obj) {
    if (void 0 !== obj.data) {
        var response = obj.data;

        verifyUpiResponseReceived(response.value, response.requestType);
    }
};