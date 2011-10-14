/**
 * Copyright (c) 2011, Qulice.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the Qulice.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.qulice.maven;

import edu.umd.cs.findbugs.FindBugs2;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * Test case for {@link FindbugsValidator} class.
 * @author Yegor Bugayenko (yegor@qulice.com)
 * @version $Id$
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ FindBugsValidator.class, FindBugs2.class })
public final class FindBugsValidatorTest {

    /**
     * The environment to work with.
     * @see #prepare()
     */
    private Environment env;

    /**
     * Forward SLF4J to Maven Log.
     * @throws Exception If something is wrong inside
     */
    @BeforeClass
    public static void initLogging() throws Exception {
        final Log log = Mockito.mock(Log.class);
        StaticLoggerBinder.getSingleton().setMavenLog(log);
    }

    /**
     * Prepare the environment.
     * @throws Exception If something wrong happens inside
     */
    @Before
    public void prepare() throws Exception {
        final MavenProject project = Mockito.mock(MavenProject.class);
        final File basedir = new File(".");
        Mockito.doReturn(new File(basedir.getPath()))
            .when(project).getBasedir();
        final Build build = Mockito.mock(Build.class);
        Mockito.doReturn(build).when(project).getBuild();
        final File classes = new File(basedir, "target/classes");
        final List<String> paths = new ArrayList<String>();
        paths.add(classes.getPath());
        Mockito.doReturn(paths).when(project).getRuntimeClasspathElements();
        Mockito.doReturn(classes.getPath()).when(build).getOutputDirectory();
        this.env = new Environment();
        this.env.setProject(project);
    }

    /**
     * Absent violations should pass.
     * @throws Exception If something wrong happens inside
     */
    @Test
    public void testValidatesWithNoViolations() throws Exception {
        PowerMockito.mockStatic(FindBugs2.class);
        final FindBugs2 findbugs = PowerMockito.mock(FindBugs2.class);
        PowerMockito.whenNew(FindBugs2.class).withNoArguments()
            .thenReturn(findbugs);
        new FindBugsValidator().validate(this.env);
    }

    /**
     * One violation should be found.
     * @throws Exception If something wrong happens inside
     */
    @Test(expected = MojoFailureException.class)
    public void testValidatesWithOneViolation() throws Exception {
        PowerMockito.mockStatic(FindBugs2.class);
        final FindBugs2 findbugs = PowerMockito.mock(FindBugs2.class);
        PowerMockito.whenNew(FindBugs2.class).withNoArguments()
            .thenReturn(findbugs);
        Mockito.doReturn(1).when(findbugs).getBugCount();
        new FindBugsValidator().validate(this.env);
    }

}
