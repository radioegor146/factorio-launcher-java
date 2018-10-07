/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package by.radioegor146.serverpinger;

import java.util.TreeMap;

/**
 *
 * @author radioegor146
 */
public class FactorioNetMessageBundle {

    private final TreeMap<Short, FactorioNetMessage> bundleMessages = new TreeMap();

    private int needCount = -1;

    public boolean handleBundleMessage(FactorioNetMessage message) {
        if (needCount == bundleMessages.size()) {
            return true;
        }
        if (message.isLastFragment) {
            needCount = message.fragmentId + 1;
        }
        bundleMessages.put(message.fragmentId, message);
        return needCount == bundleMessages.size();
    }

    public FactorioNetMessage getOverallMessage() {
        if (needCount != bundleMessages.size()) {
            return null;
        }
        int overallSize = 0;
        overallSize = bundleMessages.values().stream().map((netMessage) -> netMessage.packetBytes.length).reduce(overallSize, Integer::sum);
        byte[] bytes = new byte[overallSize];
        int arrayPointer = 0;
        for (FactorioNetMessage netMessage : bundleMessages.values()) {
            System.arraycopy(netMessage.packetBytes, 0, bytes, arrayPointer, netMessage.packetBytes.length);
            arrayPointer += netMessage.packetBytes.length;
        }
        return new FactorioNetMessage(bundleMessages.firstEntry().getValue().type, bytes);
    }
}
