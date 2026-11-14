/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.android.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource as androidPainterResource
import androidx.compose.ui.unit.dp
import com.metrolist.android.BuildConfig
import com.metrolist.android.LocalMiniPlayerBottomInset
import com.metrolist.android.R
import com.metrolist.android.playback.LocalPlayerConnection
import com.metrolist.android.playback.resolveIncomingPlaybackSong
import com.metrolist.shared.discord.openDiscordUrl
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.rln_brand_name
import com.metrolist.shared.generated.resources.rln_open_telegram
import com.metrolist.shared.generated.resources.youtube_music
import com.metrolist.shared.state.GlobalYouTubeRepository
import com.metrolist.shared.ui.screens.AboutCommunityLinkKind
import com.metrolist.shared.ui.screens.AboutScreen
import com.metrolist.shared.ui.settings.LocalSettingsPageHasTopBar
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun AndroidAboutScreen(
    onNavigateBack: () -> Unit,
    onDevtoolsUnlocked: () -> Unit = {},
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val miniPlayerInset = LocalMiniPlayerBottomInset.current
    val playerConnection = LocalPlayerConnection.current
    val coroutineScope = rememberCoroutineScope()
    val youtubeMusicLabel = stringResource(Res.string.youtube_music)

    AboutScreen(
        logo = androidPainterResource(R.drawable.rln_network_logo),
        appName = stringResource(Res.string.rln_brand_name),
        version = BuildConfig.VERSION_NAME,
        isDebug = BuildConfig.DEBUG,
        tintLogo = false,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
        bottomInset = miniPlayerInset,
        onNavigateBack = onNavigateBack.takeUnless { LocalSettingsPageHasTopBar.current },
        onOpenCommunityLink = { link ->
            if (link.kind == AboutCommunityLinkKind.DISCORD) {
                runCatching { context.openDiscordUrl(link.url) }
            } else {
                runCatching { uriHandler.openUri(link.url) }
            }
        },
        onDevtoolsUnlocked = onDevtoolsUnlocked,
        afterLicense = {
            Card(
                onClick = { runCatching { uriHandler.openUri("https://t.me/RadioMessenger_bot") } },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = androidPainterResource(R.drawable.rln_network_logo),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                    )
                    Text(stringResource(Res.string.rln_open_telegram))
                }
            }
        },
        onPlayFavoriteSong =
            playerConnection?.let { connection ->
                { videoId ->
                    coroutineScope.launch {
                        val song =
                            resolveIncomingPlaybackSong(
                                videoId = videoId,
                                fallbackArtist = youtubeMusicLabel,
                                loadMetadata = GlobalYouTubeRepository.instance::getSongMetadata,
                                loadPlayerInformation = GlobalYouTubeRepository.instance::getSongPlayerInformation,
                            )
                        connection.playSong(song, "")
                    }
                }
            },
    )
}
