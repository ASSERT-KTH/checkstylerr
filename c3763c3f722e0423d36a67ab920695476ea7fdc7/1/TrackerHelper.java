package nl.tudelft.blockchain.scaleoutdistributedledger;

import nl.tudelft.blockchain.scaleoutdistributedledger.model.Node;
import nl.tudelft.blockchain.scaleoutdistributedledger.model.OwnNode;
import nl.tudelft.blockchain.scaleoutdistributedledger.utils.Log;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Inet4Address;
import java.util.Map;
import java.util.logging.Level;

/**
 * Helper class for interacting with the tracker.
 */
public final class TrackerHelper {
	private TrackerHelper() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Reset the tracker server with a new empty nodelist.
	 * @return - boolean identifying if the reset was successful
	 * @throws IOException - exception while resetting tracker server
	 */
	public static boolean resetTrackerServer() throws IOException {
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpPost request = new HttpPost(String.format("http://%s:%d/reset", Application.TRACKER_SERVER_ADDRESS, Application.TRACKER_SERVER_PORT));
			JSONObject response = new JSONObject(IOUtils.toString(client.execute(request).getEntity().getContent()));
			if (response.getBoolean("success")) {
				Log.log(Level.INFO, "Successfully resetted the tracker server");
				return true;
			}
			Log.log(Level.SEVERE, "Error while resetting the tracker server");
			return false;
		}
	}

	/**
	 * Registers this node with the given public key.
	 * @param nodePort  - the port of the node
	 * @param publicKey - the publicKey of the new node
	 * @return - the registered node
	 * @throws IOException - IOException while registering node
	 * @throws NodeRegisterFailedException - Server side exception while registering node
	 */
	public static OwnNode registerNode(int nodePort, byte[] publicKey) throws IOException, NodeRegisterFailedException {
		JSONObject json = new JSONObject();
		json.put("address", Inet4Address.getLocalHost().getHostAddress());
		json.put("port", nodePort);
		json.put("publicKey", publicKey);

		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			StringEntity requestEntity = new StringEntity(json.toString(), ContentType.APPLICATION_JSON);
			HttpPost request = new HttpPost(String.format("http://%s:%d/register-node", Application.TRACKER_SERVER_ADDRESS, Application.TRACKER_SERVER_PORT));
			request.setEntity(requestEntity);
			JSONObject response = new JSONObject(IOUtils.toString(client.execute(request).getEntity().getContent()));
			if (response.getBoolean("success")) {
				Log.log(Level.INFO, "Successfully registered node to tracker server");
				return new OwnNode(response.getInt("id"), publicKey,
						Inet4Address.getLocalHost().getHostAddress(), nodePort);
			}
			Log.log(Level.SEVERE, "Error while registering node");
			throw new NodeRegisterFailedException();
		}
	}

	/**
	 *
	 * Updates the given map of nodes with new information from the tracker.
	 * @param nodes - the map of nodes
	 * @throws IOException - exception while updating nodes
	 */
	public static void updateNodes(Map<Integer, Node> nodes, OwnNode ownNode) throws IOException {
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpGet request = new HttpGet(String.format("http://%s:%d", Application.TRACKER_SERVER_ADDRESS, Application.TRACKER_SERVER_PORT));
			JSONArray nodesArray = (JSONArray) new JSONObject(IOUtils.toString(client.execute(request).getEntity().getContent())).get("nodes");

			for (int i = 0; i < nodesArray.length(); i++) {
				JSONObject object = (JSONObject) nodesArray.get(i);
				byte[] publicKey = jsonArrayToByteArray((JSONArray) object.get("publicKey"));
				String address = object.getString("address");
				int port = object.getInt("port");
				if (nodes.containsKey(i)) {
					Node node = nodes.get(i);
					node.setAddress(address);
					node.setPort(port);
				} else {
					Node node = new Node(i, publicKey, address, port);
					
					if (ownNode != null) {
						node.getChain().setGenesisBlock(ownNode.getChain().getGenesisBlock());
					}
					
					nodes.put(i, node);
				}
			}
		}
	}

	/** Get the number of registered nodes in tracker.
	 * @return the number of nodes already registered in tracker
	 * @throws IOException when problems with creating/closing http client
	 */
	public static int getRegistered() throws IOException {
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpGet request = new HttpGet(String.format("http://%s:%d/registered", Application.TRACKER_SERVER_ADDRESS, Application.TRACKER_SERVER_PORT));
			return new JSONObject(IOUtils.toString(client.execute(request).getEntity().getContent())).getInt("registered");
		}
	}

	/**
	 * Converts JSONArray containing ints to byte array.
	 * @param json - the jsonarray to convert.
	 * @return - the generated byte array
	 */
	private static byte[] jsonArrayToByteArray(JSONArray json) {
		byte[] res = new byte[json.length()];
		for (int i = 0; i < json.length(); i++) {
			res[i] = (byte) json.getInt(i);
		}
		return res;
	}
}
