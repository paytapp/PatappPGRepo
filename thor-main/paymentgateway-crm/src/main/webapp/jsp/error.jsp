<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>Error Page</title>
	<link rel="shortcut icon" href="../img/favicon.ico" type="image/x-icon">

	<link rel="stylesheet" href="../css/bootstrap.min.css">
	<link rel="stylesheet" href="../css/bootstrap-flex.css">
	<link rel="stylesheet" href="../css/common-style.css">
	<link rel="stylesheet" href="../css/error-page.css">
	<link rel="stylesheet" href="../css/loader-animation.css">
</head>
<body class="bg-off-white">
	<!-- LOADER -->
	<div class="loader-container w-100 vh-100 d-flex justify-content-center align-items-center flex-column">
        <div class="loaderImage">
            <img src="../image/loader.gif" alt="Loader">
        </div>
    </div>
	<!-- /.loader-container -->

	<header class="bg-color-primary py-15 position-fixed top-0 left-0 w-100">
		<div class="container">
			<div class="row">
				<div class="col-12">
					<h1 class="m-0"><img src="../image/white-logo.png" alt="/"></h1>
				</div>
				<!-- /.col-12 -->
			</div>
			<!-- /.row -->
		</div>
		<!-- /.container -->
	</header>

	<div class="container container-error-page d-flex align-items-center justify-content-center">
		<div class="row">
			<div class="col-12">
				<div class="error-content text-center">
					<h1 class="text-black font-weight-medium font-size-55 mb-10">Oops!</h1>
					<p class="text-primary-lightest font-size-30 font-weight-normal m-0">We can't find the page you are looking for.</p>
				</div>
				<!-- /.error-content -->
			</div>
			<!-- /.col-12 -->
		</div>
		<!-- /.row -->
	</div>
	<!-- /.container -->

	<script>
		window.addEventListener("load", function(e) {
			document.getElementsByTagName("body")[0].classList.add("loader--inactive");
		});
	</script>
</body>
</html>