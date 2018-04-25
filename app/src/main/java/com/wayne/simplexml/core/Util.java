package com.wayne.simplexml.core;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;

/**
 * Created by lenovo on 2018/3/12.
 */

public class Util {

    /**
     * 基础类判断
     * @param targetClazz
     * @return
     */
    public static boolean isBasicType(Class<?> targetClazz) {
        // 判断包装类
        if (Number.class.isAssignableFrom(targetClazz) || targetClazz == String.class) {
            return true;
        }
        // 判断原始类,过滤掉特殊的基本类型
        if (targetClazz == void.class) {
            return false;
        }
        return targetClazz.isPrimitive();
    }

    /**
     * 调用getter方法
     * @param obj
     * @param att
     * @return
     */
    public static Object getter (Object obj, String att){
        try{
            Method met = obj.getClass().getMethod("get" + initStr(att)) ;    // 得到setter方法
            return met.invoke(obj) ;
        }catch(Exception e){
            e.printStackTrace() ;
            return null;
        }
    }

    /**
     * 调用setter方法
     * @param obj
     * @param att
     * @return
     */
    public static boolean setter (Object obj, Object objset, Field att) {
        try{
            Method met = obj.getClass().getMethod("set" + initStr(att.getName()), att.getType()) ;    // 得到setter方法

            if(att.getType().equals(int.class)) {
                met.invoke(obj, Integer.parseInt((String) objset)) ;
            } else if(att.getType().equals(double.class)) {
                met.invoke(obj, Double.parseDouble((String) objset)) ;
            } else if(att.getType().equals(long.class)) {
                met.invoke(obj, Long.parseLong((String) objset)) ;
            } else if(att.getType().equals(float.class)) {
                met.invoke(obj, Float.parseFloat((String) objset)) ;
            } else {
                met.invoke(obj, objset) ;
            }
            return true;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static String initStr(String att) {
        StringBuilder attr = new StringBuilder(att);
        char a = Character.toUpperCase(attr.charAt(0));
        attr.setCharAt(0, a);
        return attr.toString();
    }

    public static HashMap getListTypes(Class clazz) {
        HashMap<String, Field> maps = new HashMap<>();
        for(Field f : clazz.getDeclaredFields()) {
            Class claz = f.getType();
            if(claz == java.util.List.class || claz == java.util.LinkedList.class ||
                    claz == java.util.ArrayList.class){
                // 如果是List类型，得到其Generic的类型
                Type genericType = f.getGenericType();
                if(genericType == null) continue;
                // 如果是泛型参数的类型
                if(genericType instanceof ParameterizedType){
                    ParameterizedType pt = (ParameterizedType) genericType;
                    //得到泛型里的class类型对象
                    Class<?> genericClazz = (Class<?>)pt.getActualTypeArguments()[0];
                    String name = genericClazz.getSimpleName();
                    maps.put(name, f);
                }
            }
            System.out.print(0);
        }

        return maps;
    }
}
