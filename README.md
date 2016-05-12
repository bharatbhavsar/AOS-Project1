# AOS-Project1
Design and implement Algorithm for Distributed Spanning tree

Build a distributed system consisting of n nodes arranged in a certain topology. The value of n, the location of each node and its set of neighbors is specified in a configuration file. You can assume that the communication topology specified in the configuration file corresponds to an undirected (symmetric) graph. That is, if node u is a neighbor of node v, then v is also a neighbor of u. Further, the communication topology generates a connected graph. Develop and implement a distributed algorithm that builds a spanning tree of the system rooted at a given node (specified in the configuration file). Note that, when the spanning tree construction algorithm terminates, each node should know its parent and its children in the tree.

This is not BFS. As network is asynchronous, message transmission time is random and leads to not forming BFS.


Algorithm:

1. Root node starts spanning tree construction by setting parent =*
2. All other node
3.  IF message received 
4.      IF(parent==-1) send ACK back and set parent = message.nodeID
5.      Else send NACK
6.  If (Message == ACK) children++ and childrenArray[children-1] = message.nodeID
7. If (Number of request sent == number ACK + Number of NACK) Output Parent and children

Teminology and Technology used:

Distributed system
Asynchronous network
Spanning tree construction
TCP/IP server client
SCTP server client
Java
Multi-threading
Synchronized methods

Thanks!

Bharat Bhavsar
