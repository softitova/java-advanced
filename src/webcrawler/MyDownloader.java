package ru.ifmo.ctddev.titova.webcrawler;

import info.kgeorgiy.java.advanced.crawler.CachingDownloader;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static ru.ifmo.ctddev.titova.webcrawler.DownloadUtils.*;

/**
 * @author Titova Sophia
 *         Dounload math. physics, informatics books from <tt>https://e.lanbook.com</tt> for last 3 years.
 *         <p>
 *         Downloads full information : Name, link, author, year etc.
 *         </p>
 */
public class MyDownloader {


    /**
     * Entry point into modification downloading program.
     *
     * @param args ignores command line args.
     * @throws IOException        if i/o error occurs.
     * @throws URISyntaxException syntax exception occurs.
     */
    public static void main(String[] args) throws IOException, URISyntaxException {

        download();
        parse();
    }

    private static void download() throws IOException {
        WebCrawler webCrawler = new WebCrawler(
                new CachingDownloader(DIR), 400, 250, 100,
                x -> (x.equals(MAIN_PAGE) || (x.startsWith(BOOK_URL) && !x.contains("download_file")))
                        || Arrays.stream(CODES).anyMatch(code ->
                        x.equals(String.format(HEADER_URL, code))
                                || x.startsWith(String.format(PAGE_URL, code))
                )
        );
        webCrawler.download(MAIN_PAGE, Integer.MAX_VALUE);
        webCrawler.close();
    }

    private static void parse() throws FileNotFoundException, URISyntaxException {
        PrintWriter out = new PrintWriter(BIBLIOGRAPHIC_RECORD_TXT);
        String[] bookPages = DIR.toFile().list();
        BufferedReader in;

        for (String book : bookPages) {
            in = new BufferedReader(new FileReader(DIR.resolve(book).toFile()));
            String html = in.lines().collect(Collectors.joining());
            Matcher info = infoPattern.matcher(html);
            Matcher year = yearPattern.matcher(html);
            while (year.find()) {
                String curYear = html.substring(year.start() + PRE_YEAR.length(),
                        year.end()).trim();
                curYear = curYear.substring(4, curYear.length() - 5);
                if (Integer.parseInt(curYear) > MIN_YEAR && info.find()) {
                    out.println(html.substring
                            (info.start() + PRE_BIBL_RECORD.length(),
                                    +info.end() - POST_BIBL_RECORD.length()).trim());
                }
            }
        }
        out.close();
    }


}


