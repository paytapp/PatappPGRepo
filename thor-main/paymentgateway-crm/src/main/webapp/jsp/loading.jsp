<!DOCTYPE html>
<%@taglib prefix="s" uri="/struts-tags"%>
<html>

<head>
  <title>Loading...</title>
  <link rel="icon" href="../image/favicon-32x32.png">
  <meta http-equiv="refresh" content="1; url=<s:url />"></meta>
  <link rel="stylesheet" href="../css/loader-animation.css">
  <script>
    function refresh() {
      //update src attribute with a cache buster query
      location.reload(true);
      setTimeout("refresh();", 1000);
    }
  </script>
</head>
 
<body onload="">
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