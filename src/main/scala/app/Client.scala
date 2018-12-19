package app

import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import ClientActor._

import collection.JavaConverters._
import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}

import scala.util.Random


object Client extends App {

  val config = ConfigFactory.load.getConfig("ApplicationConfig")
  val processes: List[String] = ConfigFactory.load.getStringList("processes").asScala.toList
  val system = ActorSystem("akkaSystem", config)

  val clientActor = system.actorOf(Props[ClientActor], "clientActor")


  println("hello world")


  clientActor ! Write("1", "maria")
  clientActor ! Write("2", "jose")
  clientActor ! Read("1")
  clientActor ! Read("2")



  class ClientActor extends Actor {

    val REGISTER = "/user/register"

    override def receive = {


      case Write(key, value) => {

        println("write bitch")

        val pro = Random.shuffle(processes).head

       // println(pro)


        val register: ActorSelection = context.actorSelection(pro.concat(REGISTER))

        register ! Write(key, value)
      }

      case Read(key) => {

        val pro = Random.shuffle(processes).head

        val register: ActorSelection = context.actorSelection(pro.concat(REGISTER))

        register ! Read(key)
      }


    }
  }

}

object ClientActor {

  case class Put(key: String, value: String)

  case class Get(key: String)

  case class Write(key: String, value: String)

  case class Read(key: String)

}
