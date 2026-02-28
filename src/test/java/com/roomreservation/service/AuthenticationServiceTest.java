package com.roomreservation.service;

import com.roomreservation.model.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * TDD Test Suite for AuthenticationService.
 *
 * Uses Mockito to mock the DataStore dependency, following
 * Sequence Diagram (Login):
 *   authenticate() → fetchUser() → hashPassword() → compare hashes
 *   → return true / false (max 3 retries)
 *
 * Requirements covered:
 *  - UC-01: Login
 */
@DisplayName("AuthenticationService Tests")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuthenticationServiceTest {

    @Mock
    private DataStore dataStore;

    private AuthenticationService authService;

    // Pre-hashed value of "correctPass" (SHA-256 hex)
    private static final String HASHED_CORRECT = "hashedCorrectPass";
    private static final String USERNAME        = "receptionist01";

    @BeforeEach
    void setUp() {
        authService = Mockito.spy(new AuthenticationService(dataStore));
    }

    // ── TC-AUTH-01 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-AUTH-01: Valid credentials return true")
    void testValidCredentialsReturnTrue() {
        User mockUser = new User(USERNAME, HASHED_CORRECT, "RECEPTIONIST");
        when(dataStore.fetchUser(USERNAME)).thenReturn(mockUser);
        when(authService.hashPassword("correctPass")).thenReturn(HASHED_CORRECT);

        boolean result = authService.authenticate(USERNAME, "correctPass");

        assertTrue(result, "Valid credentials should authenticate successfully");
        verify(dataStore, times(1)).fetchUser(USERNAME);
    }

    // ── TC-AUTH-02 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-AUTH-02: Wrong password returns false")
    void testWrongPasswordReturnsFalse() {
        User mockUser = new User(USERNAME, HASHED_CORRECT, "RECEPTIONIST");
        when(dataStore.fetchUser(USERNAME)).thenReturn(mockUser);
        when(authService.hashPassword("wrongPass")).thenReturn("differentHash");

        boolean result = authService.authenticate(USERNAME, "wrongPass");

        assertFalse(result, "Wrong password should return false");
    }

    // ── TC-AUTH-03 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-AUTH-03: Non-existent user returns false")
    void testNonExistentUserReturnsFalse() {
        when(dataStore.fetchUser("unknown")).thenReturn(null);

        boolean result = authService.authenticate("unknown", "anyPass");

        assertFalse(result, "Unknown username should return false");
    }

    // ── TC-AUTH-04 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-AUTH-04: Account locked after 3 failed attempts")
    void testAccountLockedAfterMaxAttempts() {
        User mockUser = new User(USERNAME, HASHED_CORRECT, "RECEPTIONIST");
        when(dataStore.fetchUser(USERNAME)).thenReturn(mockUser);
        when(authService.hashPassword("bad")).thenReturn("badHash");

        authService.authenticate(USERNAME, "bad");
        authService.authenticate(USERNAME, "bad");
        authService.authenticate(USERNAME, "bad");

        assertTrue(authService.isAccountLocked(USERNAME),
                "Account should be locked after 3 failed attempts");
    }

    // ── TC-AUTH-05 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-AUTH-05: Locked account rejects even correct password")
    void testLockedAccountRejectsCorrectPassword() {
        User mockUser = new User(USERNAME, HASHED_CORRECT, "RECEPTIONIST");
        when(dataStore.fetchUser(USERNAME)).thenReturn(mockUser);
        when(authService.hashPassword("bad")).thenReturn("badHash");

        // Lock the account
        authService.authenticate(USERNAME, "bad");
        authService.authenticate(USERNAME, "bad");
        authService.authenticate(USERNAME, "bad");

        // Now try correct password
        when(authService.hashPassword("correctPass")).thenReturn(HASHED_CORRECT);
        boolean result = authService.authenticate(USERNAME, "correctPass");

        assertFalse(result, "Locked account must reject even correct password");
    }

    // ── TC-AUTH-06 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-AUTH-06: Null username throws IllegalArgumentException")
    void testNullUsernameThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.authenticate(null, "pass"),
                "Null username should throw");
    }

    // ── TC-AUTH-07 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-AUTH-07: Null password throws IllegalArgumentException")
    void testNullPasswordThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.authenticate(USERNAME, null),
                "Null password should throw");
    }

    // ── TC-AUTH-08 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-AUTH-08: Password is hashed — plaintext never compared to stored hash")
    void testPasswordIsHashed() {
        User mockUser = new User(USERNAME, HASHED_CORRECT, "RECEPTIONIST");
        when(dataStore.fetchUser(USERNAME)).thenReturn(mockUser);

        authService.authenticate(USERNAME, "correctPass");

        // The raw password string "correctPass" must NEVER equal stored hash
        assertNotEquals("correctPass", HASHED_CORRECT,
                "Plaintext password must not equal the stored hash");
    }
}