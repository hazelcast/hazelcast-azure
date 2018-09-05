/*
 * Copyright (c) 2016, Microsoft Corporation. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hazelcast.azure;

import com.hazelcast.config.properties.PropertyDefinition;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(HazelcastParallelClassRunner.class)
@Category(QuickTest.class)
public class AzureDiscoveryStrategyFactoryTest extends HazelcastTestSupport {

    @Test(expected = IllegalArgumentException.class)
    public void testMissingConfigValue() throws Exception {

        Map<String, Comparable> properties = new HashMap<String, Comparable>();
        properties.put("client-id", "test-value");
        properties.put("client-secret", "test-value");
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

        assertTrue(properties.contains(AzureProperties.SUBSCRIPTION_ID));
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
