/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.controller;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collection;

import static pw.ahs.app.dleely.controller.SettingsController.Setting.sFile_Path;

public class Controller {
    public static final BeforeExitController beforeExit = BeforeExitController.getInstance();
    public static final CurrentLangController curLang = CurrentLangController.getInstance();
    public static final IOController io = IOController.getInstance();
    public static final SettingsController settings = SettingsController.getInstance();
    public static final Controller util = Controller.getInstance();
    public static final InternetController net = InternetController.getInstance();
    public static final UndoRedoController undoRedo = UndoRedoController.getInstance();
    public static final UnsavedController unsaved = UnsavedController.getInstance();


    private static Controller instance = null;

    public static Controller getInstance() {
        if (instance == null)
            instance = new Controller();
        return instance;
    }

    private Controller() {
    }

    public Timestamp toTimeStamp(LocalDateTime localDateTime) {
        return new Timestamp(localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli());
    }

    public LocalDateTime toLocalDateTime(Timestamp timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp.getTime());
        return LocalDateTime.ofEpochSecond(instant.getEpochSecond(), instant.getNano(), ZoneOffset.UTC);
    }

    public LocalDateTime toLocalDateTime(String millisString) {
        Instant instant = Instant.ofEpochMilli(Long.parseLong(millisString));
        return LocalDateTime.ofEpochSecond(instant.getEpochSecond(), instant.getNano(), ZoneOffset.UTC);
    }

    public long toMillis(LocalDateTime localDateTime) {
        return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public String joinArray(long[] array, String del) {
        if (array.length == 0) return "";

        StringBuilder str = new StringBuilder();
        for (long id : array) {
            str.append(id).append(del);
        }
        str.delete(str.length() - del.length(), str.length());
        return str.toString();
    }

    public String joinArray(Object[] array, String del) {
        if (array.length == 0) return "";

        StringBuilder str = new StringBuilder();
        for (Object obj : array) {
            str.append(obj).append(del);
        }
        str.delete(str.length() - del.length(), str.length());
        return str.toString();
    }

    public String joinCollection(Collection<?> collection, String del) {
        if (collection.isEmpty()) return "";

        StringBuilder str = new StringBuilder();
        for (Object obj : collection) {
            str.append(obj).append(del);
        }
        str.delete(str.length() - del.length(), str.length());
        return str.toString();
    }

    public Object[] sortArray(Object[] array) {
        Arrays.sort(array);
        return array;
    }

    public String trimQuotes(String text) {
        int start = 0;
        int end = text.length();
        for (int i = start; i < end; ++i, ++start) if (text.charAt(i) != '"' && text.charAt(i) != '\'') break;
        for (int i = end - 1; i > start; --i, --end) if (text.charAt(i) != '"' && text.charAt(i) != '\'') break;

        return text.substring(start, end);
    }

    public String[] parseCSVLine(String line) {
        return line.split("\",\"");
    }

    public String handleRecentOpenedFiles(String newPath) {
        /*
        Notes:
        1. Path separator is ;
        2. Must NOT start with ;
        3. Must NOT end with ;
         */
        String pathsString = Controller.settings.getSetting(sFile_Path);
        String[] paths = pathsString.split(";");
        String newPathsString = newPath;
        for (int i = 0, j = 1; i < paths.length && j < SettingsController.RECENT_FILES_LIMIT; i++) {
            if (paths[i].isEmpty() || paths[i].equals(newPath))
                continue;
            newPathsString += ";" + paths[i];
            j++;
        }
        return newPathsString;
    }
}
