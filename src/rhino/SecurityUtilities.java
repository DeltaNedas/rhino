package rhino;

import java.security.*;

/**
 * @author Attila Szegedi
 */
public class SecurityUtilities{
    /**
     * Retrieves a system property within a privileged block. Use it only when
     * the property is used from within Rhino code and is not passed out of it.
     * @param name the name of the system property
     * @return the value of the system property
     */
    public static String getSystemProperty(final String name){
        return AccessController.doPrivileged(
        (PrivilegedAction<String>)() -> System.getProperty(name));
    }

    public static ProtectionDomain getProtectionDomain(final Class<?> clazz){
        return AccessController.doPrivileged(
        (PrivilegedAction<ProtectionDomain>)() -> clazz.getProtectionDomain());
    }

    /**
     * Look up the top-most element in the current stack representing a
     * script and return its protection domain. This relies on the system-wide
     * SecurityManager being an instance of {@link RhinoSecurityManager},
     * otherwise it returns <code>null</code>.
     * @return The protection of the top-most script in the current stack, or null
     */
    public static ProtectionDomain getScriptProtectionDomain(){
        final SecurityManager securityManager = System.getSecurityManager();
        if(securityManager instanceof RhinoSecurityManager){
            return AccessController.doPrivileged(
            (PrivilegedAction<ProtectionDomain>)() -> {
                Class<?> c = ((RhinoSecurityManager)securityManager)
                .getCurrentScriptClass();
                return c == null ? null : c.getProtectionDomain();
            }
            );
        }
        return null;
    }
}