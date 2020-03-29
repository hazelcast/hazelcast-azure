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

import com.hazelcast.instance.EndpointQualifier;
import com.hazelcast.spi.MemberAddressProvider;

import java.net.InetSocketAddress;

/**
 * Build-in Azure implementation of {@link MemberAddressProvider}
 */
public class AzureMemberAddressProvider implements MemberAddressProvider {

    private final AzureMetadataApi metadataApi;


    public AzureMemberAddressProvider() {
        this.metadataApi = new AzureMetadataApi();
    }

    @Override
    public InetSocketAddress getBindAddress() {
        return metadataApi.privateAddress();
    }

    @Override
    public InetSocketAddress getBindAddress(EndpointQualifier endpointQualifier) {
        return metadataApi.privateAddress();
    }

    @Override
    public InetSocketAddress getPublicAddress() {
        return metadataApi.publicAddress();
    }

    @Override
    public InetSocketAddress getPublicAddress(EndpointQualifier endpointQualifier) {
        return metadataApi.publicAddress();
    }
}
