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
package org.web3j.protocol.core.filters;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FilterRecoveryTest {

    private Web3jService web3jService;
    private Web3j web3j;
    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    private ScheduledExecutorService scheduledExecutorService =
            Executors.newSingleThreadScheduledExecutor();

    @BeforeEach
    public void setUp() {
        web3jService = mock(Web3jService.class);
        web3j = Web3j.build(web3jService, 1000, scheduledExecutorService);
    }

    @Test
    public void testFilterRecoveryDuringInitialLogs() throws Exception {
        EthFilter ethFilter =
                objectMapper.readValue(
                        "{\"id\":1,\"jsonrpc\": \"2.0\",\"result\": \"0x1\"}", EthFilter.class);

        EthLog notFoundFilter =
                objectMapper.readValue(
                        "{\"jsonrpc\":\"2.0\",\"id\":1,"
                                + "\"error\":{\"code\":-32000,\"message\":\"filter not found\"}}",
                        EthLog.class);

        EthLog successLog =
                objectMapper.readValue(
                        "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":[{\"address\":\"0x1\",\"data\":\"0x2\"}]}",
                        EthLog.class);

        when(web3jService.send(any(Request.class), eq(EthFilter.class))).thenReturn(ethFilter);

        // First getFilterLogs returns "filter not found"
        // Second call (after reinstall) returns success
        when(web3jService.send(any(Request.class), eq(EthLog.class)))
                .thenReturn(notFoundFilter)
                .thenReturn(successLog);

        CountDownLatch latch = new CountDownLatch(1);
        LogFilter logFilter =
                new LogFilter(
                        web3j,
                        log -> latch.countDown(),
                        new org.web3j.protocol.core.methods.request.EthFilter());

        logFilter.run(scheduledExecutorService, 1000);

        assertTrue(
                latch.await(5, TimeUnit.SECONDS),
                "Filter should have recovered and processed logs");
    }
}
