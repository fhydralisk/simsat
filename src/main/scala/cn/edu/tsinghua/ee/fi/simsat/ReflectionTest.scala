package cn.edu.tsinghua.ee.fi.simsat

/**
  * Created by hydra on 2018/1/2.
  */
trait Test {
  def p
}

class Test1(arg: String) extends Test {
  def p = println(arg)
}

class Test2 extends Test{
  def p = println("test2")
}
object ReflectionTest {
  val applier = "cn.edu.tsinghua.ee.fi.simsat.Test2"

  private val topoApplier: Test = {
    val applierClassName = applier.split('|')(0)
    val args = applier.split('|').tail
    val clazz = Class.forName(applierClassName)
    clazz.getConstructors()(0).newInstance(args : _*).asInstanceOf[Test]
  }
  def main(args: Array[String]): Unit = {
    topoApplier.p
  }
}
