<%@ page language="java" pageEncoding="utf-8"%>
<%@include file="tag-head.jsp" %>
<div class="page-header navbar navbar-fixed-top">
	<!-- BEGIN HEADER INNER -->
	<div class="page-header-inner ">
		<!-- BEGIN LOGO -->
		<div class="page-logo">
			<a href="index.html"> <img	src="<%=resourcePath%>/assets/layouts/layout/img/logo.png"	alt="logo" class="logo-default" />
			</a>
			<div class="menu-toggler sidebar-toggler"></div>
		</div>
		<!-- END LOGO -->
		<!-- BEGIN RESPONSIVE MENU TOGGLER -->
		<a href="javascript:;" class="menu-toggler responsive-toggler"
			data-toggle="collapse" data-target=".navbar-collapse"> </a>
		<!-- END RESPONSIVE MENU TOGGLER -->
		<!-- BEGIN TOP NAVIGATION MENU -->
		<div class="top-menu">
			<ul class="nav navbar-nav pull-right">
				<!-- BEGIN USER LOGIN DROPDOWN -->
				<!-- DOC: Apply "dropdown-dark" class after below "dropdown-extended" to change the dropdown styte -->
				<s:hasRole name="superAdmin">
					<li class="dropdown dropdown-extended " id="menu-manager"><a
						href="javascript:;" class="dropdown-toggle"> <i
							class="fa fa-list"></i> <span> 菜单管理 </span>
					</a></li>
				</s:hasRole>
				<li class="dropdown dropdown-user"><a href="javascript:;"
					class="dropdown-toggle" data-toggle="dropdown"
					data-hover="dropdown" data-close-others="true"> <span
						class="username username-hide-on-mobile"><span>${current_shop_name }：</span>
							<span class="namespan">${USER_INFO.username } </span></span> <i class="fa fa-angle-down"></i>
				</a>
					<ul class="dropdown-menu dropdown-menu-default">
						<li id="updateUserInfo"><a href="javascript:;"> <i
								class="icon-user"></i>个人信息
						</a></li>
						<li class="divider"></li>
						<li><a href="branduser/logout"> <i class="icon-key"></i>
								退出
						</a></li>
					</ul></li>
				<!-- END USER LOGIN DROPDOWN -->
			</ul>
		</div>
		<!-- END TOP NAVIGATION MENU -->
	</div>
	<!-- END HEADER INNER -->
</div>