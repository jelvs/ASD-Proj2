package replication

import akka.actor.{ ActorSystem, Props}
import replication.StateMachine._

import scala.collection.mutable.TreeMap

class StateMachine (ownAddress: String, replicas : Set[String], system: ActorSystem){

  var pending_requests : List[NewOperation] = List.empty

  var stateMachine = TreeMap[Int, Operation]()

  val proposer = system.actorOf(Props(new Proposer()))

  val accepter = system.actorOf(Props(new Accepter()))

  val learner = system.actorOf(Props(new Learner()))


  def writeOperation (operation: String, index: Int, key: Int, value: String) = {

    stateMachine.put(index, Operation(operation, key, value))


  }

  def initPaxos(operation: Operation, code: Int) = {
        proposer ! Proposer.Init(operation, replicas)
  }

  def stopActors () = {
    system.stop(proposer);
    system.stop(accepter);
    system.stop(learner);
  }




}

object StateMachine{

  val props: Props = Props[StateMachine]

  case class NewOperation( code: String, key: String,  arg: String )

  case class Init_Prepare( operation: Operation )
}