<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>


<div id="control">
	<!-- 增值发票详情模态框开始 -->
<div class="modal fade" id="consigneeModal" tabindex="-1" role="dialog" 
  	 aria-labelledby=consigneeModalLabel" aria-hidden="true">
   <div class="modal-dialog">
      <div class="modal-content">
         <div class="modal-header">
            <button type="button" class="close" 
               data-dismiss="modal" aria-hidden="true">
                  &times;
            </button>
            <h4 class="modal-title" id="consigneeModalLabel">
          		   增值发票详情
            </h4>
         </div>
         <div class="modal-body">
           	<dl class="dl-horizontal">
				<dt>单位名称：</dt><dd>{{smsticketInfo.companyName}}</dd><br/>
				<dt>纳税人识别码：</dt><dd>{{smsticketInfo.taxpayerCode}}</dd><br/>
				<dt>注册地址：</dt><dd>{{smsticketInfo.registeredAddress}}</dd><br/>
				<dt>注册电话：</dt><dd>{{smsticketInfo.registeredPhoe}}</dd><br/>
				<dt>开户银行：</dt><dd>{{smsticketInfo.bankName}}</dd><br/>
				<dt>银行账户：</dt><dd>{{smsticketInfo.bankAccount}}</dd><br/>
				<dt>收货人姓名：</dt><dd>{{smsticketInfo.name}}</dd><br/>
				<dt>收货人电话：</dt><dd>{{smsticketInfo.phone}}</dd><br/>
				<dt>收货人地址：</dt>{{smsticketInfo.address}}<dd></dd><br/>
			</dl>
         </div>
         <div class="modal-footer">
            <button type="button" class="btn btn-default" 
               data-dismiss="modal">关闭
            </button>
         </div>
      </div><!-- /.modal-content -->
	</div><!-- /.modal -->
</div>	

<div class="modal fade" id="generalModal" tabindex="-1" role="dialog" 
  	 aria-labelledby="generalModalLabel" aria-hidden="true">
   <div class="modal-dialog">
      <div class="modal-content">
         <div class="modal-header">
            <button type="button" class="close" 
               data-dismiss="modal" aria-hidden="true">
                  &times;
            </button>
            <h4 class="modal-title" id="generalModalLabel">
          		   普通发票详情
            </h4>
         </div>
         <div class="modal-body">
           	<dl class="dl-horizontal">
				<dt>收货人姓名：</dt><dd>{{smsticketInfo.name}}</dd><br/>
				<dt>收货人电话：</dt><dd>{{smsticketInfo.phone}}</dd><br/>
				<dt>收货人地址：</dt>{{smsticketInfo.address}}<dd></dd><br/>
			</dl>
         </div>
         <div class="modal-footer">
            <button type="button" class="btn btn-default" 
               data-dismiss="modal">关闭
            </button>
         </div>
      </div><!-- /.modal-content -->
	</div><!-- /.modal -->
</div>	

	<!-- 表格   start-->
	<div class="table-div">
		<div class="table-operator">
			<s:hasPermission name="brand/add">
			<button class="btn green pull-right" @click="create">新建</button>
			</s:hasPermission>
		</div>
		<div class="clearfix"></div>
		<div class="table-filter">&nbsp;</div>
		<div class="table-body">
			<table class="table table-striped table-hover table-bordered "></table>
		</div>
	</div>
	<!-- 表格   end-->
</div>


<script>
	$(document).ready( function () {
		//载入 表格数据
	    tb = $('.table-body>table').DataTable({
			ajax : {
				url : "invoice/list_all",
				dataSrc : ""
			},
			columns : [
				{                 
					title : "发票抬头",
					data : "title",
				},                 
				{                 
					title : "申请时间",
					data : "createTime",
					createdCell:function(td,tdData){
						$(td).html(new Date(tdData).format("yyyy-MM-dd hh:mm:ss"))
					}
				},
				{                 
					title : "内容",
					data : "content",
				}, 
				{                 
					title : "完成时间",
					data : "pushTime",
					createdCell:function(td,tdData){
						if(tdData!=null){
							$(td).html(new Date(tdData).format("yyyy-MM-dd hh:mm:ss"))
						}
					}
				}, 
				{                 
					title : "状态",
					data : "status",
					createdCell:function(td,tdData){
						if(tdData==0){
							$(td).html("申请中...");
						}else if(tdData==1){
							$(td).html("已完成");
						}
					}
				}, 
				{                 
					title : "申请人",
					data : "proposer",
				}, 
				{                 
					title : "金额",
					data : "money",
					createdCell:function(td,tdData){
						$(td).html(tdData+"  <span class='glyphicon glyphicon-yen' aria-hidden='true'></span>");
					}
				}, 
				{                 
					title : "发票类型",
					data : "type",
					createdCell:function(td,tdData){
						if(tdData==1){
							$(td).html("普通发票")
						}else if (tdData==2){
							$(td).html("增值税发票")
						}
					}
					
				}, 
				
				{
					title : "操作",
					data : "id",
					createdCell : function(td, tdData, rowData) {
						console.log(rowData);
						var info=[];
						if(rowData.type==2){
							var btn = createBtn(null, "查看详情",
 									"btn-sm btn-primary",
 									function() {
										vueObj.showDetailInfo(rowData);
										$("#consigneeModal").modal();
 									})
						}else if(rowData.type==1){
							var btn = createBtn(null,"查看详情","btn-sm btn-default",function(){
								$("#generalModal").modal();									
							})
						}
// 						vueObj.showDetailInfo(rowData);
						info.push(btn);
						$(td).html(info);
					}
				} 
				],
	    });
		
	    var C = new Controller("#control",tb);
		
		var vueObj = new Vue({
			el:"#control",
			data:{
				smsticketInfo:{},
			},
			methods:{
				create:function(){
				},
				showDetailInfo:function(smsticketInfo){
					this.smsticketInfo = smsticketInfo;
				},
				edit:function(brandId,brandInfo){
					this.brandInfo = brandInfo;
					this.showMsg = false;
					$("#brandInfoModal").modal();
				},
				save:function(e){
					var brandSign = this.brandInfo.brandSign;
					var brandId = this.brandInfo.id;
					$.post("brand/validateInfo",{brandId:brandId,brandSign:brandSign},function(result){
						if(result.success){
							C.ajaxFormEx(e.target,function(){
								$("#brandInfoModal").modal("hide");//关闭模态框
								tb.ajax.reload();
							});
						}else{
							vueObj.showMsg = true;
						}
					});
				}
			}
		});
		
		//创建一个按钮
		function createBtn(btnName, btnValue, btnClass, btnfunction) {
			return $('<input />', {
				name : btnName,
				value : btnValue,
				type : "button",
				class : "btn " + btnClass,
				click : btnfunction
			})
		}		
		
	} );
	
</script>