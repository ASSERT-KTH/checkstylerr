package com.bakdata.conquery.models.externalservice;

import static com.bakdata.conquery.io.result.arrow.ArrowUtil.NAMED_FIELD_DATE_DAY;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.StringJoiner;

import com.bakdata.conquery.io.cps.CPSBase;
import com.bakdata.conquery.io.cps.CPSType;
import com.bakdata.conquery.models.common.CDate;
import com.bakdata.conquery.models.common.daterange.CDateRange;
import com.bakdata.conquery.models.config.ConqueryConfig;
import com.bakdata.conquery.models.events.MajorTypeId;
import com.bakdata.conquery.models.forms.util.DateContext;
import com.bakdata.conquery.models.query.PrintSettings;
import com.bakdata.conquery.models.query.resultinfo.ResultInfo;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.arrow.vector.types.FloatingPointPrecision;
import org.apache.arrow.vector.types.pojo.ArrowType;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.FieldType;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, property = "type")
@CPSBase
public interface ResultType {

    default String printNullable(PrintSettings cfg, Object f) {
        if (f == null) {
            return "";
        }
        return print(cfg, f);
    }

    default String print(PrintSettings cfg, @NonNull Object f) {
        return f.toString();
    }

    Field getArrowFieldType(ResultInfo info, PrintSettings settings);

    String typeInfo();

    static ResultType resolveResultType(MajorTypeId majorTypeId) {
        switch (majorTypeId) {
            case STRING:
                return StringT.INSTANCE;
            case BOOLEAN:
                return BooleanT.INSTANCE;
            case DATE:
                return DateT.INSTANCE;
            case DATE_RANGE:
                return DateRangeT.INSTANCE;
            case INTEGER:
                return IntegerT.INSTANCE;
            case MONEY:
                return MoneyT.INSTANCE;
            case DECIMAL:
            case REAL:
                return NumericT.INSTANCE;
            default:
                throw new IllegalStateException(String.format("Invalid column type '%s'", majorTypeId));
        }
    }

    abstract static class PrimitiveResultType implements ResultType {
        @Override
        public String typeInfo() {
            return this.getClass().getAnnotation(CPSType.class).id();
        }

        @Override
        public String toString() {
            return typeInfo();
        }
    }

    @CPSType(id = "BOOLEAN", base = ResultType.class)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
	class BooleanT extends PrimitiveResultType {
		@Getter(onMethod_ = @JsonCreator)
		public static final BooleanT INSTANCE = new BooleanT();

        @Override
        public String print(PrintSettings cfg, Object f) {
            if (f instanceof java.lang.Boolean) {
                return (java.lang.Boolean) f ? "t" : "f";
            }
            throw new IllegalStateException("Expected a Boolean but got an " + f.getClass() + " (" + f + ")");
        }

        @Override
        public Field getArrowFieldType(ResultInfo info, PrintSettings settings) {
            return new Field(info.getUniqueName(settings), FieldType.nullable(ArrowType.Bool.INSTANCE), null);
        }
    }


    @CPSType(id = "INTEGER", base = ResultType.class)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
	class IntegerT extends PrimitiveResultType {
		@Getter(onMethod_ = @JsonCreator)
		public static final IntegerT INSTANCE = new IntegerT();

        @Override
        public String print(PrintSettings cfg, Object f) {
            if (cfg.isPrettyPrint()) {
                return cfg.getIntegerFormat().format(((Number) f).longValue());
            }
            return f.toString();
        }

        @Override
        public Field getArrowFieldType(ResultInfo info, PrintSettings settings) {
            return new Field(info.getUniqueName(settings), FieldType.nullable(new ArrowType.Int(32, true)), null);
        }
    }

    @CPSType(id = "NUMERIC", base = ResultType.class)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
	class NumericT extends PrimitiveResultType {
		@Getter(onMethod_ = @JsonCreator)
		public static final NumericT INSTANCE = new NumericT();

        @Override
        public String print(PrintSettings cfg, Object f) {
            if(cfg.isPrettyPrint()) {
                return cfg.getDecimalFormat().format(f);
            }
            return f.toString();
        }

        @Override
        public Field getArrowFieldType(ResultInfo info, PrintSettings settings) {
            return new Field(info.getUniqueName(settings), FieldType.nullable(new ArrowType.FloatingPoint(FloatingPointPrecision.DOUBLE)), null);
        }
    }

    @CPSType(id = "CATEGORICAL", base = ResultType.class)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
	class CategoricalT extends PrimitiveResultType {
		@Getter(onMethod_ = @JsonCreator)
		public static final CategoricalT INSTANCE = new CategoricalT();

        @Override
        public Field getArrowFieldType(ResultInfo info, PrintSettings settings) {
            return new Field(info.getUniqueName(settings), FieldType.nullable(new ArrowType.Utf8()), null);
        }
    }

    @CPSType(id = "RESOLUTION", base = ResultType.class)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
	class ResolutionT extends PrimitiveResultType {
		@Getter(onMethod_ = @JsonCreator)
        public static final ResolutionT INSTANCE = new ResolutionT();

        @Override
        public String print(PrintSettings cfg, Object f) {
            if (f instanceof DateContext.Resolution) {
                return ((DateContext.Resolution) f).toString(cfg.getLocale());
            }
            try {
                // If the object was parsed as a simple string, try to convert it to a
                // DateContextMode to get Internationalization
                return DateContext.Resolution.valueOf(f.toString()).toString(cfg.getLocale());
            } catch (Exception e) {
                throw new IllegalArgumentException(f + " is not a valid resolution.", e);
            }
        }

        @Override
        public Field getArrowFieldType(ResultInfo info, PrintSettings settings) {
            return new Field(info.getUniqueName(settings), FieldType.nullable(new ArrowType.Utf8()), null);
        }
    }

    @CPSType(id = "DATE", base = ResultType.class)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
	class DateT extends PrimitiveResultType {
		@Getter(onMethod_ = @JsonCreator)
        public static final DateT INSTANCE = new DateT();

        @Override
        public String print(PrintSettings cfg, @NonNull Object f) {
            if(!(f instanceof Number)) {
                throw new IllegalStateException("Expected an Number but got an '" + (f != null ? f.getClass().getName() : "no type") + "' with the value: " + f );
            }
            return CDate.toLocalDate(((Number)f).intValue()).toString();
        }

        @Override
        public Field getArrowFieldType(ResultInfo info, PrintSettings settings) {
            return NAMED_FIELD_DATE_DAY.apply(info.getUniqueName(settings));
        }
    }

    /**
     * A DateRange is provided by in a query result as two ints in a list, both standing for an epochday (see {@link LocalDate#toEpochDay()}).
     * The first int describes the included lower bound of the range. The second int descibes the included upper bound.
     */
    @CPSType(id = "DATE_RANGE", base = ResultType.class)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
	class DateRangeT extends PrimitiveResultType {
		@Getter(onMethod_ = @JsonCreator)
        public static final DateRangeT INSTANCE = new DateRangeT();

        @Override
        public String print(PrintSettings cfg, @NonNull Object f) {
            if(!(f instanceof List)) {
                throw new IllegalStateException(String.format("Expected a List got %s (Type: %s, as string: %s)", f, f != null ? f.getClass().getName() : "no type", f));
            }
            List list = (List) f;
            if(list.size() != 2) {
                throw new IllegalStateException("Expected a list with 2 elements, one min, one max. The list was: " + list);
            }
            return CDateRange.of((Integer) list.get(0), (Integer) list.get(1)).toString();
        }

        @Override
        public Field getArrowFieldType(ResultInfo info, PrintSettings settings) {
            return new Field(
                    info.getUniqueName(settings),
                    FieldType.nullable(ArrowType.Struct.INSTANCE),
                    List.of(
                            NAMED_FIELD_DATE_DAY.apply("min"),
                            NAMED_FIELD_DATE_DAY.apply("max")
                    ));
        }
    }

    @CPSType(id = "STRING", base = ResultType.class)
	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	class StringT extends PrimitiveResultType {
		@Getter(onMethod_ = @JsonCreator)
        public static final StringT INSTANCE = new StringT();

        @Override
        public Field getArrowFieldType(ResultInfo info, PrintSettings settings) {
            return new Field(info.getUniqueName(settings), FieldType.nullable(new ArrowType.Utf8()), null);
        }
    }

    @CPSType(id = "ID", base = ResultType.class)
	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	class IdT extends PrimitiveResultType {
		@Getter(onMethod_ = @JsonCreator)
        public static final IdT INSTANCE = new IdT();

        @Override
        public Field getArrowFieldType(ResultInfo info, PrintSettings settings) {
            return new Field(info.getUniqueName(settings), FieldType.nullable(new ArrowType.Utf8()), null);
        }
    }

    @CPSType(id = "MONEY", base = ResultType.class)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
	class MoneyT extends PrimitiveResultType {
        public static final int CURRENCY_DIGITS = ConqueryConfig.getInstance().getLocale().getCurrency().getDefaultFractionDigits();

        @Getter(onMethod_ = @JsonCreator)
		public static final MoneyT INSTANCE = new MoneyT();

        @Override
        public String print(PrintSettings cfg, Object f) {
            if (cfg.isPrettyPrint()) {
                return cfg.getDecimalFormat().format(new BigDecimal(((Number) f).longValue()).movePointLeft(CURRENCY_DIGITS));
            }
            return IntegerT.INSTANCE.print(cfg, f);
        }

        @Override
        public Field getArrowFieldType(ResultInfo info, PrintSettings settings) {
            return new Field(info.getUniqueName(settings), FieldType.nullable(new ArrowType.Int(32, true)), null);
        }
    }

    @CPSType(id = "LIST", base = ResultType.class)
	@Getter
	class ListT implements ResultType {
        @NonNull
        private final ResultType elementType;

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
		public ListT(ResultType elementType) {
			this.elementType = elementType;
		}

		@Override
        public String print(PrintSettings cfg, @NonNull Object f) {
            // Jackson deserializes collections as lists instead of an array, if the type is not given
            if(!(f instanceof List)) {
                throw new IllegalStateException(String.format("Expected a List got %s (Type: %s, as string: %s)", f, f != null ? f.getClass().getName() : "no type", f));
            }
            // Not sure if this escaping is enough
            String listDelimEscape = cfg.getListElementEscaper() + cfg.getListElementDelimiter();
            StringJoiner joiner = new StringJoiner(cfg.getListElementDelimiter(), cfg.getListPrefix(), cfg.getListPostfix());
            for(Object obj : (List) f) {
                joiner.add(elementType.print(cfg,obj).replace(cfg.getListElementDelimiter(), listDelimEscape));
            }
            return joiner.toString();
        }

        @Override
        public Field getArrowFieldType(ResultInfo info, PrintSettings settings) {

            // This is a workaround right now because currently the ArrowWriter does not write the underlying DataVector
            // of the List vector or the ArrowReader does not read it correctly. So we print the list as a flat String.
            return new Field(info.getUniqueName(settings), FieldType.nullable(new ArrowType.Utf8()), null);

            // This is the intended way of handling list values -> putting them in a ListVector
            //return new Field(info.getUniqueName(settings), FieldType.nullable(ArrowType.List.INSTANCE), List.of(new Field ("elem", elementType.getArrowFieldType(info, settings).getFieldType(),null)));
        }

        @Override
        public String typeInfo() {
            return this.getClass().getAnnotation(CPSType.class).id() + "[" + elementType.typeInfo() + "]";
        }

        @Override
        public String toString() {
            return typeInfo();
        }
    }
    static boolean isArray(Object obj)
    {
        return obj!=null && obj.getClass().isArray();
    }
}
