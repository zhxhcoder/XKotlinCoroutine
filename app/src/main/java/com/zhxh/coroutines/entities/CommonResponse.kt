package com.zhxh.coroutines.entities

/**
 * Created by zhxh on 2019/05/19
 */
data class CommonBean(
    val user: CommonBean
)

data class CommonResult(
    val success: Boolean,
    val data: List<CommonBean>
)