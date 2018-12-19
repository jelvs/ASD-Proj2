package app

import akka.actor.Actor
import app.LifeKeeper._
import app.Register.ImHere
import replication.StateMachine.RemoveReplica

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class LifeKeeper extends Actor {

  var processesAlive = Map[String, Double]()
  var uAlive = Map[String, Double]()
  var ownAddress: String = ""
  val LIFEKEEPER = "/user/lifekeeper"

  override def receive = {


    case message: Init => {
      ownAddress = message.ownAddress

    }

    case initHeartbeat: InitHeartbeat => {

      context.system.scheduler.schedule(0 seconds, 5 seconds)(initHeartbeat)
      context.system.scheduler.schedule(0 seconds, 5 seconds)((searchFailedProcesses()))
    }

    case heartbeat: Heartbeat => {

      var timer: Double = System.currentTimeMillis()
      if (processesAlive.contains(sender.path.address.toString)) {
        processesAlive += (sender.path.address.toString -> timer)

      }
    }

    case uThere: UThere => {


      val process = context.actorSelection(s"${sender.path.address.toString}/user/PartialView")
      process ! ImHere()
    }

    case receiveAlive : ReceiveImHere => {

      uAlive -= receiveAlive.senderAddress
      val timer: Double = System.currentTimeMillis()
      processesAlive += (sender.path.address.toString -> timer)

    }



  }

  def initHeartbeat() = {

    var process = context.actorSelection(s"${ownAddress}/user/lifekeeper")
    process ! Heartbeat()
  }

  def searchFailedProcesses() = {


    for ((n, t) <- processesAlive) {

      // 7 seconds heartbeat
      if ((System.currentTimeMillis() - t) >= 5000) {
        processesAlive -= n

        val timer: Double = System.currentTimeMillis()
        uAlive += (n -> timer)
        uThere(n)

      }
    }

    for ((n, t) <- uAlive) {
      // more than 10 seconds

      if ((System.currentTimeMillis() - t) >= 7000) {

        permanentFailure(processesAlive, n)


      }
    }


  }

  def uThere(n : String): Unit = {
    val process = context.actorSelection(s"${n}/user/register")
    process ! ImHere()

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

  case class UThere(n: String)

  case class ReceiveImHere(senderAddress : String)

}
