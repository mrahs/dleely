/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.controller;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.*;

public class ControllerTest {
    private Controller C;
    @Before
    public void setUp() throws Exception {
        C = Controller.getInstance();
    }

    @Test
    public void testJoinArray() throws Exception {
        long[] arr = new long[] {1,2,3,4,5};
        String arrString = C.joinArray(arr, " ");
        assertEquals("1 2 3 4 5", arrString);
        arrString = C.joinArray(arr, ", ");
        assertEquals("1, 2, 3, 4, 5", arrString);
    }

    @Test
    public void testTrimQuotes() throws Exception {
        String trimmed = C.trimQuotes("Hi there :)");
        assertEquals("Hi there :)", trimmed);

        trimmed = C.trimQuotes("'Hi there :)");
        assertEquals("Hi there :)", trimmed);

        trimmed = C.trimQuotes("Hi there :)'");
        assertEquals("Hi there :)", trimmed);

        trimmed = C.trimQuotes("'Hi there :)'");
        assertEquals("Hi there :)", trimmed);

        trimmed = C.trimQuotes("\"Hi there :)");
        assertEquals("Hi there :)", trimmed);

        trimmed = C.trimQuotes("Hi there :)\"");
        assertEquals("Hi there :)", trimmed);

        trimmed = C.trimQuotes("\"Hi there :)\"");
        assertEquals("Hi there :)", trimmed);

        trimmed = C.trimQuotes("\"\"\"Hi there :)\"");
        assertEquals("Hi there :)", trimmed);

        trimmed = C.trimQuotes("'''Hi there :)\"");
        assertEquals("Hi there :)", trimmed);
    }
}
