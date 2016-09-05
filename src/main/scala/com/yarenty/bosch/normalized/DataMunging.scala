package com.yarenty.bosch.normalized

import java.io.File
import com.yarenty.bosch.utils.Helper
import org.apache.spark._
import org.apache.spark.h2o._
import org.joda.time.format.DateTimeFormat
import water._
import water.fvec._

import org.apache.spark.sql.{DataFrame, SQLContext}
import water.parser.BufferedString
import water.support.ParseSupport._
import water.support.SparkContextSupport


/**
  * Created by yarenty on 05/09/2016.
  * (C)2016 SkyCorp Ltd.
  */
object DataMunging extends SparkContextSupport {


  val data_dir = "/opt/data/bosch/"
  val numeric_train_file = "train_numeric.csv"

  val output_filename = "train"


  def process(h2oContext: H2OContext) {

    import h2oContext._
    import h2oContext.implicits._
    val sc = h2oContext.sparkContext
    implicit val sqlContext = new SQLContext(sc)


    addFiles(h2oContext.sparkContext,
      absPath(data_dir + numeric_train_file)
    )


    Helper.memoryPrint(sc)


    println(
      s"""
         |!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
         |!!  OUTPUT CREATED: ${output_filename} !!
         |!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
         """.stripMargin)

    //clean
    println("... and cleaned")

  }


  def lineBuilder(out: DataFrame): Frame = {

    val headers = Array("", "")
    val len = headers.length
    println("LEN::" + len)

    val fs = new Array[Futures](len)
    val av = new Array[AppendableVec](len)
    val chunks = new Array[NewChunk](len)
    val vecs = new Array[Vec](len)


    for (i <- 0 until len) {
      fs(i) = new Futures()
      if (i == 0 || i == 1 || i == 2)
        av(i) = new AppendableVec(new Vec.VectorGroup().addVec(), Vec.T_STR)
      else
        av(i) = new AppendableVec(new Vec.VectorGroup().addVec(), Vec.T_NUM)
      chunks(i) = new NewChunk(av(i), 0)
    }

    println("Structure is there")
    out.head(20).foreach(ae => {
      //collect


      chunks(0).addStr(ae.getString(0)) //device_id
      chunks(1).addStr(ae.getString(1))
      chunks(2).addNum(ae.getString(2).toInt)
      chunks(3).addStr(ae.getString(3))
      chunks(4).addStr(ae.getString(4))
      chunks(5).addStr(ae.getString(5))

      for (i <- 6 until len) chunks(i).addNum(ae.getDouble(i - 6))

    })

    println("Finalize")

    for (i <- 0 until len) {
      chunks(i).close(0, fs(i))
      vecs(i) = av(i).layout_and_close(fs(i))
      fs(i).blockForPending()
    }

    val key = Key.make("DataMunged")
    return new Frame(key, headers, vecs)

  }

}
