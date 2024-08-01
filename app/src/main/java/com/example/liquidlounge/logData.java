package com.example.liquidlounge;

import java.util.Map;

public class logData {
    private String name;
    private String customer_id;
    private String order_id;
    private String date;
    private String address;
    private int price;
    private Map<String, Object> productsOrdered;

    public logData(String name, String customer_id, String order_id, String date, String address, int price, Map<String, Object> productsOrdered) {
        this.name = name;
        this.customer_id = customer_id;
        this.order_id = order_id;
        this.date = date;
        this.address = address;
        this.price = price;
        this.productsOrdered = productsOrdered;
    }

    public Map<String, Object> getProductsOrdered() {
        return productsOrdered;
    }

    public void setProductsOrdered(Map<String, Object> productsOrdered) {
        this.productsOrdered = productsOrdered;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(String customer_id) {
        this.customer_id = customer_id;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @Override
    public String toString() {
        // Convert the Map into a formatted string
        StringBuilder productsOrderedString = new StringBuilder();
        if (productsOrdered != null && !productsOrdered.isEmpty()) {
            for (Map.Entry<String, Object> entry : productsOrdered.entrySet()) {
                productsOrderedString.append(entry.getKey())
                        .append(": ")
                        .append(entry.getValue())
                        .append("\n");
            }
        }

        return "logData{" +
                "name='" + name + '\'' +
                ", customer_id='" + customer_id + '\'' +
                ", order_id='" + order_id + '\'' +
                ", date='" + date + '\'' +
                ", address='" + address + '\'' +
                ", productsOrdered=\n" + productsOrderedString.toString() +
                ", price=" + price +
                '}';
    }
}
