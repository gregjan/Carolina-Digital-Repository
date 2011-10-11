/**
 * Copyright 2008 The University of North Carolina at Chapel Hill
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.unc.lib.dl.ingest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.unc.lib.dl.agents.Agent;
import edu.unc.lib.dl.agents.AgentManager;
import edu.unc.lib.dl.agents.MockPersonAgent;
import edu.unc.lib.dl.fedora.PID;
import edu.unc.lib.dl.ingest.aip.AIPIngestPipeline;
import edu.unc.lib.dl.ingest.aip.ArchivalInformationPackage;
import edu.unc.lib.dl.ingest.sip.METSPackageSIP;
import edu.unc.lib.dl.ingest.sip.METSPackageSIPProcessor;
import edu.unc.lib.dl.ingest.sip.MultiFileObjectSIP;
import edu.unc.lib.dl.ingest.sip.MultiFileObjectSIP.Datastream;
import edu.unc.lib.dl.ingest.sip.MultiFileObjectSIPProcessor;
import edu.unc.lib.dl.ingest.sip.SingleFolderSIP;
import edu.unc.lib.dl.ingest.sip.SingleFolderSIPProcessor;
import edu.unc.lib.dl.util.ContentModelHelper;
import edu.unc.lib.dl.util.PremisEventLogger;
import edu.unc.lib.dl.util.TripleStoreQueryService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/service-context.xml" })
public class AIPIngestPipelineTest {
    private static final Log log = LogFactory.getLog(AIPIngestPipelineTest.class);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Resource
    private AIPIngestPipeline aipIngestPipeline = null;

    @Resource
    private METSPackageSIPProcessor metsPackageSIPProcessor = null;

    @Resource
    private MultiFileObjectSIPProcessor multiFileObjectSIPProcessor = null;

    @Resource
    private SingleFolderSIPProcessor singleFolderSIPProcessor = null;

    public MultiFileObjectSIPProcessor getMultiFileObjectSIPProcessor() {
	return multiFileObjectSIPProcessor;
    }

    public void setMultiFileObjectSIPProcessor(MultiFileObjectSIPProcessor multiFileObjectSIPProcessor) {
	this.multiFileObjectSIPProcessor = multiFileObjectSIPProcessor;
    }

    @Autowired
    TripleStoreQueryService tripleStoreQueryService = null;

    public AIPIngestPipeline getAipIngestPipeline() {
	return aipIngestPipeline;
    }

    public METSPackageSIPProcessor getMetsPackageSIPProcessor() {
	return metsPackageSIPProcessor;
    }

    public void setAipIngestPipeline(AIPIngestPipeline aipIngestPipeline) {
	this.aipIngestPipeline = aipIngestPipeline;
    }

    public void setMetsPackageSIPProcessor(METSPackageSIPProcessor metsPackageSIPProcessor) {
	this.metsPackageSIPProcessor = metsPackageSIPProcessor;
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testProcessAIP() {
	// testing for successful conversion of SIP w/simple content model
	File testFile = new File("src/test/resources/simple.zip");
	Agent user = new MockPersonAgent("Testy Testerson", "test", new PID("cdr-test:142"));
	PremisEventLogger logger = new PremisEventLogger(user);
	METSPackageSIP sip = null;
	ArchivalInformationPackage aip = null;
	String containerPath = "/test/container/path";
	PID containerPID = new PID("test:1");
	try {
	    sip = new METSPackageSIP(containerPath, testFile, user, true);
	    sip.setDiscardDataFilesOnDestroy(false);
	} catch (IOException e) {
	    throw new Error(e);
	}

	// SETUP MOCK TRIPLES!
	ArrayList<URI> ans = new ArrayList<URI>();
	ans.add(ContentModelHelper.Model.CONTAINER.getURI());
	when(this.tripleStoreQueryService.lookupContentModels(any(PID.class))).thenReturn(ans);
	when(this.tripleStoreQueryService.fetchByRepositoryPath(eq(containerPath))).thenReturn(containerPID);

	try {
	    aip = this.getMetsPackageSIPProcessor().createAIP(sip, logger);
	    aip.setDeleteFilesOnDestroy(false);
	} catch (IngestException e) {
	    throw new Error(e);
	}
	assertNotNull("The result ingest context is null.", aip);
	int count = aip.getPIDs().size();
	assertTrue("There should be 14 PIDs in the resulting AIP, found " + count, count == 14);

	try {
	    aip = this.getAipIngestPipeline().processAIP(aip);
	    aip.prepareIngest();
	} catch (IngestException e) {
	    log.error("ingest exception during test", e);
	    fail("get exception processing AIP" + e.getMessage());
	}
    }

    @Test
    public void testExtraFiles() {
	this.testProcessBadAIP("src/test/resources/extrafiles.zip");
    }

    @Test
    public void testInvalidMETS() {
	this.testProcessBadAIP("src/test/resources/invalid_mets.zip");
    }

    @Test
    public void testBadProfileMETS() {
	this.testProcessBadAIP("src/test/resources/simple_bad_profile.zip");
    }

    public void testProcessBadAIP(String testfile) {
	// testing for failed conversion of SIP w/simple content model
	File testFile = new File(testfile);
	Agent user = new MockPersonAgent("Testy Testerson", "test", new PID("cdr-test:142"));
	PremisEventLogger logger = new PremisEventLogger(user);
	METSPackageSIP sip = null;
	ArchivalInformationPackage aip = null;
	String containerPath = "/test/container/path";
	PID containerPID = new PID("test:1");
	try {
	    sip = new METSPackageSIP(containerPath, testFile, user, true);
	    sip.setDiscardDataFilesOnDestroy(false);
	} catch (IOException e) {
	    throw new Error(e);
	}

	// SETUP MOCK TRIPLES!
	ArrayList<URI> ans = new ArrayList<URI>();
	ans.add(ContentModelHelper.Model.CONTAINER.getURI());
	when(this.tripleStoreQueryService.lookupContentModels(any(PID.class))).thenReturn(ans);
	when(this.tripleStoreQueryService.fetchByRepositoryPath(eq(containerPath))).thenReturn(containerPID);

	try {
	    log.debug("about to create AIP");
	    aip = this.getMetsPackageSIPProcessor().createAIP(sip, logger);
	    aip.setDeleteFilesOnDestroy(false);
	    assertNotNull("The result ingest context is null.", aip);
	    int count = aip.getPIDs().size();
	    assertTrue("There should be 13 PIDs in the resulting AIP, found " + count, count == 13);
	    aip = this.getAipIngestPipeline().processAIP(aip);
	    aip.prepareIngest();
	    fail("Failed to throw an IngestException for the extrafiles.zip");
	} catch (IngestException e) { // expected test path
	    // exercise the email code (via a mock in this spring config)
	    // this.mailNotifier.sendIngestFailureNotice(e, user, aip, sip);
	}
    }

    // @Test
    public void testProcessBigETDAIP() {
	// testing for successful conversion of SIP w/simple content model
	File testFile = new File("src/test/resources/METS.xml");
	Agent user = AgentManager.getAdministrativeGroupAgentStub();
	PremisEventLogger logger = new PremisEventLogger(user);
	METSPackageSIP sip = null;
	ArchivalInformationPackage aip = null;
	String containerPath = "/collections";
	PID containerPID = new PID("test:Foo");
	try {
	    sip = new METSPackageSIP(containerPath, testFile, user, false);
	    sip.setDiscardDataFilesOnDestroy(false);
	} catch (IOException e) {
	    throw new Error(e);
	}

	// SETUP MOCK TRIPLES!
	ArrayList<URI> ans = new ArrayList<URI>();
	ans.add(ContentModelHelper.Model.CONTAINER.getURI());
	when(this.tripleStoreQueryService.lookupContentModels(any(PID.class))).thenReturn(ans);
	when(this.tripleStoreQueryService.fetchByRepositoryPath(eq(containerPath))).thenReturn(containerPID);

	try {
	    aip = this.getMetsPackageSIPProcessor().createAIP(sip, logger);
	    aip.setDeleteFilesOnDestroy(false);
	} catch (IngestException e) {
	    throw new Error(e);
	}
	assertNotNull("The result ingest context is null.", aip);
	int count = aip.getPIDs().size();
	assertTrue("There should be 2014 PIDs in the resulting AIP, found " + count, count == 2014);

	try {
	    aip = this.getAipIngestPipeline().processAIP(aip);
	    aip.prepareIngest();
	} catch (IngestException e) {
	    log.error("ingest exception during test", e);
	    Element error = e.getErrorXML();
	    try {
		FileOutputStream fos = new FileOutputStream(new File("/tmp/error.xml"));
		XMLOutputter out = new XMLOutputter();
		out.output(error, fos);
		fos.flush();
		fos.close();
		log.error("Wrote ingest log to /tmp/error.xml");
	    } catch (IOException ioe) {
		log.error("Could not write /tmp/error.xml file.", ioe);
	    }
	    fail("get exception processing AIP" + e.getMessage());
	}
    }

    @Test
    public void testProcessMultiFileObjectAIP() {
	// testing for successful conversion of SIP w/simple content model
	Agent user = AgentManager.getAdministrativeGroupAgentStub();
	PremisEventLogger logger = new PremisEventLogger(user);
	MultiFileObjectSIP sip = null;
	ArchivalInformationPackage aip = null;
	String containerPath = "/test/container/path";
	PID containerPID = new PID("test:1");
	sip = new MultiFileObjectSIP();
	sip.setDiscardFilesOnDestroy(false);
	List<Datastream> data = new ArrayList<Datastream>();
	File file1 = new File("src/test/resources/simple.zip");
	File file2 = new File("src/test/resources/METS.xml");
	File file3 = new File("src/test/resources/invalid_mets.zip");
	data.add(sip.new Datastream(file1, "file 1", null, "archive/zip"));
	Datastream webdata = sip.new Datastream(file2, "file 2 (XML)", null, "text/xml");
	data.add(webdata);
	data.add(sip.new Datastream(file3, "file 3", null, "archive/zip"));
	sip.setAllowIndexing(true);
	sip.setContainerPath(containerPath);
	sip.setDatastreams(data);
	sip.setDefaultWebData(webdata);
	sip.setModsXML(new File("src/test/resources/testmods.xml"));
	sip.setOwner(user);

	// SETUP MOCK TRIPLES!
	ArrayList<URI> ans = new ArrayList<URI>();
	ans.add(ContentModelHelper.Model.CONTAINER.getURI());
	when(this.tripleStoreQueryService.lookupContentModels(any(PID.class))).thenReturn(ans);
	when(this.tripleStoreQueryService.fetchByRepositoryPath(eq(containerPath))).thenReturn(containerPID);

	try {
	    aip = this.getMultiFileObjectSIPProcessor().createAIP(sip, logger);
	    aip.setDeleteFilesOnDestroy(false);
	} catch (IngestException e) {
	    throw new Error(e);
	}
	assertNotNull("The result ingest context is null.", aip);
	int count = aip.getPIDs().size();
	assertTrue("There should be 1 PID in the resulting AIP, found " + count, count == 1);

	try {
	    aip = this.getAipIngestPipeline().processAIP(aip);
	    aip.prepareIngest();
	} catch (IngestException e) {
	    log.error("ingest exception during test", e);
	    fail("get exception processing AIP" + e.getMessage());
	}
    }

    @Test
    public void testProcessCollectionFolder() {
	// testing for successful conversion of SIP w/simple content model
	File testFile = new File("src/test/resources/coll_mods.xml");
	Agent user = new MockPersonAgent("Testy Testerson", "test", new PID("cdr-test:142"));
	PremisEventLogger logger = new PremisEventLogger(user);
	SingleFolderSIP sip = null;
	ArchivalInformationPackage aip = null;
	String containerPath = "/test/container/path";
	PID containerPID = new PID("test:1");
	sip = new SingleFolderSIP();
	sip.setContainerPath(containerPath);
	sip.setSlug("etd");
	sip.setModsXML(testFile);
	sip.setOwner(user);

	// SETUP MOCK TRIPLES!
	ArrayList<URI> ans = new ArrayList<URI>();
	ans.add(ContentModelHelper.Model.CONTAINER.getURI());
	when(this.tripleStoreQueryService.lookupContentModels(any(PID.class))).thenReturn(ans);
	when(this.tripleStoreQueryService.fetchByRepositoryPath(eq(containerPath))).thenReturn(containerPID);

	try {
	    aip = this.getSingleFolderSIPProcessor().createAIP(sip, logger);
	    aip.setDeleteFilesOnDestroy(false);
	} catch (IngestException e) {
	    throw new Error(e);
	}
	assertNotNull("The result ingest context is null.", aip);
	int count = aip.getPIDs().size();
	assertTrue("There should be 1 PID in the resulting AIP, found " + count, count == 1);

	try {
	    aip = this.getAipIngestPipeline().processAIP(aip);
	    aip.prepareIngest();
	} catch (IngestException e) {
	    log.error("ingest exception during test", e);
	    fail("get exception processing AIP" + e.getMessage());
	}
    }

    public SingleFolderSIPProcessor getSingleFolderSIPProcessor() {
        return singleFolderSIPProcessor;
    }

    public void setSingleFolderSIPProcessor(SingleFolderSIPProcessor singleFolderSIPProcessor) {
        this.singleFolderSIPProcessor = singleFolderSIPProcessor;
    }
}