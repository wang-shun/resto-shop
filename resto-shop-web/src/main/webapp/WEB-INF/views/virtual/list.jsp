<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<div id="control">
    <div class="row form-div" v-if="showform">
        <div class="col-md-offset-3 col-md-6" >
            <div class="portlet light bordered">
                <div class="portlet-title">
                    <div class="caption">
                        <span class="caption-subject bold font-blue-hoki"> 表单</span>
                    </div>
                </div>
                <div class="portlet-body">
                    <form role="form" action="{{m.id?'showphoto/modify':'showphoto/create'}}" @submit.prevent="save">
                        <div class="form-body">
                            <div class="form-group">
                                <label>体验名称</label>
                                <input type="text" class="form-control" name="title" required v-model="m.title"  >
                            </div>
                            <div class="form-group">
                                <label>体验排序</label>
                                <div>
                                    <span class="article-units">
                                        <input type="checkbox" value="0">荤菜厨房
                                    </span>
                                    <span class="article-units">
                                        <input type="checkbox" value="1">素菜厨房
                                    </span>
                                    <span class="article-units">
                                        <input type="checkbox" value="2">
                                    <span class="article-units">
                                </div>
                            </div>
                            <div>
                                <label class="col-sm-1 control-label" style="padding-left:0px">体验类型</label>
                                <div class="col-sm-9" v-if="m.showType !=null">
                                    <div>
                                        <label >
                                            <input type="radio" name="showType" required v-model="m.showType" value="2">
                                            启用
                                        </label>
                                        <label>
                                            <input type="radio" name="showType" required v-model="m.showType" value="4">
                                            不启用
                                        </label>
                                    </div>
                                </div>
                                <div class="col-sm-9" v-if="m.showType ==null">
                                    <div>
                                        <label>
                                            <input type="radio" name="showType" v-model="m.showType" value="2" checked="checked">
                                            启用
                                        </label>
                                        <label>
                                            <input type="radio" name="showType" v-model="m.showType" value="4">
                                            不启用
                                        </label>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <input type="hidden" name="id" v-model="m.id" />
                        <input class="btn green"  type="submit"  value="保存"/>
                        <a class="btn default" @click="cancel" >取消</a>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <div class="table-div">
        <div class="table-operator">
            <s:hasPermission name="showphoto/add">
                <button class="btn green pull-right" @click="create">新建</button>
            </s:hasPermission>
        </div>
        <div class="clearfix"></div>
        <div class="table-filter"></div>
        <div class="table-body">
            <table class="table table-striped table-hover table-bordered "></table>
        </div>
    </div>
</div>


<script>
    (function(){
        var cid="#control";
        var $table = $(".table-body>table");
        var tb = $table.DataTable({
            ajax : {
                url : "showphoto/list_all",
                dataSrc : ""
            },
            columns : [
                {
                    title : "体验类型",
                    data : "showType",
                    createdCell:function(td,tdData){
                        if(tdData == 2){
                            $(td).html("<td>好评</td>");
                        }else{
                            $(td).html("<td>差评</td>");
                        }
                    }
                },
                {
                    title : "体验名称",
                    data : "title",
                },
                {
                    title : "操作",
                    data : "id",
                    createdCell:function(td,tdData,rowData,row){
                        var operator=[
                            <s:hasPermission name="showphoto/delete">
                            C.createDelBtn(tdData,"showphoto/delete"),
                            </s:hasPermission>
                            <s:hasPermission name="showphoto/edit">
                            C.createEditBtn(rowData),
                            </s:hasPermission>
                        ];
                        $(td).html(operator);
                    }
                }],
        });

        var C = new Controller(null,tb);
        var vueObj = new Vue({
            el:"#control",
            data:{
                typeNames:[{"id":"1","value":"餐品图片"},{"id":"2","value":"展示的图片"},{"id":"4","value":"差评"}],
            },
            mixins:[C.formVueMix],
            methods:{
                uploadSuccess:function(url){
                    $("[name='showType']").val(url).trigger("change");
                    C.simpleMsg("提交成功");
                },
                uploadError:function(msg){
                    C.errorMsg(msg);
                },
                save:function(e){
                    var that = this;
                    var formDom = e.target;
                    var showType = e.showType;
                    C.ajaxFormEx(formDom,function(){
                        that.cancel();
                        tb.ajax.reload();
                    });
                },
            }
        });
        C.vue=vueObj;
    }());

</script>