package com.smartnsoft.droid4me.ext.test;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.TargetLostException;
import org.apache.bcel.util.InstructionFinder;
import org.apache.bcel.util.InstructionFinder.CodeConstraint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.umd.cs.findbugs.ba.URLClassPathRepository;

/**
 * @author ɉdouard Mercier
 * @since 2013.07.27
 */
public final class BcelTest
{

  private final static class StripIntruction
  {

    public final String pattern;

    public final InstructionFinder.CodeConstraint codeConstraint;

    public StripIntruction(String pattern, CodeConstraint codeConstraint)
    {
      this.pattern = pattern;
      this.codeConstraint = codeConstraint;
    }

  }

  @Before
  public void setup()
  {
  }

  @After
  public void tearDown()
  {
  }

  @Test
  public void test()
      throws IOException, ClassNotFoundException
  {
    final String jarFilePath = System.getProperty("jar.filePath");
    final String destinationDirectoryPath = System.getProperty("destination.directoryPath");
    final ZipFile zip = new ZipFile(jarFilePath);
    final URLClassPathRepository urlClassPathRepository = new URLClassPathRepository();
    urlClassPathRepository.addURL(jarFilePath);

    for (Enumeration<?> list = zip.entries(); list.hasMoreElements();)
    {
      final ZipEntry entry = (ZipEntry) list.nextElement();
      final String entryName = entry.getName();
      final String dotClassExtension = ".class";
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

        });
        final StripIntruction stripIntruction2 = new StripIntruction("invokevirtual", new InstructionFinder.CodeConstraint()
        {

          @Override
          public boolean checkCode(InstructionHandle[] match)
          {
            final INVOKEVIRTUAL invokeStatic = (INVOKEVIRTUAL) match[0].getInstruction();
            if (invokeStatic.getIndex() == constantPoolGen.lookupMethodref("java.io.PrintStream", "println", "(Ljava/lang/String;)V"))
            {
              return true;
            }
            return false;
          }

        });
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
              }
            }
            if (modificationsCount > 0)
            {
              System.out.println("Changes count in '" + className + "', method '" + method.toString() + ": " + modificationsCount);
              // We replace the method
              methods[methodIndex] = methodGen.getMethod();
            }
          }
          methodIndex++;
        }
        clazz.setConstantPool(constantPoolGen.getFinalConstantPool());
        clazz.dump(destinationDirectoryPath + "/" + folderPath + "/" + className + dotClassExtension);
      }
    }
  }

}
