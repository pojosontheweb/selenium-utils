#!/usr/bin/env groovy

def cli = new CliBuilder(usage: 'grid-start.groovy [options]')
cli.with {
	h 	longOpt: 'help', 'Show usage information'
	hh 	longOpt: 'host-ip', args:1, argName:'host', 'Hostname/IP'
	vd 	longOpt: 'video-dir', args:1, argName: 'video-dir', 'Path to store videos to (must exist)'
	n 	longOpt: 'nb-nodes', args:1, argName: 'nb-nodes', 'Number of nodes to start'
	hu	longOpt: 'hub-url', args:1, argName: 'hub-url', 'Do not start the hub, use provided URL instead'
}

def options = cli.parse(args)

if (!options || options.h) {
	cli.usage()
	return
}

def exitWithError = { String msg ->
	println msg
	cli.usage()
	System.exit(255)
}

if (!options.hh) {
	exitWithError "host must be provided"
}

if (!options.n) {
	exitWithError "Nb nodes must be provided"
}

def exec = { String... commandArgs ->
//	println "Executing command :"
//	println commandArgs.join(" ")
	def p = new ProcessBuilder(commandArgs)
		.redirectErrorStream(true)
		.start()
	p.waitFor()
	return p.text
}

if (options.hu) {
	// no-hub, start nodes only
	println "TODO"
	return
}

// start the hub
String[] startHubCommand = ["docker", "run", "--net=host", "-tid", "-p", "4444:4444", "pojosontheweb/selgrid", "/grid/run-hub.sh"]
String hubId = exec(startHubCommand)
int started = 0
if (hubId) {
	print "[Hub] ${options.hh}:4444 $hubId"
	int nbNodes = Integer.parseInt(options.n)
	int start = 5555
	String videoDir = options.vd ?: '/tmp'
	String host = options.hh
	(start..(start + nbNodes - 1)).each { int port ->
		String[] nodeCommand = ["docker", "run", "--security-opt", "seccomp=unconfined", "-v", "/dev/shm:/dev/shm", "--net=host", "-tid", "-p", "$port:$port", "-v", "$videoDir:/grid/videos", "pojosontheweb/selgrid", "/grid/run-node.sh", "$port", "$host", "http://$host:4444/grid/register"]
		String nodeId = exec(nodeCommand)
		print "[Node] ${options.hh}:$port $nodeId"
		started++
	}
}
println "Started $started nodes"
