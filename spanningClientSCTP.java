/**
 * This is client class which uses SCTP client protocol to create connection
 * with server with the help of server host name and port. SCTP requires to
 * bind this server port with client local port. Once the response from
 * server is received, client parse that response and closes connection.
 * 
 * @author Bharat M Bhavsar
 */
import java.io.IOException;
import java.net.*;
import com.sun.nio.sctp.*;
import java.nio.*;

public class spanningClientSCTP implements Runnable {

	String neighborID;
	String hostName;
	int serverPortNumber;
	int clientPortNumber;
	public static final int MESSAGE_SIZE = 100;

	public spanningClientSCTP(String neighborID, String hostName, int serverPortNumber, int clientPortNumber) {
		/*
		 * Constructor for client thread where all required information is received
		 * as arguments from caller and stored in local variables.
		 */
		super();
		this.neighborID = neighborID;
		this.hostName = hostName;
		this.serverPortNumber = serverPortNumber;
		this.clientPortNumber = clientPortNumber;
	}

	public void run() {
		String response = "";
		ByteBuffer byteBufferForMessageSent = ByteBuffer.allocate(MESSAGE_SIZE);
		ByteBuffer byteBufferForMessageReceived = ByteBuffer.allocate(MESSAGE_SIZE);
		String messageToServer = spanningTreeSCTP.myNodeID;
		try {
			/*
			 * Create connection with serve which is binded that connection port
			 * to the local port. This is different than normal TCP/IP client port
			 */
			SocketAddress socketAddress = new InetSocketAddress(hostName, serverPortNumber);
			SctpChannel sctpClientChannel = SctpChannel.open();
			sctpClientChannel.bind(new InetSocketAddress(clientPortNumber));
			sctpClientChannel.connect(socketAddress);
			/*
			 * Create outgoing message with required information to bind to it.
			 * Message is always sent and received in byte format for both client and
			 * server in SCTP client-server protocol
			 */
			MessageInfo infoOfMessageToServer = MessageInfo.createOutgoing(null,0);
			byteBufferForMessageSent.put(messageToServer.getBytes());
			byteBufferForMessageSent.flip();
			sctpClientChannel.send(byteBufferForMessageSent,infoOfMessageToServer);
			/*
			 * Receive message and parse it to identify it it is ACK or NACK
			 * If it is ACK then the server ID is added to children list else
			 * for NACK nothing has to be done and after this parsing, client
			 * close the connection.
			 */
			MessageInfo infoOfMessageFromServer = sctpClientChannel.receive(byteBufferForMessageReceived,null,null);
			response = byteToString(byteBufferForMessageReceived).split("\0")[0];
			if (response.equals("ACK")) {
				spanningTreeSCTP.children.add(neighborID);
			}
			synchronized (spanningTreeSCTP.numberOfNeighborsResponded) {
				spanningTreeSCTP.numberOfNeighborsResponded++;
			}
			sctpClientChannel.close();
		} 
		catch (ConnectException e){
			/*
			 * This try-catch block is used to handle connectionException
			 * only when other node has 
			 * not yet started its server and therefore connection
			 * is refused where this client will wait for few seconds and
			 * will try again to connect.
			 */
			try {
				Thread.currentThread();
				Thread.sleep(50);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			run();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * This block is used to change received byteBuffered message
	 * to string. Remember that this block converts whole byteBuffer
	 * into string and padding is done for black bytes with null value.
	 * @param byteBuffer
	 * @return
	 */
	public String byteToString(ByteBuffer byteBuffer)
	{
		byteBuffer.position(0);
		byteBuffer.limit(MESSAGE_SIZE);
		byte[] bufArr = new byte[byteBuffer.remaining()];
		byteBuffer.get(bufArr);
		return new String(bufArr);
	}
}