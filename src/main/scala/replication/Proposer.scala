package replication

import akka.actor.{Actor, ActorSelection, Props}
import replication.Accepter.{AcceptOk, PrepareOk}
import replication.Proposer._
import replication.StateMachine.Init_Prepare

class Proposer extends Actor {

  val ACCEPTOR = "/user/Acceptor"
  val STATE_MACHINE = "/user/statemachine"

  var proposal : Operation = _
  var quorum_size : Int = 0
  var numb_replicas : Int = 0
  var highest_sn : Int = 0
  var sqn : Int = 0
  var current_index: Int = 0
  var replicas : Set[String] = Set.empty
  var prepareOk_replies : Set[ (Int, Operation) ] = Set.empty
  var acceptOk_replies : Set[String] = Set.empty

  def getOperationWithHighestSqn: Operation = {

    var top_sqn : Int = 0
    var operation : Operation = null

    if(prepareOk_replies.isEmpty) return null

    for( prepareOk <- prepareOk_replies )
      if( prepareOk._1 > top_sqn ){
        top_sqn = prepareOk._1
        operation = prepareOk._2
      }

    operation
  }

  override def receive: PartialFunction[Any, Unit] = {

    case init : Init =>
      replicas = init.replicas
      numb_replicas = replicas.size
      quorum_size = numb_replicas / 2 + 1

    case init_propose : Init_Prepare =>

      sqn = highest_sn + 1
      proposal = init_propose.operation

      replicas.foreach(replica => context.actorSelection(replica + ACCEPTOR) ! Prepare(sqn))

    case prepare_ok: PrepareOk =>

      if(prepare_ok.sqn == sqn) {
        if (!prepareOk_replies.contains(prepare_ok.va))
          prepareOk_replies += prepare_ok.va

        if( prepareOk_replies.size == quorum_size ) {
          val operation: Operation = getOperationWithHighestSqn
          if( operation != null ) proposal = operation

          for( replica : String <- replicas ){
            val accepter : ActorSelection = context.actorSelection(  replica.concat( ACCEPTOR ) )
            accepter ! Accept(sqn, proposal)
          }
        }
      }

    case accept_ok : AcceptOk =>

      if(accept_ok.sqn == sqn){
        if(!acceptOk_replies.contains(sender().toString())) //correct this
          acceptOk_replies += sender().toString()

        if(acceptOk_replies.size == quorum_size) {
          val stateMachine: ActorSelection = context.actorSelection(STATE_MACHINE)
          stateMachine ! Decide(current_index, proposal)
        }
      }
  }
}

object Proposer{

  val props: Props = Props[Proposer]

  case class Init( replicas : Set[String] )

  case class Prepare( sqn: Int  )

  case class Accept( sqn : Int, operation: Operation)

  case class Decide( pos: Int, operation: Operation )

}
