$(document).ready(function(e){

    function createVariableJson(d){
        var _input = document.querySelectorAll('[data-var]');
        var _obj = {};
        _input.forEach(function(index, array, element){
            _obj[index.getAttribute("data-var")] = index.value;
        })
        _obj.draw = d.draw;
        _obj.length = d.length;
        _obj.start = d.start;
        console.log(_obj);
        return _obj;
    }

    function reportLoad(){
        document.querySelector("body").classList.remove("loader--inactive");
        $("#transactionInfoReport").dataTable({
            // "aaData": _data,
            "ajax" : {
                "type" : "POST",
                "url" : "gettingCoinSwitchCustTxnData",
                "data" : function (d){
                    return createVariableJson(d)
                }
            },

            "searching" : true,
            "ordering" : false,
            "destroy": true,
            "serverSide" : true,
            
// "sAjaxDataProp" : "aaDataTransact",
            "fnDrawCallback" : function(settings, json) {
                setTimeout(function(e){
                    document.querySelector("body").classList.add("loader--inactive");
                }, 500);
            },
            "aoColumns": [
                { "mData": "virtualAccountNo" },
                { "mData": "custName" },
                { "mData": "rrn" },
                { "mData": "txnType"},
                { "mData": "amount"},
                { "mData": "createDate"},
                { "mData": "status"}
            ]
        });

    }

    document.querySelector("#view").onclick = function(e){
        var _checkNull = document.querySelector(".hasError");
        if(_checkNull == null){
            reportLoad();
        }
    }

    reportLoad();

})

function downloadReport(){
    var _allInput = document.querySelectorAll("[data-var]");
    _allInput.forEach(function(index, array, element){
        var _option = "<input type='hidden' name='"+index.getAttribute("data-var")+"' value='"+index.value+"' />";
        document.querySelector("#downloadForm").innerHTML += _option;
        document.querySelector("#downloadForm").submit();
    })
}



function removeErrorField(_this){
    _this.closest("div").classList.remove("hasError");
}

function onlyDigit(event){
    var x = event.keyCode;
    if (x > 47 && x < 58 || x == 32) {
    } else {
        event.preventDefault();
    }
}

// letters and alpabet
function lettersAndAlphabet(event) {
    var x = event.keyCode;
    if (x > 64 && x < 91 || x > 96 && x < 123 || x > 47 && x < 58 || x == 32) {
    } else {
        event.preventDefault();
    }
}

function checkRegEx(e){
    var _getRegEx = e.getAttribute("data-regex");
    var _newRegEx = new RegExp(_getRegEx);
    var _value = e.value;
    if(_value != ""){
        if(_newRegEx.test(_value) != true){
            var _getLabel = e.closest("div").querySelector("label").innerText;
            e.closest("div").querySelector(".error-field").innerText = "Invalid "+_getLabel;
            e.closest("div").classList.add("hasError");
        }
    }else{
        e.closest(".col-md-3").classList.remove("has-error");
    }
}
