/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.controller;

import pw.ahs.app.dleely.gui.OneParamFunction;

import java.util.Collection;
import java.util.LinkedHashSet;

public class CurrentLangController {
    private String lang = "en";
    private final Collection<OneParamFunction<String>> listeners = new LinkedHashSet<>();
    private final Collection<OneParamFunction<String>> beforeListeners = new LinkedHashSet<>();

    private static CurrentLangController instance = null;

    public static CurrentLangController getInstance() {
        if (instance == null)
            instance = new CurrentLangController();
        return instance;
    }

    private CurrentLangController() {
    }

    public void setLang(String lang) {
        for (OneParamFunction<String> f : beforeListeners) {
            f.apply(lang);
        }
        this.lang = lang;
        for (OneParamFunction<String> f : listeners) {
            f.apply(lang);
        }
    }

    public String getLang() {
        return lang;
    }

    public void addListener(OneParamFunction<String> listener) {
        listeners.add(listener);
    }

    public void addBeforeChangeListener(OneParamFunction<String> listener) {
        beforeListeners.add(listener);
    }
}
