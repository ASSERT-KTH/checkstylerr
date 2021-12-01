package com.java110.fee.api;

import com.alibaba.fastjson.JSONObject;
import com.java110.dto.payFeeAudit.PayFeeAuditDto;
import com.java110.fee.bmo.payFeeAudit.IDeletePayFeeAuditBMO;
import com.java110.fee.bmo.payFeeAudit.IGetPayFeeAuditBMO;
import com.java110.fee.bmo.payFeeAudit.ISavePayFeeAuditBMO;
import com.java110.fee.bmo.payFeeAudit.IUpdatePayFeeAuditBMO;
import com.java110.po.payFeeAudit.PayFeeAuditPo;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/payFeeAudit")
public class PayFeeAuditApi {

    @Autowired
    private ISavePayFeeAuditBMO savePayFeeAuditBMOImpl;
    @Autowired
    private IUpdatePayFeeAuditBMO updatePayFeeAuditBMOImpl;
    @Autowired
    private IDeletePayFeeAuditBMO deletePayFeeAuditBMOImpl;

    @Autowired
    private IGetPayFeeAuditBMO getPayFeeAuditBMOImpl;

    /**
     * 微信保存消息模板
     *
     * @param reqJson
     * @return
     * @serviceCode /payFeeAudit/savePayFeeAudit
     * @path /app/payFeeAudit/savePayFeeAudit
     */
    @RequestMapping(value = "/savePayFeeAudit", method = RequestMethod.POST)
    public ResponseEntity<String> savePayFeeAudit(@RequestBody JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "communityId", "请求报文中未包含communityId");
        Assert.hasKeyAndValue(reqJson, "feeId", "请求报文中未包含feeId");
        Assert.hasKeyAndValue(reqJson, "auditUserId", "请求报文中未包含auditUserId");
        Assert.hasKeyAndValue(reqJson, "auditUserName", "请求报文中未包含auditUserName");
        Assert.hasKeyAndValue(reqJson, "state", "请求报文中未包含state");


        PayFeeAuditPo payFeeAuditPo = BeanConvertUtil.covertBean(reqJson, PayFeeAuditPo.class);
        return savePayFeeAuditBMOImpl.save(payFeeAuditPo);
    }

    /**
     * 微信修改消息模板
     *
     * @param reqJson
     * @return
     * @serviceCode /payFeeAudit/updatePayFeeAudit
     * @path /app/payFeeAudit/updatePayFeeAudit
     */
    @RequestMapping(value = "/updatePayFeeAudit", method = RequestMethod.POST)
    public ResponseEntity<String> updatePayFeeAudit(@RequestBody JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "communityId", "请求报文中未包含communityId");
        Assert.hasKeyAndValue(reqJson, "feeId", "请求报文中未包含feeId");
        Assert.hasKeyAndValue(reqJson, "auditUserId", "请求报文中未包含auditUserId");
        Assert.hasKeyAndValue(reqJson, "auditUserName", "请求报文中未包含auditUserName");
        Assert.hasKeyAndValue(reqJson, "state", "请求报文中未包含state");
        Assert.hasKeyAndValue(reqJson, "auditId", "auditId不能为空");


        PayFeeAuditPo payFeeAuditPo = BeanConvertUtil.covertBean(reqJson, PayFeeAuditPo.class);
        return updatePayFeeAuditBMOImpl.update(payFeeAuditPo);
    }

    /**
     * 微信删除消息模板
     *
     * @param reqJson
     * @return
     * @serviceCode /payFeeAudit/deletePayFeeAudit
     * @path /app/payFeeAudit/deletePayFeeAudit
     */
    @RequestMapping(value = "/deletePayFeeAudit", method = RequestMethod.POST)
    public ResponseEntity<String> deletePayFeeAudit(@RequestBody JSONObject reqJson) {
        Assert.hasKeyAndValue(reqJson, "communityId", "小区ID不能为空");

        Assert.hasKeyAndValue(reqJson, "auditId", "auditId不能为空");


        PayFeeAuditPo payFeeAuditPo = BeanConvertUtil.covertBean(reqJson, PayFeeAuditPo.class);
        return deletePayFeeAuditBMOImpl.delete(payFeeAuditPo);
    }

    /**
     * 微信删除消息模板
     *
     * @param communityId 小区ID
     * @return
     * @serviceCode /payFeeAudit/queryPayFeeAudit
     * @path /app/payFeeAudit/queryPayFeeAudit
     */
    @RequestMapping(value = "/queryPayFeeAudit", method = RequestMethod.GET)
    public ResponseEntity<String> queryPayFeeAudit(@RequestParam(value = "communityId") String communityId,
                                                   @RequestParam(value = "page") int page,
                                                   @RequestParam(value = "row") int row) {
        PayFeeAuditDto payFeeAuditDto = new PayFeeAuditDto();
        payFeeAuditDto.setPage(page);
        payFeeAuditDto.setRow(row);
        payFeeAuditDto.setCommunityId(communityId);
        return getPayFeeAuditBMOImpl.get(payFeeAuditDto);
    }
}
