/*
 * Copyright 2020 Hazelcast Inc.
 *
 * Licensed under the Hazelcast Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://hazelcast.com/hazelcast-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.hazelcast.azure;

import com.hazelcast.azure.AzureProperties;
import com.hazelcast.azure.AzureDiscoveryStrategy;
import com.hazelcast.azure.AzureDiscoveryStrategyFactory;

import com.hazelcast.config.Config;
import com.hazelcast.config.InvalidConfigurationException;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.annotation.QuickTest;
import com.hazelcast.config.properties.PropertyDefinition;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static com.hazelcast.internal.util.StringUtil.stringToBytes;

@RunWith(HazelcastParallelClassRunner.class)
@Category(QuickTest.class)
public class AzureDiscoveryStrategyFactoryTest extends HazelcastTestSupport {

    @Test(expected = IllegalArgumentException.class)
    public void testNewDiscoveryFactory() throws Exception {

        Map<String, Comparable> properties = new HashMap<String, Comparable>();
        properties.put("client-id", "test-value");
        properties.put("client-secret", "test-value");
        properties.put("subscription-id", "test-value");
        properties.put("cluster-id", "test-value");
        properties.put("group-name", "test-value");

        AzureDiscoveryStrategyFactory factory = new AzureDiscoveryStrategyFactory();
        factory.newDiscoveryStrategy(null, null, properties);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingConfigValue() throws Exception {

        Map<String, Comparable> properties = new HashMap<String, Comparable>();
        properties.put("client-id", "test-value");
        properties.put("client-secret", "test-value");
        properties.put("subscription-id", "test-value");
        properties.put("cluster-id", "test-value");
        properties.put("group-name", "test-value");

        AzureDiscoveryStrategyFactory factory = new AzureDiscoveryStrategyFactory();
        // should recognize tenant-id is missing
        factory.newDiscoveryStrategy(null, null, properties);
    }

    @Test
    public void testPropertyDefintions() {
        AzureDiscoveryStrategyFactory factory = new AzureDiscoveryStrategyFactory();
        Collection<PropertyDefinition> properties = factory.getConfigurationProperties();

        assertTrue(properties.contains(AzureProperties.CLIENT_ID));
        assertTrue(properties.contains(AzureProperties.TENANT_ID));
        assertTrue(properties.contains(AzureProperties.SUBSCRIPTION_ID));
        assertTrue(properties.contains(AzureProperties.CLIENT_SECRET));
        assertTrue(properties.contains(AzureProperties.CLUSTER_ID));
        assertTrue(properties.contains(AzureProperties.GROUP_NAME));
    }

    @Test
    public void testGetConfigurationProperties() {

        Map<String, Comparable> properties = new HashMap<String, Comparable>();
        properties.put("client-id", "test-value");
        properties.put("client-secret", "test-value");
        properties.put("subscription-id", "test-value");
        properties.put("cluster-id", "test-value");
        properties.put("tenant-id", "test-value");
        properties.put("group-name", "test-value");

        AzureDiscoveryStrategyFactory factory = new AzureDiscoveryStrategyFactory();

        for (PropertyDefinition def : factory.getConfigurationProperties()) {
            // test each proprety actually maps to those defined above
            assertTrue(AzureProperties.getOrNull(def, properties) != null);
        }
        
    }

    @Test
    public void testgetDiscoveryStrategyType() {
        AzureDiscoveryStrategyFactory factory = new AzureDiscoveryStrategyFactory();
        assertEquals(factory.getDiscoveryStrategyType(), AzureDiscoveryStrategy.class);
    }
}
