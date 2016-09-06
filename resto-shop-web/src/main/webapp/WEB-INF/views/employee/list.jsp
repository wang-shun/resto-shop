<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>

<div class="table-div" id="control">
    <div class="clearfix"></div>
    <div class="table-filter">&nbsp;
        <!-- data-formurl返回的是新增的页面   data-formaction是添加 -->
        <button class="btn green pull-right margin-bottom-20" data-formurl="employee/add" data-formaction="employee/addData">新增用户</button>
    </div>
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
                formaction:action,

            });
        });

        var $table = $(".table-body>table");
        var tb = $table.DataTable({
            ajax : {
                url : "employee/list_all",
                dataSrc : ""
            },
            columns : [
                {
                    title:"姓名",
                    data:"name"
                },
                {
                    title:"性别",
                    data:"sex"
                },
                {
                    title:"手机号",
                    data:"telephone"
                }
                ,
                // {
                // title : "createUser",
                // data : "createUser",
                //},
                // {
                //   title : "lastLoginTime",
                // data : "lastLoginTime",
                //},
                // {
                //   title : "updateUser",
                // data : "updateUser",
                //},

                //{
                //  title : "state",
                // data : "state",
                //},
                {
                    title : "额度",
                    data : "money",
                },
                {
                    title : "二维码",
                    data : "qrCode",
                },
                {
                    title : "操作",
                    data : "id",
                    createdCell:function(td,tdData){
                        var operator = [];
                        <s:hasPermission name="employee/delete">
                        var delBtn = C.createDelBtn(tdData,"employee/delete");
                        operator.push(delBtn);
                        </s:hasPermission>
                        <s:hasPermission name="employee/modify">
                        var editBtn = C.createEditBtn(tdData,"employee/modify","employee/modify");
                        operator.push(editBtn);
                        </s:hasPermission>
                        <s:hasPermission name="employee/assign">
                        operator.push(createAssignBtn(tdData));
                        </s:hasPermission>
                        $(td).html(operator);
                    }
                } ],
        });

        function createAssignBtn(employeeId){
            return C.createFormBtn({
                url:"employee/employee_role",
                formaction:"employee/assign_form",
                data:{employeeId:employeeId},
                name:"分配角色"
            });
        }
        var C = new Controller(null,tb);
    }());

</script>
