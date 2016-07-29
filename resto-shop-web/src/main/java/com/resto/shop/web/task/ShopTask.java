package com.resto.shop.web.task;

import com.resto.shop.web.controller.GenericController;
import org.springframework.stereotype.Component;

/**
 * Created by KONATA on 2016/7/14.
 */
@Component("shopTask")
public class ShopTask extends GenericController {

//	@Autowired
//	private OrderService orderService;
//
//	@Scheduled(cron = "00 56 14 * * ?")   //每天12点执行
//    public void job1() throws ClassNotFoundException {
//        String url = null;
//        String driver = null;
//        String username = null;
//        String password = null;
//        Connection con = null;
//        PreparedStatement sta = null;
//
//            try {
//
//                url = "jdbc:mysql://127.0.0.1:3306/middle?useUnicode=true&characterEncoding=utf8";
//                driver = "com.mysql.jdbc.Driver";
//                Class.forName(driver);
//                username = "root";
//                password = "root";
//                con = DriverManager.getConnection(url, username, password);
//
//                String sql = "insert into shop_article(id,article_id,article_family_name,article_name,sales,sales_occupies,sell,sell_occupies) values(?,?,?,?,?,?,?,?)";
// 			    sta = con.prepareStatement(sql);
//
// 			    //查询出店铺菜品的数据(今天的数据 )
// 			    List<ArticleSellDto> list = orderService.selectShopArticleSellByDate("2016-07-20", "2016-07-20","f48a0a35e0be4dd8aaeb7cf727603958" ,"0asc");
//
// 			    for (ArticleSellDto articleSellDto : list) {
// 			    	 sta.setString(1,ApplicationUtils.randomUUID());
// 	 				 sta.setString(2, articleSellDto.getArticleId());
// 	 				 sta.setString(3, articleSellDto.getArticleFamilyName());
// 	 				 sta.setString(4, articleSellDto.getArticleName());
// 	 				 sta.setInt(5, articleSellDto.getShopSellNum());
// 	 				 sta.setString(6, articleSellDto.getNumRatio());
// 	 				 sta.setBigDecimal(7, articleSellDto.getSalles());
// 	 				 sta.setString(8, articleSellDto.getSalesRatio());
// 	 				 sta.executeUpdate();
//				}
// 				 System.out.println("插入成功");
//            } catch (SQLException e) {
//                e.printStackTrace();
//            } finally {
//                close(con, sta, null);
//            }
//
//    }
//
//
//    // 关闭相关的对象
//    public void close(Connection con, PreparedStatement st, ResultSet rs) {
//        try {
//            if (rs != null) {
//                rs.close();
//            }
//            if (st != null) {
//                st.close();
//            }
//            if (con != null) {
//                con.close();
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//


//    public static void main(String[] args) throws ClassNotFoundException {
//    	String url = "jdbc:mysql://127.0.0.1:3306/middle?useUnicode=true&characterEncoding=utf8";
//    	String driver = "com.mysql.jdbc.Driver";
//    	String username = "root" ;
//    	String password = "root";
//    	Connection con = null;
//    	try {
//    		Class.forName(driver);
//			con = DriverManager.getConnection(url, username, password);
//			 String sql = "insert into shop_article(id,article_id,article_family_name,article_name,sales,sales_occupies,sell,sell_occupies) values(?,?,?,?,?,?,?,?)"; 
//			 PreparedStatement sta = con.prepareStatement(sql); 
//			 sta.setString(1,ApplicationUtils.randomUUID()); 
//			 sta.setString(2, "243440kklkjjhk"); 
//			 sta.setString(3, "主食");
//			 sta.setString(4, "米饭");
//			 sta.setInt(5, 10);
//			 sta.setString(6, "20%");
//			 sta.setBigDecimal(7, new BigDecimal(20));
//			 sta.setString(8, "20%");
//			 int rows = sta.executeUpdate(); 
//			 if(rows > 0) { 
//				 System.out.println("operate successfully!");
//				} 
//			 sta.close(); 
//			 con.close();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//    	
//    	
//    	
//    	
//    	
//    	
//	}
//    
//    


}
