package com.resto.shop.web.dto;

/**
 * Created by carl on 2017/7/29.
 */
public class ArticleSellCountDto {
    private String articleId;
    private String articleName;
    private String createTime;
    private int orginCount;
    private int refundCount;
    private int  mealFeeNumber;


    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public String getArticleName() {
        return articleName;
    }

    public void setArticleName(String articleName) {
        this.articleName = articleName;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public int getOrginCount() {
        return orginCount;
    }

    public void setOrginCount(int orginCount) {
        this.orginCount = orginCount;
    }

    public int getRefundCount() {
        return refundCount;
    }

    public void setRefundCount(int refundCount) {
        this.refundCount = refundCount;
    }

    public int getMealFeeNumber() {
        return mealFeeNumber;
    }

    public void setMealFeeNumber(int mealFeeNumber) {
        this.mealFeeNumber = mealFeeNumber;
    }

    @Override
    public String toString() {
        return "ArticleSellCountDto{" +
                "articleId='" + articleId + '\'' +
                ", articleName='" + articleName + '\'' +
                ", createTime='" + createTime + '\'' +
                ", orginCount='" + orginCount + '\'' +
                ", refundCount='" + refundCount + '\'' +
                ", mealFeeNumber='" + mealFeeNumber + '\'' +
                '}';
    }
}
