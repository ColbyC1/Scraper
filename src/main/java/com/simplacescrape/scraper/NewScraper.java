/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.simplacescrape.scraper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author colby
 */
public class NewScraper {

    private Workbook workbook;
    private CellStyle style;
    private FileInputStream inputStream = null;
    private FileOutputStream fileOutputStream = null;
    private OutputStream fileOut = null;

    //Houses for SALE
    private final String baseSaleURL = "https://www.har.com/zipcode_";
    private final String endSaleURL = "/realestate/for_sale?view=list";

    //Houses for RENT
    private final String baseRentURL = "https://www.har.com/zipcode_";
    private final String endRentURL = "/realestate/for_rent?prop_type=SGL&sort=listdate%20desc&view=list";

    private final String avgIncomeURL = "https://www.incomebyzipcode.com/texas/";

    //creates connection using jsoup
    public Document saleConnect(String zipCode) throws IOException {
        Connection connection = Jsoup.connect(baseSaleURL + zipCode + endSaleURL);
        return connection.get();
    }

    public Document rentConnect(String zipCode) throws IOException {
        Connection connection = Jsoup.connect(baseRentURL + zipCode + endRentURL);
        return connection.get();
    }

    public Document avgIncomeConnect(String zipCode) throws IOException {
        Connection connection = Jsoup.connect(avgIncomeURL + zipCode);
        return connection.get();
    }

    //returns a list of url data passed into another of list , divDetails
    public List<List<String>> divDetails(Document document) {

        //parent div
        Elements elements = document.select(".col-12.pt-3.pt-md-0.listing-card-item");

        //lists of lists
        List< List<String>> divDetails = new ArrayList<>();

        for (Element element : elements) {
            ArrayList<String> details = new ArrayList<>();

            //address
            details.add(element.select(".cardv2--landscape__content__body__details_address_left_add").text());

            //price
            details.add(element.select(".cardv2--landscape__content__body__details_price").text());

            //parent span
            Elements spanElements = element.select(".cardv2--landscape__content__body__details_features_item");

            //loop skips empty text fields, adding all span text to details, and then to divDetails
            //grabs bed, sqft, baths, lot sqft., story, and year built text fields
            for (Element detailElement : spanElements) {
                if (!detailElement.select("span").text().isEmpty()) {
                    details.add(detailElement.text());
                }
            }

            divDetails.add(details);
        }

        return divDetails;
    }

    //checks if excel workbook in specified location exists and if it doesnt, one gets created, exported to desktop
    public void saleWorkbook() throws IOException {

        String filePath = System.getProperty("user.home") + "/Desktop/ForSale.xlsx";

        if (!new File(filePath).exists()) {
            workbook = new XSSFWorkbook();
            fileOut = new FileOutputStream("ForSale.xlsx");
            workbook.write(fileOut);

        } else {
            inputStream = new FileInputStream(new File("ForSale.xlsx"));
            workbook = WorkbookFactory.create(inputStream);
        }
    }

    public void rentWorkbook() throws IOException {

        String filePath = System.getProperty("user.home") + "/Desktop/ForRent.xlsx";

        if (!new File(filePath).exists()) {
            workbook = new XSSFWorkbook();
            fileOut = new FileOutputStream("ForRent.xlsx");
            workbook.write(fileOut);

        } else {
            inputStream = new FileInputStream(new File("ForRent.xlsx"));
            workbook = WorkbookFactory.create(inputStream);
        }
    }

    //writes all data to excel file creating new sheet for every zip code and labeled with zip code
    public void saleToExcel(String url, String sheetName, List<List<String>> saleList) throws IOException {
        saleWorkbook();

        //using list zipCode to label/get sheet to be updated
        Sheet sheet = workbook.createSheet(sheetName);
        sheet = workbook.getSheet(sheetName);

        //table headers
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Address");
        headerRow.createCell(1).setCellValue("Price");
        headerRow.createCell(2).setCellValue("Bed");
        headerRow.createCell(3).setCellValue("Sqft.");
        headerRow.createCell(4).setCellValue("Bath");
        headerRow.createCell(5).setCellValue("Lot Sqft.");
        headerRow.createCell(6).setCellValue("Story");
        headerRow.createCell(7).setCellValue("Year Built");
        headerRow.createCell(8).setCellValue("70 %");
        headerRow.createCell(9).setCellValue("URL");

        //using list scrapeData to grab each list (divDetails) and paste parsed data to excel sheet
        for (List<String> list : saleList) {

            int rowCount = sheet.getLastRowNum();
            Row row = sheet.createRow(++rowCount);

            int colNum = 0;

            for (String value : list) {
                Cell cell = row.createCell(colNum++);

                if (colNum == 2) {

                    //Change type of column B to NUMERIC and replace special characters
                    value = value.replace("$", "").replace(",", "");
                    cell.setCellValue(Double.parseDouble(value));

                    //sets column style to CURRENCY
                    cell.setCellStyle(workbook.createCellStyle());
                    style = workbook.getCellStyleAt((short) cell.getCellStyle().getIndex());
                    style.setDataFormat(workbook.createDataFormat().getFormat("$#,##0.00"));
                    cell.setCellStyle(style);

                } else {
                    cell.setCellValue(value);
                }

            }

            // Create a new cell for Column i
            Cell cellEight = row.createCell(8);

            //Set the formula for Column i: Column B * 0.7 and sets style to CURRENCY
            cellEight.setCellFormula("B" + (rowCount + 1) + "* 0.7");
            cellEight.setCellStyle(style);

            Cell cellNine = row.createCell(9);
            cellNine.setCellValue(url);


            CellRangeAddress filterRange = new CellRangeAddress(0, rowCount, 0, 9);
            sheet.setAutoFilter(filterRange);
        }

        //output stream to file
        fileOutputStream = new FileOutputStream("ForSale.xlsx");
        workbook.write(fileOutputStream);

        fileOutputStream.close();
        workbook.close();

    }

    public void rentToExcel(String zipCode, String url, String sheetName, List<List<String>> rentList) throws IOException {
        rentWorkbook();

        Document documentTwo = avgIncomeConnect(zipCode);
        Elements element = documentTwo.select("table.table.my-3.mb-5:nth-of-type(2) td.text-right");
        String avgIncome = element.first().text();

        //using list zipCode to label/get sheet to be updated
        Sheet sheet = workbook.createSheet(sheetName);
        sheet = workbook.getSheet(sheetName);

        //table headers
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Address");
        headerRow.createCell(1).setCellValue("Rent");
        headerRow.createCell(2).setCellValue("Bed");
        headerRow.createCell(3).setCellValue("Sqft.");
        headerRow.createCell(4).setCellValue("Bath");
        headerRow.createCell(5).setCellValue("Lot Sqft.");
        headerRow.createCell(6).setCellValue("Story");
        headerRow.createCell(7).setCellValue("Year Built");
        headerRow.createCell(8).setCellValue("URL");
        headerRow.createCell(9).setCellValue("Avg. Household Income");
        headerRow.createCell(10).setCellValue("Avg. Allowable");
        headerRow.createCell(11).setCellValue("Mo. Max Allowable");

        //using list scrapeData to grab each list (divDetails) and paste parsed data to excel sheet
        for (List<String> list : rentList) {

            int rowCount = sheet.getLastRowNum();
            Row row = sheet.createRow(++rowCount);

            int colNum = 0;

            for (String value : list) {
                Cell cell = row.createCell(colNum++);

                if (colNum == 2) {

                    //Change type of column B to NUMERIC and replace special characters
                    value = value.replace("$", "").replace(",", "");
                    cell.setCellValue(Double.parseDouble(value));

                    //sets column style to CURRENCY
                    cell.setCellStyle(workbook.createCellStyle());
                    style = workbook.getCellStyleAt((short) cell.getCellStyle().getIndex());
                    style.setDataFormat(workbook.createDataFormat().getFormat("$#,##0.00"));
                    cell.setCellStyle(style);

                } else {
                    cell.setCellValue(value);
                }

            }

            Cell cellEleven = row.createCell(8);
            cellEleven.setCellValue(url);

            //Create a new cell for Column i
            Cell cellEight = row.createCell(9);
            avgIncome = avgIncome.replace("$", "").replace(",", "");
            cellEight.setCellValue(Double.parseDouble(avgIncome));
            cellEight.setCellStyle(style);

            Cell cellNine = row.createCell(10);
            cellNine.setCellFormula("j" + (rowCount + 1) + "/3");
            cellNine.setCellStyle(style);

            Cell cellTen = row.createCell(11);
            cellTen.setCellFormula("k" + (rowCount + 1) + "/12");
            cellTen.setCellStyle(style);

            CellRangeAddress filterRange = new CellRangeAddress(0, rowCount, 0, 11);
            sheet.setAutoFilter(filterRange);
        }

        //output stream to file
        fileOutputStream = new FileOutputStream("ForRent.xlsx");
        workbook.write(fileOutputStream);

        fileOutputStream.close();
        workbook.close();
    }
}
