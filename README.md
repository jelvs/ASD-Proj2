# Algorithms and Distributed Systems

## 2º Project


## Publish-Subscribe using Unstructured

### Introduction

The goal of this phase of the Project is to implement a publish-subscribe protocol on top of an unstructured overlay 
network (also known as unstructured overlay network).
We consider a publish subscribe system similar to the one in option A, which offers to other protocols the following 
interface:

**subscribe (TOPIC):** That notifies the system that the protocol above is interested in received 
(through a notification) all messages published that are tagged with TOPIC.
 
**unsubscribe (TOPIC) :**  That notifies the system that the protocol above is no longer interested in receiving 
messages tagged with TOPIC

**publish (TOPIC, message):** That publishes, to the system , a message tagged with TOPIC. For simplicity, 
we consider that messages are tagged with exactly one topic.

### State Machine Replication - Paxos



The application layer (i.e, the service) is a simple key value store, where all replicas should have the same information. The service itself provides two client operations to manipulate the key value store: read(Key) and write(key, value). For simplicity both Keys and Values can be modeled as strings. As an implementation suggestion, students can, at the application level, rely on a HashMap.
In addition to this the service should also support two additional management operations: AddReplica(Replica) and RemoveReplica(Replica). Replica can be any data format that assists students in their implementation, a suggestion is to provide the necessary information for contacting that replica (e.g, IP + PORT).
At bootstrap the replicas should be made aware (through a configuration file) of the initial set of replicas. This information should be used to initialize the Paxos/Multi-Paxos (see below) component. Replicas that fail should be automatically removed from the system.

In this option the students are asked to implement a replicated service using state machine replication. In this variant the state machine replication layer should use **Paxos**.