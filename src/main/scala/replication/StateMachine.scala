package replication

import akka.actor.{Actor, ActorSelection, Props}
import replication.Proposer.Decide
import replication.StateMachine._

class StateMachine extends  Actor{

  var numb_replicas : Int = 0
  var replicas : Set[String] = Set.empty
  var operations : Set[Operation] = Set.empty
  var pending_requests : Set[Operation] = Set.empty

  val PROPOSER = "/user/proposer"

  override def receive: Receive = {

    case init: Init =>
      replicas = init.replicas
      numb_replicas = replicas.size

    case op : NewOperation =>
      pending_requests += op.operation
      val proposer: ActorSelection = context.actorSelection(PROPOSER)
      proposer ! Init_Prepare(op.operation)
      //timer here or in paxos?

    case _ : Decide =>
           

  }
}

object StateMachine{

  val props: Props = Props[StateMachine]

  case class Init ( replicas: Set[String] )

  case class NewOperation( operation: Operation )

  case class Init_Prepare( operation: Operation )
}