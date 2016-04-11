#!/usr/bin/env groovy

@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7')

import groovyx.net.http.*
import org.apache.http.*
import org.apache.http.client.*
import org.apache.http.impl.client.*
import org.apache.http.client.methods.*


def cli = new CliBuilder(usage: 'grid-client.groovy [options]')
cli.with {
	h longOpt: 'help', 'Show usage information'
	hh longOpt: 'hub-host', args:1, argName: 'hostname/IP', 'Hub hostname'
	l longOpt: 'list-videos', 'List recorded videos'
	d longOpt: 'download-video', args:1, argName: 'sessionId', 'Download recorded test session'
	u longOpt: 'video-url', args:1, argName: 'sessionId', 'Return the dl url of a video'
	v longOpt: 'verbose', 'Verbose mode'
}

def options = cli.parse(args)

if (!options || options.h) {
	cli.usage()
	return
}

def debug = { String msg ->
	if (options.v) {
		println msg
	}
}

if (!options.hh) {
	println "Hub url is mandatory"
	cli.usage()
	return
} else {
	debug("Using hub at ${options.hh}")
}

if (options.l) {
	// list files...
	new HTTPBuilder("http://${options.hh}:4444/").get(
		path:'/grid/admin/FrontEndServlet',
		query: [command:'list']
	) { resp, json ->
		json.videos.each { println it.sessionId }
	}
} else if (options.d) {
	// download video
	HttpGet req = new HttpGet("http://${options.hh}:4444/grid/admin/FrontEndServlet?command=download&sessionId=${options.d}");
	HttpClient client = new DefaultHttpClient();
	HttpResponse response = client.execute(req);
// validate response code, etc.
	InputStream inputStream = response.getEntity().getContent();
	byte[] buffer = new byte[1024];
	int len = inputStream.read(buffer);
	while (len != -1) {
		System.out.write(buffer, 0, len);
		len = inputStream.read(buffer);
	}
	inputStream.close()
} else if (options.u) {
	println "http://${options.hh}:4444/grid/admin/FrontEndServlet?command=download&sessionId=${options.u}"
}
