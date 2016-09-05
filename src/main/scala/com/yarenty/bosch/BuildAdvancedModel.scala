package com.yarenty.bosch

import java.io.{File, FileOutputStream, PrintWriter}
import java.net.URI

import com.yarenty.bosch.utils.Helper
import hex.Distribution
import hex.deeplearning.{DeepLearning, DeepLearningModel}
import hex.deeplearning.DeepLearningModel.DeepLearningParameters
import hex.genmodel.GenModel
import hex.kmeans.{KMeans, KMeansModel}
import hex.kmeans.KMeansModel.KMeansParameters
import hex.naivebayes.{NaiveBayes, NaiveBayesModel}
import hex.naivebayes.NaiveBayesModel.NaiveBayesParameters
import hex.tree.drf.DRFModel.DRFParameters
import hex.tree.drf.{DRF, DRFModel}
import hex.tree.gbm.GBMModel.GBMParameters
import hex.tree.gbm.{GBM, GBMModel}
import org.apache.commons.io.FileUtils
import org.apache.spark.h2o.{H2OContext, H2OFrame}
import org.apache.spark.sql.SQLContext
import org.apache.spark.{h2o, SparkContext, SparkFiles}
import water.{AutoBuffer, Key}
import water.fvec.{Vec, Frame}
import water.parser.{ParseSetup, ParseDataset}
import water.support.SparkContextSupport


import MLProcessor.h2oContext._
import MLProcessor.h2oContext.implicits._
import MLProcessor.sqlContext.implicits._

/**
  * Created by yarenty on 05/09/2016.
  * (C)2016 SkyCorp Ltd.
  */
object BuildAdvancedModel extends SparkContextSupport {


  val data_dir = "/opt/data/bosch/input/"
  val numeric_train_file = "train_numeric.csv"
  val numeric_test_file = "test_numeric.csv"

  def process(h2oContext: H2OContext) {

    val sc = h2oContext.sparkContext

    import h2oContext._
    import h2oContext.implicits._
    implicit val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._


    println(s"\n\n LETS MODEL\n")


    addFiles(sc, absPath(data_dir + numeric_train_file))
    val trainURI = new URI("file:///" + SparkFiles.get(numeric_train_file))
    val trainData = new h2o.H2OFrame(trainURI)
    trainData.colToEnum(Array("Response"))

    val model = DRFModel(trainData)



    addFiles(sc, absPath(data_dir + numeric_test_file))
    val testURI = new URI("file:///" + SparkFiles.get(numeric_test_file))
    val testData = new h2o.H2OFrame(testURI)
    val predict = model.score(testData)

    println(predict)

    val predVec = predict.lastVec // only one

    val out = testData.subframe(Array("Id"))
    out.add("Response",predict.vec("predict"))

    Helper.saveCSV(out,data_dir + "submit.csv")

    println("=========> off to go!!!")
  }



  /** ****************************************************
    * MODELS
    * *****************************************************/



  def DRFModel(train: H2OFrame): DRFModel = {

    val params = new DRFParameters()
    params._train = train.key
    //params._distribution = Distribution.Family.gaussian
    params._response_column = "Response"
    params._ignored_columns = Array("Id")
    params._ignore_const_cols = true
    //   params._seed =  -6242730077026816667 //-1188814820856564594L  //5428260616053944984
    params._ntrees = 50
//    params._max_depth = 12
    params._distribution = Distribution.Family.AUTO

    println("BUILDING:" + params.fullName)
    val dl = new DRF(params)
    dl.trainModel.get
  }


  def DLModel(train: H2OFrame): DeepLearningModel = {


    val params = new DeepLearningParameters
    params._train = train.key
    params._response_column = "Response"
    params._ignored_columns = Array("Id")
    params._ignore_const_cols = true

//    params._seed = -8823609696683622000L // 6433149976926940000L    //-8996666368897430268

//    params._hidden = Array(200, 200) //Feel Lucky
    // params._hidden = Array(512) //Eagle Eye
    // params._hidden = Array(64,64,64) //Puppy Brain
     params._hidden = Array(32,32,32) //Junior Chess Master
//     params._hidden = Array(32,32,32,32,32) //Junior Chess Master


    params._epochs = 200.0
    params._standardize = true
    params._score_each_iteration = true
    params._variable_importances = true


    println("BUILDING:" + params.fullName)
    val dl = new DeepLearning(params)
    dl.trainModel.get
  }

}

