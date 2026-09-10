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
data class StreamStatusResponse(
    val success: Boolean,
    val valid: Boolean,
    @SerialName("stream_id")
    val streamId: String? = null,
    @SerialName("stream_expires_at")
    val streamExpiresAt: Long? = null,
    val error: String? = null,
)
