/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.store;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pw.ahs.app.dleely.model.Item;
import pw.ahs.app.dleely.model.Tag;

import java.util.*;

import static junit.framework.Assert.*;

public class H2DBStoreTest2 {
    private H2DBStore h2DBStore;
    private Random random;
    private Set<Integer> generated;

    @Before
    public void setUp() throws Exception {
        h2DBStore = new H2DBStore(null);
        h2DBStore.open(true, false);
        random = new Random(0);
        generated = new HashSet<>();
    }

    @After
    public void tearDown() throws Exception {
        h2DBStore.close();
    }

    private Item makeItem() {
        int i = getRandom();
        Item tmp = new Item("item " + i, "ref " + i);
        tmp.setInfo("info " + i);
        tmp.setPrivy(random.nextBoolean());
        tmp.getTags().addAll(Arrays.asList(makeTag(), makeTag()));
        return tmp;
    }

    private Tag makeTag() {
        return Tag.getInstance("tag" + getRandom());
    }

    private int getRandom() {
        int i;
        while (true) {
            if (generated.contains(i = random.nextInt(32))) continue;
            break;
        }
        generated.add(i);
        return i;
    }

    @Test
    public void testItemExists() throws Exception {
        Item i1 = makeItem();
        Item tmp[] = new Item[1];
        assertTrue(h2DBStore.addUpdateItem(i1, false));
        assertTrue(h2DBStore.itemExist(i1.getRef(), tmp));
        assertTrue(tmp[0].getId() >= 0);
        assertEquals(i1.getName(), tmp[0].getName());
        assertEquals(i1.getRef(), tmp[0].getRef());
        assertEquals(i1.getInfo(), tmp[0].getInfo());
        assertEquals(i1.getPrivy(), tmp[0].getPrivy());
        assertEquals(i1.getTags(), tmp[0].getTags());
        assertEquals(i1.getDateAdd(), tmp[0].getDateAdd());
        assertEquals(i1.getDateMod(), tmp[0].getDateMod());
    }


    @Test
    public void testTagExists() throws Exception {
        Tag t1 = makeTag();
        Tag[] tmp = new Tag[]{Tag.getInstance("Null")};
        assertTrue(h2DBStore.addUpdateTag(t1, false));

        assertTrue(h2DBStore.tagExist(t1.getName(), tmp));
        assertTrue(tmp[0].getId() >= 0);
        assertEquals(t1.getName(), tmp[0].getName());
    }

    @Test
    public void testGetItem() throws Exception {
        Item i1 = makeItem();
        assertTrue(h2DBStore.addUpdateItem(i1, false));

        Item tmp = h2DBStore.getItem(i1.getId());
        assertNotNull(tmp);
        assertEquals(i1.getName(), tmp.getName());
        assertEquals(i1.getRef(), tmp.getRef());
        assertEquals(i1.getInfo(), tmp.getInfo());
        assertEquals(i1.getPrivy(), tmp.getPrivy());
        assertEquals(i1.getDateAdd(), tmp.getDateAdd());
        assertEquals(i1.getDateMod(), tmp.getDateMod());
        assertEquals(i1.getTags(), tmp.getTags());

        // item tag count
        assertEquals(i1.getTags().size(), h2DBStore.getItemTagCount(i1.getId()));
    }

    @Test
    public void testGetTag() throws Exception {
        Tag t1 = makeTag();
        assertTrue(h2DBStore.addUpdateTag(t1, false));

        Tag tmp = h2DBStore.getTag(t1.getId());
        assertNotNull(tmp);
        assertEquals(t1.getName(), tmp.getName());
        assertEquals(t1.getDateAdd(), tmp.getDateAdd());
        assertEquals(t1.getDateMod(), tmp.getDateMod());
    }

    @Test
    public void testUpdateItem() throws Exception {
        Item i1 = makeItem();
        assertTrue(h2DBStore.addUpdateItem(i1, false));
        i1.setName("new name");
        i1.setInfo("new info");

        assertTrue(h2DBStore.addUpdateItem(i1, true));
        i1 = h2DBStore.getItem(i1.getId());
        assertNotNull(i1);
        assertEquals("new name", i1.getName());
        assertEquals("new info", i1.getInfo());


        i1 = makeItem();
        Item i2 = makeItem();
        h2DBStore.addUpdateItems(Arrays.asList(i1, i2), false);
        i1.setName("updated 1");
        i2.setName("updated 2");
        h2DBStore.addUpdateItems(Arrays.asList(i1, i2), true);
        Item[] tmp = new Item[1];
        h2DBStore.itemExist(i1.getRef(), tmp);
        assertEquals(i1.getName(), tmp[0].getName());
        h2DBStore.itemExist(i2.getRef(), tmp);
        assertEquals(i2.getName(), tmp[0].getName());
    }

    @Test
    public void testUpdateTag() throws Exception {
        Tag t1 = makeTag();
        assertTrue(h2DBStore.addUpdateTag(t1, false));
        Tag t2 = Tag.getInstance("updated");
        t2.setId(t1.getId());
        assertTrue(h2DBStore.addUpdateTag(t2, true));
        assertFalse(h2DBStore.tagExist(t1.getName(), null));
    }

    @Test
    public void testRemoveTag() throws Exception {
        Tag t1 = makeTag();
        assertTrue(h2DBStore.addUpdateTag(t1, false));

        assertTrue(h2DBStore.removeTag(t1.getId()));
        assertFalse(h2DBStore.tagExist(t1.getName(), null));

        Item i2 = makeItem();
        Tag t2 = i2.getTags().iterator().next();
        assertTrue(h2DBStore.addUpdateItem(i2, false));
        assertTrue(h2DBStore.removeTagWithItems(t2.getId()));
        assertFalse(h2DBStore.tagExist(t2.getName(), null));
        assertFalse(h2DBStore.itemExist(i2.getRef(), null));
    }

    @Test
    public void testRemoveItem() throws Exception {
        Item i1 = makeItem();
        assertTrue(h2DBStore.addUpdateItem(i1, false));

        assertTrue(h2DBStore.removeItem(i1.getId()));
        assertFalse(h2DBStore.itemExist(i1.getName(), null));

        i1 = makeItem();
        Item i2 = makeItem();
        h2DBStore.addUpdateItems(Arrays.asList(i1, i2), false);

        assertTrue(h2DBStore.removeItems(i1.getId(), i2.getId()));
        assertFalse(h2DBStore.itemExist(i1.getRef(), null));
        assertFalse(h2DBStore.itemExist(i2.getRef(), null));
    }

    @Test
    public void testTagItemsLoadItemTags() throws Exception {
        Tag[] t1 = new Tag[]{makeTag()};
        Tag[] t2 = new Tag[]{makeTag()};
        Tag[] t3 = new Tag[]{makeTag()};
        Item i1 = makeItem();
        Item i2 = makeItem();
        h2DBStore.addUpdateItems(Arrays.asList(i1, i2), false);

        h2DBStore.tagItems(new long[]{i1.getId(), i2.getId()}, new Tag[]{t1[0], t2[0], t3[0]});

        assertTrue(h2DBStore.tagExist(t1[0].getName(), t1));
        assertTrue(h2DBStore.tagExist(t2[0].getName(), t2));
        assertTrue(h2DBStore.tagExist(t3[0].getName(), t3));

        Collection<Tag> tags = new ArrayList<>(3);
        h2DBStore.loadItemTags(i1.getId(), tags);
        assertTrue(tags.contains(t1[0]));
        assertTrue(tags.contains(t2[0]));
        assertTrue(tags.contains(t3[0]));

        tags.clear();
        h2DBStore.loadItemTags(i2.getId(), tags);
        assertTrue(tags.contains(t1[0]));
        assertTrue(tags.contains(t2[0]));
        assertTrue(tags.contains(t3[0]));
    }

    @Test
    public void testLoadById() throws Exception {
        Item i1 = makeItem();
        Item i2 = makeItem();

        assertTrue(h2DBStore.addUpdateItem(i1, false));
        assertTrue(h2DBStore.addUpdateItem(i2, false));

        Collection<Item> items = new ArrayList<>(2);
        h2DBStore.loadItemsById(items, i1.getId(), i2.getId());
        assertEquals(2, items.size());
        assertEquals(Arrays.asList(i1, i2), items);

        Tag t1 = makeTag();
        Tag t2 = makeTag();

        assertTrue(h2DBStore.addUpdateTag(t1, false));
        assertTrue(h2DBStore.addUpdateTag(t2, false));

        Collection<Tag> tags = new ArrayList<>(2);
        h2DBStore.loadTagsById(tags, t1.getId(), t2.getId());
        assertEquals(2, tags.size());
        assertEquals(Arrays.asList(t1, t2), tags);
    }

    @Test
    public void testReplaceTag() throws Exception {
        Item[] i1 = new Item[]{makeItem()};
        Iterator<Tag> itr = i1[0].getTags().iterator();
        Tag[] t1 = new Tag[]{itr.next()};
        Tag[] t2 = new Tag[]{itr.next()};

        assertTrue(h2DBStore.addUpdateItem(i1[0], false));

        assertTrue(h2DBStore.tagExist(t1[0].getName(), t1));
        assertTrue(h2DBStore.tagExist(t2[0].getName(), t2));

        Tag t3 = makeTag();
        assertTrue(h2DBStore.addUpdateTag(t3, false));

        assertTrue(h2DBStore.replaceTag(t2[0].getId(), t3.getId()));
        assertFalse(h2DBStore.tagExist(t2[0].getName(), null));

        assertTrue(h2DBStore.itemExist(i1[0].getRef(), i1));
        assertFalse(i1[0].getTags().contains(t2[0]));
        assertTrue(i1[0].getTags().contains(t3));
    }
}
