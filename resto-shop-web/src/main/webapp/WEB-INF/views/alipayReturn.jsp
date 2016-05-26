<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
			+ path + "/";
%>

<!DOCTYPE html>
<html>
<head>
<title>充值通知</title>
</head>
<body>
<%-- 	<c:if test=”${returnParams.restoResut==success}”> --%>
<!-- 		<h1>充值成功</h1> -->
<!-- 		<p class="text-success">充值成功！关掉此页面即可</p> -->
<%-- 	</c:if> --%>
<%-- 	<c:if test=”${returnParams.restoResut==fail}”> --%>
<!-- 		<h1>充值失败</h1> -->
<!-- 		<p class="text-success">充值失败！请关闭此页面，重新操作！</p> -->
<%-- 	</c:if> --%>
		<h1>最外面充值成功</h1>
		<p class="text-success">充值成功！关掉此页面即可</p>
</body>
</html>