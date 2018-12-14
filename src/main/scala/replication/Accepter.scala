package replication

import akka.actor.Actor
import app._
import Accepter._
import replication.Proposer.Accept

class Accepter extends Actor {

  var np: Int = 0
  var na: Int = 0

  override def receive = {





    case accept: Accept => {

    }

  }

}

object Accepter{

  case class AcceptOk( sqn: Int )

  case class PrepareOk( sqn: Int, va: (Int, Operation ))

}
