package com.premiumorcracked.sync;

import com.premiumorcracked.api.AuthType;
import com.premiumorcracked.config.PluginConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SyncCodecTest {
    private final SyncCodec codec = new SyncCodec();

    @Test
    void configSnapshotRoundTripPreservesPrefixValues() throws Exception {
        ConfigSnapshot original = ConfigSnapshot.from(PluginConfiguration.defaults());
        original.prefixes.get("premium").value = "&7[&6PREM&7] ";
        SyncMessage decoded = codec.decode(codec.encode(new SyncMessage.ConfigSync(original)));
        assertThat(decoded).isInstanceOf(SyncMessage.ConfigSync.class);
        PluginConfiguration restored = ((SyncMessage.ConfigSync) decoded).snapshot().toConfiguration();
        assertThat(restored.prefix(AuthType.PREMIUM).value()).isEqualTo("&7[&6PREM&7] ");
        assertThat(restored.prefix(AuthType.CRACKED).value()).isEqualTo("&7[&fC&7] ");
        assertThat(restored.display().tablist()).isTrue();
        assertThat(restored.formatting().miniMessage()).isTrue();
    }

    @Test
    void playerRecordRoundTrip() throws Exception {
        PlayerAuthRecord record = new PlayerAuthRecord(UUID.randomUUID(), "Steve", AuthType.CRACKED);
        SyncMessage decoded = codec.decode(codec.encode(new SyncMessage.PlayerSync(record)));
        assertThat(decoded).isEqualTo(new SyncMessage.PlayerSync(record));
    }

    @Test
    void bulkAndRequestRoundTrip() throws Exception {
        List<PlayerAuthRecord> records = List.of(
                new PlayerAuthRecord(UUID.randomUUID(), "Alex", AuthType.PREMIUM),
                new PlayerAuthRecord(UUID.randomUUID(), "Steve", AuthType.CRACKED)
        );
        SyncMessage bulk = codec.decode(codec.encode(new SyncMessage.PlayerBulkSync(records)));
        assertThat(bulk).isEqualTo(new SyncMessage.PlayerBulkSync(records));
        SyncMessage request = codec.decode(codec.encode(new SyncMessage.RequestSync("lobby")));
        assertThat(request).isEqualTo(new SyncMessage.RequestSync("lobby"));
    }

    @Test
    void rejectsTruncatedPackets() {
        assertThatThrownBy(() -> codec.decode(new byte[]{1, 2, 3}))
                .isInstanceOf(SyncProtocolException.class);
        assertThatThrownBy(() -> codec.decode("XXXX".getBytes()))
                .isInstanceOf(SyncProtocolException.class);
    }
}
