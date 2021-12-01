/*
 * All GTAS code is Copyright 2016, The Department of Homeland Security (DHS), U.S. Customs and Border Protection (CBP).
 * 
 * Please see LICENSE.txt for details.
 */
package gov.gtas.controller;

import gov.gtas.enumtype.Status;
import gov.gtas.json.JsonServiceResponse;
import gov.gtas.model.Attachment;
import gov.gtas.model.Passenger;
import gov.gtas.repository.*;
import gov.gtas.vo.passenger.AttachmentVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialException;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class UploadController {
	private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

	@Autowired
	private FlightRepository flightRespository;

	@Autowired
	private LookUpRepository lookupRepo;

	@Autowired
	private PassengerRepository passengerRepo;

	@Autowired
	private AttachmentRepository attRepo;


	@ResponseStatus(HttpStatus.OK)
	@PostMapping(value = "/uploadattachments")
	public @ResponseBody JsonServiceResponse uploadAttachments(@RequestParam("file") MultipartFile file,
			@RequestParam("username") String username, @RequestParam("password") String password,
			@RequestParam("desc") String desc, @RequestParam("paxId") String paxId)
			throws IOException, SerialException, SQLException {
		if (file.isEmpty()) {
			logger.info("empty file!");
			return new JsonServiceResponse(Status.FAILURE, "Failed to upload attachment: File was empty");
		}

		// Insert validation with uname/password here or currently logged in user with
		// session
		try {
			storeAttachmentToPassenger(file, desc, paxId);
		} catch (SQLException e) {
			return new JsonServiceResponse(Status.FAILURE, "Failed to upload attachment: SQLException ");
		}
		return new JsonServiceResponse(Status.SUCCESS, "Successfully uploaded attachment");
	}

	private void storeAttachmentToPassenger(MultipartFile file, String desc, String paxId)
			throws IOException, SerialException, SQLException {
		if (!file.isEmpty()) {
			// Build attachment to be added to pax
			Attachment attachment = new Attachment();
			attachment.setContentType(file.getContentType());
			attachment.setDescription(desc);
			attachment.setFilename(file.getOriginalFilename());
			attachment.setName(file.getName());
			byte[] bytes = file.getBytes();
			Blob blob = new javax.sql.rowset.serial.SerialBlob(bytes);
			attachment.setContent(blob);

			// Grab pax to add attachment to it
			Passenger pax = passengerRepo.findOne(Long.parseLong(paxId));
			/*
			 * if(pax.getAttachments() != null){ pax.getAttachments().add(attachment); }
			 * else{ Set<Attachment> tmpSet = new HashSet<Attachment>();
			 * tmpSet.add(attachment); pax.setAttachments(tmpSet); }
			 * passengerRepo.save(pax);
			 */
			attachment.setPassenger(pax);
			attRepo.save(attachment);
		}
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = "/getattachments", method = RequestMethod.GET)
	public @ResponseBody List<AttachmentVo> getAttachments(@RequestParam String paxId)
			throws SQLException {
		List<Attachment> returnSet = attRepo.findAllAttachmentsByPassengerId(Long.parseLong(paxId));

		List<AttachmentVo> attVoList = new ArrayList<AttachmentVo>();
		for (Attachment a : returnSet) {
			AttachmentVo attVo = new AttachmentVo();
			// Turn blob into byte[], as input stream is not serializable
			attVo.setContent(a.getContent().getBytes(1, (int) a.getContent().length()));
			attVo.setId(a.getId());
			attVo.setContentType(a.getContentType());
			attVo.setDescription(a.getDescription());
			attVo.setFilename(a.getFilename());
			// Drop blob from being held in memory after each set
			a.getContent().free();
			// Add to attVoList to be returned to front-end
			attVoList.add(attVo);
		}
		return attVoList;
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = "/deleteattachment", method = RequestMethod.GET)
	public @ResponseBody JsonServiceResponse deleteAttachment(@RequestParam String attachmentId) {
		JsonServiceResponse response;

		// Insure id is not null/empty/ or less than 1
		if (attachmentId == null || attachmentId.isEmpty() || Long.parseLong(attachmentId) < 1) {
			return new JsonServiceResponse(Status.FAILURE, "Invalid attachment id");
		}
		// Attempt removal
		attRepo.deleteById(Long.parseLong(attachmentId));

		return new JsonServiceResponse(Status.SUCCESS, "Successfully deleted attachment with id: " + attachmentId);
	}

}
