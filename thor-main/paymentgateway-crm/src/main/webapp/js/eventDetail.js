
    var regex = new RegExp("(.*?)\.(csv)$");
    
    function triggerValidation(el) {
      if (!(regex.test(el.value.toLowerCase()))) {
        el.value = '';
        alert('Please select correct file format');
      }
    }

    $(document).ready(function() {
        // AJAX HANDLER
        var ajaxHandler = function(props) {
            $.ajax({
                type: "POST",
                url: props.url,
                data: props.data,
                success: function(data) {
                    console.group(props.url);
                    console.groupEnd();

                    fetchData(data.aaData);
                },
                error: function() {
                    alert("Try again, Something went wrong!");
                }
            });
        }

        $('input:file').change(function() {
            if ($(this).val()=="") {
                $('input:submit').attr('disabled',true);
            } else {
                $('input:submit').attr('disabled',false);
            }
        });
    
        $('#example').DataTable( {
            dom: 'B',
            buttons: [
                'csv'
            ]
        });
    
        // SLIDE SWITCH
        $("body").on("click", ".slideSwitch", function(e) {
            let that = $(this);
            let checkbox = that.find("input[type='checkbox']");
            let isChecked = checkbox.is(":checked");
            if(isChecked == false) {
                that.addClass("active");
                that.removeClass("inactive");
                checkbox.attr("checked", true);                
            } else {
                that.removeClass("active");
                that.addClass("inactive");
                checkbox.attr("checked", false);
            }
        });
    
    
        // EDIT BUTTON HANDLER
        $("body").on("click", ".btn-edit", function(e) {
            e.preventDefault();
            $(this).closest("tr").addClass("editable--active");
        });

        // CANCEL EDITABLE ROW
        $("body").on("click", ".btn-cancel", function(e) {
            e.preventDefault();
            $(this).closest("tr").removeClass("editable--active");
        });
    
        // FETCH DATA
        var fetchData = function(data) {
            let tableWrapper = $("#issuerDetail");
            document.getElementById("issuerDetail").querySelector("tbody").innerHTML = "";
            let detailBox = "";            
    
            if(data.length > 0) {
                for(let i = 0; i < data.length; i++) {
                    var active = "";
                    var checked = "";

                    detailBox += '<tr>'; // ROW START
    
                    // FIRST COLUMN START
                    detailBox += '<td class="border-right-grey-lighter"><span>'+ data[i]["merchantName"] +'</span><input type="hidden" id="payId" name="payId" value="'+ data[i]["payId"] +'"><input type="hidden" id="merchantName" name="merchantName" value="'+ data[i]["merchantName"] +'"></td>';
                    
                    // SECOND COLUMN START
                    detailBox += '<td class="border-right-grey-lighter"><span>'+ data[i]["issuerName"] +'</span><input type="hidden" name="issuerName" value="'+ data[i]["issuerName"] +'"><input type="hidden" name="slabId" value="'+data[i]["id"]+'"></td>';

                    // THIRD COLUMN START;
                    var isAlwaysOn = data[i]["alwaysOn"];                    
                    if(isAlwaysOn) {
                        active = "active";
                        checked = "checked";
                    }

                    detailBox += '<td class="border-right-grey-lighter"><span>'+ data[i]["paymentType"] +'</span><input type="hidden" name="paymentType" value="'+ data[i]["paymentType"] +'"></td>';
                    detailBox += '<td class="border-right-grey-lighter"><span>'+ data[i]["tenure"] +'</span><input type="hidden" name="tenure" value="'+ data[i]["tenure"] +'">';
                    detailBox += '<td class="border-right-grey-lighter"><span>'+ data[i]["rateOfInterest"] +'</span><input type="hidden" value="'+ data[i]["rateOfInterest"] +'"><input type="text" name="rateOfInterest" class="input-edit form-control py-5 px-5 height-auto font-size-14 max-width-100" onkeyup="onlyNumericKey(this, event, 2);" onkeypress="onlyNumericKey(this, event, 2);" value="'+ data[i]["rateOfInterest"] +'"></td>';

                    // SLIDE SWITCH
                    detailBox += '<td><div class="slideSwitch '+ active +'"><input type="checkbox" id="alwaysOn-'+ i +'" name="alwaysOn-'+ i +'" '+ checked +' /><label for="alwaysOn-'+ i +'"></label></div>';

                    // ACTIONS
                    detailBox += '<td><div class="actionBtns visual-mode"><a  href="javascript:;" class="btn-edit bg-color-primary-light-2 color-primary hover-color-primary border-radius-6 font-size-15 d-flex align-items-center justify-content-center"><i class="fa fa-pencil-alt"></i></a><a href="javascript;"  class="btn-delete bg-color-red-lightest color-red hover-color-red border-radius-6 font-size-15 d-flex align-items-center justify-content-center"><i class="fa fa-trash"></i></a></div><div class="actionBtns edit-mode"><a href="#" class="btn-save bg-color-green-lightest color-green hover-color-green border-radius-6 font-size-15 d-flex align-items-center justify-content-center"><i class="fa fa-check"></i></a><a href="#" class="btn-cancel bg-color-red-lightest color-red hover-color-red border-radius-6 font-size-15 d-flex align-items-center justify-content-center"><i class="fa fa-times"></i></a></div></td>';
                }
                
            } else {
                detailBox += '<tr><td colspan="7">No data available</td></tr>'
            }

            $(detailBox).appendTo(tableWrapper.find("tbody"));
        }
    
        // SHOW ALL DATA
        function showAllPayments() {
            $.ajax({
                type: "GET",
                url: "IssuerDetailsActionAllSlab",
                success: function(data) {
                    console.group("showAllPayments()")
                    console.groupEnd();

                    fetchData(data.aaData);
                },
                error: function() {
                    alert("Try again, Something went wrong!");
                }
            });
        }

        showAllPayments();
    
        $("body").on("click", ".btn-save", function(e){
            e.preventDefault();
            var parent = $(this).closest("tr");
            parent.removeClass("editable--active");
            
            var slabId = parent.find("input[name='slabId']").val();
            var rateOfInterest = parent.find("input[name='rateOfInterest']").val();
            var isAlwaysOn = parent.find(".slideSwitch input[type='checkbox']").is(":checked");

            if(rateOfInterest !== "") {
                ajaxHandler({
                    url: "editEmiIssuerDetailsAction",
                    data: {
                        slabId: slabId,
                        rateOfInterest: rateOfInterest,
                        alwaysOnOff: isAlwaysOn
                    }
                });
            } else {
                alert("Please fill the blank fields.");
                return false;
            }
        });
        
        // DELETE INDIVIDUAL ROW DATA
        $("body").on("click", ".btn-delete", function(e){
            e.preventDefault();
            var isAgree = confirm("Are you sure you want to delete.");
            
            if(isAgree) {
                var parent = $(this).closest("tr");
                parent.removeClass("editable--active");
                var slabId = parent.find("input[name='slabId']").val();
    
                ajaxHandler({
                    url: "DeleteIssuerDetailAction",
                    data: {
                        slabId: slabId
                    }
                });
            }
        });
    
        // FILTER DATA    
        $("body").on("change", ".view-emi-filter", function(e){
            var payId = $("#merchantId").val();
            var issuerName = $("#acquirer").val();
            
            if(payId != "" && issuerName != "") {
                ajaxHandler({
                    url: "IssuerDetailsFilterAction",
                    data: {
                        payId: payId,
                        issuerName: issuerName
                    }
                });
            }            
        });
    });
    
    