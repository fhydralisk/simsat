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
class NetworkLatencyApplier(interface: String) extends TopoApplier {
  override def applyTopo(latency: Long): Unit = {
    cleanup()
    s"tc qdisc add dev s$interface root netem delay ${latency}ms".!
  }

  override def cleanup(): Unit = {
    s"tc qdisc del dev $interface root".!
  }

  override def doDelay(): Unit = {
    // Do nothing
  }
}
