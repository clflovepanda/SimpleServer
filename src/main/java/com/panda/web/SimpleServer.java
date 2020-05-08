package com.panda.web;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SimpleServer {

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 10, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    private static String staticPath = "";

    public static void start() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(12306);
                    while (true) {
                        final Socket socket = serverSocket.accept();
                        threadPoolExecutor.execute(new Runnable() {
                            public void run() {
                                try {
                                    responseSocket(socket);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private static void responseSocket(Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();
        OriginHttpRequest originHttpRequest = getHttpProtocol(inputStream);
        analysisHttpRequest(originHttpRequest);
    }

    private static void analysisHttpRequest(OriginHttpRequest originHttpRequest) {
        HttpRequest httpRequest = new HttpRequest();
        analysisFirstLine(originHttpRequest.getFirstLine(), httpRequest);

    }

    private static void analysisFirstLine(String firstLine, HttpRequest httpRequest) {
        String[] attrs = firstLine.split(" ");
        if (attrs.length == 3) {
            int index = attrs[1].indexOf("?");
            if (index > 0) {//url带参数
                String path = attrs[1].substring(0, index);
                httpRequest.setPath(path);
                String params = attrs[1].substring(index + 1, attrs[1].length());
                Map<String, String> paramsMap = analysisParams(params);
                httpRequest.setParams(paramsMap);
            } else {//url不带参数
                httpRequest.setPath(attrs[1]);
            }
        }
    }
    private static Map<String, String> analysisParams(String params) {
        String[] paramArr = params.split("&");
        Map<String, String> paramsMap = new HashMap<String, String>(paramArr.length);
        for (String param : paramArr) {
            String[] kv = param.split("=");
            if (kv.length == 2) {
                String key = URLDecoder.decode(kv[0]);
                String value = URLDecoder.decode(kv[1]);
                paramsMap.put(key, value);
            }
        }
        return paramsMap;
    }

    private static OriginHttpRequest getHttpProtocol(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        int protocolStatus = 0;//0首行，1属性，2内容
        OriginHttpRequest originHttpRequest = new OriginHttpRequest();
        while (true) {
            String line = bufferedReader.readLine();
            if (line == null) {
                break;
            }
            if ("".equals(line)) {
                protocolStatus ++;
            }
            if (protocolStatus == 0) {
                originHttpRequest.setFirstLine(line);
            } else if (protocolStatus == 1) {
                originHttpRequest.getAttributes().add(line);
            } else if (protocolStatus > 1) {
                originHttpRequest.getContents().add(line);
            }
        }
        return originHttpRequest;
    }

    private static class OriginHttpRequest {
        private String firstLine;
        private List<String> attributes;
        private List<String> contents;

        public OriginHttpRequest() {
            this.attributes = new ArrayList<String>();
            this.contents = new ArrayList<String>();
        }

        public String getFirstLine() {
            return firstLine;
        }

        public void setFirstLine(String firstLine) {
            this.firstLine = firstLine;
        }

        public List<String> getAttributes() {
            return attributes;
        }

        public void setAttributes(List<String> attributes) {
            this.attributes = attributes;
        }

        public List<String> getContents() {
            return contents;
        }

        public void setContents(List<String> contents) {
            this.contents = contents;
        }
    }

    private static class HttpRequest {
        private String path;
        private String method;
        private Map<String, String> params;
        private Map<String, String> headers;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public Map<String, String> getParams() {
            return params;
        }

        public void setParams(Map<String, String> params) {
            this.params = params;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }
    }

    private static class HttpResponse {
        private String code;
        private String msg;
        private String context;
    }

    public static void main(String[] args) throws IOException {
//        File file = new File("a.txt");
//        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
//        while (true) {
//            String line = bufferedReader.readLine();
//            if (line == null) {
//                break;
//            }
//            System.out.println(line);
//        }
        System.out.println("sad");
    }

}
