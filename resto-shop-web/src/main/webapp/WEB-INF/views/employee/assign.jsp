<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<form role="form">
	<%--<input type="hidden" name="employeeId" value="${employeeId}"/>--%>
		<input type="text" name="employeeId" value="${employeeId}"/>
	<div id="assign-tree">正在加载店铺角色列表.....</div>
	<div id="assign-ids">
	</div>
</form>

<script>
	var C = new Controller();
	var employeeId = $("[name='employeeId']").val();
	
	C.ajax("employee/assignData",{employeeId:employeeId},function(assignData){
		loadTree(assignData.data);
	});
	
	function loadTree(assignData){
		var allERoles= assignData.eRoles;
		$('#assign-tree').jstree({
			'plugins' : [ "wholerow", "checkbox", "types" ],
			'core' : {
				"themes" : {
					"responsive" : false
				},
				'data' : allERoles
			},
			"types" : {
				"default" : {
					"icon" : "fa fa-folder icon-state-warning icon-lg"
				},
				"file" : {
					"icon" : "fa fa-file icon-state-warning icon-lg"
				}
			},
		});
		
		
		$('#assign-tree').on("changed.jstree",function(e,data){
			$("#assign-ids").html(idsToInputs(data.selected));
		});
		var hasERoles = assignData.hasERoles;
		$("#assign-tree").on("ready.jstree",function(e,data){
			console.log(data);
			data.instance.select_node(hasERoles);
		});
	}
	
	function idsToInputs(perIds){
		var ids = new Array();
		for(var i in perIds){
			var id = perIds[i];
			ids.push($("<input>",{type:"hidden",name:"pids",value:id}))
		}
		return ids;
	}
	
</script>