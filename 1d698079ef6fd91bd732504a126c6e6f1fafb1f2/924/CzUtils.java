/*
 * Copyright (C) 2018 dy_only, Inc. All Rights Reserved.
 */
package com.hlj.utils.ip;

import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

public class CzUtils {
	public static byte[] getIpByteArrayFromString(String ip) {
		byte[] ret = new byte[4];
		StringTokenizer st = new StringTokenizer(ip, ".");

		try {
			ret[0] = (byte) (Integer.parseInt(st.nextToken()) & 255);
			ret[1] = (byte) (Integer.parseInt(st.nextToken()) & 255);
			ret[2] = (byte) (Integer.parseInt(st.nextToken()) & 255);
			ret[3] = (byte) (Integer.parseInt(st.nextToken()) & 255);
		} catch (Exception arg3) {
			System.out.println(arg3.getMessage());
		}

		return ret;
	}

	public static void main(String[] args) {
		byte[] a = getIpByteArrayFromString(args[0]);

		for (int i = 0; i < a.length; ++i) {
			System.out.println(a[i]);
		}

		System.out.println(getIpStringFromBytes(a));
	}

	public static String getString(String s, String srcEncoding, String destEncoding) {
		try {
			return new String(s.getBytes(srcEncoding), destEncoding);
		} catch (UnsupportedEncodingException arg3) {
			return s;
		}
	}

	public static String getString(byte[] b, String encoding) {
		try {
			return new String(b, encoding);
		} catch (UnsupportedEncodingException arg2) {
			return new String(b);
		}
	}

	public static String getString(byte[] b, int offset, int len, String encoding) {
		try {
			return new String(b, offset, len, encoding);
		} catch (UnsupportedEncodingException arg4) {
			return new String(b, offset, len);
		}
	}

	public static String getIpStringFromBytes(byte[] ip) {
		StringBuffer sb = new StringBuffer();
		sb.append(ip[0] & 255);
		sb.append('.');
		sb.append(ip[1] & 255);
		sb.append('.');
		sb.append(ip[2] & 255);
		sb.append('.');
		sb.append(ip[3] & 255);
		return sb.toString();
	}
}
