package com.example.smp.controller.test;

import com.alibaba.fastjson.JSON;
import com.example.smp.entity.test.TestEntity;
import com.example.smp.service.test.TestService;
import com.example.smp.utils.SendRequest;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/test")
@CrossOrigin
public class TestController {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TestService testService ;

    @RequestMapping(value = "/get",method = RequestMethod.GET)
    public TestEntity test(@RequestParam Integer id){
        logger.info("-----id:-----"+id);
        return testService.getById(id);
    }

    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public PageInfo<TestEntity> list(@RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                     @RequestParam(value = "pageSize",defaultValue = "5") int pageSize){
        PageHelper.offsetPage((pageNum-1) * pageSize,pageSize);
        List<TestEntity> list = testService.getList();
        logger.info("list:"+ JSON.toJSONString(list));
        PageInfo<TestEntity> pageInfo = new PageInfo<>(list);
        //计算总页数
        int pageNumTotal = (int) Math.ceil((double)pageInfo.getTotal()/(double)pageSize);
        if(pageNum > pageNumTotal){
            PageInfo<TestEntity> entityPageInfo = new PageInfo<>();
            entityPageInfo.setList(new ArrayList<>());
            entityPageInfo.setTotal(pageInfo.getTotal());
            entityPageInfo.setPageNum(pageNum);
            entityPageInfo.setPageSize(pageSize);
            return entityPageInfo;
        }else {
            return pageInfo;
        }
    }

    @RequestMapping(value = "/save",method = RequestMethod.POST)
    public String save(TestEntity testEntity){
        if(testEntity.getId() != null){
            testService.update(testEntity);
            return "修改成功";
        }else{
            testService.save(testEntity);
            return "保存成功";
        }
    }

    @RequestMapping(value = "/delete",method = RequestMethod.GET)
    public String delete(@RequestParam(value = "id") int id){
        testService.delete(id);
        return "删除成功";
    }

    @RequestMapping(value = "/sendRequest",method = RequestMethod.GET)
    public String sendRequest(){
        return SendRequest.post();
    }
}