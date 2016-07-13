<%@ page language="java" pageEncoding="utf-8"%>
<%@include file="../tag-head.jsp" %>
<form id="share-form" role="form" action="modulelist/edit_share">
	<div class="form-body">
		<div class="form-group">
		    <label>分享标题</label>
		    <input type="text" class="form-control" name="shareTitle" v-model="m.shareTitle">
		</div>
		<div class="form-group">
		    <label>分享图标</label>
		    <input type="hidden" class="form-control" name="shareIcon" v-model="m.shareIcon">
		    <img-file-upload class="form-control" @success="uploadSuccess" @error="uploadError"></img-file-upload>
		</div>
		<div class="form-group">
		    <label>最低分享分数</label>
		    <input type="number" min="1" max="5" class="form-control" name="minLevel" v-model="m.minLevel">
		</div>
		<div class="form-group">
		    <label>最少分享字数</label>
		    <input type="number" class="form-control" name="minLength" v-model="m.minLength">
		</div>
		<div class="form-group">
		    <label>返利(%)</label>
		    <input type="text" class="form-control" name="rebate" v-model="m.rebate">
		</div>
		<div class="form-group">
		    <label>最小金额</label>
		    <input type="text" class="form-control" name="minMoney" v-model="m.minMoney">
		</div>
		<div class="form-group">
		    <label>最大金额</label>
		    <input type="text" class="form-control" name="maxMoney" v-model="m.maxMoney">
		</div>
		<div class="form-group">
		    <label>注册按钮文字</label>
		    <input type="text" class="form-control" name=registerButton v-model="m.registerButton">
		</div>
		<div class="form-group">
			<label>延迟提醒时间(秒)</label>
			<input type="text" class="form-control" name="delayTime" v-model="m.delayTime">
		</div>
		<div class="form-group">
			<label for="">是否启用</label>
		    <div class="radio-list">
				<label class="radio-inline">
				  <input type="radio" name="isActivity" v-model="m.isActivity" value="1"> 启用
				</label>
				<label class="radio-inline">
				  <input type="radio" name="isActivity" v-model="m.isActivity" value="0"> 不启用
				</label>
		    </div>
		</div>
		<div class="form-group">
		    <label>分享弹窗文本</label>
		    <textarea class="ueditor-textarea"  name="dialogText">{{m.dialogText}}</textarea>
		</div>
	</div>
</form>
<script>
	UEDITOR_CONFIG.zIndex=11005;
	UEDITOR_CONFIG.toolbars=[[
        'source', '|', 'undo', 'redo', '|',
        'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'superscript', 'subscript', 'removeformat', 'formatmatch', 'autotypeset', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'selectall', 'cleardoc', '|',
        'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
        'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
        'directionalityltr', 'directionalityrtl', 'indent', '|',
        'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
        'link', 'unlink', 'anchor',  ]]
	var obj = new Vue({
		el:"#share-form",
		data:{
			m:{
				isActivity : 1,
				minMoney:2,
				maxMoney:100,
			},
		},
		methods:{
			uploadSuccess:function(url){
				this.m.shareIcon = url;
				toastr.success("上传图标成功");
			},
			uploadError:function(){
				toastr.error("上传失败");
			}
		},
		created:function(){
			var that = this;
			$.post("modulelist/data_share",null,function(result){
				if(result.data){
					that.m = result.data;
				}
				Vue.nextTick(function(){
					var randomId = "ueditor_id_"+new Date().getTime();
					$(".ueditor-textarea").attr("id",randomId);
					var ue = UE.getEditor(randomId);
				});
			});
		}
	});
</script>