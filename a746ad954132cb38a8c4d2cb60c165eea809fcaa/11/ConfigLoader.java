package com.formulasearchengine.mathosphere.pomlp.util.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Properties;

public class ConfigLoader {

    private static final Logger LOG = LogManager.getLogger( ConfigLoader.class.getName() );

    public static final String GITHUB_URL           = "github";

    public static final String GITHUB_REPO_NAME     = "github.repo.name";
    public static final String GITHUB_REPO_OWNER    = "github.repo.owner";
    public static final String GITHUB_REPO_PATH     = "github.repo.subpath";

    public static final String GOULDI_MAXIMUM_NUM   = "gouldi.max";
    public static final String GOULDI_LOCAL_PATH    = "gouldi.local";

    private static final String THIRDPARTY_PREFIX   = "thirdparty.";
    public static final String MATHEMATICAL         = THIRDPARTY_PREFIX + "mathematical";
    public static final String LATEX2MML            = THIRDPARTY_PREFIX + "latex2mathml";

    public static final Properties CONFIG = loadConfiguration();

    public static Properties loadConfiguration() {
        try {
            Path config = new PathBuilder()
                    .initResourcesPath()
                    .addSubPath("config.properties")
                    .build();
            return loadConfiguration( config );
        } catch ( FileNotFoundException e ){
            LOG.error("Cannot find config file.");
            return null;
        }
    }

    public static Properties loadConfiguration( Path configFile )
            throws FileNotFoundException
    {
        File config = configFile.toFile();
        if ( !config.exists() )
            throw new FileNotFoundException("Cannot find configuration file! " + configFile.toAbsolutePath().toString());

        Properties props = new Properties();
        try ( FileInputStream in = new FileInputStream(config) ) {
            props.load(in);
            return props;
        } catch ( IOException ioe ){
            LOG.error("Cannot load config file!", ioe);
            return null;
        }
    }
}
