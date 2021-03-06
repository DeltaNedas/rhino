package rhino;

import java.io.*;
import java.lang.ref.*;
import java.lang.reflect.*;
import java.net.*;
import java.security.*;
import java.util.*;

/**
 * @author Attila Szegedi
 */
public abstract class SecureCaller{
    private static final byte[] secureCallerImplBytecode = loadBytecode();

    // We're storing a CodeSource -> (ClassLoader -> SecureRenderer), since we
    // need to have one renderer per class loader. We're using weak hash maps
    // and soft references all the way, since we don't want to interfere with
    // cleanup of either CodeSource or ClassLoader objects.
    private static final Map<CodeSource, Map<ClassLoader, SoftReference<SecureCaller>>>
    callers =
    new WeakHashMap<>();

    public abstract Object call(Callable callable, Context cx,
                                Scriptable scope, Scriptable thisObj, Object[] args);

    /**
     * Call the specified callable using a protection domain belonging to the
     * specified code source.
     */
    static Object callSecurely(final CodeSource codeSource, Callable callable,
                               Context cx, Scriptable scope, Scriptable thisObj, Object[] args){
        final Thread thread = Thread.currentThread();
        // Run in doPrivileged as we might be checked for "getClassLoader"
        // runtime permission
        final ClassLoader classLoader = (ClassLoader)AccessController.doPrivileged(
        (PrivilegedAction<Object>)() -> thread.getContextClassLoader());
        Map<ClassLoader, SoftReference<SecureCaller>> classLoaderMap;
        synchronized(callers){
            classLoaderMap = callers.get(codeSource);
            if(classLoaderMap == null){
                classLoaderMap = new WeakHashMap<>();
                callers.put(codeSource, classLoaderMap);
            }
        }
        SecureCaller caller;
        synchronized(classLoaderMap){
            SoftReference<SecureCaller> ref = classLoaderMap.get(classLoader);
            if(ref != null){
                caller = ref.get();
            }else{
                caller = null;
            }
            if(caller == null){
                try{
                    // Run in doPrivileged as we'll be checked for
                    // "createClassLoader" runtime permission
                    caller = (SecureCaller)AccessController.doPrivileged(
                    new PrivilegedExceptionAction<Object>(){
                        @Override
                        public Object run() throws Exception{
                            ClassLoader effectiveClassLoader;
                            Class<?> thisClass = getClass();
                            if(classLoader.loadClass(thisClass.getName()) != thisClass){
                                effectiveClassLoader = thisClass.getClassLoader();
                            }else{
                                effectiveClassLoader = classLoader;
                            }
                            SecureClassLoaderImpl secCl =
                            new SecureClassLoaderImpl(effectiveClassLoader);
                            Class<?> c = secCl.defineAndLinkClass(
                            SecureCaller.class.getName() + "Impl",
                            secureCallerImplBytecode, codeSource);
                            return c.newInstance();
                        }
                    });
                    classLoaderMap.put(classLoader, new SoftReference<>(caller));
                }catch(PrivilegedActionException ex){
                    throw new UndeclaredThrowableException(ex.getCause());
                }
            }
        }
        return caller.call(callable, cx, scope, thisObj, args);
    }

    private static class SecureClassLoaderImpl extends SecureClassLoader{
        SecureClassLoaderImpl(ClassLoader parent){
            super(parent);
        }

        Class<?> defineAndLinkClass(String name, byte[] bytes, CodeSource cs){
            Class<?> cl = defineClass(name, bytes, 0, bytes.length, cs);
            resolveClass(cl);
            return cl;
        }
    }

    private static byte[] loadBytecode(){
        return (byte[])AccessController.doPrivileged((PrivilegedAction<Object>)() -> loadBytecodePrivileged());
    }

    private static byte[] loadBytecodePrivileged(){
        URL url = SecureCaller.class.getResource("SecureCallerImpl.clazz");
        try{
            InputStream in = url.openStream();
            try{
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                while(true){
                    int r = in.read();
                    if(r == -1){
                        return bout.toByteArray();
                    }
                    bout.write(r);
                }
            }finally{
                in.close();
            }
        }catch(IOException e){
            throw new UndeclaredThrowableException(e);
        }
    }
}
