package replication

import akka.actor.Actor
import app._
import Learner._


class Learner  extends Actor{

  var na: Int = 0
  var va = Operation("", 0, "")
  var decision = Operation("", 0, "")
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

        //val process = context.actorSelection()    statemachine
        // process ! sendDecision
      }

    }



  }


}

object Learner{

  case class InitPaxos();

  case class Accept_OK(n : Int, operation: Operation,  replicas: Set[String]);


}

