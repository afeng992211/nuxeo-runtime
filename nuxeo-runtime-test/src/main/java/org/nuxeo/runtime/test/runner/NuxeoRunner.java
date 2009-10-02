package org.nuxeo.runtime.test.runner;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;


/**
 * jUnit4 runner that can Inject class into the test class
 * Injection is based on the Guice injection framework
 * @author dmetzler
 *
 */
public class NuxeoRunner extends BlockJUnit4ClassRunner {

    private static final Logger LOG = Logger.getLogger(NuxeoRunner.class);

    /**
     * Runtim harness that holds all the machinery
     */
    protected static RuntimeHarness harness = new RuntimeHarness();


    /**
     * Guice modules to create the injector
     */
    protected Module[] modules;

    /**
     * Guice injector
     */
    protected Injector injector;

    /**
     * Test class settings (annotations)
     */
    private Settings settings;


    /**
     * The running instance
     * FIXME: see if it is needed
     */
    private static NuxeoRunner currentInstance;


    public NuxeoRunner(Class<?> classToRun) throws InitializationError {
        this(classToRun, new RuntimeModule());
    }

    public NuxeoRunner(Class<?> classToRun, Module... modules)
            throws InitializationError {
        super(classToRun);

        //Create the Guice injector, based on modules
        this.modules = modules;
        this.injector = Guice.createInjector(modules);

        settings = new Settings(getDescription());
        currentInstance = this;

    }

    public void resetInjector() {
        this.injector = Guice.createInjector(modules);
    }



    @Override
    public Object createTest() {


        //Return a Guice injected test class
        return injector.getInstance(getTestClass().getJavaClass());
    }

    /**
     * Deploy bundles specified in the @Bundles annotation
     */
    private void deployTestClassBundles() {
        try {
            if (settings.getBundles().length > 0) {
                harness = getRuntimeHarness();
                for (String bundle : settings.getBundles()) {
                    harness.deployBundle(bundle);
                }
            }
        } catch (Exception e) {
            LOG.error("Unable to start bundles: " + settings.getBundles());
        }

    }

    @Override
    protected void validateZeroArgConstructor(List<Throwable> errors) {
        // Guice can inject constructors with parameters so we don't want this
        // method to trigger an error
    }

    /**
     * Returns the Guice injector.
     *
     * @return the Guice injector
     */
    protected Injector getInjector() {
        return injector;
    }

    /**
     * Can be useful in test class in order to reset things
     *
     * @return
     */
    public static NuxeoRunner getInstance() {
        return currentInstance;
    }

    public Settings getSettings() {
        return settings;
    }

    /**
     * Returns the harness used by the Nuxeo Runner (only used by the RTHarnessProvider)
     * @return
     * @throws Exception
     */
    static RuntimeHarness getRuntimeHarness() throws Exception {
        return harness;
    }


    @Override
    public void run(final RunNotifier notifier) {
        try {
            //Starts Nuxeo Runtim
            harness.start();

            //Deploy additional bundles
            deployTestClassBundles();

            //Runs the class
            super.run(notifier);

            //Stops the harness if needed
            if (harness.isStarted()) {
                harness.stop();
            }
        } catch (Exception e) {
            notifier.fireTestFailure(new Failure(getDescription(), e));
        }

    }
}
