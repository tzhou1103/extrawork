
package com.nio.service;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.nio.service package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ReleaseCRUpdate_QNAME = new QName("http://service.nio.com/", "releaseCRUpdate");
    private final static QName _ReviseBOMOnlyEngPart_QNAME = new QName("http://service.nio.com/", "reviseBOMOnlyEngPart");
    private final static QName _ReviseBOMOnlyEngPartResponse_QNAME = new QName("http://service.nio.com/", "reviseBOMOnlyEngPartResponse");
    private final static QName _ReleaseUpdate_QNAME = new QName("http://service.nio.com/", "releaseUpdate");
    private final static QName _Exception_QNAME = new QName("http://service.nio.com/", "Exception");
    private final static QName _CreateBOMOnlyEngPartResponse_QNAME = new QName("http://service.nio.com/", "createBOMOnlyEngPartResponse");
    private final static QName _IsENGPartRevRelatedCADPartRev_QNAME = new QName("http://service.nio.com/", "isENGPartRevRelatedCADPartRev");
    private final static QName _IsENGPartRevRelatedCADPartRevResponse_QNAME = new QName("http://service.nio.com/", "isENGPartRevRelatedCADPartRevResponse");
    private final static QName _ReviseENGPartResponse_QNAME = new QName("http://service.nio.com/", "reviseENGPartResponse");
    private final static QName _ReleaseUpdateResponse_QNAME = new QName("http://service.nio.com/", "releaseUpdateResponse");
    private final static QName _GetGroupAndUserResponse_QNAME = new QName("http://service.nio.com/", "getGroupAndUserResponse");
    private final static QName _CreateBOMOnlyEngPart_QNAME = new QName("http://service.nio.com/", "createBOMOnlyEngPart");
    private final static QName _ReleaseCRUpdateResponse_QNAME = new QName("http://service.nio.com/", "releaseCRUpdateResponse");
    private final static QName _ReviseENGPart_QNAME = new QName("http://service.nio.com/", "reviseENGPart");
    private final static QName _GetGroupAndUser_QNAME = new QName("http://service.nio.com/", "getGroupAndUser");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.nio.service
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Exception }
     * 
     */
    public Exception createException() {
        return new Exception();
    }

    /**
     * Create an instance of {@link ReviseENGPartResponse }
     * 
     */
    public ReviseENGPartResponse createReviseENGPartResponse() {
        return new ReviseENGPartResponse();
    }

    /**
     * Create an instance of {@link ReleaseUpdateResponse }
     * 
     */
    public ReleaseUpdateResponse createReleaseUpdateResponse() {
        return new ReleaseUpdateResponse();
    }

    /**
     * Create an instance of {@link CreateBOMOnlyEngPartResponse }
     * 
     */
    public CreateBOMOnlyEngPartResponse createCreateBOMOnlyEngPartResponse() {
        return new CreateBOMOnlyEngPartResponse();
    }

    /**
     * Create an instance of {@link IsENGPartRevRelatedCADPartRev }
     * 
     */
    public IsENGPartRevRelatedCADPartRev createIsENGPartRevRelatedCADPartRev() {
        return new IsENGPartRevRelatedCADPartRev();
    }

    /**
     * Create an instance of {@link IsENGPartRevRelatedCADPartRevResponse }
     * 
     */
    public IsENGPartRevRelatedCADPartRevResponse createIsENGPartRevRelatedCADPartRevResponse() {
        return new IsENGPartRevRelatedCADPartRevResponse();
    }

    /**
     * Create an instance of {@link ReviseBOMOnlyEngPart }
     * 
     */
    public ReviseBOMOnlyEngPart createReviseBOMOnlyEngPart() {
        return new ReviseBOMOnlyEngPart();
    }

    /**
     * Create an instance of {@link ReleaseCRUpdate }
     * 
     */
    public ReleaseCRUpdate createReleaseCRUpdate() {
        return new ReleaseCRUpdate();
    }

    /**
     * Create an instance of {@link ReleaseUpdate }
     * 
     */
    public ReleaseUpdate createReleaseUpdate() {
        return new ReleaseUpdate();
    }

    /**
     * Create an instance of {@link ReviseBOMOnlyEngPartResponse }
     * 
     */
    public ReviseBOMOnlyEngPartResponse createReviseBOMOnlyEngPartResponse() {
        return new ReviseBOMOnlyEngPartResponse();
    }

    /**
     * Create an instance of {@link GetGroupAndUser }
     * 
     */
    public GetGroupAndUser createGetGroupAndUser() {
        return new GetGroupAndUser();
    }

    /**
     * Create an instance of {@link GetGroupAndUserResponse }
     * 
     */
    public GetGroupAndUserResponse createGetGroupAndUserResponse() {
        return new GetGroupAndUserResponse();
    }

    /**
     * Create an instance of {@link CreateBOMOnlyEngPart }
     * 
     */
    public CreateBOMOnlyEngPart createCreateBOMOnlyEngPart() {
        return new CreateBOMOnlyEngPart();
    }

    /**
     * Create an instance of {@link ReviseENGPart }
     * 
     */
    public ReviseENGPart createReviseENGPart() {
        return new ReviseENGPart();
    }

    /**
     * Create an instance of {@link ReleaseCRUpdateResponse }
     * 
     */
    public ReleaseCRUpdateResponse createReleaseCRUpdateResponse() {
        return new ReleaseCRUpdateResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReleaseCRUpdate }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.nio.com/", name = "releaseCRUpdate")
    public JAXBElement<ReleaseCRUpdate> createReleaseCRUpdate(ReleaseCRUpdate value) {
        return new JAXBElement<ReleaseCRUpdate>(_ReleaseCRUpdate_QNAME, ReleaseCRUpdate.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReviseBOMOnlyEngPart }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.nio.com/", name = "reviseBOMOnlyEngPart")
    public JAXBElement<ReviseBOMOnlyEngPart> createReviseBOMOnlyEngPart(ReviseBOMOnlyEngPart value) {
        return new JAXBElement<ReviseBOMOnlyEngPart>(_ReviseBOMOnlyEngPart_QNAME, ReviseBOMOnlyEngPart.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReviseBOMOnlyEngPartResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.nio.com/", name = "reviseBOMOnlyEngPartResponse")
    public JAXBElement<ReviseBOMOnlyEngPartResponse> createReviseBOMOnlyEngPartResponse(ReviseBOMOnlyEngPartResponse value) {
        return new JAXBElement<ReviseBOMOnlyEngPartResponse>(_ReviseBOMOnlyEngPartResponse_QNAME, ReviseBOMOnlyEngPartResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReleaseUpdate }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.nio.com/", name = "releaseUpdate")
    public JAXBElement<ReleaseUpdate> createReleaseUpdate(ReleaseUpdate value) {
        return new JAXBElement<ReleaseUpdate>(_ReleaseUpdate_QNAME, ReleaseUpdate.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Exception }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.nio.com/", name = "Exception")
    public JAXBElement<Exception> createException(Exception value) {
        return new JAXBElement<Exception>(_Exception_QNAME, Exception.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateBOMOnlyEngPartResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.nio.com/", name = "createBOMOnlyEngPartResponse")
    public JAXBElement<CreateBOMOnlyEngPartResponse> createCreateBOMOnlyEngPartResponse(CreateBOMOnlyEngPartResponse value) {
        return new JAXBElement<CreateBOMOnlyEngPartResponse>(_CreateBOMOnlyEngPartResponse_QNAME, CreateBOMOnlyEngPartResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IsENGPartRevRelatedCADPartRev }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.nio.com/", name = "isENGPartRevRelatedCADPartRev")
    public JAXBElement<IsENGPartRevRelatedCADPartRev> createIsENGPartRevRelatedCADPartRev(IsENGPartRevRelatedCADPartRev value) {
        return new JAXBElement<IsENGPartRevRelatedCADPartRev>(_IsENGPartRevRelatedCADPartRev_QNAME, IsENGPartRevRelatedCADPartRev.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IsENGPartRevRelatedCADPartRevResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.nio.com/", name = "isENGPartRevRelatedCADPartRevResponse")
    public JAXBElement<IsENGPartRevRelatedCADPartRevResponse> createIsENGPartRevRelatedCADPartRevResponse(IsENGPartRevRelatedCADPartRevResponse value) {
        return new JAXBElement<IsENGPartRevRelatedCADPartRevResponse>(_IsENGPartRevRelatedCADPartRevResponse_QNAME, IsENGPartRevRelatedCADPartRevResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReviseENGPartResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.nio.com/", name = "reviseENGPartResponse")
    public JAXBElement<ReviseENGPartResponse> createReviseENGPartResponse(ReviseENGPartResponse value) {
        return new JAXBElement<ReviseENGPartResponse>(_ReviseENGPartResponse_QNAME, ReviseENGPartResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReleaseUpdateResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.nio.com/", name = "releaseUpdateResponse")
    public JAXBElement<ReleaseUpdateResponse> createReleaseUpdateResponse(ReleaseUpdateResponse value) {
        return new JAXBElement<ReleaseUpdateResponse>(_ReleaseUpdateResponse_QNAME, ReleaseUpdateResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetGroupAndUserResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.nio.com/", name = "getGroupAndUserResponse")
    public JAXBElement<GetGroupAndUserResponse> createGetGroupAndUserResponse(GetGroupAndUserResponse value) {
        return new JAXBElement<GetGroupAndUserResponse>(_GetGroupAndUserResponse_QNAME, GetGroupAndUserResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateBOMOnlyEngPart }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.nio.com/", name = "createBOMOnlyEngPart")
    public JAXBElement<CreateBOMOnlyEngPart> createCreateBOMOnlyEngPart(CreateBOMOnlyEngPart value) {
        return new JAXBElement<CreateBOMOnlyEngPart>(_CreateBOMOnlyEngPart_QNAME, CreateBOMOnlyEngPart.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReleaseCRUpdateResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.nio.com/", name = "releaseCRUpdateResponse")
    public JAXBElement<ReleaseCRUpdateResponse> createReleaseCRUpdateResponse(ReleaseCRUpdateResponse value) {
        return new JAXBElement<ReleaseCRUpdateResponse>(_ReleaseCRUpdateResponse_QNAME, ReleaseCRUpdateResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReviseENGPart }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.nio.com/", name = "reviseENGPart")
    public JAXBElement<ReviseENGPart> createReviseENGPart(ReviseENGPart value) {
        return new JAXBElement<ReviseENGPart>(_ReviseENGPart_QNAME, ReviseENGPart.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetGroupAndUser }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.nio.com/", name = "getGroupAndUser")
    public JAXBElement<GetGroupAndUser> createGetGroupAndUser(GetGroupAndUser value) {
        return new JAXBElement<GetGroupAndUser>(_GetGroupAndUser_QNAME, GetGroupAndUser.class, null, value);
    }

}
