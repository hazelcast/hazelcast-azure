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

import com.hazelcast.config.properties.PropertyDefinition;
import com.hazelcast.logging.ILogger;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.DiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryStrategyFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Factory class which returns {@link AzureDiscoveryStrategy} to Discovery SPI
 */
public class AzureDiscoveryStrategyFactory implements DiscoveryStrategyFactory {

    private static final Collection<PropertyDefinition> PROPERTY_DEFINITIONS;

    static {
        List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
        propertyDefinitions.add(AzureProperties.CLIENT_ID);
        propertyDefinitions.add(AzureProperties.CLIENT_SECRET);
        propertyDefinitions.add(AzureProperties.SUBSCRIPTION_ID);
        propertyDefinitions.add(AzureProperties.TENANT_ID);
        propertyDefinitions.add(AzureProperties.GROUP_NAME);
        propertyDefinitions.add(AzureProperties.CLUSTER_ID);
        PROPERTY_DEFINITIONS = Collections.unmodifiableCollection(propertyDefinitions);
    }

    @Override
    public Class<? extends DiscoveryStrategy> getDiscoveryStrategyType() {
        return AzureDiscoveryStrategy.class;
    }

    @Override
    public DiscoveryStrategy newDiscoveryStrategy(DiscoveryNode node, ILogger logger, Map<String, Comparable> properties) {

        // validate configuration
        for (PropertyDefinition prop : PROPERTY_DEFINITIONS) {
            if (AzureProperties.getOrNull(prop, properties) == null) {
                throw new IllegalArgumentException("Property, " + prop.key() + " cannot be null");
            }
        }

        return new AzureDiscoveryStrategy(properties);
    }

    /**
     * Gets the configuration property definitions
     *
     * @return {@code Collection<PropertyDefinition>} the property defitions for the AzureDiscoveryStrategy
     */
    public Collection<PropertyDefinition> getConfigurationProperties() {
        return PROPERTY_DEFINITIONS;
    }
}
