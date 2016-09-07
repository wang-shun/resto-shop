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
					<td style="width: 30%"><input type="checkbox"  id="{{shop.shopId}}" value={{shop.shopId} />{{shop.shopName}}</td>
                    <%--<td> <input type=""text" value={{shop.shopId}}/></td>--%>
					<td style="width: 70%">
						<div class="checkbox" v-for = "eRole in shop.eRolelist">
							<label>
                                <input type="hidden" value="{{shop.shopId}}_{{eRole.id}}" name="em">
								<input type="checkbox" id="{{shop.shopId}}_{{eRole.id}}" value={{eRole.id}}/> {{eRole.roleName}}
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
                    employee:{},
                    formData:"",
				},
			methods:{
				showAllShopAndRoles : function() {
					$.ajax({
						url:'employee/listAllShopsAndRoles',
						success:function (result) {
							vm.shopERoles=result.data;
						}
					})

				},
				showSelected : function(employeeId) {

				},
				save : function () {
//					$("input:checked").each(function(i,d){
//						console.log(d);
//					})
                    $.ajax({
                        url:"employee/assign_form",
                        dataType:"post",
                        data:{
                            "employeeId":employeeId,
                            "id":vm.formData,
                        },
                        success:function (result) {
                            alert("保存成功");
                        }
                    })

				}
				},
				created : function(){
					var that = this;
					//加载所有的多选框数据(店铺数据和角色数据)
					that.showAllShopAndRoles();
					//已经有的店铺的角色设为选中状态
                    that.formData="31164cebcc4b422685e8d9a32db12ab8_1002,31164cebcc4b422685e8d9a32db12ab8_1003"
					that.showSelected(employeeId);
				}
		})


	});


</script>
