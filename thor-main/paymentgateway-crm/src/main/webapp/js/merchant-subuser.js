// function handleChange() {
	// 	reloadTable();
	// }
	$(document).ready(function() {

        function handleChange(){
            reloadTable();
        }

        $("#merchant").on("change", handleChange);
        renderTable();
        function renderTable() {
                var token  = document.getElementsByName("token")[0].value;
                var buttonCommon = {
                    exportOptions: {
                        format: {
                            body: function ( data, column, row, node ) {
                                // Strip $ from salary column to make it numeric
                                if(column == 6){
                                    
                                }
                                return column === 0 ? "'"+data : column === 1 ? data.replace("&#x40;", "@") : data;
                            }
                        }
                    }
                };
                $('#datatable').dataTable({
                    dom : 'BTftlpi',
                    language: {
                        search: "",
                        searchPlaceholder: "Search records"
                    },
                    buttons : [
                    $.extend( true, {}, buttonCommon, {
                        extend : 'copyHtml5',
                        exportOptions : {
                            columns: [':visible']
                        }
                    } ), 
                    $.extend( true, {}, buttonCommon, {
                        extend : 'csvHtml5',
                        exportOptions : {
                            columns: [':visible']
                        }
                    } ), 
                    {
                        extend : 'pdfHtml5',
                        title : 'Merchant Subuser List',
                        orientation: 'landscape',
                        exportOptions : {
                            columns: [':visible']
                        },
                        customize: function (doc) {
                            doc.defaultStyle.alignment = 'center';
                             doc.styles.tableHeader.alignment = 'center';
                          }
                    }, {
                        extend : 'print',
                        title : 'Merchant Subuser List',
                        exportOptions : {
                            columns: [':visible']
                        }
                    },{
                        extend : 'colvis',
                        //collectionLayout: 'fixed two-column',
                        columns : [0, 1, 2, 3, 4, 5, 6]
                    }],				
                    "ajax" : {
                        "url" : "merchantSubUserList",
                        "type" : "POST",
                        "data" : function (d){
                            return generatePostData();
                        },
                    },
                    "bProcessing" : true,
                    "bLengthChange" : true,
                    "bAutoWidth" : false,
                    "iDisplayLength" : 10,
                    "order": [[ 5, "desc" ]],
                    "aoColumns" : [ {
                        "mData" : "payId"
                    }, {
                        "mData" : "emailId"
                    }, 
                    {
                        "mData" : "businessName"
                    },{
                        "mData" : "status"
                    },{
                        "mData" : "userType"
                    },	{
                        "mData" : "mobile"
                    },{
                        "mData" : "registrationDate", "class": "registerDate"
                    },{
                        "data" : null,
                        "visible" : false,
                        "className" : "displayNone",
                        "mRender" : function(row) {
                              return "\u0027" + row.payId;
                       }
                    } ]
                });
            }
            function reloadTable() {
                var tableObj = $('#datatable');
                var table = tableObj.DataTable();
                table.ajax.reload();
            }
            function generatePostData() {
                var token = document.getElementsByName("token")[0].value;
                var emailId = null;
                if(null != document.getElementById("merchant")){
                    emailId = document.getElementById("merchant").value;
                }else{
                    emailId = 'ALL';
                }
                var obj = {				
                        token : token,
                        emailId : emailId,
                };
    
                return obj;
            }
        });