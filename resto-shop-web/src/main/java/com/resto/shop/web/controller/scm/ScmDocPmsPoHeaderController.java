 package com.resto.shop.web.controller.scm;

 import com.resto.brand.core.entity.Result;
 import com.resto.scm.web.dto.DocPmsPoHeaderDetailDo;
 import com.resto.scm.web.model.DocPmsPoHeader;
 import com.resto.scm.web.service.DocPmsPoHeaderService;
 import com.resto.shop.web.controller.GenericController;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;

 import javax.annotation.Resource;
 import javax.validation.Valid;

@Controller
@RequestMapping("scmDocPmsPoHeader")
public class ScmDocPmsPoHeaderController extends GenericController{

	@Resource
	DocPmsPoHeaderService docPmsPoHeaderService;
	
	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public Result listData(){
		return getSuccessResult(docPmsPoHeaderService.queryJoin4Page(getCurrentShopId(),getCurrentShopName()));
	}
	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(Long id){
		DocPmsPoHeader docPmsPoHeader = docPmsPoHeaderService.selectById(id);
		return getSuccessResult(docPmsPoHeader);
	}
	
	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid DocPmsPoHeaderDetailDo docPmsPoHeaderDetailDo){
		docPmsPoHeaderService.createPmsPoHeaderDetailDo(docPmsPoHeaderDetailDo);
		return Result.getSuccess();
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid DocPmsPoHeader docPmsPoHeader){
		docPmsPoHeaderService.update(docPmsPoHeader);
		return Result.getSuccess();
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(Long id){
		docPmsPoHeaderService.deleteById(id);
		return Result.getSuccess();
	}

	@RequestMapping("approve")
	@ResponseBody
	public Result approve(Long id,Integer orderStatus){
		docPmsPoHeaderService.updateStateById(id,orderStatus);
		return Result.getSuccess();
	}

}
