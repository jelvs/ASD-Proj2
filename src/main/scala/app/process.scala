package app

import akka.actor.{ActorSystem, Props}
import app.Register.{Init}
import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import replication._

import scala.collection.JavaConverters._


object Process extends App {

  var port = 2552
  if (args.length != 0) {
    port = args(0).toInt
  }

  val config = configure()
  val system = ActorSystem("akkaSystem", config)
  val ownAddress = getOwnAddress(port)


  //val client = system.actorOf(Props[Client], "client")
  val register = system.actorOf(Props[Register], "register")
  val stateMachine = system.actorOf(Props[StateMachine], "statemachine")
  val lifekeeper = system.actorOf(Props[LifeKeeper], "lifekeeper")
  val proposer = system.actorOf(Props[Proposer], "proposer")
  val accepter = system.actorOf(Props[Accepter], "accepter")
  val learner = system.actorOf(Props[Learner], "learner")

  val processes: List[String] = ConfigFactory.load.getStringList("processes").asScala.toList

  //hardcode alert
  var id : Int = -1
  if(port == 2552) id =1
  if(port == 2554) id = 2
  if(port == 2256 ) id = 3

  register ! Init(ownAddress)
  lifekeeper ! LifeKeeper.Init(ownAddress)
  stateMachine ! StateMachine.Init(processes)
  proposer ! Proposer.Init(id, processes)
  accepter ! Accepter.Init(processes)


  var contactNode = ""
  if (args.length > 1) {
    contactNode =  s"akka.tcp://${system.name}@${args(1)}"
    println("Contact: " + contactNode)
  }








  def configure(): Config = {

    ConfigFactory.load.getConfig("Process").withValue("akka.remote.netty.tcp.port",
      ConfigValueFactory.fromAnyRef(port))

  }

  def getOwnAddress(port: Int) = {
    val address = config.getAnyRef("akka.remote.netty.tcp.hostname")
    val port = config.getAnyRef("akka.remote.netty.tcp.port")

    s"akka.tcp://${system.name}@${address}:${port}"
  }





}

