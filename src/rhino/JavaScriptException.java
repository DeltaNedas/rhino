// API class

package rhino;

/**
 * Java reflection of JavaScript exceptions.
 * Instances of this class are thrown by the JavaScript 'throw' keyword.
 * @author Mike McCabe
 */
public class JavaScriptException extends RhinoException{
    /**
     * @deprecated Use {@link WrappedException#WrappedException(Throwable)} to report
     * exceptions in Java code.
     */
    @Deprecated
    public JavaScriptException(Object value){
        this(value, "", 0);
    }

    /**
     * Create a JavaScript exception wrapping the given JavaScript value
     * @param value the JavaScript value thrown.
     */
    public JavaScriptException(Object value, String sourceName, int lineNumber){
        recordErrorOrigin(sourceName, lineNumber, null, 0);
        this.value = value;
        // Fill in fileName and lineNumber automatically when not specified
        // explicitly, see Bugzilla issue #342807
        if(value instanceof NativeError && Context.getContext()
        .hasFeature(Context.FEATURE_LOCATION_INFORMATION_IN_ERROR)){
            NativeError error = (NativeError)value;
            if(!error.has("fileName", error)){
                error.put("fileName", error, sourceName);
            }
            if(!error.has("lineNumber", error)){
                error.put("lineNumber", error, lineNumber);
            }
            // set stack property, see bug #549604
            error.setStackProvider(this);
        }
    }

    @Override
    public String details(){
        if(value == null){
            return "null";
        }else if(value instanceof NativeError){
            return value.toString();
        }
        try{
            return ScriptRuntime.toString(value);
        }catch(RuntimeException rte){
            // ScriptRuntime.toString may throw a RuntimeException
            if(value instanceof Scriptable){
                return ScriptRuntime.defaultObjectToString((Scriptable)value);
            }
            return value.toString();
        }
    }

    /**
     * @return the value wrapped by this exception
     */
    public Object getValue(){
        return value;
    }

    /**
     * @deprecated Use {@link RhinoException#sourceName()} from the super class.
     */
    @Deprecated
    public String getSourceName(){
        return sourceName();
    }

    /**
     * @deprecated Use {@link RhinoException#lineNumber()} from the super class.
     */
    @Deprecated
    public int getLineNumber(){
        return lineNumber();
    }

    private final Object value;
}
