<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta http-equiv="refresh" content="1; url=<s:url includeParams="all"/>"></meta>
  <!-- <meta http-equiv="X-UA-Compatible" content="ie=edge"> -->
  <title>Loading...</title>
  <link rel="stylesheet" href="../css/loader-animation.css">
  <!-- <script>
    function refresh() {
      //update src attribute with a cache buster query
      location.reload(true);
      setTimeout("refresh();", 1000);
    }
  </script> -->
</head>
<body>
<div class="loader-container">
  <div class="loader-box">
    <div class="loader--dot"></div>
    <div class="loader--dot"></div>
    <div class="loader--dot"></div>
    <div class="loader--dot"></div>
    <div class="loader--dot"></div>
    <div class="loader--dot"></div>
    <div class="loader--text"></div>
  </div>
</div>
</body>
</html>