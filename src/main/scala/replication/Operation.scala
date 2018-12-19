package replication

case class Operation( code: String, key: String, value: String, var pos: Int )
