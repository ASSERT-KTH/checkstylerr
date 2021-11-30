package com.healerjean.proj.util.validate;

import com.healerjean.proj.util.validate.anno.GreaterLess;
import com.healerjean.proj.util.validate.anno.NameInclude;
import com.healerjean.proj.util.validate.validator.GreaterLessValidator;
import com.healerjean.proj.util.validate.validator.NameIncludeValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.HibernateValidatorContext;
import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.internal.engine.ConfigurationImpl;
import org.hibernate.validator.internal.engine.ValidatorContextImpl;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.provider.MetaDataProvider;
import org.hibernate.validator.internal.metadata.provider.ProgrammaticMetaDataProvider;
import org.hibernate.validator.internal.metadata.provider.XmlMetaDataProvider;

import javax.validation.*;
import javax.validation.spi.ConfigurationState;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

/**
 * @author HealerJean
 * @ClassName CustomValidatorFactoryImpl
 * @date 2019/8/9  14:54.
 * @Description
 */
public class CustomValidatorFactoryImpl implements HibernateValidatorFactory {

    private final MessageInterpolator messageInterpolator;
    private final TraversableResolver traversableResolver;
    private final ConstraintValidatorFactory constraintValidatorFactory;
    private final BeanMetaDataManager metaDataManager;
    private final boolean failFast;


    public CustomValidatorFactoryImpl(ConfigurationState configurationState) {
        this.messageInterpolator = configurationState.getMessageInterpolator();
        this.constraintValidatorFactory = configurationState.getConstraintValidatorFactory();
        this.traversableResolver = configurationState.getTraversableResolver();

        ConstraintHelper constraintHelper = new ConstraintHelper();
        List<MetaDataProvider> metaDataProviders = newArrayList();
        List<Class<? extends ConstraintValidator<?, ?>>> constraintList = new ArrayList<>(1);
        constraintList.add(GreaterLessValidator.class);
        constraintHelper.addConstraintValidatorDefinition(GreaterLess.class, constraintList);
        constraintList = new ArrayList<>(1);
        constraintList.add(NameIncludeValidator.class);
        constraintHelper.addConstraintValidatorDefinition(NameInclude.class, constraintList);
        // HV-302; don't load XmlMappingParser if not necessary
        if (!configurationState.getMappingStreams().isEmpty()) {
            metaDataProviders.add(
                    new XmlMetaDataProvider(
                            constraintHelper, configurationState.getMappingStreams()
                    )
            );
        }

        Map<String, String> properties = configurationState.getProperties();

        boolean tmpFailFast = false;
        if (configurationState instanceof ConfigurationImpl) {
            ConfigurationImpl hibernateSpecificConfig = (ConfigurationImpl) configurationState;

            if (hibernateSpecificConfig.getProgrammaticMappings().size() > 0) {
                metaDataProviders.add(
                        new ProgrammaticMetaDataProvider(
                                constraintHelper,
                                hibernateSpecificConfig.getProgrammaticMappings()
                        )
                );
            }
            // check whether fail fast is programmatically enabled
            tmpFailFast = hibernateSpecificConfig.getFailFast();
        }
        tmpFailFast = checkPropertiesForFailFast(
                properties, tmpFailFast
        );
        this.failFast = tmpFailFast;
        metaDataManager = new BeanMetaDataManager(constraintHelper, metaDataProviders);
    }

    @Override
    public Validator getValidator() {
        return usingContext().getValidator();
    }

    @Override
    public MessageInterpolator getMessageInterpolator() {
        return messageInterpolator;
    }

    @Override
    public TraversableResolver getTraversableResolver() {
        return traversableResolver;
    }

    @Override
    public ConstraintValidatorFactory getConstraintValidatorFactory() {
        return constraintValidatorFactory;
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        if (HibernateValidatorFactory.class.equals(type)) {
            return type.cast(this);
        }
        throw new ValidationException(type.toString());
    }

    @Override
    public HibernateValidatorContext usingContext() {
        return new ValidatorContextImpl(
                constraintValidatorFactory,
                messageInterpolator,
                traversableResolver,
                metaDataManager,
                failFast
        );
    }

    private boolean checkPropertiesForFailFast(Map<String, String> properties, boolean programmaticConfiguredFailFast) {
        boolean failFast = programmaticConfiguredFailFast;
        String failFastPropValue = properties.get(HibernateValidatorConfiguration.FAIL_FAST);
        if (failFastPropValue != null) {
            boolean tmpFailFast = Boolean.valueOf(failFastPropValue);
            if (programmaticConfiguredFailFast && !tmpFailFast) {
                throw new ValidationException("快速返回失败");
            }
            failFast = tmpFailFast;
        }
        return failFast;
    }


}
