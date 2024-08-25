package com.vv.vada.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.vada.annotation.AuthCheck;
import com.vv.vada.common.BaseResponse;
import com.vv.vada.common.DeleteRequest;
import com.vv.vada.common.ErrorCode;
import com.vv.vada.common.ResultUtils;
import com.vv.vada.constant.UserConstant;
import com.vv.vada.exception.BusinessException;
import com.vv.vada.exception.ThrowUtils;
import com.vv.vada.model.dto.scoreResult.ScoreResultAddRequest;
import com.vv.vada.model.dto.scoreResult.ScoreResultEditRequest;
import com.vv.vada.model.dto.scoreResult.ScoreResultQueryRequest;
import com.vv.vada.model.dto.scoreResult.ScoreResultUpdateRequest;
import com.vv.vada.model.entity.ScoreResult;
import com.vv.vada.model.entity.User;
import com.vv.vada.model.vo.ScoreResultVO;
import com.vv.vada.service.ScoreResultService;
import com.vv.vada.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 评分结果接口
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@RestController
@RequestMapping("/scoreResult")
@Slf4j
public class ScoreResultController {

    @Resource
    private ScoreResultService scoreResultService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建评分结果
     *
     * @param scoreResultAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addScoreResult(@RequestBody ScoreResultAddRequest scoreResultAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(scoreResultAddRequest == null, ErrorCode.PARAMS_ERROR);
        // todo 在此处将实体类和 DTO 进行转换
        ScoreResult scoreResult = new ScoreResult();
        BeanUtils.copyProperties(scoreResultAddRequest, scoreResult);
        // 数据校验
        scoreResultService.validScoreResult(scoreResult, true);
        // todo 填充默认值
        User loginUser = userService.getLoginUser(request);
        scoreResult.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = scoreResultService.save(scoreResult);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newScoreResultId = scoreResult.getId();
        return ResultUtils.success(newScoreResultId);
    }

    /**
     * 删除评分结果
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteScoreResult(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        ScoreResult oldScoreResult = scoreResultService.getById(id);
        ThrowUtils.throwIf(oldScoreResult == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldScoreResult.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = scoreResultService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新评分结果（仅管理员可用）
     *
     * @param scoreResultUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateScoreResult(@RequestBody ScoreResultUpdateRequest scoreResultUpdateRequest) {
        if (scoreResultUpdateRequest == null || scoreResultUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        ScoreResult scoreResult = new ScoreResult();
        BeanUtils.copyProperties(scoreResultUpdateRequest, scoreResult);
        // 数据校验
        scoreResultService.validScoreResult(scoreResult, false);
        // 判断是否存在
        long id = scoreResultUpdateRequest.getId();
        ScoreResult oldScoreResult = scoreResultService.getById(id);
        ThrowUtils.throwIf(oldScoreResult == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = scoreResultService.updateById(scoreResult);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取评分结果（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<ScoreResultVO> getScoreResultVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        ScoreResult scoreResult = scoreResultService.getById(id);
        ThrowUtils.throwIf(scoreResult == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(scoreResultService.getScoreResultVO(scoreResult, request));
    }

    /**
     * 分页获取评分结果列表（仅管理员可用）
     *
     * @param scoreResultQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ScoreResult>> listScoreResultByPage(@RequestBody ScoreResultQueryRequest scoreResultQueryRequest) {
        long current = scoreResultQueryRequest.getCurrent();
        long size = scoreResultQueryRequest.getPageSize();
        // 查询数据库
        Page<ScoreResult> scoreResultPage = scoreResultService.page(new Page<>(current, size),
                scoreResultService.getQueryWrapper(scoreResultQueryRequest));
        return ResultUtils.success(scoreResultPage);
    }

    /**
     * 分页获取评分结果列表（封装类）
     *
     * @param scoreResultQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<ScoreResultVO>> listScoreResultVOByPage(@RequestBody ScoreResultQueryRequest scoreResultQueryRequest,
                                                               HttpServletRequest request) {
        long current = scoreResultQueryRequest.getCurrent();
        long size = scoreResultQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<ScoreResult> scoreResultPage = scoreResultService.page(new Page<>(current, size),
                scoreResultService.getQueryWrapper(scoreResultQueryRequest));
        // 获取封装类
        return ResultUtils.success(scoreResultService.getScoreResultVOPage(scoreResultPage, request));
    }

    /**
     * 分页获取当前登录用户创建的评分结果列表
     *
     * @param scoreResultQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<ScoreResultVO>> listMyScoreResultVOByPage(@RequestBody ScoreResultQueryRequest scoreResultQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(scoreResultQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        scoreResultQueryRequest.setUserId(loginUser.getId());
        long current = scoreResultQueryRequest.getCurrent();
        long size = scoreResultQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<ScoreResult> scoreResultPage = scoreResultService.page(new Page<>(current, size),
                scoreResultService.getQueryWrapper(scoreResultQueryRequest));
        // 获取封装类
        return ResultUtils.success(scoreResultService.getScoreResultVOPage(scoreResultPage, request));
    }

    /**
     * 编辑评分结果（给用户使用）
     *
     * @param scoreResultEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editScoreResult(@RequestBody ScoreResultEditRequest scoreResultEditRequest, HttpServletRequest request) {
        if (scoreResultEditRequest == null || scoreResultEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        ScoreResult scoreResult = new ScoreResult();
        BeanUtils.copyProperties(scoreResultEditRequest, scoreResult);
        // 数据校验
        scoreResultService.validScoreResult(scoreResult, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = scoreResultEditRequest.getId();
        ScoreResult oldScoreResult = scoreResultService.getById(id);
        ThrowUtils.throwIf(oldScoreResult == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldScoreResult.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = scoreResultService.updateById(scoreResult);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion
}
