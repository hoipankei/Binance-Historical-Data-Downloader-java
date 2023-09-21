
package com.historicaldatadownloader.Binance.Utilities;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.EnumSet;
import java.util.stream.Collectors;

// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.List;

public class BinanceEnums {

    // public enum Year {
    //     Y2017, Y2018, Y2019, Y2020, Y2021, Y2022, Y2023;
    // }
    
    public static final Integer[] Years = { 2017, 2018, 2019, 2020, 2021, 2022, 2023};
    // public static final List<String> Years = Arrays.asList({ "2017", "2018", "2019", "2020", "2021", "2022", "2023"});

    public enum Interval {
        S1("1s"), M1("1m"), M3("3m"), M5("5m"), M15("15m"), M30("30m"),
        H1("1h"), H2("2h"), H4("4h"), H6("6h"), H8("8h"), H12("12h"),
        D1("1d"), D3("3d"), W1("1w"), MO1("1mo");

        private final String value;
        
        Interval(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum DailyInterval {
        S1("1s"), M1("1m"), M3("3m"), M5("5m"), M15("15m"), M30("30m"),
        H1("1h"), H2("2h"), H4("4h"), H6("6h"), H8("8h"), H12("12h"), D1("1d");

        private final String value;
        
        DailyInterval(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum TradingType {
        spot, um, cm;
    }

    public enum Month {
        JAN(1), FEB(2), MAR(3), APR(4), MAY(5), JUN(6),
        JUL(7), AUG(8), SEP(9), OCT(10), NOV(11), DEC(12);

        private final int value;

        Month(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
        
    }

    public static List<Integer> MonthtoIntList(){
        return EnumSet.allOf(Month.class).stream()
            .map(Month::getValue)
            .collect(Collectors.toList());
    }

    public static String getDownloadDirectory() {
        String homeDir = System.getProperty("user.home");
    
        // Identify OS
        String os = System.getProperty("os.name").toLowerCase();
    
        if (os.contains("win")) {
            // Typically, on Windows, it's in the 'Downloads' directory in user home.
            return homeDir + "\\Downloads\\";
        } else if (os.contains("mac")) {
            // Typically, on macOS, it's in the 'Downloads' directory in user home.
            return homeDir + "/Downloads/";
        } else if (os.contains("nux")) {
            // Typically, on Linux distributions like Ubuntu, it's in the 'Downloads' directory in user home.
            return homeDir + "/Downloads/";
        } else {
            // For other OS, return user home (fallback)
            return homeDir;
        }
    }

    public static final String PERIOD_START_DATE = "2020-01-01";
    public static final String BASE_URL = "https://data.binance.vision/";
    public static final String UM_URL = "https://fapi.binance.com/fapi/v1/exchangeInfo";
    public static final String CM_URL = "https://dapi.binance.com/dapi/v1/exchangeInfo";
    public static final String DEFAULT_URL = "https://api.binance.com/api/v3/exchangeInfo";
    public static final String START_DATE = "2017-01-01";
    public static final String END_DATE = LocalDate.now(ZoneId.of("UTC")).minusDays(1).toString();
    public static final String DEFAULT_DOWNLOAD_FOLDER_STRING = getDownloadDirectory();
    // public static final String DEFAULT_DOWNLOAD_DRECTORY = getDownloadDirectory();

    public static void main(String[] args) {
        System.out.println("START_DATE: " + START_DATE);
        System.out.println("END_DATE: " + END_DATE);
    }
}
