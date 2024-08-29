package com.ej.hgj.controller.gonggao;

import com.ej.hgj.constant.Constant;
import com.ej.hgj.controller.base.BaseController;
import com.ej.hgj.dao.gonggao.GonggaoDaoMapper;
import com.ej.hgj.dao.gonggao.GonggaoReadDaoMapper;
import com.ej.hgj.dao.gonggao.GonggaoTypeDaoMapper;
import com.ej.hgj.entity.gonggao.Gonggao;
import com.ej.hgj.entity.gonggao.GonggaoRead;
import com.ej.hgj.entity.gonggao.GonggaoType;
import com.ej.hgj.enums.JiasvBasicRespCode;
import com.ej.hgj.enums.MonsterBasicRespCode;
import com.ej.hgj.utils.DateUtils;
import com.ej.hgj.utils.bill.TimestampGenerator;
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

	@Autowired
	private GonggaoReadDaoMapper gonggaoReadDaoMapper;

	/**
	 * 公告查询-首页
	 */
	@ResponseBody
	@RequestMapping("/gonggao/query.do")
	public GonggaoVo queryGongGao(@RequestBody GonggaoVo gonggaoVo) {
		String proNum = gonggaoVo.getProNum();
		String wxOpenId = gonggaoVo.getWxOpenId();
		Gonggao gonggao = new Gonggao();
		gonggao.setProNum(proNum);
		List<Gonggao> list = gonggaoDaoMapper.getList(gonggao);
		gonggaoVo.setList(list);
		// 查询未读数量
		GonggaoRead gonggaoRead = new GonggaoRead();
		gonggaoRead.setWxOpenId(wxOpenId);
		gonggaoRead.setProNum(proNum);
		gonggaoRead.setReadStatus(1);
		List<GonggaoRead> gonggaoReadList = gonggaoReadDaoMapper.getList(gonggaoRead);
		Integer notReadNum = list.size() - gonggaoReadList.size();
		if(notReadNum < 0 ){
			notReadNum = 0;
		}
		gonggaoVo.setNotReadNum(notReadNum);
		gonggaoVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		return gonggaoVo;
	}

	/**
	 * 公告查询-首页-根据分类
	 * @return
	 */
//	@RequestMapping("/gonggao/query.do")
//	@ResponseBody
//	public GonggaoTypeVo queryGongGao(@RequestBody GonggaoTypeVo gonggaoTypeVo) {
//		List<GonggaoType> list = gonggaoTypeDaoMapper.getList(new GonggaoType());
//		List<String> typeIds = new ArrayList<>();
//		for(GonggaoType gonggaoType : list){
//			typeIds.add(gonggaoType.getId());
//		}
//		Gonggao gonggao = new Gonggao();
//		gonggao.setProNum(gonggaoTypeVo.getProNum());
//		gonggao.setGonggaoTypeList(typeIds);
//		List<Gonggao> gonggaoList = gonggaoDaoMapper.getList(gonggao);
//		for (GonggaoType gonggaoType : list){
//			List<Gonggao> gonggaos = gonggaoList.stream().filter(gg -> gonggaoType.getId().equals(gg.getType())).collect(Collectors.toList());
//			if(!gonggaos.isEmpty()){
//				gonggaoType.setGonggao(gonggaos.get(0));
//			}
//		}
//		logger.info("list返回记录数");
//		logger.info(list != null ? list.size() + "":0 + "");
//		if(gonggaoList.isEmpty()){
//			gonggaoTypeVo.setList(null);
//		}else {
//			gonggaoTypeVo.setList(list);
//		}
//		gonggaoTypeVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
//		return gonggaoTypeVo;
//	}

	/**
	 * 查询分类
	 * @param request
	 * @return
	 */
	@RequestMapping("/gonggao/queryType.do")
	@ResponseBody
	public GonggaoTypeVo queryGongGaoType(HttpServletRequest request, @RequestBody GonggaoTypeVo gonggaoTypeVo) {
		PageHelper.offsetPage((gonggaoTypeVo.getPageNum()-1) * gonggaoTypeVo.getPageSize(),gonggaoTypeVo.getPageSize());
		List<GonggaoType> list = gonggaoTypeDaoMapper.getList(new GonggaoType());
		List<String> typeIds = new ArrayList<>();
		for(GonggaoType gonggaoType : list){
			typeIds.add(gonggaoType.getId());
		}
		// 根据项目号，分类查询公告
		Gonggao gonggao = new Gonggao();
		gonggao.setProNum(gonggaoTypeVo.getProNum());
		gonggao.setGonggaoTypeList(typeIds);
		List<Gonggao> gonggaoList = gonggaoDaoMapper.getList(gonggao);
		// 根据项目号，wxOpenId查询公告已阅读记录
		GonggaoRead gonggaoRead = new GonggaoRead();
		gonggaoRead.setProNum(gonggaoTypeVo.getProNum());
		gonggaoRead.setWxOpenId(gonggaoTypeVo.getWxOpenId());
		gonggaoRead.setReadStatus(1);
		List<GonggaoRead> gonggaoReadList = gonggaoReadDaoMapper.getList(gonggaoRead);

		for (GonggaoType gonggaoType : list){
			// 当前分类下公告数量
			Integer typeGongGaoSize = 0;
			// 当前分类下已读公告数量
			Integer alreadyGongGaoSize = 0;
			// 过滤出当前分类下的所有公告
			List<Gonggao> gonggaos = gonggaoList.stream().filter(gg -> gonggaoType.getId().equals(gg.getType())).collect(Collectors.toList());
			if(!gonggaos.isEmpty()){
				gonggaoType.setGonggao(gonggaos.get(0));
				typeGongGaoSize = gonggaos.size();
			}

			// 过滤出当前分类下已读公告
			List<GonggaoRead> gonggaoReads = gonggaoReadList.stream().filter(gg -> gonggaoType.getId().equals(gg.getTypeId())).collect(Collectors.toList());
			if(!gonggaoReads.isEmpty()){
				alreadyGongGaoSize = gonggaoReads.size();
			}

			// 计算当前分类下用户未读公告数量
			Integer notReadNum = typeGongGaoSize - alreadyGongGaoSize;
			if(notReadNum < 0 ){
				notReadNum = 0;
			}
			gonggaoType.setNotReadNum(notReadNum);
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
	 * @return
	 */
	@RequestMapping("/gonggao/queryTypeGonggao.do")
	@ResponseBody
	public GonggaoVo queryTypeGonggao(@RequestBody GonggaoVo gonggaoVo) {
		// 根据项目号、wxOpenId、公告分类查询当前用户已读公告
		GonggaoRead gonggaoRead = new GonggaoRead();
		gonggaoRead.setProNum(gonggaoVo.getProNum());
		gonggaoRead.setWxOpenId(gonggaoVo.getWxOpenId());
		gonggaoRead.setTypeId(gonggaoVo.getTypeId());
		gonggaoRead.setReadStatus(1);
		List<GonggaoRead> readList = gonggaoReadDaoMapper.getList(gonggaoRead);
		PageHelper.offsetPage((gonggaoVo.getPageNum()-1) * gonggaoVo.getPageSize(),gonggaoVo.getPageSize());
		Gonggao gonggao = new Gonggao();
		gonggao.setType(gonggaoVo.getTypeId());
		gonggao.setProNum(gonggaoVo.getProNum());
		List<Gonggao> list = gonggaoDaoMapper.getList(gonggao);
		// 标记公告是否已读
		for(Gonggao g : list){
			List<GonggaoRead> readListFilter = readList.stream().filter(gr -> g.getId().equals(gr.getGonggaoId())).collect(Collectors.toList());
			if(!readListFilter.isEmpty()){
				g.setReadStatus(1);
			}else {
				g.setReadStatus(0);
			}
		}
		//list.sort(Comparator.comparingInt(Gonggao::getReadStatus));
		PageInfo<Gonggao> pageInfo = new PageInfo<>(list);
		int pageNumTotal = (int) Math.ceil((double)pageInfo.getTotal()/(double)gonggaoVo.getPageSize());
		list = pageInfo.getList();
		logger.info("list返回记录数");
		logger.info(list != null ? list.size() + "":0 + "");
		gonggaoVo.setPages(pageNumTotal);
		gonggaoVo.setTotalNum((int) pageInfo.getTotal());
		gonggaoVo.setPageSize(gonggaoVo.getPageSize());
		gonggaoVo.setList(list);
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

		// 根据项目号、wxOpenId、公告ID查询阅读记录
		GonggaoRead gr = new GonggaoRead();
		gr.setProNum(gonggaoVo.getProNum());
		gr.setWxOpenId(gonggaoVo.getWxOpenId());
		gr.setGonggaoId(gonggaoVo.getId());
		gr.setReadStatus(1);
		List<GonggaoRead> readList = gonggaoReadDaoMapper.getList(gr);
		if(readList.isEmpty()) {
			// 保存阅读记录
			GonggaoRead gonggaoRead = new GonggaoRead();
			gonggaoRead.setId(TimestampGenerator.generateSerialNumber());
			gonggaoRead.setProNum(gonggaoVo.getProNum());
			gonggaoRead.setTypeId(gonggao.getType());
			gonggaoRead.setGonggaoId(gonggaoVo.getId());
			gonggaoRead.setWxOpenId(gonggaoVo.getWxOpenId());
			gonggaoRead.setReadStatus(1);
			gonggaoRead.setCreateTime(new Date());
			gonggaoRead.setUpdateTime(new Date());
			gonggaoRead.setDeleteFlag(Constant.DELETE_FLAG_NOT);
			gonggaoReadDaoMapper.save(gonggaoRead);
		}
		return gonggaoVo;
	}
}
