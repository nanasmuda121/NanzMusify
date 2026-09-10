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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class BackendExtractorResponse(
    val success: Boolean = false,
    val valid: Boolean = false,
    val cached: Boolean = false,
    @SerialName("server_version")
    val serverVersion: String? = null,
    val title: String? = null,
    val thumbnail: String? = null,
    @SerialName("stream_url")
    val streamUrl: String? = null,
    @SerialName("stream_path")
    val streamPath: String? = null,
    @SerialName("stream_expires_at")
    val streamExpiresAt: Long? = null,
    @SerialName("format_id")
    val formatId: String? = null,
    val ext: String? = null,
    val acodec: String? = null,
    @SerialName("mime_type")
    val mimeType: String? = null,
    val error: String? = null,
)
