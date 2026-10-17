// SPDX-License-Identifier: AGPL-3.0-or-later
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Affero General Public License for more details.
//
package com.soreverse.mcp.mcp

import com.soreverse.mcp.core.MemoryGuard
import com.soreverse.mcp.core.SelfArtifactGuard
import java.io.File
import org.json.JSONObject

/**
 * Guards shared by the ported APK toolchain.
 *
 * The upstream 1.0.22 baseline protects its own artifact in two places: the
 * bridged APK-MCP calls and `so_open` / `build_so` (see [SelfArtifactGuard]).
 * The ported APK tools accept arbitrary local paths, including the path of the
 * installed SOMCP APK itself, so they need the same treatment — otherwise
 * `jadx_decompile` or `apk_extract` becomes a way to dump the app's own dex,
 * resources and bundled `.so` files, which is exactly what the guard exists to
 * prevent.
 *
 * [refuseSelfTarget] returns the standard refusal payload when [args] names
 * SOMCP's own artifact, or `null` when the call may proceed.
 */
internal object ApkToolGuards {

    /**
     * Largest input the ported tools accept, matching APKEditor / jadx practical
     * ceilings on a phone. A 512 MiB APK becomes multiple GiB inside jadx's
     * class tree, so the guard refuses at the door instead of dying mid-way.
     */
    const val MAX_INPUT_BYTES = 512L * 1024L * 1024L

    /**
     * Every argument that can carry a target path in the APK toolchain. Kept as
     * one list so a new tool cannot silently skip the check by naming its path
     * parameter something else.
     */
    private val PATH_ARGS = listOf(
        "path", "filePath", "inputPath", "apkPath",
        "inputApk", "input", "target", "soPath", "xmlPath", "smaliDir"
    )

    /**
     * True when [path] resolves to SOMCP's own APK or one of its bundled
     * libraries. Uses the same judges as the rest of the app so a copied-out
     * library is still recognised by content.
     */
    fun isSelfTarget(context: android.content.Context, path: String): Boolean =
        path.isNotBlank() && runCatching { SelfArtifactGuard.isSelfArtifact(context, path) }.getOrDefault(false)

    /** Refusal payload for a call that targets SOMCP itself, or `null` to proceed. */
    fun refuseSelfTarget(context: android.content.Context, tool: String, args: JSONObject): JSONObject? {
        val hit = PATH_ARGS.firstNotNullOfOrNull { key ->
            val value = args.optString(key, "")
            value.takeIf { it.isNotBlank() && isSelfTarget(context, it) }
        } ?: return null
        return SelfArtifactGuard.forbidden(
            hit,
            "SOMCP refuses to run '$tool' against its own APK or bundled native library"
        )
    }

    /**
     * Refuse inputs larger than the caller can safely materialise.
     *
     * The ported handlers all take a `File` and feed it to a JVM library that
     * reads it whole (jadx, DexKit, APKEditor). Unidbg-class heap guards are not
     * enough for these: the library allocates several times the file size, so the
     * refusal has to happen before the file is opened.
     */
    fun tooLarge(file: File, limitBytes: Long, tool: String): JSONObject? {
        if (!file.isFile) return null
        val size = file.length()
        if (size <= limitBytes) return null
        return com.soreverse.mcp.core.err(
            "INPUT_TOO_LARGE",
            "$tool refuses an input of ${size / (1024 * 1024)} MiB; the limit is ${limitBytes / (1024 * 1024)} MiB",
            "path",
            file.absolutePath
        )
    }

    /** Heap guard for a file about to be fully materialised by a JVM library. */
    fun ensureHeap(file: File, tool: String): JSONObject? {
        if (!file.isFile) return null
        return runCatching { MemoryGuard.ensureAnalysisMemory(file.length(), "$tool on ${file.name}") }
            .exceptionOrNull()
            ?.let { e ->
                com.soreverse.mcp.core.err(
                    "INSUFFICIENT_MEMORY",
                    e.message ?: "Not enough heap to load ${file.name}",
                    "path",
                    file.absolutePath
                )
            }
    }
}
