package com.wayne.simplexml.writer;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Created by lenovo on 2018/3/8.
 */

public class XmlSerializerMaker {
    private static final String TAG = "XmlSerializerMaker";
    private XmlSerializer sSerializer;
    private StringWriter sWriter;
    private static XmlSerializerMaker sXmlSerializerMaker;

    private XmlSerializerMaker() {
        sSerializer = Xml.newSerializer();
        //sWriter = new StringWriter();
    }

    public static XmlSerializerMaker getInstance() {
        if(null == sXmlSerializerMaker) {
            sXmlSerializerMaker = new XmlSerializerMaker();
        }
        return sXmlSerializerMaker;
    }

    public XmlSerializer getsSerializer() {
        return sSerializer;
    }

    public void setsSerializer(XmlSerializer sSerializer) {
        this.sSerializer = sSerializer;
    }

    public void startRoot(String root) throws IOException {
        sWriter = new StringWriter();
        sSerializer.setOutput(sWriter);
        sSerializer.startDocument("UTF-8", true);
        sSerializer.startTag("", root);
    }

    public String endRoot(String root) throws IOException {
        sSerializer.endTag("", root);
        sSerializer.endDocument();
        String result = sWriter.toString();
        sWriter.close();
        return result;
    }

    public void startNode(String node) throws IOException {
        sSerializer.startTag("", node);
    }

    public void endNode(String node) throws IOException {
        sSerializer.endTag("", node);
    }

    public void setValue(String value) {
        setText(sSerializer, value);
    }

    public void setAttribute(String key, String value) throws IOException {
        sSerializer.attribute(null, key, value);
    }

    private void setText(XmlSerializer serializer, String value) {
        try {
            if(value == null) {
                value = "";
            }
            serializer.text(value);
        } catch (IOException e) {
            Log.e(TAG, "tag:" + serializer.getName()+" set serializer text failed!!", e);
        }
    }
}
