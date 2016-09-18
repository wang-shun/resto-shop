<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>


<div id="control">
    <%--<table class="table table-bordered">--%>
        <%--<thead>--%>
        <%--<tr>--%>
            <%--<th>序号</th>--%>
            <%--<th>角色</th>--%>
            <%--<th>买单</th>--%>
            <%--<th>申请额度</th>--%>
            <%--<th>同意申请</th>--%>
            <%--<th>发短信</th>--%>
        <%--</tr>--%>
        <%--</thead>--%>
        <%--<tbody>--%>
        <%--<tr>--%>
            <%--<td>1</td>--%>
            <%--<td>经理</td>--%>
            <%--<td><input type="checkbox" class="make-switch" checked data-on-color="primary" data-off-color="info" data-size="small"></td>--%>
            <%--<td><input type="checkbox" class="make-switch" checked data-on-color="primary" data-off-color="info" data-size="small"></td>--%>
            <%--<td><input type="checkbox" class="make-switch" checked data-on-color="primary" data-off-color="info" data-size="small"></td>--%>
            <%--<td><input type="checkbox" class="make-switch" checked data-on-color="primary" data-off-color="info" data-size="small"></td>--%>
        <%--</tr>--%>
        <%--<tr>--%>
            <%--<td>2</td>--%>
            <%--<td>服务员</td>--%>
            <%--<td><input type="checkbox" class="make-switch" checked data-on-color="primary" data-off-color="info" data-size="small"></td>--%>
            <%--<td><input type="checkbox" class="make-switch" checked data-on-color="primary" data-off-color="info" data-size="small"></td>--%>
            <%--<td><input type="checkbox" class="make-switch" checked data-on-color="primary" data-off-color="info" data-size="small"></td>--%>
            <%--<td><input type="checkbox" class="make-switch" checked data-on-color="primary" data-off-color="info" data-size="small"></td>--%>
        <%--</tr>--%>
        <%--<tr>--%>
            <%--<td>3</td>--%>
            <%--<td>店长</td>--%>
            <%--<td><input type="checkbox" class="make-switch" checked data-on-color="primary" data-off-color="info" data-size="small"></td>--%>
            <%--<td><input type="checkbox" class="make-switch" checked data-on-color="primary" data-off-color="info" data-size="small"></td>--%>
            <%--<td><input type="checkbox" class="make-switch" checked data-on-color="primary" data-off-color="info" data-size="small"></td>--%>
            <%--<td><input type="checkbox" class="make-switch" checked data-on-color="primary" data-off-color="info" data-size="small"></td>--%>
        <%--</tr>--%>
        <%--</tbody>--%>
    <%--</table>--%>


    <table class="table table-bordered">
        <thead>
        <tr>
            <th>序号</th>
            <th>角色</th>
            <th v-for="p in permission">{{p.permissionName}}</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(index,r) in eroleList">
            <td>{{index+1}}</td>
            <td>{{r}}</td>
            <td v-for="rp in r.permissions/"><input type="checkbox" class="make-switch"  data-on-color="primary" data-off-color="info" data-size="small" >





            </td>
        </tr>
        </tbody>
    </table>

	</div>
	

<script>
	(function(){
	    var vm = new Vue({
	        el:'#control',
            data:{
                permission:[],
                eroleList :[]
            },
            ready: function () {
                var that = this;
                //查询所有的角色用于显示行
                $.ajax({
                    url:"permission/list_all",
                    success:function (result) {
                        that.permission=result;
                    }
                });
                $.ajax({
                    url:"rolepermission/list_all",
                    success:function (result) {
                        console.log(result.data);
                    }
                })
            },
        })
	}());
	

</script>
