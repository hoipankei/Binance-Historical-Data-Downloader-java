package com.historicaldatadownloader.Binance;

import java.io.IOException;
import java.time.LocalDate;
// import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.historicaldatadownloader.Binance.Utilities.BinanceUtility;
import com.historicaldatadownloader.Binance.Utilities.BinanceEnums;
// import com.historicaldatadownloader.Binance.Utilities.BinanceEnums.TradingType;

import org.apache.commons.cli.*;
import org.json.JSONException;

public class TradeDownloader {
    private static final String START_DATE = BinanceEnums.START_DATE;
    private static final String END_DATE = BinanceEnums.END_DATE;

    public static void downloadMonthlyTrades(String tradingType, List<String> symbols, int numSymbols,
                                             List<Integer> years, List<Integer> months, 
                                             String startDate, String endDate, 
                                             String folder, int checksum) throws IOException {
        int current = 0;
        // String dateRange = null;

        // if (startDate != null && endDate != null) {
        //     dateRange = String.join(" ", startDate, endDate);
        // }

        if (startDate == null) {
            startDate = START_DATE;
        }
        
        if (endDate == null) {
            endDate = END_DATE;
        }

        LocalDate startLocalDate = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate endLocalDate = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        System.out.println("Found " + numSymbols + " symbols");

        for (String symbol : symbols) {
            System.out.println("[" + (current + 1) + "/" + numSymbols + "] - start download monthly " + symbol + " trades");
            for (int year : years) {
                for (int month : months) {
                    LocalDate currentLocalDate = LocalDate.of(year, month, 1);
                    if ((currentLocalDate.isAfter(startLocalDate) || currentLocalDate.isEqual(startLocalDate))
                            && (currentLocalDate.isBefore(endLocalDate) || currentLocalDate.isEqual(endLocalDate))) {
                        String path = BinanceUtility.getPath(tradingType, "trades", "monthly", symbol, null);
                        String fileName = String.format("%s-trades-%d-%02d.zip", symbol.toUpperCase(), year, month);
                        BinanceUtility.downloadFile(path, fileName, folder);

                        if (checksum == 1) {
                            String checksumPath = BinanceUtility.getPath(tradingType, "trades", "monthly", symbol, null);
                            String checksumFileName = String.format("%s-trades-%d-%02d.zip.CHECKSUM", symbol.toUpperCase(), year, month);
                            BinanceUtility.downloadFile(checksumPath, checksumFileName, folder);
                        }
                    }
                }
            }
            current++;
        }
    }

    public static void downloadDailyTrades(String tradingType, List<String> symbols, int numSymbols, 
                                            List<String>  dates, String startDate, String endDate, 
                                            String folder, int checksum) throws IOException{
        int current = 0;
        // String dateRange = null;

        // if (startDate != null && endDate != null){
        //     dateRange = String.join(" ", startDate, endDate);
        // }

        if (startDate == null) {
            startDate = START_DATE;
        }
        
        if (endDate == null) {
            endDate = END_DATE;
        }

        LocalDate startLocalDate = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate endLocalDate = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        System.out.println("Found" + numSymbols + "symbols");
        for (String symbol:symbols){
            System.out.println("[" + (current + 1) + "/" + numSymbols + "] - start download daily " + symbol + " trades");
            
            for (String date: dates){
                LocalDate currentDate = BinanceUtility.convertToDateObject(date);
                if ((currentDate.isAfter(startLocalDate) || currentDate.equals(startLocalDate)) && (currentDate.isBefore(endLocalDate) || currentDate.equals(endLocalDate))){
                    String path = BinanceUtility.getPath(tradingType ,"trades", "daily", symbol, null);
                    String fileName = String.format("%s-trades-%s.zip", symbol.toUpperCase(), date);
                    BinanceUtility.downloadFile(path, fileName, folder);
                    
                    if (checksum == 1) {
                            String checksumPath = BinanceUtility.getPath(tradingType, "trades", "daily", symbol, null);
                            String checksumFileName = String.format("%s-trades-%s.zip.CHECKSUM", symbol.toUpperCase(), date);
                            BinanceUtility.downloadFile(checksumPath, checksumFileName, folder);
                        }
                }
            }
            current += 1;
        }

    }
    
    public static void main(String[] args) throws IOException, JSONException {
        Options options = BinanceUtility.getOptions("trades");  // Assuming you want the parserType to be "trades"

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println("Error: Unable to parse command-line arguments.");
            System.exit(1);
        }

        // Process parsed command-line options
        String tradngType = cmd.getOptionValue("type", BinanceEnums.TradingType.spot.toString()); // Default to UM
        List<String> symbols = cmd.hasOption("symbols") ? Arrays.asList(cmd.getOptionValue("symbols").split(" ")) : BinanceUtility.getAllSymbols(tradngType);
        int numSymbols = symbols.size();

        String startDate = cmd.getOptionValue("startDate", START_DATE);  // Default to START_DATE if not provided
        String endDate = cmd.getOptionValue("endDate", END_DATE);        // Default to END_DATE if not provided
        
        int checksum = Integer.parseInt(cmd.getOptionValue("checksum", "0"));  // Default to 0 if not provided
        int skipMonthly = Integer.parseInt(cmd.getOptionValue("skip_monthly", "0")); // Default to 0
        int skipDaily = Integer.parseInt(cmd.getOptionValue("skip_daily", "0")); // Default to 0
        
        List<Integer> years = cmd.hasOption("years") ? BinanceUtility.convertStringListtoNumList(Arrays.asList(cmd.getOptionValue("years").split(" "))) : Arrays.asList(BinanceEnums.Years); // Default to 2017 to 2023
        List<Integer> months = cmd.hasOption("months") ? BinanceUtility.convertStringListtoNumList(Arrays.asList(cmd.getOptionValue("months").split(" "))) : BinanceEnums.MonthtoIntList(); // Default to 2017 to 2023
        String folder = cmd.hasOption("folder") ? cmd.getOptionValue("folder") : BinanceEnums.DEFAULT_DOWNLOAD_FOLDER_STRING; 

        List<String> dates;
        if  (cmd.hasOption("dates")){
            dates = Arrays.asList(cmd.getOptionValue("dates").split(" "));
        }
        else{
            LocalDate periodStartDate = BinanceUtility.convertToDateObject(BinanceEnums.PERIOD_START_DATE);
            LocalDate todayDate = LocalDate.now();
            long period = ChronoUnit.DAYS.between(periodStartDate, todayDate);
            dates = new ArrayList<>();
            for (long i = 0; i <= period; i++) {
                dates.add(periodStartDate.plusDays(i).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                if (skipMonthly == 0) {
                    downloadMonthlyTrades(tradngType, symbols, numSymbols, years, months, periodStartDate.toString(), todayDate.toString(), folder ,checksum);
                }
            }
        }
        // List<String> dates = new ArrayList<>();
        // dates.add("2023-09-12");
        if (skipDaily == 0) {
            downloadDailyTrades(tradngType, symbols, numSymbols, dates, startDate, endDate, folder ,checksum);
        }   
    }
}
