package app

import akka.actor.Actor

class Register extends Actor  {

  var keyValueStore = Map[String, String]()

  override def receive = {


    case _ =>{

    }
  }

}
