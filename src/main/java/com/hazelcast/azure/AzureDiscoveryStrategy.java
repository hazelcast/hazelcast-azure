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


import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.cluster.Address;
import com.hazelcast.spi.discovery.AbstractDiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.DiscoveryStrategy;
import com.hazelcast.spi.discovery.SimpleDiscoveryNode;
import com.hazelcast.spi.partitiongroup.PartitionGroupMetaData;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.PowerState;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.implementation.ComputeManager;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NicIPConfiguration;
import com.microsoft.azure.management.network.PublicIPAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


/**
 * Azure implementation of {@link DiscoveryStrategy}
 */
public class AzureDiscoveryStrategy extends AbstractDiscoveryStrategy {

    private static final ILogger LOGGER = Logger.getLogger(AzureDiscoveryStrategy.class);

    private final Map<String, Comparable> properties;
    private final Map<String, String> memberMetaData = new HashMap<String, String>();

    private ComputeManager computeManager;

    /**
     * Instantiates a new AzureDiscoveryStrategy
     *
     * @param properties the discovery strategy properties
     */
    public AzureDiscoveryStrategy(Map<String, Comparable> properties) {
        super(LOGGER, properties);
        this.properties = properties;
    }

    @Override
    public void start() {
        try {
            computeManager = ComputeManagerHelper.getComputeManager(properties);
        } catch (CloudException e) {
            LOGGER.severe("Failed to start Azure SPI", e);
        }
    }

    @Override
    public Map<String, String> discoverLocalMetadata() {
        if (memberMetaData.size() == 0) {
            discoverNodes();
        }
        return memberMetaData;
    }

    @Override
    public Iterable<DiscoveryNode> discoverNodes() {
        try {
            String resourceGroup = AzureProperties.getOrNull(AzureProperties.GROUP_NAME, properties);
            String clusterId = AzureProperties.getOrNull(AzureProperties.CLUSTER_ID, properties);

            PagedList<VirtualMachine> virtualMachines = computeManager.virtualMachines().listByResourceGroup(resourceGroup);

            ArrayList<DiscoveryNode> nodes = new ArrayList<DiscoveryNode>();

            for (VirtualMachine vm : virtualMachines) {
                Map<String, String> tags = vm.tags();
                // a tag is required with the hazelcast clusterid
                // and the value should be the port number
                if (tags.get(clusterId) == null) {
                    continue;
                }

                // skip any deallocated vms
                if (!PowerState.RUNNING.equals(vm.powerState())) {
                    continue;
                }

                Integer faultDomainId = vm.instanceView().platformFaultDomain();
                if (faultDomainId != null) {
                    memberMetaData.put(PartitionGroupMetaData.PARTITION_GROUP_ZONE, faultDomainId.toString());
                }
                int port = Integer.parseInt(tags.get(clusterId));
                DiscoveryNode node = buildDiscoveredNode(faultDomainId, vm, port);

                if (node != null) {
                    nodes.add(node);
                }
            }
            LOGGER.info("Azure Discovery SPI Discovered " + nodes.size() + " nodes");
            return nodes;
        } catch (Exception e) {
            LOGGER.finest("Failed to discover nodes with Azure SPI", e);
            return null;
        }

    }

    @Override
    public void destroy() {
        // no native resources were allocated so nothing to do here
    }

    /**
     * Builds a discovery node
     *
     * @param faultDomainId
     * @param vm
     * @param port
     * @return DiscoveryNode the Hazelcast DiscoveryNode
     */
    private DiscoveryNode buildDiscoveredNode(Integer faultDomainId, VirtualMachine vm, int port)
            throws UnknownHostException {
        NetworkInterface networkInterface = vm.getPrimaryNetworkInterface();
        for (NicIPConfiguration ipConfiguration : networkInterface.ipConfigurations().values()) {
            PublicIPAddress publicIPAddress = ipConfiguration.getPublicIPAddress();
            String privateIP = ipConfiguration.privateIPAddress();
            Address privateAddress = new Address(privateIP, port);
            String localHostAddress = getLocalHostAddress();
            if (publicIPAddress != null) {
                String publicIP = publicIPAddress.ipAddress();
                Address publicAddress = new Address(publicIP, port);

                if (localHostAddress != null && publicIP.equals(localHostAddress)) {
                    updateVirtualMachineMetaData(faultDomainId);
                }
                return new SimpleDiscoveryNode(privateAddress, publicAddress);
            }
            if (localHostAddress != null && privateIP.equals(localHostAddress)) {
                //In private address there is no host name so we are passing null.
                updateVirtualMachineMetaData(faultDomainId);
            }
            return new SimpleDiscoveryNode(privateAddress);
        }

        // no node found;
        return null;
    }

    private void updateVirtualMachineMetaData(Integer faultDomain) {
        if (faultDomain != null) {
            memberMetaData.put(PartitionGroupMetaData.PARTITION_GROUP_ZONE, faultDomain.toString());
        }
    }

    public String getLocalHostAddress() {
        try {
            InetAddress candidateAddress = null;
            // Iterate all NICs (network interface cards)...
            for (Enumeration ifaces = java.net.NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
                java.net.NetworkInterface iface = (java.net.NetworkInterface) ifaces.nextElement();
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {
                        if (inetAddr.isSiteLocalAddress()) {
                            return inetAddr.getHostAddress();
                        } else if (candidateAddress == null) {
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                return candidateAddress.getHostAddress();
            }
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
            }
            return jdkSuppliedAddress.getHostAddress();
        } catch (Exception e) {
            LOGGER.warning("Failed to determine Host address: " + e);
            return null;
        }
    }
}
