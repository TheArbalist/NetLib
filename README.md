# NetLib
NetLib is a small and stable NIO and OIO networking library. It supports both TCP and UDP. It uses it's own protocol, so it expects you to use NetLib on both client and server side. Currently, there's only a NIO server. NetLib focuses on stability and scalability, if there's really urgent need for an OIO client, I'll implement it.

## Todo
- Encryption
- Write proper documentation for all classes

## Packets
NetLib doesn't expose direct access to sockets and thus not to streams/ByteBuffers. Instead, it provides high-performance
Packet and PacketBuilder classes.

A Packet must always contain an opcode, which can be set using the PacketBuilder#opcode(int) method. This is an example of how to construct a Packet with opcode 0x01:
```java
Packet packet = new PacketBuilder().opcode(0x01).toPacket();
```

You can write any primitive data to a PacketBuilder, it also provides a method to write a String, this, however, requires a 2 bytes header(short).

## Starting a Server
Starting a server is as easy as:
```java
Server server = new NioServer();
server.bind(4444, 5555);
```

Server implements EndPoint, so it can be closed using the EndPoint#close() method.

#Starting a Client
Starting a client is as easy as:
```java
Client client = new OioClient();
client.connect("127.0.0.1", 4444, 5555);
```

Client implements EndPoint, so it can be closed using the EndPoint#close() method.

## Listeners
A ConnectionListener can listen to four events:
- A new incoming connection
- A connection that has been closed
- A packet that has been received
- A non-vital exception that was thrown(

To receive events, you can attach a ConnectionListener to an EndPoint(Client or Server) using:
```java
Client client = new OioClient();
client.addConnectionListener(...);
