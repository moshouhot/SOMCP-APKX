// SPDX-License-Identifier: AGPL-3.0-only
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
// You should have received a copy of the GNU Affero General Public License
// along with this program. If not, see <https://www.gnu.org/licenses/>.
//
// Guards for the ported APK toolchain. The load-bearing case is visibility:
// lean mode is on by default and only CORE/META/gateway tools are advertised,
// so registering the ported tools as EXTRA in a niche category would ship 13
// features that no MCP client can discover without having called them first —
// which is impossible. The derivative this toolchain came from made exactly
// that mistake.
package com.soreverse.mcp.mcp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ApkToolchainTest {

    /**
     * Every tool ported from the XuanXingNieHe derivative. Kept as an explicit
     * list rather than a category query so that dropping one is a test failure,
     * not a silent catalog shrink.
     */
    private val ported = listOf(
        "jadx_decompile",
        "baksmali_decode",
        "apk_decode",
        "smali_assemble",
        "apk_rebuild",
        "apk_sign",
        "apk_sign_info",
        "dex_search",
        "string_scan",
        "apk_diff",
        "apk_extract",
        "dex_unpack",
        "frida_control"
    )

    @Test
    fun everyPortedToolIsRegistered() {
        ported.forEach {
            assertNotNull("$it is not registered in ToolCatalog.ALL", ToolCatalog.byName[it])
        }
        assertEquals("ported tool list has duplicates", ported.size, ported.distinct().size)
    }

    @Test
    fun everyPortedToolIsAdvertisedInLeanMode() {
        // No popularity map: this is the cold-start list a fresh client sees.
        val lean = ToolCatalog.leanNames().toSet()
        ported.forEach {
            assertTrue("$it is hidden from the default lean list", it in lean)
        }
    }

    @Test
    fun everyPortedToolSurvivesWithPromotionDisabled() {
        val lean = ToolCatalog.registry.leanNames(popularity = null, promotionSlots = 0).toSet()
        ported.forEach {
            assertTrue("$it disappears when promotion is off", it in lean)
        }
    }

    @Test
    fun portedToolsLandInGroupedCatalogCategories() {
        // grouped() only renders categories declared in categoryDescriptions, so a
        // tool in an undeclared category silently vanishes from the UI tool list.
        val declared = ToolCatalogPresentation.categoryDescriptions(false).map { it.first }.toSet()
        val rendered = ToolCatalogPresentation.grouped(false)
            .flatMap { it.second }
            .map { it.first }
            .toSet()
        ported.forEach { name ->
            val category = ToolCatalog.categoryOf(name)
            assertNotNull("$name has no category", category)
            assertTrue("category '$category' is not declared in categoryDescriptions", category in declared)
            assertTrue("$name is missing from the grouped tool list", name in rendered)
        }
    }

    @Test
    fun portedToolsExposeInputSchemas() {
        // A handler without a schema is unusable from an MCP client: the caller
        // has no way to know the argument names.
        ported.forEach { name ->
            val descriptor = ToolCatalog.toolDescriptor(ToolCatalog.byName.getValue(name), includeCategory = true)
            val schema = descriptor.optJSONObject("inputSchema")
            assertNotNull("$name has no inputSchema", schema)
            assertTrue("$name declares an empty inputSchema", schema!!.optJSONObject("properties")!!.length() > 0)
            assertEquals(name, descriptor.optString("name"))
        }
    }

    @Test
    fun portedToolsAreMarkedHeavyWhereTheyLoadWholeFiles() {
        // heavy=true is what serialises the call behind the global gate. The tools
        // that materialise a whole APK/dex in the JVM must not run concurrently
        // with an analysis task or with each other.
        listOf(
            "jadx_decompile",
            "baksmali_decode",
            "apk_rebuild",
            "dex_search",
            "dex_unpack",
            "apk_sign"
        ).forEach { name ->
            assertTrue("$name must be heavy", name in ToolCatalog.heavyNames)
        }
        // apk_sign_info only verifies signatures; keeping it out of the gate keeps
        // the cheap pre-flight check usable while an analysis runs.
        assertTrue("apk_sign_info should stay lightweight", "apk_sign_info" !in ToolCatalog.heavyNames)
    }
}
