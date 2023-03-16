package org.starloco.locos.exchange;

import org.starloco.locos.kernel.Console;
import org.starloco.locos.kernel.Logging;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;

class ExchangeHandler implements IoHandler {

    @Override
    public void exceptionCaught(IoSession arg0, Throwable arg1) throws Exception {
        this.setLogged(arg0, "exceptionCaught : " + arg1.getMessage());
        write("eSession " + arg0.getId() + " exception : " + arg1.getCause() + " : " + arg1.getMessage());
    }

    @Override
    public void messageReceived(IoSession session, Object packet) throws Exception {
        String string = new String(((IoBuffer) packet).array());
        write("eSession " + session.getId() + " < " + string);

        ExchangeClient client = (ExchangeClient) session.getAttribute("client");
        String message = bufferToString(packet);

        if (client.getServer() != null)
            Logging.getInstance().write("Game", "messageReceived de " + client.getServer().getId() + " : " + message);
        else {
            InetSocketAddress socketAddress = (InetSocketAddress) session.getRemoteAddress();
            InetAddress inetAddress = socketAddress.getAddress();
            String IP = inetAddress.getHostAddress();
            Logging.getInstance().write("Game", "messageReceived de " + IP + " : " + message);
        }
        client.parser(message);

    }

    @Override
    public void messageSent(IoSession session, Object packet) throws Exception {
        String message = bufferToString(packet);
        this.setLogged(session, "messageSent : " + message);
        write("eSession " + session.getId() + " > " + message);
    }

    @Override
    public void inputClosed(IoSession session) {
        write("eSession " + session.getId() + " closed");
        final ExchangeClient client = (ExchangeClient) session.getAttribute("client");
        client.getServer().setState(0);
        client.kick();
        session.close(true);
        this.setLogged(session, "sessionClosed");
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        write("eSession " + session.getId() + " closed");
        final ExchangeClient client = (ExchangeClient) session.getAttribute("client");
        client.getServer().setState(0);
        client.kick();
        session.close(true);
        this.setLogged(session, "sessionClosed");
    }

    @Override
    public void sessionCreated(IoSession session) {
        write("eSession " + session.getId() + " created");
        session.setAttribute("client", new ExchangeClient(session));

        IoBuffer ioBuffer = IoBuffer.allocate(2048);
        ioBuffer.put("SK?".getBytes());
        ioBuffer.flip();
        session.write(ioBuffer);

        this.setLogged(session, "sessionCreated");
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus arg1) {
        this.setLogged(session, "sessionIdle");
        write("eSession " + session.getId() + " idle");
    }

    @Override
    public void sessionOpened(IoSession arg0) {
        this.setLogged(arg0, "sessionOpened");
    }

    public String bufferToString(Object o) {
        IoBuffer actualBuffer = (IoBuffer) o;
        IoBuffer buffer = IoBuffer.allocate(actualBuffer.capacity());
        buffer.put((IoBuffer) o);
        buffer.flip();

        CharsetDecoder cd = StandardCharsets.UTF_8.newDecoder();

        try {
            return buffer.getString(cd);
        } catch (CharacterCodingException e) {
            e.printStackTrace();
        }
        return "undefined";
    }

    private void setLogged(IoSession arg0, String msg) {
        ExchangeClient client = (ExchangeClient) arg0.getAttribute("client");
        Logging.getInstance().write("Game", msg + " -> " + client.getServer().getId());
    }

    private void write(String message)
    {
        Console.instance.write(message);
    }
}
