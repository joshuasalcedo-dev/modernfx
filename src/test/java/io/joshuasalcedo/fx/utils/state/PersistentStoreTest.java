package io.joshuasalcedo.fx.utils.state;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PersistentStoreTest {

    record Settings(String theme, int width, List<String> recent) {
        static final Settings DEFAULTS = new Settings("dark", 1200, List.of());
    }

    sealed interface Action {
        record Theme(String theme) implements Action {}
        record Width(int width) implements Action {}
    }

    static Settings reduce(Settings s, Action a) {
        return switch (a) {
            case Action.Theme t -> new Settings(t.theme(), s.width(), s.recent());
            case Action.Width w -> new Settings(s.theme(), w.width(), s.recent());
        };
    }

    @TempDir
    Path dir;

    private PersistentStore.Builder<Settings, Action> builder(Path file) {
        return PersistentStore.<Settings, Action>builder(Settings.class, Settings.DEFAULTS, PersistentStoreTest::reduce)
                .file(file)
                .updates(Runnable::run)
                .saveDelay(Duration.ofMillis(20));
    }

    @Test
    void startsFromTheInitialStateWhenThereIsNoFile() {
        try (var store = builder(dir.resolve("state.json")).open()) {
            assertThat(store.get()).isEqualTo(Settings.DEFAULTS);
        }
        assertThat(dir.resolve("state.json")).doesNotExist();
    }

    @Test
    void savesOnCloseAndReadsItBack() {
        Path file = dir.resolve("app/state.json");
        try (var store = builder(file).open()) {
            store.dispatch(new Action.Theme("light"));
            store.dispatch(new Action.Width(800));
        }
        assertThat(file).exists();
        assertThat(dir.resolve("app/state.json.tmp")).doesNotExist();

        try (var store = builder(file).open()) {
            assertThat(store.get()).isEqualTo(new Settings("light", 800, List.of()));
        }
    }

    @Test
    void savesByItselfShortlyAfterAChange() throws Exception {
        Path file = dir.resolve("state.json");
        try (var store = builder(file).open()) {
            store.dispatch(new Action.Theme("light"));
            for (int i = 0; i < 100 && !Files.exists(file); i++) {
                Thread.sleep(20);
            }
            assertThat(Files.readString(file)).contains("\"light\"").contains("\"version\" : 1");
        }
    }

    @Test
    void movesAnUnreadableFileAsideAndStartsOver() throws Exception {
        Path file = dir.resolve("state.json");
        Files.writeString(file, "{ not json");
        try (var store = builder(file).open()) {
            assertThat(store.get()).isEqualTo(Settings.DEFAULTS);
        }
        assertThat(Files.readString(dir.resolve("state.json.broken"))).isEqualTo("{ not json");
    }

    @Test
    void keepsAFileFromANewerVersionAside() throws Exception {
        Path file = dir.resolve("state.json");
        Files.writeString(file, """
                {"version": 5, "state": {"theme": "light", "width": 1, "recent": []}}""");
        try (var store = builder(file).version(2).open()) {
            assertThat(store.get()).isEqualTo(Settings.DEFAULTS);
        }
        assertThat(dir.resolve("state.json.broken")).exists();
    }

    @Test
    void migratesAnOlderFile() throws Exception {
        Path file = dir.resolve("state.json");
        // Version 1 called the theme "colors" and had no recent list.
        Files.writeString(file, """
                {"version": 1, "state": {"colors": "light", "width": 900}}""");
        var store = builder(file)
                .version(2)
                .migrate(1, s -> {
                    s.set("theme", s.remove("colors"));
                    s.putArray("recent").add("a.mp4");
                    return s;
                })
                .open();
        try (store) {
            assertThat(store.get()).isEqualTo(new Settings("light", 900, List.of("a.mp4")));
            store.dispatch(new Action.Width(1000));
        }
        assertThat(Files.readString(file)).contains("\"version\" : 2").doesNotContain("colors");
    }

    @Test
    void ignoresFieldsTheStateNoLongerHas() throws Exception {
        Path file = dir.resolve("state.json");
        Files.writeString(file, """
                {"version": 1, "state": {"theme": "light", "width": 700, "recent": [], "old": true}}""");
        try (var store = builder(file).open()) {
            assertThat(store.get()).isEqualTo(new Settings("light", 700, List.of()));
        }
    }
}
