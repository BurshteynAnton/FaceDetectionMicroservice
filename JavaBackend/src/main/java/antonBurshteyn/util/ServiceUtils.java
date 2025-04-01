package antonBurshteyn.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceUtils {

    public static String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "anonymous";
    }

    public static <T> T withUserContext(Supplier<T> supplier, String userEmail) {
        try (MDC.MDCCloseable ignored = MDC.putCloseable("userEmail", userEmail)) {
            return supplier.get();
        }
    }

    public static void logDuration(Logger logger, Instant start, String message, Object... args) {
        Instant end = Instant.now();
        long durationMs = Duration.between(start, end).toMillis();
        String formattedMessage = message + " in {} ms";
        Object[] finalArgs = concatArgs(args, durationMs);
        logger.info(formattedMessage, finalArgs);
    }

    private static Object[] concatArgs(Object[] args, long durationMs) {
        Object[] newArgs = new Object[args.length + 1];
        System.arraycopy(args, 0, newArgs, 0, args.length);
        newArgs[args.length] = durationMs;
        return newArgs;
    }
}