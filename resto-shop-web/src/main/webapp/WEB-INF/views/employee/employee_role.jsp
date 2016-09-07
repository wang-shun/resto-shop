<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags"%>
<style>
th {
	width: 30%;
}
dt,dd{
	height: 25px;
}
</style>
<h2 class="text-center">
	<strong>给员工分配角色</strong>
</h2>
<br />
<div class="row" id="empRole">
	<div class="col-md-12">
		<form class="form-inline">
			<input type="hidden" name="employeeId" value="${employeeId}"/>
			<label class="checkbox-inline">
				<input type="checkbox" id="inlineCheckbox1" value="option1"> 1
			</label>
			<label class="checkbox-inline">
				<input type="checkbox" id="inlineCheckbox2" value="option2"> 2
			</label>
			<label class="checkbox-inline">
				<input type="checkbox" id="inlineCheckbox3" value="option3"> 3
			</label>
		</form>
	</div>
</div>



<script>
	$(function(){

		var empoyeeId = $("[name='employeeId']").val();
			//创建vue对象
		var  vm = new Vue({
			el:"empRole",
			data : {
					shopIds:{},
					roles:{}
			},
			created : function (){
					//加载所有的多选框数据(店铺数据和角色数据)
					vm.showAllShopAndRoles();
					//加载所有该员工已经有的店铺角色把多选框改为选中状态
					vm.showSelected(empoyeeId);





		})

			//查询有店铺和角色并封装成map




		// do something
	});


</script>
