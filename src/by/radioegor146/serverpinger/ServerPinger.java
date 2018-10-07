/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package by.radioegor146.serverpinger;

import by.radioegor146.serverpinger.messages.ConnectionAcceptOrDenyMessage;
import by.radioegor146.serverpinger.messages.ConnectionRequestMessage;
import by.radioegor146.serverpinger.messages.ConnectionRequestReplyConfirmMessage;
import by.radioegor146.serverpinger.messages.ConnectionRequestReplyMessage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

/**
 *
 * @author radioegor146
 */
public class ServerPinger {

    private DatagramSocket socket;

    private int state = 0;
    private boolean working = false;
    private int lastServerMessageId;
    private short currentMessageId;
    private ConnectionAcceptOrDenyMessage serverInfo = new ConnectionAcceptOrDenyMessage();

    public ConnectionAcceptOrDenyMessage getServerInfo(String serverIp, int port) {
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(1000);
            socket.connect(InetAddress.getByName(serverIp), port);
            working = true;
            Thread thread = new ReceiveThread();
            thread.start();
            sendMessage(new ConnectionRequestMessage());
            while (state == 0) {
                Thread.sleep(1);
            }
            working = false;
            thread.interrupt();
            return serverInfo;
        } catch (IOException | InterruptedException ex) {
            return new ConnectionAcceptOrDenyMessage();
        }
    }

    private void sendMessage(ClientMessage message) throws IOException {
        currentMessageId++;
        byte[] messageData = message.getBytes();
        FactorioNetMessage[] messages;
        if (messageData.length > 500) {
            messages = new FactorioNetMessage[messageData.length / 500 + 1];
            for (int i = 0; i < messageData.length / 500 + 1; i++) {
                messages[i] = new FactorioNetMessage();
                messages[i].isFragmented = true;
                messages[i].isLastFragment = (i == messageData.length / 500);
                messages[i].fragmentId = (short) (i + 1);
                messages[i].type = message.getMessageType();
                if (messages[i].isLastFragment) {
                    messages[i].packetBytes = new byte[messageData.length % 500];
                } else {
                    messages[i].packetBytes = new byte[500];
                }
                System.arraycopy(messageData, i * 500, messages[i].packetBytes, 0, messages[i].packetBytes.length);
            }
        } else {
            messages = new FactorioNetMessage[1];
            messages[0] = new FactorioNetMessage();
            messages[0].type = message.getMessageType();
            messages[0].packetBytes = message.getBytes();
        }
        for (FactorioNetMessage msg : messages) {
            msg.currentMessageId = currentMessageId;
            msg.lastServerMessageId = (short) lastServerMessageId;
            socket.send(msg.getPacket());
        }
    }

    private void handleMessage(byte type, byte[] data) throws IOException {
        switch (type) {
            case 3:
                ConnectionRequestReplyConfirmMessage message = new ConnectionRequestReplyConfirmMessage();
                ConnectionRequestReplyMessage tmsg = new ConnectionRequestReplyMessage();
                tmsg.fromBytes(data);
                message.connectionRequestIDGeneratedOnServer = tmsg.connectionRequestIDGeneratedOnServer;
                sendMessage(message);
                break;
            case 5:
                if (state == 0) {
                    ConnectionAcceptOrDenyMessage msg = new ConnectionAcceptOrDenyMessage();
                    msg.fromBytes(data);
                    serverInfo = msg;
                }
                state = 1;
                break;
        }
    }

    private class ReceiveThread extends Thread {

        private final HashMap<Short, FactorioNetMessageBundle> fragmentedPackets = new HashMap();

        @Override
        public void run() {
            while (working) {
                if (socket == null) {
                    continue;
                }
                DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
                try {
                    socket.receive(packet);
                    handlePacket(packet);
                } catch (Exception ex) {
                    state = 2;
                    return;
                }
            }
        }

        private void handlePacket(DatagramPacket packet) throws Exception {
            FactorioNetMessage netMessage = new FactorioNetMessage(packet);
            lastServerMessageId = netMessage.messageId;
            if (netMessage.isFragmented) {
                if (!fragmentedPackets.containsKey(netMessage.messageId)) {
                    fragmentedPackets.put(netMessage.messageId, new FactorioNetMessageBundle());
                }
                if (fragmentedPackets.get(netMessage.messageId).handleBundleMessage(netMessage)) {
                    handleMessage(netMessage.type, fragmentedPackets.get(netMessage.messageId).getOverallMessage().packetBytes);
                    fragmentedPackets.remove(netMessage.messageId);
                }
            } else {
                handleMessage(netMessage.type, netMessage.packetBytes);
            }
        }
    }
}
