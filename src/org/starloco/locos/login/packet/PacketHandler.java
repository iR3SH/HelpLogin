package org.starloco.locos.login.packet;

import org.starloco.locos.kernel.Console;
import org.starloco.locos.kernel.Main;
import org.starloco.locos.login.LoginClient;

import java.util.UUID;

public class PacketHandler {

    public static void parser(LoginClient client, String packet) {
        switch (client.getStatus()) {
            case WAIT_VERSION: // ok
                Console.instance.write("[" + client.getIoSession().getId() + "] Checking for version '" + packet + "'.");
                Version.verify(client);
                client.setClientVersion(packet);
                break;

            case WAIT_ACCOUNT: // a modifier
                if (!client.isSwitchPacketState()) {
                    if (!packet.equals("#S")) {
                        if (packet.length() < 3) {
                            Console.instance.write("[" + client.getIoSession().getId() + "] Sending of packet '" + packet + "' to verify the account. The client going to be kicked.");
                            client.send("AlEf");
                            client.kick();
                            return;
                        }
                        Console.instance.write("[" + client.getIoSession().getId() + "] Verification of account '" + packet + "'.");
                        AccountName.verify(client, packet);
                    }
                    else {
                        client.setSwitchPacketState(true);
                    }
                }
                else{
                    String replaced_packet = packet.replaceAll("\n", "");
                    client.setAccountBySwitchPacketKey(replaced_packet);
                }
                break;

            case WAIT_PASSWORD: // ok
                if (packet.length() < 3) {
                    Console.instance.write("[" + client.getIoSession().getId() + "] Sending of packet '" + packet + "' to verify the password. The client going to be kicked.");
                    client.send("AlEf");
                    client.kick();
                    return;
                }

                Console.instance.write("[" + client.getIoSession().getId() + "] Verification of password '" + packet + "'.");
                Password.verify(client, packet);
                break;

            case WAIT_NICKNAME: // ok
                Console.instance.write("[" + client.getIoSession().getId() + "] Verification of nickname '" + packet + "'.");
                ChooseNickName.verify(client, packet);
                break;

            case SERVER:
                ServerParse(client, packet);
                break;

        }
    }

    public static void ServerParse(LoginClient client, String packet)
    {
        switch (packet.substring(0, 2)) {
            case "AF":
                FriendServerList.get(client, packet.substring(2));
                break;

            case "Af": // ok
                AccountQueue.verify(client);
                break;

            case "AX":
                ServerSelected.get(client, packet.substring(2));
                break;

            case "Ax":
                ServerList.get(client);
                break;

            case "BA":
                client.send(packet.substring(2));
                break;
            case "Ap":
                break;
            // Demande de Clé pour le Changement de Personnage
            case"Ai":
                client.getAccount().setSwitchPacketKey(UUID.randomUUID().toString());
                Main.database.getAccountData().update(client.getAccount());
                break;
            default:
                client.kick();
                break;
        }
    }
}
