# simplexml
SimpleXml序列化\反序列化说明

1.接口
1.1序列化：java bean->xml字符串
SerializeXml.toXml(Object input)
返回序列化后的String
1.2.反序列化：xml字符串->javabean
FromXmlDom.fromXml(String xml,Class class)
返回Object

1.3.Java Bean注解
@Root
	Type类型，作用于class，标注根节点，序列化时读取
@Element
	作用于field，标注子节点
@ElementList
	作用于field，标注同类子节点List
@Value
	作用于field，标注非普通类型子节点中的text值字段
@Attribute
	作用于field，标注非普通类型子节点中的属性值字段
@Order
	作用于field，标注xml的序列化之后字段的顺序，从小到大排列
	
限制与约定
A.@Element：xml子节点名称等于成员变量名
B.@ElementList：xml子节点名称等于List的泛型类型名称
C.javabean类必须有getter、setter方法和无参数构造方法（如果重载了有参数的构造方法，必须显式给出无参构造方法）
D.反序列化时，会跳过根节点的解析

2.示例
2.1 序列化
2.1.1.Element
基础类型(int\double\float\String\Integer\Double\Float)
@Root
public class Request {
    
    @Element
    private int id;
  
    @Element
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
调用：
Request request = new Request();
request.setId(1001);
request.setName("Auth");
request.setDescription("Auth Request");
try {
    String requestXml = SerializeXml.toXml(request);
    System.out.print(0);
} catch (IOException e) {
    e.printStackTrace();
}

输出：
<?xml version="1.0" encoding="UTF-8" standalone="true"?>
<Request>
	<name>Auth</name>
	<id>1001</id>
</Request>

Element自定义类型
public static class Detail {
    @Element
    private int Age;

    @Element
    private String Grade;

    @Element
    private String ClassNo;

    public int getAge() {
        return Age;
    }
    public void setAge(int age) {
        Age = age;
    }
    public String getGrade() {
        return Grade;
    }
    public void setGrade(String grade) {
        Grade = grade;
    }
    public String getClassNo() {
        return ClassNo;
    }
    public void setClassNo(String classNo) {
        ClassNo = classNo;
    }
}

调用：
StudentInfo studentInfo = new StudentInfo();
StudentInfo.Detail detail = new StudentInfo.Detail();
studentInfo.setDetail(detail);

detail.setAge(10);
detail.setGrade("四");
detail.setClassNo("1");

studentInfo.setName("Wayne");

try {
    String xxx = SerializeXml.toXml(studentInfo);
} catch (IOException e) {
    e.printStackTrace();
}
输出：
<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>
<StudentInfo>
<Detail>
<Grade>四</Grade>
<ClassNo>1</ClassNo>
<Age>10</Age>
</Detail>
<Name>Wayne</Name>
</StudentInfo>

2.1.2.ElementList
@Root
public class RequestStudents {

    @Attribute
    private String requestType;

    @ElementList
    List<Student> students;

    public String getRequestType() {
        return requestType;
    }
	
    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }
	
    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public static class Student {

        @Element
        private String name;

        @Element
        private String sex;

        public Student(String name, String sex) {
            this.name = name;
            this.sex = sex;
        }

        public Student() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSex() {
            return sex;
        }

        public void setSex(String sex) {
            this.sex = sex;
        }
    }
}

调用：
RequestStudents studentrequest = new RequestStudents();

List<RequestStudents.Student> students = new LinkedList<>();

RequestStudents.Student student1 = new RequestStudents.Student("Wayne", "Male");
RequestStudents.Student student2 = new RequestStudents.Student("Bruce", "FeMale");

students.add(student1);
students.add(student2);

studentrequest.setRequestType("getstudents");
studentrequest.setStudents(students);

try {
    String b = SerializeXml.toXml(studentrequest);
    System.out.print("");
} catch (IOException e) {
    e.printStackTrace();
}

输出：
<?xml version="1.0" encoding="UTF-8" standalone="true"?>
<RequestStudents requestType="getstudents">
<Student>
	<name>Wayne</name>
	<sex>Male</sex>
</Student>
<Student>
	<name>Bruce</name>
	<sex>FeMale</sex>
</Student>
</RequestStudents>

2.2反序列化
2.2.1基本类型反序列化
<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>
<ResponseMessage>
	<code>200</code>
	<data>
		<message>成功</message>
		<type>SUCCESS</type>
	</data>
</ResponseMessage>

@Root
public class ResponseMessage {

    @Element
    private int code;

    @Element
    private Data data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {

        @Element
        private String message;

        @Element
        private String type;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}

2.2.2元素数组反序列化
<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>
<RequestStudents requestType="getstudents">
<Student>
<name>Wayne</name>
<sex>Male</sex>
</Student>
<Student>
<name>Bruce</name>
<sex>FeMale</sex>
</Student>
</RequestStudents>"
@Root
public class RequestStudents {

    @Attribute
    private String requestType;

    @ElementList
    List<Student> students;

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public static class Student {

        @Element
        private String name;

        @Element
        private String sex;

        public Student(String name, String sex) {
            this.name = name;
            this.sex = sex;
        }

        public Student() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSex() {
            return sex;
        }

        public void setSex(String sex) {
            this.sex = sex;
        }
    }
}

调用：
Object object = FromXmlDom.fromXml(studentsxml, RequestStudents.class);
