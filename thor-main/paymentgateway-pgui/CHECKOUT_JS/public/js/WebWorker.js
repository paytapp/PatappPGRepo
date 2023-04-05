let timeCount = 0;
const sleepResponse = function (payload) {
    setTimeout(function() {
        timeCount += 10000;

        if(timeCount > 300000) {
            self.postMessage("cancel");
        } else {
            verifyUpiResponseReceived(payload);
        }
    }, 10000);
};

function verifyUpiResponseReceived(payload) {
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
                sleepResponse(payload);
            } else {
                self.postMessage(responseJson);
            }
        } else {
            sleepResponse(payload);
        }
    })
    .catch(error => console.error(error));
}

self.onmessage = function (obj) {
    if (void 0 !== obj.data) {
        verifyUpiResponseReceived(obj.data);
    }
};