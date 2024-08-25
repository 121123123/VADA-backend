package com.vv.vada.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.vada.common.ErrorCode;
import com.vv.vada.constant.CommonConstant;
import com.vv.vada.exception.ThrowUtils;
import com.vv.vada.mapper.ScoreResultMapper;
import com.vv.vada.model.dto.scoreResult.ScoreResultQueryRequest;
import com.vv.vada.model.entity.ScoreResult;
import com.vv.vada.model.entity.ScoreResultFavour;
import com.vv.vada.model.entity.ScoreResultThumb;
import com.vv.vada.model.entity.User;
import com.vv.vada.model.vo.ScoreResultVO;
import com.vv.vada.model.vo.UserVO;
import com.vv.vada.service.ScoreResultService;
import com.vv.vada.service.UserService;
import com.vv.vada.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 评分结果服务实现
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Service
@Slf4j
public class ScoreResultServiceImpl extends ServiceImpl<ScoreResultMapper, ScoreResult> implements ScoreResultService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param scoreResult
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validScoreResult(ScoreResult scoreResult, boolean add) {
        ThrowUtils.throwIf(scoreResult == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = scoreResult.getTitle();
        // 创建数据时，参数不能为空
        if (add) {
            // todo 补充校验规则
            ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR);
        }
        // 修改数据时，有参数则校验
        // todo 补充校验规则
        if (StringUtils.isNotBlank(title)) {
            ThrowUtils.throwIf(title.length() > 80, ErrorCode.PARAMS_ERROR, "标题过长");
        }
    }

    /**
     * 获取查询条件
     *
     * @param scoreResultQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<ScoreResult> getQueryWrapper(ScoreResultQueryRequest scoreResultQueryRequest) {
        QueryWrapper<ScoreResult> queryWrapper = new QueryWrapper<>();
        if (scoreResultQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = scoreResultQueryRequest.getId();
        Long notId = scoreResultQueryRequest.getNotId();
        String title = scoreResultQueryRequest.getTitle();
        String content = scoreResultQueryRequest.getContent();
        String searchText = scoreResultQueryRequest.getSearchText();
        String sortField = scoreResultQueryRequest.getSortField();
        String sortOrder = scoreResultQueryRequest.getSortOrder();
        List<String> tagList = scoreResultQueryRequest.getTags();
        Long userId = scoreResultQueryRequest.getUserId();
        // todo 补充需要的查询条件
        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取评分结果封装
     *
     * @param scoreResult
     * @param request
     * @return
     */
    @Override
    public ScoreResultVO getScoreResultVO(ScoreResult scoreResult, HttpServletRequest request) {
        // 对象转封装类
        ScoreResultVO scoreResultVO = ScoreResultVO.objToVo(scoreResult);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = scoreResult.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        scoreResultVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        long scoreResultId = scoreResult.getId();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<ScoreResultThumb> scoreResultThumbQueryWrapper = new QueryWrapper<>();
            scoreResultThumbQueryWrapper.in("scoreResultId", scoreResultId);
            scoreResultThumbQueryWrapper.eq("userId", loginUser.getId());
            ScoreResultThumb scoreResultThumb = scoreResultThumbMapper.selectOne(scoreResultThumbQueryWrapper);
            scoreResultVO.setHasThumb(scoreResultThumb != null);
            // 获取收藏
            QueryWrapper<ScoreResultFavour> scoreResultFavourQueryWrapper = new QueryWrapper<>();
            scoreResultFavourQueryWrapper.in("scoreResultId", scoreResultId);
            scoreResultFavourQueryWrapper.eq("userId", loginUser.getId());
            ScoreResultFavour scoreResultFavour = scoreResultFavourMapper.selectOne(scoreResultFavourQueryWrapper);
            scoreResultVO.setHasFavour(scoreResultFavour != null);
        }
        // endregion

        return scoreResultVO;
    }

    /**
     * 分页获取评分结果封装
     *
     * @param scoreResultPage
     * @param request
     * @return
     */
    @Override
    public Page<ScoreResultVO> getScoreResultVOPage(Page<ScoreResult> scoreResultPage, HttpServletRequest request) {
        List<ScoreResult> scoreResultList = scoreResultPage.getRecords();
        Page<ScoreResultVO> scoreResultVOPage = new Page<>(scoreResultPage.getCurrent(), scoreResultPage.getSize(), scoreResultPage.getTotal());
        if (CollUtil.isEmpty(scoreResultList)) {
            return scoreResultVOPage;
        }
        // 对象列表 => 封装对象列表
        List<ScoreResultVO> scoreResultVOList = scoreResultList.stream().map(scoreResult -> {
            return ScoreResultVO.objToVo(scoreResult);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = scoreResultList.stream().map(ScoreResult::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> scoreResultIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> scoreResultIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> scoreResultIdSet = scoreResultList.stream().map(ScoreResult::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<ScoreResultThumb> scoreResultThumbQueryWrapper = new QueryWrapper<>();
            scoreResultThumbQueryWrapper.in("scoreResultId", scoreResultIdSet);
            scoreResultThumbQueryWrapper.eq("userId", loginUser.getId());
            List<ScoreResultThumb> scoreResultScoreResultThumbList = scoreResultThumbMapper.selectList(scoreResultThumbQueryWrapper);
            scoreResultScoreResultThumbList.forEach(scoreResultScoreResultThumb -> scoreResultIdHasThumbMap.put(scoreResultScoreResultThumb.getScoreResultId(), true));
            // 获取收藏
            QueryWrapper<ScoreResultFavour> scoreResultFavourQueryWrapper = new QueryWrapper<>();
            scoreResultFavourQueryWrapper.in("scoreResultId", scoreResultIdSet);
            scoreResultFavourQueryWrapper.eq("userId", loginUser.getId());
            List<ScoreResultFavour> scoreResultFavourList = scoreResultFavourMapper.selectList(scoreResultFavourQueryWrapper);
            scoreResultFavourList.forEach(scoreResultFavour -> scoreResultIdHasFavourMap.put(scoreResultFavour.getScoreResultId(), true));
        }
        // 填充信息
        scoreResultVOList.forEach(scoreResultVO -> {
            Long userId = scoreResultVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            scoreResultVO.setUser(userService.getUserVO(user));
            scoreResultVO.setHasThumb(scoreResultIdHasThumbMap.getOrDefault(scoreResultVO.getId(), false));
            scoreResultVO.setHasFavour(scoreResultIdHasFavourMap.getOrDefault(scoreResultVO.getId(), false));
        });
        // endregion

        scoreResultVOPage.setRecords(scoreResultVOList);
        return scoreResultVOPage;
    }

}
