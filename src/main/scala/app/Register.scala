package app

import akka.actor.{Actor, ActorSelection}
import app.ClientActor._
import app.LifeKeeper.ReceiveImHere
import app.Register.{ImHere, Init, ReceiveState, Response}
import com.typesafe.config.ConfigFactory
import replication.Operation
import replication.StateMachine._

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Random

class Register extends Actor {

  val STATE_MACHINE = "/user/statemachine"
  val CLIENT = "/user/clientActor"
  val LIFEKEEPER = "/user/lifekeeper"

  var keyValueStore : mutable.HashMap[String, String]  = mutable.HashMap.empty

  var ownAddress: String = ""

  val processes: List[String] = ConfigFactory.load.getStringList("processes").asScala.toList

  var replicas : Set[String] = Set.empty

  def executeRead(operation: Operation): Unit = {
    println("hey no read")
    var toReturn : String = null

    keyValueStore.get(operation.key) match {
      case Some(value: String) => toReturn = value
      case None => //Do nothing
    }

    if( operation.replica == ownAddress ) {
      println("vou responder")
      val client: ActorSelection = context.actorSelection(operation.client + CLIENT)
      client ! Response(toReturn)
    }
  }

  def executeWrite(operation: Operation): Unit = {
    println("Hey no write")
    var toReturn : String = null

    keyValueStore.get(operation.key) match {
      case Some(value: String) => toReturn = value
      case None => //Do nothing
    }

    keyValueStore.put(operation.key, operation.value)

    if(operation.replica == ownAddress) {
      println("Vou responder")
      val client: ActorSelection = context.actorSelection(operation.client + CLIENT)
      client ! Response(toReturn)
    }
  }

  override def receive: PartialFunction[Any, Unit] = {


    case message: Init =>

      ownAddress = message.ownAddress
      val life_keeper: ActorSelection = context.actorSelection(LIFEKEEPER)
      life_keeper ! LifeKeeper.InitHeartbeat(ownAddress)

      if(!processes.contains(ownAddress)){
        val process = Random.shuffle(processes).head
        val statemachine: ActorSelection = context.actorSelection(process.concat(STATE_MACHINE))
        statemachine ! AddAndSend()
      }

    case write: Write =>

      val statemachine: ActorSelection = context.actorSelection(STATE_MACHINE)
      val operation = Operation("write", write.key, write.value, -1, write.address, ownAddress)
      statemachine ! NewOperation(operation)

    case read: Read =>
      val statemachine: ActorSelection = context.actorSelection(STATE_MACHINE)
      val operation = Operation("read", read.key, "", -1, read.address, ownAddress)
      statemachine ! NewOperation(operation)

    case operation_to_execute : ExecuteOp =>
      println("Recebi algo para executar")
      operation_to_execute.operation.code match {
        case "read" => executeRead(operation_to_execute.operation)
        case "write" => executeWrite(operation_to_execute.operation)
      }

    case  updateState: ReceiveState =>
      val stateMachine: ActorSelection = context.actorSelection(STATE_MACHINE)
      stateMachine ! AddStateM(updateState.replicas, updateState.decided)

    case _ : ImHere =>
      val lifeKeeper: ActorSelection = context.actorSelection(sender.path.address.toString.concat(LIFEKEEPER))
      lifeKeeper ! ReceiveImHere(ownAddress)
  }
}

object Register {

  case class Init(ownAddress: String)

  case class ReceiveState(replicas: List[String], decided: List[Operation])

  case class ImHere()

  case class Response(value: String)
}
