package cn.edu.tsinghua.ee.fi.simsat.util

/**
  * Created by hydra on 2018/1/2.
  */

import scala.io.Source


class HopBasedReader extends LatencyReader {

  override def latencies(path: String): List[List[Long]] = {
    val file = Source.fromFile(path)
    file.getLines() map { line =>
      line.split(' ') map { _.toDouble * 1000 } map { _.toLong } toList
    } toList
  }

}
