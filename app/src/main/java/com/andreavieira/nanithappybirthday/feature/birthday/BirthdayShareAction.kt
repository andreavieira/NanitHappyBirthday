package com.andreavieira.nanithappybirthday.feature.birthday

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import com.andreavieira.nanithappybirthday.R
import com.andreavieira.nanithappybirthday.platform.share.BirthdaySharing
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Exposes image preparation progress and the action that opens Android's Sharesheet. */
internal data class BirthdayShareAction(val isPreparing: Boolean, val launch: () -> Unit)

@Composable
internal fun rememberBirthdayShareAction(
    shareImageLayer: GraphicsLayer,
    padding: PaddingValues,
    onFailure: () -> Unit,
): BirthdayShareAction {
    val context = LocalContext.current
    val density = LocalDensity.current
    val direction = LocalLayoutDirection.current
    val scope = rememberCoroutineScope()
    var preparing by remember { mutableStateOf(false) }
    return BirthdayShareAction(preparing) {
        if (!preparing) {
            preparing = true
            scope.launch {
                try {
                    val systemInsets = with(density) {
                        Rect(
                            padding.calculateLeftPadding(direction).roundToPx(),
                            padding.calculateTopPadding().roundToPx(),
                            padding.calculateRightPadding(direction).roundToPx(),
                            padding.calculateBottomPadding().roundToPx(),
                        )
                    }
                    val imageUri = captureAndSaveBirthdayCard(context, shareImageLayer, systemInsets)
                    context.startActivity(Intent.createChooser(BirthdaySharing.shareIntent(context, imageUri), context.getString(R.string.share_the_news)))
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    onFailure()
                } finally {
                    preparing = false
                }
            }
        }
    }
}

private suspend fun captureAndSaveBirthdayCard(
    context: Context,
    shareImageLayer: GraphicsLayer,
    systemInsets: Rect,
): Uri {
    // Wait for the latest photo/frame before capturing the artwork.
    withFrameNanos { }
    val capturedBitmap = shareImageLayer.toImageBitmap().asAndroidBitmap()
    try {
        return withContext(Dispatchers.IO) {
            val cardBitmap = Bitmap.createBitmap(
                capturedBitmap,
                systemInsets.left,
                systemInsets.top,
                capturedBitmap.width - systemInsets.left - systemInsets.right,
                capturedBitmap.height - systemInsets.top - systemInsets.bottom,
            )
            try {
                // Save a PNG and return its temporary, shareable content URI.
                BirthdaySharing.writeImage(context, cardBitmap)
            } finally {
                // Cropping can return the original bitmap when there are no insets.
                if (cardBitmap !== capturedBitmap) cardBitmap.recycle()
            }
        }
    } finally {
        // Release the captured pixels even if cropping or saving fails.
        capturedBitmap.recycle()
    }
}
