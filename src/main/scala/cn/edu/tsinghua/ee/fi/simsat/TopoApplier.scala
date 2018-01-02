package cn.edu.tsinghua.ee.fi.simsat

/**
  * Created by hydra on 2018/1/2.
  */

// The interface for a satellite node to apply topo.
trait TopoApplier {
  def applyTopo(latency: Long): Unit
  def cleanup(): Unit
  def doDelay(): Unit
}
