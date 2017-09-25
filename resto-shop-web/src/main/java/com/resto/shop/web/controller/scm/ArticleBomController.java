 package com.resto.shop.web.controller.scm;

 import com.resto.brand.core.entity.Result;
 import com.resto.scm.web.dto.MdRulArticleBomHeadDo;
 import com.resto.scm.web.model.MdRulArticleBomHead;
 import com.resto.scm.web.service.ArticleBomHeadService;
 import com.resto.shop.web.controller.GenericController;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;

 import javax.annotation.Resource;
 import javax.validation.Valid;

@Controller
@RequestMapping("scmBom")
public class ArticleBomController extends GenericController{

	@Resource
	ArticleBomHeadService articlebomService;

	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public Result listData(){
		return  getSuccessResult(articlebomService.queryJoin4Page(getCurrentShopId()));
	}

	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(Long id){
		MdRulArticleBomHead articlebom = articlebomService.queryById(id);
		return getSuccessResult(articlebom);
	}

	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid @RequestBody MdRulArticleBomHeadDo articlebom){
		try {
			articlebom.setShopDetailId(this.getCurrentShopId());
			articlebom.setCreaterId(this.getCurrentUserId());
			articlebomService.addArticleBomHead(articlebom);
			return Result.getSuccess();
		}catch (Exception e){
			return new Result("保存失败", 5000,false);
		}

	}

	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid @RequestBody MdRulArticleBomHeadDo articlebom){
		articlebomService.updateRulArticleBomHead(articlebom);
		return Result.getSuccess();
	}

	@RequestMapping("delete")
	@ResponseBody
	public Result delete(Long id){
		articlebomService.deleteById(id);
		return Result.getSuccess();
	}
}
