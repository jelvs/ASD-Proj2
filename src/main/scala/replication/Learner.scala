package replication

import akka.actor.Actor
import app._
import Learner._


class Learner  extends Actor{

  var na: Int = 0
  //var va: ???
  //var decision: ???
  var majority: Boolean = false
  var nAcceptedOk = 0

  override def receive = {

    case InitPaxos => {
      majority = false
      nAcceptedOk = 0

    }

    case accept: Accept_OK => {

    }



  }


}

object Learner{

  case class InitPaxos();

  case class Accept_OK(n : Int,  replicas: Set[String], stateMCounter: Int);

}
