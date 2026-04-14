/*
 * Copyright 2026 Web3 Labs Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.web3j.crypto;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.Test;

import org.web3j.crypto.transaction.type.Transaction4844;
import org.web3j.utils.Numeric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Eip7594TransactionTest {

    @Test
    public void testOsakaTransactionEncoding() throws Exception {
        Credentials credentials =
                Credentials.create(
                        "0x45a915e4d060149eb43658c930b24c694a91a91a91a91a91a91a91a91a91a91a");

        List<Blob> blobs = Collections.singletonList(new Blob(new byte[131072]));
        List<Bytes> kzgCommitments = Collections.singletonList(Bytes.wrap(new byte[48]));
        List<List<Bytes>> cellProofs =
                Collections.singletonList(
                        Arrays.asList(Bytes.wrap(new byte[48]), Bytes.wrap(new byte[48])));
        BigInteger wrapperVersion = BigInteger.ONE;

        RawTransaction rawTransaction =
                RawTransaction.createOsakaTransaction(
                        blobs,
                        kzgCommitments,
                        cellProofs,
                        wrapperVersion,
                        1,
                        BigInteger.ZERO,
                        BigInteger.TEN,
                        BigInteger.TEN,
                        BigInteger.valueOf(21000),
                        "0x0000000000000000000000000000000000000000",
                        BigInteger.ZERO,
                        "",
                        BigInteger.TEN,
                        Collections.singletonList(Bytes.wrap(new byte[32])));

        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexTx = Numeric.toHexString(signedMessage);

        RawTransaction decodedTx = TransactionDecoder.decode(hexTx);

        assertTrue(decodedTx.getTransaction() instanceof Transaction4844);
        Transaction4844 decoded4844 = (Transaction4844) decodedTx.getTransaction();

        assertTrue(decoded4844.getWrapperVersion().isPresent());
        assertEquals(wrapperVersion, decoded4844.getWrapperVersion().get());
        assertTrue(decoded4844.getCellProofs().isPresent());
        assertEquals(1, decoded4844.getCellProofs().get().size());
        assertEquals(2, decoded4844.getCellProofs().get().get(0).size());

        // Verify blobs and commitments
        assertTrue(decoded4844.getBlobs().isPresent());
        assertEquals(1, decoded4844.getBlobs().get().size());
        assertTrue(decoded4844.getKzgCommitments().isPresent());
        assertEquals(1, decoded4844.getKzgCommitments().get().size());
    }
}
