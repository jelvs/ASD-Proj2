package replication

import akka.actor.{Actor, ActorSelection}
import app._
import Learner._
import replication.Proposer.Decide


class Learner  extends Actor{

  val STATE_MACHINE = "/user/statemachine"

  var na: Int = 0
  var va: Operation = _
  var decision: Operation = _
  var majority: Boolean = false
  var nAcceptedOk = 0

  override def receive = {

    case InitPaxos => {
      majority = false
      nAcceptedOk = 0

    }

    case accept: Accept_OK => {

      if(accept.n >= na){
        na = accept.n
        va = accept.operation
      }

      nAcceptedOk +=1

      if(nAcceptedOk >= (accept.replicas.size/2)+1 && !majority){
        majority = true
        decision = va

      }
    }

    case decide: Decide =>
      val stateMachine : ActorSelection = context.actorSelection(STATE_MACHINE)
      stateMachine ! decide


  }


}

object Learner{

  case class InitPaxos();

  case class Accept_OK(n : Int, operation: Operation,  replicas: List[String]);


}

