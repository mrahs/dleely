/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

import pw.ahs.app.dleely.controller.IOController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Test {
    private static final IOController IO = IOController.getInstance();
    private static final Path baseDir = IO.getNormPath(System.getProperty("java.io.tmpdir"));

    public static void main(String[] args) throws IOException {
//        LocalDateTime now = LocalDateTime.now();
//        System.out.println(now.toInstant(ZoneOffset.UTC).toEpochMilli());
//        System.out.println(new Timestamp(now.toInstant(ZoneOffset.UTC).toEpochMilli()).getTime());

        // Test IOController.zipFiles
        // testZipFiles();

        // Test IOController.unzip
        // testUnzip();

        // Test IOController.copy
        // tested with zip and unzip
// Test IOController.deleteFile
        // tested with deleteIfPossible

        // Test IOController.writeTextToFile

        // Test IOController.deleteIfPossible
        // Controller.io.deleteIfPossible(baseDir.resolve("1zip_test"));

        // Test IOController.isDleelyH2DBFile
        // System.out.println(Controller.io.isDleelyH2DBFile(baseDir.resolve("111/test."+Globals.FILE_EXT), false));
        // System.out.println(Controller.io.isDleelyH2DBFile(baseDir.resolve("111/test."+Globals.FILE_EXT), true));

        // Test IOController.isDleelyBackupFile
        // System.out.println(Controller.io.isDleelyBackupFile(baseDir.resolve("111/test."+Globals.FILE_EXT_BACKUP)));

        // Test IOController.isDleelyImportExportFile
        // System.out.println(Controller.io.isDleelyImportExportFile(baseDir.resolve("111/test.htm")));
        // System.out.println(Controller.io.isDleelyImportExportFile(baseDir.resolve("111/test.html")));
        // System.out.println(Controller.io.isDleelyImportExportFile(baseDir.resolve("111/test.csv")));
        // System.out.println(Controller.io.isDleelyImportExportFile(baseDir.resolve("111/test.xml")));

        // Test IOController.isExistingFile
        // System.out.println(Controller.io.isExistingFile(baseDir.resolve("111/test.txt")));
        // System.out.println(Controller.io.isExistingFile(baseDir.resolve("111/test")));

        // Test IOController.isExistingDir
        // System.out.println(Controller.io.isExistingDir(baseDir.resolve("111/test")));
        // System.out.println(Controller.io.isExistingDir(baseDir.resolve("111/test.txt")));

//        Item item = new Item("Name", "Ref");
//        item.setId(1);
//        item.setInfo("Info");
//        item.setPrivy(true);
//        item.getTags().addAll(Arrays.asList(new Tag("tag1"), new Tag("tag2")));
//        System.out.println(item);
    }

    private static void testZipFiles() throws IOException {
        Path tmp = baseDir.resolve("1zip_test");
        Path p1 = tmp.resolve("f1.txt");
        Path p2 = tmp.resolve("f2.txt");
        Path p3 = tmp.resolve("f3");

        Files.createDirectory(tmp);
        Files.createFile(p1);
        Files.createFile(p2);
        Files.createDirectory(p3);

        IO.zipFiles(true, tmp.resolve("f1.zip"), p1, p2, p3);
    }

    private static void testUnzip() throws IOException {
        Path tmp = baseDir.resolve("1zip_test");

        IO.unzip(tmp.resolve("f1.zip"), tmp, true);
    }
}