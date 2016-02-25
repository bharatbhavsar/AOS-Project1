/**
 * This program generates spanning tree on distributed system.
 * Required inputs are Node ID and Neighbors
 * These inputs are provided on command line as arguments through launcher script
 * which have already parsed the input configuration file.
 * Every node opens its server and create connection to server of other neighbor
 * nodes through own client
 * 
 * @author: Bharat M Bhavsar UTDallas 2021218838
 */
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

public class spanningTreeSCTP {
	/* All required variables are initiated here which will be used at different
	stages of program.*/
	static Thread myServerThread;
	static Thread myClientThread;
	static String myNodeID;
	static int myPort;
	static int myClientPort;
	static String myConfigFileName;
	static HashSet<String> myNeighbors = new HashSet<String>();
	static Integer numberOfNeighborsResponded = 0;
	static Integer numberOfNeighborsRequested = 0;
	static String configFileName;
	static HashMap<String, String> nodeList = new HashMap<String, String>();
	static String myParent = "-1";
	static ArrayList<String> children = new ArrayList<String>();

	public static void main(String[] args) throws IOException, InterruptedException {

		/*
		 * This block of code checks if the current node is root node or not
		 * All other nodes will have 4 command line arguments where as root node will
		 * receive 5 command line arguments. If argument size is different than
		 * 4 or 5 then program throws error.
		 */
		if (args.length == 4) {
			init(args);
			startServerProcess();
		} else if (args.length == 5) { // This block of code will only be called if given node is selected as rootNode.
			init(args);
			myParent = "*";
			myServerThread = new Thread(new spanningServerSCTP());
			myServerThread.start();
			makeAspanningTreeSCTP();
		} else
			System.out.println("Wrong number of input parameters. Can't process");
	}

	/**
	 * Initiates client threads for root node. And once all threads receive response,
	 * it prints output to file and terminates. 
	 * @throws IOException
	 */
	private static void makeAspanningTreeSCTP() throws IOException {
		for (Entry<String, String> entry : nodeList.entrySet()) {
			new Thread(
					new spanningClientSCTP(entry.getKey(), getHostName(entry.getValue()), getPortNumber(entry.getValue()),myClientPort++))
							.start();
			numberOfNeighborsRequested++;
		}
		
		while (true) {
			synchronized (numberOfNeighborsResponded) {
				if (spanningTreeSCTP.numberOfNeighborsRequested == spanningTreeSCTP.numberOfNeighborsResponded) {
					writeToFile();
					break;
				}
			}
		}
	}

	/**
	 * All command line arguments are parsed and stored in appropriate variable.
	 * This block is used by both root and non root node. 
	 * @param args
	 */
	static void init(String[] args) {
		myNodeID = args[0];
		String[] neighbors = args[2].split(" ");
		for (int i = 0; i < neighbors.length; i++)
			myNeighbors.add(neighbors[i]);

		String[] nodes = args[1].split("#");

		for (int i = 0; i < nodes.length; i++) {
			String[] temp = nodes[i].split(" ");
			if (temp[0].equalsIgnoreCase(myNodeID)) {
				myPort = Integer.parseInt(temp[2]);
				continue;
			}
			if (myNeighbors.contains(temp[0]))
				nodeList.put(temp[0], temp[1] + '#' + temp[2]);
		}
		myConfigFileName = args[3];
		myClientPort = myPort + 1;
	}

	/**
	 * This block is used by non root nodes to initiate the server thread.
	 * And once it receive response for all its requests, it write output 
	 * to file and terminates. 
	 * @throws IOException
	 */
	private static void startServerProcess() throws IOException {
		myServerThread = new Thread(new spanningServerSCTP());
		myServerThread.start();
		while (true) {
			synchronized (spanningTreeSCTP.numberOfNeighborsResponded) {
				if (!spanningTreeSCTP.myParent.equalsIgnoreCase("-1")) {
					if (spanningTreeSCTP.numberOfNeighborsRequested == spanningTreeSCTP.numberOfNeighborsResponded) {
						writeToFile();
						break;
					}
				}
			}
		}
	}

	/**
	 * Following 2 methods return for host name and host ID. 
	 * @param value
	 * @return
	 */
	static String getHostName(String value) {
		return value.split("#")[0];
	}

	static int getPortNumber(String value) {
		return Integer.parseInt(value.split("#")[1]);
	}

	/**
	 * This block of code creates output file as per the requirement and writes
	 * parent ID on first line of the file and children IDs on second line
	 * separated by single space. If no child, then child is equal to '*'
	 * @throws IOException
	 */
	public static void writeToFile() throws IOException {
		String fileName = myConfigFileName + "-" + myNodeID + ".out";
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
		writer.write(myParent.toString());
		writer.newLine();
		if (children.size() == 0)
			writer.write("*");
		else {
			for (String child : children) {
				writer.write(child + " ");
			}
		}
		writer.close();
	}
}