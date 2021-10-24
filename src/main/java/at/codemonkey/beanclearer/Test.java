package at.codemonkey.beanclearer;

import lombok.SneakyThrows;
import lombok.Value;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class Test {

    public static void main(String[] args) {
        A a = new A();
        System.out.println(JsonMapper.toJson(a));
        clearEmptyBeans(a, new HashSet<>());
        System.out.println(JsonMapper.toJson(a));
    }

    @SneakyThrows
    private static boolean clearEmptyBeans(Object bean, Set<Object> visitedObjects) {
        if(!visitedObjects.add(bean)) { // prevent infinite loops
            return false;
        }
        for (PropertyDescriptor propertyDescriptor : BeanUtils.getPropertyDescriptors(bean.getClass())) {
            if (propertyDescriptor.getPropertyType().equals(Class.class)) {
                continue;
            }
            Object attribute = propertyDescriptor.getReadMethod().invoke(bean);
            if (attribute != null) {
                if (attribute instanceof Iterable) {
                    Iterable<?> iterable = (Iterable<?>) attribute;
                    Iterator<?> iterator = iterable.iterator();
                    while (iterator.hasNext()) {
                        if (clearEmptyBeans(iterator.next(), visitedObjects)) {
                            iterator.remove();
                        }
                    }
                    return false;
                } else {
                    if (BeanUtils.isSimpleProperty(attribute.getClass())) {
                        return false;
                    }
                    if (clearEmptyBeans(attribute, visitedObjects)) {
                        setToNull(bean, propertyDescriptor);
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static void setToNull(Object bean, PropertyDescriptor propertyDescriptor) throws InvocationTargetException, IllegalAccessException {
        if (propertyDescriptor.getWriteMethod() != null) {
            propertyDescriptor.getWriteMethod().invoke(bean, (Object) null);
        } else {
            Field field = requireNonNull(ReflectionUtils.findField(bean.getClass(), propertyDescriptor.getName()));
            ReflectionUtils.makeAccessible(field);
            ReflectionUtils.setField(field, bean, null);
        }
    }


    @Value
    public static class A {
        String x = "a";
        B b = new B();

        List<B> bs = new ArrayList<>();

        {
            bs.add(new B());
        }

    }

    @Value
    static class B {
        String y = null;
        String z = null;
    }

}
