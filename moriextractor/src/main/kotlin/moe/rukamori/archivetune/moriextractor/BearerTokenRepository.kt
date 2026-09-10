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

import java.util.concurrent.atomic.AtomicReference

interface BearerTokenRepository {
    fun getToken(): String?

    fun updateToken(token: String)

    fun clearToken()
}

class InMemoryBearerTokenRepository(
    initialToken: String? = null,
) : BearerTokenRepository {
    private val token = AtomicReference(initialToken.normalizedToken())

    override fun getToken(): String? = token.get()

    override fun updateToken(token: String) {
        val normalizedToken = requireNotNull(token.normalizedToken()) { "Bearer token must not be blank" }
        this.token.set(normalizedToken)
    }

    override fun clearToken() {
        token.set(null)
    }
}

private fun String?.normalizedToken(): String? = this?.trim()?.takeIf(String::isNotEmpty)
