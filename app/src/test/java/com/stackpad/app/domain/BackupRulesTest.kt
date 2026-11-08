package com.stackpad.app.domain

import com.stackpad.app.domain.backup.BackupRules
import com.stackpad.app.domain.backup.MergeDecision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

class BackupRulesTest {
    @Test fun newNoteIsAdded() = assertEquals(MergeDecision.ADD, BackupRules.decide(null, 5))
    @Test fun newerBackupReplaces() = assertEquals(MergeDecision.REPLACE, BackupRules.decide(5, 6))
    @Test fun olderBackupIsSkipped() = assertEquals(MergeDecision.SKIP, BackupRules.decide(6, 5))
    @Test fun equalTimestampIsSkipped() = assertEquals(MergeDecision.SKIP, BackupRules.decide(5, 5))

    @Test fun acceptsAppGeneratedNames() {
        assertTrue(BackupRules.isSafeMediaName("img_123e4567-e89b-12d3-a456-426614174000.jpg"))
        assertTrue(BackupRules.isSafeMediaName("aud_abc.m4a"))
    }

    @Test fun rejectsPathTricks() {
        listOf("", "../x.jpg", "a/b.jpg", "a\\b.jpg", "..", ".hidden", "a..b", "/etc/passwd", "a b.jpg", "x".repeat(101))
            .forEach { assertFalse("'$it' should be rejected", BackupRules.isSafeMediaName(it)) }
    }

    @Test fun mediaEntryParsing() {
        assertEquals("img_1.jpg", BackupRules.mediaNameOf("media/img_1.jpg"))
        assertNull(BackupRules.mediaNameOf("media/../evil"))
        assertNull(BackupRules.mediaNameOf("media/sub/x.jpg"))
        assertNull(BackupRules.mediaNameOf("other/img_1.jpg"))
        assertNull(BackupRules.mediaNameOf("backup.json"))
    }

    @Test fun entryNameRoundTrips() {
        assertEquals("img_1.jpg", BackupRules.mediaNameOf(BackupRules.mediaEntryName("img_1.jpg")))
    }

    @Test fun versionSupport() {
        assertTrue(BackupRules.isSupportedVersion(1))
        assertFalse(BackupRules.isSupportedVersion(0))
        assertFalse(BackupRules.isSupportedVersion(BackupRules.FORMAT_VERSION + 1))
    }

    @Test fun fileNameUsesLocalDate() {
        // 2026-11-08 20:00 UTC is already 2026-11-09 in Manila.
        val utc = 1_794_168_000_000L
        assertEquals("stackpad-backup-2026-11-08.zip", BackupRules.fileName(utc, ZoneId.of("UTC")))
        assertEquals("stackpad-backup-2026-11-09.zip", BackupRules.fileName(utc, ZoneId.of("Asia/Manila")))
    }
}
