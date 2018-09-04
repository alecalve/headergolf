// Copyright (c) 2018 The headergolf developers
// Distributed under the MIT software license, see the accompanying
// file COPYING or http://www.opensource.org/licenses/mit-license.php.

package io.alc.headergolf.util

import java.security.MessageDigest

fun sha256Twice(data: ByteArray): ByteArray {
    val digest = MessageDigest.getInstance("SHA-256")
    digest.update(data)
    return digest.digest(digest.digest())
}