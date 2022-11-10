package httpserver;

/**
 *
 * @author An Ngo - Phu Xuan
 */
import java.io.IOException;
import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;

public class HTTPServer {

        private static final int LINGER_TIME = 5000;
        private static final File ROOT = new File("D:\\www");
        private static final int PORT = 8080;

        private void serve() {
            try {
                /**
                 * default InetAddress is local IP address and backlog (max
                 * number of connection) is 50
                 */
                ServerSocket sSoc = new ServerSocket(HTTPServer.PORT);
                while (true) {
                    Socket connectedClient = sSoc.accept();
                    connectedClient.setSoLinger(true, HTTPServer.LINGER_TIME);
                    /**
                     * MultiThread Handling: Our 'main' thread can only handle a
                     * socket, so we need multiple thread to handle multiple
                     * socket that connect to our server. We do MultiThreading
                     * by implements Runnable Interface
                     */
                    Thread handler = new Thread(
                            new RequestHandler(connectedClient, ROOT));

                    handler.setPriority(Thread.MAX_PRIORITY);
                    handler.start();
                }
            } catch (IOException e) {
                System.err.println("Server Failure!");
            }
        }
    
    /**
     * @param args [0] is File directory to 'root' - where it will search for
     * requested files, [1] is Port which the server will listen on.
     * But they're not used here....
     */
    public static void main(String args[]) {
        HTTPServer hS = new HTTPServer();
        hS.serve();
    }
}
