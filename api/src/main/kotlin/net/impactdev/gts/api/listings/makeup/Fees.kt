package net.impactdev.gts.api.listings.makeup

import net.impactdev.gts.api.listings.prices.Price
import net.impactdev.impactor.api.utilities.Builder
import net.impactdev.impactor.api.utilities.Time
import net.impactdev.impactor.api.utilities.mappings.Tuple

class Fees(builder: FeeBuilder) {
    val price: Tuple<Price<*, *, *>, Boolean>?
    val time: Tuple<Time, Double>?
    val total: Double
        get() = price!!.first.calculateFee(price.second) + time!!.second

    class FeeBuilder : Builder<Fees, FeeBuilder> {
        var price: Tuple<Price<*, *, *>, Boolean>? = null
        var time: Tuple<Time, Double>? = null
        fun price(value: Price<*, *, *>, type: Boolean): FeeBuilder {
            price = Tuple(value, type)
            return this
        }

        fun time(time: Time, value: Double): FeeBuilder {
            this.time = Tuple(time, value)
            return this
        }

        override fun from(fees: Fees): FeeBuilder {
            price = fees.price
            time = fees.time
            return this
        }

        override fun build(): Fees {
            return Fees(this)
        }
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun builder(): FeeBuilder {
            return FeeBuilder()
        }
    }

    init {
        price = builder.price
        time = builder.time
    }
}