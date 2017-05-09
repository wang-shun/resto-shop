<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8"/>
    <base href="<%=basePath%>">
    <title>店铺管理-登录</title>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8">
    <!-- BEGIN GLOBAL MANDATORY STYLES -->
    <link href="assets/global/plugins/font-awesome/css/font-awesome.min.css" rel="stylesheet" type="text/css"/>
    <link href="assets/global/plugins/bootstrap/css/bootstrap.min.css" rel="stylesheet" type="text/css"/>
    <!-- END GLOBAL MANDATORY STYLES -->
    <!-- BEGIN PAGE LEVEL STYLES -->
    <link href="assets/pages/css/login.css" rel="stylesheet" type="text/css"/>
    <!-- END PAGE LEVEL SCRIPTS -->
    <!-- BEGIN THEME STYLES -->
    <link href="assets/global/css/components.css" id="style_components" rel="stylesheet" type="text/css"/>
    <link href="assets/global/css/plugins.css" rel="stylesheet" type="text/css"/>
    <!-- END THEME STYLES -->
    <link rel="shortcut icon" href="assets/pages/img/favicon.ico" />
</head>
<!-- BEGIN BODY -->
<body class="login">
<!-- BEGIN SIDEBAR TOGGLER BUTTON -->
<div class="menu-toggler sidebar-toggler">
</div>
<!-- END SIDEBAR TOGGLER BUTTON -->
<!-- BEGIN LOGO -->
<div class="logo">
    <a href="index.html">
        <img src="assets/pages/img/Resto+.png" style="height: 40px;" alt=""/>
    </a>
</div>
<!-- END LOGO -->
<!-- BEGIN LOGIN -->
<div class="content">
    <!-- BEGIN LOGIN FORM -->
    <form class="login-form" method="post" action="branduser/login">
        <div style="text-align: center">
            <strong>为确保高峰期间系统稳定性，后台开放时间暂时调整为上午《00:00:00 -- 11:00:00》下午《13:00:00 - 17:00:00》
            晚上《19:00:00 - 00:00:00》,谢谢您的配合，如需紧急修改后台数据
                联系电话：400 805 1711</strong>
        </div>
    </form>
    <!-- END LOGIN FORM -->
</div>
<div class="copyright">
    &copy;  上海餐加企业管理咨询有限公司　v3.1.0
</div>
<!-- END LOGIN -->
<script src="assets/global/plugins/jquery.min.js" type="text/javascript"></script>
<script src="assets/global/plugins/jquery-migrate.min.js" type="text/javascript"></script>
<script src="assets/global/plugins/bootstrap/js/bootstrap.min.js" type="text/javascript"></script>
<!-- END CORE PLUGINS -->
<!-- BEGIN PAGE LEVEL PLUGINS -->
<script src="assets/global/plugins/jquery-validation/js/jquery.validate.min.js" type="text/javascript"></script>
<script src="assets/global/plugins/jquery-validation/js/localization/messages_zh.js" type="text/javascript" charset="utf-8"></script>
<!-- END PAGE LEVEL PLUGINS -->
<script>

</script>
<!-- END JAVASCRIPTS -->
</body>
<!-- END BODY -->
</html>