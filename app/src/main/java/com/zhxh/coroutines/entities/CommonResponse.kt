package com.zhxh.coroutines.entities

/**
 * Created by zhxh on 2019/05/19
 */
data class CommonBean(
    val _id: String,
    val createdAt: String,
    val desc: String,
    val publishedAt: String,
    val source: String,
    val type: String,
    val who: String
)

data class CommonResult(
    val error: Boolean,
    val results: List<CommonBean>
)