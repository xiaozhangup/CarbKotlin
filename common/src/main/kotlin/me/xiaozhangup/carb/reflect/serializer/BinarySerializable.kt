package me.xiaozhangup.carb.reflect.serializer

interface BinarySerializable {

    /**
     * 写入到 ByteBuffer
     */
    fun writeTo(writer: BinaryWriter)
}