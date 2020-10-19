/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.command;

import pw.ahs.app.dleely.gui.UIHelper;
import pw.ahs.app.dleely.model.Item;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public class AddItemsCommand implements ICommand {
    private final Collection<Item> items = new HashSet<>();

    public AddItemsCommand(Item... items) {
        this(Arrays.asList(items));
    }

    public AddItemsCommand(Collection<Item> items) {
        this.items.addAll(items);
    }

    @Override
    public boolean execute() {
        if (items.size() == 1) {
            return UIHelper.io.addItem(items.iterator().next());
        }
        return UIHelper.io.addItems(items);
//        int addCount = 0;
//        for (Item item : items) {
//            if (UIHelper.io.addItem(item)) addCount++;
//        }
//        return addCount > 0;
    }

    @Override
    public boolean undo() {
        return UIHelper.io.deleteItems(items);
    }

    @Override
    public String getDesc() {
        return "command.add";
    }
}
