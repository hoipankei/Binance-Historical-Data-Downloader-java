package com.historicaldatadownloader;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class test_download {

    public static void main(String[] args) {
        String baseUrl = "https://data.binance.vision/data/spot/daily/trades/BTCUSDT/";
        String savePath = "BTCUSDT-trades-2023-09-16.zip";  // Update the path to where you want to save

        try {
            // String downloadUrl = extractDirectDownloadLink(baseUrl);
            downloadFile(baseUrl + savePath, savePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // public static String extractDirectDownloadLink(String baseUrl) throws IOException {
    //     Document doc = Jsoup.connect(baseUrl).get();
    //     Elements links = doc.select("#listing > tr:nth-child(3) > td:nth-child(1) > a");
    //     for (Element link : links) {
    //         String href = link.absUrl("href");
    //         if (href.contains("BTCUSDT-trades-2023-09-16.zip")) {
    //             System.out.println("Found download link: " + href);
    //             return baseUrl + href;  // Construct the full URL for downloading
    //         }
    //     }
    //     throw new IOException("Direct download link not found");
    // }

    public static void downloadFile(String urlString, String fileName) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        int length = conn.getContentLength();
        int blocksize = Math.max(4096, length / 100);

        try (BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
             FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
            byte[] dataBuffer = new byte[blocksize];
            int bytesRead;
            int downloadProgress = 0;

            System.out.println("\nFile Download: " + fileName);
            while ((bytesRead = in.read(dataBuffer, 0, blocksize)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
                downloadProgress += bytesRead;
                displayProgress(downloadProgress, length);
            }
        }
    }

    public static void displayProgress(int progress, int total) {
        int done = (int) (50.0 * progress / total);
        System.out.print("\r[" + "#".repeat(done) + ".".repeat(50 - done) + "]");
        System.out.flush();
    }
}
