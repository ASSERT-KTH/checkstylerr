/*
 * Copyright 2016 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.examples.compute.snippets;

import com.google.cloud.compute.deprecated.Address;
import com.google.cloud.compute.deprecated.AddressInfo;
import com.google.cloud.compute.deprecated.AttachedDisk;
import com.google.cloud.compute.deprecated.AttachedDisk.PersistentDiskConfiguration;
import com.google.cloud.compute.deprecated.Compute;
import com.google.cloud.compute.deprecated.ComputeOptions;
import com.google.cloud.compute.deprecated.DiskId;
import com.google.cloud.compute.deprecated.DiskInfo;
import com.google.cloud.compute.deprecated.ImageDiskConfiguration;
import com.google.cloud.compute.deprecated.ImageId;
import com.google.cloud.compute.deprecated.InstanceId;
import com.google.cloud.compute.deprecated.InstanceInfo;
import com.google.cloud.compute.deprecated.MachineTypeId;
import com.google.cloud.compute.deprecated.NetworkId;
import com.google.cloud.compute.deprecated.NetworkInterface;
import com.google.cloud.compute.deprecated.NetworkInterface.AccessConfig;
import com.google.cloud.compute.deprecated.Operation;
import com.google.cloud.compute.deprecated.RegionAddressId;

import java.util.concurrent.TimeoutException;

/**
 * A snippet for Google Cloud Compute Engine showing how to create a disk and an address. The
 * snippet also shows how to create a virtual machine instance using the created disk and address.
 */
public class CreateAddressDiskAndInstance {

  public static void main(String... args) throws InterruptedException, TimeoutException {
    // Create a service object
    // Credentials are inferred from the environment.
    Compute compute = ComputeOptions.getDefaultInstance().getService();

    // Create an external region address
    RegionAddressId addressId = RegionAddressId.of("us-central1", "test-address");
    Operation operation = compute.create(AddressInfo.of(addressId));
    // Wait for operation to complete
    operation = operation.waitFor();
    if (operation.getErrors() == null) {
      System.out.println("Address " + addressId + " was successfully created");
    } else {
      // inspect operation.getErrors()
      throw new RuntimeException("Address creation failed");
    }

    // Create a persistent disk
    ImageId imageId = ImageId.of("debian-cloud", "debian-8-jessie-v20160329");
    DiskId diskId = DiskId.of("us-central1-a", "test-disk");
    ImageDiskConfiguration diskConfiguration = ImageDiskConfiguration.of(imageId);
    DiskInfo disk = DiskInfo.of(diskId, diskConfiguration);
    operation = compute.create(disk);
    // Wait for operation to complete
    operation = operation.waitFor();
    if (operation.getErrors() == null) {
      System.out.println("Disk " + diskId + " was successfully created");
    } else {
      // inspect operation.getErrors()
      throw new RuntimeException("Disk creation failed");
    }

    // Create a virtual machine instance
    Address externalIp = compute.getAddress(addressId);
    InstanceId instanceId = InstanceId.of("us-central1-a", "test-instance");
    NetworkId networkId = NetworkId.of("default");
    PersistentDiskConfiguration attachConfiguration =
        PersistentDiskConfiguration.newBuilder(diskId).setBoot(true).build();
    AttachedDisk attachedDisk = AttachedDisk.of("dev0", attachConfiguration);
    NetworkInterface networkInterface = NetworkInterface.newBuilder(networkId)
        .setAccessConfigurations(AccessConfig.of(externalIp.getAddress()))
        .build();
    MachineTypeId machineTypeId = MachineTypeId.of("us-central1-a", "n1-standard-1");
    InstanceInfo instance =
        InstanceInfo.of(instanceId, machineTypeId, attachedDisk, networkInterface);
    operation = compute.create(instance);
    // Wait for operation to complete
    operation = operation.waitFor();
    if (operation.getErrors() == null) {
      System.out.println("Instance " + instanceId + " was successfully created");
    } else {
      // inspect operation.getErrors()
      throw new RuntimeException("Instance creation failed");
    }
  }
}
