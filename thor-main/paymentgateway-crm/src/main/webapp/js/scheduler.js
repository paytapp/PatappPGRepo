$(document).ready(function(){
    var _token = $("[name='token']").val();
    var _selectedJobs = [];
    var _showDataInputBox = [];

    // create checkbox select option 
    function createNewSelect(id){
      var checkBox = "";
      $("#"+id+" option").each(function(e){
        var optionsValue = $(this).val();
        var optionsText = $(this).text();
        checkBox += "<label class='checkbox-label unchecked' for='1'>"+optionsText;
        checkBox += "<input type='checkbox' value='"+optionsValue+"' data-name='"+optionsText+"' id='"+optionsValue+"-check'>";
        checkBox += "</label>"
      })
      $("."+id+"-div").append(checkBox);
    }
    createNewSelect("acquirerType");
    createNewSelect("txnType");
    createNewSelect("status");
    createNewSelect("paymentType");
    createNewSelect("mopType");
    createNewSelect("payId");
    // createNewSelect("jobFrequency");''


    // show div when click create new job button
    $("#jobType").on("change", function(e){
      var _val = $(this).val();
      if(_val == "All"){
        $(".create-job-btn").addClass("d-none");
      }else{
        $(".create-job-btn").removeClass("d-none");
      }
      $(".static-select").addClass("d-none");
      getJobTypesOnLoad();
    });

    // show div accroding to whats coming
    function showFields(data){
      var createElement = "";
      var checkBox = "";
      $(".dynamically-added-input").html("");
      $(".dynamically-added-checkbox").html("");
      if(data.length > 0){
      for(var i = 0; i < data.length; i++){
        var _closestDiv = $("#"+data[i]).closest(".col-md-3");
        var _removeUnderScore = data[i].replace("_", " ");
        console.log(_removeUnderScore);
        if(_closestDiv.length == 1){
        _closestDiv.removeClass("d-none");
        _closestDiv.addClass("active-input");
        }else{
          if(data[i] != "autoRefund"){
            var _tempName = data[i];
            var _placeHolder = "";
            var newReg = /[A-Z]/;
            for(var j = 0; j < _tempName.length; j++){
                if(_tempName[j].match(newReg)){
                  _placeHolder += " "+_tempName[j];
                }else{
                  _placeHolder += _tempName[j];
                }
            }
            createElement += "<div class='col-md-3 mb-20 active-input'><div class='acquirer-input '>";
            createElement += "<input class='lpay-input' placeholder='"+ _placeHolder +"'  data-id='"+ data[i] +"'>";
            createElement += "</div></div>";
          }else{
            checkBox += "<div class='col-md-3 active-input'><div class='aqquirer-box acquirer-input'><label class='checkbox-label unchecked' for='"+data[i]+"-check'>Auto Refund";
            checkBox += "<input type='checkbox' value='true' id='"+data[i]+"-check' data-id='"+data[i]+"'>";
            checkBox += "</label></div></div>";
            }

          }
      }
      $(".dynamically-added-input").append(createElement);
      $(".dynamically-added-checkbox").append(checkBox);
      }
    }

    // open select box
    $(".lpay-select").attr("disabled", true);
    $(".open-select-box").on("click", function(e){
      $(this).next().addClass("active-box");
      if($("#scheduler-manage-div").hasClass("active-edit")){
      }else{
        _selectedJobs = [];
        _showDataInputBox = [];
      }
    });

    // remove checkbox when blur outside of input box 
    $(document).click(function(e) {
      $('.aqquirer-box').not($('.aqquirer-box').has($(e.target))).children('.select-box-custom').removeClass('active-box'); 
    });

    // set id to label for click label and checked
    $(".checkbox-label").each(function(e){
      var _getId = $(this).find("input[type='checkbox']").attr("id");
      $(this).attr("for", _getId);
      if($(this).find("input[type='checkbox']").is(":checked")){
        $(this).addClass("checbox-checked");
      }
    });

    // check input boxes on change 
    $("body").on("change", ".checkbox-label input", function(e){
      var _parent = $(this).closest(".aqquirer-box");
      var _addArray = $(this).val();
      var _addName = $(this).attr("data-name");
      if($(this).is(":checked")){
        $(this).closest("label").addClass("checkbox-checked");
        _selectedJobs.push(_addArray);
        _showDataInputBox.push(_addName);
        _parent.find(".open-select-box input").val(_selectedJobs);
        _parent.find(".showData").text(_showDataInputBox);
      }else{
        var getLast = $(this).val();
        var index = _selectedJobs.indexOf(getLast);
        if (index > -1) {
          _selectedJobs.splice(index, 1);
          _showDataInputBox.splice(index, 1);
        }
        $(this).closest("label").removeClass("checkbox-checked");
        _parent.find(".open-select-box input").val(_selectedJobs); 
        _parent.find(".showData").text(_showDataInputBox);
      }
    })
  

    // get job on load
    function getJobTypesOnLoad(){
      $("body").removeClass("loader--inactive");
      var _jobType = $("#jobType").val();
      if(_jobType == "" || _jobType == undefined || _jobType == null || _jobType == "All"){
        _jobType = null;
      }
      $.ajax({
        type: "post",
        url: "schedulerJobs",
        data: {"struts.token.name": "token", "token": _token, "jobType": _jobType},
        success: function(data){
          tableData(data.jobs.activeJob);
          showFields(data.jobs.jobParams);
          setInterval(function(){
            $("body").addClass("loader--inactive");
          }, 500);
        },
      })
    }

    getJobTypesOnLoad();

    // create show feild on button
    $("#createJob").on("click", function(e){
      var _jobType = $("#jobType").val();
      if(_jobType == "All"){
        alert("Please select job type first");
      }else{
        $("#scheduler-manage-div").removeClass("d-none");
      }
    });

    $(".active-input .acquirer-input input").on("change keyup", function(){
      $(this).removeClass("red-line");
    })

    // creating new job
    function createNewJob(url){
      $("body").removeClass("loader--inactive");
      var _flag = false;
      $(".active-input .acquirer-input input").each(function(e){
        var _this = $(this).val();
        if(_this != ""){
          $(this).removeClass("red-line");
          _flag = true;
        }else{
          $(this).addClass("red-line");
          _flag = false;
        }
      }) 
      if(_flag){
        var _jobId = $("[data-id='jobId']").val();
        if(_jobId == "" || _jobId == undefined || _jobId == null){
          _jobId = null;
        }
          var _dataObj = {
              "token": _token,
              "struts.token.name": "token",
              "jobFrequency" : $("#jobFrequency").val(),
              "jobType" : $("#jobType").val(),
              "jobId": _jobId
          };
          $(".active-input").each(function(e){
              var _key = $(this).find(".acquirer-input input").attr("data-id");
              var _val = $(this).find(".acquirer-input input").val();
              if(_val != "true"){
                _dataObj[_key] = _val;
              }else{
                
                if($("#autoRefund-check").is(":checked")){
                  
                  _dataObj[_key] = true;
                }else{
                 
                  _dataObj[_key] = false;
                }
              }
          });
          $.ajax({
              type: "post",
              url: url,
              data: _dataObj,
              success: function(data){
                 setInterval(function(){
                   $("body").addClass("loader--inactive");
                  }, 500);
                  setInterval(function(){
                    $(".save-status-div").slideDown();
                    location.reload();
                  }, 700);
              }
          });
      }
    }


    // cancle btn 
    $("#cancel-btn").on("click", function(e){
      $("#scheduler-manage-div").addClass("d-none");
      $("#jobHeading").text("Create New Jobs");
      $(".link-edit").attr("disabled", false);
      $("#jobType").val("All");
      $(".lpay-input").val("");
      $("#scheduler-manage-div").removeClass("active-edit");
      $("#save-btn").removeClass("d-none");
      $("#update-btn").addClass("d-none");
      $(".static-select").addClass("d-none");
    })

    $("#save-btn").on("click", function(e){
      createNewJob("createSchedulerJob");
    });

    // date time picker
    var _today = new Date();
    $('#jobTime').datetimepicker({
      format: 'DD-MM-YYYY HH:mm', // HH = 00 01 ... 22 23
      showClose: true,
      minDate: _today,
      ignoreReadonly: true
    });

    // default data function
    function tableData(data){
      $("#jobData").html("");
      if(data.length > 0){
        $(".table-foot").addClass("d-none");
        var createTr = "";
        createTr += "<tr>";
        for(var i = 0; i < data.length; i++){
          createTr += "<td>"+data[i]["jobType"]+"<input type='hidden' class='jobId' value='"+data[i]["jobId"]+"'></td>";
          createTr += "<td>"+data[i]["jobTime"]+"</td>";
          createTr += "<td>"+data[i]["jobFrequency"]+"</td>";
          createTr += "<td>"+data[i]["jobDetails"]+"</td>";
          createTr += "<td class='action-btn'><button class='edit-btn'><img src='../image/edit.png' ></button><button class='trash-btn'><img src='../image/trash.png' ></button></td>";
          createTr += "</tr>";
        }
        $("#jobData").append(createTr);
      }else{
        $(".table-foot").removeClass("d-none");
      }
    }
    
    // delete job
    $("body").on("click", ".trash-btn", function(e){
      e.preventDefault();
      var _parent = $(this).closest("tr");
      $("#jobData tr").removeClass("active-tr");
      _parent.addClass("active-tr");
      $("#fancybox").fancybox({
          'overlayShow': true
      }).trigger('click');
    });

    function deleteAction(e){
      window.scrollTo(0, 0);
      var _parent = $(".active-tr");
      var _jobId = _parent.find(".jobId").val();
      $("body").removeClass("loader--inactive");
      $.ajax({
          type: "post",
          url: "deleteSchedulerJob",
          data: {"jobId": _jobId, "token": _token},
          success: function(data){
              // console.log(data);
              $(".delete-status-div").slideDown();
              location.reload();
              setInterval(function(){
                $("body").addClass("loader--inactive");
              }, 500);
          },
          error: function(data){
          }
      });
  }

    $("body").on("click", "#confirm-btn", function(e){
      deleteAction();
      $.fancybox.close();
    });

    $("body").on("click", "#cancel-btn", function(e){
        $.fancybox.close();
    })

    // update job

    $("body").on("click", "#update-btn", function(e){
      createNewJob("updateSchedulerJob");
    })

    $("body").on("click",".edit-btn" ,function(e){
      $("#jobHeading").text("Edit Job");
      $(".link-edit").attr("disabled", true);
      $("body").removeClass("loader--inactive");
      $(".dynamically-added-input").html("");
      $(".dynamically-added-checkbox").html("");
      $("#save-btn").addClass("d-none");
      $("#update-btn").removeClass("d-none");
      $("#scheduler-manage-div").addClass("active-edit");
      $("#scheduler-manage-div").removeClass("d-none");
      var _parent = $(this).closest("tr");
      $("#jobData tr").removeClass("active-row");
      _parent.addClass("active-row");
      var _jobId = $(".active-row").find(".jobId").val();
      $("[data-id='jobId']").val(_jobId);
      console.log(_jobId);
      $.ajax({
        type: "post",
        url: "schedulerJobs",
        data: {"jobId": _jobId, "token": _token, "struts.token.name": "token"},
        success: function(data){
          var newObj = data.jobs.activeJob.jobParams;
          // console.log(data.jobs.activeJob.jobFrequency);
          
          $("#jobType").val(data.jobs.activeJob.jobType);
          $("[data-id='jobDetails']").val(data.jobs.activeJob.jobDetails);
          $("#jobFrequency").val(data.jobs.activeJob.jobFrequency);
          $("[data-id='jobTime']").val(data.jobs.activeJob.jobTime);
          var dataObj = JSON.parse(newObj);
          var createElement = "";
          var checkBox = "";

            for(key in dataObj){
              _showDataInputBox = [];
              var _parent = $("[data-id="+key+"]").closest(".static-select");
              if(_parent.length == 1){
                $("[data-id="+key+"]").val(dataObj[key]);
                if(dataObj[key].length > 1){
                  for(var i = 0; i < dataObj[key].length; i++){
                    var newVal = $("[data-id="+key+"]").closest(".static-select").find(".checkbox-label input[value='"+dataObj[key][i]+"']").attr("data-name");
                    _showDataInputBox.push(newVal);
                  }
                }else{
                  _showDataInputBox += $("[data-id="+key+"]").closest(".static-select").find(".checkbox-label input[value='"+dataObj[key]+"']").attr("data-name");

                }
                $("[data-id="+key+"]").next(".showData").text(_showDataInputBox);
                $("[data-id='"+key+"']").closest(".col-md-3").addClass("active-input");
                $("[data-id='"+key+"']").closest(".col-md-3").removeClass("d-none");
              }else{
                // console.log(key);
                if(dataObj[key] == true || dataObj[key] == false){
                  if(dataObj[key] == false){
                  checkBox += "<div class='col-md-3 active-input'><div class='aqquirer-box acquirer-input'><label class='checkbox-label unchecked' for='"+key+"-check'>Auto Refund";
                  }else{
                    checkBox += "<div class='col-md-3 active-input'><div class='aqquirer-box acquirer-input'><label class='checkbox-label checkbox-checked unchecked' for='"+key+"-check'>Auto Refund";
                  }
                  checkBox += "<input type='checkbox' value='true' id='"+key+"-check' data-id='"+key+"'>";
                  checkBox += "</label></div></div>";
                }else{
                  createElement += "<div class='col-md-3 mb-20 active-input'><div class='acquirer-input '>";
                  createElement += "<input class='lpay-input' value='"+dataObj[key]+"' placeholder='"+ key +"'  data-id='"+ key +"'>";
                  createElement += "</div></div>";
                }
              }
            }
            $(".dynamically-added-input").append(createElement);
            $(".dynamically-added-checkbox").append(checkBox);

            $("body").addClass("loader--inactive");
        }
      })
    })

    $("body").on("click", ".active-edit .static-select .lpay-input", function(e){
      _selectedJobs = $(this).val().split(",");
      _showDataInputBox = $(this).next(".showData").text().split(",");
      for(var i = 0; i < _selectedJobs.length; i++){
         $("input[value='"+_selectedJobs[i]+"']").attr("checked", "checked");
         $("input[value='"+_selectedJobs[i]+"']").closest("label").addClass("checkbox-checked");
      }
    })


  });