package org.infinispan.tutorial.simple.redis;

import redis.clients.jedis.JedisPooled;

import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.HOST;
import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.PASSWORD;
import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.SINGLE_PORT;
import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.USER;

public class JedisRedisClientCache {

   public static void main(String[] args) {
      JedisPooled pool = new JedisPooled(HOST, SINGLE_PORT, USER, PASSWORD);
      String key = "Hello";
      pool.set(key, "world");
      String value = pool.get(key);
      System.out.println(String.format("Read from Infinispan using a Redis Client (Resp Protocol): %s %s", key, value));
   }

}
