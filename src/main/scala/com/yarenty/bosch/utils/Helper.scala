package com.yarenty.bosch.utils

import java.io.{File, PrintWriter, FileOutputStream}

import hex.tree.drf.DRFModel
import org.apache.spark.SparkContext
import water.AutoBuffer
import water.fvec.{Frame, Vec}

/**
  * Created by yarenty on 05/09/2016.
  * (C)2016 SkyCorp Ltd.
  */
object Helper {

  def saveJavaPOJOModel(model: DRFModel, path: String): Unit = {
    val name = path + System.currentTimeMillis() + ".java"
    val om = new FileOutputStream(name)
    model.toJava(om, false, false)
    println("Java POJO model saved as" + name)
  }

  def saveBinaryModel(model: DRFModel, path: String): Unit = {
    val name = path + System.currentTimeMillis() + ".hex"
    val omab = new FileOutputStream(name)
    val ab = new AutoBuffer(omab, true)
    model.write(ab)
    ab.close()
    println("HEX(iced) model saved as" + name)
  }


  def saveCSV(f: Frame, name: String): Unit = {
    val csv = f.toCSV(true, false)
    val csv_writer = new PrintWriter(new File(name))
    while (csv.available() > 0) {
      csv_writer.write(csv.read.toChar)
    }
    csv_writer.close
  }

  def memoryPrint(sc: SparkContext): Unit = {
    for (m <- sc.getExecutorMemoryStatus) println("MEMORY STATUS:: " + m._1 + " => " + m._2)
  }


  def display[T](c: T, n: String): Unit = {

    if (c.isInstanceOf[Array[T]]) {
      println(s"\n ${n.toUpperCase}")
      for (i <- c.asInstanceOf[Array[T]]) {
        print(i + ", ")
      }
      println
    } else {
      println(s"${n.toUpperCase}  =>  ${c}")
    }
  }

  def display[T](c: Array[Array[T]], n: String): Unit = {
    println(s"\n ${n.toUpperCase}")
    if (c != null)
      for (z <- c) {
        if (z != null) {
          for (i <- z) {
            print(i + ", ")
          }
          println
        }
      }
  }


  def main(args: Array[String]) {
  }

}
