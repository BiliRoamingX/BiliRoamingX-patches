package app.revanced.patches.bilibili.misc.json.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object CardClickProcessorFingerprint : MethodFingerprint(
    strings = listOf("action:feed:dislike_reason", "action:feed:dislike_toast")
)
