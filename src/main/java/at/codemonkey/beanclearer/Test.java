package at.codemonkey.beanclearer;

import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Test {

    public static void main(String[] args) {

        A a = new A();
        System.out.println(JsonMapper.toJson(a));
        clearEmptyBeans(a);
        System.out.println(JsonMapper.toJson(a));


    }

    @SneakyThrows
    private static boolean clearEmptyBeans(Object bean) {
//        System.out.println("check " + bean);
        for (PropertyDescriptor propertyDescriptor : BeanUtils.getPropertyDescriptors(bean.getClass())) {
            if(!Iterable.class.isAssignableFrom(propertyDescriptor.getPropertyType()) && propertyDescriptor.getPropertyType().getPackageName().startsWith("java")) {
                continue;
            }
            Object attribute = propertyDescriptor.getReadMethod().invoke(bean);
//            System.out.println("attribute " + attribute);
            if(attribute != null) {
                if(attribute instanceof Iterable) {
                    Iterable<?> iterable = (Iterable<?>) attribute;
                    Iterator<?> iterator = iterable.iterator();
                    while(iterator.hasNext()) {
                        if(clearEmptyBeans(iterator.next())) {
                            iterator.remove();
                        }
                    }
                } else {
                    if (BeanUtils.isSimpleProperty(attribute.getClass())) {
                        return false;
                    }
                    if (clearEmptyBeans(attribute)) {
                        propertyDescriptor.getWriteMethod().invoke(bean, (Object) null);
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    @Data
    public static class A {
        String x = "a";
        B b = new B();

        List<B> bs = new ArrayList<>(); {
            bs.add(new B());
        }

    }

    @Data static class B {
        String y = null;
        String z = null;
    }

}
