package org.onetwo.common.spring.mcache;

import java.io.Serializable;

public class HashCodeCacheKey implements Serializable {
	private static final long serialVersionUID = 3904677167731454262L;

	  /**
	   * Number that helps keep the uniqueness of this key.
	   */
	  private long checkSum;

	  /**
	   * Pre-calculated hash code.
	   */
	  private int hashCode;

	  /**
	   * Construct a <code>HashCodeCacheKey</code>.
	   */
	  public HashCodeCacheKey() {
	    super();
	  }

	  /**
	   * Construct a <code>HashCodeCacheKey</code>.
	   * 
	   * @param newCheckSum
	   *          the number that helps keep the uniqueness of this key
	   * @param newHashCode
	   *          the pre-calculated hash code
	   */
	  public HashCodeCacheKey(long newCheckSum, int newHashCode) {
	    this();
	    setCheckSum(newCheckSum);
	    setHashCode(newHashCode);
	  }

	  /**
	   * @see Object#equals(Object)
	   */
	  public boolean equals(Object obj) {
	    if (this == obj) return true;
	    if (!(obj instanceof HashCodeCacheKey)) return false;

	    HashCodeCacheKey other = (HashCodeCacheKey) obj;
	    if (checkSum != other.checkSum) return false;
	    if (hashCode != other.hashCode) return false;

	    return true;
	  }

	  /**
	   * @return the number that helps keep the uniqueness of this key
	   */
	  public long getCheckSum() {
	    return checkSum;
	  }

	  /**
	   * @return the pre-calculated hash code
	   */
	  public int getHashCode() {
	    return hashCode;
	  }

	  /**
	   * @see Object#hashCode()
	   */
	  public int hashCode() {
	    return getHashCode();
	  }

	  /**
	   * Sets the number that helps keep the uniqueness of this key.
	   * 
	   * @param newCheckSum
	   *          the new number
	   */
	  public void setCheckSum(long newCheckSum) {
	    checkSum = newCheckSum;
	  }

	  /**
	   * Sets the pre-calculated hash code.
	   * 
	   * @param newHashCode
	   *          the new hash code
	   */
	  public void setHashCode(int newHashCode) {
	    hashCode = newHashCode;
	  }

	  /**
	   * @see Object#toString()
	   */
	  public String toString() {
	    return getHashCode() + "|" + getCheckSum();
	  }
}
