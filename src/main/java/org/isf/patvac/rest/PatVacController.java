/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2023 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
 *
 * Open Hospital is a free and open source software for healthcare data management.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * https://www.gnu.org/licenses/gpl-3.0-standalone.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.isf.patvac.rest;

import java.time.LocalDate;
import java.util.List;

import org.isf.patvac.dto.PatientVaccineDTO;
import org.isf.patvac.manager.PatVacManager;
import org.isf.patvac.mapper.PatVacMapper;
import org.isf.patvac.model.PatientVaccine;
import org.isf.shared.exceptions.OHAPIException;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.model.OHExceptionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Patient Vaccines")
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class PatVacController {

	private static final Logger LOGGER = LoggerFactory.getLogger(PatVacController.class);

	private final PatVacManager patVacManager;

	private final PatVacMapper mapper;

	public PatVacController(PatVacManager patVacManager, PatVacMapper patientVaccineMapper) {
		this.patVacManager = patVacManager;
		this.mapper = patientVaccineMapper;
	}

	/**
	 * Create a new {@link PatientVaccine}.
	 * @param patientVaccineDTO Patient Vaccine DTO
	 * @return {@code true} if the operation type has been stored, {@code false} otherwise.
	 * @throws OHServiceException When failed to create patient vaccine
	 */
	@PostMapping("/patientvaccines")
	@ResponseStatus(HttpStatus.CREATED)
	public PatientVaccineDTO newPatientVaccine(@RequestBody PatientVaccineDTO patientVaccineDTO) throws OHServiceException {
		LOGGER.info("Create patient vaccine {}", patientVaccineDTO.getCode());

		try {
			return mapper.map2DTO(patVacManager.newPatientVaccine(mapper.map2Model(patientVaccineDTO)));
		} catch (OHServiceException serviceException) {
			LOGGER.error("Patient vaccine not created.");
			throw new OHAPIException(new OHExceptionMessage("Patient vaccine not created."));
		}
	}

	/**
	 * Updates the specified {@link PatientVaccine}.
	 * @param patientVaccineDTO Patient Vaccine payload
	 * @return {@code true} if the operation type has been updated, {@code false} otherwise.
	 * @throws OHServiceException When failed to update patient vaccine
	 */
	@PutMapping("/patientvaccines/{code}")
	public PatientVaccineDTO updatePatientVaccinet(
		@PathVariable Integer code, @RequestBody PatientVaccineDTO patientVaccineDTO
	) throws OHServiceException {
		LOGGER.info("Update patientvaccines code: {}", patientVaccineDTO.getCode());
		PatientVaccine patvac = mapper.map2Model(patientVaccineDTO);
		patvac.setLock(patientVaccineDTO.getLock());
		try {
			return mapper.map2DTO(patVacManager.updatePatientVaccine(mapper.map2Model(patientVaccineDTO)));
		} catch (OHServiceException serviceException) {
			LOGGER.error("Patient vaccine not updated.");
			throw new OHAPIException(new OHExceptionMessage("Patient vaccine not updated."));
		}
	}

	/**
	 * Get all the {@link PatientVaccine}s for today or in the last week.
	 * @return the list of {@link PatientVaccine}s
	 * @throws OHServiceException When failed to get patient vaccines
	 */
	@GetMapping("/patientvaccines/week")
	public List<PatientVaccineDTO> getPatientVaccines(
		@RequestParam(required=false) Boolean oneWeek
	) throws OHServiceException {
		LOGGER.info("Get the all patient vaccine of to day or one week");
		if (oneWeek == null) {
			oneWeek = false;
		}

		return mapper.map2DTOList(patVacManager.getPatientVaccine(oneWeek));
	}

	/**
	 * Get all {@link PatientVaccine}s within {@code dateFrom} and {@code dateTo}.
	 * @return the list of {@link PatientVaccine}s
	 * @throws OHServiceException When failed to get patient vaccines
	 */
	@GetMapping("/patientvaccines/filter")
	public List<PatientVaccineDTO> getPatientVaccinesByDatesRanges(
		@RequestParam String vaccineTypeCode,
		@RequestParam String vaccineCode,
		@RequestParam LocalDate dateFrom,
		@RequestParam LocalDate dateTo,
		@RequestParam char sex,
		@RequestParam int ageFrom,
		@RequestParam int ageTo
	) throws OHServiceException {
		LOGGER.info("filter patient vaccine by dates ranges");

		return mapper.map2DTOList(patVacManager.getPatientVaccine(
			vaccineTypeCode, vaccineCode, dateFrom.atStartOfDay(), dateTo.atStartOfDay(), sex, ageFrom, ageTo
		));
	}

	/**
	 * Get the maximum progressive number within specified year or within current year if {@code 0}.
	 * @return {@code int} - the progressive number in the year
	 * @throws OHServiceException When failed to get the progressive number
	 */
	@GetMapping("/patientvaccines/progyear/{year}")
	public Integer getProgYear(@PathVariable int year) throws OHServiceException {
		LOGGER.info("Get progressive number within specified year");

		return patVacManager.getProgYear(year);
	}

	/**
	 * Delete {@link PatientVaccine} for specified code.
	 * @param code Patient vaccine code
	 * @return {@code true} if the {@link PatientVaccine} has been deleted, {@code false} otherwise.
	 * @throws OHServiceException When failed to delete patient vaccine
	 */
	@DeleteMapping("/patientvaccines/{code}")
	public boolean deletePatientVaccine(@PathVariable int code) throws OHServiceException {
		LOGGER.info("Delete patient vaccine code: {}", code);
		PatientVaccine patVac = new PatientVaccine();
		patVac.setCode(code);
		try {
			patVacManager.deletePatientVaccine(patVac);
			return true;
		} catch (OHServiceException serviceException) {
			throw new OHAPIException(new OHExceptionMessage("Patient vaccine not deleted."));
		}
	}
}
