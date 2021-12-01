package com.twilio.type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;

import java.util.Objects;

/**
 * Subscribe Rule
 *
 * <p>
 * For more information see:
 * <a href=https://www.twilio.com/docs/video/api/track-subscriptions#specifying-sr>Specifying Subscribe Rules</a>
 * </p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class SubscribeRule implements Rule {

    private static final SubscribeRule subscribeAll = builder().withType(Type.INCLUDE).withAll().build();
    private static final SubscribeRule subscribeNone = builder().withType(Type.EXCLUDE).withAll().build();

    @JsonProperty("type")
    private final Type type;

    @JsonProperty("all")
    private final Boolean all;

    @JsonProperty("publisher")
    private final String publisher;

    @JsonProperty("track")
    private final String track;

    @JsonProperty("kind")
    private final Kind kind;

    @JsonProperty("priority")
    private final Priority priority;

    public SubscribeRule(@JsonProperty("type") final Type type,
            @JsonProperty("all") final Boolean all,
            @JsonProperty("publisher") final String publisher,
            @JsonProperty("track") final String track,
            @JsonProperty("kind") final Kind kind,
            @JsonProperty("priority") final Priority priority) {
        this.type = type;
        this.all = all;
        this.publisher = publisher;
        this.track = track;
        this.kind = kind;
        this.priority = priority;
    }

    public SubscribeRule() {
        this.type = null;
        this.all = null;
        this.publisher = null;
        this.track = null;
        this.kind = null;
        this.priority = null;
    }

    public static BuilderStart builder() {
        return new Builder();
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Boolean getAll() {
        return all;
    }

    @Override
    public String getPublisher() {
        return publisher;
    }

    @Override
    public String getTrack() {
        return track;
    }

    @Override
    public Kind getKind() {
        return kind;
    }

    public Priority getPriority() {
        return priority;
    }

    public static SubscribeRule all() {
        return subscribeAll;
    }

    public static SubscribeRule none() {
        return subscribeNone;
    }

    public interface BuilderStart {
        BuilderMiddle withType(final Type type);
    }

    public interface BuilderMiddle {
        BuilderMiddleBuild withPublisher(final String publisher);
        BuilderMiddleBuild withKind(final Kind kind);
        BuilderMiddleBuild withTrack(final String track);
        BuilderMiddleBuild withPriority(final Priority priority);
        BuilderBuild withAll();
    }

    public interface BuilderMiddleBuild {
        BuilderMiddleBuild withPublisher(final String publisher);
        BuilderMiddleBuild withKind(final Kind kind);
        BuilderMiddleBuild withTrack(final String track);
        BuilderMiddleBuild withPriority(final Priority priority);
        SubscribeRule build();
    }

    public interface BuilderBuild {
        SubscribeRule build();
    }

    public static class Builder implements
            BuilderStart,
            BuilderMiddle,
            BuilderMiddleBuild,
            BuilderBuild {
        private Type type;
        private Boolean all;
        private String publisher;
        private Kind kind;
        private String track;
        private Priority priority;

        private Builder() {
        }

        public BuilderMiddle withType(final Type type) {
            this.type = type;
            return this;
        }

        public BuilderBuild withAll() {
            this.all = true;
            return this;
        }
        public BuilderMiddleBuild withPublisher(final String publisher) {
            this.publisher = publisher;
            return this;
        }
        public BuilderMiddleBuild withKind(final Kind kind) {
            this.kind = kind;
            return this;
        }
        public BuilderMiddleBuild withTrack(final String track) {
            this.track = track;
            return this;
        }
        public BuilderMiddleBuild withPriority(final Priority priority) {
            this.priority = priority;
            return this;
        }

        private boolean hasOneFilter() {
            // at least one filter must be set
            return this.kind != null
                    || this.all != null
                    || this.track != null
                    || this.publisher != null
                    || this.priority != null;
        }

        private boolean hasType() {
            // every rule must have a type
            return this.type != null;
        }

        public SubscribeRule build() {
            if (!hasType()) {
                throw new IllegalArgumentException("Subscribe Rule must have a type");
            }
            if (!hasOneFilter()) {
                throw new IllegalArgumentException("Subscribe Rule must have at least one filter");
            }

            return new SubscribeRule(this.type, this.all, this.publisher, this.track, this.kind, this.priority);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubscribeRule)) return false;
        SubscribeRule that = (SubscribeRule) o;
        return getType() == that.getType() &&
                Objects.equals(getAll(), that.getAll()) &&
                Objects.equals(getPublisher(), that.getPublisher()) &&
                Objects.equals(getTrack(), that.getTrack()) &&
                getKind() == that.getKind() &&
                getPriority() == that.getPriority();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getAll(), getPublisher(), getTrack(), getKind(), getPriority());
    }
}
