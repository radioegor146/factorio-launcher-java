/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package by.radioegor146.serverpinger.classes;

import by.radioegor146.serverpinger.utils.DataStreamUtils;
import by.radioegor146.serverpinger.utils.LittleEndianInputStream;
import by.radioegor146.serverpinger.utils.LittleEndianOutputStream;
import java.io.IOException;

/**
 *
 * @author radioegor146
 */
public class AddressToUsernameMapping {

    public String username;
    public String address;

    public AddressToUsernameMapping read(LittleEndianInputStream stream) throws IOException {
        username = DataStreamUtils.readComplexString(stream);
        address = DataStreamUtils.readComplexString(stream);
        return this;
    }

    public void write(LittleEndianOutputStream stream) throws IOException {

    }
}
