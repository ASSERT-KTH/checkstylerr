package com.java110.core.smo.impl;

import com.java110.dto.RoomDto;
import com.java110.dto.fee.FeeDto;
import com.java110.dto.owner.OwnerCarDto;
import com.java110.dto.parking.ParkingSpaceDto;
import com.java110.intf.community.IParkingSpaceInnerServiceSMO;
import com.java110.intf.community.IRoomInnerServiceSMO;
import com.java110.intf.fee.IFeeInnerServiceSMO;
import com.java110.intf.user.IOwnerCarInnerServiceSMO;
import com.java110.core.smo.IComputeFeeSMO;
import com.java110.utils.constant.ResponseConstant;
import com.java110.utils.exception.ListenerExecuteException;
import com.java110.utils.util.Assert;
import com.java110.utils.util.DateUtil;
import com.java110.utils.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    @Autowired
    private IFeeInnerServiceSMO feeInnerServiceSMOImpl;

    @Autowired
    private IRoomInnerServiceSMO roomInnerServiceSMOImpl;

    @Autowired
    private IOwnerCarInnerServiceSMO ownerCarInnerServiceSMOImpl;

    @Autowired
    private IParkingSpaceInnerServiceSMO parkingSpaceInnerServiceSMOImpl;

    @Override
    public Date getFeeEndTime() {
        return null;
    }

    /**
     * 查询费用对象名称
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
        endCalender.add(Calendar.MONTH, Integer.parseInt(Math.floor(cycle) + ""));
        int hours = new Double((cycle - Math.floor(cycle)) * DateUtil.getCurrentMonthDay() * 24).intValue();
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
        endCalender.add(Calendar.MONTH, Integer.parseInt(Math.floor(cycle) + ""));
        int hours = new Double((cycle - Math.floor(cycle)) * DateUtil.getCurrentMonthDay() * 24).intValue();
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
            } else {
                throw new IllegalArgumentException("暂不支持该类公式");
            }
        }
        return feePrice.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
    }


    public Map getTargetEndDateAndOweMonth(FeeDto feeDto) {
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
                OwnerCarDto ownerCarDto = new OwnerCarDto();
                ownerCarDto.setCommunityId(feeDto.getCommunityId());
                ownerCarDto.setCarId(feeDto.getPayerObjId());
                List<OwnerCarDto> ownerCarDtos = ownerCarInnerServiceSMOImpl.queryOwnerCars(ownerCarDto);

                if (ownerCarDtos == null || ownerCarDtos.size() != 1) {
                    targetEndDateAndOweMonth.put("oweMonth", 0);
                    targetEndDateAndOweMonth.put("targetEndDate", "");
                    return targetEndDateAndOweMonth;
                }

                targetEndDate = ownerCarDtos.get(0).getEndTime();
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

    @Override
    public double dayCompare(Date fromDate, Date toDate) {
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
        long days = (t2 - t1) / (24 * 60 * 60 * 1000);

        BigDecimal tmpDays = new BigDecimal(days);
        BigDecimal monthDay = new BigDecimal(30);

        return tmpDays.divide(monthDay, 2, RoundingMode.HALF_UP).doubleValue() + result;
    }

    @Override
    public Date getTargetEndTime(double month, Date startDate) {
        Calendar endDate = Calendar.getInstance();
        endDate.setTime(startDate);
        endDate.add(Calendar.MONTH, (int) month);
        return endDate.getTime();
    }
}
