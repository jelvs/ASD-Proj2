package app

import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import ClientActor._
import app.Register.Response

import collection.JavaConverters._
import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}

import scala.util.Random


object Client extends App {

  var port = 8080
  if (args.length != 0) {
    port = args(0).toInt
  }



  val config = ConfigFactory.load.getConfig("ApplicationConfig")
  val processes: List[String] = ConfigFactory.load.getStringList("processes").asScala.toList
  val system = ActorSystem("akkaSystem", config)

  val clientActor = system.actorOf(Props[ClientActor], "clientActor")

  val ownAddress = getOwnAddress(port)

  println("Eu: " + ownAddress)
  println("hello world")


  clientActor ! Write("1", "maria", ownAddress)
  clientActor ! Read("1", ownAddress)
  //clientActor ! Write("2", "jose", ownAddress)
  //clientActor ! Read("1")
  //clientActor ! Read("2")

  def getOwnAddress(port: Int) = {
    val address = config.getAnyRef("akka.remote.netty.tcp.hostname")
    val port = config.getAnyRef("akka.remote.netty.tcp.port")

    s"akka.tcp://${system.name}@${address}:${port}"
  }



  class ClientActor extends Actor {

    val REGISTER = "/user/register"

    override def receive = {


      case Write(key, value, addr) => {

        println("write bitch")

        val pro = Random.shuffle(processes).head

       // println(pro)


        val register: ActorSelection = context.actorSelection(pro.concat(REGISTER))

        register ! Write(key, value, addr)
      }

      case Read(key, addr) => {

        val pro = Random.shuffle(processes).head

        val register: ActorSelection = context.actorSelection(pro.concat(REGISTER))

        register ! Read(key, addr)
      }

      case response: Response =>
        println("Respsta " + response.value)

    }
  }

}

object ClientActor {

  case class Put(key: String, value: String, address:String)

  case class Get(key: String, address: String)

  case class Write(key: String, value: String, address: String)

  case class Read(key: String, address: String)

}
