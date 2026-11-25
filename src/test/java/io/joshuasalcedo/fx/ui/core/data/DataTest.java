package io.joshuasalcedo.fx.ui.core.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class DataTest {

    record Contact(int id, String name) {
    }

    private final List<Contact> contacts = List.of(
            new Contact(1, "Cara"), new Contact(2, "ana"), new Contact(3, "Ben"), new Contact(4, "Bea"));

    @Test
    void listDataSourceSortsFiltersAndPages() {
        var source = new ListDataSource<>(contacts).sortBy("name", c -> c.name().toLowerCase());
        PageRequest request = PageRequest.first(2).withSort(List.of(SortSpec.ascending("name")));

        Page<Contact> first = source.load(request).join();
        assertThat(first.items()).extracting(Contact::name).containsExactly("ana", "Bea");
        assertThat(first.total()).isEqualTo(4);
        assertThat(first.hasMore(request)).isTrue();

        Page<Contact> second = source.load(request.next(null)).join();
        assertThat(second.items()).extracting(Contact::name).containsExactly("Ben", "Cara");
        assertThat(second.hasMore(request.next(null))).isFalse();

        Page<Contact> found = source.load(request.withFilter(" b ")).join();
        assertThat(found.items()).extracting(Contact::name).containsExactly("Bea", "Ben");
    }

    @Test
    void descendingReversesAndUnknownKeysFail() {
        var source = new ListDataSource<>(contacts).sortBy("id", Contact::id);
        var desc = PageRequest.first(10).withSort(List.of(SortSpec.descending("id")));
        assertThat(source.load(desc).join().items()).extracting(Contact::id).containsExactly(4, 3, 2, 1);
        var unknown = PageRequest.first(10).withSort(List.of(SortSpec.ascending("nope")));
        assertThat(source.load(unknown)).isCompletedExceptionally();
    }

    @Test
    void cursorPagesHaveMoreWhileThereIsACursor() {
        var request = PageRequest.first(2);
        assertThat(Page.ofCursor(List.of(1, 2), "next").hasMore(request)).isTrue();
        assertThat(Page.ofCursor(List.of(1), null).hasMore(request)).isFalse();
        assertThatThrownBy(() -> PageRequest.first(0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void selectionIsByKeyAndSurvivesNewObjects() {
        var selection = new SelectionState<Contact>(Contact::id, SelectionMode.MULTIPLE);
        selection.select(contacts.get(0));
        assertThat(selection.isSelected(new Contact(1, "Cara (renamed)"))).isTrue();

        selection.selectRange(contacts, 3, 1);
        assertThat(selection.count()).isEqualTo(4);
        assertThat(selection.containsAll(contacts)).isTrue();

        selection.deselect(contacts.get(2));
        assertThat(selection.containsSome(contacts)).isTrue();
        assertThat(selection.selectedIn(contacts)).extracting(Contact::id).containsExactly(1, 2, 4);

        selection.clear();
        assertThat(selection.emptyProperty().get()).isTrue();
    }

    @Test
    void singleSelectionKeepsOne() {
        var selection = new SelectionState<Contact>(Contact::id, SelectionMode.SINGLE);
        selection.select(contacts.get(0));
        selection.select(contacts.get(1));
        assertThat(selection.keys()).containsExactly(2);
        selection.selectAll(contacts);
        assertThat(selection.keys()).containsExactly(4);

        var none = new SelectionState<Contact>(Contact::id, SelectionMode.NONE);
        none.select(contacts.get(0));
        assertThat(none.count()).isZero();
    }

    @Test
    void pagedDataSourceRunsOffTheCallingThreadAndReportsErrors() {
        Thread caller = Thread.currentThread();
        PagedDataSource<String> ok = r -> {
            assertThat(Thread.currentThread()).isNotSameAs(caller);
            return Page.of(List.of("a"), 1);
        };
        assertThat(ok.load(PageRequest.first(10)).join().items()).containsExactly("a");
        PagedDataSource<String> failing = r -> {
            throw new IllegalStateException("down");
        };
        assertThat(failing.load(PageRequest.first(10))).failsWithin(java.time.Duration.ofSeconds(5));
    }
}
