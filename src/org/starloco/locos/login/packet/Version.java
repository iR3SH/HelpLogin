package org.starloco.locos.login.packet;

import org.starloco.locos.login.LoginClient;
import org.starloco.locos.login.LoginClient.Status;

class Version {

    public static void verify(LoginClient client) {
        client.setStatus(Status.WAIT_ACCOUNT);
    }
}
