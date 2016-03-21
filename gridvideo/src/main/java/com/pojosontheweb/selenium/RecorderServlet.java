package com.pojosontheweb.selenium;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class RecorderServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(RecorderServlet.class.getName());

    private ScreenRecordr recordr;
    private File destDir;

    private Map<String,String> recordedFiles = new ConcurrentHashMap<>();

    @Override
    public void init() throws ServletException {
        String vd = System.getProperty(SysProps.webtests.video.dir);
        if (vd == null) {
            throw new ServletException("Unable to initialize RecorderServlet : video dir must be passed via webtests.video.dir sys prop.");
        }
        destDir = new File(vd);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        recordr = new ScreenRecordr();
    }

    private void start(HttpServletResponse resp) {
        log.info("Starting Video Recording");
        recordr.start();
        resp.setStatus(HttpStatus.SC_OK);
    }

    private void stop(HttpServletResponse resp, String sessionId, String testName) {
        log.info("Stopping Video Recording");
        recordr.stop();
        log.info("Storing video files to " + destDir + " for session:" + sessionId + ", testName=" + testName);
        recordr.moveVideoFilesTo(destDir, sessionId);
        log.info("Video files moved for session:" + sessionId + ", testName=" + testName);
        resp.setStatus(HttpStatus.SC_OK);
        recordedFiles.put(sessionId, testName);
    }

    private void list(HttpServletResponse resp) throws IOException {
        JsonObject res = new JsonObject();
        JsonArray items = new JsonArray();
        res.add("videos", items);
        for (String sessionId : recordedFiles.keySet()) {
            String testName = recordedFiles.get(sessionId);
            JsonObject item = new JsonObject();
            item.addProperty("sessionId", sessionId);
            item.addProperty("testName", testName != null ? testName : sessionId);
            items.add(item);
        }
        respond(resp, res);
    }

    public static void respond(HttpServletResponse resp, JsonObject o) throws IOException {
        String json = new GsonBuilder()
            .setPrettyPrinting()
            .create()
            .toJson(o);
        resp.setContentType("application/json");
        resp.setContentLength(json.length());
        IOUtils.copy(new ByteArrayInputStream(json.getBytes("utf-8")), resp.getOutputStream());
        resp.getOutputStream().flush();
    }

    private void download(String sessionId, HttpServletResponse response) throws IOException {
        String testName = recordedFiles.get(sessionId);
        File f = new File(destDir, testName + ".mov");
        int len = (int)f.length();
        response.setContentLength(len);
        response.setContentType("video/quicktime");
        IOUtils.copy(new FileInputStream(f), response.getOutputStream());
    }

    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String command = req.getParameter("command");
        if (command == null) {
            resp.setStatus(HttpStatus.SC_BAD_REQUEST);
            resp.getWriter().write("Missing parameter: 'command'");
            return;
        }

        if (command.equalsIgnoreCase("start")) {
            start(resp);
        } else if (command.equalsIgnoreCase("stop")) {
            String sessionId = req.getParameter("sessionId");
            if (sessionId == null) {
                resp.setStatus(HttpStatus.SC_BAD_REQUEST);
                resp.getWriter().write("Missing parameter: 'sessionId'");
            } else {
                String testName = req.getParameter(NodeProxy.REQUEST_PARAM_TEST_NAME);
                if (testName == null) {
                    testName = sessionId;
                }
                stop(resp, sessionId, testName);
            }
        } else if (command.equalsIgnoreCase("list")) {
            list(resp);
        } else if (command.equalsIgnoreCase("download")) {
            String sessionId = req.getParameter("sessionId");
            download(sessionId, resp);
        } else {
            resp.setStatus(HttpStatus.SC_BAD_REQUEST);
            resp.getWriter().write("Bad parameter: unsupported command : " + command);
        }
    }


}
