package com.hujiang.project.zhgd.hjConstructionCompany.api;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hujiang.common.utils.http.HttpUtils;
import com.hujiang.framework.web.controller.BaseController;
import com.hujiang.framework.web.domain.AjaxResult;
import com.hujiang.project.common.AesUtils;
import com.hujiang.project.common.CommonUtils;
import com.hujiang.project.common.FuJianUtils;
import com.hujiang.project.common.LoggerUitls;
import com.hujiang.project.common.Result;
import com.hujiang.project.zhgd.hjConstructionCompany.domain.HjConstructionCompany;
import com.hujiang.project.zhgd.hjConstructionCompany.service.IHjConstructionCompanyService;
import com.hujiang.project.zhgd.hjConstructionProject.domain.HjConstructionProject;
import com.hujiang.project.zhgd.hjConstructionProject.service.IHjConstructionProjectService;
import com.hujiang.project.zhgd.hjDictionaries.domain.HjDictionaries;
import com.hujiang.project.zhgd.hjDictionaries.service.IHjDictionariesService;
import com.hujiang.project.zhgd.hjLogging.domain.HjLogging;
import com.hujiang.project.zhgd.hjLogging.service.IHjLoggingService;
import com.hujiang.project.zhgd.hjProject.domain.HjProject;
import com.hujiang.project.zhgd.hjProject.service.IHjProjectService;
import com.hujiang.project.zhgd.hjSynchronizationInformation.domain.HjSynchronizationInformation;
import com.hujiang.project.zhgd.hjSynchronizationInformation.service.IHjSynchronizationInformationService;
import com.hujiang.project.zhgd.utils.APIClient;
import com.hujiang.project.zhgd.utils.Constants;
import com.hujiang.project.zhgd.utils.ZCAPIClientTwo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 参建单位
 *
 * @author hujiang
 * @date 2019-05-19
 */
@RestController
@RequestMapping(value = "/provider/constructionCompanyApi", method = RequestMethod.POST)
public class ConstructionCompanyApi extends BaseController {
    private Logger logger = Logger.getLogger(ConstructionCompanyApi.class.getName());
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private IHjConstructionCompanyService hjConstructionCompanyService;
    @Autowired
    private IHjConstructionProjectService hjConstructionProjectService;
    @Autowired
    private IHjProjectService projectService;
    @Autowired
    private IHjSynchronizationInformationService hjSynchronizationInformationService;
    @Autowired
    private IHjDictionariesService hjDictionariesService;
    @Autowired
    private IHjLoggingService hjLoggingService;

    /**
     * 新建保存参建单位
     * @param projectId 项目id
     * @return
     */
    @RequestMapping("/insertConstructionCompany")
    @ResponseBody
    public AjaxResult insertConstructionCompany(@RequestBody HjConstructionCompany hjConstructionCompany, Integer projectId) throws Exception {

        logger.info("保存参建单位开始");
        String platformName = null;
        String token = null;
        if ("1".equals(hjConstructionCompany.getIsUpload())) {
            //同步参建单位到住建局
            HjSynchronizationInformation hs = new HjSynchronizationInformation();
            hs.setProjectId(projectId);
            hs.setState(1);
            hs.setApiType("keytype1");
//        hs.setPlatformName("HOUS");
            List<HjSynchronizationInformation> hList = hjSynchronizationInformationService.selectHjSynchronizationInformationList(hs);

            Map<String, Object> projectMap = projectService.getProject(projectId);
            HjProject project = JSONObject.parseObject(JSONObject.toJSONString(projectMap), HjProject.class);

            String apiKey = null;
            String apiSecret = null;
            if (hList != null && hList.size() > 0) {
                apiKey = hList.get(0).getApiKey();
                apiSecret = hList.get(0).getApiSecret();
            }

            //有秘钥才去上传
            for (HjSynchronizationInformation h : hList) {
                //深圳住建局上传企业
                if ("HOUS".equals(h.getPlatformName())) {
                    JSONObject json = new JSONObject();
                    json.put("Project_ID", h.getProjectNumber());
                    json.put("Company_Name", hjConstructionCompany.getConstructionName());
                    json.put("Legal_Person", hjConstructionCompany.getLegalPerson());
                    json.put("SUID", hjConstructionCompany.getSuid());
                    HjDictionaries hd = hjDictionariesService.selectHjDictionariesById(hjConstructionCompany.getCompanyType());
                    json.put("type", hd.getTag().split("-")[0]);
                    String url = ZCAPIClientTwo.getUrl(h.getApiSecret(), h.getApiKey(), "1.1", h.getClientSerial(), json.toString(), Constants.HJ_FORMALHOST + "AddCompany");
                    String result = ZCAPIClientTwo.httpPostWithJSON(url, json);
                    JSONObject s = JSONObject.parseObject(result);
                    if ("false".equals(s.getString("result"))) {
                        HjLogging hl = new HjLogging();
                        hl.setProjectId(projectId);
                        hl.setLoggingMessage(s.getString("detail_message"));
                        hl.setLoggingData(result);
                        hl.setInOut("上传企业信息失败！");
                        hl.setUserName(hjConstructionCompany.getConstructionName());
                        hl.setLoggingTag(h.getPlatformName());
                        hl.setLoggingTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                        hjLoggingService.insertHjLogging(hl);

                    }

                } else if ("DGHOUS".equals(h.getPlatformName())) {
                    //东莞住建局上传企业
                    JSONObject body = new JSONObject();
                    JSONObject body2 = new JSONObject();
                    body2.put("Name", hjConstructionCompany.getConstructionName());
                    body2.put("Email", hjConstructionCompany.getEmail());
                    body2.put("SocialUnifiedCreditCode", hjConstructionCompany.getSuid());
                    HjDictionaries hd = hjDictionariesService.selectHjDictionariesById(hjConstructionCompany.getCompanyType());
                    body2.put("Type", hd.getTag().split("-")[2]);
                    body2.put("VocationalAdmin", hjConstructionCompany.getContacts());
                    body2.put("VocationalAdminLinktel", hjConstructionCompany.getMobilePhone());
                    body.put("Data", body2);
                    String url = APIClient.getUrlDG(h.getApiKey(), body.toString(), Constants.DG_HOUS + "/UploadCompany");
                    String result = APIClient.httpPostWithJSONDG(url, body);
                    JSONObject s = JSONObject.parseObject(result);
                    if (!"1".equals(s.getString("StateCode"))) {
                        HjLogging hl = new HjLogging();
                        hl.setProjectId(projectId);
                        hl.setLoggingMessage(s.getString("ErrMsg"));
                        hl.setLoggingData(result);
                        hl.setInOut("上传企业信息失败！");
                        hl.setUserName(hjConstructionCompany.getConstructionName());
                        hl.setLoggingTag(h.getPlatformName());
                        hl.setLoggingTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                        hjLoggingService.insertHjLogging(hl);
                    } else {
                        hjConstructionCompany.setComId(s.getJSONObject("ResultData").getInteger("ComId"));
                    }
                    platformName = h.getPlatformName();
                    token = h.getApiKey();
                }

                //对接福建两制
                if ("FUJIAN".equals(h.getPlatformName())) {
                    String paramStr = uploadParam(apiKey, apiSecret, hjConstructionCompany, project);
                    String msg = "福建两制上传项目参建单位信息";
                    Result result = HttpUtils.httpPostWithjson(Constants.HUJIAN_TWO_SYSTEMS, paramStr, msg);
                    if ("0".equals(result.getCode()) && result.getData() != null) {
                        LoggerUitls.logInfo("福建两制上传项目参建单位信息成功", result.getCode(), result.getMessage(), result.getData());
                    } else {
                        LoggerUitls.logInfo("福建两制上传项目参建单位信息失败", result.getCode(), result.getMessage(), result.getData());
                    }
                }
            }
//            /** 对接惠州市住建局 */
//            if ("XIEHOUS".equals(h.getPlatformName())){
//
//            }

        }

        logger.info("保存参建单位");
        //保存参建单位信息
        int i = hjConstructionCompanyService.insertHjConstructionCompany(hjConstructionCompany);
        if (i < 1) {
            return AjaxResult.error("保存参建单位失败");
        }
        //将总包单位和监理单位更新到项目信息
        if (hjConstructionCompany.getCompanyType() == 2) {
            HjProject project = new HjProject();
            project.setId(hjConstructionCompany.getProjectId());
            project.setSupervisorId(hjConstructionCompany.getId());
            projectService.updateHjProject(project);
            if ("DGHOUS".equals(platformName)) {
                isUploadDG(projectId, token);
            }
        }
        if (hjConstructionCompany.getCompanyType() == 4) {
            HjProject project = new HjProject();
            project.setId(hjConstructionCompany.getProjectId());
            project.setConstructionId(hjConstructionCompany.getId());
            projectService.updateHjProject(project);
            if ("DGHOUS".equals(platformName)) {
                isUploadDG(projectId, token);
            }
        }

        logger.info("保存参建单位项目");
        HjConstructionProject hjConstructionProject = new HjConstructionProject();
        hjConstructionProject.setConstructionId(hjConstructionCompany.getId());
        hjConstructionProject.setProjectId(projectId);
        int j = hjConstructionProjectService.insertHjConstructionProject(hjConstructionProject);
        if (j < 1) {
            return AjaxResult.error("保存参建单位项目失败");
        }
        logger.info("保存参建单位结束");
        return AjaxResult.success();
    }

    /**
     * @Author xieya
     * @Description 上传项目参建单位信息入参组装
     * @Date 2020/4/17 11:54
     * @param apiKey
     * @param apiSecret
     * @return java.util.Map<java.lang.String, java.lang.String>
     **/
    private String uploadParam(String apiKey, String apiSecret, HjConstructionCompany hjConstructionCompany, HjProject project) {
        Map apiParam = FuJianUtils.setHeader(FuJianUtils.PROJECT_SUBCONTRACTOR_UPLOAD);
        apiParam.put("appid", apiKey);
        Map<String, Object> param = uploadParamMap(hjConstructionCompany, project, apiSecret);

        String data = JSON.toJSONString(param);

        apiParam.put("data", data);
        String sign = CommonUtils.getSign(apiParam, apiSecret);
        apiParam.put("sign", sign);
        return JSON.toJSONString(apiParam);
    }

    private Map<String, Object> uploadParamMap(HjConstructionCompany constructionCompany, HjProject project, String apiSecret) {
        Map<String, Object> paramMap = new HashMap<>();
        //项目编码
        paramMap.put("projectCode", project.getProjectCode());
        //统一社会信用代码， 如果无统一社会信用代码，则用组织机构代码
        paramMap.put("corpCode", constructionCompany.getSuid());
        //企业名称
        paramMap.put("corpName", constructionCompany.getConstructionName());
        //参建类型。参考参建单位类型字典表
        paramMap.put("corpType", constructionCompany.getCompanyType());
        //发放工资的银行。 JSON数组
        JSONArray bankInfos = new JSONArray();
        JSONObject json = new JSONObject();
        json.put("bankName", constructionCompany.getBankAddress());
        //aes加密
        json.put("bankNumber", AesUtils.encrypt(constructionCompany.getBankNum(), apiSecret));
        bankInfos.add(json);
        paramMap.put("bankInfos", bankInfos);
        //项目经理名称
        paramMap.put("pmName", constructionCompany.getCompanyType());
        //项目经理电话
        paramMap.put("pmPhone", constructionCompany.getMobilePhone());

        return paramMap;
    }

    /**
     * 是否上传东莞住建
     */
    private void isUploadDG(Integer projectId, String token) throws Exception {
        HjProject hjProject = projectService.selectHjProjectById(projectId);
        //总包和监理都已上传
        if (hjProject.getConstructionId() != null && hjProject.getSupervisorId() != null) {
            JSONObject body = new JSONObject();
            JSONObject body2 = new JSONObject();
            String[] areaId = hjProject.getProjectRegion().split(",");
            body2.put("AreaId", "00" + areaId[areaId.length - 1]);
            body2.put("ItemName", hjProject.getProjectName());
            body2.put("Circs", hjProject.getProjectState());
            body2.put("ItemType", hjProject.getProjectType());
            body2.put("SupLevel", 2);
            body2.put("Manager", hjProject.getProjectPrincipal());
            body2.put("ManagerPhone", hjProject.getPhone());
            body2.put("StartDate", hjProject.getStartingTime().substring(0, hjProject.getStartingTime().indexOf(" ")));
            body2.put("PlanCompletionDate", hjProject.getFinishTime().substring(0, hjProject.getFinishTime().indexOf(" ")));
            HjConstructionCompany hcc = hjConstructionCompanyService.selectHjConstructionCompanyById(hjProject.getConstructionId());
            HjConstructionCompany hcc2 = hjConstructionCompanyService.selectHjConstructionCompanyById(hjProject.getSupervisorId());
            body2.put("BuilderUnit", hcc.getComId());
            body2.put("SupervisorUnit", hcc2.getComId());
            body.put("Data", body2);
            System.out.println(body);
            String url = APIClient.getUrlDG(token, body.toString(), Constants.DG_HOUS + "/UploadItem");
            String result = APIClient.httpPostWithJSONDG(url, body);
            JSONObject s = JSONObject.parseObject(result);
            //失败则记录原因
            if (!"1".equals(s.getString("StateCode"))) {
                HjLogging hl = new HjLogging();
                hl.setProjectId(projectId);
                hl.setLoggingMessage(s.getString("ErrMsg"));
                hl.setLoggingData(result);
                hl.setInOut("上传工程项目信息失败！");
                hl.setUserName(hjProject.getProjectName());
                hl.setLoggingTag("DGHOUS");
                hl.setLoggingTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                hjLoggingService.insertHjLogging(hl);
            } else {
                //成功更新ID
                hjProject.setItemId(s.getJSONObject("ResultData").getInteger("ItemId"));
                projectService.updateHjProject(hjProject);
            }
        }
    }

    /**
     * 新建保存参建单位
     * @return
     */
    @RequestMapping("/insertConstructionCompanyTwo")
    public AjaxResult insertConstructionCompanyTwo(@RequestBody String json) {
        JSONObject jsonResult = JSONObject.parseObject(json);
        HjConstructionCompany hjConstructionCompany = JSONObject.parseObject(jsonResult.toJSONString(), HjConstructionCompany.class);
        HjProject hjProject = projectService.selectHjProjectById(hjConstructionCompany.getProjectId()); //获取项目ID
        if (hjProject != null) {  //判断项目ID是否存在
            //保存参建单位信息
            int i = hjConstructionCompanyService.insertHjConstructionCompany(hjConstructionCompany);
            if (i < 1) {
                return AjaxResult.error("保存参建单位失败");
            }


            HjConstructionProject hjConstructionProject = new HjConstructionProject();
            hjConstructionProject.setConstructionId(hjConstructionCompany.getId());
            hjConstructionProject.setProjectId(hjConstructionCompany.getProjectId());
            int j = hjConstructionProjectService.insertHjConstructionProject(hjConstructionProject);
            if (j < 1) {
                return AjaxResult.error("保存参建单位项目失败");
            }

        } else {
            return AjaxResult.error("项目ID不存在");
        }


        return AjaxResult.success();
    }


    /**
     * 修改保存参建单位
     * @param hjConstructionCompany 参建单位信息
     * @return
     */
    @RequestMapping("/updateConstructionCompany")
    @ResponseBody
    public AjaxResult updateConstructionCompany(@RequestBody HjConstructionCompany hjConstructionCompany) {

        //同步参建单位到福建
        HjSynchronizationInformation hs = new HjSynchronizationInformation();
        hs.setProjectId(hjConstructionCompany.getProjectId());
        hs.setState(1);
        hs.setApiType("keytype1");
        List<HjSynchronizationInformation> hList = hjSynchronizationInformationService.selectHjSynchronizationInformationList(hs);

        Map<String, Object> projectMap = projectService.getProject(hjConstructionCompany.getProjectId());
        HjProject project = JSONObject.parseObject(JSONObject.toJSONString(projectMap), HjProject.class);

        String apiKey = null;
        String apiSecret = null;
        if (hList != null && hList.size() > 0) {
            apiKey = hList.get(0).getApiKey();
            apiSecret = hList.get(0).getApiSecret();
        }

        //对接福建两制
        try{
            for (HjSynchronizationInformation h : hList) {
                if ("FUJIAN".equals(h.getPlatformName())) {
                    String paramStr = updateParam(apiKey, apiSecret, hjConstructionCompany, project);
                    String msg = "福建两制跟新项目参建单位信息";
                    Result result = HttpUtils.httpPostWithjson(Constants.HUJIAN_TWO_SYSTEMS, paramStr, msg);
                    if ("0".equals(result.getCode()) && result.getData() != null) {
                        LoggerUitls.logInfo("福建两制跟新项目参建单位信息成功", result.getCode(), result.getMessage(), result.getData());
                    } else {
                        LoggerUitls.logInfo("福建两制跟新项目参建单位信息失败", result.getCode(), result.getMessage(), result.getData());
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        int i;
        logger.info("更新参建单位开始");
        hjConstructionCompany.setUpdateDate(dateFormat.format(System.currentTimeMillis()));
        //保存参建单位信息
        i = hjConstructionCompanyService.updateHjConstructionCompany(hjConstructionCompany);
        if (i < 1) {
            return AjaxResult.error("保存参建单位失败");
        }
        logger.info("更新参建单位结束");
        return AjaxResult.success();
    }

    private String updateParam(String apiKey, String apiSecret, HjConstructionCompany hjConstructionCompany, HjProject project) {
        Map apiParam = FuJianUtils.setHeader(FuJianUtils.PROJECT_SUBCONTRACTOR_UPDATE);
        apiParam.put("appid", apiKey);
        Map<String, Object> param = uploadParamMap(hjConstructionCompany, project, apiSecret);

        String data = JSON.toJSONString(param);

        apiParam.put("data", data);
        String sign = CommonUtils.getSign(apiParam, apiSecret);
        apiParam.put("sign", sign);
        return JSON.toJSONString(apiParam);
    }

    /**
     * 导出参建单位 数据源
     * @param param
     * @param suid
     * @param projectId
     * @return
     */
    @PostMapping("/export")
    @ResponseBody
    public List<HjConstructionCompany> export(String param, String suid, Integer projectId) {
        System.out.println(param);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("projectId", projectId);
        if (!"".equals(param) && param != "") {
            map.put("param", param);
        }
        if (!"".equals(suid) && suid != "") {
            map.put("suid", suid);
        }
        return hjConstructionCompanyService.selectHjConstructionCompanyListTwo(map);

    }

    /**
     * 查询参建单位列表
     * @param projectId 项目id
     * @param param 项目名或简称
     * @return
     */
    @RequestMapping("/selectConstructionCompanyList")
    @ResponseBody
    public AjaxResult selectHjConstructionCompanyListTwo(String param, String suid, Integer projectId) {
        logger.info("查询参建单位列表开始---/selectConstructionCompanyList");
        startPage();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("projectId", projectId);
        System.out.println(param);
        System.out.println(projectId);
        if (!"".equals(param) && param != "") {
            map.put("param", param);
        }
        if (!"".equals(suid) && suid != "") {
            map.put("suid", suid);
        }
        logger.info("查询参建单位列表结束");
        return AjaxResult.success(getDataTable(hjConstructionCompanyService.selectHjConstructionCompanyListTwo(map)));
    }

    /**
     * 根据id查询参建单位
     * @param id 参建单位id
     * @return
     */
    @RequestMapping("/selectConstructionCompanyId")
    @ResponseBody
    public AjaxResult selectHjConstructionCompanyIds(Integer id) {
        logger.info("根据id查询参建单位开始---/selectConstructionCompanyId");
        HjConstructionCompany hjConstructionCompany = hjConstructionCompanyService.selectHjConstructionCompanyById(id);
        logger.info("查询参建单位列表结束");
        return AjaxResult.success(hjConstructionCompany);
    }


    /**
     * 参建单位
     * @param projectId 项目id
     * @return
     */
    @RequestMapping("/selectConstructionCompany")
    @ResponseBody
    public Map<String, Object> selectConstructionCompany(Integer projectId) {
        return hjConstructionCompanyService.selectConstructionCompany(projectId);
    }


    /**
     * 删除参建单位
     * @param ids 参建单位id
     * @return
     */
    @RequestMapping("/deleteConstructionCompanyIds")
    @ResponseBody
    public AjaxResult deleteHjConstructionCompanyIds(String ids) throws Exception {
        logger.info("删除参建单位开始---deleteConstructionCompanyIds---start");
        int i = hjConstructionCompanyService.deleteHjConstructionCompanyByIdsTwo(ids);
        if (i < 1) {
            return AjaxResult.error("删除参建单位失败");
        }
        HjConstructionCompany hjConstructionCompany = hjConstructionCompanyService.selectHjConstructionCompanyById(Integer.valueOf(ids));
        HjConstructionProject hp = new HjConstructionProject();
        hp.setConstructionId(Integer.valueOf(ids));
        List<HjConstructionProject> hcpList = hjConstructionProjectService.selectHjConstructionProjectList(hp);
        HjConstructionProject hcp = hcpList.get(0);
        Integer projectId = hcp.getProjectId();
        //同步参建单位到住建局
        HjSynchronizationInformation hs = new HjSynchronizationInformation();
        hs.setProjectId(projectId);
        hs.setState(1);
        hs.setApiType("keytype1");
        hs.setPlatformName("HOUS");
        List<HjSynchronizationInformation> hList = hjSynchronizationInformationService.selectHjSynchronizationInformationList(hs);
        //有秘钥才去上传
        if (hList.size() > 0) {
            HjSynchronizationInformation h = hList.get(0);
            JSONObject json = new JSONObject();
            json.put("Project_ID", h.getProjectNumber());
            json.put("Company_Name", hjConstructionCompany.getConstructionName());
            json.put("SUID", hjConstructionCompany.getSuid());
            String url = ZCAPIClientTwo.getUrl(h.getApiSecret(), h.getApiKey(), "1.1", h.getClientSerial(), json.toString(), Constants.HJ_FORMALHOST + "ProjectRemoveCompany");
            String result = ZCAPIClientTwo.httpPostWithJSON(url, json);
            JSONObject s = JSONObject.parseObject(result);
            if ("false".equals(s.getString("result"))) {
                HjLogging hl = new HjLogging();
                hl.setProjectId(projectId);
                hl.setLoggingMessage(s.getString("detail_message"));
                hl.setLoggingData(result);
                hl.setInOut("移除企业信息失败！");
                hl.setUserName(hjConstructionCompany.getConstructionName());
                hl.setLoggingTag(h.getPlatformName());
                hl.setLoggingTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                hjLoggingService.insertHjLogging(hl);

            }

        }

        logger.info("删除参建单位列表结束---deleteConstructionCompanyIds---end");
        return AjaxResult.success("删除参建单位成功");
    }


}
