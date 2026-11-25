package io.joshuasalcedo.fx.ui.core.sheet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class SheetTest {

    /// A sheet in memory, for the clipboard and undo tests.
    static final class MemoryModel implements SheetModel {
        private final int rows;
        private final int columns;
        private final Map<CellAddress, CellValue> cells = new HashMap<>();
        private final List<Consumer<Change>> listeners = new ArrayList<>();

        MemoryModel(int rows, int columns) {
            this.rows = rows;
            this.columns = columns;
        }

        @Override public int rowCount() { return rows; }
        @Override public int columnCount() { return columns; }
        @Override public CellValue get(CellAddress a) { return cells.getOrDefault(a, CellValue.empty()); }

        @Override
        public void set(CellAddress a, CellValue v) {
            if (a.row() >= rows || a.column() >= columns) {
                return;
            }
            CellValue before = get(a);
            cells.put(a, v);
            listeners.forEach(l -> l.accept(new Change(a, before, v)));
        }

        @Override public void addListener(Consumer<Change> l) { listeners.add(l); }
        @Override public void removeListener(Consumer<Change> l) { listeners.remove(l); }
    }

    @Test
    void addressesReadAndWriteA1() {
        assertThat(CellAddress.parse("A1")).isEqualTo(CellAddress.of(0, 0));
        assertThat(CellAddress.parse("b3")).isEqualTo(CellAddress.of(2, 1));
        assertThat(CellAddress.parse("AA10")).isEqualTo(CellAddress.of(9, 26));
        assertThat(CellAddress.of(0, 701).toString()).isEqualTo("ZZ1");
        assertThat(CellAddress.of(0, 702).toString()).isEqualTo("AAA1");
        assertThatThrownBy(() -> CellAddress.parse("1A")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rangesAreNormalized() {
        CellRange r = CellRange.parse("C3:A1");
        assertThat(r.toString()).isEqualTo("A1:C3");
        assertThat(r.rows()).isEqualTo(3);
        assertThat(r.cells()).hasSize(9);
        assertThat(r.contains(CellAddress.parse("B2"))).isTrue();
        assertThat(r.contains(CellAddress.parse("D2"))).isFalse();
    }

    @Test
    void valuesParseLikeASpreadsheet() {
        assertThat(CellValue.parse("12.5").type()).isEqualTo(CellType.NUMBER);
        assertThat(CellValue.parse("1,240").asNumber()).isEqualByComparingTo("1240");
        assertThat(CellValue.parse("$1,200.50").type()).isEqualTo(CellType.MONEY);
        assertThat(CellValue.parse("-$5").asNumber()).isEqualByComparingTo("-5");
        assertThat(CellValue.parse("15%").asNumber()).isEqualByComparingTo("0.15");
        assertThat(CellValue.parse("2026-11-25").value()).isEqualTo(LocalDate.of(2026, 11, 25));
        assertThat(CellValue.parse("TRUE").value()).isEqualTo(true);
        assertThat(CellValue.parse("  ").isEmpty()).isTrue();
        assertThat(CellValue.parse("12 apples").type()).isEqualTo(CellType.TEXT);
        assertThat(CellValue.parse("2026-13-45").type()).isEqualTo(CellType.TEXT);
        assertThat(CellValue.parse("1,2,3").type()).isEqualTo(CellType.TEXT);
        assertThat(CellValue.number(new BigDecimal("2.50")).asText()).isEqualTo("2.5");
    }

    @Test
    void clipboardQuotesLikeExcelAndReadsItBack() {
        var model = new MemoryModel(3, 3);
        model.set(CellAddress.parse("A1"), CellValue.text("plain"));
        model.set(CellAddress.parse("B1"), CellValue.text("tab\there"));
        model.set(CellAddress.parse("A2"), CellValue.text("say \"hi\"\nbye"));
        model.set(CellAddress.parse("B2"), CellValue.number(3));
        String tsv = SheetClipboard.toTsv(model, CellRange.parse("A1:B2"));
        assertThat(tsv).isEqualTo("plain\t\"tab\there\"\n\"say \"\"hi\"\"\nbye\"\t3");

        assertThat(SheetClipboard.parseTsv(tsv)).containsExactly(
                List.of("plain", "tab\there"), List.of("say \"hi\"\nbye", "3"));
        assertThat(SheetClipboard.parseTsv("a\tb\r\nc\td\r\n")).containsExactly(List.of("a", "b"), List.of("c", "d"));
        assertThat(SheetClipboard.parseTsv("a\t\tc")).containsExactly(List.of("a", "", "c"));
    }

    @Test
    void pasteStopsAtTheEdgeAndParsesValues() {
        var model = new MemoryModel(2, 2);
        CellRange written = SheetClipboard.paste(model, CellAddress.parse("B1"), "10\tcut\n15%\tcut\nlost\tlost");
        assertThat(written).isEqualTo(CellRange.parse("B1:B2"));
        assertThat(model.get(CellAddress.parse("B1")).asNumber()).isEqualByComparingTo("10");
        assertThat(model.get(CellAddress.parse("B2")).type()).isEqualTo(CellType.PERCENT);
    }

    @Test
    void undoTakesBackAPasteAtOnceAndRedoDoesItAgain() {
        var model = new MemoryModel(3, 3);
        var undo = new SheetUndoStack(model);
        model.set(CellAddress.parse("A1"), CellValue.text("first"));
        undo.compound(() -> SheetClipboard.paste(model, CellAddress.parse("A1"), "a\tb\nc\td"));
        assertThat(model.get(CellAddress.parse("B2")).asText()).isEqualTo("d");

        undo.undo();
        assertThat(model.get(CellAddress.parse("A1")).asText()).isEqualTo("first");
        assertThat(model.get(CellAddress.parse("B2")).isEmpty()).isTrue();
        assertThat(undo.canRedo()).isTrue();

        undo.redo();
        assertThat(model.get(CellAddress.parse("A1")).asText()).isEqualTo("a");

        undo.undo();
        undo.undo();
        assertThat(model.get(CellAddress.parse("A1")).isEmpty()).isTrue();
        assertThat(undo.canUndo()).isFalse();

        model.set(CellAddress.parse("C3"), CellValue.text("new"));
        assertThat(undo.canRedo()).as("a new edit drops the redo steps").isFalse();
    }

    @Test
    void validationRefusesWithAMessage() {
        var hours = SheetValidation.numberBetween(CellRange.parse("B2:B10"), BigDecimal.ZERO, BigDecimal.valueOf(80))
                .and(SheetValidation.required(CellRange.parse("A2:A10")));
        assertThat(hours.validate(CellAddress.parse("B2"), CellValue.parse("40"))).isNull();
        assertThat(hours.validate(CellAddress.parse("B2"), CellValue.parse("90"))).contains("0 to 80");
        assertThat(hours.validate(CellAddress.parse("B2"), CellValue.parse("lots"))).contains("number");
        assertThat(hours.validate(CellAddress.parse("A3"), CellValue.empty())).contains("empty");
        assertThat(hours.validate(CellAddress.parse("C3"), CellValue.parse("anything"))).isNull();
    }
}
