package com.krakenrs.spade.logging;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Adapted from: https://stackoverflow.com/questions/11295764/slf4j-without-tostring
 * 
 * Custom logback {@link MessageConverter} to translate IR elements in the output to 
 * 
 * TODO: 
 * @author Bibl
 *
 */
public class IRMessageConverter extends MessageConverter {
    private static final String EMPTY = "";
    private static final String PLACEHOLDER = "{}";

    @Override
    public String convert(ILoggingEvent event) {
        StringBuilder message = new StringBuilder(event.getMessage());

        int argumentIndex = 0;
        int placeholderIndex = -1;
        while ((placeholderIndex = message.indexOf(PLACEHOLDER, placeholderIndex)) != -1
                && message.charAt(placeholderIndex - 1) != '\\') {
            String stringValue = valueOf(getArgument(event.getArgumentArray(), argumentIndex++));
            message.replace(placeholderIndex, placeholderIndex + PLACEHOLDER.length(), stringValue);
        }
        return message.toString();
    }

    /**
     * Return a {@link String} representation of the given object. It use convert
     * some complex objects as a single line string and use {@link String#valueOf(Object))}
     * for other objects.
     */
    private String valueOf(final Object object) {
//        if (object instanceof AuthenticatedUser)
//            return valueOf((AuthenticatedUser) object);

        return String.valueOf(object);
    }

//    private String valueOf(AuthenticatedUser user) {
//        return user.getUsername();
//    }

    /**
     * Retrieve an argument at a given position but avoid {@link ArrayIndexOutOfBoundsException}
     * by returning {@link PrettyMessageConverter#EMPTY} string when the index
     * is out of bounds.
     */
    private Object getArgument(Object[] arguments, int index) {
        return (index < arguments.length) ? arguments[index] : EMPTY;
    }
}
