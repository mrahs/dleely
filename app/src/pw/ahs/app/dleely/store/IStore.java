/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.store;

import pw.ahs.app.dleely.Globals;
import pw.ahs.app.dleely.exporter.ExportConfigs;
import pw.ahs.app.dleely.gui.OneParamFunction;
import pw.ahs.app.dleely.gui.TwoParamFunction;
import pw.ahs.app.dleely.importer.ImportConfigs;
import pw.ahs.app.dleely.model.Item;
import pw.ahs.app.dleely.model.Tag;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;

public interface IStore extends AutoCloseable {

    // storage

    /**
     * Opens the storage for read/write operations.
     * Implementations may require the user to initialize some variables (e.g. encryption password).
     *
     * @param create                  create the file if it doesn't exist
     * @param attemptRecoveryIfNeeded if true, check for inappropriate close and attempt recovery if needed.
     *                                might cause an {@link java.lang.IllegalStateException} if {@code create} is set to true
     */
    public void open(boolean create, boolean attemptRecoveryIfNeeded) throws Exception;

    public boolean checkInappropriateClose();

    /**
     * @return true if the storage if currently open.
     */
    public boolean isOpened();

    public void close() throws Exception;

    // working

    /**
     * @return true if the there is an ongoing operation with the storage, false otherwise
     */
    public boolean isWorking();

    /**
     * Stops whatever operation is ongoing with the storage. This may not have an immediate effect.
     */
    public void cancel();


    // data

    public void backup(Path path) throws Exception;

    public void restore(Path path) throws Exception;

    public long importItems(
            Reader reader,
            Globals.FileFormat fileFormat,
            ImportConfigs importConfigs,
            Collection<Item> items,
            OneParamFunction<Item> onAdd,
            TwoParamFunction<Item, Item> onConflict
    ) throws Exception;

    public long importItems(
            Path path,
            Globals.FileFormat fileFormat,
            ImportConfigs importConfigs,
            Collection<Item> items,
            OneParamFunction<Item> onAdd,
            TwoParamFunction<Item, Item> onConflict
    ) throws Exception;

    public long exportItems(
            Writer writer,
            Globals.FileFormat fileFormat,
            ExportConfigs exportConfigs,
            Collection<Item> items
    ) throws Exception;

    public long exportItems(
            Path path,
            Globals.FileFormat fileFormat,
            ExportConfigs exportConfigs,
            Collection<Item> items
    ) throws Exception;

    /**
     * @param ref  the reference to validate upon
     * @param item an Item instance to fill it with data if found, may be null
     * @return true if an item with the same reference already exists, false otherwise
     */
    public boolean itemExist(String ref, Item[] item) throws Exception;

    /**
     * @param name the tag name to validate upon
     * @param tag  a Tag instance to fill it with data if found, may be null
     * @return true if a tag with the same name already exists, false otherwise.
     */
    public boolean tagExist(String name, Tag[] tag) throws Exception;

    public Item getItem(long id) throws Exception;

    public void loadAllItems(Collection<Item> items) throws Exception;

    public Collection<Item> getAllItems() throws Exception;

    public void loadAllItems(Collection<Item> items, long limit, long offset) throws Exception;

    public Collection<Item> getAllItems(long limit, long offset) throws Exception;

    public void loadUntaggedItems(Collection<Item> items) throws Exception;

    public Collection<Item> getUntaggedItems() throws Exception;

    public void loadItemsById(Collection<Item> items, long... ids) throws Exception;

    public Collection<Item> getItemsById(long... ids) throws Exception;

    public long getItemTagCount(long id) throws Exception;

    /**
     * Note: the item's id is updated to match the assigned one in the data store
     *
     * @return true if insert/update was successful; false if the item's ref. already exists
     * @throws Exception
     */
    public boolean addUpdateItem(Item item, boolean update) throws Exception;

    public void addUpdateItems(Collection<Item> items, boolean update) throws Exception;

    /**
     * Note: the tag's id is updated to match the assigned one in the data store
     *
     * @return true if insert/update was successful; false if the tag's name already exists
     * @throws Exception
     */
    public boolean addUpdateTag(Tag tag, boolean update) throws Exception;

    public void tagItems(long[] ids, Tag[] tags) throws Exception;

    public boolean removeItem(long id) throws Exception;

    public boolean removeItems(long... ids) throws Exception;

    public Tag getTag(long id) throws Exception;

    public void loadAllTags(Collection<Tag> tags) throws Exception;

    public Collection<Tag> getAllTags() throws Exception;

    public void loadAllTags(Collection<Tag> tags, long limit, long offset) throws Exception;

    public Collection<Tag> getAllTags(long limit, long offset) throws Exception;

    public void loadUnusedTags(Collection<Tag> tags) throws Exception;

    public Collection<Tag> getUnusedTags() throws Exception;

    public void loadTagsById(Collection<Tag> tags, long... ids) throws Exception;

    public Collection<Tag> getTagsById(long... ids) throws Exception;

    public long getTagItemCount(long id) throws Exception;

    public Map<Long, Long> getTagItemCounts(long... ids) throws Exception;

    public void loadItemTags(long id, Collection<Tag> tags) throws Exception;

    public Collection<Tag> getItemTags(long id) throws Exception;

    public boolean removeTag(long id) throws Exception;

    public boolean removeTagWithItems(long id) throws Exception;

    public long removeUnusedTags() throws Exception;

    public boolean replaceTag(long removeId, long keepId) throws Exception;

    public boolean replaceTag(long removeId, Tag tag) throws Exception;

    // stats
    public long getItemCount() throws Exception;

    public long getTagCount() throws Exception;

    public long getUnusedTagCount() throws Exception;

    public long getUntaggedItemCount() throws Exception;

    public Tag getMostUsedTag() throws Exception;

    public Item getMostTaggedItem() throws Exception;

    public Collection<Tag> getTop5Tags() throws Exception;

    public Map<LocalDate, Long> getDateTimeCountMap() throws Exception;

    // search

    public long searchItems(Collection<Item> items, String searchQuery) throws Exception;

    public Map<Long, Long> searchTags(Collection<Tag> tags, String searchQuery) throws Exception;

}
