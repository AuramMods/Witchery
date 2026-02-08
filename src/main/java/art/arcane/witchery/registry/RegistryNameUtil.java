package art.arcane.witchery.registry;

import java.util.Locale;

final class RegistryNameUtil {
    private RegistryNameUtil() {
    }

    static String normalizePath(String legacyName) {
        String normalized = legacyName.toLowerCase(Locale.ROOT);
        StringBuilder sb = new StringBuilder(normalized.length());
        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
            boolean valid = (c >= 'a' && c <= 'z')
                    || (c >= '0' && c <= '9')
                    || c == '/'
                    || c == '_'
                    || c == '-'
                    || c == '.';
            sb.append(valid ? c : '_');
        }
        return sb.toString();
    }
}
