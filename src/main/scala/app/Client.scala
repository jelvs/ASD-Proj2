package app

import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import ClientActor._

import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}



object Client extends App{

  val config = ConfigFactory.load.getConfig("ApplicationConfig")
  val system = ActorSystem("akkaSystem", config)

  val clientActor = system.actorOf(Props[ClientActor], "clientActor")


  println("hello world")

  clientActor ! Write("1", "maria")





}

class ClientActor extends Actor {

  val Register = "/user/Register"

  override def receive = {




    case Write(key, value) => {

      println("write bitch")

      val register: ActorSelection = context.actorSelection(Register)

      register ! Write(key, value)
    }

    case Read(key) => {

      val register: ActorSelection = context.actorSelection(Register)

      register ! Read(key)
    }





  }

}

object ClientActor {

  case class Put(key: String, value: String)

  case class Get(key: String)

  case class Write(key: String, value: String)

  case class Read(key: String)

}
