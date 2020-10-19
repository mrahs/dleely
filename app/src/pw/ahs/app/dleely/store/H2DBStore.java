/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.store;

import pw.ahs.app.dleely.Globals;
import pw.ahs.app.dleely.controller.Controller;
import pw.ahs.app.dleely.exporter.ExportConfigs;
import pw.ahs.app.dleely.exporter.Exporter;
import pw.ahs.app.dleely.exporter.TextExporter;
import pw.ahs.app.dleely.exporter.XMLExporter;
import pw.ahs.app.dleely.gui.OneParamFunction;
import pw.ahs.app.dleely.gui.TwoParamFunction;
import pw.ahs.app.dleely.importer.*;
import pw.ahs.app.dleely.model.Item;
import pw.ahs.app.dleely.model.Tag;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static pw.ahs.app.dleely.Globals.NULL_ID;


public class H2DBStore implements IStore {

    // DB

    private static final String DB_DRIVER = "org.h2.Driver";
    private final Map<String, String> sql;
    private Connection con = null;

    // IO

    public static final String H2DB_FILE_EXT = "h2.db";
    private static final String DB_USER = "Dleely";
    private static final String DB_PASS = "&3bHie3&!`";
    private static final String BK_PASS = "lH0(7!@folDS";
    private static final String DB_FILE_NAME = "Dleely";
    private static final String TEMP_DIR_SUFFIX = "_dleely";

    private final Path filePath;
    private final Path tempDirPath;
    private final Path h2dbPath;

    // Workflow

    private final AtomicBoolean cancel = new AtomicBoolean();
    private final AtomicBoolean working = new AtomicBoolean();

    public H2DBStore(Path path) throws Exception {
        Class.forName(DB_DRIVER);

        filePath = path;
        if (filePath == null) {
            tempDirPath = null;
            h2dbPath = null;
        } else {
            if (!Controller.io.isDleelyH2DBFile(filePath, false)) throw new IllegalArgumentException("invalid file");
            tempDirPath = Controller.io.getNormPath(filePath.getParent().resolve("." + filePath.getFileName()) + TEMP_DIR_SUFFIX);
            h2dbPath = tempDirPath.resolve(DB_FILE_NAME);
        }

        // load sql
        sql = SQLLoader.getInstance().sql;
    }

    private Path getH2dbPathWithExt() {
        return Controller.io.setFileExt(h2dbPath, H2DB_FILE_EXT);
    }

    private void connect(String dbUrl) throws SQLException {
        con = DriverManager.getConnection(dbUrl, DB_USER, DB_PASS);
        con.setAutoCommit(true);
    }

    private boolean verifyDb() throws SQLException {
        boolean good = false;
        DatabaseMetaData dbmt = con.getMetaData();
        ResultSet rs = dbmt.getTables(null, null, "%", new String[]{"TABLE"});
        if (rs.isBeforeFirst()) {
            boolean tableFound[] = {false, false, false, false};
            while (rs.next() && !good) {
                String tableName = rs.getString("TABLE_NAME");
                if (tableName.equalsIgnoreCase("item")) tableFound[0] = true;
                else if (tableName.equalsIgnoreCase("tag")) tableFound[1] = true;
                else if (tableName.equalsIgnoreCase("tag_item")) tableFound[2] = true;
                good = tableFound[0] && tableFound[1] && tableFound[2];
            }
        }
        rs.close();
        return good;
    }

    public Path getPath() {
        return filePath;
    }

    @Override
    public boolean checkInappropriateClose() {
        return !isOpened() && Controller.io.isExistingDir(tempDirPath) && Controller.io.isExistingFile(getH2dbPathWithExt());
    }

    @Override
    public void open(boolean create, boolean attemptRecoveryIfNeeded) throws Exception {
        if (isOpened())
            throw new IllegalStateException("Already opened, should close first");

        if (filePath == null) {
            String dbUrl = "jdbc:h2:mem:";
            connect(dbUrl);
            PreparedStatement ps = con.prepareStatement(sql.get("create_db"));
            ps.execute();
            ps.close();

            return;
        }

        if (create)
            create(attemptRecoveryIfNeeded);
        else
            open(attemptRecoveryIfNeeded);
    }

    private void create(boolean attemptRecoveryIfNeeded) throws Exception {
        /*
        1. Attempt recovery if needed
        2. Remove temporary hidden directory if exists
        3. Create temporary hidden directory
        4. Prepare H2 database file path and connect to database
        5. Initialize database
        */

        // Step 1
        if (attemptRecoveryIfNeeded && checkInappropriateClose()) {
            throw new IllegalStateException("inappropriate close detected");
        }

        // Step 2
        Controller.io.deleteFile(tempDirPath);

        // Step 3
        Controller.io.createHiddenDir(tempDirPath);

        // Step 4
        String dbUrl = "jdbc:h2:" + h2dbPath + ";TRACE_LEVEL_FILE=0";

        try {
            connect(dbUrl);
        } catch (SQLException e) {
            Controller.io.deleteIfPossible(tempDirPath);
            throw e;
        }

        try {
            PreparedStatement ps = con.prepareStatement(sql.get("create_db"));
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            close();
            throw e;
        }
    }

    private void open(boolean attemptRecoveryIfNeeded) throws Exception {
        /*
        1. Create temporary hidden directory if not exists
        2. Attempt recovery if needed
        3. Unzip file content inside the aforementioned directory
        4. Prepare H2 database file path and connect to database
        5. Verify database
        */

        // Step 1
        Controller.io.createHiddenDir(tempDirPath);

        // Step 2 & 3
        if (!attemptRecoveryIfNeeded || !checkInappropriateClose()) {
            try {
                Controller.io.unzip(this.filePath, this.tempDirPath, true);
            } catch (IOException e) {
                Controller.io.deleteIfPossible(tempDirPath);
                throw e;
            }
        }

        // Step 4
        String dbUrl = "jdbc:h2:" + h2dbPath + ";TRACE_LEVEL_FILE=0;IFEXISTS=TRUE";

        try {
            connect(dbUrl);
        } catch (SQLException e) {
            Controller.io.deleteIfPossible(tempDirPath);
            throw e;
        }

        if (!verifyDb()) {
            // opened but not a database of our own
            close();
            throw new IllegalArgumentException("invalid file");
        }
    }

    @Override
    public boolean isOpened() {
        return con != null;
    }

    @Override
    public void close() throws Exception {
        if (!isOpened()) return;

        /*
        1. Close database connection
        2. Zip database file
        3. Delete temporary directory
         */

        try {
            con.close();
        } catch (SQLException e) {
            // ignored because we might still be able to zip files
        }

        con = null;

        if (filePath == null) return;

        // this exception will be thrown because we don't wanna delete the files if we cannot save them
        Controller.io.zipFiles(true, filePath, getH2dbPathWithExt());

        Controller.io.deleteIfPossible(tempDirPath);
    }

    @Override
    public boolean isWorking() {
        return working.get();
    }

    private void start() {
        working.set(true);
    }

    private void finish() {
        working.set(false);
        cancel.set(false);
    }

    @Override
    public void cancel() {
        cancel.set(true);
    }

    @Override
    public void backup(Path path) throws Exception {
        // prepare file
        if (Controller.io.getFileExt(path).isEmpty())
            path = Controller.io.setFileExt(path, Globals.FILE_EXT_BACKUP);

        if (!Controller.io.isDleelyBackupFile(path)) throw new Exception("h2.error.invalid-file");

        PreparedStatement ps = con.prepareStatement(sql.get("backup_db"));
        ps.setString(1, path.toString());
        ps.setString(2, BK_PASS);
        ps.execute();
        ps.close();
    }

    @Override
    public void restore(Path path) throws Exception {
        if (!Controller.io.isDleelyBackupFile(path)) throw new Exception("h2.error.invalid-file");

        PreparedStatement psClear = con.prepareStatement(sql.get("clear_db"));
        PreparedStatement psRestore = con.prepareStatement(sql.get("restore_db"));
        psRestore.setString(1, path.toString());
        psRestore.setString(2, BK_PASS);
        psClear.execute();
        psRestore.execute();
        psClear.close();
        psRestore.close();
    }

    @Override
    public boolean itemExist(String ref, Item[] item) throws Exception {
        boolean state = true;
        PreparedStatement ps = con.prepareStatement(
                sql.get("select_item_by_ref"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ps.setString(1, ref);
        ResultSet rs = ps.executeQuery();
        if (!rs.isBeforeFirst()) state = false;
        else if (item != null) {
            rs.next();
            Item i = fetchItem(rs);
            item[0] = i;
        }
        ps.close();
        return state;
    }

    @Override
    public boolean tagExist(String name, Tag[] tag) throws Exception {
        boolean state = true;
        PreparedStatement ps = con.prepareStatement(
                sql.get("select_tag_by_name"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ps.setString(1, name);
        ResultSet rs = ps.executeQuery();
        if (!rs.isBeforeFirst()) state = false;
        else if (tag != null) {
            rs.next();
            Tag t = fetchTag(rs);
            tag[0] = t;
        }
        ps.close();
        return state;
    }

    @Override
    public Item getItem(long id) throws Exception {
        Item i = null;
        PreparedStatement ps =
                con.prepareStatement(sql.get("select_item_by_id"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ps.setLong(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.isBeforeFirst()) {
            rs.next();
            i = fetchItem(rs);
        }
        ps.close();
        return i;
    }

    @Override
    public void loadAllItems(Collection<Item> items) throws Exception {
        start();
        try (PreparedStatement ps = con.prepareStatement(
                sql.get("select_all_items"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            ResultSet rs = ps.executeQuery();
            fetchItems(rs, items);
        } catch (SQLException e) {
            finish();
            throw e;
        }
        finish();
    }

    @Override
    public Collection<Item> getAllItems() throws Exception {
        Collection<Item> items = new HashSet<>();
        loadAllItems(items);
        return items;
    }

    @Override
    public void loadAllItems(Collection<Item> items, long limit, long offset) throws Exception {
        start();
        try (PreparedStatement ps = con.prepareStatement(
                sql.get("select_all_items_2"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            ps.setLong(1, limit);
            ps.setLong(2, offset);
            ResultSet rs = ps.executeQuery();
            fetchItems(rs, items);
        } catch (SQLException e) {
            finish();
            throw e;
        }
        finish();
    }

    @Override
    public Collection<Item> getAllItems(long limit, long offset) throws Exception {
        Collection<Item> items = new HashSet<>();
        loadAllItems(items, limit, offset);
        return items;
    }

    @Override
    public void loadUntaggedItems(Collection<Item> items) throws Exception {
        start();
        try (PreparedStatement ps = con.prepareStatement(
                sql.get("select_untagged_items"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            ResultSet rs = ps.executeQuery();
            fetchItems(rs, items);
        } catch (SQLException e) {
            finish();
            throw e;
        }
        finish();
    }

    @Override
    public Collection<Item> getUntaggedItems() throws Exception {
        Collection<Item> items = new HashSet<>();
        loadUntaggedItems(items);
        return items;
    }

    @Override
    public void loadItemsById(Collection<Item> items, long... ids) throws Exception {
        // prepare ids list
        String statement = sql.get("select_items_by_id").replace("CSV", Controller.util.joinArray(ids, ","));

        start();
        try (Statement s = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            ResultSet rs = s.executeQuery(statement);
            fetchItems(rs, items);
        } catch (SQLException e) {
            finish();
            throw e;
        }
        finish();
    }

    @Override
    public Collection<Item> getItemsById(long... ids) throws Exception {
        Collection<Item> items = new HashSet<>();
        loadItemsById(items, ids);
        return items;
    }

    @Override
    public void loadItemTags(long id, Collection<Tag> tags) throws Exception {
        loadItemTags(id, tags, true);
    }

    private void loadItemTags(long id, Collection<Tag> tags, boolean useStartFinish) throws SQLException {
        if (useStartFinish) start();
        PreparedStatement ps = con.prepareStatement(
                sql.get("select_item_tags"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ps.setLong(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.isBeforeFirst()) {
            while (rs.next()) {
                tags.add(fetchTag(rs));
                if (cancel.get()) break;
            }
        }
        ps.close();
        if (useStartFinish) finish();
    }

    @Override
    public Collection<Tag> getItemTags(long id) throws Exception {
        Collection<Tag> tags = new HashSet<>();
        loadItemTags(id, tags);
        return tags;
    }

    @Override
    public long getItemTagCount(long id) throws Exception {
        PreparedStatement ps = con.prepareStatement(
                sql.get("count_item_tags"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ps.setLong(1, id);
        ResultSet rs = ps.executeQuery();
        long count = fetchCount(rs);
        ps.close();
        return count;
    }

    @Override
    public Tag getTag(long id) throws Exception {
        Tag t = null;
        PreparedStatement ps = con.prepareStatement(
                sql.get("select_tag_by_id"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ps.setLong(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.isBeforeFirst()) {
            rs.next();
            t = fetchTag(rs);
        }
        ps.close();
        return t;
    }

    @Override
    public void loadAllTags(Collection<Tag> tags) throws Exception {
        start();
        try (PreparedStatement ps = con.prepareStatement(
                sql.get("select_tags"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            ResultSet rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    tags.add(fetchTag(rs));
                    if (cancel.get()) break;
                }
            }
        } catch (SQLException e) {
            finish();
            throw e;
        }
        finish();
    }

    @Override
    public Collection<Tag> getAllTags() throws Exception {
        Collection<Tag> tags = new HashSet<>();
        loadAllTags(tags);
        return tags;
    }

    @Override
    public void loadUnusedTags(Collection<Tag> tags) throws Exception {
        start();
        try (PreparedStatement ps = con.prepareStatement(
                sql.get("select_unused_tags"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            ResultSet rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    tags.add(fetchTag(rs));
                    if (cancel.get()) break;
                }
            }
        } catch (SQLException e) {
            finish();
            throw e;
        }
        finish();
    }

    @Override
    public Collection<Tag> getUnusedTags() throws Exception {
        Collection<Tag> tags = new HashSet<>();
        loadUnusedTags(tags);
        return tags;
    }

    @Override
    public void loadAllTags(Collection<Tag> tags, long limit, long offset) throws Exception {
        start();
        try (PreparedStatement ps = con.prepareStatement(
                sql.get("select_tags_2"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            ps.setLong(1, limit);
            ps.setLong(2, offset);
            ResultSet rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    tags.add(fetchTag(rs));
                    if (cancel.get()) break;
                }
            }
        } catch (SQLException e) {
            finish();
            throw e;
        }
        finish();
    }

    @Override
    public Collection<Tag> getAllTags(long limit, long offset) throws Exception {
        Collection<Tag> tags = new HashSet<>();
        loadAllTags(tags, limit, offset);
        return tags;
    }

    @Override
    public void loadTagsById(Collection<Tag> tags, long... ids) throws Exception {
        if (ids.length == 0) return;

        start();
        try (PreparedStatement ps = con.prepareStatement(
                sql.get("select_tags_by_id").replace("CSV", Controller.util.joinArray(ids, ",")),
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {

            ResultSet rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    tags.add(fetchTag(rs));
                    if (cancel.get()) break;
                }
            }
        } catch (SQLException e) {
            finish();
            throw e;
        }
        finish();
    }

    @Override
    public Collection<Tag> getTagsById(long... ids) throws Exception {
        Collection<Tag> tags = new HashSet<>();
        loadTagsById(tags, ids);
        return tags;
    }

    @Override
    public long getTagItemCount(long id) throws Exception {
        long count = 0;
        PreparedStatement ps = con.prepareStatement(
                sql.get("count_tag_items"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ps.setLong(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.isBeforeFirst()) {
            rs.next();
            count = rs.getLong(1);
        }
        ps.close();
        return count;
    }

    @Override
    public Map<Long, Long> getTagItemCounts(long... ids) throws Exception {
        Map<Long, Long> counts = new LinkedHashMap<>(ids.length);
        start();
        try {
            for (long id : ids) {
                counts.put(id, getTagItemCount(id));
                if (cancel.get()) break;
            }
        } catch (Exception e) {
            finish();
            throw e;
        }
        finish();
        return counts;
    }

    private long[] extractIds(Collection<Tag> tags) {
        long[] ids = new long[tags.size()];
        int i = 0;
        for (Tag tag : tags) {
            ids[i] = tag.getId();
            ++i;
        }
        return ids;
    }

    @Override
    public boolean addUpdateItem(Item item, boolean update) throws Exception {
        // insert item
        PreparedStatement ps = con.prepareStatement(
                update ? sql.get("update_item") : sql.get("insert_item"), PreparedStatement.RETURN_GENERATED_KEYS
        );

        ps.setString(1, item.getName());
        ps.setString(2, item.getRef());
        ps.setString(3, item.getInfo());
        ps.setBoolean(4, item.getPrivy());
        ps.setTimestamp(5, Controller.util.toTimeStamp(item.getDateAdd()));
        ps.setTimestamp(6, Controller.util.toTimeStamp(item.getDateMod()));

        if (update) {
            ps.setLong(7, item.getId());
            ps.setString(8, item.getRef());
            ps.setLong(9, item.getId());
        }

        int ar = ps.executeUpdate(); // affected rows

        if (update) {
            if (ar == 0) {
                // update rejected (updated ref. already exists)
                return false;
            } else {
                PreparedStatement ps2 = con.prepareStatement(sql.get("remove_item_tags"));
                ps2.setLong(1, item.getId());
                ps2.execute();
                ps2.close();
            }
        } else {
            if (ar == 0) {
                // nothing was inserted (already exists)
                return false;
            } else {
                ResultSet rs = ps.getGeneratedKeys();
                rs.next();
                item.setId(rs.getLong(1));
                rs.close();
            }
        }

        // insert tags
        tagItems(new long[]{item.getId()}, item.getTags().toArray(new Tag[item.getTags().size()]));

        ps.close();
        return true;
    }

    @Override
    public void addUpdateItems(Collection<Item> items, boolean update) throws Exception {
        start();
        try {
            Iterator<Item> itr = items.iterator();
            for (int i = 0; i < items.size(); ++i) {
                addUpdateItem(itr.next(), update);
                if (cancel.get()) break;
            }
        } catch (Exception e) {
            finish();
            throw e;
        }
        finish();
    }

    @Override
    public boolean addUpdateTag(Tag tag, boolean update) throws Exception {
        // insert tag
        PreparedStatement psTag = con.prepareStatement(
                update ? sql.get("update_tag") : sql.get("insert_tag"), PreparedStatement.RETURN_GENERATED_KEYS);
        psTag.setString(1, tag.getName());
        if (tag.getParentId() == NULL_ID)
            psTag.setNull(2, Types.BIGINT);
        else
            psTag.setLong(2, tag.getParentId());
        if (tag.getGroupId() == NULL_ID)
            psTag.setNull(3, Types.BIGINT);
        else
            psTag.setLong(3, tag.getGroupId());
        psTag.setTimestamp(4, Controller.util.toTimeStamp(tag.getDateAdd()));
        psTag.setTimestamp(5, Controller.util.toTimeStamp(tag.getDateMod()));

        if (update) {
            // update
            psTag.setLong(6, tag.getId());
            psTag.setString(7, tag.getName());
            psTag.setLong(8, tag.getId());
        }

        int ar = psTag.executeUpdate(); // affected rows

        if (update) {
            if (ar == 0) {
                // update rejected (updated name already exists)
                return false;
            }
        } else {
            if (ar == 0) {
                // nothing was inserted (already exists)
                return false;
            } else {
                ResultSet rs = psTag.getGeneratedKeys();
                rs.next();
                tag.setId(rs.getLong(1));
                rs.close();
            }
        }

        return true;
    }

    @Override
    public void tagItems(long[] ids, Tag[] tags) throws Exception {
        if (ids.length == 0 || tags.length == 0) return;

        boolean state = true;
        start();
        try (PreparedStatement psMap = con.prepareStatement(
                sql.get("insert_tag_item_map"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            for (long id : ids) {
                for (Tag t : tags) {
                    addUpdateTag(t, false);
                    psMap.setLong(1, id);
                    psMap.setString(2, t.getName());
                    psMap.addBatch();
                    if (cancel.get()) {
                        state = false;
                        break;
                    }
                }
                if (!state) break;
            }
            psMap.executeBatch();
        } catch (SQLException e) {
            finish();
            throw e;
        }
        finish();
    }

    @Override
    public boolean removeItem(long id) throws Exception {
        PreparedStatement ps = con.prepareStatement(
                sql.get("remove_item"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ps.setLong(1, id);
        int ar = ps.executeUpdate();
        ps.close();
        return ar > 0;
    }

    @Override
    public boolean removeItems(long... ids) throws Exception {
        String statement = sql.get("remove_items").replace("CSV", Controller.util.joinArray(ids, ","));

        Statement s = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        int ar = s.executeUpdate(statement);
        s.close();
        return ar > 0;
    }

    @Override
    public boolean removeTag(long id) throws Exception {
        PreparedStatement ps = con.prepareStatement(
                sql.get("remove_tag"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ps.setLong(1, id);
        int ar = ps.executeUpdate();
        ps.close();
        return ar > 0;
    }

    @Override
    public boolean removeTagWithItems(long id) throws Exception {
        PreparedStatement psItems = con.prepareStatement(
                sql.get("remove_tag_items"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        PreparedStatement psTag = con.prepareStatement(
                sql.get("remove_tag"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        psItems.setLong(1, id);
        psItems.execute();
        psTag.setLong(1, id);
        psTag.execute();
        psItems.close();
        psTag.close();
        return true;
    }

    @Override
    public long removeUnusedTags() throws Exception {
        PreparedStatement ps = con.prepareStatement(
                sql.get("remove_unused_tags"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        int ar = ps.executeUpdate();
        ps.close();
        return ar;
    }

    @Override
    public boolean replaceTag(long removeId, long keepId) throws Exception {
        PreparedStatement psRep = con.prepareStatement(sql.get("replace_tag"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        PreparedStatement psDel = con.prepareStatement(sql.get("remove_tag"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        psRep.setLong(1, keepId);
        psRep.setLong(2, removeId);
        psRep.setLong(3, keepId);
        psRep.setLong(4, removeId);
        psRep.execute();
        psDel.setLong(1, removeId);
        psDel.execute();

        psRep.close();
        psDel.close();
        return true;
    }

    @Override
    public boolean replaceTag(long removeId, Tag tag) throws Exception {
        addUpdateTag(tag, false);
        return replaceTag(removeId, tag.getId());
    }

    @Override
    public long getItemCount() throws Exception {
        PreparedStatement ps = con.prepareStatement(
                sql.get("count_items"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = ps.executeQuery();
        long count = fetchCount(rs);
        ps.close();
        return count;
    }

    @Override
    public long getTagCount() throws Exception {
        PreparedStatement ps = con.prepareStatement(
                sql.get("count_tags"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = ps.executeQuery();
        long count = fetchCount(rs);
        ps.close();
        return count;
    }

    @Override
    public long getUnusedTagCount() throws Exception {
        PreparedStatement ps = con.prepareStatement(
                sql.get("count_unused_tags"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = ps.executeQuery();
        long count = fetchCount(rs);
        ps.close();
        return count;
    }

    @Override
    public long getUntaggedItemCount() throws Exception {
        PreparedStatement ps = con.prepareStatement(
                sql.get("count_untagged_items"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = ps.executeQuery();
        long count = fetchCount(rs);
        ps.close();
        return count;
    }

    @Override
    public Tag getMostUsedTag() throws Exception {
        Tag t = null;
        PreparedStatement ps = con.prepareStatement(
                sql.get("select_most_used_tag"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = ps.executeQuery();
        if (rs.isBeforeFirst()) {
            rs.next();
            t = fetchTag(rs);
        }

        ps.close();
        return t;
    }

    @Override
    public Item getMostTaggedItem() throws Exception {
        Item i = null;
        PreparedStatement ps = con.prepareStatement(
                sql.get("select_most_tagged_item"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = ps.executeQuery();
        if (rs.isBeforeFirst()) {
            rs.next();
            i = fetchItem(rs);
        }

        ps.close();
        return i;
    }

    @Override
    public Collection<Tag> getTop5Tags() throws Exception {
        Collection<Tag> top = null;
        PreparedStatement ps = con.prepareStatement(
                sql.get("select_most_5_tags"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = ps.executeQuery();
        if (rs.isBeforeFirst()) {
            top = new LinkedHashSet<>(5);
            while (rs.next()) {
                top.add(fetchTag(rs));
            }
        }

        ps.close();
        return top;
    }

    @Override
    public Map<LocalDate, Long> getDateTimeCountMap() throws Exception {
        Map<LocalDate, Long> dateTimeLongMap = null;
        PreparedStatement ps = con.prepareStatement(
                sql.get("select_date_time_count"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = ps.executeQuery();
        if (rs.isBeforeFirst()) {
            dateTimeLongMap = new LinkedHashMap<>();
            while (rs.next()) {
                dateTimeLongMap.put(LocalDate.parse(rs.getString(1)), rs.getLong(2));
            }
        }

        ps.close();
        return dateTimeLongMap;
    }

    private Item fetchItem(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        Collection<Tag> tags = new HashSet<>();
        loadItemTags(id, tags, false);

        Item i = new Item(rs.getString("name"), rs.getString("ref"));
        i.setId(id);
        i.setInfo(rs.getString("info"));
        i.setPrivy(rs.getBoolean("privy"));
        i.getTags().addAll(tags);
        i.setDateAdd(Controller.util.toLocalDateTime(rs.getTimestamp("dateadd")));
        i.setDateMod(Controller.util.toLocalDateTime(rs.getTimestamp("datemod")));

        return i;
    }

    private Tag fetchTag(ResultSet rs) throws SQLException {
        Tag tag = Tag.getInstance(rs.getString("name"));
        tag.setId(rs.getLong("id"));

        long id = rs.getLong("parent_id");
        if (id == 0) id = -1;
        tag.setParentId(id);

        id = rs.getLong("group_id");
        if (id == 0) id = -1;
        tag.setGroupId(id);

        tag.setDateAdd(Controller.util.toLocalDateTime(rs.getTimestamp("dateadd")));
        tag.setDateMod(Controller.util.toLocalDateTime(rs.getTimestamp("datemod")));

        return tag;
    }

    private void fetchItems(ResultSet rs, Collection<Item> items) throws SQLException {
        if (!rs.isBeforeFirst()) return;
        while (rs.next()) {
            items.add(fetchItem(rs));
            if (cancel.get()) break;
        }
    }

    private long fetchCount(ResultSet rs) throws SQLException {
        if (!rs.isBeforeFirst()) return 0;
        rs.next();
        return rs.getLong(1);
    }

    @Override
    public long searchItems(Collection<Item> items, String searchQuery) throws Exception {
        searchQuery = searchQuery.trim();
        if (searchQuery.isEmpty()) {
            loadAllItems(items);
            if (items.isEmpty()) return 0;
            return items.size();
        }

        if (searchQuery.equalsIgnoreCase(":untagged")) {
            loadUntaggedItems(items);
            if (items.isEmpty()) return 0;
            return items.size();
        }

        String[] tags = searchQuery.split("\\s");

        start();
        try (PreparedStatement ps = con.prepareStatement(
                sql.get("search_items").replace("CSV", generateQuestionMarks(tags.length)),
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            for (int i = 0; i < tags.length; ++i) ps.setString(i + 1, tags[i]);
            ps.setLong(tags.length+1, tags.length);
            ResultSet rs = ps.executeQuery();
            fetchItems(rs, items);
        } catch (SQLException e) {
            finish();
            throw e;
        }
        finish();

        return items.size();
    }

    private String generateQuestionMarks(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) sb.append("?,");
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    @Override
    public Map<Long, Long> searchTags(Collection<Tag> tags, String searchQuery) throws Exception {
        searchQuery = searchQuery.trim();
        if (searchQuery.isEmpty()) {
            loadAllTags(tags);
            if (tags.isEmpty()) return null;
            return getTagItemCounts(extractIds(tags));
        }

        if (searchQuery.equalsIgnoreCase(":unused")) {
            loadUnusedTags(tags);
            if (tags.isEmpty()) return null;
            return getTagItemCounts(extractIds(tags));
        }

        start();
        try (PreparedStatement ps = con.prepareStatement(
                sql.get("search_tags"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            ps.setString(1, "%" + searchQuery + "%");
            ResultSet rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    tags.add(fetchTag(rs));
                    if (cancel.get()) break;
                }
            }
        } catch (SQLException e) {
            finish();
            throw e;
        }
        finish();

        if (tags.isEmpty()) return null;
        return getTagItemCounts(extractIds(tags));
    }

    @Override
    public long importItems(
            Reader reader,
            Globals.FileFormat fileFormat,
            ImportConfigs importConfigs,
            Collection<Item> items,
            OneParamFunction<Item> onAdd,
            TwoParamFunction<Item, Item> onConflict
    ) throws Exception {
        Item[] tmp = new Item[1];
        boolean conflict;
        Importer importer;

        switch (fileFormat) {
            case DLEELY_CVS:
                importer = CsvImporter.getDleelyInstance(reader);
                break;
            case DIIGO:
                importer = CsvImporter.getDiigoInstance(reader);
                break;
            case NETSCAPE:
                importer = HtmlImporter.getNetscapeInstance(reader);
                break;
            case DLEELY_JSON:
                importer = JsonImporter.getDleelyInstance(reader);
                break;
            case DLEELY_XML:
                importer = XmlImporter.getDleelyInstance(reader);
                break;
            default:
                return 0;
        }

        start();
        try {
            while (importer.hasNext()) {
                if (cancel.get()) break;
                conflict = false;
                tmp[0] = null;
                Item imported = importer.nextItem();
                if (itemExist(imported.getRef(), tmp)) {
                    imported = importConfigs.resolve(tmp[0], imported);
                    conflict = true;
                }
                importConfigs.process(imported);
                addUpdateItem(imported, conflict);
                if (items != null)
                    items.add(imported);
                if (conflict && onConflict != null)
                    onConflict.apply(tmp[0], imported);
                else if (onAdd != null && !conflict)
                    onAdd.apply(imported);
            }
        } catch (Exception e) {
            finish();
            importer.close();
            throw e;
        }
        finish();
        importer.close();
        return importer.getImportedCount();
    }

    @Override
    public long importItems(Path path, Globals.FileFormat fileFormat, ImportConfigs importConfigs, Collection<Item> items, OneParamFunction<Item> onAdd, TwoParamFunction<Item, Item> onConflict) throws Exception {
        return importItems(Files.newBufferedReader(path, Charset.forName("UTF-8")), fileFormat, importConfigs, items, onAdd, onConflict);
    }

    @Override
    public long exportItems(Writer writer, Globals.FileFormat fileFormat, ExportConfigs exportConfigs, Collection<Item> items) throws Exception {
        if (exportConfigs.isSkipAll()) return 0;

        Exporter exporter;
        switch (fileFormat) {
            case DLEELY_CVS:
                exporter = new TextExporter(writer, TextExporter.TextExportFormatter.getDleelyCsvFormatter(), exportConfigs);
                break;
            case DIIGO:
                exporter = new TextExporter(writer, TextExporter.TextExportFormatter.getDiigoFormatter(), exportConfigs);
                break;
            case NETSCAPE:
                exporter = new TextExporter(writer, TextExporter.TextExportFormatter.getNetscapeFormatter(), exportConfigs);
                break;
            case DLEELY_JSON:
                exporter = new TextExporter(writer, TextExporter.TextExportFormatter.getDleelyJsonFormatter(), exportConfigs);
                break;
            case DLEELY_XML:
                exporter = XMLExporter.getDleelyExporter(writer, exportConfigs);
                break;
            default:
                return 0;
        }

        start();
        try {
            for (Item item : items) {
                if (cancel.get()) break;
                exporter.put(item);
            }
        } catch (Exception e) {
            finish();
            exporter.close();
            throw e;
        }
        finish();
        exporter.close();
        return exporter.getExportedCount();
    }

    @Override
    public long exportItems(Path path, Globals.FileFormat fileFormat, ExportConfigs exportConfigs, Collection<Item> items) throws Exception {
        return exportItems(Files.newBufferedWriter(path, Charset.forName("UTF-8")), fileFormat, exportConfigs, items);
    }
}
