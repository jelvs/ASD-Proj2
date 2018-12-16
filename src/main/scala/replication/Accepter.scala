package replication

import akka.actor.{Actor, ActorSelection}
import Accepter._
import replication.Proposer.{Accept, Prepare}

class Accepter extends Actor {

  val Learner = "/user/learner"
  var replicas : Set[String] = Set.empty

  var np: Int = 0
  var na: Int = 0
  var va : Operation = _

  override def receive = {

    case init: Init =>
      replicas = init.replicas

    case prepare: Prepare =>

      if(prepare.sqn >= np){
        np = prepare.sqn
        sender ! PrepareOk(np, (na, va))
      }


    case accept: Accept => {

      if(accept.sqn >= np) {
        na = accept.sqn
        va = accept.operation

        sender ! AcceptOk(accept.sqn, va)


        for(r <- accept.replicas) {
          //Send to learners
          val learner: ActorSelection = context.actorSelection(Learner)
          learner ! AcceptOkLearner(na, va, accept.replicas)
        }

      }

    }

  }

}

object Accepter{

  case class Init( replicas: Set[String] )

  case class AcceptOk( sqn: Int, operation: Operation)

  case class AcceptOkLearner( sqn: Int, operation: Operation, replicas: Set[String])

  case class PrepareOk( sqn: Int, va: (Int, Operation) )

  //case class Operation (operation: String, key: Int, value: String)

}
