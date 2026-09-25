package me.xiaozhangup.carb.reflect

class EnumNotFoundException(val enumName: String) : Exception("Enum not found: $enumName")
