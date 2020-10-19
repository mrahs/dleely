/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.dleely.controller;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import pw.ahs.app.dleely.command.ICommand;

public class UndoRedoController {
    private final ObservableList<ICommand> undoStack;
    private final ObservableList<ICommand> redoStack;
    private ICommand cmdSaveOffset;

    private static UndoRedoController instance = null;

    public static UndoRedoController getInstance() {
        if (instance == null)
            instance = new UndoRedoController();
        return instance;
    }

    private UndoRedoController() {
        undoStack = FXCollections.observableArrayList();
        redoStack = FXCollections.observableArrayList();
        cmdSaveOffset = null;
    }

    public void clearStacks() {
        undoStack.clear();
        redoStack.clear();
    }

    public void pushCommand(ICommand cmd) {
        undoStack.add(cmd);
    }

    public void executeThenPushCommand(ICommand cmd) {
        boolean wasSaved = Controller.unsaved.isSaved();
        if (cmd.execute()) {
            undoStack.add(cmd);
            if (wasSaved)
                cmdSaveOffset = cmd;
        }

    }

    public void addUndoListener(ListChangeListener<ICommand> listener) {
        undoStack.addListener(listener);
    }

    public void addRedoListener(ListChangeListener<ICommand> listener) {
        redoStack.addListener(listener);
    }

    public ICommand peekUndo() {
        if (noUndo())
            return null;
        return undoStack.get(undoStack.size() - 1);
    }

    public ICommand peekRedo() {
        if (noRedo())
            return null;
        return redoStack.get(redoStack.size() - 1);
    }

    public ICommand popUndo() {
        if (noUndo())
            return null;
        return undoStack.remove(undoStack.size() - 1);
    }

    public ICommand popRedo() {
        if (noRedo())
            return null;
        return redoStack.remove(redoStack.size() - 1);
    }

    public boolean noUndo() {
        return undoStack.isEmpty();
    }

    public boolean noRedo() {
        return redoStack.isEmpty();
    }

    public void undo() {
        if (noUndo()) return;
        ICommand lastCommand = peekUndo();
        if (lastCommand.undo()) {
            redoStack.add(popUndo());
            if (lastCommand == cmdSaveOffset) {
                Controller.unsaved.setSaved(true);
                cmdSaveOffset = null;
            }
        }
    }

    public void redo() {
        if (noRedo()) return;
        ICommand lastCommand = peekRedo();
        boolean wasSaved = Controller.unsaved.isSaved();
        if (lastCommand.execute()) {
            undoStack.add(popRedo());
            if (wasSaved)
                cmdSaveOffset = lastCommand;
        }
    }
}
