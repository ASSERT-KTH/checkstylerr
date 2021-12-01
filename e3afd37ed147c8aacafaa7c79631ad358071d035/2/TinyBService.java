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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sputnikdev.bluetooth.URL;
import org.sputnikdev.bluetooth.manager.transport.Characteristic;
import org.sputnikdev.bluetooth.manager.transport.Service;
import tinyb.BluetoothDevice;
import tinyb.BluetoothGattCharacteristic;
import tinyb.BluetoothGattService;


/**
 *
 * @author Vlad Kolotov
 */
class TinyBService implements Service {

    private final BluetoothGattService service;

    TinyBService(BluetoothGattService service) {
        this.service = service;
    }

    @Override
    public URL getURL() {
        BluetoothDevice device = service.getDevice();
        return new URL(TinyBFactory.TINYB_PROTOCOL_NAME, device.getAdapter().getAddress(),
                device.getAddress(), service.getUUID(), null, null);
    }

    @Override
    public List<Characteristic> getCharacteristics() {
        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
        List<Characteristic> result = new ArrayList<>(characteristics.size());
        for (BluetoothGattCharacteristic nativeCharacteristic : characteristics) {
            result.add(new TinyBCharacteristic(nativeCharacteristic));
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public void dispose() { }
}
