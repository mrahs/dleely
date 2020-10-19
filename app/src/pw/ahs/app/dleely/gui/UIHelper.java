/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.gui;

import com.sun.javafx.scene.control.behavior.TextAreaBehavior;
import com.sun.javafx.scene.control.skin.TextAreaSkin;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import pw.ahs.app.dleely.gui.helper.*;


public class UIHelper {
    public static final MenuHelper menu = MenuHelper.getInstance();
    public static final AboutHelper about = AboutHelper.getInstance();
    public static final ListHelper list = ListHelper.getInstance();
    public static final IOHelper io = IOHelper.getInstance();
    public static final SettingsHelper settings = SettingsHelper.getInstance();

    public static void hackTextAreaTab(TextArea textArea) {
        // a hack to traverse focus on tab key pressed
        textArea.addEventFilter(KeyEvent.KEY_PRESSED, evt -> {
            if (evt.getCode() == KeyCode.TAB) {
                TextAreaSkin skin = (TextAreaSkin) textArea.getSkin();
                TextAreaBehavior behavior = skin.getBehavior();
                if (evt.isControlDown()) {
                    behavior.callAction("InsertTab");
                } else if (evt.isShiftDown()) {
                    behavior.callAction("TraversePrevious");
                } else {
                    behavior.callAction("TraverseNext");
                }
                evt.consume();
            } else if (evt.getCode() == KeyCode.ENTER) {
                evt.consume();
            }
        });
    }
}
