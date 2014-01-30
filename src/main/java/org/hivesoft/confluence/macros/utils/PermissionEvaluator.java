/**
 * Copyright (c) 2006-2014, Confluence Community
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hivesoft.confluence.macros.utils;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.user.User;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang3.StringUtils;
import org.hivesoft.confluence.macros.vote.model.Ballot;

import java.util.List;

public class PermissionEvaluator {

  final UserAccessor userAccessor;
  final UserManager userManager;
  final PermissionManager permissionManager;

  public PermissionEvaluator(UserAccessor userAccessor, UserManager userManager, PermissionManager permissionManager) {
    this.userAccessor = userAccessor;
    this.userManager = userManager;
    this.permissionManager = permissionManager;
  }

  public boolean canAttachFile(ContentEntityObject contentEntityObject) {
    return permissionManager.hasCreatePermission(getRemoteUser(), contentEntityObject, Attachment.class);
  }

  public User getRemoteUser() {
    return userAccessor.getUser(getRemoteUsername());
  }

  public String getRemoteUsername() {
    return userManager.getRemoteUsername();
  }

  public Boolean isPermissionListEmptyOrContainsGivenUser(List<String> listOfUsersOrGroups, String username) {
    if (StringUtils.isBlank(username)) {
      return Boolean.FALSE;
    }

    if (listOfUsersOrGroups.isEmpty() || listOfUsersOrGroups.contains(username)) {
      return Boolean.TRUE;
    }

    // 1.1.7.2: next try one of the entries is a group. Check whether the user is in this group!
    for (String permittedElement : listOfUsersOrGroups) {
      if (userAccessor.hasMembership(permittedElement.trim(), username)) {
        return Boolean.TRUE;
      }
    }
    return Boolean.FALSE;
  }

  public boolean getCanSeeVoters(String visibleVoters, boolean canSeeResults) {
    if (!canSeeResults || StringUtils.isBlank(visibleVoters))
      return false;
    return Boolean.parseBoolean(visibleVoters);
  }

  /**
   * Determine if a user is authorized to cast a vote, taking into account whether they are a voter (either explicitly or implicitly)
   * and whether or not they have already cast a vote. Only logged in users can vote.
   *
   * @param username the username of the user about to see the ballot.
   * @param ballot   the ballot that is about to be shown.
   * @return <code>true</code> if the user can cast a vote, <code>false</code> if they cannot.
   */
  public Boolean getCanVote(String username, Ballot ballot) {
    if (!TextUtils.stringSet(username)) {
      return Boolean.FALSE;
    }

    boolean isVoter = ballot.getConfig().getVoters().isEmpty() || ballot.getConfig().getVoters().contains(username);
    if (!isVoter) {
      // 1.1.7.2: next try one of the entries is a group. Check whether the user is in this group!
      for (String currentUser : ballot.getConfig().getVoters()) {
        if (userAccessor.hasMembership(currentUser.trim(), username)) {
          isVoter = true;
          break;
        }
      }

      if (!isVoter) // user is not permitted via groupName either
        return Boolean.FALSE;
    }

    return !ballot.getHasVoted(username) || ballot.getConfig().isChangeableVotes();
  }
}
