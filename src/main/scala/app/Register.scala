package app

import akka.actor.{Actor, ActorSelection}
import app.ClientActor._
import app.Register.{ForwardRead, Init, ReceiveState}
import com.typesafe.config.ConfigFactory
import replication.{Operation, StateMachine}
import replication.StateMachine._

import collection.JavaConverters._
import scala.collection.mutable.HashMap
import scala.util.Random

class Register extends Actor {

  val STATE_MACHINE = "/user/statemachine"

  val CLIENT = "/user/client"

  var keyValueStore = HashMap[String, String]()

  var ownAddress: String = ""

  val processes: List[String] = ConfigFactory.load.getStringList("processes").asScala.toList

  override def receive = {


    case message: Init => {

      ownAddress = message.ownAddress

      if(!processes.contains(ownAddress)){
        val process = Random.shuffle(processes).head

        val statemachine: ActorSelection = context.actorSelection(process.concat(STATE_MACHINE))

        statemachine ! AddAndSend()
      }



    }


    case write: Write => {

      println("ola bitch im back")

      val statemachine: ActorSelection = context.actorSelection(STATE_MACHINE)
      val pos: Int = -1

      val operation = Operation("write", write.key, write.value, pos)

      statemachine ! NewOperation(operation)


    }


    case read: Read => {

      val client: ActorSelection = context.actorSelection(CLIENT)

      if (keyValueStore.contains(read.key)) {
        //client ! SendRead(keyValueStore.get(read.key))
      }
      else {
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



      case  updateState: ReceiveState => {

        val stateMachine: ActorSelection = context.actorSelection(ownAddress.concat(STATE_MACHINE))

        stateMachine ! AddStateM(updateState.replicas, updateState.decided)

        updateState.replicas.foreach(r => {
          val rep = context.actorSelection(r.concat(STATE_MACHINE))
          rep ! AddReplica(ownAddress)
        })

    }


  }

}

object Register {

  case class ForwardWrite();

  case class ForwardRead();

  case class SendRead(value: String)

  case class sendWrite(value: String)

  case class Init(ownAddress: String)

  case class ReceiveState(replicas: Set[String], decided: List[Operation])

}
