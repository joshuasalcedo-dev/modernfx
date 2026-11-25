package io.joshuasalcedo.fx.ui.core.list;

import io.joshuasalcedo.fx.ui.core.data.LoadState;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.jspecify.annotations.NullMarked;

/// A ListMfx that loads the next page by itself when the user scrolls near the end (within `threshold` rows), and
/// while the loaded rows don't fill the list yet. A "Loading…" row shows under the items while a page loads.
///
/// ```java
/// var messages = new InfiniteListMfx<Message>();
/// messages.setDataSource((PagedDataSource<Message>) request -> api.messages(request.cursor(), request.limit()));
/// ```
@NullMarked
public class InfiniteListMfx<T> extends ListMfx<T> {

    private final IntegerProperty threshold = new SimpleIntegerProperty(this, "threshold", 5);

    public InfiniteListMfx() {
        setPagingStyle(null);
        getStyleClass().add("mfx-infinite-list");
        loadStateProperty().addListener((obs, old, now) -> {
            if (now == LoadState.LOADED) {
                onScrolled();
            }
        });
    }

    /// How many rows from the end the next page starts loading.
    public final IntegerProperty thresholdProperty() { return threshold; }
    public final int getThreshold() { return threshold.get(); }
    public final void setThreshold(int value) { threshold.set(Math.max(0, value)); }

    @Override
    protected void onScrolled() {
        super.onScrolled();
        if (getLoadState() != LoadState.LOADED || !hasMoreProperty().get()) {
            return;
        }
        int last = lastVisibleIndex();
        if (last >= 0 && last >= getItems().size() - 1 - threshold.get()) {
            loadMore();
        }
    }
}
