package org.infinispan.tutorial.simple.remote;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InfinispanClientCacheTest {

    @BeforeAll
    public static void start() {
        InfinispanClientCache.connectToInfinispan();
    }

    @AfterAll
    public static void stop() {
        InfinispanClientCache.disconnect();
    }

    @Test
    public void testRemoteCache() {
        assertNotNull(InfinispanClientCache.cache);

        InfinispanClientCache.manipulateCache();

        assertEquals("value", InfinispanClientCache.cache.get("key"));
    }

}
