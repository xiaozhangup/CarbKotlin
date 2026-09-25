package me.xiaozhangup.crab.command.internal

internal object ColorTransform {

    // 将一个颜色代码白化
    fun white(colorCode: String, size: Int = 10, level: Int = 8): String {
        return gradient(colorCode, "#ffffff", size)[level]
    }

    // 获取颜色渐变
    fun gradient(cA: String, cB: String, number: Int): List<String> {
        val a = hex2RGB(cA)
        val b = hex2RGB(cB)
        val colors: MutableList<String> = ArrayList()
        for (i in 0 until number) {
            val aR = a[0]
            val aG = a[1]
            val aB = a[2]
            val bR = b[0]
            val bG = b[1]
            val bB = b[2]
            colors.add(
                rgb2Hex(
                    calculateColor(aR, bR, number - 1, i),
                    calculateColor(aG, bG, number - 1, i),
                    calculateColor(aB, bB, number - 1, i)
                )
            )
        }
        return colors
    }

    private fun calculateColor(a: Int, b: Int, step: Int, number: Int): Int {
        return a + (b - a) * number / step
    }

    private fun rgb2Hex(r: Int, g: Int, b: Int): String {
        return String.format("#%02X%02X%02X", r, g, b)
    }

    private fun hex2RGB(hexStr: String): IntArray {
        if (hexStr.length == 7) {
            val rgb = IntArray(3)
            rgb[0] = hexStr.substring(1, 3).toInt(16)
            rgb[1] = hexStr.substring(3, 5).toInt(16)
            rgb[2] = hexStr.substring(5, 7).toInt(16)
            return rgb
        }
        throw Exception()
    }
}