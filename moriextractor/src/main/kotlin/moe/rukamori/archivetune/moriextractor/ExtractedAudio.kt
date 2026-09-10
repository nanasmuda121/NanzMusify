/*
 * Copyright (C) 2026 morieeattonkatsu
 *
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * This file is part of moriextractor.
 *
 * moriextractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3.
 *
 * This program is distributed without any warranty; without even the
 * implied warranty of merchantability or fitness for a particular purpose.
 *
 * A separate commercial license may be obtained from the copyright holder.
 */

package moe.rukamori.archivetune.moriextractor

data class ExtractedAudio(
    val success: Boolean,
    val valid: Boolean,
    val cached: Boolean,
    val serverVersion: String,
    val title: String?,
    val thumbnail: String?,
    val streamUrl: String,
    val streamPath: String,
    val streamExpiresAt: Long,
    val formatId: String?,
    val ext: String?,
    val acodec: String?,
    val mimeType: String?,
    val error: String?,
)
