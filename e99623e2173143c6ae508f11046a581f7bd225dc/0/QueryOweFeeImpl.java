package com.java110.fee.bmo.impl;

import com.alibaba.fastjson.JSONArray;
import com.java110.dto.RoomDto;
import com.java110.dto.fee.BillDto;
import com.java110.dto.fee.BillOweFeeDto;
import com.java110.dto.fee.FeeConfigDto;
import com.java110.dto.fee.FeeDto;
import com.java110.dto.owner.OwnerCarDto;
import com.java110.dto.owner.OwnerDto;
import com.java110.dto.parking.ParkingSpaceDto;
import com.java110.fee.bmo.IQueryOweFee;
import com.java110.intf.community.IParkingSpaceInnerServiceSMO;
import com.java110.intf.community.IRoomInnerServiceSMO;
import com.java110.intf.fee.IFeeConfigInnerServiceSMO;
import com.java110.intf.fee.IFeeInnerServiceSMO;
import com.java110.intf.user.IOwnerCarInnerServiceSMO;
import com.java110.intf.user.IOwnerInnerServiceSMO;
import com.java110.utils.util.DateUtil;
import com.java110.utils.util.StringUtil;
import com.java110.vo.ResultVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.*;

@Service
public class QueryOweFeeImpl implements IQueryOweFee {


    private final static Logger logger = LoggerFactory.getLogger(QueryOweFeeImpl.class);


    @Autowired
    private IFeeInnerServiceSMO feeInnerServiceSMOImpl;

    @Autowired
    private IOwnerCarInnerServiceSMO ownerCarInnerServiceSMOImpl;

    @Autowired
    private IFeeConfigInnerServiceSMO feeConfigInnerServiceSMOImpl;

    @Autowired
    private IParkingSpaceInnerServiceSMO parkingSpaceInnerServiceSMOImpl;

    @Autowired
    private IRoomInnerServiceSMO roomInnerServiceSMOImpl;

    @Autowired
    private IOwnerInnerServiceSMO ownerInnerServiceSMOImpl;


    @Override
    public ResponseEntity<String> query(FeeDto feeDto) {

        //查询费用信息arrearsEndTime
        feeDto.setArrearsEndTime(DateUtil.getCurrentDate());
        feeDto.setState(FeeDto.STATE_DOING);
        List<FeeDto> feeDtos = feeInnerServiceSMOImpl.queryFees(feeDto);

        if (feeDtos == null || feeDtos.size() < 1) {
            feeDtos = new ArrayList<>();
            return ResultVo.createResponseEntity(feeDtos);
        }
        List<FeeDto> tmpFeeDtos = new ArrayList<>();
        for (FeeDto tmpFeeDto : feeDtos) {
            computeOweFee(tmpFeeDto);//计算欠费金额

            //如果金额为0 就排除
            if (tmpFeeDto.getFeePrice() > 0) {
                tmpFeeDtos.add(tmpFeeDto);
            }
        }


        return ResultVo.createResponseEntity(tmpFeeDtos);
    }

    @Override
    public ResponseEntity<String> queryAllOwneFee(FeeDto feeDto) {
        ResponseEntity<String> responseEntity = null;

        if (!freshFeeDtoParam(feeDto)) {
            return ResultVo.createResponseEntity(1, 0, new JSONArray());
        }

        if (FeeConfigDto.BILL_TYPE_EVERY.equals(feeDto.getBillType())) {
            responseEntity = computeEveryOweFee(feeDto);
        } else {
            responseEntity = computeBillOweFee(feeDto);
        }
        return responseEntity;
    }

    private boolean freshFeeDtoParam(FeeDto feeDto) {

        if (StringUtil.isEmpty(feeDto.getPayerObjId())) {
            return true;
        }

        if (!feeDto.getPayerObjId().contains("-")) {
            return false;
        }
        if (FeeDto.PAYER_OBJ_TYPE_ROOM.equals(feeDto.getPayerObjType())) {
            String[] nums = feeDto.getPayerObjId().split("-");
            if (nums.length != 3) {
                return false;
            }
            RoomDto roomDto = new RoomDto();
            roomDto.setFloorNum(nums[0]);
            roomDto.setUnitNum(nums[1]);
            roomDto.setRoomNum(nums[2]);
            roomDto.setCommunityId(feeDto.getCommunityId());
            List<RoomDto> roomDtos = roomInnerServiceSMOImpl.queryRooms(roomDto);

            if (roomDtos == null || roomDtos.size() < 1) {
                return false;
            }
            feeDto.setPayerObjId(roomDtos.get(0).getRoomId());

        } else {
            String[] nums = feeDto.getPayerObjId().split("-");
            if (nums.length != 2) {
                return false;
            }
            ParkingSpaceDto parkingSpaceDto = new ParkingSpaceDto();
            parkingSpaceDto.setAreaNum(nums[0]);
            parkingSpaceDto.setNum(nums[1]);
            parkingSpaceDto.setCommunityId(feeDto.getCommunityId());
            List<ParkingSpaceDto> parkingSpaceDtos = parkingSpaceInnerServiceSMOImpl.queryParkingSpaces(parkingSpaceDto);

            if (parkingSpaceDtos == null || parkingSpaceDtos.size() < 1) {
                return false;
            }
            feeDto.setPayerObjId(parkingSpaceDtos.get(0).getPsId());
        }

        return true;
    }

    /**
     * 账单费用
     *
     * @param feeDto
     * @return
     */
    private ResponseEntity<String> computeBillOweFee(FeeDto feeDto) {
        int count = feeInnerServiceSMOImpl.computeBillOweFeeCount(feeDto);
        List<FeeDto> feeDtos = null;
        if (count > 0) {
            feeDtos = feeInnerServiceSMOImpl.computeBillOweFee(feeDto);
        } else {
            feeDtos = new ArrayList<>();
        }
        return ResultVo.createResponseEntity((int) Math.ceil((double) count / (double) feeDto.getRow()), count, feeDtos);
    }

    /**
     * 实时费用
     *
     * @param feeDto
     * @return
     */
    private ResponseEntity<String> computeEveryOweFee(FeeDto feeDto) {

        int count = feeInnerServiceSMOImpl.computeEveryOweFeeCount(feeDto);
        List<FeeDto> feeDtos = null;
        if (count > 0) {
            feeDtos = feeInnerServiceSMOImpl.computeEveryOweFee(feeDto);
            computeFeePrices(feeDtos);

        } else {
            feeDtos = new ArrayList<>();
        }
        return ResultVo.createResponseEntity((int) Math.ceil((double) count / (double) feeDto.getRow()), count, feeDtos);
    }

    private void computeFeePrices(List<FeeDto> feeDtos) {

        List<FeeDto> roomFees = new ArrayList<>();
        List<FeeDto> psFees = new ArrayList<>();
        List<String> roomIds = new ArrayList<>();
        List<String> psIds = new ArrayList<>();

        for (FeeDto fee : feeDtos) {
            // 轮数 * 周期 * 30 + 开始时间 = 目标 到期时间
            if ("3333".equals(fee.getPayerObjType())) { //房屋相关
                roomFees.add(fee);
                roomIds.add(fee.getPayerObjId());
            } else if ("6666".equals(fee.getPayerObjType())) {//车位相关
                psFees.add(fee);
                psIds.add(fee.getPayerObjId());
            }
        }

        if (roomFees.size() > 0) {
            computeRoomFee(roomFees, roomIds);
        }

        if (psFees.size() > 0) {
            computePsFee(psFees, psIds);
        }
    }

    /**
     * 计算停车费
     *
     * @param psFees
     */
    private void computePsFee(List<FeeDto> psFees, List<String> psIds) {
        ParkingSpaceDto parkingSpaceDto = new ParkingSpaceDto();
        parkingSpaceDto.setCommunityId(psFees.get(0).getCommunityId());
        parkingSpaceDto.setPsIds(psIds.toArray(new String[psIds.size()]));
        List<ParkingSpaceDto> parkingSpaceDtos = parkingSpaceInnerServiceSMOImpl.queryParkingSpaces(parkingSpaceDto);

        if (parkingSpaceDtos == null || parkingSpaceDtos.size() < 1) { //数据有问题
            return;
        }
        for (ParkingSpaceDto tmpParkingSpaceDto : parkingSpaceDtos) {
            for (FeeDto feeDto : psFees) {
                dealFeePs(tmpParkingSpaceDto, feeDto);
            }
        }

        OwnerDto ownerDto = new OwnerDto();
        ownerDto.setOwnerIds(psIds.toArray(new String[psIds.size()]));
        ownerDto.setCommunityId(psFees.get(0).getCommunityId());
        List<OwnerDto> ownerDtos = ownerInnerServiceSMOImpl.queryOwnersByParkingSpace(ownerDto);

        for (OwnerDto tmpOwnerDto : ownerDtos) {
            for (FeeDto feeDto : psFees) {
                dealFeeOwner(tmpOwnerDto, feeDto);
            }
        }
    }

    private void dealFeePs(ParkingSpaceDto tmpParkingSpaceDto, FeeDto feeDto) {
        // 轮数 * 周期 * 30 + 开始时间 = 目标 到期时间
        Map<String, Object> targetEndDateAndOweMonth = getTargetEndDateAndOweMonth(feeDto);
        Date targetEndDate = (Date) targetEndDateAndOweMonth.get("targetEndDate");
        double oweMonth = (double) targetEndDateAndOweMonth.get("oweMonth");
        if (!tmpParkingSpaceDto.getPsId().equals(feeDto.getPayerObjId())) {
            return;
        }
        feeDto.setRoomName(tmpParkingSpaceDto.getAreaNum() + "停车场" + tmpParkingSpaceDto.getNum() + "车位");

        String computingFormula = feeDto.getComputingFormula();
        double feePrice = 0.00;
        if ("1001".equals(computingFormula)) { //面积*单价+附加费
            BigDecimal squarePrice = new BigDecimal(Double.parseDouble(feeDto.getSquarePrice()));
            BigDecimal builtUpArea = new BigDecimal(Double.parseDouble(tmpParkingSpaceDto.getArea()));
            BigDecimal additionalAmount = new BigDecimal(Double.parseDouble(feeDto.getAdditionalAmount()));
            feePrice = squarePrice.multiply(builtUpArea).add(additionalAmount).setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
        } else if ("2002".equals(computingFormula)) { // 固定费用

            BigDecimal additionalAmount = new BigDecimal(Double.parseDouble(feeDto.getAdditionalAmount()));
            feePrice = additionalAmount.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
        } else if ("4004".equals(computingFormula)) {
            feePrice = Double.parseDouble(feeDto.getAmount());
        } else if ("5005".equals(computingFormula)) {
            if (StringUtil.isEmpty(feeDto.getCurDegrees())) {
                feePrice = -1.00;
            } else {
                BigDecimal curDegree = new BigDecimal(Double.parseDouble(feeDto.getCurDegrees()));
                BigDecimal preDegree = new BigDecimal(Double.parseDouble(feeDto.getPreDegrees()));
                BigDecimal squarePrice = new BigDecimal(Double.parseDouble(feeDto.getSquarePrice()));
                BigDecimal additionalAmount = new BigDecimal(Double.parseDouble(feeDto.getAdditionalAmount()));
                BigDecimal sub = curDegree.subtract(preDegree);
                feePrice = sub.multiply(squarePrice)
                        .add(additionalAmount)
                        .setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
            }
        } else {
            feePrice = 0.00;
        }

        feeDto.setFeePrice(feePrice);
        // double month = dayCompare(feeDto.getEndTime(), DateUtil.getCurrentDate());
        BigDecimal price = new BigDecimal(feeDto.getFeePrice());
        price = price.multiply(new BigDecimal(oweMonth));
        feeDto.setAmountOwed(price.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue() + "");
        feeDto.setDeadlineTime(targetEndDate);
        //动态费用
        if ("4004".equals(computingFormula)) {
            feeDto.setAmountOwed(feeDto.getFeePrice() + "");
            feeDto.setDeadlineTime(DateUtil.getCurrentDate());
        }
    }

    /**
     * 计算房屋费
     *
     * @param roomFees
     */
    private void computeRoomFee(List<FeeDto> roomFees, List<String> roomIds) {
        RoomDto roomDto = new RoomDto();
        roomDto.setCommunityId(roomFees.get(0).getCommunityId());
        roomDto.setRoomIds(roomIds.toArray(new String[roomIds.size()]));
        List<RoomDto> roomDtos = roomInnerServiceSMOImpl.queryRooms(roomDto);

        if (roomDtos == null || roomDtos.size() < 1) { //数据有问题
            return;
        }


        for (RoomDto tmpRoomDto : roomDtos) {
            for (FeeDto feeDto : roomFees) {
                dealFeeRoom(tmpRoomDto, feeDto);
            }
        }

        OwnerDto ownerDto = new OwnerDto();
        ownerDto.setRoomIds(roomIds.toArray(new String[roomIds.size()]));
        ownerDto.setCommunityId(roomFees.get(0).getCommunityId());
        List<OwnerDto> ownerDtos = ownerInnerServiceSMOImpl.queryOwners(ownerDto);

        for (OwnerDto tmpOwnerDto : ownerDtos) {
            for (FeeDto feeDto : roomFees) {
                dealFeeOwner(tmpOwnerDto, feeDto);
            }
        }

    }

    private void dealFeeOwner(OwnerDto tmpOwnerDto, FeeDto feeDto) {

        if (!tmpOwnerDto.getRoomId().equals(feeDto.getPayerObjId())) {
            return;
        }

        feeDto.setOwnerName(tmpOwnerDto.getName());
        feeDto.setOwnerTel(tmpOwnerDto.getLink());
    }

    private void dealFeeRoom(RoomDto tmpRoomDto, FeeDto feeDto) {
        Map<String, Object> targetEndDateAndOweMonth = getTargetEndDateAndOweMonth(feeDto);
        Date targetEndDate = (Date) targetEndDateAndOweMonth.get("targetEndDate");
        double oweMonth = (double) targetEndDateAndOweMonth.get("oweMonth");
        if (!tmpRoomDto.getRoomId().equals(feeDto.getPayerObjId())) {
            return;
        }
        feeDto.setRoomName(tmpRoomDto.getFloorNum() + "栋" + tmpRoomDto.getUnitNum() + "单元" + tmpRoomDto.getRoomNum() + "室");

        String computingFormula = feeDto.getComputingFormula();
        double feePrice = 0.00;
        if ("1001".equals(computingFormula)) { //面积*单价+附加费
            BigDecimal squarePrice = new BigDecimal(Double.parseDouble(feeDto.getSquarePrice()));
            BigDecimal builtUpArea = new BigDecimal(Double.parseDouble(tmpRoomDto.getBuiltUpArea()));
            BigDecimal additionalAmount = new BigDecimal(Double.parseDouble(feeDto.getAdditionalAmount()));
            feePrice = squarePrice.multiply(builtUpArea).add(additionalAmount).setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
        } else if ("2002".equals(computingFormula)) { // 固定费用
            BigDecimal additionalAmount = new BigDecimal(Double.parseDouble(feeDto.getAdditionalAmount()));
            feePrice = additionalAmount.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
        } else if ("4004".equals(computingFormula)) {
            feePrice = Double.parseDouble(feeDto.getAmount());
        } else if ("5005".equals(computingFormula)) {

            if (StringUtil.isEmpty(feeDto.getCurDegrees())) {
                feePrice = -1.00;
            } else {
                BigDecimal curDegree = new BigDecimal(Double.parseDouble(feeDto.getCurDegrees()));
                BigDecimal preDegree = new BigDecimal(Double.parseDouble(feeDto.getPreDegrees()));
                BigDecimal squarePrice = new BigDecimal(Double.parseDouble(feeDto.getSquarePrice()));
                BigDecimal additionalAmount = new BigDecimal(Double.parseDouble(feeDto.getAdditionalAmount()));
                BigDecimal sub = curDegree.subtract(preDegree);
                feePrice = sub.multiply(squarePrice)
                        .add(additionalAmount)
                        .setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
            }
        } else {
            feePrice = 0.00;
        }
        feeDto.setFeePrice(feePrice);

        //double month = dayCompare(feeDto.getEndTime(), DateUtil.getCurrentDate());
        BigDecimal price = new BigDecimal(feeDto.getFeePrice());
        price = price.multiply(new BigDecimal(oweMonth));
        feeDto.setAmountOwed(price.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue() + "");
        feeDto.setDeadlineTime(targetEndDate);

        //动态费用
        if ("4004".equals(computingFormula)) {
            feeDto.setAmountOwed(feeDto.getFeePrice() + "");
            feeDto.setDeadlineTime(DateUtil.getCurrentDate());
        }

    }

    /**
     * 计算欠费金额
     *
     * @param tmpFeeDto
     */
    private void computeOweFee(FeeDto tmpFeeDto) {
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

        if ("3333".equals(feeDto.getPayerObjType())) { //房屋相关
            computeFeePriceByRoom(feeDto);
        } else if ("6666".equals(feeDto.getPayerObjType())) {//车位相关
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
        double feePrice = 0.00;
        if ("1001".equals(computingFormula)) { //面积*单价+附加费
            BigDecimal squarePrice = new BigDecimal(Double.parseDouble(feeDto.getSquarePrice()));
            BigDecimal builtUpArea = new BigDecimal(Double.parseDouble(parkingSpaceDtos.get(0).getArea()));
            BigDecimal additionalAmount = new BigDecimal(Double.parseDouble(feeDto.getAdditionalAmount()));
            feePrice = squarePrice.multiply(builtUpArea).add(additionalAmount).setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
        } else if ("2002".equals(computingFormula)) { // 固定费用

            BigDecimal additionalAmount = new BigDecimal(Double.parseDouble(feeDto.getAdditionalAmount()));
            feePrice = additionalAmount.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
        } else if ("4004".equals(computingFormula)) {
            feePrice = Double.parseDouble(feeDto.getAmount());
        } else if ("5005".equals(computingFormula)) {
            if (StringUtil.isEmpty(feeDto.getCurDegrees())) {
                feePrice = -1.00;
            } else {
                BigDecimal curDegree = new BigDecimal(Double.parseDouble(feeDto.getCurDegrees()));
                BigDecimal preDegree = new BigDecimal(Double.parseDouble(feeDto.getPreDegrees()));
                BigDecimal squarePrice = new BigDecimal(Double.parseDouble(feeDto.getSquarePrice()));
                BigDecimal additionalAmount = new BigDecimal(Double.parseDouble(feeDto.getAdditionalAmount()));
                BigDecimal sub = curDegree.subtract(preDegree);
                feePrice = sub.multiply(squarePrice)
                        .add(additionalAmount)
                        .setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
            }
        } else {
            feePrice = 0.00;
        }
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
        double feePrice = 0.00;
        if ("1001".equals(computingFormula)) { //面积*单价+附加费
            BigDecimal squarePrice = new BigDecimal(Double.parseDouble(feeDto.getSquarePrice()));
            BigDecimal builtUpArea = new BigDecimal(Double.parseDouble(roomDtos.get(0).getBuiltUpArea()));
            BigDecimal additionalAmount = new BigDecimal(Double.parseDouble(feeDto.getAdditionalAmount()));
            feePrice = squarePrice.multiply(builtUpArea).add(additionalAmount).setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
        } else if ("2002".equals(computingFormula)) { // 固定费用
            BigDecimal additionalAmount = new BigDecimal(Double.parseDouble(feeDto.getAdditionalAmount()));
            feePrice = additionalAmount.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
        } else if ("4004".equals(computingFormula)) {
            feePrice = Double.parseDouble(feeDto.getAmount());
        } else if ("5005".equals(computingFormula)) {
            if (StringUtil.isEmpty(feeDto.getCurDegrees())) {
                feePrice = -1.00;
            } else {
                BigDecimal curDegree = new BigDecimal(Double.parseDouble(feeDto.getCurDegrees()));
                BigDecimal preDegree = new BigDecimal(Double.parseDouble(feeDto.getPreDegrees()));
                BigDecimal squarePrice = new BigDecimal(Double.parseDouble(feeDto.getSquarePrice()));
                BigDecimal additionalAmount = new BigDecimal(Double.parseDouble(feeDto.getAdditionalAmount()));
                BigDecimal sub = curDegree.subtract(preDegree);
                feePrice = sub.multiply(squarePrice)
                        .add(additionalAmount)
                        .setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
            }
        } else {
            feePrice = 0.00;
        }

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
     * 计算2个日期之间相差的  以年、月、日为单位，各自计算结果是多少
     * 比如：2011-02-02 到  2017-03-02
     * 以年为单位相差为：6年
     * 以月为单位相差为：73个月
     * 以日为单位相差为：2220天
     *
     * @param fromDate
     * @param toDate
     * @return
     */
    public static double dayCompare(Date fromDate, Date toDate) {
        Calendar from = Calendar.getInstance();
        from.setTime(fromDate);
        Calendar to = Calendar.getInstance();
        to.setTime(toDate);

        long t1 = from.getTimeInMillis();
        long t2 = to.getTimeInMillis();
        long days = (t2 - t1) / (24 * 60 * 60 * 1000);

        BigDecimal tmpDays = new BigDecimal(days);
        BigDecimal monthDay = new BigDecimal(30);

        return tmpDays.divide(monthDay, 2, RoundingMode.HALF_UP).doubleValue();
    }

    private Map getTargetEndDateAndOweMonth(FeeDto feeDto) {
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

    private Date getTargetEndTime(double v, Date startDate) {
        Calendar endDate = Calendar.getInstance();
        endDate.setTime(startDate);
        endDate.add(Calendar.MONTH, (int) v);
        return endDate.getTime();
    }

}
