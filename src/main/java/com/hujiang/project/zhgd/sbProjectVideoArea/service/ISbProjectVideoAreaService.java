package com.hujiang.project.zhgd.sbProjectVideoArea.service;

import com.hujiang.project.zhgd.sbProjectVideoArea.domain.*;

import java.util.List;

/**
 * 项目视频区 服务层
 * 
 * @author hujiang
 * @date 2019-06-23
 */
public interface ISbProjectVideoAreaService 
{
	/**
     * 查询项目视频区信息
     * 
     * @param id 项目视频区ID
     * @return 项目视频区信息
     */
	public SbProjectVideoArea selectSbProjectVideoAreaById(Integer id);
	
	/**
     * 查询项目视频区列表
     * 
     * @param sbProjectVideoArea 项目视频区信息
     * @return 项目视频区集合
     */
	public List<SbProjectVideoArea> selectSbProjectVideoAreaList(SbProjectVideoArea sbProjectVideoArea);
	
	/**
     * 新增项目视频区
     * 
     * @param sbProjectVideoArea 项目视频区信息
     * @return 结果
     */
	public int insertSbProjectVideoArea(SbProjectVideoArea sbProjectVideoArea);
	
	/**
     * 修改项目视频区
     * 
     * @param sbProjectVideoArea 项目视频区信息
     * @return 结果
     */
	public int updateSbProjectVideoArea(SbProjectVideoArea sbProjectVideoArea);
		
	/**
     * 删除项目视频区信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
	public int deleteSbProjectVideoAreaByIds(String ids);
	/**
	 * 集团获取摄像头列表
	 */
	public List<SbJTArea> getVideoListJT(Integer cid);

	public List<VideoPicUrl> getVideoPicUrl();
	/**
	 * 项目获取摄像头列表
	 */
	public ProjectVideoJT getVideoList(Integer pid);
	public List<PvideoJT> getPvideoList(Integer cid);
}
