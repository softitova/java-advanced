package ru.ifmo.ctddev.titova.webcrawler;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.regex.Pattern;

/**
 * @author Titova Sophia
 *         Utils for modification.
 */
class DownloadUtils {
    /**
     * Constants to parse html page.
     */
    static final String MAIN_PAGE = "https://e.lanbook.com/books";
    static final String HEADER_URL = "https://e.lanbook.com/books/%d";
    static final String PAGE_URL = "https://e.lanbook.com/books/%d?page=";
    static final String BOOK_URL = "https://e.lanbook.com/book/";
    static final int CODES[] = new int[]{917, 918, 1537};
    static final Path DIR = Paths.get("Data");
    static final String BIBLIOGRAPHIC_RECORD_TXT = "bibliographic_record.txt";
    static final String PRE_YEAR = "<dt>Год:</dt>";
    static final String PRE_BIBL_RECORD = "<div id=\"bibliographic_record\">";
    static final String POST_BIBL_RECORD = "</div>";
    static final int MIN_YEAR = Calendar.getInstance().get(Calendar.YEAR) - 5;
    /**
     * Patterns to parse html page.
     */
    static final Pattern infoPattern = Pattern.compile(PRE_BIBL_RECORD + "[^<]*" + POST_BIBL_RECORD);
    static final Pattern yearPattern = Pattern.compile(PRE_YEAR + "\\s*<dd>\\d*</dd>");
}
