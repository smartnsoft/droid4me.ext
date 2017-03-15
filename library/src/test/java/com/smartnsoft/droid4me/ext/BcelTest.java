package com.smartnsoft.droid4me.ext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import edu.umd.cs.findbugs.ba.URLClassPathRepository;
import org.apache.bcel.Constants;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.AALOAD;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.BIPUSH;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.BranchHandle;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.IF_ICMPGE;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.TargetLostException;
import org.apache.bcel.generic.Type;
import org.apache.bcel.util.BCELifier;
import org.apache.bcel.util.InstructionFinder;
import org.apache.bcel.util.InstructionFinder.CodeConstraint;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author ɉdouard Mercier
 * @since 2013.07.27
 */
public final class BcelTest
{

  private abstract static class StripIntruction
  {

    public final String pattern;

    public final InstructionFinder.CodeConstraint codeConstraint;

    public abstract void handle(ConstantPoolGen constantPoolGen, InstructionHandle instructionHandle);

    public StripIntruction(String pattern, CodeConstraint codeConstraint)
    {
      this.pattern = pattern;
      this.codeConstraint = codeConstraint;
    }

  }

  private String destinationDirectoryPath;

  @Before
  public void setup()
  {
    destinationDirectoryPath = System.getProperty("destination.directoryPath");
  }

  @After
  public void tearDown()
  {
  }

  @Test
  @Ignore
  public void test()
      throws IOException, ClassNotFoundException
  {
    final String jarFilePath = System.getProperty("jar.filePath");
    final ZipFile zip = new ZipFile(jarFilePath);
    final URLClassPathRepository urlClassPathRepository = new URLClassPathRepository();
    urlClassPathRepository.addURL(jarFilePath);
    final String dotClassExtension = ".class";
    final String loggerClassName = "fr.canalplus.android.freeboxv6.Logger";
    final String loggerMethodName = "logAndroid";
    final BasicType loggerMethodReturnType = Type.INT;

    {
      final String classFilePathPrefix = destinationDirectoryPath + "/" + "fr/canalplus/android/freeboxv6" + "/" + "Logger";
      final ClassGen classGen = new ClassGen(loggerClassName, "java.lang.Object", classFilePathPrefix + ".java", Constants.ACC_PUBLIC | Constants.ACC_SUPER, new String[] {});
      final ConstantPoolGen constantPoolGen = classGen.getConstantPool();
      {
        final InstructionList instructionList = new InstructionList();
        final MethodGen method = new MethodGen(Constants.ACC_PRIVATE, Type.VOID, Type.NO_ARGS, new String[] {}, "<init>", loggerClassName, instructionList, constantPoolGen);
        final InstructionFactory factory = new InstructionFactory(constantPoolGen);
        instructionList.append(InstructionFactory.createLoad(Type.OBJECT, 0));
        instructionList.append(factory.createInvoke("java.lang.Object", "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
        instructionList.append(InstructionFactory.createReturn(Type.VOID));
        method.setMaxStack();
        method.setMaxLocals();
        classGen.addMethod(method.getMethod());
        instructionList.dispose();
      }
      {
        final InstructionList instructionList = new InstructionList();
        final MethodGen methodGen = new MethodGen(Constants.ACC_PUBLIC | Constants.ACC_STATIC, loggerMethodReturnType, new Type[] { Type.STRING, Type.STRING }, new String[] {
            "tag", "message" }, loggerMethodName, classGen.getClassName(), instructionList, constantPoolGen);
        instructionList.append(new PUSH(constantPoolGen, 0));
        instructionList.append(InstructionFactory.createReturn(loggerMethodReturnType));
        methodGen.setMaxStack();
        methodGen.setMaxLocals();
        classGen.addMethod(methodGen.getMethod());
        instructionList.dispose();
      }
      final JavaClass javaClass = classGen.getJavaClass();
      javaClass.setConstantPool(constantPoolGen.getFinalConstantPool());
      javaClass.dump(classFilePathPrefix + dotClassExtension);
    }

    for (Enumeration<?> list = zip.entries(); list.hasMoreElements();)
    {
      final ZipEntry entry = (ZipEntry) list.nextElement();
      final String entryName = entry.getName();
      if (entryName.endsWith(dotClassExtension) == true)
      {
        final String strippedDotClass = entryName.replace(dotClassExtension, "");
        final int lastIndexOf = strippedDotClass.lastIndexOf("/");
        final String folderPath = strippedDotClass.substring(0, lastIndexOf);
        final String className = strippedDotClass.substring(lastIndexOf + 1);
        final String strippedSlashes = strippedDotClass.replace("/", ".");
        final String classFqn = strippedSlashes;

        // System.out.println(folderPath + " => " + classFqn + " => " + className);

        final JavaClass clazz = urlClassPathRepository.loadClass(classFqn);
        // final Repository repository = new Repository();
        final Method[] methods = clazz.getMethods();

        final ConstantPoolGen constantPoolGen = new ConstantPoolGen(clazz.getConstantPool());

        final StripIntruction stripIntruction1 = new StripIntruction("invokestatic", new InstructionFinder.CodeConstraint()
        {

          @Override
          public boolean checkCode(InstructionHandle[] match)
          {
            final INVOKESTATIC invokeStatic = (INVOKESTATIC) match[0].getInstruction();
            if (invokeStatic.getIndex() == constantPoolGen.lookupMethodref("android.util.Log", "e", "(Ljava/lang/String;Ljava/lang/String;)I"))
            {
              return true;
            }
            return false;
          }

        })
        {

          @Override
          public void handle(ConstantPoolGen constantPoolGen, InstructionHandle instructionHandle)
          {
            final InstructionFactory factory = new InstructionFactory(constantPoolGen);
            // final String anotherLoggerClassName = "android.util.Log";
            // final String anotherLoggerMethodName = "d";
            // final BasicType anotherLoggerMethodReturnType = Type.INT;
            final String anotherLoggerClassName = loggerClassName;
            final String anotherLoggerMethodName = loggerMethodName;
            final BasicType anotherLoggerMethodReturnType = loggerMethodReturnType;
            instructionHandle.setInstruction(factory.createInvoke(anotherLoggerClassName, anotherLoggerMethodName, anotherLoggerMethodReturnType, new Type[] {
                Type.STRING, Type.STRING }, Constants.INVOKESTATIC));
          }

        };
        final StripIntruction stripIntruction2 = new StripIntruction("getstatic ldc invokevirtual", new InstructionFinder.CodeConstraint()
        {

          @Override
          public boolean checkCode(InstructionHandle[] match)
          {
            final INVOKEVIRTUAL invokeStatic = (INVOKEVIRTUAL) match[2].getInstruction();
            if (invokeStatic.getIndex() == constantPoolGen.lookupMethodref("java.io.PrintStream", "println", "(Ljava/lang/String;)V"))
            {
              return true;
            }
            return false;
          }

        })
        {

          @Override
          public void handle(ConstantPoolGen constantPoolGen, InstructionHandle instructionHandle)
          {
            System.out.println("stripIntruction2");
            // final InstructionFactory factory = new InstructionFactory(constantPoolGen);
            // final String anotherLoggerClassName = loggerClassName;
            // final String anotherLoggerMethodName = loggerMethodName;
            // final BasicType anotherLoggerMethodReturnType = loggerMethodReturnType;
            // instructionHandle.setInstruction(factory.createInvoke(anotherLoggerClassName, anotherLoggerMethodName, anotherLoggerMethodReturnType,
            // new Type[] {
            // Type.STRING, Type.STRING }, Constants.INVOKESTATIC));
          }

        };
        final StripIntruction[] stripInstructions = { stripIntruction1, stripIntruction2 };

        int methodIndex = 0;
        for (Method method : methods)
        {
          if (method.isAbstract() == false && method.isNative() == false)
          {
            // System.out.println(method);
            // final Code code = method.getCode();
            // System.out.println(code);

            final MethodGen methodGen = new MethodGen(method, clazz.getClassName(), constantPoolGen);

            final InstructionList instructionList = methodGen.getInstructionList();
            int modificationsCount = 0;
            for (StripIntruction stripIntruction : stripInstructions)
            {
              final InstructionFinder instructionFinder = new InstructionFinder(instructionList);
              InstructionHandle next = null;
              final Iterator<?> iterator = instructionFinder.search(stripIntruction.pattern, stripIntruction.codeConstraint);
              while (iterator.hasNext())
              {
                System.out.println("Changes in '" + className + "', method '" + method.toString() + " regarding instruction '" + stripIntruction.pattern + "'");
                modificationsCount++;
                final InstructionHandle[] match = (InstructionHandle[]) iterator.next();
                final InstructionHandle first = match[0];
                final Instruction firstInstruction = first.getInstruction();
                final InstructionHandle beforeFirst = first.getPrev();
                @SuppressWarnings("unused")
                final InstructionHandle afterFirst = first.getNext();
                if ("a".equals(""))
                {
                  final InstructionHandle last = match[match.length - 1];
                  if ((next = last.getNext()) == null)
                  {
                    break;
                  }
                  try
                  {
                    instructionList.delete(first, last);
                  }
                  catch (TargetLostException exception)
                  {
                    final InstructionHandle[] targets = exception.getTargets();
                    for (int index = 0; index < targets.length; index++)
                    {
                      final InstructionTargeter[] targeters = targets[index].getTargeters();
                      for (int targeterIndex = 0; targeterIndex < targeters.length; targeterIndex++)
                      {
                        targeters[targeterIndex].updateTarget(targets[index], next);
                      }
                    }
                  }
                }
                if ("a".equals(""))
                {
                  final InstructionFactory factory = new InstructionFactory(constantPoolGen);
                  final InstructionList insertedInstructions = new InstructionList();

                  insertedInstructions.append(new BIPUSH((byte) 1));
                  insertedInstructions.append(new BIPUSH((byte) 0));

                  final BranchInstruction branchInstruction = InstructionFactory.createBranchInstruction(Constants.IF_ICMPGE, null);
                  insertedInstructions.append(branchInstruction);

                  // insertedInstructions.append(new GETSTATIC(constantPoolGen.addFieldref("java.lang.System", "out", "Ljava/io/PrintStream;")));
                  // insertedInstructions.append(new LDC(constantPoolGen.addString("Less than five")));
                  // insertedInstructions.append(new INVOKEVIRTUAL(constantPoolGen.addMethodref("java.io.PrintStream", "println",
                  // "(Ljava/lang/String;)V")));
                  // insertedInstructions.append(factory.createPrintln("Here!"));
                  insertedInstructions.append(firstInstruction);
                  final InstructionHandle aftertThenInstruction = insertedInstructions.append(factory.createPrintln("Not!"));

                  branchInstruction.setTarget(aftertThenInstruction);
                  instructionList.insert(beforeFirst, insertedInstructions);
                }
                else if ("".equals(""))
                {
                  stripIntruction.handle(constantPoolGen, first);
                }
              }
            }
            if (modificationsCount > 0)
            {
              System.out.println("Changes count in '" + className + "', method '" + method.toString() + ": " + modificationsCount);
              // We replace the method
              methodGen.setMaxLocals();
              methodGen.setMaxStack();
              methods[methodIndex] = methodGen.getMethod();
              instructionList.dispose();
            }
          }
          methodIndex++;
        }
        clazz.setConstantPool(constantPoolGen.getFinalConstantPool());
        clazz.dump(destinationDirectoryPath + "/" + folderPath + "/" + className + dotClassExtension);
      }
    }
  }

  @Test
  @Ignore
  public void bcelifier()
      throws ClassNotFoundException, FileNotFoundException
  {
    final JavaClass clazz = Repository.lookupClass(Simple.class);
    new BCELifier(clazz, new FileOutputStream(new File("Test.java"))).start();
  }

  @Test
  @Ignore
  public void otherTest()
      throws FileNotFoundException
  {
    // Create a ClassGen for our brand new class.
    ClassGen classGen = new ClassGen("com.geekyarticles.bcel.SyntheticClass", "java.lang.Object", "SyntheticClass.java", Constants.ACC_PUBLIC, null);

    // Get a reference to the constant pool of the class. This will be modified as we add methods, fields etc. Note that it already constains
    // a few constants.
    ConstantPoolGen constantPoolGen = classGen.getConstantPool();

    // The list of instructions for a method.
    InstructionList instructionList = new InstructionList();

    // Add the appropriate instructions.

    // Get the reference to static field out in class java.lang.System.
    instructionList.append(new GETSTATIC(constantPoolGen.addFieldref("java.lang.System", "out", "Ljava/io/PrintStream;")));

    instructionList.append(new ALOAD(0));// the argument of the main function
    instructionList.append(new BIPUSH((byte) 0));// Push 0.
    instructionList.append(new AALOAD());// Got value of the variable as a String;
    instructionList.append(new INVOKESTATIC(constantPoolGen.addMethodref("java.lang.Integer", "parseInt", "(Ljava/lang/String;)I")));// Now we got the
                                                                                                                                     // value as int

    instructionList.append(new BIPUSH((byte) 5));

    BranchHandle ifHandle = instructionList.append(new IF_ICMPGE(null)); // We do not yet know the position of the target. Will set it later.
    // Push the String to print
    instructionList.append(new LDC(constantPoolGen.addString("Less than five modified")));
    // Invoke println. we already have the object ref in the stack
    instructionList.append(new INVOKEVIRTUAL(constantPoolGen.addMethodref("java.io.PrintStream", "println", "(Ljava/lang/String;)V")));

    // Not to fall through the else part also. jump to the end
    // BranchHandle gotoHandle = instructionList.append(new GOTO(null));// We do not yet know the position of the target. Will set it later.

    // Push the String to print. This would be the target of the if_icmpge
    InstructionHandle matchHandle = instructionList.append(new LDC(constantPoolGen.addString("Greater than or equal to five")));
    // Invoke println. we already have the object ref in the stack
    instructionList.append(new INVOKEVIRTUAL(constantPoolGen.addMethodref("java.io.PrintStream", "println", "(Ljava/lang/String;)V")));

    // Return from the method. This would be the target of the goto.
    // InstructionHandle returnHandle = instructionList.append(new RETURN());

    ifHandle.setTarget(matchHandle);
    // gotoHandle.setTarget(returnHandle);

    MethodGen methodGen = new MethodGen(Constants.ACC_PUBLIC | Constants.ACC_STATIC, Type.VOID, new Type[] { new ArrayType(Type.STRING, 1) }, new String[] { "args" }, "main", "com.geekyarticles.bcel.SyntheticClass", instructionList, constantPoolGen);

    methodGen.setMaxLocals();// Calculate the maximum number of local variables.
    methodGen.setMaxStack();// Very important: must calculate the maximum size of the stack.

    classGen.addMethod(methodGen.getMethod()); // Add the method to the class

    // Print a few things.
    System.out.println("********Constant Pool**********");
    System.out.println(constantPoolGen.getFinalConstantPool());
    System.out.println("********Method**********");
    System.out.println(methodGen);
    System.out.println("********Instruction List**********");
    System.out.println(instructionList);

    // Now generate the class
    JavaClass javaClass = classGen.getJavaClass();
    try
    {
      // Write the class byte code into a file
      javaClass.dump(destinationDirectoryPath + "/" + "com/geekyarticles/bcel/SyntheticClass.class");

    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    // That's it.
  }

}
