package app

import akka.actor.Actor
import replication.{Operation, StateMachine}
import Application._

import scala.collection.mutable.HashMap
import scala.collection.mutable.Queue

class Application extends Actor  {

  var keyValueStore = HashMap[String, String]()
  var pending = Queue[Operation]()
  var ownAddress: String = ""
  var replicas : Set[String] = Set.empty

  override def receive = {
    case init: InitReplication => {

    }

    case write: Write => {

      val operation = Operation("write", write.key, write.value)

      //Tirar da Queue ????
      pending.enqueue(operation)

      StateMachine.Init_Prepare(operation,replicas)

    }

    case addReplica: AddRep => {


    }


    case remReplica: RemRep => {


    }


    case read: Read => {
      if (keyValueStore.contains(read.key.toString)) {
        keyValueStore.get(read.key.toString)

        //Presumindo que a state machine vai escrevendo sempre após terminar um write

      }

    }
  }

}

object Application{

  case class AddRep();

  case class RemRep();

  case class InitReplication();

  case class Write(key: Int, value: String);

  case class Read(key: Int);
}
