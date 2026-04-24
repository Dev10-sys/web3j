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
package org.web3j.protocol;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrecisionLossTest {

    @Test
    public void testPrecisionLossWithWeb3jMapper() {
        // Use the ObjectMapper from our factory which now has the fix applied
        ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();

        String json =
                "{\"balance\":209338520.59559551,\"received\":209338520.59559551,\"immature\":0.00000000}";
        String jsonArray =
                "[{\"balance\":209338520.59559551,\"received\":209338520.59559551,\"immature\":0.00000000}]";

        try {
            // Test single object
            var tree = mapper.readTree(json);
            var balance = mapper.treeToValue(tree, OriginalAddressRangeBalance2.class);
            System.out.println("Single object balance: " + balance.getBalance());

            // Test array (as used in sendBatch)
            ArrayNode nodes = (ArrayNode) mapper.readTree(jsonArray);
            List<OriginalAddressRangeBalance2> balances = new ArrayList<>();
            for (int i = 0; i < nodes.size(); i++) {
                var b = mapper.treeToValue(nodes.get(i), OriginalAddressRangeBalance2.class);
                balances.add(b);
            }
            System.out.println("Batch balance: " + balances.get(0).getBalance());

            // Expected value: 209338520.59559551
            BigDecimal expected = new BigDecimal("209338520.59559551");

            assertEquals(
                    0,
                    expected.compareTo(balance.getBalance()),
                    "Single object balance precision loss with Web3j mapper");
            assertEquals(
                    0,
                    expected.compareTo(balances.get(0).getBalance()),
                    "Batch balance precision loss with Web3j mapper");

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static class OriginalAddressRangeBalance2 {
        private BigDecimal balance;
        private BigDecimal received;
        private BigDecimal immature;

        public OriginalAddressRangeBalance2() {}

        public BigDecimal getBalance() {
            return balance;
        }

        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }

        public BigDecimal getReceived() {
            return received;
        }

        public void setReceived(BigDecimal received) {
            this.received = received;
        }

        public BigDecimal getImmature() {
            return immature;
        }

        public void setImmature(BigDecimal immature) {
            this.immature = immature;
        }
    }
}
