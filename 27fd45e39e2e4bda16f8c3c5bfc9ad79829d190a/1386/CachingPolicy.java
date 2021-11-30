
/**
 * Enum class containing the four caching strategies implemented in the pattern.
 */
public enum CachingPolicy {

	THROUGH("through"), AROUND("around"), BEHIND("behind"), ASIDE("aside");

	private String policy;

	private CachingPolicy(String policy) {
		this.policy = policy;
	}

	public String getPolicy() {
		return policy;
	}
}
