package com.java110.core.smo.impl;

import com.java110.core.smo.IComputeFeeSMO;
import com.java110.dto.RoomDto;
import com.java110.dto.fee.*;
import com.java110.dto.owner.OwnerCarDto;
import com.java110.dto.parking.ParkingSpaceDto;
import com.java110.dto.report.ReportCarDto;
import com.java110.dto.report.ReportFeeDto;
import com.java110.dto.report.ReportRoomDto;
import com.java110.intf.community.IParkingSpaceInnerServiceSMO;
import com.java110.intf.community.IRoomInnerServiceSMO;
import com.java110.intf.fee.IFeeInnerServiceSMO;
import com.java110.intf.user.IOwnerCarInnerServiceSMO;
import com.java110.po.feeReceiptDetail.FeeReceiptDetailPo;
import com.java110.utils.constant.ResponseConstant;
import com.java110.utils.exception.ListenerExecuteException;
import com.java110.utils.util.Assert;
import com.java110.utils.util.DateUtil;
import com.java110.utils.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

/**
 * 费用计算 服务类
 * <p>
 * add by wuxw 2020-09-23
 *
 * @openSource https://gitee.com/wuxw7/MicroCommunity.git
 */

@Service
public class ComputeFeeSMOImpl implements IComputeFeeSMO {

    protected static final Logger logger = LoggerFactory.getLogger(ComputeFeeSMOImpl.class);

    @Autowired(required = false)
    private IFeeInnerServiceSMO feeInnerServiceSMOImpl;

    @Autowired(required = false)
    private IRoomInnerServiceSMO roomInnerServiceSMOImpl;

    @Autowired(required = false)
    private IOwnerCarInnerServiceSMO ownerCarInnerServiceSMOImpl;

    @Autowired(required = false)
    private IParkingSpaceInnerServiceSMO parkingSpaceInnerServiceSMOImpl;

    @Override
    public Date getFeeEndTime() {
        return null;
    }

    /**
     * 计算欠费金额
     *
     * @param tmpFeeDto
     */
    public void computeOweFee(FeeDto tmpFeeDto) {
        String billType = tmpFeeDto.getBillType();

        if (FeeConfigDto.BILL_TYPE_EVERY.equals(billType)) {
            computeFeePrice(tmpFeeDto);
            return;
        }
        BillDto billDto = new BillDto();
        billDto.setCommunityId(tmpFeeDto.getCommunityId());
        billDto.setConfigId(tmpFeeDto.getConfigId());
        billDto.setCurBill("T");
        List<BillDto> billDtos = feeInnerServiceSMOImpl.queryBills(billDto);
        if (billDtos == null || billDtos.size() < 1) {
            tmpFeeDto.setFeePrice(0.00);
            return;
        }
        BillOweFeeDto billOweFeeDto = new BillOweFeeDto();
        billOweFeeDto.setCommunityId(tmpFeeDto.getCommunityId());
        billOweFeeDto.setFeeId(tmpFeeDto.getFeeId());
        billOweFeeDto.setState(BillOweFeeDto.STATE_WILL_FEE);
        billOweFeeDto.setBillId(billDtos.get(0).getBillId());
        List<BillOweFeeDto> billOweFeeDtos = feeInnerServiceSMOImpl.queryBillOweFees(billOweFeeDto);
        if (billOweFeeDtos == null || billOweFeeDtos.size() < 1) {
            tmpFeeDto.setFeePrice(0.00);
            return;
        }
        try {
            tmpFeeDto.setDeadlineTime(DateUtil.getDateFromString(billOweFeeDtos.get(0).getDeadlineTime(), DateUtil.DATE_FORMATE_STRING_A));
        } catch (ParseException e) {
            logger.error("获取结束时间失败", e);
        }
        tmpFeeDto.setFeePrice(Double.parseDouble(billOweFeeDtos.get(0).getAmountOwed()));
    }

    private void computeFeePrice(FeeDto feeDto) {

        if (FeeDto.PAYER_OBJ_TYPE_ROOM.equals(feeDto.getPayerObjType())) { //房屋相关
            computeFeePriceByRoom(feeDto);
        } else if (FeeDto.PAYER_OBJ_TYPE_CAR.equals(feeDto.getPayerObjType())) {//车位相关
            computeFeePriceByParkingSpace(feeDto);
        }
    }

    private void computeFeePriceByParkingSpace(FeeDto feeDto) {
        Map<String, Object> targetEndDateAndOweMonth = getTargetEndDateAndOweMonth(feeDto);
        Date targetEndDate = (Date) targetEndDateAndOweMonth.get("targetEndDate");
        double oweMonth = (double) targetEndDateAndOweMonth.get("oweMonth");
        ParkingSpaceDto parkingSpaceDto = new ParkingSpaceDto();
        parkingSpaceDto.setCommunityId(feeDto.getCommunityId());
        parkingSpaceDto.setPsId(feeDto.getPayerObjId());
        List<ParkingSpaceDto> parkingSpaceDtos = parkingSpaceInnerServiceSMOImpl.queryParkingSpaces(parkingSpaceDto);

        if (parkingSpaceDtos == null || parkingSpaceDtos.size() < 1) { //数据有问题
            return;
        }

        String computingFormula = feeDto.getComputingFormula();
        double feePrice = getFeePrice(feeDto);

        feeDto.setFeePrice(feePrice);
        double month = dayCompare(feeDto.getEndTime(), DateUtil.getCurrentDate());
        BigDecimal price = new BigDecimal(feeDto.getFeePrice());
        price = price.multiply(new BigDecimal(oweMonth));
        feeDto.setFeePrice(price.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
        feeDto.setDeadlineTime(targetEndDate);

        //动态费用
        if ("4004".equals(computingFormula)) {
            feeDto.setAmountOwed(feeDto.getFeePrice() + "");
            feeDto.setDeadlineTime(DateUtil.getCurrentDate());
        }

    }

    /**
     * 根据房屋来算单价
     *
     * @param feeDto
     */
    private void computeFeePriceByRoom(FeeDto feeDto) {
        Map<String, Object> targetEndDateAndOweMonth = getTargetEndDateAndOweMonth(feeDto);
        Date targetEndDate = (Date) targetEndDateAndOweMonth.get("targetEndDate");
        double oweMonth = (double) targetEndDateAndOweMonth.get("oweMonth");
        RoomDto roomDto = new RoomDto();
        roomDto.setCommunityId(feeDto.getCommunityId());
        roomDto.setRoomId(feeDto.getPayerObjId());
        List<RoomDto> roomDtos = roomInnerServiceSMOImpl.queryRooms(roomDto);

        if (roomDtos == null || roomDtos.size() < 1) { //数据有问题
            return;
        }

        String computingFormula = feeDto.getComputingFormula();
        double feePrice = getFeePrice(feeDto);
        feeDto.setFeePrice(feePrice);
        //double month = dayCompare(feeDto.getEndTime(), DateUtil.getCurrentDate());
        BigDecimal price = new BigDecimal(feeDto.getFeePrice());
        price = price.multiply(new BigDecimal(oweMonth));
        feeDto.setFeePrice(price.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
        feeDto.setDeadlineTime(targetEndDate);

        //动态费用
        if ("4004".equals(computingFormula)) {
            feeDto.setAmountOwed(feeDto.getFeePrice() + "");
            feeDto.setDeadlineTime(DateUtil.getCurrentDate());
        }
    }


    /**
     * 刷新 收据明细
     *
     * @param feeDto
     * @param feeReceiptDetailPo
     */
    @Override
    public void freshFeeReceiptDetail(FeeDto feeDto, FeeReceiptDetailPo feeReceiptDetailPo) {
        if (FeeDto.PAYER_OBJ_TYPE_ROOM.equals(feeDto.getPayerObjType())) { //房屋相关
            String computingFormula = feeDto.getComputingFormula();
            RoomDto roomDto = new RoomDto();
            roomDto.setRoomId(feeDto.getPayerObjId());
            roomDto.setCommunityId(feeDto.getCommunityId());
            List<RoomDto> roomDtos = roomInnerServiceSMOImpl.queryRooms(roomDto);
            if (roomDtos == null || roomDtos.size() != 1) {
                return;
            }
            if ("1001".equals(computingFormula)) { //面积*单价+附加费
                feeReceiptDetailPo.setArea(roomDtos.get(0).getBuiltUpArea());
                feeReceiptDetailPo.setSquarePrice(feeDto.getSquarePrice() + "/" + feeDto.getAdditionalAmount());
            } else if ("2002".equals(computingFormula)) { // 固定费用
                feeReceiptDetailPo.setArea("");
                feeReceiptDetailPo.setSquarePrice(feeDto.getAdditionalAmount());
            } else if ("4004".equals(computingFormula)) {
            } else if ("5005".equals(computingFormula)) {
                if (StringUtil.isEmpty(feeDto.getCurDegrees())) {
                } else {
                    BigDecimal curDegree = new BigDecimal(Double.parseDouble(feeDto.getCurDegrees()));
                    BigDecimal preDegree = new BigDecimal(Double.parseDouble(feeDto.getPreDegrees()));
                    BigDecimal sub = curDegree.subtract(preDegree).setScale(2, BigDecimal.ROUND_HALF_EVEN);
                    feeReceiptDetailPo.setArea(sub.doubleValue() + "");
                    feeReceiptDetailPo.setSquarePrice(feeDto.getSquarePrice() + "/" + feeDto.getAdditionalAmount());
                }
            } else if ("6006".equals(computingFormula)) {
                String value = "";
                List<FeeAttrDto> feeAttrDtos = feeDto.getFeeAttrDtos();
                for (FeeAttrDto feeAttrDto : feeAttrDtos) {
                    if (feeAttrDto.getSpecCd().equals(FeeAttrDto.SPEC_CD_PROXY_CONSUMPTION)) {
                        value = feeAttrDto.getValue();
                    }
                }
                feeReceiptDetailPo.setArea(value);
                feeReceiptDetailPo.setSquarePrice(feeDto.getSquarePrice() + "/" + feeDto.getAdditionalAmount());
            } else {
            }
        } else if (FeeDto.PAYER_OBJ_TYPE_CAR.equals(feeDto.getPayerObjType())) {//车位相关
            String computingFormula = feeDto.getComputingFormula();
            OwnerCarDto ownerCarDto = new OwnerCarDto();
            ownerCarDto.setCommunityId(feeDto.getCommunityId());
            ownerCarDto.setCarId(feeDto.getPayerObjId());
            List<OwnerCarDto> ownerCarDtos = ownerCarInnerServiceSMOImpl.queryOwnerCars(ownerCarDto);
            Assert.listOnlyOne(ownerCarDtos, "未找到车辆信息");
            if ("1001".equals(computingFormula)) { //面积*单价+附加费
                ParkingSpaceDto parkingSpaceDto = new ParkingSpaceDto();
                parkingSpaceDto.setCommunityId(feeDto.getCommunityId());
                parkingSpaceDto.setPsId(ownerCarDtos.get(0).getPsId());
                List<ParkingSpaceDto> parkingSpaceDtos = parkingSpaceInnerServiceSMOImpl.queryParkingSpaces(parkingSpaceDto);
                if (parkingSpaceDtos == null || parkingSpaceDtos.size() < 1) { //数据有问题
                    return;
                }
                feeReceiptDetailPo.setArea(parkingSpaceDtos.get(0).getArea());
                feeReceiptDetailPo.setSquarePrice(feeDto.getSquarePrice() + "/" + feeDto.getAdditionalAmount());
            } else if ("2002".equals(computingFormula)) { // 固定费用
                feeReceiptDetailPo.setArea("");
                feeReceiptDetailPo.setSquarePrice(feeDto.getAdditionalAmount());
            } else if ("4004".equals(computingFormula)) {
            } else if ("5005".equals(computingFormula)) {
                if (StringUtil.isEmpty(feeDto.getCurDegrees())) {
                } else {
                    BigDecimal curDegree = new BigDecimal(Double.parseDouble(feeDto.getCurDegrees()));
                    BigDecimal preDegree = new BigDecimal(Double.parseDouble(feeDto.getPreDegrees()));
                    BigDecimal sub = curDegree.subtract(preDegree).setScale(2, BigDecimal.ROUND_HALF_EVEN);
                    feeReceiptDetailPo.setArea(sub.doubleValue() + "");
                    feeReceiptDetailPo.setSquarePrice(feeDto.getSquarePrice() + "/" + feeDto.getAdditionalAmount());
                }
            } else if ("6006".equals(computingFormula)) {
                String value = "";
                List<FeeAttrDto> feeAttrDtos = feeDto.getFeeAttrDtos();
                for (FeeAttrDto feeAttrDto : feeAttrDtos) {
                    if (feeAttrDto.getSpecCd().equals(FeeAttrDto.SPEC_CD_PROXY_CONSUMPTION)) {
                        value = feeAttrDto.getValue();
                    }
                }
                feeReceiptDetailPo.setArea(value);
                feeReceiptDetailPo.setSquarePrice(feeDto.getSquarePrice() + "/" + feeDto.getAdditionalAmount());
            } else {
            }
        }
    }

    /**
     * 查询费用对象名称
     *
     * @param feeDto
     * @return
     */
    @Override
    public String getFeeObjName(FeeDto feeDto) {
        String objName = "";
        if (FeeDto.PAYER_OBJ_TYPE_ROOM.equals(feeDto.getPayerObjType())) { //房屋相关
            RoomDto roomDto = new RoomDto();
            roomDto.setRoomId(feeDto.getPayerObjId());
            roomDto.setCommunityId(feeDto.getCommunityId());
            List<RoomDto> roomDtos = roomInnerServiceSMOImpl.queryRooms(roomDto);
            if (roomDtos == null || roomDtos.size() != 1) {
                return objName;
            }
            roomDto = roomDtos.get(0);
            objName = roomDto.getFloorNum() + "栋" + roomDto.getUnitNum() + "单元" + roomDto.getRoomNum() + "室";
        } else if (FeeDto.PAYER_OBJ_TYPE_CAR.equals(feeDto.getPayerObjType())) {//车位相关

            OwnerCarDto ownerCarDto = new OwnerCarDto();
            ownerCarDto.setCommunityId(feeDto.getCommunityId());
            ownerCarDto.setCarId(feeDto.getPayerObjId());
            List<OwnerCarDto> ownerCarDtos = ownerCarInnerServiceSMOImpl.queryOwnerCars(ownerCarDto);
            if (ownerCarDtos == null || ownerCarDtos.size() < 1) {
                return objName;
            }

            objName = ownerCarDtos.get(0).getCarNum();
            ParkingSpaceDto parkingSpaceDto = new ParkingSpaceDto();
            parkingSpaceDto.setCommunityId(feeDto.getCommunityId());
            parkingSpaceDto.setPsId(ownerCarDtos.get(0).getPsId());
            List<ParkingSpaceDto> parkingSpaceDtos = parkingSpaceInnerServiceSMOImpl.queryParkingSpaces(parkingSpaceDto);
            if (parkingSpaceDtos == null || parkingSpaceDtos.size() < 1) { //数据有问题
                return objName;
            }
            objName = objName + "(" + parkingSpaceDtos.get(0).getAreaNum() + "停车场" + parkingSpaceDtos.get(0).getNum() + "车位)";
        }
        return objName;
    }

    @Override
    public void freshFeeObjName(List<FeeDto> feeDtos) {

        List<String> roomIds = new ArrayList<>();
        List<String> carIds = new ArrayList<>();
        for (FeeDto feeDto : feeDtos) {
            if (FeeDto.PAYER_OBJ_TYPE_ROOM.equals(feeDto.getPayerObjType())) {
                roomIds.add(feeDto.getPayerObjId());
            } else if (FeeDto.PAYER_OBJ_TYPE_CAR.equals(feeDto.getPayerObjType())) {
                carIds.add(feeDto.getPayerObjId());
            }
        }

        // 用房屋信息刷 费用付费对象
        freshFeeObjNameByRoomId(feeDtos, roomIds);

        // 用车辆车位 刷 付费对象
        freshFeeObjNameByCarId(feeDtos, carIds);

    }

    /**
     * 刷费用
     *
     * @param feeDtos
     * @param carIds
     */
    private void freshFeeObjNameByCarId(List<FeeDto> feeDtos, List<String> carIds) {

        if (carIds.size() < 1) {
            return;
        }


        OwnerCarDto ownerCarDto = new OwnerCarDto();
        ownerCarDto.setCommunityId(feeDtos.get(0).getCommunityId());
        ownerCarDto.setCarIds(carIds.toArray(new String[carIds.size()]));
        List<OwnerCarDto> ownerCarDtos = ownerCarInnerServiceSMOImpl.queryOwnerCars(ownerCarDto);

        if (ownerCarDtos == null || ownerCarDtos.size() < 1) {
            return;
        }

        List<String> psIds = new ArrayList<>();

        for (OwnerCarDto tmpOwnerCarDto : ownerCarDtos) {
            if (StringUtil.isEmpty(tmpOwnerCarDto.getPsId()) || tmpOwnerCarDto.getPsId().startsWith("-")) {
                continue;
            }
            psIds.add(tmpOwnerCarDto.getPsId());
        }

        //没有车位情况下
        if (psIds.size() < 1) {
            for (OwnerCarDto tmpOwnerCarDto : ownerCarDtos) {
                for (FeeDto feeDto : feeDtos) {
                    if (!FeeDto.PAYER_OBJ_TYPE_CAR.equals(feeDto.getPayerObjType())) {
                        continue;
                    }

                    if (feeDto.getPayerObjId().equals(tmpOwnerCarDto.getCarId())) {
                        feeDto.setPayerObjName(tmpOwnerCarDto.getCarNum());
                    }
                }
            }
            return;
        }


        ParkingSpaceDto parkingSpaceDto = new ParkingSpaceDto();
        parkingSpaceDto.setCommunityId(feeDtos.get(0).getCommunityId());
        parkingSpaceDto.setPsIds(psIds.toArray(new String[psIds.size()]));
        List<ParkingSpaceDto> parkingSpaceDtos = parkingSpaceInnerServiceSMOImpl.queryParkingSpaces(parkingSpaceDto);
        for (OwnerCarDto tmpOwnerCarDto : ownerCarDtos) {
            for (ParkingSpaceDto tmpParkingSpaceDto : parkingSpaceDtos) {
                if (tmpParkingSpaceDto.getPsId().equals(tmpOwnerCarDto.getPsId())) {
                    tmpOwnerCarDto.setAreaNum(tmpParkingSpaceDto.getAreaNum());
                    tmpOwnerCarDto.setNum(tmpParkingSpaceDto.getNum());
                }
            }
        }
        for (OwnerCarDto tmpOwnerCarDto : ownerCarDtos) {
            for (FeeDto feeDto : feeDtos) {
                if (!FeeDto.PAYER_OBJ_TYPE_CAR.equals(feeDto.getPayerObjType())) {
                    continue;
                }

                if (feeDto.getPayerObjId().equals(tmpOwnerCarDto.getCarId())) {
                    feeDto.setPayerObjName(tmpOwnerCarDto.getCarNum() + "(" + tmpOwnerCarDto.getAreaNum() + "停车场" + tmpOwnerCarDto.getNum() + "车位)");
                }
            }
        }

    }

    /**
     * 用房屋信息刷付费方名称
     *
     * @param feeDtos
     * @param roomIds
     */
    private void freshFeeObjNameByRoomId(List<FeeDto> feeDtos, List<String> roomIds) {

        if (roomIds.size() < 1) {
            return;
        }

        RoomDto roomDto = new RoomDto();
        roomDto.setRoomIds(roomIds.toArray(new String[roomIds.size()]));
        roomDto.setCommunityId(feeDtos.get(0).getCommunityId());
        List<RoomDto> roomDtos = roomInnerServiceSMOImpl.queryRooms(roomDto);
        String objName = "";
        for (RoomDto tmpRoomDto : roomDtos) {
            for (FeeDto feeDto : feeDtos) {
                if (!FeeDto.PAYER_OBJ_TYPE_ROOM.equals(feeDto.getPayerObjType())) {
                    continue;
                }
                if (tmpRoomDto.getRoomId().equals(feeDto.getPayerObjId())) {
                    objName = tmpRoomDto.getFloorNum() + "栋" + tmpRoomDto.getUnitNum() + "单元" + tmpRoomDto.getRoomNum() + "室";
                    feeDto.setPayerObjName(objName);
                }
            }
        }
    }

    /**
     * 根据周期 计算费用状态
     *
     * @param feeDto
     * @param cycles
     * @return
     */
    public String getFeeStateByCycles(FeeDto feeDto, String cycles) {
        double cycle = Double.parseDouble(cycles);
        Date endTime = feeDto.getEndTime();
        Calendar endCalender = Calendar.getInstance();
        endCalender.setTime(endTime);
        endCalender.add(Calendar.MONTH, new Double(Math.floor(cycle)).intValue());
//        Calendar futureDate = Calendar.getInstance();
//        futureDate.setTime(endCalender.getTime());
//        futureDate.add(Calendar.MONTH, 1);
        int futureDay = endCalender.getActualMaximum(Calendar.DAY_OF_MONTH);
        int hours = new Double((cycle - Math.floor(cycle)) * futureDay * 24).intValue();
        endCalender.add(Calendar.HOUR, hours);
        if (FeeDto.FEE_FLAG_ONCE.equals(feeDto.getFeeFlag())) {
            return FeeDto.STATE_FINISH;
        } else {
            if ((endCalender.getTime()).after(feeDto.getConfigEndTime())) {
                return FeeDto.STATE_FINISH;
            }
        }
        return FeeDto.STATE_DOING;
    }

    public Date getFeeEndTimeByCycles(FeeDto feeDto, String cycles) {
        double cycle = Double.parseDouble(cycles);

        Date endTime = feeDto.getEndTime();
        Calendar endCalender = Calendar.getInstance();
        endCalender.setTime(endTime);
        endCalender.add(Calendar.MONTH, new Double(Math.floor(cycle)).intValue());
//        Calendar futureDate = Calendar.getInstance();
//        futureDate.setTime(endCalender.getTime());
//        futureDate.add(Calendar.MONTH, 1);
        int futureDay = endCalender.getActualMaximum(Calendar.DAY_OF_MONTH);
        int hours = new Double((cycle - Math.floor(cycle)) * futureDay * 24).intValue();
        endCalender.add(Calendar.HOUR, hours);
        if (FeeDto.FEE_FLAG_ONCE.equals(feeDto.getFeeFlag())) {
            if (!StringUtil.isEmpty(feeDto.getCurDegrees())) {
                endCalender.setTime(feeDto.getCurReadingTime());
            } else if (feeDto.getImportFeeEndTime() == null) {
                endCalender.setTime(feeDto.getConfigEndTime());
            } else {
                endCalender.setTime(feeDto.getImportFeeEndTime());
            }
        } else {
            if ((endCalender.getTime()).after(feeDto.getConfigEndTime())) {
                endCalender.setTime(feeDto.getConfigEndTime());
            }
        }

        return endCalender.getTime();
    }


    @Override
    public double getCycle() {
        return 0;
    }

    @Override
    public double getReportFeePrice(ReportFeeDto tmpReportFeeDto, ReportRoomDto reportRoomDto, ReportCarDto reportCarDto) {
        BigDecimal feePrice = new BigDecimal(0.0);
        if (FeeDto.PAYER_OBJ_TYPE_ROOM.equals(tmpReportFeeDto.getPayerObjType())) { //房屋相关
            String computingFormula = tmpReportFeeDto.getComputingFormula();

            if ("1001".equals(computingFormula)) { //面积*单价+附加费
                //feePrice = Double.parseDouble(feeDto.getSquarePrice()) * Double.parseDouble(roomDtos.get(0).getBuiltUpArea()) + Double.parseDouble(feeDto.getAdditionalAmount());
                BigDecimal squarePrice = new BigDecimal(Double.parseDouble(tmpReportFeeDto.getSquarePrice()));
                BigDecimal builtUpArea = new BigDecimal(Double.parseDouble(reportRoomDto.getBuiltUpArea()));
                BigDecimal additionalAmount = new BigDecimal(Double.parseDouble(tmpReportFeeDto.getAdditionalAmount()));
                feePrice = squarePrice.multiply(builtUpArea).add(additionalAmount).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            } else if ("2002".equals(computingFormula)) { // 固定费用
                //feePrice = Double.parseDouble(feeDto.getAdditionalAmount());
                BigDecimal additionalAmount = new BigDecimal(Double.parseDouble(tmpReportFeeDto.getAdditionalAmount()));
                feePrice = additionalAmount.setScale(2, BigDecimal.ROUND_HALF_EVEN);
            } else if ("4004".equals(computingFormula)) {
                feePrice = new BigDecimal(Double.parseDouble(tmpReportFeeDto.getAmount()));
            } else if ("5005".equals(computingFormula)) {
                if (StringUtil.isEmpty(tmpReportFeeDto.getCurDegrees())) {
                    //throw new IllegalArgumentException("抄表数据异常");
                } else {
                    BigDecimal curDegree = new BigDecimal(Double.parseDouble(tmpReportFeeDto.getCurDegrees()));
                    BigDecimal preDegree = new BigDecimal(Double.parseDouble(tmpReportFeeDto.getPreDegrees()));
                    BigDecimal squarePrice = new BigDecimal(Double.parseDouble(tmpReportFeeDto.getSquarePrice()));
                    BigDecimal additionalAmount = new BigDecimal(Double.parseDouble(tmpReportFeeDto.getAdditionalAmount()));
                    BigDecimal sub = curDegree.subtract(preDegree);
                    feePrice = sub.multiply(squarePrice)
                            .add(additionalAmount)
                            .setScale(2, BigDecimal.ROUND_HALF_EVEN);
                }
            } else if ("6006".equals(computingFormula)) {
                feePrice = new BigDecimal(Double.parseDouble(tmpReportFeeDto.getAmount()));
            } else {
                throw new IllegalArgumentException("暂不支持该类公式");
            }
        } else if (FeeDto.PAYER_OBJ_TYPE_CAR.equals(tmpReportFeeDto.getPayerObjType())) {//车位相关
            String computingFormula = tmpReportFeeDto.getComputingFormula();

            if ("1001".equals(computingFormula)) { //面积*单价+附加费
                BigDecimal squarePrice = new BigDecimal(Double.parseDouble(tmpReportFeeDto.getSquarePrice()));
                BigDecimal builtUpArea = new BigDecimal(Double.parseDouble("0"));
                BigDecimal additionalAmount = new BigDecimal(Double.parseDouble(tmpReportFeeDto.getAdditionalAmount()));
                feePrice = squarePrice.multiply(builtUpArea).add(additionalAmount).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            } else if ("2002".equals(computingFormula)) { // 固定费用
                //feePrice = Double.parseDouble(feeDto.getAdditionalAmount());
                BigDecimal additionalAmount = new BigDecimal(Double.parseDouble(tmpReportFeeDto.getAdditionalAmount()));
                feePrice = additionalAmount.setScale(2, BigDecimal.ROUND_HALF_EVEN);
            } else if ("4004".equals(computingFormula)) {
                feePrice = new BigDecimal(Double.parseDouble(tmpReportFeeDto.getAmount()));
            } else if ("5005".equals(computingFormula)) {
                if (StringUtil.isEmpty(tmpReportFeeDto.getCurDegrees())) {
                    throw new IllegalArgumentException("抄表数据异常");
                } else {
                    BigDecimal curDegree = new BigDecimal(Double.parseDouble(tmpReportFeeDto.getCurDegrees()));
                    BigDecimal preDegree = new BigDecimal(Double.parseDouble(tmpReportFeeDto.getPreDegrees()));
                    BigDecimal squarePrice = new BigDecimal(Double.parseDouble(tmpReportFeeDto.getSquarePrice()));
                    BigDecimal additionalAmount = new BigDecimal(Double.parseDouble(tmpReportFeeDto.getAdditionalAmount()));
                    BigDecimal sub = curDegree.subtract(preDegree);
                    feePrice = sub.multiply(squarePrice)
                            .add(additionalAmount)
                            .setScale(2, BigDecimal.ROUND_HALF_EVEN);
                }
            } else if ("6006".equals(computingFormula)) {
                feePrice = new BigDecimal(Double.parseDouble(tmpReportFeeDto.getAmount()));
            } else {
                throw new IllegalArgumentException("暂不支持该类公式");
            }
        }
        return feePrice.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
    }

    @Override
    public double getFeePrice(FeeDto feeDto) {
        BigDecimal feePrice = new BigDecimal(0.0);
        if (FeeDto.PAYER_OBJ_TYPE_ROOM.equals(feeDto.getPayerObjType())) { //房屋相关
            String computingFormula = feeDto.getComputingFormula();
            RoomDto roomDto = new RoomDto();
            roomDto.setRoomId(feeDto.getPayerObjId());
            roomDto.setCommunityId(feeDto.getCommunityId());
            List<RoomDto> roomDtos = roomInnerServiceSMOImpl.queryRooms(roomDto);
            if (roomDtos == null || roomDtos.size() != 1) {
                throw new ListenerExecuteException(ResponseConstant.RESULT_CODE_ERROR, "未查到房屋信息，查询多条数据");
            }
            roomDto = roomDtos.get(0);
            if ("1001".equals(computingFormula)) { //面积*单价+附加费
                //feePrice = Double.parseDouble(feeDto.getSquarePrice()) * Double.parseDouble(roomDtos.get(0).getBuiltUpArea()) + Double.parseDouble(feeDto.getAdditionalAmount());
                BigDecimal squarePrice = new BigDecimal(Double.parseDouble(feeDto.getSquarePrice()));
                BigDecimal builtUpArea = new BigDecimal(Double.parseDouble(roomDtos.get(0).getBuiltUpArea()));
                BigDecimal additionalAmount = new BigDecimal(Double.parseDouble(feeDto.getAdditionalAmount()));
                feePrice = squarePrice.multiply(builtUpArea).add(additionalAmount).setScale(3, BigDecimal.ROUND_HALF_EVEN);
            } else if ("2002".equals(computingFormula)) { // 固定费用
                //feePrice = Double.parseDouble(feeDto.getAdditionalAmount());
                BigDecimal additionalAmount = new BigDecimal(Double.parseDouble(feeDto.getAdditionalAmount()));
                feePrice = additionalAmount.setScale(3, BigDecimal.ROUND_HALF_EVEN);
            } else if ("4004".equals(computingFormula)) {  //动态费用
                feePrice = new BigDecimal(Double.parseDouble(feeDto.getAmount()));
            } else if ("5005".equals(computingFormula)) {  //(本期度数-上期度数)*单价+附加费
                if (StringUtil.isEmpty(feeDto.getCurDegrees())) {
                    //throw new IllegalArgumentException("抄表数据异常");
                } else {
                    BigDecimal curDegree = new BigDecimal(Double.parseDouble(feeDto.getCurDegrees()));
                    BigDecimal preDegree = new BigDecimal(Double.parseDouble(feeDto.getPreDegrees()));
                    BigDecimal squarePrice = new BigDecimal(Double.parseDouble(feeDto.getSquarePrice()));
                    BigDecimal additionalAmount = new BigDecimal(Double.parseDouble(feeDto.getAdditionalAmount()));
                    BigDecimal sub = curDegree.subtract(preDegree);
                    feePrice = sub.multiply(squarePrice)
                            .add(additionalAmount)
                            .setScale(2, BigDecimal.ROUND_HALF_EVEN);
                }
            } else if ("6006".equals(computingFormula)) {
                feePrice = new BigDecimal(Double.parseDouble(feeDto.getAmount()));
            } else {
                throw new IllegalArgumentException("暂不支持该类公式");
            }
        } else if (FeeDto.PAYER_OBJ_TYPE_CAR.equals(feeDto.getPayerObjType())) {//车位相关
            String computingFormula = feeDto.getComputingFormula();

            OwnerCarDto ownerCarDto = new OwnerCarDto();
            ownerCarDto.setCommunityId(feeDto.getCommunityId());
            ownerCarDto.setCarId(feeDto.getPayerObjId());
            List<OwnerCarDto> ownerCarDtos = ownerCarInnerServiceSMOImpl.queryOwnerCars(ownerCarDto);
            Assert.listOnlyOne(ownerCarDtos, "未找到车辆信息");
            if ("1001".equals(computingFormula)) { //面积*单价+附加费
                ParkingSpaceDto parkingSpaceDto = new ParkingSpaceDto();
                parkingSpaceDto.setCommunityId(feeDto.getCommunityId());
                parkingSpaceDto.setPsId(ownerCarDtos.get(0).getPsId());
                List<ParkingSpaceDto> parkingSpaceDtos = parkingSpaceInnerServiceSMOImpl.queryParkingSpaces(parkingSpaceDto);
                if (parkingSpaceDtos == null || parkingSpaceDtos.size() < 1) { //数据有问题
                    throw new ListenerExecuteException(ResponseConstant.RESULT_CODE_ERROR, "未查到停车位信息，查询多条数据");
                }
                BigDecimal squarePrice = new BigDecimal(Double.parseDouble(feeDto.getSquarePrice()));
                BigDecimal builtUpArea = new BigDecimal(Double.parseDouble(parkingSpaceDtos.get(0).getArea()));
                BigDecimal additionalAmount = new BigDecimal(Double.parseDouble(feeDto.getAdditionalAmount()));
                feePrice = squarePrice.multiply(builtUpArea).add(additionalAmount).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            } else if ("2002".equals(computingFormula)) { // 固定费用
                //feePrice = Double.parseDouble(feeDto.getAdditionalAmount());
                BigDecimal additionalAmount = new BigDecimal(Double.parseDouble(feeDto.getAdditionalAmount()));
                feePrice = additionalAmount.setScale(2, BigDecimal.ROUND_HALF_EVEN);
            } else if ("4004".equals(computingFormula)) {
                feePrice = new BigDecimal(Double.parseDouble(feeDto.getAmount()));
            } else if ("5005".equals(computingFormula)) {
                if (StringUtil.isEmpty(feeDto.getCurDegrees())) {
                    throw new IllegalArgumentException("抄表数据异常");
                } else {
                    BigDecimal curDegree = new BigDecimal(Double.parseDouble(feeDto.getCurDegrees()));
                    BigDecimal preDegree = new BigDecimal(Double.parseDouble(feeDto.getPreDegrees()));
                    BigDecimal squarePrice = new BigDecimal(Double.parseDouble(feeDto.getSquarePrice()));
                    BigDecimal additionalAmount = new BigDecimal(Double.parseDouble(feeDto.getAdditionalAmount()));
                    BigDecimal sub = curDegree.subtract(preDegree);
                    feePrice = sub.multiply(squarePrice)
                            .add(additionalAmount)
                            .setScale(2, BigDecimal.ROUND_HALF_EVEN);
                }
            } else if ("6006".equals(computingFormula)) {
                feePrice = new BigDecimal(Double.parseDouble(feeDto.getAmount()));
            } else {
                throw new IllegalArgumentException("暂不支持该类公式");
            }
        }
        return feePrice.setScale(3, BigDecimal.ROUND_HALF_EVEN).doubleValue();
    }

    public Map getTargetEndDateAndOweMonth(FeeDto feeDto, OwnerCarDto ownerCarDto) {
        Date targetEndDate = null;
        double oweMonth = 0.0;

        Map<String, Object> targetEndDateAndOweMonth = new HashMap<>();

        if (FeeDto.STATE_FINISH.equals(feeDto.getState())) {
            targetEndDate = feeDto.getEndTime();
            targetEndDateAndOweMonth.put("oweMonth", oweMonth);
            targetEndDateAndOweMonth.put("targetEndDate", targetEndDate);
            return targetEndDateAndOweMonth;
        }
        if (FeeDto.FEE_FLAG_ONCE.equals(feeDto.getFeeFlag())) {
            if (!StringUtil.isEmpty(feeDto.getCurDegrees())) {
                targetEndDate = feeDto.getCurReadingTime();
            } else if (feeDto.getImportFeeEndTime() == null) {
                targetEndDate = feeDto.getConfigEndTime();
            } else {
                targetEndDate = feeDto.getImportFeeEndTime();
            }
            //判断当前费用是不是导入费用
            oweMonth = 1.0;

        } else {
            //当前时间
            Date billEndTime = DateUtil.getCurrentDate();
            //开始时间
            Date startDate = feeDto.getStartTime();
            //到期时间
            Date endDate = feeDto.getEndTime();
            if (FeeDto.PAYER_OBJ_TYPE_CAR.equals(feeDto.getPayerObjType())) {
                if (ownerCarDto == null) {
                    targetEndDateAndOweMonth.put("oweMonth", 0);
                    targetEndDateAndOweMonth.put("targetEndDate", "");
                    return targetEndDateAndOweMonth;
                }
                targetEndDate = ownerCarDto.getEndTime();
                //说明没有欠费
                if (endDate.getTime() >= targetEndDate.getTime()) {
                    // 目标到期时间 - 到期时间 = 欠费月份
                    oweMonth = 0;
                    targetEndDateAndOweMonth.put("oweMonth", oweMonth);
                    targetEndDateAndOweMonth.put("targetEndDate", targetEndDate);
                    return targetEndDateAndOweMonth;
                }
            }

            //缴费周期
            long paymentCycle = Long.parseLong(feeDto.getPaymentCycle());
            // 当前时间 - 开始时间  = 月份
            double mulMonth = 0.0;
            mulMonth = dayCompare(startDate, billEndTime);

            // 月份/ 周期 = 轮数（向上取整）
            double round = 0.0;
            if ("1200".equals(feeDto.getPaymentCd())) { // 预付费
                round = Math.floor(mulMonth / paymentCycle) + 1;
            } else { //后付费
                round = Math.floor(mulMonth / paymentCycle);
            }
            // 轮数 * 周期 * 30 + 开始时间 = 目标 到期时间
            targetEndDate = getTargetEndTime(round * paymentCycle, startDate);
            //费用 快结束了
            if (feeDto.getConfigEndTime().getTime() < targetEndDate.getTime()) {
                targetEndDate = feeDto.getConfigEndTime();
            }
            //说明没有欠费
            if (endDate.getTime() < targetEndDate.getTime()) {
                // 目标到期时间 - 到期时间 = 欠费月份
                oweMonth = dayCompare(endDate, targetEndDate);
            }

            if (feeDto.getEndTime().getTime() > targetEndDate.getTime()) {
                targetEndDate = feeDto.getEndTime();
            }
        }

        targetEndDateAndOweMonth.put("oweMonth", oweMonth);
        targetEndDateAndOweMonth.put("targetEndDate", targetEndDate);
        return targetEndDateAndOweMonth;
    }

    public Map getTargetEndDateAndOweMonth(FeeDto feeDto) {
        if (FeeDto.PAYER_OBJ_TYPE_CAR.equals(feeDto.getPayerObjType())) {
            OwnerCarDto ownerCarDto = new OwnerCarDto();
            ownerCarDto.setCommunityId(feeDto.getCommunityId());
            ownerCarDto.setCarId(feeDto.getPayerObjId());
            List<OwnerCarDto> ownerCarDtos = ownerCarInnerServiceSMOImpl.queryOwnerCars(ownerCarDto);
            return getTargetEndDateAndOweMonth(feeDto, ownerCarDtos == null || ownerCarDtos.size() < 1 ? null : ownerCarDtos.get(0));
        }
        return getTargetEndDateAndOweMonth(feeDto, null);
    }

    @Override
    public double dayCompare(Date fromDate, Date toDate) {
        double resMonth = 0.0;
        Calendar from = Calendar.getInstance();
        from.setTime(fromDate);
        Calendar to = Calendar.getInstance();
        to.setTime(toDate);
        int result = to.get(Calendar.MONTH) - from.get(Calendar.MONTH);
        int month = (to.get(Calendar.YEAR) - from.get(Calendar.YEAR)) * 12;

        result = result + month;
        Calendar newFrom = Calendar.getInstance();
        newFrom.setTime(fromDate);
        newFrom.add(Calendar.MONTH, result);

        long t1 = newFrom.getTimeInMillis();
        long t2 = to.getTimeInMillis();
        double days = (t2 - t1) * 1.00 / (24 * 60 * 60 * 1000);
        BigDecimal tmpDays = new BigDecimal(days);
        BigDecimal monthDay = null;
        Calendar newFromMaxDay = Calendar.getInstance();
        newFromMaxDay.set(newFrom.get(Calendar.YEAR), newFrom.get(Calendar.MONTH), 1, 0, 0, 0);
        newFromMaxDay.add(Calendar.MONTH, 1);
        //在当前月中
        if (toDate.getTime() < newFromMaxDay.getTime().getTime()) {
            monthDay = new BigDecimal(newFromMaxDay.getActualMaximum(Calendar.DAY_OF_MONTH));
            return tmpDays.divide(monthDay, 2, BigDecimal.ROUND_HALF_UP).add(new BigDecimal(result)).doubleValue();
        }
        // 上月天数
        days = (newFromMaxDay.getTimeInMillis() - t1) * 1.00 / (24 * 60 * 60 * 1000);
        tmpDays = new BigDecimal(days);
        monthDay = new BigDecimal(newFrom.getActualMaximum(Calendar.DAY_OF_MONTH));
        BigDecimal preRresMonth = tmpDays.divide(monthDay, 2, BigDecimal.ROUND_HALF_UP).add(new BigDecimal(result));

        //下月天数
        days = (t2 - newFromMaxDay.getTimeInMillis()) * 1.00 / (24 * 60 * 60 * 1000);
        tmpDays = new BigDecimal(days);
        monthDay = new BigDecimal(newFromMaxDay.getActualMaximum(Calendar.DAY_OF_MONTH));
        resMonth = tmpDays.divide(monthDay, 2, BigDecimal.ROUND_HALF_UP).add(new BigDecimal(result)).add(preRresMonth).doubleValue();
        return resMonth;
    }

    @Override
    public Date getTargetEndTime(double month, Date startDate) {
        Calendar endDate = Calendar.getInstance();
        endDate.setTime(startDate);

        Double intMonth = Math.floor(month);
        endDate.add(Calendar.MONTH, intMonth.intValue());
        double doubleMonth = month - intMonth;
        if (doubleMonth <= 0) {
            return endDate.getTime();
        }
//        Calendar futureDate = Calendar.getInstance();
//        futureDate.setTime(endDate.getTime());
//        futureDate.add(Calendar.MONTH, 1);
        int futureDay = endDate.getActualMaximum(Calendar.DAY_OF_MONTH);
        Double hour = doubleMonth * futureDay * 24;
        endDate.add(Calendar.HOUR_OF_DAY, hour.intValue());
        return endDate.getTime();
    }

}
