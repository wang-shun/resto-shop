<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<div class="table-div">
	<div class="table-operator">
		<button class="btn green" data-formurl="role/add" data-formaction="role/addData">Add</button>
	</div>
	<div class="clearfix"></div>
	<div class="table-filter">&nbsp;</div>
	<div class="table-body">
		<table class="table table-striped table-hover table-bordered "></table>
	</div>
</div>

<script>
	(function(){
		$("[data-formurl]").click(function(){
			var url = $(this).data("formurl");
			var action = $(this).data("formaction");
			C.loadForm({
				url:url,
				formaction:action
			});
		});
		var $table = $(".table-body>table");
		var tb = $table.DataTable({
			ajax : {
				url : "role/listData",
				dataSrc : ""
			},
			columns : [
				{
					title:"角色名",
					data:"roleName"
				},
				{
					title:"描述",
					data:"description"
				},
				{
					title:"标示",
					data:"roleSign"
				}
				, {
					title : "操作",
					data : "id",
					createdCell:function(td,tdData){
						var operator = [];
						<s:hasPermission name="role/delete">
						var delBtn = C.createDelBtn(tdData,"role/delete");
						operator.push(delBtn);
						</s:hasPermission>
						<s:hasPermission name="role/edit">
						var editBtn = C.createEditBtn(tdData,"role/edit");
						operator.push(editBtn);
						</s:hasPermission>
						<s:hasPermission name="role/assign">
						operator.push(createAssignBtn(tdData));
						</s:hasPermission>				
						$(td).html(operator);
					}
				} ],
		});
		
		function createAssignBtn(roleId){
			return C.createFormBtn({
				url:"role/assign", 
				formaction:"role/assign_form",
				data:{roleId:roleId},
				name:"分配权限"			
			});
		}
		var C = new Controller(null,tb);
	}());	
</script>