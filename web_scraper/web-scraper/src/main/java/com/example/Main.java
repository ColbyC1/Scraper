package com.example;

import java.io.IOException;
import java.sql.Connection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.Connection;

public class Main {

    public Main() {

    }

    public static void main(String[] args) throws IOException {

        Document doc = null;

        try {

            String url = "example.com";
            Connection connection = org.jsoup.connect(url);
            doc = connection.get();

        } catch (IOException e) {
            System.out.println("Error Occurred.");
            e.printStackTrace();

            if (doc != null) {
                System.out.println(doc.title());
            }

        }
    }
}