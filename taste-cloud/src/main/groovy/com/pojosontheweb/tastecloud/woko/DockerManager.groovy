package com.pojosontheweb.tastecloud.woko

import com.spotify.docker.client.DefaultDockerClient
import com.spotify.docker.client.DockerClient
import com.spotify.docker.client.LogStream
import com.spotify.docker.client.messages.ContainerConfig
import com.spotify.docker.client.messages.ContainerCreation
import com.spotify.docker.client.messages.HostConfig
import woko.util.WLogger

class DockerManager {

    private static final WLogger logger = WLogger.getLogger(DockerManager.class)

    static final String KEY = 'DockerManager'

    def startRun(String dockerUrl, File dataDir, Closure logHandler) {

        logger.info("Starting run, dockerUrl=$dockerUrl, dataDir=$dataDir")

        final DockerClient docker = new DefaultDockerClient(dockerUrl)

        final String image = 'taste'

        // Pull image
//        docker.pull(image)

        // Create container with exposed ports
        final ContainerConfig config =
            ContainerConfig
                .builder()
                .image(image)
                .cmd("sh", "-c", "while :; do sleep 1; done")
                .build()

        logger.info("Mounting : ${dataDir.absolutePath}:/mnt")

        final HostConfig hostConfig = HostConfig
            .builder()
            .binds([dataDir.absolutePath + ':/mnt'])
            .build()

        final ContainerCreation creation = docker.createContainer(config)
        final String id = creation.id();

        logger.info("$id created")

        try {

            // Inspect container
//            final ContainerInfo info = docker.inspectContainer(id);

            // Start container
            docker.startContainer(id, hostConfig);

            logger.info("$id started")

            // Exec command inside running container with attached STDOUT and STDERR
            String target = '/mnt/target'
            String cfg = '/mnt/cfg.taste'
            String tests = '/mnt/tests.taste'
            final String[] command = ["/run-taste.sh", "-d", target, "-c", cfg, tests]
            final String execId = docker.execCreate(
                id,
                command,
                DockerClient.ExecParameter.STDOUT,
                DockerClient.ExecParameter.STDERR);
            final LogStream output = docker.execStart(execId);
            logger.info("$id exec $execId")
            try {
                while (output.hasNext()) {
                    logHandler(output.next())
                }
            } catch (Exception e) {
                logger.error("Exception caught reading output. Will exit.", e)
            }
        } finally {

            // Kill container
            logger.info("Cleaning up...")
            try {
                docker.killContainer(id);
                logger.info("$id killed")
            } catch (Exception e) {
                logger.error("Exception caught killing $id", e)
            }

            // Remove container
            try {
                docker.removeContainer(id);
                logger.info("$id removed")
            } catch (Exception e) {
                logger.error("Exception caught removing $id", e)
            }

            logger.info("...done")
        }
    }

}
