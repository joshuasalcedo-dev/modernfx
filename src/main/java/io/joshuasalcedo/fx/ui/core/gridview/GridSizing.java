package io.joshuasalcedo.fx.ui.core.gridview;

/// How a GridViewMfx sizes its tiles: no narrower than `minTileWidth`, `aspectRatio` wide for each 1 high (1 is
/// square, 16/9.0 a video), with `gap` pixels between them. The tiles grow to fill the row.
public record GridSizing(double minTileWidth, double aspectRatio, double gap) {

    /// Square tiles of at least 160 pixels, 12 apart.
    public static final GridSizing DEFAULT = new GridSizing(160, 1, 12);

    public GridSizing {
        if (minTileWidth <= 0 || aspectRatio <= 0 || gap < 0) {
            throw new IllegalArgumentException("minTileWidth and aspectRatio must be > 0, gap >= 0");
        }
    }

    /// How many columns fit in `width`.
    public int columns(double width) {
        return Math.max(1, (int) Math.floor((width + gap) / (minTileWidth + gap)));
    }

    /// The width of each tile when `width` is filled.
    public double tileWidth(double width) {
        int n = columns(width);
        return Math.max(minTileWidth, Math.floor((width - gap * (n - 1)) / n));
    }

    /// The height of a tile this wide.
    public double tileHeight(double tileWidth) {
        return Math.floor(tileWidth / aspectRatio);
    }
}
