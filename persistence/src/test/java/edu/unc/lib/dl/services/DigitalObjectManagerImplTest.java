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
package edu.unc.lib.dl.services;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.UUID;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;

import org.jdom.Document;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.unc.lib.dl.agents.Agent;
import edu.unc.lib.dl.agents.AgentManager;
import edu.unc.lib.dl.agents.MockPersonAgent;
import edu.unc.lib.dl.fedora.AccessClient;
import edu.unc.lib.dl.fedora.FedoraException;
import edu.unc.lib.dl.fedora.ManagementClient;
import edu.unc.lib.dl.fedora.ManagementClient.ChecksumType;
import edu.unc.lib.dl.fedora.ManagementClient.Format;
import edu.unc.lib.dl.fedora.PID;
import edu.unc.lib.dl.fedora.ServiceException;
import edu.unc.lib.dl.fedora.types.MIMETypedStream;
import edu.unc.lib.dl.ingest.IngestException;
import edu.unc.lib.dl.ingest.sip.METSPackageSIP;
import edu.unc.lib.dl.util.ContentModelHelper;
import edu.unc.lib.dl.util.FileUtils;
import edu.unc.lib.dl.util.TripleStoreQueryService;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * @author Gregory Jansen
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/service-context.xml" })
public class DigitalObjectManagerImplTest {

    @Resource
    private DigitalObjectManagerImpl digitalObjectManagerImpl = null;

    public DigitalObjectManagerImpl getDigitalObjectManagerImpl() {
	return digitalObjectManagerImpl;
    }

    public void setDigitalObjectManagerImpl(DigitalObjectManagerImpl digitalObjectManagerImpl) {
	this.digitalObjectManagerImpl = digitalObjectManagerImpl;
    }

    @Resource
    ManagementClient managementClient = null;

    @Resource
    AccessClient accessClient = null;

    @Resource
    TripleStoreQueryService tripleStoreQueryService = null;

    @Resource
    JavaMailSender javaMailSender = null;

    private static final String MD_CONTENTS = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
		    + "<m:structMap xmlns:m=\"http://www.loc.gov/METS/\">" + "<m:div TYPE=\"Container\">"
		    + "<m:div ID=\"test:delete\" ORDER=\"0\"/>" + "</m:div>" + "</m:structMap>";

    private static final String MD_EVENTS = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
		    + "<premis xmlns=\"info:lc/xmlns/premis-v2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
		    + "<object xsi:type=\"representation\">" + "<objectIdentifier>"
		    + "<objectIdentifierType>PID</objectIdentifierType>"
		    + "<objectIdentifierValue>test:container</objectIdentifierValue>" + "</objectIdentifier>"
		    + "</object>" + "</premis>";

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
	reset(this.managementClient,this.javaMailSender, this.tripleStoreQueryService);
	this.getDigitalObjectManagerImpl().setAvailable(true, "available");
	// setup default MD_CONTENTS stream
	MIMETypedStream mts = mock(MIMETypedStream.class);
	when(mts.getStream()).thenReturn(MD_CONTENTS.getBytes());
	when(accessClient.getDatastreamDissemination(any(PID.class), eq("MD_CONTENTS"), anyString())).thenReturn(mts);

	// setup default MD_EVENTS stream
	MIMETypedStream mts2 = mock(MIMETypedStream.class);
	when(mts2.getStream()).thenReturn(MD_EVENTS.getBytes());
	when(accessClient.getDatastreamDissemination(any(PID.class), eq("MD_EVENTS"), any(String.class))).thenReturn(
			mts2);

	// management client upload responses
	Answer<String> upload = new Answer<String>() {
	    @Override
	    public String answer(InvocationOnMock invocation) throws Throwable {
		return "upload://" + UUID.randomUUID();
	    }
	};
	when(this.managementClient.upload(any(File.class))).thenAnswer(upload);
	when(this.managementClient.upload(any(Document.class))).thenAnswer(upload);

	// setup mail sender mock invocations
	when(this.javaMailSender.createMimeMessage()).thenCallRealMethod();

	Answer dumpMessage = new Answer() {
	    @Override
	    public Object answer(InvocationOnMock invocation) throws Throwable {
		Object arg = invocation.getArguments()[0];
		if(arg instanceof MimeMessage) {
		    MimeMessage m = (MimeMessage)arg;
		    System.out.println("EMAIL DUMP:");
		    m.writeTo(System.out);
		} else if(arg instanceof SimpleMailMessage) {
		    SimpleMailMessage m = (SimpleMailMessage)arg;
		    System.out.println("EMAIL DUMP:");
		    System.out.println(m.toString());
		} else {
		    throw new Error("Could not print email: "+arg);
		}
		return null;
	    }
	};
	doAnswer(dumpMessage).when(this.javaMailSender).send(any(MimeMessage.class));
	doAnswer(dumpMessage).when(this.javaMailSender).send(any(SimpleMailMessage.class));
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for
     * {@link edu.unc.lib.dl.services.DigitalObjectManagerImpl#addRelationship(edu.unc.lib.dl.fedora.PID, edu.unc.lib.dl.util.ContentModelHelper.Relationship, edu.unc.lib.dl.fedora.PID)}
     * .
     */
    @Test
    public void testAddRelationship() {
	try {
	    when(managementClient.addObjectRelationship(any(PID.class), any(String.class), any(PID.class))).thenReturn(
			    Boolean.TRUE);
	    this.getDigitalObjectManagerImpl().addRelationship(new PID("cdr:test1"),
			    ContentModelHelper.Relationship.contains, new PID("cdr:test2"));
	    verify(managementClient, times(1)).addObjectRelationship(any(PID.class),
			    eq(ContentModelHelper.Relationship.contains.getURI().toString()), any(PID.class));
	} catch (Exception e) {
	    fail("Got unexpected exception: " + e.getMessage());
	}
    }

    /**
     * Test method for
     * {@link edu.unc.lib.dl.services.DigitalObjectManagerImpl#delete(edu.unc.lib.dl.fedora.PID, edu.unc.lib.dl.agents.Agent, java.lang.String)}
     * .
     */
    @Test
    public void testDelete() throws Exception {
	// verify works with references internal to delete contents
	// verify works with container reference
	Agent tron = new MockPersonAgent("Tester Tron", "tron", new PID("tes:tron"));
	// setup mocks
	when(tripleStoreQueryService.fetchAllContents(any(PID.class))).thenReturn(new ArrayList<PID>());
	PID container = new PID("test:container");
	ArrayList<PID> refs = new ArrayList<PID>();
	refs.add(container);
	when(tripleStoreQueryService.fetchObjectReferences(any(PID.class))).thenReturn(refs);
	when(tripleStoreQueryService.fetchContainer(any(PID.class))).thenReturn(container, container, container);

	when(managementClient.purgeObjectRelationship(any(PID.class), any(String.class), any(PID.class))).thenReturn(true);

	ArrayList<URI> cms = new ArrayList<URI>();
	cms.add(ContentModelHelper.Model.CONTAINER.getURI());
	when(tripleStoreQueryService.lookupContentModels(any(PID.class))).thenReturn(cms);

	PID test = new PID("test:delete");
	this.getDigitalObjectManagerImpl().delete(test, tron, "testing delete");

	verify(managementClient, times(1)).modifyInlineXMLDatastream(any(PID.class), eq("MD_CONTENTS"), eq(false),
			any(String.class), (ArrayList<String>) any(), any(String.class), any(Document.class));
	verify(managementClient, times(1)).modifyDatastreamByReference(any(PID.class), eq("MD_EVENTS"), eq(false),
			any(String.class), any(new ArrayList<String>().getClass()), any(String.class),
			any(String.class), any(String.class), any(ChecksumType.class), startsWith("upload://"));
	verify(managementClient, times(1)).purgeObject(eq(test), any(String.class), eq(false));
	verify(managementClient, times(0)).purgeObject(any(PID.class), any(String.class), eq(true));
	verify(managementClient, times(1)).purgeObject(any(PID.class), any(String.class), anyBoolean());
    }

    /**
     * Test method for
     * {@link edu.unc.lib.dl.services.DigitalObjectManagerImpl#delete(edu.unc.lib.dl.fedora.PID, edu.unc.lib.dl.agents.Agent, java.lang.String)}
     * . verify exception when referenced by PIDs not being deleted and not
     * container, verify exception cites the referencing PID.
     */
    @Test(expected = IngestException.class)
    public void testDeleteReferencedPIDException() throws Exception {
	// setup mocks
	Agent tron = new MockPersonAgent("Tester Tron", "tron", new PID("tes:tron"));
	when(tripleStoreQueryService.fetchAllContents(any(PID.class))).thenReturn(new ArrayList<PID>());
	PID container = new PID("test:container");
	ArrayList<PID> refs = new ArrayList<PID>();
	refs.add(container);
	refs.add(new PID("test:randomReference"));
	when(tripleStoreQueryService.fetchObjectReferences(any(PID.class))).thenReturn(refs);
	when(tripleStoreQueryService.fetchContainer(any(PID.class))).thenReturn(container);

	this.getDigitalObjectManagerImpl().delete(new PID("test:delete"), tron, "testing delete");
    }

    /**
     * Test method for
     * {@link edu.unc.lib.dl.services.DigitalObjectManagerImpl#delete(edu.unc.lib.dl.fedora.PID, edu.unc.lib.dl.agents.Agent, java.lang.String)}
     * .
     */
    @Test
    public void testDeleteForFedoraFault() throws Exception {
	// verify works with references internal to delete contents
	// verify works with container reference
	Agent tron = new MockPersonAgent("Tester Tron", "tron", new PID("tes:tron"));
	// setup mocks
	when(tripleStoreQueryService.fetchAllContents(any(PID.class))).thenReturn(new ArrayList<PID>());
	PID container = new PID("test:container");
	ArrayList<PID> refs = new ArrayList<PID>();
	refs.add(container);
	when(tripleStoreQueryService.fetchObjectReferences(any(PID.class))).thenReturn(refs);
	when(tripleStoreQueryService.fetchContainer(any(PID.class))).thenReturn(container, container, container);

	when(managementClient.purgeObjectRelationship(any(PID.class), any(String.class), any(PID.class))).thenReturn(true);

	ArrayList<URI> cms = new ArrayList<URI>();
	cms.add(ContentModelHelper.Model.CONTAINER.getURI());
	when(tripleStoreQueryService.lookupContentModels(any(PID.class))).thenReturn(cms);

	PID test = new PID("test:delete");
	FedoraException fe = mock(FedoraException.class);
	when(managementClient.purgeObject(any(PID.class), any(String.class), eq(false))).thenThrow(fe);
	Throwable thrown = null;
	try {
	    this.getDigitalObjectManagerImpl().delete(test, tron, "testing delete");
	} catch (IngestException e) {
	    thrown = e;
	}
	assertNotNull("An exception must be thrown", thrown);
	assertTrue("Exception must be an IngestException", thrown instanceof IngestException);

	// delete failures always result in a log dump (probably not necessary
	// unless PID are uncontained)

	// verify container was updated
	verify(managementClient, times(1)).modifyInlineXMLDatastream(any(PID.class), eq("MD_CONTENTS"), eq(false),
			any(String.class), (ArrayList<String>) any(), any(String.class), any(Document.class));
	verify(managementClient, times(1)).modifyDatastreamByReference(any(PID.class), eq("MD_EVENTS"), eq(false),
			any(String.class), any(new ArrayList<String>().getClass()), any(String.class),
			any(String.class), any(String.class), any(ChecksumType.class), startsWith("upload://"));

	// purge call will fail resulting in a log dump of rollback info
	verify(managementClient, times(1)).purgeObject(any(PID.class), any(String.class), anyBoolean());

	// TODO also test failure of "remove from container" operation
    }

    /**
     * Test method for
     * {@link edu.unc.lib.dl.services.DigitalObjectManagerImpl#delete(edu.unc.lib.dl.fedora.PID, edu.unc.lib.dl.agents.Agent, java.lang.String)}
     * .
     */
    @Test
    public void testDeleteForFedoraGone() throws Exception {
	// verify works with references internal to delete contents
	// verify works with container reference
	Agent tron = new MockPersonAgent("Tester Tron", "tron", new PID("tes:tron"));
	// setup mocks
	when(tripleStoreQueryService.fetchAllContents(any(PID.class))).thenReturn(new ArrayList<PID>());
	PID container = new PID("test:container");
	ArrayList<PID> refs = new ArrayList<PID>();
	refs.add(container);
	when(tripleStoreQueryService.fetchObjectReferences(any(PID.class))).thenReturn(refs);
	when(tripleStoreQueryService.fetchContainer(any(PID.class))).thenReturn(container, container, container);

	when(managementClient.purgeObjectRelationship(any(PID.class), any(String.class), any(PID.class))).thenReturn(true);

	ArrayList<URI> cms = new ArrayList<URI>();
	cms.add(ContentModelHelper.Model.CONTAINER.getURI());
	when(tripleStoreQueryService.lookupContentModels(any(PID.class))).thenReturn(cms);

	PID test = new PID("test:delete");
	ServiceException fe = mock(ServiceException.class);
	when(managementClient.purgeObject(any(PID.class), any(String.class), eq(false))).thenThrow(fe);
	Throwable thrown = null;
	try {
	    this.getDigitalObjectManagerImpl().delete(test, tron, "testing delete");
	} catch (IngestException e) {
	    thrown = e;
	}
	assertNotNull("An exception must be thrown", thrown);
	assertTrue("Exception must be an IngestException", thrown instanceof IngestException);

	// delete failures always result in a log dump (probably not necessary
	// unless PID are uncontained)

	// verify container was updated
	verify(managementClient, times(1)).modifyInlineXMLDatastream(any(PID.class), eq("MD_CONTENTS"), eq(false),
			any(String.class), (ArrayList<String>) any(), any(String.class), any(Document.class));
	verify(managementClient, times(1)).modifyDatastreamByReference(any(PID.class), eq("MD_EVENTS"), eq(false),
			any(String.class), any(new ArrayList<String>().getClass()), any(String.class),
			any(String.class), any(String.class), any(ChecksumType.class), startsWith("upload://"));

	// purge call will fail resulting in a log dump of rollback info
	verify(managementClient, times(1)).purgeObject(any(PID.class), any(String.class), anyBoolean());
	assertTrue("DOM must be made unavailable after a service exception", !this.getDigitalObjectManagerImpl()
			.isAvailable());
    }

    // TODO test fedora fault and ccessful rollback of a failed move
    // TODO test fedora fault and successful rollback of a failed update
    // TODO test fedora gone and successful log dump of rollback info for a
    // failed move
    // TODO test fedora gone and successful log dump of rollback info for a
    // failed update
    // TODO wrap mock around JavaMailSender with debug, verify email always sent
    // TODO testMailSendFailureLogging

    /**
     * Test method for
     * {@link edu.unc.lib.dl.services.DigitalObjectManagerImpl#purgeRelationship(edu.unc.lib.dl.fedora.PID, edu.unc.lib.dl.util.ContentModelHelper.Relationship, edu.unc.lib.dl.fedora.PID)}
     * .
     */
    @Test
    public void testPurgeRelationship() throws Exception {
	PID test = new PID("test:object");
	PID test2 = new PID("test:object2");
	this.getDigitalObjectManagerImpl().purgeRelationship(test, ContentModelHelper.Relationship.member, test2);
	verify(managementClient, times(1)).purgeObjectRelationship(eq(test),
			eq(ContentModelHelper.Relationship.member.getURI().toString()), eq(test2));
    }

    /**
     * Test method for
     * {@link edu.unc.lib.dl.services.DigitalObjectManagerImpl#updateSourceData(edu.unc.lib.dl.fedora.PID, java.lang.String, java.io.File, java.lang.String, java.lang.String, java.lang.String, edu.unc.lib.dl.agents.Agent, java.lang.String)}
     * .
     */
    //@Test
    public void testUpdateSourceData() {
	fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for
     * {@link edu.unc.lib.dl.services.DigitalObjectManagerImpl#checkAvailable()}
     * .
     */
    @Test(expected = IngestException.class)
    public void testAvailabilityException() throws Exception {
	this.getDigitalObjectManagerImpl().setAvailable(false,
			"The repository manager is unavailable for a test of the availability check.");
	this.getDigitalObjectManagerImpl().addRelationship(new PID("foo"), ContentModelHelper.Relationship.member,
			new PID("bar"));
    }

    /**
     * Test method for
     * {@link edu.unc.lib.dl.services.DigitalObjectManagerImpl#updateDescription(edu.unc.lib.dl.fedora.PID, java.io.File, java.lang.String, edu.unc.lib.dl.agents.Agent, java.lang.String)}
     * .
     */
    //@Test
    public void testUpdateDescription() {
	fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for
     * {@link edu.unc.lib.dl.services.DigitalObjectManagerImpl#move(java.util.List, java.lang.String, edu.unc.lib.dl.agents.Agent, java.lang.String)}
     * .
     */
    //@Test
    public void testMove() {
	fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link
     * edu.unc.lib.dl.services.DigitalObjectManagerImpl#add(...)} .
     */
    @Test
    public void testAdd() throws Exception {
	File orig = new File("src/test/resources/simple.zip");
	File test = File.createTempFile("testAdd", ".zip");
	test.deleteOnExit();
	FileUtils.copyFile(orig, test);
	Agent user = AgentManager.getAdministrativeGroupAgentStub();
	METSPackageSIP sip = new METSPackageSIP("/test/container/path", test, user, true);

	PID container = new PID("test:container");
	when(this.tripleStoreQueryService.fetchByRepositoryPath(eq("/test/container/path"))).thenReturn(container);
	ArrayList<URI> ans = new ArrayList<URI>();
	ans.add(ContentModelHelper.Model.CONTAINER.getURI());
	when(this.tripleStoreQueryService.lookupContentModels(eq(container))).thenReturn(ans);
	this.getDigitalObjectManagerImpl().add(sip, user, "testAdd for a good METS SIP", true);
	// verify ingest called
	verify(this.managementClient, times(14)).ingest(any(Document.class), any(Format.class), any(String.class));
	// verify parent updated
	verify(managementClient, times(1)).modifyInlineXMLDatastream(eq(container), eq("MD_CONTENTS"), eq(false),
			any(String.class), (ArrayList<String>) any(), any(String.class), any(Document.class));
	verify(managementClient, times(1)).modifyDatastreamByReference(eq(container), eq("MD_EVENTS"), eq(false),
			any(String.class), any(new ArrayList<String>().getClass()), any(String.class),
			any(String.class), any(String.class), any(ChecksumType.class), startsWith("upload://"));
	verify(this.javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    /**
     * Tests the case when one particular foxml is rejected by Fedora, throwing
     * a FedoraException. Verifies that any ingested objects in the transaction
     * are purged. Test method for
     * {@link edu.unc.lib.dl.services.DigitalObjectManagerImpl#add()} .
     */
    @Test
    public void testAddThenRollback() throws Exception {
	File orig = new File("src/test/resources/simple.zip");
	File test = File.createTempFile("testAdd", ".zip");
	test.deleteOnExit();
	FileUtils.copyFile(orig, test);
	Agent user = AgentManager.getAdministrativeGroupAgentStub();
	METSPackageSIP sip = new METSPackageSIP("/test/container/path", test, user, true);

	PID container = new PID("test:container");
	when(this.tripleStoreQueryService.fetchByRepositoryPath(eq("/test/container/path"))).thenReturn(container);
	ArrayList<URI> ans = new ArrayList<URI>();
	ans.add(ContentModelHelper.Model.CONTAINER.getURI());
	when(this.tripleStoreQueryService.lookupContentModels(eq(container))).thenReturn(ans);
	// failure of managementClient
	Answer<PID> pidAnswer = new Answer<PID>() {
	    @Override
	    public PID answer(InvocationOnMock invocation) throws Throwable {
		return new PID("uuid:" + UUID.randomUUID());
	    }
	};
	FedoraException fe = mock(FedoraException.class);
	when(fe.getMessage()).thenReturn("MOCKED FedoraException MESSAGE");
	when(this.managementClient.ingest(any(Document.class), any(Format.class), any(String.class))).thenAnswer(pidAnswer)
			.thenAnswer(pidAnswer).thenThrow(fe);

	Throwable thrown = null;
	try {
	    this.getDigitalObjectManagerImpl().add(sip, user, "testAdd for a good METS SIP", true);
	} catch (Throwable e) {
	    thrown = e;
	}
	assertNotNull("An ingest exception was thrown.", thrown);
	assertTrue("Exception must be IngestException", thrown instanceof IngestException);

	// verify rollback
	verify(this.managementClient, times(3)).ingest(any(Document.class), any(Format.class), any(String.class));
	verify(this.managementClient, times(2)).purgeObject(any(PID.class), any(String.class), eq(false));

	verify(this.javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    /**
     * Tests the case where ingest is interrupted by a ServiceException and
     * Fedora cannot be reached. Verifies that rollback information is dumped to
     * logs and that the DOM service is made unavailable. Test method for
     * {@link edu.unc.lib.dl.services.DigitalObjectManagerImpl#add()} .
     */
    @Test
    public void testAddThenDumpRollbackInfo() throws Exception {
	File orig = new File("src/test/resources/simple.zip");
	File test = File.createTempFile("testAdd", ".zip");
	test.deleteOnExit();
	FileUtils.copyFile(orig, test);
	Agent user = AgentManager.getAdministrativeGroupAgentStub();
	METSPackageSIP sip = new METSPackageSIP("/test/container/path", test, user, true);

	PID container = new PID("test:container");
	when(this.tripleStoreQueryService.fetchByRepositoryPath(eq("/test/container/path"))).thenReturn(container);
	ArrayList<URI> ans = new ArrayList<URI>();
	ans.add(ContentModelHelper.Model.CONTAINER.getURI());
	when(this.tripleStoreQueryService.lookupContentModels(eq(container))).thenReturn(ans);
	// failure of managementClient
	Answer<PID> pidAnswer = new Answer<PID>() {
	    @Override
	    public PID answer(InvocationOnMock invocation) throws Throwable {
		return new PID("uuid:" + UUID.randomUUID());
	    }
	};
	ServiceException se = mock(ServiceException.class);
	when(se.getMessage()).thenReturn("MOCKED ServiceException MESSAGE");
	when(this.managementClient.ingest(any(Document.class), any(Format.class), any(String.class))).thenAnswer(pidAnswer)
			.thenAnswer(pidAnswer).thenThrow(se).thenThrow(se, se, se, se);
	when(this.managementClient.purgeObject(any(PID.class), any(String.class), eq(false))).thenThrow(se);

	Throwable thrown = null;
	try {
	    this.getDigitalObjectManagerImpl().add(sip, user, "testAdd for a good METS SIP");
	} catch (Throwable e) {
	    thrown = e;
	}
	System.err.println("FOO");
	thrown.printStackTrace();
	assertNotNull("An exception must be thrown.", thrown);
	assertTrue("Exception must be an IngestException", thrown instanceof IngestException);

	// verify rollback
	verify(this.managementClient, times(3)).ingest(any(Document.class), any(Format.class), any(String.class));
	verify(this.managementClient, times(1)).purgeObject(any(PID.class), any(String.class), eq(false));
	assertTrue("The DOM must be made unavailable", !this.getDigitalObjectManagerImpl().isAvailable());

	// manually verify adequate rollback information was logged

	verify(this.javaMailSender, times(1)).send(any(MimeMessage.class));
    }
}