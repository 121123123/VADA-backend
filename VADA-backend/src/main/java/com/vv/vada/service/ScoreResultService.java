package com.vv.vada.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.vada.model.dto.scoreResult.ScoreResultQueryRequest;
import com.vv.vada.model.entity.ScoreResult;
import com.vv.vada.model.vo.ScoreResultVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 评分结果服务
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
public interface ScoreResultService extends IService<ScoreResult> {

    /**
     * 校验数据
     *
     * @param scoreResult
     * @param add 对创建的数据进行校验
     */
    void validScoreResult(ScoreResult scoreResult, boolean add);

    /**
     * 获取查询条件
     *
     * @param scoreResultQueryRequest
     * @return
     */
    QueryWrapper<ScoreResult> getQueryWrapper(ScoreResultQueryRequest scoreResultQueryRequest);
    
    /**
     * 获取评分结果封装
     *
     * @param scoreResult
     * @param request
     * @return
     */
    ScoreResultVO getScoreResultVO(ScoreResult scoreResult, HttpServletRequest request);

    /**
     * 分页获取评分结果封装
     *
     * @param scoreResultPage
     * @param request
     * @return
     */
    Page<ScoreResultVO> getScoreResultVOPage(Page<ScoreResult> scoreResultPage, HttpServletRequest request);
}
