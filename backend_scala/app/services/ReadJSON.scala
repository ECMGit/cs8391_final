package services
import scala.collection.mutable.ArrayBuffer
import models.Service

/**
 *
 * @project: backend_scala
 * @author: Junhao Shen
 * @date: 11/16/21
 * */
object ReadJSON {
  def read(filename: String = "input.json"): List[List[Service]] = {
    val jsonString = os.read(os.pwd/"public"/filename)
    val data = ujson.read(jsonString)
    val sc1list = deserializeServiceList(data("SC1"))
    val sc2list = deserializeServiceList(data("SC2"))
    val sc3list = deserializeServiceList(data("SC3"))
    List(sc1list, sc2list, sc3list)
  }

  def deserializeServiceList(serviceCluster: ujson.Value): List[Service] = {
    val scarray = serviceCluster.arr.toArray
    val arrayBuffer: ArrayBuffer[Service] = ArrayBuffer()
    scarray.foreach{serviceJson =>
      val id = serviceJson.obj("id").num.toInt
      val cost = serviceJson.obj("cost").num.toInt
      val rel = serviceJson.obj("reliability").num.toFloat
      val avb = serviceJson.obj("availability").num.toFloat
      val time = serviceJson.obj("time").num.toInt
      val nextservice = Service(id, cost, rel, time, avb)
      arrayBuffer += nextservice
    }
    arrayBuffer.toList
  }
}
