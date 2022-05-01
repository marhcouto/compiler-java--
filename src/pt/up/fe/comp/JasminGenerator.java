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
        String jasminHeader = this.constructJasminHeader(ollirResult);
        String jasminFields = this.constructJasminFields(ollirClass.getFields());
        String jasminMethods = this.constructJasminMethods(ollirClass.getMethods());

        //(String className, String jasminCode, List< Report > reports, Map<String, String> config)

        System.out.println(jasminHeader + "\n" +jasminFields + "\n" + jasminMethods);


        var jasminResult = new JasminResult(jasminHeader + "\n" + jasminFields + "\n" + jasminMethods + "\n");

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

    //TODO 1 - go through all fields
    public String constructJasminFields(ArrayList<Field> fields){



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
    public String constructJasminMethods(ArrayList<Method> methods ){

        //.method <access-spec> <method-name><method-signature>
        //<statements>
        //.end method
        String methodStr = "";
        System.out.println("Printing "+ methods.size() + " Methods");

        for (Method method : methods)
        {
            methodStr += ".method ";
            System.out.println(method.getMethodName());
            //TODO - constructStatements();
            String accessSpec = method.getMethodAccessModifier().name();

            if(method.isStaticMethod())
                methodStr += "static ";

            if(method.isFinalMethod())
                methodStr += "final ";

            methodStr += method.getMethodName() + "\n";
            String x = method.getInstructions().toString();
            System.out.println(x);
            methodStr += ".end method";
            break;
        }


        System.out.println("here: " + methodStr);
        return methodStr;
    }


    public String constructJasminHeader(OllirResult ollirResult)
    {
        String className = ollirResult.getOllirClass().getClassName();
        String packageName = ollirResult.getOllirClass().getPackage();
        String classType = ollirResult.getOllirClass().getClassAccessModifier().name();
        System.out.println(classType);
        String superClassName = ollirResult.getOllirClass().getSuperClass();
        String classDirective = ".class ";

        if(classType != "DEFAULT")
            classDirective += classType;
        if(ollirResult.getOllirClass().isStaticClass())
            classDirective += "static ";
        if(ollirResult.getOllirClass().isFinalClass())
            classDirective += "final ";

        if(packageName != null)
        {
            classDirective += packageName + "/";
        }
        classDirective += className + "\n";

        String superNameJasmin = ".super ";

        //Assume-se isto?
        if(superClassName == null )
        {
            superNameJasmin += "java/lang/Object" + "\n";

        }
        else
            superNameJasmin += superClassName + "\n";

        return classDirective + superNameJasmin;
    }
}