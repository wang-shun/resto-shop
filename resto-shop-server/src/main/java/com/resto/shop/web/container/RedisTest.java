package com.resto.shop.web.container;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import com.resto.brand.core.util.ObjectTranscoder;
import com.resto.shop.web.model.Order;

import redis.clients.jedis.Jedis;

public class RedisTest {
	
	public final static String VIRTUAL_COURSE_PREX = "_lc_vc_";
	
	static String port = "localhost";
	//连接
	static Jedis jedis = new Jedis(port);
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		// 判断服务是否正常运行
		System.out.println("Server is running: " + jedis.ping());
		System.out.println("------------------------");
////		//单个赋值  --- 字符串
////		//赋值
////		jedis.set("userName", "asong");
////		// 取值
////		System.out.println("取值："+ jedis.get("userName"));
////		//-------
////		//赋值集合
////		jedis.lpush("jiaohao_list", "111111");
////		jedis.lpush("jiaohao_list", "222222");
////		jedis.lpush("jiaohao_list", "333333");
////		//取值集合
////		List<String> jiaohao_list = jedis.lrange("jiaohao_list", 0, -1);
////		for(int i=0; i<jiaohao_list.size(); i++) {
////	       System.out.println("Stored string in redis:: "+jiaohao_list.get(i));
////	     }
////		System.out.println("jiaohao_list 的长度为："+jiaohao_list.size());
////		System.out.println("------------------------");
//		
//		
		//得到所有的 key
		for(String key : jedis.keys("*")){
			System.out.println("key："+ key);
		}
		System.out.println("----------------");
		Set<byte[]> orderSet = jedis.zrangeByScore("PUSH_ORDER_MAP@86d0cb619e224a85a1419060d3fba8de".getBytes(), 1462245806002L, System.currentTimeMillis());
		Iterator<byte[]> iterator = orderSet.iterator();
		Order order = new Order();
		while (iterator.hasNext()) {  
			order = (Order)ObjectTranscoder.deserialize(iterator.next());
			
			System.out.println(order.getId()+"-"+order.getPushOrderTime().getTime());
		}
		
//		jedis.zrem("PUSH_ORDER_MAP@86d0cb619e224a85a1419060d3fba8de".getBytes(), ObjectTranscoder.serialize(order));
		
//		
//		//清空所有数据
//		//jedis.flushAll();
//		
////		ByteArrayOutputStream bos =  new ByteArrayOutputStream();
////		ObjectOutputStream oos =  new ObjectOutputStream(bos);
////		
////		Brand brand = new Brand();
////		brand.setBrandName("leo");
////		brand.setAddUser("asong");
////		oos.writeObject(brand);
////		//赋值
////		byte [] byteArray = bos.toByteArray();
////		oos.close();
////		bos.close();
////		String setObjectRet = jedis.set( "brand" .getBytes(), byteArray);
////		System. out .println( "赋值  \t" + setObjectRet);
////		//取值
////		byte [] bs = jedis.get( "brand" .getBytes());
////		ByteArrayInputStream bis =  new ByteArrayInputStream(bs);
////		ObjectInputStream inputStream =  new ObjectInputStream(bis);
////		Brand readObject = (Brand) inputStream.readObject();
////		System. out .println( " 取值 \t" + readObject.getBrandName()+"---"+readObject.getAddUser());
////
////		inputStream.close();
////		bis.close();
//		
//		
////		byte[] id = ("push_"+System.currentTimeMillis()).getBytes();
////		for(int i=0;i<=5;i++){
////			Brand brand = new Brand();
////			brand.setBrandName("leo"+i);
////			brand.setAddUser("asong"+i);
////			jedis.zadd(id, System.currentTimeMillis(), ObjectTranscoder.serialize(brand));
////		}
//		
//		System.out.println("-------");
//		
////		//判断是否存在
////		jedis.hset("shop_info", "abc-123" , "true");
////		System.out.println(jedis.hexists("shop_info", "abc-123"));
//
//		
//		byte[] id = ("test_id").getBytes();
////		for(int i=0;i<=5;i++){
////			Brand brand = new Brand();
////			brand.setBrandName("leo"+i);
////			jedis.zadd(id, System.currentTimeMillis(), ObjectTranscoder.serialize(brand));
////		}
//		
////		//读取
////		Brand brand = new Brand();
////		brand.setBrandName("leo+++++++++++");
////		jedis.zadd(id, System.currentTimeMillis(), ObjectTranscoder.serialize(brand));
////		System.out.println(temp);
////		Iterator<byte[]> it = jedis.zrange(id, 0, System.currentTimeMillis()).iterator();
////		while (it.hasNext()) {  
////			Brand brandtest = (Brand)ObjectTranscoder.deserialize(it.next());
////			System.out.println(brandtest.getBrandName());  
////		}
//		jedis.del(id);
		
	
//		//删除
////		jedis.zremrangeByScore(id, 0, System.currentTimeMillis());
//		
////		jedis.hdel("SHOP_INFO_test", "2");
////		
////		jedis.hset("SHOP_INFO_test", "3", "true----");
//////		
//		System.out.println(jedis.hexists("SHOP_INFO_test", "1"));
////		
//		System.out.println(jedis.hkeys("SHOP_INFO_test"));
////		jedis.flushAll();
////		ZREM
////		jedis.zrem("test_id".getBytes(), ObjectTranscoder.serialize(brand));
////		System.out.println("success");
//		
//		//ObjectTranscoder.deserialize(jedis.zrange(id, 0, 5));
//		
////		System.out.println("------------------------");
////		for(String key : jedis.keys("*")){
////			System.out.println("key："+ key);
////		}
	}
	
	
	/**
	 * 得到Key
	 * @param key
	 * @return
	 */
	public static String buildKey(String key){
		return VIRTUAL_COURSE_PREX + key;
	}
	
	
//	public static void main(String[] args) {
////		OrderProductionStateContainer
//		OrderProductionStateContainer.clearMap("86d0cb619e224a85a1419060d3fba8de");
//		System.out.println("success");
//	}
}
