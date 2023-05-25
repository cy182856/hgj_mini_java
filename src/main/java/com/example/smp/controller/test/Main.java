package com.example.smp.controller.test;

import org.codehaus.xfire.client.Client;

import java.net.URL;

public class Main {

    public static void main(String[] args) throws Exception {
//        int total = 12;
//        int pageSize = 5;
//        int ceil = (int) Math.ceil((double)total/(double)pageSize);
//        System.out.println(ceil);

        Client client = new Client(new URL("http://127.0.0.1:9990/configcenter/services/configurationService?wsdl"));
        Object[] ob = client.invoke("getTheSystemAllConfigurations", new Object[]{"globalConfig", "dev"});
        System.out.println(ob);

    }
}
