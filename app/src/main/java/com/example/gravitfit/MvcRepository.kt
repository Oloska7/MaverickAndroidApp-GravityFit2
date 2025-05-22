package com.example.gravitfit

object MvcRepository {
    private val mvcMap = mapOf(
        1 to MvcData(400, 400, 1000, 1200),
        2 to MvcData(1000, 650, 1000, 1200),
        3 to MvcData(600, 500, 1200, 1000),
        4 to MvcData(1000, 1500, 1300, 500),
        5 to MvcData(600, 200, 1200, 1900),
        6 to MvcData(600, 300, 1600, 1400),
        7 to MvcData(410, 750, 750, 900),
        9 to MvcData(750, 300, 750, 450),
        10 to MvcData(380, 200, 620, 400)

    )

    fun getMvcData(athleteId: Int): MvcData? = mvcMap[athleteId]
}
