package com.hlj.proj.utils.log4j2;

import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.Constants;
import org.apache.logging.log4j.util.StringBuilderFormattable;
import org.apache.logging.log4j.util.StringBuilders;

import java.util.Arrays;

/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName SensitiveParameterizedMessage
 * @date 2019/6/13  20:01.
 * @Description 脱敏消息
 */
public class SensitiveParameterizedMessage implements Message, StringBuilderFormattable {

    private static final int DEFAULT_STRING_BUILDER_SIZE = 255;

    /**
     * Prefix for recursion.
     */
    public static final String RECURSION_PREFIX = SensitiveParameterFormatter.RECURSION_PREFIX;
    /**
     * Suffix for recursion.
     */
    public static final String RECURSION_SUFFIX = SensitiveParameterFormatter.RECURSION_SUFFIX;

    /**
     * Prefix for errors.
     */
    public static final String ERROR_PREFIX = SensitiveParameterFormatter.ERROR_PREFIX;

    /**
     * Separator for errors.
     */
    public static final String ERROR_SEPARATOR = SensitiveParameterFormatter.ERROR_SEPARATOR;

    /**
     * Separator for error messages.
     */
    public static final String ERROR_MSG_SEPARATOR = SensitiveParameterFormatter.ERROR_MSG_SEPARATOR;

    /**
     * Suffix for errors.
     */
    public static final String ERROR_SUFFIX = SensitiveParameterFormatter.ERROR_SUFFIX;

    private static final long serialVersionUID = -665975803997290697L;

    private static final int HASHVAL = 31;

    private static ThreadLocal<StringBuilder> threadLocalStringBuilder = new ThreadLocal<>();

    private String messagePattern;
    private transient Object[] argArray;

    private String formattedMessage;
    private transient Throwable throwable;
    private int[] indices;
    private int usedCount;

    @Deprecated
    public SensitiveParameterizedMessage(final String messagePattern, final String[] arguments, final Throwable throwable) {
        this.argArray = arguments;
        this.throwable = throwable;
        init(messagePattern);
    }

    public SensitiveParameterizedMessage(final String messagePattern, final Object[] arguments, final Throwable throwable) {
        this.argArray = arguments;
        this.throwable = throwable;
        init(messagePattern);
    }

    public SensitiveParameterizedMessage(final String messagePattern, final Object... arguments) {
        this.argArray = arguments;
        init(messagePattern);
    }

    public SensitiveParameterizedMessage(final String messagePattern, final Object arg) {
        this(messagePattern, new Object[]{arg});
    }

    public SensitiveParameterizedMessage(final String messagePattern, final Object arg0, final Object arg1) {
        this(messagePattern, new Object[]{arg0, arg1});
    }

    private void init(final String messagePattern) {
        this.messagePattern = messagePattern;
        final int len = Math.max(1, messagePattern == null ? 0 : messagePattern.length() >> 1);
        this.indices = new int[len];
        final int placeholders = SensitiveParameterFormatter.countArgumentPlaceholders2(messagePattern, indices);
        initThrowable(argArray, placeholders);
        this.usedCount = Math.min(placeholders, argArray == null ? 0 : argArray.length);
    }

    private void initThrowable(final Object[] params, final int usedParams) {
        if (params != null) {
            final int argCount = params.length;
            if (usedParams < argCount && this.throwable == null && params[argCount - 1] instanceof Throwable) {
                this.throwable = (Throwable) params[argCount - 1];
            }
        }
    }

    /**
     * Returns the message pattern.
     * @return the message pattern.
     */
    @Override
    public String getFormat() {
        return messagePattern;
    }

    /**
     * Returns the message parameters.
     * @return the message parameters.
     */
    @Override
    public Object[] getParameters() {
        return argArray;
    }

    /**
     * Returns the Throwable that was given as the last argument, if any.
     * It will not survive serialization. The Throwable exists as part of the message
     * primarily so that it can be extracted from the end of the list of parameters
     * and then be added to the LogEvent. As such, the Throwable in the event should
     * not be used once the LogEvent has been constructed.
     *
     * @return the Throwable, if any.
     */
    @Override
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * Returns the formatted message.
     * @return the formatted message.
     */
    @Override
    public String getFormattedMessage() {
        if (formattedMessage == null) {
            final StringBuilder buffer = getThreadLocalStringBuilder();
            formatTo(buffer);
            formattedMessage = buffer.toString();
            StringBuilders.trimToMaxSize(buffer, Constants.MAX_REUSABLE_MESSAGE_SIZE);
        }
        return formattedMessage;
    }

    private static StringBuilder getThreadLocalStringBuilder() {
        StringBuilder buffer = threadLocalStringBuilder.get();
        if (buffer == null) {
            buffer = new StringBuilder(DEFAULT_STRING_BUILDER_SIZE);
            threadLocalStringBuilder.set(buffer);
        }
        buffer.setLength(0);
        return buffer;
    }

    @Override
    public void formatTo(final StringBuilder buffer) {
        if (formattedMessage != null) {
            buffer.append(formattedMessage);
        } else {
            if (indices[0] < 0) {
                SensitiveParameterFormatter.formatMessage(buffer, messagePattern, argArray, usedCount);
            } else {
                SensitiveParameterFormatter.formatMessage2(buffer, messagePattern, argArray, usedCount, indices);
            }
        }
    }


    public static String format(final String messagePattern, final Object[] arguments) {
        return SensitiveParameterFormatter.format(messagePattern, arguments);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final SensitiveParameterizedMessage that = (SensitiveParameterizedMessage) o;

        if (messagePattern != null ? !messagePattern.equals(that.messagePattern) : that.messagePattern != null) {
            return false;
        }
        if (!Arrays.equals(this.argArray, that.argArray)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = messagePattern != null ? messagePattern.hashCode() : 0;
        result = HASHVAL * result + (argArray != null ? Arrays.hashCode(argArray) : 0);
        return result;
    }


    public static int countArgumentPlaceholders(final String messagePattern) {
        return SensitiveParameterFormatter.countArgumentPlaceholders(messagePattern);
    }


    public static String deepToString(final Object o) {
        return SensitiveParameterFormatter.deepToString(o);
    }

    public static String identityToString(final Object obj) {
        return SensitiveParameterFormatter.identityToString(obj);
    }

    @Override
    public String toString() {
        return "ParameterizedMessage[messagePattern=" + messagePattern + ", stringArgs=" +
                Arrays.toString(argArray) + ", throwable=" + throwable + ']';
    }



}
