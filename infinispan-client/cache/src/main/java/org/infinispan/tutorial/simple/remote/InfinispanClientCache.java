package org.infinispan.tutorial.simple.remote;

import org.infinispan.api.Infinispan;
import org.infinispan.api.sync.SyncCache;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.TUTORIAL_CACHE_NAME;

public class InfinispanClientCache {
    static Infinispan infinispanClient;
    static SyncCache<String, String> cache;

    public static void main(String[] args) {
        connectToInfinispan();
        manipulateCache();
        disconnect();
    }

    static void manipulateCache() {
        // Store a value
        cache.put("key", "value");
        // Retrieve the value and print it out
        System.out.printf("key = %s\n", cache.get("key"));
    }

    static void connectToInfinispan() {
        // Connect to the server
        infinispanClient = TutorialsConnectorHelper.connect();
        // Obtain the remote cache
        cache = infinispanClient.sync().caches().get(TUTORIAL_CACHE_NAME);
    }

    static void disconnect() {
        // Stop the cache manager and release all resources
        TutorialsConnectorHelper.stop(infinispanClient);
    }
}
