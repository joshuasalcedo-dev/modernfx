package io.joshuasalcedo.fx.ui.core.dialog;

import io.joshuasalcedo.fx.ui.core.button.ButtonMfx;
import org.jspecify.annotations.NullMarked;

/// shadcn's AlertDialog: asks the user to confirm, and waits. No X and no closing by clicking outside: only
/// Cancel or the action close it.
///
/// ```java
/// new AlertDialogMfx("Are you absolutely sure?", "This can't be undone.")
///         .action("Delete", ButtonMfx.Variant.DESTRUCTIVE, this::delete)
///         .show(deleteButton);
/// ```
@NullMarked
public class AlertDialogMfx extends DialogMfx {

    private final ButtonMfx cancel = new ButtonMfx("Cancel").variant(ButtonMfx.Variant.OUTLINE);
    private final ButtonMfx action = new ButtonMfx("Continue");

    public AlertDialogMfx(String title, String description) {
        super(title, description);
        getStyleClass().add("mfx-alert-dialog");
        closeButton(false);
        persistent(true);
        cancel.setOnAction(e -> close());
        action.setOnAction(e -> close());
        footer(cancel, action);
    }

    /// The main button: its text, its look, and what it does (then the dialog closes).
    public AlertDialogMfx action(String text, ButtonMfx.Variant variant, Runnable onAction) {
        action.setText(text);
        action.variant(variant);
        action.setOnAction(e -> {
            onAction.run();
            close();
        });
        return this;
    }

    /// The text of the Cancel button.
    public AlertDialogMfx cancelText(String text) {
        cancel.setText(text);
        return this;
    }

    public ButtonMfx getCancel() {
        return cancel;
    }

    public ButtonMfx getAction() {
        return action;
    }
}
