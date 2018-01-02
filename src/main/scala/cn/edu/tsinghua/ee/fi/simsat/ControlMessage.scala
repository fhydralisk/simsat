package cn.edu.tsinghua.ee.fi.simsat

/**
  * Created by hydra on 2018/1/2.
  */

import akka.actor.ActorPath


object ControlMessage {

  /**
    * Apply topology with network latency.
    * @param latency: Maps satellite name to satellite seq and latency(network or next hop latency).
    */

  type LatencyMap = Map[ActorPath, (Int, Long)]

  final case class DeployCheck()
  final case class DeployCheckReply()

  final case class ApplyTopo(latency: LatencyMap, controller: ActorPath)
  final case class ApplyTopoAck()

  // Ask the first satellite to start sending message.
  final case class Mail()

  // The answer from the last satellite which indicates that the sending process is finished
  final case class MailFinish(tStart: Long, tFinish: Long)
}
