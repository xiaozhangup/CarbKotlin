package me.xiaozhangup.carb.reflect

class TypeNotFoundException(val typeName: String) : Exception("Type not found: $typeName")
