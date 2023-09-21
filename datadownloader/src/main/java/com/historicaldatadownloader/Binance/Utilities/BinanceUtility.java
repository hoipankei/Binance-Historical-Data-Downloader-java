package com.historicaldatadownloader.Binance.Utilities;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

import org.json.*;

import com.historicaldatadownloader.Binance.Utilities.BinanceEnums.TradingType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.apache.commons.cli.*;


public class BinanceUtility {
    private static final String BASE_URL = BinanceEnums.BASE_URL; // Replace with the actual BASE URL from your enums
    private static final String UM_URL = BinanceEnums.UM_URL;
    private static final String CM_URL = BinanceEnums.CM_URL;
    private static final String DEFAULT_URL = BinanceEnums.DEFAULT_URL;

    public static String getDestinationDir(String fileUrl, String folder) {
        String storeDirectory = System.getenv("STORE_DIRECTORY");
        if (folder != null && !folder.isEmpty()) {
            storeDirectory = folder;
        }
        if (storeDirectory == null || storeDirectory.isEmpty()) {
            storeDirectory = Paths.get("").toAbsolutePath().toString();
        }
        return Paths.get(storeDirectory, fileUrl).toString();
    }

    public static String getDownloadUrl(String fileUrl) {
        return BASE_URL + fileUrl;
    }

    public static List<String> getAllSymbols(String type) throws IOException, JSONException {
        String urlString = "";
        switch (type) {
            case "um":
                urlString = UM_URL;
                break;
            case "cm":
                urlString = CM_URL;
                break;
            default:
                urlString = DEFAULT_URL;
        }

        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        // StringBuilder 类在 Java 5 中被提出，它和 StringBuffer 之间的最大不同在于 StringBuilder 的方法不是线程安全的（不能同步访问）。
        // 由于 StringBuilder 相较于 StringBuffer 有速度优势，所以多数情况下建议使用 StringBuilder 类。
        StringBuilder result = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        reader.close();

        JSONObject jsonResponse = new JSONObject(result.toString());
        JSONArray symbolsArray = jsonResponse.getJSONArray("symbols");

        List<String> symbols = new ArrayList<>();
        for (int i = 0; i < symbolsArray.length(); i++) {
            symbols.add(symbolsArray.getJSONObject(i).getString("symbol"));
        }

        return symbols;
    }

    public static void downloadFile(String basePath, String fileName, String folder) throws IOException {
        String downloadUrlString = basePath + fileName;
        
        if (folder != null) {
            basePath = Paths.get(folder, basePath).toString();
        }
        // if (dateRange != null) {
        //     dateRange = dateRange.replace(" ", "_");
        //     basePath = Paths.get(basePath, dateRange).toString();
        // }
        
        Path savePath = Paths.get(getDestinationDir(fileName, basePath));
        String savingpath = savePath.toString();
        if (Files.exists(savePath)) {
            System.out.println("\nFile already exists! " + savePath);
            return;
        }

        if (!Files.exists(Paths.get(basePath))) {
            try {
                Files.createDirectories(Paths.get(basePath));
            } catch (IOException e) {
                e.printStackTrace();
                
            }
        }
        String downloadUrl = getDownloadUrl(downloadUrlString);
        URL url = new URL(downloadUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        int length = conn.getContentLength();
        int blocksize = Math.max(4096, length / 100);

        try (BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
             FileOutputStream fileOutputStream = new FileOutputStream(savingpath)) {
            byte[] dataBuffer = new byte[blocksize];
            int bytesRead;
            int downloadProgress = 0;

            System.out.println("\nFile Download: " + savingpath);
            while ((bytesRead = in.read(dataBuffer, 0, blocksize)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
                downloadProgress += bytesRead;
                displayProgress(downloadProgress, length);
            }
        }
        catch (IOException e) {
            System.out.println("\nFile not found: " + e);
        }
    }

    private static void displayProgress(int progress, int total) {
        int done = (int) (50.0 * progress / total);
        System.out.print("\r[" + "#".repeat(done) + ".".repeat(50 - done) + "]");
        System.out.flush();
    }

    public static LocalDate convertToDateObject(String d) {
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        
        try {
            return LocalDate.parse(d, formatter1);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(d, formatter2);
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Invalid date format. Supported formats are yyyy-MM-dd and yyyy/MM/dd.", ex);
            }
        }
    }

    public static LocalDate[] getStartEndDateObjects(String dateRange) {
        String[] parts = dateRange.split(" ");
        LocalDate startDate = convertToDateObject(parts[0]);
        LocalDate endDate = convertToDateObject(parts[1]);

        return new LocalDate[]{startDate, endDate};
    }

    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

    public static String matchDateRegex(String argValue) throws IllegalArgumentException {
        if (!DATE_PATTERN.matcher(argValue).matches()) {
            throw new IllegalArgumentException("Invalid date format");
        }
        return argValue;
    }

    public static String checkDirectory(String argValue) throws IOException {
        Path dirPath = Paths.get(argValue);
        if (Files.exists(dirPath)) {
            try (Scanner scanner = new Scanner(System.in)) {  // use try-with-resources here
                while (true) {
                    System.out.print("Folder already exists! Do you want to overwrite it? y/n  ");
                    String option = scanner.nextLine();
                    if (!option.equals("y") && !option.equals("n")) {
                        System.out.println("Invalid Option!");
                        continue;
                    } else if (option.equals("y")) {
                        Files.walk(dirPath).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
                        break;
                    } else {
                        break;
                    }
                }
            }
        }
        return argValue;
    }
    
    public static void raiseArgError(String msg) throws IllegalArgumentException {
        throw new IllegalArgumentException(msg);
    }

    public static String getPath(String tradingType, String marketDataType, String timePeriod, String symbol, String interval) {
        String tradingTypePath = "data/spot";
        if (!"spot".equals(tradingType)) {
            tradingTypePath = String.format("data/futures/%s", tradingType);
        }
        return interval != null ?
               String.format("%s/%s/%s/%s/%s/", tradingTypePath, timePeriod, marketDataType, symbol.toUpperCase(), interval) :
               String.format("%s/%s/%s/%s/", tradingTypePath, timePeriod, marketDataType, symbol.toUpperCase());
    }

    public static Options getOptions(String parserType) {
        Options options = new Options();

        options.addOption("s", "symbols", true, "Single symbol or multiple symbols separated by space");
        options.addOption("y", "years", true, String.format("Single year or multiple years separated by space. Example: -y 2019 2021 means to download %s from 2019 and 2021", parserType));
        options.addOption("m", "months", true, String.format("Single month or multiple months separated by space. Example: -m 2 12 means to download %s from Feb and Dec", parserType));
        options.addOption("d", "dates", true, "Date to download in [YYYY-MM-DD] format. Single date or multiple dates separated by space. Default: download from 2020-01-01 if no argument is parsed");
        options.addOption("startDate", true, "Starting date to download in [YYYY-MM-DD] format");
        options.addOption("endDate", true, "Ending date to download in [YYYY-MM-DD] format");
        options.addOption("folder", true, "Directory to store the downloaded data");
        options.addOption("skip_monthly", true, "1 to skip downloading of monthly data, default 0");
        options.addOption("skip_daily", true, "1 to skip downloading of daily data, default 0");
        options.addOption("c", "checksum", true, "1 to download checksum file, default 0");
        options.addOption("t", "type", true, String.format("Valid trading types: %s", Arrays.toString(TradingType.values())));

        if ("klines".equals(parserType)) {
            options.addOption("i", "intervals", true, "Single kline interval or multiple intervals separated by space. Example: -i 1m 1w means to download klines interval of 1 minute and 1 week");
        }

        return options;
    }

    public static List<Integer> convertStringListtoNumList(List<String> stringList){
        List<Integer> List = new ArrayList<>();
        
        for (int i = 0 ; i < stringList.size(); i++ ){
            try{
                List.add(Integer.parseInt(stringList.get(i)));
            }
            catch(NumberFormatException e){
                System.out.println("'" + stringList.get(i) + "' is not a valid number. Skipping.");
            }   
        }
        return List;
    }

    public static void main(String[] args) {
        String dateRange = "2021-09-01 2021-09-10";
        LocalDate[] dates = getStartEndDateObjects(dateRange);
        System.out.println("Start Date: " + dates[0]);
        System.out.println("End Date: " + dates[1]);

        try {
            // Usage example
            String date = matchDateRegex("2023-09-13");
            System.out.println("Validated Date: " + date);

            String path = checkDirectory("./sample_directory");
            System.out.println("Directory Path: " + path);

            String generatedPath = getPath("spot", "typeA", "timeA", "symbolA", "intervalA");
            System.out.println("Generated Path: " + generatedPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
