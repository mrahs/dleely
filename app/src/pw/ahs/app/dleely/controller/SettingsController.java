/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.controller;

import pw.ahs.app.dleely.Globals;
import pw.ahs.app.dleely.gui.TwoParamFunction;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static pw.ahs.app.dleely.controller.SettingsController.Setting.*;

public class SettingsController {
    /*
   ************************************************************
   *
   * Settings Literals
   *
   ************************************************************/
    public static enum Setting {
        sLang,
        sFile_Path,
        sReopen_Last_File,
        sRemember_Opened_Files,
        sCheck_For_Update_On_Startup,
        sConfirm_DELETE;

        @Override
        public String toString() {
            return super.toString().substring(1);
        }

        public String t() {
            return toString();
        }
    }

    public static final int RECENT_FILES_LIMIT = 5;

     /*
    ************************************************************
    *
    * Fields
    *
    ************************************************************/


    private final Properties settingsDefault;
    private final Properties settings;
    private final Map<Setting, Set<TwoParamFunction<String, String>>> listeners;

    private static SettingsController instance = null;

    public static SettingsController getInstance() {
        if (instance == null)
            instance = new SettingsController();
        return instance;
    }

    private SettingsController() {
        settingsDefault = new Properties();
        settings = new Properties(settingsDefault) {
            @Override
            public synchronized Object setProperty(String key, String value) {
                if (defaults.getProperty(key).equals(value)) {
                    remove(key);
                    return value;
                }
                return super.setProperty(key, value);
            }
        };
        listeners = new HashMap<>();
    }

    public void init() {
        // Language & File
        if (System.getProperty("user.language").startsWith("ar"))
            settingsDefault.setProperty(sLang.t(), "ar");
        else
            settingsDefault.setProperty(sLang.t(), "en");

        settingsDefault.setProperty(sFile_Path.t(), "");
        settingsDefault.setProperty(sRemember_Opened_Files.t(), "true");
        settingsDefault.setProperty(sReopen_Last_File.t(), "true");
        settingsDefault.setProperty(sCheck_For_Update_On_Startup.t(), "true");
        settingsDefault.setProperty(sConfirm_DELETE.t(), "true");
    }

    public void setSetting(Setting key, String val) {
        String oldValue = settings.getProperty(key.t());
        settings.setProperty(key.t(), val);
        fire(key, oldValue, val);
    }

    public String getSetting(Setting key) {
        return settings.getProperty(key.t());
    }

    public boolean getSettingBoolean(Setting key) {
        String val = settings.getProperty(key.t());
        return val != null && Boolean.parseBoolean(val);
    }

//    public void updateDefault(Setting key, String val) {
//        settingsDefault.setProperty(key.t(), val);
//    }

    public void restoreToDefault(Setting key) {
        String oldValue = settings.getProperty(key.t());
        settings.remove(key.t());
        fire(key, oldValue, settings.getProperty(key.t()));
    }

    public void restoreToDefault() {
        for (Setting key : Setting.values()) {
            fire(key, settings.getProperty(key.t()), settingsDefault.getProperty(key.t()));
        }
        settings.clear();
    }

    public boolean load(String filePath) {
        File f = new File(filePath);
        if (!f.exists()) return true;
        try {
            Reader r = new FileReader(f);
            settings.load(r);
            r.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean save(String filePath) {
        boolean doNotSave = settings.isEmpty();

        if (doNotSave) {
            try {
                Files.deleteIfExists(Paths.get(filePath));
            } catch (IOException ignored) {
                doNotSave = false;
            }
        }

        if (doNotSave) return true;

        try {
            Writer w = new FileWriter(filePath);
            settings.store(w, Globals.APP_TITLE + " v" + Globals.APP_VERSION + " Settings");
            w.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void fire(Setting key, String oldVal, String newVal) {
        Set<TwoParamFunction<String, String>> listeners = this.listeners.get(key);
        if (listeners == null) return;

        for (TwoParamFunction<String, String> listener : listeners) {
            listener.apply(oldVal, newVal);
        }
    }

    public void addListener(Setting setting, TwoParamFunction<String, String> listener) {
        listeners.merge(
                setting,
                new LinkedHashSet<>(Arrays.asList(listener)),
                (oldSet, newSet) -> {
                    newSet.addAll(oldSet);
                    return newSet;
                }
        );
    }
}
