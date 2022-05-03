package pt.up.fe.comp;

import org.specs.comp.ollir.AccessModifiers;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Field;
import org.specs.comp.ollir.Method;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class JasminGenerator implements JasminBackend {

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {


        ollirResult.getOllirClass().show();

        ClassUnit ollirClass = ollirResult.getOllirClass();
        String jasminStr = createJasminStr(ollirClass);
        showJasmin(jasminStr);

        //(String className, String jasminCode, List< Report > reports, Map<String, String> config)

        var jasminResult = new JasminResult(jasminStr);

        System.out.println("1");
        File outputDir = new File("test/fixtures/public/testing");
        System.out.println("2");
        jasminResult.compile(outputDir);
        System.out.println("3");

        return null;
    }
    //TODO - Criar um ficheiro com superclass
    //TODO - Criar um ficheiro com packages
    //TODO - Criar um ficheiro com class public
    //TODO - Criar um ficheiro com fields com todos os tipos
    //TODO - Criar metodos e testar cada hipotese

    public void showJasmin(String jasminStr)
    {
        System.out.println(jasminStr);
    }

    public String createJasminStr(ClassUnit ollirClass){

        String jasminHeader = this.createJasminHeader(ollirClass);
        String jasminFields = this.createJasminFields(ollirClass.getFields());
        String jasminMethods = this.createJasminMethods(ollirClass.getMethods());

        return jasminHeader + jasminFields + jasminMethods;
    }

    //TODO 1 - go through all fields
    public String createJasminFields(ArrayList<Field> fields){

        System.out.println("Printing "+ fields.size() + " Fields");
        String fieldsStr = "";

        for (Field field : fields)
        {
            fieldsStr += ".field ";
            String accModifiers = field.getFieldAccessModifier().name();

            Boolean isStatic = field.isStaticField();
            Boolean isFinal = field.isFinalField();

            if(accModifiers != "DEFAULT")
            {
                fieldsStr += accModifiers.toLowerCase() + " ";
            }
            if(isStatic)
            {
                fieldsStr += "static ";
            }
            if(isFinal)
            {
                fieldsStr += "final ";
            }

            System.out.println(accModifiers);

            System.out.println(field.getFieldName());

            fieldsStr += field.getFieldName() + " ";
            //TODO - convertFieldTypeToPrimitiveJasmin();
            if(field.getFieldType().toString() == "INT32")
                fieldsStr += "I ";
            if(field.isInitialized())
                fieldsStr += "=" + field.getInitialValue();
        }

        fieldsStr += "\n";
        System.out.println(fieldsStr);
        return fieldsStr;
    }

    //TODO 2 - go through methods
    public String createJasminMethods(ArrayList<Method> methods ){

        //.method <access-spec> <method-name><method-signature>
        //<statements>
        //.end method
        String methodStr = "";
        System.out.println("Printing "+ methods.size() + " Methods");

        for (Method method : methods)
        {
            methodStr += ".method ";
            System.out.println(method.getMethodName());
            //TODO - createStatements();
            String accessSpec = method.getMethodAccessModifier().name();

            if(method.isStaticMethod())
                methodStr += "static ";

            if(method.isFinalMethod())
                methodStr += "final ";

            methodStr += method.getMethodName() + "\n";

            if(method.iscreateMethod())
            {
                methodStr += "<init>()" + method.getReturnType() + "\n";

                methodStr += "aload 0 \n";
                methodStr += "invokenonvirtual " + method.getInstr(0).toString();
                methodStr += "return \n";

            }


            String x = method.getInstructions().toString();
            System.out.println(x);
            methodStr += ".end method";
            break;
        }


        System.out.println("here: " + methodStr);
        return methodStr;
    }

    public String createAccessSpecsStr(ClassUnit ollirClass)
    {
        String classType = ollirClass.getClassAccessModifier().name();
        String accessSpecsStr = "";

        if(classType != "DEFAULT")
            accessSpecsStr += classType;
        if(ollirClass.isStaticClass())
            accessSpecsStr += "static ";
        if(ollirClass.isFinalClass())
            accessSpecsStr += "final ";

        return accessSpecsStr;
    }


    public String createClassDirective(ClassUnit ollirClass){

        String accessSpecsStr = createAccessSpecsStr(ollirClass);
        String className = ollirClass.getClassName();
        String packageName = ollirClass.getPackage();

        if(packageName != null) {
            className = packageName + "/" + className;
        }

        return ".class" + accessSpecsStr + className + '\n';
    }

    public String createSuperDirective(ClassUnit ollirClass){

        String superClassName = ollirClass.getSuperClass();

        //Assume-se isto?
        if(superClassName == null ) {
            superClassName = "java/lang/Object";
        }

        return ".super"  + superClassName + '\n';
    }


    public String createJasminHeader(ClassUnit ollirClass)
    {
        String classDirective = createClassDirective(ollirClass);
        String superDirective = createSuperDirective(ollirClass);

        return classDirective + superDirective;
    }
}
