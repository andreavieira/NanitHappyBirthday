package com.andreavieira.nanithappybirthday.feature.birthday

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.andreavieira.nanithappybirthday.R
import com.andreavieira.nanithappybirthday.ui.theme.NanitColors
import com.andreavieira.nanithappybirthday.ui.theme.NanitDimens
import java.time.LocalDate

@Composable
fun BirthdayScreen(
    name: String,
    birthday: LocalDate,
    photoPath: String?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    theme: BirthdayTheme = BirthdayTheme.FOX,
    today: LocalDate = LocalDate.now(),
    contentPadding: PaddingValues = PaddingValues(),
    onChoosePhoto: (() -> Unit)? = null,
    photoChoiceEnabled: Boolean = true,
    shareImageLayer: GraphicsLayer? = null,
    onShare: (() -> Unit)? = null,
    shareEnabled: Boolean = true,
    onPhotoReady: (Boolean) -> Unit = {},
) {
    val age = birthdayAge(birthday, today)
    var photoBounds by remember { mutableStateOf(Rect.Zero) }
    var shareButtonTop by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    var shareButtonHeight by remember { mutableStateOf(NanitDimens.MinimumTouchTarget) }
    // The route resets readiness when the path changes; do not overwrite an immediate cache
    // success.
    LaunchedEffect(photoPath) { if (photoPath == null) onPhotoReady(true) }
    val scrollState = rememberScrollState()
    BoxWithConstraints(modifier.fillMaxSize().clipToBounds()) {
        val windowHeight = this.maxHeight
        val windowWidth = this.maxWidth
        val layoutHeight =
            windowHeight -
                contentPadding.calculateTopPadding() -
                contentPadding.calculateBottomPadding()
        BirthdayArtwork(
            name = name,
            age = age,
            photoPath = photoPath,
            theme = theme,
            contentPadding = contentPadding,
            reserveShareSpace = onShare != null,
            shareButtonHeight = shareButtonHeight,
            scrollState = scrollState,
            layoutHeight = layoutHeight,
            onPhotoBounds = { photoBounds = it },
            onShareButtonTop = { shareButtonTop = it },
        )
        if (shareImageLayer != null) {
            BirthdayShareImageRenderer(
                name = name,
                age = age,
                photoPath = photoPath,
                theme = theme,
                contentPadding = contentPadding,
                reserveShareSpace = onShare != null,
                shareButtonHeight = shareButtonHeight,
                layoutHeight = layoutHeight,
                windowWidth = windowWidth,
                windowHeight = windowHeight,
                scrollOverflowPx = scrollState.maxValue,
                shareImageLayer = shareImageLayer,
                onPhotoReady = onPhotoReady,
            )
        } else {
            LaunchedEffect(photoPath) { onPhotoReady(true) }
        }
        // Controls draw above the recorded card, so they never enter the shared bitmap.
        if (onChoosePhoto != null && photoBounds != Rect.Zero) {
            Box(Modifier.offset { IntOffset(photoBounds.left.toInt(), photoBounds.top.toInt()) }) {
                BirthdayCameraButton(
                    theme,
                    with(density) { photoBounds.width.toDp() },
                    photoChoiceEnabled,
                    onChoosePhoto,
                )
            }
        }
        if (onShare != null && shareButtonTop > 0f) {
            BirthdayShareButton(
                onShare,
                shareEnabled,
                Modifier.align(Alignment.TopCenter)
                    .offset { IntOffset(0, shareButtonTop.toInt()) }
                    .onSizeChanged { shareButtonHeight = with(density) { it.height.toDp() } },
            )
        }
        Box(Modifier.fillMaxSize().padding(contentPadding)) {
            BirthdayBackButton(
                onClose,
                Modifier.align(Alignment.TopStart)
                    .padding(
                        start = BirthdayDimens.BackTouchInset,
                        top = BirthdayDimens.BackTouchInset,
                    ),
            )
        }
    }
}

@Composable
private fun BirthdayShareImageRenderer(
    name: String,
    age: BirthdayAge,
    photoPath: String?,
    theme: BirthdayTheme,
    contentPadding: PaddingValues,
    reserveShareSpace: Boolean,
    shareButtonHeight: Dp,
    layoutHeight: Dp,
    windowWidth: Dp,
    windowHeight: Dp,
    scrollOverflowPx: Int,
    shareImageLayer: GraphicsLayer,
    onPhotoReady: (Boolean) -> Unit,
) {
    val density = LocalDensity.current
    val overflow = if (scrollOverflowPx == Int.MAX_VALUE) 0 else scrollOverflowPx
    val exportHeight = windowHeight + with(density) { overflow.toDp() }
    BirthdayArtwork(
        name = name,
        age = age,
        photoPath = photoPath,
        theme = theme,
        contentPadding = contentPadding,
        reserveShareSpace = reserveShareSpace,
        shareButtonHeight = shareButtonHeight,
        scrollState = null,
        layoutHeight = layoutHeight,
        onPhotoBounds = {},
        onShareButtonTop = {},
        modifier =
            Modifier.wrapContentSize(Alignment.TopStart, unbounded = true)
                .requiredSize(windowWidth, exportHeight)
                .clearAndSetSemantics {}
                // Record without drawing on screen; full height preserves content hidden by
                // scrolling.
                .drawWithContent { shareImageLayer.record { this@drawWithContent.drawContent() } },
        onPhotoReady = onPhotoReady,
    )
}

@Composable
private fun BirthdayArtwork(
    name: String,
    age: BirthdayAge,
    photoPath: String?,
    theme: BirthdayTheme,
    contentPadding: PaddingValues,
    reserveShareSpace: Boolean,
    shareButtonHeight: Dp,
    scrollState: ScrollState?,
    layoutHeight: Dp,
    onPhotoBounds: (Rect) -> Unit,
    onShareButtonTop: (Float) -> Unit,
    modifier: Modifier = Modifier,
    onPhotoReady: (Boolean) -> Unit = {},
) {
    Box(modifier.fillMaxSize().background(theme.backgroundColor)) {
        BirthdayBackgroundIllustration(theme)
        Box(Modifier.fillMaxSize().padding(contentPadding)) {
            BirthdayContent(
                name,
                age,
                photoPath,
                theme,
                reserveShareSpace,
                shareButtonHeight,
                onPhotoBounds,
                onShareButtonTop,
                onPhotoReady,
                scrollState,
                layoutHeight,
            )
        }
    }
}

@Composable
private fun BirthdayContent(
    name: String,
    age: BirthdayAge,
    photoPath: String?,
    theme: BirthdayTheme,
    reserveShareSpace: Boolean,
    shareButtonHeight: Dp,
    onPhotoBounds: (Rect) -> Unit,
    onShareButtonTop: (Float) -> Unit,
    onPhotoReady: (Boolean) -> Unit,
    scrollState: ScrollState?,
    layoutHeight: Dp,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val availableWidth = this.maxWidth
        val photoDiameter =
            (availableWidth - BirthdayDimens.PhotoSideClearance * 2).coerceIn(
                1.dp,
                BirthdayDimens.PhotoDiameter,
            )
        val logoHeight = BirthdayDimens.LogoHeight
        // Preserve the established photo/header placement; only Share uses the bottom anchor.
        val photoTop =
            (layoutHeight / 2 -
                    BirthdayDimens.PhotoVerticalCenterOffset -
                    if (theme == BirthdayTheme.PELICAN) BirthdayDimens.PelicanPhotoOffset else 0.dp)
                .coerceAtLeast(BirthdayDimens.MinimumHeadingHeight)
        val logoBottom = photoTop + photoDiameter + BirthdayDimens.PhotoToLogoSpacing + logoHeight
        val logoToShareGap =
            (layoutHeight - NanitDimens.BottomActionClearance - shareButtonHeight - logoBottom)
                .coerceAtLeast(NanitDimens.BottomActionClearance)
        val spaceBelowLogo =
            if (reserveShareSpace)
                logoToShareGap + shareButtonHeight + NanitDimens.BottomActionClearance
            else NanitDimens.ScreenPadding
        val density = LocalDensity.current
        val contentModifier = Modifier.fillMaxWidth()
        val scrollingModifier =
            if (scrollState == null) contentModifier
            else contentModifier.verticalScroll(scrollState)
        Column(
            scrollingModifier.padding(horizontal = NanitDimens.ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier.fillMaxWidth()
                    .heightIn(min = photoTop)
                    .padding(
                        top = BirthdayDimens.HeadingEdgePadding,
                        bottom = BirthdayDimens.HeadingEdgePadding,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                BirthdayHeading(name, age)
            }
            Box(
                Modifier.size(photoDiameter).onGloballyPositioned {
                    onPhotoBounds(it.boundsInRoot())
                }
            ) {
                BirthdayPhoto(photoPath, theme, photoDiameter, onPhotoReady)
            }
            Spacer(Modifier.height(BirthdayDimens.PhotoToLogoSpacing))
            Image(
                painter = painterResource(R.drawable.nanit_logo),
                contentDescription = stringResource(R.string.nanit_logo_description),
                modifier =
                    Modifier.size(BirthdayDimens.LogoWidth, logoHeight).onGloballyPositioned {
                        onShareButtonTop(
                            it.boundsInRoot().bottom + with(density) { logoToShareGap.toPx() }
                        )
                    },
            )
            Spacer(Modifier.height(spaceBelowLogo))
        }
    }
}

@Composable
private fun BirthdayBackgroundIllustration(theme: BirthdayTheme) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        // Recorded layer is 459 wide within a 360-wide frame; clip its side overflow.
        val illustrationWidth = this.maxWidth * BirthdayDimens.BackgroundWidthRatio
        Image(
            painter = painterResource(theme.backgroundResource),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier =
                Modifier.align(Alignment.BottomCenter)
                    .requiredWidth(illustrationWidth)
                    .aspectRatio(BirthdayDimens.BackgroundAspectRatio),
        )
    }
}

@Composable
private fun BirthdayHeading(name: String, age: BirthdayAge) {
    val title = stringResource(R.string.birthday_heading, name.trim().uppercase())
    val ageLabel =
        androidx.compose.ui.res.pluralStringResource(
            if (age.inYears) R.plurals.birthday_years_old else R.plurals.birthday_months_old,
            age.number,
        )
    Column(
        modifier = Modifier.widthIn(max = BirthdayDimens.HeadingMaxWidth),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            modifier = Modifier.widthIn(max = BirthdayDimens.NameMaxWidth),
            fontSize = BirthdayDimens.HeadingTextSize,
            lineHeight = BirthdayDimens.HeadingLineHeight,
            letterSpacing = BirthdayDimens.HeadingLetterSpacing,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = NanitColors.BirthdayForeground,
        )
        Spacer(Modifier.height(BirthdayDimens.HeadingToNumberSpacing))
        BirthdayNumber(age.number)
        Spacer(Modifier.height(BirthdayDimens.NumberToAgeLabelSpacing))
        Text(
            text = ageLabel,
            fontSize = BirthdayDimens.AgeLabelTextSize,
            letterSpacing = BirthdayDimens.AgeLabelLetterSpacing,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = NanitColors.BirthdayForeground,
        )
    }
}

@Composable
private fun BirthdayNumber(number: Int) {
    val resources =
        listOf(
            R.drawable.birthday_number_0,
            R.drawable.birthday_number_1,
            R.drawable.birthday_number_2,
            R.drawable.birthday_number_3,
            R.drawable.birthday_number_4,
            R.drawable.birthday_number_5,
            R.drawable.birthday_number_6,
            R.drawable.birthday_number_7,
            R.drawable.birthday_number_8,
            R.drawable.birthday_number_9,
            R.drawable.birthday_number_10,
            R.drawable.birthday_number_11,
            R.drawable.birthday_number_12,
        )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BirthdayDimens.SwirlToNumberSpacing),
    ) {
        Image(
            painterResource(R.drawable.birthday_swirl_left),
            null,
            Modifier.size(BirthdayDimens.SwirlWidth, BirthdayDimens.SwirlHeight),
        )
        Row(
            Modifier.height(BirthdayDimens.NumberHeight).clearAndSetSemantics {
                contentDescription = number.toString()
            },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Use the supplied compound artwork through 12; compose digits for larger ages.
            val artwork =
                if (number <= 12) listOf(resources[number])
                else number.toString().map { resources[it.digitToInt()] }
            artwork.forEach { resource ->
                val painter = painterResource(resource)
                Image(
                    painter,
                    null,
                    Modifier.height(BirthdayDimens.NumberHeight)
                        .width(
                            BirthdayDimens.NumberHeight * painter.intrinsicSize.width /
                                painter.intrinsicSize.height
                        ),
                )
            }
        }
        Image(
            painterResource(R.drawable.birthday_swirl_right),
            null,
            Modifier.size(BirthdayDimens.SwirlWidth, BirthdayDimens.SwirlHeight),
        )
    }
}

@Composable
private fun BirthdayPhoto(
    photoPath: String?,
    theme: BirthdayTheme,
    diameter: Dp,
    onReady: (Boolean) -> Unit,
) {
    val modifier =
        Modifier.size(diameter)
            .clip(CircleShape)
            .background(theme.placeholderColor)
            .border(
                BirthdayDimens.PhotoBorderWidth,
                theme.borderColor.copy(alpha = 0.78f),
                CircleShape,
            )
    if (photoPath == null) {
        Image(
            painter = painterResource(theme.placeholderResource),
            contentDescription = stringResource(R.string.baby_placeholder_description),
            modifier = modifier,
        )
    } else {
        AsyncImage(
            model = photoPath,
            onSuccess = { onReady(true) },
            onError = { onReady(true) },
            contentDescription = stringResource(R.string.baby_photo_description),
            modifier = modifier,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(theme.placeholderResource),
            error = painterResource(theme.placeholderResource),
        )
    }
}

@Composable
private fun BirthdayCameraButton(
    theme: BirthdayTheme,
    diameter: Dp,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    // Center the badge's 48 dp touch target on the ring at 45°, independently of its 36 dp artwork.
    val center = diameter / 2
    val ringRadius = center - BirthdayDimens.PhotoBorderWidth / 2
    val diagonal = ringRadius / kotlin.math.sqrt(2f)
    val touchRadius = NanitDimens.MinimumTouchTarget / 2
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier =
            Modifier.offset(center + diagonal - touchRadius, center - diagonal - touchRadius)
                .size(NanitDimens.MinimumTouchTarget),
    ) {
        Image(
            painterResource(theme.cameraResource),
            contentDescription = stringResource(R.string.choose_picture),
            modifier = Modifier.size(BirthdayDimens.CameraIconSize),
        )
    }
}

@Composable
private fun BirthdayShareButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier =
            modifier
                .heightIn(min = NanitDimens.MinimumTouchTarget)
                .widthIn(min = BirthdayDimens.ShareMinimumWidth),
        shape = RoundedCornerShape(BirthdayDimens.ShareCornerRadius),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = NanitColors.ShareBackground,
                contentColor = Color.White,
                disabledContainerColor = NanitColors.ShareBackground,
                disabledContentColor = Color.White.copy(alpha = 0.7f),
            ),
    ) {
        Text(
            stringResource(R.string.share_the_news),
            fontSize = NanitDimens.ActionTextSize,
            lineHeight = NanitDimens.ActionLineHeight,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.sp,
        )
        Spacer(Modifier.width(BirthdayDimens.ShareIconSpacing))
        Image(
            painterResource(R.drawable.birthday_share),
            null,
            Modifier.size(NanitDimens.StandardIconSize),
        )
    }
}

@Composable
private fun BirthdayBackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val label = stringResource(R.string.close_birthday)
    IconButton(
        onClick = onClick,
        modifier =
            modifier.size(NanitDimens.MinimumTouchTarget).semantics { contentDescription = label },
    ) {
        // The export is the inner Union; restore its measured padding in the 24 dp Figma slot.
        Box(Modifier.size(NanitDimens.StandardIconSize)) {
            Image(
                painter = painterResource(R.drawable.birthday_back),
                contentDescription = null,
                modifier =
                    Modifier.offset(
                            BirthdayDimens.BackGlyphOffsetX,
                            BirthdayDimens.BackGlyphOffsetY,
                        )
                        .size(BirthdayDimens.BackGlyphWidth, BirthdayDimens.BackGlyphHeight),
            )
        }
    }
}

@Preview(
    name = "Fox",
    group = "Birthday themes",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun FoxBirthdayPreview() {
    MaterialTheme {
        BirthdayScreen(
            "Alex",
            LocalDate.of(2025, 1, 15),
            null,
            {},
            onChoosePhoto = {},
            onShare = {},
            theme = BirthdayTheme.FOX,
            today = LocalDate.of(2025, 2, 15),
        )
    }
}

@Preview(
    name = "Elephant",
    group = "Birthday themes",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun ElephantBirthdayPreview() {
    MaterialTheme {
        BirthdayScreen(
            "Alex",
            LocalDate.of(2025, 1, 15),
            null,
            {},
            onChoosePhoto = {},
            onShare = {},
            theme = BirthdayTheme.ELEPHANT,
            today = LocalDate.of(2025, 11, 15),
        )
    }
}

@Preview(
    name = "Pelican",
    group = "Birthday themes",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun PelicanBirthdayPreview() {
    MaterialTheme {
        BirthdayScreen(
            "Alex",
            LocalDate.of(2025, 1, 15),
            null,
            {},
            onChoosePhoto = {},
            onShare = {},
            theme = BirthdayTheme.PELICAN,
            today = LocalDate.of(2026, 1, 15),
        )
    }
}

@Preview(
    name = "Long name",
    group = "Birthday header",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun LongNameBirthdayPreview() {
    MaterialTheme {
        BirthdayScreen(
            "Cristiano Ronaldo",
            LocalDate.of(2025, 1, 15),
            null,
            {},
            onChoosePhoto = {},
            onShare = {},
            today = LocalDate.of(2025, 12, 15),
        )
    }
}

@Preview(
    name = "Small screen, large text",
    group = "Birthday header",
    showBackground = true,
    widthDp = 320,
    heightDp = 568,
    fontScale = 1.5f,
)
@Composable
private fun SmallScreenBirthdayPreview() {
    MaterialTheme {
        BirthdayScreen(
            "Cristiano Ronaldo",
            LocalDate.of(2025, 1, 15),
            null,
            {},
            onChoosePhoto = {},
            onShare = {},
            today = LocalDate.of(2025, 12, 15),
        )
    }
}
