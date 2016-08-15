package com.resto.shop.web.task;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.resto.brand.core.util.ApplicationUtils;
import com.resto.brand.web.model.BrandUser;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.BrandUserService;
import com.resto.brand.web.service.ShopDetailService;

/**
 * 定时任务。 用于同步 中间数据库
 * 在每天 凌晨两点半   同步前一天的 报表数据
 * @author lmx
 *
 */
@Component("reportDataTask")
public class ReportDataTask{

	@Autowired
    BrandUserService brandUserService;
    @Autowired
    ShopDetailService shopDetailService;

    static Logger log = Logger.getLogger(ReportDataTask.class);
    
    //链接前缀
    String urlBase = "http://localhost:8081";//http://op.restoplus.cn
    //登入的url
    String loginUrl = urlBase + "/shop/branduser/login";
    //品牌总收入
    String brandIncomeUrl = urlBase + "/shop/syncData/syncBrandIncome";
    //店铺总收入
    String shopIncomeUrl = urlBase + "/shop/syncData/syncShopIncome";    
    //品牌菜品销售报表					
    String brandArticleUrl = urlBase + "/shop/syncData/syncBrandOrderArticle";
    //店铺菜品的销售报表的url
    String shopArticleUrl = urlBase + "/shop/articleSell/shop_data";
    //订单的url
    String shopOrderUrl = urlBase + "/shop/orderReport/AllOrder";
    //订单 菜品信息 url
    String orderItemsUrl = urlBase + "/shop/syncData/syncOrderItems";

    //数据库 参数
    String url = "jdbc:mysql://127.0.0.1:3306/middle?useUnicode=true&characterEncoding=utf8";
    String driver = "com.mysql.jdbc.Driver";
    String username = "root";
    String password = "root";
    
    static {
		//注册驱动类 
		try { 
		     Class.forName("com.mysql.jdbc.Driver"); 
		} catch (ClassNotFoundException e) { 
		     log.error("#ERROR# :加载数据库驱动异常，请检查！", e); 
		} 
    } 
    
    
    @Scheduled(cron = "0/5 * *  * * ?")   //每5秒执行一次
    //				   ss mm HH
//    @Scheduled(cron = "00 19 17 * * ?")   //每天12点执行
    public void syncData() throws ClassNotFoundException, UnsupportedEncodingException {
    	
    	//简厨 974b0b1e31dc4b3fb0c3d9a0970d22e4
    	//书香茶香 1386c0c0f35f466097fc770bec7d6400
    	String brandId = "974b0b1e31dc4b3fb0c3d9a0970d22e4";
        //获取品牌用户
        BrandUser brandUser = brandUserService.selectUserInfoByBrandIdAndRole(brandId, 8);
        List<ShopDetail> list_shopDetail = shopDetailService.selectByBrandId(brandId);
        
        //创建 Client 对象
        CloseableHttpClient client = HttpClients.createDefault();
        //设置登录参数
        Map<String,String> parameterMap = new HashMap<>();
        parameterMap.put("username", brandUser.getUsername());
        parameterMap.put("password", brandUser.getPassword());// 527527527
        parameterMap.put("isMD5", "true");
        //登录
        HttpResponse loginResponse = doPost(client, loginUrl, parameterMap);
        
        //得到httpResponse的状态响应码
        int statusCode = loginResponse.getStatusLine().getStatusCode();
        
        if (statusCode == 302 && statusCode != HttpStatus.SC_OK) {//登录成功后会 进行 重定向  页面跳转，返回的  statusCode 为 302，正常访问 密码错误时，返回的是 200.【HttpStatus.SC_OK=200】
        	log.info("--------------HttpClient 登录成功！");
        	
        	Map<String,String> requestMap = new HashMap<>();
        	requestMap.put("beginDate","2016-08-14");
        	requestMap.put("endDate","2016-08-14");
            
        	HttpResponse httpResponse = doPost(client, orderItemsUrl, requestMap);
        	if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
//        		createSQL(httpResponse, "order_article");
        		executeSQL(httpResponse, "order_article");
//        		String resultData = getResult(httpResponse);
//        		JSONObject resultJsonObject = new JSONObject(resultData);
//                JSONArray jsonArray = resultJsonObject.getJSONArray("data");
//                Iterator<Object> it_data = jsonArray.iterator();
//                while (it_data.hasNext()) {
//					JSONObject ob = (JSONObject) it_data.next();
//					Iterator it_map = ob.keys();
//					while (it_map.hasNext()) {
//						String key = (String) it_map.next();  
//						System.out.println(key+"  --  "+ob.get(key));
//					}
//                }
//              
                log.info("--------------导入完成");
        	}else{
            	log.info("--------------操作失败！");
        	}
        }else{
        	log.info("--------------HttpClient 登录失败！");
        }
    }
    
    
    /**
     * HttpClient Post 请求
     * @param client
     * @param url
     * @param parameterMap
     * @return
     */
    public HttpResponse doPost(CloseableHttpClient client,String url,Map<String,String> parameterMap){
        HttpPost httpPost = new HttpPost(url);
        //封装请求参数
        List<NameValuePair> param = new ArrayList<NameValuePair>();
        Iterator it = parameterMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry parmEntry = (Map.Entry) it.next();
            param.add(new BasicNameValuePair((String) parmEntry.getKey(),(String) parmEntry.getValue()));
        }
        HttpResponse httpResponse = null;
        try {
			UrlEncodedFormEntity postEntity = new UrlEncodedFormEntity(param, "UTF-8");
			httpPost.setEntity(postEntity);
			httpResponse =client.execute(httpPost);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return httpResponse;
    }
    
    
    /**
     * 获取response里的数据
     * @param httpResponse
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public static  String getResult(HttpResponse httpResponse){
        // 获取响应消息实体
        HttpEntity entity = httpResponse.getEntity();
        // 判断响应实体是否为空
        String responseString = "";
        if (entity != null) {
            try {
				responseString = EntityUtils.toString(entity).replace("\r\n", "");
			} catch (ParseException | IOException e) {
				e.printStackTrace();
			}
        }
        return responseString;
    }

    /**
     * 拼接 SQL 语句
     * @param httpResponse
     * @param tableName
     */
    public void createSQL(HttpResponse httpResponse,String tableName){
    	String resultData = getResult(httpResponse);
		JSONObject resultJsonObject = new JSONObject(resultData);
        JSONArray jsonArray = resultJsonObject.getJSONArray("data");
        Iterator<Object> it_data = jsonArray.iterator();
        while (it_data.hasNext()) {
        	StringBuffer sql_parameters = new StringBuffer("id,");
        	StringBuffer sql_values = new StringBuffer("'"+ApplicationUtils.randomUUID()+"',");
			JSONObject ob = (JSONObject) it_data.next();
			Iterator it_map = ob.keys();
			while (it_map.hasNext()) {
				String key = (String) it_map.next();  
//				System.out.println(key+"  --  "+ob.get(key));
				sql_parameters.append(key+",");
				sql_values.append("'"+ob.get(key)+"',");
			}
			String parameters = sql_parameters.substring(0, sql_parameters.lastIndexOf(",")) ;
	        String values = sql_values.substring(0, sql_values.lastIndexOf(",")) ;
	        String sql = "insert into "+tableName+"("+parameters+") values("+values+")";
	        System.out.println(sql);
        }
    }
    
    
    /**
     * 执行 SQL
     * @param httpResponse
     * @param tableName
     */
    public void executeSQL(HttpResponse httpResponse,String tableName){
    	String resultData = getResult(httpResponse);
		JSONObject resultJsonObject = new JSONObject(resultData);
        JSONArray jsonArray = resultJsonObject.getJSONArray("data");
        Iterator<Object> it_data = jsonArray.iterator();
        List<String> sqlList = new LinkedList<>();
        List<Object> sqlParameters_yuan = new LinkedList<>();
        while (it_data.hasNext()) {
            List<Object> sqlParameters = new LinkedList<>();
        	StringBuffer sql_parameters = new StringBuffer("id,");
        	StringBuffer sql_values = new StringBuffer("?,");
        	sqlParameters.add(ApplicationUtils.randomUUID());
			JSONObject ob = (JSONObject) it_data.next();
			Iterator it_map = ob.keys();
			while (it_map.hasNext()) {
				String key = (String) it_map.next();  
				sql_parameters.append(key+",");
				sql_values.append("？,");
				sqlParameters.add(ob.get(key));
			}
			String parameters = sql_parameters.substring(0, sql_parameters.lastIndexOf(",")) ;
	        String values = sql_values.substring(0, sql_values.lastIndexOf(",")) ;
	        System.out.println("-------------------------------------------------------------------");
	        String sql = "insert into "+tableName+"("+parameters+") values("+values+")";
	        sqlList.add(sql);
	        sqlParameters_yuan.add(sqlParameters);
	        System.out.println(sql);
	        for(Object o : sqlParameters){
	        	System.out.println(o);
	        }
        }
        System.out.println(sqlList.size()  + "  -----   " + sqlParameters_yuan.size());
    }
    
    public void insertDate(){
    	Connection conn = getConnection();
    	
    }
    
    
    /**
     * 创建一个数据库连接
     * @return
     */
    public Connection getConnection() {
        Connection conn = null;
        //创建数据库连接 
        try {
        	conn = DriverManager.getConnection(url, username, password); 
        } catch (SQLException e) {
        	log.error("#ERROR# :创建数据库连接发生异常，请检查！", e); 
        } 
        return conn; 
    }
}
