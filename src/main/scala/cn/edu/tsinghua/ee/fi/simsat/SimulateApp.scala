package cn.edu.tsinghua.ee.fi.simsat

/**
  * Created by hydra on 2018/1/2.
  */

import java.io.File

import akka.actor.{ActorSystem, Address, Deploy, Props}
import akka.cluster.Cluster
import akka.remote.RemoteScope
import cn.edu.tsinghua.ee.fi.simsat.util.LatencyReader
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._

object SimulateApp {
  def main(args: Array[String]): Unit = {
    val configFile: String = args(0)
    val appConfig = ConfigFactory.parseFile(new File(configFile))
    val akkaConfig = appConfig.withFallback(ConfigFactory.load())

    val system = ActorSystem("SimulateSatellite", akkaConfig)
    val cluster = Cluster(system)

    try
      if (appConfig.getBoolean("app.isController"))
        setupController(system, cluster, appConfig)
    catch {
      case _: Throwable =>
    }
  }

  private def doDeployment(config: Config, system: ActorSystem) = {
    val nodesNames = config.getStringList("app.controller.deployment.names").asScala

    val applier = config.getString("app.deployment.applier")
    val parameter = config.getString("app.controller.deployment.parameter")

    // Deploy satellite nodes
    nodesNames.sorted map { nN =>

      val nodeConfig = config.getConfig(s"app.controller.deployment.$nN")
      val nodeAddress = Address("akka.tcp", "SimulateSatellite", nodeConfig.getString("host"), nodeConfig.getInt("port"))

      // Determines whether the class needs a parameter.
      val applierDescriptor = if (parameter.length > 0)
        applier + '|' + nodeConfig.getConfig(parameter)
      else
        applier

      println(s"deploying $nN")
      system.actorOf(Props(classOf[Satellite], applierDescriptor).withDeploy(Deploy(scope = RemoteScope(nodeAddress))))
    }
  }

  def setupController(system: ActorSystem, cluster: Cluster, appConfig: Config): Unit = {
    cluster.registerOnMemberUp {
      val deployment = doDeployment(appConfig, system)
      val dparg = deployment map { _.path }

      val reader = Class.forName(appConfig.getString("app.controller.reader")).newInstance().asInstanceOf[LatencyReader]
      val inputPath = appConfig.getString("app.controller.input.path")
      val latencies = reader.latencies(inputPath)

      system.actorOf(Props(new Controller(dparg.toList, appConfig.getConfig("app.controller"), latencies)))
    }
  }
}
