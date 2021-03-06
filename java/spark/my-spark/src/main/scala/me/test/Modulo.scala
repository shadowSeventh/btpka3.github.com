package me.test

import java.util.Date
import java.util.concurrent.atomic.AtomicInteger

import breeze.linalg.{DenseMatrix, Matrix}
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.spark.rdd.RDD
import org.apache.spark.{Accumulable, AccumulableParam, SparkConf, SparkContext}

import scala.Array._
import scala.collection.mutable.ListBuffer

/**
  * Created by zll on 16-5-4.
  */
object Modulo {
  def main9(args: Array[String]): Unit = {
    val m = DenseMatrix(
      (2, 0, 0, 0),
      (0, 4, 0, 1),
      (0, 0, 6, 10),
      (0, 0, 0, 8)
    )
    println(m.forall(_ % 2 == 0))
    println("========")
    println(m)
  }


  // 数据量统计
  def main73(args: Array[String]): Unit = {
    //val json = """{"level":21,"modu":"3","map":["120212","212222","100220","000112","001210"],"pieces":["XXX.,..X.,..XX","XX,X.","...X,X.XX,XXXX","X.,XX,X.",".X..,XXXX,..XX,...X,...X",".X,XX","...XX,...X.,XXXX.,.X...,.X...","X...,X...,XXX.,..XX,..XX","..X.,.XXX,.XX.,XX..,X...","..X.,..X.,..XX,XXX.,XX..","XX,.X,.X"]}"""
    val json =
      """ {"level":27,"modu":"3","map":["21211","20220","20012","22002","22000"],"pieces":["XX.,X..,XXX","X..,XXX","XXX,X.X","..XX,..X.,XXX.","X..,XXX,X..",".X.,XXX,XX.",".X.,.XX,XX.,.X.","X,X,X,X","X,X,X","XX,XX","XXXX,..X.",".X,XX,X.",".XX,XX."]}"""
    val mapper = new ObjectMapper()
    val moduloLevel = mapper.readValue(json, classOf[ModuleLevel])
    println(moduloLevel)
    println("=============")

    val map: Matrix[Int] = mapToMatrix(moduloLevel.map)
    val pieces: Array[Matrix[Int]] = moduloLevel.pieces.map(pieceToMatrix(_))

    val buf = new StringBuilder
    var count = 1L
    pieces.foreach(piece => {
      val length = calcPiecePos(map, piece).length
      count *= length
      buf.append(length)
      buf.append("*")
    })
    buf.setLength(buf.length - 1)
    println("总共有：" + buf.toString() + " = " + count + " 种情形需要遍历")
    //println("Tip： 第21关： 9*20*9*15*3*20*2*3*3*3*15 = 1180980000   I7 CPU 4线程执行 需要3分钟")

  }

  def main(args: Array[String]): Unit = {

    //val json = """{"level":18,"modu":"2","map":["1010","0011","0101","1101","1110"],"pieces":["XX,.X",".X.,.X.,XXX","X.,XX,X.",".X..,XX..,.XXX,.XX.",".X.,XXX",".X..,XX..,XXXX,.X..",".X,.X,XX","X..,X..,XXX,XX.,X..","X.,X.,XX","X.,XX,.X"]}"""
    val json =
      """{"level":20,"modu":"2","map":["111011","001110","001000","110110","110100"],"pieces":["X.X.,XXX.,..XX,..X.","XX.,.X.,XXX,X..","X...,XXXX,.X..,.X..","....XX,XXXXX.,...X..,...X..","X..,X..,XXX","...X.,XXXXX,.X...,.X...","XXX,X..","XXX.,..XX,...X,..XX,..X.",".X,XX","XX,.X,.X"]}"""
    val mapper = new ObjectMapper()
    val moduloLevel = mapper.readValue(json, classOf[ModuleLevel])
    println(moduloLevel)
    println("=============")

    val map: Matrix[Int] = mapToMatrix(moduloLevel.map)
    val pieces: Array[Matrix[Int]] = moduloLevel.pieces.map(pieceToMatrix(_))

    val buf = new StringBuilder
    var count = 1L
    pieces.foreach(piece => {
      val length = calcPiecePos(map, piece).length
      count *= length
      buf.append(length)
      buf.append("*")
    })
    buf.setLength(buf.length - 1)
    println(json)
    println("总共有：" + buf.toString() + " = " + count + " 种情形需要遍历")

    val modu = moduloLevel.modu
    val actualSeconds = new AtomicInteger

    val workerCount = 4

    // 计算每个piece可用的位置
    val piecePosArrArr: Array[Array[PiecePos]] = pieces.map(piece => calcPiecePos(map, piece).map(pos => (piece, pos)))
    var pCount = 1
    var pEnd = 0
    var workerLoopCount = 1
    var minPCount = 100
    for (i <- 0 until piecePosArrArr.length) {
      if (pCount < workerCount || pCount < minPCount) {
        pEnd += 1
        pCount *= piecePosArrArr(i).length
      } else {
        workerLoopCount *= piecePosArrArr(i).length
      }
    }

    println(s"将使用 ${pEnd + 1} / ${pieces.length} 作为分片（$pCount），其余的piece将在一个worker内遍历，共需遍历 $workerLoopCount 次")
    val parPiecePosArrArr = piecePosArrArr.slice(0, pEnd + 1)
    val workerPiecePosArrArr = piecePosArrArr.slice(pEnd, piecePosArrArr.length)
    assert(parPiecePosArrArr.length == pEnd + 1)

    val conf = new SparkConf()
      .setAppName("btpka3")
      .setMaster(s"local[${workerCount}]")

    val sc = new SparkContext(conf)
    val foundResult: Accumulable[ModuResults, ModuResult] = sc.accumulable(new ModuResults())(PiecePosAccParam)


    val rdds: Array[RDD[PiecePos]] = parPiecePosArrArr.map(sc.parallelize(_))
    println("----------------------------111" + rdds.getClass)
    var r: RDD[Array[PiecePos]] = null
    var a = 0
    for (rdd <- rdds) {
      if (rdd == rdds.head) {
        r = rdd.map(Array(_))
      } else {
        r = r.cartesian(rdd).map(t2 => concat(t2._1, Array(t2._2)))
      }
      //val leftRdd = if (r == null) rdds.head else rdd
      //r = leftRdd.map(Array(_)).cartesian(rdd.map(Array(_))).map(t2 => concat(t2._1, t2._2))
    }
    println("=========================@@ r.getNumPartitions" + r.getNumPartitions)
    r = r.repartition(workerCount)
    r.foreach(moduResult => {
      val tmpMap = map.copy
      moduResult.foreach(piecePos => {
        addPieceToMap(tmpMap, piecePos._1, piecePos._2)
      })
      workerPiecePosArrArr.foreach(workerPiecePosArr => {
        workerPiecePosArr.foreach(workerPiecePos => {
          addPieceToMap(tmpMap, workerPiecePos._1, workerPiecePos._2)
        })
      })
      if (isValidResult(tmpMap, modu)) {
        //        println("~@@@@@@@@@@@@@@@@@@ " + moduResult)
        foundResult.add(moduResult)
      }
    })


    new Thread() {
      override def run {
        val start: Date = new Date
        System.out.println("----- started at : " + start)
        var i: Int = 0
        try {
          // && i < 600
          while (foundResult.value.length == 0) {
            i += 1
            Thread.sleep(1000)
          }
          sc.cancelAllJobs
          println("watching thread exited on success")
        }
        catch {
          case e: InterruptedException => {
            e.printStackTrace
            println("watching thread exited on error")
          }
          case e: Exception => {
            e.printStackTrace
            println("watching thread exited on exception")
          }

        }
        val end: Date = new Date
        actualSeconds.addAndGet((end.getTime - start.getTime).toInt / 1000)
        println("----- finished at : " + end + ", cost " + actualSeconds + " seconds  " + foundResult.value.length)
        println("----- finished at : " + moduResultToString(foundResult.value.head))
      }
    }.start


    println("----------------------------")

    // FIXME 20关 总共需循环398131200， 单线程循环 71081241 找到答案，耗时 80s
  }

  // OK, but slow
  def main7(args: Array[String]): Unit = {

    //val json = """{"level":18,"modu":"2","map":["1010","0011","0101","1101","1110"],"pieces":["XX,.X",".X.,.X.,XXX","X.,XX,X.",".X..,XX..,.XXX,.XX.",".X.,XXX",".X..,XX..,XXXX,.X..",".X,.X,XX","X..,X..,XXX,XX.,X..","X.,X.,XX","X.,XX,.X"]}"""
    val json =
      """{"level":20,"modu":"2","map":["111011","001110","001000","110110","110100"],"pieces":["X.X.,XXX.,..XX,..X.","XX.,.X.,XXX,X..","X...,XXXX,.X..,.X..","....XX,XXXXX.,...X..,...X..","X..,X..,XXX","...X.,XXXXX,.X...,.X...","XXX,X..","XXX.,..XX,...X,..XX,..X.",".X,XX","XX,.X,.X"]}"""
    val mapper = new ObjectMapper()
    val moduloLevel = mapper.readValue(json, classOf[ModuleLevel])
    println(moduloLevel)
    println("=============")

    val map: Matrix[Int] = mapToMatrix(moduloLevel.map)
    val pieces: Array[Matrix[Int]] = moduloLevel.pieces.map(pieceToMatrix(_))
    val modu = moduloLevel.modu
    val actualSeconds = new AtomicInteger

    println("=========================map")
    println(map)
    println("=========================pieces.head")
    println(pieces.head)

    val conf = new SparkConf()
      .setAppName("btpka3")
      .setMaster("local[4]")

    val sc = new SparkContext(conf)
    val foundResult: Accumulable[ModuResults, ModuResult] = sc.accumulable(new ModuResults())(PiecePosAccParam)
    //    val mapMatrix = sc.broadcast(map)

    println("=========================pieces.head.pos")
    println(calcPiecePos(map, pieces.head).map(pos => (pieces.head, pos)).getClass)
    calcPiecePos(map, pieces.head).map(pos => (pieces.head, pos)).foreach(println(_))
    val rdds: Array[RDD[(Matrix[Int], (Int, Int))]] = pieces.map(piece => sc.parallelize(calcPiecePos(map, piece).map(pos => (piece, pos))))
    println("----------------------------111" + rdds.getClass)
    var r: RDD[Array[(Matrix[Int], (Int, Int))]] = null
    var a = 0
    for (rdd <- rdds) {
      if (rdd == rdds.head) {
        r = rdd.map(Array(_))
      } else {
        r = r.cartesian(rdd).map(t2 => concat(t2._1, Array(t2._2)))
      }
      //val leftRdd = if (r == null) rdds.head else rdd
      //r = leftRdd.map(Array(_)).cartesian(rdd.map(Array(_))).map(t2 => concat(t2._1, t2._2))
    }
    println("========================= r.getNumPartitions = " + r.getNumPartitions)
    r = r.repartition(4)
    r.foreach(moduResult => {
      val tmpMap = map.copy
      moduResult.foreach(piecePos => {
        addPieceToMap(tmpMap, piecePos._1, piecePos._2)
      })
      if (isValidResult(tmpMap, modu)) {
        foundResult.add(moduResult)
      }
    })


    new Thread() {
      override def run {
        val start: Date = new Date
        System.out.println("----- started at : " + start)
        var i: Int = 0
        try {
          while (foundResult.value.length > 0 && i < 60) {
            {
              i += 1
              Thread.sleep(500)
            }
          }
          sc.cancelAllJobs
          println("watching thread exited on success")
        }
        catch {
          case e: InterruptedException => {
            e.printStackTrace
            println("watching thread exited on error")
          }
        }
        val end: Date = new Date
        actualSeconds.addAndGet((end.getTime - start.getTime).toInt / 1000)
        println("----- finished at : " + end + ", cost " + actualSeconds + " seconds")
        println("----- finished at : " + moduResultToString(foundResult.value.head))
      }
    }.start


    println("----------------------------")
    //r.collect().foreach(l => l.foreach(println(_)))
    //    println(rdd.collect())
    //rdd.repartition(4)


    //    rdd.map(piecePosArr => {
    //      piecePosArr.
    //    })
    //
    //    sc.parallelize(pieces)
    //      .map(piece => calcPiecePos(map, piece).map((piece, _)))
    //      .repartition(4)


    // piece => Array[(piece, pos)]
    //    val d = distData.flatMap { p =>
    //      Array(p + 100, p + 200)
    //    }
    //    println(d.collect().toList)
    // rdd1 =  piece1.map(_ -> Array[(_, pos)]))
    // rdd2 =  piece2.map(_ -> Array[(_, pos)]))
    // rddAll = rdd1.cartesian(rdd2).repartition(4)
  }

  def main0(args: Array[String]): Unit = {

    val map = mapToMatrix(Array("01010", "10100", "01101", "11101", "00111", "01011"))
    val piece = pieceToMatrix("XX.,.XX")

    println("---------------------map")
    println(map)
    println("---------------------piece")
    println(piece)
    println("---------------------pos")
    println(calcPiecePos(map, piece))
    println("---------------------add")
    addPieceToMap(map, piece, (1, 1))
    println(map)

    //println(mapToMatrix(Array("01010", "10100", "01101", "11101", "00111", "01011")))
    //    val arr = Array("XX.,.XX", "X,X,X,X", "X.XX,XXX.,.X..,.XX.", "XXX,.X.", "XXX..,X.XXX", "..X.,XXXX", ".X,XX,XX,X.", ".XXX,.XX.,..X.,XXX.,..X.", "...X,...X,XXXX", "..X,XXX", "..XX.,XXXXX,XX..X", "X..,X..,XXX,.X.")
    //    for (a <- arr) {
    //      println("------------------------------------")
    //      println(pieceToMatrix(a))
    //    }

    //
    //    val nums = 1 :: (2 :: (3 :: (4 :: Nil)))
    //    println(nums)
    //    println("111" :: "222" :: "333" :: Nil)
    //    var z: List[String] = List()
    //    //    z += "ccc"
    //    println(z)
    //    var z1 = new ListBuffer[String]()
    //    z1 += "ccc"
    //    z1 += "ddd"
    //    println(z1)
    //    var z2 = new ListBuffer[(Int,Int)]()
    //    z2 += ((1,1))
    //    z2 += ((2,2))
    //    println(z2)
    //
    //    val fruit1 = "apples" :: ("oranges" :: ("pears" :: Nil))
    //    val fruit2 = "mangoes" :: ("banana" :: Nil)
    //
    //    var ab = fruit1 ::: fruit2
    //    println(ab.::("ccc").::("ddd"))
  }

  //
  def mapToMatrix(strArr: Array[String]): Matrix[Int] = {
    // http://stackoverflow.com/questions/31064753/how-pass-scala-array-into-scala-vararg-method
    //var strArr = Array("01010", "10100", "01101", "11101", "00111", "01011")
    val a = strArr.map(str => str.map(x => x - 48)) // x => x.toString.toInt
    DenseMatrix(a: _*)
  }

  // "XX.,.XX"
  // ->
  // 110
  // 011
  def pieceToMatrix(str: String): Matrix[Int] = {
    val a = str.split(",").map(str => str.map(x => if ('X' == x) 1 else 0))
    DenseMatrix(a: _*)
  }


  def calcPiecePos(map: Matrix[Int], piece: Matrix[Int]): Array[(Int, Int)] = {
    val arr = new ListBuffer[(Int, Int)]()
    for (i <- 0 to map.rows - piece.rows) {
      for (j <- 0 to map.cols - piece.cols) {
        arr += ((i, j))
      }
    }
    arr.toArray
  }

  def addPieceToMap(map: Matrix[Int], piece: Matrix[Int], pos: (Int, Int)): Unit = {
    map(pos._1 until pos._1 + piece.rows, pos._2 until pos._2 + piece.cols) += piece
  }

  def isValidResult(filledMap: Matrix[Int], modu: Int): Boolean = {
    filledMap.forall(_ % modu == 0)
  }

  def moduResultToString(moduResult: ModuResult): String = {
    val buf = new StringBuilder
    moduResult.foreach(a => {
      buf.append(a._2._1.toString)
      buf.append(a._2._2.toString)
    })
    buf.toString()
  }


  def main1(args: Array[String]): Unit = {
    val conf = new SparkConf()
      .setAppName("btpka3")
      .setMaster("local[2]")

    val sc = new SparkContext(conf)
    val data = Array(1, 2, 3, 4, 5)
    val distData = sc.parallelize(data)
    val d = distData.flatMap { p =>
      Array(p + 100, p + 200)
    }
    println(d.collect().toList)
  }


  def main2(args: Array[String]): Unit = {


    val s = """{"level":24,"modu":"2","map":["01010","10100","01101","11101","00111","01011"],"pieces":["XX.,.XX","X,X,X,X","X.XX,XXX.,.X..,.XX.","XXX,.X.","XXX..,X.XXX","..X.,XXXX",".X,XX,XX,X.",".XXX,.XX.,..X.,XXX.,..X.","...X,...X,XXXX","..X,XXX","..XX.,XXXXX,XX..X","X..,X..,XXX,.X."]}"""
    println("-------------------------99")
    println(s)
    println("-------------------------99")
    //JsonParser
    val mapper = new ObjectMapper()
    val node = mapper.readTree(s)
    println(node.get("modu"))
  }


  def main1(): Unit = {

  }

  /*
  1. 1
    */


  implicit object PiecePosAccParam extends AccumulableParam[ModuResults, ModuResult] {

    def addAccumulator(r: ModuResults, t: ModuResult): ModuResults = {
      r += t
      r
    }

    def addInPlace(r1: ModuResults, r2: ModuResults): ModuResults = {
      r1 ++= r2
    }

    def zero(initialValue: ModuResults): ModuResults = {
      initialValue
    }
  }

}
