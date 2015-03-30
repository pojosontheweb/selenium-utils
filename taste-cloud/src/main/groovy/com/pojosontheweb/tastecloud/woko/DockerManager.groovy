package com.pojosontheweb.tastecloud.woko

import com.google.common.collect.ImmutableList
import com.spotify.docker.client.DefaultDockerClient
import com.spotify.docker.client.DockerClient
import com.spotify.docker.client.LogMessage
import com.spotify.docker.client.LogStream
import com.spotify.docker.client.messages.ContainerConfig
import com.spotify.docker.client.messages.ContainerCreation
import com.spotify.docker.client.messages.ContainerInfo
import com.spotify.docker.client.messages.HostConfig

class DockerManager {

    static final String KEY = 'DockerManager'

    def startRun(File dataDir, Closure logHandler) {

        // Create a client based on DOCKER_HOST and DOCKER_CERT_PATH env vars
//        final DockerClient docker = DefaultDockerClient.fromEnv().build()
        final DockerClient docker = new DefaultDockerClient('http://10.211.55.16:2375/')

        final String image = 'taste'

        // Pull image
//        docker.pull(image)

        // Create container with exposed ports
        final ContainerConfig config =
            ContainerConfig
                .builder()
                .image(image)
//                .volumes('/mnt')
                .cmd("sh", "-c", "while :; do sleep 1; done")
                .build()

        final HostConfig hostConfig = HostConfig
            .builder()
            .binds([dataDir.absolutePath + ':/mnt'])
            .build()

        final ContainerCreation creation = docker.createContainer(config)
        final String id = creation.id();

        try {

            // Inspect container
            final ContainerInfo info = docker.inspectContainer(id);

            // Start container
            docker.startContainer(id, hostConfig);

            // Exec command inside running container with attached STDOUT and STDERR
            String target = '/mnt/target'
            String cfg = '/mnt/cfg.taste'
            String tests = '/mnt/tests.taste'
//            final String[] command = ["/run-taste.sh", "-d", target, "-c", cfg, tests]
            final String[] command = ["ls", "-l", target, "/mnt", cfg, tests]
            final String execId = docker.execCreate(
                id,
                command,
                DockerClient.ExecParameter.STDOUT,
                DockerClient.ExecParameter.STDERR);
            final LogStream output = docker.execStart(execId);
            try {
                while (output.hasNext()) {
                    logHandler(output.next())
                }
            } catch (Exception e) {
                e.printStackTrace()
            }
        } finally {

            // Kill container
            try {
                docker.killContainer(id);
            } catch (Exception e) {
                e.printStackTrace()
            }

            // Remove container
            try {
                docker.removeContainer(id);
            } catch (Exception e) {
                e.printStackTrace()
            }
        }
    }

}
