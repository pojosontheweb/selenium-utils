package com.pojosontheweb.taste

import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.ecs.AmazonECSClient
import com.amazonaws.services.ecs.model.ContainerDefinition
import com.amazonaws.services.ecs.model.CreateClusterRequest
import com.amazonaws.services.ecs.model.CreateClusterResult
import com.amazonaws.services.ecs.model.DeleteClusterRequest
import com.amazonaws.services.ecs.model.DeleteClusterResult
import com.amazonaws.services.ecs.model.DeregisterTaskDefinitionRequest
import com.amazonaws.services.ecs.model.DeregisterTaskDefinitionResult
import com.amazonaws.services.ecs.model.ListClustersResult
import com.amazonaws.services.ecs.model.RegisterTaskDefinitionRequest
import com.amazonaws.services.ecs.model.RegisterTaskDefinitionResult
import org.junit.Test

class AwsTest {

    @Test
    void doIt() {

        AmazonECSClient ecs = new AmazonECSClient()
        Region region = Region.getRegion(Regions.EU_WEST_1)
        ecs.setRegion(region)

        println "Clusters:"
        ListClustersResult list = ecs.listClusters()
        list.clusterArns.each {
            println it
        }

        println "Task Defs:"
        ecs.listTaskDefinitions().taskDefinitionArns.each {
            println it
        }

        println "Creating cluster"

        // create a cluster
        final String clusterName = "test-cluster-${UUID.randomUUID().toString()}"
        CreateClusterRequest ccReq = new CreateClusterRequest()
        ccReq.setClusterName(clusterName)
        CreateClusterResult ccRes = ecs.createCluster()
        String clusterArn = ccRes.cluster.clusterArn

        println "Created cluster : $clusterArn"
        try {

            // create a task def
            println "Register task defs"
            RegisterTaskDefinitionRequest regReq = new RegisterTaskDefinitionRequest()
            regReq.containerDefinitions = [
                new ContainerDefinition(
                    name: 'taste-help-cntr',
                    image: 'pojosontheweb/taste',
                    command: ['/run-taste.sh', '-h'],
                    cpu: 10,
                    memory: 300
                )
            ]
            regReq.family = 'taste-help-family'
            RegisterTaskDefinitionResult regRes = ecs.registerTaskDefinition(regReq)
            try {
                println "Task def registered : ${regRes.taskDefinition.taskDefinitionArn}"

                // TODO run the task

            } finally {
                // drop the task definition
                println "De-register task def"
                DeregisterTaskDefinitionResult deRegRes = ecs.deregisterTaskDefinition(new DeregisterTaskDefinitionRequest(taskDefinition: regRes.taskDefinition.taskDefinitionArn))
                println "Taskdef de-registered : $deRegRes.taskDefinition.taskDefinitionArn"
            }

        } finally {
            println "Deleting cluster $clusterArn"
            DeleteClusterResult dcRes = ecs.deleteCluster(new DeleteClusterRequest(cluster: clusterArn))
            println "Deleted cluster : status = ${dcRes.cluster.status}"

            list = ecs.listClusters()
            list.clusterArns.each {
                println it
            }
        }
    }

}
