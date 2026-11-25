package io.joshuasalcedo.fx.ui.core.list;

import io.joshuasalcedo.fx.ui.core.button.ButtonMfx;
import io.joshuasalcedo.fx.ui.core.data.DataSource;
import io.joshuasalcedo.fx.ui.core.data.Density;
import io.joshuasalcedo.fx.ui.core.data.KeyExtractor;
import io.joshuasalcedo.fx.ui.core.data.LoadState;
import io.joshuasalcedo.fx.ui.core.data.PageRequest;
import io.joshuasalcedo.fx.ui.core.data.SelectionMode;
import io.joshuasalcedo.fx.ui.core.data.SelectionState;
import io.joshuasalcedo.fx.ui.core.data.SortSpec;
import io.joshuasalcedo.fx.ui.core.internal.ButtonLoadingState;
import io.joshuasalcedo.fx.ui.core.internal.PagedLoader;
import io.joshuasalcedo.fx.ui.core.internal.StyleClasses;
import io.joshuasalcedo.fx.ui.core.internal.VirtualFlows;
import io.joshuasalcedo.fx.ui.core.pagination.PaginationStyle;
import io.joshuasalcedo.fx.ui.core.placeholder.CollectionPlaceholderMfx;
import io.joshuasalcedo.fx.ui.core.placeholder.EmptyStateSpec;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// A virtualized list (a ListView inside) with rows from a ListCellFactory, a selection that survives reloads
/// (SelectionState), a Density, sections with a sticky header, and the loading, empty and error states.
///
/// The items are either set directly ([#getItems]) or loaded from a DataSource a page at a time: then `filter`,
/// `sort` and `pageSize` go into the PageRequest, and changing them reloads. With [PaginationStyle#LOAD_MORE]
/// (the default) a "Load more" button shows under the items while there are more; InfiniteListMfx loads them as
/// you scroll.
///
/// ```java
/// var contacts = new ListMfx<Contact>();
/// contacts.setCellFactory((row, c) -> row.title(c.name()).subtitle(c.email()));
/// contacts.setSectionBy(c -> c.name().substring(0, 1));
/// contacts.setDataSource(new ListDataSource<>(all).sortBy("name", Contact::name));
/// ```
@NullMarked
public class ListMfx<T> extends VBox {

    public static final String STYLE_CLASS = "mfx-list-box";

    private final ListView<T> view = new ListView<>();
    private final PagedLoader<T> loader = new PagedLoader<>(view.getItems());
    private final CollectionPlaceholderMfx placeholder = new CollectionPlaceholderMfx(view);
    private final ListSectionMfx sticky = new ListSectionMfx();
    private final HBox footer = new HBox();
    private final ButtonMfx loadMoreButton = new ButtonMfx("Load more").variant(ButtonMfx.Variant.OUTLINE).size(ButtonMfx.Size.SM);
    private final Label loadingMore = new Label("Loading…", ButtonLoadingState.spinner());
    private final SetChangeListener<Object> selectionKeys = c -> selectFromState();
    private boolean syncing;

    private final ObjectProperty<ListCellFactory<T>> cellFactory =
            new SimpleObjectProperty<>(this, "cellFactory", ListCellFactory.title(String::valueOf));
    private final ObjectProperty<@Nullable Function<T, String>> sectionBy = new SimpleObjectProperty<>(this, "sectionBy");
    private final ObjectProperty<Density> density = new SimpleObjectProperty<>(this, "density", Density.COMFORTABLE);
    private final ObjectProperty<SelectionState<T>> selection = new SimpleObjectProperty<>(this, "selection",
            new SelectionState<>(KeyExtractor.identity(), SelectionMode.SINGLE));
    private final ObjectProperty<@Nullable DataSource<T>> dataSource = new SimpleObjectProperty<>(this, "dataSource");
    private final IntegerProperty pageSize = new SimpleIntegerProperty(this, "pageSize", PageRequest.DEFAULT_LIMIT);
    private final StringProperty filter = new SimpleStringProperty(this, "filter", "");
    private final ObjectProperty<List<SortSpec>> sort = new SimpleObjectProperty<>(this, "sort", List.of());
    private final ObjectProperty<@Nullable PaginationStyle> pagingStyle =
            new SimpleObjectProperty<>(this, "pagingStyle", PaginationStyle.LOAD_MORE);

    public ListMfx() {
        getStyleClass().add(STYLE_CLASS);
        view.getStyleClass().add("mfx-list");
        view.setPlaceholder(new Region());
        StyleClasses.bind(view, density, Density::styleClass);

        sticky.getStyleClass().add("mfx-list-section-sticky");
        sticky.setMouseTransparent(true);
        sticky.setVisible(false);
        StackPane.setAlignment(sticky, Pos.TOP_LEFT);
        StackPane.setMargin(sticky, new Insets(1, 12, 0, 1));
        StackPane body = new StackPane(placeholder, sticky);
        VBox.setVgrow(body, Priority.ALWAYS);

        footer.getStyleClass().add("mfx-list-footer");
        footer.setAlignment(Pos.CENTER);
        loadingMore.getStyleClass().add("mfx-list-loading-more");
        loadMoreButton.setOnAction(e -> loadMore());
        getChildren().addAll(body, footer);

        placeholder.stateProperty().bind(loader.stateProperty());
        placeholder.errorMessageProperty().bind(loader.errorProperty());
        placeholder.setOnRetry(this::reload);
        loader.setAfterLoad(this::selectFromState);
        loader.stateProperty().addListener(o -> {
            updateFooter();
            updateSticky();
        });
        loader.hasMoreProperty().addListener(o -> updateFooter());
        pagingStyle.addListener(o -> updateFooter());

        cellFactory.addListener(o -> updateCellFactory());
        sectionBy.addListener(o -> updateCellFactory());
        updateCellFactory();

        selection.addListener((obs, old, now) -> {
            old.keys().removeListener(selectionKeys);
            applySelection(now);
        });
        applySelection(selection.get());
        view.getSelectionModel().getSelectedIndices().addListener((ListChangeListener<Integer>) c -> pushToState());

        dataSource.addListener((obs, old, now) -> {
            loader.setRequest(request());
            loader.setSource(now);
        });
        pageSize.addListener(o -> loader.setRequest(request()));
        filter.addListener(o -> loader.setRequest(request()));
        sort.addListener(o -> loader.setRequest(request()));

        VirtualFlows.onScroll(view, this::onScrolled);
        view.getItems().addListener((ListChangeListener<T>) c -> Platform.runLater(this::onScrolled));
        updateFooter();
    }

    /// The ListView inside: for what ListMfx doesn't cover.
    public final ListView<T> listView() {
        return view;
    }

    /// The items shown. With a DataSource, the loader fills them: don't change them yourself.
    public final ObservableList<T> getItems() {
        return view.getItems();
    }

    /// Loads the first page again (a no-op without a DataSource).
    public void reload() {
        loader.reload();
    }

    /// Loads the next page under the items there (a no-op without a DataSource, while loading, or at the end).
    public void loadMore() {
        loader.loadMore();
    }

    /// Called when the list scrolls or its items change. Subclasses add to it.
    protected void onScrolled() {
        updateSticky();
    }

    /// The index of the first row on screen, or -1.
    protected int firstVisibleIndex() {
        return VirtualFlows.firstVisibleIndex(view);
    }

    /// The index of the last row on screen, or -1.
    protected int lastVisibleIndex() {
        return VirtualFlows.lastVisibleIndex(view);
    }

    /// What shows when there are no items.
    public final void setEmpty(EmptyStateSpec spec) {
        placeholder.setEmpty(spec);
    }

    /// The placeholder around the list: its loading, empty and error views.
    public final CollectionPlaceholderMfx placeholder() {
        return placeholder;
    }

    public final ReadOnlyObjectProperty<LoadState> loadStateProperty() { return loader.stateProperty(); }
    public final LoadState getLoadState() { return loader.getState(); }

    /// How many items there are in all (−1 when the source doesn't say).
    public final ReadOnlyLongProperty totalProperty() { return loader.totalProperty(); }

    /// True when the source has more pages.
    public final ReadOnlyBooleanProperty hasMoreProperty() { return loader.hasMoreProperty(); }

    public final ObjectProperty<ListCellFactory<T>> cellFactoryProperty() { return cellFactory; }
    public final void setCellFactory(ListCellFactory<T> value) { cellFactory.set(Objects.requireNonNull(value)); }

    /// The section of an item (a letter, a date): items of a section must be next to each other.
    public final ObjectProperty<@Nullable Function<T, String>> sectionByProperty() { return sectionBy; }
    public final void setSectionBy(@Nullable Function<T, String> value) { sectionBy.set(value); }

    public final ObjectProperty<Density> densityProperty() { return density; }
    public final Density getDensity() { return density.get(); }
    public final void setDensity(Density value) { density.set(Objects.requireNonNull(value)); }

    /// What is selected, by key. Its mode is the list's selection mode.
    public final ObjectProperty<SelectionState<T>> selectionProperty() { return selection; }
    public final SelectionState<T> getSelection() { return selection.get(); }
    public final void setSelection(SelectionState<T> value) { selection.set(Objects.requireNonNull(value)); }

    public final ObjectProperty<@Nullable DataSource<T>> dataSourceProperty() { return dataSource; }
    public final void setDataSource(@Nullable DataSource<T> value) { dataSource.set(value); }

    public final IntegerProperty pageSizeProperty() { return pageSize; }
    public final void setPageSize(int value) { pageSize.set(value); }

    /// The search text sent to the DataSource.
    public final StringProperty filterProperty() { return filter; }
    public final void setFilter(String value) { filter.set(value); }

    public final ObjectProperty<List<SortSpec>> sortProperty() { return sort; }
    public final void setSort(List<SortSpec> value) { sort.set(List.copyOf(value)); }

    /// LOAD_MORE shows the "Load more" button; null shows none.
    public final ObjectProperty<@Nullable PaginationStyle> pagingStyleProperty() { return pagingStyle; }
    public final void setPagingStyle(@Nullable PaginationStyle value) { pagingStyle.set(value); }

    private PageRequest request() {
        return new PageRequest(0, Math.max(1, pageSize.get()), sort.get(), filter.get().strip(), null);
    }

    private void updateCellFactory() {
        ListCellFactory<T> factory = cellFactory.get();
        Function<T, String> sections = sectionBy.get();
        view.setCellFactory(v -> new ListCellMfx<>(factory, sections));
        updateSticky();
    }

    private void updateFooter() {
        LoadState state = loader.getState();
        if (state == LoadState.LOADING_MORE) {
            footer.getChildren().setAll(loadingMore);
        } else if (pagingStyle.get() == PaginationStyle.LOAD_MORE && loader.hasMoreProperty().get()
                && state == LoadState.LOADED) {
            footer.getChildren().setAll(loadMoreButton);
        } else {
            footer.getChildren().clear();
        }
        footer.setVisible(!footer.getChildren().isEmpty());
        footer.setManaged(footer.isVisible());
    }

    private void updateSticky() {
        Function<T, String> by = sectionBy.get();
        int first = firstVisibleIndex();
        LoadState state = loader.getState();
        boolean showing = state == LoadState.LOADED || state == LoadState.LOADING_MORE;
        if (by == null || !showing || first < 0 || first >= view.getItems().size()) {
            sticky.setVisible(false);
            return;
        }
        sticky.setText(by.apply(view.getItems().get(first)));
        sticky.setVisible(true);
    }

    private void applySelection(SelectionState<T> state) {
        MultipleSelectionModel<T> model = view.getSelectionModel();
        model.setSelectionMode(state.mode() == SelectionMode.MULTIPLE
                ? javafx.scene.control.SelectionMode.MULTIPLE : javafx.scene.control.SelectionMode.SINGLE);
        state.keys().addListener(selectionKeys);
        selectFromState();
    }

    /// The ListView's selection into the SelectionState, for the items loaded (the others keep theirs).
    private void pushToState() {
        if (syncing) {
            return;
        }
        SelectionState<T> state = selection.get();
        MultipleSelectionModel<T> model = view.getSelectionModel();
        if (state.mode() == SelectionMode.NONE) {
            if (!model.isEmpty()) {
                Platform.runLater(model::clearSelection);
            }
            return;
        }
        syncing = true;
        try {
            List<T> items = view.getItems();
            Set<Integer> selected = new HashSet<>(model.getSelectedIndices());
            if (state.mode() == SelectionMode.SINGLE) {
                Integer at = model.getSelectedIndex();
                if (at >= 0 && at < items.size()) {
                    state.select(items.get(at));
                } else {
                    state.deselectAll(items);
                }
            } else {
                for (int i = 0; i < items.size(); i++) {
                    state.set(items.get(i), selected.contains(i));
                }
            }
        } finally {
            syncing = false;
        }
    }

    /// The SelectionState into the ListView, after a load or a change from outside.
    private void selectFromState() {
        if (syncing) {
            return;
        }
        syncing = true;
        try {
            SelectionState<T> state = selection.get();
            MultipleSelectionModel<T> model = view.getSelectionModel();
            model.clearSelection();
            List<T> items = view.getItems();
            for (int i = 0; i < items.size(); i++) {
                if (state.isSelected(items.get(i))) {
                    model.select(i);
                }
            }
        } finally {
            syncing = false;
        }
    }
}
