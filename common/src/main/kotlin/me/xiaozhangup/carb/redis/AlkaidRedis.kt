/*
 * Copyright 2022 Alkaid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.xiaozhangup.carb.redis


object AlkaidRedis {

    /**
     * 创建 Redis 连接器
     *
     * @return [SingleRedisConnector]
     */
    fun create(): SingleRedisConnector {
        return SingleRedisConnector()
    }

    /**
     * 创建 RedisCluster 连接器
     * Redis集群模式连接器
     *
     * Node按照官方推荐最小是 6个 3主3从 最低是 3主
     *
     * @return [ClusterRedisConnector]
     */
    fun linkCluster(builder: ClusterRedisConnector.() -> Unit = {}): ClusterRedisConnector {
        return ClusterRedisConnector().apply {
            builder.invoke(this)
            build()
        }
    }

    /**
     * 创建 Redis 连接
     *
     * @return [SingleRedisConnection]
     */
    fun createDefault(connector: (SingleRedisConnector) -> Unit = { }): SingleRedisConnection {
        return create().also(connector).connect().connection()
    }
}
