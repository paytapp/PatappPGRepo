export function isNumberKey(evt) {
	let elementValue = evt.target.value;
	if (!(/^[0-9]+$/.test(elementValue))) {
		evt.target.value = elementValue.replace(/[^0-9]/g, "");
	}
}

export function validateMobileNumber(_this){
    var _value = _this.target.value;
    if(_value.length < 10){
        return false
    }else{
        return true
    }
}