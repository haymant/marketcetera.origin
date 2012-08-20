package org.marketcetera.core.module;

import java.io.File;
import java.net.URL;
import java.util.Date;

import org.marketcetera.api.attributes.ClassVersion;
import org.marketcetera.core.util.log.I18NBoundMessage1P;

/* $License$ */
/**
 * This provider supports multiple module instances that
 * are not auto-instantiated and need to manually started.
 * 
 * Moreover, this factory needs a complex set of parameters to
 * create new modules.
 *
 * @author anshul@marketcetera.com
 */
@ClassVersion("$Id: ComplexModuleFactory.java 82330 2012-04-10 16:29:13Z colin $")
public class ComplexModuleFactory extends ModuleFactory {
    public ComplexModuleFactory() {
        super(PROVIDER_URN, TestMessages.MULTIPLE_2_PROVIDER, true, false,
                String.class, File.class, URL.class, Date.class);
    }
    public MultipleModule create(Object... parameters)
            throws ModuleCreationException {
        ModuleURN u = new ModuleURN(PROVIDER_URN ,(String)parameters[0]);
        File f = (File) parameters[1];
        URL url = (URL) parameters[2];
        Date date = (Date) parameters[3];
        if(!f.isAbsolute()) {
            throw new ModuleCreationException(new I18NBoundMessage1P(
                    TestMessages.INCORRECT_FILE_PATH, f.toString()));
        }
        if(!"http".equals(url.getProtocol())) {
            throw new ModuleCreationException(new I18NBoundMessage1P(
                    TestMessages.INCORRECT_URL, url));
        }
        if(date == null) {
            throw new ModuleCreationException(new I18NBoundMessage1P(
                    TestMessages.DATE_NOT_SUPPLIED, date));
        }
        return new MultipleModule(u, false);
    }

    static final ModuleURN PROVIDER_URN = new ModuleURN("metc:test:multiple2");
}