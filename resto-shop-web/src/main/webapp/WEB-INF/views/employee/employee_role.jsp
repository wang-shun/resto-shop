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
		<form class="form-inline" role="form"  action="/employee/assign_form"  @submit.prevent="save">
			<input type="text" name="employeeId" value="${employeeId}"/>
			<table class="table table-bordered">
				<tr v-for="shop in shopERoles">
					<td style="width: 30%"><input type="checkbox" name="shopName" id="{{shop.shopId}}" value={{shop.shopId} />{{shop.shopName}}</td>
					<td style="width: 70%">
						<div class="checkbox" v-for = "eRole in shop.eRolelist">
							<label>
								<input type="checkbox" id="{{eRole.id}}" name="{{eRole.id}}" value={{eRole.id}}/> {{eRole.roleName}}
							</label>
						</div>
					</td>
				</tr>
			</table>
			<button type="submit" class="btn btn-primary">提交</button>
		</form>
	</div>
</div>


<script>
	$(function(){

		var employeeId =  $("[name='employeeId']").val();

		var vm = new Vue({
				el:"#empRole",
				data:{
					shopERoles:[],
				},
			methods:{
				showAllShopAndRoles : function() {
					$.ajax({
						url:'employee/listAllShopsAndRoles',
						success:function (result) {
							console.log(result.data);
							vm.shopERoles=result.data;
						}
					})

				},
				showSelected : function(employeeId) {

				},
				save : function () {
//					$(this.shopERoles).each(function (index,item) {
//						console.log($("#"+item.shopId));
//						debugger;
//						if($("#"+item.shopId).attr("checked")){
//							console.log(item.shopName);
//						}else{
//							console.log("未选择"+item.shopName);
//						}
//
//					})
					$("input:checked").each(function(i,d){
						console.log(d);
					})
				}
				},
				created : function(){
					var that = this;
					//加载所有的多选框数据(店铺数据和角色数据)
					that.showAllShopAndRoles();
					//已经有的店铺的角色设为选中状态
					that.showSelected(employeeId);

				}
		})


	});


</script>
