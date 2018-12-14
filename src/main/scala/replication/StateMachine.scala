package replication

import akka.actor.{Actor, Props}
import replication.StateMachine._

class StateMachine extends  Actor{

  var pending_requests : List[NewOperation] = List.empty

  override def receive: Receive = {

    case init: Init =>



    case op : NewOperation => {

    }

    case decide: Decide =>{



    }



  }
}

object StateMachine{

  val props: Props = Props[StateMachine]

  case class NewOperation( code: String, key: String,  arg: String )

  case class Init_Prepare( operation: Operation )
}