package com.pojosontheweb.tastecloud.woko

import com.spotify.docker.client.DefaultDockerClient
import com.spotify.docker.client.DockerClient
import com.spotify.docker.client.DockerRequestException
import com.spotify.docker.client.LogStream
import com.spotify.docker.client.messages.ContainerConfig
import com.spotify.docker.client.messages.ContainerCreation
import com.spotify.docker.client.messages.HostConfig
import woko.util.WLogger

class DockerManager {

    private static final WLogger logger = WLogger.getLogger(DockerManager.class)

    static final String KEY = 'DockerManager'

    def startRun(
        String imageName,
        String dockerUrl,
        File dataDir,
        String tasteFileRelativePath) {

        logger.info("Starting run, dockerUrl=$dockerUrl, dataDir=$dataDir")

        final DockerClient docker = new DefaultDockerClient(dockerUrl)

        final String image = imageName

        // Pull image
//        docker.pull(image)

        String target = '/mnt/target'
        String cfg = '/mnt/cfg.taste'
        String tests = '/mnt/' + tasteFileRelativePath
        final String[] command = ["/run-taste.sh", "-d", target, "-c", cfg, tests]
        final ContainerConfig config =
            ContainerConfig
                .builder()
                .image(image)
                .cmd(command)
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

            def exit = docker.waitContainer(id)
            logger.info("$id done : $exit")

            LogStream logStream = docker.logs(id, DockerClient.LogsParameter.STDOUT, DockerClient.LogsParameter.STDERR)
            logger.info("Docker log : \n${logStream.readFully()}")
            logStream.close()

            def info = docker.inspectContainer(id);
            logger.info("Exit status: ${info.state().exitCode()}")

            return info

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
