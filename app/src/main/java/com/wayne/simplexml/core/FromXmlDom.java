package com.wayne.simplexml.core;

import android.util.Log;


import com.wayne.simplexml.annotations.Attribute;
import com.wayne.simplexml.annotations.Element;
import com.wayne.simplexml.annotations.Value;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by lenovo on 2018/3/12.
 */

public class FromXmlDom {

    private final static String TAG = "FromXmlDom";

    public static Object fromXml(String xml, Class clazz) {
        DocumentBuilderFactory docbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder docb = null;
        InputSource is = new InputSource();
        Object object = null;
        is.setCharacterStream(new StringReader(xml));
        try {
            docb = docbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        try {
            Document doc = docb.parse(is);
            object = clazz.getConstructor().newInstance();
            //解析xml，跳过根节点
            ReadXmlTreeStructure(doc.getChildNodes().item(0).getChildNodes(), object, clazz);
            System.out.print(0);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return object;
    }

    private static void ReadXmlTreeStructure(NodeList nodes, Object object, Class clazz) throws
            NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        //遍历class中，找到类定义中类型为List的成员（field）
        HashMap<String, Field> listMaps = Util.getListTypes(clazz);
        // 遍历所有子节点
        for (int i = 0; i < nodes.getLength(); i++) {
            // 获得字节点名，判断子节点的类型，区分出text类型的node以及element类型的node
            Node item = nodes.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                //设置节点属性值
                setObjectAttributes(item, object, clazz);
                Field f = null;
                try {
                    //获取class中与xml节点名称一样的字段（field）
                    f = clazz.getDeclaredField(item.getNodeName());
                } catch (NoSuchFieldException e) {
                    Log.i(TAG, "NoSuchFeild:" + item.getNodeName());
                    //e.printStackTrace();
                }

                //查找父类是否有这个field,满足sdk response消息
                if (f == null) {
                    try {
                        Class superClass =  clazz.getSuperclass();

                        f = (superClass != null && superClass != Object.class) ? superClass.getDeclaredField(item.getNodeName()) : null;
                    } catch (NoSuchFieldException e) {
                        Log.i(TAG, "Parent NoSuchFeild:" + item.getNodeName());
                    }
                }

                //区分属性对应class字段是否为基础类型（int、double、String等）
                //如果是基础类型，直接设置该字段的值，如果是非基础类型，则新建对象然后设置值
                if (f != null && f.isAnnotationPresent(Element.class)) {
                    if (Util.isBasicType(f.getType())) {
                        Log.i(TAG, "Basic element:" + item.getNodeName());
                        Node firstChild = item.getFirstChild();
                        if(null != firstChild && firstChild instanceof Text) {
                            Util.setter(object, ((Text) firstChild).getData(), f);
                        }
                    } else {
                        Log.i(TAG, "Node element:" + item.getNodeName());
                        Object input = f.getType().getConstructor().newInstance();
                        Util.setter(object, input, f);
                        Node firstChild = item.getFirstChild();
                        if(null != firstChild && firstChild instanceof Text) {
                            setObjectValue(input, ((Text) firstChild).getData());
                        }
                        //递归调用，下一层xml节点
                        ReadXmlTreeStructure(item.getChildNodes(), input, f.getType());
                    }
                }

                //如果xml节点的名称和listMaps中记录的List类型的泛型类型名称一致，则认为是ElementList
                //新建arrayList并赋值
                Field listField = listMaps.get(item.getNodeName());
                if(null != listField) {
                    Object listObject = Util.getter(object, listField.getName());
                    if(null == listObject) {
                        ArrayList list = new ArrayList();
                        Util.setter(object, list, listField);
                        Class itemClass = (Class<?>)((ParameterizedType)listField.getGenericType()).
                                getActualTypeArguments()[0];
                        Object listitemObject = itemClass.getConstructor().newInstance();

                        list.add(listitemObject);
                        setObjectAttributes(item, listitemObject, itemClass);
                        //递归调用，下一层xml节点
                        ReadXmlTreeStructure(item.getChildNodes(), listitemObject, itemClass);
                    } else {
                        Class itemClass = (Class<?>)((ParameterizedType)listField.getGenericType()).
                                getActualTypeArguments()[0];
                        Object listitemObject = itemClass.getConstructor().newInstance();
                        ((List)listObject).add(listitemObject);
                        setObjectAttributes(item, listitemObject, itemClass);
                        //递归调用，下一层xml节点
                        ReadXmlTreeStructure(item.getChildNodes(), listitemObject, itemClass);
                    }
                }
            } else if (item.getNodeType() == Node.TEXT_NODE) { //text类型节点，无子节点，直接赋值
                if(item instanceof Text) {
                    setObjectValue(object, ((Text) item).getData());
                }
            }
        }
    }

    private static void setObjectValue(Object element, String s) {
        for(Field field:element.getClass().getDeclaredFields()) {
            if(field.isAnnotationPresent(Value.class)) {
                Util.setter(element, s, field);
                break;
            }
        }
    }

    private static void setObjectAttributes(Node item, Object listitemObject, Class itemClass) {
        NamedNodeMap attrs = item.getAttributes();
        for(int j = 0; j < attrs.getLength(); j++) {
            String attrname = attrs.item(j).getNodeName();
            String attrvalue = attrs.item(j).getNodeValue();
            Field attrfield = null;
            try {
                attrfield = itemClass.getDeclaredField(attrname);
            } catch (NoSuchFieldException e) {
                Log.i(TAG, "NoSuchFeild:" + attrname);
                //e.printStackTrace();
            }
            if(attrfield != null && attrfield.isAnnotationPresent(Attribute.class)) {
                Util.setter(listitemObject, attrvalue, attrfield);
            }
        }
    }
}
