package app

import akka.actor.{Actor, ActorSelection}
import app.Client._
import app.Register.ForwardRead
import replication.StateMachine

class Register extends Actor  {

  val STATE_MACHINE = "/user/statemachine"

  val CLIENT = "/user/client"

  var keyValueStore = Map[String, String]()

  override def receive = {


    case read : Read =>{

      val statemachine: ActorSelection = context.actorSelection(STATE_MACHINE)

      statemachine ! ForwardRead()


    }


    case write : Write =>{

      val statemachine: ActorSelection = context.actorSelection(STATE_MACHINE)

      statemachine ! ForwardRead()


    }

    /*//receber da statemachine mandar para o client
    case forwardRead : SendRead =>{

      val client: ActorSelection = context.actorSelection(CLIENT)

      //client ! SendRead(forwardRead.value)

    }

    //receber da statemachine mandar para o client
    case forwardWrite : receiveWrite =>{

      val client: ActorSelection = context.actorSelection(CLIENT)

      //client ! sendWrite(value)

    }*/
  }

}

object Register{

  case class ForwardWrite();

  case class ForwardRead();

  case class SendRead(value : String)

  case class sendWrite(value: String)

}
