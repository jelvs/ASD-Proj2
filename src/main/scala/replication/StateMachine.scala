package replication

import akka.actor.{Actor, ActorSelection, Props}
import replication.Proposer.Decide
import replication.StateMachine._

class StateMachine extends  Actor{

  var last_executed : Int = 0
  var numb_replicas : Int = 0
  var replicas : Set[String] = Set.empty
  var decided : List[Operation] = List.empty
  var pending_requests : List[Operation] = List.empty


  val PROPOSER = "/user/proposer"
  val proposer = context.actorOf(Props(new Proposer()),"proposer")
  val accepter = context.actorOf(Props(new Accepter()), "accepter")
  val learner = context.actorOf(Props(new Learner()), "learner")

  def getOperation(pos: Int): Operation = {

    val option : Option[Operation] = decided.lift(pos)
      option match {
        case Some( operation: Operation ) => operation
        case None => null
      }
  }

  override def receive: Receive = {

    case init: Init =>
      replicas = init.replicas
      numb_replicas = replicas.size

    case op : NewOperation =>
      pending_requests = pending_requests :+ op.operation
      val proposer: ActorSelection = context.actorSelection(PROPOSER)
      proposer ! Init_Prepare(op.operation)
      //timer here or in paxos?

    case decide : Decide =>
      if( pending_requests.contains( decide.operation ) )
        if( decide.operation == pending_requests.head ) //if it was what I proposed
          pending_requests = pending_requests.filter( _ == decide.operation)

      var hasHole : Boolean = false
      var i : Int= last_executed
      while ( i < decided.length || !hasHole ){

        val operation : Operation = getOperation(i)
        if(operation == null) hasHole = true
        else {

          //trigger Register.Execute(operation)
          i+=1
        }

      }

    case addReplica: AddReplica =>
      if(!replicas.contains(addReplica.replica)){
        replicas +: addReplica.replica
      }

      
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

}