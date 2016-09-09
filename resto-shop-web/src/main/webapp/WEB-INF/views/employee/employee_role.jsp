<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags"%>
<%@ taglib prefix="v-bind" uri="http://www.springframework.org/tags/form" %>
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
			<input type="hidden" name="employeeId" value="${employeeId}"/>
			<table class="table table-bordered">
					<c:forEach var="item" items="${elist}" >
						<tr>
							<td style="width: 25%"><input type="checkbox"  id="${item.shopId}" value=${item.shopId} class="item-edit" name="shops"  />${item.shopName}  </td>
							<c:forEach  var="i" items="${item.eRolelist}" >
								<td style="width: 25%">
									<div class="checkbox" >
										<label>
											<input type="checkbox" id="${item.shopId}_${i.id}" value=${item.shopId}_${i.id} name="spCodeId"  class="item-edit" /> ${i.roleName}
										</label>
									</div>
								</td>
							</c:forEach>
						</tr>
					</c:forEach>
			</table>
			<button type="submit" class="btn btn-primary">提交</button>
		</form>
	</div>
</div>


<script>

	$(function(){
		var employeeId =  $("[name='employeeId']").val();
		var vm;
		vm = new Vue({
			//parent: vueObj,

			el: "#empRole",
			data: {
				shopERoles: [],
				employee: {},
				formData: "",
			},
			methods: {
                save: function () {
                	var that = this;
                	//获取所有的选中的checkbox的id值
					$('input:checkbox[name=spCodeId]:checked').each(function(i){
						if(0==i){
							vm.formData = $(this).val();
						}else{
							vm.formData += (","+$(this).val());
						}
					});

                    $.ajax({
                        url: "employee/assign_form",
                        type: "post",
                        data: {
                            "employeeId": employeeId,
                            "id": vm.formData,
                        },
                        success: function (result) {
							toastr.success("保存成功");
							$("#employeeRoModal").modal('hide');
                        }
                    })
                },

			},
			ready : function () {
				//选中已经有的角色
				$.ajax({
							url :  "employee/listIds",
							data : {
								"employeeId":employeeId,
							},
							success: function (result) {
								var data = result.data;
								if(result.data!=null){
									for(var i=0 ; i<data.length ;i++){
										$("#"+data[i]).prop("checked", "checked");
									}
								}
						}
					}
				)
				//
			$('.item-edit').on('click', function() {
				var item = $(this).attr("id");//获取这个id
				var temp = $(this).is(':checked');
				//console.log(temp);

				//选择店铺的时候做全选和全部选操作
				if(item.indexOf("_")==-1){
					$("input:checkbox[name=spCodeId]").each(function(i){
							if($(this).attr("id").indexOf(item)>=0){
								$(this).prop("checked",temp)
							}
					});
				}else {
					//如果选择的是角色如果 是选中则对应的店铺
						if(temp){
							$("input:checkbox[name=shops]").each(function(i){
								var tem2 = item.substring(0,item.indexOf("_"))
								console.log(tem2);
								$("#"+temp2).prop("checked","checked");
							});
						}else {
							//如果是未选择则判断这一行的角色是否都是没选中则让店铺为未选择状态
							var temp2 = item.substring(0,item.indexOf("_"))
							var temp3 = false//默认店铺未选中
							$("input:checkbox[name=spCodeId]:checked").each(function(i){
								console.log($(this).attr("id"));
							});
							//$("#"+temp2).prop("checked",temp3);

						}



				}

			});

			},
		});



	});

</script>
