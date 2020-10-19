/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.store;

import org.junit.Before;
import org.junit.Test;
import pw.ahs.app.dleely.Globals;
import pw.ahs.app.dleely.model.Item;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class H2DBStoreTest1 {
    private Path baseDir;
    private H2DBStore h2DBStore;

    @Before
    public void setUp() throws Exception {
        baseDir = Paths.get(System.getProperty("java.io.tmpdir")).normalize();
        h2DBStore = new H2DBStore(baseDir.resolve("test." + Globals.FILE_EXT));
    }

    @Test
    public void testH2DBStore() throws Exception {
        // create new
        h2DBStore.open(true, false);

        // close
        h2DBStore.close();

        // open existing
        h2DBStore.open(false, true);

        // check inappropriate
        assertTrue(h2DBStore.checkInappropriateClose());
        h2DBStore.open(false, true);

        h2DBStore.addUpdateItems(Arrays.asList(
                new Item("name a", "ref a"),
                new Item("name b", "ref b"),
                new Item("name c", "ref c")
        ), false);

        // backup
        h2DBStore.backup(baseDir.resolve("test." + Globals.FILE_EXT_BACKUP));

        // restore
        h2DBStore.restore(baseDir.resolve("test." + Globals.FILE_EXT_BACKUP));

        assertEquals(3, h2DBStore.getItemCount());

        h2DBStore.close();

        // search tags
        // is working
        // cancel
        // import
        // export
        // search items
    }
}
