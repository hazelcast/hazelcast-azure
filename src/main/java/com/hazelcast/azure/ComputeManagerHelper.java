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

import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.compute.implementation.ComputeManager;

import java.util.Map;

import static com.hazelcast.azure.AzureProperties.CLIENT_ID;
import static com.hazelcast.azure.AzureProperties.CLIENT_SECRET;
import static com.hazelcast.azure.AzureProperties.SUBSCRIPTION_ID;
import static com.hazelcast.azure.AzureProperties.TENANT_ID;

/**
 * Azure {@link ComputeManager} helper
 */
public final class ComputeManagerHelper {

    private ComputeManagerHelper() {
    }

    /**
     * Create a compute manager client to manage compute resources.
     *
     * @param properties the properties Map provided by Hazelcast
     * @return ComputeManager a client to manage compute resources
     */
    public static ComputeManager getComputeManager(Map<String, Comparable> properties) {
        ApplicationTokenCredentials atc = new ApplicationTokenCredentials(
                (String) AzureProperties.getOrNull(CLIENT_ID, properties),
                (String) AzureProperties.getOrNull(TENANT_ID, properties),
                (String) AzureProperties.getOrNull(CLIENT_SECRET, properties), null);
        return ComputeManager.authenticate(atc, (String) AzureProperties.getOrNull(SUBSCRIPTION_ID, properties));
    }
}
