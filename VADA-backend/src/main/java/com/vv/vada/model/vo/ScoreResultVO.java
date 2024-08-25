package com.vv.vada.model.vo;

import cn.hutool.json.JSONUtil;
import com.vv.vada.model.entity.ScoreResult;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 评分结果视图
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Data
public class ScoreResultVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 标签列表
     */
    private List<String> tagList;

    /**
     * 创建用户信息
     */
    private UserVO user;

    /**
     * 封装类转对象
     *
     * @param scoreResultVO
     * @return
     */
    public static ScoreResult voToObj(ScoreResultVO scoreResultVO) {
        if (scoreResultVO == null) {
            return null;
        }
        ScoreResult scoreResult = new ScoreResult();
        BeanUtils.copyProperties(scoreResultVO, scoreResult);
        List<String> tagList = scoreResultVO.getTagList();
        scoreResult.setTags(JSONUtil.toJsonStr(tagList));
        return scoreResult;
    }

    /**
     * 对象转封装类
     *
     * @param scoreResult
     * @return
     */
    public static ScoreResultVO objToVo(ScoreResult scoreResult) {
        if (scoreResult == null) {
            return null;
        }
        ScoreResultVO scoreResultVO = new ScoreResultVO();
        BeanUtils.copyProperties(scoreResult, scoreResultVO);
        scoreResultVO.setTagList(JSONUtil.toList(scoreResult.getTags(), String.class));
        return scoreResultVO;
    }
}
