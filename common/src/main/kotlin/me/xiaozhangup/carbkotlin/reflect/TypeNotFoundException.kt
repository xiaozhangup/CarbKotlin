package me.xiaozhangup.carbkotlin.reflect

class TypeNotFoundException(val typeName: String) : Exception("Type not found: $typeName")
