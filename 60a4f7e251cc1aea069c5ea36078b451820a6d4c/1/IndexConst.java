package ai.preferred.cerebro.index.utils;

import java.util.Collections;
import java.util.Set;

/**
 * Cerebro's reserved keyword list and necessary constants.
 *
 * @author hpminh@apcs.vn
 */
public class IndexConst {
    //Reserved keywords to avoid using as fieldname
    public final static String IDFieldName = "ID";
    public final static String VecFieldName = "Feature_Vector";
    public final static String VecLenFieldName = "Vec_Length";
    public final static String HashFieldName = "LSH_Hash_Code";
    public static final String CONTENTS = "contents";

    public static final long mb = 1 << 20;
}