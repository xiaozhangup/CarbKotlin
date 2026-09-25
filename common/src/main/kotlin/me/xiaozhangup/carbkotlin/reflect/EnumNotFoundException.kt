package me.xiaozhangup.carbkotlin.reflect

class EnumNotFoundException(val enumName: String) : Exception("Enum not found: $enumName")
