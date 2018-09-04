// Copyright (c) 2018 The headergolf developers
// Distributed under the MIT software license, see the accompanying
// file COPYING or http://www.opensource.org/licenses/mit-license.php.

package io.alc.headergolf

import io.alc.headergolf.util.readUint32
import io.alc.headergolf.util.sha256Twice
import io.alc.headergolf.util.writeUint32
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.Arrays

data class Header(
    val version: Int,
    val previousBlock: ByteArray,
    val merkleRoot: ByteArray,
    val time: Int,
    val bits: Int,
    val nonce: Int
) {
    val hash: ByteArray = sha256Twice(this.toByteArray())

    private fun toByteArray(): ByteArray {
        return ByteArrayOutputStream().use { stream ->
            stream.writeUint32(version.toLong())
            stream.write(previousBlock)
            stream.write(merkleRoot)
            stream.writeUint32(time.toLong())
            stream.writeUint32(bits.toLong())
            stream.writeUint32(nonce.toLong())
            stream.toByteArray()
        }
    }

    // We have to re-implement equals and hashCode
    // because we use have ByteArray members
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Header

        if (version != other.version) return false
        if (!Arrays.equals(previousBlock, other.previousBlock)) return false
        if (!Arrays.equals(merkleRoot, other.merkleRoot)) return false
        if (time != other.time) return false
        if (bits != other.bits) return false
        if (nonce != other.nonce) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version
        result = 31 * result + Arrays.hashCode(previousBlock)
        result = 31 * result + Arrays.hashCode(merkleRoot)
        result = 31 * result + time
        result = 31 * result + bits
        result = 31 * result + nonce
        return result
    }

    companion object {
        fun fromBytes(data: ByteArray): Header {
            if (data.size != 80) {
                throw Exception("Invalid hex size (${data.size} != 80)")
            }

            val headerBuffer = ByteBuffer.wrap(data)
            val version = headerBuffer.readUint32()
            val prevBlock = ByteArray(32)
            headerBuffer.get(prevBlock)
            val merkleRoot = ByteArray(32)
            headerBuffer.get(merkleRoot)
            val time = headerBuffer.readUint32()
            val bits = headerBuffer.readUint32()
            val nonce = headerBuffer.readUint32()

            return Header(
                version.toInt(),
                prevBlock,
                merkleRoot,
                time.toInt(),
                bits.toInt(),
                nonce.toInt()
            )
        }

        fun listFromBytes(headers: ByteArray): List<Header> {
            // Since headers are fixed-size, no need to use length-prefixing
            val headerCount = headers.size / 80
            val buffer = ByteBuffer.wrap(headers)

            return (0 until headerCount).map {
                val header = ByteArray(80)
                buffer.get(header)
                Header.fromBytes(header)
            }
        }
    }
}