/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely;

import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import pw.ahs.app.dleely.exporter.ExportConfigs;
import pw.ahs.app.dleely.gui.App;
import pw.ahs.app.dleely.model.Item;

import java.text.Collator;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Globals {
    // meta data
    public static final String APP_TITLE = "Dleely";
    public static final String APP_VERSION = "1.0.0";
    public static final String APP_HOME = "http://ahs.pw/app/dleely/";
    public static final String DEV_HOME = "http://ahs.pw/";
    public static final String PREFS_FILE_NAME = APP_TITLE + ".conf";


    // IO
    public static final String FILE_EXT = "dly";
    public static final String FILE_EXT_BACKUP = "dlb";
    public static final String EXPORT_IMPORT_FORMATS = "html;htm;csv;xml";
    public static final String DEFAULT_FILE_NAME = "Dleely";
    public static final FileChooser.ExtensionFilter DLEELY_EXT_FILTER =
            new FileChooser.ExtensionFilter("Dleely Files (." + FILE_EXT + ")", "*." + FILE_EXT);
    public static final FileChooser.ExtensionFilter IMPORT_EXT_FILTER =
            new FileChooser.ExtensionFilter("Dleely Import Files (csv, html, js, xml)", "*.csv", "*.html", "*.htm", "*.js", "*.xml");

    // Search Flags
    public static final int SEARCH_TAG_MASK = 1 << 1;
    public static final int SEARCH_NAME_MASK = 1 << 2;
    public static final int SEARCH_URL_MASK = 1 << 3;
    public static final int SEARCH_NOTES_MASK = 1 << 4;
    public static final int SEARCH_UNTAGGED_MASK = 1 << 5;

    // Import Export File Formats
    public static enum FileFormat {
        DLEELY_CVS, DIIGO, NETSCAPE, DLEELY_JSON, DLEELY_XML;

        public String getExt() {
            switch (this) {
                case DIIGO:
                case DLEELY_CVS:
                    return "csv";
                case NETSCAPE:
                    return "html";
                case DLEELY_JSON:
                    return "js";
                case DLEELY_XML:
                    return "xml";
                default:
                    return "";
            }
        }
    }

    public static final ExportConfigs EXPORT_CONFIGS_SKIP_ID_ONLY = new ExportConfigs();

    // Other
    public static final long NULL_ID = -1;
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd kk:mm:ss", Locale.US);
    public static final Locale LOCAL_AR = new Locale("ar", "SA");

    public static long[] extractIds(Collection<Item> items) {
        long[] ids = new long[items.size()];
        Iterator<Item> itr = items.iterator();
        for (int i = 0; i < items.size(); ++i) {
            ids[i] = itr.next().getId();
        }
        return ids;
    }

    // UI
    public static App view = null;
    public static ResourceBundle i18n = ResourceBundle.getBundle("pw.ahs.app.dleely.i18n.word", Locale.US);
    public static final Collator COLLATOR_EN = Collator.getInstance(new Locale("en", "US"));
    public static final Collator COLLATOR_AR = Collator.getInstance(new Locale("ar", "SA"));
    public static Collator theCollator = COLLATOR_EN;
    public static final StringConverter<String> THE_LANG_NAME_CODE_CONVERTER = new StringConverter<String>() {
        @Override
        public String toString(String s) {
            switch (s) {
                case "en":
                    return "English";
                case "ar":
                    return "العربية";
                case "English":
                    return "en";
                case "العربية":
                    return "ar";
                default:
                    return "";
            }
        }

        @Override
        public String fromString(String s) {
            return toString(s);
        }
    };
    public static final Comparator<String> THE_TEXT_COMPARATOR = (o1, o2) -> {
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) return 1;
        if (o2 == null) return -1;

        if (o1.isEmpty() && o2.isEmpty()) return 0;
        if (o1.isEmpty()) return 1;
        if (o2.isEmpty()) return -1;

        return theCollator.compare(o1, o2);
    };
    public static final Comparator<LocalDateTime> THE_DATE_TIME_COMPARATOR = (o1, o2) -> {
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) return 1;
        if (o2 == null) return -1;

        return o1.compareTo(o2);
    };


    // user agent
    public static String getUserAgentString() {
        return APP_TITLE
                + "/"
                + APP_VERSION
                + "; "
                + "Java/"
                + System.getProperty("java.version")
                + " ("
                + System.getProperty("os.name")
                + " "
                + System.getProperty("os.version")
                + "; "
                + System.getProperty("os.arch")
                + ")";
    }

    // initializer
    static {
        EXPORT_CONFIGS_SKIP_ID_ONLY.addSkipField(Item.Field.ID);
    }
}
