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
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

class ExchangeHandler implements IoHandler {

    @Override
    public void exceptionCaught(IoSession arg0, Throwable arg1) throws Exception {
        this.setLogged(arg0, "exceptionCaught : " + arg1.getMessage());
        Console.instance.write("eSession " + arg0.getId() + " exception : " + arg1.getCause() + " : " + arg1.getMessage());
    }

    @Override
    public void messageReceived(IoSession session, Object packet) throws Exception {
        String string = new String(((IoBuffer) packet).array());
        Console.instance.write("eSession " + session.getId() + " < " + string);

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
        Console.instance.write("eSession " + session.getId() + " > " + message);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        Console.instance.write("eSession " + session.getId() + " closed");
        final ExchangeClient client = (ExchangeClient) session.getAttribute("client");
        client.getServer().setState(0);
        
        this.setLogged(session, "sessionClosed");
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        Console.instance.write("eSession " + session.getId() + " created");
        session.setAttribute("client", new ExchangeClient(session));

        IoBuffer ioBuffer = IoBuffer.allocate(2048);
        ioBuffer.put("SK?".getBytes());
        ioBuffer.flip();
        session.write(ioBuffer);

        this.setLogged(session, "sessionCreated");
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus arg1) throws Exception {
        this.setLogged(session, "sessionIdle");
        Console.instance.write("eSession " + session.getId() + " idle");
    }

    @Override
    public void sessionOpened(IoSession arg0) throws Exception {
        this.setLogged(arg0, "sessionOpened");
    }

    public String bufferToString(Object o) {
        IoBuffer actualBuffer = (IoBuffer) o;
        IoBuffer buffer = IoBuffer.allocate(actualBuffer.capacity());
        buffer.put((IoBuffer) o);
        buffer.flip();

        CharsetDecoder cd = Charset.forName("UTF-8").newDecoder();

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
}
