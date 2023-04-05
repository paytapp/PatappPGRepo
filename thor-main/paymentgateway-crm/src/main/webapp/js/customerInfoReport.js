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
        return _obj;
    }

    function reportLoad(){
        document.querySelector("body").classList.remove("loader--inactive");
        $("#compositeReportTabel").dataTable({

            "ajax" : {
                "type" : "POST",
                "url" : "gettingCoinSwitchCustData",
                "data" : function(d){
                    return createVariableJson(d);
                }
            },

            "searching" : false,
            "ordering" : false,
            "destroy": true,
            "serverSide" : true,

            "fnDrawCallback" : function(settings, json) {
                setTimeout(function(e){
                    document.querySelector("body").classList.add("loader--inactive");
                }, 500);
            },

            "aoColumns": [

                { "mData": "virtualAccountNo" },
                { "mData": "custName" },
                { "mData": "emailId" },
                { "mData": "pan"},
                { "mData": "phoneNo"},
                { "mData": "status"},

            ]

        });

    }

    $("body").on("click", ".dataTables_wrapper tbody td", function(e){
        var _currentTable = $(this).closest("table").attr("id");
        var table = new $.fn.dataTable.Api("#"+_currentTable);
        console.log(e.target.localName);
        _check = _currentTable;
		if(e.target.localName != "button" && e.target.localName != "input" && e.target.localName != "label"){
			var tr = $(this).closest('tr');
			var row = table.row(tr);
			if ( row.child.isShown() ) {
				tr.removeClass('shown');
				setTimeout(function(e){
					row.child()[0].children[0].classList.remove("active-row");
					row.child.hide();
				}, 600)
			}
			else {
				row.child( format(row.data()) ).show();
				row.child()[0].children[0].classList.add("active-row");
				getAllData();
				tr.addClass('shown');
			}
		}
		
	})

	function getAllData(){
		var _new = document.querySelectorAll(".inner-div");
		_new.forEach(function(index, array, element){
			var _getValue = _new[array].children[1].innerText;
			if(_getValue == 'null' || _getValue == '' || _getValue == 'undefined') {
				_new[array].classList.add("d-none");
			}
		})
	}

    document.querySelector("#view").onclick = function(e){
        var _error = document.querySelector(".hasError");
        if(_error == null){
            reportLoad();
        }
    }

    reportLoad();

})

function format ( d ) {

    var _obj = {
        "accountNo" : "Account Number",
        "bankAccountHolderName" : "Account Holder Name",
        "bankName" : "Bank Name",
        "bankIfsc" : "Bank IFSC",
        "aadhar" : "Aadhar Number",
        "address" : "Address"
    }

    _new = "<div class='main-div'>";
    
    for(key in _obj){
        if(_obj[key].hasOwnProperty("className")){
            var _getKey = Object.keys(_obj[key]);
            _new += '<div class="inner-div '+_obj[key]["className"]+'">'+
                    '<span>'+_obj[key][_getKey[0]]+'</span>'+
                    '<span>'+d[_getKey[0]]+'</span>'+
                '</div>'
        }else{
            _new += '<div class="inner-div">'+
                '<span>'+_obj[key]+'</span>'+
                '<span>'+d[key]+'</span>'+
            '</div>'
        }
    }
    
    if(_check == "st-datatable"){
        _new += '<div class="inner-div" style="width: 100%;text-align: center">'+
        '<span></span>'+'<span>'+d.button()+'</span>'+'</div>';
    }




    _new += "</div>";

    return _new;
   
}

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


// console.log(createVariableJson());