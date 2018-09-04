// Copyright (c) 2018 The headergolf developers
// Distributed under the MIT software license, see the accompanying
// file COPYING or http://www.opensource.org/licenses/mit-license.php.

package io.alc.headergolf

import com.xenomachina.argparser.ArgParser
import io.alc.headergolf.compressor.HeadersCompressor
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class CompressArguments(parser: ArgParser) {
    val input by parser.storing("-i", help = "Input file (uncompressed)")
    val output by parser.storing("-o", help = "Output file (compressed)")
}

fun main(args: Array<String>) {
    val arguments = CompressArguments(ArgParser(args))

    val input = File(arguments.input)

    if (!input.exists()) {
        System.err.println("Input file doesn't exist")
        System.exit(1)
    }

    if (input.length() % 80 != 0L) {
        System.err.println("Invalid input file size (mod 80 != 0)")
        System.exit(1)
    }

    val headers = ByteArray(input.length().toInt())

    FileInputStream(input).use {
        it.read(headers)
    }

    val parsedHeaders = Header.listFromBytes(headers)

    val compressor = HeadersCompressor()

    val compressed = compressor.compress(parsedHeaders)
    val decompressed = compressor.decompress(compressed)

    if (decompressed.size != parsedHeaders.size) {
        System.err.println("Invalid number of headers, expected ${parsedHeaders.size}, got ${decompressed.size}")
        System.exit(1)
    }

    if (decompressed != parsedHeaders) {
        System.err.println("decompress(compress(headers)) != headers")
        System.exit(1)
    }

    println("Uncompressed: ${headers.size} bytes")
    println("  Compressed: ${compressed.size} bytes")

    val ratio = headers.size.toDouble() / compressed.size
    println("Compression ratio is %.2f:1".format(ratio))

    val outputFile = File(arguments.output)
    FileOutputStream(outputFile).use { stream ->
        stream.write(compressed)
        stream.flush()
    }
}