package me.xiaozhangup.crab.reflect

class EnumNotFoundException(val enumName: String) : Exception("Enum not found: $enumName")
