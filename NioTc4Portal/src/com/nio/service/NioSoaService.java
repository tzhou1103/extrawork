package com.nio.service;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * 新增服务
 * 
 * @author zhoutong
 * @since 2018-12-12
 */
@WebService
public interface NioSoaService 
{	
	@WebMethod
	public abstract void reviseENGPart(@WebParam(name="engPartID")String engPartID, @WebParam(name="crID")String crID) throws Exception;
	
	@WebMethod
	public abstract void releaseUpdate(@WebParam(name="crID")String crID, @WebParam(name="pdfFileBase64")String pdfFileBase64, @WebParam(name="pdfFileName")String pdfFileName, @WebParam(name="status")String status) throws Exception;
	
	/**
	 * @return 新建的ENG Part对象ID
	 * @param partType 对象类型(2018-12-19新增)
	 * @throws Exception
	 */
	@WebMethod
	public abstract String createBOMOnlyEngPart(@WebParam(name="partType")String partType, @WebParam(name="owning_user")String owning_user, @WebParam(name="owning_group")String owning_group, @WebParam(name="crNum")String crNum) throws Exception;
	
	@WebMethod
	public abstract void reviseBOMOnlyEngPart(@WebParam(name="engPartID")String engPartID, @WebParam(name="crID")String crID, @WebParam(name="status")String status) throws Exception;

	/**
	 * @return 组和用户的JSON字符串
	 * @throws Exception
	 */
	@WebMethod
	public abstract String getGroupAndUser() throws Exception;
	
	@WebMethod
	public abstract boolean isENGPartRevRelatedCADPartRev(@WebParam(name="engPartID")String engPartID, @WebParam(name="crID")String crID) throws Exception; 
	
	@WebMethod
	public abstract void releaseCRUpdate(@WebParam(name="crID")String crID, @WebParam(name="status")String status) throws Exception;
}
