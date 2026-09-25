package me.xiaozhangup.crab.reflect

class TypeNotFoundException(val typeName: String) : Exception("Type not found: $typeName")
