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
							<div class="control-label">是否启用餐盒费</div>
							<label>
								<input type="radio" name="isMealFee" onchange="showMealFee()" v-model="m.isMealFee" value="1">
								是
							</label>
							<label>
								<input type="radio" name="isMealFee"  v-model="m.isMealFee" value="0" onchange="hideMealFee()">
								否
							</label>
						</div>
						<div class="form-group" id="mealFeeDivOne" style="display: none">
							<label>名称</label>
							<input type="test" class="form-control" name="mealFeeName" v-if="!m.mealFeeName" value="餐盒费" required="required">
							<input type="test" class="form-control" name="mealFeeName" v-if="m.mealFeeName" v-model="m.mealFeeName" required="required">
						</div>
						<div class="form-group" id="mealFeeDivTwo" style="display: none">
							<label>餐盒费/盒</label>
							<input type="test" class="form-control" name="mealFeePrice" placeholder="(建议输入整数)" v-model="m.mealFeePrice" required="required">
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
	function showMealFee(){
		$('#mealFeeDivOne').show();
		$('#mealFeeDivTwo').show();
	}

	function hideMealFee(){
		$('#mealFeeDivOne').hide();
		$('#mealFeeDivTwo').hide();
	}
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
					console.log(result.data);
					vueObj.m=result.data;
					if(result.data.isMealFee == 1){
						$('#mealFeeDivOne').show();
						$('#mealFeeDivTwo').show();
					}else{
						$('#mealFeeDivOne').hide();
						$('#mealFeeDivTwo').hide();
					}
				}
			})
		}
		
		
	}());
	
</script>