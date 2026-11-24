package art.arcane.witchery.particle;

import art.arcane.witchery.registry.WitcheryRegistries;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;

public record CauldronParticleOptions(int color, Form form) implements ParticleOptions {
    public enum Form {
        BUBBLE, SPARK, CIRCLING_SPARK
    }

    private static final Codec<CauldronParticleOptions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(0, 0xFFFFFF).fieldOf("color").forGetter(CauldronParticleOptions::color),
            Codec.intRange(0, Form.values().length - 1).xmap(index -> Form.values()[index], Form::ordinal)
                    .fieldOf("form").forGetter(CauldronParticleOptions::form)
    ).apply(instance, CauldronParticleOptions::new));

    private static final Deserializer<CauldronParticleOptions> DESERIALIZER = new Deserializer<>() {
        @Override
        public CauldronParticleOptions fromCommand(ParticleType<CauldronParticleOptions> type, StringReader reader)
                throws CommandSyntaxException {
            reader.expect(' ');
            int color = IntegerArgumentType.integer(0, 0xFFFFFF).parse(reader);
            reader.expect(' ');
            int form = IntegerArgumentType.integer(0, Form.values().length - 1).parse(reader);
            return new CauldronParticleOptions(color, Form.values()[form]);
        }

        @Override
        public CauldronParticleOptions fromNetwork(ParticleType<CauldronParticleOptions> type, FriendlyByteBuf buffer) {
            return new CauldronParticleOptions(buffer.readInt(), buffer.readEnum(Form.class));
        }
    };

    public static ParticleType<CauldronParticleOptions> type() {
        return new ParticleType<>(false, DESERIALIZER) {
            @Override
            public Codec<CauldronParticleOptions> codec() {
                return CODEC;
            }
        };
    }

    @Override
    public ParticleType<CauldronParticleOptions> getType() {
        return WitcheryRegistries.CAULDRON_EFFECT.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeInt(color);
        buffer.writeEnum(form);
    }

    @Override
    public String writeToString() {
        return "witchery:cauldron_effect " + color + " " + form.ordinal();
    }
}
