package app

import akka.actor.{Actor, ActorSelection}
import app.ClientActor._
import app.Register.ForwardRead
import replication.{Operation, StateMachine}
import replication.StateMachine.{ExecuteOp, NewOperation}

import scala.collection.mutable.HashMap

class Register extends Actor {

  val STATE_MACHINE = "/user/statemachine"

  val CLIENT = "/user/client"

  var keyValueStore = HashMap[String, String]()

  override def receive = {


    /*case read: Read => {

      val statemachine: ActorSelection = context.actorSelection(STATE_MACHINE)

      statemachine ! ForwardRead()


    }*/


    case write: Write => {

      val statemachine: ActorSelection = context.actorSelection(STATE_MACHINE)
      val pos : Int = -1

      val operation = Operation("write", write.key, write.value, pos )

      statemachine ! NewOperation(operation)


    }



    case read : Read =>{

      val client: ActorSelection = context.actorSelection(CLIENT)

      if(keyValueStore.contains(read.key)){
        //client ! SendRead(keyValueStore.get(read.key))
      }
      else{
        //erro nao existe value para essa determinada key

        //client ! SendRead(keyValueStore.get(forwardRead.operation.value)
      }





    }

    //receber da statemachine guardar e mandar para o client
    case forwardWrite: ExecuteOp => {

      var previousValue = keyValueStore.get(forwardWrite.operation.key)

      keyValueStore.put(forwardWrite.operation.key, forwardWrite.operation.value)

      val client: ActorSelection = context.actorSelection(CLIENT)

      //Send Preevious value

    }



  }

}

object Register {

  case class ForwardWrite();

  case class ForwardRead();

  case class SendRead(value: String)

  case class sendWrite(value: String)

}
