package app

import akka.actor.Actor
import replication.{Operation, StateMachine}
import Application._
import replication._

import scala.collection.mutable._

class Application extends Actor  {

  var keyValueStore = HashMap[String, String]()
  var stateMachines = TreeMap[Int, StateMachine]()
  var pending = Queue[Operation]()
  var ownAddress: String = ""
  var code: Int = 0

  override def receive = {
    case init: InitReplication => {

    }

    case write: Write => {

      val operation = Operation("write", write.key, write.value)

      //Tirar da Queue ????
      //pending.enqueue(op)

      stateMachines.get(code).get.initPaxos(operation,code)

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

  case class InitReplication();

  case class Write(key: Int, value: String);

  case class Read(key: Int);
}
