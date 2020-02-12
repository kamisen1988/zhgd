package com.hujiang.project.zhgd.sbgroup.mapper;


import com.hujiang.project.zhgd.sbgroup.domain.SbProject;

import java.util.List;

/**
 * 集团对讲账号 数据层
 * 
 * @author hujiang
 * @date 2019-12-05
 */
public interface SbGroupMapper
{
	/**
     * 查询集团信息
     * 
     * @param cid 集团账号ID
     * @return 集团账号信息
     */
	public Object selectSbGroupById(Integer cid);
	
	/**
     * 查询集团下项目总投资额和参建单位
     * 
     * @param @param cid 集团账号ID
     * @return
     */
	public Object selectSbGroupMU(Integer cid);

	/**
	 * 查询集团下项目信息
	 *
	 * @param @param cid 集团账号ID
	 * @return
	 */
	public List<SbProject> selectProjectList(Integer cid);

	/**
	 * 查询集团下不同状态项目数量
	 *
	 * @param @param cid 集团账号ID
	 * @return
	 */
	public int selectProjectCount(Integer cid , String state);

	/**
	 * 查询集团下工人考勤总数量
	 *
	 * @param @param cid 集团账号ID
	 * @return
	 */
	public int selectWorkerAttendance(Integer cid , String time);

	/**
	 * 查询集团下管理人员考勤数量
	 *
	 * @param @param cid 集团账号ID
	 * @return
	 */
	public int selectAdministorAttendance(Integer cid , String time);

	/**
	 * 查询集团下在岗人数
	 *
	 * @param @param cid 集团账号ID
	 * @return
	 */
	public int selectOnGuard(Integer cid);

	/**
	 * 查询集团下车辆通行数据
	 *
	 * @param @param cid 集团账号ID
	 * @return
	 */
	public int selectPlate(Integer cid ,Integer inOut,String time);

	/**
	 * 查询集团下TSP历史数据
	 *
	 * @param @param cid 集团账号ID
	 * @return
	 */
	public int selectTsp(Integer cid ,Integer min,Integer max);
}