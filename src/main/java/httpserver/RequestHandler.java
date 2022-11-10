package httpserver;

import java.io.BufferedReader;
import java.net.Socket;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

/**
 * Important Class. I will analyze this class as detail as possible
 */
public class RequestHandler implements Runnable {

    private static final class HttpCommand {

        private static final String SERVER_ID_HEADER = "Server: Httpd 1.0";
        private static final String HTTP_GET_REQ = "GET";
        private static final String HTTP_OK_RES = "HTTP/1.0 200 Oke";
        private static final String HTTP_NOTFOUND_RES = "HTTP/1.0 404 File Not Found";
        private static final String HTTP_NOTFOUND_HTML
                = "<HTML>"
                + "<HEAD>"
                + "<TITLE>FILE NOT FOUND!</TITLE>"
                + "</HEAD>"
                + "<BODY>"
                + "<H1>HTTP Error 404: File Not Found</H1>"
                + "</BODY>"
                + "</HTML>";
        private static final String HTTP_NOTIMPLEMENTED_RES = "HTTP/1.0 501 Not Implemented";
        private static final String HTTP_NOTIMPLEMENTED_HTML
                = "<HTML>"
                + "<HEAD>"
                + "<TITLE>NOT IMPLEMENTED!</TITLE>"
                + "</HEAD>"
                + "<BODY>"
                + "<H1>HTTP Error 501: Not Implemented</H1>"
                + "</BODY>"
                + "</HTML>";
    }

    private Socket client;
    private File rootDir;

    RequestHandler(Socket sock, File dir) {
        this.client = sock;
        this.rootDir = dir;
    }

    // Just an Annotation that this function is an override
    @Override
    public void run() {
        try {
            HttpRequest req = readHttpRequest();
            if (req == null) {
                return;
            }
            if (req.method.equals(HttpCommand.HTTP_GET_REQ)) {
                handleGetRequest(req);
            } else {
                getHandle.sendErrorMessgae(client,
                        HttpCommand.HTTP_NOTIMPLEMENTED_RES,
                        HttpCommand.HTTP_NOTIMPLEMENTED_RES,
                        req.httpVersion);
            }
        } catch (IOException e) {
        } finally {
            try {
                client.close();
            } catch (IOException e) {
            }
        }
    }

    // Please Notice: We don't use java.net.http.HttpRequest class
    // This is a self declared HttpRequest class
    private static class HttpRequest {

        private String method;
        private String path;
        private String httpVersion;
        private List<String> headers;

        HttpRequest(String method, String path, String version) {
            this.method = method;
            this.path = path;
            this.httpVersion = version;

            this.headers = new ArrayList<>();
        }

        private void addHeader(String header) {
            this.headers.add(header);
        }
    }

    private HttpRequest readHttpRequest() throws IOException {
        // Read message from client
        BufferedReader fromClient = new BufferedReader(
                new InputStreamReader(client.getInputStream())
        );
        String requestLine = fromClient.readLine();
        if (requestLine == null) {
            return null;
        }
        /* Split request into many tasks (tokens) */
        String[] requestMainHeaders = requestLine.split(" ");
        // The first Header is Main header contains: method, path, version
        // Ex: GET /index.html HTTP/1.0
        HttpRequest request = new HttpRequest(
                requestMainHeaders[0],
                requestMainHeaders[1],
                requestMainHeaders[2]);

        // Read every lines, which is a Header
        while ((requestLine = fromClient.readLine()) != null
                /*string that remove space*/
                && (requestLine.trim().equals("")) == false) {
            request.addHeader(requestLine);
        }

        return request;
    }

    private static final class getHandle {

        // Remove the first slash at the begining of file path
        private static String removeFirstSlash(String path_w_slash) {
            return path_w_slash.substring(1);
        }

        private static byte[] fileReadToBytes(File rootDir, String path_RelativeToRoot)
                throws IOException {
            File f = new File(rootDir, path_RelativeToRoot);
            try ( FileInputStream fromFile = new FileInputStream(f)) {
                byte[] buf = new byte[(int) f.length()];
                fromFile.read(buf);
                return buf;
            }
        }

        // Return content-type protocol for each type (their extension) of file 
        private static String getMimeFromExtension(String name) {
            if (name.endsWith(".html") || name.endsWith(".htm")) {
                return "text/html";
            }
            if (name.endsWith(".txt") || name.endsWith(".java")) {
                return "text/plain";
            }
            if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
                return "image/jpeg";
            }
            if (name.endsWith(".gif")) {
                return "image/gif";
            }
            if (name.endsWith(".png")) {
                return "image/png";
            }
            if (name.endsWith(".css")) {
                return "text/css";
            }
            if (name.endsWith(".ico")) {
                return "image/x-icon";
            } else {
                return "application/octet-stream";
            }
        }

        private static void sendErrorMessgae(Socket client, String code, String html, String httpVer)
                throws IOException {
            PrintWriter pw = new PrintWriter(client.getOutputStream());
            if (httpVer.startsWith("HTTP/")) {
                pw.println(code);
                pw.println("Date: " + (new Date()));
                pw.println(HttpCommand.SERVER_ID_HEADER);
                pw.println("Content-type: text/html");
                pw.println();
            }
            pw.println(html);
            pw.flush();
        }
    }

    private void handleGetRequest(HttpRequest reqFromClient) throws IOException {
        if (reqFromClient.path.endsWith("/")) {
            reqFromClient.path += "index.html";
        }

        OutputStream resToClient = client.getOutputStream();

        try {
            byte[] fileContent = getHandle.fileReadToBytes(rootDir,
                    getHandle.removeFirstSlash(
                            reqFromClient.path));

            // Write HTTP Server Header to Client
            if (reqFromClient.httpVersion.startsWith("HTTP/")) {
                PrintWriter pw = new PrintWriter(resToClient);
                pw.println(HttpCommand.HTTP_OK_RES);
                pw.println("Date: " + LocalDateTime.now());
                pw.println(HttpCommand.SERVER_ID_HEADER);
                pw.println("Content-length: " + fileContent.length);
                pw.println("Content-type: " + getHandle.getMimeFromExtension(reqFromClient.path));
                pw.println();
                pw.flush();
            }

            // Write Replies content to Client
            resToClient.write(fileContent);
        } catch (IOException e) {
            getHandle.sendErrorMessgae(client,
                    HttpCommand.HTTP_NOTFOUND_RES,
                    HttpCommand.HTTP_NOTFOUND_HTML,
                    reqFromClient.httpVersion);
        }

    }

}
