package cn.zdxh.personnelmanage.controller;


import cn.zdxh.personnelmanage.enums.ResultEnum;
import cn.zdxh.personnelmanage.exception.MyException;
import cn.zdxh.personnelmanage.po.EmployeeInfo;
import cn.zdxh.personnelmanage.po.InductionInfo;
import cn.zdxh.personnelmanage.po.ProfessionalSkills;
import cn.zdxh.personnelmanage.service.EmployeeInfoService;
import cn.zdxh.personnelmanage.service.ProfessionalSkillsService;
import cn.zdxh.personnelmanage.utils.ExcelTitlesHelperUtils;
import cn.zdxh.personnelmanage.utils.ExcelValuesHelperUtils;
import cn.zdxh.personnelmanage.utils.ExportExcelUtils;
import cn.zdxh.personnelmanage.utils.UploadUtils;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 员工相关专业技能证书档案表 前端控制器
 * </p>
 *
 * @author Justin
 * @since 2019-03-15
 */
@Controller
@RequestMapping("/ProfessionalSkills")
public class ProfessionalSkillsController {
    @Autowired
    private EmployeeInfoService employeeInfoService;
    @Autowired
    private ProfessionalSkillsService professionalSkillsService;

    @GetMapping("/list")
    public String findProfessionSkill(@RequestParam(value = "currentPage", defaultValue = "0") Integer currentPage, @RequestParam(value = "limit", defaultValue = "3") Integer limit, Map<String, Object> map){
        List<ProfessionalSkills> list = professionalSkillsService.findProfessionalSkillsAll(currentPage, limit);
        Integer addCount = professionalSkillsService.findProfessionalSkillsAllCount();
        map.put("list", list);
        map.put("currentPage", currentPage);
        map.put("totalPage", addCount);
        map.put("operation", "professional_skill_model");
        map.put("URL", "/ProfessionalSkills");
        return "employee/employee_list";
    }

    @GetMapping("/create")
    public String goProfessionCreate(Map<String, Object> map){
        List<EmployeeInfo> list = employeeInfoService.selectList(null);
        map.put("type", "create");
        map.put("operation", "professional_skill_model");
        map.put("req_url", "/ProfessionalSkills/add");
        map.put("employees", list);
        return "employee/employee_create";
    }

    @GetMapping("/add")
    public String addProfessionSkill(ProfessionalSkills professionalSkills, Map<String, Object> map){
        if (professionalSkills != null){
            professionalSkillsService.insertProfessionalSkills(professionalSkills);
        }
        map.put("operation", "success");
        return "result/success";
    }

    @GetMapping("/update/{id}")
    public String goProfessionUpdate(@PathVariable("id") Integer id, Map<String, Object> map){
        ProfessionalSkills professionalSkills = professionalSkillsService.findProfessionalSkills(id);
        List<EmployeeInfo> list = employeeInfoService.selectList(null);
        map.put("type", "update");
        map.put("operation", "professional_skill_model");
        map.put("req_url", "/ProfessionalSkills/update");
        map.put("employees", list);
        map.put("professionalSkills", professionalSkills);
        return "employee/employee_create";
    }

    @PostMapping("/update")
    public String updateProfessionSkill(ProfessionalSkills professionalSkills, Map<String, Object> map){
        ProfessionalSkills professionalSkills1 = professionalSkillsService.findProfessionalSkills(professionalSkills.getPsId());
        if (professionalSkills != null){
            professionalSkillsService.updateProfessionalSkills(professionalSkills);
        }
        map.put("operat", "success");
        return "result/success";
    }

    @DeleteMapping("/delete/{id}")
    public String deleteProfessionSkills(@PathVariable("id") Integer id, Map<String, Object> map){
        ProfessionalSkills professionalSkills  = professionalSkillsService.findProfessionalSkills(id);
        if (professionalSkills != null){
            professionalSkillsService.deleteProfessionalSkills(id);
        }
        map.put("opert", "success");
        return "result/success";
    }

    @DeleteMapping("/batchDelete")
    public String batchDeleteBirth(@RequestParam("data_id") String data_id, Map<String, Object> map){
        String[] id = data_id.split(",");
        for (int i = 0; i < id.length; i++){
            ProfessionalSkills professionalSkills  = professionalSkillsService.findProfessionalSkills(Integer.parseInt(id[i]));
            if (professionalSkills != null){
                professionalSkillsService.deleteProfessionalSkills(Integer.parseInt(id[i]));
            }
        }
        return "result/success";
    }

    @PostMapping("/search")
    public String searchBirthRecord(@RequestParam("data") String data, Map<String, Object> map){
        List<ProfessionalSkills> list = professionalSkillsService.findAllProfessionalSkillsBySearch(data);
        map.put("list", list);
        map.put("operation", "professional_skill_model");
        return "employee/employee_list";
    }


    /**
     * 功能需求：完成客户端的Excel表数据写入数据库功能
     *
     * @param file //用户上传的Excel文件
     * @param uploadUtils //上传文件的工具类 cn.zdxh.personnelmanage.utils.UploadUtils
     * @return
     * @throws Exception
     */
    @PostMapping("/updateExcel")
    @ResponseBody
    public JSONObject updateExcel(@RequestParam("file") MultipartFile file, UploadUtils uploadUtils) throws Exception {
        JSONObject json = new JSONObject();
        try {
            //第一个参数为Excel表，第二个参数为从第几行读取Excel的内容，返回值为一个字符串数组集合（每一个数组代表Excel表的一行数据）
            List<String[]> list =  uploadUtils.updateExcelUtils(file, 1);
            //遍历字符串数组集合中的数据
            for (String[] str:list){
                //获取实体类对象封装数据（每一个实体类对象封装Excel表中的一行数据）
                ProfessionalSkills professionalSkills = new ProfessionalSkills();
                //一个工具类，把字符串数组数据封装到实体类对象中，第一个参数为实体类对象，第二个参数为字符串数组
                ExcelValuesHelperUtils.setAttributeValue(professionalSkills, str);

                /**
                 * 在完成Excel表中数据写入数据库操作之前先判断
                 * 该实体类对象的是否为数据库已有（进行更新操作）
                 * 该实体类对象的数据为数据库没有（进行插入操作）
                 */
                if (professionalSkillsService.findProfessionalSkills(Integer.parseInt(str[0])) != null){
                    professionalSkillsService.updateProfessionalSkills(professionalSkills);
                } else {
                    professionalSkillsService.insertProfessionalSkills(professionalSkills);
                }
            }
        } catch (Exception e){
            /**
             * 做一个报错检测
             */
            throw new MyException(ResultEnum.UPDATE_EXCEL_ERROR.getCode(), ResultEnum.UPDATE_EXCEL_ERROR.getMsg());
        }

        //返回客户端的数据
        json.put("code", 1);
        json.put("data", "Excel表上传成功！");
        json.put("ret", true);
        return json;
    }


    /**
     * 需求功能：完成服务器端把数据库中的数据读出客户端功能
     *
     * @param response //把生成的Excel表响应到客户端
     * @throws NoSuchMethodException //报错
     * @throws IllegalAccessException   //报错
     * @throws InvocationTargetException    //报错
     * @throws InstantiationException   //报错
     */
    @GetMapping("/exportExcel")
    public void exportExcel(HttpServletResponse response) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        //先把数据库中的数据查询出来
        List<ProfessionalSkills> list1 = professionalSkillsService.selectList(null);
        //一个设置Excel表标题信息的工具类，获取Excel表标题的字符串数组
        String[] strings = ExcelTitlesHelperUtils.getProfessionalSkillsTitles();
        //一个能把对象集合转换成字符串数组集合的工具类，参数为对象集合，返回字符串数组集合
        List<Object[]> list = ExcelValuesHelperUtils.exportExcel(list1);
        try {
            //一个能创建Excel表并完成发送客户端的工具类，第一个参数为字符串数组集合（Excel表内容），第二个参数为字符串数组（Excel表标题），第三个参数为响应器
            ExportExcelUtils.createExcelUtils(list, strings, response);
        } catch (Exception e){
            //导表发生异常的时候
            throw new MyException(ResultEnum.EXPORT_EXCEL_ERROR.getCode(),ResultEnum.EXPORT_EXCEL_ERROR.getMsg());
        }

    }

}

