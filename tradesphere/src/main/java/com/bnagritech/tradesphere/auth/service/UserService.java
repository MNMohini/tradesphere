package com.bnagritech.tradesphere.auth.service;

import com.bnagritech.tradesphere.auth.dto.CreateUserRequest;
import com.bnagritech.tradesphere.auth.dto.ImportUsersResponse;
import com.bnagritech.tradesphere.auth.dto.LoginHistoryResponse;
import com.bnagritech.tradesphere.auth.dto.MessageResponse;
import com.bnagritech.tradesphere.auth.dto.MoveUserRequest;
import com.bnagritech.tradesphere.auth.dto.RegisterRequest;
import com.bnagritech.tradesphere.auth.dto.TempPasswordResponse;
import com.bnagritech.tradesphere.auth.dto.UpdateUserRequest;
import com.bnagritech.tradesphere.auth.dto.UpdateUserStatusRequest;
import com.bnagritech.tradesphere.auth.dto.UserResponse;
import com.bnagritech.tradesphere.auth.dto.UserSearchRequest;
import com.bnagritech.tradesphere.auth.dto.UserSummaryResponse;
import com.bnagritech.tradesphere.auth.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * User management. A user can manage another user only if the other user is below them in the hierarchy
 * (an admin can manage everyone). Visibility follows the same rule, and ADMIN and NATIONAL_HEAD see everyone.
 */
public interface UserService {

    /** Creates a user with a generated temporary password. The creator must be the manager or above. */
    TempPasswordResponse createUser(CreateUserRequest request, User actor);

    /** An employee asks for an account. Works only when app.auth.self-registration=true. */
    MessageResponse selfRegister(RegisterRequest request);

    UserResponse getUserById(String userId, User actor);

    /** Users the caller may see, narrowed by the filters. */
    List<UserSummaryResponse> searchUsers(UserSearchRequest request, User actor);

    /** The caller's direct reports. */
    List<UserSummaryResponse> getMyTeam(User actor);

    UserResponse updateUser(String userId, UpdateUserRequest request, User actor);

    /** Approve or reject a registration, deactivate, suspend or reactivate. */
    UserResponse updateUserStatus(String userId, UpdateUserStatusRequest request, User actor);

    /** Moves a user and everyone below them under a different manager. */
    UserResponse moveUser(String userId, MoveUserRequest request, User actor);

    /** Gives the user a new temporary password and ends their sessions. */
    TempPasswordResponse resetUserPassword(String userId, User actor);

    /** Lets the user log in from a new phone when the single-device rule is on. */
    MessageResponse resetUserDevice(String userId, User actor);

    /** Creates users from an .xlsx file (max 500 rows). Each row succeeds or fails on its own. */
    ImportUsersResponse importUsers(MultipartFile file, User actor);

    List<LoginHistoryResponse> getLoginHistory(String userId, User actor);

    /** Entity to response (also used by the login code). */
    UserResponse toResponse(User user);

    /** Ends every session of a user on every device and rejects their access tokens issued so far. */
    void revokeAllSessions(User user);
}
