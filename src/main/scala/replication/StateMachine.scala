package replication

import akka.actor._
import replication.StateMachine._

import scala.collection.mutable.TreeMap

class StateMachine extends Actor{

  val system = ActorSystem("Process")

  var pending_requests : List[NewOperation] = List.empty

  var stateMachine = TreeMap[Int, Operation]()

  val proposer = system.actorOf(Props(new Proposer()), "proposer")
  val accepter = system.actorOf(Props(new Accepter()), "accepter")
  val learner = system.actorOf(Props(new Learner()), "learner")

  override def receive = {

    case init : Init_Prepare => {

      proposer ! Proposer.Init(init.operation, init.replicas)

    }

    case writeOp : WriteOperation =>{

      writeOperation(writeOp.operation, writeOp.index, writeOp.key, writeOp.value)
    }

    def writeOperation(operation: String, index: Int, key: Int, value: String) = {

      stateMachine.put(index, Operation(operation, key, value))


    }


    def stopActors() = {
      system.stop(proposer);
      system.stop(accepter);
      system.stop(learner);
    }
  }

  
}

object StateMachine{

  val props: Props = Props[StateMachine]

  case class NewOperation( code: String, key: String,  arg: String )

  case class WriteOperation( operation: String, index: Int,  key: Int,  value: String )

  case class Init_Prepare( operation: Operation, replicas: Set[String])
}