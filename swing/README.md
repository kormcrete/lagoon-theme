# Lagoon for Swing

`lagoon-flatlaf`: the Lagoon themes for [FlatLaf](https://www.formdev.com/flatlaf/). Part of
[lagoon-theme](../README.md), next to the [Compose theme](../compose/README.md).

- **Classic Lagoon**, `FlatLagoonLaf`: a light, clearly framed desktop look, with the quieter `FlatLagoonSoftLaf`
  and the dark `FlatLagoonDarkLaf` and `FlatLagoonSoftDarkLaf` (see [Dark](#dark)). Its colours live in this
  module's properties files.
- **Modern Lagoon**, `FlatLagoonModernLaf`: white cards and soft corners, built from the shared design tokens in
  [`tokens/`](../tokens/lagoon.tokens.json), as the Compose theme is (see [The modern Lagoon](#the-modern-lagoon)).

<p>
  <img src="../docs/order-desk-windows.png" alt="Order Desk with the Lagoon theme on Windows: the orders list, one order selected" width="49%">
  <img src="../docs/order-desk-windows-new-order.png" alt="A new order on Windows: categories, the product catalogue and a cart of four products in different quantities" width="49%">
  <img src="../docs/order-desk-macos.png" alt="The orders list with the Lagoon theme on macOS" width="49%">
  <img src="../docs/order-desk-macos-new-order.png" alt="The new order with the Lagoon theme on macOS" width="49%">
  <img src="../docs/order-desk-linux.png" alt="The orders list with the Lagoon theme on Linux" width="49%">
  <img src="../docs/order-desk-linux-new-order.png" alt="The new order with the Lagoon theme on Linux" width="49%">
</p>

<sub>Windows, macOS and Linux (top to bottom): the orders list and a new order in
<a href="https://github.com/kormcrete/swing-order-desk-demo">Order Desk</a>, a demo Swing application, under Lagoon,
captured with <a href="https://github.com/kormcrete/swing-ui-agent">swing-ui-agent</a>. Order Desk sets its own row
heights and table lines; <code>LagoonGallery</code> (see <a href="#build">Build</a>) shows every component as the theme
comes.</sub>

Classic Lagoon: a neutral grey palette, softened corners, one-pixel dark frames set in a light ring, bevelled buttons, framed card
tabs, white tables with a grid and glossy blue scroll bars with buttons: the clearly delimited look of a traditional desktop
application, for forms and data-heavy screens, on FlatLaf Light's layout and sizes — with everything FlatLaf brings
underneath: HiDPI scaling, native window decorations, SVG icons and an actively maintained look-and-feel. An
application that wants it denser turns on a few switches (see *A denser, classic look*).

## Use

Not on Maven Central yet: build it with `mvn install` in this folder, then depend on the snapshot.

```xml
<dependency>
    <groupId>io.github.kormcrete</groupId>
    <artifactId>lagoon-flatlaf</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

FlatLaf comes in as a transitive dependency (3.7.1); declare your own FlatLaf dependency to pin
another version.

```java
import io.github.kormcrete.flatlaf.lagoon.FlatLagoonLaf;

FlatLagoonLaf.setup();   // before the first Swing component is created
```

### Gloss and Soft

`FlatLagoonLaf` comes with the Gloss shade: scroll thumbs shaded like a tube (light on one side, darker along the
middle, glowing towards the other side) with round ends, in a grooved track, and rounded tabs with a pillow shade.
`FlatLagoonSoftLaf` is the quieter variant: thumbs and tabs shaded plainly from light to dark, with smaller round
ends; everything else is the same.

```java
FlatLagoonSoftLaf.setup();
```

The shade is the key `Lagoon.shade` (`gloss`, `soft` or `flat`, one colour each as in earlier versions), and the
round ends are `ScrollBar.thumbArc` and `TabbedPane.cardTabArc`; an application can set them in its own properties
(see *A denser, classic look*). Every shade is worked out from the part's colour in its state, so hover and pressed
keep it, and it is painted with plain gradients: no images.

### Dark

`FlatLagoonDarkLaf` is Lagoon at night: the same shapes, shading and sizes in a calm blue-grey palette, with soft
text that does not glare and the accent kept for what marks state. `FlatLagoonSoftDarkLaf` is its Soft variant.
Both are dark FlatLaf themes (`FlatLaf.isLafDark()`), built on FlatLaf Dark's defaults.

```java
FlatLagoonDarkLaf.setup();
```

Every colour of the dark palette comes from the variables at the top of `FlatLagoonDarkLaf.properties`, so an
application can retint it by setting those variables in its own `FlatLagoonDarkLaf.properties`. An application's
`FlatLagoonLaf.properties` applies to the dark themes too: write the colours there with FlatLaf's `[light]` prefix
(`[light]@background = #F5F6F7`) so they stay out of the dark ones; switches such as `Lagoon.dialogFont` apply to both.

### Fonts

Like FlatLaf Light, the theme uses the platform's UI font, with menus and the toolbar in the regular
weight; frame titles are bold. An application that wants another base font sets its own `defaultFont` before calling
`setup()`; the theme leaves it alone (for fonts based on Dialog 12, see `Lagoon.dialogFont` below):

```java
UIManager.put("defaultFont", new Font(Font.SANS_SERIF, Font.PLAIN, 13));
FlatLagoonLaf.setup();
```

### Switching at runtime

Like any FlatLaf theme:

```java
FlatLagoonLaf.setup();
FlatLaf.updateUI();
```

## What is in it

- `FlatLagoonLaf` — a `FlatLightLaf` subclass; FlatLaf loads `FlatLagoonLaf.properties` from the
  same package. The properties file is the one place that names colours and sizes, and is
  commented section by section.
- `FlatLagoonSoftLaf` — Lagoon with the Soft shade; `FlatLagoonSoftLaf.properties` holds its differences.
- `FlatLagoonDarkLaf`, `FlatLagoonSoftDarkLaf` — the dark Lagoon and its Soft variant; FlatLaf loads FlatLaf Dark's
  defaults, then `FlatLagoonLaf.properties`, then `FlatLagoonDarkLaf.properties`, which sets every colour again from its
  palette.
- `LagoonTabbedPaneUI` — FlatLaf's card tabs frame only the selected tab; this theme frames and
  shades the unselected ones too. Its two colours are properties keys
  (`TabbedPane.unselectedBorderColor`, `TabbedPane.unselectedBottomColor`), and it handles all
  four tab placements. `TabbedPane.selectedTabRaise` (2 pixels; 0 for none) lowers the unselected
  tabs so the selected one stands above them.
- `LagoonBorders` — fields, combo boxes, spinners, buttons and scroll panes set their frame in an
  etched ring (`Component.ringShadowColor` above and left, `Component.ringHighlightColor` below and
  right) in every enabled state, in the space FlatLaf reserves for its focus ring. A focused
  field's frame turns `Component.focusColor` and grows one pixel inwards.
- Sizes, paddings, row heights, the message dialog layout and trees follow FlatLaf Light's
  defaults. The palette is built on `@background` (#F2F2F2, FlatLaf Light's grey): one line in an
  application's properties re-tints every panel, toolbar, header and button with it.
- A focused table, list or tree shows its selection strongly (`@selectionBackground` with white
  text), an unfocused one in a pale blue.
- `LagoonSheen` — the raised face of buttons: a soft gradient under a one-pixel highlight, turned
  over and darker when pressed (`Lagoon.sheen.*` keys); hover shows in the frame. A button with a
  colour of its own, set or from a FlatLaf style (a primary action, say), is painted flat in that
  colour, under its own foreground.
- `LagoonTableUI` — a selected cell keeps the colour its renderer gives it unselected (a row or column
  colour), made more vivid, instead of the flat selection colour; cells in the table's own colours are selected
  as usual. Off for every table with `Table.selectionFollowsCellColor = false`, or for one table with the client
  property of the same name set to `false`.
- `LagoonTableHeaderUI` — a header the theme draws is as tall as its text.
- `LagoonProgressBarUI` — a grey track and an accent bar, as in FlatLaf Light; with
  `ProgressBar.borderColor` and `ProgressBar.barBorderColor` set (and a `ProgressBar.border` that leaves
  room), the track is framed like a field and the bar framed a pixel inside it.
- `LagoonTreeUI` — a tree that sets `JTree.lineStyle` to `"None"` paints no lines, as in Swing's
  own look-and-feels.
- `LagoonComboBoxUI`, `LagoonSpinnerUI`, `LagoonLabelUI`, `LagoonButtonUI` — FlatLaf's arrows and
  sizes by default; the switches below give the classic variants.

Still to follow as further UI delegates: the raised face on table headers, and a gradient on the
menubar and toolbar.

## A denser, classic look

An application turns these on in its own `FlatLagoonLaf.properties`, registered as FlatLaf custom
defaults before the theme is installed. All are `false` by default:

| Key | Effect |
|---|---|
| `Lagoon.dialogFont` | Fonts based on Dialog 12 instead of the platform's UI font. |
| `Lagoon.labelGap` | Labels, table cells, tree rows and buttons keep their icon-text gap after the text even without an icon; text above or below an icon gets the gap twice. |
| `Lagoon.oddButtonSize` | Buttons get an odd width and height. |
| `Lagoon.raisedArrowButtons` | Combo boxes and spinners get small raised arrow buttons with solid triangles, set inside the frame. |

```java
FlatLaf.registerCustomDefaultsSource("com.example.app.theme"); // holds com/example/app/theme/FlatLagoonLaf.properties
FlatLagoonLaf.setup();
```

The same file can set any other key, e.g. `@background`, `Table.rowHeight` or `TextField.margin`.
`src/test/resources/io/github/kormcrete/flatlaf/lagoon/dense/FlatLagoonLaf.properties` is a complete
example: every switch on, with the denser sizes, palette, trees and message layout the theme had
before its defaults followed FlatLaf Light.

## The modern Lagoon

`FlatLagoonModernLaf`, in the same jar, is Lagoon's modern look: a light ground (#F4F7FA), white cards with soft
corners and soft shadows, one blue accent (#1868C7), teal for what is good and amber for what needs attention. Its
colours, corner radii, control heights and text sizes come from `tokens/lagoon.tokens.json` at the top of the
repository, the design tokens the [Compose theme](../compose/README.md) is generated from too; the jar carries the file and the theme reads it when it is
installed. The classic `FlatLagoonLaf` stays as it is.

```java
import io.github.kormcrete.flatlaf.lagoon.FlatLagoonModernLaf;

FlatLagoonModernLaf.setup();   // before the first Swing component is created
```

The tokens name Inter as the font. Add FlatLaf's Inter to use it; without it the theme takes the platform's UI font:

```xml
<dependency>
    <groupId>com.formdev</groupId>
    <artifactId>flatlaf-fonts-inter</artifactId>
    <version>4.1</version>
</dependency>
```

Body text keeps the platform's UI font size, so FlatLaf scales the theme as it scales its own; headings and small
text keep the tokens' steps from it. An application that chose its own family with `FlatLaf.setPreferredFontFamily`
keeps it.

The tokens become UI defaults (`Lagoon.color.primary`, `Lagoon.radius.card`, `Lagoon.metrics.controlHeight`, ...;
the list is at the top of `FlatLagoonModernLaf.properties`). Buttons are the tokens' `buttonHeight` tall, fields,
combo boxes and spinners their `controlHeight`, table rows their `rowHeight`. An application re-tints through the
theme's own keys in its own `FlatLagoonModernLaf.properties`, as with any FlatLaf theme (`@accentColor`,
`Button.default.background`, a `[style]` rule...).

### Style classes

Set with `putClientProperty(FlatClientProperties.STYLE_CLASS, "...")`; several at once separated by spaces, e.g.
`"h2 muted"`.

| Style class | On | Look |
|---|---|---|
| `primary` | `JButton` | The accent with white text, as the default button. |
| `tonal` | `JButton` | The accent's tint with its strong ink. |
| `danger` | `JButton` | White, red text and a red hairline; a red tint on hover. |
| `card` | `JPanel` | White, the card's corners, a hairline and the tokens' gap inside. |
| `h1`, `h2`, `h3` | any | Title, headline (bold) and heading (semibold) sizes of the tokens. |
| `small`, `caption` | any | The tokens' small text, and their label size in semibold. |
| `muted` | any | The muted ink, for secondary text. |
| `pill-info`, `pill-success`, `pill-attention`, `pill-danger`, `pill-neutral` | `JLabel` | A status pill in a tone's tint and ink. |
| `segmented` | `JPanel`, `JToggleButton` | A rounded track and its options (see `SegmentedControl`). |
| `search` | `JTextField` | A magnifier in front, a clear button, a soft line. |

### Components

Small public classes in the same package, themed through UI defaults of the modern theme (`LagoonCard.*`,
`Stepper.*`, ...; each class's Javadoc lists its keys):

- `LagoonCard` — a white panel with rounded corners on a soft shadow, painted on whatever is under it.
- `StatusPill` — "● Placed": a label with a dot in one of the `LagoonTone`s (info, success, attention, danger,
  neutral).
- `AvatarTile` — initials or an icon in a tinted rounded tile; the same key always gets the same of the tokens' tile
  colours, as in the Compose theme.
- `Stepper` — − 3 + in a pill, between a minimum and a maximum; plain or filled in the accent.
- `SegmentedControl` — options in a rounded track, the selected one white.
- `Scrim` — a translucent veil over a window, in its layered pane at the modal layer, with modal content in the
  middle: `Scrim.show(frame.getRootPane(), card)` ... `scrim.close()`.

## Notes for applications

- Cell renderers that keep Swing's static `DefaultTableCellRenderer.noFocusBorder` (one pixel) and
  take the focus border from the look-and-feel shift their text when a cell gains the focus, because
  FlatLaf's cell margins are wider. Read `Table.cellNoFocusBorder` from `UIManager` instead, or set
  `Table.cellMargins = 1,1,1,1` in the application's own properties.
- FlatLaf honours `apple.laf.useScreenMenuBar` on macOS, which some other look-and-feels ignore.
  If your menu bar moves to the top of the screen after switching, that property is why.
- On Java 24 and later, FlatLaf's native library (window decorations and other platform
  integration) prints a "restricted method" warning when it loads; a future Java will block it.
  Start the application with `--enable-native-access=ALL-UNNAMED`, or add
  `Enable-Native-Access: ALL-UNNAMED` to an executable jar's manifest.

## Build

In this folder:

```
mvn package
```

Java 8 or later; CI runs the tests on Java 8 and 25, on Linux, macOS and Windows. Tests are headless. `LagoonGallery` (under `src/test`) opens a window with one
of everything, for a visual check:

```
mvn -q test-compile exec:java -Dexec.classpathScope=test -Dexec.mainClass=io.github.kormcrete.flatlaf.lagoon.LagoonGallery
```

Add `-Dexec.args=dark` (or `soft`, `soft-dark`) for another of the classic themes.

`LagoonModernGallery` does the same for the modern Lagoon: its components and style classes on the ground colour.
Given a file name it paints the gallery into a PNG instead, off screen:

```
mvn -q test-compile exec:java -Dexec.classpathScope=test -Dexec.mainClass=io.github.kormcrete.flatlaf.lagoon.LagoonModernGallery -Dexec.args=gallery.png -Djava.awt.headless=true
```

## Licence

Apache License 2.0 — see [`LICENSE`](../LICENSE) and [`NOTICE`](../NOTICE) at the top of the repository.
