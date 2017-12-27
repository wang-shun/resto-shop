package com.resto.shop.web.model;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 */
@Data
public class OffLineOrder implements Serializable {
    private String id;

    private String shopDetailId;

    private String brandId;

    private Integer resource;

    private BigDecimal enterTotal;

    private Integer enterCount;

    private Integer deliveryOrders;
    
    private BigDecimal orderBooks;

    private Integer numGuest;

    //@DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date createTime;

    //@DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createDate;

    private Integer state;


    public static OffLineOrder init(){
        OffLineOrder offLineOrder = new OffLineOrder();
        offLineOrder.setEnterTotal(BigDecimal.ZERO);
        offLineOrder.setOrderBooks(BigDecimal.ZERO);
        return  offLineOrder;
    }

    public OffLineOrder sumOrder(OffLineOrder offLineOrder) {
        offLineOrder.setOrderBooks(this.getOrderBooks().add(offLineOrder.getOrderBooks()));
        offLineOrder.setEnterTotal(this.getEnterTotal().add(offLineOrder.getEnterTotal()));
        offLineOrder.setDeliveryOrders(this.getDeliveryOrders()+offLineOrder.getDeliveryOrders());
        offLineOrder.setEnterCount(this.getEnterCount()+offLineOrder.getEnterCount());
        return offLineOrder;
    }
}