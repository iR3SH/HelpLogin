package org.starloco.locos.login.packet;

import org.starloco.locos.login.LoginClient;
import org.starloco.locos.login.LoginClient.Status;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class Password {

    public static void verify(LoginClient client, String pass) {
        String password = decryptPassword(pass, client.getKey());

        if (!password.equals(client.getAccount().getPass())){
            client.send("AlEf");
            client.kick();
            return;
        }

        client.setStatus(Status.SERVER);
    }

    private static String decryptPassword(String pass, String key) {
        if (pass.startsWith("#1"))
            pass = pass.substring(2);
        String chain = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_";

        char PPass, PKey;
        int APass, AKey, ANB, ANB2, somme1, somme2;

        StringBuilder decrypted = new StringBuilder();

        for (int i = 0; i < pass.length(); i += 2) {
            PKey = key.charAt(i / 2);
            ANB = chain.indexOf(pass.charAt(i));
            ANB2 = chain.indexOf(pass.charAt(i + 1));

            somme1 = ANB + chain.length();
            somme2 = ANB2 + chain.length();

            APass = somme1 - (int) PKey;
            if (APass < 0)
                APass += 64;
            APass *= 16;

            AKey = somme2 - (int) PKey;
            if (AKey < 0)
                AKey += 64;

            PPass = (char) (APass + AKey);

            decrypted.append(PPass);
        }

        return decrypted.toString();
    }

    private static String cryptPassword(String message, String type) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(type);
            md.update(message.getBytes());
            byte[] mb = md.digest();
            StringBuilder out = new StringBuilder();
            for (byte temp : mb) {
                StringBuilder s = new StringBuilder(Integer.toHexString(temp));
                while (s.length() < 2) {
                    s.insert(0, "0");
                }
                s = new StringBuilder(s.substring(s.length() - 2));
                out.append(s);
            }
            return out.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
