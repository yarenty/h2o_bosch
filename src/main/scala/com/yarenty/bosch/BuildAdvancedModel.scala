package com.yarenty.bosch

import java.io.{File, FileOutputStream, PrintWriter}
import java.net.URI

import com.yarenty.bosch.utils.Helper
import hex.Distribution
import hex.ScoreKeeper.StoppingMetric
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

    val model = DLModel(trainData)



    addFiles(sc, absPath(data_dir + numeric_test_file))
    val testURI = new URI("file:///" + SparkFiles.get(numeric_test_file))
    val testData = new h2o.H2OFrame(testURI)
    val predict = model.score(testData)

    println(predict)

    val predVec = predict.lastVec // only one

    val out = testData.subframe(Array("Id"))
    out.add("Response", predict.vec("predict"))

    Helper.saveCSV(out, data_dir + "submit.csv")

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
    params._stopping_metric  = StoppingMetric.AUC

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
    params._epochs = 500.0

    params._activation = DeepLearningParameters.Activation.TanhWithDropout
    params._input_dropout_ratio =0.02
    params._hidden_dropout_ratios = Array(0.05,0.05,0.05,0.05,0.05,0.05)

//    params._score_each_iteration = true
    params._variable_importances = true

    params._max_w2  = 10
    params._stopping_metric =  StoppingMetric.MSE
    params._stopping_rounds = 42

    params._adaptive_rate = false
    params._rate = 0.01
    params._rate_annealing = 2e-6

    params._momentum_start=0.2
    params._momentum_stable=0.4
    params._momentum_ramp=1e7

    params._l1=1e-5
    params._l2=1e-5


    println("BUILDING:" + params.fullName)
    val dl = new DeepLearning(params)
    dl.trainModel.get
  }

}

