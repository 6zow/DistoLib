package ru.shizow.proxy.transform;

import ru.shizow.proxy.InjectionProvider;
import ru.shizow.proxy.InvocationDelegate;
import ru.shizow.proxy.MethodProxy;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

public class ProxyAdapterTest {

    @Test
    public void test() throws Exception {
        MethodProxy.setInjectionProvider(new InjectionProvider() {
            @Override
            public Object getResource(Annotation[] annotations) {
                return "injected resource";
            }
        });
        MethodProxy.setInvocationDelegate(new InvocationDelegate() {
            @Override
            public Object invoke(Object target, String methodName, String descriptor, Object[] params) {
                System.out.println("Invoke: " + methodName + Arrays.toString(params));
                return MethodProxy.invoke(target, methodName, descriptor, params);
            }
        });
        URL resource = this.getClass().getClassLoader().getResource(
                this.getClass().getCanonicalName().replace('.', '/') + ".class");
        resource = new URL(resource.toString().substring(0,
                resource.toString().length() - this.getClass().getCanonicalName().length() - ".class".length()));
        final String auxTestClassName = this.getClass().getCanonicalName() + "$AuxTest";
        TransformingUrlClassLoader loader = new TransformingUrlClassLoader(new URL[]{resource},
                new Class<?>[]{Proxy.class}, new Class<?>[]{Resource.class}) {

            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                // First, check if the class has already been loaded
                Class c = findLoadedClass(name);
                if (c == null) {
                    if (!name.equals(auxTestClassName)) {
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
        Class<?> aClass = loader.loadClass(auxTestClassName);
        Object o = aClass.newInstance();
        aClass.getMethod("test").invoke(o);
//        new AuxTest().test();
    }

    public static class AuxTest {

        public void test() {
            empty();
            Assert.assertTrue(z(false));
            Assert.assertEquals(3, b((byte) 2));
            Assert.assertEquals(3, s((short) 2));
            Assert.assertEquals(3, i(2));
            Assert.assertEquals(3, l(2));
            Assert.assertEquals(3, f(2), 0);
            Assert.assertEquals(3, d(2), 0);
            Assert.assertEquals("1!", string("1"));
            Object[] a = new Object[]{"x", 2};
            Assert.assertSame(a, oArr(a));
            double[] da = new double[]{1, 2};
            Assert.assertSame(da, dArr(da));
            double[][] dda = new double[2][3];
            Assert.assertSame(dda, ddArr(dda));
            Assert.assertEquals(12, chain1(1));
            Assert.assertEquals("injected resource", resource);
        }

        @Resource
        private String resource;

        @Proxy
        public void empty() {
        }

        @Proxy
        public boolean z(boolean b) {
            return !b;
        }

        @Proxy
        public byte b(byte b) {
            return (byte) (b + 1);
        }

        @Proxy
        public short s(short s) {
            return (short) (s + 1);
        }

        @Proxy
        public int i(int i) {
            return i + 1;
        }

        @Proxy
        public long l(long l) {
            return l + 1;
        }

        @Proxy
        public float f(float f) {
            return f + 1;
        }

        @Proxy
        public double d(double d) {
            return d + 1;
        }

        @Proxy
        public String string(String s) {
            return s + "!";
        }

        @Proxy
        public Object[] oArr(Object[] a) {
            return a;
        }

        @Proxy
        public double[] dArr(double[] a) {
            return a;
        }

        @Proxy
        public double[][] ddArr(double[][] a) {
            return a;
        }

        @Proxy
        public int chain1(int x) {
            return chain2(x + 10);
        }

        @Proxy
        public int chain2(int x) {
            return x + 1;
        }

        int partitions;
        int partition;

        @DistributedProxy(reduceMethod = "sumInts")
        public int sumPrimes(int max) {
            int ans = 0;
            for (int i = 2 + partition; i <= max; i += partitions) {
                if (isPrime(i)) {
                    ans += i;
                }
            }
            return ans;
        }

        @DistributedProxy(reduceMethod = "andBools")
        private boolean isPrime(int n) {
            int max = (int) Math.sqrt(n + 1);
            for (int i = 2 + partition; i <= max; i += partitions) {
                if (n % i == 0) {
                    return false;
                }
            }
            return true;
        }

        public int sumInts(Collection<Integer> elems) {
            int ans = 0;
            for (int i : elems) {
                ans += i;
            }
            return ans;
        }

        public boolean andBools(Collection<Boolean> elems) {
            for (boolean b : elems) {
                if (!b) {
                    return false;
                }
            }
            return true;
        }
    }
}

@interface Proxy {
}

@interface DistributedProxy {
    public String reduceMethod();// default "";
}

@interface Resource {
}
