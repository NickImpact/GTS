package net.impactdev.gts.api.listings.makeup;

import net.impactdev.gts.api.listings.prices.Price;
import net.impactdev.impactor.api.builders.Builder;
import net.impactdev.impactor.api.utilities.Time;
import net.impactdev.impactor.api.utilities.mappings.Tuple;

public class Fees {

    private final Tuple<Price<?, ?, ?>, Boolean> price;
    private final Tuple<Time, Double> time;

    public Fees(FeeBuilder builder) {
        this.price = builder.price;
        this.time = builder.time;
    }

    public Tuple<Price<?, ?, ?>, Boolean> getPrice() {
        return this.price;
    }

    public Tuple<Time, Double> getTime() {
        return this.time;
    }

    public double getTotal() {
        return this.price.getFirst().calculateFee(this.price.getSecond()) + this.time.getSecond();
    }

    public static FeeBuilder builder() {
        return new FeeBuilder();
    }

    public static class FeeBuilder implements Builder<Fees> {

        private Tuple<Price<?, ?, ?>, Boolean> price;
        private Tuple<Time, Double> time;

        public FeeBuilder price(Price<?, ?, ?> value, boolean type) {
            this.price = new Tuple<>(value, type);
            return this;
        }

        public FeeBuilder time(Time time, double value) {
            this.time = new Tuple<>(time, value);
            return this;
        }

//        @Override
//        public FeeBuilder from(Fees fees) {
//            this.price = fees.price;
//            this.time = fees.time;
//            return this;
//        }

        @Override
        public Fees build() {
            return new Fees(this);
        }
    }
}
