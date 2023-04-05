<%@taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	
	<title>Initiated Refund Data</title>
	<link rel="icon" href="../image/favicon-32x32.png">
	<link href="../css/jquery-ui.css" rel="stylesheet" />
	<script src="../js/jquery.min.js"></script>
	<script src="../js/jquery.dataTables.js"></script>
	<script src="../js/jquery-ui.js"></script>
	<script type="text/javascript" src="../js/daterangepicker.js"></script>
	

	<link href="../fonts/css/font-awesome.min.css" rel="stylesheet">
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<script src="../js/bootstrap-select.min.js"></script>
	
	<script src="../js/messi.js"></script>
	<link href="../css/messi.css" rel="stylesheet" />

	<link rel="stylesheet" href="../css/common-style.css">
	<link rel="stylesheet" href="../css/bootstrap-flex.css">
	<script src="../js/common-scripts.js"></script>

	<script>
		function dateValidationHandler() {
			const transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val()),
				transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());

			if (transFrom == null || transTo == null) {
				alert('Enter date value');
				return false;
			}

			if (transFrom > transTo) {
				alert('From date must be before the to date');
				$('#dateFrom').focus();
				return false;
			}

			if (transTo - transFrom > 31 * 86400000) {
				alert('No. of days can not be more than 31');
				$('#dateFrom').focus();
				return false;
			}

			return true;
		}

		function downloadInitiatedRefundFile(e, that) {
			e.preventDefault();

			const result = dateValidationHandler();

			if(result) {
				that.closest("form").submit();
			}
		}
	</script>
	
</head>
	<body id="mainBody">
		<section class="invoice-search lpay_section white-bg box-shadow-box mt-70 p20">
			<form method="POST" action="downloadInitiatedRefundFile" id="initiatedRefundForm">
				<div class="row">
					<div class="col-md-12">
						<div class="heading_with_icon mb-30">
							<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
							<h2 class="heading_text">Initiated Refund Data</h2>
						</div>
						<!-- /.heading_icon -->
					</div>
					<!-- /.col-md-12 -->

					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="acquirerType">Acquirer Type <span class="text-danger">*</span></label>
							<div class="position-relative">
								<input type="text" name="acquirerType" value="SBI" class="lpay_input" readonly />
								<span error-id="acquirer" class="error">Please select acquirer</span>
							</div>
							<!-- /.position-relative -->
						</div>
						<!-- /.lpay_select_group -->  
					</div>
					<!-- /.col-md-4 -->
					
					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Refund Initiated From</label>
							<s:textfield
								type="text"
								id="dateFrom"
								name="refundInitiatedFrom"
								class="lpay_input"
								autocomplete="off"
								readonly="true"
							/>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 -->

					<div class="col-md-4 mb-20">
						<div class="lpay_input_group">
							<label for="">Refund Initiated To</label>
							<s:textfield
								type="text"
								id="dateTo"
								name="refundInitiatedTo"
								class="lpay_input"
								autocomplete="off"
								readonly="true"
							/>
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 -->

					<div class="col-md-12 text-center">
						<button
							class="lpay_button lpay_button-md lpay_button-secondary"
							id="downloadBtn"
							onclick="downloadInitiatedRefundFile(event, this)">Download</button>
					</div>
					<!-- /.col-md-12 text-center -->
				</div>
				<!-- /.row -->
			</form>
		</section>
		<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
	</body>
</html>