// Copyright (c) 2018 The headergolf developers
// Distributed under the MIT software license, see the accompanying
// file COPYING or http://www.opensource.org/licenses/mit-license.php.

package io.alc.headergolf.util

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result

/**
 * A simple bitcoind REST API client, can only fetch headers.
 */
class BitcoindRestClient(private val host: String, private val port: Int) {
    private fun <A : Any, B : Exception> getData(response: Response, result: Result<A, B>): A {
        val (data, error) = result
        if (error == null) {
            return data!!
        } else {
            throw Exception("Error ${response.statusCode}: ${response.responseMessage}")
        }
    }

    private fun doRestRequest(path: String): ByteArray {
        val (_, response, result) = Fuel.get("http://$host:$port$path").response()
        return getData(response, result)
    }

    /**
     * https://github.com/bitcoin/bitcoin/blob/master/doc/REST-interface.md#blockheaders
     */
    fun getNHeadersAfter(block: String, n: Int): ByteArray {
        return doRestRequest("/rest/headers/$n/$block.bin")
    }
}
