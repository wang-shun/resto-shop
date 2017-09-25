 package com.resto.shop.web.controller.scm;

 import com.resto.brand.core.entity.Result;
 import com.resto.scm.web.dto.DocStkInPlanHeaderDo;
 import com.resto.scm.web.service.StockInPlanService;
 import com.resto.shop.web.controller.GenericController;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;

 import javax.annotation.Resource;
 import javax.validation.Valid;
 import java.util.List;

 /***
  *
  * 库存入库
  *
  */
@Controller
@RequestMapping("scmStockPlan")
public class StockInPlanController extends GenericController{

	@Resource
	StockInPlanService stockinplanService;
	
	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public Result listData(){
    	return getSuccessResult(stockinplanService.queryJoin4Page(getCurrentShopId()));
	}
	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(Long id){
	      //DocStkInPlanHeaderDo stockinplan = stockinplanService.queryByStkInPlanHeaderId();
		return getSuccessResult(null);
	}
	
	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid @RequestBody DocStkInPlanHeaderDo stockinplan){
		stockinplanService.addDocStkInPlanHeaderDo(stockinplan);
		return Result.getSuccess();
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid @RequestBody DocStkInPlanHeaderDo stockinplan){
		stockinplanService.updateDocStkInPlanHeaderDo(stockinplan);
		return Result.getSuccess();
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(Long id){
		stockinplanService.deleteById(id);
		return Result.getSuccess();
	}
}
