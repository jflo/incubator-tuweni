/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.apache.tuweni.junit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@ExtendWith(RedisServerExtension.class)
class RedisServerExtensionTest {

  @Test
  void shouldHaveAccessToARedisServer(@RedisPort Integer port) {
    assertNotNull(port);
    assertTrue(port >= 32768, "Port must be more than 32768, was:" + port);
    try (JedisPool client =
        new JedisPool(new JedisPoolConfig(), InetAddress.getLoopbackAddress().getHostAddress(), port)) {
      try (Jedis conn = client.getResource()) {
        assertTrue(conn.isConnected());
      }
    }
  }
}
