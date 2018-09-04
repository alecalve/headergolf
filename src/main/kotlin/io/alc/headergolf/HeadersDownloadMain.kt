// Copyright (c) 2018 The headergolf developers
// Distributed under the MIT software license, see the accompanying
// file COPYING or http://www.opensource.org/licenses/mit-license.php.

package io.alc.headergolf

import io.alc.headergolf.util.BitcoindRestClient
import java.io.File
import java.io.FileOutputStream
import javax.xml.bind.DatatypeConverter

/**
 * To be run using gradle run.
 *
 * Will download the headerchain and dump it to a file.
 *
 * Can be configured with the following JVM arguments:
 *
 *    -Doutput="headers.dat"
 *          path to the file where the headerchain will be written
 *    -DbitcoindHost="localhost"
 *          hostname of the bitcoind instance
 *    -DbitcoindPort="8332"
 *          port of the bitcoind instance
 */
fun main(args: Array<String>) {
    val output: String = System.getProperty("output")
    val bitcoindHost = System.getProperty("bitcoindHost", "localhost")
    val bitcoindPort = System.getProperty("bitcoindPort", "8332").toInt()
    val client = BitcoindRestClient(bitcoindHost, bitcoindPort)

    var hash = "000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f"
    var downloaded: Int
    val batchSize = 2000

    val chunks = mutableListOf<ByteArray>()

    do {
        val headers = client.getNHeadersAfter(hash, batchSize)
        downloaded = headers.size / 80

        val chunk = if (chunks.isEmpty()) {
            headers
        } else {
            headers.drop(80).toByteArray()
        }

        chunks.add(chunk)

        val lastHeader = Header.fromBytes(chunk.takeLast(80).toByteArray())
        hash = DatatypeConverter.printHexBinary(lastHeader.hash.reversedArray()).toLowerCase()
    } while (downloaded == batchSize)

    FileOutputStream(File(output)).use { stream ->
        chunks.forEach {
            stream.write(it)
        }

        stream.flush()
    }
}