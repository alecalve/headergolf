// Copyright (c) 2018 The headergolf developers
// Distributed under the MIT software license, see the accompanying
// file COPYING or http://www.opensource.org/licenses/mit-license.php.

package io.alc.headergolf.compressor

import io.alc.headergolf.Header
import io.alc.headergolf.util.readUint32
import io.alc.headergolf.util.writeUint32
import me.lemire.integercompression.differential.IntegratedIntCompressor
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class HeadersCompressor {
    private val intCompressor = IntegratedIntCompressor()

    /**
     * Compression is achieved using the following heuristics:
     *     - previous block hash doesn't need to be stored as it can be computed
     *     from the previous header. Since the very first header's previous hash
     *     is zero-filled, we don't even need to store the first one:
     *         prevHashN = sha256(sha256(headerN-1))
     *         prevHash0 = 0x000000....0000000000000
     *
     *     - since version numbers are mostly 1 or 2, they are compressed using
     *     FastPFOR
     *
     */
    fun compress(headers: List<Header>): ByteArray {
        val versions = headers.map { it.version }

        return ByteArrayOutputStream().use { stream ->
            var prevHash = ByteArray(32, { 0.toByte() })

            // Writing how many headers we store
            stream.writeUint32(headers.size.toLong())

            headers.forEach { header ->
                // Sanity check: are we iterating in the correct order
                assert(header.previousBlock.contentEquals(prevHash))
                prevHash = header.hash

                stream.writeUint32(header.time.toLong())
                stream.writeUint32(header.bits.toLong())
                stream.writeUint32(header.nonce.toLong())
                stream.write(header.merkleRoot)
            }

            writeInts(versions.toIntArray(), stream)

            stream.toByteArray()
        }
    }

    /**
     * The compressed data is as follows:
     *     - nHeaders: 4 bytes int
     *     - list of :
     *         - uInt32 bits
     *         - uInt32 nonce
     *         - 32 bytes merkle root
     *
     *     - compressed versions
     *     - compressed timestamps
     *
     */
    fun decompress(headers: ByteArray): List<Header> {
        return ByteArrayInputStream(headers).use { stream ->
            val nHeaders = stream.readUint32().toInt()

            // First, recover the uncompressed data
            val partialHeaders = (0 until nHeaders).map {
                val time = stream.readUint32()
                val bits = stream.readUint32()
                val nonce = stream.readUint32()
                val merkleRoot = ByteArray(32)
                stream.read(merkleRoot)

                Header(
                    -1,
                    ByteArray(32),
                    merkleRoot,
                    time.toInt(),
                    bits.toInt(),
                    nonce.toInt()
                )
            }

            // now recover the versions
            val versions = readInts(stream)
            assert(versions.size == nHeaders)

            // prevHash0 = 0x000000....0000000000000
            var prevHash = ByteArray(32, { 0.toByte() })

            partialHeaders.mapIndexed { index, header ->
                val recovered = header.copy(
                    version = versions[index],
                    previousBlock = prevHash
                )

                // prevHashN = sha256(sha256(headerN-1))
                prevHash = recovered.hash
                recovered
            }
        }
    }

    /**
     * Writes a list of ints as a length-prefixed FastPFOR compressed list.
     */
    private fun writeInts(ints: IntArray, stream: ByteArrayOutputStream) {
        val compressed = intCompressor.compress(ints)
        stream.writeUint32(compressed.size.toLong())
        compressed.forEach {
            stream.writeUint32(it.toLong())
        }
    }

    /**
     * Reads a list written with writeInts
     */
    private fun readInts(stream: ByteArrayInputStream): IntArray {
        val nItems = stream.readUint32()
        val compressed = (0 until nItems).map {
            stream.readUint32().toInt()
        }

        return intCompressor.uncompress(compressed.toIntArray())
    }
}