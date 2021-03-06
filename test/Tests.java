import org.junit.*;
import rhino.*;

import static org.junit.Assert.*;

public class Tests{
    Context cx = Context.enter();
    Scriptable scope = new ImporterTopLevel(cx);

    {
        cx.setLanguageVersion(Context.VERSION_ES6);
    }

    @Test
    public void test(){
        //const outside block
        assertThrows(EcmaError.class, () -> {
            eval("{const b = 0;} b");
        });

        //redeclaration of const
        assertThrows(EvaluatorException.class, () -> {
            eval("const a = 5125125; a = 0;");
        });

        //blocks can be messy
        assertThrows(EvaluatorException.class, () -> {
            eval("{ const a = 5125125; a = 0; }");
        });

        assertEquals("123", eval("const gg = 123; gg").toString());

        eval("const someValue = 99");
        eval("(function(){ const someValue = 444; return someValue })();");
        eval("someValue");
        eval("var c = new java.lang.Object().getClass(); new JavaAdapter(c, {})");
    }

    Object eval(String str){
        Object res = cx.evaluateString(scope, str, "testfile", 0, null);
        System.out.println(res);
        return res;
    }
}
