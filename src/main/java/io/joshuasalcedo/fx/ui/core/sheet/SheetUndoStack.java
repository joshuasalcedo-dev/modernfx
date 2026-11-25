package io.joshuasalcedo.fx.ui.core.sheet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// Undo and redo for a sheet's edits: each edit, paste or clear is one step (a paste of many cells undoes at once).
/// SheetMfx has one, on Ctrl+Z and Ctrl+Y (Ctrl+Shift+Z).
///
/// ```java
/// undoButton.disableProperty().bind(sheet.undoStack().canUndoProperty().not());
/// undoButton.setOnAction(e -> sheet.undoStack().undo());
/// ```
@NullMarked
public class SheetUndoStack {

    /// How many steps it keeps.
    public static final int LIMIT = 200;

    private final SheetModel model;
    private final Deque<List<SheetModel.Change>> undo = new ArrayDeque<>();
    private final Deque<List<SheetModel.Change>> redo = new ArrayDeque<>();
    private final ReadOnlyBooleanWrapper canUndo = new ReadOnlyBooleanWrapper(this, "canUndo");
    private final ReadOnlyBooleanWrapper canRedo = new ReadOnlyBooleanWrapper(this, "canRedo");
    private @Nullable List<SheetModel.Change> group;
    private boolean applying;

    /// A stack that records every change of `model`.
    public SheetUndoStack(SheetModel model) {
        this(model, true);
    }

    /// With `listen` false the owner passes the changes to [#record] (SheetMfx does, after validating them).
    SheetUndoStack(SheetModel model, boolean listen) {
        this.model = Objects.requireNonNull(model, "model");
        if (listen) {
            model.addListener(this::record);
        }
    }

    public ReadOnlyBooleanProperty canUndoProperty() { return canUndo.getReadOnlyProperty(); }
    public boolean canUndo() { return canUndo.get(); }
    public ReadOnlyBooleanProperty canRedoProperty() { return canRedo.getReadOnlyProperty(); }
    public boolean canRedo() { return canRedo.get(); }

    /// Runs `edits` as one step: undo takes all of them back at once.
    public void compound(Runnable edits) {
        if (group != null) {
            edits.run();
            return;
        }
        List<SheetModel.Change> changes = new ArrayList<>();
        group = changes;
        try {
            edits.run();
        } finally {
            group = null;
            if (!changes.isEmpty()) {
                push(changes);
            }
        }
    }

    /// Takes back the last step.
    public void undo() {
        List<SheetModel.Change> step = undo.poll();
        if (step == null) {
            return;
        }
        apply(() -> {
            for (int i = step.size() - 1; i >= 0; i--) {
                model.set(step.get(i).address(), step.get(i).before());
            }
        });
        redo.push(step);
        update();
    }

    /// Does again the last step taken back.
    public void redo() {
        List<SheetModel.Change> step = redo.poll();
        if (step == null) {
            return;
        }
        apply(() -> {
            for (SheetModel.Change c : step) {
                model.set(c.address(), c.after());
            }
        });
        undo.push(step);
        update();
    }

    /// Forgets every step.
    public void clear() {
        undo.clear();
        redo.clear();
        update();
    }

    /// Records a change (unless it's undo or redo setting cells).
    void record(SheetModel.Change change) {
        if (applying) {
            return;
        }
        if (group != null) {
            group.add(change);
        } else {
            push(List.of(change));
        }
    }

    private void push(List<SheetModel.Change> step) {
        undo.push(List.copyOf(step));
        while (undo.size() > LIMIT) {
            undo.removeLast();
        }
        redo.clear();
        update();
    }

    private void apply(Runnable run) {
        applying = true;
        try {
            run.run();
        } finally {
            applying = false;
        }
    }

    private void update() {
        canUndo.set(!undo.isEmpty());
        canRedo.set(!redo.isEmpty());
    }
}
