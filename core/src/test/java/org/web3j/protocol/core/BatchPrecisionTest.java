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
package org.web3j.protocol.core;

import java.math.BigDecimal;
import java.util.Collections;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import org.web3j.protocol.BatchTester;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BatchPrecisionTest extends BatchTester {

    private Web3j web3j;
    private HttpService service;

    @Override
    protected void initWeb3Client(HttpService httpService) {
        this.service = httpService;
        web3j = Web3j.build(httpService);
    }

    public static class BigDecimalResponse extends Response<BigDecimal> {}

    @Test
    void testBatchResponsePrecision() throws Exception {
        String highPrecisionValue = "209338520.59559551";
        buildResponse(
                "["
                        + "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": "
                        + highPrecisionValue
                        + "\n"
                        + "}"
                        + "]");

        Request<?, BigDecimalResponse> request =
                new Request<>(
                        "eth_someMethod",
                        Collections.emptyList(),
                        service,
                        BigDecimalResponse.class);

        BatchResponse response = web3j.newBatch().add(request).send();

        BigDecimal actual = ((BigDecimalResponse) response.getResponses().get(0)).getResult();
        BigDecimal expected = new BigDecimal(highPrecisionValue);

        // Compare values ignoring scale
        assertEquals(0, expected.compareTo(actual), "Expected " + expected + " but got " + actual);
        // Also check exact scale if original was intended
        assertEquals(highPrecisionValue, actual.toPlainString());
    }

    @Test
    void testDirectObjectMapperPrecision() throws Exception {
        String highPrecisionValue = "209338520.59559551";
        String json = "{\"value\": " + highPrecisionValue + "}";
        ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();
        JsonNode node = mapper.readTree(json);
        JsonNode valueNode = node.get("value");

        assertTrue(
                valueNode.isBigDecimal(),
                "Node should be BigDecimalNode but was " + valueNode.getClass().getSimpleName());
        assertEquals(new BigDecimal(highPrecisionValue), valueNode.decimalValue());
    }

    @Test
    void testPrecisionWithZero() throws Exception {
        String zeroValue = "0.00000000";
        buildResponse(
                "["
                        + "{\n"
                        + "  \"id\":1,\n"
                        + "  \"jsonrpc\":\"2.0\",\n"
                        + "  \"result\": "
                        + zeroValue
                        + "\n"
                        + "}"
                        + "]");

        Request<?, BigDecimalResponse> request =
                new Request<>(
                        "eth_someMethod",
                        Collections.emptyList(),
                        service,
                        BigDecimalResponse.class);

        BatchResponse response = web3j.newBatch().add(request).send();

        BigDecimal actual = ((BigDecimalResponse) response.getResponses().get(0)).getResult();
        assertEquals(0, new BigDecimal(zeroValue).compareTo(actual));
    }
}
