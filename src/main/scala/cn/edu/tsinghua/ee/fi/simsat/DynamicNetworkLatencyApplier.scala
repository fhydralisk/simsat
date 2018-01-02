package cn.edu.tsinghua.ee.fi.simsat

import scala.sys.process._

/**
  * Created by hydra on 2018/1/2.
  */

/**
  * NetworkLatencyApplier
  * Performs a simulated network latency by TC tool.
  * @param interface: The name of network interface.
  */
class DynamicNetworkLatencyApplier(interface: String) extends TopoApplier {
  var delay: Long = 0

  override def applyTopo(latency: Long): Unit = {
    cleanup()
    delay = latency
  }

  override def cleanup(): Unit = {
    println(s"tc qdisc del dev $interface root")
    s"tc qdisc del dev $interface root".!
  }

  override def doDelay(): Unit = {
    println(s"tc qdisc add dev $interface root netem delay ${delay}ms")
    s"tc qdisc add dev $interface root netem delay ${delay}ms".!
    Thread.sleep(1)
  }
}
