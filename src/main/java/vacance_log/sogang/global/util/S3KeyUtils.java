package vacance_log.sogang.global.util;

import java.net.URI;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class S3KeyUtils {

    private S3KeyUtils() {}

    public static String toKey(String bucket, String urlOrKey) {
        if (urlOrKey == null || urlOrKey.isBlank()) return urlOrKey;

        if (!urlOrKey.startsWith("http")) {
            return stripLeadingSlash(urlOrKey);
        }

        try {
            URI uri = URI.create(urlOrKey);
            String host = uri.getHost();
            String rawPath = uri.getPath() == null ? "" : uri.getPath();
            String path = stripLeadingSlash(rawPath);

            if (host != null && host.contains(bucket)) {
                return path;
            }

            if (path.startsWith(bucket + "/")) {
                return path.substring(bucket.length() + 1);
            }

            if (host != null && host.contains("amazonaws.com")) {
                return path;
            }

            return path;
        } catch (Exception e) {
            log.warn("⚠️ S3 URL parsing failed: {}. Returning stripped original.", urlOrKey);
            return stripLeadingSlash(urlOrKey);
        }
    }

    public static String extractFileName(String urlOrKey) {
        if (urlOrKey == null || urlOrKey.isBlank()) {
            return "unknown_file";
        }

        String path = urlOrKey;
        if (path.startsWith("http")) {
            try {
                path = URI.create(path).getPath();
            } catch (Exception e) {
            }
        }

        int lastSlashIndex = path.lastIndexOf('/');
        return (lastSlashIndex >= 0) ? path.substring(lastSlashIndex + 1) : path;
    }

    private static String stripLeadingSlash(String s) {
        if (s == null) return null;
        return s.startsWith("/") ? s.substring(1) : s;
    }
}
