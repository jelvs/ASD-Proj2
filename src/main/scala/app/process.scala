package app

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}



object Process extends App {

  var port = 2552
  if (args.length != 0) {
    port = args(0).toInt
  }

  val config = configure()
  val system = ActorSystem("SystemName", config)
  val ownAddress = getOwnAddress(port)


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

