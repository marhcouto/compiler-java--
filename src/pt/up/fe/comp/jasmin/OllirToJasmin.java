package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Element;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.Type;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;

import java.util.ArrayList;

public class OllirToJasmin {

    private final ClassUnit classUnit;

    public OllirToJasmin(ClassUnit classUnit){
        this.classUnit = classUnit;
    }

    public String getCode()
    {
        var code = new StringBuilder();

        classUnit.show();

        code.append(createJasminHeader());
        code.append(createMethods());

        return code.toString();
    }

    public String getFullyQualifiedName(String className){

        if(className == null) return "java/lang/Object";

        for(var importString : classUnit.getImports()){
            var splittedImport = importString.split("\\.");
            String lastName;

            if(splittedImport.length == 0){
                lastName = importString;
            }
            else{
                lastName = splittedImport[splittedImport.length-1];

            }
            if(lastName.equals(className)){
                return importString.replace('.', '/');
            }

        }

        throw new RuntimeException("Could not find import for class " + className);
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

    public String createClassDirective(){

        String classType = classUnit.getClassAccessModifier().name();
        String accessSpecsStr = createAccessSpecsStr(classType, classUnit.isStaticClass(), classUnit.isFinalClass());
        String className = classUnit.getClassName();
        String packageName = classUnit.getPackage();

        if(packageName != null) {
            className = packageName + "/" + className;
        }

        return ".class " + accessSpecsStr + className + '\n';
    }

    public String createSuperDirective(){

        String superClassName = classUnit.getSuperClass();
        String qualifiedSuperClassName = getFullyQualifiedName(superClassName);

        return ".super "  + qualifiedSuperClassName + '\n';
    }

    public String createJasminHeader()
    {
        String classDirective = createClassDirective();
        String superDirective = createSuperDirective();

        return classDirective + superDirective;
    }

    public String createConstructMethod(String superClassName){

        System.out.println("here" + superClassName);
        String constructStr = SpecsIo.getResource("fixtures/public/jasmin/jasminConstructor.template").replace("$<SUPERCLASS_NAME>", superClassName);


        return constructStr;

    }

    public String createMethods( ){

        String methodsStr = "";
        ArrayList<Method> methods = classUnit.getMethods();

        for (Method method : methods)
        {
            String fieldStr = getCode(method);
            methodsStr += fieldStr;

        }

        return methodsStr;
    }

    public String getReturnValueJasmin(Type returnValue)
    {
        switch (returnValue.getTypeOfElement())
        {
            case INT32:
                return "I";
            case BOOLEAN:
                return "Z";
            case ARRAYREF:
                return "[";
            case OBJECTREF:
                return "L" ;
            case CLASS:
                return "[" + classUnit.getClassName() + ";";
            case THIS:
                return getFullyQualifiedName(classUnit.getSuperClass());
            case STRING:
                return "[Ljava/lang/String;" +  returnValue.getClass();
            case VOID:
                return "V";
            default:
                return "";
        }

    }

    public String getCode(Method method){

        var code = new StringBuilder();

        if(method.isConstructMethod())
        {
            code.append(createConstructMethod(getFullyQualifiedName(classUnit.getSuperClass())));
        }
        else
        {
            code.append(".method ");
            String accessSpecs = createAccessSpecsStr(method.getMethodAccessModifier().name(), method.isStaticMethod(), method.isFinalMethod());
            code.append(accessSpecs + method.getMethodName() + '(');
            //code.append(accessSpecs + method.getMethodName() + '\n');
            ArrayList<Element> params = method.getParams();

            for(var param : params) {
                var typeJasmin = getReturnValueJasmin(param.getType());
                param.show();
                System.out.println(param + "->" + typeJasmin);
                code.append(typeJasmin);
            }

            code.append(")\n");


            /*String statements = createStatements(method.getInstructions());
            methodStr += statements;*/

            code.append("\treturn \n");
            code.append(".end method\n");
        }

        return code.toString();

    }


}
