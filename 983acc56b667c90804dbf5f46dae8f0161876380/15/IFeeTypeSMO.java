package com.java110.front.smo.feeType;

import com.java110.core.context.IPageData;
import org.springframework.http.ResponseEntity;

public interface IFeeTypeSMO {

    public ResponseEntity<String> list(IPageData pd);
}
