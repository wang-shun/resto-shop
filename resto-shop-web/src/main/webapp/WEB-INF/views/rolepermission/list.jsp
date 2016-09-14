<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>


<div id="control">
    <table class="table table-bordered">
        <thead>
        <tr>
            <%--<th style="width: 50px;">序号</th>--%>
            <th>角色</th>
            <th v-for="erole in eroleList.permissions">{{erole.roleName}}</th>
        </tr>
        </thead>
        <tbody>
        <tr v>
            <%--<td class="center"  style="width: 30px;">1</td>--%>
            <td class="center" id="ROLE_NAMETd3264c8e83d0248bb9e3ea6195b4c021">一级管理员</td>
            <td class="center" style="height: 20px;">
                <label>
                    <input name="switch-field-1" onclick="upRb('3264c8e83d0248bb9e3ea6195b4c0216','3542adfbda73410c976e185ffe50ad06')" class="make-switch" type="checkbox">
                    <span class="lbl"></span>
                </label>
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
                eroleList:[],
            },
            ready: function () {
                //查询列和行
                $.ajax({
                    url:"rolepermission/list_all",
                    success:function (result) {
                        vm.eroleList=result.data;
                    }
                    
                })
            }

        })


	}());
	

</script>
