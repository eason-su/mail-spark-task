package com.chongdianleme.mail
import com.chongdianleme.mail.SVMJob.readParquetFile
import org.apache.spark._
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.evaluation.{BinaryClassificationMetrics, MulticlassMetrics}
import org.apache.spark.mllib.util.MLUtils
import scopt.OptionParser
import scala.collection.mutable.ArrayBuffer
/**
* Created by 充电了么App-陈敬雷
* 官网：http://chongdianleme.com/
* 朴素贝叶斯法是基于贝叶斯定理与特征条件独立假设的分类方法。
*/
object NaiveBayesJob {
  case class Params(
                     inputPath: String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\sample_libsvm_data.txt\\",
                     outputPath: String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\NaiveBayesOut\\",
                     modelPath: String = "file:///D:\\chongdianleme\\chongdianleme-spark-task\\data\\NaiveBayesModel\\",
                     mode: String = "local"
                   )
  def main(args: Array[String]) {
    val defaultParams = Params()
    val parser = new OptionParser[Params]("naiveBayesJob") {
      head("naiveBayesJob: 解析参数.")
      opt[String]("inputPath")
        .text(s"inputPath 输入目录, default: ${defaultParams.inputPath}}")
        .action((x, c) => c.copy(inputPath = x))
      opt[String]("outputPath")
        .text(s"outputPath 输入目录, default: ${defaultParams.outputPath}}")
        .action((x, c) => c.copy(outputPath = x))
      opt[String]("modelPath")
        .text(s"modelPath 模型输出, default: ${defaultParams.modelPath}}")
        .action((x, c) => c.copy(modelPath = x))
      opt[String]("mode")
        .text(s"mode 运行模式, default: ${defaultParams.mode}")
        .action((x, c) => c.copy(mode = x))
      note(
        """
          |naiveBayes dataset:
        """.stripMargin)
    }
    parser.parse(args, defaultParams).map { params => {
      println("参数值：" + params)
      trainNaiveBayes(params.inputPath, params.outputPath, params.modelPath, params.mode)
    }
    } getOrElse {
      System.exit(1)
    }
  }
  /**
  * 贝叶斯模型训练
  * @param inputPath 输入目录，格式如下
  *第一列是类的标签值，后面的都是特征值，多个特征以空格分割：
  *0 128:51 129:159 130:253 131:159 132:50 155:48 156:238 157:252 158:252 159:252 160:237 182:54 183:227 184:253 185:252 186:239 187:233 188:252 189:57 190:6 208:10 209:60 210:224 211:252 212:253 213:252 214:202 215:84 216:252 217:253 218:122 236:163 237:252 238:252 239:252 240:253 241:252 242:252 243:96 244:189 245:253 246:167 263:51 264:238 265:253 266:253 267:190 268:114 269:253 270:228 271:47 272:79 273:255 274:168 290:48 291:238 292:252 293:252 294:179 295:12 296:75 297:121 298:21 301:253 302:243 303:50 317:38 318:165 319:253 320:233 321:208 322:84 329:253 330:252 331:165 344:7 345:178 346:252 347:240 348:71 349:19 350:28 357:253 358:252 359:195 372:57 373:252 374:252 375:63 385:253 386:252 387:195 400:198 401:253 402:190 413:255 414:253 415:196 427:76 428:246 429:252 430:112 441:253 442:252 443:148 455:85 456:252 457:230 458:25 467:7 468:135 469:253 470:186 471:12 483:85 484:252 485:223 494:7 495:131 496:252 497:225 498:71 511:85 512:252 513:145 521:48 522:165 523:252 524:173 539:86 540:253 541:225 548:114 549:238 550:253 551:162 567:85 568:252 569:249 570:146 571:48 572:29 573:85 574:178 575:225 576:253 577:223 578:167 579:56 595:85 596:252 597:252 598:252 599:229 600:215 601:252 602:252 603:252 604:196 605:130 623:28 624:199 625:252 626:252 627:253 628:252 629:252 630:233 631:145 652:25 653:128 654:252 655:253 656:252 657:141 658:37
  *1 159:124 160:253 161:255 162:63 186:96 187:244 188:251 189:253 190:62 214:127 215:251 216:251 217:253 218:62 241:68 242:236 243:251 244:211 245:31 246:8 268:60 269:228 270:251 271:251 272:94 296:155 297:253 298:253 299:189 323:20 324:253 325:251 326:235 327:66 350:32 351:205 352:253 353:251 354:126 378:104 379:251 380:253 381:184 382:15 405:80 406:240 407:251 408:193 409:23 432:32 433:253 434:253 435:253 436:159 460:151 461:251 462:251 463:251 464:39 487:48 488:221 489:251 490:251 491:172 515:234 516:251 517:251 518:196 519:12 543:253 544:251 545:251 546:89 570:159 571:255 572:253 573:253 574:31 597:48 598:228 599:253 600:247 601:140 602:8 625:64 626:251 627:253 628:220 653:64 654:251 655:253 656:220 681:24 682:193 683:253 684:220
  * @param modelPath 模型持久化存储路径
  */
  def trainNaiveBayes(inputPath : String,outputPath:String,modelPath:String,mode:String): Unit = {
    val startTime = System.currentTimeMillis()
    val sparkConf = new SparkConf().setAppName("naiveBayesJob")
    sparkConf.set("spark.sql.warehouse.dir", "file:///C:/warehouse/temp/")
    sparkConf.setMaster(mode)
    val sc = new SparkContext(sparkConf)
    //加载训练数据，svm格式的数据，贝叶斯要求数据特征必须是非负数
    val data = MLUtils.loadLibSVMFile(sc,inputPath)
    //训练数据，随机拆分数据80%做为训练集，20%作为测试集
    val splitsData = data.randomSplit(Array(0.8, 0.2))
    val (trainningData, testData) = (splitsData(0), splitsData(1))
    //训练模型，拿80%数据作为训练集
    val model = NaiveBayes.train(trainningData)
    //模型持久化存储到文件
    model.save(sc,modelPath)
    val trainendTime = System.currentTimeMillis()
    // 用测试集来评估模型的效果
    val scoreAndLabels = testData.map { point =>
      //基于模型来预测归一化后的数据特征属于哪个分类标签
      val prediction = model.predict(point.features)
      (prediction, point.label)
    }
    // 模型评估指标：AUC   二值分类通用指标ROC曲线面积AUC
    val metrics = new BinaryClassificationMetrics(scoreAndLabels)
    val auROC = metrics.areaUnderROC()
    //打印ROC模型--ROC曲线值，越大越精准,ROC曲线下方的面积（Area Under the ROC Curve, AUC）提供了评价模型平均性能的另一种方法。如果模型是完美的，那么它的AUC = 1，如果模型是个简单的随机猜测模型，那么它的AUC = 0.5，如果一个模型好于另一个，则它的曲线下方面积相对较大
    println("Area under ROC = " + auROC)
    //模型评估指标：准确度
    val metricsPrecision = new MulticlassMetrics(scoreAndLabels)
    val precision = metricsPrecision.precision
    println("precision = " + precision)
    val predictEndTime = System.currentTimeMillis()
    val time1 = s"训练时间：${(trainendTime - startTime) / (1000 * 60)} 分钟"
    val time2 = s"预测时间：${(predictEndTime - trainendTime) / (1000 * 60)} 分钟"
    val auc = s"AUC:$auROC"
    val ps = s"precision$precision"
    val out = ArrayBuffer[String]()
    out += ("贝叶斯算法:", time1, time2, auc, ps)
    sc.parallelize(out, 1).saveAsTextFile(outputPath)
    sc.stop()
    //查看下训练模型文件里的内容
    readParquetFile(modelPath + "data/*.parquet", 8000)
  }
}