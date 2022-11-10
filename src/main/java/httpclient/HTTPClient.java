package httpclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.net.Socket;

/**
 * Send an HTTP request and print respond *
 */
public class HTTPClient {

    private static class Server {

        private final int timeOut = 10000;
        private final String address;
        private final Integer port;
        private final Http http;

        Server(String addr, String port) {
            this.address = addr;
            this.port = Integer.valueOf(port);
            this.http = new Http(this.address);
        }
    }

    private static class Http {

        private final String request = "Get / HTTP/1.1";
        private String hostHeader = "Host: ";

        Http(String HostServer) {
            hostHeader += HostServer;
        }
    }

    /**
     * @param args [0] is Host, [1] is Port
     */
    public static void main(String[] args) {
        Server sv = new Server(args[0], args[1]);

        try {
            
            Socket cSoc = new Socket(sv.address, sv.port);
            cSoc.setSoTimeout(sv.timeOut);
            
            
            OutputStream outServer = cSoc.getOutputStream();
            PrintWriter messageToServer = new PrintWriter(outServer);
            messageToServer.println(sv.http.request);
            messageToServer.println(sv.http.hostHeader);
            messageToServer.println();
            messageToServer.flush();
            
            
            BufferedReader fromServer = new BufferedReader(
                    new InputStreamReader(
                            cSoc.getInputStream()
                    )
            );
            String messageFromServer;
            while ((messageFromServer = fromServer.readLine()) != null){
                System.out.println(messageFromServer);
            }
            
            
        } catch (IOException e) {
        }
    }
}
