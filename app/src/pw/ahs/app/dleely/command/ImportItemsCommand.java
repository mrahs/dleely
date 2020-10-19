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

import java.util.Collection;
import java.util.HashSet;

public class ImportItemsCommand implements ICommand {
    private final Collection<Item> added;
    private final Collection<Item[]> edited;

    public ImportItemsCommand(Collection<Item> added, Collection<Item[]> edited) {
        this.added = added;
        this.edited = edited;
    }

    @Override
    public boolean execute() {
        UIHelper.io.addItems(added);
        UIHelper.io.editItems(edited);
        return true;
    }

    @Override
    public boolean undo() {
        boolean res = added.isEmpty() || UIHelper.io.deleteItems(added);
        if (res) {
            Collection<Item[]> editedReversed = new HashSet<>(edited.size());
            for (Item[] pair : edited) {
                editedReversed.add(new Item[]{pair[1], pair[0]});
            }
            UIHelper.io.editItems(editedReversed);
        }
        return res;
    }

    @Override
    public String getDesc() {
        return "command.import";
    }
}
