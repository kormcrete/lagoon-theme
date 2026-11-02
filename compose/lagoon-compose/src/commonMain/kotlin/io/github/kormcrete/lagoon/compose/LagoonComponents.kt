package io.github.kormcrete.lagoon.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ---------------------------------------------------------------- surfaces

/** A white card with soft corners and a soft shadow: Lagoon's main surface. */
@Composable
fun LagoonCard(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(0.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = Lagoon.colors
    val shape = Lagoon.shapes.card
    Column(
        modifier
            .shadow(6.dp, shape, ambientColor = c.shadow.copy(alpha = 0.10f), spotColor = c.shadow.copy(alpha = 0.10f))
            .background(c.surface, shape)
            .clip(shape)
            .padding(padding),
        content = content,
    )
}

/** A thin divider in the card's divider colour. */
@Composable
fun LagoonDivider(modifier: Modifier = Modifier) =
    HorizontalDivider(modifier, thickness = 1.dp, color = Lagoon.colors.divider)

/** A small grey heading above a group: "OCTOBER", "ITEMS". */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(text.uppercase(), modifier, style = Lagoon.type.overline, color = Lagoon.colors.inkMuted)
}

// ---------------------------------------------------------------- buttons

enum class LagoonButtonStyle { Primary, Tonal, Outline, Danger, Night }

/** Lagoon's button. [height] defaults to the platform's button height. */
@Composable
fun LagoonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: LagoonButtonStyle = LagoonButtonStyle.Primary,
    icon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = Lagoon.metrics.buttonHeight,
) {
    val c = Lagoon.colors
    val shape = Lagoon.shapes.control
    val (background, content, border) = when (style) {
        LagoonButtonStyle.Primary -> Triple(c.primary, c.onPrimary, Color.Transparent)
        LagoonButtonStyle.Tonal -> Triple(c.primaryTint, c.primaryStrong, Color.Transparent)
        LagoonButtonStyle.Outline -> Triple(c.surface, c.ink, c.line)
        LagoonButtonStyle.Danger -> Triple(c.surface, c.danger, c.dangerLine)
        LagoonButtonStyle.Night -> Triple(c.night, c.onNight, Color.Transparent)
    }
    val lifted = style == LagoonButtonStyle.Primary && enabled
    Row(
        modifier
            .height(height)
            .then(
                if (lifted) Modifier.shadow(8.dp, shape, ambientColor = c.primary.copy(alpha = 0.30f), spotColor = c.primary.copy(alpha = 0.35f))
                else Modifier
            )
            .clip(shape)
            .background(background)
            .border(1.dp, border, shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .alpha(if (enabled) 1f else 0.5f)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) Icon(icon, null, Modifier.size(18.dp), tint = content)
        Text(text, style = Lagoon.type.bodyStrong, color = content, maxLines = 1)
        if (trailingIcon != null) Icon(trailingIcon, null, Modifier.size(18.dp), tint = content)
    }
}

/** A round icon button; white with a hairline shadow by default, as in app bars. */
@Composable
fun LagoonIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = Lagoon.metrics.touchTarget,
    background: Color = Lagoon.colors.surface,
    tint: Color = Lagoon.colors.ink,
    lifted: Boolean = true,
) {
    Box(
        modifier
            .size(size)
            .then(if (lifted) Modifier.shadow(2.dp, CircleShape, ambientColor = Lagoon.colors.shadow.copy(alpha = 0.2f)) else Modifier)
            .clip(CircleShape)
            .background(background)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, Modifier.size(size * 0.45f), tint = tint)
    }
}

// ---------------------------------------------------------------- small pieces

enum class LagoonTone { Info, Success, Attention, Danger, Neutral }

@Composable
@androidx.compose.runtime.ReadOnlyComposable
private fun LagoonTone.colors(): Pair<Color, Color> {
    val c = Lagoon.colors
    return when (this) {
        LagoonTone.Info -> c.primaryTint to c.primaryStrong
        LagoonTone.Success -> c.successTint to c.success
        LagoonTone.Attention -> c.attentionTint to c.attention
        LagoonTone.Danger -> c.dangerTint to c.danger
        LagoonTone.Neutral -> c.neutralTint to c.neutral
    }
}

/** A status pill with a dot: "● Placed". */
@Composable
fun StatusPill(text: String, tone: LagoonTone, modifier: Modifier = Modifier) {
    val (bg, fg) = tone.colors()
    Row(
        modifier.clip(Lagoon.shapes.pill).background(bg).padding(start = 7.dp, end = 8.dp, top = 3.dp, bottom = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(fg))
        Text(text, style = Lagoon.type.label, color = fg, maxLines = 1)
    }
}

/** A small tinted tag: "Invoice · 30 days". */
@Composable
fun LagoonTag(text: String, modifier: Modifier = Modifier, tone: LagoonTone? = null) {
    val (bg, fg) = tone?.colors() ?: (Lagoon.colors.surfaceMuted to Lagoon.colors.ink)
    Text(
        text,
        modifier.clip(Lagoon.shapes.small).background(bg).padding(horizontal = 8.dp, vertical = 4.dp),
        style = if (tone == null) Lagoon.type.small else Lagoon.type.label,
        color = fg,
        maxLines = 1,
    )
}

/** A rounded square with initials or a short code, coloured by [key]: customers, products, quantities. */
@Composable
fun AvatarTile(text: String, modifier: Modifier = Modifier, key: String = text, size: Dp = 40.dp, colors: Pair<Color, Color>? = null) {
    val (bg, fg) = colors ?: Lagoon.colors.tile(key)
    Box(
        modifier.size(size).clip(Lagoon.shapes.control).background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = Lagoon.type.label.copy(fontSize = Lagoon.type.label.fontSize * (size.value / 36f)), color = fg, maxLines = 1)
    }
}

/** A tinted rounded square holding an icon: delivery, shipping and payment rows. */
@Composable
fun IconTile(icon: ImageVector, modifier: Modifier = Modifier, tone: LagoonTone? = null, size: Dp = 36.dp) {
    val (bg, fg) = tone?.colors() ?: (Lagoon.colors.surfaceMuted to Lagoon.colors.primary)
    Box(modifier.size(size).clip(Lagoon.shapes.small).background(bg), contentAlignment = Alignment.Center) {
        Icon(icon, null, Modifier.size(size * 0.5f), tint = fg)
    }
}

/** A tinted message line: "Within credit limit · €1,451.84 left". */
@Composable
fun LagoonBanner(text: String, tone: LagoonTone, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    val (bg, fg) = tone.colors()
    Row(
        modifier.clip(Lagoon.shapes.control).background(bg).padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) Icon(icon, null, Modifier.size(18.dp), tint = fg)
        Text(text, style = Lagoon.type.small.copy(fontWeight = Lagoon.type.label.fontWeight), color = fg)
    }
}

// ---------------------------------------------------------------- inputs

/** A quantity stepper: − 3 +. [filled] uses the primary colour, as in a product list. */
@Composable
fun LagoonStepper(
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    filled: Boolean = false,
    canDecrement: Boolean = true,
    canIncrement: Boolean = true,
) {
    val c = Lagoon.colors
    val h = if (Lagoon.metrics.touchTarget >= 44.dp) 40.dp else 30.dp
    val bg = if (filled) c.primary else c.surfaceMuted
    val fg = if (filled) c.onPrimary else c.ink
    Row(modifier.height(h).clip(Lagoon.shapes.pill).background(bg), verticalAlignment = Alignment.CenterVertically) {
        StepperButton(LagoonIcons.Minus, "One less $label", onDecrement, canDecrement, fg, h)
        Text(
            value.toString(),
            Modifier.widthIn(min = 22.dp),
            style = Lagoon.type.bodyStrong,
            color = fg,
            textAlign = TextAlign.Center,
        )
        StepperButton(LagoonIcons.Plus, "One more $label", onIncrement, canIncrement, fg, h)
    }
}

@Composable
private fun StepperButton(icon: ImageVector, description: String, onClick: () -> Unit, enabled: Boolean, tint: Color, height: Dp) {
    Box(
        Modifier
            .size(width = height * 0.95f, height = height)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description }
            .alpha(if (enabled) 1f else 0.35f),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, Modifier.size(height * 0.4f), tint = tint)
    }
}

/** Options in one rounded track; the selected one is a white raised segment: "All 12 · Placed 4 · …". */
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = Lagoon.colors
    val h = Lagoon.metrics.controlHeight - 8.dp
    Row(
        modifier
            .clip(Lagoon.shapes.control)
            .background(c.track)
            .padding(4.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEachIndexed { i, option ->
            val selected = i == selectedIndex
            Box(
                Modifier
                    .height(h)
                    .then(if (selected) Modifier.shadow(2.dp, Lagoon.shapes.small, ambientColor = c.shadow.copy(alpha = 0.25f)) else Modifier)
                    .clip(Lagoon.shapes.small)
                    .background(if (selected) c.surface else Color.Transparent)
                    .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(i) })
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    option,
                    style = if (selected) Lagoon.type.label.copy(fontSize = Lagoon.metrics.smallSize) else Lagoon.type.small,
                    color = if (selected) c.ink else c.inkMuted,
                    maxLines = 1,
                )
            }
        }
    }
}

/** A chip in a horizontal filter row; the selected one is dark. */
@Composable
fun LagoonChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    val c = Lagoon.colors
    Row(
        modifier
            .height(Lagoon.metrics.controlHeight - 4.dp)
            .clip(Lagoon.shapes.control)
            .background(if (selected) c.night else c.surface)
            .border(1.dp, if (selected) c.night else c.line, Lagoon.shapes.control)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val fg = if (selected) c.onNight else c.ink
        if (icon != null) Icon(icon, null, Modifier.size(16.dp), tint = fg)
        Text(text, style = Lagoon.type.small.copy(fontWeight = Lagoon.type.label.fontWeight), color = fg, maxLines = 1)
    }
}

/** A rounded search field with a magnifier and a placeholder. */
@Composable
fun LagoonSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (RowScope.() -> Unit)? = null,
) {
    val c = Lagoon.colors
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.semantics { contentDescription = placeholder },
        singleLine = true,
        textStyle = Lagoon.type.body.copy(color = c.ink),
        cursorBrush = SolidColor(c.primary),
        decorationBox = { inner ->
            Row(
                Modifier
                    .height(Lagoon.metrics.controlHeight + 2.dp)
                    .clip(Lagoon.shapes.control)
                    .background(c.surface)
                    .border(1.dp, c.lineSoft, Lagoon.shapes.control)
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(LagoonIcons.Search, null, Modifier.size(18.dp), tint = c.inkMuted)
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) Text(placeholder, style = Lagoon.type.body, color = c.inkMuted, maxLines = 1)
                    inner()
                }
                trailing?.invoke(this)
            }
        },
    )
}

/** A labelled text field with an optional error under it. */
@Composable
fun LagoonTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    hint: String? = null,
    error: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val c = Lagoon.colors
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = Lagoon.type.small.copy(fontWeight = Lagoon.type.label.fontWeight), color = c.ink)
            if (hint != null) Text(hint, style = Lagoon.type.small, color = c.inkMuted)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = label },
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = keyboardOptions,
            textStyle = Lagoon.type.body.copy(color = c.ink),
            cursorBrush = SolidColor(c.primary),
            decorationBox = { inner ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = Lagoon.metrics.controlHeight)
                        .clip(Lagoon.shapes.control)
                        .background(c.surface)
                        .border(if (error != null) 1.5.dp else 1.dp, if (error != null) c.danger else c.line, Lagoon.shapes.control)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) Text(placeholder, style = Lagoon.type.body, color = c.inkFaint)
                    inner()
                }
            },
        )
        if (error != null) Text(error, style = Lagoon.type.small, color = c.danger)
    }
}

/** A selectable card with an icon tile, title, subtitle and an amount: shipping and payment options. */
@Composable
fun OptionCard(
    selected: Boolean,
    onClick: () -> Unit,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tone: LagoonTone = LagoonTone.Info,
    amount: String? = null,
    enabled: Boolean = true,
) {
    val c = Lagoon.colors
    val shape = Lagoon.shapes.card
    Row(
        modifier
            .fillMaxWidth()
            .shadow(if (enabled) 4.dp else 0.dp, shape, ambientColor = c.shadow.copy(alpha = 0.08f), spotColor = c.shadow.copy(alpha = 0.08f))
            .clip(shape)
            .background(if (selected) c.selection else c.surface)
            .border(if (selected) 2.dp else 1.dp, if (selected) c.primary else c.lineSoft, shape)
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .alpha(if (enabled) 1f else 0.5f)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) IconTile(icon, tone = tone, size = 44.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = Lagoon.type.bodyStrong, color = c.ink)
            Text(subtitle, style = Lagoon.type.small, color = c.inkMuted)
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (amount != null) Text(amount, style = Lagoon.type.bodyStrong, color = c.ink)
            Box(
                Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (selected) c.primary else Color.Transparent)
                    .border(2.dp, if (selected) c.primary else c.line, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) Icon(LagoonIcons.Check, null, Modifier.size(12.dp), tint = c.onPrimary)
            }
        }
    }
}

/** Progress as segments, for step flows: ▬▬▭▭ is step 2 of 4. */
@Composable
fun StepProgress(steps: Int, current: Int, modifier: Modifier = Modifier) {
    val c = Lagoon.colors
    Row(
        modifier.semantics { contentDescription = "Step $current of $steps" },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(steps) { i ->
            Box(Modifier.weight(1f).height(4.dp).clip(Lagoon.shapes.pill).background(if (i < current) c.primary else c.line))
        }
    }
}

/** A label and value on one line, right-aligned value: totals blocks. */
@Composable
fun AmountRow(label: String, amount: String, modifier: Modifier = Modifier, strong: Boolean = false, amountColor: Color? = null) {
    val c = Lagoon.colors
    val style = if (strong) Lagoon.type.heading.copy(fontWeight = Lagoon.type.title.fontWeight) else Lagoon.type.small
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = style, color = if (strong) c.ink else c.inkMuted)
        Text(amount, style = style, color = amountColor ?: c.ink, maxLines = 1, overflow = TextOverflow.Clip)
    }
}

/** Gives [content] the Lagoon ink colour and body text style, as a screen's root. */
@Composable
fun LagoonSurface(modifier: Modifier = Modifier, color: Color = Lagoon.colors.ground, content: @Composable () -> Unit) {
    Box(modifier.background(color)) {
        CompositionLocalProvider(LocalContentColor provides Lagoon.colors.ink, LocalTextStyle provides Lagoon.type.body) {
            content()
        }
    }
}

/** Fixed-width spacer. */
@Composable
fun HSpace(width: Dp) = Spacer(Modifier.width(width))

/** Fixed-height spacer. */
@Composable
fun VSpace(height: Dp) = Spacer(Modifier.height(height))

/** Fills a row up to [minHeight]: list rows at the platform's row height. */
fun Modifier.rowHeight(minHeight: Dp): Modifier = this.heightIn(min = minHeight)
