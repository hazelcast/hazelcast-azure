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
import com.hazelcast.internal.nio.IOUtil;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.DiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryStrategyFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Factory class which returns {@link AzureDiscoveryStrategy} to Discovery SPI
 */
public class AzureDiscoveryStrategyFactory implements DiscoveryStrategyFactory {
    private static final ILogger LOGGER = Logger.getLogger(AzureDiscoveryStrategyFactory.class);
    @Override
    public Class<? extends DiscoveryStrategy> getDiscoveryStrategyType() {
        return AzureDiscoveryStrategy.class;
    }

    @Override
    public DiscoveryStrategy newDiscoveryStrategy(DiscoveryNode node, ILogger logger,
                                                  Map<String, Comparable> properties) {
        return new AzureDiscoveryStrategy(properties);
    }

    @Override
    public Collection<PropertyDefinition> getConfigurationProperties() {
        List<PropertyDefinition> result = new ArrayList<PropertyDefinition>();
        for (AzureProperties property : AzureProperties.values()) {
            result.add(property.getDefinition());
        }
        return result;
    }

    /**
     * Checks if Hazelcast is running on Azure.
     * <p>
     * To check if Hazelcast is running on Azure, we first check whether Azure DNS name server is configured as "168.63.129.16"
     * in "/etc/resolv.conf". Such an approach is not officially documented but seems like a good enough heuristic to detect an
     * Azure Compute VM Instance. Since it's not the official method, we still need to make an API call to a local, non-routable
     * address http://169.254.169.254/metadata/instance.
     *
     * @return true if running on Azure Instance
     * @see https://docs.microsoft.com/en-us/azure/virtual-machines/linux/instance-metadata-service#metadata-apis
     */
    @Override
    public boolean isAutoDetectionApplicable() {
        return azureDnsServerConfigured() && azureInstanceMetadataAvailable();
    }

    private static boolean azureDnsServerConfigured() {
        return readFileContents("/etc/resolv.conf").contains("168.63.129.16");
    }

    static String readFileContents(String fileName) {
        InputStream is = null;
        try {
            File file = new File(fileName);
            byte[] data = new byte[(int) file.length()];
            is = new FileInputStream(file);
            is.read(data);
            return new String(data, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Could not get " + fileName, e);
        } finally {
            IOUtil.closeResource(is);
        }
    }

    private static boolean azureInstanceMetadataAvailable() {
        return isEndpointAvailable("http://169.254.169.254/metadata/instance?api-version=2020-06-01");
    }


    static boolean isEndpointAvailable(String url) {
        return !RestClient.create(url)
                .withHeader("Metadata", "True")
                .get()
                .isEmpty();
    }

    @Override
    public DiscoveryStrategyLevel discoveryStrategyLevel() {
        return DiscoveryStrategyLevel.CLOUD_VM;
    }
}
