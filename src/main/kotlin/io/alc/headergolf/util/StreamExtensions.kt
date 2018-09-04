// Copyright (c) 2018 The headergolf developers
// Distributed under the MIT software license, see the accompanying
// file COPYING or http://www.opensource.org/licenses/mit-license.php.

package io.alc.headergolf.util

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

fun ByteArrayInputStream.readUint32(): Long {
    val data = ByteArray(4)
    read(data)
    return data[0].toLong() and 0xff or
        (data[1].toLong() and 0xff shl 8) or
        (data[2].toLong() and 0xff shl 16) or
        (data[3].toLong() and 0xff shl 24)
}

fun ByteArrayOutputStream.writeUint32(uint: Long) {
    write(0xff and uint.toInt())
    write(0xff and (uint.toInt() shr 8))
    write(0xff and (uint.toInt() shr 16))
    write(0xff and (uint.toInt() shr 24))
}

fun ByteBuffer.readUint32(): Long {
    return get().toLong() and 0xff or
        (get().toLong() and 0xff shl 8) or
        (get().toLong() and 0xff shl 16) or
        (get().toLong() and 0xff shl 24)
}
