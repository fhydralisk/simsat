package cn.edu.tsinghua.ee.fi.simsat

import akka.actor.{Actor, ActorLogging, ActorPath}

/**
  * Created by hydra on 2018/1/2.
  */
class Satellite(applier: String) extends Actor with ActorLogging {

  log.info(s"Satellite ${self.path} is deployed.")

  var controllerPath: ActorPath = _
  var topo: ControlMessage.LatencyMap = _

  private val topoApplier: TopoApplier = {
    val applierClassName = applier.split('|')(0)
    val args = applier.split('|').tail
    val clazz = Class.forName(applierClassName)
    clazz.getConstructors()(0).newInstance(args : _*).asInstanceOf[TopoApplier]
  }

  override def receive: Receive = {
    case ControlMessage.DeployCheck() =>
      sender() ! ControlMessage.DeployCheckReply()

    case ControlMessage.ApplyTopo(lmap) =>
      assert(lmap.exists {case (path, _) => context.actorSelection(path) == context.actorSelection(self.path)} )
      topoApplier.applyTopo(
        lmap.find {
          case (path, _) =>
            context.actorSelection(path) == context.actorSelection(self.path)
        }.get._2._2
      )
      controllerPath = sender.path
      topo = lmap
      log.info(s"Topo is applied. $lmap")
      sender() ! ControlMessage.ApplyTopoAck()

    case ControlMessage.Mail() =>
      nextHop(SatelliteMessage.Message(System.currentTimeMillis()))

    case msg : SatelliteMessage.Message =>
      if (!nextHop(msg)) {
        feedResult(msg.start)
      }
      topoApplier.cleanup()
  }

  /**
    * Forward the message to next satellite.
    * @param message:  The message to forward
    * @return True if message is forwarded. False indicates that this satellite is the end of Path.
    */
  private def nextHop(message: SatelliteMessage.Message): Boolean = {
    val seq = topo.toList.sortBy(_._2._1)
    val tails = seq.dropWhile { case (path, _) => context.actorSelection(path) != context.actorSelection(self.path) }

    assert(tails.nonEmpty)
    if (tails.size > 1) {
      topoApplier.doDelay()
      context.actorSelection(tails(1)._1) ! message
      true
    } else
      false
  }

  private def feedResult(timeStart: Long): Unit = {
    context.actorSelection(controllerPath) ! ControlMessage.MailFinish(timeStart, System.currentTimeMillis())
  }
}
