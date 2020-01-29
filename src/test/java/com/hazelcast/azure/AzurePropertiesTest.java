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

import com.hazelcast.config.properties.ValidationException;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

@RunWith(HazelcastParallelClassRunner.class)
@Category(QuickTest.class)
public class AzurePropertiesTest extends HazelcastTestSupport {

    @Test
    public void testNewDiscoveryFactory() throws Exception {

        Map<String, Comparable> properties = new HashMap<String, Comparable>();
        properties.put("client-id", "test-value");
        properties.put("client-secret", "test-value");
        properties.put("subscription-id", "test-value");
        properties.put("cluster-id", "test-value");
        properties.put("tenant-id", "test-value");
        properties.put("group-name", "test-value");

        assertTrue("Expected to find AzureProperties.CLIENT_ID", AzureProperties.getOrNull(AzureProperties.CLIENT_ID, properties) != null);
        assertTrue("Expected to find AzureProperties.TENANT_ID", AzureProperties.getOrNull(AzureProperties.TENANT_ID, properties) != null);
        assertTrue("Expected to find AzureProperties.SUBSCRIPTION_ID", AzureProperties.getOrNull(AzureProperties.SUBSCRIPTION_ID, properties) != null);
        assertTrue("Expected to find AzureProperties.CLIENT_SECRET", AzureProperties.getOrNull(AzureProperties.CLIENT_SECRET, properties) != null);
        assertTrue("Expected to find AzureProperties.CLUSTER_ID", AzureProperties.getOrNull(AzureProperties.CLUSTER_ID, properties) != null);
        assertTrue("Expected to find AzureProperties.GROUP_NAME", AzureProperties.getOrNull(AzureProperties.GROUP_NAME, properties) != null);
    }

    @Test(expected = ValidationException.class)
    public void testPortValueValidator_validate_negative_val() throws Exception {

        AzureProperties.PortValueValidator validator = new AzureProperties.PortValueValidator();

        validator.validate(-1);
    }

    @Test(expected = ValidationException.class)
    public void testPortValueValidatorValidateTooBig() throws Exception {

        AzureProperties.PortValueValidator validator = new AzureProperties.PortValueValidator();

        validator.validate(65536);
    }

    @Test
    public void testPortValueValidatorValidate() throws Exception {

        AzureProperties.PortValueValidator validator = new AzureProperties.PortValueValidator();

        validator.validate(0);
        validator.validate(1000);
        validator.validate(65535);
    }

}
