/*
 * Copyright (c) 2012 David Boissier
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

package org.codinjutsu.tools.jenkins.logic;

import org.apache.commons.io.IOUtils;
import org.codinjutsu.tools.jenkins.JenkinsConfiguration;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.model.View;
import org.codinjutsu.tools.jenkins.security.SecurityClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import static org.codinjutsu.tools.jenkins.model.BuildStatusEnum.FAILURE;
import static org.codinjutsu.tools.jenkins.model.BuildStatusEnum.SUCCESS;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class JsonRequestManagerTest {

    private RequestManager requestManager;

    private JenkinsConfiguration configuration;

    @Mock
    private SecurityClient securityClientMock;


    @Test
    @Ignore
    public void loadJenkinsWorkSpace() throws Exception {
        when(securityClientMock.execute(any(URL.class)))
                .thenReturn(IOUtils.toString(UberXmlRequestManagerTest.class.getResourceAsStream("JsonRequestManager_loadWorkspace.json")));
        Jenkins jenkins = requestManager.loadJenkinsWorkspace(configuration);

        List<View> actualViews = jenkins.getViews();

        List<View> expectedViews = new LinkedList<View>();
        expectedViews.add(View.createView("Framework", "http://myjenkins/view/Framework/"));
        expectedViews.add(View.createView("Tools", "http://myjenkins/view/Tools/"));
        expectedViews.add(View.createView("Tous", "http://myjenkins/"));

        assertReflectionEquals(expectedViews, actualViews);

        assertReflectionEquals(View.createView("Tous", "http://myjenkins"), jenkins.getPrimaryView());
    }

    @Test
    @Ignore
    public void loadJenkinsWorkSpaceWithNestedViews() throws Exception {
        when(securityClientMock.execute(any(URL.class)))
                .thenReturn(IOUtils.toString(UberXmlRequestManagerTest.class.getResourceAsStream("JsonRequestManager_loadWorkspaceWithNestedView.json")));
        Jenkins jenkins = requestManager.loadJenkinsWorkspace(configuration);

        List<View> actualViews = jenkins.getViews();

        List<View> expectedViews = new LinkedList<View>();
        expectedViews.add(View.createView("Framework", "http://myjenkins/view/Framework/"));
        View nestedView = View.createView("NestedView", "http://myjenkins/view/NestedView/");

        nestedView.addSubView(View.createNestedView("FirstSubView", "http://myjenkins/view/NestedView/view/FirstSubView/"));
        nestedView.addSubView(View.createNestedView("SecondSubView", "http://myjenkins/view/NestedView/view/SecondSubView/"));
        expectedViews.add(nestedView);

        expectedViews.add(View.createView("Tous", "http://myjenkins/"));

        assertReflectionEquals(expectedViews, actualViews);

        assertReflectionEquals(View.createView("Tous", "http://myjenkins"), jenkins.getPrimaryView());
    }


    @Test
    @Ignore
    public void loadClassicView() throws Exception {
        when(securityClientMock.execute(any(URL.class)))
                .thenReturn(IOUtils.toString(UberXmlRequestManagerTest.class.getResourceAsStream("JsonRequestManager_loadClassicView.json")));

        List<Job> actualJobs = requestManager.loadJenkinsView("http://myjenkins/");

        List<Job> expectedJobs = new LinkedList<Job>();


        expectedJobs.add(new JobBuilder().job("sql-tools", "blue", "http://myjenkins/job/sql-tools/", "true", "true")
                .lastBuild("http://myjenkins/job/sql-tools/15/", "15", SUCCESS.getStatus(), "false", "2012-04-02_15-26-29")
                .health("health-80plus", "0 tests en echec sur un total de 24 tests").get());
        expectedJobs.add(new JobBuilder().job("db-utils", "grey", "http://myjenkins/job/db-utils/", "false", "true").get());
        expectedJobs.add(new JobBuilder().job("myapp", "red", "http://myjenkins/job/myapp/", "false", "true")
                .lastBuild("http://myjenkins/job/myapp/12/", "12", FAILURE.getStatus(), "true", "2012-04-02_16-26-29")
                .health("health-00to19", "24 tests en echec sur un total de 24 tests")
                .parameter("param1", "ChoiceParameterDefinition", "value1", "value1", "value2", "value3")
                .parameter("runIntegrationTest", "BooleanParameterDefinition", null)
                .get());
        expectedJobs.add(new JobBuilder().job("swing-utils", "disabled", "http://myjenkins/job/swing-utils/", "true", "false")
                .lastBuild("http://myjenkins/job/swing-utils/5/", "5", FAILURE.getStatus(), "false", "2012-04-02_10-26-29")
                .health("health20to39", "0 tests en echec sur un total de 24 tests")
                .parameter("dummyParam", null, null)
                .get());

        assertReflectionEquals(expectedJobs, actualJobs);
    }

    @Test
    @Ignore
    public void loadCloudbeesView() throws Exception {
        when(securityClientMock.execute(any(URL.class)))
                .thenReturn(IOUtils.toString(UberXmlRequestManagerTest.class.getResourceAsStream("JsonRequestManager_loadCloudbeesView.json")));

        requestManager.setJenkinsPlateform(JenkinsPlateform.CLOUDBEES);
        List<Job> actualJobs = requestManager.loadJenkinsView("http://myjenkins/");

        List<Job> expectedJobs = new LinkedList<Job>();


        expectedJobs.add(new JobBuilder().job("sql-tools", "blue", "http://myjenkins/job/sql-tools/", "true", "true")
                .lastBuild("http://myjenkins/job/sql-tools/15/", "15", SUCCESS.getStatus(), "false", "2012-04-02_15-26-29")
                .health("health-80plus", "0 tests en echec sur un total de 24 tests").get());
        expectedJobs.add(new JobBuilder().job("db-utils", "grey", "http://myjenkins/job/db-utils/", "false", "true").get());
        expectedJobs.add(new JobBuilder().job("myapp", "red", "http://myjenkins/job/myapp/", "false", "true")
                .lastBuild("http://myjenkins/job/myapp/12/", "12", FAILURE.getStatus(), "true", "2012-04-02_16-26-29")
                .health("health-00to19", "24 tests en echec sur un total de 24 tests")
                .parameter("param1", "ChoiceParameterDefinition", "value1", "value1", "value2", "value3")
                .parameter("runIntegrationTest", "BooleanParameterDefinition", null)
                .get());
        expectedJobs.add(new JobBuilder().job("swing-utils", "disabled", "http://myjenkins/job/swing-utils/", "true", "false")
                .lastBuild("http://myjenkins/job/swing-utils/5/", "5", FAILURE.getStatus(), "false", "2012-04-02_10-26-29")
                .health("health20to39", "0 tests en echec sur un total de 24 tests")
                .parameter("dummyParam", null, null)
                .get());

        assertReflectionEquals(expectedJobs, actualJobs);
    }

    @Test
    @Ignore
    public void loadJenkinsWorkspaceWithIncorrectServerPortInTheResponse() throws Exception {
        configuration.setServerUrl("http://myjenkins:8080");
        when(securityClientMock.execute(any(URL.class)))
                .thenReturn(IOUtils.toString(UberXmlRequestManagerTest.class.getResourceAsStream("JsonRequestManager_loadJenkinsWorkspaceWithIncorrectPortInTheResponse.json")));
        try {
            requestManager.loadJenkinsWorkspace(configuration);
            Assert.fail();
        } catch (ConfigurationException ex) {
            Assert.assertEquals("Jenkins Port seems to be incorrect in the Server configuration page. Please fix 'Jenkins URL' at http://myjenkins:8080/configure", ex.getMessage());
        }

        configuration.setServerUrl("http://myjenkins");
        when(securityClientMock.execute(any(URL.class)))
                .thenReturn(IOUtils.toString(UberXmlRequestManagerTest.class.getResourceAsStream("JsonRequestManager_loadJenkinsWorkspaceWithIncorrectPortInTheResponse.json")));
        requestManager.loadJenkinsWorkspace(configuration);

    }

    @Test
    @Ignore
    public void testLoadJob() throws Exception {
        when(securityClientMock.execute(any(URL.class)))
                .thenReturn(IOUtils.toString(getClass().getResourceAsStream("JsonRequestManager_loadJob.json")));

        Job actualJob = requestManager.loadJob("http://ci.jenkins-ci.org/job/config-provider-model/");

        assertReflectionEquals(new JobBuilder()
                .job("config-provider-model", "blue", "http://ci.jenkins-ci.org/job/config-provider-model/", "false", "true")
                .lastBuild("http://ci.jenkins-ci.org/job/config-provider-model/8/", "8", "SUCCESS", "false", "2012-04-02_16-26-29")
                .health("health-80plus", "0 tests en echec sur un total de 24 tests")
                .get(), actualJob);
    }


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        configuration = new JenkinsConfiguration();
        requestManager = new JsonRequestManager(securityClientMock);
    }
}
