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

public class EditItemCommand implements ICommand {
    private final Item oldValue;
    private final Item newValue;

    public EditItemCommand(Item oldValue, Item newValue) {
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.newValue.setId(this.oldValue.getId());
    }

    @Override
    public boolean execute() {
        return UIHelper.io.editItem(oldValue, newValue);
    }

    @Override
    public boolean undo() {
        return UIHelper.io.editItem(newValue, oldValue);
    }

    @Override
    public String getDesc() {
        return "command.edit";
    }
}
