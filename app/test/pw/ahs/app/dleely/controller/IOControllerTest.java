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

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class IOControllerTest {
    private IOController IO;
    private Path baseDir;

    @Before
    public void setUp() throws Exception {
        IO = IOController.getInstance();
        baseDir = IO.getNormPath(System.getProperty("java.io.tmpdir"));
    }

    @Test
    public void testCreateHiddenDir() throws Exception {
        Path path = baseDir.resolve(".bla");
        IO.createHiddenDir(path);
        assertTrue(Files.isDirectory(path));
        assertTrue((boolean) Files.getAttribute(path, "dos:hidden", LinkOption.NOFOLLOW_LINKS));
    }

    @Test
    public void testGetFileExt() throws Exception {
        Path path = baseDir.resolve("bla.txt");
        assertEquals("txt", IO.getFileExt(path));

        path = baseDir.resolve("bla.txt.zip");
        assertEquals("zip", IO.getFileExt(path));

        path = baseDir.resolve("bla.h2.db");
        assertEquals("h2.db", IO.getFileExt(path));
    }

    @Test
    public void testSetFileExt() throws Exception {
        Path path = baseDir.resolve("bla.txt");
        assertEquals("bla.zip", IO.setFileExt(path, "zip").getFileName().toString());

        path = baseDir.resolve("bla.txt.zip");
        assertEquals("bla.txt.rar", IO.setFileExt(path, "rar").getFileName().toString());

        path = baseDir.resolve("bla.h2.db");
        assertEquals("bla.tar.gz", IO.setFileExt(path, "tar.gz").getFileName().toString());
    }
}
