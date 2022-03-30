/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tuweni.eth.precompiles

import com.sun.jna.ptr.IntByReference
import org.apache.tuweni.bytes.Bytes
import org.apache.tuweni.bytes.Bytes32
import org.apache.tuweni.bytes.MutableBytes
import org.apache.tuweni.crypto.Hash
import org.apache.tuweni.crypto.SECP256K1
import org.hyperledger.besu.nativelib.bls12_381.LibEthPairings
import java.math.BigInteger
import kotlin.experimental.and

/**
 * Contract returning a copy of the input.
 */
class IDPrecompiledContract : PrecompileContract {
  override fun run(input: Bytes): Result = Result(gas = input.size().toLong() / 32 * 3 + 15, output = input.copy())
}

/**
 * Contract recovering a hash from a signature.
 */
class ECRECPrecompiledContract : PrecompileContract {
  override fun run(input: Bytes): Result {
    val padded = if (input.size() < 128)
      Bytes.wrap(input.toArrayUnsafe(), 0, 128)
    else
      input
    val h = padded.slice(0, 32) as Bytes32
    val v = padded.slice(32, 32)
    val r = padded.slice(64, 32)
    val s = padded.slice(96, 32)
    if (v.numberOfLeadingZeroBytes() != 31) {
      return Result(3000, Bytes.EMPTY)
    }
    try {
      val signature = SECP256K1.Signature.create(v.get(31), r.toUnsignedBigInteger(), s.toUnsignedBigInteger())
      val pkey = SECP256K1.PublicKey.recoverFromHashAndSignature(h, signature) ?: return Result(3000, Bytes.EMPTY)
      val hashed: Bytes32 = Hash.keccak256(pkey.bytes())
      return Result(3000, Bytes32.leftPad(hashed.slice(12)))
    } catch (e: IllegalArgumentException) {
      return Result(3000, Bytes.EMPTY)
    }
  }
}

/**
 * SHA-2-256 Hash precompile contract.
 */
class Sha256PrecompiledContract : PrecompileContract {
  override fun run(input: Bytes): Result {
    return Result(12 * input.size().toLong() / 32 + 60, Hash.sha2_256(input))
  }
}

/**
 * RIPEMD160 precompile contract.
 */
class RIPEMD160PrecompiledContract : PrecompileContract {
  override fun run(input: Bytes): Result {
    return Result(120 * input.size().toLong() / 32 + 600, Hash.digestUsingAlgorithm(input, "RIPEMD160"))
  }
}

class ModExpPrecmpileContract : PrecompileContract {
  companion object {
    val BASE_OFFSET = BigInteger.valueOf(96)

    fun extractParameter(input: Bytes, offset: BigInteger, length: Int): BigInteger {
      return if (BigInteger.valueOf(input.size().toLong()).compareTo(offset) <= 0) {
        BigInteger.ZERO
      } else input.slice(offset.toInt(), length).toUnsignedBigInteger()
    }
  }

  override fun run(input: Bytes): Result {
    val padded = if (input.size() < 96)
      Bytes.wrap(input.toArrayUnsafe(), 0, 96)
    else
      input
    val baseLength = padded.slice(0, 32).toUnsignedBigInteger()
    val exponentLength = padded.slice(32, 32).toUnsignedBigInteger()
    val modulusLength = padded.slice(64, 32).toUnsignedBigInteger()

    if (baseLength == BigInteger.ZERO && modulusLength == BigInteger.ZERO) {
      return Result(0, Bytes.EMPTY)
    }
    val exponentOffset = BASE_OFFSET.add(baseLength)
    val modulusOffset = exponentOffset.add(exponentLength)
    val base = extractParameter(input, BASE_OFFSET, baseLength.toInt())
    val exp = extractParameter(input, exponentOffset, exponentLength.toInt())
    val mod = extractParameter(input, modulusOffset, modulusLength.toInt())

    val modExp = if (mod.compareTo(BigInteger.ZERO) == 0) {
      MutableBytes.EMPTY
    } else {
      Bytes.wrap(base.modPow(exp, mod).toByteArray()).trimLeadingZeros()
    }

    return Result(0, Bytes.wrap(modExp.toArrayUnsafe(), 0, modulusLength.toInt()))
  }
}

class AltBN128PrecompiledContract(private val operation: Byte, val inputLen: Int) : PrecompileContract {

  override fun run(input: Bytes): Result {
    val result = ByteArray(LibEthPairings.EIP196_PREALLOCATE_FOR_RESULT_BYTES)
    val error = ByteArray(LibEthPairings.EIP2537_PREALLOCATE_FOR_ERROR_BYTES)

    val o_len = IntByReference(LibEthPairings.EIP196_PREALLOCATE_FOR_RESULT_BYTES)
    val err_len = IntByReference(LibEthPairings.EIP2537_PREALLOCATE_FOR_ERROR_BYTES)
    val inputSize = Math.min(inputLen, input.size())
    val errorNo: Int = LibEthPairings.eip196_perform_operation(
      operation,
      input.slice(0, inputSize).toArrayUnsafe(),
      inputSize,
      result,
      o_len,
      error,
      err_len
    )
    return if (errorNo == 0) {
      Result(0, Bytes.wrap(result, 0, o_len.getValue()))
    } else {
      Result(0, Bytes.EMPTY)
    }
  }
}

class Blake2BFPrecompileContract : PrecompileContract {
  override fun run(input: Bytes): Result {
    if (input.size() != 213) {
      // Input is malformed, we can't read the number of rounds.
      // Precompile can't be executed so we set its price to 0.
      return Result(0, Bytes.EMPTY)
    }
    if (input.get(212).and(0xFE.toByte()) != 0x00.toByte()) {
      // Input is malformed, F value can be only 0 or 1
      return Result(0, Bytes.EMPTY)
    }

    val rounds = input.getInt(0)

    return Result(rounds.toLong(), Hash.digestUsingAlgorithm(input, "BLAKE2BF"))
  }
}
