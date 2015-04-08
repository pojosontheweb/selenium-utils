package com.pojosontheweb.tastecloud

import com.pojosontheweb.tastecloud.woko.DockerManager
import com.spotify.docker.client.DefaultDockerClient
import com.spotify.docker.client.DockerClient
import com.spotify.docker.client.LogMessage
import com.spotify.docker.client.LogStream
import com.spotify.docker.client.messages.ContainerConfig
import com.spotify.docker.client.messages.ContainerCreation
import com.spotify.docker.client.messages.ContainerInfo
import com.spotify.docker.client.messages.HostConfig
import com.spotify.docker.client.messages.PortBinding
import org.junit.Ignore
import org.junit.Test

import java.nio.ByteBuffer

class DockerManagerTest {

    @Test
    @Ignore
    void test() {
        def d = new File('/media/psf/projects/selenium-utils/taste/docker/sample')
        new DockerManager().startRun('http://10.211.55.16:2375/', d) { LogMessage lm ->
            ByteBuffer bb = lm.content()
            byte[] b = new byte[bb.remaining()]
            bb.get(b)
            print new String(b, 'utf-8')
        }
    }

//    @Test
//    void test2() {
//        // Create a client based on DOCKER_HOST and DOCKER_CERT_PATH env vars
//        final DockerClient docker = new DefaultDockerClient('http://10.211.55.16:2375/')
//
//// Pull image
//        docker.pull("busybox");
//
//// Create container with exposed ports
//        final String[] ports = ["9980", "9922"];
//        final ContainerConfig config = ContainerConfig.builder()
//            .image("busybox").exposedPorts(ports)
//            .cmd("sh", "-c", "while :; do sleep 1; done")
//            .build();
//
//// bind container ports to host ports
//        final Map<String, List<PortBinding>> portBindings = new HashMap<String, List<PortBinding>>();
//        for(String port : ports) {
//            List<PortBinding> hostPorts = new ArrayList<PortBinding>();
//            hostPorts.add(PortBinding.of("0.0.0.0", port));
//            portBindings.put(port, hostPorts);
//        }
//        final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();
//
//        final ContainerCreation creation = docker.createContainer(config);
//        final String id = creation.id();
//
//// Inspect container
//        final ContainerInfo info = docker.inspectContainer(id);
//
//// Start container
//        docker.startContainer(id, hostConfig);
//
//// Exec command inside running container with attached STDOUT and STDERR
//        final String[] command = ["bash", "-c", "ls"];
//        final String execId = docker.execCreate(id, command, DockerClient.ExecParameter.STDOUT, DockerClient.ExecParameter.STDERR);
//        final LogStream output = docker.execStart(execId);
//        final String execOutput = output.readFully();
//
//        print execOutput
//
//// Kill container
//        docker.killContainer(id);
//
//// Remove container
//        docker.removeContainer(id);
//    }
}
