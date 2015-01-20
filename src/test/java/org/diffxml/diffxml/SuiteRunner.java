package org.diffxml.diffxml;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.diffxml.diffxml.fmes.Fmes;
import org.diffxml.patchxml.DULPatch;
import org.diffxml.patchxml.PatchFormatException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.w3c.dom.Document;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * Runs all the test cases in the suite.
 * Ant seems to insist on the extends :(
 *
 * @author Adrian Mouat
 */
@RunWith(Parameterized.class)
public class SuiteRunner {

  /**
   * Where the test files are stored.
   */
  private static final String SUITE_DIR = "suite";

  @Parameterized.Parameters(name = "{index}: {2}")
  public static Collection<Object[]> data() throws URISyntaxException {
    URL resource = SuiteRunner.class.getClassLoader().getResource(SUITE_DIR + "/suite.txt");
    if (resource == null)
      throw new IllegalStateException("No suite found");
    File suiteDir = new File(resource.toURI()).getParentFile();
    Collection<Object[]> objects = new ArrayList<Object[]>();
    for (File fA : suiteDir.listFiles(new FilesEndAFilter())) {
      File fB = new File(fA.getAbsolutePath().replace("A.xml", "B.xml"));
      objects.add(new Object[] { fA, fB, fA.getName() + "," + fB.getName() });
    }
    return objects;
  }

  final File fA;
  final File fB;

  public SuiteRunner(File fA, File fB, String name) {
    this.fA = fA;
    this.fB = fB;
  }

  /**
   * Compares the two given files, applies the patch and checks they are the
   * same afterwards.
   *
   * @param fA first file to compare
   * @param fB second file to compare
   */
  @Test
  public final void runFMESTest() throws IOException {

    Fmes diffInstance = new Fmes();
    File tmpFile = File.createTempFile("test", fA.getName() + "_" + fB.getName());
    tmpFile.deleteOnExit();

    Document dA = null;
    Document dB = null;

    dA = DOMOps.getDocument(fA);
    dB = DOMOps.getDocument(fB);

    Document delta = null;
    try {
      delta = diffInstance.diff(dA, dB);
      DOMOps.outputXML(delta, new FileOutputStream(tmpFile), true);
    } catch (DiffException e) {
      fail("Diff threw exception: " + e.getMessage());
    } catch (IOException e) {
      fail("Caught IO exception:" + e.getMessage());
    }

    DULPatch patcher = new DULPatch();
    try {
      //Note the diff will modify dA, so need to read in again
      dA = DOMOps.getDocument(fA);
      patcher.apply(dA, delta);

    } catch (PatchFormatException e) {
      e.printStackTrace();
    }

    try {
      delta = diffInstance.diff(DOMOps.getDocument(fB), dA);
      DOMOps.outputXML(delta, new FileOutputStream(tmpFile), true);
    } catch (DiffException e) {
      fail("Diff threw exception: " + e.getMessage());
    } catch (FileNotFoundException e) {
      fail("Caught Exception: " + e.getMessage());
    } catch (IOException e) {
      fail("Caught Exception: " + e.getMessage());
    }

    assertFalse(delta.getDocumentElement().hasChildNodes());
  }

  /**
   * Returns only files ending in "A.xml".
   */
  static class FilesEndAFilter implements FileFilter {

    /**
     * Tests whether the given file is a file and ends in "A.xml".
     *
     * @param f The file to be tested
     * @return True if the file meets the criteria
     */
    public boolean accept(final File f) {

      boolean ret = false;
      if (f.isFile() && f.getName().endsWith("A.xml")) {
        ret = true;
      }

      return ret;
    }
  }

}

