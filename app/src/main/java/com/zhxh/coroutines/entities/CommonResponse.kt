package com.zhxh.coroutines.entities

/**
 * Created by zhxh on 2019/05/19
 */
data class CommonBean(
    val name: String
)

data class CommonResult(
    val success: Boolean,
    val data: List<CommonBean>
)