package cn.edu.tsinghua.ee.fi.simsat

/**
  * Created by hydra on 2018/1/2.
  */

/**
  * SleepLatencyApplier
  * Use a thread sleep to simulate a latency.
  */
class SleepLatencyApplier extends TopoApplier {
  private var sleepTime: Long = 0

  override def applyTopo(latency: Long): Unit = {
    sleepTime = latency
  }

  override def cleanup(): Unit = {
    sleepTime = 0
  }

  override def doDelay(): Unit = {
    Thread.sleep(sleepTime)
  }
}
