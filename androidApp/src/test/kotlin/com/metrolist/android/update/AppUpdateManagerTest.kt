package com.metrolist.android.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateManagerTest {
    @Test
    fun apkSelectionOnlyPicksTheOfficialRlnRelease() {
        assertEquals(
            "RLN-Network-2.0.0.apk",
            releaseWithAssets("RLN-Network-2.0.0.apk", "Metrolist.apk").apk?.name,
        )
        assertEquals(
            "RLN-Network-2.0.apk",
            releaseWithAssets("RLN-Network-2.0.apk").apk?.name,
        )
        assertEquals(
            "RLN-Network-2.0.1.apk",
            releaseWithAssets("RLN-Network-2.0.1.apk", version = "2.0.1").apk?.name,
        )
    }

    @Test
    fun apkSelectionRejectsWrongVersionMetrolistAndUnsafeBuilds() {
        assertNull(releaseWithAssets("Metrolist.apk", "metrolist-android-2.0.0.apk").apk)
        assertNull(releaseWithAssets("RLN-Network-2.0.1.apk").apk)
        assertNull(releaseWithAssets("RLN-Network-2.0.apk", version = "2.0.1").apk)
        assertNull(releaseWithAssets("RLN-Network-2.0.0-debug.apk").apk)
        assertNull(releaseWithAssets("RLN-Network-2.0.0-test.apk").apk)
        assertNull(releaseWithAssets("unknown.apk").apk)
        val incomplete = releaseWithAssets("RLN-Network-2.0.0.apk")
        assertNull(incomplete.copy(assets = incomplete.assets.map { it.copy(state = "new") }).apk)
        assertNull(releaseWithAssets("RLN-Network-2.0.0.apk", "rln-network-2.0.0.apk").apk)
    }

    private fun releaseWithAssets(vararg names: String, version: String = "2.0.0") =
        GitHubRelease(
            tagName = "v$version",
            htmlUrl = "https://github.com/sinisgallof-pixel/RLN-Network/releases/tag/v$version",
            assets = names.map {
                GitHubReleaseAsset(
                    name = it,
                    state = "uploaded",
                    size = 100,
                    downloadUrl = "https://github.com/sinisgallof-pixel/RLN-Network/releases/download/v$version/$it",
                )
            },
        )

    @Test
    fun apkSignerMustMatchCurrentSignerOrProveForwardRotation() {
        val old = "old".encodeToByteArray()
        val current = "current".encodeToByteArray()
        val next = "next".encodeToByteArray()

        assertTrue(AppUpdateManager.signerDigestsMatch(listOf(current), listOf(current), listOf(old, current)))
        assertTrue(AppUpdateManager.signerDigestsMatch(listOf(current), listOf(next), listOf(old, current, next)))
        assertFalse(AppUpdateManager.signerDigestsMatch(listOf(current), listOf(old), listOf(old)))
        assertFalse(AppUpdateManager.signerDigestsMatch(listOf(current), listOf(next), listOf(next)))
        assertTrue(AppUpdateManager.signerDigestsMatch(listOf(old, current), listOf(current, old), emptyList()))
        assertFalse(AppUpdateManager.signerDigestsMatch(listOf(old, current), listOf(current), listOf(old, current)))
        assertFalse(AppUpdateManager.signerDigestsMatch(emptyList(), listOf(current), listOf(current)))
        assertFalse(AppUpdateManager.signerDigestsMatch(listOf(current), emptyList(), listOf(current)))
        assertFalse(AppUpdateManager.signerDigestsMatch(emptyList(), emptyList(), listOf(current)))
    }
}
