/*
 * Copyright (C) 2018 dy_only, Inc. All Rights Reserved.
 */
package com.hlj.utils.ip;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IPSeeker {
	private static String IP_FILE = "";
	private static final int IP_RECORD_LENGTH = 7;
	private static final byte AREA_FOLLOWED = 1;
	private static final byte NO_AREA = 2;
	private Map ipCache = new ConcurrentHashMap();
	private RandomAccessFile ipFile;
	private MappedByteBuffer mbb;
	private static IPSeeker instance;
	private long ipBegin;
	private long ipEnd;
	private IpLocation loc = new IpLocation();
	private byte[] buf = new byte[100];
	private byte[] b4 = new byte[4];
	private byte[] b3 = new byte[3];

	static {
		TODO 讲这里变成我们自己的路径
		IP_FILE =  IPSeeker.class.getResource (qqwry.dat).getPath();
		instance = new IPSeeker();
	}

	public static IPSeeker getInstance() {
		return instance;
	}

	public IPEntry getAddress(String ip) {
		String country = this.getCountry(ip).equals(" CZ88.NET") ? "null" : this.getCountry(ip);
		String address = this.getArea(ip).equals(" CZ88.NET") ? "null" : this.getArea(ip);
		return new IPEntry(country,address);
	}

	private IPSeeker() {
		try {
			this.ipFile = new RandomAccessFile(IP_FILE, "r");
		} catch (FileNotFoundException arg1) {
			this.ipFile = null;
		}

		if (this.ipFile != null) {
			try {
				this.ipBegin = this.readLong4(0L);
				this.ipEnd = this.readLong4(4L);
				if (this.ipBegin == -1L || this.ipEnd == -1L) {
					this.ipFile.close();
					this.ipFile = null;
				}
			} catch (IOException arg2) {
				System.out.println("IP地址信息文件格式有错误，IP显示功能将无法使用");
				this.ipFile = null;
			}
		}

	}


	public String getCountry(byte[] ip) {
		if (this.ipFile == null) {
			return "错误的IP数据库文件";
		} else {
			String ipStr = CzUtils.getIpStringFromBytes(ip);
			IpLocation loc;
			if (this.ipCache.containsKey(ipStr)) {
				loc = (IpLocation) this.ipCache.get(ipStr);
				return loc.country;
			} else {
				loc = this.getIPLocation(ip);
				this.ipCache.put(ipStr, loc.getCopy());
				return loc.country;
			}
		}
	}

	public String getCountry(String ip) {
		return this.getCountry(CzUtils.getIpByteArrayFromString(ip));
	}

	public String getArea(byte[] ip) {
		if (this.ipFile == null) {
			return "错误的IP数据库文件";
		} else {
			String ipStr = CzUtils.getIpStringFromBytes(ip);
			IpLocation loc;
			if (this.ipCache.containsKey(ipStr)) {
				loc = (IpLocation) this.ipCache.get(ipStr);
				return loc.area;
			} else {
				loc = this.getIPLocation(ip);
				this.ipCache.put(ipStr, loc.getCopy());
				return loc.area;
			}
		}
	}

	public String getArea(String ip) {
		return this.getArea(CzUtils.getIpByteArrayFromString(ip));
	}

	private IpLocation getIPLocation(byte[] ip) {
		IpLocation info = null;
		long offset = this.locateIP(ip);
		if (offset != -1L) {
			info = this.getIPLocation(offset);
		}

		if (info == null) {
			info = new IpLocation();
			info.country = "未知国家";
			info.area = "未知地区";
		}

		return info;
	}

	private long readLong4(long offset) {
		long ret = 0L;

		try {
			this.ipFile.seek(offset);
			ret |= (long) (this.ipFile.readByte() & 255);
			ret |= (long) (this.ipFile.readByte() << 8 & '＀');
			ret |= (long) (this.ipFile.readByte() << 16 & 16711680);
			ret |= (long) (this.ipFile.readByte() << 24 & -16777216);
			return ret;
		} catch (IOException arg5) {
			return -1L;
		}
	}

	private long readLong3(long offset) {
		long ret = 0L;

		try {
			this.ipFile.seek(offset);
			this.ipFile.readFully(this.b3);
			ret |= (long) (this.b3[0] & 255);
			ret |= (long) (this.b3[1] << 8 & '＀');
			ret |= (long) (this.b3[2] << 16 & 16711680);
			return ret;
		} catch (IOException arg5) {
			return -1L;
		}
	}

	private long readLong3() {
		long ret = 0L;

		try {
			this.ipFile.readFully(this.b3);
			ret |= (long) (this.b3[0] & 255);
			ret |= (long) (this.b3[1] << 8 & '＀');
			ret |= (long) (this.b3[2] << 16 & 16711680);
			return ret;
		} catch (IOException arg3) {
			return -1L;
		}
	}

	private void readIP(long offset, byte[] ip) {
		try {
			this.ipFile.seek(offset);
			this.ipFile.readFully(ip);
			byte e = ip[0];
			ip[0] = ip[3];
			ip[3] = e;
			e = ip[1];
			ip[1] = ip[2];
			ip[2] = e;
		} catch (IOException arg4) {
			System.out.println(arg4.getMessage());
		}

	}



	private int compareIP(byte[] ip, byte[] beginIp) {
		for (int i = 0; i < 4; ++i) {
			int r = this.compareByte(ip[i], beginIp[i]);
			if (r != 0) {
				return r;
			}
		}

		return 0;
	}

	private int compareByte(byte b1, byte b2) {
		return (b1 & 255) > (b2 & 255) ? 1 : ((b1 ^ b2) == 0 ? 0 : -1);
	}

	private long locateIP(byte[] ip) {
		long m = 0L;
		this.readIP(this.ipBegin, this.b4);
		int r = this.compareIP(ip, this.b4);
		if (r == 0) {
			return this.ipBegin;
		} else if (r < 0) {
			return -1L;
		} else {
			long i = this.ipBegin;
			long j = this.ipEnd;

			while (i < j) {
				m = this.getMiddleOffset(i, j);
				this.readIP(m, this.b4);
				r = this.compareIP(ip, this.b4);
				if (r > 0) {
					i = m;
				} else {
					if (r >= 0) {
						return this.readLong3(m + 4L);
					}

					if (m == j) {
						j -= 7L;
						m = j;
					} else {
						j = m;
					}
				}
			}

			m = this.readLong3(m + 4L);
			this.readIP(m, this.b4);
			r = this.compareIP(ip, this.b4);
			if (r <= 0) {
				return m;
			} else {
				return -1L;
			}
		}
	}

	private long getMiddleOffset(long begin, long end) {
		long records = (end - begin) / 7L;
		records >>= 1;
		if (records == 0L) {
			records = 1L;
		}

		return begin + records * 7L;
	}

	private IpLocation getIPLocation(long offset) {
		try {
			this.ipFile.seek(offset + 4L);
			byte e = this.ipFile.readByte();
			if (e == 1) {
				long countryOffset = this.readLong3();
				this.ipFile.seek(countryOffset);
				e = this.ipFile.readByte();
				if (e == 2) {
					this.loc.country = this.readString(this.readLong3());
					this.ipFile.seek(countryOffset + 4L);
				} else {
					this.loc.country = this.readString(countryOffset);
				}

				this.loc.area = this.readArea(this.ipFile.getFilePointer());
			} else if (e == 2) {
				this.loc.country = this.readString(this.readLong3());
				this.loc.area = this.readArea(offset + 8L);
			} else {
				this.loc.country = this.readString(this.ipFile.getFilePointer() - 1L);
				this.loc.area = this.readArea(this.ipFile.getFilePointer());
			}

			return this.loc;
		} catch (IOException arg5) {
			return null;
		}
	}


	private String readArea(long offset) throws IOException {
		this.ipFile.seek(offset);
		byte b = this.ipFile.readByte();
		if (b != 1 && b != 2) {
			return this.readString(offset);
		} else {
			long areaOffset = this.readLong3(offset + 1L);
			return areaOffset == 0L ? "未知地区" : this.readString(areaOffset);
		}
	}


	private String readString(long offset) {
		try {
			this.ipFile.seek(offset);
			int e = 0;

			for (this.buf[e] = this.ipFile.readByte(); this.buf[e] != 0; this.buf[e] = this.ipFile.readByte()) {
				++e;
			}

			if (e != 0) {
				return CzUtils.getString(this.buf, 0, e, "GBK");
			}
		} catch (IOException arg3) {
			System.out.println(arg3.getMessage());
		}

		return "";
	}



	private class IpLocation {
		public String country;
		public String area;

		public IpLocation() {
			this.country = this.area = "";
		}

		public IpLocation getCopy() {
			IpLocation ret = IPSeeker.this.new IpLocation();
			ret.country = this.country;
			ret.area = this.area;
			return ret;
		}
	}



}
