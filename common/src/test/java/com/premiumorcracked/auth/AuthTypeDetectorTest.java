package com.premiumorcracked.auth;

import com.premiumorcracked.api.AuthType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuthTypeDetectorTest {
    private final AuthTypeDetector detector = new AuthTypeDetector();

    @Test
    void onlineModeIsPremium() {
        assertThat(detector.detect(true, UUID.randomUUID(), "Steve")).isEqualTo(AuthType.PREMIUM);
    }

    @Test
    void offlineUuidIsCracked() {
        UUID offline = OfflineUuids.fromName("Steve");
        assertThat(detector.detect(false, offline, "Steve")).isEqualTo(AuthType.CRACKED);
    }

    @Test
    void randomV4UuidWithoutOnlineModeIsTreatedAsPremium() {
        UUID premiumLike = UUID.fromString("069a79f4-44e9-4726-a5be-fca404e73beb");
        assertThat(premiumLike.version()).isEqualTo(4);
        assertThat(detector.detect(false, premiumLike, "Notch")).isEqualTo(AuthType.PREMIUM);
    }

    @Test
    void missingIdentityIsUnknown() {
        assertThat(detector.detect(false, null, "Steve")).isEqualTo(AuthType.UNKNOWN);
        assertThat(detector.detect(false, UUID.randomUUID(), null)).isEqualTo(AuthType.UNKNOWN);
    }
}
