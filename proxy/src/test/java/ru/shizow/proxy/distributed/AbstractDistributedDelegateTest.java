package ru.shizow.proxy.distributed;

import ru.shizow.proxy.MethodProxy;
import ru.shizow.proxy.test.TestHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

public class AbstractDistributedDelegateTest {
    @Test
    public void test() throws Exception {
//        MethodProxy.setInjectionProvider(new InjectionProvider() {
//            @Override
//            public Object getResource(Annotation[] annotations) {
//                return "injected resource";
//            }
//        });
        TestHelper.getTest(AuxTest.class, new Class[]{AuxTest.MyAbstractDistributedDelegate.class},
                new Class[]{DistributedMethod.class}, new Class[]{})
                .test();
    }

    public static class AuxTest implements TestHelper.TestInterface {

        @Override
        public void test() {
            MethodProxy.setInvocationDelegate(new MyAbstractDistributedDelegate());
            Assert.assertEquals(17, sumPrimes(10));
        }

        int partitions;
        int partition;

        @DistributedMethod(aggregatorMethod = "sumInts")
        public int sumPrimes(int max) {
            System.out.println("sumPrimes(" + max + ")");
            int ans = 0;
            for (int i = 2 + partition; i <= max; i += partitions) {
                if (isPrime(i)) {
                    System.out.println(i + " is prime");
                    ans += i;
                }
            }
            return ans;
        }

        @DistributedMethod(aggregatorClass = AndBools.class)
        private boolean isPrime(int n) {
            System.out.println("isPrime(" + n + ")");
            int max = (int) Math.sqrt(n + 0.1);
            for (int i = 2 + partition; i <= max; i += partitions) {
                System.out.println("Check " + n + " % " + i);
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

        //        public boolean andBools(Collection<Boolean> elems) {
//            for (boolean b : elems) {
//                if (!b) {
//                    return false;
//                }
//            }
//            return true;
//        }
        private static class AndBools implements DistributedAggregator<Boolean> {
            @Override
            public Boolean aggregate(Collection<Boolean> booleans) {
                for (boolean b : booleans) {
                    if (!b) {
                        return false;
                    }
                }
                return true;
            }
        }

        private static class MyAbstractDistributedDelegate extends AbstractDistributedDelegate {

            @Override
            public Collection<?> multiInvoke(Object target, String methodName, String descriptor, Object[] params) {
                System.out.println("multiInvoke(" + target + ", " + methodName + ", " + descriptor + ", "
                        + Arrays.toString(params));
                ArrayList<Object> list = new ArrayList<Object>();
                int parts = 3;
                for (int i = 0; i < parts; i++) {
                    AuxTest auxTest = new AuxTest();
                    auxTest.partitions = parts;
                    auxTest.partition = i;
                    list.add(MethodProxy.invoke(auxTest, methodName, descriptor, params));
                }
                return list;
            }
        }
    }
}
