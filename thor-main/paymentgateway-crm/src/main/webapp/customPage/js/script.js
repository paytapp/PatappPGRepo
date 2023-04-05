$("document").ready(function() {
    jQuery.validator.addMethod('regex', function(value) {
        var _regex = /^([a-zA-Z0-9_.+-])+\@(([a-zA-Z0-9-])+\.)+([a-zA-Z0-9]{2,4})+$/;
        return _regex.test(value);
    });
    var _requiredField = {};
    var _requireFieldMsg = {};
    var _getAllInput = document.querySelectorAll(".form-control");

    _getAllInput.forEach(function(index, element, array){
        if(index.required == true){
            _requiredField[index.name] = {
                required : true
            };
        }
        if(index.maxLength != -1){
            _requiredField[index.name].maxLength = true
        }
    })

    _getAllInput.forEach(function(index, element, array){
        var _getLabel = index.closest(".form-group").querySelector("label").innerText;
        console.log(_getLabel.slice(0, _getLabel.length-1));
        if(index.required == true){
            _requireFieldMsg[index.name] = {
                required : "Please enter your "+_getLabel
            }
        }
    })

    $("#paymentDetail").validate({
        rules: _requiredField,
        messages: _requireFieldMsg,
        submitHandler: function(form) {
            $("body").removeClass("loader--inactive");
            updateOrderId();
            updateCustomerId();
            updateAmount();
            genrateHash(form);
            form.submit();
        }
    });
});