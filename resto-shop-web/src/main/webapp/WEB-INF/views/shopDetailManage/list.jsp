<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags"%>

<div id="control">
	<div class="col-md-offset-3 col-md-6">
		<div class="portlet light bordered">
			<div class="portlet-title">
				<div class="caption">
					<span class="caption-subject bold font-blue-hoki"> 表单</span>
				</div>
			</div>
			<div class="portlet-body">
				<form role="form" action="{{'shopDetailManage/modify'}}" @submit.prevent="save">
					<div class="form-body">
						<div class="form-group">
							<label>店铺名称</label> 
							<input type="text" class="form-control" name="name" :value="m.name" placeholder="必填" required="required">
						</div>
						<div class="form-group">
							<div class="control-label">等位红包</div>
							<label >
								<input type="radio" name="waitRedEnvelope" v-model="m.waitRedEnvelope" value="1">
								开启
							</label>
							<label>
								<input type="radio" name="waitRedEnvelope" v-model="m.waitRedEnvelope" value="0">
								关闭
							</label>
						</div>
						<div  class="form-group">
							<label>等位红包每秒增加价</label>
							<input type="text" class="form-control" name="baseMoney" :value="m.baseMoney">
						</div>
						<div  class="form-group">
							<label>等位红包上限价格</label>
							<input type="text" class="form-control" name="highMoney" :value="m.highMoney">
						</div>
					</div>
					<input class="btn green" type="submit" value="保存" /> 
					<a class="btn default" @click="cancel">取消</a>
				</form>
			</div>
		</div>
	</div>
</div>

<script>
	
	$(document).ready(function(){
		initContent();
		toastr.options = {
				  "closeButton": true,
				  "debug": false,
				  "positionClass": "toast-top-right",
				  "onclick": null,
				  "showDuration": "500",
				  "hideDuration": "500",
				  "timeOut": "3000",
				  "extendedTimeOut": "500",
				  "showEasing": "swing",
				  "hideEasing": "linear",
				  "showMethod": "fadeIn",
				  "hideMethod": "fadeOut"
				}
		var temp;
		var vueObj = new Vue({
			el:"#control",
			data:{
				m:{},
			},
			methods:{
				initTime :function(){
					$(".timepicker-no-seconds").timepicker({
						 autoclose: true,
						 showMeridian:false,
			             minuteStep: 5
					  });
				},
				save:function(e){
					var formDom = e.target;
					$.ajax({
						url:"shopDetailManage/modify",
						data:$(formDom).serialize(),
						success:function(result){
							if(result.success){
								toastr.clear();
								toastr.success("保存成功！");
							}else{
								toastr.clear();
								toastr.error("保存失败");
							}
						},
						error:function(){
							toastr.clear();
							toastr.error("保存失败");
						}
					})
					
				},
				cancel:function(){
					initContent();
					
				}
			}
		});
		
		function initContent(){
			$.ajax({
				url:"shopDetailManage/list_one",
				success:function(result){
					var tem1 = result.data.openTime;
					var tem2 = result.data.closeTime;
					var open;
	 				var close;
					open = new Date(tem1).format("hh:mm"); 
	 				close = new Date(tem2).format("hh:mm");
	 				if(open=='aN:aN'){
						open = tem1;
					}
					if(close=='aN:aN'){
						close=tem2;
	 				}
	 				result.data.openTime=open;
	 				result.data.closeTime=close;
	 				objectName = result.data;
	 				vueObj.m=result.data;
				}
			})
		}
		
		
	}());
	
</script>
