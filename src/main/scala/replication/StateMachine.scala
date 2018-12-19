package replication

import akka.actor.{Actor, ActorSelection, Props}
import app.Register.ReceiveState
import replication.Proposer.Decide
import replication.StateMachine._

class StateMachine extends  Actor{

  var paxos_initiated : Boolean = false
  var operations_executed : Int = 0
  var replicas : Set[String] = Set.empty
  var decided : List[Operation] = List.empty
  var pending_requests : List[Operation] = List.empty

  val PROPOSER = "/user/proposer"
  val REGISTER = "/user/register"

  def getOperation(pos: Int): Operation = {

    val option : Option[Operation] = decided.lift(pos)
      option match {
        case Some( operation: Operation ) => operation
        case None => null
      }
  }

  def initPaxos(): Unit = {

    paxos_initiated = true
    val to_propose : Operation = pending_requests.head
    to_propose.pos = decided.size +1
    val proposer: ActorSelection = context.actorSelection(PROPOSER)
    proposer ! Init_Prepare(to_propose)
    //timer here or in paxos?

  }


  def addReplicaToSets(replica : String): Unit = {

    replicas += replica
    val proposer : ActorSelection = context.actorSelection(PROPOSER)
    proposer ! addReplicaToSet(replica)

  }

  def removeReplicaFromSets(replica: String): Unit ={
    replicas = replicas.filter(!_.equals(replica))
    val proposer : ActorSelection = context.actorSelection(PROPOSER)
    proposer ! removeReplicaFromSet(replica)
  }

  override def receive: Receive = {

    case init: Init =>
      replicas = init.replicas


    case op : NewOperation =>

      pending_requests = pending_requests :+ op.operation
      if(!paxos_initiated) {
        initPaxos()
      }

    case decide : Decide =>
      if( pending_requests.contains( decide.operation ) )
        pending_requests = pending_requests.filter( _ == decide.operation)

      var hasHole : Boolean = false
      var i : Int= operations_executed
      while ( i < decided.length || !hasHole ){

        val operation : Operation = getOperation(i)
        if(operation == null) hasHole = true
        else {

          if( operation.code == "addReplica" ) addReplicaToSets( operation.value )
          else if (operation.code == "removeReplica") removeReplicaFromSets(operation.value)

          //trigger Register.Execute(operation)
          i+=1
        }

       if(pending_requests.nonEmpty)
        initPaxos()
      }

    case addReplica: AddReplica =>
      if (!replicas.contains(addReplica.replica)) self ! NewOperation( Operation( "addReplica", "", addReplica.replica, -1 ))

    case removeReplica: RemoveReplica =>
      self ! NewOperation(Operation("removeReplica", "", removeReplica.replica, -1 ) )

    case sendStateRep: AddAndSend =>

      val register: ActorSelection = context.actorSelection(sender.path.address.toString.concat(REGISTER))
      register ! ReceiveState(replicas, decided)
      replicas +: sender.path.address.toString

    case refresh : AddStateM =>
      replicas = refresh.replicas
      decided = refresh.decided

  }
}

object StateMachine{

  val props: Props = Props[StateMachine]

  case class Init ( replicas: Set[String] )

  case class NewOperation( operation: Operation )

  case class Init_Prepare( operation: Operation )

  case class AddReplica(replica: String)

  case class RemoveReplica(replica : String)

  case class ExecuteOp(operation : Operation)

  case class AddAndSend()

  case class AddStateM(replicas: Set[String], decided: List[Operation])

  case class addReplicaToSet(replica: String)

  case class removeReplicaFromSet(replica : String)

}