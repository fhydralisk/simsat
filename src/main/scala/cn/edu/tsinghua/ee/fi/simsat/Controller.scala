package cn.edu.tsinghua.ee.fi.simsat

/**
  * Created by hydra on 2018/1/2.
  */

import akka.actor.{Actor, ActorLogging, ActorPath, Cancellable}
import cn.edu.tsinghua.ee.fi.simsat.util.ResultsWriter
import com.typesafe.config.Config
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * The controller of simulator.
  * @param deployment: Actor deployments, sorted by sequence.
  * @param controllerConfig: The controller's config.
  * @param latencies: input data.
*/
class Controller(deployment: List[ActorPath], controllerConfig: Config, latencies: List[List[Long]]) extends Actor with ActorLogging {

  import context.dispatcher

  context become initializing

  private var deployCheckMap: Map[ActorPath, Boolean] = deployment map { _ -> false} toMap

  private var k = 0

  private var results: List[Long] = List()

  private val check_sch: Cancellable = context.system.scheduler.schedule(5 seconds, 3 seconds) {
    log.info(s"Checking deployment... $deployCheckMap")
    if (deployCheckMap.exists(!_._2))
      deployCheckMap filter {!_._2} foreach { case (ap, _) =>
          context.actorSelection(ap) ! ControlMessage.DeployCheck()
      }
    else {
      startWorking()
    }
  }

  def startWorking(): Unit = {
    log.info("Deployment succeeded. Start the simulation right now.")
    check_sch.cancel
    context become working
    next()
  }

  override def receive: Receive = {
    case _ =>
  }

  def initializing: Receive = {
    case ControlMessage.DeployCheckReply() =>
      log.info(s"${sender.path} is deployed.")
      deployCheckMap = deployCheckMap.updated(sender.path, true)
  }

  def working: Receive = {
    case ControlMessage.MailFinish(tStart, tFinish)=>
      log.info(s"Test $k has completed, duration: ${tFinish-tStart} ms.")
      results :+= (tFinish - tStart)
      if (!next()) {
        saveResult()
      }

    case unhandled @ _ =>
      log.warning(s"Ignoring unhandled message $unhandled")

  }

  def next(): Boolean = {
    if (latencies.size <= k)
      false
    else {
      val thisLatency = latencies(k)

      // Decide which nodes to send the apply topo messages.
      val toApplyTopo = deployment take thisLatency.size

      // Construct a map: (actorpath -> (number, latency))
      val topo = toApplyTopo.zipWithIndex zip thisLatency map {
        case ((nodePath, nodeNumber), latency) =>
          nodePath -> (nodeNumber, latency)
      } toMap

      implicit val timeout: Timeout = 1 second
      val asks = toApplyTopo map { node =>
        context.actorSelection(node) ? ControlMessage.ApplyTopo(topo, self.path)
      }

      // Wait until all satellites received the apply topo message.
      Future.sequence(asks).onComplete {
        case Success(_) =>
          log.info(s"Deploy completed, start sending request. Begin with ${toApplyTopo.head}, end with ${toApplyTopo.last}")
          context.actorSelection(toApplyTopo.head) ! ControlMessage.Mail()
        case Failure(_) =>
          log.error("Cannot finish deployment, test canceled.")
      }

      k+=1
      true
    }
  }

  def saveResult(): Unit = {
    if (new ResultsWriter[Long](controllerConfig.getString("output.path")).write(results)) {
      log.info(s"Result saved to ${controllerConfig.getString("output.path")}")
    } else {
      log.error(s"Cannot save to ${controllerConfig.getString("output.path")}")
    }
    context.system.terminate()
  }

}
