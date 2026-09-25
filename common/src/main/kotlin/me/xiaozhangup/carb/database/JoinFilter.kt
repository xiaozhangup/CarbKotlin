package me.xiaozhangup.carb.database

class JoinFilter : Filter() {

    /** 连接条件 */
    fun on(criteria: Criteria) {
        append(criteria)
    }
}