package com.vv.vada.model.dto.postthumb;

import java.io.Serializable;
import lombok.Data;

/**
 * 帖子点赞请求
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 */
@Data
public class PostThumbAddRequest implements Serializable {

    /**
     * vvTest
     * 帖子 id
     */
    private Long postId;

    private int id;

    private static final long serialVersionUID = 1L;
}