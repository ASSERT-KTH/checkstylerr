package org.sputnikdev.bluetooth.manager.transport.tinyb;

/*-
 * #%L
 * org.sputnikdev:bluetooth-manager
 * %%
 * Copyright (C) 2017 Sputnik Dev
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.sputnikdev.bluetooth.URL;
import org.sputnikdev.bluetooth.manager.transport.Notification;
import org.sputnikdev.bluetooth.manager.transport.Adapter;
import org.sputnikdev.bluetooth.manager.transport.Device;
import tinyb.BluetoothAdapter;
import tinyb.BluetoothDevice;
import tinyb.BluetoothNotification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Vlad Kolotov
 */
class TinyBAdapter implements Adapter {

    private final BluetoothAdapter adapter;

    TinyBAdapter(BluetoothAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public URL getURL() {
        return new URL(TinyBFactory.TINYB_PROTOCOL_NAME, adapter.getAddress(), null);
    }

    @Override
    public String getAlias() {
        return adapter.getAlias();
    }

    @Override
    public String getName() {
        return adapter.getName();
    }

    @Override
    public void setAlias(String s) {
        adapter.setAlias(s);
    }

    @Override
    public boolean isPowered() {
        return adapter.getPowered();
    }

    @Override
    public void enablePoweredNotifications(Notification<Boolean> notification) {
        adapter.enablePoweredNotifications(new BluetoothNotification<Boolean>() {
            @Override public void run(Boolean powered) {
                notification.notify(powered);
            }
        });
    }

    @Override
    public void disablePoweredNotifications() {
        adapter.disablePoweredNotifications();
    }

    @Override
    public void setPowered(boolean b) {
        adapter.setPowered(b);
    }

    @Override
    public boolean isDiscovering() {
        return adapter.getDiscovering();
    }

    @Override
    public void enableDiscoveringNotifications(Notification<Boolean> notification) {
        adapter.enableDiscoveringNotifications(new BluetoothNotification<Boolean>() {
            @Override public void run(Boolean value) {
                notification.notify(value);
            }
        });
    }

    @Override
    public void disableDiscoveringNotifications() {
        adapter.disableDiscoveringNotifications();
    }

    @Override
    public boolean startDiscovery() {
        return adapter.startDiscovery();
    }

    @Override
    public boolean stopDiscovery() {
        return adapter.stopDiscovery();
    }

    @Override
    public List<Device> getDevices() {
        List<BluetoothDevice> devices = adapter.getDevices();
        List<Device> result = new ArrayList<>(devices.size());
        for (BluetoothDevice device : devices) {
            if (device.getRSSI() != 0) {
                result.add(new TinyBDevice(device));
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public void dispose() { }
}
