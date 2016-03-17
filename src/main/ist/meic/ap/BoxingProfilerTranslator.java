package ist.meic.ap;

import java.util.ArrayList;

import javassist.*;
import javassist.expr.*;

public class BoxingProfilerTranslator implements Translator {
  private ArrayList<CtMethod> autoboxingMethods = new ArrayList<CtMethod>();
  private ArrayList<CtMethod> unboxingMethods = new ArrayList<CtMethod>();

  public void start(ClassPool pool)
      throws NotFoundException, CannotCompileException {
	  
    CtClass compileClass = pool.get("java.lang.Integer");
    CtMethod ValueCm = compileClass.getMethod("intValue", "()I");
    CtMethod ValueOfCm = compileClass.getMethod("valueOf", "(I)Ljava/lang/Integer;");
    unboxingMethods.add(ValueCm);
    autoboxingMethods.add(ValueOfCm);
    
    compileClass = pool.get("java.lang.Long");
    ValueCm = compileClass.getMethod("longValue", "()J");
    ValueOfCm = compileClass.getMethod("valueOf", "(J)Ljava/lang/Long;");
    unboxingMethods.add(ValueCm);
    autoboxingMethods.add(ValueOfCm);
    
    compileClass = pool.get("java.lang.Double");
    ValueCm = compileClass.getMethod("doubleValue", "()D");
    ValueOfCm = compileClass.getMethod("valueOf", "(D)Ljava/lang/Double;");
    unboxingMethods.add(ValueCm);
    autoboxingMethods.add(ValueOfCm);
    
    compileClass = pool.get("java.lang.Float");
    ValueCm = compileClass.getMethod("floatValue", "()F");
    ValueOfCm = compileClass.getMethod("valueOf", "(F)Ljava/lang/Float;");
    unboxingMethods.add(ValueCm);
    autoboxingMethods.add(ValueOfCm);
    
    compileClass = pool.get("java.lang.Short");
    ValueCm = compileClass.getMethod("shortValue", "()S");
    ValueOfCm = compileClass.getMethod("valueOf", "(S)Ljava/lang/Short;");
    unboxingMethods.add(ValueCm);
    autoboxingMethods.add(ValueOfCm);
    
    compileClass = pool.get("java.lang.Boolean");
    ValueCm = compileClass.getMethod("booleanValue", "()Z");
    ValueOfCm = compileClass.getMethod("valueOf", "(Z)Ljava/lang/Boolean;");
    unboxingMethods.add(ValueCm);
    autoboxingMethods.add(ValueOfCm);
    
    compileClass = pool.get("java.lang.Byte");
    ValueCm = compileClass.getMethod("byteValue", "()B");
    ValueOfCm = compileClass.getMethod("valueOf", "(B)Ljava/lang/Byte;");
    unboxingMethods.add(ValueCm);
    autoboxingMethods.add(ValueOfCm);
    
    compileClass = pool.get("java.lang.Character");
    ValueCm = compileClass.getMethod("charValue", "()C");
    ValueOfCm = compileClass.getMethod("valueOf", "(C)Ljava/lang/Character;");
    unboxingMethods.add(ValueCm);
    autoboxingMethods.add(ValueOfCm);
     
//  CtMethod[] m = compileClass.getMethods();
//
//     for (CtMethod ctm : m) {
//     if (ctm.getName().equals("valueOf") || ctm.getName().equals("charValue"))
//     System.out.println(ctm.getName()+" && "+ctm.getSignature());
//     }
    
  }

  public void onLoad(ClassPool pool, String className)
      throws NotFoundException, CannotCompileException {
    if (className.equals("ist.meic.ap.ProfilingResults")) return;

    CtClass ctClass = pool.get(className);

    makeBoxingProfiler(ctClass);
  }

  void makeBoxingProfiler(CtClass ctClass) throws NotFoundException, CannotCompileException {
    for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
      ctMethod.instrument(new ExprEditor() {
        public void edit(MethodCall expr) {
          // System.out.println("MethodCall " + expr.getClassName() + " "
          //     + expr.getMethodName() + " " + expr.getSignature());

          try {
            CtMethod calledMethod = expr.getMethod();

            String key = ctMethod.getLongName() + " "
              // + calledMethod.getDeclaringClass().getName();
              + expr.getClassName();

            final String template =
              "{" +
              "  ist.meic.ap.ProfilingResults.add(\"" + key + " %s\");" +
              "  $_ = $proceed($$);" +
              "}";

            if (unboxingMethods.contains(calledMethod)) {
              try {
                expr.replace(String.format(template, "unboxed"));
              } catch (CannotCompileException e) {
                e.printStackTrace();
              }
            }

            if (autoboxingMethods.contains(calledMethod)) {
              try {
                expr.replace(String.format(template, "boxed"));
              } catch (CannotCompileException e) {
                e.printStackTrace();
              }
            }
          } catch (javassist.NotFoundException e) {
          }
        }
      });
    }
  }
}
