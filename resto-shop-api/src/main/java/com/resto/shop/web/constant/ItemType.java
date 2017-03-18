package com.resto.shop.web.constant;


public class ItemType {

	public static final int SINGLE_NOSIZE= 1;  //单品无规格
	public static final int SINGLE_OLD =2;  //单品老规格
	public static final int MEAL_FATHER=3;    //套餐主品
	public static final int MEAL_SUN = 4;	//套餐子品
	public static final int SINGLE_NEW=5; //单品新规格


    public static String getItemTypeName(int state){
        switch (state){
            case ItemType.SINGLE_NOSIZE:
                return  "单品无规格";
            case ItemType.SINGLE_OLD:
                return  "单品老规格";
            case ItemType.MEAL_FATHER:
                return  "套餐主品";
            case ItemType.MEAL_SUN:
                return  "套餐子品";
            case ItemType.SINGLE_NEW:
                return  "单品新规格";
            default:
                return "";
        }
	}

}
