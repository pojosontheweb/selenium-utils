package com.pojosontheweb.selenium;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.Registry;
import org.openqa.grid.internal.TestSession;
import org.openqa.selenium.remote.internal.HttpClientFactory;

import java.util.logging.Logger;

public class NodeProxy extends org.openqa.grid.selenium.proxy.DefaultRemoteProxy {

    private static final Logger log = Logger.getLogger(NodeProxy.class.getName());

    private final HttpClient client;
    private final HttpHost remoteHost;
    private final String serviceUrl;

    public NodeProxy(RegistrationRequest request, Registry registry) {
        super(request, registry);
        remoteHost = new HttpHost(getRemoteHost().getHost(), getRemoteHost().getPort());
        HttpClientFactory httpClientFactory = new HttpClientFactory();
        client = httpClientFactory.getHttpClient();
        serviceUrl = getRemoteHost() + "/extra/RecorderServlet";
    }

    @Override
    public void beforeSession(TestSession session) {
        super.beforeSession(session);
        HttpPost r = new HttpPost(serviceUrl + "?command=start");
        try {
            HttpResponse response = client.execute(remoteHost, r);
            if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                log.warning("Could not start video reporting: " + EntityUtils.toString(response.getEntity()));
                return;
            }
            log.info("Started recording for new session on node: " + getId());

        } catch (Exception e) {
            log.warning("Could not start video reporting due to exception: " + e.getMessage());
            e.printStackTrace();
        }
        finally {
            r.releaseConnection();
        }
    }

    @Override
    public void afterSession(TestSession session) {
        super.afterSession(session);
        HttpPost r = new HttpPost(serviceUrl + "?command=stop");
        try {
            HttpResponse response = client.execute(remoteHost, r);
            if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                log.warning("Could not stop video reporting: " + EntityUtils.toString(response.getEntity()));
                return;
            }
            log.info("Stopped recording for new session on node: " + getId());

        } catch (Exception e) {
            log.warning("Could not stop video reporting due to exception: " + e.getMessage());
            e.printStackTrace();
        }
        finally {
            r.releaseConnection();
        }
    }
}
