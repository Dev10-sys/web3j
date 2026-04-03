/*
 * Copyright 2019 Web3 Labs Ltd.
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
package org.web3j.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Async task facilitation. */
public class Async {

    private Async() {}

    /**
     * The task executor used for all async operations.
     *
     * <p>Note that this executor is static and will live for the duration of the JVM lifecycle
     * unless manually shut down. In web container environments (e.g. Tomcat, Jetty), this can lead
     * to ClassLoader leaks when applications are reloaded. Use {@link #shutdown()} to manually stop
     * the executor during application undeployment.
     */
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown(executor)));
    }

    /**
     * Stop the executor service.
     *
     * <p>This method should be called when an application is undeployed in a web container (e.g.
     * via a {@code ServletContextListener}) to prevent ClassLoader leaks.
     *
     * <p>Example usage in a {@code ServletContextListener}:
     *
     * <pre>
     * public void contextDestroyed(ServletContextEvent sce) {
     *     Async.shutdown();
     * }
     * </pre>
     */
    public static void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static <T> CompletableFuture<T> run(Callable<T> callable) {
        CompletableFuture<T> result = new CompletableFuture<>();
        CompletableFuture.runAsync(
                () -> {
                    // we need to explicitly catch any exceptions,
                    // otherwise they will be silently discarded
                    try {
                        result.complete(callable.call());
                    } catch (Throwable e) {
                        result.completeExceptionally(e);
                    }
                },
                executor);
        return result;
    }

    private static int getCpuCount() {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * Provide a new ScheduledExecutorService instance.
     *
     * <p>A shutdown hook is created to terminate the thread pool on application termination.
     *
     * @return new ScheduledExecutorService
     */
    public static ScheduledExecutorService defaultExecutorService() {
        ScheduledExecutorService scheduledExecutorService =
                Executors.newScheduledThreadPool(getCpuCount());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown(scheduledExecutorService)));

        return scheduledExecutorService;
    }

    /**
     * Shutdown as per {@link ExecutorService} Javadoc recommendation.
     *
     * @param executorService executor service we wish to shut down.
     */
    private static void shutdown(ExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Thread pool did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
