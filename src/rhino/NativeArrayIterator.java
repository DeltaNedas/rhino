package rhino;

public final class NativeArrayIterator extends ES6Iterator{
    public enum ARRAY_ITERATOR_TYPE{
        ENTRIES,
        KEYS,
        VALUES
    }

    private static final String ITERATOR_TAG = "ArrayIterator";

    private ARRAY_ITERATOR_TYPE type;

    static void init(ScriptableObject scope, boolean sealed){
        ES6Iterator.init(scope, sealed, new NativeArrayIterator(), ITERATOR_TAG);
    }

    /**
     * Only for constructing the prototype object.
     */
    private NativeArrayIterator(){
        super();
    }

    public NativeArrayIterator(Scriptable scope, Scriptable arrayLike, ARRAY_ITERATOR_TYPE type){
        super(scope, ITERATOR_TAG);
        this.index = 0;
        this.arrayLike = arrayLike;
        this.type = type;
    }

    @Override
    public String getClassName(){
        return "Array Iterator";
    }

    @Override
    protected boolean isDone(Context cx, Scriptable scope){
        return index >= NativeArray.getLengthProperty(cx, arrayLike, false);
    }

    @Override
    protected Object nextValue(Context cx, Scriptable scope){
        if(type == ARRAY_ITERATOR_TYPE.KEYS){
            return index++;
        }

        Object value = arrayLike.get(index, arrayLike);
        if(value == ScriptableObject.NOT_FOUND){
            value = Undefined.instance;
        }

        if(type == ARRAY_ITERATOR_TYPE.ENTRIES){
            value = cx.newArray(scope, new Object[]{index, value});
        }

        index++;
        return value;
    }

    @Override
    protected String getTag(){
        return ITERATOR_TAG;
    }

    private Scriptable arrayLike;
    private int index;
}

