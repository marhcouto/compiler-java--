package pt.up.fe.comp;

import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;

import java.io.File;

public class JasminGenerator implements JasminBackend {

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {

        ollirResult.getOllirClass().show();
        String jasminHeader = this.constructJasminHeader(ollirResult);
        String jasminFields = this.constructJasminFields(ollirResult);
        String jasminMethods = this.constructJasminMethods(ollirResult);

        //(String className, String jasminCode, List< Report > reports, Map<String, String> config)

        System.out.println(jasminHeader);

        var jasminResult = new JasminResult(jasminHeader);

        System.out.println("1");
        File outputDir = new File("test/fixtures/public/testing");
        System.out.println("2");
        jasminResult.compile(outputDir);
        System.out.println("3");

        return null;
    }

    //TODO
    public String constructJasminFields(OllirResult ollirResult){
        String fields = ollirResult.getOllirClass().getFields().toString();
        System.out.println(fields);
        return "";
    }

    public String constructJasminMethods(OllirResult ollirResult){
        String methods = ollirResult.getOllirClass().getMethods().toString();
        System.out.println(methods);
        return "";
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
