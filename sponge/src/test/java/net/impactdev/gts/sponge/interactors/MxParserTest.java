package net.impactdev.gts.sponge.interactors;

import org.junit.Test;
import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;
import org.mariuszgromada.math.mxparser.Function;

public class MxParserTest {

    @Test
    public void test() {
        Function test = new Function("f(x) = 5 * (x - 1) + 50");
        Argument x = new Argument("hours", 1);
        Expression expression1 = new Expression("f(x)", test, x);


        Function test2 = new Function("f(hours,minutes) = 5 * (hours - 1 + (minutes > 0)) + 50");
        Argument y = new Argument("minutes", 0);
        Expression expression2 = new Expression("f(hours,minutes)", test2, x, y);
        System.out.println(expression2.getExpressionString() + " = " + expression2.calculate());

        y.setArgumentValue(0.5);
        System.out.println(expression2.getExpressionString() + " = " + expression2.calculate());

        System.out.println(test.checkSyntax());
        System.out.println(x.checkSyntax());
        System.out.println(y.checkSyntax());
        System.out.println(expression2.checkSyntax());
    }

}
