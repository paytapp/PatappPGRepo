/**
 * @ISHA
 */

function generateCaptcha() {
	var min = 1000;
	var max = 9999;
	var str = [ '' ];
	var str2 = [ '' ];
	var num = Math.floor(Math.random() * (max - min + 1)) + min;
	var randomIndex = Math.floor(Math.random() * str.length);
	var randomIndex2 = Math.floor(Math.random() * str2.length);
	var randomElement = str[randomIndex];
	var randomElement2 = str2[randomIndex2];
	var res = randomElement.concat(num);
	var fn = res.concat(randomElement2);
	document.getElementById("captchaCode").value = fn;
	document.getElementById("captchaCode").readOnly = true;
	return true;
}
function ValidCaptcha() {
	var str1 = removeSpaces(document.getElementById('captchaCode').value);
	var str2 = removeSpaces(document.getElementById('captcha').value);
	var str3 = removeSpaces(document.getElementById('emailId').value);
	if (str3 != "") {

		if (str1 == str2) {
			document.getElementById('error3').innerHTML = "";
			return true;
		}

		else {
			document.getElementById('error3').innerHTML = "Invalid Captcha !!.";

			return false;
		}
		return false;
	}
}
function removeSpaces(string) {
	return string.split(' ').join('');
}