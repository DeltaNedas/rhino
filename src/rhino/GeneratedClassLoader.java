// API class

package rhino;

/**
 * Interface to define classes from generated byte code.
 */
public interface GeneratedClassLoader{

    /**
     * Define a new Java class.
     * Classes created via this method should have the same class loader.
     * @param name fully qualified class name
     * @param data class byte code
     * @return new class object
     */
    Class<?> defineClass(String name, byte[] data);

    /**
     * Link the given class.
     * @param cl Class instance returned from the previous call to
     * {@link #defineClass(String, byte[])}
     * @see java.lang.ClassLoader
     */
    void linkClass(Class<?> cl);
}