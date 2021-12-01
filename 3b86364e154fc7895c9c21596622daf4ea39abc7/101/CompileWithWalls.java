/*
 * Copyright (c) 2001-2004 Ant-Contrib project.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.antcontrib.walls;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.JAXPUtils;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Created on Aug 24, 2003.
 *
 * @author <a href="mailto:dean@xsoftware.biz">Dean Hiller</a>
 */
public class CompileWithWalls extends Task {
    /**
     * Field setWallsTwice.
     */
    private boolean setWallsTwice = false;

    /**
     * Field setJavacTwice.
     */
    private boolean setJavacTwice = false;

    /**
     * Field walls.
     */
    private Walls walls;

    /**
     * Field javac.
     */
    private Javac javac;

    /**
     * Field wallsFile.
     */
    private File wallsFile;

    /**
     * Field tempBuildDir.
     */
    private File tempBuildDir;

    /**
     * Field packagesNeedingCompiling.
     */
    @SuppressWarnings("unused")
    private Map<String, String> packagesNeedingCompiling = new HashMap<String, String>();

    /**
     * Field cachedSAXException.
     */
    private SAXException cachedSAXException = null;

    /**
     * Field cachedIOException.
     */
    private IOException cachedIOException = null;

    /**
     * Method setIntermediaryBuildDir.
     *
     * @param f File
     */
    public void setIntermediaryBuildDir(File f) {
        tempBuildDir = f;
    }

    /**
     * Method getIntermediaryBuildDir.
     *
     * @return File
     */
    public File getIntermediaryBuildDir() {
        return tempBuildDir;
    }

    /**
     * Method setWalls.
     *
     * @param f File
     */
    public void setWalls(File f) {
        this.wallsFile = f;

        XMLReader parser = JAXPUtils.getXMLReader();
        DefaultHandler hb = new WallsFileHandler(this, wallsFile);
        parser.setContentHandler(hb);
        parser.setEntityResolver(hb);
        parser.setErrorHandler(hb);
        parser.setDTDHandler(hb);
        try {
            log("about to start parsing walls file", Project.MSG_INFO);
            parser.parse(wallsFile.toURI().toURL().toExternalForm());
        } catch (SAXException e) {
            cachedSAXException = e;
            throw new ParsingWallsException("Problem parsing walls file attached:", e);
        } catch (IOException e) {
            cachedIOException = e;
            throw new ParsingWallsException("IOException on walls file attached:", e);
        }
    }

    /**
     * Method getWalls.
     *
     * @return File
     */
    public File getWalls() {
        return wallsFile;
    }

    /**
     * Method createWalls.
     *
     * @return Walls
     */
    public Walls createWalls() {
        if (walls != null) {
            setWallsTwice = true;
        }
        walls = new Walls();
        return walls;
    }

    /**
     * Method createJavac.
     *
     * @return Javac
     */
    public Javac createJavac() {
        if (javac != null) {
            setJavacTwice = true;
        }
        javac = new Javac();
        return javac;
    }

    /**
     * Method execute.
     *
     * @throws BuildException if task definition is incorrect or something goes wrong
     */
    public void execute() throws BuildException {
        if (cachedIOException != null) {
            throw new BuildException(cachedIOException, getLocation());
        } else if (cachedSAXException != null) {
            throw new BuildException(cachedSAXException, getLocation());
        } else if (tempBuildDir == null) {
            throw new BuildException(
                    "intermediaryBuildDir attribute must be specified on the compilewithwalls element",
                    getLocation());
        } else if (javac == null) {
            throw new BuildException(
                    "There must be a nested javac element",
                    getLocation());
        } else if (walls == null) {
            throw new BuildException(
                    "There must be a nested walls element",
                    getLocation());
        } else if (setWallsTwice) {
            throw new BuildException(
                    "compilewithwalls task only supports one nested walls element or one walls attribute",
                    getLocation());
        } else if (setJavacTwice) {
            throw new BuildException(
                    "compilewithwalls task only supports one nested javac element",
                    getLocation());
        }

        getProject().addTaskDefinition("SilentMove", SilentMove.class);
        getProject().addTaskDefinition("SilentCopy", SilentCopy.class);

        Path src = javac.getSrcdir();
        if (src == null) {
            throw new BuildException("Javac inside compilewithwalls must have a srcdir specified");
        }

        List<File> tempSrcDirs = new ArrayList<File>();
        for (String file : src.list()) {
            tempSrcDirs.add(getProject().resolveFile(file));
        }

        String[] classpaths = new String[0];
        if (javac.getClasspath() != null) {
            classpaths = javac.getClasspath().list();
        }

        for (String classpath : classpaths) {
            File temp = new File(classpath);
            if (temp.isDirectory()) {

                for (File tempSrcDir : tempSrcDirs) {
                    if (tempSrcDir.compareTo(temp) == 0) {
                        throw new BuildException("The classpath cannot contain any of the\n"
                                + "src directories, but it does.\n"
                                + "srcdir=" + tempSrcDir);
                    }
                }
            }
        }

        //get rid of non-existent srcDirs
        List<File> srcDirs2 = new ArrayList<File>();
        for (File tempSrcDir : tempSrcDirs) {
            if (tempSrcDir.exists()) {
                srcDirs2.add(tempSrcDir);
            }
        }

        File destDir = javac.getDestdir();
        if (destDir == null) {
            throw new BuildException("destdir was not specified in nested javac task",
                    getLocation());
        }

        //make sure tempBuildDir is not inside destDir or we are in trouble!!
        if (file1IsChildOfFile2(tempBuildDir, destDir)) {
            throw new BuildException("intermediaryBuildDir attribute cannot be specified\n"
                    + "to be the same as destdir or inside desdir of the javac task.\n"
                    + "This is an intermediary build directory only used by the\n"
                    + "compilewithwalls task, not the class file output directory.\n"
                    + "The class file output directory is specified in javac's destdir attribute",
                    getLocation());
        }

        //create the tempBuildDir if it doesn't exist.
        if (!tempBuildDir.exists()) {
            tempBuildDir.mkdirs();
            log("created direction=" + tempBuildDir, Project.MSG_VERBOSE);
        }

        Iterator<Package> iter = walls.getPackagesToCompile();
        while (iter.hasNext()) {
            Package toCompile = iter.next();

            File buildSpace = toCompile.getBuildSpace(tempBuildDir);
            if (!buildSpace.exists()) {
                buildSpace.mkdir();
                log("created directory=" + buildSpace, Project.MSG_VERBOSE);
            }

            FileSet javaIncludes2 = toCompile.getJavaCopyFileSet(getProject(), getLocation());

            for (File srcDir : srcDirs2) {
                javaIncludes2.setDir(srcDir);
                log(toCompile.getPackage() + ": sourceDir[" + srcDirs2.indexOf(srcDir) + "]="
                        + srcDir + " destDir=" + buildSpace, Project.MSG_VERBOSE);
                copyFiles(srcDir, buildSpace, javaIncludes2);
            }

            Path srcDir2 = toCompile.getSrcPath(tempBuildDir, getProject());
            Path classPath = toCompile.getClasspath(tempBuildDir, getProject());
            if (javac.getClasspath() != null) {
                classPath.addExisting(javac.getClasspath());
            }

            //unfortunately, we cannot clear the SrcDir in Javac, so we have to
            //clone instead of just reusing the other Javac....this means added
            //params in future releases will be missed unless this task is kept
            //up to date. need to convert to reflection later so we don't need
            //to keep this up to date.
            Javac buildSpaceJavac = new Javac();
            buildSpaceJavac.setProject(getProject());
            buildSpaceJavac.setOwningTarget(getOwningTarget());
            buildSpaceJavac.setTaskName(getTaskName());
            log(toCompile.getPackage() + ": Compiling");
            log(toCompile.getPackage() + ": sourceDir=" + srcDir2, Project.MSG_VERBOSE);
            log(toCompile.getPackage() + ": classPath=" + classPath, Project.MSG_VERBOSE);
            log(toCompile.getPackage() + ": destDir=" + buildSpace, Project.MSG_VERBOSE);
            buildSpaceJavac.setSrcdir(srcDir2);
            buildSpaceJavac.setDestdir(buildSpace);
            //includes not used...ie. ignored
            //includesfile not used
            //excludes not used
            //excludesfile not used
            buildSpaceJavac.setClasspath(classPath);
            //sourcepath not used
            buildSpaceJavac.setBootclasspath(javac.getBootclasspath());
            //classpath not used..redefined by us
            //sourcepathref not used...redefined by us.
            //bootclasspathref was already copied above(see javac and you will understand)
            buildSpaceJavac.setExtdirs(javac.getExtdirs());
            buildSpaceJavac.setEncoding(javac.getEncoding());
            buildSpaceJavac.setNowarn(javac.getNowarn());
            buildSpaceJavac.setDebug(javac.getDebug());
            buildSpaceJavac.setDebugLevel(javac.getDebugLevel());
            buildSpaceJavac.setOptimize(javac.getOptimize());
            buildSpaceJavac.setDeprecation(javac.getDeprecation());
            buildSpaceJavac.setTarget(javac.getTarget());
            buildSpaceJavac.setVerbose(javac.getVerbose());
            buildSpaceJavac.setDepend(javac.getDepend());
            buildSpaceJavac.setIncludeantruntime(javac.getIncludeantruntime());
            buildSpaceJavac.setIncludejavaruntime(javac.getIncludejavaruntime());
            buildSpaceJavac.setFork(javac.isForkedJavac());
            buildSpaceJavac.setExecutable(javac.getJavacExecutable());
            buildSpaceJavac.setMemoryInitialSize(javac.getMemoryInitialSize());
            buildSpaceJavac.setMemoryMaximumSize(javac.getMemoryMaximumSize());
            buildSpaceJavac.setFailonerror(javac.getFailonerror());
            buildSpaceJavac.setSource(javac.getSource());
            buildSpaceJavac.setCompiler(javac.getCompiler());

            String[] args = javac.getCurrentCompilerArgs();
            if (args != null) {
                for (String jcarg : args) {
                    buildSpaceJavac.createCompilerArg().setValue(jcarg);
                }
            }

            buildSpaceJavac.setProject(getProject());
            buildSpaceJavac.perform();

            //copy class files to javac's destDir where the user wants the class files
            copyFiles(buildSpace, destDir, toCompile.getClassCopyFileSet(getProject(), getLocation()));
        }
    }

    /**
     * file1IsChildOfFile2() method.
     *
     * @param tempBuildDir File
     * @param destDir      File
     * @return boolean
     */
    private boolean file1IsChildOfFile2(File tempBuildDir, File destDir) {
        File parent = tempBuildDir;
        for (int i = 0; i < 1000; i++) {
            if (parent.compareTo(destDir) == 0) {
                return true;
            }
            parent = parent.getParentFile();
            if (parent == null) {
                return false;
            }
        }

        throw new RuntimeException("You either have more than 1000 directories in"
                + "\nyour hierarchy or this is a bug, please report. parent=" + parent
                + "  destDir=" + destDir);
    }

    /**
     * Move java or class files to temp files or moves the temp files
     * back to java or class files.  This must be done because javac
     * is too nice and sticks already compiled classes and ones depended
     * on in the classpath destroying the compile time wall.  This way,
     * we can keep the wall up.
     *
     * @param srcDir  Directory to copy files from
     *                (Usually the java files dir or the class files dir)
     * @param destDir Directory to copy files to
     * @param fileset The fileset of files to include in the move.
     */
    private void copyFiles(
            File srcDir,
            File destDir,
            FileSet fileset) {

        fileset.setDir(srcDir);
        if (!srcDir.exists()) {
            throw new BuildException(
                    "Directory=" + srcDir + " does not exist",
                    getLocation());
        }

        //before we do this, we have to move all files not
        //in the above fileset to xxx.java.ant-tempfile
        //so that they don't get dragged into the compile
        //This way we don't miss anything and all the dependencies
        //are listed or the compile will break.
        Copy move = (Copy) getProject().createTask("SilentCopy");
        move.setProject(getProject());
        move.setOwningTarget(getOwningTarget());
        move.setTaskName(getTaskName());
        move.setLocation(getLocation());
        move.setTodir(destDir);
//        move.setOverwrite(true);
        move.addFileset(fileset);
        move.perform();
    }

    /**
     * Method log.
     *
     * @param msg   String
     * @param level int
     */
    public void log(String msg, int level) {
        super.log(msg, level);
    }

    //until 1.3 is deprecated, this is a cheat to chain exceptions.

    /**
     */
    @SuppressWarnings("serial")
    private static class ParsingWallsException extends RuntimeException {
        /**
         * Field message.
         */
        private String message;

        /**
         * Constructor for ParsingWallsException.
         *
         * @param message String
         * @param cause   Throwable
         */
        public ParsingWallsException(String message, Throwable cause) {
            super(message);

            this.message = message + "\n";

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            cause.printStackTrace(pw);

            this.message += sw;
        }

        /**
         * Method getMessage.
         *
         * @return String
         */
        public String getMessage() {
            return message;
        }

        /**
         * Method toString.
         *
         * @return String
         */
        public String toString() {
            return getMessage();
        }
    }
}
