package org.ektorp.audit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.Aspects;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.domain.TestDocument;
import org.ektorp.domain.TestRepository;
import org.ektorp.http.HttpClient;
import org.ektorp.http.HttpResponse;
import org.ektorp.impl.HttpResponseStub;
import org.ektorp.impl.StdCouchDbInstance;
import org.ektorp.impl.StdObjectMapperFactory;
import org.ektorp.util.Assert;
import org.ektorp.util.Documents;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

@RunWith(MockitoJUnitRunner.class)
public class AuditTest {

    @Mock
    private HttpClient client;
    
    private TestRepository repo;
    private ObjectMapper mapper;

    // default auditor name
    private String auditorName = "";

    protected void setAuditorName(String name ){
        this.auditorName = name;
    }
    /**
     * Configures the audit handler in the AuditAspect
     */
    private void configureAuditHandler(){
        AuditAspect aspect = Aspects.aspectOf(AuditAspect.class);
        
        // specify the handler
        AuditingHandler handler = new AuditingHandler();
        aspect.setAuditingHandler(handler);

        // specify the auditor for the handler
        AuditorAware<String> auditor = new AuditorAware<String>() {
            public String getCurrentAuditor() {
                return auditorName;
            }
        };
        handler.setAuditorAware(auditor);
    }
    
    @Before
    public void setup() throws MalformedURLException{
        // initialize the auditor aspect
        configureAuditHandler();
        
        // setup the mock client
        when(client.head("/testdb/")).thenReturn(HttpResponseStub.valueOf(201, "{\"ok\": true}"));
        when(client.put(anyString(), anyString())).thenAnswer(new Answer<HttpResponse>() {
            @Override
            public HttpResponse answer(InvocationOnMock invocation) throws Throwable {
              Object[] args = invocation.getArguments();
              String json = (String)args[1];
              Object o = mapper.readValue(json, TestDocument.class);
              
              // Get the doc id
              String id = Documents.getId(o);
              Assert.hasText(id, "document id cannot be empty");
              
              // return a created string
              return HttpResponseStub.valueOf( 201, "{\"ok\": true, \"id\": \"" + id + "\", \"rev\": \""+ id + "\"}");
            }
          });
        
        when(client.post(anyString(), anyString())).thenAnswer(new Answer<HttpResponse>() {
            @Override
            public HttpResponse answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                String json = (String)args[1];
                Object o = mapper.readValue(json, TestDocument.class);
                // generate a random id and rev if object doesn't already contain one
                String id = Documents.getId(o);
                if( StringUtils.isEmpty(id) )
                    id = RandomStringUtils.randomAlphanumeric(5);
                
                // return a success string
                return HttpResponseStub.valueOf( 200, "{\"ok\": true, \"id\": \"" + id + "\", \"rev\": \""+ id + "\"}");
            }
        });
            
        // configure the object mapper to register the Joda converters automatically
        StdObjectMapperFactory omf = new StdObjectMapperFactory(){
            @Override
            public synchronized ObjectMapper createObjectMapper() {
                ObjectMapper om = super.createObjectMapper();
                om.registerModule(new JodaModule());
                return om;
            }

            @Override
            public ObjectMapper createObjectMapper(CouchDbConnector connector) {
                ObjectMapper om = super.createObjectMapper();
                om.registerModule(new JodaModule());
                return om;
            }
        };
        

        // init the repo
        CouchDbInstance instance = new StdCouchDbInstance(client, omf);
        CouchDbConnector connector = instance.createConnector("testdb/",  true);
        mapper = omf.createObjectMapper();
        
        repo = new TestRepository(TestDocument.class,  connector);
    }
    
    
    
    
    @Test
    public void testAllAuditFields() {
        DateTime now = new DateTime();
        String auditorName = RandomStringUtils.randomAlphanumeric(10);
        setAuditorName( auditorName );

        TestDocument doc = new TestDocument();
        doc.setValue("Some Test Data");
        repo.add(doc);
        
        AuditableBean auditableDoc = (AuditableBean)doc;

        DateTime createdDate = auditableDoc.getCreatedDate();
        assertThat(auditableDoc.getCreatedDate(), greaterThanOrEqualTo(now));
        assertThat(auditableDoc.getLastModifiedDate(), greaterThanOrEqualTo(now));
        assertThat((String)auditableDoc.getCreatedBy(), equalTo(auditorName));
        assertThat((String)auditableDoc.getLastModifiedBy(), equalTo(auditorName));
        
        // update the document as a different user
        String updateAuditor = RandomStringUtils.randomAlphanumeric(10);
        setAuditorName( updateAuditor);
        doc.setValue("New Data");
        repo.update(doc);
        
        assertThat(auditableDoc.getCreatedDate(), equalTo(createdDate));
        assertThat(auditableDoc.getLastModifiedDate(), greaterThan(createdDate));
        assertThat((String)auditableDoc.getCreatedBy(), equalTo(auditorName));
        assertThat((String)auditableDoc.getLastModifiedBy(), equalTo(updateAuditor));
        
    }

}
