package com.wayne.simplexml.core;

import android.text.TextUtils;
import android.util.Log;

import com.wayne.simplexml.annotations.Attribute;
import com.wayne.simplexml.annotations.Element;
import com.wayne.simplexml.annotations.ElementList;
import com.wayne.simplexml.annotations.Order;
import com.wayne.simplexml.annotations.Root;
import com.wayne.simplexml.annotations.Value;
import com.wayne.simplexml.writer.XmlSerializerMaker;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by lenovo on 2018/3/9.
 */

public class SerializeXml {

    private static XmlSerializerMaker sXmlSerializerMaker;

    private static String root;

    private static StringBuffer mOutputString;

    static {
        sXmlSerializerMaker = XmlSerializerMaker.getInstance();
    }

    public static String toXml(Object input) throws IOException {
        return toXml(input, null);
    }
    /**
     * 从bean生成xml
     * @param input
     * @return
     * @throws IOException
     */
    private static String toXml(Object input, String nodeName) throws IOException {

        Class inputClass = input.getClass();
        Field[] fields = inputClass.getDeclaredFields();
        //fields排序，attribute要在starttag之后立刻调用，因此需要将attribute field排在前面
        for(int i = 0; i < fields.length;i++) {
            for(int j = fields.length - 1; j > i; j--) {
                if(fields[j].getAnnotation(Attribute.class) != null) {
                    Field temp = fields[j - 1];
                    fields[j - 1] = fields[j];
                    fields[j] = temp;
                }
            }
        }

        Arrays.sort(fields, new Comparator<Field>() {
                    @Override
                    public int compare(Field o1, Field o2) {
                        Order or1 = o1.getAnnotation(Order.class);
                        Order or2 = o2.getAnnotation(Order.class);

                        int val1 = (or1 == null) ? 1000 : or1.value();
                        int val2 = (or2 == null) ? 1000 : or2.value();

                        return val1 - val2;
                    }
                });

        //根节点,class的@Root注解，类无此注解则为普通Node
        Annotation rootAnnotion = inputClass.getAnnotation(Root.class);
        if(rootAnnotion != null) {
            root = inputClass.getSimpleName();
            Log.i("Root", root);
            String name = ((Root)rootAnnotion).name();
            if(!TextUtils.isEmpty(name)) {
                sXmlSerializerMaker.startRoot(name);
            } else {
                sXmlSerializerMaker.startRoot(inputClass.getSimpleName());
            }
        } else {
            Log.i("Node", inputClass.getName());
            if(null != nodeName) {
                sXmlSerializerMaker.startNode(nodeName);
            } else {
                sXmlSerializerMaker.startNode(inputClass.getSimpleName());
            }
        }

        for(Field f: fields) {
            String fieldName = f.getName();
            Object fieldObject = Util.getter(input, fieldName);
            if(f.isAnnotationPresent(Element.class)) {
                if(!Util.isBasicType(f.getType())) {
                    Log.i("Element", fieldName);
                    if(null != fieldObject) {
                        //递归调用,分析节点
                        toXml(fieldObject, fieldName);
                    }
                } else {
                    Log.i("Element", fieldName);
                    Object elementValue = Util.getter(input, fieldName);
                    if(f.getAnnotation(Element.class).optional() && elementValue == null) {
                        Log.i("Optional Element", fieldName + "is null");
                    }
                    else {
                        sXmlSerializerMaker.startNode(fieldName);
                        sXmlSerializerMaker.setValue(elementValue != null ? elementValue.toString() : "");
                        sXmlSerializerMaker.endNode(fieldName);
                    }
                }
            }
            if(f.isAnnotationPresent(Value.class)) {
                Log.i("Value", fieldName);
                Object value = Util.getter(input, fieldName);
                sXmlSerializerMaker.setValue(value != null ? value.toString() : "");
            }
            if(f.isAnnotationPresent(ElementList.class)) {
                Log.i("ElementList", fieldName);
                List list = (List) Util.getter(input, f.getName());
                for(Object o:list) {
                    //递归调用,分析list中的节点
                    toXml(o, null);
                }
            }
            if(f.isAnnotationPresent(Attribute.class)) {
                Log.i("Attribute", fieldName);
                Object objectAttribute = Util.getter(input, fieldName);
                sXmlSerializerMaker.setAttribute(fieldName, objectAttribute != null ? objectAttribute.toString() : "");
            }
        }

        if(rootAnnotion != null) {
            String name = ((Root)rootAnnotion).name();
            if(!TextUtils.isEmpty(name)) {
                return sXmlSerializerMaker.endRoot(name);
            } else {
                return sXmlSerializerMaker.endRoot(inputClass.getSimpleName());
            }
        } else {
            if(null != nodeName) {
                sXmlSerializerMaker.endNode(nodeName);
            } else {
                sXmlSerializerMaker.endNode(inputClass.getSimpleName());
            }
        }

        return null;
    }
}
