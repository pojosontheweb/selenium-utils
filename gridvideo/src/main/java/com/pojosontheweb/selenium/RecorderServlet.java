package com.pojosontheweb.selenium;

import org.apache.http.HttpStatus;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

public class RecorderServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(RecorderServlet.class.getName());

    private ScreenRecordr recordr;
    private File destDir;

    @Override
    public void init() throws ServletException {
        String vd = System.getProperty(SysProps.webtests.video.dir);
        if (vd == null) {
            throw new ServletException("Unable to initialize RecorderServlet : video dir must be passed via webtests.video.dir sys prop.");
        }
        destDir = new File(vd);
        if (!destDir.exists()) {
            throw new ServletException("Unable to initialize RecorderServlet : video dir doesn't exist " + vd);
        }
        recordr = new ScreenRecordr();
    }

    private void start(HttpServletResponse resp) {
        log.info("Starting Video Recording");
        recordr.start();
        resp.setStatus(HttpStatus.SC_OK);
    }

    private void stop(HttpServletResponse resp, String sessionId) {
        log.info("Stopping Video Recording");
        recordr.stop();
        log.info("Storing video files to " + destDir + " for session:" + sessionId);
        recordr.moveVideoFilesTo(destDir, sessionId);
        log.info("Video files moved for session:" + sessionId);
        resp.setStatus(HttpStatus.SC_OK);
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
                stop(resp, sessionId);
            }
        } else {
            resp.setStatus(HttpStatus.SC_BAD_REQUEST);
            resp.getWriter().write("Bad parameter: unsupported command : " + command);
        }
    }


}
