/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.controller;

import java.util.ArrayList;
import java.util.Collection;

public class BeforeExitController {
    private final Collection<Runnable> beforeClosingController = new ArrayList<>();
    private static BeforeExitController instance = null;

    public static BeforeExitController getInstance() {
        if (instance == null)
            instance = new BeforeExitController();
        return instance;
    }

    private BeforeExitController() {
    }

    public void addAction(Runnable action) {
        beforeClosingController.add(action);
    }

    public void doAll() {
        for (Runnable action : beforeClosingController) {
            action.run();
        }
    }
}
