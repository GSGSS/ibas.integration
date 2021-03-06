package org.colorcoding.ibas.integration.service.rest;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.colorcoding.ibas.bobas.common.Criteria;
import org.colorcoding.ibas.bobas.common.ICondition;
import org.colorcoding.ibas.bobas.common.ICriteria;
import org.colorcoding.ibas.bobas.common.IOperationResult;
import org.colorcoding.ibas.bobas.common.OperationMessage;
import org.colorcoding.ibas.bobas.common.OperationResult;
import org.colorcoding.ibas.bobas.data.FileData;
import org.colorcoding.ibas.bobas.data.emYesNo;
import org.colorcoding.ibas.bobas.i18n.I18N;
import org.colorcoding.ibas.bobas.repository.FileRepository;
import org.colorcoding.ibas.integration.MyConfiguration;
import org.colorcoding.ibas.integration.bo.integration.Action;
import org.colorcoding.ibas.integration.repository.FileRepositoryAction;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

@Path("action")
public class ActionService extends FileRepositoryAction {

	@GET
	@Path("{path: .*}")
	public void resource(@PathParam("path") String path, @QueryParam("token") String token,
			@Context HttpServletResponse response) {
		try {
			int index = path.indexOf("/");
			String group = null, file = null;
			if (index > 0) {
				group = path.substring(0, index);
				file = path.substring(index + 1);
			}
			if (group == null || group.isEmpty() || file == null || file.isEmpty()) {
				throw new WebApplicationException(404);
			}
			ICriteria criteria = new Criteria();
			ICondition condition = criteria.getConditions().create();
			condition.setAlias(FileRepositoryAction.CRITERIA_CONDITION_ALIAS_FOLDER);
			condition.setValue(group);
			condition = criteria.getConditions().create();
			condition.setAlias(FileRepositoryAction.CRITERIA_CONDITION_ALIAS_INCLUDE_SUBFOLDER);
			condition.setValue(emYesNo.YES);
			IOperationResult<FileData> operationResult = this.fetchFile(criteria, token);
			for (FileData item : operationResult.getResultObjects()) {
				String location = item.getLocation().substring(item.getLocation().indexOf(group))
						.replace(File.separator, "/");
				if (location.equalsIgnoreCase(path)) {
					// 设置内容类型
					response.setContentType(this.getContentType(item));
					// 写入响应输出流
					OutputStream os = response.getOutputStream();
					os.write(item.getFileBytes());
					os.flush();
					return;
				}
			}
			throw new WebApplicationException(404);
		} catch (WebApplicationException e) {
			throw e;
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	@POST
	@Path("uploadPackage")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public OperationResult<Action> uploadPackage(@FormDataParam("file") InputStream fileStream,
			@FormDataParam("file") FormDataContentDisposition fileDisposition, @QueryParam("token") String token) {
		try {
			FileData fileData = new FileData();
			fileData.setOriginalName(fileDisposition.getFileName());
			fileData.setStream(fileStream);
			FileRepository fileRepository = new FileRepository();
			fileRepository.setRepositoryFolder(MyConfiguration.getTempFolder());
			IOperationResult<FileData> opRsltFile = fileRepository.save(fileData);
			if (opRsltFile.getError() != null) {
				throw opRsltFile.getError();
			}
			fileData = opRsltFile.getResultObjects().firstOrDefault();
			if (fileData == null) {
				throw new Exception(I18N.prop("msg_ig_package_parsing_failure"));
			}
			return this.registerAction(new File(fileData.getLocation()), token);
		} catch (Exception e) {
			return new OperationResult<>(e);
		}
	}

	/**
	 * 删除集成动作组
	 * 
	 * @param id
	 *            动作标记
	 * @param token
	 *            口令
	 * @return 操作结果
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("deletePackage")
	public OperationMessage deletePackage(@QueryParam("group") String group, @QueryParam("token") String token) {
		return super.deletePackage(group, token);
	}

	// --------------------------------------------------------------------------------------------//
	/**
	 * 查询-集成动作
	 * 
	 * @param criteria
	 *            查询
	 * @param token
	 *            口令
	 * @return 操作结果
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("fetchAction")
	public OperationResult<Action> fetchAction(Criteria criteria, @QueryParam("token") String token) {
		return super.fetchAction(criteria, token);
	}

}
