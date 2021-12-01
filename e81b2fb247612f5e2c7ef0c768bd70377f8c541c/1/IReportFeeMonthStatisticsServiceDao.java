package com.java110.report.dao;


import com.java110.utils.exception.DAOException;

import java.util.List;
import java.util.Map;

/**
 * 费用月统计组件内部之间使用，没有给外围系统提供服务能力
 * 费用月统计服务接口类，要求全部以字符串传输，方便微服务化
 * 新建客户，修改客户，删除客户，查询客户等功能
 * <p>
 * Created by wuxw on 2016/12/27.
 */
public interface IReportFeeMonthStatisticsServiceDao {


    /**
     * 保存 费用月统计信息
     *
     * @param info
     * @throws DAOException DAO异常
     */
    void saveReportFeeMonthStatisticsInfo(Map info) throws DAOException;


    /**
     * 查询费用月统计信息（instance过程）
     * 根据bId 查询费用月统计信息
     *
     * @param info bId 信息
     * @return 费用月统计信息
     * @throws DAOException DAO异常
     */
    List<Map> getReportFeeMonthStatisticsInfo(Map info) throws DAOException;


    /**
     * 修改费用月统计信息
     *
     * @param info 修改信息
     * @throws DAOException DAO异常
     */
    void updateReportFeeMonthStatisticsInfo(Map info) throws DAOException;


    /**
     * 修改费用月统计信息
     *
     * @param info 修改信息
     * @throws DAOException DAO异常
     */
    void updateReportFeeMonthStatisticsOwe(Map info) throws DAOException;


    /**
     * 查询费用月统计总数
     *
     * @param info 费用月统计信息
     * @return 费用月统计数量
     */
    int queryReportFeeMonthStatisticssCount(Map info);

    /**
     * 查询费用月统计总数
     *
     * @param info 费用月统计信息
     * @return 费用月统计数量
     */
    int queryReportFeeSummaryCount(Map info);


    /**
     * 查询费用月统计信息（instance过程）
     * 根据bId 查询费用月统计信息
     *
     * @param info bId 信息
     * @return 费用月统计信息
     * @throws DAOException DAO异常
     */
    List<Map> queryReportFeeSummary(Map info) throws DAOException;


    /**
     * 查询费用月统计总数
     *
     * @param info 费用月统计信息
     * @return 费用月统计数量
     */
    int queryReportFloorUnitFeeSummaryCount(Map info);


    /**
     * 查询费用月统计信息（instance过程）
     * 根据bId 查询费用月统计信息
     *
     * @param info bId 信息
     * @return 费用月统计信息
     * @throws DAOException DAO异常
     */
    List<Map> queryReportFloorUnitFeeSummary(Map info) throws DAOException;

    /**
     * 查询费用月统计总数
     *
     * @param info 费用月统计信息
     * @return 费用月统计数量
     */
    int queryFeeBreakdownCount(Map info);


    /**
     * 查询费用月统计信息（instance过程）
     * 根据bId 查询费用月统计信息
     *
     * @param info bId 信息
     * @return 费用月统计信息
     * @throws DAOException DAO异常
     */
    List<Map> queryFeeBreakdown(Map info) throws DAOException;

    /**
     * 查询费用月统计总数
     *
     * @param info 费用月统计信息
     * @return 费用月统计数量
     */
    int queryFeeDetailCount(Map info);


    /**
     * 查询费用月统计信息（instance过程）
     * 根据bId 查询费用月统计信息
     *
     * @param info bId 信息
     * @return 费用月统计信息
     * @throws DAOException DAO异常
     */
    List<Map> queryFeeDetail(Map info) throws DAOException;


    /**
     * 查询费用月统计总数
     *
     * @param info 费用月统计信息
     * @return 费用月统计数量
     */
    int queryOweFeeDetailCount(Map info);
    int queryPayFeeDetailCount(Map info);
    int queryDeadlineFeeCount(Map info);
    /**
     * 查询费用月统计信息（instance过程）
     * 根据bId 查询费用月统计信息
     *
     * @param info bId 信息
     * @return 费用月统计信息
     * @throws DAOException DAO异常
     */
    List<Map> queryPrePayment(Map info) throws DAOException;


    /**
     * 查询费用月统计总数
     *
     * @param info 费用月统计信息
     * @return 费用月统计数量
     */
    int queryPrePaymentNewCount(Map info);


    /**
     * 查询费用月统计信息（instance过程）
     * 根据bId 查询费用月统计信息
     *
     * @param info bId 信息
     * @return 费用月统计信息
     * @throws DAOException DAO异常
     */
    List<Map> queryOweFeeDetail(Map info) throws DAOException;
    List<Map> queryPayFeeDetail(Map info) throws DAOException;
    List<Map> queryDeadlineFee(Map info) throws DAOException;

    /**
     * 查询预交费
     * 根据bId 查询费用月统计信息
     *
     * @param info bId 信息
     * @return 费用月统计信息
     * @throws DAOException DAO异常
     */
    List<Map> queryPrePaymentCount(Map info) throws DAOException;
    List<Map> queryDeadlinePaymentCount(Map info) throws DAOException;
    /**
     * 查询预交费
     * 根据bId 查询费用月统计信息
     *
     * @param info bId 信息
     * @return 费用月统计信息
     * @throws DAOException DAO异常
     */
    List<Map> queryOwePaymentCount(Map info) throws DAOException;


    /**
     * 查询 缴费已经结束 报表表还有欠费的数据
     * 根据bId 查询费用月统计信息
     *
     * @param info bId 信息
     * @return 费用月统计信息
     * @throws DAOException DAO异常
     */
    List<Map> queryFinishOweFee(Map info) throws DAOException;


}
