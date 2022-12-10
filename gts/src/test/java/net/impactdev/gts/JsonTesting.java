package net.impactdev.gts;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.kyori.adventure.nbt.TagStringIO;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class JsonTesting {

    @Test
    public void nbt() {
        CompoundBinaryTag nbt = CompoundBinaryTag.builder()
                .put("id", StringBinaryTag.of(UUID.randomUUID().toString()))
                .putInt("random", new Random().nextInt())
                .build();

        TagStringIO io = TagStringIO.get();

        assertDoesNotThrow(() -> {
            String output = io.asString(nbt);
            System.out.println(output);
        });
    }

}
