package ru.shizow.proxy.transform;

import ru.shizow.proxy.InjectionProvider;
import ru.shizow.proxy.InvocationDelegate;
import ru.shizow.proxy.MethodProxy;
import ru.shizow.proxy.test.TestHelper;

import java.lang.annotation.Annotation;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Max Gorbunov
 */
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
        TestHelper.getTest(AuxTest.class, new Class[0], new Class<?>[]{Proxy.class}, new Class<?>[]{Resource.class})
                .test();
    }

    public static class AuxTest implements TestHelper.TestInterface {

        @Override
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
    }
}

@interface Proxy {
}

@interface Resource {
}
