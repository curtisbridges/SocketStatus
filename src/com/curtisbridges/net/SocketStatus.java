package com.curtisbridges.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;

public class SocketStatus implements Runnable {
    protected InetSocketAddress inetAddress;
    protected Object            statusLock;
    protected List<SocketListener>              listeners;

    public SocketStatus(String ipAddress, int port) {
        inetAddress = new InetSocketAddress(ipAddress, port);

        listeners = new LinkedList<SocketListener>();
    }

    public void run() {
        Socket socket = new Socket();
        try {
            System.out.println("Connecting to " + inetAddress);
            socket.connect(inetAddress, 10000);
            socket.close();

            fireSocketLived();
        }
        catch (SocketTimeoutException exc) {
            System.out.println("Socket timed out!");
            fireSocketDied();
        }
        catch (IOException exc) {
            fireSocketDied();
        }
    }

    public void addSocketListener(SocketListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeSocketListener(SocketListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    protected void fireSocketLived() {
        synchronized (listeners) {
            for (int index = listeners.size() - 1; index >= 0; index--) {
                SocketListener listener = (SocketListener) listeners.get(index);
                listener.socketLived();
            }
        }
    }

    protected void fireSocketDied() {
        synchronized (listeners) {
            for (int index = listeners.size() - 1; index >= 0; index--) {
                SocketListener listener = (SocketListener) listeners.get(index);
                listener.socketDied();
            }
        }
    }

    public static void main(String[] args) {
        String ip;

        if (args.length >= 1) {
            System.out.println(args[0]);
            ip = args[0];
        }
        else {
            ip = "192.168.1.180";
        }

        SocketStatus status = new SocketStatus(ip, 22);
        status.addSocketListener(new SocketListener() {
            public void socketLived() {
                System.out.println("Alive.");
                System.exit(0);
            }

            public void socketDied() {
                System.out.println("Dead.");
                System.exit(-1);
            }
        });

        new Thread(status).start();

        // real code wouldn't do this but we are just to stop from exiting the
        // main thread.
        try {
            Thread.sleep(30000);
        }
        catch (InterruptedException exc) {
            exc.printStackTrace();
        }
    }
}
