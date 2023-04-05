var error_snackbar = document.getElementById("error-snackbar");
var success_snackbar = document.getElementById("success-snackbar");

// SNACKBAR
function showSnackbar(id) {
	// Get the snackbar DIV
	var x = document.getElementById(id);
  
	// Add the "show" class to DIV
	x.classList.add("show");
  
	// After 3 seconds, remove the show class from DIV
	setTimeout(function() {
		x.classList.remove("show");
	}, 3000);
}