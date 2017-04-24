package com.resto.shop.web.service.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.DateUtil;
import com.resto.shop.web.dao.SupportTimeMapper;
import com.resto.shop.web.model.SupportTime;
import com.resto.shop.web.service.FreedayService;
import com.resto.shop.web.service.SupportTimeService;

import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class SupportTimeServiceImpl extends GenericServiceImpl<SupportTime, Integer> implements SupportTimeService {

    @Resource
    private SupportTimeMapper supporttimeMapper;
    @Resource
    private FreedayService freeDayService;

    @Override
    public GenericDao<SupportTime, Integer> getDao() {
        return supporttimeMapper;
    }

    @Override
    public List<SupportTime> selectList(String shopDetailId) {
        return supporttimeMapper.selectList(shopDetailId);
    }

	@Override
	public void saveSupportTimes(String articleId, Integer[] supportTimes) {
		supporttimeMapper.deleteArticleSupportTime(articleId);
		if(supportTimes!=null&&supportTimes.length>0){
			supporttimeMapper.insertArticleSupportTime(articleId,supportTimes);
		}
	}

	@Override
	public List<Integer> selectByIdsArticleId(String articleId) {
		return supporttimeMapper.selectByArticleId(articleId);
	}

	@Override
	public List<SupportTime> selectNowSopport(String currentShopId) {
		List<SupportTime> supportTime = supporttimeMapper.selectList(currentShopId);
		boolean isFreeDay = freeDayService.selectExists(new Date(),currentShopId);
		Date now = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(now);
		int freeDaybin = isFreeDay?1<<8:1<<7;
		//int weekDay = c.get(Calendar.DAY_OF_WEEK)-1;  //这里周末等于1 所以全部减一

        //设置 weekDay
        int weekDay = c.get(Calendar.DAY_OF_WEEK);
        if(weekDay != 1){//如果为周一到周六，则默认减2。
            weekDay-=2;
        }else{//如果是周日，则不减2，设为默认值6，代表周日 。
            weekDay=6;
        }

		List<SupportTime> support = new ArrayList<>();
		for (SupportTime st : supportTime) {
			int bin = st.getSupportWeekBin();
			int todaybin = 1<<weekDay;
            System.out.println(bin&todaybin);
            System.out.println(bin&freeDaybin);
            if((bin&todaybin)>0||(bin&freeDaybin)>0){
				try {
//					Date begin = DateUtil.parseDate(st.getBeginTime(),"HH:mm:ss");
//					Date end = DateUtil.parseDate(st.getEndTime(),"HH:mm:ss");
//					int nowMin = DateUtil.getMinOfDay(now);
//					int beginMin = DateUtil.getMinOfDay(begin);
//					int endMin = DateUtil.getMinOfDay(end);
					Long begin = DateUtil.parseDate(st.getBeginTime(),"HH:mm:ss").getTime();
					Long end = DateUtil.parseDate(st.getEndTime(),"HH:mm:ss").getTime();
					Long nowMin = new Date("HH:mm:ss").getTime();
					if(begin<=nowMin&&end>=nowMin){
						support.add(st);
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
		return support;
	} 
    
    public static void main(String[] args) {
//		for(int i=0;i<9;i++){
//			int bin = 1<<i;
//			System.out.println(bin);
//			System.out.println("1----"+(1&bin));
//			System.out.println("2----"+(2&bin));
//			System.out.println("128----"+(128&bin));
//			System.out.println("256----"+(256&bin));
//		}

		System.out.println("31 -- 3 ---> " + (31&3));
		System.out.println("96 -- 3 ---> " + (96&3));
		System.out.println("511 -- 96 ---> " + (511&96));
		System.out.println("511 -- 3 ---> " + (511&3));
		System.out.println("25 -- 2 ---> " + (25&2));
		System.out.println("25 -- 6 ---> " + (25&6));
		System.out.println("25 -- 38 ---> " + (25&38));
		System.out.println("38 -- 25 ---> " + (38&25));
		System.out.println("27 -- 38 ---> " + (27&38));


	}

}
