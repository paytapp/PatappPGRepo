// only letters
function mzOnlyLetters(event) {
    var x = event.keyCode;
    if (x > 64 && x < 91 || x > 96 && x < 123 || x == 32) {
        
    } else {
        event.preventDefault();
    }
}

// only number
function mzOnlyNumbers(event){
    var x = event.keyCode;
    if (x > 47 && x < 58 || x == 32) {
    } else {
        event.preventDefault();
    }
}

// letters and alpabet
function mzLettersAndAlphabet(event) {
    var x = event.keyCode;
    if (x > 64 && x < 91 || x > 96 && x < 123 || x > 47 && x < 58 || x == 32) {
    } else {
        event.preventDefault();
    }
}

// digit backspace dot
function mzDigitDot(event) {
    var x = event.keyCode;
    if (x > 47 && x < 58 || x == 32 || x == 08 || x == 46) {
    } else {
        event.preventDefault();
    }
}