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

<div class="modal fade" id="employeeRoModal" tabindex="-1" role="dialog" data-backdrop="static">
    <div class="modal-dialog modal-full">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true" @click="closeModal"></button>
            </div>
            <div class="modal-body"> </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-info btn-block" data-dismiss="modal" aria-hidden="true" @click="closeModal">关闭</button>
            </div>
        </div>
        <!-- /.modal-content -->
    </div>
    <!-- /.modal-dialog -->
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
                            var btn = vueObj.createBtn(null, "分配角色",
                                    "btn-sm btn-info",
                                    function() {
                                       // $("#employeeRoModal").modal();
                                       vueObj. showEmployeeRoleMoal("给用户分配店铺角色",tdData)
                                        console.log(tdData);
                                    })
                        operator.push(btn);
                        </s:hasPermission>
                        $(td).html(operator);
                    }
                } ],
        });

        var C = new Controller(null,tb);

        var vueObj = new Vue({
            el:"#control",
            mixins:[C.formVueMix],
            methods:{
                    openModal : function(url, modalTitle,employeeId) {
                    $.post(url, {"employeeId":employeeId},function(result) {
                        var modal = $("#employeeRoModal");
                        modal.find(".modal-body").html(result);
                        modal.find(".modal-title > strong").html(modalTitle);
                        modal.modal()
                    })

                },
                showEmployeeRoleMoal : function(title,employeeId) {
                    $("#employeeRoModal").modal('show');
                    this.openModal("employee/employee_role", title,employeeId);
                },
                createBtn :function (btnName,btnValue,btnClass,btnfunction) {
                    return $('<input />', {
                    name : btnName,
                    value : btnValue,
                    type : "button",
                    class : "btn " + btnClass,
                    click : btnfunction
                    })
                }


    }
        });
        C.vue=vueObj;
    }());


</script>
