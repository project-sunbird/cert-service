package utils;

import play.libs.typedmap.TypedKey;

public class Attrs {
    public static final TypedKey<String> USER_ID = TypedKey.<String>create(JsonKey.USER_ID);
    public static final TypedKey<String> REQUESTED_FOR = TypedKey.<String>create(JsonKey.REQUESTED_FOR);
    public static final TypedKey<String> X_AUTH_TOKEN = TypedKey.<String>create(JsonKey.X_AUTH_TOKEN);
}
