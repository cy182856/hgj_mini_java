package com.example.smp.controller.test;

public class Main {

    public static void main(String[] args) {
        int total = 12;
        int pageSize = 5;
        int ceil = (int) Math.ceil((double)total/(double)pageSize);
        System.out.println(ceil);
    }
}
