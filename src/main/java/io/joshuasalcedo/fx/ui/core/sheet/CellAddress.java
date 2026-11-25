package io.joshuasalcedo.fx.ui.core.sheet;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/// A cell's place: its row and column, from 0. Written in A1 notation, `A1` is (0, 0), `B3` is (2, 1), `AA10`
/// is (9, 26).
public record CellAddress(int row, int column) implements Comparable<CellAddress> {

    private static final Pattern A1 = Pattern.compile("([A-Za-z]+)(\\d+)");

    public CellAddress {
        if (row < 0 || column < 0) {
            throw new IllegalArgumentException("row and column start at 0: " + row + ", " + column);
        }
    }

    public static CellAddress of(int row, int column) {
        return new CellAddress(row, column);
    }

    /// Reads A1 notation (`B3`, `aa10`).
    public static CellAddress parse(String a1) {
        Matcher m = A1.matcher(a1.strip());
        if (!m.matches()) {
            throw new IllegalArgumentException("not an A1 address: " + a1);
        }
        int row = Integer.parseInt(m.group(2)) - 1;
        return new CellAddress(row, columnIndex(m.group(1)));
    }

    /// The letters of a column: 0 is `A`, 25 is `Z`, 26 is `AA`.
    public static String columnName(int column) {
        if (column < 0) {
            throw new IllegalArgumentException("column < 0: " + column);
        }
        StringBuilder out = new StringBuilder();
        for (int n = column + 1; n > 0; n = (n - 1) / 26) {
            out.insert(0, (char) ('A' + (n - 1) % 26));
        }
        return out.toString();
    }

    /// The index of a column's letters: `A` is 0, `AA` is 26.
    public static int columnIndex(String letters) {
        int n = 0;
        for (char ch : letters.toUpperCase(Locale.ROOT).toCharArray()) {
            if (ch < 'A' || ch > 'Z') {
                throw new IllegalArgumentException("not column letters: " + letters);
            }
            n = n * 26 + (ch - 'A' + 1);
        }
        return n - 1;
    }

    /// The address `rows` down and `columns` right of this one.
    public CellAddress offset(int rows, int columns) {
        return new CellAddress(row + rows, column + columns);
    }

    @Override
    public int compareTo(CellAddress o) {
        return row != o.row ? Integer.compare(row, o.row) : Integer.compare(column, o.column);
    }

    /// A1 notation.
    @Override
    public String toString() {
        return columnName(column) + (row + 1);
    }
}
