package replication

import akka.actor._
import replication.StateMachine._

import scala.collection.mutable.TreeMap

class StateMachine extends Actor{

  //val system = ActorSystem("Process")

  var pending_requests : List[NewOperation] = List.empty
  var succeed_requests : List[NewOperation] = List.empty

  var stateMachine = TreeMap[Int, Operation]()

  val proposer = context.actorOf(Props(new Proposer()),"proposer")
  val accepter = context.actorOf(Props(new Accepter()), "accepter")
  val learner = context.actorOf(Props(new Learner()), "learner")

  override def receive = {

    case init : Init_Prepare => {

      proposer ! Proposer.Init(init.operation, init.replicas)

    }

    case writeOp : WriteOperation =>{

      writeOperation(writeOp.operation, writeOp.index, writeOp.key, writeOp.value)
    }

    /*
    //TODO Sub-dividir se adiciona ou remove uma replica ou se é um write (put)

    case Decide =>{



    }*/

    /*case sendMessagenewRep =>{

    }*/

    /*case getReplica =>{

    }*/

    def writeOperation(operation: String, index: Int, key: Int, value: String) = {

      stateMachine.put(index, Operation(operation, key, value))


    }


    def stopActors() = {
      context.stop(proposer);
      context.stop(accepter);
      context.stop(learner);
    }
  }


}

object StateMachine{

  val props: Props = Props[StateMachine]

  case class NewOperation( code: String, key: String,  arg: String )

  case class WriteOperation( operation: String, index: Int,  key: Int,  value: String )

  case class Init_Prepare( operation: Operation, replicas: Set[String])
}