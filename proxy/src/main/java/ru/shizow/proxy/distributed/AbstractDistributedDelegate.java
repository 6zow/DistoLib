package ru.shizow.proxy.distributed;

import ru.shizow.proxy.InvocationDelegate;
import ru.shizow.proxy.MethodProxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * @author Max Gorbunov
 */
public abstract class AbstractDistributedDelegate implements InvocationDelegate {

    @Override
    public final Object invoke(final Object target, String methodName, String descriptor, Object[] params) {
        DistributedAggregator<?> aggregator;
        try {
            Method method = MethodProxy.getMethod(target.getClass(), methodName, descriptor);
            DistributedMethod annotation = method.getAnnotation(DistributedMethod.class);
            if (annotation == null) {
                throw new RuntimeException("Annotation is not found on "
                        + target.getClass().getName() + "." + methodName + descriptor);
            }
            Class<? extends DistributedAggregator> aggClazz = annotation.aggregatorClass();
            String aggMethodName = annotation.aggregatorMethod();
            if (aggClazz == DistributedMethod.Null.class && "".equals(aggMethodName)) {
                throw new RuntimeException("Annotation does not contain aggregatorClass or aggregatorMethod");
            }
            if (aggClazz != DistributedMethod.Null.class) {
                if (!"".equals(aggMethodName)) {
                    throw new RuntimeException("Annotation cannot contain both aggregatorClass and aggregatorMethod");
                }
                Constructor<? extends DistributedAggregator> constructor = aggClazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                aggregator = constructor.newInstance();
            } else {
                final Method aggMethod = target.getClass().getMethod(aggMethodName, Collection.class);
                aggregator = new DistributedAggregator() {
                    @Override
                    public Object aggregate(Collection collection) {
                        try {
                            return aggMethod.invoke(target, collection);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // TODO: method is already known, create another version of multiInvoke and MethodProxy#invoke to accept Method
        Collection collection = multiInvoke(target, methodName, descriptor, params);
        //noinspection unchecked
        return aggregator.aggregate(collection);
    }

    public abstract Collection<?> multiInvoke(Object target, String methodName, String descriptor, Object[] params);
}
