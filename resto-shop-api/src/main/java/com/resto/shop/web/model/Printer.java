package com.resto.shop.web.model;

import org.hibernate.validator.constraints.NotBlank;

public class Printer {
    private Integer id;

    @NotBlank(message="{打印机名称 不能为空}")
    private String name;

    @NotBlank(message="{IP 地址不能为空}")
    private String ip;
    
    @NotBlank(message="{端口号 不能为空}")
    private String port;

    private String shopDetailId;
    
    private Integer printType;

    //小票类型 0小票 1贴纸
    private Integer ticketType;

    public Integer getTicketType() {
        return ticketType;
    }

    public void setTicketType(Integer ticketType) {
        this.ticketType = ticketType;
    }

    public Integer getPrintType() {
		return printType;
	}

	public void setPrintType(Integer printType) {
		this.printType = printType;
	}

	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip == null ? null : ip.trim();
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port == null ? null : port.trim();
    }

    public String getShopDetailId() {
        return shopDetailId;
    }

    public void setShopDetailId(String shopDetailId) {
        this.shopDetailId = shopDetailId == null ? null : shopDetailId.trim();
    }
}