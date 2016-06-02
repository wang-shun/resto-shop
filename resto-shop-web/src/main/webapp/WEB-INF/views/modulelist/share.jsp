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
		    <input type="text" class="form-control" name="shareIcon" v-model="m.shareIcon">
		</div>
		<div class="form-group">
		    <label>最低分享分数</label>
		    <input type="text" class="form-control" name="minLevel" v-model="m.minLevel">
		</div>
		<div class="form-group">
		    <label>最少分享字数</label>
		    <input type="text" class="form-control" name="minLength" v-model="m.minLength">
		</div>
		<div class="form-group">
		    <label>返利(%)</label>
		    <input type="text" class="form-control" name="rebate" v-model="m.rebate">
		</div>
		<div class="form-group">
			<label for="">是否启用</label>
		    <label class="radio-inline">
		    	<input type="checkbox"  v-bind:true-value="true" v-bind:false-value="false" v-model="m.isActivity">启用
		    </label>
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
			m:{},
		},
		created:function(){
			var that = this;
			$.post("modulelist/data_share",null,function(result){
				var shareSetting = result.data;
				if(!shareSetting){
					shareSetting={
						isActivity:true
					};
				}
				that.m=shareSetting;
				Vue.nextTick(function(){
					var randomId = "ueditor_id_"+new Date().getTime();
					$(".ueditor-textarea").attr("id",randomId);
					var ue = UE.getEditor(randomId);
				});
			});
		}
	});
</script>