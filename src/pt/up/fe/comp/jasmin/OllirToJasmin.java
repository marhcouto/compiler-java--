package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.classmap.FunctionClassMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.ArrayList;
import java.util.stream.Collectors;

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

    public String getJasminType(Type type)
    {
        if(type instanceof ArrayType)
        {
            return "[" + getJasminType(((ArrayType) type).getArrayType());
        }
        else if(type instanceof ClassType)
            return "L" + type.getClass() + ";";
        else if(type instanceof Type)
            return getJasminType(type.getTypeOfElement());
        else{
            throw new NotImplementedException(type.getTypeOfElement());
        }

    }

    public String getJasminType(ElementType type)
    {
        System.out.println(type);
        switch (type)
        {
            case INT32:
                return "I";
            case BOOLEAN:
                return "Z";
            /*TODO
            case ARRAYREF:
                //return "[" + ((ArrayType) typeOllir).getTypeOfElement();
                return "[";
            case OBJECTREF:
                //return "L" + getFullyQualifiedName(classUnit.getSuperClass()) + ";";
                return "";
            case CLASS:
                return "[" + classUnit.getClassName() + ";";
            case THIS:
                return getFullyQualifiedName(classUnit.getSuperClass());*/
            case STRING:
                return "Ljava/lang/String;";
            case VOID:
                return "V";
            default:
                throw new NotImplementedException(type);
        }

    }

    public String createMethodBody(Method method){
        var code = new StringBuilder();

        String accessSpecs = createAccessSpecsStr(method.getMethodAccessModifier().name(), method.isStaticMethod(), method.isFinalMethod());
        code.append(accessSpecs + method.getMethodName() + '(');

        var paramsTypes = method.getParams().stream()
                .map(element -> getJasminType(element.getType()))
                .collect(Collectors.joining());
        code.append(paramsTypes).append(")").append(getJasminType(method.getReturnType()) + '\n');

        code.append("\t.limit stack 99\n");
        code.append("\t.limit locals 99\n");

        /*for(var instruction : method.getInstructions())
        {
            code.append(getCode(instruction));
        }*/

        return code.toString();
    }

    public String createNonConstructMethod(Method method){

        var code = new StringBuilder();

        code.append(".method ");
        code.append(createMethodBody(method));
        code.append("\treturn \n");
        code.append(".end method\n");

        return code.toString();
    }

    public String getCode(Method method){

        var code = new StringBuilder();

        if(method.isConstructMethod())
        {
            code.append(createConstructMethod(getFullyQualifiedName(classUnit.getSuperClass())));
        }
        else
        {
            code.append(createNonConstructMethod(method));
        }

        return code.toString();

    }

    public String getCode(Instruction instruction){
        FunctionClassMap<Instruction, String> instructionMap = new FunctionClassMap<>();

        instructionMap.put(CallInstruction.class, this::getCode);
        instructionMap.apply(instruction);

        throw new NotImplementedException(instruction.getInstType());
    }

    private String getCodeInvokeStatic(CallInstruction instruction)
    {
        var code = new StringBuilder();
        var methodClass = ((Operand) instruction.getFirstArg()).getName();
        var methodName = ((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", "");;

        code.append("invokestatic " + getFullyQualifiedName(methodClass));
        code.append("/" + methodName + "(");

        //TODO-Exemplo
        var operandsTypes = instruction.getListOfOperands().stream()
                .map(element -> getJasminType(element.getType()))
                .collect(Collectors.joining());
        code.append(operandsTypes).append(")").append(getJasminType(instruction.getReturnType()) + '\n');

        return code.toString();

    }

    public String getCode(CallInstruction instruction) {


        switch(instruction.getInvocationType()){
            case invokestatic:
                return getCodeInvokeStatic(instruction);
        }


        throw new NotImplementedException(instruction.getInstType());
    }


}
