package com.healerjean.proj.cache.avengers.H03_GeoHash;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

/**
 * @author zhangyujin
 * @date 2021/5/8  11:19 上午.
 * @description
 */
public class GeoHash {
    public static final double MINLAT = -90;
    public static final double MAXLAT = 90;
    public static final double MINLNG = -180;
    public static final double MAXLNG = 180;

    private static int numbits = 3 * 5; //经纬度单独编码长度

    private static double minLat;
    private static double minLng;

    private final static char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
            '9', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm', 'n', 'p',
            'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    //定义编码映射关系
    final static HashMap<Character, Integer> lookup = new HashMap<Character, Integer>();

    //初始化编码映射内容
    static {
        int i = 0;
        for (char c : digits)
            lookup.put(c, i++);
    }

    public GeoHash() {
        setMinLatLng();
    }

    public String encode(double lat, double lon) {
        BitSet latbits = getBits(lat, -90, 90);
        BitSet lonbits = getBits(lon, -180, 180);
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < numbits; i++) {
            buffer.append((lonbits.get(i)) ? '1' : '0');
            buffer.append((latbits.get(i)) ? '1' : '0');
        }
        String code = base32(Long.parseLong(buffer.toString(), 2));
        //Log.i("okunu", "encode  lat = " + lat + "  lng = " + lon + "  code = " + code);
        return code;
    }

    public ArrayList<String> getArroundGeoHash(double lat, double lon) {
        //Log.i("okunu", "getArroundGeoHash  lat = " + lat + "  lng = " + lon);
        ArrayList<String> list = new ArrayList<>();
        double uplat = lat + minLat;
        double downLat = lat - minLat;

        double leftlng = lon - minLng;
        double rightLng = lon + minLng;

        String leftUp = encode(uplat, leftlng);
        list.add(leftUp);

        String leftMid = encode(lat, leftlng);
        list.add(leftMid);

        String leftDown = encode(downLat, leftlng);
        list.add(leftDown);

        String midUp = encode(uplat, lon);
        list.add(midUp);

        String midMid = encode(lat, lon);
        list.add(midMid);

        String midDown = encode(downLat, lon);
        list.add(midDown);

        String rightUp = encode(uplat, rightLng);
        list.add(rightUp);

        String rightMid = encode(lat, rightLng);
        list.add(rightMid);

        String rightDown = encode(downLat, rightLng);
        list.add(rightDown);

        //Log.i("okunu", "getArroundGeoHash list = " + list.toString());
        return list;
    }

    //根据经纬度和范围，获取对应的二进制
    private BitSet getBits(double lat, double floor, double ceiling) {
        BitSet buffer = new BitSet(numbits);
        for (int i = 0; i < numbits; i++) {
            double mid = (floor + ceiling) / 2;
            if (lat >= mid) {
                buffer.set(i);
                floor = mid;
            } else {
                ceiling = mid;
            }
        }
        return buffer;
    }

    //将经纬度合并后的二进制进行指定的32位编码
    private String base32(long i) {
        char[] buf = new char[65];
        int charPos = 64;
        boolean negative = (i < 0);
        if (!negative) {
            i = -i;
        }
        while (i <= -32) {
            buf[charPos--] = digits[(int) (-(i % 32))];
            i /= 32;
        }
        buf[charPos] = digits[(int) (-i)];
        if (negative) {
            buf[--charPos] = '-';
        }
        return new String(buf, charPos, (65 - charPos));
    }

    private void setMinLatLng() {
        minLat = MAXLAT - MINLAT;
        for (int i = 0; i < numbits; i++) {
            minLat /= 2.0;
        }
        minLng = MAXLNG - MINLNG;
        for (int i = 0; i < numbits; i++) {
            minLng /= 2.0;
        }
    }

    //根据二进制和范围解码
    private double decode(BitSet bs, double floor, double ceiling) {
        double mid = 0;
        for (int i = 0; i < bs.length(); i++) {
            mid = (floor + ceiling) / 2;
            if (bs.get(i))
                floor = mid;
            else
                ceiling = mid;
        }
        return mid;
    }

    //对编码后的字符串解码
    public double[] decode(String geohash) {
        StringBuilder buffer = new StringBuilder();
        for (char c : geohash.toCharArray()) {
            int i = lookup.get(c) + 32;
            buffer.append(Integer.toString(i, 2).substring(1));
        }

        BitSet lonset = new BitSet();
        BitSet latset = new BitSet();

        //偶数位，经度
        int j = 0;
        for (int i = 0; i < numbits * 2; i += 2) {
            boolean isSet = false;
            if (i < buffer.length())
                isSet = buffer.charAt(i) == '1';
            lonset.set(j++, isSet);
        }

        //奇数位，纬度
        j = 0;
        for (int i = 1; i < numbits * 2; i += 2) {
            boolean isSet = false;
            if (i < buffer.length())
                isSet = buffer.charAt(i) == '1';
            latset.set(j++, isSet);
        }

        double lon = decode(lonset, -180, 180);
        double lat = decode(latset, -90, 90);

        return new double[]{lat, lon};
    }

    public static void main(String[] args) throws Exception {
        GeoHash geohash = new GeoHash();
//        String s = geohash.encode(40.222012, 116.248283);
//        System.out.println(s);
        geohash.getArroundGeoHash(40.222012, 116.248283);
//        double[] geo = geohash.decode(s);
//        System.out.println(geo[0]+" "+geo[1]);
    }
}
