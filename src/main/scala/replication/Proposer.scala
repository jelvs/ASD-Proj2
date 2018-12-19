package replication

import akka.actor.{Actor, ActorSelection, Props}
import replication.Accepter.{AcceptOk, PrepareOk}
import replication.Proposer._
import replication.StateMachine.Init_Prepare

class Proposer extends Actor {

  val ACCEPTOR = "/user/accepter"
  val STATE_MACHINE = "/user/statemachine"

  var id : Int = 0
  var proposal : Operation = _
  var operations_executed : Int = 0
  var quorum_size : Int = 0
  var highest_sn : Int = 0
  var sqn : Int = 0
  var replicas : List[String] = List.empty
  var prepareOk_replies : List[ PrepareOk ] = List.empty
  var acceptOk_replies : List[String] = List.empty

  def getOperationWithHighestSqn: Operation = {

    var top_sqn : Int = 0
    var operation : Operation = null

    if(prepareOk_replies.isEmpty) return null

    for( prepareOk <- prepareOk_replies )
      if( prepareOk.va._1 > top_sqn ){
        top_sqn = prepareOk.va._1
        operation = prepareOk.va._2
      }

    operation
  }

  override def receive: PartialFunction[Any, Unit] = {

    case init : Init =>
      id = init.id
      replicas = init.replicas
      quorum_size = replicas.size / 2 + 1

    case init_propose : Init_Prepare =>
      println("Intiating propose")
      if(sqn < highest_sn) sqn = id * operations_executed
      proposal = init_propose.operation
      replicas.foreach(replica => {
        println("Mandar prepare para " + replica)
        val actorSelection : ActorSelection = context.actorSelection(replica + ACCEPTOR)
        actorSelection ! Prepare(sqn, proposal)
      })

    case prepare_ok: PrepareOk =>
      println("Got reply")
      if(prepare_ok.sqn == sqn) {
        println("Entrei aqui")
        prepareOk_replies = prepareOk_replies :+ prepare_ok

        if( prepare_ok.sqn > highest_sn ) highest_sn = prepare_ok.sqn

        println("Quorum size "  +  quorum_size)
        println("Replies: " + prepareOk_replies.size)
        if( prepareOk_replies.size == quorum_size ) {
          println("Got Majority")
          val operation: Operation = getOperationWithHighestSqn
          if( operation != null ) proposal = operation

          for( replica : String <- replicas ){
            val accepter : ActorSelection = context.actorSelection(  replica.concat( ACCEPTOR ) )
            accepter ! Accept(sqn, proposal, replicas)
          }
        }
      }

    case accept_ok : AcceptOk =>

      if(accept_ok.sqn == sqn){
        if(!acceptOk_replies.contains(sender().toString())) //correct this
          acceptOk_replies = acceptOk_replies :+ sender().toString()

        if(acceptOk_replies.size == quorum_size) {
          println("Got majority accept")

          val stateMachine: ActorSelection = context.actorSelection(STATE_MACHINE)
          stateMachine ! Decide(operations_executed, proposal)
        }
      }
  }
}

object Proposer{

  val props: Props = Props[Proposer]

  case class Init( id: Int, replicas : List[String] )

  case class Prepare( sqn: Int, operation: Operation  )

  case class Accept( sqn : Int, operation: Operation, replicas: List[String])

  case class Decide( pos: Int, operation: Operation )

}
