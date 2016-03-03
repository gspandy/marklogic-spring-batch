package com.marklogic.spring.batch.bind;

import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jdom2.input.DOMBuilder;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import com.marklogic.junit.Fragment;
import com.marklogic.spring.batch.AbstractSpringBatchTest;
import com.marklogic.spring.batch.JobExecutionTestUtils;
import com.marklogic.spring.batch.core.AdaptedJobExecution;

public class MarshallSpringBatchPojoToXmlTest extends AbstractSpringBatchTest {
	
	AdaptedJobExecution jobExec;
	
	Document doc;
	Marshaller marshaller;
	
	@Before
	public void setup() throws Exception {
		AdaptedJobExecution adaptedJobExecution = new AdaptedJobExecution(JobExecutionTestUtils.getJobExecution());
		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();	
		marshaller = JAXBContext.newInstance(AdaptedJobExecution.class).createMarshaller();
	    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	    marshaller.marshal(adaptedJobExecution, doc);
	}
	
	@Test
    public void marshallJobParametersTest() throws Exception {
        Fragment frag = new Fragment(new DOMBuilder().build(doc));
        frag.setNamespaces(getNamespaceProvider().getNamespaces()); 
        frag.assertElementExists("/msb:jobExecution/msb:jobParameters/msb:jobParameter[@key = 'stringTest' and text() = 'Joe Cool' and @identifier = 'true']");
        frag.assertElementExists("/msb:jobExecution/msb:jobParameters/msb:jobParameter[@key = 'longTest' and text() = '1239' and @identifier = 'false']");
        frag.assertElementExists("/msb:jobExecution/msb:jobParameters/msb:jobParameter[@key = 'start' and @identifier = 'false']");
        frag.assertElementExists("/msb:jobExecution/msb:jobParameters/msb:jobParameter[@key = 'doubleTest' and text() = '1.35' and @identifier = 'false']");
        frag.prettyPrint();
    }
	
	@Test
	public void marshallJobInstanceTest() throws Exception {
        Fragment frag = new Fragment(new DOMBuilder().build(doc));
        frag.setNamespaces(getNamespaceProvider().getNamespaces()); 
        frag.prettyPrint();
        frag.assertElementExists("/msb:jobExecution/msb:jobInstance/msb:id");
	}

	@Test
	public void marshallJobExecutionTest() throws Exception {
        Fragment frag = new Fragment(new DOMBuilder().build(doc));
        frag.setNamespaces(getNamespaceProvider().getNamespaces()); 
        frag.prettyPrint();
        frag.assertElementExists("/msb:jobExecution/msb:id");
        frag.assertElementExists("/msb:jobExecution/msb:createDateTime");
        frag.assertElementValue("/msb:jobExecution/msb:status", "STARTING");
	}
	
	@Test
	public void marshallStepExecutionTest() throws Exception {
		Fragment frag = new Fragment(new DOMBuilder().build(doc));
		frag.setNamespaces(getNamespaceProvider().getNamespaces());
		frag.prettyPrint();
		List<Fragment> steps = frag.getFragments("/msb:jobExecution/msb:stepExecutions/msb:stepExecution");
		steps.get(0).assertElementValue("/msb:stepExecution/msb:stepName", "sampleStep1");
		steps.get(1).assertElementValue("/msb:stepExecution/msb:stepName", "sampleStep2");
		
	}
}