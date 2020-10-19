/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.store;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class SQLLoader {

    private static SQLLoader instance = null;
    private final Map<String, String> _sql = new HashMap<>();
    public final Map<String, String> sql = Collections.unmodifiableMap(_sql);

    public static SQLLoader getInstance() {
        if (instance == null)
            instance = new SQLLoader();
        return instance;
    }

    public SQLLoader() {
        Scanner scan = new Scanner(getClass().getResourceAsStream("/pw/ahs/app/dleely/store/sql.sql"), "UTF-8");
        while (scan.hasNext()) {
            String line = scan.nextLine().trim();
            if (line.startsWith("--")) {
                String key = line.substring(2);
                StringBuilder statement = new StringBuilder();
                while (!(line = scan.nextLine()).startsWith("--end")) {
                    statement.append(line).append("\n");
                }
                _sql.put(key, statement.toString());
            }
        }
        scan.close();
    }
}
