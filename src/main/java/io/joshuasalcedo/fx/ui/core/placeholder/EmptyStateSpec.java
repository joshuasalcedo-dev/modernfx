package io.joshuasalcedo.fx.ui.core.placeholder;

import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.feather.Feather;

/// What an empty collection says: an icon, a title, a message, and an optional action (`New contact`).
///
/// ```java
/// EmptyStateSpec.of("No contacts yet", "Contacts you add show up here.")
///         .withAction("New contact", this::newContact);
/// ```
@NullMarked
public record EmptyStateSpec(@Nullable Ikon icon, String title, String message,
                             @Nullable String actionText, @Nullable Runnable action) {

    /// What collections say when nothing is set.
    public static final EmptyStateSpec DEFAULT = of("No results", "There is nothing here yet.");

    public EmptyStateSpec {
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(message, "message");
    }

    /// An empty state with the inbox icon and no action.
    public static EmptyStateSpec of(String title, String message) {
        return new EmptyStateSpec(Feather.INBOX, title, message, null, null);
    }

    /// The same, with a button that runs `action`.
    public EmptyStateSpec withAction(String text, Runnable run) {
        return new EmptyStateSpec(icon, title, message, text, run);
    }

    /// The same, with another icon (or none).
    public EmptyStateSpec withIcon(@Nullable Ikon value) {
        return new EmptyStateSpec(value, title, message, actionText, action);
    }
}
