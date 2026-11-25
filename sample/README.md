# ModernFX Sample

A small back office built with ModernFX, as an app that uses the library would be: its own pom, a dependency on
the `modernfx` jar, a module of its own. Each screen is a window inside the main window (`ui.core.mdi`):

| Screen | Shows |
|---|---|
| Contacts | `ListMfx` with a `ListDataSource`: search, a team filter, sections by letter, 25 at a time |
| Invoices | shadcn's data table: `TableMfx`, a checkbox column, renderers (avatar, badge, date, money), row actions, the Columns menu, bulk actions, totals |
| Tasks | `BoardMfx`: drag cards between columns, a WIP limit, + to add |
| Files | `TreeMfx`: folders that load when first opened |
| Timesheet | `SheetMfx`: hours checked (0–24), undo and redo, copy and paste with Excel |

The bar at the top opens the screens, tiles the windows (`SplitButtonMfx`) and switches light and dark.

## Run it

ModernFX isn't on Maven Central yet, so install it first, then run the sample:

```
cd ..            # the modernfx folder
mvn install -DskipTests
cd sample
mvn javafx:run
```

`pom.xml` adds the two `--add-exports` options SheetMfx (ControlsFX's SpreadsheetView) needs on the module path.
