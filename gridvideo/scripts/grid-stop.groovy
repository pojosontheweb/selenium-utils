#!/usr/bin/env groovy

def cli = new CliBuilder(usage: 'grid-stop.groovy')
cli.with {
	h 	longOpt: 'help', 'Show usage information'
}

def options = cli.parse(args)

if (!options || options.h) {
	cli.usage()
	return
}

def ps = new ProcessBuilder('docker', 'ps')
	.redirectErrorStream(true)
	.start()
int nbStopped = 0
ps.inputStream.eachLine { line ->
	String[] parts = line.split()
	if (parts[0] != 'CONTAINER') {
		String cId = parts[0]
		String img = parts[1]
		if (img == 'pojosontheweb/selgrid') {
			def p = new ProcessBuilder('docker', 'rm', '-f', cId).start()
			p.waitFor()
			print p.text
			nbStopped++
		}
	}
}
ps.waitFor()
println "$nbStopped containers removed"