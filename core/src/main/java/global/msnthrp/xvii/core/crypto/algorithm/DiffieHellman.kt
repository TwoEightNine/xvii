/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package global.msnthrp.xvii.core.crypto.algorithm

import global.msnthrp.xvii.core.crypto.safeprime.entity.SafePrime
import java.math.BigInteger
import java.security.SecureRandom

/**
 * usage:
 * <code>
 *      // as generator
 *      val dh = DiffieHellman()
 *      val data = dh.getData()
 *      // send data to other
 *      // receive other public nonce
 *      dh.publicOther = publicOther
 *      val sharedKey = dh.key
 *
 *
 *      // as receiver
 *      // other data received
 *      val dh = DiffeHellman(data)
 *      val publicOwn = dh.publicOwn
 *      // send publicOwn to other
 *      val key = dh.key
 *      // shared key
 *
 * </code>
 */
class DiffieHellman {

    private var privateOwn = BigInteger.ONE
    private var generator = BigInteger.ONE
    private var modulo = BigInteger.ONE

    var publicOwn: BigInteger = BigInteger.ONE
        private set

    /**
     * sets other public nonce, computes shared key
     * used in case of own generating
     */
    var publicOther: BigInteger = BigInteger.ONE
        set(value) {
            field = value
            key = value.modPow(privateOwn, modulo)
        }

    var key: BigInteger = BigInteger.ONE
        private set

    /**
     * generates own private and public nonce
     */
    constructor(safePrime: SafePrime) {
        modulo = BigInteger(safePrime.p)
        generator = BigInteger(safePrime.g)

        privateOwn = BigInteger(BITS - 1, SecureRandom())
        publicOwn = generator.modPow(privateOwn, modulo)
    }

    /**
     * gets generator and other public nonce, generates own private and public nonces,
     * creates shared key
     */
    constructor(otherData: Data) {
        generator = otherData.generator
        modulo = otherData.modulo
        publicOther = otherData.public

        privateOwn = BigInteger(BITS - 1, SecureRandom())
        publicOwn = generator.modPow(privateOwn, modulo)
        key = publicOther.modPow(privateOwn, modulo)
    }

    fun getData() = Data(generator, modulo, publicOwn)

    companion object {
        private const val BITS = 2048
    }

    data class Data(
            val generator: BigInteger,
            val modulo: BigInteger,
            val public: BigInteger
    )
}