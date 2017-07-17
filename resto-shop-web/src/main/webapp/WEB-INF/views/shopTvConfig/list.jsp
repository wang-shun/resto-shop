<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags"%>
<style>
	.formBox{
		color: #5bc0de;
	}
</style>
<div id="control" class="row">
	<div class="col-md-offset-3 col-md-6">
		<div class="portlet light bordered">
			<div class="portlet-title">
				<div class="caption">
					<span class="caption-subject bold font-blue-hoki"> 表单</span>
				</div>
			</div>
			<div class="portlet-body">
				<form role="form" action="{{'shopTvConfig/modify'}}" @submit.prevent="save">
					<div class="form-body">
						<input type="hidden" class="form-control" name="shopDetailId" v-model="m.shopDetailId">
						<input type="hidden" class="form-control" name="id" v-model="m.id">

						<div class="form-group">
							<label>准备中底色</label>
							<div>
								<input type="text" class="form-control color-mini" name="readyBackColor"
									   data-position="bottom left" v-model="m.readyBackColor">
							</div>
						</div>

						<div style="clear:both"></div>
						<br/>
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
				showWaitTime:true
			},
			created: function () {
				var a = $('.color-mini').minicolors({
					change: function (hex, opacity) {
						if (!hex) return;
						if (typeof console === 'object') {
							$(this).attr("value", hex);
						}
					},
					theme: 'bootstrap'
				});
				this.$watch("m", function () {
					if (this.m.id) {
						$('.color-mini').minicolors("value", this.m.readyBackColor);
					}
				});
			},
			methods:{
				save:function(e){
					var formDom = e.target;
					$.ajax({
						url:"shopTvConfig/modify",
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
				},
				uploadSuccess: function (url) {
					console.log(url);
					$("[name='tvBackground']").val(url).trigger("change");
					C.simpleMsg("上传成功");
					$("#tvBackground").attr("src", "/" + url);
				},
				uploadError: function (msg) {
					C.errorMsg(msg);
				},
			}

		});
		
		function initContent(){
			$.ajax({
				url:"shopTvConfig/list_one",
				success:function(result){
	 				vueObj.m=result.data;
				}
			})
		}
		
		
	}());
	
</script>
