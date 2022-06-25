/*
 * This file is part of Impactor, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2018-2022 NickImpact
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package net.impactdev.gts.common.adventure.processors.gradients;

import com.google.gson.reflect.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.function.Function;

public class NumberBasedGradientProcessor<T extends Number> extends GradientProcessor<T> {

    private final Function<T, Component> translator;
    private final Function<T, Float> factor;
    private final int min;
    private final int max;

    private NumberBasedGradientProcessor(NumberBasedGradientProcessorBuilder<T> builder) {
        super(builder);
        this.translator = builder.translator;
        this.factor = builder.factor;
        this.min = builder.min;
        this.max = builder.max;
    }

    public static <T extends Number> NumberBasedGradientProcessorBuilder<T> builder() {
        return new NumberBasedGradientProcessorBuilder<>();
    }

    @Override
    public Component process(T input) {
        double value = input.doubleValue();
        Component working = translator.apply(input);

        if(value <= this.min) {
            working = working.color(this.colors.getFirst());
        } else if(value >= this.max) {
            working = working.color(this.colors.getLast());
        } else {
            float factor = this.factor.apply(input);

            TextColor first = this.colors.getFirst();
            TextColor last = this.colors.getLast();

            TextColor color = TextColor.color(
                    Math.round(first.red() + factor * (last.red() - first.red())),
                    Math.round(first.green() + factor * (last.green() - first.green())),
                    Math.round(first.blue() + factor * (last.blue() - first.blue()))
            );
            working = working.color(color);
        }

        return working;
    }

    public static class NumberBasedGradientProcessorBuilder<T extends Number> extends GradientProcessorBuilder<NumberBasedGradientProcessor<T>> {

        private Function<T, Component> translator;
        private Function<T, Float> factor;
        private int min;
        private int max;

        public <B extends Number> NumberBasedGradientProcessorBuilder<B> type(TypeToken<B> type) {
            return new NumberBasedGradientProcessorBuilder<>();
        }

        public NumberBasedGradientProcessorBuilder<T> translator(Function<T, Component> translator) {
            this.translator = translator;
            return this;
        }

        public NumberBasedGradientProcessorBuilder<T> factor(Function<T, Float> factor) {
            this.factor = factor;
            return this;
        }

        public NumberBasedGradientProcessorBuilder<T> min(int min) {
            this.min = min;
            return this;
        }

        public NumberBasedGradientProcessorBuilder<T> max(int max) {
            this.max = max;
            return this;
        }

        @Override
        public NumberBasedGradientProcessor<T> build() {
            return new NumberBasedGradientProcessor<>(this);
        }

    }

}
