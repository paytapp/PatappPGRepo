<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>B60 Download</title>

	<script src="../js/jquery.js"></script>
	<script src="../js/jquery-ui.js"></script>
	<link rel="stylesheet" href="../css/jquery-ui.css">
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<script src="../js/common-scripts.js"></script>
	<script src="../js/bootstrap-select.min.js"></script>
	<script src="../js/jquery.dataTables.js"></script>

	<style>
		

		.hasError .error {
			margin-right: 0;
			opacity: 1;
		}

		.error {
			position: absolute;
			top: 0;
			white-space: nowrap;
			right: 0;
			opacity: 0;
			margin-right: -15px;
			color: #f00;
			transition: all .5s ease;
		}
		.mt-50 { margin-top: 50px !important; }
		.d-inline-block { display: inline-block !important; }
		.w-100 { width: 100% !important; }

		.lpay_popup-innerbox[data-type="GENERATING"] .cancel-button,
		.lpay_popup-innerbox[data-type="PROCESSING"] .cancel-button {
			display: none;
		}
	</style>
</head>
<body id="mainBody">	
	<section class="msedcl lpay_section white-bg box-shadow-box mt-70 p20">
	   <div class="row">
		  	<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">B60 Files</h2>
			 	</div>
			 <!-- /.heading_icon -->
		  	</div>
		  	<!-- /.col-md-12 -->

		  	<div class="col-md-12 mb-20">
				<ul class="lpay_tabs d-flex">
					<li class="lpay-nav-item active">
						<a href="#" class="lpay-nav-link" data-action="gettingB60FileData" data-id="b60Downlaod">B60 Downlaod</a>
					</li>
					<li class="lpay-nav-item">
						<a href="#" class="lpay-nav-link" data-action="gettingB60FileDataWithCcCode" data-id="b60DownloadWithCC">B60 Download With CC </a>
					</li>
					<li class="lpay-nav-item">
						<a href="#" class="lpay-nav-link" data-action="gettingB60MergedFileData" data-id="b60DownloadTextFile">B60 Download TXT file</a>
					</li>
				</ul>
			</div>
			<!-- col-md-12 -->

			<div class="lpay_tabs_content w-100" data-target="b60Downlaod">
				<section class="d-inline-block w-100">
					<div class="col-md-3 mb-20">
						<div class="lpay_input_group">
							<label for="generate-dateFrom">From Date</label>
							<input type="text" id="generate-dateFrom" data-var="fromDate" class="data-input lpay_input datepick">
						</div>
						<!-- /.lpay_input_group -->
					</div>
					
					<div class="col-md-3 mb-20">
						<div class="lpay_input_group">
							<label for="generate-dateTo">To Date</label>
							<input type="text" id="generate-dateTo" data-var="toDate" class="data-input lpay_input datepick">
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 -->
	
					<div class="col-md-4 input-btn-space">
						<button class="lpay_button lpay_button-md lpay_button-primary m-0" data-id="generate-btn">Generate File</button>
					</div>
					<!-- /.col-md-3 -->
				</section>

				<section class="mt-50 d-inline-block w-100">
					<div class="col-xs-12">
						<h3 class="heading_text">Download File</h3>
					</div>
					<div class="col-md-12 lpay_table_style-2">
						<div class="lpay_table">
							<table id="b60Downlaod-table" class="display" cellspacing="0" width="100%">
								<thead>
									<tr class="lpay_table_head">
                                        <th class="text-center">From Date</th>
                                        <th class="text-center">To Date</th>
                                        <th class="text-center">Created Date</th>
                                        <th class="text-center">File Name</th>
                                        <th class="text-center" width="10%">Download</th>
									</tr>
								</thead>
							</table>
						</div>
					</div>
				</section>
			</div>
			<!-- lpay_tabs_content -->

			<div class="lpay_tabs_content d-none w-100" data-target="b60DownloadWithCC">
				<section class="d-inline-block w-100">
					<div class="col-md-3">
						<div class="lpay_select_group">
							<label for="">CC Codes</label>
							<select name="cc" data-live-search="true" onchange="removeError(this)" id="cc" data-var="ccCode" class="data-input selectpicker">
								<option value="">Select Codes</option>
								<option value="02">02</option>
								<option value="03">03</option>
								<option value="04">04</option>
								<option value="05">05</option>
								<option value="06">06</option>
								<option value="07">07</option>
								<option value="08">08</option>
								<option value="09">09</option>
								<option value="10">10</option>
								<option value="11">11</option>
								<option value="12">12</option>
								<option value="13">13</option>
								<option value="14">14</option>
								<option value="15">15</option>
								<option value="16">16</option>
								<option value="17">17</option>
								<option value="18">18</option>
								<option value="19">19</option>
								<option value="20">20</option>
								<option value="21">21</option>
								<option value="22">22</option>
								<option value="23">23</option>
								<option value="24">24</option>
								<option value="25">25</option>
								<option value="26">26</option>
								<option value="27">27</option>
								<option value="28">28</option>
								<option value="29">29</option>
								<option value="30">30</option>
								<option value="31">31</option>
								<option value="32">32</option>
								<option value="33">33</option>
								<option value="34">34</option>
								<option value="35">35</option>
								<option value="36">36</option>
								<option value="37">37</option>
								<option value="38">38</option>
								<option value="39">39</option>
								<option value="40">40</option>
								<option value="41">41</option>
								<option value="42">42</option>
							</select>
							<span class="error">Please select CC codes</span>
						</div>
						<!-- /.lpay_select_group -->
					</div>
					<!-- /.col-md-3 -->
	
					<div class="col-md-3 mb-20">
						<div class="lpay_input_group">
							<label for="">From Date</label>
							<input type="text" data-var="fromDate" class="data-input lpay_input datepick">
						</div>
						<!-- /.lpay_input_group -->
					</div>
					
					<div class="col-md-3 mb-20">
						<div class="lpay_input_group">
							<label for="">To Date</label>
							<input type="text" data-var="toDate" class="data-input lpay_input datepick">
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 -->
	
					<div class="col-md-3 input-btn-space">
						<button class="lpay_button lpay_button-md lpay_button-primary m-0" data-id="generate-btn">Generate File</button>
					</div>
					<!-- /.col-md-3 -->
				</section>

				<section class="mt-50 d-inline-block w-100">
					<div class="col-xs-12">
						<h3 class="heading_text">Download File</h3>
					</div>
					<div class="col-md-12 lpay_table_style-2">
						<div class="lpay_table">
							<table id="b60DownloadWithCC-table" class="display" cellspacing="0" width="100%">
								<thead>
									<tr class="lpay_table_head">
										<th class="text-center">CC Code</th>
                                        <th class="text-center">From Date</th>
                                        <th class="text-center">To Date</th>
                                        <th class="text-center">Created Date</th>
                                        <th class="text-center">File Name</th>
                                        <th class="text-center" width="10%">Download</th>
									</tr>
								</thead>
							</table>
						</div>
					</div>
				</section>
			</div>
			<!-- lpay_tabs_content -->

			<div class="lpay_tabs_content d-none w-100" data-target="b60DownloadTextFile">
				<section class="d-inline-block w-100">
					<div class="col-md-3 mb-20">
						<div class="lpay_input_group">
							<label for="generate-dateFrom">From Date</label>
							<input type="text" id="generateText-dateFrom" data-var="fromDate" class="data-input lpay_input datepick">
						</div>
						<!-- /.lpay_input_group -->
					</div>
					
					<div class="col-md-3 mb-20">
						<div class="lpay_input_group">
							<label for="generate-dateTo">To Date</label>
							<input type="text" id="generateText-dateTo" data-var="toDate" class="data-input lpay_input datepick">
						</div>
						<!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 -->
	
					<div class="col-md-4 input-btn-space">
						<button class="lpay_button lpay_button-md lpay_button-secondary m-0" data-id="generate-btn-txt">Generate TXT File</button>
					</div>
					<!-- /.col-md-3 -->
				</section>

				<section class="mt-50 d-inline-block w-100">
					<div class="col-xs-12">
						<h3 class="heading_text">Download File</h3>
					</div>
					<div class="col-md-12 lpay_table_style-2">
						<div class="lpay_table">
							<table id="b60DownloadTextFile-table" class="display" cellspacing="0" width="100%">
								<thead>
									<tr class="lpay_table_head">
                                        <th class="text-center">From Date</th>
                                        <th class="text-center">To Date</th>
                                        <th class="text-center">Created Date</th>
                                        <th class="text-center">File Name</th>
                                        <th class="text-center" width="10%">Download</th>
									</tr>
								</thead>
							</table>
						</div>
					</div>
				</section>
			</div>
			<!-- lpay_tabs_content -->

	   </div>
	   <!-- /.row -->
	</section>
	<!-- /.lpay_section white-bg box-shadow-box mt-70 p20 -->

	<form id="txtFile" method="POST" action="downloadB60MergedFile">
		<!-- <input type="hidden" id="payoutDate" name="payoutDate"> -->
	</form>

	<form action="downloadB60File" method="POST" id="downloadB60File-form"></form>

	<div class="lpay_popup">
        <div class="lpay_popup-inner">
            <div class="lpay_popup-innerbox" data-status="default">
                <div class="lpay_popup-innerbox-success lpay-center">
                    <div class="lpay_popup_icon">
                        <img src="../image/tick.png" alt="">
                    </div>
                    <!-- /.lpay_popup_icon -->
                    <div class="lpay_popup-content">
                        <h3 class="responseMsg"></h3>
                    </div>
                    <!-- /.lpay_popup-content -->
                    <div class="lpay_popup-button">
						<button class="lpay_button lpay_button-md lpay_button-secondary cancel-button">No</button>
                        <button class="lpay_button lpay_button-md lpay_button-secondary confirm-button">Yes</button>
                    </div>
                    <!-- /.lpay_popup-button -->
                </div>
                <!-- /.lpay_popup-innerbox-success -->

                <div class="lpay_popup-innerbox-error lpay-center">
                    <div class="lpay_popup_icon">
                        <img src="../image/wrong-tick.png" alt="">
                    </div>
                    <!-- /.lpay_popup_icon -->
                    <div class="lpay_popup-content">
                        <h3 class="responseMsg"></h3>
                    </div>
                    <!-- /.lpay_popup-content -->
                    <div class="lpay_popup-button">
                        <button class="lpay_button lpay_button-md lpay_button-secondary confirmButton" onclick="resetAllInput()">Ok</button>
                    </div>
                    <!-- /.lpay_popup-button -->
                </div>
                <!-- /.lpay_popup-innerbox-success -->
            </div>
            <!-- /.lpay_popup-innerbox -->
        </div>
        <!-- /.lpay_popup-inner -->
    </div>
    <!-- /.lpay_popup -->

	<script src="../js/b60-download.js"></script>
</body>
</html>