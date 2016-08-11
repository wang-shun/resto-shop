package com.resto.shop.web.task;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.resto.brand.core.util.DateUtil;
import com.resto.brand.web.model.BrandUser;
import com.resto.brand.web.service.BrandUserService;

/**
 * 定时任务。 用于同步 中间数据库
 * 在每天 凌晨两点半   同步前一天的 报表数据
 * @author lmx
 *
 */
@Component("reportDataTask")
public class ReportDataTask {
	
	@Autowired
    BrandUserService brandUserService;
	
	//链接前缀
    String urlBase = "http://localhost:8081";//http://op.restoplus.cn
	//登入的url
    String loginUrl = urlBase + "/shop/branduser/login";
    //获取营业收入报表的数据的url(包括品牌数据和店铺的数据)
    String incomeUrl = urlBase + "/shop/totalIncome/reportIncome";
    //品牌菜品销售报表的数据的url
    String brandArticleUrl = urlBase + "/shop/articleSell/brand_id_data";
    //店铺菜品的销售报表的url
    String shopArticleUrl = urlBase + "/shop/articleSell/shop_data";
    //订单的url
    String shopOrderUrl = urlBase + "/shop/orderReport/AllOrder";
    
    
    //获取前一天的时间 (开始时间 和 结束时间)
    String beginTime = DateUtil.getYesterDayBegin();
    String endTime = DateUtil.getYesterDayEnd();
    
	
//    				   ss mm HH
    @Scheduled(cron = "00 21 20 * * ?")   //每天  02:30:00 （凌晨两点半）执行
//  @Scheduled(cron = "0/5 * *  * * ?")   //每5秒执行一次
    public void syncData() throws ClassNotFoundException, UnsupportedEncodingException {
    	
    	//获取品牌用户账户信息（目前只有 茶香书香 需要同步数据）
    	BrandUser brandUser = brandUserService.selectUserInfoByBrandIdAndRole("1386c0c0f35f466097fc770bec7d6400", 8);
    	
    	//创建 client 对象
    	CloseableHttpClient client = HttpClients.createDefault();
    	//设置登录参数
    	Map<String, String> loginMap = new HashMap<>();
        loginMap.put("username", "kc_admin");
        loginMap.put("password", "c888c24ab6f0d64439f3002823f211f2fb4015cb");// 527527527
        loginMap.put("isMD5", "true");//跳过MD5加密操作
    	//登录
        HttpResponse loginResponse = doPost(client, loginUrl, loginMap);
        
        //得到 loginResponse 的状态响应码
        int statusCode=loginResponse.getStatusLine().getStatusCode();
        if (statusCode== HttpStatus.SC_OK) {
        	System.out.println("--------登录成功！");
        	
        	//设置 访问链接
        	List<String> list = new LinkedList<>();
            list.add(incomeUrl);
            list.add(brandArticleUrl);
            list.add(shopArticleUrl);
            list.add(shopOrderUrl);
            
            //设置访问参数
            Map<String, String> reportMap = new HashMap<>();
            reportMap.put("beginDate",beginTime);
            reportMap.put("endDate",endTime);
            
            //轮询访问获取数据
            for (int i = 0; i <list.size() ; i++) {
                if(i==1){
                	reportMap.put("sort","desc");
                }else if (i==2){
                	reportMap.put("shopId","f3910fceb055442ab6b3abc6642eb70a");
                }else if(i==3){
                	reportMap.put("shopId","f3910fceb055442ab6b3abc6642eb70a");
                }
                
                HttpResponse reportResponse = doPost(client, list.get(i), reportMap);
                
                //得到httpResponse的状态响应码
                statusCode = reportResponse.getStatusLine().getStatusCode();
            	try {
            		if (statusCode== HttpStatus.SC_OK) {
            			String result = getResult(reportResponse);
            			
            		}
				} catch (ParseException | IOException e) {
					e.printStackTrace();
				}
            }
        	
        }else{
        	System.out.println("--------登录失败！");
        }
    	
//    	List<String> list = new LinkedList<>();
//        list.add(incomeUrl);
//        list.add(brandArticleUrl);
//        list.add(shopArticleUrl);
//        list.add(shopOrderUrl);
    }
    
    
    // Post 请求
    public HttpResponse doPost(CloseableHttpClient client,String url,Map parameterMap){
    	//创建 post 请求的对象
    	HttpPost httpPost = new HttpPost(url);
    	
    	//将传入的 参数 进行封装
    	List<NameValuePair> param = new ArrayList<NameValuePair>();
        Iterator it = parameterMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry parmEntry = (Map.Entry) it.next();
            param.add(new BasicNameValuePair((String) parmEntry.getKey(),
                    (String) parmEntry.getValue()));
        }
    	
        //创建 Response 对象
        HttpResponse httpResponse = null;
		try {
			//创建参数对象
			UrlEncodedFormEntity postEntity = new UrlEncodedFormEntity( param, "UTF-8");
			//设置参数
	        httpPost.setEntity(postEntity);
	        //执行
	        httpResponse = client.execute(httpPost);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return httpResponse;
    }
    
    //获取response里的数据
    public static  String getResult(HttpResponse httpResponse)throws ParseException, IOException{
        // 获取响应消息实体
        HttpEntity entity = httpResponse.getEntity();
        // 判断响应实体是否为空
        String responseString = "";
        if (entity != null) {
            responseString = EntityUtils.toString(entity).replace("\r\n", "");
        }

        return responseString;
    }
}
