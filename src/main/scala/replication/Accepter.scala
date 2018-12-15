package replication

import akka.actor.Actor
import app._
import Accepter._


class Accepter extends Actor {

  var np: Int = 0
  var na: Int = 0
  var va = Operation("", 0, "")

  override def receive = {

    case prepare: PrepareOk => {

      if(prepare.sqn >= np){

        np = prepare.sqn

        //Send prepare_ok to sender
        //sender ! Prepare_OK(np, prepare.operation)
      }
    }




    case accept: AcceptOk => {

      if(accept.sqn >= np) {
        na = accept.sqn
        va = accept.operation

        //
        sender ! Proposer.Accept(accept.sqn, va)

        for(r <- accept.replicas) {
          //Send to learners
          //val process = context.actorSelection(...)
          //process ! Learner.Accept_OK(na, va, accept.replicas)
        }

      }

    }

  }

}

object Accepter{

  case class AcceptOk( sqn: Int, operation: Operation, replicas : Set[String])

  case class PrepareOk( sqn: Int, va: (Int, Operation ))



}
