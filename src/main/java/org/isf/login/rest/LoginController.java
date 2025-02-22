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
package org.isf.login.rest;

import java.time.LocalDateTime;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.isf.login.dto.LoginRequest;
import org.isf.login.dto.LoginResponse;
import org.isf.login.dto.TokenRefreshRequest;
import org.isf.menu.manager.UserBrowsingManager;
import org.isf.menu.model.User;
import org.isf.security.CustomAuthenticationManager;
import org.isf.security.jwt.TokenProvider;
import org.isf.security.jwt.TokenValidationResult;
import org.isf.sessionaudit.manager.SessionAuditManager;
import org.isf.sessionaudit.model.SessionAudit;
import org.isf.sessionaudit.model.UserSession;
import org.isf.shared.exceptions.OHAPIException;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.model.OHExceptionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Login")
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class LoginController {

	private final HttpSession httpSession;

	private final SessionAuditManager sessionAuditManager;

	private final TokenProvider tokenProvider;

	private final CustomAuthenticationManager authenticationManager;

	private final UserBrowsingManager userManager;

	private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

	public LoginController(
		HttpSession httpSession,
		SessionAuditManager sessionAuditManager,
		TokenProvider tokenProvider,
		CustomAuthenticationManager authenticationManager,
		UserBrowsingManager userManager
	) {
		this.httpSession = httpSession;
		this.sessionAuditManager = sessionAuditManager;
		this.tokenProvider = tokenProvider;
		this.authenticationManager = authenticationManager;
		this.userManager = userManager;
	}

	@PostMapping(value = "/auth/login")
	public LoginResponse authenticateUser(
		@Valid @RequestBody LoginRequest loginRequest
	) {
		Authentication authentication = authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = tokenProvider.generateJwtToken(authentication, false); // use the shorter validity
		String refreshToken = tokenProvider.generateRefreshToken(authentication);

		String userDetails = (String) authentication.getPrincipal();
		User user;
		try {
			user = userManager.getUserByName(loginRequest.getUsername());
			UserSession.setUser(user);
		} catch (OHServiceException e) {
			e.printStackTrace();
		}

		try {
			this.httpSession.setAttribute("sessionAuditId",
				sessionAuditManager.newSessionAudit(new SessionAudit(userDetails, LocalDateTime.now(), null)));
		} catch (OHServiceException e1) {
			LOGGER.error("Unable to log user login in the session_audit table");
		}

		return new LoginResponse(jwt, refreshToken, userDetails);
	}

	@PostMapping("/auth/refresh-token")
	public LoginResponse refreshToken(@RequestBody TokenRefreshRequest request) throws OHAPIException {
		String refreshToken = request.getRefreshToken();

		try {
			if (tokenProvider.validateToken(refreshToken) == TokenValidationResult.VALID) {
				String username = tokenProvider.getUsernameFromToken(refreshToken);
				Authentication authentication = tokenProvider.getAuthenticationByUsername(username);
				String newAccessToken = tokenProvider.generateJwtToken(authentication, false);
				String newRefreshToken = tokenProvider.generateRefreshToken(authentication);

				return new LoginResponse(newAccessToken, newRefreshToken, username);
			} else {
				throw new OHAPIException(new OHExceptionMessage("Invalid Refresh Token"));
			}
		} catch (JwtException e) {
			throw new OHAPIException(new OHExceptionMessage("Refresh token expired or invalid"));
		}
	}
}
