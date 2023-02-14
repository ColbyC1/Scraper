/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.simplacescrape.scraper;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author colby
 */
public class SaleCheckBox extends JFrame {

    private static final long serialVersionUID = 1L;
    private final JTable table;

    public SaleCheckBox(List<List<String>> scrapedData, String zipCode) {
        String[] columnNames = {"Address", "Price", "Bed", "Sqft.", "Bath", "Lot Sqft", "Story", "Year Built"};

        String[] newColumnNames = Arrays.copyOf(columnNames, columnNames.length + 2);
        newColumnNames[columnNames.length] = "URL";
        newColumnNames[columnNames.length + 1] = "Select";

        Object[][] data = new Object[scrapedData.size()][newColumnNames.length];

        for (int i = 0; i < scrapedData.size(); i++) {
            List<String> row = scrapedData.get(i);

            Object[] newRow = new Object[newColumnNames.length];

            System.arraycopy(row.toArray(), 0, newRow, 0, row.size());

            String url = "https://www.har.com/zipcode_" + zipCode + "/realestate/for_sale?view=list";

            if (i <= 119) {
                newRow[newColumnNames.length - 2] = url;
            } else if (i <= 239) {
                newRow[newColumnNames.length - 2] = url + "&page=" + 2;
            } else if (i <= 359) {
                newRow[newColumnNames.length - 2] = url + "&page=" + 3;
            } else if (i <= 479) {
                newRow[newColumnNames.length - 2] = url + "&page=" + 4;
            } else if (i <= 599) {
                newRow[newColumnNames.length - 2] = url + "&page=" + 5;
            }

            data[i] = newRow;

            newRow[newColumnNames.length - 1] = false;
            data[i] = newRow;
        }

        DefaultTableModel model = new DefaultTableModel(data, newColumnNames);
        table = new JTable(model) {

            @Override
            public Class getColumnClass(int column) {
                switch (column) {
                    case 0:
                        return String.class;
                    case 1:
                        return String.class;
                    case 2:
                        return String.class;
                    case 3:
                        return String.class;
                    case 4:
                        return String.class;
                    case 5:
                        return String.class;
                    case 6:
                        return String.class;
                    case 7:
                        return String.class;
                    case 8:
                        return String.class;
                    default:
                        return Boolean.class;
                }
            }
        };

        table.setPreferredScrollableViewportSize(table.getPreferredSize());
        JScrollPane scrollPane = new JScrollPane(table);
        getContentPane().add(scrollPane);

        JPanel buttonPanel = new JPanel();
        JButton exportButton = new JButton("Export");
        buttonPanel.add(exportButton);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        exportButton.addActionListener(new ActionListener() {
            List<List<String>> selectedData = new ArrayList<>();
            int urlColumnIndex = 0;
            String url = "";
            String urlEx = "";

            @Override
            public void actionPerformed(ActionEvent e) {

                // Iterate through each row in the table
                for (int i = 0; i < table.getRowCount(); i++) {

                    // Check the value of the checkbox for the current row
                    boolean isSelected = (boolean) table.getValueAt(i, newColumnNames.length - 1);

                    // If the checkbox is checked, add the data for that row to the list
                    if (isSelected) {
                        List<String> rowData = new ArrayList<>();

                        for (int j = 0; j < newColumnNames.length - 1; j++) {
                            rowData.add((String) table.getValueAt(i, j));
                        }

                        for (int x = 0; x < newColumnNames.length; x++) {

                            if (newColumnNames[x].equals("URL")) {
                                urlColumnIndex = x;
                            }

                        }
                        url = (String) table.getValueAt(i, urlColumnIndex);
                        rowData.add(url);
                        urlEx = rowData.get(rowData.size() - 1);
                        selectedData.add(rowData);

                    }

                }

                if (!selectedData.isEmpty()) {
                    NewScraper zipCodeScraper = new NewScraper();

                    try {
                        zipCodeScraper.saleToExcel(urlEx, "For Sale - " + zipCode, selectedData);
                        JOptionPane.showMessageDialog(null, "Excel export was successful.");
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "Excel export failed.");
                        ex.getMessage();
                    }
                }

            }
        });
    }
}
