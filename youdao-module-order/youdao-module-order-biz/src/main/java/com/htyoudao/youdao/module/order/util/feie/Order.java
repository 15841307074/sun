package com.htyoudao.youdao.module.order.util.feie;

public class Order {
    private String title;
    private String price;
    private String num;
    private Boolean isBig;
    public Order() {
    }

    public Order(String title, String price, String num,Boolean isBig) {
        this.title = title;
        this.num = num;
        this.price = price;
        this.isBig = isBig;
    }

    @Override
    public String toString() {
        return "Order [title=" + title + ", num=" + num + ", price=" + price + "]";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public Boolean getIsBig() {
        return isBig;
    }

    public void setIsBig(String num) {
        this.isBig = isBig;
    }
}
