package cn.edu.tsinghua.ee.fi.simsat.util

/**
  * Created by hydra on 2018/1/2.
  */
trait LatencyReader {
  def latencies(path: String): List[List[Long]]
}
