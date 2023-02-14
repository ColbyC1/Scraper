/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.simplacescrape.scraper;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * A simple web scraper program that is used to gather housing data by zip code
 * and export to excel file; 
 * Sources: {https://www.har.com/}, {https://www.incomebyzipcode.com/}, {https://www.incomebyzipcode.com/}
 *
 * @author colby
 */
public class Main {

    public static void main(String[] args) throws IOException {

        NewScraper zipCodeScraper = new NewScraper();

        String zipCode = JOptionPane.showInputDialog("Enter zip code: ");
        Document document = zipCodeScraper.saleConnect(zipCode);
        List<List<String>> saleList = zipCodeScraper.divDetails(document);

        for (int i = 2; i < 6; i++) {
            String extraPagesURL = "https://www.har.com/zipcode_" + zipCode + "/realestate/for_sale?view=list" + "&page=" + i;
            Document doc = Jsoup.connect(extraPagesURL).get();
            List<List<String>> extraList = zipCodeScraper.divDetails(doc);
            saleList.addAll(extraList);
        }

        Document documentOne = zipCodeScraper.rentConnect(zipCode);
        List<List<String>> rentList = zipCodeScraper.divDetails(documentOne);

        for (int i = 2; i < 6; i++) {
            String extraPagesURL = "https://www.har.com/zipcode_" + zipCode + "/realestate/for_rent?prop_type=SGL&view=list&page=" + i;
            Document doc = Jsoup.connect(extraPagesURL).get();
            List<List<String>> extraList = zipCodeScraper.divDetails(doc);
            rentList.addAll(extraList);
        }

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                SaleCheckBox saleFrame = new SaleCheckBox(saleList, zipCode);
                saleFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                saleFrame.pack();
                saleFrame.setLocation(150, 150);
                saleFrame.setVisible(true);
                saleFrame.addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosed(WindowEvent e) {
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                RentCheckBox rentFrame = new RentCheckBox(rentList, zipCode);
                                rentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                                rentFrame.pack();
                                rentFrame.setLocation(150, 150);

                                SwingUtilities.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        rentFrame.setVisible(true);
                                    }
                                });
                            }
                        }).start();
                    }
                });

            }
        });

    }

}
