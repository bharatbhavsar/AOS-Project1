/**
 * * This is server class which opens server port. This class uses SCTP protocol to create server port.
 * Except for root node, all other nodes will initiate their client threads through
 * server thread using this block. For root node, server thread will be created, but all
 * its client threads are initiated through master thread.
 * @author Bharat M Bhavsar
 */
import java.io.IOException;
import java.util.Map.Entry;
import java.net.*;
import com.sun.nio.sctp.*;
import java.nio.*;

public class spanningServerSCTP implements Runnable {

	public static final int MESSAGE_SIZE = 100;
	int clientPortNumber = spanningTreeSCTP.myPort + 1;

	public void run() {
		
		ByteBuffer byteBufferForReceive = ByteBuffer.allocate(MESSAGE_SIZE);
		ByteBuffer byteBufferForSend = ByteBuffer.allocate(MESSAGE_SIZE);
		try {
			// opened server channel and that binds the socket port number to the channel 
			SctpServerChannel sctpServerChannel = SctpServerChannel.open();
			InetSocketAddress serverAddr = new InetSocketAddress(spanningTreeSCTP.myPort);
			sctpServerChannel.bind(serverAddr);

			while (true) {
				//Server starts accepting request once it's port is opened.
				SctpChannel sctpServerSocket = sctpServerChannel.accept();
				//Message is received with binded information
				MessageInfo infoForReceivedMessage = sctpServerSocket.receive(byteBufferForReceive,null,null);
				String messageReceived = byteToString(byteBufferForReceive);
				MessageInfo infoForSendMessage = MessageInfo.createOutgoing(null,0);
				/*
				 * If this is first received message, that means no parent defined and
				 * sender is potential parent. This block accept sender as parent and
				 * sets parent value. Also initiate client threads and sends 'ACK' response
				 * to parent. 
				 */
				if (spanningTreeSCTP.myParent == "-1") {
					byteBufferForSend.clear();
					byteBufferForSend.put(("ACK").getBytes());
					byteBufferForSend.flip();
					sctpServerSocket.send(byteBufferForSend,infoForSendMessage);
					for (Entry<String, String> entry : spanningTreeSCTP.nodeList.entrySet()) {
						if (entry.getKey().equalsIgnoreCase(messageReceived))
							continue;
						new Thread(new spanningClientSCTP(entry.getKey(), spanningTreeSCTP.getHostName(entry.getValue()),
								spanningTreeSCTP.getPortNumber(entry.getValue()), clientPortNumber++)).start();
						synchronized (spanningTreeSCTP.numberOfNeighborsRequested) {
							spanningTreeSCTP.numberOfNeighborsRequested++;
						}
					}
					spanningTreeSCTP.myParent = messageReceived;
				} else {
					/*
					 * This block is used when parent is already defined and need to
					 * send 'NACK' to all requests came after first request.
					 */
					byteBufferForSend.clear();
					byteBufferForSend.put(("NACK").getBytes());
					byteBufferForSend.flip();
					sctpServerSocket.send(byteBufferForSend,infoForSendMessage);
				}
			}
		} catch (IOException ex) {
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