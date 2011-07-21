package ru.shizow.proxy.test;

import org.objectweb.asm.Type;
import ru.shizow.proxy.transform.TransformingUrlClassLoader;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;

/**
 * @author Max Gorbunov
 */
public class TestHelper {

    public static TestInterface getTest(Class<? extends TestInterface> testClass, final Class<?>[] modifyClasses,
                                        Class[] proxyMethodAnnotations,
                                        Class[] injectAnnotations)
            throws MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        URL resource = testClass.getClassLoader().getResource(Type.getInternalName(testClass) + ".class");
        resource = new URL(resource.toString().substring(0,
                resource.toString().length() - Type.getInternalName(testClass).length() - ".class".length()));
        final String auxTestClassName = Type.getInternalName(testClass).replace('/', '.');
        final HashSet<String> classesToModify = new HashSet<String>();
        for (Class c : modifyClasses) {
            classesToModify.add(Type.getInternalName(c).replace('/', '.'));
        }
        TransformingUrlClassLoader loader = new TransformingUrlClassLoader(new URL[]{resource},
                proxyMethodAnnotations, injectAnnotations) {

            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                // First, check if the class has already been loaded
                Class c = findLoadedClass(name);
                if (c == null) {
                    if (!name.equals(auxTestClassName) && !classesToModify.contains(name)) {
                        return super.loadClass(name, resolve);
                    }
                    try {
                        c = findClass(name);
                        if (resolve) {
                            resolveClass(c);
                        }
                        return c;
                    } catch (ClassNotFoundException e) {
                        //
                    }
                }
                return super.loadClass(name, resolve);
            }
        };
        return (TestInterface) loader.loadClass(auxTestClassName).newInstance();
    }

    public interface TestInterface {
        void test();
    }
}
