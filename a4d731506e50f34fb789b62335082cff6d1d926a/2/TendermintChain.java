package nl.tudelft.blockchain.scaleoutdistributedledger.model.mainchain.tendermint;

import com.github.jtendermint.jabci.socket.TSocket;
import nl.tudelft.blockchain.scaleoutdistributedledger.model.BlockAbstract;
import nl.tudelft.blockchain.scaleoutdistributedledger.model.Sha256Hash;
import nl.tudelft.blockchain.scaleoutdistributedledger.model.mainchain.MainChain;
import nl.tudelft.blockchain.scaleoutdistributedledger.utils.Log;

import java.util.*;
import java.util.logging.Level;

/**
 * Class implementing {@link MainChain} for a Tendermint chain.
 * @see <a href="https://tendermint.com/">Tendermint.com</a>
 */
public final class TendermintChain implements MainChain {
	public static final String DEFAULT_ADDRESS = "localhost";
	public static final int DEFAULT_ABCI_SERVER_PORT = 46658;
	public int abciServerPort;

	private ABCIServer handler;
	private ABCIClient client;
	private TSocket socket;

	private Set<Sha256Hash> cache;
	private long currentHeight = 0;

	/**
	 * Create and start the ABCI app (server) to connect with Tendermint on the default port (46658).
	 * Also uses (port - 1), which Tendermint should listen on for RPC (rpc.laddr)
	 */
	public TendermintChain() {
		this(DEFAULT_ABCI_SERVER_PORT);
	}
	/**
	 * Create and start the ABCI app (server) to connect with Tendermint on the given port.
	 * Also uses (port - 1), which Tendermint should listen on for RPC (rpc.laddr)
	 * @param port - the port on which we run the server
	 */
	public TendermintChain(final int port) {
		abciServerPort = port;
		this.cache = new HashSet<>();

		socket = new TSocket();
		handler = new ABCIServer(this);

		socket.registerListener(handler);

		Thread t = new Thread(() -> socket.start(abciServerPort));
		t.setName("Main Chain Socket");
		t.start();
		this.initClient();
		this.initialUpdateCache();
		Log.log(Level.INFO, "Successfully started Tendermint chain (ABCI server + client); server on " + DEFAULT_ADDRESS + ":" + abciServerPort);
	}

	/**
	 * Called on start of the instance.
	 */
	public void initClient() {
		this.client = new ABCIClient(DEFAULT_ADDRESS + ":" + (abciServerPort - 1));
		Log.log(Level.INFO, "Started ABCI Client on " + DEFAULT_ADDRESS + ":" + (abciServerPort - 1));
	}

	private void initialUpdateCache() {
		boolean updated = false;
		do {
			try {
				updateCacheBlocking(-1);
				updated = true;
			} catch (Exception e) {
				int retryTime = 3;
				Log.log(Level.INFO, "Could not update cache on startup, trying again in " + retryTime + "s.");
				Log.log(Level.FINE, "", e);
				try {
					Thread.sleep(retryTime * 1000);
				} catch (InterruptedException e1) {
					Thread.currentThread().interrupt();
				}
			}
		} while (!updated);
		Log.log(Level.INFO, "Successfully updated cache on startup.");
	}

	/**
	 * Update the cache of the chain.
	 * This method starts a separate thread, so the cache is not yet updated on returning from this call.
	 *
	 * @param height - The height to update to, if -1 check the needed height with Tendermint
	 */
	protected void updateCache(long height) {
		if (client == null) return; // If in startup
		new Thread(() -> {
			updateCacheBlocking(height);
		}).start();
	}

	/**
	 * Update the cache of the chain.
	 * Note that this method is blocking and execution may therefore take a while,
	 * It is recommended to use {@link TendermintChain#updateCache(long)} instead.
	 *
	 * @param height - The height to update to, if -1 check the needed height with Tendermint
	 */
	private void updateCacheBlocking(long height) {
		if (height == -1) {
			height = this.client.status().getLong("latest_block_height");
		}

		for (long i = currentHeight + 1; i <= height; i++) {
			List<BlockAbstract> abstractsAtCurrentHeight = this.client.query(i);
			if (abstractsAtCurrentHeight == null) {
				Log.log(Level.WARNING, "Could not get block at height " + i + ", perhaps the tendermint rpc is not (yet) running (or broken)");
				return;
			}
			for (BlockAbstract abs : abstractsAtCurrentHeight) {
				cache.add(abs.getBlockHash());
			}
		}
		if (currentHeight < height) {
			Log.log(Level.INFO, "Successfully updated the Tendermint cache from height " + currentHeight + " -> " + height
					+ ", number of cached hashes of abstracts on main chain is now " + cache.size());
		}
		currentHeight = Math.max(currentHeight, height);	// For concurrency reasons use the maximum
	}

	/**
	 * Stop the connection to Tendermint.
	 * Used for testing.
	 */
	protected void stop() {
		socket.stop();
	}

	@Override
	public Sha256Hash commitAbstract(BlockAbstract abs) {
		byte[] hash = client.commit(abs);
		if (hash == null) {
			Log.log(Level.INFO, "Commiting abstract to tendermint failed");
			return null;
		} else {
			abs.setAbstractHash(Sha256Hash.withHash(hash));

			abs.setOnMainChain(Optional.of(true));
			return Sha256Hash.withHash(hash);
		}
	}

	@Override
	public boolean isPresent(Sha256Hash blockHash) {
		if (cache.contains(blockHash)) {
			return true;
		} else {
			// We could miss some blocks in our cache, so update and wait for the results
			updateCacheBlocking(-1);

			return cache.contains(blockHash);
			//TODO: We might want to check the actual main chain in the false case
			//      For when an abstract is in a block that is not yet closed by an ENDBLOCK
			//		This now works because the block size is 1
		}
	}

}
