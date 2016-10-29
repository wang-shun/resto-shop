package com.resto.shop.web.service.impl;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.ArticleAttrMapper;
import com.resto.shop.web.dao.ArticleUnitMapper;
import com.resto.shop.web.model.ArticleAttr;
import com.resto.shop.web.model.ArticleUnit;
import com.resto.shop.web.service.ArticleAttrService;

import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class ArticleAttrServiceImpl extends GenericServiceImpl<ArticleAttr, Integer> implements ArticleAttrService {

    @Resource
    private ArticleAttrMapper articleattrMapper;
    @Resource
    private ArticleUnitMapper articleUnitMapper;

    @Override
    public GenericDao<ArticleAttr, Integer> getDao() {
        return articleattrMapper;
    }

    /**
     * 根据店铺ID查询信息
     */
	@Override
	public List<ArticleAttr> selectListByShopId(String shopId) {
		List<ArticleAttr> articleAttrs = articleattrMapper.selectListByShopId(shopId);
		for(ArticleAttr articleAttr : articleAttrs){
			List<ArticleUnit> articleUnit = articleUnitMapper.selectListByAttrId(articleAttr.getId());
			articleAttr.setArticleUnits(articleUnit);
		}
		return articleAttrs;
	}

	/**
	 * 添加 信息
	 * @param articleAttr
	 */
	@Override
	public void create(ArticleAttr articleAttr) {
		articleattrMapper.insertInfo(articleAttr);
		//判断是否 添加了规格
		if(articleAttr.getUnits() != null && articleAttr.getUnits().length > 0){
			Integer tbArticleAttrId = articleAttr.getId();//添加完 ArticleAttr 的主键 ID
			String[] units = articleAttr.getUnits();
			String[] unitSorts = articleAttr.getUnitSorts();
			for(int i = 0; i <units.length ; i++){
				ArticleUnit articleUnit= new ArticleUnit(units[i], new BigDecimal(unitSorts[i]), tbArticleAttrId);
				articleUnitMapper.insert(articleUnit);
			}
		}
	}

	/**
	 * 删除信息
	 */
	@Override
	public void deleteInfo(Integer id) {
		//删除 ArticleAttr 信息，改变  state 状态
		articleattrMapper.deleteByPrimaryKey(id);
		//删除 ArticleUnit 信息，改变  state 状态
		articleUnitMapper.deleteByAttrId(id);
	}

	/**
	 * 修改信息
	 */
	@Override
	public void updateInfo(ArticleAttr articleAttr) {
		//修改  ArticleAttr 信息
		articleattrMapper.updateByPrimaryKeySelective(articleAttr);
		//删除  ArticleUnit  信息
		List<ArticleUnit> articleUnits = articleUnitMapper.selectListByAttrId(articleAttr.getId());
		for(ArticleUnit articleUnit : articleUnits){
			articleUnitMapper.deleteByPrimaryKey(articleUnit.getId());
		}
		//添加  ArticleUnit  信息
		if(articleAttr.getUnits() != null && articleAttr.getUnits().length > 0){
			Integer tbArticleAttrId = articleAttr.getId();
			String[] unitIds = articleAttr.getUnitIds();
			String[] units = articleAttr.getUnits();
			String[] unitSorts = articleAttr.getUnitSorts();
			for(int i = 0; i <units.length ; i++){
				ArticleUnit articleUnit= new ArticleUnit(unitIds[i] ,units[i], new BigDecimal(unitSorts[i]), tbArticleAttrId);
				articleUnitMapper.insertSelective(articleUnit);
			}
		}
	}

	@Override
	public List<ArticleAttr> selectListByArticleId(String articleId) {
		return articleattrMapper.selectListByArticleId(articleId);
	}

	@Override
	public int insertByAuto(ArticleAttr articleAttr) {
		return articleattrMapper.insertByAuto(articleAttr);
	}

	@Override
	public ArticleAttr selectSame(String name, String shopId) {
		return articleattrMapper.selectSame(name, shopId);
	}
}
