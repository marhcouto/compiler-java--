package pt.up.fe.comp;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.Type;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;

import java.io.File;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
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

        return jasminHeader + "\n" + jasminFields + "\n" + jasminMethods + "\n";
    }

    public String convertFieldTypeToPrimitiveJasmin(Type type){

        //TODO - Add other types
        switch (type.toString())
        {
            case ("INT32"):
                return "I";
        }

        return "";
    }

    public String createField(Field field)
    {
        String fieldStr = ".field ";

        String accModifiers = field.getFieldAccessModifier().name();
        String accessModifiers = createAccessSpecsStr(accModifiers, field.isStaticField(), field.isFinalField());

        fieldStr += accessModifiers + field.getFieldName() + " " ;
        String fieldType = convertFieldTypeToPrimitiveJasmin(field.getFieldType());

        if(field.isInitialized())
            fieldType += "=" + field.getInitialValue();

        return fieldStr + fieldType;
    }

    public String createJasminFields(ArrayList<Field> fields){

        System.out.println("Printing "+ fields.size() + " Fields");
        String fieldsStr = "";

        for (Field field : fields)
        {
            String fieldStr = createField(field);
            fieldsStr += fieldStr;
        }

        fieldsStr += "\n";
        System.out.println(fieldsStr);
        return fieldsStr;
    }

    public String createConstructMethod(){

        String constructStr = "public";


        return constructStr;

    }

    public String getReturnValueJasmin(Type returnValue)
    {
        switch (returnValue.toString())
        {
            case "VOID":
                return "V";
            default:
                return "";
        }

    }

    /*public String getInstrType(InstructionType instructionType)
    {
        switch (instructionType.toString())
        {
            case "CALL":
                return "(";
            default:
                return "";
        }

    }*/

    public String createStatements(ArrayList<Instruction> instructions)
    {
        for (Instruction instruction : instructions) {
            instruction.show();
            InstructionType instrType = instruction.getInstType();
            System.out.println("tipo de instrução" + instruction + "\n");

            String instrStr = "";
            switch (instrType.toString())
            {
                case "CALL":
                    instrStr += "invokespecial(";
                default:
                    instrStr += "";
            }

        }
        return "";
    }


    //TODO - Refactoring
    public String createMethod(Method method) {

        String methodStr = ".method ";
        System.out.println(method.getMethodName());
        String accessSpecs = "";
        //TODO - createStatements();

        Type returnType = method.getReturnType();
        ArrayList<Element> params = method.getParams();


        if(method.isConstructMethod())
        {
            //createConstructMethod();
            accessSpecs = "public ";
            System.out.println("construct method");
            String returnTypeStr = getReturnValueJasmin(returnType);
            //String instructionType = getInstrType(instrType);
            String paramStr = "";

            for (Element param : params)
            {
                paramStr += convertFieldTypeToPrimitiveJasmin(param.getType());
            }


            methodStr += accessSpecs + "<init>(" + paramStr + ")" + returnTypeStr + "\n";

        }
        else
        {
            String accModifiers = method.getMethodAccessModifier().name();
            accessSpecs = createAccessSpecsStr(accModifiers, method.isStaticMethod(), method.isFinalMethod());
            methodStr += accessSpecs + method.getMethodName();
        }



        /*if(method.iscreateMethod())
        {
            methodStr += "<init>()" + method.getReturnType() + "\n";

            methodStr += "aload 0 \n";
            methodStr += "invokenonvirtual " + method.getInstr(0).toString();
            methodStr += "return \n";

        }*/


        //String x = method.getInstructions().toString();

        createStatements(method.getInstructions());

        System.out.println("Labels: " + method.getLabels());

        methodStr += ".end method";


        return methodStr;

    }

    public String createJasminMethods(ArrayList<Method> methods ){

        //.method <access-spec> <method-name><method-signature>
        //<statements>
        //.end method

        String methodsStr = "";
        System.out.println("Printing "+ methods.size() + " Methods");

        for (Method method : methods)
        {
            String fieldStr = createMethod(method);
            methodsStr += fieldStr;
            break;
        }

        System.out.println("here: " + methodsStr);
        return methodsStr;
    }

    public String createAccessSpecsStr(String classType, Boolean isStatic, Boolean isFinal)
    {
        String accessSpecsStr = "";

        if(classType != "DEFAULT")
            accessSpecsStr += classType.toLowerCase() + " ";
        if(isStatic)
            accessSpecsStr += "static ";
        if(isFinal)
            accessSpecsStr += "final ";

        return accessSpecsStr;
    }


    public String createClassDirective(ClassUnit ollirClass){

        String classType = ollirClass.getClassAccessModifier().name();
        String accessSpecsStr = createAccessSpecsStr(classType, ollirClass.isStaticClass(), ollirClass.isFinalClass());
        String className = ollirClass.getClassName();
        String packageName = ollirClass.getPackage();

        if(packageName != null) {
            className = packageName + "/" + className;
        }

        return ".class " + accessSpecsStr + className + '\n';
    }

    public String createSuperDirective(ClassUnit ollirClass){

        String superClassName = ollirClass.getSuperClass();

        //Assume-se isto?
        if(superClassName == null ) {
            superClassName = "java/lang/Object";
        }

        return ".super "  + superClassName + '\n';
    }

    public String createJasminHeader(ClassUnit ollirClass)
    {
        String classDirective = createClassDirective(ollirClass);
        String superDirective = createSuperDirective(ollirClass);

        return classDirective + superDirective;
    }
}
