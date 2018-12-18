package app

import akka.actor.Actor
import app.LifeKeeper.{Heartbeat, Init, InitHeartbeat}
import replication.StateMachine.RemoveReplica

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class LifeKeeper extends Actor {

  var processesAlive = Map[String, Double]()
  var ownAddress: String = ""
  val LIFEKEEPER = "/user/lifekeeper"

  override def receive = {


    case message: Init => {
      ownAddress = message.ownAddress

    }

    case initHeartbeat: InitHeartbeat => {

      context.system.scheduler.schedule(0 seconds, 5 seconds)(initHeartbeat())
      context.system.scheduler.schedule(0 seconds, 5 seconds)((searchFailedProcesses()))
    }

    case heartbeat: Heartbeat => {

      var timer: Double = System.currentTimeMillis()
      if (processesAlive.contains(sender.path.address.toString)) {
        processesAlive += (sender.path.address.toString -> timer)

      }
    }


  }

  def initHeartbeat() = {

    var process = context.actorSelection(s"${ownAddress}/user/lifekeeper")
    process ! Heartbeat()
  }

  def searchFailedProcesses() = {


    for ((n, t) <- processesAlive) {

      // 7 seconds heartbeat
      if ((System.currentTimeMillis() - t) >= 7000) {
        processesAlive -= n

        permanentFailure(processesAlive, n)

      }


    }


  }

  def permanentFailure(processesAlive: Map[String, Double], n : String) = {

    processesAlive.foreach(p => {
      val process = context.actorSelection(s"${p}/user/statemachine")
      process ! RemoveReplica(n)
    })





  }


}

object LifeKeeper {

  case class Init(ownAddress: String)

  case class InitHeartbeat(senderAddress: String)

  case class Heartbeat()

}
