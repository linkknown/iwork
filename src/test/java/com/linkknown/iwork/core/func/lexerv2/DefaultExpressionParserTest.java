package com.linkknown.iwork.core.func.lexerv2;

import com.linkknown.iwork.core.func.lexerv2.DefaultExpressionParser;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

//@RunWith(SpringRunner.class)
@SpringBootTest
public class DefaultExpressionParserTest {

    @Test
    public void testExpressionParser () {
        String[] expressionArr = new String[] {
                "-1;",
                "1;",
                "1+2;",
                "$sql_query_FilterComment.rows;",
                "ifThenElse($Error.isNoError,$Global.SUCCESS,$Global.ERROR);",
                "ifThenElse($Error.isNoError,$Global.SUCCESS,$Global.ERROR;);",
                "ifThenElse($Error.isNoError,$Global.SUCCESS,$Global.ERROR);ifThenElse($Error.isNoError,$Global.SUCCESS,$Global.ERROR);",
                "ifThenElse($Error.isNoError,$Global.SUCCESS,ifThenElse($Error.isNoError,$Global.SUCCESS,$Global.ERROR));",
                "ifThenElse($Error.isNoError,$Global.SUCCESS,ifThenElse($Error.isNoError,ifThenElse($Error.isNoError,$Global.SUCCESS,$Global.ERROR),$Global.ERROR));"
        };

        for (String expression : expressionArr) {
            try {
                System.out.println("【expression】" + expression);
                System.out.println("【result】\n" + new DefaultExpressionParser().parseToTreeNodeString(expression));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}
