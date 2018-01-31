// package com.resto.shop.web.controller.scm;
//
// import com.resto.brand.core.entity.Result;
// import com.resto.brand.web.model.BrandSetting;
// import com.resto.brand.web.service.BrandSettingService;
// import com.resto.scm.web.model.MdBill;
// import com.resto.scm.web.service.MdBillService;
// import com.resto.shop.web.constant.Common;
// import com.resto.shop.web.controller.GenericController;
// import org.springframework.stereotype.Controller;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.ResponseBody;
//
// import javax.annotation.Resource;
// import javax.validation.Valid;
//
//@Controller
//@RequestMapping("scmMdBill")
//public class ScmMdBillController extends GenericController{
//
//	@Resource
//	MdBillService mdBillService;
//
//
//    @Resource
//    private BrandSettingService brandSettingService;
//
//    @RequestMapping("/list")
//    public String list() {
//           BrandSetting brandSetting = brandSettingService.selectByBrandId(getCurrentBrandId());
//           if (brandSetting.getIsOpenScm().equals(Common.YES)) {
//               return "scmMdBill/list";
//           } else {
//                return "notopen";
//           }
//       }
//
//
//	@RequestMapping("/list_all")
//	@ResponseBody
//	public Result listData(String beginDate,String endDate){
//		return getSuccessResult(mdBillService.queryJoin4Page(getCurrentShopId(),beginDate,endDate));
//	}
//
//	@RequestMapping("list_one")
//	@ResponseBody
//	public Result list_one(Long id){
//		MdBill mdBill = mdBillService.selectById(id);
//		return getSuccessResult(mdBill);
//	}
//
//	@RequestMapping("create")
//	@ResponseBody
//	public Result create(@Valid MdBill mdBill){
//		mdBillService.insert(mdBill);
//		return Result.getSuccess();
//	}
//
//	@RequestMapping("modify")
//	@ResponseBody
//	public Result modify(@Valid MdBill mdBill){
//		mdBillService.update(mdBill);
//		return Result.getSuccess();
//	}
//
//	@RequestMapping("delete")
//	@ResponseBody
//	public Result delete(Long id){
//		mdBillService.deleteById(id);
//		return Result.getSuccess();
//	}
//}
