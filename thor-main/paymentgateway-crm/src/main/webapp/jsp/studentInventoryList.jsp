<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
    <!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
    <%@ taglib uri="/struts-tags" prefix="s"%>
        <html dir="ltr" lang="en-US">

        <head>
            <title>Student List</title>

            <link rel="icon" href="../image/favicon-32x32.png">
            <script type="text/javascript" src="../js/jquery.min.js"></script>
            <script src="../js/jquery.dataTables.js"></script>
            <script type="text/javascript" src="../js/dataTables.buttons.js"></script>
            <link href="../fonts/css/font-awesome.min.css" rel="stylesheet" />
            <script type="text/javascript" src="../js/daterangepicker.js"></script>
            <script type="text/javascript" src="../js/pdfmake.js"></script>
			<link href="../fonts/css/font-awesome.min.css" rel="stylesheet">
			<link rel="stylesheet" href="../css/bootstrap-select.min.css">
			<script src="../js/bootstrap-select.min.js"></script>

            <script type="text/javascript">
                $(document).ready(function() {
                    // $('#example').DataTable({
                    //     dom: 'B',
                    //     buttons: ['csv']
                    // });
                });

                $(document).ready(function() {

                    $(function() {
                        renderTable();
                    });

                    $("#submit").click(function(env) {
                        $("body").removeClass("loader--inactive");
                        reloadTable();
                    });


                });

                function decodeVal(text) {
                    return $('<div/>').html(text).text();
                }

                function renderTable() {
                    var table = new $.fn.dataTable.Api('#studentListDataTable');
                    var token = document.getElementsByName("token")[0].value;
                    var buttonCommon = {
                        exportOptions: {
                            format: {
                                body: function(data, column, row, node) {
                                    return column === 0 ? "'" + data : (column === 1 ? "'" +
                                        data : data);
                                }
                            }
                        }
                    };

                    $('#studentListDataTable').dataTable({
                        "footerCallback": function(row, data, start, end, display) {
                            var api = this.api(),
                                data;

                        },
                        "columnDefs": [{
                            className: "dt-body-right",
                            "targets": [0, 1, 2, 3, 4, 5, 6]
                        }],
                        dom: 'BTrftlpi',
                        buttons: [
                            $.extend(true, {}, buttonCommon, {
                                extend: 'copyHtml5',
                                exportOptions: {
                                    columns: [0, 1, 2, 3, 4, 5, 6]
                                },
                            }),
                            $.extend(true, {}, buttonCommon, {
                                extend: 'csvHtml5',
                                title: 'Student_list',
                                exportOptions: {

                                    columns: [0, 1, 2, 3, 4, 5, 6]
                                },
                            }), {
                                extend: 'pdfHtml5',
                                orientation: 'landscape',
                                pageSize: 'legal',
                                //footer : true,
                                title: 'Student_list',
                                exportOptions: {
                                    columns: [0, 1, 2, 3, 4, 5, 6]
                                },
                                customize: function(doc) {
                                    doc.defaultStyle.alignment = 'center';
                                    doc.styles.tableHeader.alignment = 'center';
                                }
                            }, {
                                extend: 'print',
                                //footer : true,
                                title: 'Student_list',
                                exportOptions: {
                                    columns: [0, 1, 2, 3, 4, 5, 6]
                                }
                            }, {
                                extend: 'colvis',
                                columns: [0, 1, 2, 3, 4, 5, 6]
                            }
                        ],

                        "ajax": {
                            "url": "studentSearchAction",
                            "type": "POST",
                            "data": function(d) {
                                return generatePostData(d);
                            }
                        },
                        "columnDefs": [{
                            "type": "html-num-fmt",
                            "targets": 4,
                            "orderable": true,
                            "targets": [0, 1, 2, 3]
                        }, {
                            'targets': 0,
                            'createdCell': function(td, cellData, rowData, row, col) {

                            }
                        }],

                        "fnDrawCallback": function() {

                            $("#submit").removeAttr("disabled");
                            $("body").addClass("loader--inactive");
                        },
                        "searching": false,
                        "ordering": false,

                        "processing": true,
                        "serverSide": true,
                        "paginationType": "full_numbers",
                        "lengthMenu": [
                            [10, 25, 50, 100],
                            [10, 25, 50, 100]
                        ],
                        "order": [
                            [2, "desc"]
                        ],
                        "columns": [{
                            "data": "id",
                            "className": "text-class"
                        }, {
                            "data": "REG_NUMBER",
                            "className": "txnType text-class"
                        }, {
                            "data": "STUDENT_NAME",
                            "className": "status text-class"
                        }, {
                            "data": "FATHER_NAME",
                            "className": "text-class"

                        }, {
                            "data": "STANDARD",
                            "className": "text-class"

                        }, {
                            "data": "SCHOOL",
                            "className": "text-class"

                        }, {
                            "data": "MOBILE",
                            "className": "text-class"

                        }, {
                            "data": null,
                            "className": "center",
                            "orderable": false,
                            "mRender": function(row) {
                                return '<button class="btn btn-info btn-xs btn-block btnChargeBack" style="font-size:10px;"  onClick = "editStudent(this)">Edit</button>';

                            }
                        }, ]
                    });

                    $("body").addClass("loader--inactive");

                }

                function reloadTable() {
                    $("#submit").attr("disabled", true);
                    var tableObj = $('#studentListDataTable');
                    var table = tableObj.DataTable();
                    table.ajax.reload();

                }

                function generatePostData(d) {
                    var token = document.getElementsByName("token")[0].value;
                    var regNo = document.getElementById("regNo").value;
                    var mobile = document.getElementById("mobile").value;
                    var status = document.getElementById("status").value;

                    if (regNo == '') {
                        regNo = 'ALL'
                    }
                    if (mobile == '') {
                        mobile = 'ALL'
                    }
                    if (status == '') {
                        status = 'ALL'
                    }


                    var obj = {
                        regNo: regNo,
                        mobile: mobile,
                        status: status,
                        draw: d.draw,
                        length: d.length,
                        start: d.start,
                        token: token,
                        "struts.token.name": "token",
                    };

                    return obj;
                }

                function editStudent(val) {

                    var row = val.parentElement.parentElement;
                    var regNo = row.cells[1].innerText;
                    document.getElementById("regNor").value = regNo;
                    document.getElementById("studentEditFrm").submit();

                }
            </script>



            <style>
                
                
                #HotelInv_wrapper {
                    overflow-y: auto;
                    white-space: nowrap;
                }
            </style>

        </head>


        <body id="mainBody">
			<section class="student-inventory lapy_section white-bg box-shadow-box mt-70 p20">
				<div class="row">
					<div class="col-md-12">
						<div class="heading_with_icon mb-30">
							<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
							<h2 class="heading_text">Student Inventory Filter</h2>
						</div>
						<!-- /.heading_icon -->
					</div>
					<!-- /.col-md-12 -->
					<div class="col-md-4 mb-20">
					  <div class="lpay_input_group">
						<label for="">Registration Number</label>
						<s:textfield id="regNo" class="lpay_input" name="regNo" type="text" value="" autocomplete="off"></s:textfield>
					  </div>
					  <!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 -->
					<div class="col-md-4 mb-20">
					  <div class="lpay_input_group">
						<label for="">Mobile</label>
						<s:textfield id="mobile" class="lpay_input" name="mobile" type="text" value="" autocomplete="off"></s:textfield>
					  </div>
					  <!-- /.lpay_input_group -->
					</div>
					<!-- /.col-md-4 -->
					<div class="col-md-4 mb-20">
						<div class="lpay_select_group">
						   <label for="">Status</label>
						   <s:select headerKey="" headerValue="All" class="selectpicker" list="#{'ACTIVE':'ACTIVE','INACTIVE':'INACTIVE'}" name="status" id="status" value="name" autocomplete="off" />
						</div>
						<!-- /.lpay_select_group -->  
					</div>
					<!-- /.col-md-3 -->
					<div class="col-md-12 text-center">
						<input type="button" id="submit" value="Submit" class="lpay_button lpay_button-md lpay_button-secondary">
					</div>
					<!-- /.col-md-12 -->
				</div>
				<!-- /.row -->
			</section>
			<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

			<section class="student-inventry lapy_section white-bg box-shadow-box mt-70 p20">
				<div class="row">
					<div class="col-md-12">
						<div class="heading_with_icon mb-30">
							<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
							<h2 class="heading_text">Student Inventory Data</h2>
						</div>
						<!-- /.heading_icon -->
					</div>
					<!-- /.col-md-12 -->
					<div class="col-md-12">
						<div class="lpay_table">
							<table id="studentListDataTable" class="" cellspacing="0" width="100%">
								<thead class="lpay_table_head">
									<tr>
										<th style='text-align: center'>Id</th>
										<th style='text-align: center'>Reg Number</th>
										<th style='text-align: center'>Student Name</th>
										<th style='text-align: center'>Fathers Name</th>
										<th style='text-align: center'>Standard</th>
										<th style='text-align: center'>School</th>
										<th style='text-align: center'>Mobile</th>
										<th style='text-align: center'>Action</th>
									</tr>
								</thead>
								<tfoot>
									<tr>
										<th></th>
										<th></th>
										<th></th>
										<th></th>
										<th></th>
										<th></th>
										<th></th>
										<th></th>
										<!-- <th></th> -->
									</tr>
								</tfoot>
							</table>
						</div>
						<!-- /.lpay_table -->
					</div>
					<!-- /.col-md-12 -->
				</div>
				<!-- /.row -->
			</section>
            <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
            
            <section class="student-inventory lapy_section white-bg box-shadow-box mt-70 p20">
                <div class="row">
                    <div class="col-md-12">
                        <div class="heading_with_icon mb-30">
                            <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                            <h2 class="heading_text">Student Inventory Upload</h2>
                        </div>
                        <!-- /.heading_icon -->
                    </div>
                    <!-- /.col-md-12 -->
                    <s:form action="studentInvManager" name="contact-form" method="POST" id="uploadInvForm" enctype="multipart/form-data">
                        <div class="col-md-4">
                            <label for="upload-input" class="lpay-upload">
                                <input type="file" name="fileName" accept=".csv" id="upload-input" class="lpay_upload_input">
                                <div class="default-upload">
                                    <h3>Upload Your CSV File</h3>
                                    <img src="../image/image_placeholder.png" class="img-responsive" id="placeholder_img" alt="">
                                </div>
                                <!-- /.default-upload -->
                                <div class="upload-status">
                                    <div class="success-wrapper upload-status-inner d-none">
                                        <div class="success-icon-box status-icon-box">
                                            <img src="../image/tick.png" alt="">
                                        </div>
                                        <div class="success-text-box">
                                            <h3>Upload Successfully</h3>
                                            <div class="fileInfo">
                                                <span id="fileName"></span>
                                            </div>
                                            <!-- /.fileInfo -->
                                        </div>
                                        <!-- /.success-text-box -->
                                    </div>
                                    <!-- /.success-wraper -->
                                    <div class="error-wrapper upload-status-inner d-none">
                                        <div class="error-icon-box status-icon-box">
                                            <img src="../image/wrong-tick.png" alt="">
                                        </div>
                                        <div class="error-text-box">
                                            <h3>Upload Failed</h3>
                                            <div class="fileInfo">
                                                <div id="fileName">File size too Long.</div>
                                            </div>
                                            <!-- /.fileInfo -->
                                        </div>
                                        <!-- /.success-text-box -->
                                    </div>
                                    <!-- /.success-wraper -->
                                </div>
                                <!-- /.upload-success -->
                            </label>
                        </div>
                        <!-- /.col-md-4 -->
                    </s:form>
                    <div class="col-md-12"></div>
                    <!-- /.col-md-12 -->
                    <div class="col-md-4 text-center mt-20">
                        <div class="button-wrapper lpay-center">
                            <button class="lpay_button lpay_button-md lpay_button-secondary" disabled id="studentInventrySubmit">Submit</button>
                            <table id="example" class="display" style="display: none;">
                                <thead>
                                    <tr>
                                        <th>REGISTRATION NO</th>
                                        <th>Name</th>
                                        <th>FATHERS NAME</th>
                                        <th>CLASS</th>
                                        <th>MOBILE NUMBER</th>
                                        <th>APRIL</th>
                                        <th>MAY</th>
                                        <th>JUNE</th>
                                        <th>JULY</th>
                                        <th>Total Fee </th>
                                    </tr>
                                </thead>
                            </table> 
                        </div>
                        <!-- /.button-wrapper lpay-center -->
                    </div>
                    <!-- /.col-md-12 -->
                </div>
                <!-- /.row -->
            </section>
            <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

            <s:form name="studentEditFrm" id="studentEditFrm" action="editStudentCallAction">
                <s:hidden name="regNo" id="regNor" value="" />
                <s:hidden name="token" value="%{#session.customToken}" />
            </s:form>

            <script src="../js/student-inventory.js"></script>

        </body>

        </html>