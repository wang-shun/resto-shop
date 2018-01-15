package com.resto.shop.web.report;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.AppraisePraise;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by carl on 2016/11/20.
 */
public interface AppraisePraiseMapper extends GenericDao<AppraisePraise,String> {

    int deleteByPrimaryKey(String id);

    int insert(AppraisePraise appraisePraise);

    int insertSelective(AppraisePraise appraisePraise);

    AppraisePraise selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(AppraisePraise appraisePraise);

    int updateByPrimaryKey(AppraisePraise appraisePraise);

    int updateCancelPraise(@Param("appraiseId") String appraiseId, @Param("customerId") String customerId, @Param("isDel") Integer isDel);

    List<AppraisePraise> appraisePraiseList(String appraiseId);

    AppraisePraise selectByAppraiseIdCustomerId(@Param("appraiseId") String appraiseId, @Param("customerId") String customerId);

    int selectByCustomerCount(String customerId);
}
