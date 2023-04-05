function alphaOnly(event) {
  var key = event.keyCode,
    spaceKey = 32,
    leftKey =37,
    rightKey = 39,
    deleteKey = 46,
    backspaceKey = 8,
    tabKey = 9;
  return ((key >= 65 && key <= 90) || key == backspaceKey || key == tabKey || key == spaceKey || key == leftKey || key == rightKey || key == deleteKey);
};
//onkeydown="return alphaOnly(event);"


function numOnly(event,Element) {
  var key = event.keyCode,
    spaceKey = 32,
    leftKey =37,
    rightKey = 39,
    deleteKey = 46,
    backspaceKey = 8,
    tabKey = 9,
    maxlengthCheck = Number(Element.getAttribute('maxlength'));
    
  if(event.key == "!" || event.key == "@" || event.key == "#" || event.key == "$" || event.key == "%" || event.key == "^" || event.key == "&" || event.key == "*" || event.key == "(" || event.key == ")"){
    return false;
  }
  if(maxlengthCheck){
    if(Element.value.length==maxlengthCheck){   
            if(key == backspaceKey || key == tabKey || key == leftKey || key == rightKey || key == deleteKey){
                return true;
            }else{
                return false;
            }
    }
  }
  return ((key >= 48 && key <= 57) || (key >= 96 && key <= 105) || key == backspaceKey || key == tabKey || key == leftKey || key == rightKey || key == deleteKey);
};
//onkeydown="return numOnly(event,this);"
//input type should be number. 

function numOnlyInTextInput(event) {
  var key = event.keyCode,
    spaceKey = 32,
    leftKey =37,
    rightKey = 39,
    deleteKey = 46,
    backspaceKey = 8,
    tabKey = 9;
    
  if(event.key == "!" || event.key == "@" || event.key == "#" || event.key == "$" || event.key == "%" || event.key == "^" || event.key == "&" || event.key == "*" || event.key == "(" || event.key == ")"){
    return false;
  }
  return ((key >= 48 && key <= 57) || (key >= 96 && key <= 105) || key == backspaceKey || key == tabKey || key == leftKey || key == rightKey || key == deleteKey);
};
//onkeydown="return numOnlyInTextInput(event);"

function alphaNumeric(event){
    var key = event.keyCode,
    spaceKey = 32,
    leftKey =37,
    rightKey = 39,
    deleteKey = 46,
    backspaceKey = 8,
    tabKey = 9;

    if(event.key == "!" || event.key == "@" || event.key == "#" || event.key == "$" || event.key == "%" || event.key == "^" || event.key == "&" || event.key == "*" || event.key == "(" || event.key == ")"){
    return false;
  }

  return ((key >= 65 && key <= 90) || (key >= 48 && key <= 57) || (key >= 96 && key <= 105) || key == backspaceKey || key == tabKey || key == spaceKey || key == leftKey || key == rightKey || key == deleteKey);
}
//onkeydown="return alphaNumeric(event);"

function businessNameValid(event){
    var key = event.keyCode,
    spaceKey = 32,
    leftKey =37,
    rightKey = 39,
    deleteKey = 46,
    backspaceKey = 8,
    tabKey = 9,
    fullstopKey = 190;

    if(event.key == "!" || event.key == "@" || event.key == "#" || event.key == "$" || event.key == "%" || event.key == "^" || event.key == "*" || event.key == "(" || event.key == ")"){
    return false;
  }

  return ((key >= 65 && key <= 90) || (key >= 48 && key <= 57) || (key >= 96 && key <= 105) || key == backspaceKey || key == tabKey || key == spaceKey || key == leftKey || key == rightKey || key == deleteKey || key == fullstopKey);
}
//onkeydown="return businessNameValid(event);"
//accepted key => alpha, numeric, space, . (fullstop) , & (and)

function isValidEmail(inputId){
    var emailexp = /^[_A-Za-z0-9-]+(\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*(\.[_A-Za-z0-9-]+)$/;
    var emailElement = document.getElementById(inputId);
    var emailValue = emailElement.value;
    if (emailValue.trim() !== "") {
        if (emailValue.match(emailexp)) {
            return true;
        } else {
            return false;
        }
    } else {
        return false;
    }
}