package app

import akka.actor.{Actor, ActorSelection, ActorRef}

import Client._
import akka.util.Timeout

class Client(proposers: List[ActorRef], replicas: Int, sqn: Int) extends Actor {

  val Register = "/user/Register"

  override def receive = {




    case Write(key, value) => {

      val register: ActorSelection = context.actorSelection(Register)

      register ! Write(key, value)
    }

    case Read(key) => {

      val register: ActorSelection = context.actorSelection(Register)

      register ! Read(key)
    }





  }

}

object Client {

  case class Put(key: String, value: String)

  case class Get(key: String)

  case class Write(key: String, value: String)

  case class Read(key: String)

}
