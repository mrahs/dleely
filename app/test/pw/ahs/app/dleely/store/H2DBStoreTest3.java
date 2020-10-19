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

public class H2DBStoreTest3 {
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
    public void testH2DBStore() throws Exception {
        Item i1 = makeItem();
        Item i2 = makeItem();
        Item i3 = makeItem();

        h2DBStore.addUpdateItems(Arrays.asList(i1, i2, i3), false);
        // get item count
        assertEquals(3, h2DBStore.getItemCount());
        // get tag count
        assertEquals(6, h2DBStore.getTagCount());

        Tag t1 = makeTag();
        Tag t2 = makeTag();
        assertTrue(h2DBStore.addUpdateTag(t1, false));
        assertTrue(h2DBStore.addUpdateTag(t2, false));

        // get unused tag count
        assertEquals(2, h2DBStore.getUnusedTagCount());

        Item i4 = makeItem();
        Item i5 = makeItem();
        i4.getTags().clear();
        i5.getTags().clear();

        h2DBStore.addUpdateItems(Arrays.asList(i4, i5), false);

        // get untagged item count
        assertEquals(2, h2DBStore.getUntaggedItemCount());

        Tag t3 = makeTag();
        h2DBStore.tagItems(new long[]{i1.getId(), i2.getId(), i3.getId(), i4.getId(), i5.getId()}, new Tag[]{t3});

        // get most used tag
        Tag t4 = h2DBStore.getMostUsedTag();
        assertEquals(t3, t4);

        // get tag item count
        assertEquals(5, h2DBStore.getTagItemCount(t3.getId()));

        // get top 5 tags
        Collection<Tag> tags = h2DBStore.getTop5Tags();
        assertEquals(5, tags.size());
        assertTrue(tags.contains(t3));

        // get most tagged item
        Tag t5 = makeTag();
        i1.getTags().add(t5);
        assertTrue(h2DBStore.addUpdateItem(i1, true));

        Item i6 = h2DBStore.getMostTaggedItem();
        assertEquals(i1, i6);
        assertEquals(i1.getName(), i6.getName());

        // load all items
        Collection<Item> items = new ArrayList<>(6);
        h2DBStore.loadAllItems(items);
        assertEquals(5, items.size());

        // load all items 2
        items.clear();
        h2DBStore.loadAllItems(items, 2, 3);
        assertEquals(2, items.size());

        // load all tags
        tags.clear();
        h2DBStore.loadAllTags(tags);
        assertEquals(10, tags.size());

        // load all tags 2
        tags.clear();
        h2DBStore.loadAllTags(tags, 2, 3);
        assertEquals(2, tags.size());

        // load unused tags
        tags.clear();
        h2DBStore.loadUnusedTags(tags);
        assertEquals(2, tags.size());
        assertTrue(Arrays.asList(t1, t2).containsAll(tags));

        // load untagged items
        Item i7 = makeItem();
        Item i8 = makeItem();
        i7.getTags().clear();
        i8.getTags().clear();
        h2DBStore.addUpdateItems(Arrays.asList(i7, i8), false);

        items.clear();
        h2DBStore.loadUntaggedItems(items);
        assertEquals(2, items.size());
        assertEquals(Arrays.asList(i7, i8), items);

        // remove unused tags
        long count = h2DBStore.removeUnusedTags();
        assertEquals(2, count);
        assertFalse(h2DBStore.tagExist(t1.getName(), null));
        assertFalse(h2DBStore.tagExist(t2.getName(), null));
    }
}
