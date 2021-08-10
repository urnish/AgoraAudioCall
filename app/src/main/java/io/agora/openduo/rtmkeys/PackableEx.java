package io.agora.openduo.rtmkeys;

public interface PackableEx extends Packable {
    void unmarshal(ByteBuf in);
}
