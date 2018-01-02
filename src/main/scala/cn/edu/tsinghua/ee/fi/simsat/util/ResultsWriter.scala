package cn.edu.tsinghua.ee.fi.simsat.util

import java.io.FileWriter

/**
  * Created by hydra on 2018/1/2.
  */
class ResultsWriter[T](filename: String) {
  def write(results: List[T]): Boolean = {
    val fw = new FileWriter(filename, false)
    try {
      results foreach { line =>
        fw.write(line.toString + '\n')
      }
      true
    } catch {
      case _: Throwable =>
        false
    }
    finally fw.close()

  }
}
