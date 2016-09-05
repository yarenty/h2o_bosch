package com.yarenty.bosch

import com.yarenty.bosch.normalized.DataMunging
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.h2o.H2OContext
import water.support.SparkContextSupport

/**
  * Created by yarenty on 05/09/2016.
  * (C)2016 SkyCorp Ltd.
  */
object MLProcessor extends SparkContextSupport {

  val conf = configure("H2O: TalkingData Mobile User Demographics")
  val sc = new SparkContext(conf)

  val h2oContext = new H2OContext(sc).start()

  import h2oContext._
  import h2oContext.implicits._

  implicit val sqlContext = new SQLContext(sc)

  import sqlContext.implicits._

  def main(args: Array[String]) {

    println(s"\n\n H2O CONTEXT is HERE !!!!!!\n")


//    DataMunging.process(h2oContext)

    BuildAdvancedModel.process(h2oContext)


    // Shutdown Spark cluster and H2O
    // h2oContext.stop(stopSparkContext = true)

  }

}
