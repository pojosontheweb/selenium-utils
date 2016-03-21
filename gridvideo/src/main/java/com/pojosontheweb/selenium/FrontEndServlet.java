package com.pojosontheweb.selenium;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.ExceptionEvent;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.openqa.grid.internal.ProxySet;
import org.openqa.grid.internal.Registry;
import org.openqa.grid.internal.RemoteProxy;
import org.openqa.grid.web.servlet.RegistryBasedServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class FrontEndServlet extends RegistryBasedServlet {

    public FrontEndServlet() {
        this(null);
    }

    private CloseableHttpAsyncClient asyncClient;

    public FrontEndServlet(Registry registry) {
        super(registry);
    }

    public static Map<String,String> SESSIONS_AND_URLS = new ConcurrentHashMap<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(10000)
                .setConnectTimeout(10000).build();
            ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();
            PoolingNHttpClientConnectionManager cm =
                new PoolingNHttpClientConnectionManager(ioReactor);
            asyncClient = HttpAsyncClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(cm)
                .build();
            asyncClient.start();
        } catch(Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            asyncClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private class NodeVideos {
//        private final String recorderUrl;
//
//        public NodeVideos(String recorderUrl) {
//            this.recorderUrl = recorderUrl;
//        }
//
//        public void fetch(final CountDownLatch latch, final JsonArray list) {
//            HttpGet get = new HttpGet(recorderUrl + "?command=list");
//            asyncClient.execute(get, new FutureCallback<HttpResponse>() {
//
//                public void completed(final HttpResponse result) {
//                    int status = result.getStatusLine().getStatusCode();
//                    if (status == 200) {
//                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                        try {
//                            result.getEntity().writeTo(bos);
//                            bos.close();
//                            String jsonResp = bos.toString("UTF-8");
//                            JsonObject resp = new Gson().fromJson(jsonResp, JsonObject.class);
//                            JsonArray videos = (JsonArray)resp.get("videos");
//                            Iterator<JsonElement> it = videos.iterator();
//                            while (it.hasNext()) {
//                                JsonObject video = (JsonObject)it.next();
//                                String sessionId = video.get("sessionId").getAsString();
//                                JsonObject jv = new JsonObject();
//                                jv.addProperty("sessionId", sessionId);
//                                jv.addProperty("recorderUrl", recorderUrl);
//                                list.add(jv);
//                            }
//                            System.out.println(resp);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    latch.countDown();
//                }
//
//                public void failed(final Exception ex) {
//                    latch.countDown();
//                    ex.printStackTrace();
//                }
//
//                public void cancelled() {
//                    latch.countDown();
//                }
//
//            });
//
//        }
//    }
//
//    private void fetchVideos(JsonArray jsonArray) {
//        Registry r = getRegistry();
//        ProxySet allProxies = r.getAllProxies();
//        Iterator<RemoteProxy> proxies = allProxies.iterator();
//        List<NodeVideos> requests = new ArrayList<>();
//        while (proxies.hasNext()) {
//            RemoteProxy proxy = proxies.next();
//            URL proxyUrl =  proxy.getRemoteHost();
//            String host = proxyUrl.getHost();
//            int port = proxyUrl.getPort();
//            String recorderUrl = "http://" + host + ":" + port + "/extra/RecorderServlet";
//            NodeVideos vi = new NodeVideos(recorderUrl);
//            requests.add(vi);
//        }
//        final CountDownLatch latch = new CountDownLatch(requests.size());
//        JsonObject res = new JsonObject();
//        res.add("videos", jsonArray);
//        for (final NodeVideos nv : requests) {
//            nv.fetch(latch, jsonArray);
//        }
//        try {
//            latch.await();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//    }

    private void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String command = req.getParameter("command");
        if ("list".equalsIgnoreCase(command)) {
            JsonObject res = new JsonObject();
            final JsonArray files = new JsonArray();
            res.add("videos", files);
            for (String sessionId : SESSIONS_AND_URLS.keySet()) {
                String url = SESSIONS_AND_URLS.get(sessionId);
                JsonObject o = new JsonObject();
                o.addProperty("sessionId", sessionId);
                o.addProperty("recorderUrl", url);
                files.add(o);
            }
            RecorderServlet.respond(resp, res);
        } else if ("download".equalsIgnoreCase(command)) {
            String sessionId = req.getParameter("sessionId");
            download(sessionId, resp);
        } else {
            resp.setStatus(HttpStatus.SC_BAD_REQUEST);
            resp.getWriter().write("Bad parameter: unsupported command : " + command);
        }
    }

    private void download(String sessionId, final HttpServletResponse response) throws IOException {
        String url = SESSIONS_AND_URLS.get(sessionId);
        HttpPost post = new HttpPost(url);
        List<NameValuePair> postParameters = new ArrayList<>();
        postParameters.add(new BasicNameValuePair("command", "download"));
        postParameters.add(new BasicNameValuePair("sessionId", sessionId));
        post.setEntity(new UrlEncodedFormEntity(postParameters));
        final CountDownLatch latch = new CountDownLatch(1);
        asyncClient.execute(post, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse httpResponse) {
                response.setContentType("video/quicktime");
                try {
                    InputStream input = httpResponse.getEntity().getContent();
                    IOUtils.copy(input, response.getOutputStream());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            }

            @Override
            public void failed(Exception e) {
                latch.countDown();

            }

            @Override
            public void cancelled() {
                latch.countDown();

            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }
}
