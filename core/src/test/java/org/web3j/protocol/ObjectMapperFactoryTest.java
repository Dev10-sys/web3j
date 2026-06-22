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

import java.io.IOException;
import java.math.BigDecimal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests to ensure objective of Issue #2141: Fix precision loss when deserializing floating-point
 * JSON values returned from batch RPC calls.
 */
public class ObjectMapperFactoryTest {

    @Test
    public void testSingleFloatingPointPrecision() throws IOException {
        String json = "{\"value\": 209338520.59559551}";
        ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();

        TestModel model = mapper.readValue(json, TestModel.class);

        // Verify exact match without truncation
        assertEquals(new BigDecimal("209338520.59559551"), model.getValue());
    }

    @Test
    public void testBatchFloatingPointPrecisionWithReadTree() throws IOException {
        String json = "[{\"value\": 209338520.59559551}, {\"value\": 1.234567890123456789}]";
        ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();

        // Simulate Service.java batch response handling using readTree
        JsonNode node = mapper.readTree(json);

        // Node 0
        JsonNode valueNode0 = node.get(0).get("value");
        assertTrue(valueNode0.isBigDecimal(), "Node should be a BigDecimalNode");
        assertEquals("209338520.59559551", valueNode0.asText());

        // Node 1
        JsonNode valueNode1 = node.get(1).get("value");
        assertTrue(valueNode1.isBigDecimal(), "Node should be a BigDecimalNode");
        assertEquals("1.234567890123456789", valueNode1.asText());
    }

    static class TestModel {
        private BigDecimal value;

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }
    }
}
