/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.controller;

import pw.ahs.app.dleely.Globals;
import pw.ahs.app.dleely.store.H2DBStore;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static pw.ahs.app.dleely.Globals.FILE_EXT;
import static pw.ahs.app.dleely.Globals.FILE_EXT_BACKUP;

public class IOController {

    private static IOController instance = null;

    public static IOController getInstance() {
        if (instance == null)
            instance = new IOController();
        return instance;
    }

    public static final String NAME_SEPARATOR = FileSystems.getDefault().getSeparator();
    /**
     * @param pathString the path string
     * @return a normalized path from the given string
     */
    public Path getNormPath(String pathString) {
        return Paths.get(pathString).normalize();
    }

    /**
     * Creates a hidden directory in Windows.
     * <p>If you want the directory to be hidden in Unix, change its name to start with a dot.</p>
     * <p>Any necessary parent directories will be created without being hidden.</p>
     *
     * @param path desired directory path
     * @throws java.nio.file.InvalidPathException if the path string cannot be converted to a Path
     */
    public void createHiddenDir(Path path) throws IOException {
        Files.createDirectories(path);
        try {
            Files.setAttribute(path, "dos:hidden", true, NOFOLLOW_LINKS);
        } catch (UnsupportedOperationException ignored) {
        }
    }

    /**
     * <p>
     * Extracts files from a given zip file into a given destinations.
     * </p>
     * If the destination path is a relative one, it'll be resolved to the given
     * zip file path.
     *
     * @param zipPath         the zip file path to extract from
     * @param toPath          the directory to extract to
     * @param replaceExisting true to replace existing files; false to skip (attributes may
     *                        be modified such as last modified time)
     * @return the destination directory
     * @throws java.io.IOException                if I/O error happens
     * @throws IllegalArgumentException           if zip file is not a file or destination path is not a
     *                                            directory
     * @throws java.nio.file.InvalidPathException if the given path is not valid
     */
    public Path unzip(Path zipPath, Path toPath, boolean replaceExisting) throws IOException {

        if (!Files.isRegularFile(zipPath, NOFOLLOW_LINKS)) throw new IllegalArgumentException("invalid file");

        if (!toPath.isAbsolute()) {
            // since the two paths come from the same provider,
            // it's ok to use .resolve(Path)
            toPath = zipPath.getParent().resolve(toPath);
        }

        Files.createDirectories(toPath);

        URI uri = URI.create("jar:" + zipPath.toUri().toString());
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        env.put("encoding", "UTF-8");
        FileSystem zipFs = FileSystems.newFileSystem(uri, env);

        CopyOption[] opt =
                (replaceExisting
                        ? new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING}
                        : new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES});

        Path zipRoot = zipFs.getPath("/");
        copy(zipRoot, toPath, opt);
        zipFs.close();
        return toPath;
    }

    /**
     * Zips a list of files into a zip file. Only regular files are zipped. If
     * directories were provided, they are ignored.
     *
     * @param zipPath         the zip file path. will be created if it doesn't exist.
     * @param paths           files to be zipped
     * @param replaceExisting true to replace existing files; false to skip (attributes may
     *                        be modified such as last modified time)
     * @return the zip file path
     * @throws java.io.IOException                if I/O error happens
     * @throws java.nio.file.InvalidPathException if the given path is not valid
     */
    public Path zipFiles(boolean replaceExisting, Path zipPath, Path... paths) throws IOException {
        URI uri = URI.create("jar:" + zipPath.toUri().toString());
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        env.put("encoding", "UTF-8");
        FileSystem zipFs = FileSystems.newFileSystem(uri, env);

        CopyOption[] opt =
                (replaceExisting
                        ? new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING}
                        : new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES});

        for (Path path : paths) {
            if (Files.isDirectory(path)) continue;
            Path to = zipFs.getPath("/" + path.getFileName());
            Files.copy(path, to, opt);
        }
        zipFs.close();
        return zipPath;
    }

    /**
     * Copies a file from the specified source to the specified destination using the specified options.
     * If the specified source is a directory, recursively copy all sub-files and sub-directories.
     * If the specified source is from a different provider than the specified target, it should be OK.
     * However, this method has only been tested with the default provider and the {@link com.sun.nio.zipfs.ZipFileSystemProvider}.
     * If a file cannot be copied for some reason, it's ignored (along with its sub-tree if it's a directory).
     *
     * @param source  the source file
     * @param target  the target file
     * @param options copy options
     * @throws java.io.IOException
     */
    public void copy(final Path source, final Path target, final CopyOption... options) throws IOException {
        Files.walkFileTree(source, new FileVisitor<Path>() {
            private void copyIt(Path from, Path to) throws IOException {
                Files.copy(from, to, options);
            }

            private Path appendSkipRoot(Path lPath, Path rPath) {
                if (rPath.getRoot() != null) rPath = rPath.getRoot().relativize(rPath);
                return lPath.resolve(rPath.toString());
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path newDir = appendSkipRoot(target, source.relativize(dir));
                // try to copy. if it fails, skip subtree
                try {
                    copyIt(dir, newDir);
                } catch (IOException e) {
                    // if file already exists, skip it and continue
                    // if the destination directory already exists, it's ok, continue
                    if (!(e instanceof FileAlreadyExistsException
                            || e instanceof DirectoryNotEmptyException)) return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path newFile = appendSkipRoot(target, source.relativize(file));
                // try to copy. if it fails ignore and continue
                try {
                    copyIt(file, newFile);
                } catch (IOException ignored) {

                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) throw exc;
                Path newDir = appendSkipRoot(target, source.relativize(dir));
                try {
                    Files.setLastModifiedTime(newDir, Files.getLastModifiedTime(dir));
                } catch (IOException e) {
                    if (!(e instanceof NoSuchFileException)) throw e;
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Get the extension of the file.
     *
     * @param path path to a file
     * @return a string represents file extension without the dot;
     * an empty string if the filePath doesn't have a file or if it's empty or the file doesn't have an extension.
     */
    public String getFileExt(Path path) {
        String fileNameString = path.getFileName().toString();
        if (fileNameString.endsWith(".h2.db"))
            return "h2.db";
        int i = fileNameString.lastIndexOf('.');
        if (i > 0)
            return fileNameString.substring(i + 1);
        return "";
    }

    /**
     * Delete a file.
     * If it's a directory, recursively delete all sub-directories and sub-files.
     * If a file cannot be deleted for some reason, an exception will be thrown.
     *
     * @param path the file to delete
     * @throws java.io.IOException if I/O error happens
     */
    public void deleteFile(Path path) throws IOException {
        if (Files.notExists(path, NOFOLLOW_LINKS)) return;

        Files.walkFileTree(path, new FileVisitor<Path>() {
            private boolean deleteIt(Path p) throws IOException {
                return Files.deleteIfExists(p);
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                deleteIt(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                if (exc != null) throw exc;
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                deleteIt(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * @param text    text to write
     * @param path    file path
     * @param charset encoding
     * @return true if text was successfully written, false otherwise
     */
    public boolean writeTextToFile(String text, Path path, Charset charset) {
        try (BufferedWriter writer = Files.newBufferedWriter(path, charset)) {
            writer.write(text);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Change the file extension to the given one. No IO operations will take place.
     * If the given extension is empty, remove the extension.
     * If the given path is a directory, the given extension is appended nonetheless.
     * The special case for .h2.db files is handled.
     *
     * @param path file path
     * @param ext  desired extension without the dot
     * @return a new path that is exactly the given path after appending the given extension
     */
    public Path setFileExt(Path path, String ext) {
        ext = ext.trim();

        String fileNameString = path.getFileName().toString();
        if (fileNameString.endsWith(".h2.db"))
            fileNameString = fileNameString.substring(0, fileNameString.length() - 6);

        int i = fileNameString.lastIndexOf('.');
        if (i > 0) {// might start with a dot (hidden file on linux)
            fileNameString = fileNameString.substring(0, i);
        }

        if (path.getParent() == null)
            path = Paths.get("");
        else
            path = path.getParent();

        if (ext.isEmpty())
            return path.resolve(fileNameString);
        return path.resolve(fileNameString + '.' + ext);
    }


    /**
     * Wraps {@link #deleteFile(java.nio.file.Path)} to suppress exceptions.
     *
     * @param path file path
     */
    public void deleteIfPossible(Path path) {
        try {
            deleteFile(path);
        } catch (IOException ignored) {
        }
    }

    /**
     * @param path         file path
     * @param checkContent if true, check file content
     * @return true if the given path is a Dleely h2 file, false otherwise
     */
    public boolean isDleelyH2DBFile(Path path, boolean checkContent) {
        if (!getFileExt(path).equalsIgnoreCase(FILE_EXT))
            return false;
        if (!checkContent) return true;
        if (!Files.isReadable(path)) return false;

        try (ZipFile zf = new ZipFile(path.toFile(), Charset.forName("utf-8"))) {
            String dbFileNameString = setFileExt(path.getFileName(), H2DBStore.H2DB_FILE_EXT).toString();
            ZipEntry ze = zf.getEntry(dbFileNameString);
            return ze != null;
        } catch (IOException | IllegalStateException ignore) {
            return false;
        }
    }

    /**
     * @param path file path
     * @return true if the given path is a Dleely backup file, false otherwise
     */
    public boolean isDleelyBackupFile(Path path) {
        return getFileExt(path).equalsIgnoreCase(FILE_EXT_BACKUP);
    }

    /**
     * @param path file path
     * @return true if the given path is a Dleely export/import file, false otherwise
     */
    public boolean isDleelyImportExportFile(Path path) {
        String fileExt = getFileExt(path);
        String[] extensions = Globals.EXPORT_IMPORT_FORMATS.split(";");
        for (String ext : extensions) {
            if (fileExt.equalsIgnoreCase(ext)) return true;
        }
        return false;
    }

    /**
     * @param path file path
     * @return true if the given path exists and is a regular file , false otherwise
     */
    public boolean isExistingFile(Path path) {
        return Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS);
    }

    /**
     * @param path file path
     * @return true if the given path exists and is a directory file , false otherwise
     */
    public boolean isExistingDir(Path path) {
        return Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS);
    }
}
