package io.github.kormcrete.lagoon.compose

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Lagoon's line icons: 24-unit grid, 2-unit round strokes. Tint them with `Icon(…, tint = …)`.
 * Kept small on purpose; add icons here rather than pulling in an icon library.
 */
object LagoonIcons {
    val Back = line("Back", "M15 5l-7 7 7 7")
    val Forward = line("Forward", "M9 5l7 7-7 7")
    val ArrowRight = line("ArrowRight", "M5 12h14M13 6l6 6-6 6")
    val Close = line("Close", "M6 6l12 12M18 6L6 18")
    val Plus = line("Plus", "M12 5v14M5 12h14")
    val Minus = line("Minus", "M5 12h14")
    val Check = line("Check", "M5 12l5 5 9-10")
    val Search = line("Search", circle(11f, 11f, 7f) + "M20 20l-4-4")
    val More = filled("More", circle(5f, 12f, 1.8f) + circle(12f, 12f, 1.8f) + circle(19f, 12f, 1.8f))
    val Trash = line("Trash", "M4 7h16M10 11v6M14 11v6M6 7l1 13h10l1-13M9 7V4h6v3")
    val Cart = line("Cart", "M3 4h2l2.4 11h10.2L20 8H6.2" + circle(9f, 19f, 1.5f) + circle(17f, 19f, 1.5f))
    val Download = line("Download", "M12 4v11M7 10l5 5 5-5M5 20h14")
    val Pin = line("Pin", "M12 21s-7-6.2-7-11a7 7 0 0114 0c0 4.8-7 11-7 11z" + circle(12f, 10f, 2.5f))
    val Truck = line("Truck", "M3 7h11v9H3zM14 10h4l3 3v3h-7" + circle(7f, 17.5f, 1.8f) + circle(17f, 17.5f, 1.8f))
    val Bolt = line("Bolt", "M13 3L5 14h6l-1 7 8-11h-6l1-7z")
    val Invoice = line("Invoice", "M7 3h8l4 4v14H7zM15 3v4h4M10 12h6M10 16h6")
    val Card = line("Card", "M3 6h18v12H3zM3 10h18M7 15h3")
    val Shield = line("Shield", "M12 3l8 3v6c0 4.5-3.4 8.2-8 9-4.6-.8-8-4.5-8-9V6zM8.5 12l2.5 2.5 4.5-5")
    val Gift = line("Gift", "M20 12v8H4v-8M2 7h20v5H2zM12 22V7M12 7H8a2.5 2.5 0 010-5c3 0 4 5 4 5zM12 7h4a2.5 2.5 0 000-5c-3 0-4 5-4 5z")
    val Info = line("Info", circle(12f, 12f, 9f) + "M12 8v.01M12 11v5")
    val Warning = line("Warning", "M12 4L2.5 20h19zM12 10v4M12 17v.01")
    val List = line("List", "M4 6h16M4 12h16M4 18h10")
    val Office = line("Office", "M7 3h8l4 4v14H7zM15 3v4h4")
    val Coffee = line("Coffee", "M5 9h12v5a5 5 0 01-5 5h-2a5 5 0 01-5-5zM17 11h2a2 2 0 010 4h-2M9 3v3M13 3v3")
    val Drop = line("Drop", "M12 3s6 6.5 6 11a6 6 0 01-12 0c0-4.5 6-11 6-11z")
    val Waves = line("Waves", "M3 10c2-2 4-2 6 0s4 2 6 0 4-2 6 0M3 15c2-2 4-2 6 0s4 2 6 0 4-2 6 0")
    val Receipt = line("Receipt", "M6 3h12v18l-3-2-3 2-3-2-3 2zM9 8h6M9 12h6M9 16h3")
}

private fun circle(cx: Float, cy: Float, r: Float): String =
    "M${cx - r},${cy}a$r,$r 0 1,0 ${2 * r},0a$r,$r 0 1,0 ${-2 * r},0"

private fun builder(name: String) = ImageVector.Builder(
    name = "Lagoon.$name",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
)

private fun line(name: String, path: String): ImageVector = builder(name).addPath(
    pathData = PathParser().parsePathString(path).toNodes(),
    fill = null,
    stroke = SolidColor(Color.Black),
    strokeLineWidth = 2f,
    strokeLineCap = StrokeCap.Round,
    strokeLineJoin = StrokeJoin.Round,
).build()

private fun filled(name: String, path: String): ImageVector = builder(name).addPath(
    pathData = PathParser().parsePathString(path).toNodes(),
    fill = SolidColor(Color.Black),
).build()
