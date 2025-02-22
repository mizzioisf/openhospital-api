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
package org.isf.users.dto;

import jakarta.validation.constraints.NotNull;

import org.isf.usergroups.dto.UserGroupDTO;

import io.swagger.v3.oas.annotations.media.Schema;

public class UserDTO {

	@NotNull
	@Schema(description = "The username (must be unique)", example = "John Doe", maxLength = 50)
	private String userName;

	@NotNull
	@Schema(description = "The user's group")
	private UserGroupDTO userGroupName;

	@NotNull
	@Schema(description = "The user's password", example = "21@U2g423", maxLength = 50)
	private String passwd;

	@Schema(description = "The user's description", example = "Lab chief technician", maxLength = 128)
	private String desc;
	@Schema(description = "Whether the user has been soft deleted or not", example = "false")
	private boolean deleted;

	public UserDTO() {
	}

	public UserDTO(String userName, UserGroupDTO userGroupName, String passwd, String desc) {
		this(userName, userGroupName, passwd, desc, false);
	}

	public UserDTO(String userName, UserGroupDTO userGroupName, String passwd, String desc, boolean deleted) {
		this.userName = userName;
		this.userGroupName = userGroupName;
		this.passwd = passwd;
		this.desc = desc;
		this.deleted = deleted;
	}

	public String getUserName() {
		return this.userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public UserGroupDTO getUserGroupName() {
		return this.userGroupName;
	}
	public void setUserGroupName(UserGroupDTO userGroupName) {
		this.userGroupName = userGroupName;
	}
	public String getPasswd() {
		return this.passwd;
	}
	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}
	public String getDesc() {
		return this.desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}

	public boolean isDeleted() {
		return deleted;
	}
	
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
}
