package net.impactdev.gts.api.util

class WaitingInteger {
    private var fallback: String? = null
    private var result: Int? = null

    constructor(fallback: String?) {
        this.fallback = fallback
    }

    constructor(result: Int) {
        this.result = result
    }

    override fun toString(): String {
        return if (result != null) {
            result.toString()
        } else fallback!!
    }
}