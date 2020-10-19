/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.utils;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.junit.Test;

import static junit.framework.Assert.*;

public class LogTest {

    @Test
    public void testLog() throws Exception {
        Log log = new Log();
        BooleanProperty listenerInvoked = new SimpleBooleanProperty(false);
        Log.LogListener logListener = loggedLine -> listenerInvoked.set(true);
        log.addListener(logListener);
        log.setAutoCommitOn(4);
        log.setClearOnCommit(true);

        log.setStatus(Log.LogStatus.ALL);
        log.i("Info", "info msg");
        log.w("Warning", "warning msg");
        log.e("Error", "error msg");

        assertEquals(3, log.getLines().size());
        assertTrue(listenerInvoked.get());

        log.clear();

        log.setStatus(Log.LogStatus.ALERT_ONLY);
        log.i("Info", "info msg");
        log.w("Warning", "warning msg");
        log.e("Error", "error msg");

        assertEquals(1, log.getLines().size());
        assertEquals("Warning", log.getLines().iterator().next().getTitle());

        log.clear();

        log.setStatus(Log.LogStatus.CRITICAL);
        log.i("Info", "info msg");
        log.w("Warning", "warning msg");
        log.e("Error", "error msg");

        assertEquals(1, log.getLines().size());
        assertEquals("Error", log.getLines().iterator().next().getTitle());

        log.clear();

        log.setStatus(Log.LogStatus.NONE);
        log.i("Info", "info msg");
        log.w("Warning", "warning msg");
        log.e("Error", "error msg");

        assertEquals(0, log.getLines().size());

        log.clear();

        log.setStatus(Log.LogStatus.NON_CRITICAL);
        log.i("Info", "info msg");
        log.w("Warning", "warning msg");
        log.e("Error", "error msg");

        assertEquals(2, log.getLines().size());

        log.clear();

        log.setStatus(Log.LogStatus.INFO);
        log.i("Info", "info msg");
        log.w("Warning", "warning msg");
        log.e("Error", "error msg");

        assertEquals(1, log.getLines().size());
        assertEquals("Info", log.getLines().iterator().next().getTitle());
    }
}
