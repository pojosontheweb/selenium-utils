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
        recordr = new ScreenRecordr();
        destDir = new File(System.getProperty("java.io.tmpdir") +
            File.separator + UUID.randomUUID().toString());
        destDir.mkdirs();
    }

    private void start(HttpServletResponse resp) {
        recordr.start();
        resp.setStatus(HttpStatus.SC_OK);
    }

    private void stop(HttpServletResponse resp) {
        recordr.stop();
        recordr.moveVideoFilesTo(destDir, "test");
        resp.setStatus(HttpStatus.SC_OK);
    }

    private void download(HttpServletResponse resp) {
        File vidFile = new File(destDir, "test-1.mov");

    }

    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String command = req.getParameter("command");
        if(command == null) {
            resp.setStatus(HttpStatus.SC_BAD_REQUEST);
            resp.getWriter().write("Missing parameter: 'command'");
            return;
        }

        if(command.equalsIgnoreCase("start")) {
            start(resp);
        } else if(command.equalsIgnoreCase("stop")) {
            stop(resp);
        } else if (command.equalsIgnoreCase("download")) {
            download(resp);
        }
        else {
            resp.setStatus(HttpStatus.SC_BAD_REQUEST);
            resp.getWriter().write("Bad parameter: unsupported command : " + command);
        }
    }


}
