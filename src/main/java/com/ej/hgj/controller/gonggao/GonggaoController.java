package com.ej.hgj.controller.gonggao;

import com.ej.hgj.controller.base.BaseController;
import com.ej.hgj.dao.gonggao.GonggaoDaoMapper;
import com.ej.hgj.dao.gonggao.GonggaoTypeDaoMapper;
import com.ej.hgj.entity.gonggao.Gonggao;
import com.ej.hgj.entity.gonggao.GonggaoType;
import com.ej.hgj.enums.JiasvBasicRespCode;
import com.ej.hgj.enums.MonsterBasicRespCode;
import com.ej.hgj.utils.DateUtils;
import com.ej.hgj.vo.gonggao.GonggaoVo;
import com.ej.hgj.vo.gonggao.GonggaoTypeVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class GonggaoController extends BaseController {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private GonggaoDaoMapper gonggaoDaoMapper;

	@Autowired
	private GonggaoTypeDaoMapper gonggaoTypeDaoMapper;

	/**
	 * 公告查询
	 */
//	@ResponseBody
//	@RequestMapping("/notice/queryCurrNotice.do")
//	public GonggaoVo queryCurrNotice(@RequestBody GonggaoVo gonggaoVo) {
//		String proNum = gonggaoVo.getProNum();
//		Gonggao gonggao = new Gonggao();
//		gonggao.setProNum(proNum);
//		List<Gonggao> list = gonggaoDaoMapper.getList(gonggao);
//		gonggaoVo.setGonggaoList(list);
//		gonggaoVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
//		return gonggaoVo;
//	}

	/**
	 * 公告查询-首页
	 * @param request
	 * @return
	 */
	@RequestMapping("/notice/queryCurrNotice.do")
	@ResponseBody
	public GonggaoTypeVo queryCurrNotice(HttpServletRequest request, @RequestBody GonggaoTypeVo gonggaoTypeVo) {
		List<GonggaoType> list = gonggaoTypeDaoMapper.getList(new GonggaoType());
		List<String> typeIds = new ArrayList<>();
		for(GonggaoType gonggaoType : list){
			typeIds.add(gonggaoType.getId());
		}
		Gonggao gonggao = new Gonggao();
		gonggao.setProNum(gonggaoTypeVo.getProNum());
		gonggao.setGonggaoTypeList(typeIds);
		List<Gonggao> gonggaoList = gonggaoDaoMapper.getList(gonggao);
		for (GonggaoType gonggaoType : list){
			List<Gonggao> gonggaos = gonggaoList.stream().filter(gg -> gonggaoType.getId().equals(gg.getType())).collect(Collectors.toList());
			if(!gonggaos.isEmpty()){
				gonggaoType.setGonggao(gonggaos.get(0));
			}
		}
		logger.info("list返回记录数");
		logger.info(list != null ? list.size() + "":0 + "");
		if(gonggaoList.isEmpty()){
			gonggaoTypeVo.setList(null);
		}else {
			gonggaoTypeVo.setList(list);
		}
		gonggaoTypeVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		return gonggaoTypeVo;
	}

	/**
	 * 查询分类
	 * @param request
	 * @return
	 */
	@RequestMapping("/gonggao/queryType.do")
	@ResponseBody
	public GonggaoTypeVo queryVisitInfos(HttpServletRequest request, @RequestBody GonggaoTypeVo gonggaoTypeVo) {
		PageHelper.offsetPage((gonggaoTypeVo.getPageNum()-1) * gonggaoTypeVo.getPageSize(),gonggaoTypeVo.getPageSize());
		List<GonggaoType> list = gonggaoTypeDaoMapper.getList(new GonggaoType());
		List<String> typeIds = new ArrayList<>();
		for(GonggaoType gonggaoType : list){
			typeIds.add(gonggaoType.getId());
		}
		Gonggao gonggao = new Gonggao();
		gonggao.setProNum(gonggaoTypeVo.getProNum());
		gonggao.setGonggaoTypeList(typeIds);
		List<Gonggao> gonggaoList = gonggaoDaoMapper.getList(gonggao);
		for (GonggaoType gonggaoType : list){
			List<Gonggao> gonggaos = gonggaoList.stream().filter(gg -> gonggaoType.getId().equals(gg.getType())).collect(Collectors.toList());
			if(!gonggaos.isEmpty()){
				gonggaoType.setGonggao(gonggaos.get(0));
			}
		}
		PageInfo<GonggaoType> pageInfo = new PageInfo<>(list);
		int pageNumTotal = (int) Math.ceil((double)pageInfo.getTotal()/(double)gonggaoTypeVo.getPageSize());
		list = pageInfo.getList();
		logger.info("list返回记录数");
		logger.info(list != null ? list.size() + "":0 + "");
		gonggaoTypeVo.setPages(pageNumTotal);
		gonggaoTypeVo.setTotalNum((int) pageInfo.getTotal());
		gonggaoTypeVo.setPageSize(gonggaoTypeVo.getPageSize());
		gonggaoTypeVo.setList(list);
		gonggaoTypeVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		return gonggaoTypeVo;
	}

	/**
	 * 查询公告及分类
	 * @param request
	 * @return
	 */
	@RequestMapping("/gonggao/queryTypeGonggao.do")
	@ResponseBody
	public GonggaoVo queryTypeGonggao(HttpServletRequest request, @RequestBody GonggaoVo gonggaoVo) {
		PageHelper.offsetPage((gonggaoVo.getPageNum()-1) * gonggaoVo.getPageSize(),gonggaoVo.getPageSize());
		Gonggao gonggao = new Gonggao();
		gonggao.setType(gonggaoVo.getTypeId());
		gonggao.setProNum(gonggaoVo.getProNum());
		List<Gonggao> list = gonggaoDaoMapper.getList(gonggao);
		PageInfo<Gonggao> pageInfo = new PageInfo<>(list);
		int pageNumTotal = (int) Math.ceil((double)pageInfo.getTotal()/(double)gonggaoVo.getPageSize());
		list = pageInfo.getList();
		logger.info("list返回记录数");
		logger.info(list != null ? list.size() + "":0 + "");
		gonggaoVo.setPages(pageNumTotal);
		gonggaoVo.setTotalNum((int) pageInfo.getTotal());
		gonggaoVo.setPageSize(gonggaoVo.getPageSize());
		gonggaoVo.setGonggaoList(list);
		gonggaoVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		return gonggaoVo;
	}

	/**
	 * 查询公告内容
	 * @return
	 */
	@RequestMapping("/gonggao/queryGonggaoContent.do")
	@ResponseBody
	public GonggaoVo queryGonggaoContent(@RequestBody GonggaoVo gonggaoVo) throws IOException {
		Gonggao gonggao = gonggaoDaoMapper.getById(gonggaoVo.getId());
		gonggaoVo.setGonggao(gonggao);
		Path path = Paths.get(gonggao.getFilePath());
		List<String> lines = Files.readAllLines(path);
		String content = lines.toString();
		content = content.replace("[","");
		content = content.replace("]","");
		gonggao.setContent(content);
		gonggaoVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		return gonggaoVo;
	}
}
