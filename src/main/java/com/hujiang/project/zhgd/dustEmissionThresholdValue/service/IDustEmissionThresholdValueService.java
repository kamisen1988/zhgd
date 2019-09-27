package com.hujiang.project.zhgd.dustEmissionThresholdValue.service;

import com.hujiang.project.zhgd.dustEmissionThresholdValue.domain.DustEmissionThresholdValue;
import java.util.List;

/**
 * 项目扬尘设备阈值 服务层
 * 
 * @author hujiang
 * @date 2019-07-08
 */
public interface IDustEmissionThresholdValueService 
{
	/**
	 * 根据项目ID查询信息
	 * @param projectId
	 * @return
	 */
	public DustEmissionThresholdValue selectDustEmissionThresholdById(int projectId);
	/**
     * 查询项目扬尘设备阈值信息
     * 
     * @param id 项目扬尘设备阈值ID
     * @return 项目扬尘设备阈值信息
     */
	public DustEmissionThresholdValue selectDustEmissionThresholdValueById(Integer id);
	
	/**
     * 查询项目扬尘设备阈值列表
     * 
     * @param dustEmissionThresholdValue 项目扬尘设备阈值信息
     * @return 项目扬尘设备阈值集合
     */
	public List<DustEmissionThresholdValue> selectDustEmissionThresholdValueList(DustEmissionThresholdValue dustEmissionThresholdValue);
	
	/**
     * 新增项目扬尘设备阈值
     * 
     * @param dustEmissionThresholdValue 项目扬尘设备阈值信息
     * @return 结果
     */
	public int insertDustEmissionThresholdValue(DustEmissionThresholdValue dustEmissionThresholdValue);
	
	/**
     * 修改项目扬尘设备阈值
     * 
     * @param dustEmissionThresholdValue 项目扬尘设备阈值信息
     * @return 结果
     */
	public int updateDustEmissionThresholdValue(DustEmissionThresholdValue dustEmissionThresholdValue);
		
	/**
     * 删除项目扬尘设备阈值信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
	public int deleteDustEmissionThresholdValueByIds(String ids);
	
}
