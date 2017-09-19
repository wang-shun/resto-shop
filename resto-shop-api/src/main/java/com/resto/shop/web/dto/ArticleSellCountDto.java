package com.resto.shop.web.dto;

/**
 * Created by carl on 2017/7/29.
 */
public class ArticleSellCountDto {
    private String articleId;
    private String articleName;

    private int totalCount;
    private int mealFeeNumber;


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


    public int getMealFeeNumber() {
        return mealFeeNumber;
    }

    public void setMealFeeNumber(int mealFeeNumber) {
        this.mealFeeNumber = mealFeeNumber;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    @Override
    public String toString() {
        return "ArticleSellCountDto{" +
                "articleId='" + articleId + '\'' +
                ", articleName='" + articleName + '\'' +
                ", totalCount=" + totalCount +
                ", mealFeeNumber=" + mealFeeNumber +
                '}';
    }
}
