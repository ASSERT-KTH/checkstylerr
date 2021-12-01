package com.formulasearchengine.mathosphere.pomlp.util;

import com.formulasearchengine.mathosphere.pomlp.convertor.POMConverter;
import com.formulasearchengine.mathosphere.pomlp.util.config.ConfigLoader;
import com.formulasearchengine.mathosphere.pomlp.xml.PomXmlWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * @author Andre Greiner-Petter
 */
public class POMLoader {

    private static final Logger LOG = LogManager.getLogger( POMLoader.class.getName() );

    public static final String PACKAGE_MLP = "mlp.";

    private Path MLP, referenceDir;

    private Class pomParser;
    private Class pomTaggedExpression;
    private Class mathTerm;

    private Object pomParserObject;
    private Object pomTaggedExpressionObject;
    //private Object mathTermObject;

    private boolean parsed = false;

    public POMLoader () throws RuntimeException {
        LOG.debug("Load paths from config file for POM-Tagger.");
        MLP = Paths.get(ConfigLoader.CONFIG.getProperty( ConfigLoader.POM_MLP ));
        referenceDir = Paths.get(ConfigLoader.CONFIG.getProperty( ConfigLoader.POM_REFERENCE_DIR ));

        if ( !Files.exists( MLP ) ){
            LOG.error( "Fail to find MLP: " + MLP.toAbsolutePath().toString() + " does not exist!" );
            throw new RuntimeException("Cannot load MLP");
        }
    }

    public void init() throws Exception {
        LOG.debug("Start to load classes from POM-Tagger.");
        File mlpJar = MLP.toFile();
        URLClassLoader urlCL = new URLClassLoader( new URL[]{ mlpJar.toURI().toURL() }, System.class.getClassLoader() );

        LOG.debug("Loading PomParser..");
        pomParser = urlCL.loadClass( PACKAGE_MLP + "PomParser" );
        LOG.debug("Loading PomTaggedExpression..");
        pomTaggedExpression = urlCL.loadClass( PACKAGE_MLP + "PomTaggedExpression" );
        LOG.debug("Loading MathTerm..");
        mathTerm = urlCL.loadClass( PACKAGE_MLP + "MathTerm" );

        LOG.debug("Instantiate Parser..");
        pomParserObject = pomParser.getDeclaredConstructor( Path.class ).newInstance( referenceDir );

        LOG.debug("Loading methods..");
        Methods.initAll( pomParser, pomTaggedExpression, mathTerm );
        parsed = false;
    }

    public Object invoke( Methods method, Object obj, Object... arguments )
            throws Exception
    {
        if ( arguments == null || arguments.length == 0 ){
            LOG.trace(obj.getClass().getName() + "." + method.internalname + "();");
            return method.method.invoke( obj );
        } else {
            LOG.trace(obj.getClass().getName() + "." + method.internalname + "(" + Arrays.toString(arguments) + ")");
            return method.method.invoke( obj, arguments );
        }
    }

    public Object parse( String latex ) throws Exception {
        LOG.debug("Invoke parse option.");
        Object obj = Methods.parse.method.invoke( pomParserObject, latex );
        LOG.debug("Successfully parsed.");
        LOG.debug("Returned Object is instance of PomTaggedExpression (should be true): "+ pomTaggedExpression.isInstance( obj ));
        pomTaggedExpressionObject = pomTaggedExpression.cast( obj );
        parsed = true;
        return pomTaggedExpressionObject;
    }

    public Object getParsedTree(){
        return pomTaggedExpressionObject;
    }

    public static void main(String[] args) throws Exception {
        POMConverter c = new POMConverter();
        c.init();
        String str = c.parseLatexMathToStringXML("\\frac{a}{v}");
        LOG.info(str);
    }

    /**
     * Methods enumeration.
     */
    public enum Methods {
        parse("parse", String.class ),
        pteGetComponents( "getComponents" ),
        pteGetTag("getTag"),
        pteGetSecondaryTags( "getSecondaryTags"),
        pteGetRoot("getRoot"),
        mtGetTag("getTag"),
        mtGetSecondaryTags( "getSecondaryTags"),
        mtGetTermText("getTermText"),
        mtIsEmpty("isEmpty");

        private String internalname;
        private Class[] args;

        private Method method;

        Methods( String internalname, Class... args ){
            this.internalname = internalname;
            this.args = args;
        }

        void init( Class clazz ) throws Exception {
            method = args == null ?
                    clazz.getMethod(internalname) : clazz.getMethod( internalname, args );
        }

        static void initAll( Class p, Class pte, Class mt ) throws Exception{
            for ( Methods m : Methods.values() ){
                if ( m.isPomTaggedExpression() )
                    m.init( pte );
                else if ( m.isMathTerm() )
                    m.init( mt );
                else m.init( p );
            }
        }

        boolean isMathTerm(){
            return name().startsWith("mt");
        }

        boolean isPomTaggedExpression(){
            return name().startsWith("pte");
        }

        public Method getMethod(){
            return method;
        }
    }
}
