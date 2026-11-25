package io.joshuasalcedo.fx.ui.core.sheet;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// Copies and pastes ranges of cells as tab-separated text, the format Excel and Google Sheets put on the clipboard,
/// so cells go between them and a SheetMfx both ways. Cells with a tab, a line break or a quote are quoted, as
/// Excel does. SheetMfx uses it for Ctrl+C and Ctrl+V.
@NullMarked
public final class SheetClipboard {

    private SheetClipboard() {
    }

    /// The cells of `range` as tab-separated text, a line per row.
    public static String toTsv(SheetModel model, CellRange range) {
        StringBuilder out = new StringBuilder();
        for (int r = range.start().row(); r <= range.end().row(); r++) {
            if (r > range.start().row()) {
                out.append('\n');
            }
            for (int c = range.start().column(); c <= range.end().column(); c++) {
                if (c > range.start().column()) {
                    out.append('\t');
                }
                out.append(quote(model.get(new CellAddress(r, c)).asText()));
            }
        }
        return out.toString();
    }

    /// Reads tab-separated text into rows of cells. A final line break doesn't make an empty row.
    public static List<List<String>> parseTsv(String text) {
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean quoted = false;
        boolean atCellStart = true;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (quoted) {
                if (ch == '"') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '"') {
                        cell.append('"');
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    cell.append(ch);
                }
            } else if (ch == '"' && atCellStart) {
                quoted = true;
                atCellStart = false;
            } else if (ch == '\t') {
                row.add(cell.toString());
                cell.setLength(0);
                atCellStart = true;
            } else if (ch == '\n' || ch == '\r') {
                if (ch == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    i++;
                }
                row.add(cell.toString());
                cell.setLength(0);
                rows.add(row);
                row = new ArrayList<>();
                atCellStart = true;
            } else {
                cell.append(ch);
                atCellStart = false;
            }
        }
        if (!atCellStart || cell.length() > 0 || !row.isEmpty()) {
            row.add(cell.toString());
            rows.add(row);
        }
        return rows;
    }

    /// Writes tab-separated text into the sheet from `at` down and right, each cell read with [CellValue#parse].
    /// Cells past the sheet's edge are left out. Returns the range written, or null if nothing was.
    public static @Nullable CellRange paste(SheetModel model, CellAddress at, String tsv) {
        List<List<String>> rows = parseTsv(tsv);
        int lastRow = -1;
        int lastColumn = -1;
        for (int r = 0; r < rows.size(); r++) {
            int row = at.row() + r;
            if (row >= model.rowCount()) {
                break;
            }
            List<String> cells = rows.get(r);
            for (int c = 0; c < cells.size(); c++) {
                int column = at.column() + c;
                if (column >= model.columnCount()) {
                    break;
                }
                model.set(new CellAddress(row, column), CellValue.parse(cells.get(c)));
                lastRow = Math.max(lastRow, row);
                lastColumn = Math.max(lastColumn, column);
            }
        }
        return lastRow < 0 ? null : new CellRange(at, new CellAddress(lastRow, lastColumn));
    }

    /// Puts the cells of `range` on the system clipboard.
    public static void copy(SheetModel model, CellRange range) {
        ClipboardContent content = new ClipboardContent();
        content.putString(toTsv(model, range));
        Clipboard.getSystemClipboard().setContent(content);
    }

    /// Pastes the system clipboard's text at `at`. Returns the range written, or null.
    public static @Nullable CellRange paste(SheetModel model, CellAddress at) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        return clipboard.hasString() ? paste(model, at, clipboard.getString()) : null;
    }

    private static String quote(String cell) {
        if (cell.indexOf('\t') < 0 && cell.indexOf('\n') < 0 && cell.indexOf('\r') < 0 && cell.indexOf('"') < 0) {
            return cell;
        }
        return '"' + cell.replace("\"", "\"\"") + '"';
    }
}
