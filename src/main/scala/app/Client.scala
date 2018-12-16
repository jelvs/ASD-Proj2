package app

import akka.actor.{Actor, ActorSelection}

import Client._

class Client extends Actor {

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

  case class Write(key: String, value: String);

  case class Read(key: String);

}
