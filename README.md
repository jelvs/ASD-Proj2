# Algorithms and Distributed Systems

## 2º Project


### State Machine Replication - Paxos



The application layer (i.e, the service) is a simple key value store, where all replicas should have the same information. The service itself provides two client operations to manipulate the key value store: read(Key) and write(key, value). For simplicity both Keys and Values can be modeled as strings. As an implementation suggestion, students can, at the application level, rely on a HashMap.
In addition to this the service should also support two additional management operations: AddReplica(Replica) and RemoveReplica(Replica). Replica can be any data format that assists students in their implementation, a suggestion is to provide the necessary information for contacting that replica (e.g, IP + PORT).
At bootstrap the replicas should be made aware (through a configuration file) of the initial set of replicas. This information should be used to initialize the Paxos/Multi-Paxos (see below) component. Replicas that fail should be automatically removed from the system.

In this option the students are asked to implement a replicated service using state machine replication. In this variant the state machine replication layer should use **Paxos**.
