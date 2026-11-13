package com.premiumorcracked.sync;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.premiumorcracked.api.AuthType;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SyncCodec {
    public static final int PROTOCOL_VERSION = 1;
    private static final byte[] MAGIC = {'P', 'O', 'C', '1'};
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    public byte[] encode(@NotNull SyncMessage message) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream data = new DataOutputStream(out);
            data.write(MAGIC);
            data.writeByte(PROTOCOL_VERSION);
            data.writeByte(message.id());
            switch (message) {
                case SyncMessage.ConfigSync configSync -> writeUtf(data, gson.toJson(configSync.snapshot()));
                case SyncMessage.PlayerSync playerSync -> writePlayer(data, playerSync.record());
                case SyncMessage.PlayerBulkSync bulkSync -> {
                    data.writeInt(bulkSync.records().size());
                    for (PlayerAuthRecord record : bulkSync.records()) {
                        writePlayer(data, record);
                    }
                }
                case SyncMessage.RequestSync requestSync -> writeUtf(data, requestSync.serverName());
            }
            data.flush();
            return out.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to encode sync packet", ex);
        }
    }

    public @NotNull SyncMessage decode(byte[] payload) throws SyncProtocolException {
        if (payload == null || payload.length < 6) {
            throw new SyncProtocolException("Packet is too short");
        }
        try (DataInputStream data = new DataInputStream(new ByteArrayInputStream(payload))) {
            byte[] magic = data.readNBytes(4);
            if (magic.length != 4 || magic[0] != MAGIC[0] || magic[1] != MAGIC[1] || magic[2] != MAGIC[2] || magic[3] != MAGIC[3]) {
                throw new SyncProtocolException("Unknown packet magic");
            }
            int version = data.readUnsignedByte();
            if (version != PROTOCOL_VERSION) {
                throw new SyncProtocolException("Unsupported protocol version " + version);
            }
            int id = data.readUnsignedByte();
            return switch (id) {
                case SyncMessage.CONFIG -> {
                    try {
                        ConfigSnapshot snapshot = gson.fromJson(readUtf(data), ConfigSnapshot.class);
                        if (snapshot == null) {
                            throw new SyncProtocolException("Config snapshot is empty");
                        }
                        yield new SyncMessage.ConfigSync(snapshot);
                    } catch (JsonParseException ex) {
                        throw new SyncProtocolException("Invalid config snapshot JSON", ex);
                    }
                }
                case SyncMessage.PLAYER -> new SyncMessage.PlayerSync(readPlayer(data));
                case SyncMessage.PLAYER_BULK -> {
                    int count = data.readInt();
                    if (count < 0 || count > 10_000) {
                        throw new SyncProtocolException("Invalid bulk player count: " + count);
                    }
                    List<PlayerAuthRecord> records = new ArrayList<>(count);
                    for (int i = 0; i < count; i++) {
                        records.add(readPlayer(data));
                    }
                    yield new SyncMessage.PlayerBulkSync(List.copyOf(records));
                }
                case SyncMessage.REQUEST_SYNC -> new SyncMessage.RequestSync(readUtf(data));
                default -> throw new SyncProtocolException("Unknown packet id " + id);
            };
        } catch (IOException ex) {
            throw new SyncProtocolException("Truncated sync packet", ex);
        }
    }

    private static void writePlayer(DataOutputStream data, PlayerAuthRecord record) throws IOException {
        data.writeLong(record.uniqueId().getMostSignificantBits());
        data.writeLong(record.uniqueId().getLeastSignificantBits());
        writeUtf(data, record.username());
        writeUtf(data, record.type().name());
    }

    private static PlayerAuthRecord readPlayer(DataInputStream data) throws IOException, SyncProtocolException {
        UUID id = new UUID(data.readLong(), data.readLong());
        String username = readUtf(data);
        AuthType type = AuthType.fromKey(readUtf(data));
        if (username.isBlank()) {
            throw new SyncProtocolException("Player username is blank");
        }
        return new PlayerAuthRecord(id, username, type);
    }

    private static void writeUtf(DataOutputStream data, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        data.writeInt(bytes.length);
        data.write(bytes);
    }

    private static String readUtf(DataInputStream data) throws IOException, SyncProtocolException {
        int length = data.readInt();
        if (length < 0 || length > 16_384) {
            throw new SyncProtocolException("Invalid UTF length: " + length);
        }
        return new String(data.readNBytes(length), StandardCharsets.UTF_8);
    }
}
